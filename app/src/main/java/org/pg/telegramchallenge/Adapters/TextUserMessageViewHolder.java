package org.pg.telegramchallenge.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.pg.telegramchallenge.R;
import org.pg.telegramchallenge.views.TextUserMessageView;

/**
 * Created by artemypestretsov on 3/1/16.
 */
public class TextUserMessageViewHolder extends BaseUserMessageViewHolder {

    private TextUserMessageView textUserMessageView;

    public TextUserMessageViewHolder(View itemView) {
        super(itemView);

        textUserMessageView = (TextUserMessageView) itemView.findViewById(R.id.content);
    }

    public void setText(String s) {
        textUserMessageView.setText(s);
    }
}
