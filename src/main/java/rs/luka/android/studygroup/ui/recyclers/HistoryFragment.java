package rs.luka.android.studygroup.ui.recyclers;

import android.app.Activity;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import java.text.DateFormat;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.network.AdditionalCursors;
import rs.luka.android.studygroup.model.History;
import rs.luka.android.studygroup.model.PastEvents;
import rs.luka.android.studygroup.ui.CursorAdapter;

/**
 * Created by luka on 20.9.15..
 */
public class HistoryFragment extends Fragment {
    private static final String ARG_ITEM = "aitem";

    private PastEvents item;

    private CircularProgressView progress;
    private RecyclerView         recycler;
    private CursorAdapter        adapter;

    public static HistoryFragment newInstance(PastEvents item) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_ITEM, (Parcelable) item);
        HistoryFragment fragment = new HistoryFragment();
        fragment.setArguments(args);
        return fragment;
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
        setData(); // TODO: 19.9.15.

        return view;
    }

    private void setData() {
        if (adapter == null) {
            adapter = new HistoryAdapter(getActivity(), null);
            recycler.setAdapter(adapter);
        }
        // TODO: 19.9.15. PastEvents#getHistory
    }

    //@Override
    public Loader<Cursor> onCreateLoader(/*int id, Bundle args*/) {
        progress.setVisibility(View.VISIBLE);
        if (item == null) { throw new AssertionError("wtf"); }
        //return group.getLoader todo
        return null;
    }

    //@Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
        if (progress != null) {
            progress.setVisibility(View.GONE);
        }
    }

    private class HistoryHolder extends RecyclerView.ViewHolder {
        private TextView entryTextView;

        private History item;

        public HistoryHolder(View itemView) {
            super(itemView);

            entryTextView = (TextView) itemView.findViewById(R.id.list_item_history);
        }

        public void bindItem(History item) {
            entryTextView.setText(getString(R.string.history_entry,
                                            item.getAuthor(),
                                            DateFormat.getDateTimeInstance().format(item.getDate()),
                                            item.getPrev()));
        }
    }

    private class HistoryAdapter extends CursorAdapter<HistoryHolder> {

        public HistoryAdapter(Context context, Cursor cursor) {
            super(context, cursor);
        }

        @Override
        public HistoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_user,
                                               parent,
                                               false);
            return new HistoryHolder(view);
        }

        @Override
        public void onBindViewHolder(HistoryHolder holder, Cursor data) {
            holder.bindItem(((AdditionalCursors.HistoryCursor) data).getHistory());
        }
    }
}
