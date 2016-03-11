package com.dpudov.answerphone.fragments.data.Lists.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DPudov on 10.03.2016.
 * This class is for the VKSdk library initialization
 */
public class Response {
    private List<Message> items = new ArrayList<>();
    public List<Message> getItems() {
        return items;
    }
}
