package com.bomberman.app;

import java.util.ArrayList;

public class GameData {

	public GameData() {
		playerList = new ArrayList<Player>();
		robotList = new ArrayList<Robot>();
		bombList = new ArrayList<Bomb>();
	}
	
	class Pos {
		private int x;
		private int y;
		
		int getX() {
			return x;
		}
		void setX(int x) {
			this.x = x;
		}
		int getY() {
			return y;
		}
		void setY(int y) {
			this.y = y;
		}
	}
	

	 class Player {
		private Pos playerPos;

		Pos getPlayerPos() {
			return playerPos;
		}

		void setPlayerPos(Pos playerPos) {
			this.playerPos = playerPos;
		}
		
	}

	 class Robot{
		private Pos RobotPos;

		Pos getRobotPos() {
			return RobotPos;
		}

		void setRobotPos(Pos robotPos) {
			RobotPos = robotPos;
		}
	}
	
	 class Bomb {
		private Pos bombPos;

		Pos getBombPos() {
			return bombPos;
		}

		void setBombPos(Pos bombPos) {
			this.bombPos = bombPos;
		}
	}
	
	
	private ArrayList<Player> playerList;
	private ArrayList<Robot> robotList;
	private ArrayList<Bomb> bombList;
	
	public ArrayList<Player> getPlayerList() {
		return playerList;
	}
	
	public void setPlayerList(ArrayList<Player> playerList) {
		this.playerList = playerList;
	}
	
	public ArrayList<Robot> getRobotList() {
		return robotList;
	}
	
	public void setRobotList(ArrayList<Robot> robotList) {
		this.robotList = robotList;
	}
	
	public ArrayList<Bomb> getBombList() {
		return bombList;
	}
	
	public void setBombList(ArrayList<Bomb> bombList) {
		this.bombList = bombList;
	}
}
