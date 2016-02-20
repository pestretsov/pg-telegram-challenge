package org.pg.telegramchallenge;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.Adapters.ChatAdapter;
import org.pg.telegramchallenge.utils.Utils;

import java.util.LinkedList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements ObserverApplication.ChatObserver, ObserverApplication.ConcreteChatObserver {

    private Toolbar toolbar;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private LinearLayoutManager layoutManager;

    private TdApi.Chat chat;

    private long chatId;
    private long myId;

    public void setChat(TdApi.Chat chat) {
        this.chat = chat;
    }

    public ObserverApplication getApplication(){
        return (ObserverApplication) getActivity().getApplication();
    }

    public ChatFragment(){
    }

    public static ChatFragment newInstance(TdApi.Chat chat) {
        ChatFragment fragment = new ChatFragment();
        fragment.setChat(chat);
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

        myId = ObserverApplication.userMe.id;
        chatId = chat.id;

        Toast.makeText(getActivity(), String.valueOf(chatId), Toast.LENGTH_SHORT).show();

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setPadding(0, Utils.getStatusBarHeight(getActivity()), 0, 0);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(false);

        List<TdApi.User> users = new LinkedList<>();

        getApplication().sendRequest(new TdApi.OpenChat(chatId));
        users.add(ObserverApplication.userMe);
        if (chat.id < 0) {
            getApplication().sendRequest(new TdApi.GetGroupFull(((TdApi.GroupChatInfo)chat.type).group.id));
        } else {
            users.add(((TdApi.PrivateChatInfo)chat.type).user);
        }

        chatAdapter = new ChatAdapter(getApplication(), getActivity(), chatId, users);
        chatRecyclerView = (RecyclerView) view.findViewById(R.id.chat_recycler_view);
        layoutManager.supportsPredictiveItemAnimations();
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setItemAnimator(new DefaultItemAnimator());

        chatRecyclerView.setAdapter(chatAdapter);

//        getApplication().sendRequest(new TdApi.GetChat(chatId));

        getApplication().sendRequest(new TdApi.GetChatHistory(chatId, 0, 0, 20));

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

    @Override
    public void proceed(TdApi.Chat obj) {

    }

    @Override
    public void proceedConcrete(TdApi.UpdateNewMessage obj) {
        chatAdapter.proceed(obj);

//        layoutManager.scrollToPosition(0);
    }

    @Override
    public long getChatId() {
        return chatId;
    }
}
