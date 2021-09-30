
public class Vector {
	private double dx,dy,dz;
	
	public Vector() {
		this.dx = 0;
		this.dy = 0;
		this.dz = 0;
	}
	
	public Vector(Point p) {
		this.dx = p.x();
		this.dy = p.y();
		this.dz = p.z();
	}
	
	public Vector(double dx, double dy, double dz) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}
	
	public double magnitude() {
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
	
	public void normalise() {
		double mag = magnitude();
		if (mag != 0) {
			dx /= mag;
			dy /= mag;
			dz /= mag;
		}
	}
	
	public Vector cross(Vector v) {
		double cX = (dy*v.dz - dz*v.dy);
		double cY = (dz*v.dx - dx*v.dz);
		double cZ = (dx*v.dy - dy*v.dx);
		return new Vector(new Point(cX,cY,cZ));
	}
	
	public double dot(Vector v) {
		return dx*v.dx + dy*v.dy + dz*v.dz;
	}
	
//	public Vector minus(Vector v) {
//		return subtract(v);
//	}
	
	public Vector subtract(Vector v) {
		return new Vector(dx-v.dx, dy-v.dy, dz-v.dz);
	}
	
	public Vector add(Vector v) {
		return new Vector(dx+v.dx, dy+v.dy, dz+v.dz);
	}
	
	public Vector multiply(Vector v) {
		return new Vector(dx*v.dx, dy*v.dy, dz*v.dz);
	}
	
	public Vector multiply(double n) {
		return new Vector(dx*n, dy*n, dz*n);
	}
	
	public double dx() {
		return dx;
	}
	
	public double dy() {
		return dy;
	}
	
	public double dz() {
		return dz;
	}
	
	public Vector absolute() {
		return new Vector(Math.abs(dx),Math.abs(dy),Math.abs(dz));
	}
	
	public String toString() {
		return "(" + this.dx + "," + this.dy + "," + this.dz + ")";
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (!(o instanceof Vector)) return false;
		
		Vector v = (Vector) o;
		if (this.dx == v.dx() && this.dy == v.dy() && this.dz == v.dz()) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void main(String[] args) {
//		Vector v1 = new Vector(0,1,0);
//		Vector v2 = new Vector(-1,1,0);
//		System.out.println(v1.cross(v2));
		
		Vector n = new Vector(0,0,-1);
		Vector l = new Vector(-1,0,1);
		//Vector r = n.multiply(n.dot(l)*2).subtract(l);
		Vector r = l.subtract(n.multiply(2*l.dot(n)));
		System.out.println(r);
		
	}
}
