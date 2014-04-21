package com.bomberman.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * TODO: document your custom view class.
 */
public class GameMap extends View {

    public enum Direction {
        DOWN,
        LEFT,
        TOP,
        RIGHT
    }

    private Direction bombermanDirection = Direction.DOWN;
    private Paint paint = new Paint();

    private final Bitmap originalTilesBitmap = initFactory(R.drawable.bomberman_tiles_sheet);
    private final Bitmap originalBombermanBitmap = initFactory(R.drawable.bomberman_bomberman_sheet); //not being used..
    private final Bitmap originalEnemiesBitmap = initFactory(R.drawable.bomberman_enemies_sheet);
    private final Bitmap originalBombBitmap = initFactory(R.drawable.bomberman_bomb_sheet);

    private final Bitmap wallBitmap = Bitmap.createBitmap(originalTilesBitmap, 28, 0, 16, originalTilesBitmap.getHeight());
    private final Bitmap obstacleBitmap = Bitmap.createBitmap(originalTilesBitmap, 58, 0, 16, originalTilesBitmap.getHeight());
    private final Bitmap enemyBitmap = Bitmap.createBitmap(originalEnemiesBitmap, 0, 0, 16, 16);

    private final Bitmap bombermanDownBitmap = createBitmap(0, 0, 14, 18);
    private final Bitmap bombermanLeftBitmap = createBitmap(30, 0, 14, 18);
    private final Bitmap bombermanTopBitmap = createBitmap(60, 0, 14, 18);
    private final Bitmap bombermanRightBitmap = createBitmap(90, 0, 14, 18);
    private final Bitmap bombBitmap = createBitmap(0, 0, 16, 16);
    private final Bitmap explosionCenterBitmap = createBitmap(150, 30, 16, 16);
    private final Bitmap explosionHorizontalBitmap = createBitmap(180, 60, 16, 16);
    private final Bitmap explosionVerticalBitmap = createBitmap(180, 0, 16, 16);
    private final Bitmap explosionLeftBitmap = createBitmap(120, 30, 16, 16);
    private final Bitmap explosionRightBitmap = createBitmap(180, 30, 16, 16);
    private final Bitmap explosionTopBitmap = createBitmap(150, 0, 16, 16);
    private final Bitmap explosionBottomBitmap = createBitmap(150, 60, 16, 16);

    private int xCoord, xCoordPrev, yCoord, yCoordPrev; // x = {0:NUM_ROWS} and y = {0:NUM_COLUMNS}

    private String levelName;
    private int gameDuration, explosionTimeout, explosionDuration, explosionRange,
            robotSpeed, pointsPerRobotKilled, pointsPerOpponentKilled;

    private int[][] map;    // keep map config internally

    private static int NUM_ROWS;
    private static int NUM_COLUMNS;
    private static final int CELL_SIZE = 16;

    private Bitmap initFactory(int id){
        return BitmapFactory.decodeResource(getResources(), id);
    }

    private Bitmap createBitmap(int a, int b, int c, int d){
        return Bitmap.createBitmap(originalBombBitmap, a, b, c, d);
    }

    private void init(AttributeSet attrs, int defStyle) {

        InputStream configFile = getResources().openRawResource(R.raw.config);
        BufferedReader input = new BufferedReader(new InputStreamReader(configFile));

        try {
            levelName = input.readLine();
            gameDuration = Integer.parseInt(input.readLine());
            explosionTimeout = Integer.parseInt(input.readLine());
            explosionDuration = Integer.parseInt(input.readLine());
            explosionRange = Integer.parseInt(input.readLine());
            robotSpeed = Integer.parseInt(input.readLine());
            pointsPerRobotKilled = Integer.parseInt(input.readLine());
            pointsPerOpponentKilled = Integer.parseInt(input.readLine());

            input.mark(10000);
            NUM_COLUMNS = input.readLine().length();
            for (NUM_ROWS = 1; input.readLine() != null; NUM_ROWS++);
            map = new int[NUM_ROWS][NUM_COLUMNS]; // [y][x]

            input.reset();
            String line;
            for(int x = 0; (line = input.readLine()) != null; x++){
                char[] row = line.toCharArray();
                for(int y = 0; y < NUM_COLUMNS; y++){
                    map[x][y] = row[y];
                    if (map[x][y] == '1') {// player 1 (us?) initial pos
                        xCoord = xCoordPrev = x;
                        yCoord = yCoordPrev = y;
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //idealmente - à excepção da bomba - isto só se faz uma vez..
        for (int x = 0; x < NUM_ROWS; x++) {
            int x_float = x * CELL_SIZE;
            for (int y = 0 ; y < NUM_COLUMNS; y++) {
                paint.setColor(getResources().getColor(android.R.color.holo_green_dark));
                int y_float = y * CELL_SIZE;
                canvas.drawRect(y_float, x_float, y_float + CELL_SIZE, x_float + CELL_SIZE, paint);
                paint.reset();
                switch (map[x][y]) {
                    case 'O':
                        canvas.drawBitmap(obstacleBitmap, y_float, x_float, paint);
                        break;
                    case 'W':
                        canvas.drawBitmap(wallBitmap, y_float, x_float, paint);
                        break;
                    case 'R':
                        canvas.drawBitmap(enemyBitmap, y_float, x_float, paint);
                        break;
                    case 'B':
                        canvas.drawBitmap(bombBitmap, y_float, x_float, paint);
                        break;
                    case 'E':
                        //TODO: não atropelar paredes.
                        int i = 1;
                        for(; i < explosionRange; i++){
                            canvas.drawBitmap(explosionHorizontalBitmap, (y-i) * CELL_SIZE, x_float, paint);
                            canvas.drawBitmap(explosionHorizontalBitmap, (y+i) * CELL_SIZE, x_float, paint);
                            canvas.drawBitmap(explosionVerticalBitmap, y_float, (x-i) * CELL_SIZE, paint);
                            canvas.drawBitmap(explosionVerticalBitmap, y_float, (x+i) * CELL_SIZE, paint);
                        }

                        canvas.drawBitmap(explosionTopBitmap, y_float, (x-i) * CELL_SIZE, paint);
                        canvas.drawBitmap(explosionLeftBitmap, (y-i) * CELL_SIZE, x_float, paint);
                        canvas.drawBitmap(explosionRightBitmap, (y+i) * CELL_SIZE, x_float, paint);
                        canvas.drawBitmap(explosionBottomBitmap, y_float, (x+i) * CELL_SIZE, paint);
                        canvas.drawBitmap(explosionCenterBitmap, y_float, x_float, paint);
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
                drawBomberman(canvas, bombermanDownBitmap);
                break;
            case LEFT:
                drawBomberman(canvas, bombermanLeftBitmap);
                break;
            case TOP:
                drawBomberman(canvas, bombermanTopBitmap);
                break;
            case RIGHT:
                drawBomberman(canvas, bombermanRightBitmap);
                break;
            default:
                break;
        }

    }

    private void drawBomberman(Canvas canvas, Bitmap id){
        canvas.drawBitmap(id, yCoord*CELL_SIZE, xCoord*CELL_SIZE, paint);
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
                kill(x, y);
                invalidate();
            }
        };

        handler.postDelayed(startExplosion, explosionTimeout*1000);
        handler.postDelayed(endExplosion, explosionTimeout*1000 + explosionDuration*1000);

    }

    private void kill(int x, int y){
        //kill everyone in the target radius; destroy walls
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

    //constructors
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

}
