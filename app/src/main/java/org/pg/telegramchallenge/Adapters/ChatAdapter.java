package org.pg.telegramchallenge.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.ChatListActivity;
import org.pg.telegramchallenge.ObserverApplication;
import org.pg.telegramchallenge.R;
import org.pg.telegramchallenge.utils.Utils;
import org.pg.telegramchallenge.views.ChatListItemView;
import org.pg.telegramchallenge.views.TextUserMessageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by artemypestretsov on 2/18/16.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatVH> implements ObserverApplication.OnGetChatHistoryObserver {
    private long chatId;
    private int lastPos = 0;

    private Context context;
    private ChatListActivity activity;

    private LinkedList<TdApi.Message> messagesList = new LinkedList<>();
    private Map<Integer, TdApi.User> usersMap = new HashMap<>();

    public ChatAdapter(Context context, Activity activity, long chatId, List<TdApi.User> users) {
        this.chatId = chatId;

        this.context = (ObserverApplication) context;
        this.activity = (ChatListActivity) activity;

        for (TdApi.User user : users) {
            usersMap.put(user.id, user);
        }
    }

    @Override
    public ChatAdapter.ChatVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_cell, parent, false);

        return new ChatVH(view);
    }

    @Override
    public void onBindViewHolder(ChatVH holder, int position) {
        if (messagesList == null) {
            return;
        }

        TdApi.Message msg = messagesList.get(position);
        TdApi.User usr = usersMap.get(msg.fromId);

        holder.chatItemView.setBarVisability(false);
        holder.chatItemView.setDateVisability(false);

        holder.chatItemView.setTitle(Utils.getFullName(usr.firstName, usr.lastName));

        String text = "BOSS";
        if (messagesList.get(position).content instanceof TdApi.MessageText && ((TdApi.MessageText) messagesList.get(position).content).text.length() > 0) {
            text = ((TdApi.MessageText)messagesList.get(position).content).text;
            holder.chatItemView.setText(text);
        } else {
            holder.chatItemView.setText(text);
        }
    }


    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public void proceed(TdApi.Messages obj) {
        messagesList.addAll(Arrays.asList(obj.messages));
        this.notifyItemRangeInserted(lastPos, obj.totalCount);
        lastPos+=obj.totalCount;
    }


    public void proceed(TdApi.UpdateNewMessage obj) {
        messagesList.addFirst(obj.message);
        this.notifyItemInserted(0);
    }


    public class ChatVH extends RecyclerView.ViewHolder {

        private TextUserMessageView chatItemView;

        public ChatVH(View itemView) {
            super(itemView);

            chatItemView = (TextUserMessageView) itemView.findViewById(R.id.chat_itemview);
        }
    }
}
