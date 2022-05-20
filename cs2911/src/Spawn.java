import java.awt.event.KeyListener;
import java.util.ArrayList;

public class Spawn {
	private Handler handler;
	private Game game;

	public Spawn(Handler handler, Game game){
		this.handler = handler;
		this.game = game;
	}
	
	public void tick(){
		ArrayList<GameObject> GoalSquares = handler.getGoalSquares();
		ArrayList<GameObject> Boxes = handler.getBoxes();

		int i = 0;
		for(GameObject g :  GoalSquares){
			for(GameObject g1 : Boxes){
				if(g1.getX() == g.getX() && g1.getY() == g.getY()){
					i++;
				}
			}
		}
		if(i == GoalSquares.size() && i != 0){
			System.out.println("You Win!");
			while(handler.object.size() != 0){
				handler.removeObject(handler.object.get(0));
			}
			
			game.removeKeyListener(game.getKeyinput());
			loadLevel1(handler);
			
			game.setKeyinput(new KeyInput(handler));
			game.addKeyListener(game.getKeyinput());
		}
	}
	
	public void loadtest(Handler handler){
		handler.addObject(new Player(32 * 1, 32 * 1, ID.Player2));
		
		handler.addObject(new Wall(32 * 8, 32 * 8, ID.Wall));
	}

	
	
	
	public void loadLevel1(Handler handler){
		handler.addObject(new GoalSquare(32 * 1, 32 * 5, ID.GoalSquare, handler));
		handler.addObject(new GoalSquare(32 * 1, 32 * 6, ID.GoalSquare, handler));
		handler.addObject(new GoalSquare(32 * 2, 32 * 6, ID.GoalSquare, handler));
		handler.addObject(new GoalSquare(32 * 4, 32 * 6, ID.GoalSquare, handler));
		handler.addObject(new GoalSquare(32 * 3, 32 * 6, ID.GoalSquare, handler));
		
		handler.addObject(new Player(32 * 1, 32 * 2, ID.Player2));
		
		handler.addObject(new Box(32 * 2, 32 * 2, ID.Box));
		handler.addObject(new Box(32 * 2, 32 * 3, ID.Box));
		handler.addObject(new Box(32 * 2, 32 * 5, ID.Box));
		handler.addObject(new Box(32 * 3, 32 * 4, ID.Box));
		handler.addObject(new Box(32 * 3, 32 * 6, ID.Box));
		
		for(int i = 0; i < 6; i++){
			handler.addObject(new Wall(32 * i, 32 * 0, ID.Wall));
			handler.addObject(new Wall(32 * i, 32 * 7, ID.Wall));
		}
		for(int i = 1; i < 7; i++){
			handler.addObject(new Wall(32 * 0, 32 * i, ID.Wall));
			handler.addObject(new Wall(32 * 5, 32 * i, ID.Wall));
		}
		for(int i = 1; i < 4 ; i++){
			handler.addObject(new Wall(32 * 4, 32 * i, ID.Wall));
		}
		
		handler.addObject(new Wall(32 * 1, 32 * 1, ID.Wall));
		handler.addObject(new Wall(32 * 1, 32 * 3, ID.Wall));
		handler.addObject(new Wall(32 * 1, 32 * 4, ID.Wall));
	}
}
