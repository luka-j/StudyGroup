package rs.luka.android.studygroup.model;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.UUID;

import rs.luka.android.studygroup.R;

/**
 * Created by Luka on 7/1/2015.
 */
public class Course {

    private static final @DrawableRes int[] defaultImages = {R.drawable.placeholder};

    private final UUID id;
    private final String subject;
    private final String teacher;
    private final @Nullable Integer year;
    private final @Nullable @DrawableRes Integer image;

    public Course(UUID id, String subject, String teacher, @Nullable Integer year, @Nullable @DrawableRes Integer image) {
        this.id = id;
        this.subject = subject;
        this.teacher = teacher;
        this.year = year;
        this.image = image;
    }

    public UUID getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getTeacher() {
        return teacher;
    }

    @Nullable
    public Integer getYear() {
        return year;
    }

    @NonNull
    public @DrawableRes Integer getImage() {
        if(image != null)
            return image;
        if(year == null || year >= defaultImages.length)
            return defaultImages[0];
        return defaultImages[year];
    }

    @Override
    public String toString() {
        return subject + " by " + teacher + ", year " + String.valueOf(year);
    }
}
