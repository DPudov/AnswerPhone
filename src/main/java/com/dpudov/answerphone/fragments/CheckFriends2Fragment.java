package com.dpudov.answerphone.fragments;

import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.dpudov.answerphone.R;
import com.dpudov.answerphone.activity.MainActivity;
import com.dpudov.answerphone.lists.FriendsListAdapter;
import com.dpudov.answerphone.util.UserIDS_Util;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
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
    private static final String TITLE = "title";
    private static final int HAS_NO_CONNECTION_FROM_FRIENDS = 1;
    private static final int HAS_NO_CONNECTION_FROM_MSG = 2;
    private SendToFriendsFragment sendToFriendsFragment;
    private String msg;
    private String ids;
    private int[] userIds;
    private OnFragmentInteractionListener mListener;
    private Button saveButton;
    private Button cancelButton;
    private ListView listView;

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
        setRetainInstance(true);
        if (savedInstanceState!=null){
            String title = savedInstanceState.getString(TITLE);
            getActivity().setTitle(title);
        }else{
            getActivity().setTitle(R.string.checkFrFrag);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_check_friends2, container, false);
        listView = (ListView) v.findViewById(R.id.listView2);
        sendToFriendsFragment = new SendToFriendsFragment();
        saveButton = (Button) v.findViewById(R.id.saveButton2);
        setUpFriendsList();

        cancelButton = (Button) v.findViewById(R.id.cancelButton);
        //setupFriendsListView(saveButton, listView);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String title = (String) getActivity().getTitle();
        outState.putString(TITLE, title);
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

    private void sentTo(@NonNull String ids) {
        msg = ((MainActivity) getActivity()).getMsg();
        VKRequest requestSend = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_IDS, ids, VKApiConst.MESSAGE, msg));
        requestSend.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Toast.makeText(getActivity(), R.string.sentMsg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                showDialog(HAS_NO_CONNECTION_FROM_MSG);
            }
        });
    }

    @Deprecated
    private void send(int userId) {
//метод для отправки сообщения user.
        msg = ((MainActivity) getActivity()).getMsg();
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

    @Deprecated
    private void sendTo(int[] userIds) {
        if (userIds != null) {
            //метод для отправки сообщений нескольким юзерам
            for (int userId : userIds) {
                send(userId);

            }
        }
    }

    private void setUpFriendsList() {
        VKRequest vkRequest = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id, first_name, last_name, photo_50, online", "order", "hints"));
        vkRequest.executeWithListener(new VKRequest.VKRequestListener() {
                                          @Override
                                          public void onComplete(VKResponse response) {
                                              super.onComplete(response);
                                              final VKUsersArray list = (VKUsersArray) response.parsedModel;
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

                                                      ids = UserIDS_Util.makeUserIdsFromIntArray(userIds);
                                                      sentTo(ids);
                                                      FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                      ft.replace(R.id.container, sendToFriendsFragment);
                                                      getActivity().setTitle(R.string.sendToFriends);
                                                      ft.commit();
                                                  }
                                              });
                                              FriendsListAdapter friendsListAdapter = new FriendsListAdapter(getActivity(), list);
                                              listView.setAdapter(friendsListAdapter);

                                          }


                                          @Override
                                          public void onError(VKError error) {
                                              super.onError(error);
                                              showDialog(HAS_NO_CONNECTION_FROM_FRIENDS);
                                          }
                                      }

        );

    }

    private void showDialog(int ID) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        switch (ID) {
            //FROM_FRIENDS
            case 1:
                adb.setTitle(R.string.no_connection);
                adb.setIcon(R.drawable.ic_warning_24dp);
                adb.setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setUpFriendsList();
                        dialog.dismiss();
                    }
                });
                adb.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                break;
            //FROM_MSG
            case 2:
                adb.setTitle(R.string.no_connection);
                adb.setIcon(R.drawable.ic_warning_24dp);
                adb.setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sentTo(ids);
                        dialog.dismiss();
                    }
                });
                adb.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.container, sendToFriendsFragment);
                        getActivity().setTitle(R.string.sendToFriends);
                        ft.commit();
                        dialog.dismiss();
                    }
                });
        }
        AlertDialog alertDialog = adb.create();
        alertDialog.show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
