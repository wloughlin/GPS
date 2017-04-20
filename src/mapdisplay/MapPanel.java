package mapdisplay;


import java.awt.*;


import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.Iterator;

import javax.swing.*;

import com.starkeffect.highway.GPSDevice;
import com.starkeffect.highway.GPSEvent;
import com.starkeffect.highway.GPSListener;

import mapdata.Data;
import mapdata.Node;
import mapdata.Pathfinder;
import mapdata.Route;
import mapdata.Way;
	

/**
 * Panel that draws and allows the user to interact with a visual map
 * @author williamloughlin
 *
 */
public class MapPanel extends JPanel {

	private Data data;
	private DataConverter converter;
	private Node Start;
	private Node End;
	private Route route;
	private Point2D pointClicked;
	
	private Point2D currentLocation;
	private GPSDevice GPS;
	private RouteTracker tracker;
	
	// While true, the mappanel will center on the users location and processes gps events
	private boolean driving;
	
	private JTextArea directions;
	

	
	private JLabel indicator;
	
	public MapPanel(Data data)
	{
		this.data = data;
		route = null;
		driving = false;
		indicator = new JLabel("Driving: false");
		setUp();
		converter = new DataConverter(data, 5000, getSize());
		GPS = new GPSDevice("Maps/" +data.getFileName());
		tracker = new RouteTracker();
		GPS.addGPSListener(tracker);
		directions = new JTextArea();
	}
	
	/**
	 * Allows different files to be displayed
	 * @param file The new file to be displayed
	 * @throws Exception
	 */
	public void load(File file) throws Exception
	{
		data = new Data(file);
		route = null;
		driving = false;
		indicator = new JLabel("Driving: false");
		converter = new DataConverter(data, 5000, getSize());
		GPS = new GPSDevice("Maps/" +data.getFileName());
		tracker = new RouteTracker();
		GPS.addGPSListener(tracker);
		repaint();
	}
	
	/**
	 * Adds listeners to this panel that allow the user to zoom and pan
	 */
	private void setUp()
	{
		indicator.setHorizontalAlignment(JLabel.CENTER);
		add(indicator);
		
		addMouseListener(new MouseAdapter()
				{
					public void mousePressed(MouseEvent e)
					{
						pointClicked = e.getPoint();
					}
					
					public void mouseClicked(MouseEvent e)
					{
						Node n = data.nodeClosest(converter.pixToDeg(e.getPoint()));
						if(e.getButton() == 1)
						{
							Start = n;
							route = null;
						}
						else if(e.getButton() == 3)
						{
							End = n;
							route = null;
						}
				
						
	
						repaint();
					}
				});
		addMouseMotionListener(new MouseAdapter()
				{
					public void mouseDragged(MouseEvent e)
					{
						Point2D newPoint = e.getPoint();
						converter.translate(pointClicked.getX()-newPoint.getX(),
								newPoint.getY()-pointClicked.getY());
						pointClicked = newPoint;
						repaint();
					}
				});
		addMouseWheelListener(new MouseAdapter()
				{
					public void mouseWheelMoved(MouseWheelEvent e)
					{
						int amount = e.getWheelRotation();
						converter.incrementScale(50*amount);
						repaint();
					}
					
				});
		addComponentListener(new ComponentListener()
				{
					@Override
					public void componentResized(ComponentEvent e) {
						converter.updatePanelSize(getSize());
						repaint();
					}
					@Override
					public void componentMoved(ComponentEvent e) {						
					}
					@Override
					public void componentShown(ComponentEvent e) {
						converter.updatePanelSize(getSize());
						repaint();
					}
					@Override
					public void componentHidden(ComponentEvent e) {
					}					
				});
	}
	
	/**
	 * Makes a route between two points selected by the user
	 */
	public void makeRoute()
	{
		if(Start != null && End != null && !Start.equals(End))
		{
			route = new Pathfinder(data).getBestPath(Start, End);
			if(route == null)
			{
				directions.setText(null);
				displayMessage("No Route Found.", 0);
				driving = false;
				indicator.setText("Driving: false");
			}
			else
			{
				directions.setText(route.toString());
			}
			
			repaint();
		
		}
		else
		{
			displayMessage("Please select a start and end point", 0);
		}
		
	}
	
	/**
	 * clears the mappanel
	 */
	public void reset()
	{
		Start = null;
		End = null;
		route = null;
		currentLocation = null;
		driving = false;
		directions.setText(null);
		indicator.setText("Driving: false");
		repaint();
	}
	
	/**
	 * Toggles the drive functionality 
	 */
	public void driveToggle()
	{
		driving = !driving;
		indicator.setText("Driving: " + driving);
	}
	
	/**
	 * Accessor for a gui component that displays the directions for a displayed route
	 * @return a JTextArea with the current routes directions printed
	 */
	public JTextArea getTextDirection()
	{
		return directions;
	}
	
	private void displayMessage(String message, int time)
	{
		if(time == 0)
		{
			JOptionPane.showMessageDialog(null, message, "", JOptionPane.PLAIN_MESSAGE);
		}
		else
		{
			JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
		
			JDialog dialog = pane.createDialog(null, null);
			dialog.setModalityType(Dialog.ModalityType.MODELESS);
			
			Timer timer = new Timer(time, new AbstractAction()
					{
						public void actionPerformed(ActionEvent e)
						{
							dialog.dispose();
						}
					});
			timer.setRepeats(false);
			timer.start();
			
			dialog.setVisible(true);
		}
		
		
		message = null;
	}
	
		
	
	/**
	 * Called in order to repaint the panel when a change occurs
	 */
	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		Iterator<Way> ways = data.wayIterator();
		while(ways.hasNext())
		{
			
			Way next = ways.next();
			if((next.getTagValue("power") != null) ||
					(converter.getZoom() < 4000 && next.getType() < 3 && next.getType() != -1))
			{
				continue;
			}
			if(next.getTagValue("boundary") != null)
			{
				g2.setColor(Color.RED);
			}
			else if(next.getTagValue("natural") != null)
			{
				if(next.getTagValue("natural").equals("wood"))
				{
					g2.setColor(Color.GREEN);
				}
				else
				{
					g2.setColor(Color.BLUE);
				}
			}
			else if(next.getType() >= 1)
			{
				g2.setColor(Color.BLACK);
				g2.setStroke(new BasicStroke((converter.getZoom()/6000)));
			}
			else if(data.size() < 25000 && converter.getZoom() > 4000)
			{
				g2.setColor(Color.DARK_GRAY);
			}
			else
			{
				continue;
			}
				
			
			Iterator<Node> nodes = next.nodeIterator();
			Node previous = null;
			while(nodes.hasNext())
			{
				Node newNode = nodes.next();
				
				if(previous != null)
				{
					Point2D Point1 = converter.degToPix(newNode.getPoint());
					Point2D Point2 = converter.degToPix(previous.getPoint());
					g2.drawLine((int)Point1.getX(), (int)Point1.getY(), 
								(int)Point2.getX(), (int)Point2.getY());
				}
				previous = newNode;
			}
			g2.setColor(Color.BLACK);
			g2.setStroke(new BasicStroke());
		}
		if(route != null)
		{
			g2.setColor(Color.MAGENTA);
			g2.setStroke(new BasicStroke(3));
			Iterator<Node> nodes = route.nodeIterator();
			Node previous = null;
			while(nodes.hasNext())
			{
				Node newNode = nodes.next();					
				if(previous != null)
				{
					Point2D Point1 = converter.degToPix(newNode.getPoint());
					Point2D Point2 = converter.degToPix(previous.getPoint());
					g2.drawLine((int)Point1.getX(), (int)Point1.getY(), 
								(int)Point2.getX(), (int)Point2.getY());
				}
				previous = newNode;
			}
			g2.setStroke(new BasicStroke());
		}
		if(Start != null)
		{
			Point2D start = converter.degToPix(Start.getPoint());
			Ellipse2D dot = new Ellipse2D.Double(start.getX()-2.5, start.getY()-2.5, 5, 5);
			g2.setColor(Color.RED);
			g2.fill(dot);
			g2.draw(dot);
			g2.setColor(Color.BLACK);

		}
		if(End != null)
		{
			Point2D end = converter.degToPix(End.getPoint());
			Ellipse2D dot = new Ellipse2D.Double(end.getX()-2.5, end.getY()-2.5, 5, 5);
			g2.setColor(Color.YELLOW);
			g2.fill(dot);
			g2.draw(dot);
			g2.setColor(Color.BLACK);
		}
		if(currentLocation != null)
		{
			Point2D p = converter.degToPix(currentLocation);
			Ellipse2D dot = new Ellipse2D.Double(p.getX()-2.5, p.getY()-2.5, 5, 5);
			g2.setColor(Color.GREEN);
			g2.fill(dot);
			g2.draw(dot);
			g2.setColor(Color.BLACK);
		}
	}
	
	/**
	 * Private class to process gps events
	 * @author williamloughlin
	 *
	 */
	private class RouteTracker implements GPSListener
	{

		private Node nodeClosest;
		
		public RouteTracker()
		{
			nodeClosest = null;
		}
		
		/**
		 * If driving the center will adjust to the current location and check if the user is on route 
		 * if a current route exists
		 */
		@Override
		public void processEvent(GPSEvent e) {
			currentLocation = new Point2D.Double(e.getLongitude(), e.getLatitude());
			
			nodeClosest =  data.nodeClosest(currentLocation);
			
			if(driving)
			{
				converter.setCenter(currentLocation);
				if(route == null)
				{
					Start = nodeClosest;
					if(End == null)
					{
						displayMessage("Select a Destination", 0);
						driving = false;
						indicator.setText("Driving: false");
					}
					else if(!End.equals(Start))
					{
						makeRoute();
						repaint();
					}
					else
					{
						displayMessage("Currently at Destination", 0);
						driving = false;
						indicator.setText("Driving: false");
					}
				}
				else if(!route.onRoute(new Point2D.Double(e.getLongitude(), e.getLatitude())))
				{
					displayMessage("Off Route. Recalculating", 1000);
					Start = nodeClosest;
					makeRoute();
				}
				else if(End.calcDist(currentLocation) < 0.01)
				{
					driveToggle();
					displayMessage("Destination Reached", 5000);
					route = null;
				}
			}
			repaint();
		}
	}
}
