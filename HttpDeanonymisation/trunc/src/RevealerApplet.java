import java.applet.Applet;
import java.applet.AppletStub;
import java.awt.Component;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Vector;
import java.lang.reflect.*;

public class RevealerApplet extends JApplet implements ActionListener, AppletStub
{
	public String m_strExternalIPs;
	public String m_strInternalIPs;

	private String m_targetURL = "/geoip/onlyip.php";
	private String m_lookupURL = "/geoip/lookup.php";
	private int m_width = 600;
	private int m_height = 300;

	private Vector m_vecExternalIPs;
	private Vector m_vecInternalIPs;
	private Vector m_vecInterfaces;
	
	private JPanel m_startPanel;
	private JPanel m_detailsPanel;
	private JButton m_btnSwitch;
	JLabel m_lblJavaIsDangerous;
	JLabel m_lblLocation;
	JLabel m_lblProvider;

	private String m_discoveredIP;

	private final static String TEXT_SWITCH_TO_DETAIL_PANEL = "Details";
	private final static String TEXT_SWITCH_TO_NETWORK_PANEL = "Zur\u00fcck";

	private String m_javaIsDangerousUrl = "https://www.jondos.de/javaisdangerous";

	public void appletResize(int width, int height)
	{
		resize(width, height);
	}
	
	public void init()
	{
		super.init();

		if(getParameter("WIDTH") != null)
		{
			try
			{
				m_width = Integer.parseInt(getParameter("WIDTH"));
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

		if(getParameter("JAVA_IS_DANGEROUS_URL") != null)
		{
			String url = getParameter("JAVA_IS_DANGEROUS_URL");
			if(url.startsWith("https://www.anonymix.eu") || url.startsWith("https://www.jondos.de"))
				m_javaIsDangerousUrl = url;
		}

			
		m_discoveredIP = getParameter("DISCOVERED_IP");

		setSize(m_width, m_height);

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
			if(m_startPanel.isVisible())
			{
				m_startPanel.setVisible(false);
				m_detailsPanel.setVisible(true);
				m_btnSwitch.setText(TEXT_SWITCH_TO_NETWORK_PANEL);
			}
			else
			{
				m_detailsPanel.setVisible(false);
				m_startPanel.setVisible(true);
				m_btnSwitch.setText(TEXT_SWITCH_TO_DETAIL_PANEL);
			}
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
			String host = getDocumentBase().getHost();
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
			
			// simple check to see if we have a valid ipv4 address
			// if it's an invalid ip it will throw an exception
			InetAddress.getByName(destIP);

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
		String host = getDocumentBase().getHost();
		String[] geoip = new String[6];

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
				}

				m_vecInterfaces.addElement(new Object[] { iface.getDisplayName(), strAddr } );
			}
		}
		catch(Exception e)
		{
			System.out.println("Listing network interfaces failed: " + e.getMessage());
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
			System.out.println("Could not read " + a_strProperty);
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
		cRoot.insets = new Insets(5, 5, 5, 10);

		AnonPropertyTable table = new AnonPropertyTable(this, true);
		table.add(new AnonProperty("Java aktiviert!", "Java beeintr\u00e4chtigt Ihre Anonymit\u00e4t!", AnonProperty.RATING_BAD));
		rootPanel.add(table, cRoot);

		cRoot.fill = GridBagConstraints.NONE;
		cRoot.gridx++;
		m_btnSwitch = new JButton(TEXT_SWITCH_TO_DETAIL_PANEL);
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
		cRoot.anchor = GridBagConstraints.NORTHWEST;
		cRoot.insets = new Insets(5, 5, 5, 0);
		rootPanel.add(m_startPanel, cRoot);

		cRoot.gridy++;
		rootPanel.add(m_detailsPanel, cRoot);
	}

	private void createDetailsPanel()
	{
		m_detailsPanel = new JPanel(new GridBagLayout());
		m_detailsPanel.setBackground(Color.WHITE);
		m_detailsPanel.setVisible(false);

		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.insets = new Insets(0, 0, 5, 0);

		AnonPropertyTable table = new AnonPropertyTable(this, true);
		table.setWidth(0, 140);
		table.setWidth(1, 430);
		table.add(new AnonProperty("Betriebssystem", System.getProperty("os.name") + " " + System.getProperty("os.arch") + " Version " + System.getProperty("os.version"), AnonProperty.RATING_OKISH));
		table.add(new AnonProperty("Java VM", System.getProperty("java.vendor") + " " + System.getProperty("java.version"), AnonProperty.RATING_OKISH));
		m_detailsPanel.add(table, c);

		table = new AnonPropertyTable(this, true);
		table.setWidth(0, 140);
		table.setWidth(1, 430);
		/*addSystemProperty(table, "browser", AnonProperty.RATING_OKISH);
		addSystemProperty(table, "browser.vendor", AnonProperty.RATING_OKISH);
		addSystemProperty(table, "browser.version", AnonProperty.RATING_OKISH);*/
		addSystemProperty(table, "java.home", AnonProperty.RATING_BAD);
		addSystemProperty(table, "user.dir", AnonProperty.RATING_BAD);
		addSystemProperty(table, "user.name", AnonProperty.RATING_BAD);
		addSystemProperty(table, "user.home", AnonProperty.RATING_BAD);
		
		c.gridy++;
		m_detailsPanel.add(table, c);

		table = new AnonPropertyTable(this, true);
		table.setWidth(0, 140);
		table.setWidth(1, 430);
		table.add(new AnonProperty("IP-Adresse", "Netzwerkschnitstelle", AnonProperty.RATING_NONE));

		for(int i = 0; i < m_vecInterfaces.size(); i++)
		{
			String name = ((Object[]) m_vecInterfaces.elementAt(i))[0].toString();
			String addr = ((Object[]) m_vecInterfaces.elementAt(i))[1].toString();
			table.add(new AnonProperty(addr, name, AnonProperty.RATING_NONE));
		}
		
		c.gridy++;
		c.weighty = 1.0;
		m_detailsPanel.add(table, c);
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

		AnonPropertyTable table = new AnonPropertyTable(this, true);
		table.setRowHeight(40);
		table.setWidth(1, 460);
		try
		{
			// this will cause a security exception on untrusted applets
			System.getProperties();
			table.add(new AnonProperty("<html>&nbsp;Applet<br>&nbsp;vertraut</html>", "<html>&nbsp;Sie haben dem signierten Applet vertraut, " + System.getProperty("user.name") + "! Ein b\u00f6sartiges<br>&nbsp;Applet h\u00e4tte nun vollen Zugriff auf ihren PC!</html>", AnonProperty.RATING_BAD));
		}
		catch(Exception ex)
		{
			//table.add(new AnonProperty("<html>Applet <br>nicht vertraut</html>", "Sie haben dem Applet nicht vertraut, es hat also nur", AnonProperty.RATING_OKISH));
		}

		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 2;
		m_startPanel.add(table, c);

		for(int i = 0; i < m_vecExternalIPs.size(); i++)
		{
			String ip = m_vecExternalIPs.elementAt(i).toString();

			if(m_discoveredIP != null && m_discoveredIP.equals(ip)) continue;

			table = new AnonPropertyTable(this, false);

			table.add(new AnonProperty("IP-Addresse", ip, AnonProperty.RATING_BAD));

			String[] geoIP = getGeoIP(ip);

			if(geoIP != null)
			{
				table.add(new AnonProperty("Standort", geoIP[0], "Karte", "http://www.mapquest.com/maps/map.adp?latlongtype=decimal&latitude=" + geoIP[3] + "&longitude=" + geoIP[4], AnonProperty.RATING_NONE));
				table.add(new AnonProperty("Netzzugang", geoIP[1] + ", " + geoIP[2], "Whois IP", "https://www.jondos.de/whois", AnonProperty.RATING_NONE));
				if(!geoIP[5].equals(ip))
				{
					table.add(new AnonProperty("Reverse DNS", geoIP[5], "Whois Domain", "https://www.jondos.de/whois?domain=1", AnonProperty.RATING_NONE));
				}
			}

			c.gridy++;
			m_strExternalIPs += ip;
			m_startPanel.add(table, c);
		}

		c.insets = new Insets(0, 0, 5, 0);
		c.weighty = 1.0;
		table = new AnonPropertyTable(this, true);
		table.setWidth(1, 460);
		for(int i = 0; i < m_vecInternalIPs.size(); i++)
		{
			table.add(new AnonProperty("Interne IP", m_vecInternalIPs.elementAt(i).toString(), AnonProperty.RATING_OKISH));

			m_strInternalIPs += (m_vecInternalIPs.elementAt(i));
		}
		c.gridy++;
		m_startPanel.add(table, c);
	}
}
