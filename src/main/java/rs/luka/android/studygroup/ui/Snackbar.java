package rs.luka.android.studygroup.ui;

import android.animation.Animator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

import rs.luka.android.studygroup.R;

/**
 * Created by luka on 19.7.15..
 */
public class Snackbar {

    public static final  int LENGTH_SHORT       = -1;
    public static final  int LENGTH_LONG        = 0;
    private static final int ANIMATION_DURATION = 250;
    private static final int ANIMATION_FADE_DURATION = 180;
    private static final int MSG_SHOW                = 0;
    private static final int MSG_DISMISS             = 1;
    private final ViewGroup               mParent;
    private final Context                 mContext;
    private final Snackbar.SnackbarLayout mView;
    private final SnackbarManager.Callback mManagerCallback = new SnackbarManager.Callback() {
        public void show() {
            Snackbar.sHandler.sendMessage(Snackbar.sHandler.obtainMessage(0, Snackbar.this));
        }

        public void dismiss() {
            Snackbar.sHandler.sendMessage(Snackbar.sHandler.obtainMessage(1, Snackbar.this));
        }
    };
    private boolean isActionPressed = false;
    private FloatingActionButton fab;
    private int            mDuration;
    private OnHideListener hideListener;
    private static final Handler sHandler = new Handler(Looper.getMainLooper(),
                                                        new Handler.Callback() {
                                                            public boolean handleMessage(
                                                                    Message message) {
                                                                switch (message.what) {
                                                                    case 0:
                                                                        ((Snackbar) message.obj).showView();
                                                                        return true;
                                                                    case 1:
                                                                        ((Snackbar) message.obj).hideView();
                                                                        return true;
                                                                    default:
                                                                        return false;
                                                                }
                                                            }
                                                        });

    Snackbar(ViewGroup parent) {
        this.mParent = parent;
        this.mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(this.mContext);
        this.mView = (Snackbar.SnackbarLayout) inflater.inflate(R.layout.snackbar,
                                                                this.mParent,
                                                                false);
    }

    public static Snackbar make(View view, CharSequence text, int duration) {
        Snackbar snackbar = new Snackbar(findSuitableParent(view));
        snackbar.setText(text);
        snackbar.setDuration(duration);
        return snackbar;
    }

    public static Snackbar make(View view, int resId, int duration) {
        return make(view, view.getResources().getText(resId), duration);
    }

    @Nullable
    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;

        do {
            if (view instanceof CoordinatorLayout) {
                return (ViewGroup) view;
            }

            if (view instanceof FrameLayout) {
                if (view.getId() == android.R.id.content /*aka 16908290*/) {
                    return (ViewGroup) view;
                }

                fallback = (ViewGroup) view;
            }

            if (view != null) {
                ViewParent parent = view.getParent();
                view = parent instanceof View ? (View) parent : null;
            }
        } while (view != null);

        return fallback;
    }

    public Snackbar colorTheFuckingTextToWhite(Context c) {
        ((TextView) mView.findViewById(android.support.design.R.id.snackbar_text))
                .setTextColor(c.getResources().getColor(R.color.white));
        return this;
    }

    public Snackbar doStuffThatGoogleDidntFuckingDoProperly(Context c, FloatingActionButton fab) {
        mView.getMessageView()
             .setWidth(((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
                                                                                   .getWidth());
        this.fab = fab;
        return this;
    }

    public Snackbar setOnHideListener(OnHideListener l) {
        hideListener = l;
        return this;
    }

    public Snackbar setAction(@StringRes int resId, View.OnClickListener listener) {
        return this.setAction(this.mContext.getText(resId), listener);
    }

    public Snackbar setAction(CharSequence text, final View.OnClickListener listener) {
        TextView tv = this.mView.getActionView();
        if (!TextUtils.isEmpty(text) && listener != null) {
            isActionPressed = false;
            tv.setVisibility(View.VISIBLE /*aka 0*/);
            tv.setText(text);
            tv.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    isActionPressed = true;
                    listener.onClick(view);
                    Snackbar.this.dismiss();
                }
            });
        } else {
            tv.setVisibility(View.GONE /*aka 8*/);
            tv.setOnClickListener(null);
        }

        return this;
    }

    public Snackbar setActionTextColor(ColorStateList colors) {
        TextView tv = this.mView.getActionView();
        tv.setTextColor(colors);
        return this;
    }

    public Snackbar setActionTextColor(int color) {
        TextView tv = this.mView.getActionView();
        tv.setTextColor(color);
        return this;
    }

    public Snackbar setText(CharSequence message) {
        TextView tv = this.mView.getMessageView();
        tv.setText(message);
        return this;
    }

    public Snackbar setText(@StringRes int resId) {
        return this.setText(this.mContext.getText(resId));
    }

    public int getDuration() {
        return this.mDuration;
    }

    public Snackbar setDuration(int duration) {
        this.mDuration = duration;
        return this;
    }

    public View getView() {
        return this.mView;
    }

    public void show() {
        SnackbarManager.getInstance().show(this.mDuration, this.mManagerCallback);
    }

    public void dismiss() {
        SnackbarManager.getInstance().dismiss(this.mManagerCallback);
    }

    final void showView() {
        if (this.mView.getParent() == null) {
            ViewGroup.LayoutParams lp = this.mView.getLayoutParams();
            if (lp instanceof android.support.design.widget.CoordinatorLayout.LayoutParams) {
                Snackbar.Behavior behavior = new Snackbar.Behavior();
                behavior.setStartAlphaSwipeDistance(0.1F);
                behavior.setEndAlphaSwipeDistance(0.6F);
                behavior.setSwipeDirection(0);
                behavior.setListener(new SwipeDismissBehavior.OnDismissListener() {
                    public void onDismiss(View view) {
                        Snackbar.this.dismiss();
                    }

                    public void onDragStateChanged(int state) {
                        switch (state) {
                            case 0:
                                SnackbarManager.getInstance()
                                               .restoreTimeout(Snackbar.this.mManagerCallback);
                                break;
                            case 1:
                            case 2:
                                SnackbarManager.getInstance()
                                               .cancelTimeout(Snackbar.this.mManagerCallback);
                        }

                    }
                });
                ((android.support.design.widget.CoordinatorLayout.LayoutParams) lp).setBehavior(
                        behavior);
            }

            this.mParent.addView(this.mView);
        }

        if (ViewCompat.isLaidOut(this.mView)) {
            this.animateViewIn();
        } else {
            this.mView.setOnLayoutChangeListener(new Snackbar.SnackbarLayout.OnLayoutChangeListener() {
                public void onLayoutChange(View view, int left, int top, int right, int bottom) {
                    Snackbar.this.animateViewIn();
                    Snackbar.this.mView.setOnLayoutChangeListener(null);
                }
            });
        }

    }

    private void animateViewIn() {
        //if(Build.VERSION.SDK_INT >= 14) {

        mView.setTranslationY(this.mView.getHeight());
        ViewPropertyAnimator viewAnim = mView.animate()
                                             .translationY(0.0F)
                                             .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                                             .setDuration(ANIMATION_DURATION)
                                             .setListener(new Animator.AnimatorListener() {

                                                 public void onAnimationStart(
                                                         Animator anim) {
                                                     Snackbar.this.mView.animateChildrenIn(
                                                             70,
                                                             180);
                                                 }

                                                 public void onAnimationEnd(Animator anim) {
                                                     SnackbarManager.getInstance()
                                                                    .onShown(Snackbar.this.mManagerCallback);
                                                 }

                                                 @Override
                                                 public void onAnimationCancel(Animator animation) {
                                                     //do nothing?
                                                 }

                                                 @Override
                                                 public void onAnimationRepeat(Animator animation) {
                                                     //do nothing?
                                                 }
                                             });
        if (fab != null) {
            ViewPropertyAnimator fabAnim = fab.animate()
                                              .translationY(-mView.getHeight())
                                              .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                                              .setDuration(ANIMATION_DURATION);
            fabAnim.start();
        }
        viewAnim.start();


        /*} else {
            Animation anim = android.view.animation.AnimationUtils.loadAnimation(this.mView.getContext(), anim.snackbar_in);
            anim.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            anim.setDuration(250L);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationEnd(Animation animation) {
                    SnackbarManager.getInstance().onShown(Snackbar.this.mManagerCallback);
                }

                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
            this.mView.startAnimation(anim);
        }*/ //ne radi (fale mi neke klase) i nepotrebno, minSdk je ICS

    }

    private void animateViewOut() {
        //if(Build.VERSION.SDK_INT >= 14) {
        ViewPropertyAnimatorCompat viewAnim = ViewCompat.animate(this.mView)
                                                        .translationY((float) this.mView.getHeight())
                                                        .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                                                        .setDuration(ANIMATION_DURATION)
                                                        .setListener(new ViewPropertyAnimatorListenerAdapter() {
                                                            public void onAnimationStart(
                                                                    View view) {
                                                                Snackbar.this.mView.animateChildrenOut(
                                                                        0,
                                                                        180);
                                                            }

                                                            public void onAnimationEnd(View view) {
                                                                Snackbar.this.onViewHidden();
                                                            }
                                                        });

        if (fab != null) {
            ViewPropertyAnimator fabAnim = fab.animate()
                                              .translationYBy(mView.getHeight())
                                              .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                                              .setDuration(ANIMATION_DURATION);
            fabAnim.start();
        }
        viewAnim.start();

        /*} else {
            Animation anim = android.view.animation.AnimationUtils.loadAnimation(this.mView.getContext(), anim.snackbar_out);
            anim.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
            anim.setDuration(250L);
            anim.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationEnd(Animation animation) {
                    Snackbar.this.onViewHidden();
                }

                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
            this.mView.startAnimation(anim);
        }*/ //ne radi i nepotrebno (minSdk je ICS)

    }

    final void hideView() {
        if (this.mView.getVisibility() == View.VISIBLE /*aka 0*/ && !this.isBeingDragged()) {
            this.animateViewOut();
        } else {
            this.onViewHidden();
        }

    }

    private void onViewHidden() {
        this.mParent.removeView(this.mView);
        if (!isActionPressed && hideListener != null) hideListener.onHide();
        SnackbarManager.getInstance().onDismissed(this.mManagerCallback);
    }

    private boolean isBeingDragged() {
        ViewGroup.LayoutParams lp = this.mView.getLayoutParams();
        if (lp instanceof android.support.design.widget.CoordinatorLayout.LayoutParams) {
            android.support.design.widget.CoordinatorLayout.LayoutParams cllp
                    = (android.support.design.widget.CoordinatorLayout.LayoutParams) lp;
            android.support.design.widget.CoordinatorLayout.Behavior behavior = cllp.getBehavior();
            if (behavior instanceof SwipeDismissBehavior) {
                return ((SwipeDismissBehavior) behavior).getDragState() != 0;
            }
        }

        return false;
    }

    public interface OnHideListener {
        void onHide();
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {
    }

    public static class SnackbarLayout extends LinearLayout {
        private TextView                                       mMessageView;
        private TextView                                       mActionView;
        private int                                            mMaxWidth;
        private int                                            mMaxInlineActionWidth;
        private Snackbar.SnackbarLayout.OnLayoutChangeListener mOnLayoutChangeListener;

        public SnackbarLayout(Context context) {
            this(context, null);
        }

        public SnackbarLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs,
                                                          android.support.design.R.styleable.SnackbarLayout);
            this.mMaxWidth
                    = a.getDimensionPixelSize(android.support.design.R.styleable.SnackbarLayout_android_maxWidth,
                                              -1);
            this.mMaxInlineActionWidth
                    = a.getDimensionPixelSize(android.support.design.R.styleable.SnackbarLayout_maxActionInlineWidth,
                                              -1);
            if (a.hasValue(android.support.design.R.styleable.SnackbarLayout_elevation)) {
                ViewCompat.setElevation(this,
                                        (float) a.getDimensionPixelSize(android.support.design.R.styleable.SnackbarLayout_elevation,
                                                                        0));
            }

            a.recycle();
            this.setClickable(true);
            LayoutInflater.from(context)
                          .inflate(android.support.design.R.layout.layout_snackbar_include,
                                   this); //namechange @ API 23, todo check
        }

        private static void updateTopBottomPadding(View view, int topPadding, int bottomPadding) {
            if (ViewCompat.isPaddingRelative(view)) {
                ViewCompat.setPaddingRelative(view,
                                              ViewCompat.getPaddingStart(view),
                                              topPadding,
                                              ViewCompat.getPaddingEnd(view),
                                              bottomPadding);
            } else {
                view.setPadding(view.getPaddingLeft(),
                                topPadding,
                                view.getPaddingRight(),
                                bottomPadding);
            }

        }

        protected void onFinishInflate() {
            super.onFinishInflate();
            this.mMessageView
                    = (TextView) this.findViewById(android.support.design.R.id.snackbar_text);
            this.mActionView
                    = (TextView) this.findViewById(android.support.design.R.id.snackbar_action);
        }

        TextView getMessageView() {
            return this.mMessageView;
        }

        TextView getActionView() {
            return this.mActionView;
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (this.mMaxWidth > 0 && this.getMeasuredWidth() > this.mMaxWidth) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(this.mMaxWidth,
                                                               MeasureSpec.EXACTLY /*aka 1073741824*/);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

            int multiLineVPadding = this.getResources()
                                        .getDimensionPixelSize(android.support.design.R.dimen.snackbar_padding_vertical_2lines); //namechange
            int singleLineVPadding = this.getResources()
                                         .getDimensionPixelSize(android.support.design.R.dimen.snackbar_padding_vertical); //namechange
            boolean isMultiLine = this.mMessageView.getLayout().getLineCount() > 1;
            boolean remeasure   = false;
            if (isMultiLine && this.mMaxInlineActionWidth > 0
                && this.mActionView.getMeasuredWidth() > this.mMaxInlineActionWidth) {
                if (this.updateViewsWithinLayout(1,
                                                 multiLineVPadding,
                                                 multiLineVPadding - singleLineVPadding)) {
                    remeasure = true;
                }
            } else {
                int messagePadding = isMultiLine ? multiLineVPadding : singleLineVPadding;
                if (this.updateViewsWithinLayout(0, messagePadding, messagePadding)) {
                    remeasure = true;
                }
            }

            if (remeasure) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

        }

        void animateChildrenIn(int delay, int duration) {
            ViewCompat.setAlpha(this.mMessageView, 0.0F);
            ViewCompat.animate(this.mMessageView)
                      .alpha(1.0F)
                      .setDuration((long) duration)
                      .setStartDelay((long) delay)
                      .start();
            if (this.mActionView.getVisibility() == View.VISIBLE /*aka 0*/) {
                ViewCompat.setAlpha(this.mActionView, 0.0F);
                ViewCompat.animate(this.mActionView)
                          .alpha(1.0F)
                          .setDuration((long) duration)
                          .setStartDelay((long) delay)
                          .start();
            }

        }

        void animateChildrenOut(int delay, int duration) {
            ViewCompat.setAlpha(this.mMessageView, 1.0F);
            ViewCompat.animate(this.mMessageView)
                      .alpha(0.0F)
                      .setDuration((long) duration)
                      .setStartDelay((long) delay)
                      .start();
            if (this.mActionView.getVisibility() == View.VISIBLE /*aka 0*/) {
                ViewCompat.setAlpha(this.mActionView, 1.0F);
                ViewCompat.animate(this.mActionView)
                          .alpha(0.0F)
                          .setDuration((long) duration)
                          .setStartDelay((long) delay)
                          .start();
            }

        }

        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (changed && this.mOnLayoutChangeListener != null) {
                this.mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
            }

        }

        void setOnLayoutChangeListener(
                Snackbar.SnackbarLayout.OnLayoutChangeListener onLayoutChangeListener) {
            this.mOnLayoutChangeListener = onLayoutChangeListener;
        }

        private boolean updateViewsWithinLayout(int orientation, int messagePadTop,
                                                int messagePadBottom) {
            boolean changed = false;
            if (orientation != this.getOrientation()) {
                this.setOrientation(orientation);
                changed = true;
            }

            if (this.mMessageView.getPaddingTop() != messagePadTop
                || this.mMessageView.getPaddingBottom() != messagePadBottom) {
                updateTopBottomPadding(this.mMessageView, messagePadTop, messagePadBottom);
                changed = true;
            }

            return changed;
        }

        interface OnLayoutChangeListener {
            void onLayoutChange(View var1, int var2, int var3, int var4, int var5);
        }
    }

    final class Behavior extends SwipeDismissBehavior<Snackbar.SnackbarLayout> {
        Behavior() {
        }

        public boolean onInterceptTouchEvent(CoordinatorLayout parent,
                                             Snackbar.SnackbarLayout child, MotionEvent event) {
            if (parent.isPointInChildBounds(child, (int) event.getX(), (int) event.getY())) {
                switch (event.getActionMasked()) {
                    case 0:
                        SnackbarManager.getInstance().cancelTimeout(Snackbar.this.mManagerCallback);
                        break;
                    case 1:
                    case 3:
                        SnackbarManager.getInstance()
                                       .restoreTimeout(Snackbar.this.mManagerCallback);
                    case 2:
                }
            }

            return super.onInterceptTouchEvent(parent, child, event);
        }
    }
}

class SnackbarManager {
    private static final int MSG_TIMEOUT       = 0;
    private static final int SHORT_DURATION_MS = 1500;
    private static final int LONG_DURATION_MS  = 2750;
    private static SnackbarManager sSnackbarManager;
    private final Object mLock = new Object();
    private SnackbarManager.SnackbarRecord mCurrentSnackbar;
    private SnackbarManager.SnackbarRecord mNextSnackbar;
    private final Handler mHandler = new Handler(Looper.getMainLooper(),
                                                 new android.os.Handler.Callback() {
                                                     public boolean handleMessage(Message message) {
                                                         switch (message.what) {
                                                             case 0:
                                                                 SnackbarManager.this.handleTimeout(
                                                                         (SnackbarManager.SnackbarRecord) message.obj);
                                                                 return true;
                                                             default:
                                                                 return false;
                                                         }
                                                     }
                                                 });

    private SnackbarManager() {
    }

    static SnackbarManager getInstance() {
        if (sSnackbarManager == null) {
            sSnackbarManager = new SnackbarManager();
        }

        return sSnackbarManager;
    }

    public void show(int duration, SnackbarManager.Callback callback) {
        Object var3 = this.mLock;
        synchronized (this.mLock) {
            if (this.isCurrentSnackbar(callback)) {
                this.mCurrentSnackbar.duration = duration;
                this.mHandler.removeCallbacksAndMessages(this.mCurrentSnackbar);
                this.scheduleTimeoutLocked(this.mCurrentSnackbar);
            } else {
                if (this.isNextSnackbar(callback)) {
                    this.mNextSnackbar.duration = duration;
                } else {
                    this.mNextSnackbar = new SnackbarManager.SnackbarRecord(duration, callback);
                }

                if (this.mCurrentSnackbar == null
                    || !this.cancelSnackbarLocked(this.mCurrentSnackbar)) {
                    this.mCurrentSnackbar = null;
                    this.showNextSnackbarLocked();
                }
            }
        }
    }

    public void dismiss(SnackbarManager.Callback callback) {
        Object var2 = this.mLock; //ne kapiram poentu ovoga, ali ajde
        synchronized (this.mLock) {
            if (this.isCurrentSnackbar(callback)) {
                this.cancelSnackbarLocked(this.mCurrentSnackbar);
            }

            if (this.isNextSnackbar(callback)) {
                this.cancelSnackbarLocked(this.mNextSnackbar);
            }

        }
    }

    public void onDismissed(SnackbarManager.Callback callback) {
        Object var2 = this.mLock;
        synchronized (this.mLock) {
            if (this.isCurrentSnackbar(callback)) {
                this.mCurrentSnackbar = null;
                if (this.mNextSnackbar != null) {
                    this.showNextSnackbarLocked();
                }
            }

        }
    }

    public void onShown(SnackbarManager.Callback callback) {
        Object var2 = this.mLock;
        synchronized (this.mLock) {
            if (this.isCurrentSnackbar(callback)) {
                this.scheduleTimeoutLocked(this.mCurrentSnackbar);
            }

        }
    }

    public void cancelTimeout(SnackbarManager.Callback callback) {
        Object var2 = this.mLock;
        synchronized (this.mLock) {
            if (this.isCurrentSnackbar(callback)) {
                this.mHandler.removeCallbacksAndMessages(this.mCurrentSnackbar);
            }

        }
    }

    public void restoreTimeout(SnackbarManager.Callback callback) {
        Object var2 = this.mLock;
        synchronized (this.mLock) {
            if (this.isCurrentSnackbar(callback)) {
                this.scheduleTimeoutLocked(this.mCurrentSnackbar);
            }

        }
    }

    private void showNextSnackbarLocked() {
        if (this.mNextSnackbar != null) {
            this.mCurrentSnackbar = this.mNextSnackbar;
            this.mNextSnackbar = null;
            SnackbarManager.Callback callback = this.mCurrentSnackbar.callback.get();
            if (callback != null) {
                callback.show();
            } else {
                this.mCurrentSnackbar = null;
            }
        }

    }

    private boolean cancelSnackbarLocked(SnackbarManager.SnackbarRecord record) {
        SnackbarManager.Callback callback = record.callback.get();
        if (callback != null) {
            callback.dismiss();
            return true;
        } else {
            return false;
        }
    }

    private boolean isCurrentSnackbar(SnackbarManager.Callback callback) {
        return this.mCurrentSnackbar != null && this.mCurrentSnackbar.isSnackbar(callback);
    }

    private boolean isNextSnackbar(SnackbarManager.Callback callback) {
        return this.mNextSnackbar != null && this.mNextSnackbar.isSnackbar(callback);
    }

    private void scheduleTimeoutLocked(SnackbarManager.SnackbarRecord r) {
        this.mHandler.removeCallbacksAndMessages(r);
        this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 0, r),
                                         r.duration == 0 ? 2750L : 1500L);
    }

    private void handleTimeout(SnackbarManager.SnackbarRecord record) {
        Object var2 = this.mLock;
        synchronized (this.mLock) {
            if (this.mCurrentSnackbar == record || this.mNextSnackbar == record) {
                this.cancelSnackbarLocked(record);
            }

        }
    }

    interface Callback {
        void show();

        void dismiss();
    }

    private static class SnackbarRecord {
        private final WeakReference<Callback> callback;
        private       int                     duration;

        SnackbarRecord(int duration, SnackbarManager.Callback callback) {
            this.callback = new WeakReference(callback);
            this.duration = duration;
        }

        boolean isSnackbar(SnackbarManager.Callback callback) {
            return callback != null && this.callback.get() == callback;
        }
    }
}

class AnimationUtils {
    static final Interpolator LINEAR_INTERPOLATOR           = new LinearInterpolator();
    static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();
    static final Interpolator DECELERATE_INTERPOLATOR       = new DecelerateInterpolator();

    AnimationUtils() {
    }

    static float lerp(float startValue, float endValue, float fraction) {
        return startValue + fraction * (endValue - startValue);
    }

    static int lerp(int startValue, int endValue, float fraction) {
        return startValue + Math.round(fraction * (float) (endValue - startValue));
    }

    static class AnimationListenerAdapter implements Animation.AnimationListener {
        AnimationListenerAdapter() {
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }
}
