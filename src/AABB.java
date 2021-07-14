import javafx.scene.paint.Color;

/**
 * Represents an Axis-Aligned Bounding Box
 * @author MrYos
 *
 */
public class AABB {
	/**
	 * Minimum coordinate of the box (x,y,z)
	 */
	private Point min;
	
	/**
	 * Maximum coordinate of the box (x,y,z)
	 */
	private Point max;
	
	/**
	 * Constructor
	 * @param min Minimum coordinate of bounding box
	 * @param max Maximum coordinate of bounding box
	 */
	public AABB(Point min, Point max) {
		this.min = min;
		this.max = max;
	}
	
	/**
	 * True if the given ray intersects the bounding box
	 * @param r Ray to check for intersection with box
	 * @return True if ray intersects the box
	 */
	public Boolean doesIntersect(Ray r) {
		return this.getIntersect(r) >= 0;
	}
	
	/**
	 * Finds the coordinate that the ray intersects the bounding box
	 * @param r Ray to check for intersection with box
	 * @return First point of intersection, or null if no intersection
	 */
	public double getIntersect(Ray r) {
		//Assign to Float.NaN as default to say "no intersection found yet"
		float dmin = Float.NaN; //First intersection of box
		float dmax = Float.NaN; //Second intersection of box
		
		//Calculates the intersection in the x-axis
		if (r.d().dx() != 0) { //Skips if there's no vector component in the x-axis
			float xmin = (float) ((min.x()-r.o().x())/r.d().dx());
			float xmax = (float) ((max.x()-r.o().x())/r.d().dx());
			//Swaps min with max to account for different ray origin directions
			if (xmin > xmax) {
				float temp = xmin;
				xmin = xmax;
				xmax = temp;
			}
			
			dmin = xmin;
			dmax = xmax;
		}
		
		//Calculates the intersection in the y-axis
		if (r.d().dy() != 0) { //Skips if there's no vector component in the y-axis
			float ymin = (float) ((min.y()-r.o().y())/r.d().dy());
			float ymax = (float) ((max.y()-r.o().y())/r.d().dy());
			//Swaps min with max to account for different ray origin directions
			if (ymin > ymax) {
				float temp = ymin;
				ymin = ymax;
				ymax = temp;
			}
			
			//Would mean ray missed the box
			if (dmin > ymax || ymin > dmax) {
				return -1;
			}

			if (ymin > dmin || Float.isNaN(dmin)) {
				dmin = ymin;
			}
			if (ymax > dmax || Float.isNaN(dmax)) {
				dmax = ymax;
			}
		}
		
		//Calculates the intersection in the z-axis
		if (r.d().dz() != 0) { //Skips if there's no vector component in the z-axis
			float zmin = (float) ((min.z()-r.o().z())/r.d().dz());
			float zmax = (float) ((max.z()-r.o().z())/r.d().dz());
			//Swaps min with max to account for different ray origin directions
			if (zmin > zmax) {
				float temp = zmin;
				zmin = zmax;
				zmax = temp;
			}
			
			//Would mean ray missed the box
			if (dmin > zmax || zmin > dmax) {
				return -1;
			}
			
			if (zmin > dmin || Float.isNaN(dmin)) {
				dmin = zmin;
			}
			if (zmax > dmax || Float.isNaN(dmax)) {
				dmax = zmax;
			}
		}
		
		if (dmin < 0 && dmax >= 0) {
			dmin = dmax;
		}
		
		//Return first point of intersection
		if (dmin != Float.NaN) {
			return dmin;
		} else {
			return -1;
		}
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
		if (p.y() > min.y() && p.y() < max.y()) {
			//System.out.println("Orange" + p.y() + " " + height);
			return Color.ORANGE; 
		} else {
			System.out.println("Purple: " + p.y() + " / " + height);
			return Color.DARKMAGENTA;
		}
	}
	
	public String toString() {
		return "Bounding box:" + "\n-> Min corner: " + min + "\n-> Max corner: " + max;
	}
}
