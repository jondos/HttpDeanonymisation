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


import netscape.javascript.JSException;
import netscape.javascript.JSObject;


import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import anon.util.Base64;


//import util.Messages;

public class RevealerApplet extends JApplet implements ActionListener
{
	private static final String MSG_NETWORK_ACCESS = "networkAccess";
	private static final String MSG_SHOW_MAP = "showMap";
	private static final String MSG_LOCATION = "location";
	private static final String MSG_OS = "os";
	private static final String MSG_INTERNAL_IP = "internalIP";
	private static final String MSG_FIX_PROBLEM = "fixProblem";
	private static final String MSG_TRUSTED_APPLET_DESC= "trustedAppletDescription";
	private static final String MSG_TRUSTED_APPLET = "trustedApplet";
	private static final String MSG_NETWORK_INTERFACE = "networkInterface";
	private static final String MSG_IP_ADDRESS = "ipAddress";
	private static final String MSG_YOUR_IP = "yourIPAddress";
	private static final String MSG_JAVA_IS_ACTIVATED = "javaIsActivated";
	private static final String MSG_JAVA_ANON_BAD = "javaAnonBad";
	private final static String MSG_DETAILS = "details";
	private final static String MSG_BACK = "back";
	private final static String MSG_FONTS = "fonts";
	private static final String MSG_JAVA_VM = "javaVM";
	private static final String MSG_LANG = "language";
	private static final String MSG_SCREEN_RESOLUTION = "screenResolution";
	private static final String MSG_SCREENS = "screens";
	
	private static final String MSG_COUNTRY = "country";
	private static final String MSG_WHOIS_DOMAIN = "whoisDomain";
	private static final String MSG_REVERSE_DNS = "reverseDNS";
	
	final static String CRLF = "\r\n";
	final static String HTTP_HEADER_END = CRLF+CRLF; //end of http message headers
	
	

	private String m_targetURL;
	private String m_lookupURL = "/geoip/lookup.php";
	private int width_column_one = 100;
	private int width_column_two = 400;
	private int width_column_three = 100;
	private int m_height = 300;
	private int m_fontSize = 14;
	private int m_iRowHeight = 18;
	
	private IPInfo m_externalIP;
	private IPInfo m_internalIP;
	private Vector m_vecInterfaces;
	
	private Vector m_vecAnonProperties;

	private JPanel m_startPanel;
	private JPanel mtailsPanel;
	private JButton m_btnSwitch;
	private JButton m_btnBack;

	private boolean m_bUseSSL;
	private String m_serverDomain;
	private URL m_urlProxifierSettingsPage; 
	
	private ResourceBundle myResources;

	public void init()
	{
		super.init();
		
		URL urlDocumentBase;
		
		System.out.println("Starting up on " + getDocumentBase());
		System.out.println("Loaded from: " + getCodeBase());
		
		
	

		if (getParameter("WIDTH_COLUMN_ONE") != null)
		{
			try
			{
				width_column_one = Integer.parseInt(getParameter("WIDTH_COLUMN_ONE"));
			}
			catch(NumberFormatException e)
			{
				e.printStackTrace();
			}
		}
		
		if (getParameter("proxifierSettingsPage") != null)
		{
			try
			{
				m_urlProxifierSettingsPage = new URL(getParameter("proxifierSettingsPage"));
			}
			catch(MalformedURLException e)
			{
				e.printStackTrace();
			}
		}
		
		
		
		
		
		if (getParameter("WIDTH_COLUMN_TWO") != null)
		{
			try
			{
				width_column_two = Integer.parseInt(getParameter("WIDTH_COLUMN_TWO"));
			}
			catch(NumberFormatException e)
			{
				e.printStackTrace();
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
				e.printStackTrace();
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
				e.printStackTrace();
			}
		}
		
		
		if (getParameter("ROW_HEIGHT") != null)
		{
			try
			{
				m_iRowHeight = Integer.parseInt(getParameter("ROW_HEIGHT"));
			}
			catch(NumberFormatException e)
			{
				e.printStackTrace();
			}
		}
		
		

		if (getParameter("HEIGHT") != null)
		{
			try
			{
				m_height = Integer.parseInt(getParameter("HEIGHT"));
			}
			catch(NumberFormatException e)
			{
				e.printStackTrace();
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
		
		String defaultIP = getParameter("DISCOVERED_IP");
		System.out.println("Already known IP: " + defaultIP);
		m_bUseSSL = getParameter("USE_SSL") != null && getParameter("USE_SSL").equalsIgnoreCase("true");
		
		m_targetURL = getParameter("SERVER_URL_FOR_IP");
		m_serverDomain = getParameter("SERVER_DOMAIN");
		// some web proxies add the port; remove it!
		if (m_serverDomain != null && m_serverDomain.indexOf(":") > 0)
		{
			m_serverDomain = m_serverDomain.substring(0, m_serverDomain.lastIndexOf(":"));
		}

		m_vecInterfaces = new Vector();

		System.out.println("Getting IPs...");
		getIPs(defaultIP);

		setSize(width_column_one + width_column_two + width_column_three, m_height);
		createRootPanel();
		
		
		
		boolean bAlreadyStartedApplet = false;
		try
		{
			/** Check whether this applet has already been started correctly and has sent IP infos to the HTML page. **/
			JSObject win = (JSObject) JSObject.getWindow(this);
			if (win.getMember("_ipInfoJava") != null)
			{
				bAlreadyStartedApplet = true;
			}
		}
		catch(Exception a_e)
		{
		}
		
		
		JSObject win = null;
		if (!bAlreadyStartedApplet)
		{
			String strJSImportJavaScriptID = "";
			/** Prepare the version information that should be sent to the HTML page. **/
			String strVersionInfoJava = 
				(System.getProperty("java.vendor") + " " + System.getProperty("java.version")).replace("'", "\"");
			String strIPInfoJava = null;
			
			// Prepare the java version variable here. JS will automatically read this in the method "mainAdditionalTableInfo".
			strJSImportJavaScriptID += " window._versionJava = '" + strVersionInfoJava + "';";
			if (m_externalIP != null && isSiteLocalAddress(m_externalIP.ip) <= 0 && m_externalIP.strBase64 != null)
			{
				// Prepare the external IP address info variable here. JS will automatically read this in the method "mainBypass".
				strIPInfoJava = m_externalIP.strBase64;
				strJSImportJavaScriptID += " window._ipInfoJava = '" + strIPInfoJava + "'; ";
			}
			
			boolean bMainAdditionalTableInfo = false;
			boolean bMainBypass = false;
			
			try
			{
				/* In order to protect against eval errors (e.g. in IE with Glype/Tor-Proxy), 
				 * set the basic variables here and hope someone reads it even on error.
				 */
				win = JSObject.getWindow(this);
				win.setMember("_versionJava", strVersionInfoJava); // set the JS variable for the version information
				if (strIPInfoJava != null)
				{
					win.setMember("_ipInfoJava", strIPInfoJava); // set the JS variable for the external IP
				}
				try
				{
					if (win.getMember("mainAdditionalTableInfo") != null)
					{
						/*
						 * This function manages all JS settings on the test page. It shows a table with 
						 * many JS values. And it reads the Java version information and presents it.
						 * 
						 * Call this function here if it is loaded. Be warned: web proxies might have filtered it out!
						 */
						win.call("mainAdditionalTableInfo", null);
						bMainAdditionalTableInfo = true;
					}
				} 
				catch (JSException a_e)
				{
					a_e.printStackTrace();
				}
				
				try
				{
					/**
					 * This function is the JS bypass function. It reads the IP address that is received from this applet and displays it.
					 * And of course, it does some other bypassing attacks on web proxies...
					 */
					if (win.getMember("mainBypass") != null)
					{
						win.call("mainBypass", null);
						bMainBypass = true;
					}
				}
				catch (JSException a_e)
				{
					a_e.printStackTrace();
				}
			}
			catch (Exception a_e)
			{
				a_e.printStackTrace();
			}
			
			// Call the two important JS functions on another way if the above calls failed. But only call them if they exist!
			String funcMainAdditionalTableInfo = "if (typeof window.mainAdditionalTableInfo == 'function') {mainAdditionalTableInfo();}";
			String funcMainBypass = "if (typeof window.mainBypass == 'function') {mainBypass();}";
			String funcCreateApplet = "";
			//m_externalIP = null;
			
			/*
			 *  The applet may load itself in another context by JS. But in order to prevent an endless loop, check if
			 *  we have already been called by JS before. 
			 */
			if (m_externalIP == null && 
					(getParameter("FROM_JS") == null || !getParameter("FROM_JS").equalsIgnoreCase("true")))
			{
				funcCreateApplet = "if (typeof window.createApplet == 'function') {createApplet(true);}";
			}
			
			/**
			 * Test whether we have to load the two important JS functions. Load them by a trick: read their source path
			 * from fake jpegs which are - in reality - JS scripts. It is just a nice import function :-)
			 */
			if (!bMainAdditionalTableInfo || !bMainBypass)
			{
				strJSImportJavaScriptID += "window._callPluginScripts = function _callPluginScripts(){" + funcMainAdditionalTableInfo + " " + funcMainBypass + " " + funcCreateApplet + "}; ";
				if (!bMainAdditionalTableInfo)
				{
					strJSImportJavaScriptID += funcMainAdditionalTableInfo;
				}
				if (!bMainBypass)
				{
					strJSImportJavaScriptID += funcMainBypass;
				}
	
				strJSImportJavaScriptID += "function importJavaScriptByID(a_sourceIDs) {var elemScriptSource = document.getElementById(a_sourceIDs.shift()); if (elemScriptSource != null && elemScriptSource.getAttribute(\"src\") != null) {var jsImport=document.createElement(\"script\"); jsImport.setAttribute(\"type\", \"text/javascript\"); jsImport.setAttribute(\"language\", \"JavaScript\"); jsImport.setAttribute(\"src\", elemScriptSource.getAttribute(\"src\")); if (a_sourceIDs.length > 0) {if (typeof a_sourceIDs[0] == 'function') {execFunction = a_sourceIDs[0];} else if (a_sourceIDs[0] === undefined || a_sourceIDs[0] == null) {return;} else {execFunction = importJavaScriptByID;}  jsImport.onreadystatechange = function () { if (this.readyState == 'complete' || this.readyState == 'loaded') execFunction(a_sourceIDs); }; jsImport.onload = function() {execFunction(a_sourceIDs);};} document.getElementsByTagName(\"head\")[0].appendChild(jsImport); }}";
				//this callPluginScripts function is important to call the startup scripts from outside if browser caching prevents starting them from here
				strJSImportJavaScriptID += "importJavaScriptByID(new Array(\"additionalInfoTable.js.php.jpg\", window._callPluginScripts));";
				
				if (getParameter("CALL_SCRIPTS") == null || !getParameter("CALL_SCRIPTS").equalsIgnoreCase("false"))
				{
					try
					{
						win = JSObject.getWindow(this);
						win.eval(strJSImportJavaScriptID);
					}
					catch (Exception a_e)
					{
						// this may happen for IE with Glype (Tor-Proxy)
						a_e.printStackTrace();
					}
				} 
			}
		}
	
		
		try
		{
			urlDocumentBase = new URL(getParameter("DocumentBase"));
		} 
		catch (MalformedURLException e1)
		{
			urlDocumentBase = null;
		}
		if (urlDocumentBase != null && !urlDocumentBase.equals(getDocumentBase()))
		{
			System.out.println("Trying to open " + urlDocumentBase + " in browser window...");
	
			getAppletContext().showDocument(urlDocumentBase, "_self");
		}
		
		//m_externalIP = null;
		/*
		if (m_externalIP == null && 
			(getParameter("FROM_JS") == null || !getParameter("FROM_JS").equalsIgnoreCase("true")))
		{
			try
			{
				win = JSObject.getWindow(this);
				if (win.getMember("mainAdditionalTableInfo") != null)
				{
					win.call("createApplet", new Object[]{new Boolean(true)});
				}
			} 
			catch (JSException a_e)
			{
				a_e.printStackTrace();
			}
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
			m_startPanel.setVisible(false);
			mtailsPanel.setVisible(true);	
		}
		else if (a_event.getSource() == m_btnBack)
		{
			mtailsPanel.setVisible(false);
			m_startPanel.setVisible(true);
		}
	}

	public void getIPs(String a_defaultIP)
	{
		getIPsFromAnontest(a_defaultIP, m_bUseSSL);
		//getIPsFromAnontest(false);
		getIPsFromNetworkInterfaces(a_defaultIP);
	}

	public void getIPsFromAnontest(String a_defaultIP, boolean a_bUseSSL)
	{
		try
		{
			String host;
			String sourceIP;
			String destIP;
			String line;
			Socket sock;
			IPInfo ipinfo = new IPInfo();

			if (getDocumentBase() != null)
			{
				host = getDocumentBase().getHost();
			}
			else
			{
				host = null;
			}
			
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
			
			if (!isLoopbackAddress(sock.getLocalAddress()) &&
					(a_defaultIP == null || !a_defaultIP.equals(sourceIP)))
			{
				m_internalIP = new IPInfo();
				m_internalIP.ip = sock.getLocalAddress();
				//m_internalIP.ip = InetAddress.getByName("95.40.26.1");
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
			ipinfo.strBase64 = destIP;
			destIP = Base64.decodeToString(destIP);			
			StringTokenizer keyTokenizer = new StringTokenizer(destIP, ";");
			StringTokenizer valueTokenizer;
			Hashtable ipAttributes = new Hashtable();
			while(keyTokenizer.hasMoreTokens())
			{
				valueTokenizer = new StringTokenizer(keyTokenizer.nextToken(), "=");
				if (valueTokenizer.countTokens() == 2)
				{
					ipAttributes.put(valueTokenizer.nextToken(), valueTokenizer.nextToken());
				}
			}
			
			ipinfo.distribution = Integer.parseInt((String)ipAttributes.get("ProxyDistribution"));
			ipinfo.strProxy = (String)ipAttributes.get("ProxyName");
			destIP = (String)ipAttributes.get("IP");
				
			
			System.out.println("Got IP: " + destIP);
			
			if (a_defaultIP != null && a_defaultIP.equals(destIP)) return;
			
			if (sourceIP.equals(destIP))
			{
				m_internalIP = null;
			}
			
			//if (m_discoveredIP != null)
			{
				//System.out.println("Already known IP (ignored): " + a_defaultIP);
			}

			// simple check to see if we have a valid ip address
			// if it's an invalid ip it will throw an exception
			ipinfo.ip = InetAddress.getByName(destIP);

			//ipinfo.ip = InetAddress.getByName("192.168.2.1");
			if (isLoopbackAddress(ipinfo.ip))
			{
				return;
			}
			

			m_externalIP = ipinfo;
			
		}
		catch(Exception e)
		{
			System.err.println("Getting IP addresses from anontest URL failed!");
			e.printStackTrace();
		}
	}
	
	private static int isSiteLocalAddress(InetAddress a_address)
	{
		try
		{
			return (a_address.isSiteLocalAddress()?1:0);
		}
		catch (Exception a_e)
		{
			// unknown
			return -1;
		}	
	}
	
	private static boolean isLoopbackAddress(InetAddress a_address)
	{
		try
		{
			return a_address.isLoopbackAddress();
		}
		catch (Exception a_e)
		{
			// java 1.3
		}
		
		if (a_address.getHostAddress().equals("127.0.0.1") || 
			a_address.getHostAddress().equals("0:0:0:0:0:0:0:1"))
		{
			return true;
		}
		return false;
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

	public void getIPsFromNetworkInterfaces(String a_defaultIP)
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
	
	private void addSystemProperty(Vector a_vector, String a_strProperty, int a_rating)
	{
		try
		{
			AnonProperty anonProperty = new AnonProperty(a_strProperty, System.getProperty(a_strProperty), a_rating);
			if (!a_vector.contains(anonProperty))
			{
				a_vector.addElement(anonProperty);
			}
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
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(null);
		getContentPane().add(scroll);

		GridBagConstraints cRoot = new GridBagConstraints();
		cRoot.gridy = 0;
		cRoot.gridx = 0;
		cRoot.weightx = 0.0;
		cRoot.fill = GridBagConstraints.HORIZONTAL;
		cRoot.anchor = GridBagConstraints.NORTHWEST;
		cRoot.insets = new Insets(0, 0, 0, 0);


		m_vecAnonProperties = new Vector();
		m_vecAnonProperties.addElement(new AnonProperty(myResources.getString(MSG_JAVA_VM), System.getProperty("java.vendor") + " " + System.getProperty("java.version")
				, AnonProperty.RATING_OKISH));
		m_vecAnonProperties.addElement(new AnonProperty(myResources.getString(MSG_OS), System.getProperty("os.name") + " " + 
				System.getProperty("os.arch") + " Version " + System.getProperty("os.version"), AnonProperty.RATING_OKISH));
		
		m_vecAnonProperties.addElement(new AnonProperty(myResources.getString(MSG_LANG), 
				Locale.getDefault().getDisplayLanguage(myResources.getLocale()) + ", " + 
				Locale.getDefault().getDisplayCountry(myResources.getLocale()), AnonProperty.RATING_OKISH));
		
		int iScreens = 1;
		try
		{
			iScreens = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;
		}
		catch (Exception a_e)
		{
			a_e.printStackTrace();
		}
		
		int iPixels = 0;
		try
		{
			iPixels = Toolkit.getDefaultToolkit().getColorModel().getPixelSize();
		}
		catch (Exception a_e)
		{
			a_e.printStackTrace();
		}
		
		m_vecAnonProperties.addElement(new AnonProperty(myResources.getString(MSG_SCREEN_RESOLUTION), 
				Toolkit.getDefaultToolkit().getScreenSize().width + " x " +
				Toolkit.getDefaultToolkit().getScreenSize().height + 
				", " + Toolkit.getDefaultToolkit().getScreenResolution() + " DPI" +
				(iPixels > 0 ? ", " + iPixels + " bit" : "") + 
				(iScreens > 1 ? ", " + iScreens + " " + myResources.getString(MSG_SCREENS) : ""), AnonProperty.RATING_OKISH));
		

		
// Toolkit.getDefaultToolkit().getScreenInsets(gc)
		
		
		/*addSystemProperty(table, "browser", AnonProperty.RATING_OKISH);
		addSystemProperty(table, "browser.vendor", AnonProperty.RATING_OKISH);
		addSystemProperty(table, "browser.version", AnonProperty.RATING_OKISH);*/
		
		//m_vecAnonProperties.addElement(new AnonProperty(myResources.getString(MSG_LANG), Locale.getDefault().getDisplayLanguage(myResources.getLocale()), AnonProperty.RATING_OKISH));
		//m_vecAnonProperties.addElement(new AnonProperty(myResources.getString(MSG_COUNTRY), Locale.getDefault().getDisplayCountry(myResources.getLocale()), AnonProperty.RATING_OKISH));
		//addSystemProperty(m_vecAnonProperties, "user.language", AnonProperty.RATING_OKISH);	
		addSystemProperty(m_vecAnonProperties, "java.home", AnonProperty.RATING_BAD);
		addSystemProperty(m_vecAnonProperties, "user.dir", AnonProperty.RATING_BAD);
		addSystemProperty(m_vecAnonProperties, "user.home", AnonProperty.RATING_BAD);
		addSystemProperty(m_vecAnonProperties, "user.name", AnonProperty.RATING_BAD);
		



		

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
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(0, 0, 0, 0);

		AnonPropertyTable tableTop;
		AnonPropertyTable tableBottom;
		AnonProperty anonProperty;
		AnonProperty anonPropertyNetwork;
		int iTopLines = 0;
		int iRating;
		
		if (m_vecInterfaces.size() == 0)
		{
			iRating = AnonProperty.RATING_NONE;
		}
		else if (m_vecInterfaces.size() <= 2)
		{
			iRating = AnonProperty.RATING_OKISH;
		}
		else
		{
			iRating = AnonProperty.RATING_BAD;
		}
		anonPropertyNetwork = new AnonProperty(myResources.getString(MSG_NETWORK_INTERFACE) + " (" + m_vecInterfaces.size() + ")", "", iRating);
		if (m_vecAnonProperties.size() == 0)
		{
			tableTop = new AnonPropertyTable(this, width_column_one + width_column_two - width_column_three, m_fontSize, m_iRowHeight);
			tableTop.add(anonPropertyNetwork);
			anonPropertyNetwork = null;
		}
		else
		{
			tableTop = new AnonPropertyTable(this, true, width_column_one, width_column_two - width_column_three, m_fontSize, m_iRowHeight);
		}
		tableBottom = new AnonPropertyTable(this, true, width_column_one, width_column_two, m_fontSize, m_iRowHeight);
		
		while (m_vecAnonProperties.size() > 0)
		{
			anonProperty = (AnonProperty)m_vecAnonProperties.lastElement();
			m_vecAnonProperties.removeElement(anonProperty);
			
			if (iTopLines < 2)
			{
				tableTop.add(anonProperty);
			}
			else
			{
				tableBottom.add(anonProperty);
			}
			iTopLines++;
		}	
		
		
		mtailsPanel.add(tableTop, c);
		m_btnBack = addSwitchButton(c, mtailsPanel, MSG_BACK);
		c.gridy++;
		if (m_vecInterfaces.size() == 0)
		{
			c.weighty = 1.0;
		}
		mtailsPanel.add(tableBottom, c);

		if (m_vecInterfaces.size() > 0)
		{
			//tableBottom = new AnonPropertyTable(this, true, width_column_one, width_column_two, m_fontSize);
			//tableBottom.add(new AnonProperty(myResources.getString(MSG_IP_ADDRESS), myResources.getString(MSG_NETWORK_INTERFACE), AnonProperty.RATING_NONE));
			tableBottom = new AnonPropertyTable(this, width_column_one + width_column_two, m_fontSize, m_iRowHeight);
			if (anonPropertyNetwork != null)
			{
				tableBottom.add(anonPropertyNetwork);
			}
	
			for(int i = 0; i < m_vecInterfaces.size(); i++)
			{
				String name = ((Object[]) m_vecInterfaces.elementAt(i))[0].toString();
				String addr = ((Object[]) m_vecInterfaces.elementAt(i))[1].toString();
				//tableBottom.add(new AnonProperty(addr, name, AnonProperty.RATING_NONE));
				tableBottom.add(new AnonProperty(name, "", AnonProperty.RATING_NONE));
			}
	
			Font[] systemFonts = null;
			try
			{
				systemFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
			}
			catch (Exception a_e)
			{
				a_e.printStackTrace();
			}
			
			if (systemFonts != null && systemFonts.length > 0)
			{
				tableBottom.add(new AnonProperty(myResources.getString(MSG_FONTS) + " (" + systemFonts.length +  ")", "", AnonProperty.RATING_BAD));
				
				for (int i = 0; i < systemFonts.length; i++)
				{
					tableBottom.add(new AnonProperty(systemFonts[i].getFontName(), "", AnonProperty.RATING_NONE));
				}
			}
			
			c.gridy++;
			c.weighty = 1.0;
			mtailsPanel.add(tableBottom, c);
		}
	}

	private JButton addSwitchButton(GridBagConstraints c, JPanel a_panel, String a_strButtonMessage)
	{
		JButton button;
		c.fill = GridBagConstraints.NONE;
		c.gridx++;
		button = new JButton(myResources.getString(a_strButtonMessage));
		button.addActionListener(this);
		a_panel.add(button, c);
		c.weightx = 1.0;
		c.gridx = 0;
		c.gridwidth = 2;
		return button;
	}
	
	private void createStartPanel()
	{
		m_startPanel = new JPanel(new GridBagLayout());
		m_startPanel.setBackground(Color.WHITE);

		GridBagConstraints c = new GridBagConstraints();
		boolean bEndButton = false;
		AnonProperty anonProperty;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridy = 0;
		c.gridx = 0;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.insets = new Insets(0, 0, 0, 0);
		
		
		//c.fill = GridBagConstraints.HORIZONTAL;
		
		AnonPropertyTable tableTop = new AnonPropertyTable(this, true, width_column_one, width_column_two - width_column_three, m_fontSize, m_iRowHeight);
		AnonPropertyTable tableBottom = new AnonPropertyTable(this, true, width_column_one, width_column_two, m_fontSize, m_iRowHeight);
		AnonProperty systemProperty = null;
		int iCountDetails = 0;
	
		//m_vecExternalIPs.clear();
		//m_vecInternalIPs.clear();
		//m_vecInterfaces.clear();
		//m_vecAnonProperties.clear();
		
		try
		{//if (1==1)throw new Exception("");
			// this will cause a security exception on untrusted applets
			System.getProperties();
			systemProperty = new AnonProperty(myResources.getString(MSG_TRUSTED_APPLET), System.getProperty("user.name") + ", " + myResources.getString(MSG_TRUSTED_APPLET_DESC), AnonProperty.RATING_BAD);
			iCountDetails++;
		}
		catch(Exception ex)
		{
			// ignore
		}
		
		if (iCountDetails + (m_externalIP != null?1:0) + (m_internalIP!= null?1:0) + m_vecAnonProperties.size() <= 4 &&
				m_vecInterfaces.size() <= 1)
		{
			c.weightx = 1.0;
			tableTop = tableBottom;
			m_btnSwitch = new JButton();
			bEndButton = true;
		}
		else
		{
			m_startPanel.add(tableTop, c);
		}

		if (systemProperty != null)
		{
			tableTop.add(systemProperty);
			if (m_btnSwitch == null)
			{
				m_btnSwitch = addSwitchButton(c, m_startPanel, MSG_DETAILS);
			}
		}

		
		
		if (m_externalIP != null)
		{
			String ip = m_externalIP.ip.getHostAddress();
			int rating = AnonProperty.RATING_BAD;
			String strText = myResources.getString(MSG_YOUR_IP);
			if (m_externalIP.strProxy != null)
			{
				ip += " (" + m_externalIP.strProxy + ")";
				if (m_externalIP.strProxy.equalsIgnoreCase("Tor"))
				{
					rating = AnonProperty.RATING_GOOD;
				}
				else if (m_externalIP.strProxy.equalsIgnoreCase("JonDonym"))
				{
					if (m_externalIP.distribution > 1)
					{
						rating = AnonProperty.RATING_GOOD;
					}
					else
					{
						rating = AnonProperty.RATING_OKISH;
					}
				}
			}
			else if (isSiteLocalAddress(m_externalIP.ip) > 0)
			{
				rating = AnonProperty.RATING_OKISH;
				strText = myResources.getString(MSG_INTERNAL_IP);
			}
		 
			if (m_urlProxifierSettingsPage == null || rating != AnonProperty.RATING_BAD)
			{
				anonProperty = new AnonProperty(strText, ip, rating);
			}
			else
			{	
				anonProperty = new AnonProperty(strText, ip, ip + " " + myResources.getString(MSG_FIX_PROBLEM), m_urlProxifierSettingsPage.toString(), rating);
			}
			iCountDetails++;
			bEndButton = addSwitchButton(tableTop, tableBottom, anonProperty, bEndButton, c);

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
		//c.gridy++;
		

		if (m_internalIP != null)
		{
			int rating = AnonProperty.RATING_OKISH;
			String strText = myResources.getString(MSG_INTERNAL_IP); 
			if (isSiteLocalAddress(m_internalIP.ip) == 0)
			{
				// This seems to be an external address!
				rating = AnonProperty.RATING_BAD;
				strText = myResources.getString(MSG_YOUR_IP);
			}
			iCountDetails++;
			
			if (m_externalIP != null || m_urlProxifierSettingsPage == null) // || rating != AnonProperty.RATING_BAD)
			{
				anonProperty = new AnonProperty(strText, m_internalIP.ip.getHostAddress(), rating);
			}
			else
			{	
				anonProperty = new AnonProperty(strText, m_internalIP.ip.getHostAddress(), 
						m_internalIP.ip.getHostAddress() + " " + myResources.getString(MSG_FIX_PROBLEM), m_urlProxifierSettingsPage.toString(), rating);
			}
			
			bEndButton = addSwitchButton(tableTop, tableBottom, anonProperty, bEndButton, c);
		}
		

		// tableBottom = new AnonPropertyTable(this, true, width_column_one, width_column_two, m_fontSize);
		while (iCountDetails < 4 && m_vecAnonProperties.size() > 0)
		{
			iCountDetails++;
			anonProperty = (AnonProperty)m_vecAnonProperties.firstElement();
			m_vecAnonProperties.removeElement(anonProperty);
			bEndButton = addSwitchButton(tableTop, tableBottom, anonProperty, bEndButton, c);
		}

		c.gridy++;
		m_startPanel.add(tableBottom, c);

		
		c.gridy++;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.VERTICAL;
		m_startPanel.add(new JLabel(), c);
	}
	
	private boolean addSwitchButton(AnonPropertyTable tableTop, AnonPropertyTable tableBottom, 
			AnonProperty anonProperty, boolean bEndButton, GridBagConstraints c)
	{
		if (m_btnSwitch == null)
		{
			m_btnSwitch = addSwitchButton(c, m_startPanel, MSG_DETAILS);
			tableTop.add(anonProperty);
			return false;
		}
		else if (!bEndButton)
		{
			tableTop.add(anonProperty);
			return true;
		}
		else
		{
			tableBottom.add(anonProperty);
			return true;
		}
	}
	
	private class IPInfo
	{
		String strProxy;
		int distribution = 0;
		InetAddress ip;
		String strBase64;
	}
}
