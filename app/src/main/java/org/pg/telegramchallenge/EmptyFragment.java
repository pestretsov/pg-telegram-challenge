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
import org.pg.telegramchallenge.views.TextUserMessageView;


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
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        View view = inflater.inflate(R.layout.fragment_empty, container, false);

        ChatListItemView itemView = (ChatListItemView) view.findViewById(R.id.sample_view);
        itemView.setUnreadCount(1);
        itemView.setStatus(ChatListItemView.ChatStatus.UNREAD);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.requestLayout();
                v.invalidate();
            }
        });

        BaseChatItemView messageView = (BaseChatItemView) view.findViewById(R.id.message);
        messageView.setOnClickListener(new View.OnClickListener() {
            int state = 0;
            StringBuilder prev = new StringBuilder();
            boolean[] bools = new boolean[3];
            @Override
            public void onClick(View v) {
                state++;
//                BaseUserMessageView mView = (BaseUserMessageView) v;
//                switch (state%bools.length) {
//                    case 0:
//                        mView.setBarVisability(bools[state%bools.length]);
//                        break;
//                    case 1:
//                        mView.setDateVisability(bools[state%bools.length]);
//                        break;
//                    case 2:
//                        mView.setAvatarAndTitleAreDisplayed(bools[state%bools.length]);
//                        break;
//                }
//                bools[state%bools.length] = !bools[state%bools.length];

                TextUserMessageView textUserMessageView = (TextUserMessageView) v;
                prev.append(Integer.toString(state++));
                prev.append(' ');
                textUserMessageView.setText(prev.toString());
            }
        });

        return view;
    }


}
