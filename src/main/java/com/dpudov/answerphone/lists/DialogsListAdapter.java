package com.dpudov.answerphone.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dpudov.answerphone.R;
import com.vk.sdk.api.model.VKApiDialog;
import com.vk.sdk.api.model.VKList;
import com.vk.sdk.api.model.VKUsersArray;

/**
 * Created by DPudov on 19.03.2016.
 * This class is for the VKSdk library initialization
 */
public class DialogsListAdapter extends BaseAdapter {
    private final Context ctx;
    private final LayoutInflater inflater;
    private final VKList<VKApiDialog> vkApiDialogs;
    private final ImageLoader imageLoader;
    private VKUsersArray userFulls;

    public DialogsListAdapter(Context context, VKList<VKApiDialog> vkApiDialogs, VKUsersArray apiUserFulls) {
        ctx = context;
        this.vkApiDialogs = vkApiDialogs;
        this.userFulls = apiUserFulls;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(context);
    }

    @Override
    public int getCount() {
        return vkApiDialogs.size();
    }

    @Override
    public Object getItem(int position) {
        return vkApiDialogs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;
        if (view == null) {
            view = inflater.inflate(R.layout.dialogs_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imgAvaOut = (ImageView) view.findViewById(R.id.ava_out);
            viewHolder.imgAvaTo = (ImageView) view.findViewById(R.id.avaFrom);
            viewHolder.textLastMsg = (TextView) view.findViewById(R.id.textLastMessage);
            viewHolder.textUserto = (TextView) view.findViewById(R.id.textFrom);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        viewHolder.textLastMsg.setText(vkApiDialogs.get(position).message.body);
        if (vkApiDialogs.get(position).message.chat_id != 0) {
            imageLoader.DisplayImage(vkApiDialogs.get(position).message.photo_50, viewHolder.imgAvaTo, 25);
            viewHolder.textUserto.setText(vkApiDialogs.get(position).message.title);
            if (userFulls.getById(vkApiDialogs.get(position).message.user_id) != null) {
                imageLoader.DisplayImage(userFulls.getById(vkApiDialogs.get(position).message.user_id).photo_50, viewHolder.imgAvaOut, 15);
            }
        } else {
            if (userFulls.getById(vkApiDialogs.get(position).message.user_id) != null) {
                imageLoader.DisplayImage(userFulls.getById(vkApiDialogs.get(position).message.user_id).photo_50, viewHolder.imgAvaTo, 25);
                String name = userFulls.getById(vkApiDialogs.get(position).message.user_id).first_name + " " + userFulls.getById(vkApiDialogs.get(position).message.user_id).last_name;
                viewHolder.textUserto.setText(name);
            }
        }
        return view;
    }

    private class ViewHolder {
        ImageView imgAvaTo;
        ImageView imgAvaOut;
        TextView textLastMsg;
        TextView textUserto;
    }


}
