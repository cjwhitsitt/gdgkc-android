package com.jaywhitsitt.gdgkc;

        import android.app.Fragment;
        import android.os.Bundle;
        import android.support.annotation.Nullable;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;

/**
 * Created by jay on 4/19/15.
 */
public class NextMeetingFragment extends Fragment {

    public NextMeetingFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_next_meeting, container, false);
        return rootView;
    }
}
