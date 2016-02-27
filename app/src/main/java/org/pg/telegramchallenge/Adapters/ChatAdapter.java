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
import org.pg.telegramchallenge.utils.Utils;
import org.pg.telegramchallenge.views.ChatListItemView;
import org.pg.telegramchallenge.views.TextUserMessageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by artemypestretsov on 2/18/16.
 */
public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatVH> implements ObserverApplication.OnGetChatHistoryObserver, ObserverApplication.OnUpdateChatReadOutboxObserver {//, ObserverApplication.OnGetGroupFullObserver {
    private static final String TAG = ChatAdapter.class.getSimpleName();

    private Long chatId = null;
    private TdApi.GroupFull groupFull = null;
    private TdApi.Chat chat = null;
    private int lastPos = 0;

    private Context context;
    private MainActivity activity;

    private LinkedList<TdApi.Message> messagesList = new LinkedList<>();
//    private Map<Integer, TdApi.User> usersMap = new HashMap<>();

    public ChatAdapter(Context context, Activity activity, long id) {
        this.chatId = id;
        this.context = (ObserverApplication) context;
        this.activity = (MainActivity) activity;

        this.chat = ObserverApplication.chats.get(chatId);

        if (chat.type instanceof TdApi.GroupChatInfo) {
            // TODO: НУЖНО ГАРАНТИРОВАТЬ, ЧТО ГРУППА БУДЕТ В ХЭШМЭПЕ
            // либо сделать обсервером ---- ТАК И ПОСТУПИМ=D
            if (ObserverApplication.groupsFull.containsKey(((TdApi.GroupChatInfo) chat.type).group.id)) {
                groupFull = ObserverApplication.groupsFull.get(((TdApi.GroupChatInfo) chat.type).group.id);

//                for (TdApi.ChatParticipant participant : groupFull.participants) {
//                    usersMap.put(participant.user.id, participant.user);
//                }
            }
        } else if (chat.type instanceof TdApi.ChannelChatInfo) {
          // TODO: complete
        } else {
            // TODO: WHATS THE DIFFERENCE BETWEEN CHANNELS AND SO ON
            chatId = chat.id;

//            usersMap.put(((TdApi.PrivateChatInfo)chat.type).user.id, ((TdApi.PrivateChatInfo)chat.type).user);
//            usersMap.put(ObserverApplication.userMe.id, ObserverApplication.userMe);
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
        TdApi.Message msgPrev = null;

        TdApi.User usr = ObserverApplication.users.get(msg.fromId);

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(Utils.timestampToMillis(msg.date)));
        Calendar calPrev = null;

        boolean sameDay = false;
        // just for fun
        long dt = 100_000_000_000L;

        if (position < messagesList.size()-1) {
            msgPrev = messagesList.get(position+1);
            calPrev = Calendar.getInstance();
            calPrev.setTime(new Date(Utils.timestampToMillis(msgPrev.date)));
            sameDay = cal.get(Calendar.YEAR) == calPrev.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == calPrev.get(Calendar.DAY_OF_YEAR);

            if (sameDay) {
                // millis - to seconds - to minutes
                dt = (cal.getTimeInMillis()/1000)/60 - (calPrev.getTimeInMillis()/1000)/60;
            }
        }

        holder.chatItemView.setDate(cal);

        if (msgPrev != null && msgPrev.fromId == msg.fromId && dt <= 5) {
            holder.chatItemView.setDetailsVisibility(false);
        } else {
            holder.chatItemView.setDetailsVisibility(true);
        }

        if (sameDay) {
            holder.chatItemView.setDateVisability(false);
        } else {
            holder.chatItemView.setDateVisability(true);
            holder.chatItemView.setDetailsVisibility(true);
        }

        holder.chatItemView.setBarVisability(false);



        holder.chatItemView.setStatus(ChatListItemView.MessageStatus.READ);
        if (!(msg.sendState instanceof TdApi.MessageIsIncoming) && msg.id > chat.lastReadOutboxMessageId) {
            holder.chatItemView.setStatus(ChatListItemView.MessageStatus.UNREAD);
        }

        try {
            holder.chatItemView.setTitle(usr.firstName, usr.lastName);
        } catch (NullPointerException e) {
            Log.e("TAG", "shouldntBeThatBad");
        }

        String text = "BOSS";
        if (msg.content instanceof TdApi.MessageText && ((TdApi.MessageText) msg.content).text.length() > 0) {
            text = ((TdApi.MessageText)msg.content).text;
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

    @Override
    public void proceed(TdApi.UpdateChatReadOutbox obj) {
        chat = ObserverApplication.chats.get(chatId);
        this.notifyDataSetChanged();
    }

//    @Override
//    public void proceed(TdApi.GroupFull obj) {
//        try {
//            groupFull = ObserverApplication.groupsFull.get(((TdApi.GroupChatInfo) chat.type).group.id);
//
//            for (TdApi.ChatParticipant participant : groupFull.participants) {
//                usersMap.put(participant.user.id, participant.user);
//            }
//        } catch (NullPointerException e) {
//            Log.e(TAG, e.toString());
//        }
//    }


    public class ChatVH extends RecyclerView.ViewHolder {

        private TextUserMessageView chatItemView;

        public ChatVH(View itemView) {
            super(itemView);

            chatItemView = (TextUserMessageView) itemView.findViewById(R.id.chat_itemview);
        }
    }
}
