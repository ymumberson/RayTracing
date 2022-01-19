
public class Camera {
	private Vector origin;
	private Vector lower_left_corner;
	private Vector horizontal;
	private Vector vertical;
	
	public Camera(Vector lookfrom, Vector lookat, Vector vuv, float vfov, float aspect) {
		Vector u,v,w;
		float theta = (float) (vfov*Math.PI/180);
		float half_height = (float) Math.tan(theta/2);
		float half_width = aspect*half_height;
		origin = lookfrom;
		w = lookfrom.minus(lookat);
		w.normalise();
//		u = vuv.cross(w);
		u = w.cross(vuv);
		u.normalise();
		v = w.cross(u);
		v.normalise(); //Might not need
//		lower_left_corner = new Vector(-half_width,half_height,1);
		lower_left_corner = origin.minus(u.multiply(half_width))
				.minus(v.multiply(half_height)).minus(w);
		horizontal = u.multiply(half_width*2);
		vertical = v.multiply(2*half_height);
	}
	
	public Ray getRay(float s, float t) {
		Vector dir = lower_left_corner
				.add(horizontal.multiply(s))
				.add(vertical.multiply(t))
				.minus(origin);
		dir.normalise();
		return new Ray(new Point(origin),dir);
	}
}




//public class Camera {
//	Point origin;
//	Vector up;
//	Vector forward;
//	Vector left;
//	double h,w;
//	
//	public Camera(Point origin, Vector forward, Vector up, float fov, float aspectRatio) {
//		forward.normalise();
//		up.normalise();
//		
//		this.forward = forward;
//		this.origin = origin;
//		
//		left = up.cross(forward);
//		left.normalise();
//		
//		this.up = forward.cross(left);
//		
//		h = Math.tan(fov);
//		w = h*aspectRatio;
//	}
//	
//	public Ray getRay(Point p) {
//		Vector dir = 
//				left.multiply(p.x()*w).
//				add(up.multiply(p.y()*h)).
//				add(forward);
//		dir.normalise();
//		return new Ray(origin,dir);
//		//Vector dir = new Vector(right.multiply(p.x()*w),up.multiply(p.y()*h),forward);
//	}
//}
