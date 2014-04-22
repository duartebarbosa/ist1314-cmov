package com.bomberman.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;


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
    private final Bitmap originalBombermanBitmap = initFactory(R.drawable.bomberman_bomberman_sheet); //not being used atm.
    private final Bitmap originalEnemiesBitmap = initFactory(R.drawable.bomberman_enemies_sheet);
    private final Bitmap originalBombBitmap = initFactory(R.drawable.bomberman_bomb_sheet);

    //gotta love magic numbers..!
    private final Bitmap wallBitmap = Bitmap.createBitmap(originalTilesBitmap, 28, 0, 16, originalTilesBitmap.getHeight());
    private final Bitmap obstacleBitmap = Bitmap.createBitmap(originalTilesBitmap, 58, 0, 16, originalTilesBitmap.getHeight());
    private final Bitmap enemyBitmap = Bitmap.createBitmap(originalEnemiesBitmap, 0, 0, 16, 16);

    private final Bitmap bombermanDownBitmap = createBombermanBitmap(0, 0, 14, 18);
    private final Bitmap bombermanLeftBitmap = createBombermanBitmap(30, 0, 14, 18);
    private final Bitmap bombermanTopBitmap = createBombermanBitmap(60, 0, 14, 18);
    private final Bitmap bombermanRightBitmap = createBombermanBitmap(90, 0, 14, 18);

    private final Bitmap bombBitmap = createBombBitmap(0, 0, 16, 16);
    private final Bitmap explosionCenterBitmap = createBombBitmap(150, 30, 16, 16);
    private final Bitmap explosionHorizontalBitmap = createBombBitmap(180, 60, 16, 16);
    private final Bitmap explosionVerticalBitmap = createBombBitmap(180, 0, 16, 16);
    private final Bitmap explosionLeftBitmap = createBombBitmap(120, 30, 16, 16);
    private final Bitmap explosionRightBitmap = createBombBitmap(180, 30, 16, 16);
    private final Bitmap explosionTopBitmap = createBombBitmap(150, 0, 16, 16);
    private final Bitmap explosionBottomBitmap = createBombBitmap(150, 60, 16, 16);

    private int xPlayerCoord, xPlayerCoordPrev, yPlayerCoord, yPlayerCoordPrev, xPlayerInitialCoord, yPlayerInitialCoord;
    private int playerScore = 0;

    private String levelName;
    private int gameDuration, explosionTimeout, explosionDuration, explosionRange,
            robotSpeed, pointsPerRobotKilled, pointsPerOpponentKilled;

    private int[][] map;    // keep map config internally

    private static int NUM_ROWS;
    private static int NUM_COLUMNS;
    private static final int CELL_SIZE = 16;


    Random randomGenerator = new Random();


    private Bitmap initFactory(int id){
        return BitmapFactory.decodeResource(getResources(), id);
    }

    private Bitmap createBombermanBitmap(int a, int b, int c, int d){
        return Bitmap.createBitmap(originalBombermanBitmap, a, b, c, d);
    }

    private Bitmap createBombBitmap(int a, int b, int c, int d){
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
                        xPlayerCoord = xPlayerCoordPrev = xPlayerInitialCoord = x;
                        yPlayerCoord = yPlayerCoordPrev = yPlayerInitialCoord = y;
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
                int y_float = y * CELL_SIZE;
                paint.setColor(getResources().getColor(android.R.color.holo_green_dark));
                paint.setAlpha(60);
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
                        boolean stopExplosionSouth,stopExplosionNorth,stopExplosionWest,stopExplosionEast;
                        stopExplosionEast=stopExplosionNorth=stopExplosionSouth=stopExplosionWest=false;
                        for(; i < explosionRange; i++){
                            if(y-i >= 0)
                                if(map[x][y-i] != 'W' && !stopExplosionWest)
                                    canvas.drawBitmap(explosionHorizontalBitmap, (y-i) * CELL_SIZE, x_float, paint);
                                else
                                    stopExplosionWest = true;
                            if(y+i < NUM_COLUMNS)
                                if(map[x][y+i] != 'W' && !stopExplosionEast)
                                    canvas.drawBitmap(explosionHorizontalBitmap, (y+i) * CELL_SIZE, x_float, paint);
                                else
                                    stopExplosionEast = true;
                            if(x-i >= 0)
                                if(map[x-i][y] != 'W' && !stopExplosionNorth)
                                    canvas.drawBitmap(explosionVerticalBitmap, y_float, (x-i) * CELL_SIZE, paint);
                                else
                                    stopExplosionNorth = true;
                            if(x+i < NUM_ROWS)
                                if(map[x+i][y] != 'W' && !stopExplosionSouth)
                                    canvas.drawBitmap(explosionVerticalBitmap, y_float, (x+i) * CELL_SIZE, paint);
                                else
                                    stopExplosionSouth = true;
                        }
                        if(x-i >= 0)
                            if(map[x-i][y] != 'W' && !stopExplosionNorth)
                                canvas.drawBitmap(explosionTopBitmap, y_float, (x-i) * CELL_SIZE, paint);
                        if(y-i >= 0)
                            if(map[x][y-i] != 'W' && !stopExplosionWest)
                                canvas.drawBitmap(explosionLeftBitmap, (y-i) * CELL_SIZE, x_float, paint);
                        if(y+i < NUM_COLUMNS)
                            if(map[x][y+i] != 'W' && !stopExplosionEast)
                                canvas.drawBitmap(explosionRightBitmap, (y+i) * CELL_SIZE, x_float, paint);
                        if(x+i < NUM_ROWS)
                            if(map[x+i][y] != 'W' && !stopExplosionSouth)
                                canvas.drawBitmap(explosionBottomBitmap, y_float, (x+i) * CELL_SIZE, paint);
                        canvas.drawBitmap(explosionCenterBitmap, y_float, x_float, paint);

                        destroy('O',x,y,canvas);
                        destroy('R', x, y, canvas);
                        killPlayer(x, y, canvas);
                        break;
                    default:
                        break;
                }
            }
        }

        if(map[xPlayerCoord][yPlayerCoord] == 'O' || map[xPlayerCoord][yPlayerCoord] == 'W') { // invalid movement, rewind
            xPlayerCoord = xPlayerCoordPrev;
            yPlayerCoord = yPlayerCoordPrev;
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
        killPlayer(xPlayerCoord,yPlayerCoord, canvas);


        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(20);
        canvas.drawText("Score: "+Integer.toString(playerScore), 0,250,paint);
        paint.reset();

    }


    private void drawBomberman(Canvas canvas, Bitmap id){
        canvas.drawBitmap(id, yPlayerCoord *CELL_SIZE, xPlayerCoord *CELL_SIZE, paint);
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
                //killPlayer(x, y);
                invalidate();
            }
        };

        handler.postDelayed(startExplosion, explosionTimeout*1000);
        handler.postDelayed(endExplosion, explosionTimeout*1000 + explosionDuration*1000);

    }

    // can we kill ourselves or only other players?
    private void killPlayer(int x, int y, Canvas canvas){

        // collision with explosion
        for(int i = 1 ; i <= explosionRange; i++) {
            if ((xPlayerCoord == x - i && yPlayerCoord == y) ||
                    (xPlayerCoord == x + i && yPlayerCoord == y) ||
                    (xPlayerCoord == x && yPlayerCoord == y - i) ||
                    (xPlayerCoord == x && yPlayerCoord == y + i)) {
                xPlayerCoord = xPlayerInitialCoord;
                yPlayerCoord = yPlayerInitialCoord;
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(20);
                canvas.drawText("ja foste, noob", getWidth() / 2, 250, paint);
                paint.reset();

            }
        }

        // collision with robots
        if (((y-1 >= 0) && (map[x][y-1] == 'R')) ||
            ((y+1 < NUM_COLUMNS) && (map[x][y+1] == 'R')) ||
            ((x-1 >= 0) && (map[x-1][y] == 'R')) ||
            ((x+1 < NUM_ROWS) && (map[x+1][y] == 'R'))) {
            xPlayerCoord = xPlayerInitialCoord;
            yPlayerCoord = yPlayerInitialCoord;
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(20);
            canvas.drawText("ja foste, noob", getWidth() / 2, 250, paint);
            paint.reset();

        }
    }

    private void destroy(char object, int x, int y, Canvas canvas) {
        boolean stopDestroyingSouth,stopDestroyingNorth,stopDestroyingWest,stopDestroyingEast;
        stopDestroyingEast=stopDestroyingNorth=stopDestroyingSouth=stopDestroyingWest=false;
        for(int i = 1 ; i <= explosionRange; i++) {
            if(x-i >= 0)
                if (map[x-i][y] != 'W' && !stopDestroyingNorth && map[x-i][y] == object) {
                    map[x - i][y] = '-';
                    if (object == 'R')
                        playerScore += pointsPerRobotKilled;
                } else
                    stopDestroyingNorth = true;
            Log.d("GameMap.Java", "x="+x+"    i="+i+"   NUM_COLUMNS="+NUM_COLUMNS+"      NUM_ROWS="+NUM_ROWS);
            if(x+i < NUM_ROWS)
                if (map[x+i][y] != 'W' && !stopDestroyingSouth && map[x+i][y] == object){
                    map[x+i][y] = '-';
                    if (object == 'R')
                        playerScore += pointsPerRobotKilled;
                } else
                    stopDestroyingSouth = true;
            if(y-i >= 0)
                if (map[x][y-i] != 'W' && !stopDestroyingEast && map[x][y-i] == object) {
                    map[x][y-i] = '-';
                    if (object == 'R')
                        playerScore += pointsPerRobotKilled;
                } else
                    stopDestroyingEast = true;
            if(y+i < NUM_COLUMNS)
                if (map[x][y+i] != 'W' && !stopDestroyingWest && map[x][y+i] == object){
                    map[x][y+i] = '-';
                    if (object == 'R')
                        playerScore += pointsPerRobotKilled;
                } else
                    stopDestroyingWest = true;

        }
    }

    private Boolean playerNear(int x, int y){
        return (map[x][y] >= 1 || map[x][y] <= 3)? true : false;
    }

    // invalid movement, rewind
    private Boolean obstacleNear(int x, int y){
        return (map[x][y] == 'O' || map[x][y] == 'W')? true : false;
    }

    private void swapCells(int x, int y, int x_new, int y_new){
        //no check is done. use carefully.
        map[x_new][y_new] = map[x][y];
        map[x][y] = '-';
    }

    private Boolean findPlayer(int x, int y){
        //top;
        if(playerNear(x-1, y)) {
            //killPlayer();
            swapCells(x, y, x-1, y);
            return true;
        }
        //right;
        if(playerNear(x, y+1)) {
            //killPlayer();
            swapCells(x, y, x, y+1);
            return true;
        }
        //down;
        if(playerNear(x+1, y)) {
            //killPlayer();
            swapCells(x, y, x+1, y);
            return true;
        }
        //left;
        if(playerNear(x, y-1)) {
            //killPlayer();
            swapCells(x, y, x, y-1);
            return true;
        }
        return false;
    }

    private void findEmptyCell(int x, int y){
        search:
        while(true) {
            int randomInt = randomGenerator.nextInt(4);

            switch (randomInt) {
                case 0: //top
                    if (obstacleNear(x-1,y)) break;
                    swapCells(x, y, x-1, y);
                    break search;
                case 1: //right
                    if (obstacleNear(x,y+1)) break;
                    swapCells(x, y, x, y+1);
                    break search;
                case 2: //down
                    if (obstacleNear(x+1,y)) break;
                    swapCells(x, y, x+1, y);
                    break search;
                case 3: //left
                    if (obstacleNear(x,y-1)) break;
                    swapCells(x, y, x, y-1);
                    break search;
                default: //fuck.
                    break;
            }
        }
    }

    public void moveRobots(int x, int y){
        //not so random. kill the bastards first if they are anywhere near.
        if(findPlayer(x, y)) return;

        //ok, they left the building earlier. just find a suitable place to rest during the night.
        findEmptyCell(x, y);
    }

    // Named get*Coord and set*Coord so we don't override superclass's get* and set* methods
    public int getXCoord() {
        return xPlayerCoord;
    }

    public void setXCoord(int x) {
        this.xPlayerCoordPrev = this.xPlayerCoord;
        this.xPlayerCoord = x;
    }

    public int getYCoord() {
        return yPlayerCoord;
    }

    public void setYCoord(int y) {
        this.yPlayerCoordPrev = this.yPlayerCoord;
        this.yPlayerCoord = y;
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
