package com.dpudov.answerphone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;

import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKUsersArray;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MessagesService extends Service {
    NotificationManager nM;
    private int NOTIFICATION = R.string.serviceStarted;
    private int[] checkedUsers;
    private int[] userId;
    String message;

    public MessagesService() {
    }

    @Override
    public void onCreate() {
        nM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();

    }

    @Override
    public void onDestroy() {
        nM.cancel(NOTIFICATION);
    }

    private void showNotification() {
        CharSequence text = getString(R.string.serviceStarted);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_answerphone_64px)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.app_name))
                .setContentText(text)
                .build();
        nM.notify(NOTIFICATION, notification);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        checkedUsers = bundle.getIntArray("userIds");
        VKSdk.wakeUpSession(getApplicationContext());
        try {
            getAndSendMessages();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void getAndSendMessages() throws Exception{

        //Запускаем поток, который проверяет новые сообщения. Если прилетает новое, читаем id отправителя. Затем шлём ему ответ.
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (hasConnection(getApplicationContext())){
                    userId = getMsg();
                    sendTo(userId);
                    try {
                        TimeUnit.MINUTES.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                stopSelf();
            }
        }).start();
    }

    private int[] getMsg() {
        VKRequest request = VKApi.messages().get(VKParameters.from("out", 0, "time_offset", 180));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKUsersArray messages = (VKUsersArray) response.parsedModel;
                userId = null;
                if (messages.size() > 0) {
                    // Пришло новое сообщение. Возвращаем true
                    ArrayList<Integer> userArr = new ArrayList<>();
                    ArrayList<Integer> userArrCopy = new ArrayList<>();
                    int firstId = messages.get(0).getId();
                    int id;
                    int c = 0;
                    userArr.add(0, firstId);
                    for (int i = 0; i < messages.size(); i++) {
                        id = messages.get(i).getId();
                        if (!(firstId == id)) {
                            c++;
                            userArr.add(c, id);
                            userArrCopy.add(c, id);
                        }
                    }
                    //проверка на соответствие с выбранными друзьями
                    for (int i = 0; i < userArr.size(); i++) {
                        for (int checkedUser : checkedUsers)
                            if (!(userArr.get(i) == checkedUser)) {
                                userArrCopy.remove(i);
                            }
                    }
//После всего создаем userIds, который проверяем на повторы и нули и закидываем в итог
                    int[] userIds = new int[userArrCopy.size()];
                    for (int i = 0; i < userArrCopy.size(); i++) {
                        userIds[i] = userArrCopy.get(i);
                    }
                    int counter = 0;
                    for (int userId1 : userIds) {
                        if (!(userId1 == 0)) {
                            counter++;
                        }
                    }
                    int count = 0;
                    userId = new int[counter];
                    for (int userId1 : userIds) {
                        if (!(userId1 == 0)) {
                            userId[count] = userId1;
                            count++;
                        }
                    }
                }

            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }

        });
        return userId;
    }

    public static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        return wifiInfo != null && wifiInfo.isConnected();
    }

    public void send(int userId) {

        message = getString(R.string.user_is_busy) + getString(R.string.defaultMsg);

        VKRequest requestSend = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, userId, VKApiConst.MESSAGE, message));
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

    public void sendTo(int[] userIds) {
        for (int userId : userIds) {
            send(userId);
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
