


import java.awt.Graphics;
import java.util.ArrayList;

public class Handler {

	ArrayList<GameObject> object = new ArrayList<GameObject>();
	
	public void tick(){
		for(int i = 0; i < object.size(); i++){
			GameObject tempObject = object.get(i);
			
			tempObject.tick();
		}
	}
	
	public void render(Graphics g){
		for(int i = 0; i < object.size(); i++){
			GameObject tempObject = object.get(i);
			
			tempObject.render(g);
		}
	}
	
	public void addObject(GameObject object){
		this.object.add(object);
	}
	
	public void removeObject(GameObject object){
		this.object.remove(object);
	}
	
	//this works when there is only one player2 on the screen
	//if player2 does not exist, then it returns null.
	public GameObject getPlayer2(){
		for(int i = 0; i < object.size(); i++){
			if(object.get(i).getID() == ID.Player2){
				return object.get(i);
			}
		}
		return null;
	}
	
	// only works when there is one box
	//if no boxes exist, then return null
	public ArrayList<GameObject> getBoxes(){
		ArrayList<GameObject> boxes = new ArrayList<GameObject>();
		for(int i = 0; i < object.size(); i++){
			if(object.get(i).getID() == ID.Box){
				boxes.add(object.get(i));
			}
		}
		return boxes;
	}
	
	public ArrayList<GameObject> getGoalSquares(){
		ArrayList<GameObject> GoalSquares = new ArrayList<GameObject>();
		for(int i = 0; i < object.size(); i++){
			if(object.get(i).getID() == ID.GoalSquare){
				GoalSquares.add(object.get(i));
			}
		}
		return GoalSquares;
	}
	
	
	public ArrayList<GameObject> getWalls(){
		ArrayList<GameObject> walls = new ArrayList<GameObject>();
		for(int i = 0; i < object.size(); i++){
			if(object.get(i).getID() == ID.Wall){
				walls.add(object.get(i));
			}
		}
		return walls;
	}
}
