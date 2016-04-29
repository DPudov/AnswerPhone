package com.dpudov.answerphone.data.network;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by DPudov on 19.04.2016.
 */
public class LongPollServer {
    public int ts;
    public String server;
    public String key;
    public int need_pts;
    public int use_ssl;
    private OkHttpClient mClient;

    public LongPollServer(OkHttpClient okHttpClient, String key, String server, int ts, int need_pts, int use_ssl) {
        mClient = okHttpClient;
        this.key = key;
        this.server = server;
        this.ts = ts;
        this.need_pts = need_pts;
        this.use_ssl = use_ssl;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public int getTs() {
        return ts;
    }

    public String getServer() {
        return server;
    }

    public String getKey() {
        return key;
    }

    public int getUse_ssl() {
        return use_ssl;
    }

    public int getNeed_pts() {
        return need_pts;
    }

    public void connectLongPoll(final String server, final String key, final int ts, final int mode) {
        String url = String.format("http://%s?act=a_check&key=%s&ts=%s&wait=25&mode=%s", server, key, ts, mode);
        final Request request = new Request.Builder()
                .url(url)
                .build();
        mClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                connectLongPoll(server, key, ts, mode);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = response.body().toString();
                System.out.println(s);
                connectLongPoll(server, key, ts, mode);
            }
        });
    }

}
