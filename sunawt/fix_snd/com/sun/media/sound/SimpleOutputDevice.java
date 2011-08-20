/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)SimpleOutputDevice.java  1.27 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.media.sound;

import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;


/**
 * Simple output device.  Contains mixer and data line functionality for
 * simple wave-out output devices.
 *
 *
  1.27 03/01/23
 * @author Kara Kytle
 */
class SimpleOutputDevice extends AbstractMixer {


    // INSTANCE PROPERTIES
    private AudioFormat format = null;
    private int bufferSize = AudioSystem.NOT_SPECIFIED;

    /**
     * Fixed set of ports available on the output device.
     */
    private Port[] ports;


    // CONSTRUCTOR

    /**
     * Constructs a new SimpleOutputDevice.
     */
    SimpleOutputDevice(SimpleOutputDeviceProvider.OutputDeviceInfo outputDeviceInfo) {

        super(outputDeviceInfo, // Mixer.Info
              null,             // Control[]
              null,             // Line.Info[] sourceLineInfo
              null);            // Line.Info[] targetLineInfo

        if (Printer.trace) Printer.trace(">> SimpleOutputDevice: constructor");

        // initialize platform-specific values, and load the native library
        Platform.initialize();

        // query our source line format capabilities
        // $$kk: 06.05.99 this is bad; should query the full set of supported formats
        boolean supports8 = nSupportsSampleSizeInBits(8);
        boolean supports16 = nSupportsSampleSizeInBits(16);
        boolean supportsMono = nSupportsChannels(1);
        boolean supportsStereo = nSupportsChannels(2);
        boolean supports8k = nSupportsSampleRate(8000.0f);
        boolean supports11k = nSupportsSampleRate(11025.0f);
        //$$fb 2001-07-20: added "support" for 16KHz
        boolean supports16k = nSupportsSampleRate(16000.0f);
        boolean supports22k = nSupportsSampleRate(22050.0f);
        //$$fb 2001-07-20: added "support" for 32KHz
        boolean supports32k = nSupportsSampleRate(32000.0f);
        boolean supports44k = nSupportsSampleRate(44100.0f);

        // $$kk: 06.02.99: we should handle the full set of formats
        // exposed by the device!!  we should also handle direct u-law
        // output where supported.  we should not be doing this stupid
        // query!!  this probably means we should return an array of
        // supported AudioFormat objects from a single native call.

        // populate a vector of supported formats
        Vector formats = new Vector();

        // $$fb: 2001-07-05 fixed incorrect framesize values. Bug 4469409
        // $$fb: 2001-07-20 added support for 16KHz and 32KHz. Bug 4479441

        // 8000k

        if (supports8 && supportsMono && supports8k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000, 8, 1, 1, 8000, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 8000, 8, 1, 1, 8000, false));
        }

        if (supports8 && supportsStereo && supports8k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000, 8, 2, 2, 8000, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 8000, 8, 2, 2, 8000, false));
        }

        if (supports16 && supportsMono && supports8k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000, 16, 1, 2, 8000, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000, 16, 1, 2, 8000, true));
        }

        if (supports16 && supportsStereo && supports8k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000, 16, 2, 4, 8000, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000, 16, 2, 4, 8000, true));
        }

        // 11025

        if (supports8 && supportsMono && supports11k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 11025, 8, 1, 1, 11025, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 11025, 8, 1, 1, 11025, false));
        }

        if (supports8 && supportsStereo && supports11k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 11025, 8, 2, 2, 11025, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 11025, 8, 2, 2, 11025, false));
        }

        //$$fb 2001-07-20: added "support" for 16KHz

        // 16000

        if (supports16 && supportsMono && supports11k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 11025, 16, 1, 2, 11025, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 11025, 16, 1, 2, 11025, true));
        }

        if (supports16 && supportsStereo && supports11k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 11025, 16, 2, 4, 11025, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 11025, 16, 2, 4, 11025, true));
        }

        if (supports8 && supportsMono && supports16k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 8, 1, 1, 16000, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 16000, 8, 1, 1, 16000, false));
        }

        if (supports8 && supportsStereo && supports16k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 8, 2, 2, 16000, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 16000, 8, 2, 2, 16000, false));
        }

        if (supports16 && supportsMono && supports16k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1, 2, 16000, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 1, 2, 16000, true));
        }

        if (supports16 && supportsStereo && supports16k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 2, 4, 16000, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 2, 4, 16000, true));
        }


        // 22050

        if (supports8 && supportsMono && supports22k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 22050, 8, 1, 1, 22050, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 22050, 8, 1, 1, 22050, false));
        }

        if (supports8 && supportsStereo && supports22k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 22050, 8, 2, 2, 22050, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 22050, 8, 2, 2, 22050, false));
        }

        if (supports16 && supportsMono && supports22k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 22050, 16, 1, 2, 22050, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 22050, 16, 1, 2, 22050, true));
        }

        if (supports16 && supportsStereo && supports22k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 22050, 16, 2, 4, 22050, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 22050, 16, 2, 4, 22050, true));
        }

        //$$fb 2001-07-20: added "support" for 32KHz

        // 32000

        if (supports8 && supportsMono && supports32k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 32000, 8, 1, 1, 32000, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 32000, 8, 1, 1, 32000, false));
        }

        if (supports8 && supportsStereo && supports32k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 32000, 8, 2, 2, 32000, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 32000, 8, 2, 2, 32000, false));
        }

        if (supports16 && supportsMono && supports32k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 32000, 16, 1, 2, 32000, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 32000, 16, 1, 2, 32000, true));
        }

        if (supports16 && supportsStereo && supports32k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 32000, 16, 2, 4, 32000, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 32000, 16, 2, 4, 32000, true));
        }


        // 44100

        if (supports8 && supportsMono && supports44k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 8, 1, 1, 44100, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 44100, 8, 1, 1, 44100, false));
        }

        if (supports8 && supportsStereo && supports44k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 8, 2, 2, 44100, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, 44100, 8, 2, 2, 44100, false));
        }

        if (supports16 && supportsMono && supports44k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, true));
        }

        if (supports16 && supportsStereo && supports44k) {
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false));
            formats.addElement(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, true));
        }


        // $$kk: 06.03.99: this is stupid; should store these as an array!!
        AudioFormat[] formatArray;
        synchronized(formats) {
            formatArray = new AudioFormat[formats.size()];
            for (int i = 0; i < formatArray.length; i++) {
                formatArray[i] = (AudioFormat)formats.elementAt(i);
            }
        }

        // $$kk: 05.31.99: need to figure this out!
        // set up the port info objects; these are the targets for the output device

        int numPorts = nGetNumPorts();
        targetLineInfo = new Port.Info[numPorts];
        ports = new Port[numPorts];
        String name;

        for (int i = 0; i < numPorts; i++) {

            name = nGetPortName(i);
            targetLineInfo[i] = getPortInfo(name);

            ports[i] = new OutputDevicePort((Port.Info)targetLineInfo[i], this, new Control[0]);
        }


        // set up the source line objects
        sourceLineInfo = new DataLine.Info[1];
        sourceLineInfo[0] = new DataLine.Info(SourceDataLine.class,
                                              formatArray,
                                              0,
                                              AudioSystem.NOT_SPECIFIED);


        // $$jb: 12.07.99: Fix for 4297115: NoSuchElementException thrown on
        // machines without capture devices.  We need to make sure that the
        // formats vector has elements before we set the initial format.

        // set the initial format as the best supported one
        if( formats.size() > 0 ) {
            format = (AudioFormat)formats.lastElement();
        } else {
            format = null;
        }

        if (Printer.trace) Printer.trace("<< SimpleOutputDevice: constructor completed");

    }


    // ABSTRACT MIXER: ABSTRACT METHOD IMPLEMENTATIONS

    // MIXER METHODS

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

            DataLine.Info dataLineInfo = (DataLine.Info)info;

            if (dataLineInfo.getLineClass().isAssignableFrom(OutputDeviceDataLine.class)) {
                AudioFormat[] supportedFormats = dataLineInfo.getFormats();
                AudioFormat defaultFormat = supportedFormats[supportedFormats.length-1];
                return new OutputDeviceDataLine(dataLineInfo, this, defaultFormat, dataLineInfo.getMaxBufferSize());
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

        // $$kk: 06.02.99: for ports, let's assume we can only have one output
        // port open at a time.  the real situation may be more complicated.

        if (fullInfo instanceof Port.Info) {
            return ports.length;
        }


        // $$kk: 06.02.99: just one for now; we're really not going
        // to let anyone use it.,,,

        if (fullInfo instanceof DataLine.Info) {
            return 1;
        }

        return 0;
    }


    // ABSTRACT LINE: ABSTRACT METHOD IMPLEMENTATIONS


    protected void implOpen() throws LineUnavailableException {

        //$$fb 2001-08-01: better check for buffer size. Part of fix for bug #4326534 (flush bug)
        int bufferSizeInFrames=(bufferSize==AudioSystem.NOT_SPECIFIED)?
            AudioSystem.NOT_SPECIFIED:(bufferSize / format.getFrameSize());

        if (bufferSizeInFrames <= 0) {
            bufferSizeInFrames = (int) (format.getFrameRate() / 2);
        }

        // open the output device
        // note that this method takes the buffer size in frames
        nOpen(((SimpleOutputDeviceProvider.OutputDeviceInfo)getMixerInfo()).getIndex(),
              (int)format.getSampleRate(),
              format.getSampleSizeInBits(),
              format.getChannels(),
              bufferSizeInFrames );

        //$$fb 2001-08-01: make sure that the bufferSize field has correct and aligned buffersize
        this.bufferSize = (bufferSizeInFrames==AudioSystem.NOT_SPECIFIED)?
            AudioSystem.NOT_SPECIFIED:(bufferSizeInFrames * format.getFrameSize());
    }

    public void implClose() {

        // close the output device
        nClose();
    }


    // ABSTRACT DATA LINE: ABSTRACT METHOD IMPLEMENTATIONS

    /**
     * Start the output device
     */
    protected void implStart() {
        nStart();
    }


    /**
     * Stop the output device
     */
    protected void implStop() {
        nStart();
    }


    // HELPER METHODS


    /**
     * Utility method for converting between String names and well-known
     * Port types.  I'm only doing the ones that can be targets.  We may
     * want to do something at least a little more general here....
     */
    private Port.Info getPortInfo(String name) {

        if (name.equals(Port.Info.SPEAKER.toString())) {
            return Port.Info.SPEAKER;
        }

        if (name.equals(Port.Info.HEADPHONE.toString())) {
            return Port.Info.HEADPHONE;
        }

        if (name.equals(Port.Info.LINE_OUT.toString())) {
            return Port.Info.LINE_OUT;
        }

        // return a new port info object.
        return (new OutputDevicePortInfo(name));
    }


    // INNER CLASSES

    /**
     * Private inner class representing the source data line for the SimpleOutputDevice.
     */
    private class OutputDeviceDataLine extends AbstractDataLine implements SourceDataLine {

        private CircularBuffer circularBuffer                           = null;


        // CONSTRUCTOR

        private OutputDeviceDataLine(DataLine.Info info, SimpleOutputDevice mixer, AudioFormat initialFormat, int initialBufferSize) {

            // $$kk: 06.02.99: need to deal with controls!
            super(info, mixer, null, initialFormat, initialBufferSize);
        }


        // SOURCE DATA LINE METHODS

        /**
         * This will generate a NullPointerException if b is null, and
         * an ArrayIndexOutOfBounds exception if len frames past off
         * overruns the end of the array.
         * @param off - in bytes
         * @param len - in sample frames
         */
        public int write(byte[] b, int off, int len) {

            // fail; this doesn't really work
            throw new SecurityException("Permission to read data from the output device not granted.");
        }


        public int available() {

            // fail; this doesn't really work
            return 0;
        }


        // ABSTRACT METHOD IMPLEMENTATIONS

        // ABSTRACT LINE

        void implOpen(AudioFormat format, int bufferSize) throws LineUnavailableException {

            // fail; this doesn't really work
            throw new SecurityException("Permission to read data from the output device not granted.");
        }


        void implClose() {

            // fail; this doesn't really work
            throw new SecurityException("Permission to read data from the output device not granted.");
        }


        // ABSTRACT DATA LINE


        void implStart() {

            // fail; this doesn't really work
            throw new SecurityException("Permission to read data from the output device not granted.");
        }


        void implStop() {

            // fail; this doesn't really work
            throw new SecurityException("Permission to read data from the output device not granted.");
        }


        // METHODS FOR INTERNAL IMPLEMENTATION USE

        private CircularBuffer getCircularBuffer() {
            return circularBuffer;
        }

    } // class OutputDeviceDataLine


    /**
     * Private inner class representing a port on the output device.
     */
    private class OutputDevicePort extends AbstractLine implements Port {

        private OutputDevicePort(Port.Info info, AbstractMixer mixer, Control[] controls) {
            super(info, mixer, controls);
        }


        public void open() throws LineUnavailableException {

            /* $$kk: 06.03.99: need to implement */
        }


        public void close() {

            /* $$kk: 06.03.99: need to implement */
        }
    } // class OutputDevicePort


    /**
     * Private inner class representing an output device port info object
     */
    private static class OutputDevicePortInfo extends Port.Info {

        private OutputDevicePortInfo(String name) {
            super(Port.class, name, false);
        }
    }





    // NATIVE METHODS

    // this will open the output device and start the output thread
    // note that this takes the buffer size in frames.
    // GM_ResumeGeneralSound
    private void nOpen(int index, float sampleRate, int sampleSizeInBits, int channels, int bufferSize) throws LineUnavailableException
    {
     throw new LineUnavailableException();
    }

    // this will close the output device and stop the output thread
    // GM_PauseGeneralSound
    private void nClose() {}

    // ?
    private void nStart() {}

    // ?
    private void nStop() {}


    // these should be replaced by a better mechanism for finding supported
    // formats
    private boolean nSupportsSampleRate(float sampleRate)
    {
     return (sampleRate == 8000) || (sampleRate == 11025) ||
             (sampleRate == 16000) || (sampleRate == 22050) ||
             (sampleRate == 32000) || (sampleRate == 44100) ||
             (sampleRate == 48000);
    }

    private boolean nSupportsSampleSizeInBits(int sampleSizeInBits)
    {
     return sampleSizeInBits == 16 || sampleSizeInBits == 8;
    }

    private boolean nSupportsChannels(int channels)
    {
     return channels == 1 || channels == 2;
    }

    // gets the number of ports
    private int nGetNumPorts() { return 0; }

    // gets the name of the port with this index
    private String nGetPortName(int index) { return null; }


    //private native void nDrain();
    //private native void nFlush();
    //private native long nGetPosition();
    //private native void nClose();
    //private native void nPause();
    //private native void nResume();

    // $$kk: 03.25.99: need to implement!
    //private static /*native*/ int nGetNumDevices() {
    //  return 1;
    //}
}
