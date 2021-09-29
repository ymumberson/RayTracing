/**
 * Represents a ray for raytracing
 * @author Yoshan Mumberson
 *
 */
public class Ray {
	/**
	 * Origin of ray
	 */
	private Point origin;
	
	/**
	 * Direction of ray
	 */
	private Vector direction;
	
	/**
	 * Constructor for ray
	 * @param origin As origin
	 * @param direction as Direction
	 */
	public Ray(Point origin, Vector direction) {
		this.origin = origin;
		this.direction = direction;
	}
	
	/**
	 * Calculates a point along the ray at a given distance from the origin.
	 * Positive distance represents in from of ray, negative distance represents behind ray 
	 * @param distance Distance from origin
	 * @return Point along ray at given distance from origin
	 */
	public Point getPoint(double distance) {
		double x = (origin.x() + direction.dx()*distance);
		double y = (origin.y() + direction.dy()*distance);
		double z = (origin.z() + direction.dz()*distance);
		return new Point(x,y,z);
	}
	
	/**
	 * Getter for direction
	 * @return
	 */
	public Vector d() {
		return this.direction;
	}
	
	/**
	 * Getter for origin
	 * @return
	 */
	public Point o() {
		return this.origin;
	}
	
	public String toString() {
		return "Origin: " + origin + ", Direction: " + direction;
	}
}
