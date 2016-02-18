package org.pg.telegramchallenge.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import org.pg.telegramchallenge.R;
import org.pg.telegramchallenge.utils.Utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Roman on 08.02.2016.
 */
public class BottomSheetAdapter extends RecyclerView.Adapter<BottomSheetAdapter.ViewHolder> {
    private Cursor mCursor;
    private final Context mContext;

    private Set<String> checkedItems = new HashSet<>();

    public BottomSheetAdapter(Context c, Cursor mediaCursor) {
        mCursor = mediaCursor;
        mContext = c;
    }

    public void setCursor(Cursor cursor) {
        if (mCursor!=null) {
            mCursor.close();
        } //TODO check if this OK
        this.mCursor = cursor;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.bottom_sheet_recycler_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String myData = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
        holder.setImage(myData);
    }

    @Override
    public int getItemCount() {
        if (mCursor == null) {
            return 0;
        }
        return mCursor.getCount();
    }

    public int getCheckedCount() {
        return checkedItems.size();
    }

    public Set<String> getCheckedItems() {
        return getCheckedItems();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView mImageView, mImageCheck;
        String mImageUri;
        int dpSqarePrewiewSize = 0;

        public ViewHolder(View itemView) {
            super(itemView);

            if (dpSqarePrewiewSize == 0) {
                dpSqarePrewiewSize = Utils.dpToPx(100, mContext);
            }

            mImageView = (ImageView) itemView.findViewById(R.id.image_preview);
            mImageCheck = (ImageView) itemView.findViewById(R.id.image_check);
            mImageView.setOnClickListener(this);
        }

        void setImage(String uri) {
            mImageUri = uri;

            int checkVisibility = checkedItems.contains(uri)?View.VISIBLE:View.INVISIBLE;
            mImageCheck.setVisibility(checkVisibility);

            DrawableRequestBuilder<String> glideBuilder = Glide.with(mContext)
                    .load(uri);

            if (checkedItems.size()>0) {
                glideBuilder.override(Target.SIZE_ORIGINAL, dpSqarePrewiewSize)
                .fitCenter();
            } else {
                glideBuilder.override(dpSqarePrewiewSize, dpSqarePrewiewSize)
                        .centerCrop();
            }

            glideBuilder.crossFade().into(mImageView);
        }

        @Override
        public void onClick(View v) {

            if (checkedItems.contains(mImageUri)) {
                checkedItems.remove(mImageUri);
                mImageCheck.setVisibility(View.INVISIBLE);
                if (getCheckedCount()==0) {
                    notifyDataSetChanged(); //TODO change that
                }
            } else {
                if (getCheckedCount()==0) {
                    notifyDataSetChanged();
                }
                checkedItems.add(mImageUri);
                mImageCheck.setVisibility(View.VISIBLE);
            }
        }
    }
}
