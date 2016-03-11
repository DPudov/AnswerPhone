package com.dpudov.answerphone.fragments.data.Lists.network;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DPudov on 11.03.2016.
 * This class is for the VKSdk library initialization
 */
public class LpServerResponse {
    private int ts;
    private List updates = new ArrayList();

    public int getTs() {
        return ts;
    }

    public List getUpdates() {
        return updates;
    }

    public void setTs(int ts) {
        this.ts = ts;
    }

    public void setUpdates(List updates) {
        this.updates = updates;
    }

    public boolean add(Object object) {
        return updates.add(object);
    }

    @Override
    public String toString() {
        return "LpServerResponse{" +
                "ts=" + ts +
                ", updates=" + updates +
                '}';
    }
}
