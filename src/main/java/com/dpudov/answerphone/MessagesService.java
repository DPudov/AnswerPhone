package com.dpudov.answerphone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.dpudov.answerphone.activity.MainActivity;
import com.dpudov.answerphone.data.AppUtils;
import com.dpudov.answerphone.data.DataManager;
import com.dpudov.answerphone.data.EventBus;
import com.dpudov.answerphone.data.network.LpServer;
import com.dpudov.answerphone.data.network.LpServerResponse;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiGetMessagesResponse;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKUsersArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.LinkedHashSet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MessagesService extends Service {
    private final int NOTIFICATION = R.string.serviceStarted;
    private NotificationManager nM;
    private int[] checkedUsers;
    private int[] userId;
    private int[] userIdCopy;
    private VKUsersArray users;
    private MyApplication myApplication;
    private DataManager dataManager;
    private LpServer mLongPollServer;
    private OkHttpClient mClient;
    private EventBus mEventBus;

    public MessagesService() {
    }

    private void startLongPoll() {
        dataManager.getLongPollServer(1);
        dataManager.setLongPollListener(new DataManager.LongPollListener() {
            @Override
            public void onResponseReceived(LpServer lpServer) {
                mLongPollServer = lpServer;
                connect(mLongPollServer);
            }
        });
    }

    private void connect(LpServer server) {
        String url = String.format(
                "http://%s?act=a_check&key=%s&ts=%s&wait=25&mode=2",
                server.getServer(),
                server.getKey(),
                server.getTs());
        Request request = new Request.Builder()
                .url(url)
                .build();
        mClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                startLongPoll();
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Reader reader = response.body().charStream();
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
                try {
                    Object object = AppUtils.parseResult(builder.toString());
                    if (object instanceof LpServerResponse) {
                        mLongPollServer.setTs(((LpServerResponse) object).getTs());
                        for (Object upd : ((LpServerResponse) object).getUpdates()) {
                            mEventBus.postOnMain(upd);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                connect(mLongPollServer);
            }
        });
    }

    @Override
    public void onCreate() {
        nM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        myApplication = (MyApplication) getApplication();
        users = myApplication.getFriendList();
    }

    @Override
    public void onDestroy() {
        nM.cancel(NOTIFICATION);

    }

    private void showNotification() {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        Bundle b = new Bundle();
        b.putInt("from", MyApplication.FROM_NOTIFICATION);
        notifyIntent.putExtras(b);
        String text = getString(R.string.serviceStarted);
        Notification notification;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_stat_name)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.app_name))
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .build();
        nM.notify(NOTIFICATION, notification);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        checkedUsers = bundle.getIntArray("userIds");
        int time = bundle.getInt("time");

        showNotification();
        //startLongPoll();
        doInThread(time);
        return startId;
    }

    private void doInThread(final int timeIn) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < timeIn * 2; i++) {
                        if (i == 0)
                            sentMsgToRecentSenders(timeIn + 2);
                        else {
                            sentMsgToRecentSenders(timeIn);
                        }
                        Thread.sleep(30000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sentMsgToRecentSenders(int time) {
//Получаем сообщения за последние time секунд
        VKRequest getMsg = VKApi.messages().get(VKParameters.from(VKApiConst.TIME_OFFSET, time * 60));
        getMsg.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKApiGetMessagesResponse getMessagesResponse = (VKApiGetMessagesResponse) response.parsedModel;
                VKList<VKApiMessage> list = getMessagesResponse.items;
                // Формируем лист с id авторов сообщений без повторений
                if (list.size() != 0) {
                    LinkedHashSet<Integer> authors = new LinkedHashSet<>();
                    for (VKApiMessage msg : list) {
                        // проверка. Если не прочитано и не из чата, добавляем
                        if ((!msg.read_state)&&(msg.chat_id==0))
                            authors.add(msg.user_id);
                    }
                    // конвертируем в массив
                    userId = new int[authors.size()];
                    Iterator<Integer> iterator = authors.iterator();
                    for (int i = 0; i < authors.size(); i++) {
                        userId[i] = iterator.next();
                    }

                    //сравниваем с выбранными друзьями
                    userIdCopy = new int[checkedUsers.length];
                    int c = 0;
                    for (int anUserId : userId) {
                        for (int checkedUser : checkedUsers) {
                            if (anUserId == checkedUser) {
                                userIdCopy[c] = anUserId;
                                c++;
                            }
                        }
                    }
                    if (c == 1)
                        send(userIdCopy[0]);
                    else
                        // Отправляем сообщение
                        sendTo(userIdCopy);
                }
            }

        });
    }

    private void send(final int userId) {
//метод для отправки сообщения user.
        if (userId != 0) {
            VKApiUserFull userFull = users.getById(userId);
            String name = userFull.first_name + " " + userFull.last_name;
            String message = getString(R.string.hi)
                    + name
                    + " "
                    + "! "
                    + getString(R.string.user_is_busy)
                    + getString(R.string.defaultMsg);
            VKRequest requestSend = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, userId, VKApiConst.MESSAGE, message));
            //noinspection EmptyMethod
            requestSend.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);

                }
            });
        }
    }

    private void sendTo(int[] userIds) {
        if (!(userIds == null)) {
            //метод для отправки сообщений нескольким юзерам
            for (int userId1 : userIds) {
                send(userId1);

            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}