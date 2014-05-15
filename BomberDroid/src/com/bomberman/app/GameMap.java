package com.bomberman.app;

import android.app.Activity;
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
import android.widget.TextView;

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
    
    // bitmask to represent who's on a certain cell
    // (those who can be on top of each other):
    //             Explosion Wall Obstacle Bomb  Robot P4   P3    P2    P1
    // bitmask:  ...   0      0      0     0     0     0     0     0     0
    // bit no.   ...   8      7      6     5     4     3     2     1     0(LSB)
    // inspired by http://www.ugrad.cs.ubc.ca/~cs490/sec202/notes/intro/bitmask.pdf
    private int cellBitmask;

    private GameScreen mainGameScreen;
    private GameDataManager gdm;

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
    private final Bitmap explosionLeftBitmap = createBombBitmap(118, 30, 16, 16);
    private final Bitmap explosionRightBitmap = createBombBitmap(181, 30, 16, 16);
    private final Bitmap explosionTopBitmap = createBombBitmap(150, 0, 16, 16);
    private final Bitmap explosionBottomBitmap = createBombBitmap(150, 62, 16, 14);

    private int xPlayerCoord, xPlayerCoordPrev, yPlayerCoord, yPlayerCoordPrev, xPlayerInitialCoord, yPlayerInitialCoord;
    private int playerScore = 0;
    private int numberOfPlayers = 1; // TODO
    private int timeLeft = 0; // TODO
    private String playerName = "Player1"; // TODO

    private String levelName;
    private float gameDuration, explosionTimeout, explosionDuration, explosionRange,
            robotSpeed, pointsPerRobotKilled, pointsPerOpponentKilled;

    private int[][] map;    // keep map config internally

    private static int NUM_ROWS;
    private static int NUM_COLUMNS;
    private static final int CELL_SIZE = 16;

    Handler handler = new Handler();
    // create a new class so we can pass arguments to the Runnable
    public class MoveRobotsRunnable implements Runnable {
        int x,y;
        public MoveRobotsRunnable(int _x, int _y) {
            this.x = _x;
            this.y = _y;
        }

        public void run() {
        	// make sure the robot is still at this position - it could've been killed
        	// between last execution of the Runnable and this one
        	if((map[x][y] & (1<<4)) != 0) {	
        		Coord newCoord = moveRobots(x,y);
	            x = newCoord.x;
	            y = newCoord.y;
	            //killPlayer(x, y);
	            invalidate();
	            handler.postDelayed(this, (long) (1000/robotSpeed));
        	}
        }
    }

    public class Coord {
        int x,y;
        public Coord(int _x, int _y) {
            this.x = _x;
            this.y = _y;
        }

    }

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
        mainGameScreen = null;
        InputStream configFile = getResources().openRawResource(R.raw.config2);
        BufferedReader input = new BufferedReader(new InputStreamReader(configFile));

        setGdm(new GameDataManager());
        
        try {
            levelName = input.readLine();
            gameDuration = Float.parseFloat(input.readLine());
            explosionTimeout = Float.parseFloat(input.readLine());
            explosionDuration = Float.parseFloat(input.readLine());
            explosionRange = Float.parseFloat(input.readLine());
            robotSpeed = Float.parseFloat(input.readLine());
            pointsPerRobotKilled = Float.parseFloat(input.readLine());
            pointsPerOpponentKilled = Float.parseFloat(input.readLine());

            input.mark(10000);
            NUM_COLUMNS = input.readLine().length();
            for (NUM_ROWS = 1; input.readLine() != null; NUM_ROWS++);
            map = new int[NUM_ROWS][NUM_COLUMNS]; // [y][x]

            input.reset();
            String line;
            for(int x = 0; (line = input.readLine()) != null; x++){
                char[] row = line.toCharArray();
                for(int y = 0; y < NUM_COLUMNS; y++){
                	
                	//map[x][y] = row[y];
                	
                	if(row[y] == '1') {
                		xPlayerCoord = xPlayerCoordPrev = xPlayerInitialCoord = x;
                        yPlayerCoord = yPlayerCoordPrev = yPlayerInitialCoord = y;
                        map[x][y] = (1 << 0);
                        getGdm().newPlayer(x,y);
                	} else if(row[y] == '2') {
                		map[x][y] = (1 << 1);
                		getGdm().newPlayer(x,y);
                	} else if(row[y] == '3') {
                		map[x][y] = (1 << 2);
                		getGdm().newPlayer(x,y);
                	} else if(row[y] == '4') {
                		map[x][y] = (1 << 3);
                		getGdm().newPlayer(x,y);
                	} else if(row[y] == 'R') {
                		map[x][y] = (1 << 4);
                		// create one Runnable for each robot
                		MoveRobotsRunnable runnable = new MoveRobotsRunnable(x,y);
                        handler.postDelayed(runnable, (long) (1000/robotSpeed));
                	} else if(row[y] == 'O')
                		map[x][y] = (1 << 6);
                	else if(row[y] == 'W')
                		map[x][y] = (1 << 7);
                	else if(row[y] == '-')	// and replace '-' for 0 so it doesn't interfere with the bitmask
                		map[x][y] = 0;
                	
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mainGameScreen.updateMap();
        mainGameScreen.updateScore(playerScore);
        mainGameScreen.updateTimeLeft(timeLeft);
        
        // desenhar fundo antes de tudo o resto - evita que explosoes sejam
        // desenhadas (na itera�ao x,y) antes do fundo que vai ser desenhado
        // em itera�oes seguintes (x+i e y+i)
        for (int x = 0; x < NUM_ROWS; x++) {
            int x_float = x * CELL_SIZE;
            for (int y = 0 ; y < NUM_COLUMNS; y++) {
                int y_float = y * CELL_SIZE;
                paint.setColor(getResources().getColor(android.R.color.holo_green_dark));
                canvas.drawRect(y_float, x_float, y_float + CELL_SIZE, x_float + CELL_SIZE, paint);
                paint.reset();
            }
        }
        
        //idealmente - à excepção da bomba - isto só se faz uma vez..
        for (int x = 0; x < NUM_ROWS; x++) {
            int x_float = x * CELL_SIZE;
            for (int y = 0 ; y < NUM_COLUMNS; y++) {
                int y_float = y * CELL_SIZE;
                
	            if((map[x][y] & (1 << 4)) != 0)
	                canvas.drawBitmap(enemyBitmap, y_float, x_float, paint);
	            
	            if((map[x][y] & (1 << 5)) != 0)
	            	canvas.drawBitmap(bombBitmap, y_float, x_float, paint);
                
	            if((map[x][y] & (1 << 6)) != 0)
                	canvas.drawBitmap(obstacleBitmap, y_float, x_float, paint);
                
                if((map[x][y] & (1 << 7)) != 0)
                	canvas.drawBitmap(wallBitmap, y_float, x_float, paint);
                
                if((map[x][y] & (1 << 8)) != 0) {
                        int i = 1;
                        boolean stopExplosionSouth,stopExplosionNorth,stopExplosionWest,stopExplosionEast;
                        stopExplosionEast=stopExplosionNorth=stopExplosionSouth=stopExplosionWest=false;
                        for(; i < explosionRange; i++){
                            if(y-i >= 0)
                                if((map[x][y-i] & (1<<7)) == 0 && !stopExplosionWest)
                                    canvas.drawBitmap(explosionHorizontalBitmap, (y-i) * CELL_SIZE, x_float, paint);
                                else
                                    stopExplosionWest = true;
                            if(y+i < NUM_COLUMNS)
                                if((map[x][y+i] & (1<<7)) == 0 && !stopExplosionEast)
                                    canvas.drawBitmap(explosionHorizontalBitmap, (y+i) * CELL_SIZE, x_float, paint);
                                else
                                    stopExplosionEast = true;
                            if(x-i >= 0)
                                if((map[x-i][y] & (1<<7)) == 0 && !stopExplosionNorth)
                                    canvas.drawBitmap(explosionVerticalBitmap, y_float, (x-i) * CELL_SIZE, paint);
                                else
                                    stopExplosionNorth = true;
                            if(x+i < NUM_ROWS)
                                if((map[x+i][y] & (1<<7)) == 0 && !stopExplosionSouth)
                                    canvas.drawBitmap(explosionVerticalBitmap, y_float, (x+i) * CELL_SIZE, paint);
                                else
                                    stopExplosionSouth = true;
                        }
                        if(x-i >= 0)
                            if((map[x-i][y] & (1<<7)) == 0 && !stopExplosionNorth)
                                canvas.drawBitmap(explosionTopBitmap, y_float, (x-i) * CELL_SIZE, paint);
                        if(y-i >= 0)
                            if((map[x][y-i] & (1<<7)) == 0 && !stopExplosionWest)
                                canvas.drawBitmap(explosionLeftBitmap, (y-i) * CELL_SIZE, x_float, paint);
                        if(y+i < NUM_COLUMNS)
                            if((map[x][y+i] & (1<<7)) == 0 && !stopExplosionEast)
                                canvas.drawBitmap(explosionRightBitmap, (y+i) * CELL_SIZE, x_float, paint);
                        if(x+i < NUM_ROWS)
                            if((map[x+i][y] & (1<<7)) == 0 && !stopExplosionSouth)
                                canvas.drawBitmap(explosionBottomBitmap, y_float, (x+i) * CELL_SIZE, paint);
                        canvas.drawBitmap(explosionCenterBitmap, y_float, x_float, paint);

                        destroy((1<<6) ,x, y, canvas);
                        destroy((1<<4), x, y, canvas);
                        killPlayer(x, y, canvas);
                }

            }
        }

        if((map[xPlayerCoord][yPlayerCoord] & (1<<6)) != 0 || (map[xPlayerCoord][yPlayerCoord] & (1<<7)) != 0 || (map[xPlayerCoord][yPlayerCoord] & (1<<5)) != 0) { // invalid movement, rewind
            updatePlayerCoords(xPlayerCoordPrev, yPlayerCoordPrev);
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
        //canvas.drawText("Score: "+Integer.toString(playerScore), 0,250,paint);
        paint.reset();

        //mainGameScreen.updateGameData();

    }



    private void drawBomberman(Canvas canvas, Bitmap id){
        canvas.drawBitmap(id, yPlayerCoord *CELL_SIZE, xPlayerCoord *CELL_SIZE, paint);
    }

    public void addBomb(final int x, final int y){
        cellBitmask = (cellBitmask | (1 << 5));
    	map[x][y] = cellBitmask;

        Runnable startExplosion = new Runnable() {
            @Override
            public void run() {
                map[x][y] = (1<<8);
                invalidate();
            }
        };
        Runnable endExplosion = new Runnable() {
            @Override
            public void run() {
                map[x][y] = 0;
                //killPlayer(x, y);
                invalidate();
            }
        };

        handler.postDelayed(startExplosion, (long) (explosionTimeout*1000));
        handler.postDelayed(endExplosion, (long) (explosionTimeout*1000 + explosionDuration*1000));

    }

    // TODO: can we kill ourselves with our own bomb or only other players?
    private void killPlayer(int x, int y, Canvas canvas){

        // collision with explosion
        for(int i = 1 ; i <= explosionRange; i++) {
            if ((xPlayerCoord == x - i && yPlayerCoord == y) ||
                    (xPlayerCoord == x + i && yPlayerCoord == y) ||
                    (xPlayerCoord == x && yPlayerCoord == y - i) ||
                    (xPlayerCoord == x && yPlayerCoord == y + i)) {
                updatePlayerCoords(xPlayerInitialCoord, yPlayerInitialCoord);
                paint.setColor(Color.BLACK);
                paint.setStyle(Paint.Style.FILL);
                paint.setTextSize(20);
                canvas.drawText("ja foste, noob", getWidth() / 2, 250, paint);
                paint.reset();

            }
        }

        // collision with robots
        if (((y-1 >= 0) && ((map[x][y-1] & (1 << 4)) != 0)) ||
            ((y+1 < NUM_COLUMNS) && ((map[x][y+1] & (1 << 4)) != 0)) ||
            ((x-1 >= 0) && ((map[x-1][y] & (1 << 4)) != 0)) ||
            ((x+1 < NUM_ROWS) && ((map[x+1][y] & (1 << 4)) != 0))) {
        	updatePlayerCoords(xPlayerInitialCoord, yPlayerInitialCoord);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(20);
            canvas.drawText("ja foste, noob", getWidth() / 2, 250, paint);
            paint.reset();

        }
    }

    // TODO: object can only be (1<<6) (obstacle) or (1<<4) (robot) - we should add players too!
    private void destroy(int object, int x, int y, Canvas canvas) {
        boolean stopDestroyingSouth,stopDestroyingNorth,stopDestroyingWest,stopDestroyingEast;
        stopDestroyingEast=stopDestroyingNorth=stopDestroyingSouth=stopDestroyingWest=false;
        for(int i = 1 ; i <= explosionRange; i++) {
            if(x-i >= 0)
                if ((map[x-i][y] & (1<<7)) == 0 && !stopDestroyingNorth) {
                    if((map[x-i][y] & (1<<6)) != 0)
                        map[x-i][y] = 0;
                    if((map[x-i][y] & (1<<4)) != 0) {
                    	map[x-i][y] = (map[x-i][y] - (1<<4));
                        playerScore += pointsPerRobotKilled;
                    }
                    
                } else
                    stopDestroyingNorth = true;
            //Log.d("GameMap.Java", "x="+x+"    i="+i+"   NUM_COLUMNS="+NUM_COLUMNS+"      NUM_ROWS="+NUM_ROWS);
            if(x+i < NUM_ROWS)
                if ((map[x+i][y] & (1<<7)) == 0 && !stopDestroyingSouth){
                	if((map[x+i][y] & (1<<6)) != 0)
                        map[x+i][y] = 0;
                    if((map[x+i][y] & (1<<4)) != 0) {
                    	map[x+i][y] = (map[x+i][y] - (1<<4));
                        playerScore += pointsPerRobotKilled;
                    }
                } else
                    stopDestroyingSouth = true;
            if(y-i >= 0)
                if ((map[x][y-i] & (1<<7)) == 0 && !stopDestroyingEast) {
                	if((map[x][y-i] & (1<<6)) != 0)
                        map[x][y-i] = 0;
                    if((map[x][y-i] & (1<<4)) != 0) {
                    	map[x][y-i] = (map[x][y-i] - (1<<4));
                        playerScore += pointsPerRobotKilled;
                    }
                } else
                    stopDestroyingEast = true;
            if(y+i < NUM_COLUMNS)
                if ((map[x][y+i] & (1<<7)) == 0 && !stopDestroyingWest){
                	if((map[x][y+i] & (1<<6)) != 0)
                        map[x][y+i] = 0;
                    if((map[x][y+i] & (1<<4)) != 0) {
                    	map[x][y+i] = (map[x][y+i] - (1<<4));
                        playerScore += pointsPerRobotKilled;
                    }
                } else
                    stopDestroyingWest = true;

        }
    }

    // TODO: check maximum number of players - we assume 4 here
    private Boolean playerNear(int x, int y){
        if ((y >= 0) && (y < NUM_COLUMNS) && (x >= 0) && (x < NUM_ROWS))
            return ((map[x][y] & (1<<0)) != 0 || (map[x][y] & (1<<1)) != 0
            	 || (map[x][y] & (1<<2)) != 0 || (map[x][y] & (1<<3)) != 0)? true : false;
        else
            return false;
    }

    // invalid movement, rewind (avoid Walls, Obstacles, Bombs and other Robots(so they don't mesh into one)
    private Boolean obstacleNear(int x, int y){
        if ((y >= 0) && (y < NUM_COLUMNS) && (x >= 0) && (x < NUM_ROWS))
            return ((map[x][y] & (1<<6)) != 0 || (map[x][y] & (1<<7)) != 0 || (map[x][y] & (1<<4)) != 0 || (map[x][y] & (1<<5)) != 0)? true : false;
        else
            return false;
    }

    private Coord swapCells(int x, int y, int x_new, int y_new){
        if ((y_new >= 0) && (y_new < NUM_COLUMNS) && (x_new >= 0) && (x_new < NUM_ROWS)) {
            map[x_new][y_new] = map[x][y];
            map[x][y] = 0;
            return new Coord(x_new,y_new);
        } else
            return null;
    }

    private Coord findPlayer(int x, int y){
        //top;
        if(playerNear(x-2, y) && !obstacleNear(x-1, y)) {
            //killPlayer();
            return swapCells(x, y, x-1, y);
        }
        //right;
        if(playerNear(x, y+2) && !obstacleNear(x, y+1)) {
            //killPlayer();
            return swapCells(x, y, x, y+1);
        }
        //down;
        if(playerNear(x+2, y) && !obstacleNear(x+1, y)) {
            //killPlayer();
            return swapCells(x, y, x+1, y);
        }
        //left;
        if(playerNear(x, y-2) && !obstacleNear(x, y-1)) {
            //killPlayer();
            return swapCells(x, y, x, y-1);
        }
        return null;
    }

    private Coord findEmptyCell(int x, int y){
        int randomInt;
        // try at most a limited amount of times - the robot could be stuck between 4 walls
        for(int noOfTries = 0 ; noOfTries < 8 ; noOfTries++) {
            randomInt = randomGenerator.nextInt(4);
            switch (randomInt) {
                case 0: //top
                    if (obstacleNear(x - 1, y)) break;
                    return swapCells(x, y, x - 1, y);
                case 1: //right
                    if (obstacleNear(x, y + 1)) break;
                    return swapCells(x, y, x, y + 1);
                case 2: //down
                    if (obstacleNear(x + 1, y)) break;
                    return swapCells(x, y, x + 1, y);
                case 3: //left
                    if (obstacleNear(x, y - 1)) break;
                    return swapCells(x, y, x, y - 1);
                default: //fuck.
                    break;
            }
        }
        // no empty cell available found after a certain amount of tries
        return new Coord(x,y);
    }

    public Coord moveRobots(int x, int y){
        //not so random. kill the bastards first if they are anywhere near.
        Coord newCoord = findPlayer(x, y);
        if(newCoord != null)
            return newCoord;
        //ok, they left the building earlier. just find a suitable place to rest during the night.
        newCoord = findEmptyCell(x, y);
        if(newCoord != null)
            return newCoord;

        // robot didn't move, keep old position
        return new Coord(x,y);
    }

    // Named get*Coord and set*Coord so we don't override superclass's get* and set* methods
    public int getXCoord() {
        return xPlayerCoord;
    }

    public void setXCoord(int x) {
        this.xPlayerCoordPrev = getXCoord();
        updatePlayerCoords(x,getYCoord());
    }

    public int getYCoord() {
        return yPlayerCoord;
    }

    public void setYCoord(int y) {
        this.yPlayerCoordPrev = getYCoord();
        updatePlayerCoords(getXCoord(),y);
    }
    
    void updatePlayerCoords(int x, int y) {
    	xPlayerCoord = x;
        yPlayerCoord = y;
        map[xPlayerCoord][yPlayerCoord] = (map[xPlayerCoord][yPlayerCoord] | (1<<0));
    	
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

    public void setMainGameScreen(GameScreen a) {
        mainGameScreen = a;
        mainGameScreen.updateNumPlayers(numberOfPlayers);
        mainGameScreen.updatePlayerName(playerName);
    }

	public Object getInitialPlayerPos() {
		// TODO Auto-generated method stub22
		return null;
	}

	public GameDataManager getGdm() {
		return gdm;
	}

	public void setGdm(GameDataManager gdm) {
		this.gdm = gdm;
	}

	public int[][] getMap() {
		return map;
	}

	public static int getNUM_ROWS() {
		return NUM_ROWS;
	}

	public static int setNUM_ROWS(int nUM_ROWS) {
		NUM_ROWS = nUM_ROWS;
		return nUM_ROWS;
	}

	public static int getNUM_COLUMNS() {
		return NUM_COLUMNS;
	}

	public static void setNUM_COLUMNS(int nUM_COLUMNS) {
		NUM_COLUMNS = nUM_COLUMNS;
	}

	public void setMap(int[][] map) {
		this.map = map;
	}
}
