
public class PositionableCamera {
	Vector origin, lowerLeft, horizontal, vertical;
	
	public PositionableCamera(float vfov, float aspect) {
		double theta = vfov*(Math.PI/180);
		double half_height = Math.tan(theta/2f);
		double half_width = aspect * half_height;
		lowerLeft = new Vector(-half_width, -half_height, -1);
		horizontal = new Vector(2*half_width,0,0);
		vertical = new Vector(0,2*half_height,0);
		origin = new Vector(0,0,0);
	}
	
	public PositionableCamera(Vector lookfrom, Vector lookat, Vector vup, float vfov, float aspect) {
		Vector u,v,w;
		double theta = vfov*(Math.PI/180);
		double half_height = Math.tan(theta/2f);
		double half_width = aspect * half_height;
		origin = lookfrom;
		w = lookfrom.minus(lookat);
		w.normalise();
		
		u = vup.cross(w);
		u.normalise();
		
		v = w.cross(u);
//		lowerLeft = new Vector(-half_width, -half_height, -1);
		lowerLeft = 
				origin.minus(u.multiply(half_width))
				.minus(v.multiply(half_height))
				.minus(w);
		horizontal = u.multiply(half_width*2);
		vertical = v.multiply(half_height*2);
	}
	
	public Ray getRay(float u, float v) {
		Vector dir = lowerLeft.add(horizontal.multiply(u))
				.add(vertical.multiply(v));
		dir.normalise();
		return new Ray(new Point(origin), dir);
	}
}
