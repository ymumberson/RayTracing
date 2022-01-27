import java.util.ArrayList;

/**
 * A balanced kd-tree used to store photons
 * @author MrYos
 *
 */
public class PhotonMap extends AABB {
	private PhotonMap left,right;
	private boolean isLeaf;
	private Photon[] photons; //Might swap out array for arraylist
	
	/**
	 * 
	 * @param photonList List of photons (Should not be empty)
	 */
	public PhotonMap(Point min, Point max) {
		super(min,max);
	}
	
	public void build(Photon[] ls, int MAX_DEPTH, int MAX_OBJECTS) {
		build(ls,MAX_DEPTH,MAX_OBJECTS,0);
	}
	
	public void build(Photon[] ls, int MAX_DEPTH, int MAX_OBJECTS, int depth) {
		if (depth >= MAX_DEPTH || ls.length <= MAX_OBJECTS) { //If at max depth, make this a leaf node
			this.photons = ls;
			this.isLeaf = true;
			return;
		}
		
		//Determine the split dimension
		int splitDim = depth % 3;
		
		//Sort list according to split plane
		quicksort(ls,splitDim,0,ls.length-1);
		
		//Create split plane and the left and right tree
		splitPlane(splitDim);
		
		//Create left and right list
		ArrayList<Photon> lsLeft = new ArrayList<Photon>();
		ArrayList<Photon> lsRight = new ArrayList<Photon>();
		double median = ls[(ls.length-1)/2].getSplitVal(splitDim); //Currently median goes in left list but I might need to change that.
		for (Photon p: ls) {
			//Assign p to either left list or right list, median goes to left list
			if (p.getSplitVal(splitDim) <= median) {
				lsLeft.add(p);
			} else {
				lsRight.add(p);
			}
		}
		
		//Build left and right subtrees
		left.build(lsLeft.toArray(new Photon[lsLeft.size()]), MAX_DEPTH, MAX_OBJECTS,depth+1);
		if (lsRight.isEmpty()) { //Right list could be empty, so set to null if empty to avoid computation
			right = null;
		} else {
			right.build(lsRight.toArray(new Photon[lsRight.size()]), MAX_DEPTH, MAX_OBJECTS,depth+1);
		}
//		right.build(lsRight.toArray(new Photon[lsRight.size()]), MAX_DEPTH, MAX_OBJECTS,depth+1);
	}
	
	private void splitPlane(int splitDim) {
		//For storing points for split
				Point minSplitPoint;
				Point maxSplitPoint;
				
				switch (splitDim) {
				case 1: //Splitting in y plane
					double newY = (min.y() + max.y())/2f;
					minSplitPoint = new Point(max.x(),newY,max.z());
					maxSplitPoint = new Point(min.x(),newY,min.z());
					break;
				case 2: //Splitting in z plane
					double newZ = (min.z() + max.z())/2f;
					minSplitPoint = new Point(max.x(),max.y(),newZ);
					maxSplitPoint = new Point(min.x(),min.y(),newZ);
					break;
				default: //Splitting in x plane
					double newX = (min.x() + max.x())/2f;
					minSplitPoint = new Point(newX,max.y(),max.z());
					maxSplitPoint = new Point(newX,min.y(),min.z());
				}
				
				//Creates left and right kd-tree using the split plane
				this.left = new PhotonMap(min,minSplitPoint);
				this.right = new PhotonMap(maxSplitPoint,max);
	}
	
	/**
	 * Adapted from: https://www.baeldung.com/java-quicksort
	 * @param ls
	 * @param splitDim
	 * @param lower
	 * @param upper
	 */
	private void quicksort(Photon[] ls, int splitDim, int lower, int upper) {
		if (lower < upper) {
			int partitionIndex = partition(ls,splitDim,lower,upper);
			quicksort(ls,splitDim,lower,partitionIndex-1);
			quicksort(ls,splitDim,partitionIndex+1,upper);
		}
	}
	
	/**
	 * Adapted from: https://www.baeldung.com/java-quicksort
	 * @param ls
	 * @param splitDim
	 * @param lower
	 * @param upper
	 * @return
	 */
	private int partition(Photon[] ls, int splitDim, int lower, int upper) {
		double pivot = ls[upper].getSplitVal(splitDim);
		int i = lower-1;
		
		for (int j=lower; j<upper; j++) {
			if (ls[j].getSplitVal(splitDim) <= pivot) {
				i++;
				
				Photon temp = ls[i];
				ls[i] = ls[j];
				ls[j] = temp;
			}
		}
		
		Photon temp = ls[i+1];
		ls[i+1] = ls[upper];
		ls[upper] = temp;
		
		return i+1;
	}
	
	public boolean isLeaf() {
		return this.isLeaf;
	}
	
	public int getDepth() {
		if (this.isLeaf) {
			return 1;
		} else {
			return 1 + left.getDepth();
		}
	}
	
	public int getNumPhotons() {
		if (this.isLeaf) {
			return photons.length;
		} else {
			if (right != null) {
				return left.getNumPhotons() + right.getNumPhotons();
			} else {
				return left.getNumPhotons();
			}
		}
	}
	
	/*
	 * TODO Make so that photons with vastly different incident directions from the point's surface normal aren't added,
	 * 	as these could be on a different surface than the current point.
	 * -> Also, currently it just returns the first N photons it find, and this may be a poor representation.
	 */
	public void getNearestNeighbours(Sphere s, ArrayList<Photon> neighbours, int N) {
//		System.out.println("Stack++");
		if (neighbours.size() >= N) return; //Already found n nearest neighbours so don't look for more
		
		if (this.isLeaf && this.intersects(s)) { //If leaf then check if photons are within the sphere
//			System.out.println("Leaf!");
			double rad = s.r();
			for (Photon p: photons) {
				//Check if within sphere, but don't return more than N photons
				double dist = getDistance(s,p.getPosition());
//				System.out.println(p.getPosition() + ": " + dist);
				if (dist <= rad && neighbours.size() <= N) {
					neighbours.add(p);
				}
			}
		} else {
			if (left.intersects(s)) {
				left.getNearestNeighbours(s,neighbours,N);
			}
			if (right != null && right.intersects(s)) {
				right.getNearestNeighbours(s,neighbours,N);
			}
		}
	}
	
	public double getDistance(Sphere s, Point p) {
		Point c = s.c();
		return Math.sqrt(Math.pow(c.x()-p.x(), 2) + Math.pow(c.y()-p.y(), 2) + Math.pow(c.z()-p.z(), 2));
	}
	
	/*
	 * https://developer.mozilla.org/en-US/docs/Games/Techniques/3D_collision_detection
	 */
	public boolean intersects(Sphere s) {
		Point c = s.c();
		//Get AABB point closest to sphere
		double x = Math.max(min.x(), Math.min(c.x(), max.x()));
		double y = Math.max(min.y(), Math.min(c.y(), max.y()));
		double z = Math.max(min.z(), Math.min(c.z(), max.z()));
		
		double dist = Math.sqrt(Math.pow((x - c.x()), 2)
				+ Math.pow((y - c.y()), 2)
				+ Math.pow((z - c.z()), 2));
		
		return dist <= s.r();
	}
	
	/**
	 * Experimenting with class.
	 * @param args
	 */
	public static void main(String[] args) {
//		Photon p1 = new Photon(new Point(0,1,2.2), new Vector(0,0,0), new Vector(0,0,0));
//		System.out.println(p1);
		PhotonMap map = new PhotonMap(new Point(0,0,0), new Point(10,10,10));
		int numPhotons = 1000000;
		Photon[] arr = new Photon[numPhotons];
		for (int i=0; i<numPhotons; i++) {
			double x = Math.random() * 10;
			double y = Math.random() * 10;
			double z = Math.random() * 10;
			arr[i] = new Photon(new Point(x,y,z), new Vector(0,0,0), new Vector(0,0,0));
//			System.out.println(arr[i]);
		}
		
//		System.out.println("Quicksort:");
//		map.quicksort(arr,2,0,numPhotons-1);
//		for (Photon p: arr) {
//			System.out.println(p);
//		}
		
		/*
		 * Building a photon map with 1,000,000 photons takes 3184ms
		 */
		System.out.println("Building photon map: " + numPhotons + " elements.");
		long start = System.currentTimeMillis();
		map.build(arr, 20, 1);
		long finish = System.currentTimeMillis();
		System.out.println("Build time: " + (finish-start) + "ms");
		System.out.println(map.getDepth());
		
		int numSearch = 500;
		System.out.println("\nGetting " + numSearch + " nearest neighbours:");
		double x = Math.random() * 10;
		double y = Math.random() * 10;
		double z = Math.random() * 10;
		Sphere s = new Sphere(new Point(x,y,z),1f);
//		Sphere s = new Sphere(new Point(1,1,1),1);
		System.out.println("Point centre: " + s.c());
		start = System.currentTimeMillis();
		ArrayList<Photon> ls = new ArrayList<Photon>();
		map.getNearestNeighbours(s, ls, numSearch);
		finish = System.currentTimeMillis();
		System.out.println("Search time: " + (finish-start) + "ms");
		System.out.println("Size of returned list = " + ls.size());
		System.out.println("Num photons in map = " + map.getNumPhotons());
//		for (int i=0; i<ls.size(); i++) {
//			System.out.println(ls.get(i));
//		}
	}
}
