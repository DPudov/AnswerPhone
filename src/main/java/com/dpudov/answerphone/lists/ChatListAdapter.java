package com.dpudov.answerphone.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dpudov.answerphone.R;
import com.dpudov.answerphone.model.VkMessage;
import com.vk.sdk.api.model.VKApiMessage;

import java.util.ArrayList;
import java.util.List;


public class ChatListAdapter extends ArrayAdapter<VkMessage> {
    private TextView chatText;
    private List<VkMessage> chatMessageList = new ArrayList<>();
    private Context context;

    //constructors
    public ChatListAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
    }

    @Override
    public void add(VkMessage object) {
        super.add(object);
    }

    @Override
    public int getCount() {
        return this.getChatMessageList().size();
    }

    @Override
    public VkMessage getItem(int position) {
        return this.getChatMessageList().get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VKApiMessage chatMessageObj = getItem(position);
        View row;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (chatMessageObj.out) {
            row = inflater.inflate(R.layout.right, parent, false);
        } else {
            row = inflater.inflate(R.layout.left, parent, false);
        }
        chatText = (TextView) row.findViewById(R.id.msgr);
        chatText.setText(chatMessageObj.body);
        return row;
    }

    @Override
    public void addAll(VkMessage... items) {
        super.addAll(items);
    }

    public List<VkMessage> getChatMessageList() {
        return chatMessageList;
    }
}
