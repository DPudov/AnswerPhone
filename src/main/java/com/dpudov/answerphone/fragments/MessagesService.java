package com.dpudov.answerphone.fragments;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.dpudov.answerphone.R;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiGetMessagesResponse;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKList;

import java.util.Iterator;
import java.util.LinkedHashSet;

public class MessagesService extends Service {
    private final int NOTIFICATION = R.string.serviceStarted;
    private NotificationManager nM;
    private int[] checkedUsers;
    private int[] userId;
    private int[] userIdCopy;

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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showNotification() {
        CharSequence text = getString(R.string.serviceStarted);
        Notification notification = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_answerphone_64px)
                    .setTicker(text)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(getText(R.string.app_name))
                    .setContentText(text)
                    .build();
        }
        nM.notify(NOTIFICATION, notification);
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        checkedUsers = bundle.getIntArray("userIds");
        showNotification();
        getAndSendMessages();
        stopSelf();

        return START_NOT_STICKY;
    }

    private void getAndSendMessages() {
        //Запускаем поток, который проверяет новые сообщения. Если прилетает новое, читаем id отправителя. Затем шлём ему ответ.
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    for (int i = 0; i < 1000; i++) {
                        sentMsgToRecentSenders();
                        Thread.sleep(30000);
                    }


                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }).start();

    }

    private void sentMsgToRecentSenders() {
//Получаем сообщения за последние 30 секунд
        VKRequest getMsg = VKApi.messages().get(VKParameters.from(VKApiConst.TIME_OFFSET, 30));
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
                    if ((!msg.read_state))
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
                // Отправляем сообщение
                sendTo(userIdCopy);
            }

        });
    }


    private void send(int userId) {
//метод для отправки сообщения user.
        String message = getString(R.string.hi) + Integer.toString(userId) + "! " + getString(R.string.user_is_busy) + getString(R.string.defaultMsg);
        if (!(userId == 0)) {
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
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}