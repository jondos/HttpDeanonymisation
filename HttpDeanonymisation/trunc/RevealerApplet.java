//package de.files.anontest;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;


public class RevealerApplet extends Applet {
	private String str_message = "Java ist aktiviert!";
	public String str_ext_ip = "";
	public String str_int_ip = "";
	private String targetHTML = "/de/files/anontest/onlyip.php";
	private int width = 2000;
	private int height = 100;

	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
	}

	public void init() {
		//super.init();
		//if(this.getParameter("TARGET") != null){
		//	targetHTML = this.getParameter("Target");
		//}
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
			int port = 443; //this.getDocumentBase().getPort();
			//if(port == -1) port = 80;
			//Socket sock = new Socket(host, port);
			SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
			SSLSocket sock = (SSLSocket) factory.createSocket(host, port);
			str_int_ip += sock.getLocalAddress().getHostAddress();
			//str_int_ip += InetAddress.isSiteLocalAddress();
			BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
			String getRequest = "GET https://"+ host + targetHTML +"\n\n\n";
			writer.write(getRequest);
			writer.flush();
			String line;
			while((line=reader.readLine()) != null){
				str_ext_ip += line;
			}
			if((str_int_ip == "127.0.0.1") || (str_int_ip == str_ext_ip)) str_int_ip="";
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
		if(str_ext_ip != "") g.drawString("Ihre externe IP ist: "+str_ext_ip, 20, 50);
		if(str_int_ip != "") g.drawString("Ihre interne IP ist: "+str_int_ip, 20, 80);
		
	}
}
