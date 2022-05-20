package ass2.spec;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileNotFoundException;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;

import javax.swing.JFrame;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.gl2.GLUT;

import toolbox.OBJFileLoader;

//import toolbox.OBJFileLoader;


/**
 * COMMENT: Comment Game
 *
 * @author malcolmr
 */
public class Game extends JFrame implements GLEventListener, KeyListener{

	private Terrain myTerrain;
	private Camera myCamera;
	private OBJFileLoader loader;
	private Seeker seeker;

	

	// constructor for the game object
	public Game(Terrain terrain, Camera camera) {
		super("Assignment 2");
		myTerrain = terrain;
		myCamera = camera;
		seeker = new Seeker(0,4,0);
	}

	/**
	 * Run the game.
	 *
	 */
	public void run() {
		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);
		GLJPanel panel = new GLJPanel();
		panel.addGLEventListener(this);

		panel.addKeyListener(myCamera);
		panel.addMouseMotionListener(myCamera);
		for (Tree t: myTerrain.getMyTrees()){
			panel.addKeyListener(t);
		}
		// Add an animator to call 'display' at 60fps
		FPSAnimator animator = new FPSAnimator(60);
		animator.add(panel);
		animator.start();

		getContentPane().add(panel);
		setSize(800, 600);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	/**
	 * Load a level file and display it.
	 * 
	 * @param args
	 *            - The first argument is a level file in JSON format
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		if (args.length == 0) {
			System.out.println("No arguments given. Exiting");
			System.exit(1);
		}
		Terrain terrain = LevelIO.load(new File(args[0]));
		Camera camera = new Camera();
		Game game = new Game(terrain, camera);
		
		game.run();
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		//Set Sky Colour
		gl.glClearColor(0.52f, 0.81f, 0.92f, 1);
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
		
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();      
        
        setUpLighting(gl);
        
        myCamera.setCameraView(gl);
        //myCamera.setPosition(myCamera.getPosition()[0], (float) myTerrain.altitude(myCamera.getPosition()[0], myCamera.getPosition()[2]) + 1.5f, myCamera.getPosition()[2]);
        checkForPortal();
        //ApplyGravityToCamera();
        
		myTerrain.draw(gl);
		seeker.draw(gl);
		gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

	}

	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		GL2 gl = drawable.getGL().getGL2();
		seeker.CleanUp(gl);
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
    	gl.glEnable(GL2.GL_DEPTH_TEST);
    	
        // enable lighting
        gl.glEnable(GL2.GL_LIGHTING);
        // turn on a light. Use default settings.
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glEnable(GL2.GL_LIGHT1);
        
        // Turn on OpenGL texturing.
        gl.glEnable(GL2.GL_TEXTURE_2D);
        
        // normalise normals (!)
        gl.glEnable(GL2.GL_NORMALIZE);
        // this is necessary to make lighting work properly
        gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		setFocusable(true);												//focuses on the window that is created
		
		//setUpLighting(gl);
		myCamera.setPosition(4, 0, -5);
		
		myTerrain.loadTerrainTextures(drawable);
		myTerrain.initTreeVBOs(gl);
		myTerrain.initPortal();
		myTerrain.initRoads();
		seeker.initialiseSeekerVBO(gl);
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
      
        gl.glFrustum(-0.5, 0.5, -0.5, 0.5, 0.5, 100.0);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
	}

	public void ApplyGravityToCamera(){
		if(myCamera.getPosition()[1] - 1.5f> (float) myTerrain.altitude(myCamera.getPosition()[0], myCamera.getPosition()[2])){
			  myCamera.setPosition(myCamera.getPosition()[0], myCamera.getPosition()[1] - 0.1f, myCamera.getPosition()[2]);
		}
	}
	
	 public void setUpLighting(GL2 gl) {    	
	    	// Light property vectors.
	    	float lightPos[] = { 0.0f, 1.5f, 3.0f, 1.0f };
	    	
	    	float lightAmb[] = { 0.0f, 0.0f, 0.0f, 1.0f };
	    	float lightDifAndSpec[] = { 1.0f, 1.0f, 1.0f, 1.0f };
	    	float globAmb[] = { 0.2f, 0.2f, 0.2f, 1.0f };
	    	
	    	gl.glEnable(GL2.GL_LIGHT0); // Enable particular light source.
	    	
	    	// Set light properties.
	    	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmb,0);
	    	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDifAndSpec,0);
	    	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightDifAndSpec,0);
	    	
	    	//This position gets multiplied by current transformation
	    
	    	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos,0);    	   	

	    	gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globAmb,0); // Global ambient light.
	    	gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL2.GL_TRUE); // Enable two-sided lighting.
	    }
	
	 public void checkForPortal(){
	        if (myCamera.getAvatar() == null){
	        	//must be in first person view
	        	for(Portal p:myTerrain.getMyPortals()){
	        		if(myCamera.getPosition()[1] > p.getY() - p.getRadius() && myCamera.getPosition()[1] < p.getY() + p.getRadius()){
	        			if(myCamera.getPosition()[0] > p.getX() - p.getRadius() && myCamera.getPosition()[0] < p.getX() + p.getRadius()){
	        				if(myCamera.getPosition()[2] > p.getZ() - 0.6 && myCamera.getPosition()[2] < p.getZ() + 0.6){
	        					float direction = 2;
	        					if(myCamera.getPosition()[2] > p.getZ() - 0.6 && myCamera.getPosition()[2] < p.getZ() ){
	        						direction = -2;
	        					} else {
	        						direction = 2;
	        					}
	        					myCamera.setPosition(p.getLinked().getExitX(), p.getLinked().getExitY(), p.getLinked().getExitZ() - direction);
	        				}
	        			}
	        		}
	        	}
	        } else {
	        	//must be in 3rd person view
	        	for(Portal p:myTerrain.getMyPortals()){
	        		if(myCamera.getAvatar().getY() > p.getY() - p.getRadius() && myCamera.getAvatar().getY() < p.getY() + p.getRadius()){
	        			if(myCamera.getAvatar().getX() > p.getX() - p.getRadius() && myCamera.getAvatar().getX() < p.getX() + p.getRadius()){
	        				if(myCamera.getAvatar().getZ() > p.getZ() - 0.6 && myCamera.getAvatar().getZ() < p.getZ() + 0.6){
	        					float direction = 2;
	        					if(myCamera.getAvatar().getZ() > p.getZ() - 0.6 && myCamera.getAvatar().getZ() < p.getZ() ){
	        						direction = -2;
	        					} else {
	        						direction = 2;
	        					}
	        					myCamera.getAvatar().setX(p.getLinked().getExitX());
	        					myCamera.getAvatar().setY(p.getLinked().getExitY());
	        					myCamera.getAvatar().setZ(p.getLinked().getExitZ() - direction);
	        				}
	        			}
	        		}
	        	}
	        }
	 }

    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        System.out.println("Something got pressed");
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }
}
