package com.dpudov.answerphone.fragments;

import android.app.FragmentTransaction;
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
import com.dpudov.answerphone.MessagesService;
import com.dpudov.answerphone.R;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends android.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String message;
    private EditText editText;
    private Button goToM8Button;
    private OnFragmentInteractionListener mListener;
    public int[] userIds;
    CheckFriendsFragment checkFriendsFragment;


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
        editText = (EditText) v.findViewById(R.id.editText);

        goToM8Button = (Button) v.findViewById(R.id.button2);
        checkFriendsFragment = new CheckFriendsFragment();
        goToM8Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, checkFriendsFragment);
                ft.commit();
            }
        });

        Switch switchMessage = (Switch) v.findViewById(R.id.switchMessage);
        switchMessage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    Toast.makeText(getActivity(), "On", Toast.LENGTH_SHORT).show();
                    // int[] usersId = new int[2];
                    //  usersId[0] = 238489071;
                    // usersId[1] = 134132102;
                    //sendTo(usersId);
                    userIds = ((MainActivity) getActivity()).getUserIds();
                    //sendTo(userIds);
                    Intent intent = null;
                    try {

                        intent = new Intent(getActivity(), MessagesService.class);
                        Bundle b = new Bundle();
                        b.putIntArray("userIds", userIds);
                        intent.putExtras(b);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "1", Toast.LENGTH_SHORT).show();
                    }
                    try {
                        SettingsFragment.this.getActivity().startService(intent);
                    } catch (Exception e) {
                        Toast.makeText(getActivity(), "2", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Off", Toast.LENGTH_SHORT).show();
                    SettingsFragment.this.getActivity().stopService(new Intent(getActivity(), MessagesService.class));
                }

            }

        });// Inflate the layout for this fragment
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void send(int userId) {
        if (userId == 0)
            Toast.makeText(getActivity(), "Ошибка", Toast.LENGTH_SHORT).show();
        else {
            message = editText.getText().toString().concat(getString(R.string.defaultMsg));
            VKRequest request = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, userId, VKApiConst.MESSAGE, message));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    Toast.makeText(getActivity(), R.string.sentMsg, Toast.LENGTH_SHORT).show();


                }

                @Override
                public void onError(VKError error) {
                    super.onError(error);
                    Toast.makeText(getActivity(), R.string.VK_Err, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void sendTo(int[] userIds) {
        if (userIds.length == 0)
            Toast.makeText(getActivity(), "Вы не выбрали друзей!", Toast.LENGTH_LONG).show();
        else {
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
