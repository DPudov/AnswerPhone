package com.dpudov.answerphone.fragments;

import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiDialog;
import com.vk.sdk.api.model.VKApiGetDialogResponse;
import com.vk.sdk.api.model.VKList;

import java.util.ArrayList;

/**
 * Created by DPudov on 04.02.2016.
 */
public class User {
//TODO: измени метод, чтоб собирал юзеров без VKResponse. Иначе теряется смысл.
    public void makeUsers(VKResponse response) {//Создает array юзеров из диалогов
        VKApiGetDialogResponse getDialogResponse = (VKApiGetDialogResponse) response.parsedModel;
        final VKList<VKApiDialog> list = getDialogResponse.items;
        ArrayList<String> usersId = new ArrayList<>();
        for (VKApiDialog msg : list){
            usersId.add(String.valueOf(msg.message.user_id));
        }

    }

}
