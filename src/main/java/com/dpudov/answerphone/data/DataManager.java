package com.dpudov.answerphone.data;

import com.dpudov.answerphone.data.network.LongPollServer;
import com.google.gson.Gson;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

/**
 * Created by DPudov on 10.03.2016.
 * This class is for the VKSdk library initialization
 */
public class DataManager {
    private Gson mGson;

    private LongPollListener mLongPollListener;

    private static final String TAG = DataManager.class.getSimpleName();

    public DataManager(Gson gson) {
        mGson = gson;
        mLongPollListener = null;
    }





    public void getLongPollServer(int needPts) {
        VKRequest request = new VKRequest("messages.getLongPollServer", VKParameters.from(
                "need_pts", needPts));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                LongPollServer lpServer = mGson.fromJson(response.json
                        .optJSONObject("response")
                        .toString(), LongPollServer.class);
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




    public void setLongPollListener(LongPollListener longPollListener) {
        mLongPollListener = longPollListener;
    }



    public interface LongPollListener {
        void onResponseReceived(LongPollServer lpServer);
    }



}
