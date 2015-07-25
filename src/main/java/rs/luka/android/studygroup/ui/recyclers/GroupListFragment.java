package rs.luka.android.studygroup.ui.recyclers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.Retriever;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.ID;
import rs.luka.android.studygroup.ui.singleitemactivities.AddGroupActivity;

/**
 * Created by luka on 17.7.15..
 */
public class GroupListFragment extends Fragment {
    private RecyclerView       recycler;
    private Callbacks          callbacks;
    private GroupAdapter       adapter;
    private FloatingActionButton fab;
    private SwipeRefreshLayout swipe;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (Callbacks) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_list, container, false);

        recycler = (RecyclerView) view.findViewById(R.id.group_recycler_view);
        fab = (FloatingActionButton) view.findViewById(R.id.fab_add_group);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddGroupActivity.class));
            }
        });
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        registerForContextMenu(recycler);
        updateUI();

        swipe = (SwipeRefreshLayout) view.findViewById(R.id.group_list_swipe);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        swipe.setColorSchemeResources(R.color.refresh_progress_1,
                                      R.color.refresh_progress_2,
                                      R.color.refresh_progress_3);

        return view;
    }

    public void stopRefreshing() {
        swipe.setRefreshing(false);
    }

    private void refresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Group> newGroups = new LinkedList<>();
                newGroups.add(new Group(new ID(System.currentTimeMillis(),
                                               (short) new Random().nextInt(65535)), "MG", "BG"));
                newGroups.add(new Group(new ID(System.currentTimeMillis(),
                                               (short) new Random().nextInt(65535)), "MG - OS", "BG"));
                adapter.setGroups(newGroups);
                adapter.notifyDataSetChanged();
                stopRefreshing();
            }
        }, 800);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //TODO
        //inflater.inflate(R.menu.fragment_group, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_edit:
                callbacks.onEditGroup(adapter.selectedGroup);
                return true;
        }
        return onContextItemSelected(item);
    }

    public void updateUI() {
        List<Group> groups = Retriever.getGroups();

        if (adapter == null) {
            adapter = new GroupAdapter(groups);
            recycler.setAdapter(adapter);
        } else {
            adapter.setGroups(groups);
            adapter.notifyDataSetChanged();
        }
    }

    public interface Callbacks {
        void onGroupSelected(Group group);

        void onEditGroup(Group group);
    }

    private class GroupHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener,
                       View.OnCreateContextMenuListener {
        private TextView name;
        private TextView place;
        private ImageView image;

        private Group group;

        public GroupHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            itemView.setOnCreateContextMenuListener(this);

            name = (TextView) itemView.findViewById(R.id.card_group_name_text);
            place = (TextView) itemView.findViewById(R.id.card_group_place_text);
            image = (ImageView) itemView.findViewById(R.id.card_group_image);
        }

        public void bindGroup(Group group) {
            this.group = group;
            name.setText(group.getName());
            place.setText(group.getPlace());
            if (group.hasImage()) { image.setImageBitmap(group.getImage()); }
        }

        @Override
        public void onClick(View v) {
            callbacks.onGroupSelected(group);
        }

        @Override
        public boolean onLongClick(View v) {
            adapter.selectedGroup = group;
            return false;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            getActivity().getMenuInflater().inflate(R.menu.context_group_list, menu);
        }
    }

    private class GroupAdapter extends RecyclerView.Adapter<GroupHolder> {
        private Group selectedGroup = null;

        private List<Group> groups;

        public GroupAdapter(List<Group> groups) {
            this.groups = groups;
        }

        @Override
        public GroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.card_group,
                                               parent,
                                               false);
            return new GroupHolder(view);
        }

        @Override
        public void onBindViewHolder(GroupHolder holder, int position) {
            Group group = groups.get(position);
            holder.bindGroup(group);
        }

        @Override
        public int getItemCount() {
            return groups.size();
        }

        public void setGroups(List<Group> groups) {
            this.groups = groups;
        }
    }
}
