package com.dpudov.answerphone.util;

import com.dpudov.answerphone.model.VkMessage;
import com.vk.sdk.api.model.VKList;

import java.util.Collections;

/**
 * Created by DPudov on 19.05.2016.
 */
public class VKMessagesSorter {
    public static VKList<VkMessage> sortMessagesReverse(VKList<VkMessage> listForSort){
        Collections.reverse(listForSort);
        return listForSort;
    }
}
