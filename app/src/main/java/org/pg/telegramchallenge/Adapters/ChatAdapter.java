package org.pg.telegramchallenge.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.MainActivity;
import org.pg.telegramchallenge.ObserverApplication;
import org.pg.telegramchallenge.R;
import org.pg.telegramchallenge.views.TextUserMessageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by artemypestretsov on 2/18/16.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatVH> implements ObserverApplication.OnGetChatHistoryObserver {

    private long chatId;
    private int lastPos = 0;

    private Context context;
    private MainActivity activity;

    private static volatile List<TdApi.Message> messagesList = new ArrayList<>();

    public ChatAdapter(Context context, Activity activity, long chatId) {
        this.chatId = chatId;

        this.context = (ObserverApplication) context;
        this.activity = (MainActivity) activity;
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

        String text = "Три";
        if (messagesList.get(position).content instanceof TdApi.MessageText && ((TdApi.MessageText) messagesList.get(position).content).text.length() > 0) {
            ((TdApi.MessageText)messagesList.get(position).content).text = text;
            holder.chatItemView.setText(((TdApi.MessageText)messagesList.get(position).content).text);
        } else {
            holder.chatItemView.setText(text);
        }

        holder.chatItemView.setBarVisability(false);
        holder.chatItemView.setDateVisability(false);
    }


    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public void proceed(TdApi.Messages obj) {

//        messagesList.addAll(Arrays.asList(obj.messages));
        for (TdApi.Message msg: obj.messages) {
            messagesList.add(msg);
        }

        this.notifyItemRangeInserted(lastPos, obj.totalCount);
        lastPos+=obj.totalCount;
    }


    public class ChatVH extends RecyclerView.ViewHolder {

        private TextUserMessageView chatItemView;

        public ChatVH(View itemView) {
            super(itemView);

            chatItemView = (TextUserMessageView) itemView.findViewById(R.id.chat_itemview);
        }
    }
}
