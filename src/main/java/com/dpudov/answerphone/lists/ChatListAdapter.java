package com.dpudov.answerphone.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dpudov.answerphone.R;
import com.vk.sdk.api.model.VKApiMessage;
import com.vk.sdk.api.model.VKList;

/**
 * Created by DPudov on 22.03.2016.
 * This class is for the VKSdk library initialization
 */
public class ChatListAdapter extends BaseAdapter {
    private Context ctx;
    private VKList<VKApiMessage> vkApiMessages;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;
    private String[] photos;

    public ChatListAdapter(Context context, VKList<VKApiMessage> vkApiMessages, String[] photos) {
        this.ctx = context;
        this.photos = photos;
        this.vkApiMessages = vkApiMessages;
        this.inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(ctx);
    }

    @Override
    public int getCount() {
        return vkApiMessages.size();
    }

    @Override
    public Object getItem(int position) {
        return vkApiMessages.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;
        String msg = vkApiMessages.get(position).body;
        if (view == null) {
            view = inflater.inflate(R.layout.message_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textFrom = (TextView) view.findViewById(R.id.msgFrom);
            viewHolder.textOut = (TextView) view.findViewById(R.id.msgOut);
            viewHolder.imageFrom = (ImageView) view.findViewById(R.id.photoFrom);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        if (vkApiMessages.get(position).out) {
            viewHolder.textOut.setText(msg);
            viewHolder.textFrom.setText("");
            imageLoader.DisplayImage(photos[0], viewHolder.imageFrom, 25);

        } else {
            viewHolder.textOut.setText("");
            viewHolder.textFrom.setText(msg);
            viewHolder.imageFrom.setEnabled(false);
        }

        return view;
    }

    private class ViewHolder {
        TextView textFrom;
        TextView textOut;
        ImageView imageFrom;
    }
}
