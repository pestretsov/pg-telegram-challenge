package org.pg.telegramchallenge.Adapters;

import android.view.View;
import org.pg.telegramchallenge.R;
import org.pg.telegramchallenge.views.ImageUserMessageView;

/**
 * Created by artemypestretsov on 3/1/16.
 */
public class ImageUserMessageViewHolder extends BaseUserMessageViewHolder {

    private ImageUserMessageView messageView;

    public ImageUserMessageViewHolder(View itemView) {
        super(itemView);

        messageView = (ImageUserMessageView) itemView.findViewById(R.id.content);
    }

    public void setImage(String path, int imageWidth, int imageHeight) {
        messageView.setImage(path, imageWidth, imageHeight);
    }
}
