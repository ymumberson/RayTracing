
public class PhotonMaxHeap {
	private Photon[] photons;
	private double[] distances;
	private int size;
	private int maxsize;
	
	public PhotonMaxHeap(int maxsize) {
		this.maxsize = maxsize;
		this.size = 0;
		this.photons = new Photon[maxsize];
		this.distances = new double[maxsize];
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
		if (i > size/2 && i <= size) {
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
		
		//if smaller than either child
		if (distances[i] < distances[left(i)]
				|| distances[i] < distances[right(i)]) {
			
			if (distances[left(i)] > distances[right(i)]) {
				swap(i, left(i));
				maxHeapify(left(i));
			} else {
				swap(i, right(i));
				maxHeapify(right(i));
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
		if (size == maxsize) return; //don't add if heap is full
		
		photons[size] = p;
		distances[size] = d;
		
		int current = size;
		while (distances[current] > distances[parent(current)]) {
			swap(current, parent(current));
			current = parent(current);
		}
		size++;
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
}
