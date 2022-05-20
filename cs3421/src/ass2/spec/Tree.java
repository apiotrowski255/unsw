package ass2.spec;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

/**
 * COMMENT: Comment Tree
 *
 * @author malcolmr
 */

public class Tree implements KeyListener{

    private double[] myPos;
	private int ORDER = 1;
    private String string = "x";
    private static final double CYLINDER_HEIGHT = 0.25;
    private static final double CYLINDER_DIAMETER = 0.05;
    private static final double ANGLE = 25;
    private boolean ReInit= false;

    
    public Tree(double x, double y, double z) {
        myPos = new double[3];
        myPos[0] = x;
        myPos[1] = y;
        myPos[2] = z;
    }
    
    public double[] getPosition() {
        return myPos;
    }
    
    public void draw(GL2 gl){
  
    	gl.glPushMatrix();
    	gl.glTranslated(myPos[0], myPos[1], myPos[2]);
    	for(int i = 0; i < string.length(); i++){
    		char c = string.charAt(i);
    		if(c == 'x'){
    			//do nothing
    		} else if (c == 'f'){
    			//draw forward
    			drawCylinder(gl);
    			gl.glTranslated(0, CYLINDER_HEIGHT, 0);
    		} else if (c == '+'){
    			gl.glRotated(ANGLE, 1, 0, 0);
    		} else if (c == '-'){
    			gl.glRotated(-ANGLE, 1, 0, 0);
    		} else if (c == '['){
    			gl.glPushMatrix();
    		}else if (c == ']'){
    			gl.glPopMatrix();
    		} else if (c == 'a'){
    			//do nothing
    		} else if (c == 'b'){
    			//do nothing
    		} else if (c == '>'){
    			gl.glRotated(ANGLE, 0, 0, 1);
    		}
    	}
    	gl.glPopMatrix();
    	
    	
    	if (ReInit == true){
    		string = "x";
    		init(gl);
    		ReInit = false;
    	}
    }
    
    
    public void drawCylinder(GL2 gl){
    	double slices = 32;
    	double width = CYLINDER_DIAMETER;
    	
    	//gl.glColor3f(1,0,0);
    	gl.glPushMatrix();
    	//gl.glTranslated(myPos[0], myPos[1], myPos[2]);
    	gl.glRotated(-90, 1, 0, 0);
    	double z1 = 0;
    	double z2 = CYLINDER_HEIGHT;
    	gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL2.GL_FILL);
    	
    	//Front circle
    	gl.glBegin(GL2.GL_TRIANGLE_FAN);{
    	
    		 gl.glNormal3d(0,0,-1);
    		 gl.glVertex3d(0,0,z1);
    		 double angleStep = 2*Math.PI/slices;
             for (int i = 0; i <= slices ; i++){
                 double a0 = i * angleStep;
                 double x0 = width*Math.cos(a0);
                 double y0 = width*Math.sin(a0);

                gl.glVertex3d(x0,y0,z1);
              
             }                
    	}gl.glEnd();
    	
    	//Back circle
    	gl.glBegin(GL2.GL_TRIANGLE_FAN);{
       
   		 gl.glNormal3d(0,0,1);
   		 gl.glVertex3d(0,0,z2);
   		 double angleStep = 2*Math.PI/slices;
            for (int i = 0; i <= slices ; i++){
                
            	double a0 = 2*Math.PI - i * angleStep;
                            
                double x0 = width*Math.cos(a0);
                double y0 = width*Math.sin(a0);

                gl.glVertex3d(x0,y0,z2);
            }
                
                
    	}gl.glEnd();
    	  
    	//Sides of the cylinder
    	gl.glBegin(GL2.GL_QUADS);
        {
            double angleStep = 2*Math.PI/slices;
            for (int i = 0; i <= slices ; i++){
                double a0 = i * angleStep;
                double a1 = ((i+1) % slices) * angleStep;
                
                //Calculate vertices for the quad
                double x0 = width*Math.cos(a0);
                double y0 = width*Math.sin(a0);

                double x1 = width*Math.cos(a1);
                double y1 = width*Math.sin(a1);
                //Calculation for face normal for each quad
                //                     (x0,y0,z2)
                //                     ^
                //                     |  u = (0,0,z2-z1) 
                //                     |
                //                     | 
                //(x1,y1,z1)<--------(x0,y0,z1)
                //v = (x1-x0,y1-y0,0)  
                //                     
                //                     
                //                       
                //                    
                //
                // u x v gives us the un-normalised normal
                // u = (0,     0,   z2-z1)
                // v = (x1-x0,y1-y0,0) 
                
                
                //If we want it to be smooth like a cylinder
                //use different normals for each different x and y
                gl.glNormal3d(x0, y0, 0);
                                             
                gl.glVertex3d(x0, y0, z1);
                gl.glVertex3d(x0, y0, z2);  
                
                //If we want it to be smooth like a cylinder
                //use different normals for each different x and y
                gl.glNormal3d(x1, y1, 0);
                
                gl.glVertex3d(x1, y1, z2);
                gl.glVertex3d(x1, y1, z1);               
                         
            }

        }
        gl.glEnd();
        gl.glPopMatrix();
    	gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL2.GL_FILL);
    }
    
    public void init(GL2 gl){
    	int i = 0;
    	while (i < ORDER) {
    		if (string.contains("f")){
    			string = string.replace("f", "ff");
    		}
    		if (string.contains("x")){
    			string = string.replace("x", "f[-x][x]f[-x]+fx");
    		}
    		i++;
    	}
    	System.out.println(string);
    }


	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == e.VK_UP) {
			ORDER++;
			System.out.println(ORDER);
			ReInit = true;
		}
		if (key == e.VK_DOWN) {
			ORDER--;
			System.out.println(ORDER);
			ReInit = true;
		}

	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}


}
