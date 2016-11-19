package rs.luka.android.studygroup.ui.recyclers;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.network.Network;
import rs.luka.android.studygroup.model.Edit;
import rs.luka.android.studygroup.model.PastEvents;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;

/**
 * Created by luka on 20.9.15..
 */
public class HistoryFragment extends Fragment implements Network.NetworkCallbacks<String> {
    private static final String ARG_ITEM = "aitem";
    private static final String TAG      = HistoryFragment.class.getSimpleName();

    private PastEvents item;

    private CircularProgressView progress;
    private RecyclerView         recycler;
    private HistoryAdapter        adapter;
    private NetworkExceptionHandler exceptionHandler;

    public static HistoryFragment newInstance(Parcelable item) {
        if(!(item instanceof PastEvents))
            throw new ClassCastException("Item passed to HistoryFragment doesn't implement PastEvents");
        Bundle args = new Bundle();
        args.putParcelable(ARG_ITEM, item);
        HistoryFragment fragment = new HistoryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        final AppCompatActivity ac = (AppCompatActivity)context;
        exceptionHandler = new NetworkExceptionHandler.DefaultHandler(ac) {
            @Override
            public void handleOffline() {
                InfoDialog.newInstance(getString(R.string.error_offline_history_title),
                                       getString(R.string.error_offline_history_text))
                          .show(ac.getSupportFragmentManager(), "");
                Network.Status.setOffline();
            }
        };
        ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        item = getArguments().getParcelable(ARG_ITEM);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        progress = (CircularProgressView) view.findViewById(R.id.progress_view);
        Activity ac = getActivity();
        recycler = (RecyclerView) view.findViewById(R.id.history_recycler);
        final LinearLayoutManager lm = new LinearLayoutManager(ac);
        recycler.setLayoutManager(lm);
        setData();

        return view;
    }

    private void setData() {
        if (adapter == null) {
            adapter = new HistoryAdapter(new ArrayList<Edit>(0));
            recycler.setAdapter(adapter);
        }
        item.getHistory(0, this);
    }

    @Override
    public void onRequestCompleted(int id, final Network.Response<String> response) {
        if (response.responseCode == Network.Response.RESPONSE_OK) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONArray jsonArray = new JSONArray(response.responseData);
                        JSONObject jsonEdit, jsonUser;
                        List<Edit> edits = new ArrayList<>(jsonArray.length());
                        for(int i=0; i<jsonArray.length(); i++) {
                            jsonEdit = jsonArray.getJSONObject(i);
                            jsonUser = jsonEdit.getJSONObject("editor");
                            User u = new User(jsonUser.getLong("id"), jsonUser.getString("username"), 0, jsonUser.getBoolean("hasImage"));
                            edits.add(new Edit(u,  jsonEdit.getInt("action"), jsonEdit.getLong("time")));
                        }
                        Collections.reverse(edits);
                        adapter.edits = edits;
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        exceptionHandler.handleJsonException();
                    }
                }
            });
        } else {
            response.handleErrorCode(exceptionHandler);
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onExceptionThrown(int id, Throwable ex) {
        if(ex instanceof Error)
            throw new Error(ex);
        if(ex instanceof IOException)
            exceptionHandler.handleIOException((IOException)ex);
        else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    InfoDialog.newInstance(getString(R.string.error_unknown_ex_title),
                                           getString(R.string.error_unknown_ex_text))
                              .show(getFragmentManager(), "");
                }
            });
            Log.e(TAG, "Unknown Throwable caught", ex);
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.GONE);
            }
        });
    }

    private class HistoryHolder extends RecyclerView.ViewHolder {
        private TextView entryTextView;

        private Edit item;

        public HistoryHolder(View itemView) {
            super(itemView);

            entryTextView = (TextView) itemView.findViewById(R.id.list_item_history);
        }

        public void bindItem(Edit item) {
            this.item = item;
            entryTextView.setText(getString(R.string.history_entry,
                                            item.getUserName(),
                                            item.getLocalizedAction(getContext()),
                                            item.getLocalizedDate(getContext())));
        }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryHolder> {

        private List<Edit> edits = new ArrayList<>();
        public HistoryAdapter(List<Edit> edits) {
            this.edits = edits;
        }

        @Override
        public HistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_history,
                                               parent,
                                               false);
            return new HistoryHolder(view);
        }

        @Override
        public void onBindViewHolder(HistoryHolder holder, int position) {
            holder.bindItem(edits.get(position));
        }

        @Override
        public int getItemCount() {
            return edits.size();
        }
    }
}
