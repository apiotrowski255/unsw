package ass2.spec;
/**
 * Created by sajidanower23 on 19/10/17.
 */
public class MathUtils {
    public static double[] normalise(double[] vec) {
        double magnitude = calculateMagnitude(vec);
        double[] normalisedVec = new double[vec.length];
        for(int i = 0; i < vec.length; i += 1) {
            normalisedVec[i] = vec[i] / magnitude;
        }
        return normalisedVec;
    }

    private static double calculateMagnitude(double[] vec) {
        double squaredTotal = 0;
        for(double p: vec) {
            squaredTotal += Math.pow(p, 2);
        }
        return Math.sqrt(squaredTotal);
    }

    /**
     * Given a vector, finds a normal vector to it
     * @param p
     * @return
     */
    public static double[] calculateNormal(double[] p) {
        return new double[] {-p[1], 0, p[0]};
    }

    /**
     * Given two points, finds the vector between them
     * @param p
     * @param q
     * @return
     */
    public static double[] makeVector(double[] p, double[] q) {
        return new double[] {q[0] - p[0], 0, q[1] - p[1]};
    }

    public static double[] multiplyVector(double[] p, double factor) {
        double[] returnValue = new double[p.length];
        for(int i = 0; i < p.length; i += 1) {
            returnValue[i] = p[i] * factor;
        }
        return returnValue;
    }

    public static double[] addVector(double[] p, double[] q) {
        p = new double[] {p[0], 0, p[1]};
        double[] returnValue = new double[p.length];
        for(int i = 0; i < p.length; i += 1) {
            returnValue[i] = p[i] + q[i];
        }
        return returnValue;
    }

    public static double[] subtractVector(double[] p, double[] q) {
        p = new double[] {p[0], 0, p[1]};
        double[] returnValue = new double[p.length];
        for(int i = 0; i < p.length; i += 1) {
            returnValue[i] = p[i] - q[i];
        }
        return returnValue;
    }
}
