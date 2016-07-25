package com.dpudov.answerphone.model;

import com.vk.sdk.api.model.VKApiMessage;

/**
 * Created by DPudov on 04.07.2016.
 */
public class ChatMessage {
    public boolean left;
    public VKApiMessage message;

    public ChatMessage(boolean left, VKApiMessage message) {
        this.left = left;
        this.message = message;
    }

    public ChatMessage(boolean left, String message) {
        this.left = left;
        this.message.body = message;
    }
}
