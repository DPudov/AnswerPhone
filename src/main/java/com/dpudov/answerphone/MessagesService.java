package com.dpudov.answerphone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

public class MessagesService extends Service {
    NotificationManager nM;
    private int NOTIFICATION = R.string.serviceStarted;
    int userSenderId = 0;
    int[] userIds;
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
                .setSmallIcon(R.drawable.ic_answerphone_48px)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.app_name))
                .setContentText(text)
                .build();
        nM.notify(NOTIFICATION, notification);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        getAndSendMessages();
        return super.onStartCommand(intent, flags, startId);
    }

    private void getAndSendMessages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
               send(getMessages());


            }
        });
    }

    private int getMessages() {
        VKRequest requestGet = new VKRequest("messages.get", VKParameters.from());
        requestGet.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }
        });
        return userSenderId;
    }

    public void send(int userId) {

        message = getString(R.string.defaultMsg);

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
