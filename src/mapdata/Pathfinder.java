package mapdata;

import java.util.ArrayList;



import java.util.Collections;
import java.util.HashMap;

import java.util.Iterator;



/**
 * Class used to calculate the shortest path between two points using Dijkstra's algorithm
 * and construct a route object
 * 
 * @author williamloughlin
 *
 */
public class Pathfinder {

	private Data data;

	public Pathfinder(Data data) {
		this.data = data;
	}
	
	/**
	 * Method implementing dijksrta's algorithm using a Fibonacci tree
	 * @param start The beginning node
	 * @param end The end node
	 * @return A route representing the shortest path from start to end
	 */
	public Route getBestPath(Node start, Node end) 
	{
		
		FibonacciHeap<Node> tree = new FibonacciHeap<Node>();
		ArrayList<Node> path = new ArrayList<Node>();
		
		HashMap<Node, Node> predecessor = new HashMap<Node, Node>();
		
		int remaining = 0;
	

		Iterator<Node> it = data.reachableIterator();
		while (it.hasNext()) 
		{
			tree.put(it.next(), Double.POSITIVE_INFINITY);
			remaining++;
		}
		tree.decreaseKey(start, 0.0);

		while (remaining > 0) 
		{
			
			double dist = tree.minValue();
			Node current = tree.extractMin();
			
			if (current == null)
				return null;
				
			if (current.equals(end))
				break;

			ArrayList<Node> adj = data.getAdjacentNodes(current);
			for (Iterator<Node> it3 = adj.iterator(); it3.hasNext();) 
			{
				Node next = it3.next();
				if(tree.decreaseKey(next, dist + current.calcDist(next.getPoint())))
					predecessor.put(next, current);
				
			}
			remaining--;
		}

		if (tree.valueOf(end) == Double.POSITIVE_INFINITY) 
		{
			return null;
		} 
		else 
		{
			Node n = end;
			while (!n.equals(start)) 
			{
				path.add(n);
				n = predecessor.get(n);
			}
			path.add(start);
			Collections.reverse(path);
			
			return new Route(path, makeDirections(path));
		}

	}
	
	/**
	 * Method to construct the directions for each segments of a calculated route
	 * @param path the arraylist of nodes that constitutes the route
	 * @return An arraylist of Directions for each segment
	 */
	private ArrayList<String> makeDirections(ArrayList<Node> path)
	{
		ArrayList<String> segments = new ArrayList<String>();
		double totalDist = 0;
		Node segStart = path.get(0);
		Node prev = null;
		Way currentWay = data.getSharedWay(segStart, path.get(1));
		for(Node node : path)
		{
			if(prev != null)
			{
				Way sharedWay = data.getSharedWay(prev, node);
				if(sharedWay != currentWay)
				{
					double segDist = data.getWayLength(segStart, prev);
					segDist = Math.round(segDist*100);
					segDist = segDist/100;
					totalDist += segDist;
					if(currentWay.getName() != null)
					{
						segments.add(currentWay.getName() + ": " + segDist + " miles.");
					}
					else
					{
						segments.add("Unkown Street: " + segDist + " miles.");	
					}

					segStart = prev;
				
				}
				currentWay = sharedWay;
			}
			
			prev = node;
		}
		currentWay = data.getSharedWay(segStart, prev);
		double finalDist = data.getWayLength(segStart, prev);
		finalDist = Math.round(finalDist*100);
		finalDist = finalDist/100;
		if(currentWay.getName() != null)
		{
			segments.add(currentWay.getName() + ": " + finalDist + " miles.");
		}
		else
		{
			segments.add("Unkown Street: " + finalDist + " miles.");
		}
		segments.add("Total: " + (totalDist+finalDist) + " miles.");
		
		return segments;
	}
	

	/**
	 * Data structure to allow log(n) access of the node with the smallest value.
	 * Coded specifically to represent the value of an object with a double as to avoid the 
	 * necessity of a comparitor
	 * @author williamloughlin
	 *
	 * @param <K> The type of object to be organized by this tree
	 */
	private class FibonacciHeap<K> 
	{
		
		private TreeNode Min;
		private DoublyLinkedList<TreeNode> roots;
		private int size;
		
		private HashMap<K, TreeNode> map;
		
		public FibonacciHeap()
		{
			Min = null;
			roots = new DoublyLinkedList<TreeNode>();
			map = new HashMap<K, TreeNode>();
			size = 0;
		}
		
		/**
		 * Puts an object into this tree by adding it to the list of roots and adjusting the 
		 * min if necessary
		 * @param key The object to be put in
		 * @param value The priority of this object
		 */
		public void put(K key, double value)
		{
			TreeNode newNode = new TreeNode(key, value, 0);
			map.put(key, newNode);
			roots.addFirst(newNode);
			if(Min == null || value < Min.value)
			{
				Min = newNode;
			}
			size++;
		}
		
		/**
		 * Accessor for the priority of a key
		 * @param key The object 
		 * @return The priority of the object
		 * @precondition The object is in the tree
		 */
		public double valueOf(K key)
		{
			return map.get(key).value;
		}
		
		/**
		 * Accessor for the priority of the minimum key
		 * @return the priority of the minimum key
		 */
		public double minValue()
		{
			return Min.value;
		}
		
		/**
		 * Extracts the object with the highest priority from the tree and recalculates an new highest
		 * priority object
		 * @return The object with the highest priority
		 */
		public K extractMin()
		{
			TreeNode temp = Min;
			roots.remove(Min);
			for(Iterator<TreeNode> it = temp.children.iterator(); it.hasNext();)
			{
				TreeNode n = it.next();
				n.parent = null;
				roots.addFirst(n);
			}
			consolidate();
			TreeNode newMin = null;
			for(Iterator<TreeNode> it = roots.iterator(); it.hasNext();)
			{
				TreeNode current = it.next();
				if(newMin == null || current.value < newMin.value)
				{
					newMin = current;
				}
			}
			Min = newMin;
			return temp.key;
			
			
		}
		
		/**
		 * Reduces the number of roots to log(n) by merging roots with the same degree into a 
		 * single tree
		 */
		private void consolidate()
		{
			FibonacciHeap.TreeNode[] store = new FibonacciHeap.TreeNode[size];
			Iterator<TreeNode> it = roots.iterator();
			while(it.hasNext())
			{
				mergeRoots(it.next(), store);
			}
			
			roots = new DoublyLinkedList<TreeNode>();
			for(TreeNode n : store)
			{
				if(n != null)
					roots.addFirst(n);
			}
		}
		
		/**
		 * recursivley merges roots with other roots of the same degree
		 * @param root The root to be merged
		 * @param store The temporary array to hold the roots with merging is taking place
		 */
		private void mergeRoots(TreeNode root, FibonacciHeap.TreeNode[] store)
		{
			if(store[root.degree] == null)
			{
				store[root.degree] = root;
			}
			else
			{
				TreeNode prev = store[root.degree];
				if(prev.value < root.value)
				{
					prev.children.addFirst(root);
					root.parent = prev;
					store[prev.degree] = null;
					prev.degree++;
					mergeRoots(prev, store);
				}
				else
				{
					root.children.addFirst(prev);
					prev.parent = root;
					store[prev.degree] = null;
					root.degree++;
					mergeRoots(root, store);
				}
			}
		
		}
			
		/**
		 * Decreases the priority of a key if the given priority is lower than its current priority
		 * and reheapifies if necessary
		 * @param key The object to be reduced
		 * @param newValue The new priority
		 * @return true if the new priority is less than the old priority
		 */
		public boolean decreaseKey(K key, double newValue)
		{
			TreeNode n = map.get(key);
			if(n.value <= newValue)
			{
				return false;
			}
			n.value = newValue;
			if(n.parent != null && n.parent.value > newValue)
			{
				// mark
				n.parent.children.remove(n);
				mark(n.parent);
				n.parent = null;
				roots.addFirst(n);
			}
			if(Min.value > newValue)
			{
				Min = n;
			}
			return true;
		}
		
		/**
		 * method to mark nodes or cut nodes that have already been marked to preserve the 
		 * properties of the Fibonacci tree
		 * @param The node to mark
		 */
		private void mark(TreeNode n)
		{
			Iterator<TreeNode> it = roots.iterator();
			while(it.hasNext())
			{
				if(it.next() == n)
					return;
			}
			if(n.marked == false)
			{
				n.marked = true;
			}
			else 
			{
				n.parent.children.remove(n);
				mark(n.parent);
				n.parent = null;
				roots.addFirst(n);
				n.degree = 0;
			}
		}

		/**
		 * Objects representing nodes in the fibonacci heap
		 * 
		 * @author williamloughlin
		 *
		 */
		private class TreeNode
		{
			private K key;
			private double value;
			private boolean marked;
			private DoublyLinkedList<TreeNode> children;
			private int degree;
			private TreeNode parent;
			
			public TreeNode(K key, double value, int degree)
			{
				this.key = key;
				this.value = value;
				marked = false;
				children = new DoublyLinkedList<TreeNode>();
				this.degree = degree;
				parent = null;
			}
		}
	}
	
	/**
	 * Class used to link roots and children in the fibonacci heap
	 * Doesn't support all methods of a typical list, just the ones that I thought would be
	 * useful for the heap
	 * @author williamloughlin
	 *
	 * @param <E>
	 */
	private class DoublyLinkedList<E>  {

		private int size;
		private Node sent;
		
		public DoublyLinkedList()
		{
			size = 0;
			sent = new Node(null, null, null);
			sent.next = sent;
			sent.prev = sent;
		}
		
		/**
		 * Adds an element to the list
		 * @param element The element to be added
		 */
		public void addFirst(E element)
		{
			Node sec = sent.next;
			Node newNode = new Node(element, sec, sent);
			sec.prev = newNode;
			sent.next = newNode;
			size++;
		}
		
		/**
		 * Iterator access
		 * @return An iterator over the objects in this list
		 */
		public Iterator<E> iterator()
		{
			return new DLLIterator();
		}
		
		/**
		 * Removes an object from the list in 0(n) time
		 * @param e The object to remove
		 */
		public void remove(E e)
		{
			Iterator<E> it = new DLLIterator();
			while(it.hasNext())
			{
				if(it.next() == e)
				{
					it.remove();
					size--;
					return;
				}
			}
			
		}
		
		/**
		 * Method to concatanate lists. Not used
		 * @param other The other list to be added
		 */
		public void concatanate(DoublyLinkedList<E> other)
		{
			Node otherSent = other.sent;
			Node otherFirst = otherSent.next;
			Node otherLast = otherSent.prev;
			otherFirst.prev = sent.prev;
			otherLast.next = sent;
			sent.prev = otherLast;
			otherSent.next = null;
			otherSent.prev = null;
			size += other.size;
		}
		
		/**
		 * Accessor for the size of this list
		 * @return The size of the list
		 */
		public int size()
		{
			return size;
		}

		/**
		 * Iterator over the objects in a doubly linked list
		 * @author williamloughlin
		 *
		 */
		private class DLLIterator implements Iterator<E>
		{
			
			private Node current;
			
			public DLLIterator()
			{
				current = sent;
			}

			@Override
			public boolean hasNext() {
				
				return current.next.element != null;
			}

			@Override
			public E next() {
				
				current = current.next;
				return current.element;
			}
			
			@Override
			public void remove()
			{
				Node newCurrent = current.prev;
				current.prev.next = current.next;
				current.next.prev = current.prev;
				current.next = null;
				current.prev = null;
				current = newCurrent;
			}
			
		}

		/**
		 * Node in the doubly linked list
		 * @author williamloughlin
		 *
		 */
		private class Node
		{
			private E element;
			private Node next;
			private Node prev;
			
			private Node(E e, Node next, Node prev)
			{
				element = e;
				this.next = next;
				this.prev = prev;
			}
		}
	}
}
