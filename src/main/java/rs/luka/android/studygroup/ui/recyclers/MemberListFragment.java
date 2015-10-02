package rs.luka.android.studygroup.ui.recyclers;

import android.app.Activity;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.pkmmte.view.CircularImageView;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.io.Network;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.ui.CursorAdapter;

/**
 * Created by luka on 19.9.15..
 */
public class MemberListFragment extends Fragment {

    private static final String ARG_GROUP = "agroup";

    private Group group;

    private CircularProgressView progress;
    private RecyclerView         recycler;
    private CursorAdapter        adapter;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_member_list, container, false);
        progress = (CircularProgressView) view.findViewById(R.id.progress_view);
        Activity ac = getActivity();
        recycler = (RecyclerView) view.findViewById(R.id.members_recycler);
        final LinearLayoutManager lm = new LinearLayoutManager(ac);
        recycler.setLayoutManager(lm);
        setData(); // TODO: 19.9.15.

        return view;
    }

    private void setData() {
        if (adapter == null) {
            adapter = new UserAdapter(getActivity(), null);
            recycler.setAdapter(adapter);
        }
        // TODO: 19.9.15. DataManager#getUsers
    }

    //@Override
    public Loader<Cursor> onCreateLoader(/*int id, Bundle args*/) {
        progress.setVisibility(View.VISIBLE);
        if (group == null) { throw new AssertionError("wtf"); }
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

    private class UserAdapter extends CursorAdapter<UserHolder> {

        public UserAdapter(Context context, Cursor cursor) {
            super(context, cursor);
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
        public void onBindViewHolder(UserHolder holder, Cursor data) {
            holder.bindUser(((Network.UserCursor) data).getUser());
        }
    }
}
