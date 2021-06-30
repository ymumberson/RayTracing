import javafx.scene.paint.Color;

public abstract class Tracable {
	protected double red;
	protected double green;
	protected double blue;
	
	/**
	 * Finds the point at which the ray intersects the object
	 * @param r
	 * @return
	 */
	public abstract double getIntersect(Ray r);
	
	 /**
	  * Returns true if the ray intersects the object
	  * @param r
	  * @return
	  */
	public boolean doesIntersect(Ray r) {
		return (this.getIntersect(r) >= 0);
	}
	
	public abstract Vector getNormal(Point p);
	
	public void setColor(Color col) {
		this.red = col.getRed();
		this.blue = col.getBlue();
		this.green = col.getGreen();
	}
	
	public double getRed() {
		return red;
	}
	
	public double getGreen() {
		return green;
	}
	
	public double getBlue() {
		return blue;
	}
	
	public static void main(String[] args) {
		Sphere s = new Sphere(new Point(0, 0, 100), 100);
		System.out.println(s.getRed());
		s.setColor(Color.BLUE);
		System.out.println(s.getRed() + "," + s.getGreen() + "," + s.getBlue());
	}
}
