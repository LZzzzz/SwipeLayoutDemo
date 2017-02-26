package com.lzj.swipelayoutdemo.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by LZJ on 2017/2/25.
 */

public class SwipeLayout extends FrameLayout {

    private ViewDragHelper helper;
    private View mBehind;
    private View mContent;
    private int mWidth;
    private int mHeight;
    private int mRange;
    private Status status = Status.CLOSE;
    private int downX;
    private int downY;

    public enum Status {
        OPEN,
        CLOSE,
        SWIPING
    }

    public SwipeLayout(Context context) {
        super(context);
        init();
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        helper = ViewDragHelper.create(this, callback);
    }

    private ViewDragHelper.Callback callback = new ViewDragHelper.Callback() {
        /**
         * 控件是否能被拖拽
         * @param child
         * @param pointerId
         * @return
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        /**
         * 获取拖拽的范围
         * @param child
         * @return
         */

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mRange;
        }

        /**
         * 限制空间拖拽范围
         * @param child
         * @param left
         * @param dx
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child == mContent) {
                if (left < -mRange) {
                    left = -mRange;
                } else if (left > 0) {
                    left = 0;
                }
            }
            if (child == mBehind) {
                if (left < mWidth - mRange) {
                    left = mWidth - mRange;
                } else if (left > mWidth) {
                    left = mWidth;
                }
            }

            return left;
        }

        /**
         * 空间位置发生改变时执行的方法
         * @param changedView
         * @param left
         * @param top
         * @param dx
         * @param dy
         */
        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (changedView == mBehind) {
                mContent.offsetLeftAndRight(dx);
            } else if (changedView == mContent) {
                mBehind.offsetLeftAndRight(dx);
            }

            dispatchEvent();
            //兼容低版本
            invalidate();
        }

        /**
         * 松开手指时是执行的方法
         * @param releasedChild
         * @param xvel x方向加速度
         * @param yvel y方向加速度
         */
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (xvel == 0 && mContent.getLeft() < -mRange * 0.5f) {
                open();
            } else if (xvel < 0) {
                open();
            } else {
                close();
            }
        }
    };

    /**
     * 关闭
     */
    private void close() {
        close(true);
    }

    public void close(boolean isSmooth) {
        if (isSmooth) {
            if (helper.smoothSlideViewTo(mContent, 0, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            LayoutContent(false);
        }
    }

    /**
     * 打开
     */
    private void open() {
        open(true);
    }

    public void open(boolean isSmooth) {
        if (isSmooth) {
            if (helper.smoothSlideViewTo(mContent, -mRange, 0)) {
                ViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            LayoutContent(true);
        }
    }

    @Override
    public void computeScroll() {
        if (helper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 根据状态执行相关操作
     */
    private void dispatchEvent() {
        Status lastStatus = status;
        status = updateStatus();
        if (status != lastStatus && swipingListener != null) {
            if (status == Status.OPEN) {
                System.out.println("打开状态");
                swipingListener.onOpened(this);
            } else if (status == Status.CLOSE) {
                System.out.println("关闭状态");
                swipingListener.onClosed(this);
            } else if (status == Status.SWIPING) {
                if (lastStatus == Status.CLOSE) {
                    System.out.println("正在打开状态");
                    swipingListener.onSwiping(this);
                }
            }
        }
    }

    /**
     * 获取当前状态
     *
     * @return
     */
    private Status updateStatus() {
        int left = mContent.getLeft();
        if (left == -mRange) {
            return Status.OPEN;
        } else if (left == 0) {
            return Status.CLOSE;
        }
        return Status.SWIPING;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) ev.getX();
                downY = (int) ev.getY();
                //按下时请求父控件不要拦截事件
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) ev.getX();
                int moveY = (int) ev.getY();

                int diffX = moveX - downX;
                int diffY = moveY - downY;
                //当上下滑时请求父控件拦截
                if (Math.abs(diffX) < Math.abs(diffY)) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    //其它情况不拦截
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return helper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try {
            helper.processTouchEvent(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        mRange = mBehind.getMeasuredWidth();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mBehind = getChildAt(0);
        mContent = getChildAt(1);
    }

    boolean isOpen = false;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        LayoutContent(isOpen);
    }

    private void LayoutContent(boolean isOpen) {
        Rect contentRect = computeContentRect(isOpen);
        mContent.layout(contentRect.left, contentRect.top, contentRect.right, contentRect.bottom);
        Rect behindRect = computeBehindRect(contentRect);
        mBehind.layout(behindRect.left, behindRect.top, behindRect.right, behindRect.bottom);
    }

    /**
     * 计算影藏按钮所在位置
     *
     * @param contentRect
     * @return
     */
    private Rect computeBehindRect(Rect contentRect) {
        return new Rect(contentRect.right, 0, contentRect.right + mRange, 0 + mHeight);
    }

    /**
     * 计算主条目所在位置
     *
     * @param isOpen
     * @return
     */
    private Rect computeContentRect(boolean isOpen) {
        int left = 0;
        if (isOpen) {
            left = -mRange;
        }
        return new Rect(left, 0, left + mWidth, 0 + mHeight);
    }

    public OnSwipingListener swipingListener;

    public void setOnSwipingListener(OnSwipingListener swipingListener) {
        this.swipingListener = swipingListener;
    }

    public interface OnSwipingListener {
        void onOpened(SwipeLayout layout);

        void onClosed(SwipeLayout layout);

        void onSwiping(SwipeLayout layout);
    }
}
