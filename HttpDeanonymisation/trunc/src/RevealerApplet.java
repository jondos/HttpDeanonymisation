import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.MalformedURLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import java.net.InetAddress;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.BorderFactory;
import javax.swing.border.MatteBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.JTable;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Vector;

public class RevealerApplet extends JApplet implements ActionListener, MouseListener
{
	public String m_strExternalIPs;
	public String m_strInternalIPs;

	private String m_targetURL = "/de/files/anontest/onlyip.php";
	private String m_lookupURL = "/geoip/lookup.php";
	private int m_width = 600;
	private int m_height = 300;

	private Vector m_vecExternalIPs;
	private Vector m_vecInternalIPs;
	private Vector m_vecInterfaces;

	private Font m_fNormal = new Font("Verdana", 0, 13);
	private Font m_fHeader = new Font("Verdana", Font.BOLD, 14);

	private Color m_cNormal = new Color(60, 60, 60);
	private Color m_cHeader = new Color(0, 0, 160);
	private JPanel m_networkPanel;
	private JPanel m_detailsPanel;
	private JButton m_btnSwitch;
	private JLabel m_lblJavaIsDangerous;
	private JLabel m_lblLocation;
	private JLabel m_lblProvider;
	
	private MatteBorder m_border;
	private MatteBorder m_2ndColumnBorder;
	
	private String m_discoveredIP;

	private final static String TEXT_SWITCH_TO_DETAIL_PANEL = "Details anzeigen";
	private final static String TEXT_SWITCH_TO_NETWORK_PANEL = "Details ausblenden";
	
	private String m_javaIsDangerousUrl = "https://www.jondos.de/javaisdangerous";
	
	public void init()
	{
		super.init();

		if(this.getParameter("WIDTH") != null)
		{
			try
			{
				m_width = Integer.parseInt(this.getParameter("WIDTH"));
			}
			catch(NumberFormatException e)
			{

			}
		}

		if(this.getParameter("HEIGHT") != null)
		{
			try
			{
				m_height = Integer.parseInt(this.getParameter("HEIGHT"));
			}
			catch(NumberFormatException e)
			{

			}
		}
		
		if(this.getParameter("JAVA_IS_DANGEROUS_URL") != null)
		{
			String url = this.getParameter("JAVA_IS_DANGEROUS_URL");
			if(url.startsWith("https://www.anonymix.eu") || url.startsWith("https://www.jondos.de"))
				m_javaIsDangerousUrl = url;
		}
		
		m_discoveredIP = this.getParameter("DISCOVERED_IP");

		this.setSize(m_width, m_height);

		m_vecExternalIPs = new Vector();
		m_vecInternalIPs = new Vector();
		m_vecInterfaces = new Vector();

		getIPs();

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
			System.err.println("Error setting native Look and Feel: " + e.getMessage());
		}

		GridBagConstraints c = new GridBagConstraints();

		createRootPanel();
		

	}

	public void start()
	{
		super.start();
	}

	public void stop()
	{
		super.stop();
	}

	public void destroy()
	{
		super.destroy();
	}

	public void actionPerformed(ActionEvent a_event)
	{
		if(a_event.getSource() == m_btnSwitch)
		{
			if(m_networkPanel.isVisible())
			{
				m_networkPanel.setVisible(false);
				m_detailsPanel.setVisible(true);
				m_btnSwitch.setText(TEXT_SWITCH_TO_NETWORK_PANEL);
			}
			else
			{
				m_detailsPanel.setVisible(false);
				m_networkPanel.setVisible(true);
				m_btnSwitch.setText(TEXT_SWITCH_TO_DETAIL_PANEL);
			}
		}
	}
	
	public void mouseClicked(MouseEvent a_event)
	{
		if(a_event.getSource() == m_lblJavaIsDangerous || a_event.getSource() == m_lblLocation ||
		   a_event.getSource() == m_lblProvider)
		{
			try
			{
				this.getAppletContext().showDocument(new URL(((JLabel) a_event.getSource()).getToolTipText()), "_blank");
			}
			catch(MalformedURLException ex) { }
		}
		
		if(a_event.getSource() instanceof javax.swing.JTable)
		{
			javax.swing.JTable tbl = (javax.swing.JTable) a_event.getSource();
			Point pt = new Point(a_event.getX(), a_event.getY());
			int col = tbl.columnAtPoint(pt);
			int row = tbl.rowAtPoint(pt);
			String value = tbl.getValueAt(row, col).toString();
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
		if(a_event.getSource() == m_lblJavaIsDangerous || a_event.getSource() == m_lblLocation ||
		  a_event.getSource() == m_lblProvider)
		{
			((JLabel)a_event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	}	
	
	public void getIPs()
	{
		getIPsFromAnontest(true);
		getIPsFromAnontest(false);
		getIPsFromNetworkInterfaces();
	}

	public void getIPsFromAnontest(boolean a_bUseSSL)
	{
		try
		{
			String host = this.getDocumentBase().getHost();
			String sourceIP;
			String destIP;
			String line;
			Socket sock;

			if(host.indexOf("jondos.de") < 0 && host.indexOf("anonymix.eu") < 0)
			{
				// we're running within an applet viewer, use the test site
				host = "www.anonymix.eu";
			}

			if(a_bUseSSL)
			{
				SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				sock = factory.createSocket(host, 443);
			}
			else
			{
				sock = new Socket(host, 80);
			}

			sourceIP = sock.getLocalAddress().getHostAddress();

			if(!m_vecInternalIPs.contains(sourceIP))
			{
				m_vecInternalIPs.addElement(sourceIP);
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));

			writer.write("GET " + (a_bUseSSL ? "https" : "http") + "://" + host + m_targetURL + "\n\n\n");
			writer.flush();

			destIP = new String();
			while((line = reader.readLine()) != null)
			{
				destIP += line;
			}

			if(!m_vecExternalIPs.contains(destIP))
			{
				m_vecExternalIPs.addElement(destIP);
			}
		}
		catch(Exception e)
		{
			System.out.println("Getting IP addresses from anontest URL failed: " + e.getMessage());
		}
	}
	
	public String[] getGeoIP(String ip)
	{
		String host = this.getDocumentBase().getHost();
		String[] geoip = new String[5];
		
		if(host.indexOf("jondos.de") < 0 && host.indexOf("anonymix.eu") < 0)
		{
			// we're running within an applet viewer, use the test site
			host = "www.anonymix.eu";
		}
		
		try
		{
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket sock = (SSLSocket) factory.createSocket(host, 443);
		
			BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));

			writer.write("GET https://" + host + m_lookupURL + "?ip=" + ip +  "\n\n\n");
			writer.flush();

			for(int i = 0; i < geoip.length; i++)
			{
				geoip[i] = reader.readLine();
			}
		}
		catch(Exception ex) {}
		
		return geoip;
	}

	public void getIPsFromNetworkInterfaces()
	{
		try
		{
			Enumeration nets = NetworkInterface.getNetworkInterfaces();
			while(nets.hasMoreElements())
			{
				NetworkInterface iface = (NetworkInterface) nets.nextElement();
				Enumeration addresses = iface.getInetAddresses();

				String strAddr = "";

				while(addresses.hasMoreElements())
				{
					InetAddress addr = (InetAddress) addresses.nextElement();

					String ip = addr.getHostAddress();
					
					// skip ipv6 addresses
					if(ip.length() > 16) continue;

					if(addr.isSiteLocalAddress())
					{
						if(!m_vecInternalIPs.contains(ip))
						{
							m_vecInternalIPs.addElement(ip);
						}
					}
					else if(ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1"))
					{
						// do nothing
					}
					else
					{
						if(!m_vecExternalIPs.contains(ip))
						{
							m_vecExternalIPs.addElement(ip);
						}
					}

					strAddr += ip + " ";
				}
				
				m_vecInterfaces.addElement(new Object[] { iface.getDisplayName(), strAddr } );
			}
		}
		catch(Exception e)
		{
			System.out.println("Listing network interfaces failed: " + e.getMessage());
		}
	}

	private void addSystemPropertyLabel(JPanel a_panel, String a_strProperty, GridBagConstraints a_constrains)
	{
		try
		{
			a_constrains.gridy++;
			a_panel.add(createBox(a_strProperty + " = " + System.getProperty(a_strProperty)), a_constrains);
		}
		catch(SecurityException ex)
		{
			System.out.println("Could not read " + a_strProperty);
		}
	}

	private JLabel createHeaderLabel(String a_strLabel)
	{
		JLabel lbl = new JLabel(a_strLabel);
		lbl.setFont(m_fHeader);
		lbl.setForeground(m_cHeader);
		return lbl;
	}

	private JLabel createBox(String a_strLabel)
	{
		return createBox(a_strLabel, false);
	}
	
	private JLabel createBox(String a_strLabel, boolean a_is2ndColumn)
	{
		JLabel lbl = new JLabel(a_strLabel);
		lbl.setFont(m_fNormal);
		lbl.setForeground(m_cNormal);
		lbl.setBorder(a_is2ndColumn ? m_2ndColumnBorder : m_border);
		return lbl;
	}
	
	private JLabel createBox(String a_strLabel, Color a_bkColor)
	{
		JLabel lbl = createBox(a_strLabel);
		if(a_bkColor != null)
		{
			lbl.setOpaque(true);
			lbl.setBackground(a_bkColor);
		}	
		return lbl;
	}
	
	private JLabel createBox(String a_strLabel, String a_url, boolean a_is2ndColumn)
	{
		JLabel lbl = createBox(a_strLabel, a_is2ndColumn);
		lbl.addMouseListener(this);
		lbl.setForeground(Color.blue);
		lbl.setToolTipText(a_url);
		
		return lbl;
	}

	private void createRootPanel() 
	{
		JPanel rootPanel = new JPanel(new GridBagLayout());
		rootPanel.setBackground(Color.WHITE);
		JScrollPane scroll = new JScrollPane(rootPanel);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(null);
		getContentPane().add(scroll);
		
		GridBagConstraints cRoot = new GridBagConstraints();
		cRoot.gridy = 0;
		cRoot.gridx = 0;
		cRoot.gridwidth = 2;
		cRoot.weightx = 0.0;
		cRoot.fill = GridBagConstraints.NONE;
		cRoot.ipadx = cRoot.ipady = 0;
		cRoot.insets = new Insets(5, 5, 5, 5);
		
		m_btnSwitch = new JButton(TEXT_SWITCH_TO_DETAIL_PANEL);
		m_btnSwitch.addActionListener(this);
		rootPanel.add(m_btnSwitch, cRoot);
		
		AnonPropertyTable table = new AnonPropertyTable(this);
		table.add(new AnonProperty("Java aktiviert!", "Java beeinträchtigt Ihre Anonymität!", "Mehr Infos", "www.bistdudoof.de", AnonProperty.RATING_BAD));
		
		cRoot.gridy++;
		cRoot.fill = GridBagConstraints.HORIZONTAL;
		cRoot.weightx = 1.0;
		rootPanel.add(table, cRoot);
		
		createNetworkPanel();
		createDetailsPanel();
		
		cRoot.gridx = 0;
		cRoot.gridy++;
		cRoot.gridwidth = 2;
		rootPanel.add(m_networkPanel, cRoot);
		
		cRoot.gridy++;
		rootPanel.add(m_detailsPanel, cRoot);
	}

	private void createDetailsPanel() 
	{
		GridBagConstraints c;
		m_detailsPanel = new JPanel(new GridBagLayout());
		m_detailsPanel.setBackground(Color.WHITE);
		m_detailsPanel.setVisible(false);
	
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridy = 0;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.insets = new Insets(0, 0, 7, 0);
		
	
		c.insets = new Insets(0, 0, 7, 0);
		m_detailsPanel.add(createHeaderLabel("Netzwerkschnittstellen:"), c);
		c.insets = new Insets(0, 0, 5, 0);
		for(int i = 0; i < m_vecInterfaces.size(); i++)
		{
			String name = ((Object[]) m_vecInterfaces.elementAt(i))[0].toString();
			String addr = ((Object[]) m_vecInterfaces.elementAt(i))[1].toString();
			c.gridy++;
			m_detailsPanel.add(createBox((i + 1) + ". " + name + " - " + addr), c);
		}
		
		c.gridy++;		
		m_detailsPanel.add(createHeaderLabel("Betriebsystem:"), c);
	
		c.gridy++;
	
		m_detailsPanel.add(createBox(System.getProperty("os.name") + " " + System.getProperty("os.arch") + " Version " + System.getProperty("os.version") + " "), c);
	
		c.gridy++;
		c.insets = new Insets(0, 0, 7, 0);
		m_detailsPanel.add(createHeaderLabel("Java VM:"), c);
		c.gridy++;
		m_detailsPanel.add(createBox(System.getProperty("java.vendor") + " " + System.getProperty("java.version")), c);
		
		c.gridy++;
		m_detailsPanel.add(createHeaderLabel("Sonstige Eigenschaften:"), c);
	
		addSystemPropertyLabel(m_detailsPanel, "browser", c);
		addSystemPropertyLabel(m_detailsPanel, "browser.vendor", c);
		addSystemPropertyLabel(m_detailsPanel, "browser.version", c);
		addSystemPropertyLabel(m_detailsPanel, "java.home", c);
		addSystemPropertyLabel(m_detailsPanel, "user.name", c);
		addSystemPropertyLabel(m_detailsPanel, "user.home", c);
		addSystemPropertyLabel(m_detailsPanel, "user.dir", c);
	}

	private void createNetworkPanel() 
	{
		GridBagConstraints c;
		m_networkPanel = new JPanel(new GridBagLayout());
		m_networkPanel.setBackground(Color.WHITE);		
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.insets = new Insets(0, 0, 5, 0);
		
		for(int i = 0; i < m_vecExternalIPs.size(); i++)
		{
			String ip = m_vecExternalIPs.elementAt(i).toString();
			
			if(m_discoveredIP != null && m_discoveredIP.equals(ip)) continue;
			
			AnonPropertyTable table = new AnonPropertyTable(this);
			
			table.add(new AnonProperty("IP-Addresse", ip, AnonProperty.RATING_BAD));
			
			String[] geoIP = getGeoIP(ip);
			
			if(geoIP != null) 
			{
				table.add(new AnonProperty("Standort", geoIP[0], "Karte", "http://www.mapquest.com/maps/map.adp?latlongtype=decimal&latitude=" + geoIP[3] + "&longitude=" + geoIP[4], AnonProperty.RATING_NONE));
				table.add(new AnonProperty("Netzzugang", geoIP[1] + ", " + geoIP[2], "Whois IP", "https://www.jondos.de/whois", AnonProperty.RATING_NONE));
			}
			
			try
			{
				
			
			InetAddress addr = InetAddress.getByName(ip);
			table.add(new AnonProperty("Reverse DNS:", addr.getHostName(), AnonProperty.RATING_NONE));
			}
			catch(Exception ex) { ex.printStackTrace(); }
			m_strExternalIPs += ip;
			m_networkPanel.add(table, c);
			c.gridy++;
		}
	
		c.insets = new Insets(0, 0, 5, 0);
		AnonPropertyTable table = new AnonPropertyTable(this);
		for(int i = 0; i < m_vecInternalIPs.size(); i++)
		{
			table.add(new AnonProperty("Interne IP", m_vecInternalIPs.elementAt(i).toString(), AnonProperty.RATING_OKISH));

			m_strInternalIPs += (m_vecInternalIPs.elementAt(i));
		}
		c.gridy++;		
		m_networkPanel.add(table, c);		
	}
	
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
				if(((AnonProperty)value).m_rating != AnonProperty.RATING_NONE) 
				setForeground(Color.black);
			}
			else if(value instanceof AnonPropertyAction)
			{
				setForeground(m_cHeader);
				setToolTipText(((AnonPropertyAction) value).m_strUrl);
			}
			
			setText(" " + value.toString());			
		}
	}
	
	class AnonPropertyTable extends JTable
	{
		private AnonPropertyTableModel m_model;
		private AnonPropertyCellRenderer m_cellRenderer;
		
		public AnonPropertyTable(RevealerApplet a_parent)
		{
			m_model = new AnonPropertyTableModel();
			m_cellRenderer = new AnonPropertyCellRenderer();
			setModel(m_model);
			setBorder(BorderFactory.createMatteBorder(1, 1, 0, 1, new Color(196, 196, 196)));
			setDefaultRenderer(String.class, m_cellRenderer);
			setDefaultRenderer(AnonProperty.class, m_cellRenderer);
			setDefaultRenderer(AnonPropertyAction.class, m_cellRenderer);
			setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			setRowHeight(23);
			
			this.getColumnModel().getColumn(0).setPreferredWidth(120);
			this.getColumnModel().getColumn(1).setPreferredWidth(350);
			this.getColumnModel().getColumn(2).setPreferredWidth(100);
			setGridColor(new Color(196, 196, 196));
			
			setEnabled(false);
			
			addMouseListener(a_parent);			
		}

		public void add(AnonProperty a_prop)
		{
			m_model.add(a_prop);
		}
	}
	
	class AnonPropertyTableModel extends AbstractTableModel
	{
		private Vector m_properties = new Vector();
		
		/**
		 * The column names
		 */
		protected String columnNames[] = new String[] { "Eigenschaft", "Wert", "Aktion" };
		
		/**
		 * The column classes
		 */
		protected Class columnClasses[] = new Class[] { AnonProperty.class, String.class, AnonPropertyAction.class };
		
		/**
		 * The last column will either display an action or the rating of the property
		 */
		private boolean m_bDisplayAction = false;
		
		public int getRowCount()
		{
			return m_properties.size();
		}
		
		public int getColumnCount()
		{
			return columnNames.length;
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return false;
		}
		
		public Class getColumnClass(int columnIndex)
		{
			return columnClasses[columnIndex];
		}

		public String getColumnName(int columnIndex)
		{
			return columnNames[columnIndex];
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
				if(columnIndex == 0)
				{
					return prop;
				}
				
				if(columnIndex == 1)
				{
					return prop.m_strValue;
				}
				
				if(columnIndex == 2)
				{
					return prop.m_action;
				}
				
			}
			catch(Exception ex) { }
			
			return null;
		}
	}

	class AnonProperty
	{
		private String m_strName;
		private String m_strValue;
		private AnonPropertyAction m_action;
		private int m_rating;
		
		public final static int RATING_NONE = 0;
		public final static int RATING_GOOD = 1;
		public final static int RATING_BAD = 2;
		public final static int RATING_OKISH = 3;
		
		public AnonProperty(String a_strName, String a_strValue, int a_rating)
		{
			m_strName = a_strName;
			m_strValue = a_strValue;
			m_rating = a_rating;
		}
		
		public AnonProperty(String a_strName, String a_strValue, String a_strAction, String a_strActionUrl, int a_rating)
		{
			m_strName = a_strName;
			m_strValue = a_strValue;
			m_action = new AnonPropertyAction(a_strAction, a_strActionUrl);
			m_rating = a_rating;
		}
		
		public Color getRatingColor()
		{
			switch(m_rating)
			{
			default:
			case RATING_NONE:
				return Color.white;
			case RATING_BAD:
				return Color.red;
			case RATING_OKISH:
				return Color.orange;
			case RATING_GOOD:
				return Color.green;
			}
		}
		
		public String toString()
		{
			return m_strName;
		}
	}
	
	class AnonPropertyAction
	{
		private String m_strName;
		private String m_strUrl;
		
		public AnonPropertyAction(String a_strName, String a_strUrl)
		{
			m_strName = a_strName;
			m_strUrl = a_strUrl;
		}
		
		public String toString()
		{
			return m_strName;
		}
	}
}
