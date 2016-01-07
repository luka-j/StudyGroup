package rs.luka.android.studygroup.ui.recyclers;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.pkmmte.view.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.Network;
import rs.luka.android.studygroup.network.AdditionalCursors;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.network.Groups;
import rs.luka.android.studygroup.ui.CursorAdapter;

/**
 * Created by luka on 19.9.15..
 */
public class MemberListFragment extends Fragment implements Network.NetworkCallbacks {

    private static final String ARG_GROUP = "agroup";

    private Group group;

    private CircularProgressView progress;
    private RecyclerView         recycler;
    private UserAdapter          adapter;

    public static MemberListFragment newInstance(Group group) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_GROUP, group);
        MemberListFragment fragment = new MemberListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.group = getArguments().getParcelable(ARG_GROUP);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_list, container, false);
        progress = (CircularProgressView) view.findViewById(R.id.progress_view);
        AppCompatActivity ac = (AppCompatActivity) getActivity();
        if (NavUtils.getParentActivityIntent(ac) != null) {
            ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recycler = (RecyclerView) view.findViewById(R.id.members_recycler);
        final LinearLayoutManager lm = new LinearLayoutManager(ac);
        recycler.setLayoutManager(lm);
        setData(); // TODO: 19.9.15.

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(group.getName());
    }

    private void setData() {
        if (adapter == null) {
            adapter = new UserAdapter(new LinkedList<User>());
            recycler.setAdapter(adapter);
        }
        progress.setVisibility(View.VISIBLE);
        Groups.getUsers(0, group.getIdValue(), this);
    }

    @Override
    public void onRequestCompleted(int id, final Network.Response response) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NetworkExceptionHandler exceptionHandler = new NetworkExceptionHandler.DefaultHandler((AppCompatActivity)getActivity());
                if(response.responseCode == Network.Response.RESPONSE_OK) {
                    try {
                        JSONArray array = new JSONArray(response.responseMessage);
                        List<User> list = new ArrayList<>(array.length());
                        for(int i=0; i<array.length(); i++) {
                            JSONObject jsonUser = array.getJSONObject(i).getJSONObject("user");
                            list.add(new User(jsonUser.getLong("id"), jsonUser.getString("username")));
                        }
                        adapter.users = list;
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        exceptionHandler.handleJsonException();
                    }
                } else {
                    response.handleException(exceptionHandler);
                }
                progress.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onExceptionThrown(int id, Throwable ex) {
        //todo
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.GONE);
            }
        });
    }

    private class UserHolder extends RecyclerView.ViewHolder {
        private TextView          nameTextView;
        private CircularImageView image;

        private User user;

        public UserHolder(View itemView) {
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.item_user_name);
            image = (CircularImageView) itemView.findViewById(R.id.item_user_image);
        }

        public void bindUser(User user) {
            this.user = user;
            nameTextView.setText(user.getName());
            if (user.hasImage()) { image.setImageBitmap(user.getImage()); } else { image.setImageBitmap(null); }
        }
    }

    private class UserAdapter extends RecyclerView.Adapter<UserHolder> {

        private List<User> users = new ArrayList<>();
        public UserAdapter(List<User> users) {
            this.users = users;
        }

        @Override
        public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_user,
                                               parent,
                                               false);
            return new UserHolder(view);
        }

        @Override
        public void onBindViewHolder(UserHolder holder, int position) {
            holder.bindUser(users.get(position));
        }

        @Override
        public int getItemCount() {
            return users.size();
        }
    }
}
