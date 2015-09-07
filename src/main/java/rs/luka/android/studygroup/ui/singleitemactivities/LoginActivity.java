package rs.luka.android.studygroup.ui.singleitemactivities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.ui.recyclers.RootActivity;

/**
 * Created by luka on 25.7.15..
 */
public class LoginActivity extends AppCompatActivity {

    CardView login;
    EditText username;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = (CardView) findViewById(R.id.button_login);
        username = (EditText) findViewById(R.id.login_username_input);
        password = (EditText) findViewById(R.id.login_password_input);

        final Context This = this;
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
                //finish();
                startActivity(new Intent(This, RootActivity.class));
            }
        });
    }

    private void login() {
        User.login(username.getText().toString(), password.getText().toString());
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
