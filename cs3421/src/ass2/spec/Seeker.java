package ass2.spec;

import java.nio.FloatBuffer;
import java.util.Random;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;

public class Seeker {
	
	private float spotAngle = 10.0f; // Spotlight cone half-angle.
	private float spotDirection[] = {0.0f, -1.0f, 0.0f}; // Spotlight direction.
	private float spotExponent = 2.0f; // Spotlight exponent = attenuation factor.
	
	float x,y,z;
	float xTarget = 0;
	float zTarget = 0;

	//Object that uses VBO, this class is for the other criteria.
	float positions[] =  {
			0,1,-1, 
			-1,-1,-1,
			1,-1,-1, 
			0, 2,-1.5f,
			-2,-2,-1.5f, 
			2,-2,-1.5f,
			
			0,1,-1,
			0, 2,-1.5f,
			-2,-2,-1.5f, 
			
			0,1,-1,
			0, 2,-1.5f,
			2,-2,-1.5f,
			
    };

	//There should be a matching entry in this array for each entry in
	//the positions array
	float colors[] = {
			1,0,0, 
			0,1,0,
			1,1,1,
			0,0,0,
			0,0,1, 
			1,1,0,
			
			1,0,0, 
			0,1,0,
			1,1,1,
			1,0,0, 
			0,1,0,
			1,1,1,
			

    }; 
	
	//These are not vertex buffer objects, they are just java containers
	private FloatBuffer  posData = Buffers.newDirectFloatBuffer(positions);
	private FloatBuffer colorData = Buffers.newDirectFloatBuffer(colors);
	
	//We will be using 1 vertex buffer object
	private int bufferIds[] = new int[1];
	
	public enum STATE {wander, chase, idle};
	public STATE state = STATE.wander;
	
	public Seeker(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void draw(GL2 gl){
		//Bind the buffer we want to use
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glDisable(GL2.GL_TEXTURE_2D);
		gl.glPushMatrix();
		gl.glTranslatef(x, y, z);
		gl.glRotated(90, 1, 0, 0);
		
    	gl.glBindBuffer(GL.GL_ARRAY_BUFFER,bufferIds[0]);

    	// Enable two vertex arrays: coordinates and color.
    	//To tell the graphics pipeline that we want it to use our vertex position and color data
    	gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_COLOR_ARRAY);

   	    // This tells OpenGL the locations for the co-ordinates and color arrays.
   	    gl.glVertexPointer(3, //3 coordinates per vertex 
   			              GL.GL_FLOAT, //each co-ordinate is a float 
   			              0, //There are no gaps in data between co-ordinates 
   			              0); //Co-ordinates are at the start of the current array buffer
   	    gl.glColorPointer(3, 
   	    		          GL.GL_FLOAT, 
   	    		          0, 
   	    		          positions.length*Float.BYTES); //colors are found after the position
   	   												     //co-ordinates in the current array buffer
    	
   	   //Draw triangles, using 6 vertices, starting at vertex index 0 
   	   gl.glDrawArrays(GL2.GL_TRIANGLES,0,12);      
        	
   	   //Disable these. Not needed in this example, but good practice.
   	   gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
   	   gl.glDisableClientState(GL2.GL_COLOR_ARRAY);
   	   
    	//Unbind the buffer. 
    	//This is not needed in this simple example but good practice
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER,0);
        gl.glPopMatrix();
        gl.glEnable(GL2.GL_TEXTURE_2D);
        gl.glEnable(GL2.GL_LIGHTING);
        
        if(state == STATE.wander){
        	
        	Random r = new Random();
        	xTarget = r.nextFloat() * 10;
        	zTarget = r.nextFloat() * 10;
        	state = STATE.chase;
        } else if (state == STATE.chase){
        	float speed = 0.1f;
        	if(xTarget > x - 1 && xTarget < x + 1){
        		
        	} else if (xTarget > x){
        		x+= speed;
        	} else {
        		x-= speed;
        	}
        	if(zTarget > z - 1&& zTarget < z + 1){
        	
        	} else if (zTarget > z){
        		z+= speed;
        	} else {
        		z-= speed;
        	}
        	
        	if (xTarget > x - 1 && xTarget < x + 1 && zTarget > z - 1&& zTarget < z + 1){
        		state = STATE.idle;
        	}
        } else {
        	Random r = new Random();
        	if (r.nextFloat() > 0.9){
        		state = STATE.wander;
        	}
        }
        
        
        
        gl.glPushMatrix();{
        	GLUT glut = new GLUT();
        	gl.glTranslated(x, y-4, z); // Move the spotlight.
        	float lightPos[] = {0.0f, 3, 0.0f, 1.0f}; // Spotlight position.
        	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos,0);  

        	// Draw the spotlight cone in wireframe after disabling lighting
        	// This is needed as there will be no light on the wireframe but we want to see it
        	// to help us understand what is happening with the spotlight settings.
        	gl.glPushMatrix();{
        		gl.glDisable(GL2.GL_LIGHTING);
        		gl.glRotated(-90.0, 1.0, 0.0, 0.0);
        		gl.glColor3f(1.0f, 1.0f, 1.0f);       		
        		glut.glutWireCone(3.0 * Math.tan( spotAngle/180.0 * Math.PI ), 5.0, 20, 20);
        		gl.glEnable(GL2.GL_LIGHTING);
        	}gl.glPopMatrix();



        }gl.glPopMatrix();
	}
	
	public void initialiseSeekerVBO(GL2 gl){
   	 
   	 //Generate 1 VBO buffer and get its ID
        gl.glGenBuffers(1,bufferIds,0);
       
   	 //This buffer is now the current array buffer
        //array buffers hold vertex attribute data
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER,bufferIds[0]);
        
        //Load the data
        //If all we care about is the positions
        //gl.glBufferData(GL2.GL_ARRAY_BUFFER, //Type of buffer 
        //           positions.length*Float.BYTES, //size needed
        //           posData,  //The data to load
        //           GL2.GL_STATIC_DRAW); //We don't intend to modify this data after loading it
       
        
        //If we want the colors as well, we have to load both sets of data
        //This is just setting aside enough empty space for all our data
        //we pass in null for the actual data for now
        gl.glBufferData(GL2.GL_ARRAY_BUFFER,    //Type of buffer  
       	        positions.length * Float.BYTES +  colors.length* Float.BYTES, //size needed
       	        null,    //We are not actually loading data here yet
       	        GL2.GL_STATIC_DRAW); //We expect once we load this data we will not modify it


          //Now load the positions data
          gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, 0, //From byte offset 0
       		 positions.length*Float.BYTES,posData);

        //Actually load the color data
        gl.glBufferSubData(GL2.GL_ARRAY_BUFFER,
       		 positions.length*Float.BYTES,  //Load after the position data
       		 colors.length*Float.BYTES,colorData);
        
	}

	public void CleanUp(GL2 gl){
		gl.glDeleteBuffers(1,bufferIds,0);
	}
	
	
	
}