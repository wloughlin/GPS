package mapdata;

import java.awt.geom.Point2D;

/**
 * Class used to represent a geographic point. Immutable
 * @author williamloughlin
 *
 */

public class Node {

	private double Latitude;
	private double Longitude;
	
	private String Id;
	
	
	public Node(double lat, double lon, String id)
	{
		Latitude = lat;
		Longitude = lon;
		Id = id;
	}
	
	/**
	 * Accessor for the Latitude of this node
	 * @return The latitude of this node
	 */
	public double getLat()
	{
		return Latitude;
	}
	
	/**
	 * Accessor for the longitude of this node
	 * @return The latitude of this node
	 */
	public double getLon()
	{
		return Longitude;
	}
	
	/**
	 * Accessor for the id of this node
	 * @return the id of this node
	 */
	public String getId()
	{
		return Id;
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
		return Id.equals(((Node)other).getId());
	}
	
	
	/**
	 * Returns a Point2D object with the coordinates of this node
	 * @return A point2D object whose x and y are equal to this nodes coordinates
	 */
	public Point2D.Double getPoint()
	{
		return new Point2D.Double(Longitude, Latitude);
	}
	
	/**
	 * Method to calculate the distance from a node to a point
	 * @param point The point 
	 * @return the distance to point in miles
	 */
	public double calcDist(Point2D other)
	{
		double otherLat = other.getY();
		double otherLon = other.getX();
		double avgLat = (Latitude + otherLat)/2;
		
		double diffX = (Longitude-otherLon)*Math.cos(Math.PI*avgLat/180)*69;
		double diffY = (Latitude - otherLat)*69;
		return Math.sqrt(diffX*diffX+diffY*diffY);
	}
	
	
}
