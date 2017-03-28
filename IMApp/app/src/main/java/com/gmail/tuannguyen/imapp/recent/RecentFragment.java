package com.gmail.tuannguyen.imapp.recent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gmail.tuannguyen.imapp.R;
import com.gmail.tuannguyen.imapp.chat.ChatActivity;
import com.gmail.tuannguyen.imapp.chat.ChatMessage;
import com.gmail.tuannguyen.imapp.connection.Connection;
import com.gmail.tuannguyen.imapp.db.MessageDBHelper;
import com.gmail.tuannguyen.imapp.util.CommonUtil;
import com.google.gson.Gson;

import java.util.ArrayList;

import static com.gmail.tuannguyen.imapp.util.Common.CHAT_MESSAGE;
import static com.gmail.tuannguyen.imapp.util.Common.CHAT_MESSAGE_TYPE;
import static com.gmail.tuannguyen.imapp.util.Common.MessageType;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RecentFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecentFragment extends Fragment {


    private OnFragmentInteractionListener mListener;
    private static RecentAdapter recentAdapter = null;
    private static BroadcastReceiver broadcastReceiver = null;

    public RecentFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RecentFragment.
     */
    public static RecentFragment newInstance() {
        RecentFragment fragment = new RecentFragment();

        /*
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        */
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recent, container, false);


        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
                    "Chat");
        } catch (Exception e) {
            e.printStackTrace();
        }

        final ListView listView = (ListView) view.findViewById(R.id.recent_list_view);
        //Set adapter for listView
        if (Connection.getInstance() == null || Connection.getInstance().getXMPPTCPConnection() == null) {
            return view;
        }
        String authenticatedUser = CommonUtil.getAuthenticatedUserName(false);
        if (authenticatedUser == null)
            return view;
        ArrayList<RecentItem> recentItems = MessageDBHelper.getInstance(getContext(), authenticatedUser
        ).getRecentContacts();
        recentAdapter = new RecentAdapter(getActivity(), recentItems);
        listView.setAdapter(recentAdapter);

        //Set auto-scroll when a new message come
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setStackFromBottom(false);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecentItem item = recentAdapter.getRecentContactItem(position);
                item.setSeen(true);
                recentAdapter.notifyDataSetChanged();
                String contact = item.getContact()
                        .getUserNameWithoutServerAddr();
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("CONTACT", contact);
                startActivity(intent);
            }
        });

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    public void initBroadcastReceiver() {
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUiForNewMessage(intent);
            }
        };
    }

    public static BroadcastReceiver getBroadcastReceiver() {
        return broadcastReceiver;
    }

    public void updateUiForNewMessage(Intent intent) {
        boolean seen = true;
        if (intent.getStringExtra(CHAT_MESSAGE_TYPE).equals(MessageType.UNSEEN_INCOMING_MESSAGE_TYPE)) {
            seen = false;
        }
        String newChatMessage = intent.getStringExtra(CHAT_MESSAGE);
        Gson gson = new Gson();
        ChatMessage chatMessage = gson.fromJson(newChatMessage, ChatMessage.class);
        String contact = chatMessage.getContact();
        updateNewMessageForAdapter(contact, chatMessage, seen);

    }

    public void updateNewMessageForAdapter(String contact, ChatMessage chatMessage, boolean seen) {
        recentAdapter.updateNewMessageForContact(contact, chatMessage, seen);
        recentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {

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
