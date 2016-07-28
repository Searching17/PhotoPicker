package com.seakun.photopicker.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;

import java.util.Observable;
import java.util.Observer;

public class ZoomImageView extends ImageView implements Observer {

    /** Paint object used when drawing bitmap. */
    private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    /** Rectangle used (and re-used) for cropping source image. */
    private final Rect mRectSrc = new Rect();

    /** Rectangle used (and re-used) for specifying drawing area on canvas. */
    private final Rect mRectDst = new Rect();

    /** Object holding aspect quotient */
    private final AspectQuotient mAspectQuotient = new AspectQuotient();

    /** State of the zoom. */
    private ZoomState mState;

    private BasicZoomControl mZoomControl;

    private float mFirstX = -1;
    private float mFirstY = -1;
    private float mSecondX = -1;
    private float mSecondY = -1;

    private int mOldCounts = 0;

    private GestureDetector mGestureDetector;
    private Handler handler = new Handler();

    public ZoomImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mZoomControl = new BasicZoomControl();
        setZoomState(mZoomControl.getZoomState());
        mZoomControl.setAspectQuotient(getAspectQuotient());
        setFocusable(true);
        setClickable(true);

        mGestureDetector = new GestureDetector(context,
                new GestureDetector.OnGestureListener() {
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public void onShowPress(MotionEvent e) {

                    }

                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        mZoomControl.pan(distanceX / getWidth(), distanceY / getHeight());
                        return true;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {

                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY) {
                        if(Math.abs(velocityX)>5000 || Math.abs(velocityY)>5000){
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    for(int i=1;i<10;i++){
                                        final float dx = -velocityX/(i *30* getWidth());
                                        final float dy = -velocityY/(i *30f * getHeight());
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mZoomControl.pan(dx, dy);
                                            }
                                        });
                                        try {
                                            Thread.sleep(20);
                                        } catch (InterruptedException e) {}
                                    }
                                }
                            }).start();
                        }
                        return true;
                    }
                });

        mGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                performClick();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent event) {
                if (mState.getZoom() < BasicZoomControl.MAX_ZOOM) {
                    mZoomControl.zoom(BasicZoomControl.MAX_ZOOM / mState.getZoom(), event.getX() / getWidth(), event.getY() / getHeight());
                } else {
                    mZoomControl.zoom(BasicZoomControl.MIN_ZOOM / mState.getZoom(), event.getX() / getWidth(), event.getY() / getHeight());
                }
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent event) {
                return true;
            }
        });
    }

    private void setZoomState(ZoomState state) {
        if (mState != null) {
            mState.deleteObserver(this);
        }

        mState = state;
        mState.addObserver(this);

        invalidate();
    }

    private AspectQuotient getAspectQuotient() {
        return mAspectQuotient;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        try {
            final float zoomX = mState.getZoom();
            if(zoomX>=1.01){
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            super.dispatchTouchEvent(event);
        }catch (IllegalArgumentException e){}
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() != null && mState != null) {
            if(getDrawable() instanceof GlideBitmapDrawable){
                GlideBitmapDrawable drawable = (GlideBitmapDrawable) getDrawable();

                float aspectQuotient = mAspectQuotient.get();

                final int viewWidth = getWidth();
                final int viewHeight = getHeight();
                final int bitmapWidth = drawable.getBitmap().getWidth();
                final int bitmapHeight = drawable.getBitmap().getHeight();

                final float panX = mState.getPanX();
                final float panY = mState.getPanY();
                final float zoomX = mState.getZoomX(aspectQuotient) * viewWidth / bitmapWidth;
                final float zoomY = mState.getZoomY(aspectQuotient) * viewHeight / bitmapHeight;

                // Setup source and destination rectangles
                mRectSrc.left = (int) (panX * bitmapWidth - viewWidth / (zoomX * 2));
                mRectSrc.top = (int) (panY * bitmapHeight - viewHeight / (zoomY * 2));
                mRectSrc.right = (int) (mRectSrc.left + viewWidth / zoomX);
                mRectSrc.bottom = (int) (mRectSrc.top + viewHeight / zoomY);

                // Adjust source rectangle so that it fits within the source image.
                if (mRectSrc.left < 0) {
                    mRectSrc.left = 0;
                }
                if (mRectSrc.right > bitmapWidth) {
                    mRectSrc.right = bitmapWidth;
                }
                if (mRectSrc.top < 0) {
                    mRectSrc.top = 0;
                }
                if (mRectSrc.bottom > bitmapHeight) {
                    mRectSrc.bottom = bitmapHeight;
                }

                mRectDst.left = 0;
                mRectDst.top = 0;
                mRectDst.right = viewWidth;
                mRectDst.bottom = viewHeight;
                float scaleX = bitmapWidth * zoomX;
                if(scaleX<viewWidth){
                    mRectDst.left = (int) ((viewWidth-scaleX)/2);
                    mRectDst.right = (int)(scaleX+(viewWidth-scaleX)/2);
                }else{
                    mRectDst.left = 0;
                    mRectDst.right = viewWidth;
                }
                float scaleY = bitmapHeight * zoomY;
                if(scaleY<viewHeight){
                    mRectDst.top = (int) ((viewHeight-scaleY)/2);
                    mRectDst.bottom = (int) (scaleY + ((viewHeight-scaleY)/2));
                }else{
                    mRectDst.top = 0;
                    mRectDst.bottom = viewHeight;
                }

                canvas.drawBitmap(drawable.getBitmap(), mRectSrc, mRectDst, mPaint);
            }else{
                super.onDraw(canvas);
            }
        }else
            super.onDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(getDrawable()!=null){
            if(getDrawable() instanceof GlideBitmapDrawable){
                GlideBitmapDrawable drawable = (GlideBitmapDrawable) getDrawable();
                mAspectQuotient.updateAspectQuotient(right - left, bottom - top,
                        drawable.getBitmap().getWidth(), drawable.getBitmap().getHeight());
                mAspectQuotient.notifyObservers();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(getDrawable()!=null && getDrawable() instanceof GifDrawable){
            mGestureDetector.onTouchEvent(event);
            return true;
        }
        try {
            int nCounts = event.getPointerCount();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (1 == nCounts) {
                        mFirstX = event.getX();
                        mFirstY = event.getY();
                        mOldCounts = 1;
                    } else if (1 == mOldCounts) {
                        mSecondX = event.getX(event.getPointerId(nCounts - 1));
                        mSecondY = event.getY(event.getPointerId(nCounts - 1));
                        mOldCounts = nCounts;
                    }
                    break;
                case MotionEvent.ACTION_MOVE: {
                    float fFirstX = event.getX();
                    float fFirstY = event.getY();
                    if (1 == nCounts) {
                        mOldCounts = 1;
                        float dx = (fFirstX - mFirstX) / getWidth();
                        if (mRectSrc.left == 0 && dx > 0) {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }

                        if (mRectSrc.right >= ((GlideBitmapDrawable) getDrawable()).getBitmap().getWidth()-1 && dx < 0) {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }
                        mFirstX = fFirstX;
                        mFirstY = fFirstY;
                    } else if (1 == mOldCounts) {
                        mSecondX = event.getX(event.getPointerId(nCounts - 1));
                        mSecondY = event.getY(event.getPointerId(nCounts - 1));
                        mOldCounts = nCounts;
                    } else {
                        float fSecondX = event
                                .getX(event.getPointerId(nCounts - 1));
                        float fSecondY = event
                                .getY(event.getPointerId(nCounts - 1));

                        double nLengthOld = getLength(mFirstX, mFirstY, mSecondX,
                                mSecondY);
                        double nLengthNow = getLength(fFirstX, fFirstY, fSecondX,
                                fSecondY);

                        float d = (float) ((nLengthNow - nLengthOld) / getWidth());

                        mZoomControl.zoom((float) Math.pow(20, d),
                                ((fFirstX + fSecondX) / 2 / getWidth()),
                                ((fFirstY + fSecondY) / 2 / getHeight()));

                        mSecondX = fSecondX;
                        mSecondY = fSecondY;
                        mFirstX = fFirstX;
                        mFirstY = fFirstY;
                    }
                    break;
                }
            }
            mGestureDetector.onTouchEvent(event);
        }catch (Exception e){}
        return true;
    }

    private double getLength(float x1, float y1, float x2, float y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    @Override
    public void update(Observable observable, Object data) {
        invalidate();
    }

    private class BasicZoomControl implements Observer {

        /** Minimum zoom level limit */
        private static final float MIN_ZOOM = 1;

        /** Maximum zoom level limit */
        private static final float MAX_ZOOM = 6;

        /** Zoom state under control */
        private final ZoomState mState = new ZoomState();

        /** Object holding aspect quotient of view and content */
        private AspectQuotient mAspectQuotient;

        /**
         * Set reference object holding aspect quotient
         *
         * @param aspectQuotient
         *            Object holding aspect quotient
         */
        public void setAspectQuotient(AspectQuotient aspectQuotient) {
            if (mAspectQuotient != null) {
                mAspectQuotient.deleteObserver(this);
            }

            mAspectQuotient = aspectQuotient;
            mAspectQuotient.addObserver(this);
        }

        /**
         * Get zoom state being controlled
         *
         * @return The zoom state
         */
        public ZoomState getZoomState() {
            return mState;
        }

        /**
         * Zoom
         *
         * @param f
         *            Factor of zoom to apply
         * @param x
         *            X-coordinate of invariant position
         * @param y
         *            Y-coordinate of invariant position
         */
        public void zoom(float f, float x, float y) {
            final float aspectQuotient = mAspectQuotient.get();

            final float prevZoomX = mState.getZoomX(aspectQuotient);
            final float prevZoomY = mState.getZoomY(aspectQuotient);

            mState.setZoom(mState.getZoom() * f);
            limitZoom();

            final float newZoomX = mState.getZoomX(aspectQuotient);
            final float newZoomY = mState.getZoomY(aspectQuotient);

            // Pan to keep x and y coordinate invariant
            mState.setPanX(mState.getPanX() + (x - .5f) * (1f / prevZoomX - 1f / newZoomX));
            mState.setPanY(mState.getPanY() + (y - .5f) * (1f / prevZoomY - 1f / newZoomY));

            limitPan();

            mState.notifyObservers();
        }

        /**
         * Pan
         *
         * @param dx
         *            Amount to pan in x-dimension
         * @param dy
         *            Amount to pan in y-dimension
         */
        public void pan(float dx, float dy) {
            final float aspectQuotient = mAspectQuotient.get();

            mState.setPanX(mState.getPanX() + dx
                    / mState.getZoomX(aspectQuotient));
            mState.setPanY(mState.getPanY() + dy
                    / mState.getZoomY(aspectQuotient));

            limitPan();

            mState.notifyObservers();
        }

        /**
         * Help function to figure out max delta of pan from center position.
         *
         * @param zoom
         *            Zoom value
         * @return Max delta of pan
         */
        private float getMaxPanDelta(float zoom) {
            return Math.max(0f, .5f * ((zoom - 1) / zoom));
        }

        /**
         * Force zoom to stay within limits
         */
        private void limitZoom() {
            if (mState.getZoom() < MIN_ZOOM) {
                mState.setZoom(MIN_ZOOM);
            } else if (mState.getZoom() > MAX_ZOOM) {
                mState.setZoom(MAX_ZOOM);
            }
        }

        /**
         * Force pan to stay within limits
         */
        private void limitPan() {
            float aspectQuotient = mAspectQuotient.get();

            final float zoomX = mState.getZoomX(aspectQuotient);
            final float zoomY = mState.getZoomY(aspectQuotient);

            final float panMinX = .5f - getMaxPanDelta(zoomX);
            final float panMaxX = .5f + getMaxPanDelta(zoomX);
            final float panMinY = .5f - getMaxPanDelta(zoomY);
            final float panMaxY = .5f + getMaxPanDelta(zoomY);

            if (mState.getPanX() < panMinX) {
                mState.setPanX(panMinX);
            }
            if (mState.getPanX() > panMaxX) {
                mState.setPanX(panMaxX);
            }
            if (mState.getPanY() < panMinY) {
                mState.setPanY(panMinY);
            }
            if (mState.getPanY() > panMaxY) {
                mState.setPanY(panMaxY);
            }
        }

        public void update(Observable observable, Object data) {
            limitZoom();
            limitPan();
            invalidate();
        }
    }

    private class AspectQuotient extends Observable {

        /**
         * Aspect quotient
         */
        private float mAspectQuotient;

        // Public methods

        /**
         * Gets aspect quotient
         *
         * @return The aspect quotient
         */
        public float get() {
            return mAspectQuotient;
        }

        /**
         * Updates and recalculates aspect quotient based on supplied view and
         * content dimensions.
         *
         * @param viewWidth
         *            Width of view
         * @param viewHeight
         *            Height of view
         * @param contentWidth
         *            Width of content
         * @param contentHeight
         *            Height of content
         */
        public void updateAspectQuotient(float viewWidth, float viewHeight,
                                         float contentWidth, float contentHeight) {
            final float aspectQuotient = (contentWidth / contentHeight) / (viewWidth / viewHeight);

            if (aspectQuotient != mAspectQuotient) {
                mAspectQuotient = aspectQuotient;
                setChanged();
            }
        }
    }

    private class ZoomState extends Observable {
        /**
         * Zoom level A value of 1.0 means the content fits the view.
         */
        private float mZoom;

        /**
         * Pan position x-coordinate X-coordinate of zoom window center
         * position, relative to the width of the content.
         */
        private float mPanX;

        /**
         * Pan position y-coordinate Y-coordinate of zoom window center
         * position, relative to the height of the content.
         */
        private float mPanY;

        // Public methods

        /**
         * Get current x-pan
         *
         * @return current x-pan
         */
        public float getPanX() {
            return mPanX;
        }

        /**
         * Get current y-pan
         *
         * @return Current y-pan
         */
        public float getPanY() {
            return mPanY;
        }

        /**
         * Get current zoom value
         *
         * @return Current zoom value
         */
        public float getZoom() {
            return mZoom;
        }

        /**
         * Help function for calculating current zoom value in x-dimension
         *
         * @param aspectQuotient
         *            (Aspect ratio content) / (Aspect ratio view)
         * @return Current zoom value in x-dimension
         */
        public float getZoomX(float aspectQuotient) {
            return Math.min(mZoom, mZoom * aspectQuotient);
        }

        /**
         * Help function for calculating current zoom value in y-dimension
         *
         * @param aspectQuotient
         *            (Aspect ratio content) / (Aspect ratio view)
         * @return Current zoom value in y-dimension
         */
        public float getZoomY(float aspectQuotient) {
            return Math.min(mZoom, mZoom / aspectQuotient);
        }

        /**
         * Set pan-x
         *
         * @param panX
         *            Pan-x value to set
         */
        public void setPanX(float panX) {
            if (panX != mPanX) {
                mPanX = panX;
                setChanged();
            }
        }

        /**
         * Set pan-y
         *
         * @param panY
         *            Pan-y value to set
         */
        public void setPanY(float panY) {
            if (panY != mPanY) {
                mPanY = panY;
                setChanged();
            }
        }

        /**
         * Set zoom
         *
         * @param zoom
         *            Zoom value to set
         */
        public void setZoom(float zoom) {
            if (zoom != mZoom) {
                mZoom = zoom;
                setChanged();
            }
        }
    }
}