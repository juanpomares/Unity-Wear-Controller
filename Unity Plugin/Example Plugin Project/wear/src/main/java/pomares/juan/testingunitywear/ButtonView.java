package pomares.juan.testingunitywear;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

enum ButtonName {Center, Up, Down, Right, Left, None}

enum ViewType {HorizontalButtonView, PadButtonView, VerticalButtonView}

interface ButtonListener
{
    public void onButtonPress(ButtonName PressedButton);
    public void onButtonHold(ButtonName HoldButton);
}

public class ButtonView extends View
{
    protected ButtonListener mButtonListener;
    protected boolean mCenterEnabled=true;
    protected int mWidth=0, mHeight=0;
    protected float MidScreenWidth =0, MidScreenHeight =0;

    protected float MidButtonWidth = 25, MidButtonHeight = 25;
    protected Path mUpButtonPath, mDownButtonPath, mRightButtonPath, mLeftButtonPath, mArrowsPath;


    protected ViewType mViewType;

    public ButtonName mButtonPressed = ButtonName.None;
    public ButtonName getPressedButton(){return mButtonPressed;}

    public ButtonView(Context context, ButtonListener _listener, ViewType _type, boolean _button) {
        super(context); mButtonListener =_listener; mViewType =_type; mCenterEnabled =_button;
    }

    public ButtonView(Context context, ButtonListener _listener, ViewType _tipo)
    {
        super(context); mButtonListener=_listener; mViewType =_tipo;
    }

    public void setCenterButtonEnabled(boolean x)
    {
        mCenterEnabled=x;
        this.invalidate();
    }

    protected boolean collidesCenterButton(float ex, float ey)
    {
        if(!mCenterEnabled) return false;

        if( ex >= MidScreenWidth - MidButtonWidth
                && ex <= MidScreenWidth + MidButtonWidth
                && ey >= MidScreenHeight - MidButtonHeight
                && ey <= MidScreenHeight + MidButtonHeight)
            return true;

        return false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth=this.getWidth();
        mHeight=this.getHeight();

        MidScreenWidth =mWidth/2.0f;
        MidScreenHeight =mHeight/2.0f;

        initializePaths();
    }

    private Paint CreatePaintWithAntialias()
    {
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
        Paint _mainPaint = CreatePaintWithAntialias();
        Paint _linesPaint = CreatePaintWithAntialias();

        int _backgroundColor= Color.LTGRAY;
        int _linesColor=Color.GRAY;
        int _arrowFilledColor=Color.WHITE;
        int _centralButtonPressedColor=Color.GRAY;
        int _centralButtonUnpressedColor=Color.DKGRAY;

        canvas.drawColor(_backgroundColor);

        _linesPaint.setColor(_linesColor);
        _linesPaint.setStyle(Paint.Style.STROKE);
        _linesPaint.setStrokeWidth(10);

        canvas.drawColor(_backgroundColor);

        _mainPaint.setColor(_linesColor);
        _mainPaint.setStyle(Paint.Style.FILL);

        if(mViewType != ViewType.HorizontalButtonView)
        {
            if(mButtonPressed == ButtonName.Right)
                canvas.drawPath(mRightButtonPath, _mainPaint);
            if(mButtonPressed == ButtonName.Left)
                canvas.drawPath(mLeftButtonPath, _mainPaint);
        }

        if(mViewType != ViewType.VerticalButtonView)
        {
            if(mButtonPressed == ButtonName.Up)
                canvas.drawPath(mUpButtonPath, _mainPaint);
            if(mButtonPressed == ButtonName.Down)
                canvas.drawPath(mDownButtonPath, _mainPaint);
        }

        _mainPaint.setColor(_arrowFilledColor);
        canvas.drawPath(mArrowsPath, _mainPaint);
        canvas.drawPath(mArrowsPath, _linesPaint);

        if (mViewType == ViewType.HorizontalButtonView)
        {
            canvas.drawLine(0, MidScreenHeight, mWidth, MidScreenHeight, _linesPaint);
        }else  if (mViewType == ViewType.VerticalButtonView)
        {
            canvas.drawLine(MidScreenWidth, 0, MidScreenWidth, mHeight, _linesPaint);
        }else if (mViewType == ViewType.PadButtonView)
        {
            canvas.drawLine(0, 0, mWidth, mHeight, _linesPaint);
            canvas.drawLine(mWidth, 0, 0, mHeight, _linesPaint);
        }

        if(mCenterEnabled)
        {
            _mainPaint.setStyle(Paint.Style.FILL);
            _mainPaint.setColor(mButtonPressed == ButtonName.Center ? _centralButtonPressedColor:_centralButtonUnpressedColor);
            canvas.drawRoundRect(MidScreenWidth - MidButtonWidth, MidScreenHeight - MidButtonHeight, MidScreenWidth + MidButtonWidth, MidScreenHeight + MidButtonHeight, 20, 20, _mainPaint);
            canvas.drawRoundRect(MidScreenWidth - MidButtonWidth, MidScreenHeight - MidButtonHeight, MidScreenWidth + MidButtonWidth, MidScreenHeight + MidButtonHeight, 20, 20, _linesPaint);
        }
    }

    private void initializePaths()
    {
        mArrowsPath =new Path();
        if (mViewType == ViewType.HorizontalButtonView)
        {
            MidButtonWidth = mWidth *0.4f;
            MidButtonHeight = mHeight /6;

            mUpButtonPath =getPathFromPoints(new float[]{0, 0, mWidth, 0, mWidth, MidScreenHeight, 0, MidScreenHeight});
            mDownButtonPath =getPathFromPoints(new float[]{0, MidScreenHeight, mWidth, MidScreenHeight, mWidth, mHeight, 0, mHeight});

            mArrowsPath.moveTo(MidScreenWidth, mHeight / 10);
            mArrowsPath.lineTo(3 * mWidth / 10, mHeight / 5);
            mArrowsPath.lineTo(7 * mWidth / 10, mHeight / 5);
            mArrowsPath.lineTo(MidScreenWidth, mHeight / 10);

            mArrowsPath.moveTo(MidScreenWidth, 9 * mHeight / 10);
            mArrowsPath.lineTo(3 * mWidth / 10, 4 * mHeight / 5);
            mArrowsPath.lineTo(7 * mWidth / 10, 4 * mHeight / 5);
            mArrowsPath.lineTo(MidScreenWidth, 9 * mHeight / 10);
        }else if (mViewType == ViewType.VerticalButtonView)
        {
            MidButtonWidth = mWidth /6.f;
            MidButtonHeight = mHeight *0.4f;

            mRightButtonPath =getPathFromPoints(new float[]{mWidth, 0, mWidth, mHeight, MidScreenWidth, mHeight, MidScreenWidth, 0});
            mLeftButtonPath =getPathFromPoints(new float[]{0, 0, 0, mHeight, MidScreenWidth, mHeight, MidScreenWidth, 0});

            mArrowsPath.moveTo(mWidth /10, MidScreenHeight);
            mArrowsPath.lineTo(mWidth /5, 3* mHeight /10);
            mArrowsPath.lineTo(mWidth /5, 7* mHeight /10);
            mArrowsPath.lineTo(mWidth /10, MidScreenHeight);

            mArrowsPath.moveTo(9* mWidth /10, MidScreenHeight);
            mArrowsPath.lineTo(4* mWidth /5, 3* mHeight /10);
            mArrowsPath.lineTo(4* mWidth /5, 7* mHeight /10);
            mArrowsPath.lineTo(9* mWidth /10, MidScreenHeight);
        }else if (mViewType == ViewType.PadButtonView)
        {
            MidButtonWidth = mWidth /6.f;
            MidButtonHeight = mHeight /6.f;

            mUpButtonPath =getPathFromPoints(new float[]{0, 0, mWidth, 0, MidScreenWidth, MidScreenHeight});
            mDownButtonPath =getPathFromPoints(new float[]{mWidth, mHeight, 0, mHeight, MidScreenWidth, MidScreenHeight});
            mRightButtonPath =getPathFromPoints(new float[]{mWidth, 0, mWidth, mHeight, MidScreenWidth, MidScreenHeight});
            mLeftButtonPath =getPathFromPoints(new float[]{0, 0, 0, mHeight, MidScreenWidth, MidScreenHeight});

            mArrowsPath.moveTo(MidScreenWidth, mHeight /10);
            mArrowsPath.lineTo(4* mWidth /10, mHeight /5);
            mArrowsPath.lineTo(4* mWidth /10, mHeight /5);
            mArrowsPath.lineTo(6* mWidth /10, mHeight /5);
            mArrowsPath.lineTo(MidScreenWidth, mHeight /10);

            mArrowsPath.moveTo(MidScreenWidth, 9* mHeight /10);
            mArrowsPath.lineTo(4* mWidth /10, 4* mHeight /5);
            mArrowsPath.lineTo(6* mWidth /10, 4* mHeight /5);
            mArrowsPath.lineTo(MidScreenWidth, 9* mHeight /10);

            mArrowsPath.moveTo(mWidth /10, MidScreenHeight);
            mArrowsPath.lineTo(mWidth /5, 4* mHeight /10);
            mArrowsPath.lineTo(mWidth /5, 6* mHeight /10);
            mArrowsPath.lineTo(mWidth /10, MidScreenHeight);

            mArrowsPath.moveTo(9* mWidth /10, MidScreenHeight);
            mArrowsPath.lineTo(4* mWidth /5, 4* mHeight /10);
            mArrowsPath.lineTo(4* mWidth /5, 6* mHeight /10);
            mArrowsPath.lineTo(9* mWidth /10, MidScreenHeight);
        }
    }

    private Path getPathFromPoints(float[] points)
    {
        Path newPath = new Path();
        newPath.reset();

        int tam=points.length/2;
        newPath.moveTo(points[0], points[1]);

        for(int i=1; i<tam; i++)
            newPath.lineTo(points[i*2], points[i*2+1]);

        newPath.close();
        return newPath;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE)
        {
            setPressedButton(CalculatePressedButton(event.getX(), event.getY()));
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP)
        {
            setPressedButton(ButtonName.None);
        }

        return true;
        // return detectorGestos.onTouchEvent(event);
    }

    private void setPressedButton(ButtonName _button)
    {
        if(_button!= mButtonPressed)
        {
            if(mButtonPressed != ButtonName.None)
                mButtonListener.onButtonHold(mButtonPressed);

            mButtonPressed = _button;

            if(mButtonPressed != ButtonName.None)
                mButtonListener.onButtonPress(mButtonPressed);

            this.invalidate();
        }
    }

    private ButtonName CalculatePressedButton(float newX, float newY)
    {
        if(collidesCenterButton(newX,newY))
            return ButtonName.Center;

        float NormalizedX=(newX- MidScreenWidth)/(MidScreenWidth);
        float NormalizedY=-(newY- MidScreenHeight)/(MidScreenHeight);

        if(NormalizedX>1) NormalizedX=1; else if(NormalizedX<-1) NormalizedX=-1;
        if(NormalizedY>1) NormalizedY=1; else if(NormalizedY<-1) NormalizedY=-1;

        if(mViewType == ViewType.VerticalButtonView)
        {
            if(NormalizedX>0)   return ButtonName.Right;
            else                return ButtonName.Left;
        }else if(mViewType == ViewType.HorizontalButtonView)
        {
            if(NormalizedY>0)   return ButtonName.Up;
            else                return ButtonName.Down;
        }else if(mViewType == ViewType.PadButtonView)
        {
            if(Math.abs(NormalizedX)>Math.abs(NormalizedY))
            {
                if(NormalizedX>0)   return ButtonName.Right;
                else                return ButtonName.Left;
            }else
            {
                if(NormalizedY>0)   return ButtonName.Up;
                else                return ButtonName.Down;
            }
        }

        return ButtonName.None;
    }
}