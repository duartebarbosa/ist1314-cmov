package com.bomberman.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
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

    int xCoord, xCoordPrev, yCoord, yCoordPrev; // x = {0:NUM_COLUMNS} and y = {0:NUM_ROWS}
    Direction bombermanDirection;
    Paint paint;
    Bitmap originalTilesBitmap;
    Bitmap originalBombermanBitmap;
    Bitmap originalEnemiesBitmap;
    Bitmap originalBombBitmap;
    Bitmap wallBitmap;
    Bitmap obstacleBitmap;
    Bitmap bombermanDownBitmap;
    Bitmap bombermanLeftBitmap;
    Bitmap bombermanTopBitmap;
    Bitmap bombermanRightBitmap;
    Bitmap bombBitmap;
    Bitmap explosionCenterBitmap;
    Bitmap explosionHorizontalBitmap;
    Bitmap explosionVerticalBitmap;
    Bitmap explosionLeftBitmap;
    Bitmap explosionRightBitmap;
    Bitmap explosionTopBitmap;
    Bitmap explosionBottomBitmap;
    Bitmap enemyBitmap;

    InputStream configFile;
    BufferedReader inn;
    String levelName;
    int gameDuration, explosionTimeout, explosionDuration, explosionRange,
            robotSpeed, pointsPerRobotKilled, pointsPerOpponentKilled;


    public enum Direction {
        DOWN,
        LEFT,
        TOP,
        RIGHT
    }

    int[][] map;    // keep map config internally

    // these will depend on the config file info... => not constants
    private static final int NUM_ROWS = 13;
    private static final int NUM_COLUMNS = 19;
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

        bombermanDirection = Direction.DOWN;
        paint = new Paint();
        originalTilesBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bomberman_tiles_sheet);
        originalBombermanBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bomberman_bomberman_sheet);
        originalEnemiesBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bomberman_enemies_sheet);
        originalBombBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bomberman_bomb_sheet);
        wallBitmap = Bitmap.createBitmap(originalTilesBitmap, 28, 0, 16, originalTilesBitmap.getHeight());
        obstacleBitmap = Bitmap.createBitmap(originalTilesBitmap, 58, 0, 16, originalTilesBitmap.getHeight());
        bombermanDownBitmap = Bitmap.createBitmap(originalBombermanBitmap, 0, 0, 14, 18);
        bombermanLeftBitmap = Bitmap.createBitmap(originalBombermanBitmap, 30, 0, 14, 18);
        bombermanTopBitmap = Bitmap.createBitmap(originalBombermanBitmap, 60, 0, 14, 18);
        bombermanRightBitmap = Bitmap.createBitmap(originalBombermanBitmap, 90, 0, 14, 18);
        bombBitmap = Bitmap.createBitmap(originalBombBitmap, 0, 0, 16, 16);
        explosionCenterBitmap = Bitmap.createBitmap(originalBombBitmap, 150, 30, 16, 16);
        explosionHorizontalBitmap = Bitmap.createBitmap(originalBombBitmap, 180, 60, 16, 16);
        explosionVerticalBitmap = Bitmap.createBitmap(originalBombBitmap, 180, 0, 16, 16);
        explosionLeftBitmap = Bitmap.createBitmap(originalBombBitmap, 120, 30, 16, 16);
        explosionRightBitmap = Bitmap.createBitmap(originalBombBitmap, 180, 30, 16, 16);
        explosionTopBitmap = Bitmap.createBitmap(originalBombBitmap, 150, 0, 16, 16);
        explosionBottomBitmap = Bitmap.createBitmap(originalBombBitmap, 150, 60, 16, 16);
        enemyBitmap = Bitmap.createBitmap(originalEnemiesBitmap, 0, 0, 16, 16);

        configFile = getResources().openRawResource(R.raw.config);
        inn = new BufferedReader(new InputStreamReader(configFile));

        try {
            levelName = inn.readLine();
            gameDuration = Integer.parseInt(inn.readLine());
            explosionTimeout = Integer.parseInt(inn.readLine());
            explosionDuration = Integer.parseInt(inn.readLine());
            explosionRange = Integer.parseInt(inn.readLine());
            robotSpeed = Integer.parseInt(inn.readLine());
            pointsPerRobotKilled = Integer.parseInt(inn.readLine());
            pointsPerOpponentKilled = Integer.parseInt(inn.readLine());

            map = new int[NUM_COLUMNS][NUM_ROWS]; // [x][y]

            //int read;
            for (int y = 0 ; y < NUM_ROWS ; y++) {
                for (int x = 0; x < NUM_COLUMNS /*(read = inn.read()) != '\n'*/ ; x++) {
                    map[x][y] = inn.read();
                    if (map[x][y] == '1') {// player 1 (us?) initial pos
                        xCoord = xCoordPrev = x;
                        yCoord = yCoordPrev = y;
                    }
                }
                inn.read(); // ignore Windows line-separator (2 chars)
                inn.read(); // (config.txt was generated in Windows)
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }




    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        for (int y = 0 ; y < NUM_ROWS ; y++) {
            for (int x = 0; x < NUM_COLUMNS; x++) {
                paint.setColor(getResources().getColor(android.R.color.holo_green_dark));
                canvas.drawRect(x * CELL_SIZE, y * CELL_SIZE, x * CELL_SIZE + CELL_SIZE, y * CELL_SIZE + CELL_SIZE, paint);
                paint.reset();
                switch (map[x][y]) {
                    case 'O':
                        canvas.drawBitmap(obstacleBitmap, x * CELL_SIZE, y * CELL_SIZE, paint);
                        break;
                    case 'W':
                        canvas.drawBitmap(wallBitmap, x * CELL_SIZE, y * CELL_SIZE, paint);
                        break;
                    case 'R':
                        canvas.drawBitmap(enemyBitmap, x * CELL_SIZE, y * CELL_SIZE, paint);
                        break;
                    case 'B':
                        canvas.drawBitmap(bombBitmap, x * CELL_SIZE, y * CELL_SIZE, paint);
                        break;
                    case 'E':
                        int i;
                        for(i=1 ; i<explosionRange ; i++){
                            canvas.drawBitmap(explosionHorizontalBitmap, (x-i) * CELL_SIZE, y * CELL_SIZE, paint);
                            canvas.drawBitmap(explosionHorizontalBitmap, (x+i) * CELL_SIZE, y * CELL_SIZE, paint);
                            canvas.drawBitmap(explosionVerticalBitmap, x * CELL_SIZE, (y-i) * CELL_SIZE, paint);
                            canvas.drawBitmap(explosionVerticalBitmap, x * CELL_SIZE, (y+i) * CELL_SIZE, paint);

                        }
                        canvas.drawBitmap(explosionTopBitmap, x * CELL_SIZE, (y-i) * CELL_SIZE, paint);
                        canvas.drawBitmap(explosionLeftBitmap, (x-i) * CELL_SIZE, y * CELL_SIZE, paint);
                        canvas.drawBitmap(explosionRightBitmap, (x+i) * CELL_SIZE, y * CELL_SIZE, paint);
                        canvas.drawBitmap(explosionBottomBitmap, x * CELL_SIZE, (y+i) * CELL_SIZE, paint);
                        canvas.drawBitmap(explosionCenterBitmap, x * CELL_SIZE, y * CELL_SIZE, paint);
                        break;
                    default:
                        break;
                }
            }
        }

        if(map[xCoord][yCoord] == 'O' || map[xCoord][yCoord] == 'W') { // invalid movement, rewind
            xCoord = xCoordPrev;
            yCoord = yCoordPrev;
        }

        switch (bombermanDirection) {
            case DOWN:
                canvas.drawBitmap(bombermanDownBitmap, xCoord*CELL_SIZE, yCoord*CELL_SIZE, paint);
                break;
            case LEFT:
                canvas.drawBitmap(bombermanLeftBitmap, xCoord*CELL_SIZE, yCoord*CELL_SIZE, paint);
                break;
            case TOP:
                canvas.drawBitmap(bombermanTopBitmap, xCoord*CELL_SIZE, yCoord*CELL_SIZE, paint);
                break;
            case RIGHT:
                canvas.drawBitmap(bombermanRightBitmap, xCoord*CELL_SIZE, yCoord*CELL_SIZE, paint);
                break;
            default:
                break;
        }

    }


    // Named get*Coord and set*Coord so we don't override superclass's get* and set* methods
    public int getXCoord() {
        return xCoord;
    }

    public void setXCoord(int x) {
        this.xCoordPrev = this.xCoord;
        this.xCoord = x;
    }

    public int getYCoord() {
        return yCoord;
    }

    public void setYCoord(int y) {
        this.yCoordPrev = this.yCoord;
        this.yCoord = y;
    }

    public Direction getBombermanDirection() {
        return bombermanDirection;
    }

    public void setBombermanDirection(Direction bombermanDirection) {
        this.bombermanDirection = bombermanDirection;
    }

    public void addBomb(final int x, final int y){
        map[x][y] = 'B';

        Handler handler = new Handler();

        Runnable startExplosion = new Runnable() {
            @Override
            public void run() {
                map[x][y] = 'E';
                invalidate();
            }
        };
        Runnable endExplosion = new Runnable() {
            @Override
            public void run() {
                map[x][y] = '-';
                invalidate();
            }
        };

        handler.postDelayed(startExplosion, explosionTimeout*1000);
        handler.postDelayed(endExplosion, explosionTimeout*1000 + explosionDuration*1000);

    }
}
