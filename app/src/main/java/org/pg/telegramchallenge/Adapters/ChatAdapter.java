package org.pg.telegramchallenge.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.MainActivity;
import org.pg.telegramchallenge.ObserverApplication;
import org.pg.telegramchallenge.R;
import org.pg.telegramchallenge.utils.Utils;
import org.pg.telegramchallenge.views.BaseUserMessageView;
import org.pg.telegramchallenge.views.ChatListItemView;
import org.pg.telegramchallenge.views.ImageUserMessageView;
import org.pg.telegramchallenge.views.TextUserMessageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Stack;

/**
 * Created by artemypestretsov on 2/18/16.
 */
public class ChatAdapter extends RecyclerView.Adapter<BaseViewHolder> implements ObserverApplication.OnGetChatHistoryObserver, ObserverApplication.OnUpdateChatReadOutboxObserver, ObserverApplication.OnUpdateFileObserver, ObserverApplication.ChatObserver, ObserverApplication.OnUpdateMessageIdObserver {
    private static final String TAG = ChatAdapter.class.getSimpleName();

    private static final int VIEW_TYPE_TEXT = 0;
    private static final int VIEW_TYPE_IMAGE = 1;
    private static final int VIEW_TYPE_STICKER = 2;

    private Long chatId = null;
    private TdApi.GroupFull groupFull = null;
    private TdApi.Chat chat = null;
    private int lastPos = 0;

    private ObserverApplication context;
    private MainActivity activity;

    private LinkedList<TdApi.Message> messagesList = new LinkedList<>();
//    private Map<Integer, TdApi.User> usersMap = new HashMap<>();
    private Map<Integer, TdApi.Message> sentMessagesList = new HashMap<>();

    public ChatAdapter(Context context, Activity activity, long id) {
        this.chatId = id;
        this.context = (ObserverApplication) context;
        this.activity = (MainActivity) activity;

        this.chat = ObserverApplication.chats.get(chatId);

        if (chat.type instanceof TdApi.GroupChatInfo) {
            // TODO: НУЖНО ГАРАНТИРОВАТЬ, ЧТО ГРУППА БУДЕТ В ХЭШМЭПЕ
            // либо сделать обсервером ---- ТАК И ПОСТУПИМ=D
            if (ObserverApplication.groupsFull.containsKey(((TdApi.GroupChatInfo) chat.type).group.id)) {
                groupFull = ObserverApplication.groupsFull.get(((TdApi.GroupChatInfo) chat.type).group.id);

//                for (TdApi.ChatParticipant participant : groupFull.participants) {
//                    usersMap.put(participant.user.id, participant.user);
//                }
            }
        } else if (chat.type instanceof TdApi.ChannelChatInfo) {
          // TODO: complete
        } else {
            // TODO: WHATS THE DIFFERENCE BETWEEN CHANNELS AND SO ON
            chatId = chat.id;

//            usersMap.put(((TdApi.PrivateChatInfo)chat.type).user.id, ((TdApi.PrivateChatInfo)chat.type).user);
//            usersMap.put(ObserverApplication.userMe.id, ObserverApplication.userMe);
        }
    }

    @Override
    public int getItemViewType(int position) {
        TdApi.Message item = messagesList.get(position);
        if (item.content instanceof TdApi.MessageText) {
            return VIEW_TYPE_TEXT;
        } else if (item.content instanceof TdApi.MessagePhoto) {
            return VIEW_TYPE_IMAGE;
        } else if (item.content instanceof TdApi.MessageSticker) {
            return VIEW_TYPE_STICKER;
        } else {
            return -1;
        }
//        return super.getItemViewType(position);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_TEXT:
                return new TextUserMessageViewHolder(inflateCustomView(R.layout.message_text_cell, parent));
            case VIEW_TYPE_IMAGE:
                return new ImageUserMessageViewHolder(inflateCustomView(R.layout.message_image_cell, parent));
            case VIEW_TYPE_STICKER:
                return new ImageUserMessageViewHolder(inflateCustomView(R.layout.message_image_cell, parent));
            default:
                return new TextUserMessageViewHolder(inflateCustomView(R.layout.message_text_cell, parent));
        }
    }

    private View inflateCustomView(int res, ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        if (messagesList == null) {
            return;
        }

        TdApi.Message msg = messagesList.get(position);
        TdApi.Message msgPrev = null;

        TdApi.User usr = ObserverApplication.users.get(msg.fromId);

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(Utils.timestampToMillis(msg.date)));
        Calendar calPrev = null;

        boolean sameDay = false;
        // just for fun
        long dt = 100_000_000_000L;

        if (position < messagesList.size()-1) {
            msgPrev = messagesList.get(position+1);
            calPrev = Calendar.getInstance();
            calPrev.setTime(new Date(Utils.timestampToMillis(msgPrev.date)));
            sameDay = cal.get(Calendar.YEAR) == calPrev.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == calPrev.get(Calendar.DAY_OF_YEAR);

            if (sameDay) {
                // millis - to seconds - to minutes
                dt = (cal.getTimeInMillis()/1000)/60 - (calPrev.getTimeInMillis()/1000)/60;
            }
        }

        holder.setDate(cal);

        if (sameDay) {
            holder.setDateVisibility(false);
        } else {
            holder.setDateVisibility(true);
            if (holder instanceof BaseUserMessageViewHolder) {
                ((BaseUserMessageViewHolder)holder).setDetailsVisibility(true);
            }
        }

        switch (getItemViewType(position)) {
            // TODO: ROMAN -- HERE
            case VIEW_TYPE_IMAGE:
                setPhoto((ImageUserMessageViewHolder)holder, (TdApi.MessagePhoto)msg.content);
                break;
            case VIEW_TYPE_STICKER:
                setSticker((ImageUserMessageViewHolder)holder, (TdApi.MessageSticker)msg.content);
                break;
            case VIEW_TYPE_TEXT:
                String text = ((TdApi.MessageText)msg.content).text;
                ((TextUserMessageViewHolder)holder).setText(text);
                break;
            default:
                ((TextUserMessageViewHolder)holder).setText("BOSS");
        }

        // TODO: usr can be null -- why!? -- проверить на чате библиотеки программиста
        if (holder instanceof BaseUserMessageViewHolder) {
            if (chat.type instanceof TdApi.ChannelChatInfo) {
                ((BaseUserMessageViewHolder) holder).setTitle(chat.title, null);
            } else {
                assert usr != null;
                ((BaseUserMessageViewHolder) holder).setTitle(usr.firstName, usr.lastName);
            }
            if (msgPrev != null && msgPrev.fromId == msg.fromId && dt <= 5) {
                ((BaseUserMessageViewHolder) holder).setDetailsVisibility(false);
            } else {
                ((BaseUserMessageViewHolder) holder).setDetailsVisibility(true);
            }

            ((BaseUserMessageViewHolder) holder).setStatus(ChatListItemView.MessageStatus.READ);
            if (!(msg.sendState instanceof TdApi.MessageIsIncoming) && msg.id > chat.lastReadOutboxMessageId) {
                ((BaseUserMessageViewHolder) holder).setStatus(ChatListItemView.MessageStatus.UNREAD);
            }

            try {
                handleAvatar((BaseUserMessageViewHolder)holder, usr.profilePhoto);
            } catch (NullPointerException e) {
                Log.e("TAG", String.valueOf(msg.fromId));
            }
        }
        holder.setBarVisibility(false);
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public void proceed(TdApi.Messages obj) {
        messagesList.addAll(Arrays.asList(obj.messages));
        this.notifyItemRangeInserted(lastPos, obj.totalCount);
        lastPos+=obj.totalCount;
    }

    @Override
    public void proceed(TdApi.UpdateChatReadOutbox obj) {
        chat = ObserverApplication.chats.get(chatId);
        this.notifyDataSetChanged();
    }

    @Override
    public void proceed(TdApi.Chat obj) {
        chat = ObserverApplication.chats.get(chatId);
        this.notifyDataSetChanged();
    }

//    @Override
//    public void proceed(TdApi.GroupFull obj) {
//        try {
//            groupFull = ObserverApplication.groupsFull.get(((TdApi.GroupChatInfo) chat.type).group.id);
//
//            for (TdApi.ChatParticipant participant : groupFull.participants) {
//                usersMap.put(participant.user.id, participant.user);
//            }
//        } catch (NullPointerException e) {
//            Log.e(TAG, e.toString());
//        }
//    }

    private boolean setSticker(ImageUserMessageViewHolder holder, TdApi.MessageSticker msg) {
        TdApi.Sticker s = msg.sticker;

        String path = s.sticker.path;
        int width = s.width;
        int height = s.height;
        holder.setImage(path, width, height);

        if (path.isEmpty()) {
            if (s.sticker.id != 0) {
                context.sendRequest(new TdApi.DownloadFile(s.sticker.id));
                return true;
            }
        }

        return false;

    }

    // TODO: add logic if no MEDIUM size
    private boolean setPhoto(ImageUserMessageViewHolder holder, TdApi.MessagePhoto msg) {
        TdApi.Photo p = msg.photo;

        String path = p.photos[2].photo.path;
        int width = p.photos[2].width;
        int height = p.photos[2].height;
        holder.setImage(path, width, height);

        if (path.isEmpty()) {
            if (p.photos[2].photo.id != 0) {
                context.sendRequest(new TdApi.DownloadFile(p.photos[2].photo.id));
                return true;
            }
        }

        return false;
    }

    /**
     *
     * @param holder to load avatar into
     * @param p which is need to be downloaded
     * @return true if had started downloading
     */
    private boolean handleAvatar(BaseUserMessageViewHolder holder, TdApi.ProfilePhoto p) {

        holder.setAvatarFilePath((p.small.path));

        if (p.small.path.isEmpty()) {
//            holder.setAvatarFilePath(null);
            if (p.small.id != 0) {
                context.sendRequest(new TdApi.DownloadFile(p.small.id));
                return true;
            }
        }

        return false;
    }

    @Override
    public void proceed(TdApi.UpdateFile obj) {
        int i = 0;
        for (TdApi.Message msg : messagesList) {
            if (ObserverApplication.users.containsKey(msg.fromId)) {
                TdApi.User usr = ObserverApplication.users.get(msg.fromId);
                if (obj.file.id == usr.profilePhoto.small.id) {
                    usr.profilePhoto.small = obj.file;
                    this.notifyItemChanged(i);
                    break;
                }
            }
            if (msg.content instanceof TdApi.MessagePhoto) {
                if (obj.file.id == ((TdApi.MessagePhoto) msg.content).photo.photos[2].photo.id) {
                    ((TdApi.MessagePhoto) msg.content).photo.photos[2].photo = obj.file;
                    this.notifyItemChanged(i);
                    break;
                }
            }
            if (msg.content instanceof TdApi.MessageSticker) {
                if (obj.file.id == ((TdApi.MessageSticker) msg.content).sticker.sticker.id) {
                    ((TdApi.MessageSticker) msg.content).sticker.sticker = obj.file;
                    this.notifyItemChanged(i);
                    break;
                }
            }
            i++;
        }
    }

    @Override
    public void proceed(TdApi.UpdateMessageId obj) {
        TdApi.Message msg = sentMessagesList.get(obj.oldId);
        msg.id = obj.newId;
    }

    public void proceed(TdApi.Message obj) {
        sentMessagesList.put(obj.id, obj);
        messagesList.addFirst(obj);
        this.notifyItemInserted(0);
    }

    public void proceed(TdApi.UpdateNewMessage obj) {
        messagesList.addFirst(obj.message);
        this.notifyItemInserted(0);
    }
}
