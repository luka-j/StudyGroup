package rs.luka.android.studygroup;

/**
 * Created by Luka on 7/1/2015.
 */

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


public abstract class SingleFragmentActivity extends AppCompatActivity {

    private Toolbar toolbar;

    protected abstract Fragment createFragment();

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.w("fk this shit", "onActivityResult: activity");
        getSupportFragmentManager().findFragmentById(R.id.fragment_container).onActivityResult(requestCode, resultCode, data);
        Log.w("fk this shit", "onActivityResult: activity exits");
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);
    }*/
}

