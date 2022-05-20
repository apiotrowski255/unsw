package ass2.spec;

import com.jogamp.opengl.GL2;

public class Portal {

	//position
	private float x,y,z;
	//The other portal and the place to teleport the camera
	private Portal linked = null;;
	private float exitX, exitY, exitZ;
	//effects for the portal
	private float theta = 0;
	private double radius = 1;
	
	public Portal(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
		this.exitX = x; 
		this.exitY = y;
		this.exitZ = z;
	}
	
	public void draw(GL2 gl){
    	
    	gl.glDisable(GL2.GL_LIGHTING);
		
		gl.glPushMatrix();
		gl.glTranslatef(x, y, z);
		theta++;
		gl.glRotatef(theta, 0, 0, 1);
		double slices = 64;
		double width = radius;
		
    	//Front circle
    	gl.glBegin(GL2.GL_TRIANGLE_FAN);{
    	
    		 gl.glNormal3d(0,0,-1);
    		 gl.glTexCoord2d(0.5,0.5);
    		 gl.glVertex3d(0,0,0);
    		 double angleStep = 2*Math.PI/slices;
             for (int i = 0; i <= slices ; i++){
                 double a0 = i * angleStep;
                 double x0 = width*Math.cos(a0);
                 double y0 = width*Math.sin(a0);
                 
                 gl.glTexCoord2d((0.4)*Math.cos(a0)+0.5, (0.4)*Math.sin(a0)+0.5);
                 gl.glVertex3d(x0,y0,0);
              
             }                
    	}gl.glEnd();
    	
    	//back circle
    	gl.glBegin(GL2.GL_TRIANGLE_FAN);{
    	
    		 gl.glNormal3d(0,0,1);
    		 gl.glTexCoord2d(0.5,0.5);
    		 gl.glVertex3d(0,0,0+0.01);
    		 double angleStep = 2*Math.PI/slices;
             for (int i = 0; i <= slices ; i++){
                 double a0 = i * angleStep;
                 double x0 = width*Math.cos(a0);
                 double y0 = width*Math.sin(a0);
                 
                 gl.glTexCoord2d((0.4)*Math.cos(a0)+0.5, (0.4)*Math.sin(a0)+0.5);
                 gl.glVertex3d(x0,y0,0+0.01);
              
             }                
    	}gl.glEnd();
    	
    	gl.glPopMatrix();
    	gl.glEnable(GL2.GL_LIGHTING);
	}
	

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public Portal getLinked() {
		return linked;
	}

	public void setLinked(Portal linked) {
		this.linked = linked;
	}

	public float getExitX() {
		return exitX;
	}

	public void setExitX(float exitX) {
		this.exitX = exitX;
	}

	public float getExitY() {
		return exitY;
	}

	public void setExitY(float exitY) {
		this.exitY = exitY;
	}

	public float getExitZ() {
		return exitZ;
	}

	public void setExitZ(float exitZ) {
		this.exitZ = exitZ;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	
	
	
}
