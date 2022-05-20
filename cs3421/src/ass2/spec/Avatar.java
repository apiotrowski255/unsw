package ass2.spec;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

public class Avatar {

	private float x,y,z;
	private static final float RADIUS = 0.5f;
	
	public Avatar(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void draw(GL2 gl){
		float lightAmb[] = { 0.0f, 1.0f, 0.0f, 1.0f };
		float lightDifAndSpec[] = { 0.0f, 1.0f, 1.0f, 1.0f };
		float globAmb[] = { 0.5f, 0.5f, 0.5f, 1.0f };

		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmb, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDifAndSpec, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightDifAndSpec, 0);
		
		gl.glPushMatrix();
		gl.glTranslatef(x, y + RADIUS, z);
		GLUT glut = new GLUT();
        glut.glutSolidSphere(RADIUS, 40, 40);
        gl.glTranslatef(0, RADIUS, 0);
        glut.glutSolidSphere(RADIUS, 40, 40);
        gl.glTranslatef(0, -2 * RADIUS, 0);
        glut.glutSolidSphere(RADIUS, 40, 40);
        gl.glPopMatrix();
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
	
	
	
}
