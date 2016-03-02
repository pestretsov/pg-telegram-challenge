package org.pg.telegramchallenge.Adapters;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import org.pg.telegramchallenge.R;
import org.pg.telegramchallenge.views.BaseUserMessageView;
import org.pg.telegramchallenge.views.ChatListItemView;

/**
 * Created by artemypestretsov on 3/1/16.
 */
public class BaseUserMessageViewHolder extends BaseViewHolder {

    private BaseUserMessageView messageView;

    public BaseUserMessageViewHolder(View itemView) {
        super(itemView);

        messageView = (BaseUserMessageView)itemView.findViewById(R.id.content);
    }

    public void setAvatarFilePath(@Nullable String path) {
        messageView.setAvatarFilePath(path);
    }

    public void setDetailsVisibility(boolean isVisible) {
        messageView.setDetailsVisibility(isVisible);
    }

    public void setTitle(@NonNull String firstName, @Nullable String secondName) {
        messageView.setTitle(firstName, secondName);
    }

    public void setStatus(ChatListItemView.MessageStatus status) {
        messageView.setStatus(status);
    }
}
