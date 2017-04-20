package mapdata;
import java.awt.geom.Point2D

;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


/**
 * Master class to contain all the data from an OSM file
 * @author williamloughlin
 *
 */
public class Data 
{
	private File file;
	
	private double maxLat;
	private double minLat;
	private double minLon;
	private double maxLon;

	private HashMap<String, Node> Nodes;
	private HashSet<Node> Reachable;
	
	
	private HashSet<Way> Ways;
	private HashSet<Way> Driveable;
	
	// Decreases runtime of pathfind by about .1 seconds (a half to a third of the total time)
	private HashMap<Node, ArrayList<Way>> nodeToWayMap;
	

	public Data(File file) throws Exception
	{
		this.file = file;
		Nodes = new HashMap<String, Node>();
		Reachable = new HashSet<Node>();
		Ways = new HashSet<Way>();
		Driveable = new HashSet<Way>();
		nodeToWayMap = new HashMap<Node, ArrayList<Way>>();
		parse();
		nodeToWaySetup();
		
	}
	

	/**
	 * Gets the name of the file being used by this data object
	 * @return the name of the osm file being used by this object
	 */
	public String getFileName()
	{
		return file.getName();
	}
	
	/**
	 * Accessor for the size of this data object in nodes
	 * @return The number of nodes in this data object
	 */
	public int size()
	{
		return Nodes.size();
	}
	
	/**
	 * Getter for nodes
	 * @param id the id of the desired node
	 * @return the node with the desired id
	 */
	public Node getNode(String id)
	{
		return Nodes.get(id);
	}
	
	/**
	 * Getter for the minimum latitude of this map
	 * @return The minimum latitude of this data set
	 */
	public double minLat()
	{
		return minLat;
	}
	
	/**
	 * Getter for the maximum latitude of this map
	 * @return The maximum latitude of this data set
	 */
	public double maxLat()
	{
		return maxLat;
	}
	
	/**
	 * Getter for the minimum longitude of this map
	 * @return The minimum longitude of this data set
	 */
	public double minLon()
	{
		return minLon;
	}
	
	/**
	 * Getter for the maximum longitude of this map
	 * @return The maximum longitude of this data set
	 */
	public double maxLon()
	{
		return maxLon;
	}
	
	/**
	 * Accessor for an iterator of the nodes contained by the data
	 * @return An iterator that iterates over the nodes in the hashmap
	 */
	public Iterator<Node> nodeIterator()
	{
		return Nodes.values().iterator();
	}
	
	/**
	 * Accessor for an iterator of the ways contained by the data
	 * @return An iterator that iterates over the ways in the hashmap
	 */
	public Iterator<Way> wayIterator()
	{
		return Ways.iterator();
	}
	
	/**
	 * Method to get the closest reachable node to a point
	 * @param Lat the lat of the point
	 * @param Lon the lon of the point
	 * @return The node closest to the given point
	 */
	public Node nodeClosest(Point2D point)
	{
		Iterator<Node> it = Reachable.iterator();
		double dist = Double.POSITIVE_INFINITY;
		Node close = null;
		while(it.hasNext())
		{
			Node n = it.next();
			double thisDist = n.calcDist(point);
			if(thisDist < dist)
			{
				close = n;
				dist = thisDist;
			}
		}
		return close;
		
	}
	
	
	/**
	 * Method to find the ways that a a reachable node belongs to
	 * @return An arraylist of driveable ways this node belongs to. 
	 * @param n The node being examined
	 * @precondition n belongs to at least 1 driveable way
	 */
	public ArrayList<Way> nodeToDriveable(Node n)
	{
		return nodeToWayMap.get(n);
	}
	
	/**
	 * Adds all driveable nodes to their parent ways on initialization
	 */
	private void nodeToWaySetup()
	{
		
		Iterator<Way> wayIt = Driveable.iterator();
		while(wayIt.hasNext())
		{
			Way way = wayIt.next();
			
			Iterator<Node> nodeIt = way.nodeIterator();
			while(nodeIt.hasNext())
			{
				Node node = nodeIt.next();
				ArrayList<Way> parents = nodeToWayMap.get(node);
				if(parents == null)
				{
					parents = new ArrayList<Way>();
					nodeToWayMap.put(node, parents);
				}
				parents.add(way);
			}
		}
	}
	
	
	/**
	 * Method to get the distance between two nodes along a way
	 * @param start The beginning node
	 * @param end The end Node
	 * @return The distance along a driveable way between them
	 * @precondition Start and end share a driveable way
	 */
	public double getWayLength(Node start, Node end)
	{
		return getSharedWay(start, end).segmentDist(start, end);
	}
	
	
	/**
	 * Method that gets the shortest shared way between two points
	 * @param start The starting node
	 * @param end The ending node
	 * @return A way shared by the two points that is the shortest among the shared ways
	 * null if no ways are shared
	 */
	public Way getSharedWay(Node start, Node end)
	{
		Way shortWay = null;
		ArrayList<Way> startWays = nodeToDriveable(start);
		ArrayList<Way> endWays = nodeToDriveable(end);
		Iterator<Way> it = startWays.iterator();
		double dist = Double.POSITIVE_INFINITY;
		while(it.hasNext())
		{
			Way way = it.next();
			if(endWays.contains(way) && Driveable.contains(way))
			{
				double currentDist = way.segmentDist(start, end);
				if(currentDist < dist)
				{
					dist = currentDist;
					shortWay = way;
				}
			}
		}
		//System.out.println(shortWay.getName());
		return shortWay;
	}
	
	/**
	 * Method to get an iterator for the reachable nodes in this data
	 * @return an iterator for the reachable nodes in this data
	 */
	public Iterator<Node> reachableIterator()
	{
		return Reachable.iterator();
	}
	
	/**
	 * Method to return the adjacent nodes to a given node
	 * @param n The node being examined
	 * @return An arraylist of nodes adjacent to the requested node
	 */
	public ArrayList<Node> getAdjacentNodes(Node n)
	{
		ArrayList<Node> adj = new ArrayList<Node>();
		ArrayList<Way> ways = nodeToDriveable(n);
		for(Way w : ways)
		{
			adj.addAll(w.getAdjacentNodes(n));
		}
		return adj;
	}
	
	/**
     * Parse the OSM file underlying this OSMParser.
     */
    private void parse()
        throws IOException, ParserConfigurationException, SAXException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        SAXParser saxParser = spf.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        OSMHandler handler = new OSMHandler();
        xmlReader.setContentHandler(handler);
        InputStream stream = null;
        try {
            stream = new FileInputStream(file);
            InputSource source = new InputSource(stream);
            xmlReader.parse(source);
        } catch(IOException x) {
            throw x;
        } finally {
            if(stream != null)
                stream.close();
        }
    }
    


    /**
     * Handler class used by the SAX XML parser.
     * The methods of this class are called back by the parser when
     * XML elements are encountered.
     */
    class OSMHandler extends DefaultHandler {

    	private ArrayList<Node> wayConstruct = new ArrayList<Node>();
    	private String wayId;
    	private HashMap<String, String> tags;
    	
        /** Current character data. */
        private String cdata;

        /** Attributes of the current element. */
        private Attributes attributes;

        /**
         * Get the most recently encountered CDATA.
         */
        public String getCdata() {
            return cdata;
        }

        /**
         * Get the attributes of the most recently encountered XML element.
         */
        public Attributes getAttributes() {
            return attributes;
        }

        /**
         * Method called by SAX parser when start of document is encountered.
         */
        public void startDocument() {
           
        }

        /**
         * Method called by SAX parser when end of document is encountered.
         */
        public void endDocument() {
            
        }

        /**
         * Method called by SAX parser when start tag for XML element is
         * encountered.
         */
        public void startElement(String namespaceURI, String localName,
                                 String qName, Attributes atts) {
            attributes = atts;
            /*
            System.out.println("startElement: " + namespaceURI + ","
                               + localName + "," + qName);
            if(atts.getLength() > 0)
                showAttrs(atts);
            */
            if(qName.equals("node"))
            {
            	String id = atts.getValue("id");
            	String lat = atts.getValue("lat");
            	String lon = atts.getValue("lon");
            	Node node = new Node(Double.parseDouble(lat), Double.parseDouble(lon), id);
            	Nodes.put(id, node);
            }
            if(qName.equals("bounds"))
            {
            	minLat = Double.parseDouble(atts.getValue("minlat"));
            	minLon = Double.parseDouble(atts.getValue("minlon"));
            	maxLat = Double.parseDouble(atts.getValue("maxlat"));
            	maxLon = Double.parseDouble(atts.getValue("maxlon"));
            }
            
            if(qName.equals("way"))
            {
              	tags = new HashMap<String, String>();  
            	wayId = atts.getValue("id");
            }
            if(qName.equals("nd"))
            {
            	wayConstruct.add(Nodes.get(atts.getValue("ref")));
            }
            if(qName.equals("tag"))
            {
	
            	if(tags != null)
            		tags.put(atts.getValue("k"), atts.getValue("v"));
            	
            }
            
            
        }
        
        private void finishCurrentObj()
        {
        	Way way = new Way(wayConstruct, wayId, tags);
        	Ways.add(way);
        	if(way.getType()>=1)
        	{
        		Driveable.add(way);
        		Iterator<Node> it = way.nodeIterator();
        		while(it.hasNext())
        		{
        			Node n = it.next();
        			Reachable.add(n);
        		}
        	}
        	wayConstruct = new ArrayList<Node>();
        	wayId = null;
        }

        /**
         * Method called by SAX parser when end tag for XML element is
         * encountered.  This can occur even if there is no explicit end
         * tag present in the document.
         */
        public void endElement(String namespaceURI, String localName,
                               String qName) throws SAXParseException {
        	/*
            System.out.println("endElement: " + namespaceURI + ","
                               + localName + "," + qName);
                               */
            if(qName.equals("way"))
            {
            	finishCurrentObj();
            }
                               
        }

        /**
         * Method called by SAX parser when character data is encountered.
         */
        public void characters(char[] ch, int start, int length)
            throws SAXParseException {
            // OSM files apparently do not have interesting CDATA.
            //System.out.println("cdata(" + length + "): '"
            //                 + new String(ch, start, length) + "'");
            cdata = (new String(ch, start, length)).trim();
        }
    }

}


	
	

