package com.dpudov.answerphone.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;

import com.dpudov.answerphone.R;
import com.dpudov.answerphone.activity.MainActivity;
import com.dpudov.answerphone.service.MessagesService;

import static com.vk.sdk.VKSdk.wakeUpSession;


public class MainFragment extends PreferenceFragment {

    private CheckFriendsFragment checkFriendsFragment;
    private boolean maddName;
    private boolean maddTime;
    private boolean maddPrefix;
    private String msg;
    private int[] userIds;
    private static final int TIME = 1;
    private static final int FRIENDS = 2;
    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        String TITLE = getResources().getString(R.string.main_page_answer);
        if (savedInstanceState != null) {
            String title = savedInstanceState.getString(TITLE);
            getActivity().setTitle(title);
        } else {
            getActivity().setTitle(R.string.main_page_answer);
        }
        addPreferencesFromResource(R.xml.main_preferences);
        Preference preference = findPreference("checkFr");
        final EditTextPreference editTextPreference = (EditTextPreference) findPreference("time");
        final EditTextPreference editMessage = (EditTextPreference) findPreference("defaultMessage");
        final CheckBoxPreference addName = (CheckBoxPreference) findPreference("addName");
        final CheckBoxPreference addPrefix = (CheckBoxPreference) findPreference("addSpecial");
        final CheckBoxPreference addTime = (CheckBoxPreference) findPreference("addTime");
        SwitchPreference switchPreference = (SwitchPreference) findPreference("swSrv");
        checkFriendsFragment = new CheckFriendsFragment();
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, checkFriendsFragment);
                ft.addToBackStack(null);
                getActivity().setTitle(R.string.checkFrFrag);
                ft.commit();
                return false;
            }
        });
        switchPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                int time;
                try {
                    time = Integer.parseInt(editTextPreference.getText());
                } catch (Exception e) {
                    time = 10;
                }
                if (((SwitchPreference) preference).isChecked()) {
                    wakeUpSession(getActivity());
                    Intent intent = new Intent(getActivity(), MessagesService.class);
                    Bundle b = new Bundle();
                    userIds = ((MainActivity) getActivity()).getUsersToSendAuto();
                    maddName = addName.isChecked();
                    maddPrefix = addPrefix.isChecked();
                    maddTime = addTime.isChecked();
                    msg = editMessage.getText();
                    // если друзья заданы, включаем сервис
                    putAndCheck(intent, b, userIds, msg, time, maddPrefix, maddName, maddTime, (SwitchPreference) preference);
                } else {
                    getActivity().stopService(new Intent(getActivity(), MessagesService.class));
                }
                return false;
            }
        });
    }

    private void putAndCheck(Intent intent, Bundle b, int[] userIds, String message, int time, boolean addPrefix, boolean addName, boolean maddTime, SwitchPreference preference) {
        if ((userIds != null)) {
            if (userIds[0] != 0) {
                b.putIntArray("userIds", userIds);
                if (time != 0) {
                    b.putInt("time", time);
                    b.putString("defaultMessage", message);
                    b.putBoolean("addName", addName);
                    b.putBoolean("addPrefix", addPrefix);
                    b.putBoolean("addTime", maddTime);
                    intent.putExtras(b);
                    getActivity().startService(intent);
                } else {
                    showAlert(TIME);
                    preference.setChecked(false);
                }
            } else {
                preference.setChecked(false);
                showAlert(FRIENDS);
            }
        } else {
            preference.setChecked(false);
            showAlert(FRIENDS);
        }

    }
    private void showAlert(int which) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        switch (which) {
            case (FRIENDS):
                adb.setTitle(getActivity().getString(R.string.friends_unchecked));
                break;
            case (TIME):
                adb.setTitle(getActivity().getString(R.string.time_unchecked));
                break;
        }
        adb.setIcon(R.drawable.ic_warning_24dp);
        adb.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = adb.create();
        alertDialog.show();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }



}
