package com.jaywhitsitt.gdgkc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        return inflater.inflate(R.layout.fragment_next_meeting, container, false);
    }
}
