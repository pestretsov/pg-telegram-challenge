package org.pg.telegramchallenge;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.Adapters.ChatListAdapter;
import org.pg.telegramchallenge.utils.AvatarImageView;
import org.pg.telegramchallenge.utils.Utils;
import org.pg.telegramchallenge.views.ChatListItemView;

import java.util.concurrent.CountDownLatch;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment implements ObserverApplication.OnErrorObserver,
        ObserverApplication.OnUpdateNewMessageObserver, ObserverApplication.ChatsObserver, ObserverApplication.OnUpdateChatTitleObserver, ObserverApplication.OnUpdateChatOrderObserver, ObserverApplication.OnUpdateFileObserver{

    final CountDownLatch latch = new CountDownLatch(1);

    private Toolbar toolbar;

    private RecyclerView chatListRecyclerView;
    private ChatListAdapter chatListAdapter;
    private LinearLayoutManager layoutManager;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private View header;
    private AvatarImageView userProfilePhoto;
    private ViewTarget<ImageView, Bitmap> glideTarget;

    private int visibleItems, totalItems, previousTotal = 0, firstVisibleItem;
    private String avatarImageFilePath = null;

    // TODO: set 25 (20)
    private int visibleThreshold = 15;
    private boolean loading = true;
    // TODO: set 50
    private int limit = 25;
    private long offsetChatId = 0;
    // объяснение магических чисел
    // https://vk.com/board55882680?act=search&q=offsetOrder
    private long offsetOrder = 9223372036854775807L; // == 2^63-1
    private int chatsCounter = 0;

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
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        navigationView = (NavigationView) view.findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        chatListRecyclerView = (RecyclerView)view.findViewById(R.id.chat_list_recycler_view);

        if (toolbar != null) {
            toolbar.setTitle(R.string.chat_list_title);

            toolbar.setNavigationIcon(R.drawable.ic_menu);
            toolbar.setPadding(0, Utils.getStatusBarHeight(getActivity()), 0, 0);
            setHasOptionsMenu(true);

            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout,
                    toolbar, R.string.open_drawer, R.string.close_drawer) {

                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                }
            };

            drawerLayout.setDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
        }

        layoutManager = new LinearLayoutManager(getActivity());
        // if changeAnimation is enabled it looks like shit; try it yourself
        ((SimpleItemAnimator)chatListRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        chatListAdapter = new ChatListAdapter(getApplication());
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
                    getApplication().sendRequest(new TdApi.GetChats(offsetOrder, offsetChatId, limit));

                    loading = true;
                }
            }
        });


        // THE ORDER OF REQUESTS IS CRUCIAL !!!
        getApplication().sendRequest(new TdApi.GetChats(offsetOrder, offsetChatId, limit));

        getApplication().sendRequest(new TdApi.GetMe(), new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject object) {
                if (object instanceof TdApi.User) {
                    ObserverApplication.userMe = (TdApi.User)object;
                    latch.countDown();
                }
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {

        }

        header = navigationView.getHeaderView(0);

        String fullName = ObserverApplication.userMe.firstName + " " + ObserverApplication.userMe.lastName;
        String phoneNumber = "+" + ObserverApplication.userMe.phoneNumber;

        ((TextView)header.findViewById(R.id.userMe_name)).setText(fullName);
        ((TextView)header.findViewById(R.id.userMe_phone)).setText(phoneNumber);


        // ТУТ ВООБЩЕ ХЗ КАК НЕ КРАШИТСЯ
        glideTarget = new ViewTarget<ImageView, Bitmap>((ImageView) header.findViewById(R.id.userMe_image)) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                String initials = Utils.getInitials(ObserverApplication.userMe.firstName + " " + ObserverApplication.userMe.lastName);
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getContext().getResources(), resource);
                roundedBitmapDrawable.setCircular(true);
                ((ImageView)header.findViewById(R.id.userMe_image)).setImageDrawable(roundedBitmapDrawable);
            }
        };

        if (!(ObserverApplication.userMe.profilePhoto.big.path.isEmpty())) {
            userProfilePhoto = (AvatarImageView)header.findViewById(R.id.userMe_image);
            userProfilePhoto.setInitials("AP");
            avatarImageFilePath = ObserverApplication.userMe.profilePhoto.big.path;
//            Glide.with(getActivity()).load(avatarImageFilePath).asBitmap().fitCenter().into(glideTarget);
        } else {
            avatarImageFilePath = null;

            if (ObserverApplication.userMe.profilePhoto.big.id != 0) {
                getApplication().sendRequest(new TdApi.DownloadFile(ObserverApplication.userMe.profilePhoto.big.id));
            }
        }

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
        chatListAdapter.updateMessage(obj.message);
    }

    @Override
    public void proceed(TdApi.Chats obj) {
        int n = obj.chats.length;
        if (n != 0) {
//            TODO: THIS OR
            offsetChatId = obj.chats[n-1].id;
            offsetOrder = obj.chats[n-1].order;
            chatListAdapter.changeData(chatsCounter, obj.chats);
            chatsCounter += obj.chats.length;
        }
    }

    @Override
    public void proceed(TdApi.UpdateChatOrder obj) {
        // TODO: OR THAT ?
//        offsetOrder = obj.order;
//        offsetChatId = obj.chatId;
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

    @Override
    public void proceed(TdApi.UpdateFile obj) {
        if (obj.file.id == ObserverApplication.userMe.profilePhoto.big.id) {
            ObserverApplication.userMe.profilePhoto.big = obj.file;
            avatarImageFilePath = ObserverApplication.userMe.profilePhoto.big.path;
//            Glide.with(getActivity()).load(avatarImageFilePath).asBitmap().fitCenter().into(glideTarget);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // need to clear ACCEPT icon which is added inside Activity's onCreateOptionsMenu method
        menu.clear();
        inflater.inflate(R.menu.menu_chat_list, menu);
    }
}
