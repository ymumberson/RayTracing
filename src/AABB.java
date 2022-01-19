import javafx.scene.paint.Color;

/**
 * Represents an Axis-Aligned Bounding Box
 * @author MrYos
 *
 */
public class AABB extends Tracable{
	/**
	 * Minimum coordinate of the box (x,y,z)
	 */
	protected Point min;
	
	/**
	 * Maximum coordinate of the box (x,y,z)
	 */
	protected Point max;
	
	/**
	 * Constructor
	 * @param min Minimum coordinate of bounding box
	 * @param max Maximum coordinate of bounding box
	 */
	public AABB(Point min, Point max) {
		this.min = min;
		this.max = max;
	}
	
	public Point getMin() {
		return min;
	}
	
	public Point getMax() {
		return max;
	}
	
	/**
	 * Finds the coordinate that the ray intersects the bounding box
	 * @param r Ray to check for intersection with box
	 * @return First point of intersection, or null if no intersection
	 */
	public double getIntersect(Ray r) {
		float tmin = (float) ((min.x() - r.o().x()) / (float)r.d().dx());
		float tmax = (float) ((max.x() - r.o().x()) / (float)r.d().dx());
		
		if (tmin > tmax) {
			float temp = tmax;
			tmax = tmin;
			tmin = temp;
		}
		
		float tymin = (float) ((min.y() - r.o().y()) / (float)r.d().dy());
		float tymax = (float) ((max.y() - r.o().y()) / (float)r.d().dy());
		
		if (tymin > tymax) {
			float temp = tymax;
			tymax = tymin;
			tymin = temp;
		}
		
		if ((tmin > tymax) || (tymin > tmax)) {return -1;}
		
		if (tymin > tmin) {
			tmin = tymin;
		}
		
		if (tymax < tmax) {
			tmax = tymax;
		}
		
		float tzmin = (float) ((min.z() - r.o().z()) / (float)r.d().dz());
		float tzmax = (float) ((max.z() - r.o().z()) / (float)r.d().dz());
		
		if (tzmin > tzmax) {
			float temp = tzmax;
			tzmax = tzmin;
			tzmin = temp;
		}
		
		if ((tmin > tzmax) || (tzmin > tmax)) {return -1;}
		
		if (tzmin > tmin) {
			tmin = tzmin;
		}
		
		if (tzmax < tmax) {
			tmax = tzmax;
		}
		
		if (tmin < 0) {
//			System.out.println(tmin + " " + tmax);
			return tmax;
		}
		return tmin;
		
//		return tmin;
//		return new double[] {tmin,tmax};
	}
	
	/**
	 * Checks if a given point is within the bounding box
	 * @param p Point to check
	 * @return True if point is within the bounding box, false otherwise
	 */
	public boolean inBounds(Point p) {
		//Cast to int to account for rounding
		//ie intersection at 4.98968594070 should be at 5.0
		int x = (int) Math.round(p.x());
		int y = (int) Math.round(p.y());
		int z = (int) Math.round(p.z());
		if ((x >= min.x() && x <= max.x()) &&
				(y >= min.y() && y <= max.y()) &&
				(z >= min.z() && z <= max.z())) {
			return true;
		} else {
			return false;
		}
	}
	
	public Color getColor() {
		return Color.BLACK;
	}
	
	public Color getColor(Point p) {
		double height = Math.abs(min.y() - max.y());
		
		//if (!inBounds(p)) System.out.println("Out of bounds");
		
		int x = (int) Math.round(p.x());
		int y = (int) Math.round(p.y());
		
		if (y > min.y() && y < max.y()) {
			//if (y < 0) System.out.println("Orange " + min.y() + " < " + y + " < " + max.y());
			//System.out.println("Orange" + p.y() + " " + height);
			//return Color.ORANGE;
			if (x > min.x() && x < max.x()) {
				return Color.ORANGE;
			} else {
				return Color.DARKOLIVEGREEN;
			}
		} else {
			//System.out.println("Purple: " + p + " / " + height);
			//if (y < 0) System.out.println("Purple " + min.y() + " < " + y + " < " + max.y());
			return Color.DARKMAGENTA;
		}
	}
	
	/**
	 * From: https://developer.mozilla.org/en-US/docs/Games/Techniques/3D_collision_detection
	 * @param box2
	 * @return
	 */
	public boolean overlaps(AABB box2) {
		return (min.x() <= box2.getMax().x() && max.x() >= box2.getMin().x()) &&
				(min.y() <= box2.getMax().y() && max.y() >= box2.getMin().y()) &&
				(min.z() <= box2.getMax().z() && max.z() >= box2.getMin().z());
	}
	
	public String toString() {
		return "Bounding box:" + "\n-> Min corner: " + min + "\n-> Max corner: " + max;
	}

	@Override
	public Vector getNormal(Point p) {
		// TODO Auto-generated method stub
		return new Vector(0,1,0);
	}

	@Override
	public AABB generateAABB() {
		// TODO Auto-generated method stub
		return this;
	}
}
