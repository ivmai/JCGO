/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)DefaultServices.java     1.14 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.media.sound;

import java.util.Vector;

/**
 * $$fb I can't see any usage of this class!
 * Removed of compilation for 1.4.2. The entire file should
 * be removed for 1.5.0
 *
 * DefaultServices returns hardcoded lists of our default
 * providers.  This class is used if the JDK 1.3 Service
 * mechanism isn't present in the current system.
 *
 * @version 1.14 03/01/23
 * @author Jan Borgersen
 */
public class DefaultServices {


    /**
     * Private, no-args constructor to ensure against instantiation
     */
    private DefaultServices() {
    }


    /**
     * Obtains a Vector containing instances of the
     * default providers for the requested service.
     */

    public static Vector getProviders( String service ) {

        Vector v = new Vector();

        if(Printer.trace)Printer.trace("> DefaultServices.getDefaultProviders: " + service);

        if(service.equals("javax.sound.sampled.spi.AudioFileWriter")) {

            v.addElement( new AuFileWriter() );
            v.addElement( new AiffFileWriter() );
            v.addElement( new WaveFileWriter() );

        } else if(service.equals("javax.sound.sampled.spi.AudioFileReader")) {

            v.addElement( new AuFileReader() );
            v.addElement( new AiffFileReader() );
            v.addElement( new WaveFileReader() );

        } else if(service.equals("javax.sound.sampled.spi.FormatConversionProvider")) {

            v.addElement( new UlawCodec() );
            v.addElement( new AlawCodec() );
            v.addElement( new PCMtoPCMCodec() );


        } else if(service.equals("javax.sound.sampled.spi.MixerProvider")) {

            v.addElement( new HeadspaceMixerProvider() );

            addSpecificProvider(v, "com.sun.media." + /* hack */
                                "sound.DirectAudioDeviceProvider");

            v.addElement( new SimpleInputDeviceProvider() );
            v.addElement( new SimpleOutputDeviceProvider() );

            v.addElement( new PortMixerProvider() );

        } else if(service.equals("javax.sound.midi.spi.MidiDeviceProvider")) {

            v.addElement( new MixerSynthProvider() );
            v.addElement( new MixerSequencerProvider() );
            v.addElement( new MidiInDeviceProvider() );
            v.addElement( new MidiOutDeviceProvider() );

        } else if(service.equals("javax.sound.midi.spi.MidiFileWriter")) {

            v.addElement( new StandardMidiFileWriter() );

        } else if(service.equals("javax.sound.midi.spi.MidiFileReader")) {

            v.addElement( new StandardMidiFileReader() );
            v.addElement( new RmfFileReader() );

        } else if(service.equals("javax.sound.midi.spi.SoundbankReader")) {

            v.addElement( new HsbParser() );

        }

        if(Printer.trace)Printer.trace("> DefaultServices.getDefaultProviders done ");
        return v;

    }

    private static void addSpecificProvider(Vector v, String classname) {
            try {
                v.addElement(Class.forName(classname).newInstance());
            } catch (Exception e) {
                /* ignored */
            } catch (UnsatisfiedLinkError e) {
                /* ignored */
            }
    }
}
