package src.toolbox;

public class Vector2f {

	public float x,y;
	
	public Vector2f(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	
	public void normalise(){
		x = (float) (x/(Math.sqrt(x*x + y*y )));
		y = (float) (y/(Math.sqrt(x*x + y*y)));
	}
}
