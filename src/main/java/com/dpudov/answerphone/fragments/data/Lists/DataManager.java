package com.dpudov.answerphone.fragments.data.Lists;

import com.dpudov.answerphone.fragments.data.Lists.model.Response;
import com.dpudov.answerphone.fragments.data.Lists.network.LpServer;
import com.google.gson.Gson;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

/**
 * Created by DPudov on 10.03.2016.
 * This class is for the VKSdk library initialization
 */
public class DataManager {private Gson mGson;

    private DataLoadedListener mDataLoadedListener;
    private LongPollListener mLongPollListener;
    private static final String TAG = DataManager.class.getSimpleName();

    public DataManager(Gson gson) {
        mGson = gson;
        mDataLoadedListener = null;
        mLongPollListener = null;
    }

    public void getDialogsAndUsers(int count, int offset, int unread, String fields) {
        VKRequest request = new VKRequest("execute.getDialogsAndUsers", VKParameters.from(
                VKApiConst.COUNT, count, VKApiConst.OFFSET, offset, VKApiConst.UNREAD, unread,
                VKApiConst.FIELDS, fields));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                Response apiResponse = mGson.fromJson(response.json
                        .optJSONObject("response")
                        .toString(), Response.class);
                mDataLoadedListener.onResponseReceived(apiResponse);
            }
        });
    }

    public void getChatHistory(int offset, int count, int userId) {
        VKRequest request = new VKRequest("messages.getHistory", VKParameters.from(
                VKApiConst.COUNT, count, VKApiConst.USER_ID, userId, VKApiConst.OFFSET, offset));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                Response apiResponse = mGson.fromJson(response.json
                        .optJSONObject("response")
                        .toString(), Response.class);
                mDataLoadedListener.onResponseReceived(apiResponse);
            }
        });
    }

    public void getLongPollServer(int needPts) {
        VKRequest request = new VKRequest("messages.getLongPollServer", VKParameters.from(
                "need_pts", needPts));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                LpServer lpServer = mGson.fromJson(response.json
                        .optJSONObject("response")
                        .toString(), LpServer.class);
                mLongPollListener.onResponseReceived(lpServer);
            }
        });
    }

    public void sendMessage(String message, int peerId) {
        VKRequest request = new VKRequest("messages.send", VKParameters.from(
                "peer_id", peerId,
                "message", message));
        request.start();
    }



    public void setDataLoadedListener(DataLoadedListener dataLoadedListener) {
        mDataLoadedListener = dataLoadedListener;
    }

    public void setLongPollListener(LongPollListener longPollListener) {
        mLongPollListener = longPollListener;
    }

    public interface DataLoadedListener {
        void onResponseReceived(Response response);
    }

    public interface LongPollListener {
        void onResponseReceived(LpServer lpServer);
    }
}
