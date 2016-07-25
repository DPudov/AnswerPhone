package com.dpudov.answerphone.service;

import android.content.Context;

import com.dpudov.answerphone.R;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by DPudov on 25.07.2016.
 */
public class MessagesManager {
    private static String first_name;
    private static String last_name;

    public static String createMessage(Context context, String defaultMessage, boolean addName, boolean addPostfix, boolean addTime, int userId, long time) {
        StringBuilder builder = new StringBuilder();
        if (userId != 0) {
            VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, userId)).executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    try {
                        JSONObject userJSON = response.json.getJSONArray("response").getJSONObject(0);
                        first_name = userJSON.getString("first_name");
                        last_name = userJSON.getString("last_name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
        if (addName) {
            builder.append(context.getResources().getString(R.string.hi)).append(", ").append(first_name).append(" ").append(last_name).append("! ");
        }
        builder.append(defaultMessage).append(" ");
        if (addTime) {
            builder.append(context.getResources().getString(R.string.when_user_returns)).append(time).append(context.getResources().getString(R.string.minutes_end));
        }
        if (addPostfix) {
            builder.append(context.getResources().getString(R.string.defaultMsg));
        }
        return builder.toString();
    }
}
