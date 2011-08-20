/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)JDK13Services.java       1.16 03/03/17
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.media.sound;

import java.util.Vector;
import java.util.Iterator;
import java.security.AccessController;
import java.security.PrivilegedAction;
import sun.misc.Service;

import javax.sound.sampled.spi.AudioFileReader;
import javax.sound.sampled.spi.AudioFileWriter;
import javax.sound.sampled.spi.FormatConversionProvider;
import javax.sound.sampled.spi.MixerProvider;

import javax.sound.midi.spi.MidiFileReader;
import javax.sound.midi.spi.MidiFileWriter;
import javax.sound.midi.spi.SoundbankReader;
import javax.sound.midi.spi.MidiDeviceProvider;

/**
 * JDK13Services uses the Service class in JDK 1.3
 * to discover a list of service providers installed
 * in the system.  This class will not be loaded in
 * an environment less than JDK 1.3
 *
 * @version 1.16 03/03/17
 * @author Jan Borgersen
 */
public class JDK13Services {


    /**
     * Private, no-args constructor to ensure against instantiation
     */
    private JDK13Services() {
    }


    /**
     * Obtains a Vector containing installed instances of the
     * providers for the requested service.
     */

    public static Vector getProviders( String service ) {

        Vector v = new Vector();

        //if(Printer.trace)Printer.trace("> JDK13Services.getJDK13Providers: " + service);

        if(service.equals("javax.sound.sampled.spi.AudioFileWriter")) {

            v = (Vector) AccessController.doPrivileged(
                                                       new PrivilegedAction() {
                                                               public Object run() {

                                                                   Vector p = new Vector();
                                                                   Iterator ps = Service.providers(AudioFileWriter.class);
                                                                   while (ps.hasNext()) {
                                                                       try {
                                                                           AudioFileWriter e =
                                                                               (AudioFileWriter)ps.next();
                                                                           p.addElement(e);
                                                                       } catch (Throwable t) {
                                                                           //$$fb 2002-11-07: do not fail on SPI not found
                                                                           if (Printer.err) t.printStackTrace();
                                                                       }
                                                                   }
                                                                   return p;
                                                               }
                                                           }
                                                       );

        } else if(service.equals("javax.sound.sampled.spi.AudioFileReader")) {

            v = (Vector) AccessController.doPrivileged(
                                                       new PrivilegedAction() {
                                                               public Object run() {

                                                                   Vector p = new Vector();
                                                                   Iterator ps = Service.providers(AudioFileReader.class);
                                                                   while (ps.hasNext()) {
                                                                       try {
                                                                           AudioFileReader e =
                                                                               (AudioFileReader)ps.next();
                                                                           p.addElement(e);
                                                                       } catch (Throwable t) {
                                                                           //$$fb 2002-11-07: do not fail on SPI not found
                                                                           if (Printer.err) t.printStackTrace();
                                                                       }
                                                                   }
                                                                   return p;
                                                               }
                                                           }
                                                       );

        } else if(service.equals("javax.sound.sampled.spi.FormatConversionProvider")) {

            v = (Vector) AccessController.doPrivileged(
                                                       new PrivilegedAction() {
                                                               public Object run() {

                                                                   Vector p = new Vector();
                                                                   Iterator ps = Service.providers(FormatConversionProvider.class);
                                                                   while (ps.hasNext()) {
                                                                       try {
                                                                           FormatConversionProvider e =
                                                                               (FormatConversionProvider)ps.next();
                                                                           p.addElement(e);
                                                                       } catch (Throwable t) {
                                                                           //$$fb 2002-11-07: do not fail on SPI not found
                                                                           if (Printer.err) t.printStackTrace();
                                                                       }
                                                                   }
                                                                   return p;
                                                               }
                                                           }
                                                       );

        } else if(service.equals("javax.sound.sampled.spi.MixerProvider")) {

            v = (Vector) AccessController.doPrivileged(
                                                       new PrivilegedAction() {
                                                               public Object run() {

                                                                   Vector p = new Vector();
                                                                   Iterator ps = Service.providers(MixerProvider.class);
                                                                   while (ps.hasNext()) {
                                                                       try {
                                                                           MixerProvider e =
                                                                               (MixerProvider)ps.next();
                                                                           p.addElement(e);
                                                                       } catch (Throwable t) {
                                                                           //$$fb 2002-11-07: do not fail on SPI not found
                                                                           if (Printer.err) t.printStackTrace();
                                                                       }
                                                                   }
                                                                   return p;
                                                               }
                                                           }
                                                       );

        } else if(service.equals("javax.sound.midi.spi.MidiDeviceProvider")) {

            v = (Vector) AccessController.doPrivileged(
                                                       new PrivilegedAction() {
                                                               public Object run() {

                                                                   Vector p = new Vector();
                                                                   Iterator ps = Service.providers(MidiDeviceProvider.class);
                                                                   while (ps.hasNext()) {
                                                                       try {
                                                                           MidiDeviceProvider e =
                                                                               (MidiDeviceProvider)ps.next();
                                                                           p.addElement(e);
                                                                       } catch (Throwable t) {
                                                                           //$$fb 2002-11-07: do not fail on SPI not found
                                                                           if (Printer.err) t.printStackTrace();
                                                                       }
                                                                   }
                                                                   return p;
                                                               }
                                                           }
                                                       );

        } else if(service.equals("javax.sound.midi.spi.MidiFileWriter")) {

            v = (Vector) AccessController.doPrivileged(
                                                       new PrivilegedAction() {
                                                               public Object run() {

                                                                   Vector p = new Vector();
                                                                   Iterator ps = Service.providers(MidiFileWriter.class);
                                                                   while (ps.hasNext()) {
                                                                       try {
                                                                           MidiFileWriter e =
                                                                               (MidiFileWriter)ps.next();
                                                                           p.addElement(e);
                                                                       } catch (Throwable t) {
                                                                           //$$fb 2002-11-07: do not fail on SPI not found
                                                                           if (Printer.err) t.printStackTrace();
                                                                       }
                                                                   }
                                                                   return p;
                                                               }
                                                           }
                                                       );

        } else if(service.equals("javax.sound.midi.spi.MidiFileReader")) {

            v = (Vector) AccessController.doPrivileged(
                                                       new PrivilegedAction() {
                                                               public Object run() {

                                                                   Vector p = new Vector();
                                                                   Iterator ps = Service.providers(MidiFileReader.class);
                                                                   while (ps.hasNext()) {
                                                                       try {
                                                                           MidiFileReader e =
                                                                               (MidiFileReader)ps.next();
                                                                           p.addElement(e);
                                                                       } catch (Throwable t) {
                                                                           //$$fb 2002-11-07: do not fail on SPI not found
                                                                           if (Printer.err) t.printStackTrace();
                                                                       }
                                                                   }
                                                                   return p;
                                                               }
                                                           }
                                                       );

        } else if(service.equals("javax.sound.midi.spi.SoundbankReader")) {

            v = (Vector) AccessController.doPrivileged(
                                                       new PrivilegedAction() {
                                                               public Object run() {

                                                                   Vector p = new Vector();
                                                                   Iterator ps = Service.providers(SoundbankReader.class);
                                                                   while (ps.hasNext()) {
                                                                       try {
                                                                           SoundbankReader e =
                                                                               (SoundbankReader)ps.next();
                                                                           p.addElement(e);
                                                                       } catch (Throwable t) {
                                                                           //$$fb 2002-11-07: do not fail on SPI not found
                                                                           if (Printer.err) t.printStackTrace();
                                                                       }                                                                   }
                                                                   return p;
                                                               }
                                                           }
                                                       );

        }
        //if(Printer.trace)Printer.trace("> JDK13Services.getJDK13Providers done ");
        if (v.size() == 0)
            v = DefaultServices.getProviders(service);
        return v;

    }
}
