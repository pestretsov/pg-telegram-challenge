package org.pg.telegramchallenge;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import org.pg.telegramchallenge.Adapters.BottomSheetAdapter;
import org.pg.telegramchallenge.utils.Utils;

/**
 * Class to show media attachment panel of the bottom of activity or fragment.
 */
public class BottomSheetDialog implements LoaderManager.LoaderCallbacks<Cursor>, DialogInterface.OnDismissListener {

    private static final int URL_LOADER = 0;
    private final Uri sourceUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    private final Context mContext;
    private final LoaderManager mManager;
    private final LayoutInflater mInflater;
    private RecyclerView mRecycler;
    private BottomSheetAdapter mAdapter;
    private OnUserActionListener mListener;
    private ProgressBar mProgressBar;

    private boolean isShown = false;
    private LinearLayoutManager mLayoutManager;

    @Override
    public void onDismiss(DialogInterface dialog) {
        mRecycler = null;
        isShown = false;
    }

    public interface OnUserActionListener {
        void onUserAction();
    }

    BottomSheetDialog(Fragment f) {
        mContext = f.getContext();
        mManager = f.getLoaderManager();
        mInflater = f.getLayoutInflater(null);

//        mManager.initLoader(URL_LOADER, null, this);
    }

    /**
     * When user makes his choice of attachment, method onUserAction is called on listener, if
     * it is not null.
     * @param listener
     */
    public void setOnUserActionListener(@Nullable OnUserActionListener listener) {
        mListener = listener;
    }

    /**
     * Makes bottom dialog appear; if called when it's already shown, throws Exception
     * @throws IllegalStateException when it's already shown
     */
    public void show() {
        if (isShown)
            throw new IllegalStateException("BottomSheet is already shown!");

        isShown = true;

        View bottomSheet = mInflater.inflate(R.layout.media_bottom_sheet, null);

        mRecycler = (RecyclerView) bottomSheet.findViewById(R.id.bottom_sheet_recycler);
        mProgressBar = (ProgressBar) bottomSheet.findViewById(R.id.progress_bar_bottom_sheet);

        mLayoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecycler.setLayoutManager(mLayoutManager);

        mManager.restartLoader(URL_LOADER, null, this);

        final Dialog mBottomSheetDialog = new Dialog (mContext,
                R.style.MaterialDialogSheet);

        mBottomSheetDialog.setOnDismissListener(this);

        mBottomSheetDialog.setContentView (bottomSheet);
        mBottomSheetDialog.setCancelable (true);
        mBottomSheetDialog.getWindow ().setLayout (LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        mBottomSheetDialog.getWindow ().setGravity (Gravity.BOTTOM);
        mBottomSheetDialog.show();
    }

    /**
     *
     * if id is right, returns CursorLoader to get all pictures from gallery and SD-card
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case URL_LOADER:
                return new CursorLoader(
                        mContext,
                        sourceUri,
                        null,
                        null,
                        null,
                        MediaStore.Images.Media.DATE_TAKEN + " DESC");

            default: return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (mRecycler!=null) {
            mAdapter = new BottomSheetAdapter(mContext, cursor);
            mProgressBar.setVisibility(View.GONE);
            mRecycler.setAdapter(mAdapter);
            mRecycler.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter!=null)
            mAdapter.setCursor(null);
    }
}
