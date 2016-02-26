package org.pg.telegramchallenge;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.pg.telegramchallenge.views.BaseChatItemView;
import org.pg.telegramchallenge.views.BaseUserMessageView;
import org.pg.telegramchallenge.views.ChatListItemView;


/**
 * A simple {@link Fragment} subclass.
 */
public class EmptyFragment extends Fragment {


    public EmptyFragment() {
        // Required empty public constructor
    }

    public static EmptyFragment newInstance() {

        Bundle args = new Bundle();

        EmptyFragment fragment = new EmptyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_empty, container, false);

        ChatListItemView itemView = (ChatListItemView) view.findViewById(R.id.sample_view);
        itemView.setStatus(ChatListItemView.MessageStatus.UNREAD);

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
            int i = 0;
            @Override
            public void onClick(View v) {
//                ((BaseChatItemView) v).setDateVisability(b);
//                ((BaseChatItemView) v).setBarVisability(b);
//                ((BaseUserMessageView) v).setDetailsVisibility(b);
                ((BaseUserMessageView) v).setStatus(ChatListItemView.MessageStatus.values()[i++%3]);

                b = !b;

            }
        });

        return view;
    }


}
