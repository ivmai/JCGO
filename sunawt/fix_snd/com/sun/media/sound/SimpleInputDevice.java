/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)SimpleInputDevice.java   1.48 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.media.sound;

import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioPermission;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.TargetDataLine;


/**
 * Simple input device.  Contains mixer and data line functionality for
 * simple wave-in input devices.
 *
 * @version 1.48, 03/01/23
 * @author Kara Kytle
 */
class SimpleInputDevice extends AbstractMixer {


    // INSTANCE VARIABLES
    private AudioFormat format = null;
    private int bufferSize = AudioSystem.NOT_SPECIFIED;

    /**
     * The default buffer size, if none of AudioSystem.NOT_SPECIFIED is
     * specified as buffer size. The default is a half second
     */
    private static final int DEFAULT_BUFFER_TIME = 500; // milliseconds

    // INSTANCE PROPERTIES

    /**
     * Security Manager;
     */
    private JSSecurity jsSecurity;

    /**
     * Fixed set of ports available on the input device.
     */
    private Port[] ports;

    /**
     * true after we call the native stream start
     */
    private boolean implStarted = false;


    // CONSTRUCTOR


    SimpleInputDevice(SimpleInputDeviceProvider.InputDeviceInfo inputDeviceInfo) {

        // pass in Line.Info, mixer, controls
        super(inputDeviceInfo,            // Mixer.Info
              null,                       // Control[]
              null,                       // Line.Info[] sourceLineInfo
              null);                      // Line.Info[] targetLineInfo

        if (Printer.trace) Printer.trace(">> SimpleInputDevice: constructor");

        // first get the Security Manger
        jsSecurity = JSSecurityManager.getJSSecurity();

        // initialize platform-specific values, and load the native library
        Platform.initialize();

        // query our target line format capabilities
        Vector formats = new Vector();
        nGetFormats( ((SimpleInputDeviceProvider.InputDeviceInfo)getMixerInfo()).getIndex(),
                     formats,
                     AudioFormat.Encoding.PCM_SIGNED,
                     AudioFormat.Encoding.PCM_UNSIGNED,
                     AudioFormat.Encoding.ULAW,
                     AudioFormat.Encoding.ALAW);

        // $$kk: 06.03.99: this is stupid; should store these as an array!!
        AudioFormat[] formatArray;
        synchronized(formats) {
            formatArray = new AudioFormat[formats.size()];
            for (int i = 0; i < formatArray.length; i++) {
                formatArray[i] = (AudioFormat)formats.elementAt(i);
            }
        }


        // $$kk: 05.31.99: need to figure this out!
        // set up the port info objects

        int numPorts = nGetNumPorts();
        sourceLineInfo = new Port.Info[numPorts];
        ports = new Port[numPorts];
        String name;

        for (int i = 0; i < numPorts; i++) {

            name = nGetPortName(i);
            sourceLineInfo[i] = getPortInfo(name);

            ports[i] = new InputDevicePort((Port.Info)sourceLineInfo[i], this, new Control[0]);
        }


        // set up the target line objects

        targetLineInfo = new DataLine.Info[1];

        targetLineInfo[0] = new DataLine.Info(TargetDataLine.class,
                                              formatArray,
                                              0,
                                              AudioSystem.NOT_SPECIFIED);


        // set the initial format as the best supported one

        // $$jb: 12.07.99: Fix for 4297115: NoSuchElementException thrown on
        // machines without capture devices.  Make sure that the formats
        // vector has elements before we set the initial format.

        if( formats.size() > 0 ) {
            format = (AudioFormat)formats.lastElement();
        } else {
            format = null;
        }

        if (Printer.trace) Printer.trace("<< SimpleInputDevice: constructor completed");
    }



    // ABSTRACT MIXER: ABSTRACT METHOD IMPLEMENTATIONS

    public Line getLine(Line.Info info) throws LineUnavailableException {

        Line.Info fullInfo = getLineInfo(info);

        if (fullInfo == null) {
            throw new IllegalArgumentException("Line unsupported: " + info);
        }

        if (fullInfo instanceof Port.Info) {

            for (int i = 0; i < ports.length; i++) {
                if (fullInfo.equals(ports[i].getLineInfo())) {
                    return ports[i];
                }
            }
        }

        if (fullInfo instanceof DataLine.Info) {

            DataLine.Info dataLineInfo = (DataLine.Info)fullInfo;
            AudioFormat[] dataLineInfoFormats = dataLineInfo.getFormats();

            if (dataLineInfoFormats.length == 0) {
                throw new IllegalArgumentException("Line unsupported: " + info);
            }
            if (dataLineInfo.getLineClass().isAssignableFrom(InputDeviceDataLine.class)) {

                int reqBufferSize = AudioSystem.NOT_SPECIFIED;
                AudioFormat reqFormat = null;

                // look for a requested format and buffer size
                if (info instanceof DataLine.Info) {

                    // look for a requested format

                    AudioFormat[] sFormats = ((DataLine.Info)info).getFormats();

                    if( (sFormats != null) && (sFormats.length > 0) ) {
                        reqFormat = sFormats[sFormats.length-1];
                    }


                    // look for a requested buffer size

                    reqBufferSize = ((DataLine.Info)info).getMaxBufferSize();

                    if (reqBufferSize == AudioSystem.NOT_SPECIFIED) {
                        reqBufferSize = ((DataLine.Info)info).getMinBufferSize();
                    }

                    if (reqBufferSize <= 0) {
                        reqBufferSize = AudioSystem.NOT_SPECIFIED;
                    }
                }


                // if no format requested, get the best supported format.
                if (reqFormat == null) {
                    reqFormat = dataLineInfoFormats[dataLineInfoFormats.length-1];
                }

                // if no buffer size requested, calculate one
                if (reqBufferSize == AudioSystem.NOT_SPECIFIED) {
                    // we are counting in bytes here
                    reqBufferSize = (int) Toolkit.millis2bytes(reqFormat, DEFAULT_BUFFER_TIME);
                }

                return new InputDeviceDataLine(dataLineInfo, this, reqFormat, reqBufferSize);
            }
        }

        throw new IllegalArgumentException("Line unsupported: " + info);
    }


    public int getMaxLines(Line.Info info) {

        Line.Info fullInfo = getLineInfo(info);


        // if it's not supported at all, return 0.
        if (fullInfo == null) {
            return 0;
        }


        // $$kk: 06.02.99: for ports, let's assume we can only have one capture
        // port open at a time.  the real situation may be more complicated.
        if (fullInfo instanceof Port.Info) {
            return 1;
        }


        // $$kk: 06.02.99: any number!
        if (fullInfo instanceof DataLine.Info) {
            return AudioSystem.NOT_SPECIFIED;
        }

        return 0;
    }


    public void implOpen() throws LineUnavailableException {
        // check for record permission
        if( jsSecurity != null ) {
            jsSecurity.checkRecordPermission();
        }

        if (Printer.trace) Printer.trace(">> SimpleInputDevice: implOpen");


        // if bufferSize is less than zero (e.g. UNSPECIFIED), set it to
        // 0, which will use the default buffer size in the native code.
        if( bufferSize < 0 ) {
            bufferSize = 0;
        }

        // $$kk: 08.04.99 trying this....  the java buffer is the largest;
        // data can be written to and read from it in pieces.  since read
        // and write block, this allows us to avoid a fast-or-famine buffer
        // lifestyle.
        int bufferSizeDivisor = 8;

        // translate the encoding into a magic number
        int encoding = PCM;
        if (format.getEncoding() == AudioFormat.Encoding.ULAW) {
            encoding = ULAW;
        } else if (format.getEncoding() == AudioFormat.Encoding.ALAW) {
            encoding = ALAW;
        }

        // open the capture device
        // note that this method takes the buffer size in frames
        nOpen(((SimpleInputDeviceProvider.InputDeviceInfo)getMixerInfo()).getIndex(),
              encoding,
              (int)format.getSampleRate(),
              format.getSampleSizeInBits(),
              format.getChannels(),
              (bufferSize / (format.getFrameSize() * bufferSizeDivisor))
              );

        // update the format and buffer size values.
        // (buffer size may differ from requested value.)
        //this.format = format;
        this.bufferSize = nGetBufferSizeInFrames() * format.getFrameSize() * bufferSizeDivisor;

        if (Printer.trace) Printer.trace("<< SimpleInputDevice: implOpen succeeded. Format="+format+"  buffersize="+bufferSize);
    }

    public void implClose() {

        // check for record permission
        if( jsSecurity != null ) {
            jsSecurity.checkRecordPermission();
        }

        if (Printer.trace) Printer.trace(">> SimpleInputDevice: implClose");

        // close the capture device
        nClose();
        implStarted = false;

        if (Printer.trace) Printer.trace("<< SimpleInputDevice: implClose succeeded");
    }


    // ABSTRACT DATA LINE: ABSTRACT METHOD IMPLEMENTATIONS

    /**
     * Start the input device
     */
    protected void implStart() {

        // check for record permission
        if( jsSecurity != null ) {
            jsSecurity.checkRecordPermission();
        }

        if (Printer.trace) Printer.trace(">> SimpleInputDevice: implStart");

        if (implStarted == false) {

            if (Printer.debug) Printer.debug("SimpleInputDevice: implStart: starting the device");
            nStart();
            implStarted = true;

        } else {

            if (Printer.debug) Printer.debug("SimpleInputDevice: implStart: resuming the device");
            nResume();
        }

        if (Printer.trace) Printer.trace("<< SimpleInputDevice: implStart succeeded");
    }


    /**
     * Stop the input device
     */
    protected void implStop() {

        if (Printer.trace) Printer.trace(">> SimpleInputDevice: implStop");

        // check for record permission
        if( jsSecurity != null ) {
            jsSecurity.checkRecordPermission();
        }

        if (Printer.trace) Printer.trace(">> SimpleInputDevice: implStop");

        nPause();

        if (Printer.trace) Printer.trace("<< SimpleInputDevice: implStop succeeded");
    }


    // METHOD OVERRIDES


    /**
     * This implementation of this method determines whether
     * this line is a source or target line, calls open(format, bufferSize)
     * on the mixer, and adds the line to the appropriate vector.  The mixer
     * must be opened at the *same* *format* as the line is requesting.
     */
    /*
      protected void open(DataLine line, AudioFormat format, int bufferSize) throws LineUnavailableException {

      if (Printer.trace) Printer.trace(">> AbstractMixer: open(line = " + line + ")");

      // $$kk: 06.11.99: ignore ourselves for now
      if (this.equals(line)) {
      return;
      }

      // source line?
      if (isSourceLine(line.getLineInfo())) {

      if (! sourceLines.contains(line) ) {

      // call the no-arg open method for the mixer; it should open at its
      // default format if it is not open yet
      open(format, bufferSize);

      // we opened successfully!  add the line to the list
      sourceLines.addElement(line);
      }
      } else {

      // target line?
      if(isTargetLine(line.getLineInfo())) {

      if (! targetLines.contains(line) ) {

      // call the no-arg open method for the mixer; it should open at its
      // default format if it is not open yet
      open(format, bufferSize);

      // we opened successfully!  add the line to the list
      targetLines.addElement(line);
      }
      } else {
      if (Printer.err) Printer.err("Unknown line received for AbstractMixer.open(Line): " + line);
      }
      }

      if (Printer.trace) Printer.trace("<< AbstractMixer: open(line, format, bufferSize) completed");
      }
    */

    // HELPER METHODS


    /**
     * Utility method for converting between String names and well-known
     * Port types.  I'm only doing the ones that can be sources.  We may
     * want to do something at least a little more general here....
     */
    private Port.Info getPortInfo(String name) {

        if (name.equals(Port.Info.MICROPHONE.toString())) {
            return Port.Info.MICROPHONE;
        }

        if (name.equals(Port.Info.LINE_IN.toString())) {
            return Port.Info.LINE_IN;
        }

        if (name.equals(Port.Info.COMPACT_DISC.toString())) {
            return Port.Info.COMPACT_DISC;
        }

        // return a new port info object.
        return (new InputDevicePortInfo(name));
    }



    // INPUT DEVICE METHODS


    // CALLBACKS

    private void callbackCaptureStreamDestroy() {

        if (Printer.debug) Printer.debug("SimpleInputDevice: callbackCaptureStreamDestroy");

        /* do we even need this callback? */
    }


    private void callbackStreamPutData(byte[] data, int lengthInFrames) {

        if (Printer.verbose) Printer.verbose(">> SimpleInputDevice: callbackStreamPutData: data: " + data + " data.length: " + data.length + " lengthInFrames: " + lengthInFrames);

        // write the captured data to the circular buffer for each open, running target data line
        int lengthInBytes = lengthInFrames * format.getFrameSize();
        InputDeviceDataLine targetLine = null;
        Vector lines = null;

        synchronized (targetLines) {
            if (targetLines.size() == 1) {
                // optimization for the most-often case
                targetLine = (InputDeviceDataLine)targetLines.elementAt(0);
            } else {
                // we need to clone the vector to prevent simultaneous locking
                // of targetLines and one of the lines itself - this caused deadlock
                lines = (Vector) targetLines.clone();
            }
        }
        if (targetLine != null) {
            targetLine.fillBuffer(data, lengthInBytes);
        } else {
            for (int i = 0; i < targetLines.size(); i++) {
                targetLine = (InputDeviceDataLine)targetLines.elementAt(i);
                targetLine.fillBuffer(data, lengthInBytes);
            }
        }

        if (Printer.verbose) Printer.verbose("<< SimpleInputDevice: callbackStreamPutData completed");
    }


    // INNER CLASSES

    /**
     * Private inner class representing the target data line for the SimpleInputDevice.
     */
    private static class InputDeviceDataLine extends AbstractDataLine implements TargetDataLine {

        private CircularBuffer circularBuffer = null;
        private SimpleInputDevice sid;


        // CONSTRUCTOR

        private InputDeviceDataLine(DataLine.Info info,
                                    SimpleInputDevice mixer,
                                    AudioFormat initialFormat,
                                    int initialBufferSize) {

            // $$kk: 06.02.99: need to deal with controls!
            super(info, mixer, null, initialFormat, initialBufferSize);

            if (Printer.trace) Printer.trace("InputDeviceDataLine CONSTRUCTOR: info: " + info + " initialFormat: " + initialFormat + " initialBufferSize: " + initialBufferSize);
            this.sid = mixer;
        }


        // TARGET DATA LINE METHODS

        public int read(byte[] b, int off, int len) {

            if (Printer.verbose) Printer.verbose("> InputDeviceDataLine.read(b.length: " + b.length + " off: " + off + " len: " + len);

            int totalBytesToRead = len;

            if (len % getFormat().getFrameSize() != 0) {
                throw new IllegalArgumentException("Illegal request to write non-integral number of frames (" + len + " bytes )");
            }

            int totalBytesRead = 0;
            int currentBytesRead = 0;

            // $$kk: 08.17.99: changed this to return if not running as well as if not open
            while (isOpen() && (totalBytesRead < totalBytesToRead)) {
                if (!isStartedRunning()) {
                    Thread.yield();
                    break;
                }

                currentBytesRead = circularBuffer.read(b, off, (totalBytesToRead - totalBytesRead));
                totalBytesRead += currentBytesRead;
                off += currentBytesRead;

                if (totalBytesRead < totalBytesToRead) {
                    try {
                        synchronized(this) {
                            // $$kk: 08.17.99: need to make sure we never block forever here!
                            wait();
                        }
                    } catch (InterruptedException e) {
                    }
                }
            }

            if (Printer.verbose) Printer.verbose("< InputDeviceDataLine.read returning: " + totalBytesRead);
            return totalBytesRead;
        }


        public int available() {
            return circularBuffer.bytesAvailableToRead();
        }


        // ABSTRACT METHOD IMPLEMENTATIONS

        // ABSTRACT LINE

        void implOpen(AudioFormat format, int bufferSize) throws LineUnavailableException {

            if (Printer.trace) Printer.trace(">> InputDeviceDataLine: implOpen");

            // check for record permission
            if( sid.jsSecurity != null ) {
                sid.jsSecurity.checkRecordPermission();
            }


            // set default buffer size, if not specified
            // fix for bug 4769277: REGRESSION:jck14a:api/javax_sound/sampled/Mixer/index.html#getTargetLines fails
            if (bufferSize < 0 || bufferSize == AudioSystem.NOT_SPECIFIED) {
                // we are counting in bytes here
                bufferSize = (int) Toolkit.millis2bytes(format, DEFAULT_BUFFER_TIME);
            }


            // reset local buffersize to the buffer size of the mixer
            // this is necessary in case the native code could not set the
            // buffer size to the requested buffer size
            // note: this method is called after mixer.implOpen was called!
            //bufferSize = sid.bufferSize;

            // only allow a new instance if the format is compatible with the already established format
            checkFormat(format);


            // figure out the signed/unsigned and big/little endian conversions

            boolean convertSign = false;
            boolean convertByteOrder = false;

            if ( (format.getSampleSizeInBits() <= 8) && ( (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) && (Platform.isSigned8() == false) ) ) {
                convertSign = true;
            } else if ( (format.getSampleSizeInBits() <= 8) && ( (format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) && (Platform.isSigned8() == true) ) ) {
                convertSign = true;
            }

            if ( (format.getSampleSizeInBits() > 8) && (format.isBigEndian() != Platform.isBigEndian()) ) {
                convertByteOrder = true;
            }


            // create the circular buffer
            circularBuffer = new CircularBuffer(bufferSize, convertSign, convertByteOrder);

            // set the bufferSize and format variables for the line
            this.bufferSize = bufferSize;
            this.format = format;

            if (Printer.trace) Printer.trace("<< InputDeviceDataLine: implOpen succeeded");
        }


        void implClose() {

            if (Printer.trace) Printer.trace(">> InputDeviceDataLine: implClose()");

            // check for record permission
            if( sid.jsSecurity != null ) {
                sid.jsSecurity.checkRecordPermission();
            }

            if (Printer.trace) Printer.trace("<< InputDeviceDataLine: implClose() succeeded");
        }


        // ABSTRACT DATA LINE

        /**
         * Call the superclass open() method, which will try to open
         * the line with the current format and buffer size values.
         * If this fails,  try to open the line with the current
         * system format.
         */
        public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {
            // if the mixer is not yet opened, set format and buffer size
            if (!mixer.isOpen()) {
                sid.format = format;
                sid.bufferSize = bufferSize;
            }
            super.open(format, bufferSize);
        }

        void implStart() {

            if (Printer.trace) Printer.trace(">> InputDeviceDataLine: implStart");

            // check for record permission
            if( sid.jsSecurity != null ) {
                sid.jsSecurity.checkRecordPermission();
            }

            if (Printer.trace) Printer.trace("<< InputDeviceDataLine: implStart succeeded");
        }


        void implStop() {

            // check for record permission
            if( sid.jsSecurity != null ) {
                sid.jsSecurity.checkRecordPermission();
            }

            if (Printer.trace) Printer.trace(">> InputDeviceDataLine: implStop");

            // set active false
            setActive(false);
            setStarted(false);

            if (Printer.trace) Printer.trace("<< InputDeviceDataLine: implStop succeeded");
        }


        // METHOD OVERRIDES


        public void drain() {

            if (isOpen()) {

                // drain data from the circular buffer
                circularBuffer.drain();

                // drain the native buffers
                sid.nDrain();
            }
        }


        public void flush() {

            if (isOpen()) {

                // flush data from the circular buffer
                circularBuffer.flush();

                // flush the native buffers
                sid.nFlush();
            }
        }


        public int getFramePosition() {
            return (isOpen()) ? (int)sid.nGetPosition() : super.getFramePosition();
        }


        // METHODS FOR INTERNAL IMPLEMENTATION USE
        private void fillBuffer(byte[] data, int lengthInBytes) {
            if (isOpen() && isStartedRunning()) {
                // set active true
                if (!isActive()) {
                    setActive(true);
                    setStarted(true);
                }

                // this will write the current data over old data if the amount of new data
                // exceeds the amount of available space in the circular buffer.
                // $$kk: 08.04.99: we can dump data here.  should have overflow event / exception /
                // notification mechanism?
                int bytesDumped = circularBuffer.writeover(data, 0, lengthInBytes);

                synchronized(this) {
                    notifyAll();
                }

                if (bytesDumped > 0) {
                    if (Printer.debug) Printer.debug("fillBuffer: buffer overflow for line " + this + "!  received " + lengthInBytes + " bytes, dumped " + bytesDumped);
                }
            }
        }

        private void checkFormat(AudioFormat format) throws LineUnavailableException {
            if (sid.isOpen()) {
                AudioFormat.Encoding enc1 = format.getEncoding();
                AudioFormat.Encoding enc2 = sid.format.getEncoding();
                boolean ok = (enc1.equals(AudioFormat.Encoding.PCM_SIGNED) && enc2.equals(AudioFormat.Encoding.PCM_UNSIGNED))
                    || (enc2.equals(AudioFormat.Encoding.PCM_SIGNED) && enc1.equals(AudioFormat.Encoding.PCM_UNSIGNED))
                    || enc1.equals(enc2);
                if (ok) {
                    // the rest - except endianness - just has to match
                    format = new AudioFormat(enc2,
                                             format.getSampleRate(),
                                             format.getSampleSizeInBits(),
                                             format.getChannels(),
                                             format.getFrameSize(),
                                             format.getFrameRate(),
                                             sid.format.isBigEndian());
                }
                if (!ok || !format.matches(sid.format)) {
                    throw new LineUnavailableException("Requested format incompatible with already established device format: " + sid.format);
                }
            }
        }


    } // class InputDeviceDataLine


    /**
     * Private inner class representing a port on the input device.
     */
    private class InputDevicePort extends AbstractLine implements Port {

        private InputDevicePort(Port.Info info, AbstractMixer mixer, Control[] controls) {
            super(info, mixer, controls);
        }


        public synchronized void open() throws LineUnavailableException {

            if (!isOpen()) {

                // check for record permission
                if( jsSecurity != null ) {
                    jsSecurity.checkRecordPermission();
                }

                // allocate mixer resources
                mixer.open(this);

                /* $$kk: 06.03.99: need to implement native open */

                // mark the line open and send events
                setOpen(true);
            }
        }


        //public synchronized void implClose() {
        public synchronized void close() {

            if (isOpen()) {

                // check for record permission
                if( jsSecurity != null ) {
                    jsSecurity.checkRecordPermission();
                }


                /* $$kk: 06.03.99: need to implement native open */

                // mark the line closed and send events
                setOpen(false);

                // release mixer resources
                mixer.close(this);
            }
        }
    } // class InputDevicePort


    /**
     * Private inner class representing an input device port info
     */
    private static class InputDevicePortInfo extends Port.Info {

        private InputDevicePortInfo(String name) {
            super(Port.class, name, true);
        }
    }





    // NATIVE METHODS

    // gets the set of formats supported by the capture device with this index
    private native void nGetFormats(int index, Vector formats,
                                    AudioFormat.Encoding pcm_signed,
                                    AudioFormat.Encoding pcm_unsigned,
                                    AudioFormat.Encoding ulaw,
                                    AudioFormat.Encoding alaw);

    // this will open the capture device and create the native stream object
    // HAE_AquireAudioCapture and GM_AudioCaptureStreamSetup
    // note that this takes the buffer size in frames.
    private native void nOpen(int index, int encoding, float sampleRate,
                              int sampleSizeInBits, int channels,
                              int bufferSize) throws LineUnavailableException;

    // GM_AudioCaptureStreamCleanup
    // calls GM_AudioCaptureStreamStop and then frees the stream
    private native void nClose();

    // GM_AudioCaptureStreamStart
    // this will allocate the buffers and create and start the capture thread
    // we do this on the first start() call
    private native void nStart();

    // GM_AudioCaptureStreamStop
    // this will deallocate the buffers and stop the capture thread.
    // $$kk: 06.13.99: need to make this call as part of close!!
    // otherwise we can crash in a callback!!  i'll leave the commented-out
    // method in cause we need it from java later....
    // private native void nStop();

    // HAE_PauseAudioCapture
    // we use this for stop(); it stops active capture but does not release the device
    private native void nPause();

    // HAE_ResumeAudioCapture
    // we use this for start(); it start active capture but does not affect the device
    private native void nResume();

    // these don't work
    private native void nDrain();
    private native void nFlush();

    // returns the frames captured at the device
    private native long nGetPosition();

    // gets the native capture buffer size in sample frames
    private native int nGetBufferSizeInFrames();

    // gets the number of ports
    private native int nGetNumPorts();

    // gets the name of the port with this index
    private native String nGetPortName(int index);
}
