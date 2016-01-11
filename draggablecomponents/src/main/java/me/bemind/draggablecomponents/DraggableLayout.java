package me.bemind.draggablecomponents;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ScrollView;

/**
 * Created by angelomoroni on 11/01/16.
 */
public class DraggableLayout extends FrameLayout {

    private boolean animating = false;

    private float mLastX,mStartY,mStartX,mLastY;
    private boolean mIsAnimating = false;
    private int mTouchSlop;


    private float iY,iX;

    private int action;
    private int windowwidth;
    private int  x_cord,y_cord,x,y;
    private int screenCenter;
    private boolean isScollViewScrollable;

    private OnDragActionEndListener onDragActionEndListener;


    public DraggableLayout(Context context) {
        super(context);
        init();
    }

    public DraggableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DraggableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);
        windowwidth = size.x;

        screenCenter = windowwidth / 2;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = ev.getRawX();
                mLastY = ev.getRawY();
                mStartX = mLastX;
                mStartY = mLastY;

                //coordinate iniziali
                iY = getY();
                iX = getX();

                setIfScrollViewCanScroll();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsAnimating = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float x = ev.getRawX();
                float y = ev.getRawY();
                float xDelta = Math.abs(x - mLastX);
                float yDelta = Math.abs(y - mLastY);

                float yDeltaTotal = y - mStartY;
                float xDeltaTotal = x - mStartX;
                if(mIsAnimating || !isScollViewScrollable){

                    return true;

                }else {
                    if (Math.abs(xDelta)>Math.abs(yDelta) && Math.abs(xDelta) > mTouchSlop) {
                        mIsAnimating = true;
                        mStartX = x;
                        return true;
                    }else  {
                        mIsAnimating = false;
                    }
                }

                break;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mIsAnimating = false;

                if (action == 0) {
                    onDragActionEndListener.onDrag(1f);
                    animate().y(iY).x(iX).rotation(0).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            animating = false;
                        }
                    });

                } else {


                    if(onDragActionEndListener != null){
                        onDragActionEndListener.remove();
                    }else {

                        //onDragActionEndListener.onDrag(1f);
                        animate().y(iY).x(iX).rotation(0).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                animating = false;
                            }
                        });
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:
                x_cord = (int) event.getRawX();
                y_cord = (int) event.getRawY();

                setX(iX + (x_cord - mLastX));
                setY(iY + (y_cord - mLastY));
                onDragActionEndListener.onDrag(
                        Math.abs(screenCenter - Math.abs(x_cord - mLastX))
                                / screenCenter);
                if (x_cord >= screenCenter) {
                    setRotation((float) ((x_cord - mLastX) * (Math.PI / 42)));


                    if (x_cord > (screenCenter + (screenCenter / 2))) {

                        if (x_cord > (windowwidth - (screenCenter / 4))) {
                            action = 2;
                        } else {
                            action = 0;
                        }
                    } else {
                        action = 0;

                    }

                } else {
                    // rotate
                    setRotation((float) ((x_cord - mLastX) * (Math.PI / 42)));
                    if (x_cord < (screenCenter / 2)) {

                        if (x_cord < screenCenter / 4) {
                            action = 1;
                        } else {
                            action = 0;
                        }
                    } else {
                        action = 0;

                    }

                }
                break;
            default: return super.onTouchEvent(event);
        }

        return true;

    }

    private void setIfScrollViewCanScroll() {
        ScrollView scrollView = (ScrollView) getChildAt(0);
        View child = scrollView.getChildAt(0);
        isScollViewScrollable = scrollView.getHeight() < child.getHeight() + scrollView.getPaddingTop() + scrollView.getPaddingBottom();
    }



    public void setOnDragActionEndListener(DraggableLayout.OnDragActionEndListener onDragActionEndListener) {
        this.onDragActionEndListener = onDragActionEndListener;
    }


    public static abstract class OnDragActionEndListener{
        public abstract void remove();
        public abstract void onDrag(float value);

    }
}
