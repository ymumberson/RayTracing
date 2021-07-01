import javafx.scene.paint.Color;

public abstract class Tracable {
	protected Color ambient;
	protected Color diffuse;
	protected Color specular;
	
	protected double reflectedAmount;
	
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
		ambient = diffuse = specular = col;
	}
	
	public void setColor(Color ambient, Color diffuse, Color specular) {
		this.ambient = ambient;
		this.diffuse = diffuse;
		this.specular = specular;
	}
	
	public Color getColor() {
		return getAmbient();
	}
	
	public Color getColor(Point p) {
		return getAmbient(p);
	}
	
	public Color getAmbient() {
		return ambient;
	}
	
	public Color getAmbient(Point p) {
		return ambient;
	}
	
	public Color getDiffuse() {
		return diffuse;
	}
	
	public Color getDiffuse(Point p) {
		return diffuse;
	}
	
	public Color getSpecular() {
		return specular;
	}
	
	public Color getSpecular(Point p) {
		return specular;
	}
	
	public boolean isReflective() {
		return getReflectedAmount() > 0;
	}
	
	public void setReflectedAmount(double reflectedAmount) {
		this.reflectedAmount = reflectedAmount;
	}
	
	public double getReflectedAmount() {
		return reflectedAmount;
	}
	
	public static void main(String[] args) {
//		Sphere s = new Sphere(new Point(0, 0, 100), 100);
//		s.setColor(Color.BLUE);
//		System.out.println(s.getColor().getRed() + "," + s.getColor().getGreen() + "," + s.getColor().getBlue());
//		
//		s.setColor(Color.color(0.2, 0.4, 0.5, 1));
//		System.out.println(s.getColor().getRed() + "," + s.getColor().getGreen() + "," + s.getColor().getBlue());
	}
}
