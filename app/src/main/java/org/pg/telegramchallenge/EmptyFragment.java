package org.pg.telegramchallenge;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        return view;
    }


}
