/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)CircularBuffer.java      1.21 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.media.sound;

//$$fb todo: real synchronization, i.e. multiple reads, but write exclusive
//$$fb todo: shouldn't use a sample counter that keeps on counting

/**
 * Represents a circular buffer.  Also handles sign and byte order coversions.
 *
 * @version 1.21, 03/01/23
 * @author Kara Kytle
 */
class CircularBuffer {

    boolean convertSign = false;
    boolean convertByteOrder = false;

    private byte[] array;
    // ay: Upgraded to longs because int was overflowing after a few
    //     hours and throwing OutOfMemoryError !
    private long bytesWritten = 0;
    private long bytesRead = 0;
    private long end = -1;


    /**
     * Creates a circular buffer with the indicated capacity in bytes.
     */
    CircularBuffer(int size, boolean convertSign, boolean convertByteOrder) {

        array = new byte[size];
        this.convertSign = convertSign;
        this.convertByteOrder = convertByteOrder;
    }

    /**
     * Writes len bytes to the circular buffer.
     * Does *not* block.  Returns the number of
     * bytes actually written.
     */
    synchronized int write(byte[] b, int off, int len) {

        if (Printer.verbose) Printer.verbose("CircularBuffer: write: b: " + b + " off: " + off + " len: " + len);

        // check for end of stream
        if (end >= 0 || len == 0) {
            return 0;
        }

        // this is how many bytes we can write to the circular buffer.
        int totalBytesToWrite = Math.min(len, bytesAvailableToWrite());

        // this is how many bytes we have left to write.
        int bytesRemainingToWrite = totalBytesToWrite;

        // this is the number of bytes to write right now
        int bytesToWrite;

        while (bytesRemainingToWrite > 0) {

            bytesToWrite = bytesRemainingToWrite;
            int writeIndex = getWriteIndex();

            // write up to the end of the circular buffer
            bytesToWrite = Math.min(bytesToWrite, (array.length - writeIndex));

            System.arraycopy(b, off, array, writeIndex, bytesToWrite);

            //$$fb fix the bug that the array passed to SourceDataLine.write() gets modified
            //     if the data has to be converted for the native format
            // do any sign and byte order conversions
            if (convertSign) {
                Toolkit.getUnsigned8(array, writeIndex, bytesToWrite);
            } else if (convertByteOrder) {
                Toolkit.getByteSwapped(array, writeIndex, bytesToWrite - (bytesToWrite % 2));
            }

            bytesWritten += bytesToWrite;
            bytesRemainingToWrite -= bytesToWrite;
            off += bytesToWrite;
            if (bytesToWrite == 0) {
                Thread.yield();
            }
        }

        if (Printer.verbose) Printer.verbose("CircularBuffer: write: returning: " + totalBytesToWrite);
        return totalBytesToWrite;
    }


    /**
     * Reads len bytes from the circular buffer.
     * Does *not* block.  Returns the number of
     * bytes actually read.
     */
    synchronized int read(byte[] b, int off, int len) {

        if (Printer.verbose) Printer.verbose("CircularBuffer: read: b: " + b + " off: " + off + " len: " + len);

        // check for end of stream
        if (end >= 0) {
            if (bytesRead >= bytesWritten) {
                return -1;
            }
        }

        // this is how many bytes we can read from the circular buffer.
        int totalBytesToRead = Math.min(len, bytesAvailableToRead());

        // this is how many bytes we have left to read.
        int bytesRemainingToRead = totalBytesToRead;

        // this is the number of bytes to read right now
        int bytesToRead;

        while (bytesRemainingToRead > 0) {

            bytesToRead = bytesRemainingToRead;

            // read up to the end of the circular buffer
            bytesToRead = Math.min(bytesToRead, (array.length - getReadIndex()));

            System.arraycopy(array, getReadIndex(), b, off, bytesToRead);

            bytesRead += bytesToRead;

            bytesRemainingToRead -= bytesToRead;
            off += bytesToRead;
            if (bytesToRead == 0) {
                Thread.yield();
            }
        }

        if (Printer.verbose) Printer.verbose("CircularBuffer: read: returning: " + totalBytesToRead);
        return totalBytesToRead;
    }


    /**
     * This will write the current data over old data if the amount of new data
     * exceeds the amount of available space in the circular buffer.  It also
     * handles the case where the amount of data to be written exceeds the total
     * length of the buffer, though this generally is *bad*.
     * The number of bytes written can be thought of as len, always, because this
     * method always writes data to the end of the requested dataset, even if it
     * must dump earlier data to do so.
     *
     * Returns the number of data bytes dumped in the process (*not* the number of
     * bytes written).
     *
     * $$kk: 08.04.99: we can dump data here.  should have overflow event / exception /
     * notification mechanism?
     */
    synchronized int writeover(byte[] b, int off, int len) {

        int bytesDumped = 0;
        int totalLen = getByteLength();

        // if the len requested is greater than the total buffer size, adjust
        // the offset and flush the circular buffer; we'll just write the last
        // totalLen bytes.

        if (len > totalLen) {
            bytesDumped = len - totalLen;
            off += bytesDumped;
            len = totalLen;
            flush();

        } else if (len > bytesAvailableToWrite()) {

            // if the len requested is greater than the number of bytes available
            // for writing, skip the difference.

            bytesDumped = len - bytesAvailableToWrite();
            skip(bytesDumped);
        }

        // now write the data into the circular buffer
        write(b, off, len);

        // return the number of bytes dumped
        return bytesDumped;
    }




    /**
     * Clear the buffer by marking everything as read.
     */
    synchronized void flush() {

        bytesRead = bytesWritten;
    }


    /*
     * Drain the buffer
     */
    void drain() {

        long targetBytesWritten = bytesWritten;

        if (Printer.debug) Printer.debug("CircularBuffer.drain(): bytesWritten="+bytesWritten
                                         +" bytesRead="+bytesRead
                                         +" bytesAvailableToRead()="+bytesAvailableToRead()
                                         +" getByteLength()="+getByteLength());

        //$$fb 2001-10-09: add a counter so that it does not block forever
        // part of fix for bug #4517739: Using JMF to playback sound clips blocks virtual machine
        int maxIterations=2000/5; // 2 seconds
        while (bytesRead < targetBytesWritten && (maxIterations--)>0) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
            }
        }
    }


    /**
     * Skips len bytes.  Return the number of bytes actually skipped.
     */
    synchronized int skip(int len) {

        // this is how many bytes we can read from the circular buffer.
        int bytesToSkip = Math.min(len, bytesAvailableToRead());
        bytesRead += bytesToSkip;
        return bytesToSkip;
    }


    /**
     * Mark the end of the circular buffer.  After all bytes written
     * at this time have been read, the circular buffer returns -1 for
     * all read calls.
     */
    synchronized void markEnd() {

        end = bytesWritten;
    }



    /**
     * Obtains the number of bytes in the buffer available for reading.
     */
    /*private*/ int bytesAvailableToRead() {

        return (int) (bytesWritten - bytesRead);
    }


    /**
     * Obtains the number of bytes in the buffer available for writing.
     */
    /*private*/ int bytesAvailableToWrite() {

        return ( (end >= 0) ? 0 : (array.length - bytesAvailableToRead()) );
    }

    /**
     * Obtains the length in bytes of the circular buffer.
     */
    int getByteLength() {

        return (array.length);
    }


    private int getReadIndex() {

        return (int) (bytesRead % array.length);
    }


    private int getWriteIndex() {

        return (int) (bytesWritten % array.length);
    }
} // class CircularBuffer
