public class Rectangle extends Tracable{
	private final boolean VALID_RECT;
	private Triangle t1,t2;
	
	public Rectangle(Triangle t1, Triangle t2) {
		this.t1 = t1;
		this.t2 = t2;
		
		if (t1.getNormal().equals(t2.getNormal())) {
			VALID_RECT = true;
		} else {
			VALID_RECT = false;
			System.out.println(this);
		}
	}
	
	public Rectangle(Point topLeft, Point topRight, Point bottomRight, Point bottomLeft) {
		t1 = new Triangle(topLeft,bottomRight,bottomLeft);
		t2 = new Triangle(topLeft,topRight,bottomRight);
		
		if (t1.getNormal().equals(t2.getNormal())) {
			VALID_RECT = true;
		} else {
			VALID_RECT = false;
			System.out.println(this);
		}
	}
	
	@Override
	public double getIntersect(Ray r) {
		if (VALID_RECT) {
			double intersect1 = t1.getIntersect(r);
			double intersect2 = t2.getIntersect(r);
			if (intersect1 > intersect2) { //Can't intersect them both so larger value is the one it intersected
				return intersect1;
			} else {
				return intersect2;
			}
			//return Math.max(t1.getIntersect(r), t2.getIntersect(r));
		} else {
			return -1; //ie false
		}
	}
	
	@Override
	public Vector getNormal(Point p) {
		return getNormal();
	}
	
	public Vector getNormal() {
		return t1.getNormal();
	}
	
	public String toString() {
		if (VALID_RECT) {
			return "Rectangle: " +
				"\n-> t1: " + t1 +
				"\n-> t2: " + t2;	
		} else {
			return "Invalid Rectangle. (Rectangle is not flat)";
		}
	}

	@Override
	public AABB generateAABB() {
//		Point[] ls = new Point[] {t1.getP0(),t1.getP1(),t1.getP2(),t2.getP0(),t2.getP1(),t2.getP2()};
//		Point min = Point.min(ls);
//		Point max = Point.min(ls);
//		return new AABB(min,max);
		
		AABB b1 = t1.generateAABB();
		AABB b2 = t2.generateAABB();
		Point min = Point.min(new Point[]{b1.min,b2.min});
		Point max = Point.min(new Point[]{b1.max,b2.max});
		return new AABB(min,max);
	}
}
