package com.dpudov.answerphone.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.dpudov.answerphone.MessagesService;
import com.dpudov.answerphone.MyHandler;
import com.dpudov.answerphone.NotificationSettings;
import com.dpudov.answerphone.R;
import com.dpudov.answerphone.RegistrationIntentService;
import com.dpudov.answerphone.activity.MainActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.microsoft.windowsazure.notifications.NotificationsManager;

import static com.vk.sdk.VKSdk.wakeUpSession;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link //SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends PreferenceFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TITLE = "title";
    private static final int TIME = 1;
    private static final int FRIENDS = 2;
    public static final String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private boolean maddName;

    private boolean maddPrefix;
    // TODO: Rename and change types of parameters
    @SuppressWarnings("FieldCanBeLocal")
    private String mParam1;
    @SuppressWarnings("FieldCanBeLocal")
    private String mParam2;
    //private OnFragmentInteractionListener mListener;
    private int[] userIds;
    private CheckFriendsFragment checkFriendsFragment;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            String title = savedInstanceState.getString(TITLE);
            getActivity().setTitle(title);
        } else {
            getActivity().setTitle(R.string.settFrag);
        }
        setRetainInstance(true);
        //initialize
        addPreferencesFromResource(R.xml.preferences);
        Preference preference = findPreference("checkFr");
        final EditTextPreference editTextPreference = (EditTextPreference) findPreference("time");
        final CheckBoxPreference addName = (CheckBoxPreference) findPreference("addName");
        final CheckBoxPreference addPrefix = (CheckBoxPreference) findPreference("addSpecial");
        SwitchPreference switchPreference = (SwitchPreference) findPreference("swSrv");
        SwitchPreference enablePush = (SwitchPreference) findPreference("push");
        Preference info = findPreference("version");
        info.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.info);
                builder.setIcon(R.drawable.ic_launcher_first);
                builder.setMessage("Версия 1.0\nНомер сборки 123");
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return false;
            }
        });
        enablePush.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (((SwitchPreference) preference).isChecked()) {
                    NotificationsManager.handleNotifications(getActivity(), NotificationSettings.SenderId, MyHandler.class);
                    registerWithNotificationHubs();
                }
                return false;
            }
        });
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
                    // если друзья заданы, включаем сервис
                    putAndCheck(intent, b, userIds, time, maddPrefix, maddName, (SwitchPreference) preference);
                } else {
                    getActivity().stopService(new Intent(getActivity(), MessagesService.class));
                }
                return false;
            }
        });
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String title = (String) getActivity().getTitle();
        outState.putString(TITLE, title);
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

    private void putAndCheck(Intent intent, Bundle b, int[] userIds, int time, boolean addPrefix, boolean addName, SwitchPreference preference) {
        if ((userIds != null)) {
            if (userIds[0] != 0) {
                b.putIntArray("userIds", userIds);
                if (time != 0) {
                    b.putInt("time", time);
                    b.putBoolean("addName", addName);
                    b.putBoolean("addPrefix", addPrefix);
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

    public void registerWithNotificationHubs() {
        Log.i(TAG, " Registering with Notification Hubs");

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(getActivity(), RegistrationIntentService.class);
            getActivity().startService(intent);
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(getActivity(), resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported by Google Play Services.");
                MainActivity.mainActivity.ToastNotify("This device is not supported by Google Play Services.");
                getActivity().finish();
            }
            return false;
        }
        return true;
    }
}
