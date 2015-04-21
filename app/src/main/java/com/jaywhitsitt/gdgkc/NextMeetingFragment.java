package com.jaywhitsitt.gdgkc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * This class handles displaying the next meeting UI.
 * Created by jay on 4/19/15.
 */
public class NextMeetingFragment extends android.support.v4.app.Fragment {

    public NextMeetingFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_next_meeting, container, false);

        TextView dateView = (TextView) rootView.findViewById(R.id.next_meeting_date);
        TextView timeView = (TextView) rootView.findViewById(R.id.next_meeting_time);

        return rootView;
    }
}
