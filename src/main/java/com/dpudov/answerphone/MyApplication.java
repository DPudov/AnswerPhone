package com.dpudov.answerphone;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKUsersArray;

/**
 * Created by DPudov on 31.01.2016.
 * This class is for the VKSdk library initialization
 */

public class MyApplication extends android.app.Application {
    public static final int FROM_NOTIFICATION = 0;
    public int[] friendsIds;
    public String[] friendsPhotos_50;
    public String[] friendsFirstNames;
    public String[] friendsLastNames;
    public boolean[] online;
    public VKUsersArray friendList;


    private final VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            //noinspection StatementWithEmptyBody
            if (newToken == null) {
// VKAccessToken is invalid
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this);
        AnalyticsTrackers.initialize(this);

        VKRequest getFriendsRequest = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id, first_name, last_name, photo_50, online", "order", "hints"));
        getFriendsRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                final VKUsersArray friends;
                friends = (VKUsersArray) response.parsedModel;
                friendsIds = new int[friends.size()];
                friendsPhotos_50 = new String[friends.size()];
                friendsFirstNames = new String[friends.size()];
                friendsLastNames = new String[friends.size()];
                online = new boolean[friends.size()];
                setUpFriends(friends);
                setFriendList(friends);
            }
        });

    }

    private void setUpFriends(VKUsersArray friends) {
        int[] friendsIds = new int[friends.size()];
        String[] friendsPhotos_50 = new String[friends.size()];
        String[] friendsFirstNames = new String[friends.size()];
        String[] friendsLastNames = new String[friends.size()];
        boolean[] online = new boolean[friends.size()];
        // Итерация массивов
        for (int i = 0; i < friends.size(); i++) {
            friendsIds[i] = friends.get(i).getId();
            friendsFirstNames[i] = friends.get(i).first_name;
            friendsLastNames[i] = friends.get(i).last_name;
            friendsPhotos_50[i] = friends.get(i).photo_50;
            online[i] = friends.get(i).online;
        }
        //Инициализация
        setFriendsIds(friendsIds);
        setFriendsFirstNames(friendsFirstNames);
        setFriendsLastNames(friendsLastNames);
        setFriendsPhotos_50(friendsPhotos_50);
        setOnline(online);
    }

    public int[] getFriendsIds() {
        return friendsIds;
    }

    public void setFriendsIds(int[] friendsIds) {
        this.friendsIds = friendsIds;
    }

    public String[] getFriendsPhotos_50() {
        return friendsPhotos_50;
    }

    public void setFriendsPhotos_50(String[] friendsPhotos_50) {
        this.friendsPhotos_50 = friendsPhotos_50;
    }

    public String[] getFriendsFirstNames() {
        return friendsFirstNames;
    }

    public void setFriendsFirstNames(String[] friendsFirstNames) {
        this.friendsFirstNames = friendsFirstNames;
    }

    public String[] getFriendsLastNames() {
        return friendsLastNames;
    }

    public void setFriendsLastNames(String[] friendsLastNames) {
        this.friendsLastNames = friendsLastNames;
    }

    public boolean[] getOnline() {
        return online;
    }

    public void setOnline(boolean[] online) {
        this.online = online;
    }

    public VKUsersArray getFriendList() {
        return friendList;
    }

    public void setFriendList(VKUsersArray friendList) {
        this.friendList = friendList;
    }

}
