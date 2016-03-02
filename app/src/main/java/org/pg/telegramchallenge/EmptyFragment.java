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
import org.pg.telegramchallenge.views.ImageUserMessageView;
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

        ImageUserMessageView itemView = (ImageUserMessageView) view.findViewById(R.id.sample_view_2);

//        itemView.setDetailsVisibility(false);
        itemView.setBarVisability(false);
        itemView.setDateVisability(false);
        itemView.setOnClickListener(new View.OnClickListener() {
            int width = 10;
            int height = 5;

            @Override
            public void onClick(View v) {
                ((ImageUserMessageView) v).setImage("http://www.saharniy-diabet.com/userfiles/apel.jpg", width, height);
                width *= 2;
                height *= 2;
            }
        });

        BaseUserMessageView messageView = (BaseUserMessageView) view.findViewById(R.id.message);
        messageView.setBarVisability(false);
        messageView.setDateVisability(false);
        messageView.setDetailsVisibility(false);
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
