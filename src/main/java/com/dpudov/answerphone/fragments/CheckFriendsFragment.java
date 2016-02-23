package com.dpudov.answerphone.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.dpudov.answerphone.MainActivity;
import com.dpudov.answerphone.R;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiUserFull;
import com.vk.sdk.api.model.VKUsersArray;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CheckFriendsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CheckFriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class CheckFriendsFragment extends android.support.v4.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ArrayList<String> users;
    // TODO: Rename and change types of parameters
    @SuppressWarnings("FieldCanBeLocal")
    private String mParam1;
    @SuppressWarnings("FieldCanBeLocal")
    private String mParam2;
    private OnFragmentInteractionListener mListener;
    private ListView listView;
    private Button saveButton;
    private SettingsFragment settingsFragment;
    private int[] userIds;
    public Bitmap photo_50;
  //  private ImageView imageView;

    public CheckFriendsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CheckFriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CheckFriendsFragment newInstance(String param1, String param2) {
        CheckFriendsFragment fragment = new CheckFriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_check_friends, container, false);
        listView = (ListView) v.findViewById(R.id.listView);
        settingsFragment = new SettingsFragment();
        saveButton = (Button) v.findViewById(R.id.saveButton);

        //imageView = (ImageView) v.findViewById(R.id.imageView3);
        VKSdk.wakeUpSession(getActivity());
        VKRequest request = VKApi.friends().get(VKParameters.from(VKApiConst.FIELDS, "id, first_name, last_name, photo_50", "order", "hints"));//
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                //Заполнение массива друзьями
                final VKUsersArray list;
                list = (VKUsersArray) response.parsedModel;
                //Попытки получить аватарку
                //photo_50 = getBitmapFromUrl(list.get(0).photo_50);
                //imageView.setImageBitmap(photo_50);
                ArrayAdapter<VKApiUserFull> arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.my_multiple_choice, list);
                listView.setAdapter(arrayAdapter);

                saveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SparseBooleanArray sbArray = listView.getCheckedItemPositions();
                        userIds = new int[sbArray.size()];
                        for (int i = 0; i < sbArray.size(); i++) {
                            int key = sbArray.keyAt(i);
                            if (sbArray.get(key)) {
                                userIds[i] = list.get(key).getId();
                            }
                        }
                        ((MainActivity) getActivity()).setUsersToSendAuto(userIds);
                        android.support.v4.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.container, settingsFragment);
                        getActivity().setTitle(R.string.settFrag);
                        ft.commit();

                    }
                });

            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Toast.makeText(getActivity(), R.string.try_again_internet, Toast.LENGTH_SHORT).show();
            }
        });


        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public static Bitmap getBitmapFromUrl(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            InputStream inputStream = httpURLConnection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return bitmap;
        } catch (IOException e) {
            return null;
        }
    }
}
