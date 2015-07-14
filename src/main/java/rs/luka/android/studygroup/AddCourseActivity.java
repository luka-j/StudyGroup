package rs.luka.android.studygroup;

import android.support.v4.app.Fragment;

/**
 * Created by luka on 13.7.15..
 */
public class AddCourseActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new AddCourseFragment();
    }
}
