package com.dpudov.answerphone;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ListView;

import com.dpudov.answerphone.lists.DialogsListAdapter;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiDialog;
import com.vk.sdk.api.model.VKApiGetDialogResponse;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKUsersArray;

public class DialogsActivity extends AppCompatActivity {
    private ListView dialogsView;
    private MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogs);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dialogsView = (ListView) findViewById(R.id.listOfDialogs);
        VKSdk.wakeUpSession(this);
        myApplication = (MyApplication) getApplication();
        VKRequest getDialogs = VKApi.messages().getDialogs(VKParameters.from(VKApiConst.COUNT, 200));
        getDialogs.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKApiGetDialogResponse getDialogResponse = (VKApiGetDialogResponse) response.parsedModel;
                VKList<VKApiDialog> vkApiDialogs = getDialogResponse.items;
                VKUsersArray vkUsersArray = myApplication.getFriendList();
                DialogsListAdapter dialogsListAdapter = new DialogsListAdapter(getApplicationContext(), vkApiDialogs, vkUsersArray);
                dialogsView.setAdapter(dialogsListAdapter);
            }
        });
        AlertDialog.Builder ab = new AlertDialog.Builder(getApplicationContext());
        ab.setIcon(R.drawable.ic_warning_24dp);
        ab.setTitle("Внимание!");
        ab.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ab.setMessage(R.string.FuncSoon);
        AlertDialog alertDialog = ab.create();
        alertDialog.show();
    }

}
