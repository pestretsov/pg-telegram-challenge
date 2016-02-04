package org.pg.telegramchallenge;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.Adapters.ChatListAdapter;
import org.pg.telegramchallenge.utils.Utils;
import org.pg.telegramchallenge.views.ChatListItemView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment implements ObserverApplication.OnErrorObserver,
        ObserverApplication.OnUpdateNewMessageObserver, ObserverApplication.ChatsObserver, ObserverApplication.OnUpdateChatTitleObserver {

    private RecyclerView chatListRecyclerView;
    private ChatListAdapter chatListAdapter;
    private LinearLayoutManager layoutManager;

    private int visibleItems, totalItems, previousTotal = 0, firstVisibleItem, visibleThreshold = 20;
    private boolean loading = true;

    private int nextLimit = 50;
    private int nextOffset = 0;

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    public ObserverApplication getApplication(){
        return (ObserverApplication) getActivity().getApplication();
    }

    public ChatListFragment() {
        // Required empty public constructor
    }

    public static ChatListFragment newInstance() {

        Bundle args = new Bundle();

        ChatListFragment fragment = new ChatListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        getApplication().addObserver(this);
        getApplication().addObserver(chatListAdapter);
    }

    @Override
    public void onPause(){
        super.onPause();

        getApplication().removeObserver(this);
        getApplication().removeObserver(chatListAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        layoutManager = new LinearLayoutManager(getActivity());
        chatListRecyclerView = (RecyclerView)view.findViewById(R.id.chatListRecyclerView);
        // if changeAnimation is enabled it looks like shit; try it yourself
        ((SimpleItemAnimator)chatListRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        chatListAdapter = new ChatListAdapter();
        chatListRecyclerView.setAdapter(chatListAdapter);
        chatListRecyclerView.setLayoutManager(layoutManager);
        chatListRecyclerView.addItemDecoration(new ItemDivider(getContext(), R.drawable.chat_list_divider));

        chatListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItems = recyclerView.getChildCount();
                totalItems = layoutManager.getItemCount();
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItems > previousTotal) {
                        loading = false;
                        previousTotal = totalItems;
                    }
                }

                if (!loading && totalItems - visibleItems <= firstVisibleItem + visibleThreshold) {
                    nextOffset+=nextLimit;

                    getApplication().sendRequest(new TdApi.GetChats(nextOffset, nextLimit));

                    loading = true;
                }
            }
        });

        getApplication().sendRequest(new TdApi.GetChats(nextOffset, nextLimit));

        // TODO: decide whether its needed or not
//        getApplication().sendRequest(new TdApi.GetMe());

        return view;
    }

    @Override
    public void proceed(TdApi.UpdateChatTitle obj) {
        chatListAdapter.updateChatTitle(obj);
    }

    @Override
    public void proceed(TdApi.Error err) {

    }

    @Override
    public void proceed(TdApi.UpdateNewMessage obj) {
//        getApplication().sendRequest(new TdApi.GetChat(obj.message.chatId));
        chatListAdapter.updateMessage(obj.message);
    }

    @Override
    public void proceed(TdApi.Chats obj) {
        chatListAdapter.changeData(obj.chats);
    }

    public static class ItemDivider extends RecyclerView.ItemDecoration {

        private static Drawable mDivider;
        private final Context mContext;

        public ItemDivider(Context context) {
            mContext = context;
            final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
            if (mDivider == null) {
                mDivider = styledAttributes.getDrawable(0);
            }
            styledAttributes.recycle();
        }

        public ItemDivider(Context context, int resId) {
            mContext = context;
            if (mDivider == null) {
                mDivider = ContextCompat.getDrawable(context, resId);
            }
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
//            super.onDraw(c, parent, state);

            int left = parent.getPaddingLeft();
            left += Utils.dpToPx(ChatListItemView.dpAvatarRadius*2+ChatListItemView.mTextPadding, mContext);
            left += mContext.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);

            int right = parent.getWidth() - parent.getPaddingRight();

            int total = parent.getChildCount();
            for (int i = 0; i < total; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }
}
