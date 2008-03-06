package de.files.anontest;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class RevealerApplet extends Applet {
	private String str_message = "Java ist aktiviert!";
	private String str_ext_ip = "";
	private String str_int_ip = "Ihre interne IP ist: ";
	private String targetHTML = "onlyip.php";
	private int width = 2000;
	private int height = 100;

	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}

	public void init() {
		super.init();
		if(this.getParameter("TARGET") != null){
			targetHTML = this.getParameter("Target");
		}
		super.init();
		if(this.getParameter("WIDTH") != null){
			try{
				width = Integer.parseInt(this.getParameter("WIDTH"));
			}catch(NumberFormatException e){}
		}
		
		if(this.getParameter("HEIGHT") != null){
			try{
				height = Integer.parseInt(this.getParameter("HEIGHT"));
			}catch(NumberFormatException e){}
		}
		
		try{
			String host = this.getDocumentBase().getHost();
			int port = this.getDocumentBase().getPort();
			if(port == -1) port = 80;
			Socket sock = new Socket(host, port);
			str_int_ip += sock.getLocalAddress().getHostAddress();
			BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			String getRequest = "GET " + targetHTML +"\n\n\n";
			writer.write(getRequest);
			writer.flush();
			String line;
			while((line=reader.readLine()) != null){
				str_ext_ip += line;
			}
			repaint();
		}catch(Exception e){
			str_ext_ip = "ERROR:" +e;
		}
	}

	public void start() {
		// TODO Auto-generated method stub
		super.start();
	}

	public void stop() {
		// TODO Auto-generated method stub
		super.stop();
	}
	
	public void paint(Graphics g){
		g.setColor(Color.black);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.white);
		g.drawString(str_message, 0, 20);
		g.drawString(str_ext_ip, 20, 50);
		g.drawString(str_int_ip, 20, 80);
		
	}
}
