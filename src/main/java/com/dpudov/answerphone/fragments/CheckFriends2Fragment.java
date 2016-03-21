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

import com.dpudov.answerphone.MainActivity;
import com.dpudov.answerphone.MyApplication;
import com.dpudov.answerphone.R;
import com.dpudov.answerphone.lists.FriendsListAdapter;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKUsersArray;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CheckFriends2Fragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CheckFriends2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CheckFriends2Fragment extends android.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private SendToFriendsFragment sendToFriendsFragment;
    private String msg;
    private int[] userIds;
    private OnFragmentInteractionListener mListener;
    private MyApplication myApplication;

    public CheckFriends2Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CheckFriends2Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CheckFriends2Fragment newInstance(String param1, String param2) {
        CheckFriends2Fragment fragment = new CheckFriends2Fragment();
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
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_check_friends2, container, false);
        ListView listView = (ListView) v.findViewById(R.id.listView2);
        sendToFriendsFragment = new SendToFriendsFragment();
        Button saveButton = (Button) v.findViewById(R.id.saveButton2);
        Button cancelButton = (Button) v.findViewById(R.id.cancelButton);
        myApplication = (MyApplication) getActivity().getApplication();
        VKSdk.wakeUpSession(getActivity());
        //Заполнение массива друзьями
        final VKUsersArray list;
        list = myApplication.getFriendList();
        FriendsListAdapter friendsListAdapter = new FriendsListAdapter(getActivity(), list);
        listView.setAdapter(friendsListAdapter);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SparseBooleanArray sbArray = listView.getCheckedItemPositions();
                userIds = new int[list.size()];
                int c = 0;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).checked) {

                        userIds[c] = list.get(i).getId();
                        c++;
                    }

                }
                ((MainActivity) getActivity()).setUsersToSendNow(userIds);
                //Отправляем сообщения
                msg = ((MainActivity) getActivity()).getMsg() + getString(R.string.defaultMsg);
                sendTo(userIds);
                //Возвращаемся на начальный фрагмент
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, sendToFriendsFragment);
                getActivity().setTitle(R.string.sendToFriends);
                ft.commit();

            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.container, sendToFriendsFragment);
                getActivity().setTitle(R.string.sendToFriends);
                ft.commit();
            }
        });
        // Inflate the layout for this fragment
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void send(int userId) {
//метод для отправки сообщения user.

        if (userId != 0) {
            VKRequest requestSend = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, userId, VKApiConst.MESSAGE, msg));
            //noinspection EmptyMethod
            requestSend.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                }
            });
        }
    }

    private void sendTo(int[] userIds) {
        if (userIds != null) {
            //метод для отправки сообщений нескольким юзерам
            for (int userId : userIds) {
                send(userId);

            }
        }
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
