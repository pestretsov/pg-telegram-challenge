package org.pg.telegramchallenge;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TG;
import org.drinkless.td.libcore.telegram.TdApi;
import org.pg.telegramchallenge.service.HandlerService;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by roman on 22.10.15.
 */
public class ObserverApplication extends Application implements Client.ResultHandler {

    public static volatile Context appContext;
    public static final String TAG = ObserverApplication.class.getSimpleName();

    public static final int PRIVATE_CHAT_INFO = 0, GROUP_CHAT_INFO = 1, SECRET_CHAT_INFO = 2, CHANNEL_CHAT_INFO = 3;

    public static volatile TdApi.User userMe = null;

    private static volatile boolean isReadyForUpdates = false;

    // CACHE
    public static ArrayMap<Integer, TdApi.User> users = new ArrayMap<>();
    public static ArrayMap<Integer, TdApi.GroupFull> groupsFull = new ArrayMap<>();
    public static Map<Long, TdApi.Chat> chats = new ConcurrentHashMap<>();

    // Map<setId, Map<stickerId, Sticker>>
    public static Map<Long, SparseArray<TdApi.Sticker>> stickers = new HashMap<>();

    // NEVER MISS UPDATES
    private static LinkedList<TdApi.UpdateNewMessage> pendingUpdateNewMessage = new LinkedList<>();
    private static LinkedList<TdApi.UpdateChatReadOutbox> pendingUpdateChatReadOutbox = new LinkedList<>();
    private static LinkedList<TdApi.UpdateChatReadInbox> pendingUpdateChatReadInbox = new LinkedList<>();
    private static LinkedList<TdApi.UpdateChatTitle> pendingUpdateChatTitle = new LinkedList<>();

    static {
        try {
            System.loadLibrary("tdjni");
        } catch (Exception e) {
            Log.v("TLibrary", e.getMessage());
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(ObserverApplication.this, "Application is created!", Toast.LENGTH_SHORT).show();

        appContext = getApplicationContext();

        handler = new Handler();
        startService(new Intent(this, HandlerService.class));
    }

//    private static final List<Class> interfaces;
//    static {
//        interfaces = new LinkedList<>();
//        interfaces.add(OnAuthObserver.class);
//        interfaces.add(OnErrorObserver.class);
//    }


    private List<OnErrorObserver> onErrorObservers = new LinkedList<>();

    private List<OnUpdateNewMessageObserver> onUpdateNewMessageObservers = new LinkedList<>();
    private List<OnUpdateUserActionObserver> onUpdateUserActionObservers = new LinkedList<>();
    private List<OnUpdateChatReadOutboxObserver> onUpdateChatReadOutboxObservers = new LinkedList<>();
    private List<OnUpdateChatReadInboxObserver> onUpdateChatReadInboxObservers = new LinkedList<>();
    private List<OnUpdateChatTitleObserver> onUpdateChatTitleObservers = new LinkedList<>();
    private List<OnUpdateChatObserver> onUpdateChatObservers = new LinkedList<>();
    private List<OnUpdateChatOrderObserver> onUpdateChatOrderObservers = new LinkedList<>();
    private List<OnUpdateFileObserver> onUpdateFileObservers = new LinkedList<>();
    private List<OnUpdateChatPhotoObserver> onUpdateChatPhotoObservers = new LinkedList<>();
    private List<OnUpdateMessageIdObserver> onUpdateMessageIdObservers = new LinkedList<>();


    private List<OnAuthObserver> onAuthObservers = new LinkedList<>();
    private List<ChatsObserver> chatsObservers = new LinkedList<>();
    private List<ChatObserver> chatObservers = new LinkedList<>();
    private List<ConcreteChatObserver> concreteChatObservers = new LinkedList<>();
    private List<OnGetChatHistoryObserver> onGetChatHistoryObservers = new LinkedList<>();
    private List<OnGetGroupFullObserver> onGetGroupFullObservers = new LinkedList<>();

    // INTERFACES
    public interface IsWaitingForPendingUpdates {

    }
    // UPDATES
    public interface OnErrorObserver {void proceed(TdApi.Error err);}

    public interface OnUpdateNewMessageObserver {void proceed(TdApi.UpdateNewMessage obj);}
    public interface OnUpdateUserActionObserver {void proceed(TdApi.UpdateUserAction obj);}
    public interface OnUpdateChatReadOutboxObserver {void proceed(TdApi.UpdateChatReadOutbox obj);}
    public interface OnUpdateChatReadInboxObserver {void proceed(TdApi.UpdateChatReadInbox obj);}
    public interface OnUpdateChatObserver {void proceed(TdApi.UpdateChat obj);}
    public interface OnUpdateChatTitleObserver {void proceed(TdApi.UpdateChatTitle obj);}
    public interface OnUpdateChatOrderObserver {void proceed(TdApi.UpdateChatOrder obj);}
    public interface OnUpdateFileObserver {void proceed(TdApi.UpdateFile obj);}
    public interface OnUpdateChatPhotoObserver {void proceed(TdApi.UpdateChatPhoto obj);}
    public interface OnUpdateMessageIdObserver {void proceed(TdApi.UpdateMessageId obj);}
    // REQUESTS
    public interface OnAuthObserver {void proceed(TdApi.AuthState obj);}
    public interface ChatsObserver {void proceed(TdApi.Chats obj);}
    public interface ChatObserver {void proceed(TdApi.Chat obj);}
    public interface ConcreteChatObserver {long getChatId(); void proceed(TdApi.UpdateNewMessage obj); void proceed(TdApi.Message obj);}
    public interface OnGetChatHistoryObserver {void proceed(TdApi.Messages obj);}
    public interface OnGetGroupFullObserver {void proceed(TdApi.GroupFull obj);}

    /** for future
     * there can be multimap of classes of tdlib
     * associated with observers which have to get
     * objects of this type*/

    private Map<Class, List> observersLists = new HashMap<>();
    {
        observersLists.put(OnAuthObserver.class, onAuthObservers);
        observersLists.put(OnErrorObserver.class, onErrorObservers);
    }

    /** i think that one method for all observers is better
     * than one for each type of them*/
    public void addObserver(Object obs) {

        Log.e(TAG, Thread.currentThread().getName());

        if (obs instanceof OnUpdateFileObserver)
            onUpdateFileObservers.add((OnUpdateFileObserver) obs);

        if (obs instanceof OnAuthObserver)
            onAuthObservers.add((OnAuthObserver) obs);

        if (obs instanceof OnErrorObserver)
            onErrorObservers.add((OnErrorObserver) obs);

        if (obs instanceof OnUpdateNewMessageObserver) {
            onUpdateNewMessageObservers.add((OnUpdateNewMessageObserver) obs);

            if (obs instanceof IsWaitingForPendingUpdates) {

                for (TdApi.UpdateNewMessage upd : pendingUpdateNewMessage) {
                    ((OnUpdateNewMessageObserver) obs).proceed(upd);
                }

                pendingUpdateNewMessage.clear();
            }
        }

        if (obs instanceof OnUpdateUserActionObserver) {
            onUpdateUserActionObservers.add((OnUpdateUserActionObserver) obs);
        }

        if (obs instanceof OnUpdateChatReadInboxObserver) {
            onUpdateChatReadInboxObservers.add((OnUpdateChatReadInboxObserver) obs);

            if (obs instanceof IsWaitingForPendingUpdates) {
                for (TdApi.UpdateChatReadInbox upd : pendingUpdateChatReadInbox) {
                    ((OnUpdateChatReadInboxObserver) obs).proceed(upd);
                }

                pendingUpdateChatReadInbox.clear();
            }
        }

        if (obs instanceof OnUpdateChatReadOutboxObserver) {
            onUpdateChatReadOutboxObservers.add((OnUpdateChatReadOutboxObserver) obs);

            if (obs instanceof IsWaitingForPendingUpdates) {
                for (TdApi.UpdateChatReadOutbox upd : pendingUpdateChatReadOutbox) {
                    ((OnUpdateChatReadOutboxObserver) obs).proceed(upd);
                }

                pendingUpdateChatReadOutbox.clear();
            }
        }

        if (obs instanceof OnUpdateChatObserver) {
            onUpdateChatObservers.add((OnUpdateChatObserver) obs);
        }

        if (obs instanceof OnUpdateChatTitleObserver) {
            onUpdateChatTitleObservers.add((OnUpdateChatTitleObserver) obs);

            if (obs instanceof IsWaitingForPendingUpdates) {
                for (TdApi.UpdateChatTitle upd : pendingUpdateChatTitle) {
                    ((OnUpdateChatTitleObserver) obs).proceed(upd);
                }

                pendingUpdateChatTitle.clear();
            }
        }

        if (obs instanceof OnUpdateMessageIdObserver) {
            onUpdateMessageIdObservers.add((OnUpdateMessageIdObserver) obs);
        }

        if (obs instanceof OnUpdateChatPhotoObserver) {
            onUpdateChatPhotoObservers.add((OnUpdateChatPhotoObserver) obs);
        }

        if (obs instanceof OnUpdateChatOrderObserver) {
            onUpdateChatOrderObservers.add((OnUpdateChatOrderObserver) obs);
        }

        if (obs instanceof ChatsObserver) {
            chatsObservers.add((ChatsObserver) obs);
        }

        if (obs instanceof ChatObserver) {
            chatObservers.add((ChatObserver) obs);
        }

        if (obs instanceof OnGetChatHistoryObserver) {
            onGetChatHistoryObservers.add((OnGetChatHistoryObserver) obs);
        }

        if (obs instanceof ConcreteChatObserver) {
            concreteChatObservers.add((ConcreteChatObserver) obs);
        }

        if (obs instanceof OnGetGroupFullObserver) {
            onGetGroupFullObservers.add((OnGetGroupFullObserver) obs);
        }
    }

    public boolean isSubscribed(Object obj) {
        if (obj instanceof ChatObserver && chatObservers.contains(obj)) {
            return true;
        }

        if (obj instanceof OnUpdateNewMessageObserver && onUpdateNewMessageObservers.contains(obj)) {
            return true;
        }

        return false;
    }

    public void removeObserver(Object obs) {
        if (obs instanceof OnAuthObserver) {
            onAuthObservers.remove(obs);
        }

        if (obs instanceof OnErrorObserver) {
            onErrorObservers.remove(obs);
        }

        if (obs instanceof OnUpdateNewMessageObserver) {
            onUpdateNewMessageObservers.remove(obs);
        }

        if (obs instanceof OnUpdateUserActionObserver) {
            onUpdateUserActionObservers.remove(obs);
        }

        if (obs instanceof OnUpdateChatReadInboxObserver) {
            onUpdateChatReadInboxObservers.remove(obs);
        }

        if (obs instanceof OnUpdateChatReadOutboxObserver) {
            onUpdateChatReadOutboxObservers.remove(obs);
        }

        if (obs instanceof OnUpdateChatObserver) {
            onUpdateChatObservers.remove(obs);
        }

        if (obs instanceof OnUpdateChatTitleObserver) {
            onUpdateChatTitleObservers.remove(obs);
        }

        if (obs instanceof OnUpdateMessageIdObserver) {
            onUpdateMessageIdObservers.remove(obs);
        }

        if (obs instanceof OnUpdateChatPhotoObserver) {
            onUpdateChatPhotoObservers.remove(obs);
        }

        if (obs instanceof OnUpdateChatOrderObserver) {
            onUpdateChatOrderObservers.remove(obs);
        }

        if (obs instanceof ChatsObserver) {
            chatsObservers.remove(obs);
        }

        if (obs instanceof ChatObserver) {
            chatObservers.remove(obs);
        }

        if (obs instanceof OnGetChatHistoryObserver) {
            onGetChatHistoryObservers.remove(obs);
        }

        if (obs instanceof ConcreteChatObserver) {
            concreteChatObservers.remove(obs);
        }

        if (obs instanceof OnGetGroupFullObserver) {
            onGetGroupFullObservers.remove(obs);
        }
    }

    private List <TdApi.TLFunction> requestPool = new LinkedList<>();

    public void sendRequest(TdApi.TLFunction request) {
        Log.e(TAG, request.toString());
        if (TG.getClientInstance() == null) {
            requestPool.add(request);
        } else {
            TG.getClientInstance().send(request, this);
        }
    }

    public void sendRequest(TdApi.TLFunction request, Client.ResultHandler handler) {
        TG.getClientInstance().send(request, handler);
    }

    public boolean invokeRequestPool() {
        if (TG.getClientInstance() == null)
            return false;

        for (TdApi.TLFunction function: requestPool) {
            TG.getClientInstance().send(function, this);
        }

        requestPool.clear();

        return true;
    }

    Handler handler = new Handler();

    @Override
    public void onResult(TdApi.TLObject object) {
        handler.post(new HandlerRunnable(object));
        Log.e(TAG, Thread.currentThread().getName());
        Log.i(TAG, object.toString());
    }

    private class HandlerRunnable implements Runnable {

        final TdApi.TLObject object;
        public HandlerRunnable(TdApi.TLObject object){
            this.object = object;
        }

        @Override
        public void run() {

            Log.e(TAG, Thread.currentThread().getName());

            if (object instanceof TdApi.UpdateStickers) {
                sendRequest(new TdApi.GetStickerSets(false), new Client.ResultHandler() {
                    @Override
                    public void onResult(TdApi.TLObject object) {
                        object.toString();
                    }
                });

                object.toString();
            }

            if (object instanceof TdApi.Update && isReadyForUpdates) {


                if (object instanceof TdApi.UpdateFile) {
                    for (OnUpdateFileObserver observer: onUpdateFileObservers) {
                        observer.proceed((TdApi.UpdateFile) object);
                    }
                    return;
                }

                if (object instanceof TdApi.UpdateNewMessage) {
                    sendRequest(new TdApi.GetChat(((TdApi.UpdateNewMessage) object).message.chatId));

//                    if (((TdApi.UpdateNewMessage) object).message.content instanceof TdApi.MessageSticker) {
//                        TdApi.MessageSticker msg =(TdApi.MessageSticker) ((TdApi.UpdateNewMessage) object).message.content;
//
//                        if (stickers.containsKey(msg.sticker.setId)) {
//                            if (stickers.get(msg.sticker.setId).get(msg.sticker.sticker.id, null) != null) {
//                                // HAS THUMB? -- NO -- DOWNLOAD?
//                            } else {
//                                sendRequest(new TdApi.DownloadFile(msg.sticker.sticker.id), new Client.ResultHandler() {
//                                    @Override
//                                    public void onResult(TdApi.TLObject object) {
//                                        Log.e(TAG, object.toString());
//                                    }
//                                });
//                            }
//                        } else {
//                            sendRequest(new TdApi.GetStickerSet(msg.sticker.setId), new Client.ResultHandler() {
//                                @Override
//                                public void onResult(TdApi.TLObject object) {
//                                    ObserverApplication.stickers.put(((TdApi.StickerSet)object).id, new SparseArray<TdApi.Sticker>());
//                                }
//                            });
//                            // then download
//                        }
//                    }

                    if (onUpdateNewMessageObservers.size() == 0) {
                        pendingUpdateNewMessage.addLast((TdApi.UpdateNewMessage) object);
                    }

                    for (OnUpdateNewMessageObserver observer : onUpdateNewMessageObservers) {
                        observer.proceed((TdApi.UpdateNewMessage) object);
                    }

                    for (ConcreteChatObserver observer : concreteChatObservers) {
                        if (observer.getChatId() == ((TdApi.UpdateNewMessage) object).message.chatId) {
                            observer.proceed((TdApi.UpdateNewMessage) object);
                        }
                    }

                    return;
                }

                if (object instanceof TdApi.UpdateChatReadInbox) {
                    sendRequest(new TdApi.GetChat(((TdApi.UpdateChatReadInbox) object).chatId));
                    if (onUpdateChatReadInboxObservers.size() == 0) {
                        pendingUpdateChatReadInbox.addLast((TdApi.UpdateChatReadInbox) object);
                    }

                    for (OnUpdateChatReadInboxObserver observer : onUpdateChatReadInboxObservers) {
                        observer.proceed((TdApi.UpdateChatReadInbox) object);
                    }
                    return;
                }

                if (object instanceof TdApi.UpdateChatReadOutbox) {
                    sendRequest(new TdApi.GetChat(((TdApi.UpdateChatReadOutbox) object).chatId));
                    if (onUpdateChatReadOutboxObservers.size() == 0) {
                        pendingUpdateChatReadOutbox.addLast((TdApi.UpdateChatReadOutbox) object);
                    }

                    for (OnUpdateChatReadOutboxObserver observer : onUpdateChatReadOutboxObservers) {
                        observer.proceed((TdApi.UpdateChatReadOutbox) object);
                    }
                    return;
                }

                if (object instanceof TdApi.UpdateChat) {
                    chats.put(((TdApi.UpdateChat) object).chat.id, ((TdApi.UpdateChat) object).chat);

                    for (OnUpdateChatObserver observer : onUpdateChatObservers) {
                        observer.proceed((TdApi.UpdateChat) object);
                    }
                }

                if (object instanceof TdApi.UpdateChatTitle) {
                    sendRequest(new TdApi.GetChat(((TdApi.UpdateChatTitle) object).chatId));
                    if (onUpdateChatTitleObservers.size() == 0) {
                        pendingUpdateChatTitle.addLast((TdApi.UpdateChatTitle) object);
                    }

                    for (OnUpdateChatTitleObserver observer : onUpdateChatTitleObservers) {
                        observer.proceed((TdApi.UpdateChatTitle) object);
                    }
                    return;
                }

                if (object instanceof TdApi.UpdateUserAction) {
                    for (OnUpdateUserActionObserver observer : onUpdateUserActionObservers) {
                        observer.proceed((TdApi.UpdateUserAction) object);
                    }
                    return;
                }

                if (object instanceof TdApi.UpdateChatPhoto) {
                    sendRequest(new TdApi.GetChat(((TdApi.UpdateChatPhoto) object).chatId));
                    for (OnUpdateChatPhotoObserver observer : onUpdateChatPhotoObservers) {
                        observer.proceed((TdApi.UpdateChatPhoto) object);
                    }
                    return;
                }

                if (object instanceof TdApi.UpdateChatOrder) {
                    for (OnUpdateChatOrderObserver observer : onUpdateChatOrderObservers) {
                        observer.proceed((TdApi.UpdateChatOrder) object);
                    }
                    return;
                }


                if (object instanceof TdApi.UpdateUser) {
                    if (users.size() == 0 && userMe == null) {
                        userMe = ((TdApi.UpdateUser) object).user;
                        users.put(userMe.id, userMe);
                    } else {
                        TdApi.User user = ((TdApi.UpdateUser) object).user;
                        users.put(user.id, user);
                    }
                    return;
                }

                if (object instanceof TdApi.UpdateMessageId) {
                    for (OnUpdateMessageIdObserver observer : onUpdateMessageIdObservers) {
                        observer.proceed((TdApi.UpdateMessageId) object);
                    }
                    return;
                }

                return;
            }

            if (object instanceof TdApi.AuthState) {
                for (OnAuthObserver observer : onAuthObservers) {
                    observer.proceed((TdApi.AuthState) object);
                }
                return;
            }

            if (object instanceof TdApi.Error) {
                for (OnErrorObserver observer : onErrorObservers) {
                    observer.proceed((TdApi.Error) object);
                }
                return;
            }

            if (object instanceof TdApi.Chats) {
                for (TdApi.Chat chat : ((TdApi.Chats) object).chats) {
                    chats.put(chat.id, chat);
                    if (chat.type instanceof TdApi.PrivateChatInfo) {
                        users.put(((TdApi.PrivateChatInfo) chat.type).user.id, ((TdApi.PrivateChatInfo) chat.type).user);
                    }
                }

                if (!isReadyForUpdates) {
                    isReadyForUpdates = true;
                }
                for (ChatsObserver observer : chatsObservers) {
                    observer.proceed((TdApi.Chats) object);
                }
                return;
            }

            if (object instanceof TdApi.Chat) {
                chats.put(((TdApi.Chat) object).id, (TdApi.Chat) object);

                for (ChatObserver observer : chatObservers) {
                    observer.proceed((TdApi.Chat) object);
                }
                return;
            }

            if (object instanceof TdApi.Messages) {
                for (OnGetChatHistoryObserver observer : onGetChatHistoryObservers) {
                    observer.proceed((TdApi.Messages) object);
                }
                return;
            }

            if (object instanceof TdApi.GroupFull) {
                groupsFull.put(((TdApi.GroupFull) object).group.id, (TdApi.GroupFull) object);
                for (TdApi.ChatParticipant participant : ((TdApi.GroupFull) object).participants) {
                    users.put(participant.user.id, participant.user);
                }

                for (OnGetGroupFullObserver observer : onGetGroupFullObservers) {
                    observer.proceed((TdApi.GroupFull) object);
                }

                return;
            }

            if (object instanceof TdApi.Message) {
                for (ConcreteChatObserver observer : concreteChatObservers) {
                    if (observer.getChatId() == ((TdApi.Message) object).chatId) {
                        observer.proceed((TdApi.Message) object);
                    }
                }
            }

//            if (object instanceof TdApi.SendMessage) {
//                for (ConcreteChatObserver observer : concreteChatObservers) {
////                    observer.proceedConcrete(((TdApi.SendMessage) object).message);
//                }
//            }
        }
    }
}
