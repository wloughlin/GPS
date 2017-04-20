package mapdisplay;

import javax.swing.*;


import mapdata.Data;

import java.awt.*;
import java.awt.event.*;
import java.io.File;


public class ApplicationFrame extends JFrame {
	
	private MapPanel mapPanel;
	private JTextArea directions;
	
	
	
	public ApplicationFrame() throws Exception
	{
		mapPanel = new MapPanel(new Data(new File("Maps/usb.osm")));
		directions = mapPanel.getTextDirection();
		directions.setEditable(false);
		setUp();
		pack();
		setVisible(true);
	}
	
	/**
	 * Sets up the frame to display the gps 
	 */
	private void setUp()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		setPreferredSize(new Dimension(600, 600));
		add(mapPanel, BorderLayout.CENTER);
		JScrollPane sp = new JScrollPane(directions);
		sp.setPreferredSize(new Dimension(0, 100));
		add(sp, BorderLayout.SOUTH);
		
		
		
		JPanel options = new JPanel();
		options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
		options.setOpaque(false);
		
		JButton getDirection = new JButton("Directions");
		getDirection.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						mapPanel.makeRoute();	
					}
				});
		options.add(getDirection);
		
		JButton drive = new JButton("Toggle Drive");
		drive.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						
						mapPanel.driveToggle();
						
					}
				});
		options.add(drive);
		
		JButton load = new JButton("Load File");
		load.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e) 
					{
						JFileChooser fc = new JFileChooser(new File("Maps"));
						int ret = fc.showOpenDialog(null);
						if(ret == JFileChooser.APPROVE_OPTION)
						{
							File file = fc.getSelectedFile();
							try
							{
								
								mapPanel.load(file);
								
							} catch (Exception ex)
							{
								JOptionPane.showMessageDialog(null, "Invalid File", "", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				});
		options.add(load);
		
		JButton reset = new JButton("Reset");
		reset.addActionListener(new ActionListener()
				{
					
					public void actionPerformed(ActionEvent e)
					{
						mapPanel.reset();
					}
				});
		options.add(reset);
		
		add(options, BorderLayout.WEST);
		
	}
	
	/**
	 * Runs the application
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		ApplicationFrame frame = new ApplicationFrame();
	}
	

}
