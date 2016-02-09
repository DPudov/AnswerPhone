package com.dpudov.answerphone.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dpudov.answerphone.R;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKUsersArray;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SendFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SendFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SendFragment extends android.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private EditText editText;
    private Button sendButton;
    private String message;
    private int[] userIdReturn;
    private int[] userId;
private Button nttbutton;
    public SendFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SendFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SendFragment newInstance(String param1, String param2) {
        SendFragment fragment = new SendFragment();
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
        View v = inflater.inflate(R.layout.fragment_send, container, false);
        sendButton = (Button) v.findViewById(R.id.sendButton);
        editText = (EditText) v.findViewById(R.id.editText2);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMe(134132102);
                sendMe(238489071);
            }
        });
        nttbutton = (Button)v.findViewById(R.id.nttbutton);
        nttbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    userId = getMsg();
                    Toast.makeText(getActivity(), Integer.toString(userId[0]), Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    Toast.makeText(getActivity(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Inflate the layout for this fragment
        return v;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    private void sendMe(int user) {

        message = editText.getText().toString().concat(getString(R.string.defaultMsg));
        VKRequest request = new VKRequest("messages.send", VKParameters.from(VKApiConst.USER_ID, user, VKApiConst.MESSAGE, message));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                Toast.makeText(getActivity(), R.string.sentMsg, Toast.LENGTH_SHORT).show();


            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
                Toast.makeText(getActivity(), R.string.VK_Err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int[] getMsg() {
        //TODO Ошибка тут. Исправляй
        VKRequest request = VKApi.messages().get(VKParameters.from("out", 0, "time_offset", 3600));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                VKUsersArray messages = (VKUsersArray) response.parsedModel;
                userIdReturn = null;
                if (messages.size() > 0) {
                    // Пришло новое сообщение. Возвращаем true
                    ArrayList<Integer> userArr = new ArrayList<>();
                    ArrayList<Integer> userArrCopy = new ArrayList<>();
                    int firstId = messages.get(0).getId();
                    int id;
                    int c = 0;
                    userArr.add(0, firstId);
                    for (int i = 0; i < messages.size(); i++) {
                        id = messages.get(i).getId();
                        if (!(firstId == id)) {
                            c++;
                            userArr.add(c, id);
                            userArrCopy.add(c, id);
                        }
                    }
                    //проверка на соответствие с выбранными друзьями
//После всего создаем userIds, который проверяем на повторы и нули и закидываем в итог
                    int[] userIds = new int[userArrCopy.size()];
                    for (int i = 0; i < userArrCopy.size(); i++) {
                        userIds[i] = userArrCopy.get(i);
                    }
                    int counter = 0;
                    for (int userId1 : userIds) {
                        if (!(userId1 == 0)) {
                            counter++;
                        }
                    }
                    int count = 0;
                    userIdReturn = new int[counter];
                    for (int userId1 : userIds) {
                        if (!(userId1 == 0)) {
                            userIdReturn[count] = userId1;
                            count++;
                        }
                    }
                }

            }

            @Override
            public void onError(VKError error) {
                super.onError(error);
            }

        });
        return userIdReturn;
    }
}

