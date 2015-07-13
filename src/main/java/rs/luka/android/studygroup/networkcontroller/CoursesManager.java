package rs.luka.android.studygroup.networkcontroller;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import rs.luka.android.studygroup.R;
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

    @Nullable
    public static List<Note> getExistingNotes(UUID courseId, String lesson) {
        return notes.get(courseId);
    }

    private static void fetchNotes(UUID id) {
        List<Note> n = new ArrayList<>();
        n.add(new Note(id, "Zemlja je okrugla", null));
        n.add(new Note(id, "Imam previse vremena", null));
        n.add(new Note(id, "Ovo nije funkcionalno", null));
        n.add(new Note(id, "Samo popunjava prostor", null));
        n.add(new Note(id, "Primer u dva reda, sa nekim dugackim tekstom", null));
        n.add(new Note(id, "Ako ima vise od dva reda, dodace tri tacke na kraju. " +
                "Ovaj prostor nije namenjen za pisanje knjiga", null));
        n.add(new Note(id, "Kada se pritisne, izadje ceo sadrzaj + slika ako postoji", null));
        n.add(new Note(id, "Jos neki red, kako bi se pojavio scroll sa strane", null));
        n.add(new Note(id, "Sakriva Toolbar kad se scrolluje", null));
        n.add(new Note(id, "Jos", null));
        n.add(new Note(id, "par", null));
        n.add(new Note(id, "redova", null));
        notes.put(id, n);
    }

    public static List<Question> getQuestions(UUID courseId, String lesson) {
        if(questions==null)
            questions = new HashMap<>();
        if(!questions.containsKey(courseId))
            fetchQuestions(courseId);
        return questions.get(courseId);
    }

    @Nullable
    public static List<Question> getExistingQuestions(UUID courseId, String lesson) {
        return questions.get(courseId);
    }

    public static void fetchQuestions(UUID id) {
        List<Question> q = new ArrayList<>();
        q.add(new Question(id, "Koliko je sati", "Otkud ja znam", null));
        q.add(new Question(id, "Koji je dan?", "Na raspustu sam", null));
        q.add(new Question(id, "Zasto je aplikacija na engleskom?", "nmp, bilo mi lakse kad sam pisao, prevescu posle", null));
        questions.put(id, q);
    }

    public static Bitmap getNoteImage(UUID id) {

        return null;
    }

    public static Bitmap getQuestionImage(UUID id) {

        return null;
    }

    public static String getNoteHistory(UUID id, Context c) {
        return c.getString(R.string.note_history, "Pera", "20. 6. 2014. 16:32");
    }

    public static String getQuestionHistory(UUID id, Context c) {
        return c.getString(R.string.note_history, "Pera", "20. 6. 2014. 16:32");
    }
}
