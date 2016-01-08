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
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListVH> {
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

        holder.chatListItemView.setAvatarFilePath(null);
        if (!(p.small.path.isEmpty())) {
            holder.chatListItemView.setAvatarFilePath((p.small.path));
        } else {
            if (p.small.id!=0) {
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
        if (chatList.get(position).topMessage.message instanceof TdApi.MessageText) {
            holder.chatListItemView.setText(((TdApi.MessageText)chatList.get(position).topMessage.message).text);
        } else {
            holder.chatListItemView.setText("BOSS");
        }


        String title = "";
        if (chatList.get(position).type instanceof TdApi.PrivateChatInfo) {
            // TODO: MAYBE NO LAST NAME
            TdApi.PrivateChatInfo privateChat = (TdApi.PrivateChatInfo) chatList.get(position).type;
            title = privateChat.user.firstName;
            if (privateChat.user.lastName.length() > 0) {
                title += " " + privateChat.user.lastName;
            }

            boolean avatarsAreDownloading = handleAvatar(holder, privateChat.user.profilePhoto);

        } else {
            TdApi.GroupChat groupChat = ((TdApi.GroupChatInfo) chatList.get(position).type).groupChat;
            title = groupChat.title;
        }

        holder.chatListItemView.setTitle(title);
    }

    @Override
    public int getItemCount() {
        if (chatList != null)
            return chatList.size();
        return 0;
    }

    public class ChatListVH extends RecyclerView.ViewHolder {
        public ChatListItemView chatListItemView;

        public ChatListVH(View itemView) {
            super(itemView);
            chatListItemView = (ChatListItemView)itemView.findViewById(R.id.chat_list_itemview);
        }
    }
}
