package com.dpudov.answerphone.fragments.data.Lists.model;

import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DPudov on 10.03.2016.
 * This class is for the VKSdk library initialization
 */
public class Message {private int id;
    private int date;
    private int out;
    @SerializedName("user_id")
    private int userId;
    @SerializedName("from_id")
    private int fromId;
    @SerializedName("read_state")
    private int readState;
    private String title;
    private String body;
    @SerializedName("chat_id")
    private int chatId;
    @SerializedName("chat_active")
    private List<Integer> chatActive = new ArrayList<>();
    @SerializedName("users_count")
    private int usersCount;
    @SerializedName("admin_id")
    private int adminId;
    private static final int FLAG_UNREAD = 1;
    private static final int FLAG_OUTBOX = 2;
    private static final int FLAG_CHAT = 8192;
    private static final int FLAG_MEDIA = 512;
    public boolean isChat;
    public int emoji;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getOut() {
        return out;
    }

    public void setOut(int out) {
        this.out = out;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getReadState() {
        return readState;
    }

    public void setReadState(int readState) {
        this.readState = readState;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public List<Integer> getChatActive() {
        return chatActive;
    }

    public void setChatActive(List<Integer> chatActive) {
        this.chatActive = chatActive;
    }




    public int getUsersCount() {
        return usersCount;
    }

    public void setUsersCount(int usersCount) {
        this.usersCount = usersCount;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public Message() {
    }

    public Message(JSONArray jsonArray) throws JSONException {
        id = jsonArray.getInt(1);
        int flags = jsonArray.getInt(2);

        if ((flags & FLAG_OUTBOX) == FLAG_OUTBOX) {
            out = 1;
        }
        else {
            out = 0;
        }
        if (out == 0) {
            if ((flags & FLAG_UNREAD) == FLAG_UNREAD) readState = 1;
            else readState = 0;
        }
        isChat = (flags & FLAG_CHAT) == FLAG_CHAT;
        JSONObject extras = jsonArray.getJSONObject(7);
        if ((flags & FLAG_MEDIA) == FLAG_MEDIA) {
            // todo attaches
        }
        if (isChat) {
            chatId = jsonArray.getInt(3) - (2000000000);
            userId = extras.getInt("from");
        } else {
            userId = jsonArray.getInt(3);
        }

        date = jsonArray.getInt(4);
        if (extras.optInt("emoji", 0) > 0) emoji = 1;
        else emoji = 0;

        if (extras.optString("attach1_type", "").equals("sticker")) {
//            sticker = new VKApiSticker();
//            sticker.id = extras.optInt("attach1");
//            sticker.product_id = extras.optInt("attach1_product_id");
        } else {
            body = jsonArray.getString(6);
        }
    }

    @Override
    public String toString() {
        return "Message " + (isChat ? "in the chat " + chatId : "") + " from " + userId + " " + getBody();
    }

    public int getFromId() {
        return fromId;
    }
}
