package rs.luka.android.studygroup.ui.singleitemactivities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.exceptions.NetworkExceptionHandler;
import rs.luka.android.studygroup.model.User;

/**
 * Created by luka on 5.2.16..
 */
public class UserInfoActivity extends AppCompatActivity {

    private Toolbar                 toolbar;
    private ImageView               avatar;
    private TextView                username;
    private TextView                email;
    private NetworkExceptionHandler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        handler = new NetworkExceptionHandler.DefaultHandler(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        avatar = (ImageView) findViewById(R.id.user_info_image);
        username = (TextView) findViewById(R.id.user_info_username);
        email = (TextView) findViewById(R.id.user_info_email);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        User me = User.getLoggedInUser();
        if(me.hasImage())
            me.getImage(this, avatar.getWidth(), handler, avatar);
        else
            avatar.setImageDrawable(getResources().getDrawable(R.drawable.default_user));
        username.setText(me.getName());
        email.setText(User.getMyEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.edit_user_info:
                startActivity(new Intent(this, EditUserInfoActivity.class)); //todo forResult ... onResult: refresh
                                                                             //todo ILI onNewIntent (ovo je singletop)
        }
        return super.onOptionsItemSelected(item);
    }
}
