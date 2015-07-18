package rs.luka.android.studygroup;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.networkcontroller.Retriever;

/**
 * Created by luka on 17.7.15..
 */
public class GroupListFragment extends Fragment {
    private RecyclerView recycler;
    private Callbacks callbacks;
    private GroupAdapter adapter;
    private FloatingActionButton fab;

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
                //TODO:
                //startActivity(new Intent(getActivity(), AddCourseActivity.class));
            }
        });
        recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();

        return view;
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
    }

    private class GroupHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView name;
        private TextView place;
        private ImageView image;

        private Group group;

        public GroupHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            name = (TextView) itemView.findViewById(R.id.card_group_name_text);
            place = (TextView) itemView.findViewById(R.id.card_group_place_text);
            image = (ImageView) itemView.findViewById(R.id.card_group_image);
        }

        public void bindGroup(Group group) {
            this.group = group;
            name.setText(group.getName());
            place.setText(group.getPlace());
            image.setImageBitmap(Retriever.getGroupImage(group.getId()));
        }

        @Override
        public void onClick(View v) {
            callbacks.onGroupSelected(group);
        }
    }

    private class GroupAdapter extends RecyclerView.Adapter<GroupHolder> {

        private List<Group> groups;

        public GroupAdapter(List<Group> groups) {
            this.groups = groups;
        }

        @Override
        public GroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.card_group, parent, false);
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
