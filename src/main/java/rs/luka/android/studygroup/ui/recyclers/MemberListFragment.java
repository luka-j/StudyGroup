package rs.luka.android.studygroup.ui.recyclers;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.util.LinkedList;
import java.util.List;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.io.network.Groups;
import rs.luka.android.studygroup.io.network.Network;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.ui.dialogs.ConfirmDialog;
import rs.luka.android.studygroup.ui.dialogs.InfoDialog;

/**
 * Created by luka on 19.9.15..
 */
public class MemberListFragment extends Fragment implements Network.NetworkCallbacks<String> {

    private static final String ARG_GROUP = "agroup";
    private static final String TAG       = "MemberListFragment";

    private static final int REQUEST_GET_LIST = 0;
    protected static final int REQUEST_CHANGE_PERMISSION = 2;

    private Group   group;
    private boolean ownerMode;
    private boolean modMode;

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
                        .show(((AppCompatActivity) context).getFragmentManager(), "");
                Network.Status.setOffline();
            }
        };
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(modMode) inflater.inflate(R.menu.menu_help_button, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.group = getArguments().getParcelable(ARG_GROUP);

        int perm = group.getPermission();
        User.getLoggedInUser().setPermission(perm);
        ownerMode = perm >= Group.PERM_OWNER;
        modMode = perm >= Group.PERM_MODIFY;
        if(modMode) setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_member_list, container, false);

        if(modMode) {
            View header = root.findViewById(R.id.member_list_header);
            header.setVisibility(View.VISIBLE);
        }
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(group.getName(getActivity()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_item_show_help) {
            InfoDialog.newInstance(getString(R.string.permissions_help_title),
                                   getString(R.string.permissions_help_text)).show(getFragmentManager(), "");
            return true;
        } else {
            return false;
        }
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
            getActivity().runOnUiThread(() -> {
                switch (id) {
                    case REQUEST_GET_LIST:
                        try {
                            JSONArray  array = new JSONArray(response.responseData);
                            List<User> list  = new ArrayList<>(array.length());
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject jsonUser = array.getJSONObject(i).getJSONObject("user");
                                list.add(new User(jsonUser.getLong("id"),
                                                  jsonUser.getString("username"),
                                                  array.getJSONObject(i).getInt("permission"),
                                                  jsonUser.getBoolean("hasImage")));
                            }
                            Collections.sort(list, (lhs, rhs) -> {
                                if(modMode) {
                                    if(lhs.getPermission() == Group.PERM_INVITED
                                            && rhs.getPermission() != Group.PERM_INVITED) return -1;
                                    if(lhs.getPermission() != Group.PERM_INVITED
                                       && rhs.getPermission() == Group.PERM_INVITED) return 1;
                                    if(lhs.getPermission() == Group.PERM_REQUEST_WRITE
                                       && rhs.getPermission() != Group.PERM_REQUEST_WRITE) return -1;
                                    if(lhs.getPermission() != Group.PERM_REQUEST_WRITE
                                       && rhs.getPermission() == Group.PERM_REQUEST_WRITE) return 1;
                                }
                                if (lhs.getPermission() > rhs.getPermission()) return -1;
                                if (lhs.getPermission() < rhs.getPermission()) return 1;
                                return lhs.getName().compareTo(rhs.getName());
                            });
                            adapter.users = list;
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            exceptionHandler.handleJsonException();
                        }
                        progress.setVisibility(View.GONE);
                        break;
                    case REQUEST_CHANGE_PERMISSION:
                        setData();
                        break;
                }
            });
        } else {
            Network.Response<String> handled = response.handleErrorCode(exceptionHandler);
            if(handled.responseCode == Network.Response.RESPONSE_OK) onRequestCompleted(id, handled);
            getActivity().runOnUiThread(() -> progress.setVisibility(View.GONE));
        }
    }

    @Override
    public void onExceptionThrown(int id, Throwable ex) {
        if(ex instanceof Error)
            throw new Error(ex);
        if(ex instanceof IOException)
            exceptionHandler.handleIOException((IOException)ex);
        else {
            getActivity().runOnUiThread(() -> InfoDialog.newInstance(getString(R.string.error_unknown_ex_title),
                                                             getString(R.string.error_unknown_ex_text))
                                                .show(getFragmentManager(), ""));
            Log.e(TAG, "Unknown Throwable caught", ex);
        }
        getActivity().runOnUiThread(() -> progress.setVisibility(View.GONE));
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
                                 .show(((AppCompatActivity)getActivity()).getSupportFragmentManager(), String.valueOf(user.getId()));
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
            if(!modMode) {
                owner.setVisibility(View.GONE);
                mod.setVisibility(View.GONE);
                member.setVisibility(View.GONE);
            } else {
                if(ownerMode) {
                    owner.setOnClickListener(ownerBoxListener);
                    mod.setOnClickListener(modBoxListener);
                }
                member.setOnClickListener(memberBoxListener);
            }
        }

        public void bindUser(User user) {
            this.user = user;
            nameTextView.setText(user.getName());
            roleTextView.setText(user.getRoleDescription(getActivity()));
            if (user.hasImage()) {
                user.getImage(getActivity(), image.getWidth(), exceptionHandler, image);
            } else {
                image.setImageDrawable(getResources().getDrawable(R.drawable.default_user));
            }
            if(modMode) {
                owner.setChecked(user.getPermission() >= Group.PERM_OWNER);
                mod.setChecked(user.getPermission() >= Group.PERM_MODIFY);
                member.setChecked(user.getPermission() >= Group.PERM_WRITE);
                member.setEnabled(true);
                if(ownerMode) {
                    owner.setEnabled(true);
                    mod.setEnabled(true);
                } else {
                    owner.setEnabled(false);
                    mod.setEnabled(false);
                }
                if(user.getId() == User.getLoggedInUser().getId()) {
                    owner.setEnabled(false);
                    mod.setEnabled(false);
                    member.setEnabled(false);
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
