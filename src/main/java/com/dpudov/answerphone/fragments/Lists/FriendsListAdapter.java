package com.dpudov.answerphone.fragments.Lists;

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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.vk.sdk.api.model.VKUsersArray;


/**
 * Created by DPudov on 25.02.2016.
 * This class is for the VKSdk library initialization
 */
public class FriendsListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    VKUsersArray userFulls;
    DisplayImageOptions options;
    ImageLoader imageLoader;

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
        if (view == null) {
            view = inflater.inflate(R.layout.friends_item, parent, false);
        }
        String text = "   " + userFulls.get(position).first_name + " " + userFulls.get(position).last_name;
        String online = "   " + parent.getResources().getString(R.string.online);
        TextView textView = (TextView) view.findViewById(R.id.textName);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageFriendAva);
        TextView textOnline = (TextView) view.findViewById(R.id.textOnline);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userFulls.get(position).setChecked(isChecked);
            }
        });
        textView.setText(text);
        if (userFulls.get(position).online)
            textOnline.setText(online);
        else
            textOnline.setText("");
        imageLoader.DisplayImage(userFulls.get(position).photo_50, imageView);
        //checkBox.setTag(position);
        //checkBox.setChecked(userFulls.get(position).checked)

        // new DownloadImageTask(imageView).execute(userFulls.get(position).photo_50);
        //ImageLoader.getInstance().displayImage(userFulls.get(position).photo_50, imageView, options);
        return view;
    }


}
