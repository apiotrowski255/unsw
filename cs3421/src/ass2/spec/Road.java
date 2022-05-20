package ass2.spec;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import ass2.spec.MathUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * COMMENT: Comment Road 
 *
 * @author malcolmr
 */
public class Road {

    private List<Double> myPoints;
    private double myWidth;
    
    /** 
     * Create a new road starting at the specified point
     */
    public Road(double width, double x0, double y0) {
        myWidth = width;
        myPoints = new ArrayList<Double>();
        myPoints.add(x0);
        myPoints.add(y0);
    }

    /**
     * Create a new road with the specified spine 
     *
     * @param width
     * @param spine
     */
    public Road(double width, double[] spine) {
        myWidth = width;
        myPoints = new ArrayList<Double>();
        for (int i = 0; i < spine.length; i++) {
            myPoints.add(spine[i]);
        }
    }

    /**
     * The width of the road.
     * 
     * @return
     */
    public double width() {
        return myWidth;
    }

    /**
     * Add a new segment of road, beginning at the last point added and ending at (x3, y3).
     * (x1, y1) and (x2, y2) are interpolated as bezier control points.
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     */
    public void addSegment(double x1, double y1, double x2, double y2, double x3, double y3) {
        myPoints.add(x1);
        myPoints.add(y1);
        myPoints.add(x2);
        myPoints.add(y2);
        myPoints.add(x3);
        myPoints.add(y3);        
    }
    
    /**
     * Get the number of segments in the curve
     * 
     * @return
     */
    public int size() {
        return myPoints.size() / 6;
    }

    /**
     * Get the specified control point.
     * 
     * @param i
     * @return
     */
    public double[] controlPoint(int i) {
        double[] p = new double[2];
        p[0] = myPoints.get(i*2);
        p[1] = myPoints.get(i*2+1);
        return p;
    }
    
    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     * 
     * @param t
     * @return
     */
    public double[] point(double t) {
        int i = (int)Math.floor(t);
        t = t - i;
        
        i *= 6;
        
        double x0 = myPoints.get(i++);
        double y0 = myPoints.get(i++);
        double x1 = myPoints.get(i++);
        double y1 = myPoints.get(i++);
        double x2 = myPoints.get(i++);
        double y2 = myPoints.get(i++);
        double x3 = myPoints.get(i++);
        double y3 = myPoints.get(i++);
        
        double[] p = new double[2];

        p[0] = b(0, t) * x0 + b(1, t) * x1 + b(2, t) * x2 + b(3, t) * x3;
        p[1] = b(0, t) * y0 + b(1, t) * y1 + b(2, t) * y2 + b(3, t) * y3;        
        
        return p;
    }
    /**
     * Calculate the Bezier coefficients
     *
     * @param i
     * @param t
     * @return
     */
    private double b(int i, double t) {

        switch(i) {

        case 0:
            return (1-t) * (1-t) * (1-t);

        case 1:
            return 3 * (1-t) * (1-t) * t;

        case 2:
            return 3 * (1-t) * t * t;

        case 3:
            return t * t * t;
        }

        // this should never happen
        throw new IllegalArgumentException("" + i);
    }
    public void draw(GL2 gl) {
        gl.glColor4d(0.0, 0.0, 0.0, 1);
//        gl.glLoadIdentity();
// debug points
//        gl.glPointSize(10);
//
//        gl.glBegin(GL2.GL_POINTS);
//        {
//            gl.glColor3d(1, 0, 0);
//            gl.glVertex2dv(controlPoint(0),0);
//            gl.glColor3d(0, 1, 1);
//            gl.glVertex2dv(controlPoint(1),0);
//            gl.glColor3d(0, 1, 0);
//            gl.glVertex2dv(controlPoint(2),0);
//            gl.glColor3d(0,0,0);
//            gl.glVertex2dv(controlPoint(3),0);
//        }
//        gl.glEnd();

        //Draw the bezier curve.
        //We are just using the values as x and y values
        //and using a z of 0.

//        gl.glPointSize(5);
        gl.glPushMatrix();
        gl.glBegin(GL2.GL_LINE_STRIP);
        gl.glColor3d(1, 0, 0);
//        int length = this.myPoints.size();
//        for(double t = 0.0; t < this.size(); t += 0.01) {
//            double[] head = point(t);
//            double[]
//        }


        int numPoints = (this.myPoints.size() + 1) * 2 ;
        double tIncrement = 1.0 / numPoints;
        double iIncrement = 0.01;
        for(double i = 0; i < numPoints; i += iIncrement) {
            double t = i * tIncrement;
            double[] head = this.point(t);
            double[] next;
            if(i + iIncrement >= numPoints) {
                next = this.point(0);
                continue;
            } else {
                next = this.point((i + iIncrement) * tIncrement);
            }
            double[] vec = MathUtils.makeVector(head, next);
            double[] normal = MathUtils.calculateNormal(vec);
            normal = MathUtils.normalise(normal);
            double[] multipliedVector = MathUtils.multiplyVector(normal, this.myWidth / 2);
            double[] topLeft = MathUtils.addVector(next, multipliedVector);
            double[] topRight = MathUtils.subtractVector(next, multipliedVector);
            double[] bottomLeft = MathUtils.addVector(head, multipliedVector);
            double[] bottomRight = MathUtils.subtractVector(next, multipliedVector);
            gl.glPushMatrix();
            gl.glBegin(GL.GL_POLYGON_OFFSET_UNITS);
            {
                gl.glVertex2dv(topLeft,0);
                gl.glVertex2dv(topRight,0);
                gl.glVertex2dv(bottomRight,0);
                gl.glVertex2dv(bottomLeft,0);
            }
            gl.glPopMatrix();
//            double[] toDraw;
//            gl.glVertex2dv(normal,0);
        }
        //Connect to the final point - we just get the final control
        //point as getting the the road.point at road.size() gives us
        //an error
//        gl.glVertex2dv(controlPoint(3),0);

        gl.glEnd();
        gl.glPopMatrix();
    }
}
