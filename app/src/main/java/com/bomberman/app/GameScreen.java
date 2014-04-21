package com.bomberman.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class GameScreen extends Activity {

    private GameMap gameMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_screen);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.game_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClickUp(View view) {
        gameMapView = (GameMap) findViewById(R.id.view);
        gameMapView.setXCoord(gameMapView.getXCoord() - 1);
        gameMapView.setBombermanDirection(GameMap.Direction.TOP);
        gameMapView.invalidate();

    }

    public void onClickDown(View view) {
        gameMapView = (GameMap) findViewById(R.id.view);
        gameMapView.setXCoord(gameMapView.getXCoord() + 1);
        gameMapView.setBombermanDirection(GameMap.Direction.DOWN);
        gameMapView.invalidate();

    }

    public void onClickLeft(View view) {
        gameMapView = (GameMap) findViewById(R.id.view);
        gameMapView.setYCoord(gameMapView.getYCoord() - 1);
        gameMapView.setBombermanDirection(GameMap.Direction.LEFT);
        gameMapView.invalidate();

    }

    public void onClickRight(View view) {
        gameMapView = (GameMap) findViewById(R.id.view);
        gameMapView.setYCoord(gameMapView.getYCoord() + 1);
        gameMapView.setBombermanDirection(GameMap.Direction.RIGHT);
        gameMapView.invalidate();

    }

    public void onClickBomb(View view) {
        gameMapView = (GameMap) findViewById(R.id.view);
        gameMapView.addBomb(gameMapView.getXCoord(), gameMapView.getYCoord());
        gameMapView.invalidate();
    }

}
