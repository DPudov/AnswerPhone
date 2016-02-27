package com.dpudov.answerphone.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;

import java.io.InputStream;
import java.util.LinkedHashSet;


/**
 * Created by DPudov on 25.02.2016.
 * This class is for the VKSdk library initialization
 */
public class FriendsListAdapter extends BaseAdapter {
    Context ctx;
    LayoutInflater inflater;
    VKUsersArray userFulls;
    LinkedHashSet<Integer> pos;

    FriendsListAdapter(Context context, VKUsersArray userFullArrayList) {
        ctx = context;
        userFulls = userFullArrayList;
        inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    VKUsersArray getUserFulls() {
        VKUsersArray vkUsersArray = new VKUsersArray();
        for (int i = 0; i < userFulls.size(); i++) {
            if (userFulls.get(i).checked) {
                vkUsersArray.add(userFulls.get(i));
            }
        }
        return vkUsersArray;
    }

    VKApiUserFull getUser(int position) {
        return ((VKApiUserFull) getItem(position));
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
        VKApiUserFull userFull = getUser(position);
        TextView textView = (TextView) view.findViewById(R.id.textName);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageFriendAva);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        checkBox.setOnCheckedChangeListener(changeListener);
        checkBox.setTag(position);
        checkBox.setChecked(userFull.checked);
        new DownloadImageTask(imageView).execute(userFulls.get(position).photo_50);
        String text = userFulls.get(position).first_name + " " + userFulls.get(position).last_name;
        textView.setText(text);
        return view;
    }

    CompoundButton.OnCheckedChangeListener changeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            getUser((Integer) buttonView.getTag()).checked = isChecked;
        }
    };


    public LinkedHashSet getMySet() {
        return this.pos;
    }

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
