import java.awt.Color;
import java.awt.Graphics;

public class Box extends GameObject{


	public Box(int x, int y, ID id) {
		super(x, y, id);

	}

	public void tick() {
		
		
	}

	public void render(Graphics g) {
		g.setColor(Color.red);
		g.fillRect(x, y, 32, 32);
	}

}
