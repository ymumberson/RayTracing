
public class Square extends Tracable {
	/**
	 * Centre of the square
	 */
	private Point c;
	
	/**
	 * Surface normal of the square
	 */
	private Vector n;
	
	/**
	 * Width of the square
	 */
	private float w;
	
	Square(Point centre, Vector surfaceNormal, float width) {
		this.c = centre;
		this.n = surfaceNormal;
		this.w = width;
	}

	@Override
	public double getIntersect(Ray r) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}
