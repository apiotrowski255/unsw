import java.awt.Color;
import java.awt.Graphics;

public class Wall extends GameObject{

	public Wall(int x, int y, ID id) {
		super(x, y, id);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void tick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(Graphics g) {
		g.setColor(Color.darkGray);
		g.fillRect(x, y, 32, 32);
	}

}
