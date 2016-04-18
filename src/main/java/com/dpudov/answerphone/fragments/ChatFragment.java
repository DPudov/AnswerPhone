package com.dpudov.answerphone.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.dpudov.answerphone.R;
import com.dpudov.answerphone.lists.ChatListAdapter;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKList;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * { ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends android.app.Fragment {

    private static final String ARG_FULL_NAME = "fullName";
    private static final String ARG_USER_ID = "userId";
    private static final String ARG_CHAT_ID = "chatId";
    private static final String ARG_PHOTOS_URLS = "photos";

    private String[] mPhotos;
    private int mUserId;
    private int mChatId;

    private VKList<VKApiMessage> mVKMsg;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param fullName Parameter first_name + last_name or chat title.
     * @param userId   Id of user, that is in Dialog.
     * @param chatId   Chat VK id.
     * @return A new instance of fragment ChatFragment.
     */

    public static ChatFragment newInstance(String fullName, int userId, int chatId, String[] photos) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        args.putInt(ARG_CHAT_ID, chatId);
        args.putString(ARG_FULL_NAME, fullName);
        args.putStringArray(ARG_PHOTOS_URLS, photos);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mChatId = 2000000000 + getArguments().getInt(ARG_CHAT_ID);
            mUserId = getArguments().getInt(ARG_USER_ID);
            mPhotos = getArguments().getStringArray(ARG_PHOTOS_URLS);
            String mFullName = getArguments().getString(ARG_FULL_NAME);

            getActivity().setTitle(mFullName);
        } else {
            getActivity().onBackPressed();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_chat, container, false);
        final ListView chatView = (ListView) v.findViewById(R.id.chatList);
        VKSdk.wakeUpSession(getActivity());
        if (mUserId != 0) {
            VKRequest request1 = new VKRequest("messages.getHistory", VKParameters.from(VKApiConst.USER_ID, mUserId));
            request1.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    try {
                        mVKMsg = new VKList<>();
                        JSONArray jsonArray = response.json.getJSONObject("response").getJSONArray("items");

                        for (int i = 0; i < jsonArray.length(); i++) {
                            VKApiMessage mes = new VKApiMessage(jsonArray.getJSONObject(i));
                            mVKMsg.add(mes);
                        }

                        ChatListAdapter chatListAdapter = new ChatListAdapter(getActivity(), mVKMsg, mPhotos, null, false);
                        chatView.setAdapter(chatListAdapter);
                        final EditText editText = (EditText) v.findViewById(R.id.editedMessage);
                        ImageButton send = (ImageButton) v.findViewById(R.id.imageSend);
                        send.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String message = editText.getText().toString();
                                if (!message.equals("")) {
                                    if (mUserId != 0) {
                                        VKRequest toSend = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, mUserId, VKApiConst.MESSAGE, message));
                                        toSend.executeWithListener(new VKRequest.VKRequestListener() {
                                            @Override
                                            public void onComplete(VKResponse response) {
                                                super.onComplete(response);
                                                Toast.makeText(getActivity(), R.string.sentMsg, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (mChatId != 2000000000) {
            VKRequest request2 = new VKRequest("messages.getHistory", VKParameters.from(VKApiConst.USER_ID, mChatId));
            request2.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    try {
                        mVKMsg = new VKList<>();
                        JSONArray jsonArray = response.json.getJSONObject("response").getJSONArray("items");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            VKApiMessage mes = new VKApiMessage(jsonArray.getJSONObject(i));
                            mVKMsg.add(mes);
                        }

                        ChatListAdapter chatListAdapter = new ChatListAdapter(getActivity(), mVKMsg, mPhotos, null, true);
                        chatView.setAdapter(chatListAdapter);
                        final EditText editText = (EditText) v.findViewById(R.id.editedMessage);
                        ImageButton send = (ImageButton) v.findViewById(R.id.imageSend);
                        send.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String message = editText.getText().toString();
                                editText.setText("");
                                if (!message.equals("")) {

                                    VKRequest toSend = new VKRequest("messages.send", VKParameters.from("chat_id", mChatId - 2000000000, VKApiConst.MESSAGE, message));
                                    toSend.executeWithListener(new VKRequest.VKRequestListener() {
                                        @Override
                                        public void onComplete(VKResponse response) {
                                            super.onComplete(response);
                                            Toast.makeText(getActivity(), R.string.sentMsg, Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }


        // Inflate the layout for this fragment
        return v;
    }

private void setUpFragmentWithVK(){

}
}
