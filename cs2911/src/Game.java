
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Random;

public class Game extends Canvas implements Runnable {

	private static final long serialVersionUID = 1222540838952399849L;

	public static final int WIDTH = 640, HEIGHT = WIDTH / 12 * 9; 
															
	private Thread thread;
	private boolean running = false;

	private Random r;
	private Handler handler;
	private Spawn spawner;
	private KeyInput keyinput;
	
	public enum STATE {
		Menu,
		Help,
		Game,
		End
	};
	
	public static STATE gameState = STATE.Menu;

	public Game() {
		//Creates a new Handler. Handler is the class that "handles" all gameObjects
		this.handler = new Handler();
		//Creates a new Spawner, The Spawner can add and remove gameObjects from the handler
		this.spawner = new Spawn(handler, this);
		
		r = new Random();

		loadLevel2(handler);
		this.setKeyinput(new KeyInput(handler));
		this.addKeyListener(getKeyinput());
		
		//Create Window for the game
		new Window(WIDTH, HEIGHT, "Comp2911", this);
	}

	public synchronized void start() {
		thread = new Thread(this);
		thread.start();
		running = true;
	}

	public synchronized void stop() {
		try {
			thread.join();
			running = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		this.requestFocus();
		long lastTime = System.nanoTime();
		double amountOfTicks = 60.0;
		double ns = 1000000000 / amountOfTicks;
		double delta = 0;
		long timer = System.currentTimeMillis();
		int frames = 0;
		while (running) {
			long now = System.nanoTime();
			delta += (now - lastTime) / ns;
			lastTime = now;
			while (delta >= 1) {
				tick();
				delta--;
			}
			if (running)
				render();
			frames++;

			if (System.currentTimeMillis() - timer > 1000) {
				timer += 1000;
				System.out.println("FPS: " + frames);
				frames = 0;
			}
		}
		stop();
	}

	private void tick() {
		handler.tick();
		spawner.tick();
	}

	private void render() {
		BufferStrategy bs = this.getBufferStrategy();
		if (bs == null) {
			this.createBufferStrategy(3);
			return;
		}

		Graphics g = bs.getDrawGraphics();

		g.setColor(Color.black);
		g.fillRect(0, 0, WIDTH, HEIGHT);

		handler.render(g);

		g.dispose();
		bs.show();
	}

	public static int clamp(int var, int max, int min) {
		if (var >= max)
			return max;
		else if (var <= min)
			return min;
		else
			return var;
	}

	public static void main(String args[]) {
		new Game();
	}
	
	
	
	
	public static void loadLevel2(Handler handler){
		
		for(int i = 3; i < 6; i++){
			handler.addObject(new GoalSquare(32 * 7, 32 * i, ID.GoalSquare, handler));
		}
		
		handler.addObject(new Player(32 * 1, 32 * 1, ID.Player2));
		
		handler.addObject(new Box(32 * 2, 32 * 2, ID.Box));
		handler.addObject(new Box(32 * 3, 32 * 2, ID.Box));
		handler.addObject(new Box(32 * 2, 32 * 3, ID.Box));
		
		for(int i = 0; i < 5; i++){
			handler.addObject(new Wall(0, 32 * i, ID.Wall));
		}
		handler.addObject(new Wall(32 * 1, 32 * 0, ID.Wall));
		for(int i = 4; i < 9; i++){
			handler.addObject(new Wall(32 * 1, 32 * i, ID.Wall));
		}
		handler.addObject(new Wall(32 * 2, 32 * 0, ID.Wall));
		for(int i = 4; i < 6; i++){
			handler.addObject(new Wall(32 * 2, 32 * i, ID.Wall));
		}
		handler.addObject(new Wall(32 * 2, 32 * 8, ID.Wall));
		
		handler.addObject(new Wall(32 * 3, 32 * 0, ID.Wall));
		handler.addObject(new Wall(32 * 3, 32 * 8, ID.Wall));
		
		for(int i = 0; i < 5; i++){
			handler.addObject(new Wall(32 * 4, 32 * i, ID.Wall));
		}
		handler.addObject(new Wall(32 * 4, 32 * 8, ID.Wall));
		
		handler.addObject(new Wall(32 * 5, 32 * 4, ID.Wall));
		for(int i = 6; i < 9; i++){
			handler.addObject(new Wall(32 * 5, 32 * i, ID.Wall));
		}
		
		for(int i = 2; i < 5; i++){
			handler.addObject(new Wall(32 * 6, 32 * i, ID.Wall));
		}
		handler.addObject(new Wall(32 * 6, 32 * 7, ID.Wall));
		
		handler.addObject(new Wall(32 * 7, 32 * 2, ID.Wall));
		handler.addObject(new Wall(32 * 7, 32 * 7, ID.Wall));
		
		for(int i = 2; i < 8; i++){
			handler.addObject(new Wall(32 * 8, 32 * i, ID.Wall));
		}
		
	}

	public KeyInput getKeyinput() {
		return keyinput;
	}

	public void setKeyinput(KeyInput keyinput) {
		this.keyinput = keyinput;
	}
}
