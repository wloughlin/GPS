package mapdata;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Immutable class used to represent a way by representing it as a series of points
 * @author williamloughlin
 *
 */
public class Way {

	private ArrayList<Node> Nodes;
	private HashSet<Node> nodeSet;
	
	private String Id;
	
	private HashMap<String, String> tags;
	
	/*
	 * Easy identifier for which rodes are driveable or not
	 * 0 = Foot/bike paths
	 * 1 = unclassified
	 * 2 = side Streets
	 * 3 = main roads
	 * 4 = highways
	 */
	private int Type;
	
	

	public Way(List<Node> nodes, String id, HashMap<String, String> tags)
	{
		Nodes = new ArrayList<Node>(nodes);
		nodeSet = new HashSet<Node>(nodes);
		Id = id;
		this.tags = tags;
		setType(tags.get("highway"));
	}
	
	/**
	 * Accessor for the id of this way
	 * @return the id of this way
	 */
	public String getId()
	{
		return Id;
	}
	
	/**
	 * Accessor for the Name of this way
	 * @return The name of this way
	 */
	public String getName()
	{
		return tags.get("name");
	}
	
	/**
	 * Accessor for the Type of this way
	 * @return A number corresponding to the type of this way
	 * 
	 * 0 - Footpath
	 * 1 - Unclassified
	 * 2 - Residential
	 * 3 - Service
	 * 4 - Highway
	 *
	 */
	public int getType()
	{
		return Type;
	}
	
	/**
	 * Accessor for an Iterator over this way's nodes
	 * @return an iterator over this way's nodes
	 */
	public Iterator<Node> nodeIterator()
	{
		return Nodes.iterator();
	}
	
	/**
	 * Method that tests whether this way contains a specified node
	 * @param node The node to be tested
	 * @return true if this way contains the given object
	 */
	public boolean contains(Node node)
	{
		return nodeSet.contains(node);
	}
	
	/**
	 * Overrides the hashCode method
	 * @return the hashcode of this node
	 */
	@Override
	public int hashCode()
	{
		int h = 0;
		for(int i = 0; i < Id.length(); i++)
		{
			h = h * 31 + Id.charAt(i);
		}
		return h;
	}
	
	/**
	 * Overrides the equals method
	 * @return Whether another node is identical to this node
	 */
	@Override
	public boolean equals(Object other)
	{
		if(other == this)
		{
			return true;
		}
		if(other == null)
		{
			return false;
		}
		if(getClass() != other.getClass())
		{
			return false;
		}
		return Id.equals(((Way)other).getId());
	}
	
	/**
	 * sets the type of this way based on its highway tag from an osm file
	 * @param The value of the highwaytag of this way
	 */
	private void setType(String s)
	{
		if(s == null)
		{
			Type = -1;
		}
		else if(s.equals("footpath") || s.equals("footway") || s.equals("path")
				|| s.equals("steps") || s.equals("proposed"))
		{
			Type = 0;
		}
		else if(s.equals("unclassified") || s.contains("link") || s.equals("road"))
		{
			Type = 1;
		}
		else if(s.equals("residential") || s.equals("service"))
		{
			Type = 2;
		}
		else if(s.equals("tertiary") || s.equals("secondary") || s.equals("primary") || s.equals("motorway"))
		{
			Type = 3;
		}
		else if(s.equals("highway") || s.equals("trunk"))
		{
			Type = 4;
		}
	}
	
	
	/**
	 * Method to calculate the distance between two nodes on this way
	 * @param start The beginning node
	 * @param end The ending node
	 * @return The distance between start and end
	 * @precondition start and end are both in this way
	 */
	public double segmentDist(Node start, Node end)
	{
		double dist = 0;
		Iterator<Node> it = Nodes.iterator();
		Node prev = null;
		boolean onSeg = false;
		while(it.hasNext())
		{
			Node n = it.next();
			
			if(onSeg)
			{
				dist += n.calcDist(prev.getPoint());
			}
			if(start.equals(n) || end.equals(n))
			{
				onSeg = !onSeg;
			}
			prev = n;
		}
		return dist;
	}
	
	/**
	 * Method to get the nodes that make up a portion of a way
	 * @param start The beginning of the desired portion
	 * @param end The end of the desired portion
	 * @return An arraylist of nodes in this way ordered from start to end
	 * @precondition Start and end are in this way
	 */
	public Iterator<Node> getSegment(Node start, Node end)
	{
		ArrayList<Node> segment = new ArrayList<Node>();
		Iterator<Node> it = Nodes.iterator();
		boolean onSeg = false;
		while(it.hasNext())
		{
			Node n = it.next();
			
			if(onSeg)
			{
				segment.add(n);
			}
			if(start.equals(n) || end.equals(n))
			{
				onSeg = !onSeg;
			}
			
		}
		if(segment.get(segment.size()-1).equals(start))
		{
			Collections.reverse(segment);
			segment.add(end);
		}
		else
		{
			segment.add(0, start);
		}
		return segment.iterator();
	}
	
	
	/**
	 * Method to get the adjacent nodes of a node in this way
	 * @param node The node being examined
	 * @return An arraylist of nodes adjacent to the parameter node
	 * @precondition node belongs to this way
	 */
	public ArrayList<Node> getAdjacentNodes(Node node)
	{
		ArrayList<Node> adj = new ArrayList<Node>();
		int position = Nodes.indexOf(node);
		if(position-1 >= 0)
		{
			adj.add(Nodes.get(position-1));
		}
		if(position+1 < Nodes.size())
		{
			adj.add(Nodes.get(position+1));
		}
		return adj;
	}
	
	/**
	 * catch all method for getting the tags of a way
	 * @param key The desired tag
	 * @return The value of the desired tag
	 */
	public String getTagValue(String key)
	{
		return tags.get(key);
	}
}
