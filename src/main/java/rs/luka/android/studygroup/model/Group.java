package rs.luka.android.studygroup.model;

import java.util.UUID;

/**
 * Created by Luka on 7/1/2015.
 */
public class Group {
    private final UUID id;
    private final String name;
    private final String image;

    public Group(UUID id, String name, String image) {
        this.id = id;
        this.name = name;
        this.image = image;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }
}
