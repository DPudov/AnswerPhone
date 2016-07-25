package com.dpudov.answerphone.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.dpudov.answerphone.MyApplication;
import com.dpudov.answerphone.R;
import com.dpudov.answerphone.activity.MainActivity;
import com.dpudov.answerphone.data.LongPollManager;
import com.dpudov.answerphone.model.VkMessage;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;

public class MessagesService extends Service {
    private final int NOTIFICATION = R.string.serviceStarted;
    private int currentUserId;
    private NotificationManager nM;
    private Notification notification;
    private String message;
    private HashSet<Integer> forgottenUsers;
    private int[] checkedUsers;
    private boolean addTime;
    private boolean addPrefix;
    private boolean addName;
    private int time;
    private String defaultMessage;
    private Date zeroDay;

    public MessagesService() {
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEvent(VkMessage message) {
        Date currentDate = new Date();
        long dist = (currentDate.getTime() - zeroDay.getTime()) / 60000;
        long inMin = time - dist;
        int from = message.user_id;
        //if is NOT from forgotten user and NOT from me!
        if (isNotForgotten(from) && !isMe(from) && !message.isChat() && isChecked(from) && (inMin > 0)) {
            this.message = MessagesManager.createMessage(getApplicationContext(), defaultMessage, addName, addPrefix, addTime, from, inMin);
            sendTo(from, this.message);
            forgetUser(from);
        } else if (inMin <= 0) {
            stopForeground(true);
            stopSelf();
        }
    }

    private void sendTo(int from, String message) {
        VKRequest requestSend = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_IDS, String.valueOf(from), VKApiConst.MESSAGE, message));
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

    private boolean isMe(int from) {
        return currentUserId == from;
    }

    @Override
    public void onCreate() {
        nM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        forgottenUsers = new HashSet<>();
        VKRequest request = VKApi.users().get();
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                JSONObject userJSON = null;
                try {
                    userJSON = response.json.getJSONArray("response").getJSONObject(0);
                    currentUserId = userJSON.getInt("id");
                    Toast.makeText(getApplicationContext(), String.valueOf(currentUserId), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        nM.cancel(NOTIFICATION);
        EventBus.getDefault().unregister(this);

    }

    private void showNotification() {

        nM.notify(NOTIFICATION, notification);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //build notification
        Intent notifyIntent = new Intent(this, MainActivity.class);
        Bundle b = new Bundle();
        b.putInt("from", MyApplication.FROM_NOTIFICATION);
        notifyIntent.putExtras(b);
        String text = getString(R.string.serviceStarted);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_stat_name)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.app_name))
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .build();

        //setup service
        Bundle bundle = intent.getExtras();
        checkedUsers = bundle.getIntArray("userIds");
        defaultMessage = bundle.getString("defaultMessage");
        time = bundle.getInt("time");
        addName = bundle.getBoolean("addName");
        addPrefix = bundle.getBoolean("addPrefix");
        addTime = bundle.getBoolean("addTime");
        zeroDay = new Date();
        //start long poll
        LongPollManager longPollManager = new LongPollManager();
        longPollManager.firstConnect();
        //start service
        startForeground(NOTIFICATION, notification);
        return startId;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean isChecked(int userId) {
        for (int id : checkedUsers) {
            if (id == userId)
                return true;
        }
        return false;
    }

    private boolean isNotForgotten(int userId) {
        return !forgottenUsers.contains(userId);
    }

    private void forgetUser(int userId) {
        forgottenUsers.add(userId);
    }
}