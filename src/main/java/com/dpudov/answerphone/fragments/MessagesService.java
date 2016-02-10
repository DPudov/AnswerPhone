package com.dpudov.answerphone.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;

import com.dpudov.answerphone.R;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiGetMessagesResponse;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKList;

import java.util.Iterator;
import java.util.LinkedHashSet;

public class MessagesService extends Service {
    NotificationManager nM;
    private int NOTIFICATION = R.string.serviceStarted;
    private int[] checkedUsers;
    private int[] userId;
    private int[] userIdCopy;
    String message;

    public MessagesService() {
    }

    @Override
    public void onCreate() {
        nM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

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

    private void showNotificationNew() {
        CharSequence text = "Error";
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_answerphone_64px)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.app_name))
                .setContentText(text)
                .build();
        nM.notify(NOTIFICATION, notification);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        checkedUsers = bundle.getIntArray("userIds");
        try {
            getAndSendMessages();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return START_NOT_STICKY;
    }

    void getAndSendMessages() throws InterruptedException {
        //Запускаем поток, который проверяет новые сообщения. Если прилетает новое, читаем id отправителя. Затем шлём ему ответ.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < 100; i++) {
                        showNotification();
                        sentMsgToRecentSenders();
                        Thread.sleep(30000);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    showNotificationNew();
                }
            }
        }).start();

    }

    private void sentMsgToRecentSenders() {
//Получаем сообщения за последние 30 секунд
        VKRequest getMsg = VKApi.messages().get(VKParameters.from(VKApiConst.TIME_OFFSET, 30000));
        getMsg.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKApiGetMessagesResponse getMessagesResponse = (VKApiGetMessagesResponse) response.parsedModel;
                VKList<VKApiMessage> list = getMessagesResponse.items;
                // Формируем лист с id авторов сообщений без повторений
                LinkedHashSet<Integer> authors = new LinkedHashSet<>();
                for (VKApiMessage msg : list) {
                    // проверка. Если не прочитано и не из чата, добавляем
                    // if ((!msg.read_state))
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
                for (int i = 0; i < userId.length; i++) {
                    for (int j = 0; i < userId.length; i++) {
                        if (userId[i] == checkedUsers[j]) {
                            userIdCopy[c] = userId[i];
                            c++;
                        }
                    }
                }
                // Отправляем сообщение
                sendTo(userIdCopy);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }
        });
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
//метод для отправки сообщения user.
        message = "Привет, " + Integer.toString(userId) + "! " + getString(R.string.user_is_busy) + getString(R.string.defaultMsg);
        if (!(userId == 0)) {
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
    }

    public void sendTo(int[] userIds) {
        if (!(userIds == null)) {
            //метод для отправки сообщений нескольким юзерам
            for (int i = 0; i < userIds.length; i++) {
                send(userIds[i]);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}