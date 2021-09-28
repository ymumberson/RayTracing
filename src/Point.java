/**
 * Represents a coordinate in 3D space
 * @author Yoshan Mumberson
 *
 */
public class Point {
	/**
	 * x coordinate of the point
	 */
	private float x;
	
	/**
	 * y coordinate of the point
	 */
	private float y;
	
	/**
	 * z coordinate of the point
	 */
	private float z;
	
	/**
	 * Constructor for Point
	 * @param x X-coordinate of the point
	 * @param y Y-coordinate of the point
	 * @param z Z-coordinate of the point
	 */
	public Point(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point(Vector v) {
		this.x = (float) v.dx();
		this.y = (float) v.dy();
		this.z = (float) v.dz();
	}
	
	/**
	 * Getter for x
	 * @return
	 */
	public float x() {
		return this.x;
	}
	
	/**
	 * Getter for y
	 * @return
	 */
	public float y() {
		return this.y;
	}
	
	/**
	 * Getter for z
	 * @return
	 */
	public float z() {
		return this.z;
	}
	
	/**
	 * Setter for x
	 * @param x
	 */
	public void setX(float x) {
		this.x = x;
	}
	
	/**
	 * Setter for y
	 * @param y
	 */
	public void setY(float y) {
		this.y = y;
	}
	
	/**
	 * Setter for z
	 * @param z
	 */
	public void setZ(float z) {
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
	
	/**
	 * Converts Point object to a string for readable output
	 */
	public String toString() {
		return "(" + this.x + "," + this.y + "," + this.z + ")";
	}
}
