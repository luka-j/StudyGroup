// TODO: 11.9.15. Finish EXTRA_ Strings cleanup (od LessonActivity pa nadalje)
// TODO: 20.9.15. clean history mess (u Cursoru je type, u History je prev; pogledaj strings). Vidi kako za PastEvents
package rs.luka.android.studygroup.ui.recyclers;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import rs.luka.android.studygroup.io.DataManager;
import rs.luka.android.studygroup.io.Database;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.User;
import rs.luka.android.studygroup.ui.SingleFragmentActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.AddGroupActivity;
import rs.luka.android.studygroup.ui.singleitemactivities.LoginActivity;

/*
 * Struktura:
 * RootActivity (GroupListFragment), fragment se ne prikazuje ako postoji samo jedna grupa, postoje Search i Create (AddGroupActivity), ne postoji hiding
 * (0) LoginActivity, uvek se vraca u root
 * (1) GroupActivity (GroupFragment, courseList), postoji Add (AddCourseActivity)
 * (2) ScheduleActivity (ScheduleFragment), postoji Add (AddExamActivity)
 * (3) MemberListActivity (MemberListFragment), todo
 * (1) * CourseActivity (CourseFragment, lessonList), preskače se ako nema ili postoji samo jedna lekcija, ne postoji History
 * (1) * * LessonActivity (LessonPager: NoteListFragment (1), QuestionListFragment (2)), postoji Add za svaki fragment
 * (1) * * (1) NotePagerActivity (NoteFragment), postoji FullscreenImage, Add: AddNoteActivity
 * (1) * * (2) QuestionPagerActivity (QuestionFragment), postoji FullScreenImage, Add: AddQuestionActivity
 * (2) (1) AddCourseActivity, sastoji se iz dva koraka (drugi je 'klasicni' unos podataka)
 * (2) (1) * SelectCourseActivity (GroupFragment), odabir predmeta na koji se kontrolni odnosi
 * (2) (2) ExamQuestionsActivity (QuestionListFragment), postoji Add (AddQuestionActivity)
 *
 * Svuda postoji history i hiding (uklj. "show all"), osim ako nije drugacije naznaceno
 */

/**
 * Created by luka on 17.7.15.
 */
public class RootActivity extends SingleFragmentActivity implements GroupListFragment.Callbacks {
    private static final String TAG = "studygroup.RootActivity";

    @Override
    protected Fragment createFragment() {
        return new GroupListFragment();
    }

    @Override
    protected boolean shouldCreateFragment() {
        if (getIntent().getBooleanExtra(GroupActivity.EXTRA_SHOW_LIST, false)) {
            Log.i(TAG, "showing list, extra supplied");
            return true;
        }
        //Log.i(TAG, "called from diff activity: " + getIntent().getPackage());
        if (!User.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            return false;
        } else {
            if (DataManager.getGroupCount(this) == 1) {
                Intent i = new Intent(this, GroupActivity.class);
                Database.GroupCursor c = Database.getInstance(this).queryGroups();
                c.moveToNext();
                i.putExtra(GroupActivity.EXTRA_GROUP, c.getGroup());
                Log.i(TAG, "one group, starting it");
                startActivity(i);
                return false;
            } else {
                Log.i(TAG, "multiple groups, showing list");
                return true;
            }
        }
    }

    @Override
    public void onGroupSelected(Group group) {
        Intent i = new Intent(this, GroupActivity.class);
        i.putExtra(GroupActivity.EXTRA_GROUP, group);
        startActivity(i);
    }

    @Override
    public void onEditGroup(Group group, int requestCode) {
        Intent i = new Intent(this, AddGroupActivity.class);
        i.putExtra(GroupActivity.EXTRA_GROUP, group);
        startActivityForResult(i, requestCode);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
