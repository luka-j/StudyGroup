package rs.luka.android.studygroup.misc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by luka on 13.7.15..
 */
public class HideShowListener extends RecyclerView.OnScrollListener {

    private static int VIEW_ELEVATION = 4;
    // Keeps track of the overall vertical offset in the list
    int     verticalOffset;
    // Determines the scroll UP/DOWN direction
    boolean scrollingUp;
    private View view;

    public HideShowListener(View v) {
        this.view = v;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            if (scrollingUp) {
                if (verticalOffset > view.getHeight()) {
                    toolbarAnimateHide();
                } else {
                    toolbarAnimateShow(verticalOffset);
                }
            } else {
                if (view.getTranslationY() < view.getHeight() * -0.6
                    && verticalOffset > view.getHeight()) {
                    toolbarAnimateHide();
                } else {
                    toolbarAnimateShow(verticalOffset);
                }
            }
        }
    }

    @Override
    public final void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        verticalOffset += dy;
        scrollingUp = dy > 0;
        int toolbarYOffset = (int) (dy - view.getTranslationY());
        view.animate().cancel();
        if (scrollingUp) {
            if (toolbarYOffset < view.getHeight()) {
                if (verticalOffset > view.getHeight()) {
                    toolbarSetElevation(VIEW_ELEVATION);
                }
                view.setTranslationY(-toolbarYOffset);
            } else {
                toolbarSetElevation(0);
                view.setTranslationY(-view.getHeight());
            }
        } else {
            if (toolbarYOffset < 0) {
                if (verticalOffset <= 0) {
                    toolbarSetElevation(0);
                }
                view.setTranslationY(0);
            } else {
                if (verticalOffset > view.getHeight()) {
                    toolbarSetElevation(VIEW_ELEVATION);
                }
                view.setTranslationY(-toolbarYOffset);
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void toolbarSetElevation(float elevation) {
        // setElevation() only works on Lollipop
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setElevation(elevation);
        }
    }

    private void toolbarAnimateShow(final int verticalOffset) {
        view.animate()
            .translationY(0)
            .setInterpolator(new LinearInterpolator())
            .setDuration(180)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    toolbarSetElevation(verticalOffset == 0 ? 0 : VIEW_ELEVATION);
                }
            });

    }

    private void toolbarAnimateHide() {
        view.animate()
            .translationY(-view.getHeight())
            .setInterpolator(new LinearInterpolator())
            .setDuration(180)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    toolbarSetElevation(0);
                }
            });
    }
}

