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
        ObserverApplication.OnUpdateChatReadOutboxObserver, ObserverApplication.OnUpdateChatReadInboxObserver {

    private volatile List<Long> chatList = new LinkedList<>();
    private volatile Map<Long, TdApi.Chat> chatMap = new HashMap<>();

    private ObserverApplication context;

    public ChatListAdapter(TdApi.Chat[] chatList) {
//        this.chatList.addAll(Arrays.asList(chatList));
    }

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
        int position = left;
        for (TdApi.Chat chat: chats) {
            chatMap.put(chat.id, chat);
            chatList.add(chat.id);
        }

        this.notifyItemRangeInserted(left, chats.length);
    }

    public ObserverApplication getApplication() {
        return context;
    }

    public void updateMessage(TdApi.Message message) {
        if (chatMap.containsKey(message.chatId)) {
            TdApi.Chat chat = chatMap.get(message.chatId);

            chat.topMessage = message;

            if (ObserverApplication.userMe != null && message.fromId != ObserverApplication.userMe.id) {
                chat.unreadCount += 1;
            }

            if (ObserverApplication.userMe != null && message.chatId == ObserverApplication.userMe.id){
                chat.lastReadInboxMessageId = chat.topMessage.id;
                chat.lastReadOutboxMessageId = chat.lastReadInboxMessageId;
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
        } else {
            // add new Chat
            chatList.add(0, chat.id);
            chatMap.put(chat.id, chat);
        }
        this.notifyItemRangeChanged(0, position+1);
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
            TdApi.PrivateChatInfo privateChat = (TdApi.PrivateChatInfo) currentChat.type;

            if (privateChat.user.firstName.length() > 0) {
                title += privateChat.user.firstName;
            }

            if (privateChat.user.lastName.length() > 0) {
                if (title.length() > 0) {
                    title += " ";
                }
                title += privateChat.user.lastName;
            }
            holder.chatListItemView.setIsGroupChat(false);
        } else {
            TdApi.Chat groupChat = currentChat;
            title = groupChat.title;
            holder.chatListItemView.setIsGroupChat(true);
        }

        // no chat name <=> DELETED
        if (title.length() == 0) {
            title = "DELETED";
        }

        holder.chatListItemView.setStatus(ChatListItemView.ChatStatus.READ);
        if (ObserverApplication.userMe != null && currentChat.topMessage.fromId != ObserverApplication.userMe.id) {
            holder.chatListItemView.setStatus(ChatListItemView.ChatStatus.READ);
        } else if (ObserverApplication.userMe != null && currentChat.topMessage.id > currentChat.lastReadOutboxMessageId) {
            holder.chatListItemView.setStatus(ChatListItemView.ChatStatus.UNREAD);
        }


        handleAvatar(holder, getProfilePhoto(currentChat));

        holder.chatListItemView.setTitle(title);
    }

    @Override
    public int getItemCount() {
        if (chatList != null)
            return chatList.size();
        return 0;
    }

    private TdApi.ChatPhoto getProfilePhoto(TdApi.Chat c) {
        switch (c.type.getConstructor()) {
            case TdApi.PrivateChatInfo.CONSTRUCTOR:
                return c.photo;
            case TdApi.GroupChatInfo.CONSTRUCTOR:
                return c.photo;
            default:
                throw new IllegalArgumentException("No profile photo in Chat!");
        }
    }

    @Override
    public void proceed(TdApi.UpdateFile obj) {
        int i = 0;
        for (Long chatId : chatList) {
            TdApi.Chat c = chatMap.get(chatId);
            if (obj.file.id == getProfilePhoto(c).small.id) {
                getProfilePhoto(c).small = obj.file;
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

    public class ChatListVH extends RecyclerView.ViewHolder {
        public ChatListItemView chatListItemView;

        public ChatListVH(View itemView) {
            super(itemView);
            chatListItemView = (ChatListItemView)itemView.findViewById(R.id.chat_list_itemview);
        }
    }
}
