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
import org.pg.telegramchallenge.views.ChatListItemView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by artemypestretsov on 1/4/16.
 */
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListVH>
        implements ObserverApplication.OnUpdateFileObserver{
    private List<TdApi.Chat> chatList = new ArrayList<>();
    private ObserverApplication context;

    public ChatListAdapter(TdApi.Chat[] chatList) {
        this.chatList.addAll(Arrays.asList(chatList));
    }

    public ChatListAdapter() {}

    @Override
    public ChatListVH onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_cell, parent, false);
        context = (ObserverApplication) parent.getContext().getApplicationContext();

        return new ChatListVH(view);
    }

    public void changeData(TdApi.Chat[] chatList) {

//        this.chatList.clear();
        this.chatList.addAll(Arrays.asList(chatList));

        this.notifyDataSetChanged();
    }

    /**
     *
     * @param holder to load avatar into
     * @param p which is need to be downloaded
     * @return true if had started downloading
     */
    private boolean handleAvatar(ChatListVH holder, TdApi.ProfilePhoto p) {

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


        TdApi.Chat currentChat = chatList.get(position);
        if (currentChat.topMessage.message instanceof TdApi.MessageText) {
            holder.chatListItemView.setText(((TdApi.MessageText) currentChat.topMessage.message).text);
        } else {
            holder.chatListItemView.setText("BOSS");
        }


        String title = "";
        if (currentChat.type instanceof TdApi.PrivateChatInfo) {
            // TODO: MAYBE NO LAST NAME
            TdApi.PrivateChatInfo privateChat = (TdApi.PrivateChatInfo) currentChat.type;
            title = privateChat.user.firstName;
            if (privateChat.user.lastName.length() > 0) {
                title += " " + privateChat.user.lastName;
            }

        } else {
            TdApi.GroupChat groupChat = ((TdApi.GroupChatInfo) currentChat.type).groupChat;
            title = groupChat.title;
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

    private TdApi.ProfilePhoto getProfilePhoto(TdApi.Chat c) {
        switch (c.type.getConstructor()) {
            case TdApi.PrivateChatInfo.CONSTRUCTOR:
                return ((TdApi.PrivateChatInfo)c.type).user.profilePhoto;
            case TdApi.GroupChatInfo.CONSTRUCTOR:
                return ((TdApi.GroupChatInfo)c.type).groupChat.photo;
            default:
                throw new IllegalArgumentException("No profile photo in Chat!");
        }
    }

    @Override
    public void proceed(TdApi.UpdateFile obj) {
        int i = 0;
        for (TdApi.Chat c : chatList) {
            if (obj.file.id == getProfilePhoto(c).small.id) {
                getProfilePhoto(c).small = obj.file;
                this.notifyItemChanged(i);
                break;
            }
            i++;
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
