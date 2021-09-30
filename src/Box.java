
public class Box extends Tracable{
	/**
	 * Minimum coordinate of the box (x,y,z)
	 */
	private Point min;
	
	/**
	 * Maximum coordinate of the box (x,y,z)
	 */
	private Point max;
	
	Rectangle[] sides = new Rectangle[6];
	
	/**
	 * Constructor
	 * @param min Minimum coordinate of bounding box
	 * @param max Maximum coordinate of bounding box
	 */
	public Box(Point min, Point max) {
		this.min = min;
		this.max = max;
		
//		Vector v = new Vector(max.subtract(min));
//		
		sides[0] = new Rectangle(new Point(min.x(),max.y(),min.z()),
				new Point(max.x(),max.y(),min.z()),
				new Point(max.x(),min.y(),min.z()),
				min);
		
		sides[1] = new Rectangle(new Point(max.x(),max.y(),min.z()),
				max,
				new Point(max.x(),min.y(),max.z()),
				new Point(max.x(),min.y(),min.z()));
		
		sides[2] = new Rectangle(max,
				new Point(min.x(),max.y(),max.z()),
				new Point(min.x(),min.y(),max.z()),
				new Point(max.x(),min.y(),max.z()));
		
		sides[3] = new Rectangle(new Point(min.x(),max.y(),max.z()),
				new Point(min.x(),max.y(),min.z()),
				min,
				new Point(min.x(),min.y(),max.z()));
		
		sides[4] = new Rectangle(new Point(min.x(),max.y(),max.z()),
				max,
				new Point(max.x(),max.y(),min.z()),
				new Point(min.x(),max.y(),min.z()));
		
		sides[5] = new Rectangle(new Point(max.x(),min.y(),min.z()),
				min,
				new Point(min.x(),min.y(),max.z()),
				new Point(max.x(),min.y(),max.z()));
	}
	
	@Override
	public double[] getIntersect(Ray r) {
		double tmin = (min.x() - r.o().x()) / r.d().dx();
		double tmax = (max.x() - r.o().x()) / r.d().dx();
		
		if (tmin > tmax) {
			double temp = tmin;
			tmin = tmax;
			tmax = temp;
		}
		
		double tymin = (min.y() - r.o().y()) / r.d().dy();
		double tymax = (max.y() - r.o().y()) / r.d().dy();
		
		if (tymin > tymax) {
			double temp = tymin;
			tymin = tymax;
			tymax = temp;
		}
		
		if ((tmin > tymax) || (tymin > tmax)) return new double[] {-1};
		
		if (tymin > tmin) tmin = tymin;
		
		if (tymax < tmax) tmax = tymax;
		
		double tzmin = (min.z() - r.o().z()) / r.d().dz();
		double tzmax = (max.z() - r.o().z()) / r.d().dz();
		
		if (tzmin > tzmax) {
			double temp = tzmin;
			tzmin = tzmax;
			tzmax = temp;
		}
		
		if ((tmin > tzmax) || (tzmin > tmax)) return new double[] {-1};
		
		if (tzmin > tmin) tmin = tzmin;
		
		if (tzmax < tmax) tmax = tzmax;
		
		return new double[] {tmin,tmax};
		
		//return sideIntersected(r).getIntersect(r);
	}

	@Override
	public Vector getNormal(Point p) {
		//return new Vector(0,0,0); //ie null (Just temp)
		
		//Doesn't give proper shading
		Vector diff = new Vector(max.subtract(min));
		Point mid = new Point(min.x()+diff.dx()/2, min.y()+diff.dy()/2, min.z()+diff.dz()/2);
		
		Vector toCentre = new Vector(mid.subtract(p));
		
		return sideIntersected(new Ray(p,toCentre)).getNormal();
	}
	
	private Rectangle sideIntersected(Ray r) {
		Rectangle rect = sides[0];
		double intersect = rect.getIntersect(r)[0];
		
		for (int i=1; i<6; i++) {
			double intersectI = sides[i].getIntersect(r)[0];
			if (intersectI >= 0 && intersectI < intersect) {
				intersect = intersectI;
				rect = sides[i];
			}
		}
		
		return rect;
	}
	
}
