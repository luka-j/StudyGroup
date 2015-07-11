package rs.luka.android.studygroup.networkcontroller;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.model.Question;

/**
 * Created by Luka on 7/2/2015.
 */
public class CoursesManager {

    private static List<Course> courses;
    private static Map<UUID, List<Note>> notes;
    private static Map<UUID, List<Question>> questions;
    private static Map<UUID, List<String>> lessons;

    public static List<Course> getCourses() {
        if(courses==null) {
            fetchCourses();
        }
        return courses;
    }

    private static void fetchCourses() {
        courses = new ArrayList<>();
        courses.add(new Course(UUID.randomUUID(), "Fizika", "Zoran Nikolic", 1, null));
        courses.add(new Course(UUID.randomUUID(), "Geometrija", "Bojana Matic", 1, null));
        courses.add(new Course(UUID.randomUUID(), "Informatika i racunarstvo", "Zeljko Lezaja", 2, null));
    }

    public static List<String> getLessons(UUID id) {
        if(lessons == null)
            lessons = new HashMap<>();
        if(!lessons.containsKey(id))
            fetchLessons(id);
        return lessons.get(id);
    }

    private static void fetchLessons(UUID id) {
        List<String> l = new ArrayList<>();
        l.add("Kinematika");
        l.add("Dinamika");
        l.add("Statika");
        l.add("Zakoni odrzanja");
        lessons.put(id, l);
    }

    public static List<Note> getNotes(UUID courseId, String lesson) {
        if(notes==null)
            notes=new HashMap<>();
        if(!notes.containsKey(courseId)) {
            fetchNotes(courseId);
        }
        return notes.get(courseId);
    }

    private static void fetchNotes(UUID id) {
        List<Note> n = new ArrayList<>();
        n.add(new Note(id, "Zemlja je okrugla", null));
        n.add(new Note(id, "Fizika je OK", null));
        n.add(new Note(id, "Geometrija je beskorisna.", null));
        notes.put(id, n);
    }

    public static List<Question> getQuestions(UUID courseId, String lesson) {
        if(questions==null)
            questions = new HashMap<>();
        if(!questions.containsKey(courseId))
            fetchQuestions(courseId);
        return questions.get(courseId);
    }

    public static void fetchQuestions(UUID id) {
        List<Question> q = new ArrayList<>();
        q.add(new Question("Koliko je sati", "Otkud ja znam", null));
        q.add(new Question("Da li ces ikada koristiti geometriju u zivotu", "Ne", null));
        questions.put(id, q);
    }

    public static Bitmap getNoteImage(UUID id) {

        return null;
    }
}
