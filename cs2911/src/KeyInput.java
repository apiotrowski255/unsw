
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedList;

public class KeyInput extends KeyAdapter {

	private Handler handler;
	public ArrayList<GameObject> walls = new ArrayList<GameObject>();
	public ArrayList<GameObject> boxes = new ArrayList<GameObject>();

	public KeyInput(Handler handler) {
		this.handler = handler;
		this.walls= handler.getWalls();
		this.boxes = handler.getBoxes();
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();

		for (int i = 0; i < handler.object.size(); i++) {
			GameObject tempObject = handler.object.get(i);
			GameObject player = handler.getPlayer2();

			if (tempObject.getID() == ID.Player) {
				// key events for player 1

				if (key == KeyEvent.VK_W) {
					tempObject.setY(tempObject.getY() - 32);
				}
				if (key == KeyEvent.VK_S) {
					tempObject.setY(tempObject.getY() + 32);
				}
				if (key == KeyEvent.VK_D) {
					tempObject.setX(tempObject.getX() + 32);
				}
				if (key == KeyEvent.VK_A) {
					tempObject.setX(tempObject.getX() - 32);
				}
			}

			if (tempObject.getID() == ID.Player2) {
				// key events for player 2

				if (key == KeyEvent.VK_UP && tempObject.canObjectMoveUp()) {
					if(!(isWallUp(tempObject))){
						if(isPlayerAboveToBox(tempObject)){
							GameObject b = getBoxAboveToPlayer(tempObject);
							if(canBoxMoveUp(b)){
								tempObject.setY(tempObject.getY() - 32);
								b.setY(b.getY() - 32);
							}
						} else {
							tempObject.setY(tempObject.getY() - 32);
						}
					}
				}
				if (key == KeyEvent.VK_DOWN && tempObject.canObjectMoveDown()) {
					if(!(isWallDown(tempObject))){
						if(isPlayerBelowToBox(tempObject)){
							GameObject b = getBoxUnderPlayer(tempObject);
							if(canBoxMoveDown(b)){
								tempObject.setY(tempObject.getY() + 32);
								b.setY(b.getY() + 32);
							}
						} else {
							tempObject.setY(tempObject.getY() + 32);
						}
					}
				}
				if (key == KeyEvent.VK_RIGHT && tempObject.canObjectMoveRight()) {
					if(!(isWallRight(tempObject))){
						if(isPlayerRightToBox(tempObject)){
							GameObject b = getBoxRightToPlayer(tempObject);
							if(canBoxMoveRight(b)){
								tempObject.setX(tempObject.getX() + 32);
								b.setX(b.getX() + 32);
							}
						} else {
							tempObject.setX(tempObject.getX() + 32);
						}
					}
				}
				if (key == KeyEvent.VK_LEFT && tempObject.canObjectMoveLeft()) {
					if(!(isWallLeft(tempObject))){
						if(isPlayerLeftToBox(tempObject)){
							GameObject b = getBoxLeftToPlayer(tempObject);
							if(canBoxMoveLeft(b)){
								tempObject.setX(tempObject.getX() - 32);
								b.setX(b.getX() - 32);
							}
						} else {
							tempObject.setX(tempObject.getX() - 32);
						}
					} 
				}
			}
		}

	}

	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();

		for (int i = 0; i < handler.object.size(); i++) {
			GameObject tempObject = handler.object.get(i);

			if (tempObject.getID() == ID.Player) {
				// key events for player 1

				if (key == KeyEvent.VK_W) {
					tempObject.setVelY(0);
				}
				if (key == KeyEvent.VK_S) {
					tempObject.setVelY(0);
				}
				if (key == KeyEvent.VK_D) {
					tempObject.setVelX(0);
				}
				if (key == KeyEvent.VK_A) {
					tempObject.setVelX(0);
				}
			}

			if (tempObject.getID() == ID.Player2) {
				// key events for player 2

				if (key == KeyEvent.VK_UP) {
					tempObject.setVelY(0);
				}
				if (key == KeyEvent.VK_DOWN) {
					tempObject.setVelY(0);
				}
				if (key == KeyEvent.VK_RIGHT) {
					tempObject.setVelX(0);
				}
				if (key == KeyEvent.VK_LEFT) {
					tempObject.setVelX(0);
				}
			}
		}
	}
	
	//helper functions
	public boolean isWallLeft(GameObject tempObject){
		for(GameObject w : walls){
			if(tempObject.getY() == w.getY()){
				if(tempObject.getX() - 32 == w.getX()){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isWallRight(GameObject tempObject){
		for(GameObject w : walls){
			if(tempObject.getY() == w.getY()){
				if(tempObject.getX() + 32 == w.getX()){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isWallUp(GameObject tempObject){
		for(GameObject w : walls){
			if(tempObject.getX() == w.getX()){
				if(tempObject.getY() - 32 == w.getY()){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isWallDown(GameObject tempObject){
		for(GameObject w : walls){
			if(tempObject.getX() == w.getX()){
				if(tempObject.getY() + 32 == w.getY()){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean canBoxMoveLeft(GameObject box){
		for(GameObject g : walls){
			if(box.getY() == g.getY()){
				if(box.getX() - 32 == g.getX()){
					return false;
				}
			}
		}
		for(GameObject b : boxes){
			if(box.getY() == b.getY()){
				if(box.getX() - 32 == b.getX()){
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean canBoxMoveRight(GameObject box){
		for(GameObject g : walls){
			if(box.getY() == g.getY()){
				if(box.getX() + 32 == g.getX()){
					return false;
				}
			}
		}
		for(GameObject b : boxes){
			if(box.getY() == b.getY()){
				if(box.getX() + 32 == b.getX()){
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean canBoxMoveUp(GameObject box){
		for(GameObject g : walls){
			if(box.getX() == g.getX()){
				if(box.getY() - 32 == g.getY()){
					return false;
				}
			}
		}
		for(GameObject b : boxes){
			if(box.getX() == b.getX()){
				if(box.getY() - 32 == b.getY()){
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean canBoxMoveDown(GameObject box){
		for(GameObject g : walls){
			if(box.getX() == g.getX()){
				if(box.getY() + 32 == g.getY()){
					return false;
				}
			}
		}
		for(GameObject b : boxes){
			if(box.getX() == b.getX()){
				if(box.getY() + 32 == b.getY()){
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean isPlayerLeftToBox(GameObject player){
		for(GameObject b : boxes){
			if(player.getY() == b.getY()){
				if(player.getX()  == b.getX() + 32){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isPlayerRightToBox(GameObject player){
		for(GameObject b : boxes){
			if(player.getY() == b.getY()){
				if(player.getX()  == b.getX() - 32){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isPlayerAboveToBox(GameObject player){
		for(GameObject b : boxes){
			if(player.getX() == b.getX()){
				if(player.getY()  == b.getY() + 32){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isPlayerBelowToBox(GameObject player){
		for(GameObject b : boxes){
			if(player.getX() == b.getX()){
				if(player.getY()  == b.getY() - 32){
					return true;
				}
			}
		}
		return false;
	}
	
	public GameObject getBoxRightToPlayer(GameObject player){
		for(GameObject b : boxes){
			if(player.getY() == b.getY()){
				if(player.getX()  == b.getX() - 32){
					return b;
				}
			}
		}
		return null;
	}
	
	public GameObject getBoxLeftToPlayer(GameObject player){
		for(GameObject b : boxes){
			if(player.getY() == b.getY()){
				if(player.getX()  == b.getX() + 32){
					return b;
				}
			}
		}
		return null;
	}
	
	public GameObject getBoxAboveToPlayer(GameObject player){
		for(GameObject b : boxes){
			if(player.getX() == b.getX()){
				if(player.getY()  == b.getY() + 32){
					return b;
				}
			}
		}
		return null;
	}
	
	public GameObject getBoxUnderPlayer(GameObject player){
		for(GameObject b : boxes){
			if(player.getX() == b.getX()){
				if(player.getY()  == b.getY() - 32){
					return b;
				}
			}
		}
		return null;
	}
	
	
	//We may not need this
	public void UpdateHandler(Handler newHandler){
		this.handler = newHandler;
	}
}
