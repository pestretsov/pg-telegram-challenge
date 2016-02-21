package org.pg.telegramchallenge;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ViewTarget;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.Adapters.ChatListAdapter;
import org.pg.telegramchallenge.utils.AvatarImageView;
import org.pg.telegramchallenge.utils.Utils;
import org.pg.telegramchallenge.views.ChatListItemView;

import java.util.concurrent.CountDownLatch;

public class ChatListActivity extends AppCompatActivity implements ObserverApplication.OnAuthObserver, ObserverApplication.OnErrorObserver,
        ObserverApplication.ChatsObserver, ObserverApplication.OnUpdateChatTitleObserver,
        ObserverApplication.OnUpdateChatOrderObserver, ObserverApplication.OnUpdateFileObserver {

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
    private int limit = 50;
    private long offsetChatId = 0;
    // объяснение магических чисел
    // https://vk.com/board55882680?act=search&q=offsetOrder
    private long offsetOrder = 9223372036854775807L; // == 2^63-1
    private int chatsCounter = 0;

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    public ObserverApplication getObserverApplication(){
        return (ObserverApplication) getApplication();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        getObserverApplication().addObserver(this);
        getObserverApplication().addObserver(chatListAdapter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        chatListRecyclerView = (RecyclerView) findViewById(R.id.chat_list_recycler_view);

        if (toolbar != null) {
            toolbar.setTitle(R.string.chat_list_title);

            toolbar.setNavigationIcon(R.drawable.ic_menu);
            toolbar.setPadding(0, Utils.getStatusBarHeight(this), 0, 0);

            this.setSupportActionBar(toolbar);

            ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
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

        layoutManager = new LinearLayoutManager(this);
        // if changeAnimation is enabled it looks like shit; try it yourself
        ((SimpleItemAnimator)chatListRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        chatListAdapter = new ChatListAdapter(getObserverApplication(), this);
        chatListRecyclerView.setAdapter(chatListAdapter);
        chatListRecyclerView.setLayoutManager(layoutManager);
        chatListRecyclerView.addItemDecoration(new ItemDivider(this, R.drawable.chat_list_divider));

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
                    getObserverApplication().sendRequest(new TdApi.GetChats(offsetOrder, offsetChatId, limit));

                    loading = true;
                }
            }
        });

        getObserverApplication().sendRequest(new TdApi.GetMe(), new Client.ResultHandler() {
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
//
//        offsetChatId = 0;
//        offsetOrder = 9223372036854775807L;
        getObserverApplication().sendRequest(new TdApi.GetChats(offsetOrder, offsetChatId, limit));

        String fullName = Utils.getFullName(ObserverApplication.userMe.firstName, ObserverApplication.userMe.lastName);
        String phoneNumber = "+" + ObserverApplication.userMe.phoneNumber;

        header = navigationView.getHeaderView(0);
        userProfilePhoto = (AvatarImageView)header.findViewById(R.id.userMe_image);
        userProfilePhoto.setInitials(Utils.getInitials(fullName));

        ((TextView)header.findViewById(R.id.userMe_name)).setText(fullName);
        ((TextView)header.findViewById(R.id.userMe_phone)).setText(phoneNumber);

        // ТУТ ВООБЩЕ ХЗ КАК НЕ КРАШИТСЯ
        glideTarget = new ViewTarget<ImageView, Bitmap>(userProfilePhoto) {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), resource);
                roundedBitmapDrawable.setCircular(true);
                ((ImageView)header.findViewById(R.id.userMe_image)).setImageDrawable(roundedBitmapDrawable);
            }
        };

        if (!(ObserverApplication.userMe.profilePhoto.big.path.isEmpty())) {
            avatarImageFilePath = ObserverApplication.userMe.profilePhoto.big.path;
//            Glide.with(getActivity()).load(avatarImageFilePath).asBitmap().fitCenter().into(glideTarget);
        } else {
            avatarImageFilePath = null;

            if (ObserverApplication.userMe.profilePhoto.big.id != 0) {
                getObserverApplication().sendRequest(new TdApi.DownloadFile(ObserverApplication.userMe.profilePhoto.big.id));
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        ObserverApplication application = getObserverApplication();

//        application.addObserver(this);

        if (getCurrentFragment()==null)
            application.sendRequest(new TdApi.GetAuthState());
    }


    @Override
    protected void onPause() {
        super.onPause();

        ObserverApplication application = getObserverApplication();
        application.removeObserver(this);
    }

    // TDAPI
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
    public void proceed(TdApi.UpdateChatTitle obj) {
        chatListAdapter.updateChatTitle(obj);
    }

    @Override
    public void proceed(TdApi.Error err) {
    }

    @Override
    public void proceed(TdApi.UpdateChatOrder obj) {
        // TODO: OR THAT ?
//        offsetOrder = obj.order;
//        offsetChatId = obj.chatId;
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
    public void proceed(TdApi.AuthState obj) {
        switch (obj.getConstructor()) {
            case TdApi.AuthStateWaitPhoneNumber.CONSTRUCTOR:
                if (getCurrentFragment() instanceof AuthPhoneNumberFragment)
                    break;
                replaceFragment(AuthPhoneNumberFragment.newInstance(), false);
                break;

            case TdApi.AuthStateWaitCode.CONSTRUCTOR:
                replaceFragment(AuthCodeFragment.newInstance(), true);
                break;

            case TdApi.AuthStateOk.CONSTRUCTOR:
                fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                break;
            case TdApi.AuthStateWaitName.CONSTRUCTOR:
                replaceFragment(AuthNameFragment.newInstance(), true);
                break;

            default:
                Toast.makeText(this, obj.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    // FRAGMENT MANAGEMENT
    private FragmentManager fragmentManager = getSupportFragmentManager();
    private FragmentTransaction ft = null;


    private Fragment getCurrentFragment() {
        return fragmentManager.findFragmentById(R.id.base_fragment);
    }

    public void replaceFragment(Fragment f, boolean addToBackStack) {
        ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left);

        ft.replace(R.id.base_fragment, f);
        if (addToBackStack) {
            ft.addToBackStack(f.getClass().getName());
        }
        ft.commit();
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
