/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)MixerSourceLine.java     1.34 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.media.sound;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

//import javax.sound.sampled.GainControl;
//import javax.sound.sampled.PanControl;
//import javax.sound.sampled.SampleRateControl;


/**
 * Represents a streaming channel on the Headspace mixer.
 *
 * @version 1.34, 03/01/23
 * @author Kara Kytle
 * @author Florian Bomers
 */
public class MixerSourceLine extends AbstractDataLine implements SourceDataLine {


    // FINAL PROPERTIES


    /**
     * Data buffer for this channel.  Gets created in updateParams();
     */
    private CircularBuffer circularBuffer = null;

    /**
     * Read buffer for this channel.  Gets created in updateParams();
     */
    private byte[] dataBuffer = null;

    /**
     * Engine identifier for this channel.  Gets set in implOpen().
     */
    // $$kk: this cannot be final if we want to reset it to zero....
    private long id;


    // DEFAULT STATE


    // CURRENT STATE

    // variables for saving state after stream shut-down
    private int finalPosition = 0;

    // true after we call the native stream start
    private boolean implStarted = false;

    // MuteControl uses this
    MixerSourceLineGainControl gainControl;



    /**
     * Constructor for source data lines for the HeadspaceMixer.
     * These should only be created by the HeadspaceMixer; we may want
     * to consider making this an inner class to the HeadspaceMixer to
     * guarantee this.
     */
    MixerSourceLine(DataLine.Info info, HeadspaceMixer mixer, AudioFormat format, int bufferSize) throws LineUnavailableException {

        super(info, mixer, new Control[4], format, bufferSize);

        if (Printer.trace) Printer.trace("MixerSourceLine: constructor: format: " + format + " bufferSize: " + bufferSize);

        // initialize the controls
        controls[0] = gainControl = new MixerSourceLineGainControl();
        controls[1] = new MixerSourceLineMuteControl();
        controls[2] = new MixerSourceLinePanControl();
        controls[3] = new MixerSourceLineSampleRateControl();
        //$$fb 2001-10-09: this isn't implemented!
        //controls[4] = new MixerSourceLineApplyReverbControl();
    }


    // SOURCE DATA LINE METHODS


    public int write(byte[] b, int off, int len) {

        // stop flag
        if (b == null) {

            if (Printer.verbose) Printer.verbose("> MixerSourceLine.write: b: " + b);
            if (circularBuffer != null) {
                circularBuffer.markEnd();
            }
            return 0;
        }

        if (Printer.verbose) Printer.verbose("> MixerSourceLine.write(b.length: " + b.length + " off: " + off + " len: " + len);

        int totalBytesToWrite = len;

        if (len % getFormat().getFrameSize() != 0) {
            throw new IllegalArgumentException("Illegal request to write non-integral number of frames (" + len + " bytes )");
        }

        int totalBytesWritten = 0;
        int currentBytesWritten = 0;

        // $$kk: 08.17.99: changed this to return if not running as well as if not open
        while (isOpen() && (totalBytesWritten < totalBytesToWrite)) {
            if (!isStartedRunning()) {
                Thread.yield();
                break;
            }

            currentBytesWritten = circularBuffer.write(b, off, (totalBytesToWrite - totalBytesWritten));
            totalBytesWritten += currentBytesWritten;
            off += currentBytesWritten;

            if (totalBytesWritten < totalBytesToWrite) {

                synchronized(this) {
                    try {
                        // $$kk: 08.17.99: need to make sure we never block forever here!
                        //
                        // ivg: Well, it does hang in some cases.
                        //      I can reproduce that with JMF by starting and closing
                        //      DataLines.  I suspect this lock is not released when
                        //      the DataLine is closed.  This is highly timing sensitive.
                        //      It doesn't look like we have a good case of concurrent
                        //      programming here.  I put in a time out value in the
                        //      wait so it will wake up itself to check.  Not optimal.
                        wait(2000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        if (Printer.trace) Printer.trace("< MixerSourceLine.write write: "
                                         + totalBytesWritten + " bytes or "
                                         + (totalBytesWritten / getFormat().getFrameSize() )
                                         + " frames");
        return totalBytesWritten;
    }


    public int available() {
        // return the number of bytes available
        if (circularBuffer != null) {
            return circularBuffer.bytesAvailableToWrite();
        }
        return 0;
    }


    // ABSTRACT METHOD IMPLEMENTATIONS

    // ABSTRACT LINE

    //synchronized void implOpen() throws LineUnavailableException {
    //$$fb 2001-10-09: this needn't be synchronized. The wrapping open method must be synchronized!
    // part of fix for bug #4517739: Using JMF to playback sound clips blocks virtual machine

    void implOpen(AudioFormat format, int bufferSize) throws LineUnavailableException {

        if (Printer.trace) Printer.trace(">> MixerSourceLine: implOpen");

        // create the native channel and set the engine identifier.
        // can throws LineUnavailableException

        // if our sample rate is not specified, match the mixer sample rate
        if (format.getSampleRate() == (float)AudioSystem.NOT_SPECIFIED) {
            float sampleRate = 44100.0f;
            if (mixer instanceof HeadspaceMixer) {
                sampleRate = ((HeadspaceMixer) mixer).getDefaultFormat().getSampleRate();
            }
            float frameRate = format.getFrameRate();
            if ((frameRate == (float)AudioSystem.NOT_SPECIFIED)
                || format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)
                || format.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)
                || format.getEncoding().equals(AudioFormat.Encoding.ULAW)
                || format.getEncoding().equals(AudioFormat.Encoding.ALAW)) {
                frameRate = sampleRate;
            }
            format = new AudioFormat(format.getEncoding(), sampleRate, format.getSampleSizeInBits(), format.getChannels(),
                                     format.getFrameSize(), frameRate, format.isBigEndian());
        }

        // if our buffer size is not specified, calculate a reasonable value
        if (bufferSize == AudioSystem.NOT_SPECIFIED || bufferSize<format.getFrameSize()) {
            bufferSize = calculateBufferSizeInBytes(format);
        }

        // $$kk: 03.30.00: fix for bug #4326534: "SourceDataLine fails to play if buffer size is too large."
        // if the requested buffer size is too large, adjust the value.
        int maxBufferSize = ((HeadspaceMixer)mixer).MAX_SAMPLES * format.getFrameSize() * 2;
        while (bufferSize > maxBufferSize) {
            bufferSize = bufferSize / 2;
        }

        //$$fb 2001-08-01: part of fix for bug #4326534 (flush bug)
        bufferSize-=bufferSize % format.getFrameSize();

        // determine whether we need to convert signed 8-bit data to unsigned,
        // or swap the byte order.

        boolean convertSign = false;
        boolean convertByteOrder = false;

        if ( (getFormat().getSampleSizeInBits() == 8) && (getFormat().getEncoding() == AudioFormat.Encoding.PCM_SIGNED) ) {
            convertSign = true;
        }

        if ( (getFormat().getSampleSizeInBits() > 8) && (getFormat().isBigEndian() != Platform.isBigEndian()) ) {
            convertByteOrder = true;
        }

        // create the data buffer.
        if ( (circularBuffer == null) || (circularBuffer.getByteLength() != bufferSize) ) {
            circularBuffer = new CircularBuffer(bufferSize, convertSign, convertByteOrder);
        }

        // create the read buffer.
        // $$kk: 03.16.99: need to stop copying data like this!
        if ( (dataBuffer == null) || (dataBuffer.length != bufferSize) ) {
            dataBuffer = new byte[bufferSize];
        }

        // open the line in the engine
        id = nOpen(getFormat().getSampleSizeInBits(), getFormat().getChannels(), getFormat().getSampleRate(), bufferSize);

        // success!  update the format and buffer size values
        this.format = format;
        this.bufferSize = bufferSize;

        // throw an exception if we failed
        if (id == 0) {
            throw new LineUnavailableException("Failed to allocate native stream.");
        }

        if (Printer.debug) Printer.debug("MixerSourceLine: constructor: id = " + id);
        if (Printer.trace) Printer.trace("<< MixerSourceLine: implOpen succeeded");

    }


    //$$fb 2001-10-09: this needn't be synchronized. The wrapping close() method must be synchronized!
    // part of fix for bug #4517739: Using JMF to playback sound clips blocks virtual machine
    void implClose() {

        if (Printer.trace) Printer.trace(">> MixerSourceLine: implClose");

        nClose(id);

        while (id != 0) {
            synchronized(this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }
        //$$fb 2002-07-18: fixed 4304737: SourceDataLine will not restart after it has been close()'ed, open()'ed
        implStarted = false;

        if (Printer.trace) Printer.trace("<< MixerSourceLine: implClose succeeded");
    }


    // ABSTRACT DATA LINE


    //$$fb 2001-10-09: this needn't be synchronized. The wrapping start() method must be synchronized!
    // part of fix for bug #4517739: Using JMF to playback sound clips blocks virtual machine
    void implStart() {

        if (Printer.trace) Printer.trace(">> MixerSourceLine: implStart");

        // $$kk: 04.01.99: note that GM_AudioStreamStart should be changed so that
        // it doesn't set streamPaused to FALSE.  then we won't need this awkward
        // case.
        if (implStarted == false) {

            if (Printer.debug) Printer.debug("MixerSourceLine: implStart: starting the stream");
            nStart(id);
            implStarted = true;

        } else {

            if (Printer.debug) Printer.debug("MixerSourceLine: implStart: resuming the stream");
            nResume(id);
        }

        if (Printer.trace) Printer.trace("<< MixerSourceLine: implStart succeeded");
    }


    //$$fb 2001-10-09: this needn't be synchronized. The wrapping stop() method must be synchronized!
    // part of fix for bug #4517739: Using JMF to playback sound clips blocks virtual machine
    void implStop() {

        if (Printer.trace) Printer.trace(">> MixerSourceLine: implStop");
        //flush();
        nPause(id);
        if (Printer.trace) Printer.trace("<< MixerSourceLine: implStop succeeded");
    }


    // METHOD OVERRIDES

    // ABSTRACT DATA LINE

    public float getLevel() {
        return (id != 0) ? nGetLevel(id) : (float)AudioSystem.NOT_SPECIFIED;
    }


    public void drain() {
        if (Printer.trace) Printer.trace("> MixerSourceLine.drain(). Active: "+isActive()
                                         +" isStartedRunning:"+isStartedRunning()+" isRunning:"+isRunning()
                                         +" implStarted:"+implStarted+" ID:"+getId());

        //$$fb 2001-10-09: sometimes, drain is called before even the native layer calls the
        // "isStarted" callback. Need to make sure that data is playing.
        // part of fix for bug #4517739: Using JMF to playback sound clips blocks virtual machine


        // drain data from the circular buffer
        if (circularBuffer != null) {
            if (Printer.debug) Printer.debug("Calling CircularBuffer.drain(). Active: "+isActive()
                                             +" isStartedRunning:"+isStartedRunning()+" isRunning:"+isRunning()
                                             +" implStarted:"+implStarted+" ID:"+getId());
            circularBuffer.drain();
            if (Printer.debug) Printer.debug("CircularBuffer.drain() finished. Active: "+isActive()
                                             +" isStartedRunning:"+isStartedRunning()+" isRunning:"+isRunning()
                                             +" implStarted:"+implStarted+" ID:"+getId());
        }

        //$$fb 2001-10-09: this looks strange: the native drain is only called when the line is NOT active.
        // explanation: Kara does not like the native drain implementation, that's why she implemented the
        // Java-layer drain below (with wait().) However, that fails when the isActive callback hasn't
        // come yet from the engine, i.e. for short sounds, the call to drain is too early. This drain
        // is handled correctly in the engine's native drain. Thus, to limit use of the native drain,
        // it is only used when the line is not active. When the line was never started, the native
        // drain returns immediately.
        // part of fix for bug #4517739: Using JMF to playback sound clips blocks virtual machine
        if (!isActive()) {
            // drain the native buffers
            nDrain(id);
        }

        // $$kk: 03.21.00:  i think this mechanism is better because wait() is
        // better than the pseudo-sleeping we do in the native drain method.

        //$$fb 2001-10-09: add a counter so that it does not block forever
        // part of fix for bug #4517739: Using JMF to playback sound clips blocks virtual machine
        int maxIterations=2000/50; // 2 seconds
        while (isActive() && (maxIterations--)>0) {
            synchronized(this) {
                try {
                    // $$kk: 08.17.99: need to make sure we never block forever here!
                    wait(50);
                } catch (InterruptedException e) {
                }
            }
        }
        if (Printer.trace) Printer.trace("< MixerSourceLine.drain(). Active: "+isActive()
                                         +" isStartedRunning:"+isStartedRunning()+" isRunning:"+isRunning()
                                         +" implStarted:"+implStarted+" ID:"+getId());
    }


    public void flush() {

        if (circularBuffer != null) {
            // flush data from the circular buffer
            circularBuffer.flush();
        }

        // flush the native buffers
        nFlush(id);
    }


    public int getFramePosition() {

        return (id != 0) ? (int)nGetPosition(id) : finalPosition;
    }



    // HELPER METHODS


    // need for linked streams.
    long getId() {

        return id;
    }

    /**
     * Given the requested buffer size in bytes, calculate the buffer size in bytes that gives
     * a number of frames that is the nearest greater-or-equal power of 2
     *
     *  $$kk: 04.29.99: i do not know *why* this matters, only that the sample
     * count falls behind otherwise!!!?
     */
    private static int calculateBufferSizeInBytes(AudioFormat format) {

        // choose at buffer size that is a power of 2 and least one-half second long
        // note that we are calculating in bytes here.

        // one-half second
        int requestedBufferSizeInFrames = (int)format.getFrameRate() / 2;

        // calculate the number of frames as a power of 2
        int actualBufferSizeInFrames = 1;
        while (requestedBufferSizeInFrames > actualBufferSizeInFrames) {
            actualBufferSizeInFrames *= 2;
        }

        // return the value in bytes
        return (actualBufferSizeInFrames * format.getFrameSize());
    }


    // CALLBACKS


    // called by the engine to read data from the stream.
    // $$kk: 03.16.99: need to do something to avoid all these
    // damned copies!!
    private synchronized int callbackStreamGetData(byte[] dataArray, int frameLength) {

        if (Printer.verbose) Printer.verbose("MixerSourceLine: callbackStreamGetData: dataArray.length: " + dataArray.length + " frameLength: " + frameLength);

        int frameSize = getFormat().getFrameSize();
        int byteLength = frameLength * frameSize;
        byteLength = Math.min(byteLength, dataArray.length);

        // the circular buffer will return -1 after it's reached its marked end
        int length = circularBuffer.read(dataArray, 0, byteLength);
        length = (length > 0) ? (length / frameSize) : length;

        notifyAll();

        if (Printer.verbose) Printer.verbose("MixerSourceLine: callbackStreamGetData: returning length: " + length);
        return length;
    }


    // called by the engine when it destroys the stream.
    private void callbackStreamDestroy() {

        if (Printer.trace) Printer.trace(">> MixerSourceLine: callbackStreamDestroy()");

        // save the state info
        finalPosition = (int)getFramePosition();

        // stream no longer exists in engine.  set identifier to 0.
        id = 0;

        synchronized(this) {
            notifyAll();
        }

        if (Printer.trace) Printer.trace("<< MixerSourceLine: callbackStreamDestroy() completed");
    }


    // called by the engine when it starts playing the stream.
    private void callbackStreamStart() {

        if (Printer.trace) Printer.trace(">> MixerSourceLine: callbackStreamStart()");

        setActive(true);
        setStarted(true);

        if (Printer.trace) Printer.trace("<< MixerSourceLine: callbackStreamStart() completed");
    }


    // called by the engine when it stops playing the stream.
    private void callbackStreamStop() {

        if (Printer.trace) Printer.trace(">> MixerSourceLine: callbackStreamStop()");

        setActive(false);
        setStarted(false);

        if (Printer.trace) Printer.trace("<< MixerSourceLine: callbackStreamStop() completed");
    }


    // called by the engine when it stops playing the stream because EOM is reached.
    // $$kk: 03.24.99: i'm just treating this as a "stop."  should we handle
    // EOM as a special case?
    // $$kk: 05.30.99: i think we should get rid of the EOM concept
    private void callbackStreamEOM() {

        if (Printer.trace) Printer.trace(">> MixerSourceLine: callbackStreamEOM()");

        setActive(false);
        setEOM();

        if (Printer.trace) Printer.trace("<< MixerSourceLine: callbackStreamEOM() completed");
    }


    // called by the engine when it starts playing the stream after a gap due to underflow.
    private void callbackStreamActive() {

        if (Printer.trace) Printer.trace(">> MixerSourceLine: callbackStreamActive()");

        synchronized(this) {
            setActive(true);
            notifyAll();
        }

        if (Printer.trace) Printer.trace("<< MixerSourceLine: callbackStreamActive() completed");
    }


    // called by the engine when it stops playing the stream due to underflow (results in a gap in playback).
    private void callbackStreamInactive() {

        if (Printer.trace) Printer.trace(">> MixerSourceLine: callbackStreamInactive()");


        synchronized(this) {
            setActive(false);
            notifyAll();
        }

        if (Printer.trace) Printer.trace("<< MixerSourceLine: callbackStreamInactive() completed");
    }


    // INNER CLASSES


    private class MixerSourceLineGainControl extends FloatControl {

        // STATE VARIABLES
        private float linearGain = 1.0f;

        private MixerSourceLineGainControl() {

            super(FloatControl.Type.MASTER_GAIN,
                  Toolkit.linearToDB(0.0f),
                  Toolkit.linearToDB(5.0f),
                  //$$fb 2001-10-09: fix for Bug 4385654
                  //Toolkit.linearToDB(1.0f / 128.0f),
                  Math.abs(Toolkit.linearToDB(5.0f)-Toolkit.linearToDB(0.0f))/128.0f,
                  -1,
                  0.0f,
                  "dB", "Minimum", "", "Maximum");
        }

        public void setValue(float newValue) {

            // don't cache the values unless the source line is open.  this is how streams work,
            // and it seems like a reasonable requirement.
            if (!isOpen()) {
                return;
            }

            // adjust value within range
            newValue = Math.min(newValue, getMaximum());
            newValue = Math.max(newValue, getMinimum());

            float newLinearGain = Toolkit.dBToLinear(newValue);

            if ( (newLinearGain != linearGain) && (id != 0) ) {
                newLinearGain = nSetLinearGain(id, newLinearGain);
            }

            linearGain = newLinearGain;
            super.setValue(Toolkit.linearToDB(linearGain));
        }
    } // class MixerSourceLineGainControl


    private class MixerSourceLinePanControl extends FloatControl {

        private MixerSourceLinePanControl() {

            super(FloatControl.Type.PAN,
                  -1.0f,
                  1.0f,
                  (1.0f / 64.0f),
                  -1,
                  0.0f,
                  "", "Left", "Center", "Right");
        }

        public void setValue(float newValue) {

            // don't cache the values unless the source line is open.  this is how streams work,
            // and it seems like a reasonable requirement.
            if (!isOpen()) {
                return;
            }


            // adjust value within range
            newValue = Math.min(newValue, getMaximum());
            newValue = Math.max(newValue, getMinimum());

            if ( (newValue != getValue()) && (id != 0) ) {

                                // $$kk: 04.07.99: the headspace docs say that the pan range
                                // is -63 (left) to +63 (right), but we are hearing the reverse.
                                // i'm throwing a -1 in here to compensate, since we do use -1
                                // for left and +1 for right.
                newValue = (-1.0f * nSetPan(id, (-1.0f * newValue)));
            }

            super.setValue(newValue);
        }
    } // class MixerSourceLinePanControl


    private class MixerSourceLineSampleRateControl extends FloatControl {

        private MixerSourceLineSampleRateControl() {

            super(FloatControl.Type.SAMPLE_RATE,
                  0.0f,
                  48000.0f,
                  1.0f,
                  -1,
                  getFormat().getFrameRate(),
                  "FPS", "Minimum", "", "Maximum");
        }

        public void setValue(float newValue) {

            // don't cache the values unless the source line is open.  this is how streams work,
            // and it seems like a reasonable requirement.
            if (!isOpen()) {
                return;
            }

            // adjust value within range
            newValue = Math.min(newValue, getMaximum());
            newValue = Math.max(newValue, getMinimum());

            if ( (newValue != getValue()) && (id != 0) ) {
                newValue = (float)nSetSampleRate(id, (int)newValue);
            }

            super.setValue(newValue);
        }

        // Update the sample rate to reflect the natural rate.
        // (needs to be done if the format changes.)
        private void update() {
            super.setValue(getFormat().getFrameRate());
        }
    } // class MixerSourceLineSampleRateControl


    private class MixerSourceLineMuteControl extends BooleanControl {

        private MixerSourceLineMuteControl() {
            super(BooleanControl.Type.MUTE, false, "True", "False");
        }

        public void setValue(boolean newValue) {

            // don't cache the values unless the source line is open.  this is how streams work,
            // and it seems like a reasonable requirement.
            if (!isOpen()) {
                return;
            }

            if (newValue && (!getValue()) && (id != 0) ) {

                nSetLinearGain(id, 0.0f);

            } else if ((!newValue) && (getValue()) && (id != 0)) {

                float linearGain = Toolkit.dBToLinear(gainControl.getValue());
                nSetLinearGain(id, linearGain);
            }

            super.setValue(newValue);
        }
    }  // class MixerSourceLineMuteControl


    private class MixerSourceLineApplyReverbControl extends BooleanControl {

        private MixerSourceLineApplyReverbControl() {
            super(BooleanControl.Type.APPLY_REVERB, false, "Yes", "No");
        }

        public void setValue(boolean newValue) {

            // don't cache the values unless the source line is open.  this is how streams work,
            // and it seems like a reasonable requirement.
            if (!isOpen()) {
                return;
            }

            if ( (newValue != getValue()) && (id != 0) ) {

                                /* $$kk: 10.11.99: need to implement! */
            }

            super.setValue(newValue);
        }
    }  // class MixerSourceLineApplyReverbControl


    // NATIVE METHODS

    private native void nDrain(long id);
    private native void nFlush(long id);
    private native long nGetPosition(long id);
    private native float nGetLevel(long id);

    // note that the buffer size is the total size of the data buffer in bytes; the engine will call back for buffers
    // of half this size (int bytes) during streaming.
    private native long nOpen(int sampleSizeInBits, int channels, float sampleRate, int bufferSize) throws LineUnavailableException;
    private native void nStart(long id);
    private native void nResume(long id);
    private native void nPause(long id);
    private native void nClose(long id);

    // set volume using linear scale
    // GM_AudioStreamSetVolume
    protected native float nSetLinearGain(long id, float linearGain);

    // GM_AudioStreamSetStereoPosition
    protected native float nSetPan(long id, float pan);

    // GM_AudioStreamSetRate
    protected native int nSetSampleRate(long id, int rate);
}
