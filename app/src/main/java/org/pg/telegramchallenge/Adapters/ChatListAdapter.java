package org.pg.telegramchallenge.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.ChatListFragment;
import org.pg.telegramchallenge.ObserverApplication;
import org.pg.telegramchallenge.R;
import org.pg.telegramchallenge.utils.Utils;
import org.pg.telegramchallenge.views.ChatListItemView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observer;

/**
 * Created by artemypestretsov on 1/4/16.
 */
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListVH>
        implements ObserverApplication.OnUpdateFileObserver, ObserverApplication.ChatObserver,
        ObserverApplication.OnUpdateChatReadOutboxObserver, ObserverApplication.OnUpdateChatReadInboxObserver, ObserverApplication.OnUpdateUserActionObserver {

    private volatile List<Long> chatList = new LinkedList<>();
    private volatile Map<Long, TdApi.Chat> chatMap = new HashMap<>();

    private ObserverApplication context;

    public ChatListAdapter() {

    }

    public ChatListAdapter(Context context) {
        this.context = (ObserverApplication) context;
    }

    @Override
    public ChatListVH onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_cell, parent, false);

        return new ChatListVH(view);
    }

    public void changeData(int left, TdApi.Chat[] chats) {
        int right = left;
        for (TdApi.Chat chat: chats) {
            if (!chatMap.containsKey(chat.id)) {
                chatMap.put(chat.id, chat);
                chatList.add(chat.id);
            } else if (chatMap.containsKey(chat.id)) {
                // rearrange
                // TODO:TEST
                chatList.remove(chat.id);
                chatList.add(chat.id);
//                right+=1;
            }
        }

        this.notifyItemRangeChanged(left, chats.length);
//        this.notifyItemRangeInserted(right, chats.length);
    }

    public ObserverApplication getApplication() {
        return context;
    }

    public void updateMessage(TdApi.Message message) {
        if (chatMap.containsKey(message.chatId)) {
            TdApi.Chat chat = chatMap.get(message.chatId);

            chat.topMessage = message;

            if (message.sendState instanceof TdApi.MessageIsIncoming) {
                chat.unreadCount += 1;
                chat.lastReadOutboxMessageId = chat.topMessage.id;
            } else {
                chat.lastReadInboxMessageId = chat.topMessage.id;
            }
            updateData(chat);
        } else {
            getApplication().sendRequest(new TdApi.GetChat(message.chatId));
        }
    }

    public void updateChatTitle(TdApi.UpdateChatTitle title) {
        if (chatMap.containsKey(title.chatId) && (chatMap.get(title.chatId).type instanceof TdApi.GroupChatInfo)) {
            chatMap.get(title.chatId).title = title.title;
        }
        this.notifyItemChanged(chatList.indexOf(title.chatId));
    }

    public void updateData(TdApi.Chat chat) {
        int position = 0;
        if (chatMap.containsKey(chat.id)) {

            // rearrange items in list
            position = chatList.indexOf(chat.id);
            chatList.remove(chat.id);
            chatList.add(0, chat.id);

            // update Chat itself
            chatMap.put(chat.id, chat);
            this.notifyItemRangeChanged(0, position+1);
        } else {
            // add new Chat
            chatList.add(0, chat.id);
            chatMap.put(chat.id, chat);
            this.notifyItemInserted(0);
        }
    }

    /**
     *
     * @param holder to load avatar into
     * @param p which is need to be downloaded
     * @return true if had started downloading
     */
    private boolean handleAvatar(ChatListVH holder, TdApi.ChatPhoto p) {
        if (!(p.small.path.isEmpty())) {
            holder.chatListItemView.setAvatarFilePath((p.small.path));
        } else {
            holder.chatListItemView.setAvatarFilePath(null);
            if (p.small.id != 0) {
                context.sendRequest(new TdApi.DownloadFile(p.small.id));
                return true;
            }
        }

        return false;
    }

    @Override
    public void onBindViewHolder(ChatListVH holder, int position) {
        if (chatList == null) {
            return;
        }

        TdApi.Chat currentChat = chatMap.get(chatList.get(position));
        if (currentChat.topMessage.content instanceof TdApi.MessageText) {
            holder.chatListItemView.setText(((TdApi.MessageText) currentChat.topMessage.content).text);
        } else {
            holder.chatListItemView.setText("BOSS");
        }

        Date date = new Date(Utils.timestampToMillis(currentChat.topMessage.date));

        holder.chatListItemView.setDate(date);


        holder.chatListItemView.setUnreadCount(currentChat.unreadCount);


        String title = "";
        if (currentChat.type instanceof TdApi.PrivateChatInfo) {
            holder.chatListItemView.setIsGroupChat(false);
        } else {
            holder.chatListItemView.setIsGroupChat(true);
        }
        title = currentChat.title;

        // no chat name <=> DELETED
        if (title.length() == 0) {
            title = "DELETED";
        }
        holder.chatListItemView.setTitle(title);

        if (currentChat.topMessage.fromId != ObserverApplication.userMe.id){
            holder.chatListItemView.setStatus(ChatListItemView.ChatStatus.READ);
        }
        else if (currentChat.lastReadOutboxMessageId < currentChat.topMessage.id) {
            holder.chatListItemView.setStatus(ChatListItemView.ChatStatus.UNREAD);
        } else {
            holder.chatListItemView.setStatus(ChatListItemView.ChatStatus.READ);
        }

        if (currentChat.topMessage.sendState instanceof TdApi.MessageIsBeingSent) {
            holder.chatListItemView.setStatus(ChatListItemView.ChatStatus.DELIVERING);
        }

//        holder.chatListItemView.setStatus(ChatListItemView.ChatStatus.READ);
//        if (currentChat.topMessage.sendState instanceof TdApi.MessageIsIncoming) {
//            holder.chatListItemView.setStatus(ChatListItemView.ChatStatus.READ);
//        } else if (currentChat.topMessage.id > currentChat.lastReadOutboxMessageId) {
//            holder.chatListItemView.setStatus(ChatListItemView.ChatStatus.UNREAD);
//        }

        handleAvatar(holder, currentChat.photo);
    }

    @Override
    public int getItemCount() {
        if (chatList != null)
            return chatList.size();
        return 0;
    }

    @Override
    public void proceed(TdApi.UpdateFile obj) {
        int i = 0;
        for (Long chatId : chatList) {
            TdApi.Chat c = chatMap.get(chatId);
            if (obj.file.id == c.photo.small.id) {
                c.photo.small = obj.file;
                this.notifyItemChanged(i);
                break;
            }
            i++;
        }
    }

    @Override
    public void proceed(TdApi.Chat obj) {
        updateData(obj);
    }

    @Override
    public void proceed(TdApi.UpdateChatReadOutbox obj) {
        if (chatMap.containsKey(obj.chatId)) {
            chatMap.get(obj.chatId).lastReadOutboxMessageId = obj.lastReadOutboxMessageId;
            this.notifyItemChanged(chatList.indexOf(obj.chatId));
        }
    }

    @Override
    public void proceed(TdApi.UpdateChatReadInbox obj) {
        if (chatMap.containsKey(obj.chatId)) {
            chatMap.get(obj.chatId).lastReadInboxMessageId = obj.lastReadInboxMessageId;
            chatMap.get(obj.chatId).unreadCount = obj.unreadCount;
            this.notifyItemChanged(chatList.indexOf(obj.chatId));
        }
    }


    // TODO: complete
    @Override
    public void proceed(TdApi.UpdateUserAction obj) {
        if (obj.action instanceof TdApi.SendMessageTypingAction) {
            if (ObserverApplication.userMe != null && obj.chatId == ObserverApplication.userMe.id) {
                chatMap.get(obj.chatId).lastReadOutboxMessageId = chatMap.get(obj.chatId).topMessage.id;
                chatMap.get(obj.chatId).lastReadInboxMessageId = chatMap.get(obj.chatId).topMessage.id;
                chatMap.get(obj.chatId).unreadCount = 0;
                this.notifyItemChanged(chatList.indexOf(obj.chatId));
            }

        }
    }

    public class ChatListVH extends RecyclerView.ViewHolder {
        public ChatListItemView chatListItemView;

        public ChatListVH(View itemView) {
            super(itemView);
            chatListItemView = (ChatListItemView)itemView.findViewById(R.id.chat_list_itemview);
        }
    }
}
