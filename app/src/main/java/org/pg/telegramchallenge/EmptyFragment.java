package org.pg.telegramchallenge;


import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.pg.telegramchallenge.Adapters.BottomSheetAdapter;
import org.pg.telegramchallenge.views.BaseChatItemView;
import org.pg.telegramchallenge.views.BaseUserMessageView;
import org.pg.telegramchallenge.views.ChatListItemView;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmptyFragment extends Fragment implements View.OnClickListener{


    private LinearLayoutManager mLayoutManager;
    private BottomSheetAdapter mAdapter;

    CursorLoader cursorLoader; // for order
    Cursor cursor;

    public EmptyFragment() {
        // Required empty public constructor
    }

    public static EmptyFragment newInstance() {

        Bundle args = new Bundle();

        EmptyFragment fragment = new EmptyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private View mOpenBottomShetButton, mBottomSheet, mTakePhotoTextView, mFromGalleryTextView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        View view = inflater.inflate(R.layout.fragment_empty, container, false);

        mOpenBottomShetButton = view.findViewById(R.id.open_bottom_sheet_btn);
        mOpenBottomShetButton.setOnClickListener(this);

        ChatListItemView itemView = (ChatListItemView) view.findViewById(R.id.sample_view);
        itemView.setStatus(ChatListItemView.ChatStatus.UNREAD);

        itemView.setOnClickListener(new View.OnClickListener() {

            int count = 1;
            @Override
            public void onClick(View v) {
                ((ChatListItemView) v).setUnreadCount(count);
                count += 1;
            }
        });

        BaseChatItemView messageView = (BaseChatItemView) view.findViewById(R.id.message);
        messageView.setOnClickListener(new View.OnClickListener() {

            boolean b = false;
            @Override
            public void onClick(View v) {
                ((BaseChatItemView) v).setDateVisability(b);
                ((BaseChatItemView) v).setBarVisability(b);
                ((BaseUserMessageView) v).setDetailsVisibility(b);

                b = !b;

            }
        });

        Button b = (Button) view.findViewById(R.id.open_bottom_sheet_btn);
        b.setVisibility(View.VISIBLE);


        bottomSheetDialog = new BottomSheetDialog(this);

        return view;
    }

    final Uri sourceUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    private RecyclerView mBottomSheetRecyclerView;
    private ProgressBar mBottomSheetProgressBar;
    private BottomSheetDialog bottomSheetDialog;

    @Override
    public void onClick(View v) {
        bottomSheetDialog.show();
    }

}
