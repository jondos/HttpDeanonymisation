package privacytest;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.applet.Applet;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

/**
 * 
 */

class AnonPropertyTable extends JTable implements MouseMotionListener, MouseListener
{
	/**
	 * 
	 */
	class AnonPropertyCellRenderer extends DefaultTableCellRenderer
	{
		public void setValue(Object value)
		{
			setFont(m_fNormal);
			setForeground(m_cNormal);
			setBackground(Color.white);
			setToolTipText("");
			
			if(value == null)
			{
				setText("");
				return;
			}
		
			if(value instanceof AnonProperty)
			{
				setBackground(((AnonProperty)value).getRatingColor());
				if(((AnonProperty)value).getRating() != AnonProperty.RATING_NONE) 
				setForeground(Color.black);
			}
			else if(value instanceof AnonPropertyAction)
			{
				setForeground(m_cHeader);
				setToolTipText(((AnonPropertyAction) value).getUrl());
			}
			AnonPropertyTable.this.setRowHeight(this.getPreferredSize().height);
			
			this.setVerticalAlignment(SwingConstants.TOP);
			
			setText((value.toString().startsWith("<html>") ? "" : " ") + value.toString());	
		}
	}

	class AnonPropertyTableModel extends AbstractTableModel
	{
		private Vector m_properties = new Vector();
		
		/**
		 * The column names
		 */
		private String m_columnNames[] = new String[] { "Eigenschaft", "Wert", "Aktion" };
		
		/**
		 * The column classes
		 */
		private Class m_columnClasses[] = new Class[] { AnonProperty.class, String.class, Object.class };
		
		public int getRowCount()
		{
			return m_properties.size();
		}
		
		public int getColumnCount()
		{
			return m_columnNames.length;
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return false;
		}
		
		public Class getColumnClass(int columnIndex)
		{
			return m_columnClasses[columnIndex];
		}
	
		public String getColumnName(int columnIndex)
		{
			return m_columnNames[columnIndex];
		}
		
		public synchronized void add(AnonProperty a_prop)
		{
			m_properties.addElement(a_prop);
		}
		
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			try
			{
				AnonProperty prop = (AnonProperty) m_properties.elementAt(rowIndex);
				
				if(columnIndex == 0) return prop;
				if(columnIndex == 1) return prop.getValue();
				if(columnIndex == 2) return prop.getAction();
			}
			catch(Exception ex) { }
			
			return null;
		}
	}

	private AnonPropertyTableModel m_model;
	private AnonPropertyCellRenderer m_cellRenderer;		
	
	private Applet m_applet;
	
	Font m_fNormal;
	private Font m_fLink = new Font("Verdana", Font.BOLD, 20);

	Color m_cNormal = new Color(68, 68, 68);
	Color m_cHeader = new Color(0, 0, 128);
	
	public AnonPropertyTable(Applet a_applet, boolean a_bHide3rdRow, int a_colSizeOne, int a_colSizeTwo, int a_fontSize)
	{
		m_fNormal = new Font("Verdana", 0, a_fontSize);
		m_model = new AnonPropertyTableModel();
		m_cellRenderer = new AnonPropertyCellRenderer();
		m_applet = a_applet;
		
		setModel(m_model);
		setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, new Color(204, 204, 204)));
		//setDefaultRenderer(String.class, m_cellRenderer);
		setDefaultRenderer(Object.class, m_cellRenderer);
		//setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		setRowHeight(18);
		
		setWidth(0, a_colSizeOne);
		setWidth(1, a_colSizeTwo);

		
		if(a_bHide3rdRow) getColumnModel().removeColumn(getColumnModel().getColumn(2));
		
		setGridColor(new Color(204, 204, 204));
		setRowSelectionAllowed(false);
		setColumnSelectionAllowed(false);
		setSelectionModel(new NilSelectionModel());
		
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	/*
	  public Component prepareEditor(TableCellEditor tce, int r, int c){
		     Component comp = super.prepareEditor(tce, r, c);
		     comp.setFont(m_fNormal);
		     return comp;
		  }*/
	
	public void setWidth(int col, int width)
	{
		getColumnModel().getColumn(col).setPreferredWidth(width);
	}
	
	public void add(AnonProperty a_prop)
	{
		m_model.add(a_prop);
	}
	
	public void mouseMoved(MouseEvent e)
	{
		Point p = new Point(e.getX(), e.getY());
	    int row = rowAtPoint(p);			
	    int col = columnAtPoint(p);
	    Object value = getValueAt(row, col);
		
	    if(value instanceof AnonPropertyAction)
	    {
    		if(getCursor() != Cursor.getPredefinedCursor(Cursor.HAND_CURSOR))
    			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    }
	    else
	    {
	    	if(getCursor() != Cursor.getDefaultCursor())
		    	setCursor(Cursor.getDefaultCursor());
	    }
	}
	
	public void mouseDragged(MouseEvent e)
	{
		
	}
	
	
	public void mouseClicked(MouseEvent a_event)
	{
		Point pt = new Point(a_event.getX(), a_event.getY());
		int row = rowAtPoint(pt);
		int col = columnAtPoint(pt);			
		Object value = getValueAt(row, col);
			
		if(value instanceof AnonPropertyAction)
		{
			try
			{
				m_applet.getAppletContext().showDocument(new URL(((AnonPropertyAction) value).getUrl()), "_blank");
			}
			catch(MalformedURLException ex) { }
		}
	}
	
	public void mousePressed(MouseEvent a_event)
	{
		
	}
	
	public void mouseReleased(MouseEvent a_event)
	{
		
	}
	
	public void mouseExited(MouseEvent a_event)
	{
		
	}

	public void mouseEntered(MouseEvent a_event)
	{

	}
}