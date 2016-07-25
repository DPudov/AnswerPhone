package com.dpudov.answerphone.fragments;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.dpudov.answerphone.R;
import com.dpudov.answerphone.data.LongPollManager;
import com.dpudov.answerphone.lists.ChatListAdapter;
import com.dpudov.answerphone.model.CurrentUser;
import com.dpudov.answerphone.model.VkMessage;
import com.dpudov.answerphone.util.VKMessagesSorter;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKList;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private CurrentUser user;
    private String[] mPhotos;
    private int mUserId;
    private int mChatId;
    private VKList<VkMessage> mVKMsg;
    private ImageButton buttonSend;
    private EditText chatText;
    private ListView listView;
    private ChatListAdapter adapter;
    private int[] chatUsersIds;

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
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        //refresh
        VKSdk.wakeUpSession(getActivity());
        getHistory();
        createChatUsers();
        //connect to LongPoll
        LongPollManager longPollManager = new LongPollManager();
        longPollManager.firstConnect();
        buttonSend = (ImageButton) v.findViewById(R.id.imageSend);
        listView = (ListView) v.findViewById(R.id.chatList);
        chatText = (EditText) v.findViewById(R.id.editedMessage);
        adapter = new ChatListAdapter(getActivity(), R.layout.right);
        listView.setAdapter(adapter);
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(adapter.getCount() - 1);
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return (event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER) && sendMessage();
            }
        });

        // Inflate the layout for this fragment
        return v;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(VkMessage message) {
        adapter.add(message);
    }

    @Subscribe
    public void onEvent(CurrentUser user) {
        this.user = user;
    }

    private boolean sendMessage() {
        final String msg = chatText.getText().toString();

        if (!msg.isEmpty()) {
            VKRequest request = null;

            if ((mUserId != 0) && (mUserId < 2000000000)) {
                request = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, mUserId, VKApiConst.MESSAGE, msg));
            } else if (mChatId != 2000000000) {
                request = new VKRequest("messages.send", VKParameters.from("chat_id", mChatId - 2000000000, VKApiConst.MESSAGE, msg));
            }
            if (request != null) {
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        Toast.makeText(getActivity(), R.string.sentMsg, Toast.LENGTH_SHORT).show();
                        chatText.setText("");
                    }

                    @Override
                    public void onError(VKError error) {
                        super.onError(error);
                        Toast.makeText(getActivity(), R.string.VK_Err, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
        return true;
    }

    private void getHistory() {
        VKRequest request = null;
        if ((mUserId != 0) && (mUserId < 2000000000)) {
            request = new VKRequest("messages.getHistory", VKParameters.from(VKApiConst.COUNT, 200, VKApiConst.USER_ID, mUserId));
        } else if (mChatId != 2000000000) {
            request = new VKRequest("messages.getHistory", VKParameters.from(VKApiConst.COUNT, 200, VKApiConst.USER_ID, mChatId));
        }
        if (request != null) {
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    mVKMsg = new VKList<>();
                    JSONArray jsonArray;
                    try {
                        jsonArray = response.json.getJSONObject("response").getJSONArray("items");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            VkMessage mes = new VkMessage(jsonArray.getJSONObject(i));
                            mVKMsg.add(mes);
                        }
                        VKMessagesSorter.sortMessagesReverse(mVKMsg);
                        for (VkMessage message : mVKMsg)
                            adapter.add(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
        }
    }

    private void createChatUsers() {
        final VKRequest request;
        if (mUserId != 0 && mUserId < 2000000000) {
            chatUsersIds = new int[2];
            chatUsersIds[0] = mUserId;
            request = new VKRequest("users.get");
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    try {
                        JSONObject userJSON = response.json.getJSONArray("response").getJSONObject(0);
                        chatUsersIds[1] = userJSON.getInt("id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else if (mChatId != 2000000000) {
            request = new VKRequest("messages.getChatUsers", VKParameters.from("chat_id", mChatId));
            request.executeWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    try {
                        JSONArray array = response.json.getJSONArray("response");
                        chatUsersIds = new int[array.length()];
                        for (int i = 0; i < chatUsersIds.length; i++) {
                            chatUsersIds[i] = array.getInt(i);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }




}
