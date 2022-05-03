
public class UnitSquare {
	private Vector[] array;
	private Vector[] cumulativeFrequencies;
	private int n;
	private float totalEnergy;
	
	public UnitSquare(int n) {
		this.n = n;
		array = new Vector[n*n];
		for (int j=0; j<n; j++) {
			for (int i=0; i<n; i++) {
				array[(n*j)+i] = new Vector(i,j,0);
			}
		}
	}
	
	/*
	 * Only works if not sorted
	 */
	public void insertVector(Vector v) {
//		Vector uv = UV(v); System.out.println(uv); uv = uv.multiply(n); System.out.println(uv);
		Vector uv = UV(v).multiply(n);
//		grid[(int)uv.dx()][(int)uv.dy()]++;
		int index = (n*(int)uv.dy() + (int)uv.dx());
		if (index >= n*n) index = n*n-1;
		
		// Add energy to cell
		array[index] = array[index].add(new Vector(0,0,1));
		
		totalEnergy++; //Increase total energy by 1 as we've inserted
	}
	
//	public void insertVector(Photon p) {
//		Vector v = p.getIncidentDirection().multiply(-1);
////		Vector uv = UV(v); System.out.println(uv); uv = uv.multiply(n); System.out.println(uv);
//		Vector uv = UV(v).multiply(n);
////		grid[(int)uv.dx()][(int)uv.dy()]++;
//		int index = (n*(int)uv.dy() + (int)uv.dx());
//		if (index >= n*n) index = n*n-1;
//		
//		// Add energy to cell
//		array[index] = array[index].add(new Vector(0,0,1));
//		
//		totalEnergy += p.getEnergy().magnitude(); //Increase total energy by 1 as we've inserted
//	}
	
//	public Vector UV(Vector v) {
//		return UV(v.dx(),v.dy(),v.dz());
//	}
//	
//	/*
//	 * Max v value seems to be 0.25 so *4 to scale to 1
//	 * -> Actually it's between -0.25 and 0.25, so -> v = 2(v + 0.25) 
//	 */
//	public Vector UV(double x, double y, double z) {
//		double v = (0.25 + (1f/(2*Math.PI)) * Math.atan(y/x)) * 2;
//		double u = 1 - z*z;
//		return new Vector(u,v,0);
//	}
//	public Vector UV(double x, double y, double z) {
//		double u = 0.5f + Math.atan2(x, z)/(2*Math.PI);
//		double v = 0.5f - Math.asin(y)/Math.PI;
//		return new Vector(u,v,0);
//	}
	/*
	 * https://gamedev.stackexchange.com/questions/135108/converting-back-and-forth-between-spherical-and-cartesian-coordinates-in-glsl
	 */
	public Vector UV(Vector v) {return UV(v.dx(),v.dy(),v.dz());}
	public Vector UV(double x, double y, double z) {
//		System.out.println("Original Vector: " + new Vector(x,y,z));
		double u = Math.atan2(-x,y);
		u = (u + (Math.PI/2)) / (Math.PI * 2) + Math.PI*(28.670 / 360.0);
		double v = Math.acos(z)/Math.PI;
//		System.out.println("Reconstructed Vector: " + XYZ(u,v));
		return new Vector(u,v,0);
	}
	
	/*
	 * https://gamedev.stackexchange.com/questions/135108/converting-back-and-forth-between-spherical-and-cartesian-coordinates-in-glsl
	 */
	public Vector XYZ(double u, double v) {
		double theta = 2*Math.PI * u + -Math.PI/2f;
		double phi = Math.PI*v;
		
		double x = Math.cos(theta) * Math.sin(phi);
		double y = Math.sin(theta) * Math.sin(phi);
		double z = Math.cos(phi);
		return new Vector(x,y,z);

	}
	
	/*
	 * UV scales v by 4, so divide by 4 here
	 * -> Now scaling by 2, and taking away 0.25
	 */
//	public Vector XYZ(double u, double v) {
//		v = (v/2f)-0.25f;
////		double temp = v; v = u; u = temp;
//		double z = Math.sqrt(1-u);
//		double y = Math.sin(2*Math.PI*v) * Math.sqrt(u);
//		double x = Math.cos(2*Math.PI*v) * Math.sqrt(u);
//		return new Vector(x,y,z);
//	}
	
	/*
	 * Think this is returning lower frequencies instead of higher frequencies
	 */
	public Vector getDirection(double probability) {
//		probability = 1f-probability;
//		for (int i=0; i<array.length; i++) { //Using old array
//			if (probability <= array[i].dz()) {
////				System.out.println(array[i]);
//				return XYZ(array[i].dx()/n,array[i].dy()/n);
//			}
//		}
		for (int i=0; i<cumulativeFrequencies.length; i++) {
//		for (int i=cumulativeFrequencies.length-1; i>=0; i--) {
			if (probability < cumulativeFrequencies[i].dz()) {
//				System.out.println(array[i]);
				Vector uv = cumulativeFrequencies[i];
//				float scalingFactor = getScalingFactor(uv);
//				float scalingFactor = getScalingFactor(array[i]);
				Vector xyz = XYZ(uv.dx()/n,uv.dy()/n);
//				Vector xyz = XYZ(uv.dx(),uv.dy());
				xyz.normalise();
				return xyz;
//				return xyz.multiply(scalingFactor);
				
//				return XYZ(array[i].dx()/n,array[i].dy()/n);
			}
		}
		return new Vector(0,0,0); //This should never run, array[length-1] should always be true
	}
	
	private float getScalingFactor(Vector uv) {
//		int index = n*(int)uv.dy() + (int)uv.dx();
//		float energyInRegion = (float) array[index].dz();
		float energyInRegion=0;
		double EPSILON = 0.00001f;
		for (int i=0; i<array.length; i++) {
//			if (array[i].dx() == uv.dx() && array[i].dy() == uv.dy()) {energyInRegion=(float) array[i].dz();}
			if (Math.abs(array[i].dx() - uv.dx()) < EPSILON && Math.abs(array[i].dy() - uv.dy()) < EPSILON) {energyInRegion=(float) array[i].dz();}
		}
		if (energyInRegion == 0) energyInRegion = 1;
		float numRegions = n*n;
		return this.totalEnergy/(energyInRegion*numRegions);
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
//			System.out.println("lower: " + lower + ", partitionIndex: " + partitionIndex); //Debugging stack overflow
			quicksort(ls,lower,partitionIndex-1);
			quicksort(ls,partitionIndex+1,upper);
		}
	}
	
//	private int partition(Vector[] ls, int lower, int upper) {
//		double x = ls[upper].dz();
//		int i = lower;
//		int j = upper;
//		Vector temp;
//		while (i < j) {
//			while (x < ls[j].dz()) {
//				j--;
//			}
//			while (x > ls[i].dz()) {
//				i++;
//			}
//			if (i < j) {
//				temp = ls[i];
//				ls[i] = ls[j];
//				ls[j] = temp;
//				j--;
//				i++;
//			}
//		}
//		return j;
//	}
	
	private int partition(Vector[] ls, int lower, int upper) {
		double pivot = ls[upper].dz();
		int i = lower-1;
	
		for (int j=lower; j<upper; j++) {
//			if (ls[j].dz() <= pivot) {
			if (ls[j].dz() > pivot) {
				i++;
			
				Vector temp = ls[i];
				ls[i] = ls[j];
				ls[j] = temp;
			}
		}
	
		Vector temp = ls[i+1];
		ls[i+1] = ls[upper];
		ls[upper] = temp;
	
		return i+1;
	}
	
	public void createCumulativeFrequencies() {
//		int total = 0;
//		for (int i=0; i<array.length; i++) {
//			total += array[i].dz();
//		}
		this.quicksort(array,0,array.length-1);
		double accum = 0;
		this.cumulativeFrequencies = new Vector[n*n];
		for (int i=0; i<array.length; i++) {
//		for (int i=array.length-1; i>=0; i--) {
//			accum += array[i].dz()/total;
//			array[i] = new Vector(array[i].dx(),array[i].dy(),accum);
			accum += array[i].dz()/this.totalEnergy;
//			accum = array[i].dz();
//			System.out.println("i: " + i + ", array[i]: " + array[i]);
			cumulativeFrequencies[i] = new Vector(array[i].dx(),array[i].dy(),accum);
//			System.out.println("CumFreq: " + cumulativeFrequencies[i]);
		}
//		this.quicksort(cumulativeFrequencies,0,cumulativeFrequencies.length-1);
	}
	
	public void print() {
		System.out.println("Unit Square");
		for (int i=0; i<cumulativeFrequencies.length; i++) {
			System.out.println("Number: " + i);
			System.out.println("UV: " + cumulativeFrequencies[i]);
			System.out.println("XYZ: " + XYZ(cumulativeFrequencies[i].dx()/n,cumulativeFrequencies[i].dy()/n));
//			int index = (n*(int)cumulativeFrequencies[i].dy() + (int)cumulativeFrequencies[i].dx());
//			System.out.println("Scaling factor: " + this.getScalingFactor(array[index]));
			System.out.println("UV: " + array[i]);
//			System.out.println("Scaling factor: " + this.getScalingFactor(cumulativeFrequencies[i]));
			System.out.println("Scaling factor: " + this.getScalingFactor(array[i]));
		}
//		Vector v = array[array.length-1];
//		System.out.println(XYZ(v.dx()/n,v.dy()/n));
		System.out.println();
	}
	
	public static void main(String[] args) {
		UnitSquare s = new UnitSquare(30);
		Point pos = new Point(0,0,0);
		Vector norm = new Vector(0,1,0);
		Vector v1 = new Vector(1,0,0);
		Vector v2 = new Vector(0,1,0);
		Vector v3 = new Vector(0,0,1);
		Photon p1 = new Photon(pos,v1,norm,new Vector(10,0,0));
		Photon p2 = new Photon(pos,v2,norm,new Vector(0,0,0.1));
		Photon p3 = new Photon(pos,v3,norm,new Vector(0,0,5));
		
		v1.normalise();
		v2.normalise();
		v3.normalise();
		System.out.println("v1: " + v1);
		System.out.println("v2: " + v2);
		System.out.println("v3: " + v3);
		
		for (int i=0; i<10; i++) {
			s.insertVector(p1.getIncidentDirection());
		}
		for (int i=0; i<30; i++) {
			s.insertVector(p2.getIncidentDirection());
		}
		for (int i=0; i<10; i++) {
			s.insertVector(p3.getIncidentDirection());
		}
		
		s.createCumulativeFrequencies();
		
		for (int i=0; i<10; i++) {
			System.out.println(s.getDirection(Math.random()));
		}
	}
}
