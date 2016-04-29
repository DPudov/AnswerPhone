package com.dpudov.answerphone.fragments;

import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.dpudov.answerphone.R;
import com.dpudov.answerphone.activity.MainActivity;
import com.dpudov.answerphone.lists.FriendsListAdapter;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKRequest.VKRequestListener;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKUsersArray;


public class CheckFriendsFragment extends android.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TITLE = "title";
    private static final int HAS_NO_CONNECTION = 1;
    private static final int NOTHING_CHOSEN = 2;
    private SettingsFragment settingsFragment;
    private int[] userIds;
    private Button saveButton;
    private ListView listView;

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
        setRetainInstance(true);
        if (savedInstanceState != null) {
            getActivity().setTitle(savedInstanceState.getString(TITLE));
        } else {
            getActivity().setTitle(R.string.checkFrFrag);
        }
        //if (getArguments() != null) {
        //    mParam1 = getArguments().getString(ARG_PARAM1);
        //    mParam2 = getArguments().getString(ARG_PARAM2);
        // }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_check_friends, container, false);
        listView = (ListView) v.findViewById(R.id.listView);
        settingsFragment = new SettingsFragment();
//set default save button action

        saveButton = (Button) v.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(NOTHING_CHOSEN);
            }

        });
        setUpFriendsList();
        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String title = (String) getActivity().getTitle();
        outState.putString(TITLE, title);
    }

    private void showDialog(int ID) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
        switch (ID) {
            case 1:
                adb.setTitle(getString(R.string.no_connection));
                adb.setIcon(R.drawable.ic_warning_24dp);
                adb.setPositiveButton(getString(R.string.try_again), new DialogInterface.OnClickListener() {
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
            case 2:
                adb.setTitle(R.string.friends_unchecked);
                adb.setTitle(R.drawable.ic_warning_24dp);
                adb.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                break;
        }
        AlertDialog alertDialog = adb.create();
        alertDialog.show();
    }

    private void setUpFriendsList() {
        VKRequest vkRequest = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id, first_name, last_name, photo_50, online", "order", "hints"));
        vkRequest.executeWithListener(new VKRequestListener() {
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
                                                      ((MainActivity) getActivity()).setUsersToSendAuto(userIds);
                                                      FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                      ft.replace(R.id.container, settingsFragment);
                                                      getActivity().setTitle(R.string.settFrag);
                                                      ft.commit();
                                                  }
                                              });
                                              FriendsListAdapter friendsListAdapter = new FriendsListAdapter(getActivity(), list);
                                              listView.setAdapter(friendsListAdapter);

                                          }

                                          @Override
                                          public void onError(VKError error) {
                                              super.onError(error);
                                              showDialog(HAS_NO_CONNECTION);
                                          }
                                      }

        );
    }
}
