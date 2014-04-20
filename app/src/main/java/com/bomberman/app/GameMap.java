package com.bomberman.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.BufferOverflowException;


/**
 * TODO: document your custom view class.
 */
public class GameMap extends View {

    int xCoord;
    int yCoord;
    Paint paint;
    Bitmap originalTilesBitmap;
    Bitmap originalBombermanBitmap;
    Bitmap wallBitmap;
    Bitmap obstacleBitmap;
    Bitmap bombermanBitmap;
    InputStream configFile;
    BufferedReader inn;

    private static final int NUM_ROWS = 12;
    private static final int CELL_SIZE = 16;

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

        xCoord = 0;
        yCoord = 0;
        paint = new Paint();
        originalTilesBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bomberman_tiles_sheet);
        originalBombermanBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bomberman_bomberman_sheet);
        wallBitmap = Bitmap.createBitmap(originalTilesBitmap, 28, 0, 16, originalTilesBitmap.getHeight());
        obstacleBitmap = Bitmap.createBitmap(originalTilesBitmap, 58, 0, 16, originalTilesBitmap.getHeight());
        bombermanBitmap = Bitmap.createBitmap(originalBombermanBitmap, 0, 0, 14, 18);

        configFile = getResources().openRawResource(R.raw.config);
        inn = new BufferedReader(new InputStreamReader(configFile));
        try {
            inn.mark(1000);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }




    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(bombermanBitmap, xCoord, yCoord, paint);

        try {
            inn.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int read;
        for (int y = 0 ; y <= NUM_ROWS/2 ; y++)
            try {
                for (int x = 0 ; (read = inn.read()) != '\n' ; x++) {
                    switch (read) {
                        case 'O':
                            canvas.drawBitmap(obstacleBitmap,x*CELL_SIZE,y*CELL_SIZE,paint);
                            break;
                        case 'W':
                            canvas.drawBitmap(wallBitmap,x*CELL_SIZE,y*CELL_SIZE,paint);
                            break;
                        default:
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

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
