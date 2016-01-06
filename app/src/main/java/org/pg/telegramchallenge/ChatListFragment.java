package org.pg.telegramchallenge;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.Adapters.ChatListAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment implements ObserverApplication.OnErrorObserver,
        ObserverApplication.OnUpdateNewMessageObserver, ObserverApplication.ChatsObserver {

    private RecyclerView chatListRecyclerView;
    private ChatListAdapter chatListAdapter;
    private LinearLayoutManager layoutManager;

    public class TestMessage {
        final String message = "FUCK THIS SHIT MAN";
    }

    public ObserverApplication getApplication(){
        return (ObserverApplication) getActivity().getApplication();
    }

    public ChatListFragment() {
        // Required empty public constructor
    }

    public static ChatListFragment newInstance() {

        Bundle args = new Bundle();

        ChatListFragment fragment = new ChatListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        getApplication().addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);


        layoutManager = new LinearLayoutManager(getActivity());
        chatListRecyclerView = (RecyclerView)view.findViewById(R.id.chatListRecyclerView);

//        list = new TdApi.Chat[10];

        chatListAdapter = new ChatListAdapter();
        chatListRecyclerView.setAdapter(chatListAdapter);
        chatListRecyclerView.setLayoutManager(layoutManager);

        getApplication().sendRequest(new TdApi.GetChats(0, 10));

        return view;
    }

    @Override
    public void proceed(TdApi.Error err) {

    }

    @Override
    public void proceed(TdApi.UpdateNewMessage obj) {

    }

    @Override
    public void proceed(TdApi.Chats obj) {
        chatListAdapter.changeData(obj.chats);
        Log.e("CHATS COUNT",String.valueOf(obj.chats.length));
    }

}
