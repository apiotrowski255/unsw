package ass2.spec;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;

import toolbox.Vector3f;

/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */
public class Terrain {

    private Dimension mySize;
    private double[][] myAltitude;
    private List<Tree> myTrees;
    private List<Road> myRoads;
    private List<Portal> myPortals;
    private float[] mySunlight;
    
    private String textureFileName1 = "res/grass.png";
    private String textureExt1 = "png";
    private String textureFileName2 = "res/tree.png";
    private String textureExt2 = "png";
    private String textureFileName3 = "res/portal.png";
    private String textureExt3 = "png";
    private final int NUM_TEXTURES = 3;
	private int imageSize = 64;
    private static final int rgba = 4;
	private MyTexture myTextures[];
	

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth) {
        mySize = new Dimension(width, depth);
        myAltitude = new double[width][depth];
        myTrees = new ArrayList<Tree>();
        myRoads = new ArrayList<Road>();
        myPortals = new ArrayList<Portal>();
        mySunlight = new float[3];
    }
    
    public Terrain(Dimension size) {
        this(size.width, size.height);
    }

    public Dimension size() {
        return mySize;
    }

    public List<Tree> trees() {
        return myTrees;
    }

    public List<Road> roads() {
        return myRoads;
    }

    public float[] getSunlight() {
        return mySunlight;
    }

    /**
     * Set the sunlight direction. 
     * 
     * Note: the sun should be treated as a directional light, without a position
     * 
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        mySunlight[0] = dx;
        mySunlight[1] = dy;
        mySunlight[2] = dz;        
    }
    
    /**
     * Resize the terrain, copying any old altitudes. 
     * 
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        mySize = new Dimension(width, height);
        double[][] oldAlt = myAltitude;
        myAltitude = new double[width][height];
        
        for (int i = 0; i < width && i < oldAlt.length; i++) {
            for (int j = 0; j < height && j < oldAlt[i].length; j++) {
                myAltitude[i][j] = oldAlt[i][j];
            }
        }
    }

    /**
     * Get the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
    	if(x < 0 || x > myAltitude.length -1 || z < 0 || z > myAltitude.length-1 ){
    		return 0;
    	} else {
    		return myAltitude[x][z];
    	}
    }

    /**
     * Set the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, double h) {
        myAltitude[x][z] = h;
    }

    /**
     * Get the altitude at an arbitrary point. 
     * Non-integer points should be interpolated from neighbouring grid points
     * 
     * TO BE COMPLETED
     * Completed on 21/9/17
     * Tasks: to be checked and confirmed
     *
     * @param x
     * @param z
     * @return
     */
    public double altitude(double x, double z) {
        
        //case to handle, if x and z are outside the grid, what to return?
        
        int x1 = (int) Math.floor(x);
        int z1 = (int) Math.floor(z);
        
        int x2 = (int) Math.ceil(x);
        int z2 = (int) Math.ceil(z);
        
        //At this point the difference between x1 and x2 should be 1
        //the difference between z1 and z2 should also be 1
        //x2 should be larger than x1 and z2 should be larger than z1
        
        //Now that we have four int, we can now get four points
        double topLeftCorner = getGridAltitude(x1, z1);
        double bottomLeftCorner = getGridAltitude(x1, z2);
        double topRightCorner = getGridAltitude(x2, z1);
        double bottomRightCorner = getGridAltitude(x2, z1);
       
        
        //find the gradient from left to right
        double gradientTop = topRightCorner - topLeftCorner;
        double gradientBottom = bottomRightCorner - bottomLeftCorner;

        double topPoint = gradientTop * (x - x1) + topLeftCorner;
        double bottomPoint = gradientBottom * (x - x1) + bottomLeftCorner;
        
        //find the gradient from bottom to top
        double gradientVertical = bottomPoint - topPoint;
        
        return gradientVertical * (z-z1) + topPoint;
    }

    /**
     * Add a tree at the specified (x,z) point. 
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     * 
     * @param x
     * @param z
     */
    public void addTree(double x, double z) {
        double y = altitude(x, z);
        Tree tree = new Tree(x, y, z);
        myTrees.add(tree);
    }


    /**
     * Add a road. 
     * 
     * @param width
     * @param spine
     */
    public void addRoad(double width, double[] spine) {
        Road road = new Road(width, spine);
        myRoads.add(road);        
    }
    
    public void draw(GL2 gl){
    	
		// Material property vectors.
		float lightPos[] = { getSunlight()[0], getSunlight()[1], getSunlight()[2], 1.0f };
    	
    	float lightAmb[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    	float lightDifAndSpec[] = { 1.0f, 1.0f, 1.0f, 1.0f };
    	float globAmb[] = { 0.2f, 0.2f, 0.2f, 1.0f };
        
  
    	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmb,0);
    	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDifAndSpec,0);
    	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightDifAndSpec,0);
    	
    	//This position gets multiplied by current transformation
    
    	gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos,0);    	   	

    	gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, globAmb,0); // Global ambient light.
    	
    	gl.glBindTexture(GL2.GL_TEXTURE_2D, myTextures[0].getTextureId());   
    	gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);

    	int s = 2;
    	
    	gl.glBegin(gl.GL_TRIANGLES);
    	{
    		for(int x = 0;x < mySize.width - 1 ; x++){
    			for(int z = 0; z < mySize.height - 1;z++){ 
        			
    				Vector3f normal = calculateNormal(x,z);
    				gl.glTexCoord2d(0, 0);
    				gl.glNormal3d(normal.x, normal.y, normal.z);
			    	gl.glVertex3d(x,getGridAltitude(x,z),z); // P0
			    	
			    	normal = calculateNormal(x,z+1);
			    	gl.glTexCoord2d(0, 1);
			    	gl.glNormal3d(normal.x, normal.y, normal.z);
		    		gl.glVertex3d(x,getGridAltitude(x,z+1),z+1);// P1
		    		
			    	normal = calculateNormal(x+1,z);
			    	gl.glTexCoord2d(1, 0);
			    	gl.glNormal3d(normal.x, normal.y, normal.z);
		    		gl.glVertex3d(x+1,getGridAltitude(x+1,z),z); // P2
			    	
			    	normal = calculateNormal(x,z+1);
			    	gl.glTexCoord2d(0, 1);
			    	gl.glNormal3d(normal.x, normal.y, normal.z);
			    	gl.glVertex3d(x,getGridAltitude(x,z+1),z+1);// P1
			    	
			    	normal = calculateNormal(x+1,z+1);
			    	gl.glTexCoord2d(1, 1);
			    	gl.glNormal3d(normal.x, normal.y, normal.z);
		    		gl.glVertex3d(x+1, getGridAltitude(x+1, z+1),z+1); // P3
		    		
			    	normal = calculateNormal(x+1,z);
			    	gl.glTexCoord2d(1, 0);
			    	gl.glNormal3d(normal.x, normal.y, normal.z);
		    		gl.glVertex3d(x+1, getGridAltitude(x+1, z),z); // P2
    			}
    		}
    	}
    	gl.glEnd();
    	
    	gl.glBindTexture(GL2.GL_TEXTURE_2D, myTextures[1].getTextureId()); 
    	gl.glTexCoord2d(0.75, 0.25);
    	drawAllTrees(gl);
    	gl.glBindTexture(GL2.GL_TEXTURE_2D, myTextures[2].getTextureId()); 
    	drawAllPortals(gl);
    	drawAllRoads(gl);
    	gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL); 
    
    }

    private Vector3f calculateNormal(int x, int z) {
		float heightL = (float) getGridAltitude(x - 1, z);
		float heightR = (float) getGridAltitude(x + 1, z);
		float heightD = (float) getGridAltitude(x, z - 1);
		float heightU = (float) getGridAltitude(x, z + 1);
		Vector3f Normal = new Vector3f(heightL - heightR, 2f, heightD - heightU);
		Normal.normalise();
		return Normal;
	}
	
	private void drawAllTrees(GL2 gl){
		for(Tree t:myTrees){
			t.draw(gl);
		}
	}
	
	private void drawAllRoads(GL2 gl){
		for(Road r:myRoads){
			//r.draw(gl);
		}
	}
	
	private void drawAllPortals(GL2 gl){
		for(Portal p:myPortals){
			p.draw(gl);
		}
	}
	
	public void loadTerrainTextures(GLAutoDrawable drawable){
		GL2 gl = drawable.getGL().getGL2();
    	gl.glClearColor(0.8f, 0.8f, 0.8f, 0.0f);
    	
    	gl.glEnable(GL2.GL_DEPTH_TEST); // Enable depth testing.
    	
    	// Turn on OpenGL texturing.
    	gl.glEnable(GL2.GL_TEXTURE_2D);
   
    	//Load in textures from files
    	myTextures = new MyTexture[NUM_TEXTURES];
    	myTextures[0] = new MyTexture(gl,textureFileName1,textureExt1,true);
    	myTextures[1] = new MyTexture(gl,textureFileName2,textureExt2,true);
    	myTextures[2] = new MyTexture(gl,textureFileName3,textureExt3,true);
    	
    	for(Road r:myRoads){
    		//r.loadTextures(gl);
    	}
	}
	
	public void initTreeVBOs(GL2 gl){
		for(Tree t:myTrees){
			t.init(gl);
		}
	}
	
	public void initPortal(){
		Portal p1 = new Portal(0,1,0);
		Portal p2 = new Portal(10,1,6);
		p1.setLinked(p2);
		p2.setLinked(p1);
		
		myPortals.add(p1);
		myPortals.add(p2);
	}
	
	public void initRoads(){
		for(Road r: myRoads){
			//r.linkTerrain(this);
		}
	}

	public List<Tree> getMyTrees() {
		return myTrees;
	}

	public void setMyTrees(List<Tree> myTrees) {
		this.myTrees = myTrees;
	}
	
	public List<Portal> getMyPortals() {
		return myPortals;
	}
		
}
