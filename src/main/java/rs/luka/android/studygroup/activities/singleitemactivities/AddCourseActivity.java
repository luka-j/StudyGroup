package rs.luka.android.studygroup.activities.singleitemactivities;

import android.support.v4.app.Fragment;

import rs.luka.android.studygroup.activities.SingleFragmentActivity;

/**
 * Created by luka on 13.7.15..
 */
public class AddCourseActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new AddCourseFragment();
    }
}
