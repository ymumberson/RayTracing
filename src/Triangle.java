
public class Triangle extends Tracable {
	private Point p0,p1,p2;
	private static final double EPSILON = 0.0000001;
	//private static final double EPSILON = Math.abs(0.2 - 0.3 + 0.1);
	
	Triangle (Point p0, Point p1, Point p2) {
		this.p1 = p1;
		this.p2 = p2;
		this.p0 = p0;
	}

//	@Override
//	public double getIntersect(Ray r) {
//		Vector v0v1 = new Vector(p1.subtract(p0));
//		Vector v0v2 = new Vector(p2.subtract(p0));
//		Vector pvec = r.d().cross(v0v2);
//		float det = (float)v0v1.dot(pvec);
//		
//		//Culling if only a 1-sided triangle
////		if (det < EPSILON) {
////			//System.out.println(det);
////			return -1; //== false
////		}
//		
//		if (Math.abs(det) < EPSILON) {
//			System.out.println("2");
//			return -1;//== false
//		}
//		
//		float invDet = 1.0f / det;
//		
//		Vector tvec = new Vector(r.o().subtract(p0));
//		double u = tvec.dot(pvec) * invDet;
//		if (u < 0 || u > 1) {return -1;}//== false
//		
//		Vector qvec = tvec.cross(v0v1);
//		double v = r.d().dot(qvec) * invDet;
//		if (v < 0 || u + v > 1) {return -1;}//== false
//		
//		double t = v0v2.dot(qvec) * invDet;
//		
////		System.out.println("\ninvDet: " + invDet);
////		System.out.println("pvec: " + pvec);
////		System.out.println("tvec: " + tvec);
////		System.out.println("v0v2: " + v0v2);
////		System.out.println("qvec: " + qvec);
////		System.out.println("v0v2.dot(qvec): " + v0v2.dot(qvec));
////		System.out.println("t: " + t);
////		System.out.println("u: " + u);
////		System.out.println("v: " + v);
////		System.out.println("Intersect at t = " + t);
////		System.out.println("Ray origin at: " + r.o());
////		System.out.println("Intersect? at: " + new Point((float)u,(float)v,(float)t));
//		return 0;//true
//	}
	
	@Override
	public double getIntersect(Ray r) {
		Vector v0v1 = new Vector(p1.subtract(p0));
		Vector v0v2 = new Vector(p2.subtract(p0));
		//Vector triangleNorm = v0v1.cross(v0v2);
		Vector triangleNorm = v0v2.cross(v0v1);
		
		double nDotRayDir = triangleNorm.dot(r.d());
		if (Math.abs(nDotRayDir) == 0) {
			//System.out.println("Fail: Normals are parallel->" + r.d() + triangleNorm);
			return -1;}
//		if (Math.abs(nDotRayDir) < EPSILON) {
//			//System.out.println("Fail: Normals are parallel");
//			return -1;}
		
		//double t = (triangleNorm.dot(new Vector(r.o())) + triangleNorm.dot(new Vector(p0))) / triangleNorm.dot(r.d());
		double d = triangleNorm.dot(new Vector(p0));
		//double t = -(triangleNorm.dot(new Vector(r.o())) + d) / nDotRayDir;
		double t = (d - triangleNorm.dot(new Vector(r.o()))) / nDotRayDir;
		if (t < 0) {
//				System.out.println("Fail: Intersection is behind ray origin, t=" + t
//				+ "\n1) -> " + (triangleNorm.dot(new Vector(r.o())))
//				+ "\nd -> " + d
//				+ "\n2) -> " + nDotRayDir);
				
//				+ "\n" + (triangleNorm.dot(new Vector(r.o())) + triangleNorm.dot(new Vector(p0)))
//				+ "\n" + triangleNorm.dot(r.d()));
				return t;}
		
		//Step 2) Inside-out test
		Vector c;
		Point p = r.o().add(new Point(r.d()).multiply((float)t));
		//Point p = r.getPoint((float)t);
		
//		Vector edge0 = new Vector(p1.subtract(p0));
//		Vector vp0 = new Vector(p.subtract(p0));
//		c = edge0.cross(vp0);
//		if (triangleNorm.dot(c) < 0) {System.out.println("Fail: v0->v1");return -1;}
//		
//		Vector edge1 = new Vector(p2.subtract(p1));
//		Vector vp1 = new Vector(p.subtract(p1));
//		c = edge1.cross(vp1);
//		if (triangleNorm.dot(c) < 0) {System.out.println("Fail: v1->v2");return -1;}
//		
//		Vector edge2 = new Vector(p0.subtract(p2));
//		Vector vp2 = new Vector(p.subtract(p2));
//		c = edge2.cross(vp2);
//		if (triangleNorm.dot(c) < 0) {System.out.println("Fail: v2->v0");return -1;}
		
		Vector edge0 = new Vector(p1.subtract(p0));
		Vector vp0 = new Vector(p.subtract(p0));
		//c = edge0.cross(vp0);
		c = vp0.cross(edge0);
		if (triangleNorm.dot(c) < 0) {
			//System.out.println("Fail: v0->v1 " + "c:" + c + "tn:" + triangleNorm + "r:" + r.o());
			return -1;
		}
		
		Vector edge1 = new Vector(p2.subtract(p1));
		Vector vp1 = new Vector(p.subtract(p1));
		//c = edge1.cross(vp1);
		c = vp1.cross(edge1);
		if (triangleNorm.dot(c) < 0) {
			//System.out.println("Fail: v1->v2 " + "c:" + c + "tn:" + triangleNorm + "r:" + r.o());
			return -1;
		}
		
		Vector edge2 = new Vector(p0.subtract(p2));
		Vector vp2 = new Vector(p.subtract(p2));
		//c = edge2.cross(vp2);
		c = vp2.cross(edge2);
		if (triangleNorm.dot(c) < 0) {
			//System.out.println("Fail: v2->v0 " + "c:" + c + "tn:" + triangleNorm + "r:" + r.o());
			return -1;
		}
		
		return t;
	}
	
	public Vector getNormal() {
		Vector v0v1 = new Vector(p1.subtract(p0));
		Vector v0v2 = new Vector(p2.subtract(p0));
		Vector triangleNorm = v0v2.cross(v0v1);
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
}
