package com.jaywhitsitt.gdgkc;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jaywhitsitt.gdgkc.data.FetchGDGTask;

/**
 * This class handles displaying the next meeting UI.
 * Created by jay on 4/19/15.
 */
public class NextMeetingFragment extends android.support.v4.app.Fragment {

    public NextMeetingFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // menu options?
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.next_meeting_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.  The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateChapter();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_next_meeting, container, false);

        TextView dateView = (TextView) rootView.findViewById(R.id.next_meeting_date);
        TextView timeView = (TextView) rootView.findViewById(R.id.next_meeting_time);

        return rootView;
    }

    private void updateChapter() {
        FetchGDGTask chapterTask = new FetchGDGTask(getActivity());
        chapterTask.execute();
    }
}
