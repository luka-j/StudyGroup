package rs.luka.android.studygroup.io;

import android.content.Context;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import rs.luka.android.studygroup.io.database.CourseTable;
import rs.luka.android.studygroup.io.network.Lessons;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.ID;
import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.model.Question;

/**
 * Created by luka on 7.2.16..
 */
public class MediaCleanup {
    private static final ScheduledExecutorService executor      = Executors.newSingleThreadScheduledExecutor();

    private static final Pattern groupPattern  = Pattern.compile("IMG_Group-[0-9]+\\.");
    private static final Pattern coursePattern = Pattern.compile("IMG_Course-[0-9]+\\.");
    private static final Pattern notePattern = Pattern.compile(".*-NIMG_[0-9]+\\.");
    private static final Pattern questionPattern = Pattern.compile(".*-QIMG_[0-9]+\\.");
    private static final Pattern noteThumbPattern = Pattern.compile(".*-nthumb[0-9]+\\.");
    private static final Pattern questionThumbPattern = Pattern.compile(".*-qthumb[0-9]+\\.");
    private static final Pattern noteAudioPattern = Pattern.compile(".*-REC_[0-9]+\\.mp3");

    public static void cleanupGroups(final Group[] groups) {
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                Set<Long> groupIds = new HashSet<>(groups.length);
                for(Group g : groups) groupIds.add(g.getIdValue());
                File[] images = LocalImages.APP_THUMBS_DIR.listFiles();
                for(File image : images) {
                    if(groupPattern.matcher(image.getName()).matches()) {
                        String id = image.getName().split("-")[1].split("\\.")[0];
                        if (!groupIds.contains(Long.parseLong(id)))
                            image.delete();
                    }
                }
            }
        }, 1, TimeUnit.SECONDS);
    }

    public static void cleanupCourses(final Course[] courses) {
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                Set<Long> courseIds = new HashSet<>(courses.length);
                Set<String> names = new HashSet<>(courses.length);
                for(Course g : courses) {courseIds.add(g.getIdValue()); names.add(g.getSubject());}
                File[] images = LocalImages.APP_THUMBS_DIR.listFiles();
                for(File image : images) {
                    String name = image.getName();
                    if(image.isDirectory() && !names.contains(name)) {
                        removeDirectory(image);
                    } else if(coursePattern.matcher(name).matches()) {
                        String id = image.getName().split("-")[1].split("\\.")[0];
                        if (!courseIds.contains(Long.parseLong(id)))
                            image.delete();
                    }
                }
            }
        }, 1, TimeUnit.SECONDS);
    }

    /**
     * Removes all files from current directory, including the directory itself. It won't delete subdirectories
     * and files inside them, and in that case the passed directory won't be deleted (however, all files inside
     * the passed directory will be removed nonetheless)
     * @param directory
     */
    private static boolean removeDirectory(File directory) {
        boolean success = true;
        File[] children = directory.listFiles();
        for(File child : children) if(!child.delete()) success = false;
        if(!directory.delete()) success = false;
        return success;
    }

    public static void cleanupLessons(final Context c, final long courseId, final Lessons.Lesson[] lessons) {
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                Course course = new CourseTable(c).queryCourse(new ID(0, courseId));
                Set<String> names = new HashSet<>(lessons.length);
                for(Lessons.Lesson l : lessons)
                    names.add(l.name);
                File[] images = new File(LocalImages.APP_IMAGE_DIR, course.getSubject()).listFiles();
                cleanupLessons(names, images);
                File[] thumbs = new File(LocalImages.APP_THUMBS_DIR, course.getSubject()).listFiles();
                cleanupLessons(names, thumbs);
                File[] recordings = new File(LocalAudio.APP_AUDIO_DIR, course.getSubject()).listFiles();
                cleanupLessons(names, recordings);
            }
        }, 2, TimeUnit.SECONDS);
    }

    private static void cleanupLessons(final Set<String> lessons, final File[] files) {
        for(File f : files)
            if(!lessons.contains(f.getName().split("-", 2)[0]))
                f.delete();
    }

    public static void cleanupNotes(final Context c, final long courseId, final Note[] notes) {
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                Set<Long> noteIds = new HashSet<>(notes.length);
                for(Note n : notes) noteIds.add(n.getIdValue());
                Course course = new CourseTable(c).queryCourse(new ID(0, courseId));
                File[] images = new File(LocalImages.APP_IMAGE_DIR, course.getSubject()).listFiles();
                cleanupItems(images, notePattern, noteIds);
                File[] thumbs = new File(LocalImages.APP_THUMBS_DIR, course.getSubject()).listFiles();
                cleanupItems(thumbs, noteThumbPattern, noteIds);
                File[] audio = new File(LocalAudio.APP_AUDIO_DIR, course.getSubject()).listFiles();
                cleanupItems(audio, noteAudioPattern, noteIds);
            }
        }, 4, TimeUnit.SECONDS);
    }

    public static void cleanupQuestions(final Context c, final long courseId, final Question[] questions) {
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                Set<Long> questionIds = new HashSet<>(questions.length);
                for(Question n : questions) questionIds.add(n.getIdValue());
                Course course = new CourseTable(c).queryCourse(new ID(0, courseId));
                File[] images = new File(LocalImages.APP_IMAGE_DIR, course.getSubject()).listFiles();
                cleanupItems(images, questionPattern, questionIds);
                File[] thumbs = new File(LocalImages.APP_THUMBS_DIR, course.getSubject()).listFiles();
                cleanupItems(thumbs, questionThumbPattern, questionIds);
            }
        }, 4, TimeUnit.SECONDS);
    }

    private static void cleanupItems(final File[] files, Pattern pattern, Set<Long> ids) {
        for(File image : files) {
            String name = image.getName();
            if(pattern.matcher(name).matches()) {
                String id = name.split("_")[1].split("\\.")[0];
                if(!ids.contains(Long.parseLong(id)))
                    image.delete();
            }
        }
    }
}
