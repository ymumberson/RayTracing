/**
 * Represents a coordinate in 3D space
 * @author Yoshan Mumberson
 *
 */
public class Point {
	/**
	 * x coordinate of the point
	 */
	private double x;
	
	/**
	 * y coordinate of the point
	 */
	private double y;
	
	/**
	 * z coordinate of the point
	 */
	private double z;
	
	/**
	 * Constructor for Point
	 * @param x X-coordinate of the point
	 * @param y Y-coordinate of the point
	 * @param z Z-coordinate of the point
	 */
	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point(Vector v) {
		this.x = v.dx();
		this.y = v.dy();
		this.z = v.dz();
	}
	
	/**
	 * Returns a new point with the smallest (x,y,z) from the given point list.
	 * @param ls
	 * @return
	 */
	public static Point min(Point[] ls) {
		if (ls != null) {
			if (ls.length == 1) {
				return ls[0];
			} else {
				double minx = ls[0].x();
				double miny = ls[0].y();
				double minz = ls[0].z();
				for (int i=1; i<ls.length; i++) {
					if (ls[i].x() < minx) {minx = ls[i].x();}
					if (ls[i].y() < miny) {miny = ls[i].y();}
					if (ls[i].z() < minz) {minz = ls[i].z();}
				}
				return new Point(minx,miny,minz);
			}
		}
		return null;
	}
	
	public static Point max(Point[] ls) {
		if (ls != null) {
			if (ls.length == 1) {
				return ls[0];
			} else {
				double maxx = ls[0].x();
				double maxy = ls[0].y();
				double maxz = ls[0].z();
				for (int i=1; i<ls.length; i++) {
					if (ls[i].x() > maxx) {maxx = ls[i].x();}
					if (ls[i].y() > maxy) {maxy = ls[i].y();}
					if (ls[i].z() > maxz) {maxz = ls[i].z();}
				}
				return new Point(maxx,maxy,maxz);
			}
		}
		return null;
	}
	
	/**
	 * Getter for x
	 * @return
	 */
	public double x() {
		return this.x;
	}
	
	/**
	 * Getter for y
	 * @return
	 */
	public double y() {
		return this.y;
	}
	
	/**
	 * Getter for z
	 * @return
	 */
	public double z() {
		return this.z;
	}
	
	/**
	 * Setter for x
	 * @param x
	 */
	public void setX(double x) {
		this.x = x;
	}
	
	/**
	 * Setter for y
	 * @param y
	 */
	public void setY(double y) {
		this.y = y;
	}
	
	/**
	 * Setter for z
	 * @param z
	 */
	public void setZ(double z) {
		this.z = z;
	}
	
	/**
	 * Points the Point by a single interval according to the given vector
	 * @param vector Vector to move Point by
	 */
	public void move(Point vector) {
		this.x += vector.x;
		this.y += vector.y;
		this.z += vector.z;
	}
	
//	public Point minus(Point p) {
//		return subtract(p);
//	}
	
	public Point subtract(Point p) {
		return new Point(x-p.x(), y-p.y(), z-p.z());
	}
	
	public Point add(Point p) {
		return new Point(x+p.x(), y+p.y(), z+p.z());
	}
	
	public Point multiply(float n) {
		return new Point(x*n,y*n,z*n);
	}
	
	public Point divide(float n) {
		return new Point(x/n,y/n,z/n);
	}
	
	public boolean equals(Point p) {
		return x==p.x() && y==p.y() && z==p.z();
	}
	
	public double euclideanDistance(Point p2) {
		return Math.sqrt(this.euclideanDistanceSquared(p2));
	}
	
	public double euclideanDistanceSquared(Point p2) {
		return Math.pow(this.x-p2.x,2)+Math.pow(this.y-p2.y,2)+Math.pow(this.z-p2.z,2);
	}
	
	/**
	 * Converts Point object to a string for readable output
	 */
	public String toString() {
		return "(" + this.x + "," + this.y + "," + this.z + ")";
	}
	
	public static void main(String[] args) {
		Point p1 = new Point(1,1,1);
		Point p2 = new Point(10,10,10);
		System.out.println(p1.euclideanDistance(p2));
		System.out.println(p2.euclideanDistance(p1));
	}
}

