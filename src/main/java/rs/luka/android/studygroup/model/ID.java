package rs.luka.android.studygroup.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by luka on 24.7.15..
 */
public class ID implements Parcelable, Comparable<ID> {
    public static final  String TAG           = "studygroup.ID";
    public static final Parcelable.Creator<ID> CREATOR
            = new Parcelable.Creator<ID>() {
        public ID createFromParcel(Parcel in) {
            return new ID(in);
        }

        public ID[] newArray(int size) {
            return new ID[size];
        }
    };
    private static final long   GROUP_ID_MASK = ~(1L << 4 * 8);
    private long  groupId;
    private short salt;
    private short courseId;
    private int   itemId;

    public ID(ID groupId, short courseId) {
        this(groupId.groupId, courseId, groupId.salt);
    }

    private ID(Parcel in) {
        groupId = in.readLong();
        salt = (short) in.readInt();
        courseId = (short) in.readInt();
        itemId = in.readInt();
    }

    public ID(ID courseId, int itemId) {
        this(courseId.groupId, courseId.courseId, itemId, courseId.salt);
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
        this.salt = random;
    }

    public ID(long groupId, int mid4) {
        this.groupId = groupId;
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(mid4);
        byteBuffer.position(0);
        this.salt = byteBuffer.getShort();
        this.courseId = byteBuffer.getShort();
        this.itemId = 0;
        /*
        byte[] bytes = Utils.intToByteArray(mid4);
        this.salt = Utils.bytesToShort(bytes[0], bytes[1]);
        this.courseId = Utils.bytesToShort(bytes[2], bytes[3]);
        this.itemId = 0;*/
    }

    public ID(long groupId, int mid4, int low4) {
        this.groupId = groupId;
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        byteBuffer.putInt(mid4);
        byteBuffer.putInt(low4);
        byteBuffer.position(0);
        this.salt = byteBuffer.getShort();
        this.courseId = byteBuffer.getShort();
        this.itemId = byteBuffer.getInt();
        /*
        byte[] mids = Utils.intToByteArray(mid4);
        this.salt = Utils.bytesToShort(mids[0], mids[1]);
        this.courseId = Utils.bytesToShort(mids[2], mids[3]);
        byte[] lows = Utils.intToByteArray(low4);
        this.itemId = Utils.bytesToInt(lows[0], lows[1], lows[2], lows[3]);*/
    }

    public static ID generateGroupId() {
        return new ID(System.currentTimeMillis(), (short) new Random().nextInt(65536));
    }

    public ID getCourseId() {
        return new ID(groupId, courseId, salt);
    }

    public ID getGroupId() {
        return new ID(groupId, salt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(groupId);
        dest.writeInt(salt);
        dest.writeInt(courseId);
        dest.writeInt(itemId);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ID) {
            ID id = (ID) o;
            return groupId == id.groupId && salt == id.salt && courseId == id.courseId && itemId == id.itemId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) (groupId & GROUP_ID_MASK) ^ getMid4() ^ itemId;
    }

    @Override
    public int compareTo(@NonNull ID another) {
        if (groupId > another.groupId) { return 1; } else if (groupId < another.groupId) { return -1; } else {
            if (courseId > another.courseId) { return 1; } else if (courseId < another.courseId) {
                return -1;
            } else {
                if (itemId > another.itemId) { return 1; } else if (itemId < another.itemId) { return -1; } else {
                    Log.e(TAG, "duplicate IDs");
                    return 0;
                }
            }
        }
    }

    public long getHigh8() {
        return groupId;
    }

    public short getSalt() {
        return salt;
    }

    public int getMid4() {
        return (salt << 2 * 8) | courseId;
    }

    public int getLow4() {
        return itemId;
    }
}
