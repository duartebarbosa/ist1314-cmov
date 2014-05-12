package com.bomberman.app;

import com.bomberman.app.GameData.Pos;

public class GameDataManager {

	private GameData gd;
	
	public GameDataManager() {
		gd = new GameData();
	}

	public GameData getGd() {
		return gd;
	}

	public void setGd(GameData gd) {
		this.gd = gd;
	}


	
	public void updatePlayerLoc(int playerNum, int xCoord, int yCoord) {
		updatePos(gd.getPlayerList().get(playerNum).getPlayerPos(), xCoord, yCoord);
	}


	public int newPlayer() {
		GameData.Player pl = gd.new Player();

		GameData.Pos plPos = gd.new Pos();
		updatePos(plPos, 0, 0);
		pl.setPlayerPos(plPos);
		
		gd.getPlayerList().add(pl);
		return gd.getPlayerList().indexOf(pl);
	}
	
	private GameData.Pos newPos(int xCoord, int yCoord){
		GameData.Pos aux = gd.new Pos();
		aux.setX(xCoord);
		aux.setY(yCoord);
		return aux;
	}
	
	private void updatePos(Pos pos, int xCoord, int yCoord) {
		pos.setX(xCoord);
		pos.setY(yCoord);
	}

	
	public Pos getPlayerPos(int playerNum) {
		
		return gd.getPlayerList().get(playerNum).getPlayerPos();
	}
}
