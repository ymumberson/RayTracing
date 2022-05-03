
public class UnitSquare2 {
	Vector[] array;
	float[] cumFreq;
	int n;
	int totalEnergy = 0;
	Vector uv;
	int index;
	Vector plusOne = new Vector(0,0,1);
	double theta, phi, x, y, z;
	Vector temp;
	
	public UnitSquare2(int n) {
		this.n = n;
		array = new Vector[n*n];
		for (int j=0; j<n; j++) {
			for (int i=0; i<n; i++) {
				array[(n*j)+i] = new Vector(i,j,0);
			}
		}
	}
	
	public void insertVector(Vector v) {
		this.uv = this.UV(v).multiply(n);
		index = (n*(int)uv.dy() + (int)uv.dx());
		if (index >= n*n) index = n*n-1;
		array[index] = array[index].add(plusOne);
		totalEnergy++;
	}
	
	/*
	 * https://gamedev.stackexchange.com/questions/135108/converting-back-and-forth-between-spherical-and-cartesian-coordinates-in-glsl
	 */
	public Vector UV(Vector v) {return UV(v.dx(),v.dy(),v.dz());}
	public Vector UV(double x, double y, double z) {
		double u = Math.atan2(-x,y);
		u = (u + (Math.PI/2)) / (Math.PI * 2) + Math.PI*(28.670 / 360.0);
		double v = Math.acos(z)/Math.PI;
		return new Vector(u,v,0);
	}
	
	/*
	 * https://gamedev.stackexchange.com/questions/135108/converting-back-and-forth-between-spherical-and-cartesian-coordinates-in-glsl
	 */
	public Vector XYZ(double u, double v) {
		theta = 2*Math.PI * u + -Math.PI/2f;
		phi = Math.PI*v;
		
		x = Math.cos(theta) * Math.sin(phi);
		y = Math.sin(theta) * Math.sin(phi);
		z = Math.cos(phi);
		return new Vector(x,y,z);

	}
	
	public void createCumulativeFrequencies() {
		this.quicksort(array,0,array.length-1);
		float accum = 0;
		cumFreq = new float[n*n];
		for (int i=0; i<array.length; i++) {
			accum += array[i].dz()/this.totalEnergy;
			cumFreq[i] = accum;
		}
	}
	
	public void sort() {
		this.quicksort(array, 0, array.length-1);
	}
	
	/**
	 * Adapted from: https://www.baeldung.com/java-quicksort
	 * @param ls
	 * @param splitDim
	 * @param lower
	 * @param upper
	 */
	private void quicksort(Vector[] ls, int lower, int upper) {
		if (lower < upper) {
			int partitionIndex = partition(ls,lower,upper);
			quicksort(ls,lower,partitionIndex-1);
			quicksort(ls,partitionIndex+1,upper);
		}
	}
	
	private int partition(Vector[] ls, int lower, int upper) {
		double pivot = ls[upper].dz();
		int i = lower-1;
	
		for (int j=lower; j<upper; j++) {
			if (ls[j].dz() <= pivot) {
//			if (ls[j].dz() > pivot) {
				i++;
			
				temp = ls[i];
				ls[i] = ls[j];
				ls[j] = temp;
			}
		}
	
		temp = ls[i+1];
		ls[i+1] = ls[upper];
		ls[upper] = temp;
	
		return i+1;
	}
	
	Vector xyz;
	public Vector getDirection(double probability) {
		for (int i=0; i<cumFreq.length; i++) {
			if (probability < cumFreq[i]) {
				xyz = XYZ(array[i].dx()/n,array[i].dy()/n);
				xyz.normalise();
				return xyz;
			}
		}
		return new Vector(0,0,0); //This should never run, array[length-1] should always be true
	}
	
	float energyInRegion=0;
	double EPSILON = 0.00001f;
	float numRegions = n*n;
	public float getScalingFactor(Vector uv) {
		for (int i=0; i<array.length; i++) {
			if (Math.abs(array[i].dx() - uv.dx()) < EPSILON && Math.abs(array[i].dy() - uv.dy()) < EPSILON) {energyInRegion=(float) array[i].dz();}
		}
		if (energyInRegion == 0) energyInRegion = 1;
		return this.totalEnergy/(energyInRegion*numRegions);
	}
}
