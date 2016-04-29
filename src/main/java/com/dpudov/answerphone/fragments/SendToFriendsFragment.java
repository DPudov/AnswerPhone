package com.dpudov.answerphone.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.dpudov.answerphone.R;
import com.dpudov.answerphone.activity.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SendToFriendsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SendToFriendsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SendToFriendsFragment extends android.app.Fragment {
    private static final String TITLE = "title";
    private EditText editText;
    private CheckFriends2Fragment checkFriends2Fragment;
    private OnFragmentInteractionListener mListener;

    public SendToFriendsFragment() {
        // Required empty public constructor
    }


    public static SendToFriendsFragment newInstance() {
        SendToFriendsFragment fragment = new SendToFriendsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (savedInstanceState!=null){
            String title = savedInstanceState.getString(TITLE);
            getActivity().setTitle(title);
        }else{
            getActivity().setTitle(R.string.msg_now);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_send_to_friends, container, false);
        checkFriends2Fragment = new CheckFriends2Fragment();
        editText = (EditText) v.findViewById(R.id.editMsgToFr);
        Button sendFrButton = (Button) v.findViewById(R.id.sendFrButton);
        sendFrButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Смотрим сообщение и отправляем на главную
                ((MainActivity) getActivity()).setMsg(editText.getText().toString());

                //После клика вызываем выбор друзей
                android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.container, checkFriends2Fragment);
                getActivity().setTitle(R.string.checkFrFrag);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String title = (String) getActivity().getTitle();
        outState.putString(TITLE, title);
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
}
