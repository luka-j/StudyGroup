package rs.luka.android.studygroup.ui.recyclers;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.misc.Utils;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.ID;
import rs.luka.android.studygroup.network.Groups;
import rs.luka.android.studygroup.network.Network;
import rs.luka.android.studygroup.ui.PoliteSwipeRefreshLayout;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;
import rs.luka.android.studygroup.ui.singleitemactivities.LoadingActivity;

/**
 * Created by luka on 3.2.16..
 */
public class GroupSearchActivity extends AppCompatActivity implements Network.NetworkCallbacks<String> {
    private static final int REQUEST_ADD_GROUP = 0; //activity request
    private static final int REQUEST_SEARCH_GROUPS = 1; //network request

    public static final String EXTRA_SEARCH_TERM = "eSearchTerm";
    private static final String TAG              = GroupSearchActivity.class.getSimpleName();
    private NetworkExceptionHandler exceptionHandler;
    private GroupAdapter adapter;
    private String searchTerm;

    private Toolbar                  toolbar;
    private RecyclerView             recycler;
    private FloatingActionButton     fab;
    private PoliteSwipeRefreshLayout swipe;
    private CircularProgressView     progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_search);

        exceptionHandler = new NetworkExceptionHandler.DefaultHandler(this) {
            @Override
            public void handleOffline() {
                InfoDialog.newInstance(getString(R.string.error_offline_search_title),
                                       getString(R.string.error_offline_search_text))
                        .show(getSupportFragmentManager(), "");
            }
        };
        searchTerm = getIntent().getStringExtra(EXTRA_SEARCH_TERM);
        recycler = (RecyclerView) findViewById(R.id.group_recycler_view);
        progress = (CircularProgressView) findViewById(R.id.progress_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*fab = (FloatingActionButton) findViewById(R.id.fab_add_group);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(GroupSearchActivity.this, AddGroupActivity.class), REQUEST_ADD_GROUP);
            }
        });*/
        final LinearLayoutManager lm = new LinearLayoutManager(this);
        recycler.setLayoutManager(lm);
        registerForContextMenu(recycler);
        setData();

        swipe = (PoliteSwipeRefreshLayout) findViewById(R.id.group_list_swipe);
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ADD_GROUP:
                startActivity(new Intent(this, LoadingActivity.class));
        }
    }

    private void setData() {
        if (adapter == null) {
            adapter = new GroupAdapter();
            recycler.setAdapter(adapter);
        }
        Groups.search(REQUEST_SEARCH_GROUPS, searchTerm, this);
        progress.setVisibility(View.VISIBLE);
    }

    private void refresh() {
        Groups.search(REQUEST_SEARCH_GROUPS, searchTerm, this);
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestCompleted(int id, final Network.Response<String> response) {
        if(response.responseCode == Network.Response.RESPONSE_OK) {
            switch (id) {
                case REQUEST_SEARCH_GROUPS:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray jsonGroups = new JSONArray(response.responseData);
                                List<Group> groups = new ArrayList<>(jsonGroups.length());
                                for(int i=0; i<jsonGroups.length(); i++) {
                                    JSONObject jsonGroup = jsonGroups.getJSONObject(i);
                                    groups.add(new Group(new ID(jsonGroup.getLong("id")),
                                                         jsonGroup.getString("name"),
                                                         jsonGroup.getString("place"),
                                                         jsonGroup.getBoolean("hasImage"),
                                                         Utils.stringToList(jsonGroup.getString("courseYears")),
                                                         Group.PERM_READ_PUBLIC));
                                }
                                adapter.groups = groups;
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                exceptionHandler.handleJsonException();
                            }
                        }
                    });
                    break;
            }
        } else {
            response.handleErrorCode(exceptionHandler);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.GONE);
                swipe.setRefreshing(false);
            }
        });
    }

    @Override
    public void onExceptionThrown(int id, final Throwable ex) {
        if(ex instanceof Error) {
            throw new Error(ex);
        } else if (ex instanceof IOException) {
            exceptionHandler.handleIOException((IOException)ex);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog.newInstance(getString(R.string.error_unknown_ex_title),
                                           getString(R.string.error_unknown_ex_text))
                            .show(getSupportFragmentManager(), "");
                    Log.e(TAG, "Unknown Exception caught", ex);
                }
            });
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.GONE);
                swipe.setRefreshing(false);
            }
        });
    }

    private class GroupHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private TextView  name;
        private TextView  place;
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
            if (group.hasImage()) {
                group.getImage(GroupSearchActivity.this,
                               getResources().getDimensionPixelSize(R.dimen.card_image_size),
                               exceptionHandler, image);
            }
        }

        @Override
        public void onClick(View v) {
            startActivity(new Intent(GroupSearchActivity.this, GroupActivity.class)
                                  .putExtra(GroupActivity.EXTRA_GROUP, group));
        }
    }

    private class GroupAdapter extends RecyclerView.Adapter<GroupHolder> {
        private List<Group> groups = new ArrayList<>();

        @Override
        public GroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(GroupSearchActivity.this);
            View view = layoutInflater.inflate(R.layout.card_group,
                                               parent,
                                               false);
            return new GroupHolder(view);
        }

        @Override
        public void onBindViewHolder(GroupHolder holder, int position) {
            holder.bindGroup(groups.get(position));
        }

        @Override
        public int getItemCount() {
            return groups.size();
        }
    }
}
