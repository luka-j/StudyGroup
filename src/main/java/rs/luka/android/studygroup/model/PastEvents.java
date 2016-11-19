package rs.luka.android.studygroup.model;

import android.os.Parcelable;

import rs.luka.android.studygroup.io.network.Network;

/**
 *
 * Created by luka on 20.9.15.
 */
public interface PastEvents extends Parcelable {
    void getHistory(int requestId, Network.NetworkCallbacks<String> callbacks);
}
