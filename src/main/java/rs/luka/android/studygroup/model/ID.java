package rs.luka.android.studygroup.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;

/**
 * Created by luka on 24.7.15..
 */
public class ID implements Parcelable {
    public static final Parcelable.Creator<ID> CREATOR
            = new Parcelable.Creator<ID>() {
        public ID createFromParcel(Parcel in) {
            return new ID(in);
        }

        public ID[] newArray(int size) {
            return new ID[size];
        }
    };
    protected final long  groupId;
    protected final short random;
    protected final short courseId;
    protected final int   itemId;

    public ID(ID groupId, short courseId) {
        this(groupId.groupId, courseId, groupId.random);
    }

    public ID(ID courseId, int itemId) {
        this(courseId.groupId, courseId.courseId, itemId, courseId.random);
    }

    public ID(long groupId, short random) {
        this(groupId, (short) 0, random);
    }

    public ID(long groupId, short courseId, short random) {
        this(groupId, courseId, 0, random);
    }

    public ID(long groupId, short courseId, int itemId, short random) {
        this.groupId = groupId;
        this.courseId = courseId;
        this.itemId = itemId;
        this.random = random;
    }

    private ID(Parcel in) {
        groupId = in.readLong();
        random = (short) in.readInt();
        courseId = (short) in.readInt();
        itemId = in.readInt();
    }

    public static ID generateGroupId() {
        return new ID(System.currentTimeMillis(), (short) new Random().nextInt(65536));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(groupId);
        dest.writeInt(random);
        dest.writeInt(courseId);
        dest.writeInt(itemId);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ID) {
            ID id = (ID) o;
            return id.groupId == groupId && id.random == random && id.courseId == courseId
                   && id.itemId == itemId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) (random + courseId * 37 + itemId / 3 + groupId / 53);
    }
}
