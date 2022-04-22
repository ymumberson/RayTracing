
public class AreaLight extends Rectangle {
	protected Point topLeft,topRight,bottomRight,bottomLeft;
	
	public AreaLight(Point topLeft, Point topRight, Point bottomRight, Point bottomLeft) {
		super(topLeft, topRight, bottomRight, bottomLeft);
		this.isLightSource = true;
		this.topLeft = topLeft;
		this.topRight = topRight;
		this.bottomRight = bottomRight;
		this.bottomLeft = bottomLeft;
	}
	
	@Override
	public Vector getEmissiveValue() {
		return new Vector(100,100,100);
	}
	
	public Point getPointOnLight(float u, float v) {
		Point widthIncrement = this.topRight.subtract(topLeft);
		Point heightIncrement = this.bottomLeft.subtract(topLeft);
		
		return topLeft.add(widthIncrement.multiply(u))
				.add(heightIncrement.multiply(v));
	}
	
	public Ray generateRandomLightRay() {
		float u = (float)Math.random();
		float v = (float)Math.random();
		Vector d = this.generateRandomDirectionOnHemisphere();
		return new Ray(this.getPointOnLight(u, v).add(new Point(d.multiply(0.000001f))), d);
	}
	
	public Vector generateRandomDirectionOnHemisphere() {
		Vector N = this.t1.getNormal();
		
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
		Vector d = new Vector(x,y,z);
		d.normalise();
		if (d.dot(N) <= 0) {
			return d.multiply(-1);
		} else {
			return d;
		}
	}
	
	public double getArea() {
		return this.topLeft.euclideanDistance(this.topRight) * this.topLeft.euclideanDistance(this.topRight);
	}
	
	public static void main(String[] args) {
		AreaLight light = new AreaLight(
				new Point(1,0,1),
				new Point(2,0,1),
				new Point(2,1,1),
				new Point(1,1,1));
		
		float numSamples = 4;
		for (int i=0; i<numSamples; i++) {
			for (int j=0; j<numSamples; j++) {
				float u = i/numSamples;
				float v = j/numSamples;
				System.out.println("u: " + u + ", v: " + v + " -> " + light.getPointOnLight(u, v));
			}
		}
	}
}
