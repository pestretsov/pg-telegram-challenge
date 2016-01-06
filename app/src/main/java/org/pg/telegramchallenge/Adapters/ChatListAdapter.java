package org.pg.telegramchallenge.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.ChatListFragment;
import org.pg.telegramchallenge.R;
import org.pg.telegramchallenge.views.ChatListItemView;

import java.util.List;

/**
 * Created by artemypestretsov on 1/4/16.
 */
public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListVH> {
    private TdApi.Chat[] chatList;

    public ChatListAdapter(TdApi.Chat[] chatList) {
        this.chatList = chatList;
    }
    public ChatListAdapter() {}
    @Override
    public ChatListVH onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_list_cell, parent, false);

        return new ChatListVH(view);
    }

    public void changeData(TdApi.Chat[] chatList) {
        this.chatList = chatList.clone();
        this.notifyDataSetChanged();
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
            title = ((TdApi.PrivateChatInfo)chatList[position].type).user.firstName + " " + ((TdApi.PrivateChatInfo)chatList[position].type).user.lastName;
        } else {
            title = ((TdApi.GroupChatInfo)chatList[position].type).groupChat.title;
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
