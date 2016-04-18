package com.dpudov.answerphone.util;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKUsersArray;

/**
 * Created by DPudov on 25.03.2016.
 * This class is for the VKSdk library initialization
 */
public class RequestUtil {
    private VKUsersArray friends;

    public void setFriends(String... scope) {
        VKRequest getFriendsRequest = VKApi.friends().get(VKParameters.from(scope));
        getFriendsRequest.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                friends = (VKUsersArray) response.parsedModel;
            }
        });
    }

    public VKUsersArray getUsersArray() {
        setFriends(VKApiConst.FIELDS, "id, first_name, last_name, photo_50, online", "order", "hints");
        return friends;
    }

    public VKUsersArray getFriends() {
        return friends;
    }
}
