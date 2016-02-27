package org.pg.telegramchallenge;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
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

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.Adapters.ChatAdapter;
import org.pg.telegramchallenge.utils.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements ObserverApplication.ChatObserver, ObserverApplication.ConcreteChatObserver,  ObserverApplication.OnGetChatHistoryObserver {
//    final CountDownLatch latch = new CountDownLatch(1);

    private Toolbar toolbar;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private LinearLayoutManager layoutManager;

    private TdApi.Chat chat;

    private long chatId;
    private int groupId = 0;
    private long myId;

    private boolean loading = true;

    // TODO: ROMAN - TRY DIFFERENT VISIBLE THRESHOLD AND OFFSET
    private int totalItems, visibleThreshold = 30, firstVisible, totalVisible, previousTotal = 0;
    private int offset = 10;


    private int msgStartFromId = 0;

//    public void setChat(TdApi.Chat chat) {
//        this.chat = chat;
//    }

    public ObserverApplication getApplication(){
        return (ObserverApplication) getActivity().getApplication();
    }

    public ChatFragment(){
    }

    public static ChatFragment newInstance(TdApi.Chat chat) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putLong("chatId", chat.id);
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

        myId = ObserverApplication.userMe.id;

        chatId = getArguments().getLong("chatId");
        chat = ObserverApplication.chats.get(chatId);

        Toast.makeText(getActivity(), String.valueOf(chatId), Toast.LENGTH_SHORT).show();

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setPadding(0, Utils.getStatusBarHeight(getActivity()), 0, 0);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

        layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(false);

        getApplication().sendRequest(new TdApi.OpenChat(chatId));

        chatRecyclerView = (RecyclerView) view.findViewById(R.id.chat_recycler_view);
        layoutManager.supportsPredictiveItemAnimations();
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setItemAnimator(new DefaultItemAnimator());
        chatAdapter = new ChatAdapter(getApplication(), getActivity(), chat.id);
        chatRecyclerView.setAdapter(chatAdapter);

        chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItems = layoutManager.getItemCount();
                totalVisible = layoutManager.getChildCount();
                firstVisible = layoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItems > previousTotal) {
                        loading = false;
                        previousTotal = totalItems;
                    }
                }

                if (!loading && totalItems-totalVisible <= firstVisible + visibleThreshold) {
                    getApplication().sendRequest(new TdApi.GetChatHistory(chatId, msgStartFromId, 0, offset));

                    loading = true;
                }
            }
        });
        if (chat.type instanceof TdApi.GroupChatInfo) {
            groupId = ((TdApi.GroupChatInfo) chat.type).group.id;
            if (!ObserverApplication.groupsFull.containsKey(groupId)) {
                new FetchGroupFull().execute(groupId);
            } else {
                getApplication().sendRequest(new TdApi.GetChatHistory(chatId, msgStartFromId, 0, 50));
            }
        } else {
            getApplication().sendRequest(new TdApi.GetChatHistory(chatId, msgStartFromId, 0, 50));
        }

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

    @Override
    public void proceed(TdApi.Messages obj) {

        if (obj.totalCount > 0) {
            msgStartFromId = obj.messages[obj.totalCount - 1].id;
        }
    }

    private class FetchGroupFull extends AsyncTask<Integer, Void, Integer> implements Client.ResultHandler{
        CountDownLatch latch = new CountDownLatch(1);
        @Override
        protected Integer doInBackground(Integer... params) {
            getApplication().sendRequest(new TdApi.GetGroupFull(groupId), this);
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        //TODO: add spinner
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            getApplication().sendRequest(new TdApi.GetChatHistory(chatId, msgStartFromId, 0, 50));

            chatAdapter.notifyDataSetChanged();
        }

        @Override
        public void onResult(TdApi.TLObject object) {
            ObserverApplication.groupsFull.put(((TdApi.GroupFull) object).group.id, (TdApi.GroupFull) object);
            for (TdApi.ChatParticipant participant : ((TdApi.GroupFull) object).participants) {
                ObserverApplication.users.put(participant.user.id, participant.user);
            }
            latch.countDown();
        }
    }
}
