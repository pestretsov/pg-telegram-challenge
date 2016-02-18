package org.pg.telegramchallenge;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.Adapters.ChatAdapter;
import org.pg.telegramchallenge.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private Toolbar toolbar;
    private RecyclerView chatRecyclerView;
    private RecyclerView.Adapter chatAdapter;
    private LinearLayoutManager layoutManager;

    private long chatId;
    private long myId;

    public ObserverApplication getApplication(){
        return (ObserverApplication) getActivity().getApplication();
    }

    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(long chatId) {
        Bundle args = new Bundle();
        args.putLong("chatId", chatId);
        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);


        Bundle args = getArguments();
        chatId = args.getLong("chatId", 0);
        myId = ObserverApplication.userMe.id;
        Toast.makeText(getActivity(), String.valueOf(chatId), Toast.LENGTH_SHORT).show();

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setPadding(0, Utils.getStatusBarHeight(getActivity()), 0, 0);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        layoutManager = new LinearLayoutManager(getActivity());
        chatAdapter = new ChatAdapter(getApplication(), getActivity() ,chatId);
        chatRecyclerView = (RecyclerView) view.findViewById(R.id.chat_recycler_view);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        getApplication().sendRequest(new TdApi.GetChatHistory(chatId, 0, 0, 50));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getApplication().addObserver(this);
        getApplication().addObserver(chatAdapter);
    }

    @Override
    public void onPause(){
        super.onPause();

        // check
        getApplication().sendRequest(new TdApi.CloseChat(chatId));
        getApplication().removeObserver(this);
        getApplication().removeObserver(chatAdapter);
    }

}
