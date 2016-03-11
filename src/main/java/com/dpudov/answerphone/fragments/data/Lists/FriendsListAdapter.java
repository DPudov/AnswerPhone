package com.dpudov.answerphone.fragments.data.Lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dpudov.answerphone.R;
import com.vk.sdk.api.model.VKUsersArray;


/**
 * Created by DPudov on 25.02.2016.
 * This class is for the VKSdk library initialization
 */
public class FriendsListAdapter extends BaseAdapter {
    private final Context ctx;
    private final LayoutInflater inflater;
    private final VKUsersArray userFulls;
    private final ImageLoader imageLoader;

    public FriendsListAdapter(Context context, VKUsersArray userFullArrayList) {
        ctx = context;
        userFulls = userFullArrayList;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(context);
    }


    @Override
    public int getCount() {
        return userFulls.size();
    }

    @Override
    public Object getItem(int position) {
        return userFulls.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;
        ViewHolder holder;
        if (view == null) {
            view = inflater.inflate(R.layout.friends_item, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView) view.findViewById(R.id.textName);
            holder.imageView = (ImageView) view.findViewById(R.id.imageFriendAva);
            holder.textOnline = (TextView) view.findViewById(R.id.textOnline);
            holder.checkBox = (CheckBox) view.findViewById(R.id.checkBox);
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    userFulls.get(position).setChecked(isChecked);
                }
            });
            view.setTag(holder);

        } else {
            holder = (ViewHolder) view.getTag();
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    userFulls.get(position).setChecked(isChecked);
                }
            });
        }
        String online = "   " + parent.getResources().getString(R.string.online);
        String text = "   " + userFulls.get(position).first_name + " " + userFulls.get(position).last_name;
        if (userFulls.get(position).online)
            holder.textOnline.setText(online);
        else
            holder.textOnline.setText("");
        holder.textView.setText(text);
        imageLoader.DisplayImage(userFulls.get(position).photo_50, holder.imageView, 25);
        holder.checkBox.setChecked(userFulls.get(position).checked);
        holder.checkBox.setTag(userFulls.get(position));
        return view;
    }

    private class ViewHolder {
        CheckBox checkBox;
        ImageView imageView;
        TextView textOnline;
        TextView textView;
    }
}

