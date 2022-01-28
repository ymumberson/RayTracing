public class Sphere extends Tracable {
	private Point c;
	private float r;
	
	public Sphere(Point c, float r) {
		this.c = c;
		this.r = r;
	}
	
	public Point c() {
		return c;
	}
	
	public float r() {
		return r;
	}
	
	public double getIntersect(Ray ray) {
		Vector v = new Vector(ray.o().subtract(c));
		double a = ray.d().dot(ray.d());
		double b = 2*(v.dot(ray.d()));
		double c = v.dot(v) - (r*r);
		
		double disc = b*b - 4*a*c;
		
		if (disc < 0) return -1;
		
		double t1 = (-b-Math.sqrt(disc))/2*a;
		double t2 = (-b+Math.sqrt(disc))/2*a;
		
		if (t1 < 0) { //ie first intersect is behind camera, or both miss
//			System.out.println(t1 + ", " + t2);
			return t2;
		}
		return t1; //Otherwise first intersect is in front of camera and there is an intersect
	}
	
	public Vector getNormal(Point p) {
		Vector v = new Vector(p.subtract(c));
		v.normalise();
		return v;
//		return new Vector(p.subtract(c));
		//snorm = new Vector(intersection.subtract(s1.c()));
	}
	
	public String toString() {
		return "Centre: " + c + "\n" + "Radius: " + r;
	}
	
	public void setRadius(float r) {
		this.r = r;
	}
	
	public float getRadius() {
		return r;
	}
	
	public void setCentre(Point c) {
		this.c = c;
	}
	
	public Point getCentre() {
		return c;
	}

	@Override
	public AABB generateAABB() {
		Point min = new Point(c.x()-r,c.y()-r,c.z()-r);
		Point max = new Point(c.x()+r,c.y()+r,c.z()+r);
		return new AABB(min,max);
	}
	
	public Point generateRandomUnitPoint() {
		double x = 0;
		double y = 0;
		double z = 0;
		int min = -1;
		int max = 1;
		do {
			x = (Math.random() * (max-min)) + min;
			y = (Math.random() * (max-min)) + min;
			z = (Math.random() * (max-min)) + min;
		} while (x*x + y*y + z*z > 1);
		return new Point(x,y,z);
	}
	
	public static void main(String[] args) {
		Sphere s = new Sphere(new Point(10,10,10), 2);
		for (int i=0; i<10; i++) {
			System.out.println(s.generateRandomUnitPoint());
		}
	}
}
