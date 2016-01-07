package rs.luka.android.studygroup.ui.recyclers;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.io.IOException;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.io.Loaders;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.ui.CursorAdapter;
import rs.luka.android.studygroup.ui.PoliteSwipeRefreshLayout;
import rs.luka.android.studygroup.ui.dialogs.ErrorDialog;
import rs.luka.android.studygroup.ui.singleitemactivities.AddGroupActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.LoginActivity;

/**
 * Created by luka on 17.7.15..
 */
public class GroupListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int REQUEST_ADD_GROUP  = 0;
    private static final int REQUEST_EDIT_GROUP = 1;

    private RecyclerView       recycler;
    private Callbacks          callbacks;
    private GroupAdapter       adapter;
    private FloatingActionButton fab;
    private PoliteSwipeRefreshLayout swipe;
    private CircularProgressView     progress;
    private NetworkExceptionHandler exceptionHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        callbacks = (Callbacks) activity;
        exceptionHandler = new NetworkExceptionHandler.DefaultHandler((AppCompatActivity)activity); //fixme dangerous ?
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_list, container, false);

        progress = (CircularProgressView) view.findViewById(R.id.progress_view);

        recycler = (RecyclerView) view.findViewById(R.id.group_recycler_view);
        fab = (FloatingActionButton) view.findViewById(R.id.fab_add_group);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getActivity(), AddGroupActivity.class), REQUEST_ADD_GROUP);
            }
        });
        final LinearLayoutManager lm = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(lm);
        registerForContextMenu(recycler);
        setData();

        swipe = (PoliteSwipeRefreshLayout) view.findViewById(R.id.group_list_swipe);
        swipe.setOnChildScrollUpListener(new PoliteSwipeRefreshLayout.OnChildScrollUpListener() {
            @Override
            public boolean canChildScrollUp() {
                return lm.findFirstCompletelyVisibleItemPosition() != 0;
            }
        });
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_GROUP:
            case REQUEST_EDIT_GROUP:
                refresh();
                break;
        }
    }

    protected void refresh() {
        swipe.setRefreshing(true);
        DataManager.refreshGroups(getContext(), this, getActivity().getLoaderManager(), exceptionHandler);
    }

    public void stopRefreshing() {
        swipe.setRefreshing(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_group_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.join_group:
                // TODO: 9.9.15.
                return true;
            case R.id.settings:
                // TODO: 9.9.15.
                return true;
        }
        return false;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_edit:
                callbacks.onEditGroup(adapter.selectedGroup, REQUEST_EDIT_GROUP);
                return true;
        }
        return onContextItemSelected(item);
    }

    public void setData() {
        if (adapter == null) {
            adapter = new GroupAdapter(getActivity(), null);
            recycler.setAdapter(adapter);
        }
        DataManager.getGroups(getActivity(), this, getActivity().getLoaderManager(), exceptionHandler);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.VISIBLE);
            }
        });
        return new Loaders.GroupLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
        stopRefreshing();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }



    public interface Callbacks {
        void onGroupSelected(Group group);

        void onEditGroup(Group group, int requestCode);
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
            if (group.hasImage()) {
                image.setImageBitmap(group.getImage(getResources().getDimensionPixelSize(R.dimen.card_image_size)));
            }
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

    private class GroupAdapter extends CursorAdapter<GroupHolder> {
        private Group selectedGroup;

        public GroupAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public void onBindViewHolder(GroupHolder viewHolder, Cursor cursor) {
            viewHolder.bindGroup(((Database.GroupCursor) cursor).getGroup());
        }

        @Override
        public GroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.card_group,
                                               parent,
                                               false);
            return new GroupHolder(view);
        }
    }
}
