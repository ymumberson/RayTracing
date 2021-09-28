import javafx.scene.paint.Color;

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
	
	public boolean doesIntersect(Ray ray) {
		Vector v = new Vector(ray.o().subtract(c));
		double a = ray.d().dot(ray.d());
		double b = 2*(v.dot(ray.d()));
		double c = v.dot(v) - (r*r);
		
		double disc = b*b - 4*a*c;
		
		if (disc >= 0) {
			return true;
		} else {
			return false;
		}
	}
	
	public double[] getIntersect(Ray ray) {
		Vector v = new Vector(ray.o().subtract(c));
		double a = ray.d().dot(ray.d());
		double b = 2*(v.dot(ray.d()));
		double c = v.dot(v) - (r*r);
		
		double disc = b*b - 4*a*c;
		
		if (disc < 0) return new double[] {-1};
		
		double t1 = (-b-Math.sqrt(disc))/2*a;
		double t2 = (-b+Math.sqrt(disc))/2*a;
		
		//System.out.println("Intersects at: " + ray.getPoint((float)t1) + " and " + ray.getPoint((float)t2));
//		
//		if (t1 < 0 && t2 >= 0) {
//			return t2;
//		} else {
//			return t1;
//		}
		
		return new double[] {t1,t2};
		
		//return (-b-Math.sqrt(disc))/2*a;
	}
	
	public Vector getNormal(Point p) {
		return new Vector(p.subtract(c));
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
	
	public static void main(String[] args) {
//		Sphere s = new Sphere(new Point(50,50,50), 10);
//		Ray r = new Ray(new Point(50,50,45),new Vector(0,0,1));
		
		Sphere s = new Sphere(new Point(320,360,50), 50);
		Ray r = new Ray(new Point(325.18466f,407.9833f,36.93393f), 
				new Vector(-0.10369322365759076,-0.9596660113933547,0.2613213767449315));
		
		double[] arr = s.getIntersect(r);
		
//		double x = 320 - 50*0.10377673954038813;
//		double y = 360 - 50*0.9610569257720889;
//		double z = 50 - 50*-0.25612491826424877;
//		
//		System.out.println(x + "," + y + "," + z);
		
		for (double d: arr) {
			System.out.println(r.getPoint((float)d) + " -> " + d);
		}
		
		System.out.println(Math.sqrt(-1));
	}
}
