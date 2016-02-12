package org.pg.telegramchallenge.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import org.pg.telegramchallenge.R;

import java.util.ArrayList;

/**
 * Created by Roman on 08.02.2016.
 */
public class BottomSheetAdapter  extends RecyclerView.Adapter<BottomSheetAdapter.ViewHolder> {
    private final Cursor mCursor;
    private final Context mContext;
//    ArrayList<>

    public BottomSheetAdapter(Context c, Cursor mediaCursor) {
        mCursor = mediaCursor;
        mContext = c;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_recycler_item, parent, false);

        return new ViewHolder((ImageView) v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
//        holder.mImageView;
        mCursor.moveToPosition(position);
        String myData = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
        Glide.with(mContext).load(myData).centerCrop().into((ImageView) holder.itemView);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public ViewHolder(ImageView itemView) {
            super(itemView);
        }
    }
}
