/*
 * Copyright 2013 Gemtec GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.xidobi.spi;

import static org.xidobi.spi.Preconditions.checkArgumentNotNull;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.xidobi.SerialConnection;
import org.xidobi.SerialPort;

/**
 * A basic implementation of the {@link SerialConnection} to provide synchonisation and proper
 * behaviour when the port is closed.
 * 
 * @author Christian Schwarz
 */
public class BasicSerialConnection implements SerialConnection {

	/** The Handle of this port contains e.g. the name. */
	@Nonnull
	private final SerialPort portHandle;

	/**
	 * <ul>
	 * <li> <code>true</code> if this port is closed, {@link #close()} was called
	 * <li> <code>false</code> if this port is open
	 * </ul>
	 */
	private volatile boolean isClosed;

	/** Ensures that {@link #writeInternal(byte[])} can only called by one thread at a time. */
	private final Lock writeLock = new ReentrantLock();
	/** Ensures that {@link #readInternal()} can only called by one thread at a time. */
	private final Lock readLock = new ReentrantLock();

	/** read operation, never <code>null</code> */
	@Nonnull
	private final Reader reader;
	/** write operation, never <code>null</code> */
	@Nonnull
	private final Writer writer;

	/**
	 * Creates a new instance with the {@link SerialPort}.
	 * 
	 * @param portHandle
	 *            must not be <code>null</code>
	 * @param reader
	 *            read operation, must not be <code>null</code>
	 * @param writer
	 *            write operation, must not be <code>null</code>
	 * @exception IllegalArgumentException
	 *                if {@code portHandle==null}
	 */
	protected BasicSerialConnection(@Nonnull SerialPort portHandle,
									@Nonnull Reader reader,
									@Nonnull Writer writer) {

		this.portHandle = checkArgumentNotNull(portHandle, "portHandle");
		this.reader = checkArgumentNotNull(reader, "reader");
		this.writer = checkArgumentNotNull(writer, "writer");
	}

	/**
	 * The implementation must write the given {@code byte[]} to the port.
	 * <p>
	 * This method will be called by {@link #write(byte[])}, if following conditions apply:
	 * <ul>
	 * <li>the port is open
	 * <li>{@code data != null}.
	 * </ul>
	 * <b>IMPORTANT:</b> Dont call this method yourself! Otherwise there is no guaratee that the
	 * port is currently open and data is not <code>null</code>!
	 * 
	 * @param data
	 *            never <code>null</code>
	 * @throws IOException
	 *             when the write operation timed out or the serial port is not open
	 */

	/**
	 * The implementation must block until at least one byte can be returned or and
	 * {@link IOException} is thrown.
	 * <p>
	 * This method will be called by {@link #read()} only if the port is open.
	 * <p>
	 * <b>IMPORTANT:</b> Dont call this method yourself! Otherwise there is no guaratee that the
	 * port is currently open!
	 * 
	 * @return the byte's read from the port, never <code>null</code>
	 */

	/**
	 * The implementation must release all native resources.
	 * <p>
	 * This method will be called by {@link #close()} as long as this method returns with not normal
	 * / throws an {@link IOException}.
	 * <p>
	 * <b>IMPORTANT:</b> Dont call this method yourself! Otherwise there is no guaratee that the
	 * port is currently open!
	 */

	/** {@inheritDoc} */
	public final void write(@Nonnull byte[] data) throws IOException {
		checkArgumentNotNull(data, "data");
		ensurePortIsOpen();
		writeLock.lock();
		try {
			writer.write(data);
		}
		catch (IOException e) {
			close();
			throw e;
		}
		finally {
			writeLock.unlock();
		}
	}

	/** {@inheritDoc} */
	@Nonnull
	public final byte[] read() throws IOException {
		ensurePortIsOpen();
		readLock.lock();
		try {
			return reader.read();
		}
		catch (IOException e) {
			close();
			throw e;
		}
		finally {
			readLock.unlock();
		}
	}

	/** {@inheritDoc} */
	public final void close() throws IOException {
		if (isClosed)
			return;
		try {
			reader.close();
		}
		finally {
			writer.close();
		}
		isClosed = true;
	}

	/** {@inheritDoc} */
	public final boolean isClosed() {
		return isClosed;
	}

	/**
	 * Throw an {@link IOException} if this port is closed.
	 * 
	 * @throws IOException
	 *             if this port is closed
	 */
	private void ensurePortIsOpen() throws IOException {
		if (isClosed)
			throw portClosedException();
	}

	/**
	 * Returns a new {@link IOException} indicating that the port is closed. Subclasses may use this
	 * to throw a consitent {@link IOException}, if a closed port was detected.
	 * <p>
	 * <b>NOTE:</b> This method is also used by {@link #read()} and {@link #write(byte[])} to throw
	 * an {@link IOException} if the port is closed. Overriding it may have consequences to the
	 * caller.
	 */
	@Nonnull
	protected IOException portClosedException() {
		return portClosedException(null);
	}

	/**
	 * Returns a new {@link IOException} indicating that the port is closed. Subclasses may use this
	 * to throw a consitent {@link IOException}, if a closed port was detected.
	 * <p>
	 * <b>NOTE:</b> This method is also used by {@link #read()} and {@link #write(byte[])} to throw
	 * an {@link IOException} if the port is closed. Overriding it may have consequences to the
	 * caller.
	 * 
	 * @param message
	 *            error description may be <code>null</code>
	 */
	@Nonnull
	protected IOException portClosedException(@Nullable String message) {
		if (message == null)
			message = "";
		else
			message = " " + message;
		return new IOException("Port " + portHandle.getPortName() + " is closed!" + message);
	}
}
