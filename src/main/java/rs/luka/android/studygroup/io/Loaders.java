package rs.luka.android.studygroup.io;

import android.content.Context;
import android.database.Cursor;

import rs.luka.android.studygroup.io.database.CourseTable;
import rs.luka.android.studygroup.io.database.ExamTable;
import rs.luka.android.studygroup.io.database.GroupTable;
import rs.luka.android.studygroup.io.database.LessonTable;
import rs.luka.android.studygroup.io.database.NoteTable;
import rs.luka.android.studygroup.io.database.QuestionTable;
import rs.luka.android.studygroup.model.ID;

/**
 * Created by luka on 29.8.15..
 */
public class Loaders {
    public static class GroupLoader extends SQLiteCursorLoader {

        public GroupLoader(Context context) {
            super(context);
        }

        @Override
        protected GroupTable.GroupCursor loadCursor() {
            return (new GroupTable(getContext())).queryGroups();
        }
    }

    public static class CourseLoader extends SQLiteCursorLoader {

        private final ID groupId;

        public CourseLoader(Context context, ID groupId) {
            super(context);
            this.groupId = groupId;
        }

        @Override
        protected Cursor loadCursor() {
            return new CourseTable(getContext()).queryCourses(groupId);
        }
    }

    public static class LessonLoader extends SQLiteCursorLoader {
        private final ID courseId;

        public LessonLoader(Context context, ID courseId) {
            super(context);
            this.courseId = courseId;
        }

        @Override
        protected Cursor loadCursor() {
            return new LessonTable(getContext()).queryLessons(courseId);
        }
    }

    public static class ItemLoader extends SQLiteCursorLoader {
        public static final int LOAD_NOTES     = 0;
        public static final int LOAD_QUESTIONS = 1;
        public static final int LOAD_EXAMS     = 2;

        private final ID     parentId;
        private final String lesson;
        private final int    item;

        public ItemLoader(Context context, ID parentId, String lesson, int item) {
            super(context);
            this.parentId = parentId;
            this.lesson = lesson;
            this.item = item;
        }

        @Override
        protected Cursor loadCursor() {
            switch (item) {
                case LOAD_NOTES:
                    return new NoteTable(getContext()).queryNotes(parentId, lesson);
                case LOAD_QUESTIONS:
                    return new QuestionTable(getContext()).queryQuestions(parentId, lesson);
                case LOAD_EXAMS:
                    return new ExamTable(getContext()).queryExams(parentId);
                default:
                    throw new IllegalArgumentException("Illegal item " + item);
            }
        }
    }
}
