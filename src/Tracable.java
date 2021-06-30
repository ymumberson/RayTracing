import javafx.scene.paint.Color;

public abstract class Tracable {
	protected double redAmbient;
	protected double greenAmbient;
	protected double blueAmbient;
	protected double redDiffuse;
	protected double greenDiffuse;
	protected double blueDiffuse;
	protected double redSpecular;
	protected double greenSpecular;
	protected double blueSpecular;
	
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
		redAmbient = col.getRed();
		greenAmbient = col.getGreen();
		blueAmbient = col.getBlue();
		
		redDiffuse = col.getRed();
		greenDiffuse = col.getGreen();
		blueDiffuse = col.getBlue();
		
		redSpecular = col.getRed();
		greenSpecular = col.getGreen();
		blueSpecular = col.getBlue();
	}
	
	public void setColor(Color ambient, Color diffuse, Color specular) {
		redAmbient = ambient.getRed();
		greenAmbient = ambient.getGreen();
		blueAmbient = ambient.getBlue();
		
		redDiffuse = diffuse.getRed();
		greenDiffuse = diffuse.getGreen();
		blueDiffuse = diffuse.getBlue();
		
		redSpecular = specular.getRed();
		greenSpecular = specular.getGreen();
		blueSpecular = specular.getBlue();
	}
	
	public double getRed() {
		return getRedAmbient();
	}
	
	public double getGreen() {
		return getGreenAmbient();
	}
	
	public double getBlue() {
		return getBlueAmbient();
	}
	
	public double getRedAmbient() {
		return redAmbient;
	}
	
	public double getGreenAmbient() {
		return greenAmbient;
	}
	
	public double getBlueAmbient() {
		return blueAmbient;
	}
	
	public double getRedDiffuse() {
		return redDiffuse;
	}
	
	public double getGreenDiffuse() {
		return greenDiffuse;
	}
	
	public double getBlueDiffuse() {
		return blueDiffuse;
	}
	
	public double getRedSpecular() {
		return redSpecular;
	}
	
	public double getGreenSpecular() {
		return greenSpecular;
	}
	
	public double getBlueSpecular() {
		return blueSpecular;
	}
	
	public static void main(String[] args) {
		Sphere s = new Sphere(new Point(0, 0, 100), 100);
		System.out.println(s.getRed());
		s.setColor(Color.BLUE);
		System.out.println(s.getRed() + "," + s.getGreen() + "," + s.getBlue());
	}
}
