import java.util.ArrayList;

public class KdTree extends AABB{
	private KdTree left,right;
	private ArrayList<Tracable> tracables = null; //Null unless is leaf node
	
	public KdTree(Point min, Point max) {
		super(min, max);
	}
	
	public void build(ArrayList<Tracable> ls,int MAX_DEPTH, int MAX_OBJECTS) {
		this.build(ls, MAX_DEPTH, MAX_OBJECTS, 0);
	}
	
	public void build(ArrayList<Tracable> ls,int MAX_DEPTH, int MAX_OBJECTS, int depth) {
		if (depth >= MAX_DEPTH || ls.size() <= MAX_OBJECTS) {
			this.tracables = ls;
//			System.out.println("Creating leaf nodes:" + tracables.size());
			return;
		} else {
			//Create two AABBs by splitting the current AABB
			this.splitPlane(depth % 3);
			
			//Place objects into corresponding AABBs
			ArrayList<Tracable> leftLs = new ArrayList<Tracable>();
			ArrayList<Tracable> rightLs = new ArrayList<Tracable>();
			for (int i=0; i<ls.size(); i++) {
				Tracable tr = ls.get(i);
				AABB objBox = tr.generateAABB();
				if (objBox.overlaps(this.left)) {leftLs.add(tr);}
				if (objBox.overlaps(this.right)) {rightLs.add(tr);}
//				if (!objBox.overlaps(this.left) && !objBox.overlaps(right)) {
//					System.out.println("Here" + tr);
//					System.out.println(left);
//					System.out.println(right);
//				}
			}
			
			//Recurse on child nodes
			left.build(leftLs, MAX_DEPTH, MAX_OBJECTS, depth+1);
			right.build(rightLs, MAX_DEPTH, MAX_OBJECTS, depth+1);
		}
	}
	
	private void splitPlane(int splitDim) {
		//For storing points for split
		Point minSplitPoint;
		Point maxSplitPoint;
		double padding = 0.0001;
		
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
			
			//Trying to fix line in middle of screen :(
//			double newXMin = Math.ceil((min.x() + max.x())/2f);
//			double newXMax = Math.floor((min.x() + max.x())/2f);
//			minSplitPoint = new Point(newXMin,max.y(),max.z());
//			maxSplitPoint = new Point(newXMax,min.y(),min.z());
		}
		
		//Creates left and right kd-tree using the split plane
		this.left = new KdTree(min,minSplitPoint);
		this.right = new KdTree(maxSplitPoint,max);
	}
	
	public boolean isLeaf() {
		return tracables != null;
	}
	
	public Tracable getTracable(Ray r) {
		if (isLeaf()) {
			if (tracables.size() == 0) {
//				System.out.println("Empty Leaf");
				return null;
			}
			//Storing the first intersection
			int index = -1;
			double smallestT = Double.POSITIVE_INFINITY;
			
			//Finding the first intersection
			for (int i=0; i<tracables.size(); i++) {
				Tracable trac = tracables.get(i);
				double t = trac.getIntersect(r);
				if (t < smallestT && t >= 0) {
					smallestT = t;
					index = i;
				}
			}
			
			if (smallestT >= 0 && index >= 0) { //If there was an intersection
				return tracables.get(index);
			} else { //Otherwise nothing intersected
				return null;
			}
		} else {
			//Get intersects for left and right tree
			double leftT = left.getIntersect(r);
			double rightT = right.getIntersect(r);
			boolean intersectsLeft = leftT >= 0;
			boolean intersectsRight = rightT >= 0;
			
			if (!intersectsLeft && !intersectsRight) {
				return null;
			} else if (intersectsLeft && intersectsRight){
				if (leftT < rightT) {
					Tracable leftTrac = left.getTracable(r);
					if (leftTrac != null) {
						return leftTrac;
					}
					Tracable rightTrac = right.getTracable(r);
					if (rightTrac != null) {
						return rightTrac;
					}
					return null;
				} else {
					Tracable rightTrac = right.getTracable(r);
					if (rightTrac != null) {
						return rightTrac;
					}
					Tracable leftTrac = left.getTracable(r);
					if (leftTrac != null) {
						return leftTrac;
					}
					return null;
				}
			} else if (intersectsLeft) {
				Tracable leftTrac = left.getTracable(r);
				if (leftTrac != null) {
					return leftTrac;
				}
			} else {
				Tracable rightTrac = right.getTracable(r);
				if (rightTrac != null) {
					return rightTrac;
				}
			}
			return null;
		}
	}
	
	public Tracable getTracableIgnoreSelf(Ray r, Tracable self) {
		if (isLeaf()) {
			if (tracables.size() == 0) {
//				System.out.println("Empty Leaf");
				return null;
			}
			//Storing the first intersection
			int index = -1;
			double smallestT = Double.POSITIVE_INFINITY;
			
			//Finding the first intersection
			for (int i=0; i<tracables.size(); i++) {
				Tracable trac = tracables.get(i);
				if (trac != self) {
					double t = trac.getIntersect(r);
					if (t < smallestT && t >= 0) {
						smallestT = t;
						index = i;
					}
				}
			}
			
			if (smallestT >= 0 && index >= 0) { //If there was an intersection
				return tracables.get(index);
			} else { //Otherwise nothing intersected
				return null;
			}
		} else {
			//Get intersects for left and right tree
			double leftT = left.getIntersect(r);
			double rightT = right.getIntersect(r);
			boolean intersectsLeft = leftT >= 0;
			boolean intersectsRight = rightT >= 0;
			
			if (!intersectsLeft && !intersectsRight) {
				return null;
			} else if (intersectsLeft && intersectsRight){
				if (leftT < rightT) {
					Tracable leftTrac = left.getTracableIgnoreSelf(r,self);
					if (leftTrac != null) {
						return leftTrac;
					}
					Tracable rightTrac = right.getTracableIgnoreSelf(r,self);
					if (rightTrac != null) {
						return rightTrac;
					}
					return null;
				} else {
					Tracable rightTrac = right.getTracableIgnoreSelf(r,self);
					if (rightTrac != null) {
						return rightTrac;
					}
					Tracable leftTrac = left.getTracableIgnoreSelf(r,self);
					if (leftTrac != null) {
						return leftTrac;
					}
					return null;
				}
			} else if (intersectsLeft) {
				Tracable leftTrac = left.getTracableIgnoreSelf(r,self);
				if (leftTrac != null) {
					return leftTrac;
				}
			} else {
				Tracable rightTrac = right.getTracableIgnoreSelf(r,self);
				if (rightTrac != null) {
					return rightTrac;
				}
			}
			return null;
		}
	}
	
	public KdTree getLeft() {
		return this.left;
	}
	
	public KdTree getRight() {
		return this.right;
	}
	
	public int getNumTracables() {
		if (this.isLeaf()) {
			return tracables.size();
		} else {
			return left.getNumTracables() + right.getNumTracables();
		}
	}
//	
//	public boolean showTree(Ray r) {
//		double t = this.getIntersect(r);
//		if (t >= 0) {
//			return showTree(r.getPoint(t));
//		}
//		return false;
//	}
//	
//	private boolean showTree(Point dir) {
//		if (dir.x() == this.min.x() || dir.x() == this.max.x() ||
//				dir.y() == this.min.y() || dir.y() == this.max.y() ||
//				dir.z() == this.min.z() || dir.z() == this.max.z()) {
//			return true;
//		} else {
//			boolean temp = false;
//			if (this.left != null) {
//				if (left.showTree(dir)) {
//					return true;
//				}
//			}
//			if (this.right != null) {
//				return right.showTree(dir);
//			}
//		}
//		return false;
//	}
}
