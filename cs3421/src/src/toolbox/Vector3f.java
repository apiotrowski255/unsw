package src.toolbox;

public class Vector3f {

	public float x,y,z;
	
	public Vector3f(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	
	public void normalise(){
		x = (float) (x/(Math.sqrt(x*x + y*y + z*z)));
		y = (float) (y/(Math.sqrt(x*x + y*y + z*z)));
		z = (float) (z/(Math.sqrt(x*x + y*y + z*z)));
	}


	public float getX() {
		return x;
	}


	public float getY() {
		return y;
	}


	public float getZ() {
		return z;
	}
	
	
}
