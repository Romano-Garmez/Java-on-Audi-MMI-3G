package org.placelab.util.swt;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Pocket PCs don't have java.awt.geom.AffineTransform so I made this
 * 
 */
public class AffineTransform {
    
    public static class DoublePoint {
        public double x, y;
        public DoublePoint(double xi, double yi) {
            x = xi;
            y = yi;
        }
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    public static class NotInvertibleException extends Exception {
        public NotInvertibleException() { super(); }
        public NotInvertibleException(String msg) {
            super(msg);
        }
    }
    
    // start with the identity matrix
    protected double[][] matrix; 
    protected AffineTransform inverse;
    protected boolean inverseValid = false;
    
    
    public static AffineTransform getRotateInstance(double theta) {
        return new AffineTransform(new double[][] {
                { Math.cos(theta), -Math.sin(theta), 0 },
                { Math.sin(theta), Math.cos(theta), 0 },
                { 0, 0, 1 }
        });
    }
    
    public static AffineTransform getRotateInstance(double theta, double anchorX, double anchorY) {
        AffineTransform ret = getTranslateInstance(-anchorX, -anchorY);
        ret.concatenate(getRotateInstance(theta));
        ret.concatenate(getTranslateInstance(anchorX, anchorY));
        return ret;
    }
    
    public static AffineTransform getScaleInstance(double sx, double sy) {
        return new AffineTransform(new double[][] {
                { sx, 0, 0 },
                { 0, sy, 0 },
                { 0, 0, 1} 
        });
    }
    
    public static AffineTransform getShearInstance(double shx, double shy) {
        return new AffineTransform(new double[][] {
                { 1, shx, 0 },
                { shy, 1, 0 },
                { 0, 0, 1 }
        });
    }
    
    public static AffineTransform getTranslateInstance(double tx, double ty) {
        return new AffineTransform(new double[][] {
                { 1, 0, tx },
                { 0, 1, ty },
                { 0, 0, 1 }
        });
    }
    
    /**
     * Returns the AffineTransform for the identity matrix
     */
    public AffineTransform() {
        matrix = new double[][] { 
                { 1, 0, 0 }, 
                { 0, 1, 0 }, 
                { 0, 0, 1 } };
    }
    
    public AffineTransform(AffineTransform Tx) {
        this(Tx.matrix);
    }
    
    public AffineTransform(double m00, double m10, double m01, double m11, double m02, double m12) {
        matrix = new double[][] { 
                { m00, m01, m02 }, 
                { m10, m11, m12 }, 
                { 0, 0, 1 } };
    }
    
    public AffineTransform(double[][] matrix) {
        //this.matrix = copyMatrix(matrix);
        this.matrix = matrix;
    }
    
    public Object clone() {
        return new AffineTransform(this);
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for(int i = 0; i < matrix.length; i++) {
            sb.append("[");
            for(int j = 0; j < matrix[i].length; j++) {
                sb.append(matrix[i][j]);
                if(j < matrix[i].length - 1) sb.append(",");
            }
            sb.append("]");
        }
        sb.append("]");
        return sb.toString();
    }
    
    public void concatenate(AffineTransform Tx) {
        // the order is important in matrix multiplication
        this.matrix = multiply(this.matrix, Tx.matrix);
        inverseValid = false;
    }
    
    public void preConcatenate(AffineTransform Cx) {
        this.matrix = multiply(Cx.matrix, this.matrix);
        inverseValid = false;
    }
    
    public Point inverseTransform(Point src) throws NotInvertibleException {
        if(!inverseValid) inverse = this.createInverse();
        return inverse.transform(src);
    }
    
    public DoublePoint inverseTransform(DoublePoint src) throws NotInvertibleException {
        if(!inverseValid) inverse = this.createInverse();
        return inverse.transform(src);
    }
    
    public Point transform(Point src) {
        double[][] result = multiply(matrix, new double[][] { {src.x} , {src.y} , {1.0} });
        return new Point((int)result[0][0], (int)result[1][0]);
    }
    
    public DoublePoint transform(DoublePoint src) {
        double[][] result = multiply(matrix, new double[][] { {src.x} , {src.y} , {1.0} });
        return new DoublePoint(result[0][0], result[1][0]);
    }
    
    public double[][] getMatrix() {
        return copyMatrix(matrix);
    }
    
    public double getScaleX() {
        return matrix[0][0];
    }
    
    public double getScaleY() {
        return matrix[1][1];
    }
    
    public double getTranslateX() {
        return matrix[0][2];
    }
    
    public double getTranslateY() {
        return matrix[1][2];
    }
    
    public double getShearX() {
        return matrix[0][1];
    }
    
    public double getShearY() {
        return matrix[1][0];
    }
    
    public static double[][] copyMatrix(double[][] a) {
        int n = a.length;
        double[][] ret = new double[n][n];
        for(int i = 0; i < ret.length; i++) {
            System.arraycopy(a[i], 0, ret[i], 0, a[i].length);
        }
        return ret;
    }
    
    public AffineTransform createInverse() throws NotInvertibleException {
        return new AffineTransform(invert(this.matrix));
    }
    
    // oof, flashbacks to matrix algebra class
    public static double[][] multiply(double[][] a, double[][] b) {
        // rows of a must be the same length as the columns of b
        
        if(!(a.length > 0 && a[0].length == b.length)) 
            throw new IllegalArgumentException("(n x m)(m x p) = (n x p)");
        
        double[][] c = new double[a.length][b[0].length];
        
        for(int i = 0; i < c.length; i++) {
            for(int j = 0; j < c[0].length; j++) {
                double sum = 0;
                for(int k = 0; k < a[i].length; k++) {
                    sum += a[i][k] * b[k][j];
                }
                c[i][j] = sum;
            }
        }
        
        return c;
    }
    
    
    
    // man, if multiply was tricky, this is going to kill me
    // based on the implementation in Numerical Recipes in C pg 39
    // minus all the 1 indexed nonsense.
    public static double[][] invert(double[][] a) throws NotInvertibleException {
        if(!(a.length > 0 && a.length == a[0].length))
            throw new NotInvertibleException("can only invert square matrices");
        int[] indxc, indxr, ipiv;
        double pivinv = 0.0;
        int n = a.length;
        int icol = 0, irow = 0;
        double[][] ret = copyMatrix(a);
        
        // these guys are used for bookkeeping on the pivoting
        indxc = new int[n];
        indxr = new int[n];
        ipiv = new int[n];
        
        for(int j = 0; j < n; j++) indxc[j] = 0;
        
        for(int i = 0; i < n; i++) { // main loop over the columns to be reduced
            double big =  0.0;
            for(int j = 0; j < n; j++) {
                if(ipiv[j] != 1) {
                    for(int k = 0; k < n; k++) {
                        if(ipiv[k] == 0) {
                            if(Math.abs(ret[j][k]) >= big) {
                                big = Math.abs(ret[j][k]);
                                irow = j;
                                icol = k;
                            }
                        } else if(ipiv[k] > 1.0) throw new NotInvertibleException("singular matrix - 1");
                    }
                }
            }
            ipiv[icol]++;
            if(irow != icol) {
                for(int l = 0; l < n; l++) {
                    double temp = ret[irow][l];
                    ret[irow][l] = ret[icol][l];
                    ret[icol][l] = temp;
                }
            }
            indxr[i] = irow;
            indxc[i] = icol;
            if(ret[icol][icol] == 0.0) throw new NotInvertibleException("singular matrix - 2");
            pivinv = 1.0 / ret[icol][icol];
            ret[icol][icol] = 1.0;
            for(int l = 0; l < n; l++) ret[icol][l] *= pivinv;
            // reduce the rows (except for the pivot one)
            for(int ll = 0; ll < n; ll++) {
                if(ll != icol) {
                    double dum = ret[ll][icol];
                    ret[ll][icol] = 0.0;
                    for(int l = 0; l < n; l++) ret[ll][l] -= ret[icol][l] * dum;
                }
            }
        } // end main column loop
        
        // now unscramble the columns
        for(int l = n - 1; l >= 0; l--) {
            if(indxr[l] != indxc[l]) {
                for(int k = 0; k < n; k++) {
                    double temp = ret[k][indxr[l]];
                    ret[k][indxr[l]] = ret[k][indxc[l]];
                    ret[k][indxc[l]] = temp;
                }
            }
        }
        
        return ret;
        
    }
    
    
    // some handy utility stuff
    
    /**
     * Supposing we applied this AffineTransform to the passed in Rectangle, 
     * this returns the rectangle that would be necessary to just contain
     * the translated one
     */
    public Rectangle getBoundingRect(Rectangle rect) {
        Point[] originalPoints = new Point[] {
                new Point(rect.x, rect.y),
                new Point(rect.x, rect.y + rect.height),
                new Point(rect.x + rect.width, rect.y),
                new Point(rect.x + rect.width, rect.y + rect.height)
        };
        
        Point[] transformed = transformPoints(originalPoints);
        
        int minX = transformed[0].x, maxX = transformed[0].x,
        	minY = transformed[0].y, maxY = transformed[0].y;
        
        for(int i = 1; i < transformed.length; i++) {
            minX = Math.min(minX, transformed[i].x);
            maxX = Math.max(maxX, transformed[i].x);
            minY = Math.min(minY, transformed[i].y);
            maxY = Math.max(maxY, transformed[i].y);
        }
        
        return new Rectangle(minX, minY, maxX - minX, maxY - minY);   
    }
    
    public Point[] transformPoints(Point[] points) {
        Point[] ret = new Point[points.length];
        for(int i = 0; i < points.length; i++) {
            ret[i] = this.transform(points[i]);
        }
        return ret;
    }
    
    public static int NEAREST_NEIGHBOR = 0;
    
    /**
     * Transforms an image according to this AffineTransform using the
     * specified algorithm for transformation (only NEAREST_NEIGHBOR is
     * currently implemented)
     * 
     * Note that the resulting image is the smallest possible image that
     * can contain the transformed image.  So if there is a translation
     * associated with this transform you will need to then draw the resulting
     * image with the appropriate x and y offsets to complete the transform
     * onscreen.
     * 
     * @param original the image to transform
     * @param method the interpolation technique
     * @return a transformed version of the image interpolated 
     */
    public ImageData transformImage(ImageData original, int method) throws NotInvertibleException {        
        Rectangle originalRect = new Rectangle(0, 0, original.width, original.height);
        Rectangle transformRect = this.getBoundingRect(originalRect);
        /*if(paintTimes == 0) {
            System.out.println("transform rect: " + transformRect);
            System.out.println("original  rect: " + originalRect);
            System.out.println("transform: " + this.toString());
            System.out.println("inverse: " + this.createInverse().toString());
        }*/
        
        ImageData transformed = new ImageData(transformRect.width, transformRect.height,
                original.depth, original.palette);
        
        int xOffset = transformRect.x;
        int yOffset = transformRect.y;
        
        for(int x = 0; x < transformRect.width; x++) {
            for(int y = 0; y < transformRect.height; y++) {
                transformed.setAlpha(x, y, 255);
            }
        }
        
        
        // interpolation strategy is for each point in destination (transformed)
        // invert it to see where it maps to the original.
        // if the mapping is outside the bounds of the original, draw transparent
        // otherwise draw point (or combination of points depending on interpolation
        // method) in the destination image
        for(int x = 0; x < transformRect.width; x++) {
            for(int y = 0; y < transformRect.height; y++) {
                DoublePoint transformP = new DoublePoint(x + xOffset, y + yOffset);
                DoublePoint originalP = this.inverseTransform(transformP);
                // can add other interpolation schemes here (nearest neighbor is pretty good for non-aa text, though,
                // which is my goal right now)
                Point loc = nearestNeighbor(originalP);
                if(loc.x < 0 || loc.y < 0 || loc.x >= original.width || loc.y >= original.height) {
                    // inverse transformed point is not in source image, so draw transparent
                    transformed.setAlpha(x, y, 0);
                    /*if(x <= 1 && y <= 1 && paintTimes == 0) 
                        System.out.println("not in: transformP " + transformP +
                            " originalP " + originalP);*/
                } else {
                    // there is a mapping to a point   
                    //System.out.println("setting " + x + "," + y + " to original " + loc.x + "," + loc.y);
                    transformed.setPixel(x, y, original.getPixel(loc.x, loc.y));
                    transformed.setAlpha(x, y, original.getAlpha(loc.x, loc.y));
                }
            }
        }
        paintTimes++;
        return transformed;
    }
    
    private int paintTimes = 0;
    
    private Point nearestNeighbor(DoublePoint dp) {
        return new Point((int)Math.round(dp.x), (int)Math.round(dp.y));
    }
    
}
