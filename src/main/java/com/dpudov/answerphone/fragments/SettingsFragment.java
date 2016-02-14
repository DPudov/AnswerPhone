package com.dpudov.answerphone.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.dpudov.answerphone.MainActivity;
import com.dpudov.answerphone.R;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import static com.vk.sdk.VKSdk.wakeUpSession;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends android.support.v4.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    @SuppressWarnings("FieldCanBeLocal")
    private String mParam1;
    @SuppressWarnings("FieldCanBeLocal")
    private String mParam2;
    private OnFragmentInteractionListener mListener;
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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        @SuppressWarnings("UnusedAssignment") EditText editText = (EditText) v.findViewById(R.id.editText);

        Button goToM8Button = (Button) v.findViewById(R.id.button2);
        checkFriendsFragment = new CheckFriendsFragment();
        goToM8Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.support.v4.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.content_frame, checkFriendsFragment);
                ft.commit();
            }
        });
        userIds = ((MainActivity) getActivity()).getUserIds();
        Switch switchMessage = (Switch) v.findViewById(R.id.switchMessage);
        switchMessage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                                     @Override
                                                     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                                         if (isChecked) {
                                                             wakeUpSession(getActivity());
                                                             Toast.makeText(getActivity(), "On", Toast.LENGTH_SHORT).show();
                                                             Intent intent = new Intent(getActivity(), MessagesService.class);
                                                             Bundle b = new Bundle();
                                                             b.putIntArray("userIds", userIds);
                                                             intent.putExtras(b);
                                                             getActivity().startService(intent);
                                                         } else

                                                         {
                                                             Toast.makeText(getActivity(), "Off", Toast.LENGTH_SHORT).show();
                                                             getActivity().stopService(new Intent(getActivity(), MessagesService.class));
                                                         }

                                                     }

                                                 }

        );// Inflate the layout for this fragment
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void send(int userId) {
        String message = "Привет, " + Integer.toString(userId) + "! " + getString(R.string.user_is_busy) + getString(R.string.defaultMsg);
        if (!(userId == 0)) {
            VKRequest requestSend = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, userId, VKApiConst.MESSAGE, message));
            requestSend.executeWithListener(new VKRequest.VKRequestListener() {
                @SuppressWarnings("EmptyMethod")
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                }

            });
        }
    }

    public void sendTo(int[] userIds) {
        if (!(userIds == null)) {
            //метод для отправки сообщений нескольким юзерам
            for (int userId : userIds) {
                send(userId);

            }
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
