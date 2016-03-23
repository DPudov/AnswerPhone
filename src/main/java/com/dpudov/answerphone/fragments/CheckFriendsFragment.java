package com.dpudov.answerphone.fragments;

import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.dpudov.answerphone.MyApplication;
import com.dpudov.answerphone.R;
import com.dpudov.answerphone.activity.MainActivity;
import com.dpudov.answerphone.lists.FriendsListAdapter;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.model.VKUsersArray;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CheckFriendsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CheckFriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class CheckFriendsFragment extends android.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    @SuppressWarnings("FieldCanBeLocal")
    private String mParam1;
    @SuppressWarnings("FieldCanBeLocal")
    private String mParam2;
    private SettingsFragment settingsFragment;
    private int[] userIds;
    MyApplication myApplication;

    public CheckFriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CheckFriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CheckFriendsFragment newInstance(String param1, String param2) {
        CheckFriendsFragment fragment = new CheckFriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_check_friends, container, false);
        ListView listView = (ListView) v.findViewById(R.id.listView);
        settingsFragment = new SettingsFragment();
        MyApplication myApplication = (MyApplication) getActivity().getApplication();
        Button saveButton = (Button) v.findViewById(R.id.saveButton);
        VKSdk.wakeUpSession(getActivity());
        //Заполнение массива друзьями
        final VKUsersArray list = myApplication.getFriendList();
        FriendsListAdapter friendsListAdapter = new FriendsListAdapter(getActivity(), list);
        listView.setAdapter(friendsListAdapter);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userIds = new int[list.size()];
                int c = 0;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).checked) {
                        userIds[c] = list.get(i).getId();
                        c++;
                    }
                }
                ((MainActivity) getActivity()).setUsersToSendAuto(userIds);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, settingsFragment);
                getActivity().setTitle(R.string.settFrag);
                ft.commit();
            }
        });


        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        OnFragmentInteractionListener mListener = null;
    }

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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


}
