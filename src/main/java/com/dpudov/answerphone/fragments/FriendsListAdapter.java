package com.dpudov.answerphone.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dpudov.answerphone.R;
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

    FriendsListAdapter(Context context, VKUsersArray userFullArrayList) {
        ctx = context;
        userFulls = userFullArrayList;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.friends_item, parent, false);
        }
        String text = "   " + userFulls.get(position).first_name + " " + userFulls.get(position).last_name;
        TextView textView = (TextView) view.findViewById(R.id.textName);
        textView.setText(text);
        TextView textOnline = (TextView) view.findViewById(R.id.textOnline);

        String online = "   " + R.string.online;
        if (userFulls.get(position).online)
            textOnline.setText(online);
        else
            textOnline.setText("");
        //checkBox.setTag(position);
        //checkBox.setChecked(userFulls.get(position).checked)


        ImageView imageView = (ImageView) view.findViewById(R.id.imageFriendAva);
        new DownloadImageTask(imageView).execute(userFulls.get(position).photo_50);

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
