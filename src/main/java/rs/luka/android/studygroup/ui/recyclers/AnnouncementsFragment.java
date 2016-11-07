package rs.luka.android.studygroup.ui.recyclers;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.model.Announcement;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.network.Groups;
import rs.luka.android.studygroup.network.Network;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;

/**
 * Created by luka on 29.10.16..
 */
public class AnnouncementsFragment extends Fragment implements Network.NetworkCallbacks<String> {
    private static final String ARG_GROUP              = "aGroup";
    private static final int REQUEST_GET_ANNOUNCEMENTS = 0;

    public static AnnouncementsFragment newInstance(Group group) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_GROUP, group);
        AnnouncementsFragment fragment = new AnnouncementsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private Group group;

    private CircularProgressView    progress;
    private RecyclerView            recycler;
    private AnnouncementAdapter     adapter;
    private NetworkExceptionHandler exceptionHandler;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        exceptionHandler = new NetworkExceptionHandler.DefaultHandler((AppCompatActivity)context) {
            @Override
            public void handleOffline() {
                InfoDialog.newInstance(getString(R.string.error_offline_memberlist_title),
                                       getString(R.string.error_offline_memberlist_text))
                          .show(((AppCompatActivity) context).getSupportFragmentManager(), "");
                Network.Status.setOffline();
            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.group = getArguments().getParcelable(ARG_GROUP);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_announcements, container, false);

        progress = (CircularProgressView) v.findViewById(R.id.progress_view);
        progress.setVisibility(View.VISIBLE);
        recycler = (RecyclerView) v.findViewById(R.id.announcements_recycler);
        final LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(lm);
        setData();
        AppCompatActivity ac = (AppCompatActivity) getActivity();
        if (NavUtils.getParentActivityIntent(ac) != null) {
            ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(group.getName(getContext()));
    }

    private void setData() {
        if (adapter == null) {
            adapter = new AnnouncementAdapter(new LinkedList<Announcement>());
            recycler.setAdapter(adapter);
        }
        progress.setVisibility(View.VISIBLE);
        Groups.getAllAnnouncements(REQUEST_GET_ANNOUNCEMENTS, group.getIdValue(), this);
    }

    @Override
    public void onRequestCompleted(final int id, final Network.Response<String> response) {
        if(response.responseCode == Network.Response.RESPONSE_OK) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (id) {
                        case REQUEST_GET_ANNOUNCEMENTS:
                            try {
                                JSONArray          jsonAnns      = new JSONArray(response.responseData);
                                List<Announcement> announcements = new ArrayList<>();
                                for(int i=0; i<jsonAnns.length(); i++) {
                                    Announcement a = new Announcement(jsonAnns.getJSONObject(i));
                                    announcements.add(a);
                                }
                                adapter.announcements = announcements;
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                exceptionHandler.handleJsonException();
                            }
                            break;
                        default:
                            Log.w("AnnouncementsFragment", "Invalid request code @ onRequestCompleted: " + id);
                    }
                }
            });
        } else {
            Network.Response<String> handled = response.handleErrorCode(exceptionHandler);
            if(handled.responseCode == Network.Response.RESPONSE_OK) onRequestCompleted(id, handled);
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
            Log.e("AnnouncementsFragment", "Unknown Throwable caught", ex);
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.GONE);
            }
        });
    }

    private class AnnouncementHolder extends RecyclerView.ViewHolder {

        private TextView years;
        private TextView date;
        private TextView text;

        private Announcement announcement;

        public AnnouncementHolder(View itemView) {
            super(itemView);

            years = (TextView) itemView.findViewById(R.id.li_announcement_years);
            date = (TextView) itemView.findViewById(R.id.li_announcement_date);
            text = (TextView) itemView.findViewById(R.id.li_announcement_text);
        }

        public void bindAnnouncement(Announcement announcement) {
            years.setText(announcement.getYears());
            date.setText(DateFormat.getDateFormat(getContext()).format(announcement.getDate()));
            text.setText(announcement.getText());

            this.announcement = announcement;
        }
    }

    private class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementHolder> {

        private List<Announcement> announcements = new ArrayList<>();
        public AnnouncementAdapter(List<Announcement> announcements) {
            this.announcements = announcements;
        }

        @Override
        public AnnouncementHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_announcement,
                                               parent,
                                               false);
            return new AnnouncementHolder(view);
        }

        @Override
        public void onBindViewHolder(AnnouncementHolder holder, int position) {
            holder.bindAnnouncement(announcements.get(position));
        }

        @Override
        public int getItemCount() {
            return announcements.size();
        }
    }
}
