package bumblebees.hobee.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.google.gson.Gson;

import java.util.ArrayList;

import bumblebees.hobee.R;
import bumblebees.hobee.objects.Event;
import bumblebees.hobee.utilities.HobbyExpandableListAdapter;
import bumblebees.hobee.utilities.SessionManager;


public class EventsMainFragment extends Fragment {

    Gson gson = new Gson();
    private ArrayList<Pair<String, ArrayList<Event>>> content;

    private ExpandableListView eventsTabList;

    private HobbyExpandableListAdapter adapter;
    private SwipeRefreshLayout refreshLayout;
    private SessionManager session;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.events_tab, container, false);
        eventsTabList = (ExpandableListView) view.findViewById(R.id.eventsTabList);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               updateData();
                refreshLayout.setRefreshing(false);
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        session = new SessionManager(getContext());
        content.clear();
        content.add(new Pair<>("Hosted events", session.getHostedEvents()));
        content.add(new Pair<>("Joined events", session.getJoinedEvents()));
        content.add(new Pair<>("Pending events", session.getPendingEvents()));

        adapter = new HobbyExpandableListAdapter(getActivity().getApplicationContext(), content);
        eventsTabList.setAdapter(adapter);

        //expand all groups by default
        for(int i=0;i<content.size(); i++){
            eventsTabList.expandGroup(i);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();

    }

    /**
     * Retrieve the data from the preferences again and add it to the list again.
     */
    private void updateData(){
        if(adapter!=null){
            content.set(0, new Pair<>("Hosted events", session.getHostedEvents()));
            content.set(1, new Pair<>("Joined events", session.getJoinedEvents()));
            content.set(2, new Pair<>("Pending events", session.getPendingEvents()));
            adapter.notifyDataSetChanged();
        }

    }
}
