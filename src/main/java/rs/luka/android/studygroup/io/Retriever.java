package rs.luka.android.studygroup.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import rs.luka.android.studygroup.R;
import rs.luka.android.studygroup.model.Course;
import rs.luka.android.studygroup.model.Exam;
import rs.luka.android.studygroup.model.Group;
import rs.luka.android.studygroup.model.ID;
import rs.luka.android.studygroup.model.Note;
import rs.luka.android.studygroup.model.Question;
import rs.luka.android.studygroup.ui.recyclers.ExamQuestionsActivity;

/**
 * Created by Luka on 7/2/2015.
 */
public class Retriever {

    private static List<Course>            courses;
    private static List<Exam>              exams;
    private static Map<ID, List<Note>>     notes;
    private static Map<ID, List<Question>> questions;
    private static Map<ID, List<String>>   lessons;

    public static List<Group> getGroups(UUID userToken) {
        List<Group> groups = new ArrayList<>();
        groups.add(new Group(ID.generateGroupId(), "MG", "Beograd"));
        //groups.add(new Group(UUID.randomUUID(), "MG - OS", "Beograd", null));
        return groups;
    }

    public static List<Course> getCourses(ID id) {
        if (courses == null) {
            fetchCourses(id);
        }
        return courses;
    }

    private static void fetchCourses(ID id) {
        courses = new ArrayList<>();
        courses.add(new Course(new ID(id, (short) 0), "Fizika", "Zoran Nikolic", 1));
        courses.add(new Course(new ID(id, (short) 1), "Geometrija", "Bojana Matic", 1));
        courses.add(new Course(new ID(id, (short) 2), "Srpski jezik", "Mirjana Mićić", 1));
        courses.add(new Course(new ID(id, (short) 3),
                               "Informatika i racunarstvo",
                               "Zeljko Lezaja",
                               2));
        courses.add(new Course(new ID(id, (short) 4), "Istorija", "Aleksandar Glavnik", 2));
    }

    public static List<Exam> getExams(ID id) {
        if(exams == null) {
            fetchExams(id);
        }
        return exams;
    }

    private static void fetchExams(ID id) {
        exams = new ArrayList<>();
        exams.add(new Exam(new ID(id, 20), "1e", "Stehiometrija", "Kontrolni", new Date(114, 10, 26)));
        exams.add(new Exam(new ID(id, 21), "1e", "if, nizovi, petlje, rekurzija, bitovni, matrice", "Usmeno", new Date(114, 10, 26)));
    }

    public static Course getCourseFor(ID id) {
        ID courseId = id.getCourseId();
        return new Course(courseId, "Hemija", "Ivana Vukovic", 2);
    }

    public static int getNumberOfLessons(ID courseId) {

        return 2;
    }

    public static List<String> getLessons(ID id) {
        if (lessons == null) { lessons = new HashMap<>(); }
        if (!lessons.containsKey(id)) { fetchLessons(id); }
        return lessons.get(id);
    }

    private static void fetchLessons(ID id) {
        List<String> l = new ArrayList<>();
        l.add("Kinematika");
        l.add("Dinamika");
        l.add("Statika");
        l.add("Zakoni odrzanja");
        lessons.put(id, l);
    }

    public static int getNumberOfNotes(ID courseId, String lesson) {

        return 18;
    }

    public static List<Note> getNotes(ID courseId, String lesson) {
        if (notes == null) { notes = new HashMap<>(); }
        if (!notes.containsKey(courseId)) {
            fetchNotes(courseId);
        }
        return notes.get(courseId);
    }

    @Nullable
    public static List<Note> getExistingNotes(ID courseId, String lesson) {
        return notes.get(courseId);
    }

    private static void fetchNotes(ID id) {
        List<Note> n = new ArrayList<>();
        n.add(new Note(new ID(id, 0), "Zemlja je okrugla"));
        n.add(new Note(new ID(id, 1), "Imam previse vremena"));
        n.add(new Note(new ID(id, 2), "Ovo nije funkcionalno"));
        n.add(new Note(new ID(id, 3), "Samo popunjava prostor"));
        n.add(new Note(new ID(id, 4), "Primer u dva reda, sa nekim dugackim tekstom"));
        n.add(new Note(new ID(id, 5), "Ako ima vise od dva reda, dodace tri tacke na kraju. " +
                                      "Ovaj prostor nije namenjen za pisanje knjiga"));
        n.add(new Note(new ID(id, 6), "Kada se pritisne, izadje ceo sadrzaj + slika ako postoji"));
        n.add(new Note(new ID(id, 7), "Jos neki red, kako bi se pojavio scroll sa strane"));
        n.add(new Note(new ID(id, 8), "Sakriva Toolbar kad se scrolluje"));
        n.add(new Note(new ID(id, 9), "Jos"));
        n.add(new Note(new ID(id, 10), "par"));
        n.add(new Note(new ID(id, 11), "redova"));
        notes.put(id, n);
    }

    public static int getNumberOfQuestions(ID courseId, String lesson) {

        return 5;
    }

    public static List<Question> getQuestions(ID courseId, String lesson) {
        if (questions == null) { questions = new HashMap<>(); }
        if (!questions.containsKey(courseId)) {
            if(lesson.startsWith(ExamQuestionsActivity.EXAM_LESSON_PREFIX)) getExamQuestions(courseId, lesson);
            else fetchQuestions(courseId);
        }
        return questions.get(courseId);
    }

    public static void getExamQuestions(ID courseId, String lesson) {
        List<Question> q = new ArrayList<>();
        q.add(new Question(new ID(courseId, 25),
                                   "Ovo je kao pitanje s nekog kontrolnog",
                                   "Vazi"));
        q.add(new Question(new ID(courseId, 26), "Ok?", "k"));
        questions.put(courseId, q);
    }

    @Nullable
    public static List<Question> getExistingQuestions(ID courseId, String lesson) {
        return questions.get(courseId);
    }

    public static void fetchQuestions(ID id) {
        List<Question> q = new ArrayList<>();
        q.add(new Question(new ID(id, 0), "Koliko je sati", "Otkud ja znam"));
        q.add(new Question(new ID(id, 1), "Koji je dan?", "Na raspustu sam"));
        q.add(new Question(new ID(id, 2),
                           "Zasto je aplikacija na engleskom?",
                           "nmp, bilo mi lakse kad sam pisao, prevescu posle"));
        questions.put(id, q);
    }

    public static Bitmap getNoteImage(ID id) {

        return null;
    }

    public static Bitmap getQuestionImage(ID id) {

        return null;
    }

    public static Bitmap getCourseImage(ID id) {

        return null;
    }

    public static Bitmap getGroupImage(ID id) {

        return null;
    }

    public static String getNoteHistory(ID id, Context c) {
        return c.getString(R.string.note_history, "Pera", "20. 6. 2014. 16:32");
    }

    public static String getQuestionHistory(ID id, Context c) {
        return c.getString(R.string.note_history, "Pera", "20. 6. 2014. 16:32");
    }
}
