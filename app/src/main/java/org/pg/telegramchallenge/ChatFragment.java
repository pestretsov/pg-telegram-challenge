package org.pg.telegramchallenge;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.Adapters.ChatAdapter;
import org.pg.telegramchallenge.utils.Utils;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements ObserverApplication.ChatObserver, ObserverApplication.ConcreteChatObserver,  ObserverApplication.OnGetChatHistoryObserver {

    private Toolbar toolbar;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private LinearLayoutManager layoutManager;
    private EditText messageEditText;
    private ImageView messageSendButton;
    private ImageView recordVoiceButton;
    private ImageView attachItemButton;

    private TdApi.Chat chat;

    private long chatId;
    private int groupId = 0;
    private long myId;

    private boolean loading = true;

    // TODO: ROMAN - TRY DIFFERENT VISIBLE THRESHOLD AND OFFSET
    private int totalItems, visibleThreshold = 30, firstVisible, totalVisible, previousTotal = 0;
    private int offset = 10;

    private int msgStartFromId = 0;

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

        getApplication().sendRequest(new TdApi.OpenChat(chatId));

        chatRecyclerView = (RecyclerView) view.findViewById(R.id.chat_recycler_view);

        messageSendButton = (ImageView) view.findViewById(R.id.btn_send);
        recordVoiceButton = (ImageView) view.findViewById(R.id.btn_right2);
        attachItemButton = (ImageView) view.findViewById(R.id.btn_right1);



        messageEditText = (EditText) view.findViewById(R.id.message_input);
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (before == 0 && count > 0) {
                    recordVoiceButton.setVisibility(View.GONE);
                    attachItemButton.setVisibility(View.GONE);
                    messageSendButton.setVisibility(View.VISIBLE);
                }

                if (start == 0 && before > 0) {
                    messageSendButton.setVisibility(View.GONE);
                    recordVoiceButton.setVisibility(View.VISIBLE);
                    attachItemButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        messageSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = messageEditText.getText().toString();
                TdApi.InputMessageContent msgContent = new TdApi.InputMessageText(msg,true, null);
                getApplication().sendRequest(new TdApi.SendMessage(chatId, 0, false, false, false, new TdApi.ReplyMarkupNone(), msgContent));
                messageEditText.getText().clear();
            }
        });

        chatRecyclerView.setLayoutManager(layoutManager);
        ((SimpleItemAnimator)chatRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
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
                new FetchGroupFull(getApplication(), chatAdapter, groupId, chatId, msgStartFromId).execute();
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
//        chatAdapter.notifyDataSetChanged();
    }

    @Override
    public void proceed(TdApi.UpdateNewMessage obj) {
        chatAdapter.proceed(obj);
        scrollToNewMessage();
    }

    @Override
    public void proceed(TdApi.Message obj) {
        chatAdapter.proceed(obj);
        scrollToNewMessage();
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

    private void scrollToNewMessage() {
        chatRecyclerView.scrollToPosition(0);
    }

    private static class FetchGroupFull extends AsyncTask<Integer, Void, Object> implements Client.ResultHandler {
        private CountDownLatch latch = new CountDownLatch(1);
        private ObserverApplication context = null;
        private int groupId;
        private long chatId;
        private int msgId;
        private ChatAdapter adapter = null;

        FetchGroupFull(ObserverApplication context, ChatAdapter adapter ,int groupId, long chatId, int msgId) {
            this.context = context;
            this.groupId = groupId;
            this.chatId = chatId;
            this.msgId = msgId;
            this.adapter = adapter;
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            context.sendRequest(new TdApi.GetGroupFull(groupId), this);
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        //TODO: add spinner
        @Override
        protected void onPostExecute(Object object) {
            super.onPostExecute(object);

            context.sendRequest(new TdApi.GetChatHistory(chatId, msgId, 0, 50));

            adapter.notifyDataSetChanged();
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
