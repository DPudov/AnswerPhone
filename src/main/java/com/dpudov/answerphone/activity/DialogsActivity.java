package com.dpudov.answerphone.activity;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dpudov.answerphone.R;
import com.dpudov.answerphone.fragments.ChatFragment;
import com.dpudov.answerphone.lists.DialogsListAdapter;
import com.dpudov.answerphone.util.UserIDS_Util;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiDialog;
import com.vk.sdk.api.model.VKApiGetDialogResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKUsersArray;

import java.util.HashSet;

public class DialogsActivity extends AppCompatActivity {
    private ListView dialogsView;
    private VKList<VKApiDialog> vkApiDialogs;
    private String ids;
    private VKUsersArray vkUsersArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dialogsView = (ListView) findViewById(R.id.listOfDialogs);
        VKSdk.wakeUpSession(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setUpDialogs();
        dialogsView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        dialogsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fullName;
                int chat_id = 0;
                int user_id = 0;
                String[] photos = new String[vkUsersArray.size()];

                if (vkApiDialogs.get(position).message.chat_id == 0) {
                    fullName = vkUsersArray.getById(vkApiDialogs.get(position).message.user_id).first_name + " " + vkUsersArray.getById(vkApiDialogs.get(position).message.user_id).last_name;
                    user_id = vkApiDialogs.get(position).message.user_id;
                    photos[0] = vkUsersArray.getById(vkApiDialogs.get(position).message.user_id).photo_50;

                } else {
                    fullName = vkApiDialogs.get(position).message.title;
                    chat_id = vkApiDialogs.get(position).message.chat_id;
                }

                ChatFragment chatFragment = ChatFragment.newInstance(fullName, user_id, chat_id, photos);
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.cont, chatFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
            setTitle(R.string.dialogs_head);
        } else
            super.onBackPressed();
    }


    private void setUpDialogs() {
        VKRequest getDialogs = VKApi.messages().getDialogs(VKParameters.from(VKApiConst.COUNT, 200));
        getDialogs.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);

                VKApiGetDialogResponse getDialogResponse = (VKApiGetDialogResponse) response.parsedModel;
                vkApiDialogs = getDialogResponse.items;
                HashSet<Integer> userIds = new HashSet<>();
                for (int i = 0; i < vkApiDialogs.size(); i++) {
                    userIds.add(vkApiDialogs.get(i).message.user_id);
                }
                //Получаем всю инфу о пользователях
                ids = UserIDS_Util.makeUserIdsFromHashSet(userIds);
                VKRequest getDialogsUsers = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, ids, VKApiConst.FIELDS, "photo_50, online"));
                getDialogsUsers.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        VKList vkList = (VKList) response.parsedModel;

                        vkUsersArray = new VKUsersArray();
                        for (int i = 0; i < vkList.size(); i++) {
                            vkUsersArray.add(i, (VKApiUserFull) vkList.get(i));
                        }
                        DialogsListAdapter dialogsListAdapter = new DialogsListAdapter(getApplicationContext(), vkApiDialogs, vkUsersArray);
                        dialogsView.setAdapter(dialogsListAdapter);
                    }
                });

            }
        });

    }
}
