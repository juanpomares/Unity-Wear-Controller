package *your.package.name*;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.MotionEvent;
import android.view.View;

interface JoystickListener
{
    public void onPositionChange(float newX, float newY);
};

public class JoystickView extends View
{
    private float mRADIUS = 25;
    private float mMEDIUM_RADIUS = 25;

    private JoystickListener mJoystickListener;
    private static final float mRadiusDivider =4;

    private int mScreenWidth =0, mScreenHeight =0;
    private float mMidWidth =0, mMidHeight =0;

    private float mX, mY;
    public float mNormalizedX, mNormalizedY;//Normalized

    private boolean mTouchingScreen =false;

    public JoystickView(Context context, JoystickListener _list){ super(context); mJoystickListener=_list;}

    private boolean collidesCircle(float ex, float ey)
    {
        setNormalizedCoordinatesWithoutListener(ex, ey);

        if( ex >= mMidWidth - mMEDIUM_RADIUS
                && ex <= mMidWidth + mMEDIUM_RADIUS
                && ey >= mMidHeight - mMEDIUM_RADIUS
                && ey <= mMidHeight + mMEDIUM_RADIUS)
            mTouchingScreen =true;

        if(!mTouchingScreen)
        {
            setNormalizedCoordinates(mMidWidth, mMidHeight);
        }else
            mJoystickListener.onPositionChange(mNormalizedX, mNormalizedY);

        return mTouchingScreen;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        mScreenWidth =this.getWidth();
        mScreenHeight =this.getHeight();

        mMidWidth = mScreenWidth /2.0f;
        mMidHeight = mScreenHeight /2.0f;

        mRADIUS = mScreenWidth / mRadiusDivider;
        mMEDIUM_RADIUS =3* mRADIUS /4;
        mX = mMidWidth;
        mY = mMidHeight;
        mNormalizedX =0; mNormalizedY =0;
        mTouchingScreen =false;
    }

    private Paint CreatePaintWithAntialias() {
        Paint _paint = new Paint();
        //Enabling antialias
        _paint.setDither(true);                    // set the dither to true
        _paint.setStrokeJoin(Paint.Join.ROUND);    // set the join to round you want
        _paint.setStrokeCap(Paint.Cap.ROUND);      // set the paint cap to round too
        _paint.setAntiAlias(true);                 // set anti alias so it smooths

        return _paint;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        Paint paint = CreatePaintWithAntialias();
        Paint paint2= CreatePaintWithAntialias();

        if(mTouchingScreen)
            paint.setColor(Color.GRAY);
        else
            paint.setColor(Color.BLACK);

        paint2.setColor(Color.WHITE);
        paint2.setAlpha(128);

        paint2.setStyle(Style.FILL);
        paint.setStyle(Style.FILL);

        canvas.drawColor(Color.WHITE);
        canvas.drawCircle(mX, mY, mRADIUS, paint);
        canvas.drawCircle(mX, mY, mRADIUS /1.5f, paint2);

        /*
        //Draw red lines
        paint.setColor(Color.RED);
        canvas.drawLine(0, MidScreenHeight, mWidth, MidScreenHeight, paint);
        canvas.drawLine(MidScreenWidth, 0, MidScreenWidth, mHeight, paint);*/
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN)
        {
            float ex = event.getX();
            float ey = event.getY();

            if(collidesCircle(ex, ey))
            {
                mX =ex; mY =ey;
                this.invalidate();
                return true;
            }else
                return false;

        } else if (event.getAction() == MotionEvent.ACTION_MOVE)
        {
            mX = event.getX();
            mY = event.getY();

            setNormalizedCoordinates(mX, mY);
            this.invalidate();


            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP)
        {
            mX = mMidWidth; mY = mMidHeight;
            setNormalizedCoordinates(mX, mY);
            mTouchingScreen =false;

            this.invalidate();
        }

        return true;
        // return detectorGestos.onTouchEvent(event);
    }

    private void setNormalizedCoordinates(float x, float y)
    {
        float _PreviousNX= mNormalizedX, _PreviousNY= mNormalizedY;

        mNormalizedX =(x- mMidWidth)/(mMidWidth);
        mNormalizedY =-(y- mMidHeight)/(mMidHeight);

        if(mNormalizedX >1) mNormalizedX =1; else if(mNormalizedX <-1) mNormalizedX =-1;
        if(mNormalizedY >1) mNormalizedY =1; else if(mNormalizedY <-1) mNormalizedY =-1;

        if(!(mNormalizedX ==_PreviousNX && mNormalizedY ==_PreviousNY))
            mJoystickListener.onPositionChange(mNormalizedX, mNormalizedY);
    }

    private void setNormalizedCoordinatesWithoutListener(float x, float y)
    {
        //float antnx= mNormalizedX, antny= mNormalizedY;

        mNormalizedX =(x- mMidWidth)/(mMidWidth);
        mNormalizedY =-(y- mMidHeight)/(mMidHeight);

        if(mNormalizedX >1) mNormalizedX =1; else if(mNormalizedX <-1) mNormalizedX =-1;
        if(mNormalizedY >1) mNormalizedY =1; else if(mNormalizedY <-1) mNormalizedY =-1;
    }
}
