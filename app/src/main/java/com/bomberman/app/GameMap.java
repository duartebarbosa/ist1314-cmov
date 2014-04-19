package com.bomberman.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;


/**
 * TODO: document your custom view class.
 */
public class GameMap extends View {

    int xCoord;
    int yCoord;
    Paint paint;

    public GameMap(Context context) {
        super(context);
        init(null, 0);


    }

    public GameMap(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public GameMap(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        xCoord = 50;
        yCoord = 50;
        paint = new Paint();


    }




    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        paint.reset();
        paint.setColor(Color.BLUE);
        canvas.drawCircle(xCoord, yCoord, 20, paint);

    }


    public int getXCoord() {
        return xCoord;
    }

    public void setXCoord(int x) {
        this.xCoord = x;
    }

    public int getYCoord() {
        return yCoord;
    }

    public void setYCoord(int y) {
        this.yCoord = y;
    }
}
