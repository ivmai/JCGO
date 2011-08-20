/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 **
 * Comment: Linux-specific file.
 */

/*
 * @(#)DirectAudioDevice.java   1.4 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.media.sound;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.sound.sampled.*;

/**
 * A Mixer which provides direct access to audio devices
 *
 * @version 1.4, 03/01/23
 * @author Florian Bomers
 */
class DirectAudioDevice extends AbstractMixer {

    // CONSTANTS
    private static final int CLIP_BUFFER_TIME = 1000; // in milliseconds

    private static final int DEFAULT_LINE_BUFFER_TIME = 500; // in milliseconds

    // INSTANCE VARIABLES

    /** number of opened lines */
    private int deviceCountOpened = 0;

    /** number of started lines */
    private int deviceCountStarted = 0;

    // CONSTRUCTOR
    DirectAudioDevice(DirectAudioDeviceProvider.DirectAudioDeviceInfo portMixerInfo) {
        // pass in Line.Info, mixer, controls
        super(portMixerInfo,              // Mixer.Info
              null,                       // Control[]
              null,                       // Line.Info[] sourceLineInfo
              null);                      // Line.Info[] targetLineInfo

        if (Printer.trace) Printer.trace(">> DirectAudioDevice: constructor");

        // source lines
        DataLine.Info srcLineInfo = createDataLineInfo(true);
        if (srcLineInfo != null) {
            sourceLineInfo = new Line.Info[2];
            // SourcedataLine
            sourceLineInfo[0] = srcLineInfo;
            // Clip
            sourceLineInfo[1] = new DirectDLI(Clip.class, srcLineInfo.getFormats(),
                                 32, // arbitrary minimum buffer size
                                 AudioSystem.NOT_SPECIFIED);
        } else {
            sourceLineInfo = new Line.Info[0];
        }

        // TargetDataLine
        DataLine.Info dstLineInfo = createDataLineInfo(false);
        if (dstLineInfo != null) {
            targetLineInfo = new Line.Info[1];
            targetLineInfo[0] = dstLineInfo;
        } else {
            targetLineInfo = new Line.Info[0];
        }

        if (Printer.trace) Printer.trace("<< DirectAudioDevice: constructor completed");
    }

    private DataLine.Info createDataLineInfo(boolean isSource) {
        Vector formats = new Vector();
        AudioFormat[] formatArray = null;

        synchronized(formats) {
            nGetFormats(getMixerIndex(), getDeviceID(),
                        isSource /* true:SourceDataLine/Clip, false:TargetDataLine */,
                        formats);
            if (formats.size() > 0) {
                formatArray = new AudioFormat[formats.size()];
                for (int i = 0; i < formatArray.length; i++) {
                    formatArray[i] = (AudioFormat)formats.elementAt(i);
                }
            }
        }
        // todo: find out more about the buffer size ?
        if (formatArray != null) {
            return new DirectDLI(isSource?SourceDataLine.class:TargetDataLine.class, formatArray,
                                 32, // arbitrary minimum buffer size
                                 AudioSystem.NOT_SPECIFIED);
        }
        return null;
    }

    // ABSTRACT MIXER: ABSTRACT METHOD IMPLEMENTATIONS

    public Line getLine(Line.Info info) throws LineUnavailableException {
        Line.Info fullInfo = getLineInfo(info);
        if (fullInfo == null) {
            throw new IllegalArgumentException("Line unsupported: " + info);
        }
        if (fullInfo instanceof DataLine.Info) {
            DataLine.Info dataLineInfo = (DataLine.Info)info;
            DataLine.Info dataLineFullInfo = (DataLine.Info)fullInfo;
            if (dataLineInfo.getLineClass().isAssignableFrom(DirectSDL.class)) {
                return new DirectSDL(dataLineFullInfo, this);
            }
            if (dataLineInfo.getLineClass().isAssignableFrom(DirectClip.class)) {
                return new DirectClip(dataLineFullInfo, this);
            }
            if (dataLineInfo.getLineClass().isAssignableFrom(DirectTDL.class)) {
                return new DirectTDL(dataLineFullInfo, this);
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

        if (fullInfo instanceof DataLine.Info) {
            // DirectAudioDevices should mix !
            return getMaxSimulLines();
        }

        return 0;
    }


    protected void implOpen() throws LineUnavailableException {
        if (Printer.trace) Printer.trace("DirectAudioDevice: implOpen - void method");
    }

    protected void implClose() {
        if (Printer.trace) Printer.trace("DirectAudioDevice: implClose - void method");
    }

    protected void implStart() {
        if (Printer.trace) Printer.trace("DirectAudioDevice: implStart - void method");
    }

    protected void implStop() {
        if (Printer.trace) Printer.trace("DirectAudioDevice: implStop - void method");
    }


    // IMPLEMENTATION HELPERS

    int getMixerIndex() {
        return ((DirectAudioDeviceProvider.DirectAudioDeviceInfo) getMixerInfo()).getIndex();
    }

    int getDeviceID() {
        return ((DirectAudioDeviceProvider.DirectAudioDeviceInfo) getMixerInfo()).getDeviceID();
    }

    int getMaxSimulLines() {
        return ((DirectAudioDeviceProvider.DirectAudioDeviceInfo) getMixerInfo()).getMaxSimulLines();
    }

    private static void addFormat(Vector v, int bits, int frameSizeInBytes, int channels, float sampleRate,
                                  int encoding, boolean signed, boolean bigEndian) {
        AudioFormat.Encoding enc = null;
        switch (encoding) {
        case PCM:
            enc = signed?AudioFormat.Encoding.PCM_SIGNED:AudioFormat.Encoding.PCM_UNSIGNED;
            break;
        case ULAW:
            enc = AudioFormat.Encoding.ULAW;
            if (bits != 8) {
                if (Printer.err) Printer.err("DirectAudioDevice.addFormat called with ULAW, but bitsPerSample="+bits);
                bits = 8; frameSizeInBytes = channels;
            }
            break;
        case ALAW:
            enc = AudioFormat.Encoding.ALAW;
            if (bits != 8) {
                if (Printer.err) Printer.err("DirectAudioDevice.addFormat called with ALAW, but bitsPerSample="+bits);
                bits = 8; frameSizeInBytes = channels;
            }
            break;
        }
        if (enc==null) {
            if (Printer.err) Printer.err("DirectAudioDevice.addFormat called with unknown encoding: "+encoding);
            return;
        }
        if (frameSizeInBytes <= 0) {
            frameSizeInBytes = ((bits + 7) / 8) * channels;
        }
        v.add(new AudioFormat(enc, sampleRate, bits, channels, frameSizeInBytes, sampleRate, bigEndian));
    }

    protected static AudioFormat getSignOrEndianChangedFormat(AudioFormat format) {
        boolean isSigned = format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED);
        boolean isUnsigned = format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED);
        if (format.getFrameSize() > 1 && isSigned) {
            // if this is PCM_SIGNED and framesize>1 then try with endian-ness magic
            return new AudioFormat(format.getEncoding(),
                                   format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(),
                                   format.getFrameSize(), format.getFrameRate(), !format.isBigEndian());
        }
        else if (format.getFrameSize() == 1 && (isSigned || isUnsigned)) {
            // if this is PCM and framesize==1 (8-bit) then try with signed-ness magic
            return new AudioFormat(isSigned?AudioFormat.Encoding.PCM_UNSIGNED:AudioFormat.Encoding.PCM_SIGNED,
                                   format.getSampleRate(), format.getSampleSizeInBits(), format.getChannels(),
                                   format.getFrameSize(), format.getFrameRate(), format.isBigEndian());
        }
        return null;
    }




    // INNER CLASSES


    /**
     * Private inner class for the DataLine.Info objects
     * adds a little magic for the isFormatSupported so
     * that the automagic conversion of endianness and sign
     * does not show up in the formats array.
     * I.e. the formats array contains only the formats
     * that are really supported by the hardware,
     * but isFormatSupported() also returns true
     * for formats with wrong endianness.
     */
    private static class DirectDLI extends DataLine.Info {

        private DirectDLI(Class clazz, AudioFormat[] formatArray,
                          int minBuffer, int maxBuffer) {
            super(clazz, formatArray, minBuffer, maxBuffer);
        }

        public boolean isFormatSupportedInHardware(AudioFormat format) {
            if (format == null) return false;
            return super.isFormatSupported(format);
        }

        public boolean isFormatSupported(AudioFormat format) {
            return isFormatSupportedInHardware(format)
                || isFormatSupportedInHardware(getSignOrEndianChangedFormat(format));
        }
    }

    /**
     * Private inner class representing a SourceDataLine
     */
    private static class DirectDL extends AbstractDataLine {
        protected int mixerIndex;
        protected int deviceID;
        protected long id;
        protected int waitTime;
        protected volatile boolean flushing;
        protected boolean isSource;         // true for SourceDataLine, false for TargetDataLine
        protected long bytePosition;
        protected volatile boolean doIO;     // true in between start() and stop() calls
        protected volatile boolean activeIO; // true during native read/write calls
        protected volatile boolean stoppedWritten;

        // if native needs to manually swap samples/convert sign, this
        // is set to the framesize
        protected int softwareConversionSize = 0;
        protected AudioFormat hardwareFormat;

        /**
         * Security Manager used for TargetDataLine
         */
        private JSSecurity jsSecurity = null;


        // CONSTRUCTOR
        protected DirectDL(DataLine.Info info,
                           DirectAudioDevice mixer,
                           int mixerIndex,
                           int deviceID,
                           boolean isSource) {
            super(info, mixer, null);
            if (Printer.trace) Printer.trace("DirectDL CONSTRUCTOR: info: " + info);
            this.mixerIndex = mixerIndex;
            this.deviceID = deviceID;
            this.waitTime = 10; // 10 milliseconds default wait time
            this.isSource = isSource;
            if (!isSource) {
                // init security manager for target data line
                jsSecurity = JSSecurityManager.getJSSecurity();
            }
        }


        // ABSTRACT METHOD IMPLEMENTATIONS

        // ABSTRACT LINE / DATALINE

        void implOpen(AudioFormat format, int bufferSize) throws LineUnavailableException {
            if (Printer.trace) Printer.trace(">> DirectDL: implOpen("+format+", "+bufferSize+" bytes)");

            // check for record permission
            if (!isSource && jsSecurity != null) {
                jsSecurity.checkRecordPermission();
            }
            int encoding = PCM;
            if (format.getEncoding().equals(AudioFormat.Encoding.ULAW)) {
                encoding = ULAW;
            }
            else if (format.getEncoding().equals(AudioFormat.Encoding.ALAW)) {
                encoding = ALAW;
            }

            if (bufferSize == AudioSystem.NOT_SPECIFIED) {
                bufferSize = (int) Toolkit.millis2bytes(format, DEFAULT_LINE_BUFFER_TIME);
            }

            DirectDLI ddli = null;
            if (info instanceof DirectDLI) {
                ddli = (DirectDLI) info;
            }

            hardwareFormat = format;

            /* some magic to account for not-supported endianness or signed-ness */
            softwareConversionSize = 0;
            if (ddli != null && !ddli.isFormatSupportedInHardware(format)) {
                AudioFormat newFormat = getSignOrEndianChangedFormat(format);
                if (ddli.isFormatSupportedInHardware(newFormat)) {
                    // apparently, the new format can be used.
                    hardwareFormat = newFormat;
                    // So do endian/sign conversion in software
                    softwareConversionSize = format.getFrameSize() / format.getChannels();
                }
            }

            // align buffer to full frames
            bufferSize = ((int) bufferSize / format.getFrameSize()) * format.getFrameSize();

            id = nOpen(mixerIndex, deviceID, isSource,
                       encoding,
                       hardwareFormat.getSampleRate(),
                       hardwareFormat.getSampleSizeInBits(),
                       hardwareFormat.getFrameSize(),
                       hardwareFormat.getChannels(),
                       hardwareFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED),
                       hardwareFormat.isBigEndian(),
                       bufferSize);

            if (id == 0) {
                // TODO: nicer error messages...
                throw new LineUnavailableException("line with format "+format+" not supported.");
            }
            this.bufferSize = nGetBufferSize(id, isSource);
            if (this.bufferSize < 1) {
                // this is an error!
                this.bufferSize = bufferSize;
            }
            this.format = format;
            // wait time = 1/4 of buffer time
            waitTime = (int) Toolkit.bytes2millis(format, this.bufferSize) / 4;
            if (waitTime < 1) {
                waitTime = 1;
            }
            else if (waitTime > 1000) {
                // we have seen large buffer sizes!
                // never wait for more than a second
                waitTime = 1000;
            }
            bytePosition = 0;
            stoppedWritten = false;
            doIO = false;

            if (Printer.trace) Printer.trace("<< DirectDL: implOpen() succeeded");
        }


        void implStart() {
            if (Printer.trace) Printer.trace(" >> DirectDL: implStart()");

            // check for record permission
            if (!isSource && jsSecurity != null) {
                jsSecurity.checkRecordPermission();
            }

            nStart(id, isSource);
            doIO = true;

            if (isSource && stoppedWritten) {
                setStarted(true);
                setActive(true);
            }

            if (Printer.trace) Printer.trace(" << DirectDL: implStart() succeeded");
        }

        void implStop() {
            if (Printer.trace) Printer.trace(" >> DirectDL: implStop()");

            // check for record permission
            if (!isSource && jsSecurity != null) {
                jsSecurity.checkRecordPermission();
            }

            nStop(id, isSource);
            // need to set doIO to false before notifying the
            // read/write thread, that's why isStartedRunning()
            // cannot be used
            doIO = false;
            // wake up any waiting threads
            synchronized(this) {
                notifyAll();
            }
            setActive(false);
            setStarted(false);
            stoppedWritten = false;

            if (Printer.trace) Printer.trace(" << DirectDL: implStop() succeeded");
        }

        void implClose() {
            if (Printer.trace) Printer.trace(">> DirectDL: implClose()");

            // check for record permission
            if (!isSource && jsSecurity != null) {
                jsSecurity.checkRecordPermission();
            }

            doIO = false;
            long oldID = id;
            id = 0;
            // wait some time to give enough time for native methods to return
            // still, race conditions may occur...
            while (activeIO) {
                try {
                    Thread.sleep(30);
                } catch (InterruptedException ie) {
                    break;
                }
            }
            nClose(oldID, isSource);
            bytePosition = 0;
            softwareConversionSize = 0;
            if (Printer.trace) Printer.trace("<< DirectDL: implClose() succeeded");
        }

        // METHOD OVERRIDES

        public int available() {
            if (id == 0) {
                return 0;
            }
            return nAvailable(id, isSource);
        }


        public void drain() {
            while ((id != 0) && nIsStillDraining(id, isSource)) {
                synchronized(this) {
                    try {
                        wait(10);
                    } catch (InterruptedException ie) {}
                }
            }
        }

        public void flush() {
            if (id != 0) {
                // first stop ongoing read/write method
                flushing = true;
                synchronized(this) {
                    notifyAll();
                }
                // then flush native buffers
                nFlush(id, isSource);
            }
        }

        public int getFramePosition() {
            long pos = nGetBytePosition(id, isSource, bytePosition);
            // hack because ALSA sometimes reports wrong framepos
            if (pos < 0) {
                pos = 0;
            }
            return (int) (pos / getFormat().getFrameSize());
        }


        /*
         * write() belongs into SourceDataLine and Clip,
         * so define it here and make it accessible by
         * declaring the respective interfaces with DirectSDL and DirectClip
         */
        public int write(byte[] b, int off, int len) {
            flushing = false;

            if (len == 0)
                return 0;
            if (len < 0)
                throw new IllegalArgumentException("illegal len: " + len);

            if (len % getFormat().getFrameSize() != 0) {
                throw new IllegalArgumentException("Illegal request to write "
                                                   +"non-integral number of frames ("
                                                   +len+" bytes, "
                                                   +"frameSize = "+getFormat().getFrameSize()+" bytes)");
            }

            if (off < 0)
                throw new ArrayIndexOutOfBoundsException(off);
            if (off + len > b.length)
                throw new ArrayIndexOutOfBoundsException(b.length);

            if (!isActive() && doIO) {
                // this is not exactly correct... would be nicer
                // if the native sub system sent a callback when IO really starts
                setActive(true);
                setStarted(true);
            }
            int written = 0;
            while (!flushing) {
                activeIO = true;
                int thisWritten = nWrite(id, b, off, len, softwareConversionSize);
                activeIO = false;
                if (thisWritten < 0) {
                    // error in native layer
                    break;
                }
                len -= thisWritten;
                bytePosition += thisWritten;
                written += thisWritten;
                if (doIO && len > 0) {
                    off += thisWritten;
                    synchronized (this) {
                        try {
                            wait(waitTime);
                        } catch (InterruptedException ie) {}
                    }
                } else {
                    break;
                }
            }

            if (written > 0 && !doIO)
                stoppedWritten = true;

            return written;
        }

    } // class DirectDL


    /**
     * Private inner class representing a SourceDataLine
     */
    private static class DirectSDL extends DirectDL implements SourceDataLine {

        // CONSTRUCTOR
        private DirectSDL(DataLine.Info info,
                          DirectAudioDevice mixer) {
            super(info, mixer, mixer.getMixerIndex(), mixer.getDeviceID(), true);
            if (Printer.trace) Printer.trace("DirectSDL CONSTRUCTOR: completed");
        }

    }

    /**
     * Private inner class representing a TargetDataLine
     */
    private static class DirectTDL extends DirectDL implements TargetDataLine {

        // CONSTRUCTOR
        private DirectTDL(DataLine.Info info,
                          DirectAudioDevice mixer) {
            super(info, mixer, mixer.getMixerIndex(), mixer.getDeviceID(), false);
            if (Printer.trace) Printer.trace("DirectTDL CONSTRUCTOR: completed");
        }

        // METHOD OVERRIDES

        public int read(byte[] b, int off, int len) {
            flushing = false;

            if (len == 0)
                return 0;
            if (len < 0)
                throw new IllegalArgumentException("illegal len: " + len);

            if (len % getFormat().getFrameSize() != 0) {
                throw new IllegalArgumentException("Illegal request to read "
                                                   +"non-integral number of frames ("
                                                   +len+" bytes )");
            }

            if (off < 0)
                throw new ArrayIndexOutOfBoundsException(off);
            if (off + len > b.length)
                throw new ArrayIndexOutOfBoundsException(b.length);

            if (!isActive() && doIO) {
                // this is not exactly correct... would be nicer
                // if the native sub system sent a callback when IO really starts
                setActive(true);
                setStarted(true);
            }
            int read = 0;
            while (doIO && !flushing) {
                activeIO = true;
                int thisRead = nRead(id, b, off, len, softwareConversionSize);
                activeIO = false;
                if (thisRead < 0) {
                    // error in native layer
                    break;
                }
                len -= thisRead;
                bytePosition += thisRead;
                read += thisRead;
                if (len > 0) {
                    off += thisRead;
                    synchronized(this) {
                        try {
                            wait(waitTime);
                        } catch (InterruptedException ie) {}
                    }
                } else {
                    break;
                }
            }
            return read;
        }

    }

    /**
     * Private inner class representing a Clip
     * This clip is realized in software only
     */
    private static class DirectClip extends DirectDL implements Clip,  Runnable, AutoClosingClip {
        private Thread thread;
        private byte[] audioData = null;
        private int frameSize;         // size of one frame in bytes
        private int m_lengthInFrames;
        private int loopCount;
        private int clipBytePosition;   // index in the audioData array at current playback
        private int newFramePosition;   // set in setFramePosition()
        private int loopStartFrame;
        private int loopEndFrame;      // the last sample included in the loop

        // auto closing clip support
        private boolean autoclosing = false;

        // CONSTRUCTOR
        private DirectClip(DataLine.Info info,
                           DirectAudioDevice mixer) {
            super(info, mixer, mixer.getMixerIndex(), mixer.getDeviceID(), true);
            if (Printer.trace) Printer.trace("DirectClip CONSTRUCTOR: completed");
        }

        // CLIP METHODS

        public void open(AudioFormat format, byte[] data, int offset, int bufferSize)
            throws LineUnavailableException {
            byte[] newData = new byte[bufferSize];
            System.arraycopy(data, offset, newData, 0, bufferSize);
            open(format, data, bufferSize / format.getFrameSize());
        }

        // this method does not copy the data array
        private void open(AudioFormat format, byte[] data, int frameLength)
            throws LineUnavailableException {

            synchronized (mixer) {
                if (Printer.trace) Printer.trace("> DirectClip.open(format, data, frameLength)");
                if (Printer.debug) Printer.debug("   data="+((data==null)?"null":""+data.length+" bytes"));
                if (Printer.debug) Printer.debug("   frameLength="+frameLength);

                if (isOpen()) {
                    throw new IllegalStateException("Clip is already open with format " + getFormat() +
                                                    " and frame lengh of " + getFrameLength());
                } else {
                    // if the line is not currently open, try to open it with this format and buffer size
                    this.audioData = data;
                    this.frameSize = format.getFrameSize();
                    this.m_lengthInFrames = frameLength;
                    // initialize loop selection with full range
                    bytePosition = 0;
                    clipBytePosition = 0;
                    newFramePosition = -1; // means: do not set to a new readFramePos
                    loopStartFrame = 0;
                    loopEndFrame = frameLength - 1;
                    loopCount = 0; // means: play the clip irrespective of loop points from beginning to end

                    try {
                        // use DirectDL's open method to open it
                        open(format, (int) Toolkit.millis2bytes(format, CLIP_BUFFER_TIME)); // one second buffer
                    } catch (LineUnavailableException lue) {
                        audioData = null;
                        throw lue;
                    } catch (IllegalArgumentException iae) {
                        audioData = null;
                        throw iae;
                    }
                    if (autoclosing) {
                        getEventDispatcher().autoClosingClipOpened(this);
                    }

                    // if we got this far, we can instanciate the thread
                    thread = new Thread(this, "Direct Clip");
                    try {
                        // don't prevent termination of the VM because of the playing clip
                        thread.setDaemon(true);
                        // increase a little bit the priority to prevent drop-outs
                        thread.setPriority(Thread.NORM_PRIORITY
                                           + (Thread.MAX_PRIORITY - Thread.NORM_PRIORITY) / 3);
                    } catch (SecurityException se) {
                        // not a big deal if we don't have the permission to raise priority
                    }
                }
            }
            if (Printer.trace) Printer.trace("< DirectClip.open completed");
        }

        public void open(AudioInputStream stream) throws LineUnavailableException, IOException {

            synchronized (mixer) {
                if (Printer.trace) Printer.trace("> DirectClip.open(stream)");
                byte[] streamData = null;

                if (isOpen()) {
                    throw new IllegalStateException("Clip is already open with format " + getFormat() +
                                                    " and frame lengh of " + getFrameLength());
                }
                int lengthInFrames = (int)stream.getFrameLength();
                if (Printer.debug) Printer.debug("DirectClip: open(AIS): lengthInFrames: " + lengthInFrames);

                int bytesRead = 0;
                if (lengthInFrames != AudioSystem.NOT_SPECIFIED) {
                    // read the data from the stream into an array in one fell swoop.
                    int arraysize = lengthInFrames * stream.getFormat().getFrameSize();
                    streamData = new byte[arraysize];

                    int bytesRemaining = arraysize;
                    int thisRead = 0;
                    while (bytesRemaining > 0 && thisRead >= 0) {
                        thisRead = stream.read(streamData, bytesRead, bytesRemaining);
                        if (thisRead > 0) {
                            bytesRead += thisRead;
                            bytesRemaining -= thisRead;
                        }
                        else if (thisRead == 0) {
                            Thread.yield();
                        }
                    }
                } else {
                    // read data from the stream until we reach the end of the stream
                    // we use a slightly modified version of ByteArrayOutputStream
                    // to get direct access to the byte array (we don't want a new array
                    // to be allocated)
                    int MAX_READ_LIMIT = 16384;
                    DirectBAOS dbaos  = new DirectBAOS();
                    byte tmp[] = new byte[MAX_READ_LIMIT];
                    int thisRead = 0;
                    while (thisRead >= 0) {
                        thisRead = stream.read(tmp, 0, tmp.length);
                        if (thisRead > 0) {
                            dbaos.write(tmp, 0, thisRead);
                            bytesRead += thisRead;
                        }
                        else if (thisRead == 0) {
                            Thread.yield();
                        }
                    } // while
                    streamData = dbaos.getInternalBuffer();
                }
                lengthInFrames = bytesRead / stream.getFormat().getFrameSize();

                if (Printer.debug) Printer.debug("Read to end of stream. lengthInFrames: " + lengthInFrames);

                // now try to open the device
                open(stream.getFormat(), streamData, lengthInFrames);

                if (Printer.trace) Printer.trace("< DirectClip.open(stream) succeeded");
            } // synchronized
        }


        public int getFrameLength() {
            return m_lengthInFrames;
        }


        public long getMicrosecondLength() {
            return Toolkit.frames2micros(getFormat(), getFrameLength());
        }


        public void setFramePosition(int frames) {
            if (Printer.trace) Printer.trace("> DirectClip: setFramePosition: " + frames);

            if (frames < 0) {
                frames = 0;
            }
            else if (frames >= getFrameLength()) {
                frames = getFrameLength();
            }
            if (doIO) {
                newFramePosition = frames;
            } else {
                bytePosition = frames * frameSize;
            }
            // cease currently playing buffer
            flush();

            if (Printer.trace) Printer.trace("< DirectClip: setFramePosition: set position to " + getFramePosition());
        }

        public int getFramePosition() {
            /* $$fb
             * this would be intuitive, but the definition of getFramePosition
             * is the number of frames rendered since opening the device...
             * That also means that setFramePosition() means something very
             * different from getFramePosition() for Clip.
             */
            // take into account the case that a new position was set...
            //if (!doIO && newFramePosition >= 0) {
            //return newFramePosition;
            //}
            return super.getFramePosition();
        }


        public synchronized void setMicrosecondPosition(long microseconds) {
            if (Printer.trace) Printer.trace("> DirectClip: setMicrosecondPosition: " + microseconds);

            long frames = Toolkit.micros2frames(getFormat(), microseconds);
            setFramePosition((int) frames);

            if (Printer.trace) Printer.trace("< DirectClip: setMicrosecondPosition succeeded");
        }

        public void setLoopPoints(int start, int end) {
            if (Printer.trace) Printer.trace("> DirectClip: setLoopPoints: start: " + start + " end: " + end);

            if (start < 0) {
                throw new IllegalArgumentException("illegal value for start: "+start);
            }
            if (end >= getFrameLength()) {
                throw new IllegalArgumentException("illegal value for end: "+end);
            }

            if (end == -1) {
                end = getFrameLength() - 1;
            }

            // if the end position is less than the start position, throw IllegalArgumentException
            if (end < start) {
                throw new IllegalArgumentException("End position " + end + "  preceeds start position " + start);
            }

            // slight race condition with the run() method, but not a big problem
            loopStartFrame = start;
            loopEndFrame = end;

            if (Printer.trace) Printer.trace("  loopStart: " + loopStartFrame + " loopEnd: " + loopEndFrame);
            if (Printer.trace) Printer.trace("< DirectClip: setLoopPoints completed");
        }


        public void loop(int count) {
            // note: when count reaches 0, it means that the entire clip
            // will be played, i.e. it will play past the loop end point
            loopCount = count;
            start();
        }

        // ABSTRACT METHOD IMPLEMENTATIONS

        // ABSTRACT LINE

        void implOpen(AudioFormat format, int bufferSize) throws LineUnavailableException {
            // only if audioData wasn't set in a calling open(format, byte[], frameSize)
            // this call is allowed.
            if (audioData == null) {
                throw new IllegalArgumentException("Illegal call to open() in interface Clip");
            }
            super.implOpen(format, bufferSize);
        }

        void implClose() {
            if (Printer.trace) Printer.trace(">> DirectClip: implClose()");

            // dispose of thread
            Thread oldThread = thread;
            thread = null;
            doIO = false;
            if (oldThread != null) {
                // wake up the thread if it's in wait()
                synchronized(this) {
                    notifyAll();
                }
                // wait for the thread to terminate itself, but max. 2 seconds
                try {
                    oldThread.join(2000);
                } catch (InterruptedException ie) {}
            }
            super.implClose();
            // remove audioData reference and hand it over to gc
            audioData = null;
            newFramePosition = -1;

            // remove this instance from the list of auto closing clips
            getEventDispatcher().autoClosingClipClosed(this);

            if (Printer.trace) Printer.trace("<< DirectClip: implClose() succeeded");
        }


        void implStart() {
            if (Printer.trace) Printer.trace("> DirectClip: implStart()");

            super.implStart();
            if (thread != null && !thread.isAlive()) {
                thread.start();
            }

            if (Printer.trace) Printer.trace("< DirectClip: implStart() succeeded");
        }

        void implStop() {
            if (Printer.trace) Printer.trace(">> DirectClip: implStop()");

            super.implStop();
            // reset loopCount field so that playback will be normal with
            // next call to start()
            loopCount = 0;

            if (Printer.trace) Printer.trace("<< DirectClip: implStop() succeeded");
        }


        // main playback loop
        public void run() {
            if (Printer.trace) Printer.trace(">>> DirectClip: run()");
            while (thread != null) {
                if (!doIO) {
                    synchronized(this) {
                        try {
                            wait();
                        } catch(InterruptedException ie) {}
                    }
                }
                while (doIO) {
                    if (newFramePosition >= 0) {
                        clipBytePosition = newFramePosition * frameSize;
                        newFramePosition = -1;
                    }
                    int endFrame = getFrameLength() - 1;
                    if (loopCount > 0 || loopCount == LOOP_CONTINUOUSLY) {
                        endFrame = loopEndFrame;
                    }
                    long framePos = (clipBytePosition / frameSize);
                    int toWriteFrames = (int) (endFrame - framePos + 1);
                    int toWriteBytes = toWriteFrames * frameSize;
                    if (toWriteBytes > getBufferSize()) {
                        toWriteBytes = Toolkit.align(getBufferSize(), frameSize);
                    }
                    int written = write(audioData, (int) clipBytePosition, toWriteBytes); // increases bytePosition
                    // make sure nobody called setFramePosition, or stop() during the write() call
                    if (doIO && newFramePosition < 0 && written >= 0) {
                        clipBytePosition += written;
                        framePos = clipBytePosition / frameSize;
                        // since endFrame is the last frame to be played,
                        // framePos is after endFrame when all frames, including framePos,
                        // are played.
                        if (framePos > endFrame) {
                            // at end of playback. If looping is on, loop back to the beginning.
                            if (loopCount > 0 || loopCount == LOOP_CONTINUOUSLY) {
                                loopCount--;
                                newFramePosition = loopStartFrame;
                            } else {
                                // no looping, stop playback
                                drain();
                                stop();
                            }
                        }
                    }
                    if (written <= 0)
                        Thread.yield();
                }
            }
            if (Printer.trace) Printer.trace("<<< DirectClip: run() succeeded");
        }

        // AUTO CLOSING CLIP SUPPORT

        public synchronized boolean isAutoClosing() {
            return autoclosing;
        }

        public synchronized void setAutoClosing(boolean value) {
            if (value != autoclosing) {
                if (isOpen()) {
                    if (value) {
                        getEventDispatcher().autoClosingClipOpened(this);
                    } else {
                        getEventDispatcher().autoClosingClipClosed(this);
                    }
                }
                autoclosing = value;
            }
        }


    } // DirectClip

    /*
     * private inner class representing a ByteArrayOutputStream
     * which allows retrieval of the internal array
     */
    private static class DirectBAOS extends ByteArrayOutputStream {
        public DirectBAOS() {
            super();
        }

        public byte[] getInternalBuffer() {
            return buf;
        }

    } // class DirectBAOS

    private static native void nGetFormats(int mixerIndex, int deviceID,
                                           boolean isSource, Vector formats);

    private static native long nOpen(int mixerIndex, int deviceID, boolean isSource,
                                     int encoding,
                                     float sampleRate,
                                     int sampleSizeInBits,
                                     int frameSize,
                                     int channels,
                                     boolean signed,
                                     boolean bigEndian,
                                     int bufferSize) throws LineUnavailableException;
    private static native void nStart(long id, boolean isSource);
    private static native void nStop(long id, boolean isSource);
    private static native void nClose(long id, boolean isSource);
    private static native int nWrite(long id, byte[] b, int off, int len, int conversionSize);
    private static native int nRead(long id, byte[] b, int off, int len, int conversionSize);
    private static native int nGetBufferSize(long id, boolean isSource);
    private static native boolean nIsStillDraining(long id, boolean isSource);
    private static native void nFlush(long id, boolean isSource);
    private static native int nAvailable(long id, boolean isSource);
    // javaPos is number of bytes read/written in Java layer
    private static native long nGetBytePosition(long id, boolean isSource, long javaPos);

}
