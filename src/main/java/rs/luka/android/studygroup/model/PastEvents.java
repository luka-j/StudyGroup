package rs.luka.android.studygroup.model;

import rs.luka.android.studygroup.network.Network;

/**
 * Pretpostavlja da sve implementacije takodje implementiraju Parcelable
 * Created by luka on 20.9.15.
 */
public interface PastEvents {
    void getHistory(int requestId, Network.NetworkCallbacks<String> callbacks);
}
