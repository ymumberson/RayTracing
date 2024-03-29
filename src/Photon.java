
public class Photon {
	/**
	 * These variables are not space efficient right now.
	 * Replacing them with more compact variables would increase the number of photons we can store,
	 * however we'd need to recompute stuff on the fly, thus render time would increase.
	 * ie we may store direction as phy&theta, then recompute it as a vector in getDirection()
	 */
	private Point position;
	private Vector incidentDirection;
	private Vector surfaceNormal;
	private Vector energy;
	private boolean illuminationPhoton;
	private boolean shadowPhoton;
	
	public Photon(Point position, Vector incidentDirection, Vector surfaceNormal, Vector energy) {
		this.position = position;
		this.incidentDirection = incidentDirection;
		this.surfaceNormal = surfaceNormal;
		this.energy = energy;
		this.shadowPhoton = false;
		this.illuminationPhoton = false;
	}
	
	/*
	 * For creating illumination and shadow photons.
	 * if illuminated == false -> Shadow photon.
	 * if illuminated == true -> illumination photon.
	 */
	public Photon(Point position, Vector incidentDirection, Vector surfaceNormal, Vector energy, Boolean illuminated) {
		this.position = position;
		this.incidentDirection = incidentDirection;
		this.surfaceNormal = surfaceNormal;
		this.energy = energy;
		this.illuminationPhoton = illuminated;
		this.shadowPhoton = !illuminated;
	}
	
	public Point getPosition() {
		return position;
	}
	
	public Vector getIncidentDirection() {
		return incidentDirection;
	}
	
	public Vector getSurfaceNormal() {
		return surfaceNormal;
	}
	
	public Vector getEnergy() {
		return energy;
	}
	
	public boolean hasBeenReflected() {
		return (!isShadowPhoton() && !isIlluminationPhoton());
	}
	
	public boolean isShadowPhoton() {
		return shadowPhoton;
	}
	
	public boolean isIlluminationPhoton() {
		return illuminationPhoton;
	}
	
	/**
	 * 
	 * @param splitplane Can be either 0,1,2 representing x,y,z
	 * @return position.{x,y,z}
	 */
	public double getSplitVal(int splitplane) {
		switch (splitplane) {
			case 1:
				return position.y();
			case 2:
				return position.z();
			default:
				return position.x();
		}
	}
	
	public String toString() {
		return "{Photon: {position:'" + position + "', energy:'" + energy + "'}}";
	}
}
