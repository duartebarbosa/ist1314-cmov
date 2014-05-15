package com.bomberman.app;
//
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


	public int newPlayer(int x, int y) {
		GameData.Player pl = gd.new Player();

		GameData.Pos plPos = gd.new Pos();
		updatePos(plPos, x, y);
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

	public void parseData(String string, int myPlayer) {
		// get players positions
		String aux[] = string.split(" ");
		for (int i = 0; i < gd.getPlayerList().size();++i){
			if (i == myPlayer)
				continue;
			updatePlayerLoc(i,Integer.parseInt(aux[i*2+0]),Integer.parseInt(aux[i*2+1]));
		}
		
	}

	public String getMsg() {
		String message = "";
		
		for (int i = 0; i < gd.getPlayerList().size();++i){
			message += gd.getPlayerList().get(i).getPlayerPos().getX() + " " +
						gd.getPlayerList().get(i).getPlayerPos().getY() + " ";
		}
		message += "\n";
		return message;
	}
}
