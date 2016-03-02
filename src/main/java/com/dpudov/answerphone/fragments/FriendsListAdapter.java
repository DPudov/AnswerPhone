package com.dpudov.answerphone.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
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
import com.nostra13.universalimageloader.core.display.CircleBitmapDisplayer;
import com.vk.sdk.api.model.VKUsersArray;

import java.io.InputStream;


/**
 * Created by DPudov on 25.02.2016.
 * This class is for the VKSdk library initialization
 */
public class FriendsListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    VKUsersArray userFulls;
    DisplayImageOptions options;


    FriendsListAdapter(Context context, VKUsersArray userFullArrayList) {
        ctx = context;
        userFulls = userFullArrayList;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .displayer(new CircleBitmapDisplayer(Color.WHITE, 5))
                .build();
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
        //checkBox.setTag(position);
        //checkBox.setChecked(userFulls.get(position).checked)

        new DownloadImageTask(imageView).execute(userFulls.get(position).photo_50);
        //ImageLoader.getInstance().displayImage(userFulls.get(position).photo_50, imageView, options);
        return view;
    }

    /**
     * this class is for downloading and setting up images
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


}
