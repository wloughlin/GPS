package mapdisplay;

import java.awt.Dimension;

import java.awt.geom.Point2D;

import mapdata.Data;


/**
 * Converts information about nodes and ways into pixel coordinates that can be drawn
 * on the screen
 * @author williamloughlin
 *
 */
public class DataConverter {
	
	private int Scale;
	private Point2D center;
	private Dimension panelSize;
	
	
	
	public DataConverter(Data data, int scale, Dimension size)
	{
		panelSize = size;
		Scale = scale;
		center = new Point2D.Double((data.maxLon()+data.minLon())/2, 
				(data.maxLat()+data.minLat())/2);
	}
	
	/**
	 * Method for converting a point in degrees to a point in coordinates
	 * @return a point in pixel coordinates
	 */
	public Point2D degToPix(Point2D coord)
	{
		double X = (((panelSize.getWidth()/2)+ (Math.cos(Math.PI*coord.getY()/180)*
				(coord.getX() - center.getX())*Scale)));
		double Y = ((panelSize.getHeight()/2)+((center.getY() - coord.getY())*Scale));
		return new Point2D.Double(X, Y);
	}
	
	
	/**
	 * Method for converting a point in pixels to degrees
	 * @return A point in degree coordinates
	 */
	public Point2D pixToDeg(Point2D pixs)
	{
		double Y = center.getY()-(pixs.getY()-(panelSize.getHeight()/2))/Scale;
		double X = center.getX()+(pixs.getX()-(panelSize.getWidth()/2))/
				(Scale*Math.cos(Math.PI*Y/180));
		return new Point2D.Double(X, Y);
	}

	
	
	/**
	 * Allows the graphical objects to be translated across the screen when 
	 * moved by the user
	 * @param dx The change in x position
	 * @param dy The change in y position
	 */
	public void translate(double dx, double dy)
	{
		double newX = center.getX()+(dx/(Scale*Math.cos(center.getY()*Math.PI/180)));
		double newY = center.getY()+(dy/Scale);
		center.setLocation(newX, newY);
		
	}
	
	/**
	 * Allows the user to zoom in or out by changing the scale
	 * @param inc The amount the scale will be changed by
	 */
	public void incrementScale(int inc)
	{
		if(Scale+inc < Integer.MAX_VALUE && Scale+inc > 0)
			Scale += inc;
		
	}
	
	/**
	 * Allows the Converted to be informed when the pixel center of the screen changes
	 * @param The new Dimensions of the map
	 */
	public void updatePanelSize(Dimension d)
	{
		panelSize = d;
	}
	
	/**
	 * Sets the point this data converter should be centered on
	 * @param newCenter The new center point
	 */
	public void setCenter(Point2D newCenter)
	{
		center = newCenter;
	}
	
	/**
	 * Accessor for the zoom factor of this panel
	 * @return the zoom factor of this panel
	 */
	public int getZoom()
	{
		return Scale;
	}
	
}
