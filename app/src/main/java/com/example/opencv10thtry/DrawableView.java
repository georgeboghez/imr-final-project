package com.example.opencv10thtry;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.Nullable;


public class DrawableView extends View {

    private Rect mRectangle;
    private Paint mPaint;
    private int x_n;
    private int y_n;
    private int width_n;
    private int height_n;
    private Context context_n;

    public DrawableView(Context context, int x, int y, int width, int height) {
        super(context);

        // create a rectangle that we'll draw later
        this.mRectangle = new Rect(x, y, x + width, y + height);
        // create the Paint and set its color
        this.mPaint = new Paint();
        this.mPaint.setColor(Color.YELLOW);
        this.x_n = x;
        this.y_n = y;
        this.width_n = width;
        this.height_n = height;
        this.context_n = context;
//        new android.os.Handler().postDelayed(
//                new Runnable() {
//                    public void run() {
//                        height_n += 10;
//                        mRectangle = new Rect(x_n, y_n, width_n, height_n);
//                    }
//                },
//                100);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawRect(this.mRectangle, this.mPaint);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
    }

    public int getX_n() {
        return x_n;
    }

    public int getY_n() {
        return y_n;
    }

    public int getWidth_n() {
        return width_n;
    }

    public int getHeight_n() {
        return height_n;
    }

    public Context getContext_n() {
        return context_n;
    }

    public void setX_n(int x_n) {
        this.x_n = x_n;
    }

    public void setY_n(int y_n) {
        this.y_n = y_n;
    }

    public void setWidth_n(int width_n) {
        this.width_n = width_n;
    }

    public void setHeight_n(int height_n) {
        this.height_n = height_n;
    }
}