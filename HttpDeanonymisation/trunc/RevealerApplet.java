import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import javax.net.ssl.SSLSocketFactory;
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

public class RevealerApplet extends JApplet implements ActionListener 
{
	public String m_strExternalIPs = "";
	public String m_strInternalIPs = "";
	
	private String m_targetURL = "/de/files/anontest/onlyip.php";
	private int m_width = 500;
	private int m_height = 250;
	
	private Vector m_vecExternalIPs;
	private Vector m_vecInternalIPs;
	private Vector m_vecInterfaces;
	
	private Font m_fNormal = new Font("Verdana", 0, 12);
	private Font m_fHeader = new Font("Verdana", Font.BOLD, 14);
	
	private Color m_cNormal = new Color(80, 80, 80);
	private Color m_cHeader = new Color(0, 0, 160);
	private JPanel m_networkPanel;
	private JPanel m_osPanel;
	private JButton m_btnSwitch;
	
	private final static String TEXT_SWITCH_TO_OS_PANEL = "Betriebssystem-Eigenschaften anzeigen";
	private final static String TEXT_SWITCH_TO_NETWORK_PANEL = "Netzwerk-Eigenschaften anzeigen";
	
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
		
		this.setSize(m_width, m_height);
		
		m_vecExternalIPs = new Vector<String>();
		m_vecInternalIPs = new Vector<String>();
		m_vecInterfaces = new Vector<Object[]>();
		
		getIPs();
	
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e)
		{
			System.out.println("Error setting native Look and Feel: " + e);
		}
		
		GridBagConstraints c = new  GridBagConstraints();
		
		JPanel rootPanel = new JPanel(new GridBagLayout());
		rootPanel.setBackground(Color.WHITE);
		JScrollPane scroll = new JScrollPane(rootPanel);
		scroll.setBorder(null);
		getContentPane().add(scroll);
		
		m_networkPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridy = 0;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.insets = new Insets(0, 0, 7, 0);
		m_networkPanel.add(createHeaderLabel("Externe IP-Adressen:"), c);
		c.insets = new Insets(0, 0, 5, 0);
		for(int i = 0; i < m_vecExternalIPs.size(); i++)
		{
			c.gridy++;
			m_networkPanel.add(createLabel(m_vecExternalIPs.elementAt(i).toString()), c);
			m_strExternalIPs += m_vecExternalIPs.elementAt(i);
		}
		
		c.gridy++;
		c.insets = new Insets(0, 0, 7, 0);
		m_networkPanel.add(createHeaderLabel("Interne IP-Adressen:"), c);
		c.insets = new Insets(0, 0, 5, 0);
		for(int i = 0; i < m_vecInternalIPs.size(); i++)
		{
			c.gridy++;
			m_networkPanel.add(createLabel(m_vecInternalIPs.elementAt(i).toString()), c);
			m_strInternalIPs += m_vecInternalIPs.elementAt(i);
		}
		
		c.gridy++;
		c.insets = new Insets(0, 0, 7, 0);
		m_networkPanel.add(createHeaderLabel("Netzwerkschnittstellen:"), c);
		m_networkPanel.setBackground(Color.WHITE);
		c.insets = new Insets(0, 0, 5, 0);
		for(int i = 0; i < m_vecInterfaces.size(); i++)
		{
			String name = ((Object[]) m_vecInterfaces.elementAt(i))[0].toString();
			String addr = ((Object[]) m_vecInterfaces.elementAt(i))[1].toString();
			c.gridy++;
			m_networkPanel.add(createLabel((i + 1) + ". " + name), c);
			c.gridy++;
			if(addr.length() != 0)
				m_networkPanel.add(createLabel("    " + addr),c);
		}
		
		m_osPanel = new JPanel(new GridBagLayout());
		m_osPanel.setBackground(Color.WHITE);
		m_osPanel.setVisible(false);
		
		c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridy = 0;
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.insets = new Insets(0, 0, 7, 0);
		m_osPanel.add(createHeaderLabel("Betriebsystem:"), c);
			
		c.gridy++;
		m_osPanel.add(createLabel(System.getProperty("os.name") + " " + System.getProperty("os.arch") + " Version " + System.getProperty("os.version") + " "), c);
		
		c.gridy++;
		c.insets = new Insets(0, 0, 7, 0);
		m_osPanel.add(createHeaderLabel("Java VM:"), c);
		c.gridy++;
		m_osPanel.add(createLabel(System.getProperty("java.vendor") + " " + System.getProperty("java.version")), c);
		
		c.gridy++;
		m_osPanel.add(createHeaderLabel("Sonstige Eigenschaften:"), c);
		
		addSystemPropertyLabel(m_osPanel, "browser", c);
		addSystemPropertyLabel(m_osPanel, "browser.vendor", c);
		addSystemPropertyLabel(m_osPanel, "browser.version", c);
		addSystemPropertyLabel(m_osPanel, "java.home", c);
		addSystemPropertyLabel(m_osPanel, "java.class.path", c);
		addSystemPropertyLabel(m_osPanel, "user.name", c);
		addSystemPropertyLabel(m_osPanel, "user.home", c);
		addSystemPropertyLabel(m_osPanel, "user.dir", c);
		
		try
		{
		c.gridy++;
		m_osPanel.add(createHeaderLabel("Dateien unter C:\\"), c);
		
		java.io.File f = new java.io.File("c:\\");
		java.io.File[] list = f.listFiles();
		for(int i = 0; i < list.length; i++)
		{
			c.gridy++;
			m_osPanel.add(createLabel(list[i].getPath()), c);
		}
		}
		catch(Exception ex)
		{
			
		}
		
		m_btnSwitch = new JButton(TEXT_SWITCH_TO_OS_PANEL);
		m_btnSwitch.addActionListener(this);
		
		GridBagConstraints cRoot = new GridBagConstraints();
		
		cRoot.anchor = GridBagConstraints.NORTHWEST;
		cRoot.fill = GridBagConstraints.HORIZONTAL;
		cRoot.weightx = 1.0;
		rootPanel.add(m_networkPanel, cRoot);
		
		cRoot.gridy++;
		rootPanel.add(m_osPanel, cRoot);
		
		cRoot.gridy++;
		cRoot.weightx = 0.0;
		cRoot.fill = GridBagConstraints.NONE;
		rootPanel.add(m_btnSwitch, cRoot);
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
				m_osPanel.setVisible(true);
				m_btnSwitch.setText(TEXT_SWITCH_TO_NETWORK_PANEL);
			}
			else
			{
				m_osPanel.setVisible(false);
				m_networkPanel.setVisible(true);
				m_btnSwitch.setText(TEXT_SWITCH_TO_OS_PANEL);
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
			String host = this.getDocumentBase().getHost();
			String sourceIP;
			String destIP;
			String line;
			Socket sock;
			
			if(!host.contains("jondos.de") && !host.contains("anonymix.eu"))
			{
				// we're running in an applet viewer
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
			
			destIP = "";
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
					
					if(addr.isSiteLocalAddress())
					{
						if(!m_vecInternalIPs.contains(ip))
						{
							m_vecInternalIPs.addElement(ip);
						}						
					}
					else if(ip.equals("127.0.0.1")) 
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
			a_panel.add(createLabel(a_strProperty + " = " + System.getProperty(a_strProperty)), a_constrains);
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

	private JLabel createLabel(String a_strLabel)
	{
		JLabel lbl = new JLabel(a_strLabel);
		lbl.setFont(m_fNormal);
		lbl.setForeground(m_cNormal);
		return lbl;
	}

}
