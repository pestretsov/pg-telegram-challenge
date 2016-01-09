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

    private int visibleItems, totalItems, previousTotal = 0, firstVisibleItem, visibleThreshold = 10;
    private boolean loading = true;

    private int nextLimit = 50;
    private int nextOffset = 0;

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

        chatListAdapter = new ChatListAdapter();
        chatListRecyclerView.setAdapter(chatListAdapter);
        chatListRecyclerView.setLayoutManager(layoutManager);

        chatListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItems = recyclerView.getChildCount();
                totalItems = layoutManager.getItemCount();
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItems > previousTotal) {
                        loading = false;
                        previousTotal = totalItems;
                    }
                }

                if (!loading && totalItems - visibleItems <= firstVisibleItem + visibleThreshold) {
                    nextOffset+=nextLimit;

                    getApplication().sendRequest(new TdApi.GetChats(nextOffset, nextLimit));

                    loading = true;
                }
            }
        });

        getApplication().sendRequest(new TdApi.GetChats(nextOffset, nextLimit));

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
    }

}
