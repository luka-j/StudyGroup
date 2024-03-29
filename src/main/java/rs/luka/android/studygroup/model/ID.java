package rs.luka.android.studygroup.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

/**
 * Hijerarhijski ID, menjao se poprilično kroz vreme. Može predstavljati grupu, kurs ili "item" - belešku, pitanje
 * ili proveru. Iako je u trenutnoj implementaciji jedan long dovoljan za predstavljanje bilo koje stavke,
 * to nije od početka bio slučaj, odnosno originalna ideja je bila da svaka stavka vuče ovaj ID sa sobom, na osnovu
 * koga bi mogla da nađe roditelje, a ID vrednost roditelja je sastavni deo ID-a deteta (grupe i kursevi su imali
 * svoj prostor, dok su itemovi delili 4 bajta). Ispostavlja se da je to nepraktično čuvati u bazi u tom obliku,
 * stoga ovaj format.
 *
 * ID nije namenjen za prosleđivanje drugima, kao ni za "rasparčavanje" (uzimanje samo određenih bajtova iz njega),
 * ali su te funkcije neophodne za rad baze i komunikaciju sa serverom, stoga su ovde pružene.
 *
 * Created by luka on 24.7.15.
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
    private long groupId;
    private long courseId;
    private long itemId;

    private ID(Parcel in) {
        groupId = in.readLong();
        courseId = in.readLong();
        itemId = in.readLong();
    }

    public ID(ID parent, long child) {
        if(parent.courseId == 0) {
            groupId = parent.groupId;
            courseId = child;
            itemId = 0;
        } else {
            groupId = parent.groupId;
            courseId = parent.courseId;
            itemId = child;
        }
    }

    public ID(long groupId) {
        this(groupId, 0);
    }

    public ID(long groupId, long courseId) {
        this(groupId, courseId, 0);
    }

    public ID(long groupId, long courseId, long itemId) {
        this.groupId = groupId;
        this.courseId = courseId;
        this.itemId = itemId;
    }

    public static ID generateGroupId() { //debug method
        return new ID(System.currentTimeMillis(), (short) new Random().nextInt(65536));
    }

    public ID getCourseId() {
        return new ID(groupId, courseId);
    }

    public ID getGroupId() {
        return new ID(groupId);
    }

    public long getGroupIdValue() {
        return groupId;
    }
    public long getCourseIdValue() {
        return courseId;
    }
    public long getItemIdValue() {
        return itemId;
    }

    public boolean isGroupId() {
        return courseId == 0 && itemId == 0;
    }

    public boolean isCourseId() {
        return courseId != 0 && itemId == 0;
    }

    public boolean isItemId() {
        return courseId != 0 && itemId != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(groupId);
        dest.writeLong(courseId);
        dest.writeLong(itemId);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ID) {
            ID id = (ID) o;
            return groupId == id.groupId && courseId == id.courseId && itemId == id.itemId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new long[] {groupId, courseId, itemId});
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

    @Override
    public String toString() {
        ByteBuffer bytes = ByteBuffer.allocate(24);
        bytes.putLong(groupId);
        bytes.putLong(courseId);
        bytes.putLong(itemId);
        return Base64.encodeToString(bytes.array(), Base64.NO_WRAP | Base64.URL_SAFE | Base64.NO_PADDING);
    }
}
