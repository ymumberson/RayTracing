/*
 * Code adapted from: https://www.geeksforgeeks.org/max-heap-in-java/
 */
public class PhotonMaxHeap {
	private Photon[] photons;
	private double[] distances;
	private int size;
	private int maxsize;
	
	public PhotonMaxHeap(int maxsize) {
		this.maxsize = maxsize+1;
		this.size = 0;
		this.photons = new Photon[maxsize+1];
		this.distances = new double[maxsize+1];
	}
	
	private int parent(int i) {
		return (i-1)/2;
	}
	
	private int left(int i) {
		return (2*i) + 1;
	}
	
	private int right(int i) {
		return (2*i) + 2;
	}
	
	private boolean isLeaf(int i) {
		if (i > size/2-1 && i < size) {
			return true;
		}
		return false;
	}
	
	private void swap(int i, int j) {
		Photon temp = photons[i];
		photons[i] = photons[j];
		photons[j] = temp;
		
		double temp2 = distances[i];
		distances[i] = distances[j];
		distances[j] = temp2;
	}
	
	public void maxHeapify(int i) {
		if (isLeaf(i)) return; //if leaf then return
		
		int right = right(i);
		int left = left(i);
		
		//if smaller than either child
		if (right < size) { //if has right
			if (distances[i] < distances[left]
					|| distances[i] < distances[right]) {
				
				if (distances[left] > distances[right]) {
					swap(i, left);
					maxHeapify(left);
				} else {
					swap(i, right);
					maxHeapify(right);
				}
			}
		} else {
			if (distances[i] < distances[left]) {
				swap(i,left);
			}
		}
		
	}
	
	public Photon getMaxPhoton() {
		return photons[0];
	}
	
	public double getMaxDistance() {
		return distances[0];
	}
	
	public void removeMax() {
		if (size <= 1) return; //Empty or contains one element
		
		distances[0] = distances[size-1];
		photons[0] = photons[size-1];
		size--;
		maxHeapify(0);
	}
	
	public void insert(Photon p, double d) {
		if (size < maxsize) {
			photons[size] = p;
			distances[size] = d;
			
			int current = size;
			while (distances[current] > distances[parent(current)]) {
				swap(current, parent(current));
				current = parent(current);
			}
			size++;	
		} else {
			photons[0] = p;
			distances[0] = d;
			maxHeapify(0);
		}
	}
	
	//Could return a smaller list than maxsize
	public Photon[] getPhotons() {
		return photons;
	}
	
	public double[] getDistances() {
		return distances;
	}
	
	public int getMaxSize() {
		return maxsize;
	}
	
	public int getSize() {
		return size;
	}
	
	public static void main(String[] args) {
		PhotonMaxHeap heap = new PhotonMaxHeap(10);
		System.out.println(heap);
		heap.insert(new Photon(new Point(1,1,1),new Vector(0,0,0), new Vector(0,0,0)),10);
		System.out.println(heap);
		heap.insert(new Photon(new Point(1,1,1),new Vector(0,0,0), new Vector(0,0,0)),5);
		System.out.println(heap);
		heap.insert(new Photon(new Point(1,1,1),new Vector(0,0,0), new Vector(0,0,0)),7);
		System.out.println(heap);
		heap.insert(new Photon(new Point(1,1,1),new Vector(0,0,0), new Vector(0,0,0)),2);
		heap.insert(new Photon(new Point(1,1,1),new Vector(0,0,0), new Vector(0,0,0)),12);
		heap.insert(new Photon(new Point(1,1,1),new Vector(0,0,0), new Vector(0,0,0)),1);
		heap.insert(new Photon(new Point(1,1,1),new Vector(0,0,0), new Vector(0,0,0)),17);
		heap.insert(new Photon(new Point(1,1,1),new Vector(0,0,0), new Vector(0,0,0)),8);
		heap.insert(new Photon(new Point(1,1,1),new Vector(0,0,0), new Vector(0,0,0)),3);
		heap.insert(new Photon(new Point(1,1,1),new Vector(0,0,0), new Vector(0,0,0)),11);
		System.out.println(heap);
		heap.removeMax();
		System.out.println(heap);
	}
	
	public String toString() {
		if (size == 0) return "Heap is empty";
		if (size == 1) return "Parent node: {" + distances[0] + "}\n";
		String str = "";
		for (int i=0; i<size/2; i++) {
			str += "Parent node: {" + distances[i] + "}\n";
			if (left(i) < size) {
				str += "Left child: {" + distances[left(i)] + "}\n";
			}
			if (right(i) < size) {
				str += "Right child: {" + distances[right(i)] + "}\n";
			}
		}
		return str;
	}
	
	public int getNumShadowPhotons() {
		if (size == 0) return 0;
		if (size == 1) return photons[0].isShadowPhoton()? 1 : 0;
		
		int numShadowPhotons = 0;
		for (int i=0; i<size; i++) {
			if (photons[i].isShadowPhoton()) numShadowPhotons++;
		}
//		for (int i=0; i<size/2; i++) {
////			str += "Parent node: {" + distances[i] + "}\n";
//			if (photons[i].isShadowPhoton()) numShadowPhotons++;
//			if (left(i) < size) {
////				str += "Left child: {" + distances[left(i)] + "}\n";
//				if (photons[left(i)].isShadowPhoton()) numShadowPhotons++;
//			}
//			if (right(i) < size) {
////				str += "Right child: {" + distances[right(i)] + "}\n";
//				if (photons[right(i)].isShadowPhoton()) numShadowPhotons++;
//			}
//		}
		return numShadowPhotons;
	}
	
	public int getNumIlluminationPhotons() {
		if (size == 0) return 0;
		if (size == 1) return photons[0].isIlluminationPhoton()? 1 : 0;
		
		int numIlluminationPhotons = 0;
		for (int i=0; i<size; i++) {
			if (photons[i].isIlluminationPhoton()) numIlluminationPhotons++;
		}
//		for (int i=0; i<size/2; i++) {
////			str += "Parent node: {" + distances[i] + "}\n";
//			if (photons[i].isIlluminationPhoton()) numIlluminationPhotons++;
//			if (left(i) < size) {
////				str += "Left child: {" + distances[left(i)] + "}\n";
//				if (photons[left(i)].isIlluminationPhoton()) numIlluminationPhotons++;
//			}
//			if (right(i) < size) {
////				str += "Right child: {" + distances[right(i)] + "}\n";
//				if (photons[right(i)].isIlluminationPhoton()) numIlluminationPhotons++;
//			}
//		}
		return numIlluminationPhotons;
	}
}
