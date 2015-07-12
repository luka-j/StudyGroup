package rs.luka.android.studygroup.model;

import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by luka on 2.7.15..
 */
public class Note implements Serializable {
    private final UUID id;
    private final String text;
    private final @Nullable String imageUrl;

    public Note(UUID id, String text, @Nullable String imageUrl) {
        this.id = id;
        this.text = text;
        this.imageUrl = imageUrl;
    }

    public String getText() {
        return text;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    public UUID getId() {
        return id;
    }
}
