
public class Triangle extends Tracable {
	private Point p0,p1,p2;
	private static final double EPSILON = 0.0000001;
	private Vector norm;
	//private static final double EPSILON = Math.abs(0.2 - 0.3 + 0.1);
	
	Triangle (Point p0, Point p1, Point p2) {
		this.p1 = p1;
		this.p2 = p2;
		this.p0 = p0;
		this.norm = this.calculateNorm();
	}
	
	public Point[] getPoints() {
		return new Point[]{p0,p1,p2};
	}
	
	public Point getP0() {
		return p0;
	}
	
	public Point getP1() {
		return p1;
	}
	
	public Point getP2() {
		return p2;
	}
	
//	@Override
	public double getIntersect(Ray r) {
//		return this.generateAABB().getIntersect(r); //For drawing boxes instead of triangles
		final double EPSILON = 0.000001;
		Vector edge1 = new Vector(p1.subtract(p0));
		Vector edge2 = new Vector(p2.subtract(p0));
		Vector pvec = r.d().cross(edge2);
		double det = edge1.dot(pvec);
		
		if (Math.abs(det) < EPSILON) {
			return -1;
		}
		
		double invDet = (1.0/det);
		
		Vector tvec = new Vector(r.o().subtract(p0));
		double u = tvec.dot(pvec) * invDet;
		if (u < 0 || u > 1) {
			return -1;
		}
		
		Vector qvec = tvec.cross(edge1);
		double v = r.d().dot(qvec) * invDet;
		if (v < 0 || u+v > 1) {
			return -1;
		}
		
//		double t = edge2.dot(qvec) * invDet;
//		return t;
		return edge2.dot(qvec) * invDet;
	}
	
	public Vector getNormal() {
		return this.norm;
	}
	
	private Vector calculateNorm() {
		Vector v0v1 = new Vector(p1.subtract(p0));
		Vector v0v2 = new Vector(p2.subtract(p0));
		Vector triangleNorm = v0v2.cross(v0v1);
		triangleNorm.normalise();
		return triangleNorm;
	}
	
	public Vector getNormal(Point p) {
		return getNormal();
	}
	
	/*
	 * Just for testing
	 */
	public double showCorners(Ray r) {
		if ((r.o().x() == p0.x() && r.o().y() == p0.y())
				|| (r.o().x() == p1.x() && r.o().y() == p1.y())
				|| (r.o().x() == p2.x() && r.o().y() == p2.y())) {
			return 1;
		}
		return -1;
	}
	
	public String toString() {
		return "Triangle: " + p0 + " " + p1 + " " + p2;
	}
	
	public Point[] getLastTwoPoint() {
		return new Point[] {p1,p2};
	}

	@Override
	public AABB generateAABB() {
		double padding = 0.0001; //Added padding so AABB isn't 2D in case of axis-aligned triangle
		Point min = new Point(
				Math.min(p0.x(),Math.min(p1.x(), p2.x()))-padding,
				Math.min(p0.y(),Math.min(p1.y(), p2.y()))+padding,
				Math.min(p0.z(),Math.min(p1.z(), p2.z()))-padding);
		Point max = new Point(
				Math.max(p0.x(),Math.max(p1.x(), p2.x())+padding),
				Math.max(p0.y(),Math.max(p1.y(), p2.y()))-padding,
				Math.max(p0.z(),Math.max(p1.z(), p2.z()))+padding);
		return new AABB(min,max);
	}
}
