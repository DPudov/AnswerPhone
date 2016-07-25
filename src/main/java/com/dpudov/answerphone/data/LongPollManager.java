package com.dpudov.answerphone.data;

import com.dpudov.answerphone.model.CurrentUser;
import com.dpudov.answerphone.model.VkMessage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by DPudov on 04.07.2016.
 */
public class LongPollManager {
    private String key;
    private String server;
    private long ts;
    private OkHttpClient mClient;
    private CurrentUser user;

    public LongPollManager() {
        mClient = new OkHttpClient();

    }

    public void firstConnect() {
        final VKRequest request = new VKRequest("messages.getLongPollServer", VKParameters.from("use_ssl", 1, "need_pts", 1));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    JSONObject object = response.json.getJSONObject("response");
                    key = object.getString("key");
                    server = object.getString("server");
                    ts = object.getLong("ts");
                    connect(ts);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                firstConnect();
            }
        });

    }


    public void connect(long newTs) {
        String url = String.format("http://%s?act=a_check&key=%s&ts=%s&wait=25&mode=2", getServer(), getKey(), newTs);
        Request request = new Request.Builder()
                .url(url)
                .build();
        mClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                firstConnect();
            }

            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                Reader reader = response.body().charStream();
                BufferedReader bufferedReader = new BufferedReader(reader);
                String line;
                StringBuilder builder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
                String resp = builder.toString();
                JsonParser parser = new JsonParser();
                JsonObject newTs = parser.parse(resp).getAsJsonObject();
                long l = newTs.get("ts").getAsLong();
                setTs(l);
                JsonArray array = newTs.get("updates").getAsJsonArray();

                if (array.size() != 0) {
                    for (int i = 0; i < array.size(); i++) {
                        JsonArray arrayItem = (JsonArray) array.get(i);
                        int type = arrayItem.get(0).getAsInt();
                        switch (type) {
                            case 4:
                                int msgId = arrayItem.get(1).getAsInt();
                                int fromId = arrayItem.get(3).getAsInt();
                                int time = arrayItem.get(4).getAsInt();
                                String text = arrayItem.get(6).getAsString();
                                VkMessage message = new VkMessage(fromId, msgId, fromId, time, text);
                                org.greenrobot.eventbus.EventBus.getDefault().post(message);
                                break;
                        }
                    }
                }
                connect(getTs());
            }
        });
    }

    private boolean isMe(int id) {
        return id == user.getId();
    }

    public String getKey() {
        return key;
    }

    public String getServer() {
        return server;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }
}
