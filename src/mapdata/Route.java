package mapdata;

import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * An array of nodes that represents a path from one destination to another
 * Can determine whether a location is on or off this route using information from a 
 * gps device
 * @author williamloughlin
 *
 */
public class Route {
	
	// Difference in degrees the onRoute method will tolerate
	public static double TOLERANCE = 10;

	private ArrayList<Node> Nodes;
	private ArrayList<String> Directions;
	private int currentIndex;

	
	/**
	 * Constructor takes an arrayList of nodes and uses it to construct a full path
	 * @param nodes The nodes on this route
	 * @param directions The printout for each segment of this route
	 */
	public Route(ArrayList<Node> nodes, ArrayList<String> directions)
	{
		Nodes = nodes;
		Directions = directions;
		currentIndex = 0;
	}
	
	/**
	 * Accessor for an iterator over the nodes in this route
	 * @return An iterator over the nodes of this route
	 */
	public Iterator<Node> nodeIterator()
	{
		return Nodes.iterator();
	}
	
	/**
	 * Returns the printout for each segment in a list
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		for(String s : Directions)
		{
			sb.append(s+"\n");

		}
		return sb.toString();
	}
	
	/**
	 * Method that checks whether the user is currently on or off the route
	 * Calculates the distance from the users location to the line formed by the segment they are
	 * currently traveling on
	 * @param location The current location of the user
	 * @return True if the user is on route, false otherwise
	 */
	public boolean onRoute(Point2D loc)
	{
		Node from = Nodes.get(currentIndex);
		Node to = Nodes.get(currentIndex+1);
		
		if(to.calcDist(loc) < 0.009)
		{
			if(currentIndex == Nodes.size()-1)
			{
				return true;
			}
			currentIndex++;
			return true;
		}
		
		double fromX = from.getLon();
		double fromY = from.getLat();
		double toX = to.getLon();
		double toY = to.getLat();
		double locX = loc.getX();
		double locY = loc.getY();
		
		double m = (toY-fromY)/(toX-fromX);
		double perpM = -1/m;
		
		double b = fromY-m*fromX;
		double perpB = locY-perpM*locX;
		
		double interX = -(b-perpB)/(m-perpM);
		double interY = m*interX+b;
		
		double avgLat = (loc.getY()+interY)/2;
		
		double diffX = (locX-interX)*Math.cos(Math.PI*avgLat/180)*69;
		double diffY = (locY-interY)*69;
		double dist = Math.sqrt(diffX*diffX+diffY*diffY);
		
		if(dist > 0.01)
		{
			return false;
		}
		else
		{
			return true;
		}
		
	}
}
