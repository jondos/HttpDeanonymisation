package privacytest;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import netscape.javascript.JSObject;


import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

//import util.Messages;

public class RevealerApplet extends JApplet implements ActionListener
{
	private static final String MSG_NETWORK_ACCESS = "networkAccess";
	private static final String MSG_SHOW_MAP = "showMap";
	private static final String MSG_LOCATION = "location";
	private static final String MSG_OS = "os";
	private static final String MSG_INTERNAL_IP = "internalIP";
	private static final String MSG_TRUSTED_APPLET_DESC= "trustedAppletDescription";
	private static final String MSG_TRUSTED_APPLET = "trustedApplet";
	private static final String MSG_NETWORK_INTERFACE = "networkInterface";
	private static final String MSG_IP_ADDRESS = "ipAddress";
	private static final String MSG_YOUR_IP = "yourIPAddress";
	private static final String MSG_JAVA_IS_ACTIVATED = "javaIsActivated";
	private static final String MSG_JAVA_ANON_BAD = "javaAnonBad";
	private final static String MSG_DETAILS = "details";
	private final static String MSG_BACK = "back";	
	private static final String MSG_JAVA_VM = "javaVM";
	private static final String MSG_WHOIS_DOMAIN = "whoisDomain";
	private static final String MSG_REVERSE_DNS = "reverseDNS";
	
	final static String CRLF = "\r\n";
	final static String HTTP_HEADER_END = CRLF+CRLF; //end of http message headers
	
	public String m_strInternalIPs;

	private String m_targetURL;
	private String m_lookupURL = "/geoip/lookup.php";
	private int width_column_one = 100;
	private int width_column_two = 400;
	private int width_column_three = 100;
	private int m_height = 300;
	private int m_fontSize = 14;
	
	private Vector m_vecExternalIPs;
	private Vector m_vecInternalIPs;
	private Vector m_vecInterfaces;

	private JPanel m_startPanel;
	private JPanel mtailsPanel;
	private JButton m_btnSwitch;

	JLabel m_lblLocation;
	JLabel m_lblProvider;

	private String m_discoveredIP;
	private boolean m_bUseSSL;
	private String m_serverDomain;
	
	private ResourceBundle myResources;

	public void init()
	{
		super.init();
		
		System.out.println("Startup!");

		if(getParameter("WIDTH_COLUMN_ONE") != null)
		{
			try
			{
				width_column_one = Integer.parseInt(getParameter("WIDTH_COLUMN_ONE"));
			}
			catch(NumberFormatException e)
			{

			}
		}
		
		if(getParameter("WIDTH_COLUMN_TWO") != null)
		{
			try
			{
				width_column_two = Integer.parseInt(getParameter("WIDTH_COLUMN_TWO"));
			}
			catch(NumberFormatException e)
			{

			}
		}
		
		if(getParameter("WIDTH_COLUMN_THREE") != null)
		{
			try
			{
				width_column_three = Integer.parseInt(getParameter("WIDTH_COLUMN_THREE"));
			}
			catch(NumberFormatException e)
			{

			}
		}
		
		if (getParameter("FONT_SIZE") != null)
		{
			try
			{
				m_fontSize = Integer.parseInt(getParameter("FONT_SIZE"));
			}
			catch(NumberFormatException e)
			{

			}
		}
		
		

		if(getParameter("HEIGHT") != null)
		{
			try
			{
				m_height = Integer.parseInt(getParameter("HEIGHT"));
			}
			catch(NumberFormatException e)
			{

			}
		}
		
		System.out.println("Reading locale...");
		
		if(getParameter("LOCALE") != null)
		{
			myResources = ResourceBundle.getBundle("anontest", new Locale(getParameter("LOCALE"), ""));
		}
		else
		{
			myResources = ResourceBundle.getBundle("anontest", Locale.ENGLISH);
		}
		
		m_discoveredIP = getParameter("DISCOVERED_IP");
		m_bUseSSL = getParameter("USE_SSL") != null && getParameter("USE_SSL").equalsIgnoreCase("true");
		
		m_targetURL = getParameter("SERVER_URL_FOR_IP");
		m_serverDomain = getParameter("SERVER_DOMAIN");

		m_vecExternalIPs = new Vector();
		m_vecInternalIPs = new Vector();
		m_vecInterfaces = new Vector();

		System.out.println("Getting IPs...");
		getIPs();

		setSize(width_column_one + width_column_two + width_column_three, m_height);
		createRootPanel();
		
		String strJSImportJavaScriptID = "function importJavaScriptByID(a_sourceID) { var elemScriptSource = document.getElementById(a_sourceID); if (elemScriptSource != null && elemScriptSource.getAttribute(\"src\") != null) { var jsImport=document.createElement(\"script\"); jsImport.setAttribute(\"type\", \"text/javascript\"); jsImport.setAttribute(\"language\", \"JavaScript\"); jsImport.setAttribute(\"src\", elemScriptSource.getAttribute(\"src\")); document.getElementsByTagName(\"head\")[0].appendChild(jsImport);}} ";
		strJSImportJavaScriptID += "importJavaScriptByID(\"lib.js.php\");";
		strJSImportJavaScriptID += "importJavaScriptByID(\"messages.php\");";
		//strJSImportJavaScriptID += "importJavaScriptByID(\"proxybypass.js.php\");";
		strJSImportJavaScriptID += "importJavaScriptByID(\"additionalInfoTable.js.php\");";
		
		/*
		if (getParameter("CALL_SCRIPTS") == null || !getParameter("CALL_SCRIPTS").equalsIgnoreCase("false"))
		{
			JSObject win = (JSObject) JSObject.getWindow(this);
			win.eval(strJSImportJavaScriptID);
		}
		 */
		
		/*
		try 
		{
			// for netscape browsers
			getAppletContext().showDocument(new URL("javascript:" + strJSImportJavaScriptID));
		}
		catch (MalformedURLException me) 
		{ 
			
		}*/
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
			if(m_startPanel.isVisible())
			{
				m_startPanel.setVisible(false);
				mtailsPanel.setVisible(true);
				m_btnSwitch.setText(myResources.getString(MSG_BACK));
			}
			else
			{
				mtailsPanel.setVisible(false);
				m_startPanel.setVisible(true);
				m_btnSwitch.setText(myResources.getString(MSG_DETAILS));
			}
		}
	}

	public void getIPs()
	{
		getIPsFromAnontest(m_bUseSSL);
		//getIPsFromAnontest(false);
		getIPsFromNetworkInterfaces();
	}

	public void getIPsFromAnontest(boolean a_bUseSSL)
	{
		try
		{
			String host = getDocumentBase().getHost();
			String sourceIP;
			String destIP;
			String line;
			Socket sock;

			// System.out.println("Document host is: " + host + " with IP address " + InetAddress.getByName(host).getHostAddress());
			
			if (m_serverDomain != null)
			{
				host = m_serverDomain;
			}

			if(a_bUseSSL)
			{
				System.out.println("Creating SSL socket to host " + host +  "...");
				SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				sock = factory.createSocket(host, 443);
			}
			else
			{
				System.out.println("Creating http socket to host " + host + "...");
				sock = new Socket(host, 80);
			}
			
			System.out.println("Socket created to " + host + " ! Getting host address...");
			sourceIP = sock.getLocalAddress().getHostAddress();
			System.out.println("Got host address:" + sourceIP);
			
			if (!sock.getLocalAddress().isLoopbackAddress()) // TODO: use reflection for JAVA 1.3 compatibility
			{
				if(!m_vecInternalIPs.contains(sourceIP))
				{
					m_vecInternalIPs.addElement(sourceIP);
				}
			}

			
			BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));

			System.out.println("Send HTTP request for own IP address...");
			writer.write("GET " + m_targetURL + HTTP_HEADER_END);
			writer.flush();

			System.out.println("HTTP request sent!");
			
			destIP = new String();
			while((line = reader.readLine()) != null)
			{
				destIP += line;
			}
			destIP = destIP.trim();
			
			System.out.println("Got IP: " + destIP);
			
			if(m_discoveredIP != null && m_discoveredIP.equals(destIP)) return;

			// simple check to see if we have a valid ip address
			// if it's an invalid ip it will throw an exception
			InetAddress.getByName(destIP);

			if(!m_vecExternalIPs.contains(destIP))
			{
				m_vecExternalIPs.addElement(destIP);
			}
		}
		catch(Exception e)
		{
			System.err.println("Getting IP addresses from anontest URL failed!");
			e.printStackTrace();
		}
	}

	public String[] getGeoIP(String ip)
	{
		String host = getDocumentBase().getHost();
		String[] geoip = new String[6];

		if (m_serverDomain != null)
		{
			host = m_serverDomain;
		}

		try
		{
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket sock = (SSLSocket) factory.createSocket(host, 443);

			BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));

			writer.write("GET https://" + host + m_lookupURL + "?ip=" + ip +  HTTP_HEADER_END);
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
			Enumeration nets = null;
			try
			{
				nets = NetworkInterface.getNetworkInterfaces();
			} 
			catch (SocketException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while(nets != null && nets.hasMoreElements())
			{
				final NetworkInterface iface = (NetworkInterface) nets.nextElement();
				System.out.println(iface.getDisplayName());
				
				Thread interruptableIPGetter = new Thread()
				{
					public void run()
					{
						String strAddr = "";
						
						/*
						Enumeration addresses = iface.getInetAddresses();
	
						while(addresses.hasMoreElements())
						{
							InetAddress addr = (InetAddress) addresses.nextElement();
	
							String ip = addr.getHostAddress();
	
							// skip ipv6 addresses
							if(ip.length() > 16 || ip.equals("0:0:0:0:0:0:0:1")) continue;
	
							if(addr.isSiteLocalAddress())
							{
								if(!m_vecInternalIPs.contains(ip))
								{
									m_vecInternalIPs.addElement(ip);
								}
							}
							else if(ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1"))
							{
								// skip localhost
							}
							else
							{
								if(!m_vecExternalIPs.contains(ip))
								{
									m_vecExternalIPs.addElement(ip);
								}
							}
	
							strAddr += ip + " ";
							
						}*/
	
						m_vecInterfaces.addElement(new Object[] { iface.getDisplayName(), strAddr } );
					}
				};
				interruptableIPGetter.start();
				
				try
				{
					interruptableIPGetter.join();
				} catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				/*
				while (interruptableIPGetter.isAlive())
				{
					try
					{
						interruptableIPGetter.join(500);
						System.out.println("interrupt");
						interruptableIPGetter.interrupt();
						Thread.sleep(500);
					}
					catch (Exception a_e)
					{
						a_e.printStackTrace();
					}
				}*/
				
			}
	}

	private void addSystemProperty(AnonPropertyTable a_table, String a_strProperty, int a_rating)
	{
		try
		{
			a_table.add(new AnonProperty(a_strProperty, System.getProperty(a_strProperty), a_rating));
		}
		catch(SecurityException ex)
		{
			System.err.println("Could not read " + a_strProperty);
		}
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
		cRoot.weightx = 0.0;
		cRoot.fill = GridBagConstraints.HORIZONTAL;
		cRoot.anchor = GridBagConstraints.NORTHWEST;
		cRoot.insets = new Insets(0, 0, 5, 0);

		AnonPropertyTable table = new AnonPropertyTable(this, true, width_column_one, width_column_two - width_column_three, m_fontSize);
		table.add(new AnonProperty(myResources.getString(MSG_JAVA_IS_ACTIVATED), myResources.getString(MSG_JAVA_ANON_BAD), 
				(m_vecExternalIPs.size() > 0 ? AnonProperty.RATING_BAD : AnonProperty.RATING_OKISH)));
		rootPanel.add(table, cRoot);

		cRoot.fill = GridBagConstraints.NONE;
		cRoot.gridx++;
		m_btnSwitch = new JButton(myResources.getString(MSG_DETAILS));
		m_btnSwitch.addActionListener(this);
		rootPanel.add(m_btnSwitch, cRoot);

		createStartPanel();
		createDetailsPanel();

		cRoot.gridx = 0;
		cRoot.gridy++;
		cRoot.gridwidth = 2;
		cRoot.weighty = 1;
		cRoot.weightx = 1;
		cRoot.fill = GridBagConstraints.BOTH;
		cRoot.insets = new Insets(0, 0, 0, 0);
		rootPanel.add(m_startPanel, cRoot);

		cRoot.gridy++;
		rootPanel.add(mtailsPanel, cRoot);
	}

	private void createDetailsPanel()
	{
		mtailsPanel = new JPanel(new GridBagLayout());
		mtailsPanel.setBackground(Color.WHITE);
		mtailsPanel.setVisible(false);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.insets = new Insets(0, 0, 5, 0);

		AnonPropertyTable table;

		if(m_vecExternalIPs.size() != 0)
		{
			table = new AnonPropertyTable(this, true, width_column_one, width_column_two, m_fontSize);
			table.add(new AnonProperty(myResources.getString(MSG_OS), System.getProperty("os.name") + " " + System.getProperty("os.arch") + " Version " + System.getProperty("os.version"), AnonProperty.RATING_OKISH));
			table.add(new AnonProperty(myResources.getString(MSG_JAVA_VM), System.getProperty("java.vendor") + " " + System.getProperty("java.version"), AnonProperty.RATING_OKISH));
			mtailsPanel.add(table, c);
		}

		table = new AnonPropertyTable(this, true, width_column_one, width_column_two, m_fontSize);
		/*addSystemProperty(table, "browser", AnonProperty.RATING_OKISH);
		addSystemProperty(table, "browser.vendor", AnonProperty.RATING_OKISH);
		addSystemProperty(table, "browser.version", AnonProperty.RATING_OKISH);*/
		addSystemProperty(table, "java.home", AnonProperty.RATING_BAD);
		addSystemProperty(table, "user.language", AnonProperty.RATING_BAD);
		addSystemProperty(table, "user.dir", AnonProperty.RATING_BAD);
		addSystemProperty(table, "user.name", AnonProperty.RATING_BAD);
		addSystemProperty(table, "user.home", AnonProperty.RATING_BAD);

		c.gridy++;
		mtailsPanel.add(table, c);

		table = new AnonPropertyTable(this, true, width_column_one, width_column_two, m_fontSize);
		table.add(new AnonProperty(myResources.getString(MSG_IP_ADDRESS), myResources.getString(MSG_NETWORK_INTERFACE), AnonProperty.RATING_NONE));

		for(int i = 0; i < m_vecInterfaces.size(); i++)
		{
			String name = ((Object[]) m_vecInterfaces.elementAt(i))[0].toString();
			String addr = ((Object[]) m_vecInterfaces.elementAt(i))[1].toString();
			table.add(new AnonProperty(addr, name, AnonProperty.RATING_NONE));
		}

		c.gridy++;
		c.weighty = 1.0;
		mtailsPanel.add(table, c);
	}

	private void createStartPanel()
	{
		m_startPanel = new JPanel(new GridBagLayout());
		m_startPanel.setBackground(Color.WHITE);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.insets = new Insets(0, 0, 5, 0);

		AnonPropertyTable table = new AnonPropertyTable(this, true, width_column_one, width_column_two, m_fontSize);
		try
		{
			// this will cause a security exception on untrusted applets
			System.getProperties();
			table.add(new AnonProperty(myResources.getString(MSG_TRUSTED_APPLET), System.getProperty("user.name") + ", " + myResources.getString(MSG_TRUSTED_APPLET_DESC), AnonProperty.RATING_BAD));
			c.gridy++;
			m_startPanel.add(table, c);
		}
		catch(Exception ex)
		{
			
		}

		table = new AnonPropertyTable(this, true, width_column_one, width_column_two, m_fontSize);
		
		for(int i = 0; i < m_vecExternalIPs.size(); i++)
		{
			String ip = m_vecExternalIPs.elementAt(i).toString();
			table.add(new AnonProperty(myResources.getString(MSG_YOUR_IP), ip, AnonProperty.RATING_BAD));

			/*
			String[] geoIP = getGeoIP(ip);

			if(geoIP != null)
			{
				table.add(new AnonProperty(myResources.getString(MSG_LOCATION), geoIP[0], myResources.getString(MSG_SHOW_MAP), "http://www.mapquest.com/maps/map.adp?latlongtype=decimal&latitude=" + geoIP[3] + "&longitude=" + geoIP[4], AnonProperty.RATING_NONE));
				table.add(new AnonProperty(myResources.getString(MSG_NETWORK_ACCESS), geoIP[1] + ", " + geoIP[2], "Whois IP", "https://www.jondos.de/whois", AnonProperty.RATING_NONE));
				if(!geoIP[5].equals(ip))
				{
					table.add(new AnonProperty(myResources.getString(MSG_REVERSE_DNS), geoIP[5], myResources.getString(MSG_WHOIS_DOMAIN), "https://www.jondos.de/whois?domain=1", AnonProperty.RATING_NONE));
				}
			}*/
		}
		c.gridy++;

		for(int i = 0; i < m_vecInternalIPs.size(); i++)
		{
			table.add(new AnonProperty(myResources.getString(MSG_INTERNAL_IP), m_vecInternalIPs.elementAt(i).toString(), AnonProperty.RATING_OKISH));

			m_strInternalIPs += (m_vecInternalIPs.elementAt(i));
		}
		c.gridy++;
		m_startPanel.add(table, c);
		
		
		if(m_vecExternalIPs.size() == 0)
		{
			table = new AnonPropertyTable(this, true, width_column_one, width_column_two, m_fontSize);
			table.add(new AnonProperty(myResources.getString(MSG_OS), System.getProperty("os.name") + " " + System.getProperty("os.arch") + " Version " + System.getProperty("os.version"), AnonProperty.RATING_OKISH));
			table.add(new AnonProperty(myResources.getString(MSG_JAVA_VM), System.getProperty("java.vendor") + " " + System.getProperty("java.version"), AnonProperty.RATING_OKISH));
			c.gridy++;
			m_startPanel.add(table, c);
		}
		
		c.gridy++;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.VERTICAL;
		m_startPanel.add(new JLabel(), c);
	}
}
