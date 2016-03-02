package org.pg.telegramchallenge.Adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.pg.telegramchallenge.R;
import org.pg.telegramchallenge.views.BaseChatItemView;
import org.pg.telegramchallenge.views.BaseUserMessageView;

import java.util.Calendar;

/**
 * Created by artemypestretsov on 3/1/16.
 */
public class BaseViewHolder extends RecyclerView.ViewHolder  {

    private BaseChatItemView chatItemView;

    public BaseViewHolder(View itemView) {
        super(itemView);

        chatItemView = (BaseChatItemView)itemView.findViewById(R.id.content);
    }

    public void setBarVisibility(boolean isShown) {
        chatItemView.setBarVisability(isShown);
    }

    public void setDateVisibility(boolean isShown) {
        chatItemView.setDateVisability(isShown);
    }

    public void setDate (Calendar c) {
        chatItemView.setDate(c);
    }
}
