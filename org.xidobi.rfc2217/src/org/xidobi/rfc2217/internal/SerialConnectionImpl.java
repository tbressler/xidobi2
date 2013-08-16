/*
 * Copyright Gemtec GmbH 2009-2013
 *
 * Erstellt am: 16.08.2013 10:59:58
 * Erstellt von: Christian Schwarz 
 */
package org.xidobi.rfc2217.internal;

import javax.annotation.Nonnull;

import org.apache.commons.net.telnet.TelnetClient;
import org.xidobi.SerialPort;
import org.xidobi.rfc2217.Rfc2217SerialPort;
import org.xidobi.spi.BasicSerialConnection;
import org.xidobi.spi.Reader;
import org.xidobi.spi.Writer;

/**
 * @author Christian Schwarz
 *
 */
public class SerialConnectionImpl extends BasicSerialConnection {

	/**
	 * @param parent the serial port, must not be <code>null</code>
	 * @param reader
	 * @param writer
	 */
	protected SerialConnectionImpl(	@Nonnull Rfc2217SerialPort parent,
									@Nonnull TelnetClient telnetClient) {
		super(parent, null, null);
	}

	
}