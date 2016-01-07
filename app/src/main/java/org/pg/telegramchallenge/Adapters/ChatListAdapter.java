package org.pg.telegramchallenge.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.ChatListFragment;
import org.pg.telegramchallenge.ObserverApplication;
import org.pg.telegramchallenge.R;
import org.pg.telegramchallenge.views.ChatListItemView;

import java.util.List;

/**
 * Created by artemypestretsov on 1/4/16.
 */
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListVH> {
    private TdApi.Chat[] chatList;
    private ObserverApplication context;

    public ChatListAdapter(TdApi.Chat[] chatList) {
        this.chatList = chatList;
    }
    public ChatListAdapter() {}
    @Override
    public ChatListVH onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_cell, parent, false);
        context = (ObserverApplication) parent.getContext().getApplicationContext();

        return new ChatListVH(view);
    }

    public void changeData(TdApi.Chat[] chatList) {
        this.chatList = chatList.clone();
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
        } else if (p.small.id!=0) {
            context.sendRequest(new TdApi.DownloadFile(p.small.id));
            return true;
        }

        return false;
    }

    @Override
    public void onBindViewHolder(ChatListVH holder, int position) {
        if (chatList == null) {
            return;
        }
        if (chatList[position].topMessage.message instanceof TdApi.MessageText) {
            holder.chatListItemView.setText(((TdApi.MessageText)chatList[position].topMessage.message).text);
        } else {
            holder.chatListItemView.setText("BOSS");
        }


        String title = "";
        if (chatList[position].type instanceof TdApi.PrivateChatInfo) {
            // TODO: MAYBE NO LAST NAME
            TdApi.PrivateChatInfo privateChat = (TdApi.PrivateChatInfo) chatList[position].type;
            title = privateChat.user.firstName;
            if (privateChat.user.lastName.length() > 0) {
                title += " " + privateChat.user.lastName;
            }

            boolean avatarsAreDownloading = handleAvatar(holder, privateChat.user.profilePhoto);

            if (avatarsAreDownloading) {
                context.sendRequest(new TdApi.GetChats(0, 10));
            }

        } else {
            TdApi.GroupChat groupChat = ((TdApi.GroupChatInfo) chatList[position].type).groupChat;
            title = groupChat.title;

            if (handleAvatar(holder, groupChat.photo)) {
                context.sendRequest(new TdApi.GetChats(0, 10));
            }
        }

        holder.chatListItemView.setTitle(title);
    }

    @Override
    public int getItemCount() {
        if (chatList != null)
            return chatList.length;
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
