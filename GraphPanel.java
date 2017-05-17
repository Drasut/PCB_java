package graphed;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;


/**
 * A panel to draw a graph
 */
public class GraphPanel extends JComponent {
	/**
	 * Constructs a graph panel.
	 * @param aToolBar
	 *            the tool bar with the node and edge tools
	 * @param aGraph
	 *            the graph to be displayed and edited
	 */
	public GraphPanel(ToolBar aToolBar, Graph aGraph) {
		toolBar = aToolBar;
		graph = aGraph;
		setBackground(Color.WHITE);
		comp = new ArrayList<componentObserver>();
		ShoppingFrame frame = new ShoppingFrame(new SimpleGraph());
		addObserver(frame);

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				Point2D mousePoint = event.getPoint();
				Node n = graph.findNode(mousePoint);
				Edge e = graph.findEdge(mousePoint);
				Object tool = toolBar.getSelectedTool();
				if (tool == null)
				{
					if (e != null) {
						selected = e;
					} else if (n != null) {
						selected = n;
						dragStartPoint = moveOnGrid(event);
						dragStartBounds = n.getBounds();
					} else {
						selected = null;
					}
				} 
				else if (tool instanceof Node) {
					Node prototype = (Node) tool;
					Node newNode = (Node) prototype.clone();
					newNode.translate(moveOnGrid(event).getX(), moveOnGrid(event).getY());
					if(IsAllowed(newNode, mousePoint))
					{
						boolean added = graph.add(newNode, moveOnGrid(event));
						if (added) {
							selected = newNode;
							dragStartPoint = moveOnGrid(event);
							dragStartBounds = newNode.getBounds();
						} else if (n != null) {
							selected = n;
							dragStartPoint = mousePoint;
							dragStartBounds = n.getBounds();
							
						}
/*FIXA IN*/				getInfo();

					}
				} 
				else if (tool instanceof Edge) {
					if (n != null)
						rubberBandStart = mousePoint;
				}
				lastMousePoint = mousePoint;
				repaint();
				
			}

			public void mouseReleased(MouseEvent event) {
				Object tool = toolBar.getSelectedTool();
				Point2D mousePoint = event.getPoint();
				if (rubberBandStart != null) {
					boolean accepted=true;						// so you can't put a line over a another line	
					Edge prototype = (Edge) tool;
					Edge newEdge = (Edge) prototype.clone();
					if (IsAllowed(null, mousePoint) && graph.connect(newEdge, rubberBandStart, mousePoint))
						selected = newEdge;
				}
				if (selected instanceof Node) {
					Node n = (Node) selected;
					if(!IsAllowed(n, mousePoint))										
						n.translate(dragStartPoint.getX()-moveOnGrid(event).getX()-(dragStartPoint.getX()-moveOnGrid(event).getX())%20, 
									dragStartPoint.getY()-moveOnGrid(event).getY()-(dragStartPoint.getY()-moveOnGrid(event).getY())%20);
				}
				validate();
				repaint();

				lastMousePoint = null;
				dragStartBounds = null;
				rubberBandStart = null;
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(MouseEvent event) {
				Point2D mousePoint = moveOnGrid(event);
				if (dragStartBounds != null) {
					if (selected instanceof Node) {
						Node n = (Node) selected;
						Rectangle2D bounds = n.getBounds();
						n.translate(dragStartBounds.getX() - bounds.getX() + mousePoint.getX() - dragStartPoint.getX(),
								dragStartBounds.getY() - bounds.getY() + mousePoint.getY() - dragStartPoint.getY());
					}
				}
				lastMousePoint = mousePoint;
				repaint();
			}
		});
	}

	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		Rectangle2D bounds = getBounds();
		Rectangle2D graphBounds = graph.getBounds(g2);
		drawGrid(g2);
		graph.draw(g2);

		if (selected instanceof Node) {
			Rectangle2D grabberBounds = ((Node) selected).getBounds();
			drawGrabber(g2, grabberBounds.getMinX(), grabberBounds.getMinY());
			drawGrabber(g2, grabberBounds.getMinX(), grabberBounds.getMaxY());
			drawGrabber(g2, grabberBounds.getMaxX(), grabberBounds.getMinY());
			drawGrabber(g2, grabberBounds.getMaxX(), grabberBounds.getMaxY());
		}

		if (selected instanceof Edge) {
			Line2D line = ((Edge) selected).getConnectionPoints();
			drawGrabber(g2, line.getX1(), line.getY1());
			drawGrabber(g2, line.getX2(), line.getY2());
		}

		if (rubberBandStart != null) {
			Color oldColor = g2.getColor();
			g2.setColor(PURPLE);
			g2.draw(new Line2D.Double(rubberBandStart, lastMousePoint));
			g2.setColor(oldColor);
		}
	}
	/** own
	 * Draw the grid in the background
	 */
	private void drawGrid(Graphics2D g) {
		g.setColor(Color.LIGHT_GRAY);
		for (int i = 0; i < this.getWidth(); i+=20)
			for (int k = 0; k < this.getHeight(); k+=20)
				g.drawRect(i, k, 2, 2);
	}
	
	/** own
	 * Moves the component to a corner in the grid
	 * @return point p
	 */
	private Point moveOnGrid(MouseEvent event){
		Point2D mousePoint = event.getPoint();
		Point p=new Point();
		double x = mousePoint.getX();
		double y = mousePoint.getY();
		p.setLocation(x-x%20, y-y%20);
		return p;
	}

	/**
	 * Removes the selected node or edge.
	 */
	public void removeSelected() {
		if (selected instanceof Node) {
			graph.removeNode((Node) selected);
		} else if (selected instanceof Edge) {
			graph.removeEdge((Edge) selected);
		}
		selected = null;
		repaint();
	}


	/**
	 * Draws a single "grabber", a filled square
	 * @param g2
	 *            the graphics context
	 * @param x
	 *            the x coordinate of the center of the grabber
	 * @param y
	 *            the y coordinate of the center of the grabber
	 */
	public static void drawGrabber(Graphics2D g2, double x, double y) {
		final int SIZE = 5;
		Color oldColor = g2.getColor();
		g2.setColor(PURPLE);
		g2.fill(new Rectangle2D.Double(x - SIZE/2, y - SIZE/2, SIZE, SIZE));
		g2.setColor(oldColor);
	}

	public Dimension getPreferredSize() {
		Rectangle2D bounds = graph.getBounds((Graphics2D) getGraphics());
		return new Dimension((int) bounds.getMaxX(), (int) bounds.getMaxY());
	}
	
	
	
/*FIXA IN*/	
	/** own
	 * add observers to the list
	 * @param s
	 */
	public void addObserver(componentObserver s) {
		comp.add(s);
	}
	
	/** own
	 * gets the information about a component, name and price
	 * @param p
	 * @param s
	 */
	public void getInfo(){
		for(componentObserver c :comp)
			c.utdateComponent(graph.getNodes());
	}
	
	private boolean IsAllowed(Object o,Point2D mousePoint)
	{
		if (o instanceof Node)
		{
			Node newNode = (Node)o;
			for(Node nod:graph.getNodes())
			{
				if(nod!=newNode && nod.getBounds().intersects(newNode.getBounds()))
					return false;
				for(Edge other: graph.getEdges())
					if(nod.getBounds().intersectsLine(other.getConnectionPoints())&& !(nod==other.getEnd() || nod==other.getStart()))
						return false;
			}
			for(Edge edg: graph.getEdges())
			{
				if(newNode.getBounds().intersectsLine(edg.getConnectionPoints()) && !(newNode==edg.getEnd() || newNode==edg.getStart()))   // fel här
					return false;
				for(Edge other: graph.getEdges())
					if(edg!=other && edg.getConnectionPoints().intersectsLine(other.getConnectionPoints()))
						return false;
			}
			
		}
		if(rubberBandStart!=null)
		{
			Line2D line = new Line2D.Double(rubberBandStart,mousePoint);
			for(Edge edg: graph.getEdges())
				if(line.intersectsLine(edg.getConnectionPoints()))
					return false;
			for(Node nod:graph.getNodes())
				if(!nod.getBounds().intersects(line.getX1(), line.getY1(), 40, 40)&& !nod.getBounds().intersects(line.getX2(), line.getY2(), 40, 40) && nod.getBounds().intersectsLine(line))
					return false;
		}
		return true;
	}

	private Graph graph;
	private ToolBar toolBar;
	private Point2D lastMousePoint;
	private Point2D rubberBandStart;
	private Point2D dragStartPoint;
	private Rectangle2D dragStartBounds;
	private Object selected;
	private static final Color PURPLE = new Color(0.7f, 0.4f, 0.7f);
	private ArrayList<componentObserver> comp;
}
