package com.gmail.tuannguyen.imapp.contact;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.gmail.tuannguyen.imapp.R;
import com.gmail.tuannguyen.imapp.chat.ChatActivity;

import java.util.ArrayList;

import static com.gmail.tuannguyen.imapp.util.Common.FRIEND_REQUEST_FROM;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * <p/>
 * to handle interaction events.
 * Use the {@link ContactFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
//{@link ChatFragment.OnFragmentInteractionListener} interface
public class ContactFragment extends Fragment {


    private static ContactAdapter contactAdapter = null;
    private static boolean active = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;
    private static BroadcastReceiver broadcastReceiver = null;

    public static BroadcastReceiver getBroadcastReceiver() {
        return broadcastReceiver;
    }

    public static ContactAdapter getContactAdapter() {
        return contactAdapter;
    }
//private OnFragmentInteractionListener mListener;

    public ContactFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ChatFragment.
     */
    public static ContactFragment newInstance() {
        ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_contact, container, false);
        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
                    "Contacts");
        } catch (Exception e) {
            e.printStackTrace();
        }
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.contact_container);
        listView = (ListView) view.findViewById(R.id.contact_list_view);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContactList();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        refreshContactList();

        //Set auto-scroll when a new contact come
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String contact = contactAdapter.getContactItem(position).getUserNameWithoutServerAddr();
                Intent intent = new Intent(getContext(), ChatActivity.class);
                intent.putExtra("CONTACT", contact);
                startActivity(intent);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*)
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                        */
                promptAddFriend(inflater);
            }
        });

        return view;
    }

    public void promptAddFriend(final LayoutInflater inflater) {
        View promptView = inflater.inflate(R.layout.prompt, null);
        final EditText editText = (EditText) promptView.findViewById(R.id.editText);
        TextView description = (TextView) promptView.findViewById(R.id.description);

        description.setText("Add Friend");
        editText.requestFocus();
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext())
                .setView(promptView)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String buddy = editText.getText() + "@" + getString(R.string.server_address);
                        Contacts.addContact(buddy);
                        contactAdapter.add(new Contact(buddy, Contact.Type.FRIEND_REQUEST_FROM_ME));
                        contactAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }


    public void initBroadcastReceiver() {
        this.broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateUi(intent);
            }
        };
    }

    public void updateUi(Intent intent) {
        String requestFrom = intent.getStringExtra(FRIEND_REQUEST_FROM);
        contactAdapter.add(new Contact(requestFrom, Contact.Type.FRIEND_REQUEST_TO_ME));
        contactAdapter.notifyDataSetChanged();
    }

    public void refreshContactList() {
        //Set adapter for listView
        ArrayList<Contact> contactList = Contacts.getContacts();
        contactAdapter = new ContactAdapter(getActivity(), this, contactList);
        listView.setAdapter(contactAdapter);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {

    }



    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }

    public static boolean isActive() {
        return active;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


}
