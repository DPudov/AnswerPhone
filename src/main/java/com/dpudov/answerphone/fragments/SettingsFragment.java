package com.dpudov.answerphone.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.dpudov.answerphone.MainActivity;
import com.dpudov.answerphone.R;

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
        addPreferencesFromResource(R.xml.preferences);
        Preference preference = findPreference("checkFr");
        checkFriendsFragment = new CheckFriendsFragment();
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, checkFriendsFragment);
                ft.commit();
                getActivity().setTitle(R.string.checkFrFrag);
                return false;
            }
        });
        SwitchPreference switchPreference = (SwitchPreference) findPreference("swSrv");
        switchPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (((SwitchPreference) preference).isChecked()) {
                    wakeUpSession(getActivity());
                    Intent intent = new Intent(getActivity(), MessagesService.class);
                    Bundle b = new Bundle();
                    userIds = ((MainActivity)getActivity()).getUsersToSendAuto();
                    Toast.makeText(getActivity(), Integer.toString(userIds[0]), Toast.LENGTH_SHORT).show();
                    // если друзья заданы, включаем сервис, иначе включаем друзей
                    if (userIds != null) {
                        b.putIntArray("userIds", userIds);
                        intent.putExtras(b);
                        getActivity().startService(intent);
                    } else {
                        Toast.makeText(getActivity(), "Друзья не выбраны!", Toast.LENGTH_SHORT).show();
                        ((SwitchPreference) preference).setChecked(false);
                    }
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

    //@Override
    //public View onCreateView(LayoutInflater inflater, final ViewGroup container,
    //                       Bundle savedInstanceState) {

    //   View v = inflater.inflate(R.layout.fragment_settings, container, false);

    //AdView adView = (AdView) v.findViewById(R.id.adViewSettings);
    //AdRequest adRequest = new AdRequest.Builder().build();
    //adView.loadAd(adRequest);
    //View btn = v.findViewById(R.id.sign_in_button);
    //btn.setOnClickListener(new View.OnClickListener() {
    //    @Override
    //    public void onClick(View v) {
    //        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
    //                false, null, null, null, null);
    //       startActivityForResult(intent, 123);
    //    }
    //});

    //Button goToM8Button = (Button) v.findViewById(R.id.button2);
    //OnClick и реализация выхода на новый фрагмент
    //checkFriendsFragment = new CheckFriendsFragment();
    //goToM8Button.setOnClickListener(new View.OnClickListener() {
    //    @Override
    //    public void onClick(View v) {
    //        android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
    //        ft.replace(R.id.container, checkFriendsFragment);
    //       getActivity().setTitle(R.string.checkFrFrag);
    //       ft.commit();
    //    }
    //});

    //Switch switchMessage = (Switch) v.findViewById(R.id.switchMessage);
    //switchMessage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    // @Override
    // public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    // if (isChecked) {
    //   wakeUpSession(getActivity());
    //   Toast.makeText(getActivity(), "On", Toast.LENGTH_SHORT).show();
    //    Intent intent = new Intent(getActivity(), MessagesService.class);
    //    Bundle b = new Bundle();
    //     // если друзья заданы, включаем сервис, иначе включаем друзей
    //   if (userIds != null) {
    //        b.putIntArray("userIds", userIds);
    //        intent.putExtras(b);
    //        getActivity().startService(intent);
    //    } else {
    //        android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
    //        ft.replace(R.id.container, checkFriendsFragment);
    //        getActivity().setTitle(R.string.checkFrFrag);
    //        ft.commit();
    //    }
    // } else

    // {
    //    Toast.makeText(getActivity(), "Off", Toast.LENGTH_SHORT).show();
    //     getActivity().stopService(new Intent(getActivity(), MessagesService.class));
    //   }

    // }

    // }

    //);// Inflate the layout for this fragment
    //    return v;
    // }

    // TODO: Rename method, update argument and hook method into UI event
    // public void onButtonPressed(Uri uri) {
    //     if (mListener != null) {
    //        mListener.onFragmentInteraction(uri);
    //     }
    // }


    // @Override
    //  public void onDetach() {
    //    super.onDetach();
    //    mListener = null;
    // }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    //public interface OnFragmentInteractionListener {
    //   // TODO: Update argument type and name
    //    void onFragmentInteraction(Uri uri);
    // }
}
