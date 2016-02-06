package com.dpudov.answerphone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MessagesService extends Service {
    NotificationManager nM;

    public MessagesService() {
    }

    class IncomingHandler extends Handler {
        @Override
        public void close() {

        }

        @Override
        public void flush() {

        }

        @Override
        public void publish(LogRecord record) {

        }
    }

    @Override
    public void onCreate() {
        nM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();
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
        nM.notify(R.string.serviceStarted, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
