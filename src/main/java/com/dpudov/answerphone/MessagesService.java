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
import com.google.gson.Gson;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiGetMessagesResponse;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class MessagesService extends Service {
    private final int NOTIFICATION = R.string.serviceStarted;
    private static final int MODE_PREFIX_AND_NAME = 1;
    private static final int MODE_PREFIX_NO_NAME = 2;
    private static final int MODE_NO_PREFIX_NAME = 3;
    private static final int MODE_NO_PREFIX_NO_NAME = 4;
    private NotificationManager nM;
    private int[] checkedUsers;
    private int[] userId;
    private int[] userIdCopy;
    private LpServer mLongPollServer;
    private HashSet<Integer> forgottenUsers;
    OkHttpClient mClient;
    Gson mGson;
    EventBus mEventBus;
    DataManager mDataManager;

    public MessagesService() {
    }

    private void startLongPoll() {
        mEventBus = new EventBus();
        mClient = new OkHttpClient();
        mLongPollServer = new LpServer();
        mGson = new Gson();
        mDataManager = new DataManager(mGson);
        mDataManager.getLongPollServer(1);
        mDataManager.setLongPollListener(new DataManager.LongPollListener() {
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
        forgottenUsers = new HashSet<>();
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
        boolean addName = bundle.getBoolean("addName");
        boolean addPrefix = bundle.getBoolean("addPrefix");
        int mode;
        if (addPrefix && addName) {
            mode = MODE_PREFIX_AND_NAME;
        } else if (addPrefix) {
            mode = MODE_PREFIX_NO_NAME;
        } else if (addName) {
            mode = MODE_NO_PREFIX_NAME;
        } else {
            mode = MODE_NO_PREFIX_NO_NAME;
        }
        showNotification();
        startLongPoll();
        doInThread(time, mode);
        return startId;
    }

    private void doInThread(final int timeIn, final int mode) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < timeIn * 2; i++) {
                        if (i == 0)
                            sentMsgToRecentSenders(timeIn + 2, mode);
                        else {
                            sentMsgToRecentSenders(timeIn, mode);
                        }
                        Thread.sleep(30000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sentMsgToRecentSenders(int time, final int mode) {
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
                        if ((!msg.read_state) && (msg.chat_id == 0))
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
                        send(userIdCopy[0], mode);
                    else
                        // Отправляем сообщение
                        sendTo(userIdCopy, mode);
                }
            }

        });
    }

    private void send(final int userId, int mode) {
//метод для отправки сообщения user.
        if (userId != 0 && isNotForgotten(userId)) {
            switch (mode) {
                //PREFIX AND NAME
                case 1:
                    VKRequest getUser = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, userId));
                    getUser.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            JSONObject object;
                            try {
                                object = response.json.getJSONArray("response").getJSONObject(0);
                                String name2 = object.getString("first_name") + " " + object.getString("last_name");
                                String message3 = getString(R.string.hi)
                                        + ", "
                                        + name2
                                        + " "
                                        + "! "
                                        + getString(R.string.user_is_busy)
                                        + getString(R.string.defaultMsg);
                                VKRequest requestSend0 = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, userId, VKApiConst.MESSAGE, message3));
                                //noinspection EmptyMethod
                                requestSend0.executeWithListener(new VKRequest.VKRequestListener() {
                                    @Override
                                    public void onComplete(VKResponse response) {
                                        super.onComplete(response);

//Forget user
                                        forgetUser(userId);
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    break;
                //PREFIX_NO_NAME
                case 2:
                    String msd1 = getString(R.string.hi)
                            + " "
                            + "! "
                            + getString(R.string.user_is_busy)
                            + getString(R.string.defaultMsg);
                    VKRequest requestSend2 = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, userId, VKApiConst.MESSAGE, msd1));
                    //noinspection EmptyMethod
                    requestSend2.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
//Forget user
                            forgetUser(userId);
                        }
                    });
                    break;
                //NAME_NO_PREFIX
                case 3:
                    VKRequest getUser1 = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, userId));
                    getUser1.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
                            JSONObject object1;
                            try {
                                object1 = response.json.getJSONArray("response").getJSONObject(0);
                                String name22 = object1.getString("first_name") + " " + object1.getString("last_name");
                                String message23 = getString(R.string.hi)
                                        + ", "
                                        + name22
                                        + " "
                                        + "! "
                                        + getString(R.string.user_is_busy)
                                        + getString(R.string.defaultMsg);
                                VKRequest requestSend01 = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, userId, VKApiConst.MESSAGE, message23));
                                //noinspection EmptyMethod
                                requestSend01.executeWithListener(new VKRequest.VKRequestListener() {
                                    @Override
                                    public void onComplete(VKResponse response) {
                                        super.onComplete(response);
                                        forgetUser(userId);
//Forget user
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    break;
                //NOTHING
                case 4:
                    String message4 = getString(R.string.hi)
                            + " "
                            + "! "
                            + getString(R.string.user_is_busy);

                    VKRequest requestSend4 = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, userId, VKApiConst.MESSAGE, message4));
                    //noinspection EmptyMethod
                    requestSend4.executeWithListener(new VKRequest.VKRequestListener() {
                        @Override
                        public void onComplete(VKResponse response) {
                            super.onComplete(response);
//Forget user
                            forgetUser(userId);
                        }
                    });
                    break;
            }
        }
    }

    private void sendTo(int[] userIds, int mode) {
        if (!(userIds == null)) {
            //метод для отправки сообщений нескольким юзерам
            for (int userId1 : userIds) {
                send(userId1, mode);

            }
        }
    }

    private void sendTo(String ids, String msg) {
        VKRequest requestSend = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_IDS, ids, VKApiConst.MESSAGE, msg));
        requestSend.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean isNotForgotten(int userId) {
        return !forgottenUsers.contains(userId);
    }

    private void forgetUser(int userId) {
        forgottenUsers.add(userId);
    }
}