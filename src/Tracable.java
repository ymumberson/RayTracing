import javafx.scene.paint.Color;

public abstract class Tracable {
	protected Color ambient;
	protected Color diffuse;
	protected Color specular;
	
	protected double reflectedPercent;
	protected double refractedPercent;
	protected double refractiveIndex = 1;
	protected double diffusePercent;
	
	protected boolean isLightSource = false;
	
	public boolean isLight() {
		return this.isLightSource;
	}
	
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
	
	public void setAmbient(Color ambient) {
		this.ambient = ambient;
	}
	
	public void setDiffuse(Color diffuse) {
		this.diffuse = diffuse;
	}
	
	public void setSpecular(Color specular) {
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
		return getReflectedPercent() > 0;
	}
	
	public void setReflectedPercent(double reflectedPercent) {
		this.reflectedPercent = reflectedPercent;
	}
	
	public double getReflectedPercent() {
		return reflectedPercent;
	}
	
	public boolean isRefractive() {
		return refractedPercent > 0;
	}
	
	public void setDiffusePercent(double diffusePercent) {
		this.diffusePercent = diffusePercent;
	}
	
	public double getDiffusePercent() {
		return this.diffusePercent;
	}
	
	public boolean isDiffuse() {
		return diffusePercent > 0;
	}
	
	public boolean isMatt() {
		return !(isReflective() || isRefractive() || isDiffuse());
	}
	
//	public boolean isDiffuse() { //More of a -> isMatt()
//		return !isRefractive() && !isReflective();
//	}
	
	public double getRefractedPercent() {
		return refractedPercent;
	}
	
	public void setRefractedPercent(double refractedPercent) {
		this.refractedPercent = refractedPercent;
	}
	
	public void setRefractiveIndex(double refractiveIndex) {
		this.refractiveIndex = refractiveIndex;
	}
	
	public double getRefractiveIndex() {
		return refractiveIndex;
	}
	
	public double getMattPercent() {
		return 1 - (refractedPercent + reflectedPercent);
	}
	
	/**
	 * Generates an Axis-Aligned Bounding Box surrounding the tracable object.
	 * @return The AABB
	 */
	public abstract AABB generateAABB();
	
	/**
	 * Returns an int representing russian roulette outcome:
	 * 0 = Absorption
	 * 1 = Diffuse reflection
	 * 2 = Specular reflection
	 * 3 = Refraction
	 * @param r
	 * @param intersection
	 * @return
	 */
	public int russianRoulette(Ray r, Point intersection) {
		float rnd = (float)Math.random();
		if (this.isDiffuse()) {
//			System.out.println(rnd + " < " + this.diffusePercent);
			if (rnd < this.diffusePercent) {
				return 1; //Diffuse reflection
			}
			return 0; //Absorbed
		} else {
//			System.out.println(rnd + " !< " + this.diffusePercent);
			float kr = 0;
			Vector n = this.getNormal(intersection);
			float etat = (float)this.getRefractiveIndex();
			
			float cosi = (float) Math.max(-1,Math.min(1, r.d().dot(n)));
			float etai = 1; //Refractive index of air
			if (cosi > 0) {
				float temp = etai;
				etai = etat;
				etat = temp;
			}
			
			//Compute using Snell's law
			float sint = (float) (etai / etat * Math.sqrt(Math.max(0f, 1 - cosi*cosi)));
			
			if (sint >= 1) { //Total internal reflection
				kr = 1;
			} else {
				float cost = (float) Math.sqrt(Math.max(0f, 1 - sint*sint));
				cosi = Math.abs(cosi);
				float rS = (float) (((etat*cosi) - (etai*cost)) / ((etat*cosi) + (etai * cost)));
				float rP = (float) (((etai*cosi) - (etat*cost)) / ((etai*cosi) + (etat * cost)));
//				System.out.println(rS + " " + rP + " " + cost + " " + cosi);
				kr = (rS * rS + rP * rP) / 2f;
			}
			float kt = 1-kr;
			if (this.isRefractive()) { //If refractive then swap reflectPerc and refractPerc
				float temp = kt;
				kt = kr;
				kr = temp;
			}
			
			float absorptionChance = 0.2f;
			//Returns
			if (rnd <= kt-absorptionChance) {
				return 2; //Specular reflection
			} else if (rnd <= kr-absorptionChance) {
				return 3; //Refraction
			}
		}
		
		return 0; //Absorption
	}
	
	public Vector getEmissiveValue() {
		return new Vector(0,0,0);
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
