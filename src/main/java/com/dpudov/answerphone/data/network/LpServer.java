package com.dpudov.answerphone.data.network;

/**
 * Created by DPudov on 10.03.2016.
 * This class is for the VKSdk library initialization
 */
public class LpServer {
    public int ts;
    public int pts;
    public String server;
    public String key;

    public int getTs() {
        return ts;
    }

    public int getPts() {
        return pts;
    }

    public String getServer() {
        return server;
    }

    public String getKey() {
        return key;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }
}

