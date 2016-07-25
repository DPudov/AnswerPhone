package com.dpudov.answerphone.model;

import android.os.Parcel;

import com.dpudov.answerphone.activity.MainActivity;
import com.vk.sdk.api.model.VKApiMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by DPudov on 06.07.2016.
 */
public class VkMessage extends VKApiMessage {
    public static Creator<VKApiMessage> CREATOR = new Creator<VKApiMessage>() {
        public VKApiMessage createFromParcel(Parcel source) {
            return new VKApiMessage(source);
        }

        public VKApiMessage[] newArray(int size) {
            return new VKApiMessage[size];
        }
    };

    public VkMessage(int chat_id, int id, int user_id, long date, String body) {
        super(chat_id, id, user_id, date, body);
        this.out = isMe(user_id);
    }

    public VkMessage(JSONObject from) throws JSONException {
        super(from);
    }

    private boolean isMe(int user_id) {
        return user_id == MainActivity.currentUserId;
    }

    public boolean isChat() {
        return this.chat_id > 2000000000;
    }
}
