package com.dpudov.answerphone.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.dpudov.answerphone.R;
import com.vk.sdk.api.VKError;
import com.vk.sdk.dialogs.VKShareDialog;

import static com.vk.sdk.VKUIHelper.getApplicationContext;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ShareFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ShareFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ShareFragment extends android.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ShareFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ShareFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ShareFragment newInstance(String param1, String param2) {
        ShareFragment fragment = new ShareFragment();
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
        View v = inflater.inflate(R.layout.fragment_share, container, false);
        Button shareButton = (Button) v.findViewById(R.id.shareButton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new VKShareDialog()
                        .setText("ANSWERPHONE - ЭТО ДАЖЕ ЛУЧШЕ ЧЕМ МЕМАС" +
                                "@send by AnswerPhone for VK Android.")
                        .setAttachmentLink("DPudov", "dimapudov99@gmail.com")
                        .setShareDialogListener(new VKShareDialog.VKShareDialogListener() {
                            @Override
                            public void onVkShareComplete(int postId) {
                                Toast toastComplete = Toast.makeText(getApplicationContext(), "Успешно отправлено", Toast.LENGTH_LONG);
                                toastComplete.show();
                            }

                            @Override
                            public void onVkShareCancel() {
                                Toast toastCancel = Toast.makeText(getApplicationContext(), "Отменено :(", Toast.LENGTH_LONG);
                                toastCancel.show();
                            }

                            @Override
                            public void onVkShareError(VKError error) {
                                Toast toastError = Toast.makeText(getApplicationContext(), "Сорри, но тут произошла ошибка. Возможно, ты потерял интернет :(", Toast.LENGTH_LONG);
                                toastError.show();
                            }
                        }).show(getFragmentManager(), "VK_SHARE_DIALOG");
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
    public void onAttach(Context context) {
        super.onAttach(context);
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
