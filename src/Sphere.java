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
	
	public double getIntersect(Ray ray) {
		Vector v = new Vector(ray.o().subtract(c));
		double a = ray.d().dot(ray.d());
		double b = 2*(v.dot(ray.d()));
		double c = v.dot(v) - (r*r);
		
		double disc = b*b - 4*a*c;
		
		return (-b-Math.sqrt(disc))/2*a;
	}
	
	public Vector getNormal(Point p) {
		return new Vector(p.subtract(c));
		//snorm = new Vector(intersection.subtract(s1.c()));
	}
	
	public String toString() {
		return "Centre: " + c + "\n" + "Radius: " + r;
	}
}
