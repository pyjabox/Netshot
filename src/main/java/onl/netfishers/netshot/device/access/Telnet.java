/**
 * Copyright 2013-2014 Sylvain Cadilhac (NetFishers)
 */
package onl.netfishers.netshot.device.access;

import java.io.IOException;
import java.io.PrintStream;

import onl.netfishers.netshot.device.NetworkAddress;

import org.apache.commons.net.telnet.TelnetClient;

/**
 * A Telnet CLI access.
 */
public class Telnet extends Cli {

	/** The port. */
	private int port = 23;
	
	/** The telnet. */
	private TelnetClient telnet = null;
	
	/**
	 * Instantiates a new telnet.
	 *
	 * @param host the host
	 */
	public Telnet(NetworkAddress host) {
		super(host);
	}
	
	/**
	 * Instantiates a new telnet.
	 *
	 * @param host the host
	 * @param port the port
	 */
	public Telnet(NetworkAddress host, int port) {
		this(host);
		this.port = port;
	}
	
	/* (non-Javadoc)
	 * @see onl.netfishers.netshot.device.access.Cli#connect()
	 */
	@Override
	public void connect() throws IOException {
		this.telnet = new TelnetClient("VT100");
		telnet.setConnectTimeout(this.connectionTimeout);
		telnet.connect(this.host.getInetAddress(), this.port);
		telnet.setSoTimeout(this.receiveTimeout);
		this.inStream = telnet.getInputStream();
		this.outStream = new PrintStream(telnet.getOutputStream());
	}

	/* (non-Javadoc)
	 * @see onl.netfishers.netshot.device.access.Cli#disconnect()
	 */
	@Override
	public void disconnect() {
		try {
			this.telnet.disconnect();
		} catch (Exception e) {
		}
	}
	
	
}
