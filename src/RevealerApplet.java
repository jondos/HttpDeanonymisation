import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;
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

import util.Messages;

public class RevealerApplet extends JApplet implements ActionListener
{
	private static final String MSG_NETWORK_ACCESS = RevealerApplet.class.getName() + ".networkAccess";
	private static final String MSG_SHOW_MAP = RevealerApplet.class.getName() + ".showMap";
	private static final String MSG_LOCATION = RevealerApplet.class.getName() + ".location";
	private static final String MSG_OS = RevealerApplet.class.getName() + ".os";
	private static final String MSG_INTERNAL_IP = RevealerApplet.class.getName() + ".internalIP";
	private static final String MSG_TRUSTED_APPLET_DESC= RevealerApplet.class.getName() + ".trustedAppletDescription";
	private static final String MSG_TRUSTED_APPLET = RevealerApplet.class.getName() + ".trustedApplet";
	private static final String MSG_NETWORK_INTERFACE = RevealerApplet.class.getName() + ".networkInterface";
	private static final String MSG_IP_ADDRESS = RevealerApplet.class.getName() + ".ipAddress";
	private static final String MSG_JAVA_IS_ACTIVATED = RevealerApplet.class.getName() + ".javaIsActivated";
	private static final String MSG_JAVA_ANON_BAD = RevealerApplet.class.getName() + ".javaAnonBad";
	private final static String MSG_DETAILS = RevealerApplet.class.getName() + ".details";
	private final static String MSG_BACK = RevealerApplet.class.getName() + ".back";	
	private static final String MSG_JAVA_VM = RevealerApplet.class.getName() + ".javaVM";
	private static final String MSG_WHOIS_DOMAIN = RevealerApplet.class.getName() + ".whoisDomain";
	private static final String MSG_REVERSE_DNS = RevealerApplet.class.getName() + ".reverseDNS";
	
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
	private JPanel mtailsPanel;
	private JButton m_btnSwitch;

	JLabel m_lblLocation;
	JLabel m_lblProvider;

	private String m_discoveredIP;

	public void init()
	{
		super.init();
		
		Messages.init("anontest");

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
		
		if(getParameter("LOCALE") != null)
		{
			Messages.init(new Locale(getParameter("LOCALE"), ""), "anontest");
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
				mtailsPanel.setVisible(true);
				m_btnSwitch.setText(Messages.getString(MSG_BACK));
			}
			else
			{
				mtailsPanel.setVisible(false);
				m_startPanel.setVisible(true);
				m_btnSwitch.setText(Messages.getString(MSG_DETAILS));
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
		cRoot.anchor = GridBagConstraints.NORTHWEST;
		cRoot.insets = new Insets(5, 5, 5, 10);

		AnonPropertyTable table = new AnonPropertyTable(this, true);
		table.add(new AnonProperty(Messages.getString(MSG_JAVA_IS_ACTIVATED), Messages.getString(MSG_JAVA_ANON_BAD), AnonProperty.RATING_BAD));
		rootPanel.add(table, cRoot);

		cRoot.fill = GridBagConstraints.NONE;
		cRoot.gridx++;
		m_btnSwitch = new JButton(Messages.getString(MSG_DETAILS));
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
		cRoot.insets = new Insets(5, 5, 5, 0);
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
			table = new AnonPropertyTable(this, true);
			table.setWidth(1, 430);
			table.add(new AnonProperty(Messages.getString(MSG_OS), System.getProperty("os.name") + " " + System.getProperty("os.arch") + " Version " + System.getProperty("os.version"), AnonProperty.RATING_OKISH));
			table.add(new AnonProperty(Messages.getString(MSG_JAVA_VM), System.getProperty("java.vendor") + " " + System.getProperty("java.version"), AnonProperty.RATING_OKISH));
			mtailsPanel.add(table, c);
		}

		table = new AnonPropertyTable(this, true);
		table.setWidth(1, 430);
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

		table = new AnonPropertyTable(this, true);
		table.setWidth(1, 430);
		table.add(new AnonProperty(Messages.getString(MSG_IP_ADDRESS), Messages.getString(MSG_NETWORK_INTERFACE), AnonProperty.RATING_NONE));

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

		AnonPropertyTable table = new AnonPropertyTable(this, true);
		table.setRowHeight(40);
		table.setWidth(1, 460);
		try
		{
			// this will cause a security exception on untrusted applets
			System.getProperties();
			table.add(new AnonProperty(Messages.getString(MSG_TRUSTED_APPLET), "<html><div style='padding-left: 5px; padding-top: 1px'>" + Messages.getString(MSG_TRUSTED_APPLET_DESC, System.getProperty("user.name")) + "</div></html>", AnonProperty.RATING_BAD));
			c.gridy++;
			m_startPanel.add(table, c);
		}
		catch(Exception ex)
		{
			
		}

		for(int i = 0; i < m_vecExternalIPs.size(); i++)
		{
			String ip = m_vecExternalIPs.elementAt(i).toString();

			table = new AnonPropertyTable(this, false);

			table.add(new AnonProperty(Messages.getString(MSG_IP_ADDRESS), ip, AnonProperty.RATING_BAD));

			String[] geoIP = getGeoIP(ip);

			if(geoIP != null)
			{
				table.add(new AnonProperty(Messages.getString(MSG_LOCATION), geoIP[0], Messages.getString(MSG_SHOW_MAP), "http://www.mapquest.com/maps/map.adp?latlongtype=decimal&latitude=" + geoIP[3] + "&longitude=" + geoIP[4], AnonProperty.RATING_NONE));
				table.add(new AnonProperty(Messages.getString(MSG_NETWORK_ACCESS), geoIP[1] + ", " + geoIP[2], "Whois IP", "https://www.jondos.de/whois", AnonProperty.RATING_NONE));
				if(!geoIP[5].equals(ip))
				{
					table.add(new AnonProperty(Messages.getString(MSG_REVERSE_DNS), geoIP[5], Messages.getString(MSG_WHOIS_DOMAIN), "https://www.jondos.de/whois?domain=1", AnonProperty.RATING_NONE));
				}
			}

			c.gridy++;
			m_strExternalIPs += ip;
			m_startPanel.add(table, c);
		}

		if(m_vecExternalIPs.size() == 0)
		{
			table = new AnonPropertyTable(this, true);
			table.setWidth(1, 460);
			table.add(new AnonProperty(Messages.getString(MSG_OS), System.getProperty("os.name") + " " + System.getProperty("os.arch") + " Version " + System.getProperty("os.version"), AnonProperty.RATING_OKISH));
			table.add(new AnonProperty(Messages.getString(MSG_JAVA_VM), System.getProperty("java.vendor") + " " + System.getProperty("java.version"), AnonProperty.RATING_OKISH));
			c.gridy++;
			m_startPanel.add(table, c);
		}

		c.insets = new Insets(0, 0, 5, 0);
		c.weighty = 1.0;
		table = new AnonPropertyTable(this, true);
		table.setWidth(1, 460);
		for(int i = 0; i < m_vecInternalIPs.size(); i++)
		{
			table.add(new AnonProperty(Messages.getString(MSG_INTERNAL_IP), m_vecInternalIPs.elementAt(i).toString(), AnonProperty.RATING_OKISH));

			m_strInternalIPs += (m_vecInternalIPs.elementAt(i));
		}
		c.gridy++;
		m_startPanel.add(table, c);
	}
}
