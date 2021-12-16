
public class Camera {
	Point origin;
	Vector up;
	Vector forward;
	Vector left;
	double h,w;
	
	public Camera(Point origin, Vector forward, Vector up, float fov, float aspectRatio) {
		forward.normalise();
		up.normalise();
		
		this.forward = forward;
		this.origin = origin;
		
		left = up.cross(forward);
		left.normalise();
		
		this.up = forward.cross(left);
		
		h = Math.tan(fov);
		w = h*aspectRatio;
	}
	
	public Ray getRay(Point p) {
		Vector dir = 
				left.multiply(p.x()*w).
				add(up.multiply(p.y()*h)).
				add(forward);
		dir.normalise();
		return new Ray(origin,dir);
		//Vector dir = new Vector(right.multiply(p.x()*w),up.multiply(p.y()*h),forward);
	}
}
