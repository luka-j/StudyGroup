package rs.luka.android.studygroup.ui.recyclers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.pkmmte.view.CircularImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.network.Groups;
import rs.luka.android.studygroup.network.Network;
import rs.luka.android.studygroup.network.UserManager;
import rs.luka.android.studygroup.ui.dialogs.ConfirmDialog;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;

/**
 * Created by luka on 19.9.15..
 */
public class MemberListFragment extends Fragment implements Network.NetworkCallbacks<String> {

    private static final String ARG_GROUP = "agroup";
    private static final String TAG       = "MemberListFragment";

    private static final int REQUEST_GET_LIST = 0;
    private static final int REQUEST_GET_PERMISSION = 1;
    protected static final int REQUEST_CHANGE_PERMISSION = 2;

    private Group   group;
    private boolean ownerMode;

    private View root;
    private CircularProgressView progress;
    private RecyclerView         recycler;
    private UserAdapter          adapter;
    private NetworkExceptionHandler exceptionHandler;

    public static MemberListFragment newInstance(Group group) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_GROUP, group);
        MemberListFragment fragment = new MemberListFragment();
        fragment.setArguments(args);
        return fragment;
    }

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.group = getArguments().getParcelable(ARG_GROUP);

        int perm = group.getPermission();
        User.getLoggedInUser().setPermission(perm);
        ownerMode = perm >= Group.PERM_OWNER;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_member_list, container, false);
        UserManager.getMyMembership(REQUEST_GET_PERMISSION, group.getIdValue(), this);

        progress = (CircularProgressView) root.findViewById(R.id.progress_view);
        progress.setVisibility(View.VISIBLE);
        recycler = (RecyclerView) root.findViewById(R.id.members_recycler);
        final LinearLayoutManager lm = new LinearLayoutManager(getActivity());
        recycler.setLayoutManager(lm);
        setData();
        AppCompatActivity ac = (AppCompatActivity) getActivity();
        if (NavUtils.getParentActivityIntent(ac) != null) {
            ac.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        return root;
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
        Groups.getUsers(REQUEST_GET_LIST, group.getIdValue(), this);
    }

    protected void showSpinner() {
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestCompleted(final int id, final Network.Response<String> response) {
        if (response.responseCode == Network.Response.RESPONSE_OK) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (id) {
                        case REQUEST_GET_LIST:
                            try {
                                JSONArray  array = new JSONArray(response.responseData);
                                List<User> list  = new ArrayList<>(array.length());
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject jsonUser = array.getJSONObject(i).getJSONObject("user");
                                    list.add(new User(jsonUser.getLong("id"),
                                                      jsonUser.getString("username"),
                                                      array.getJSONObject(i).getInt("permission")));
                                }
                                Collections.sort(list, new Comparator<User>() {
                                    @Override
                                    public int compare(User lhs, User rhs) {
                                        if(ownerMode) {
                                            if(lhs.getPermission() == Group.PERM_REQUEST_WRITE
                                               && rhs.getPermission() != Group.PERM_REQUEST_WRITE) return -1;
                                            if(lhs.getPermission() != Group.PERM_REQUEST_WRITE
                                               && rhs.getPermission() == Group.PERM_REQUEST_WRITE) return 1;
                                        }
                                        if (lhs.getPermission() > rhs.getPermission()) return -1;
                                        if (lhs.getPermission() < rhs.getPermission()) return 1;
                                        return lhs.getName().compareTo(rhs.getName());
                                    }
                                });
                                adapter.users = list;
                                adapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                exceptionHandler.handleJsonException();
                            }
                            progress.setVisibility(View.GONE);
                            break;
                        case REQUEST_GET_PERMISSION:
                            try {
                                JSONObject me = new JSONObject(response.responseData);
                                if(!ownerMode) {
                                    root.findViewById(R.id.member_list_header).setVisibility(View.GONE);
                                }
                                User.setMyId(me.getJSONObject("user").getLong("id"));
                                break;
                            } catch (JSONException e) {
                                exceptionHandler.handleJsonException();
                            }
                        case REQUEST_CHANGE_PERMISSION:
                            setData();
                            break;
                    }
                }
            });
        } else {
            response.handleErrorCode(exceptionHandler);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress.setVisibility(View.GONE);
                }
            });
        }
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

    private class UserHolder extends RecyclerView.ViewHolder {
        private TextView          nameTextView;
        private TextView          roleTextView;
        private CircularImageView image;

        private CheckBox owner;
        private CheckBox mod;
        private CheckBox member;

        private User user;


        private View.OnClickListener ownerBoxListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((CompoundButton)v).isChecked();
                if(isChecked) {
                    Groups.grantOwner(REQUEST_CHANGE_PERMISSION, group.getIdValue(), user.getId(), MemberListFragment.this);
                } else {
                    Groups.grantMod(REQUEST_CHANGE_PERMISSION, group.getIdValue(), user.getId(), MemberListFragment.this);
                }
                progress.setVisibility(View.VISIBLE);
            }
        };
        private View.OnClickListener modBoxListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((CompoundButton)v).isChecked();
                if(isChecked) {
                    Groups.grantMod(REQUEST_CHANGE_PERMISSION, group.getIdValue(), user.getId(), MemberListFragment.this);
                } else {
                    Groups.grantMember(REQUEST_CHANGE_PERMISSION, group.getIdValue(), user.getId(), MemberListFragment.this);
                }
                progress.setVisibility(View.VISIBLE);
            }
        };
        private View.OnClickListener memberBoxListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = ((CompoundButton)v).isChecked();
                if(isChecked) {
                    Groups.grantMember(REQUEST_CHANGE_PERMISSION, group.getIdValue(), user.getId(), MemberListFragment.this);
                    progress.setVisibility(View.VISIBLE);
                } else {
                    ConfirmDialog.newInstance(R.string.confirm_revoke_write_title,
                                              R.string.confirm_revoke_write_text,
                                              R.string.confirm_revoke_write_positive,
                                              R.string.cancel)
                                 .show(getFragmentManager(), String.valueOf(user.getId()));
                }
            }
        };


        public UserHolder(View itemView) {
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.item_user_name);
            roleTextView = (TextView) itemView.findViewById(R.id.item_user_role);
            image = (CircularImageView) itemView.findViewById(R.id.item_user_image);
            owner = (CheckBox)itemView.findViewById(R.id.item_user_owner_box);
            mod = (CheckBox)itemView.findViewById(R.id.item_user_mod_box);
            member = (CheckBox)itemView.findViewById(R.id.item_user_member_box);
            if(!ownerMode) {
                owner.setVisibility(View.GONE);
                mod.setVisibility(View.GONE);
                member.setVisibility(View.GONE);
            } else {
                owner.setOnClickListener(ownerBoxListener);
                mod.setOnClickListener(modBoxListener);
                member.setOnClickListener(memberBoxListener);
            }
        }

        public void bindUser(User user) {
            this.user = user;
            nameTextView.setText(user.getName());
            roleTextView.setText(user.getRoleDescription(getContext()));
            if (user.hasImage()) { image.setImageBitmap(user.getImage()); } else { image.setImageBitmap(null); }
            if(ownerMode) {
                owner.setChecked(user.getPermission() >= Group.PERM_OWNER);
                mod.setChecked(user.getPermission() >= Group.PERM_MODIFY);
                member.setChecked(user.getPermission() >= Group.PERM_WRITE);
                if(user.getId() == User.getLoggedInUser().getId()) {
                    owner.setEnabled(false);
                    mod.setEnabled(false);
                    member.setEnabled(false);
                } else {
                    owner.setEnabled(true);
                    mod.setEnabled(true);
                    member.setEnabled(true);
                }
            }
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
