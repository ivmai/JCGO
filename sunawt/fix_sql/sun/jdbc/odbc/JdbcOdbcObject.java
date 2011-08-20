/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)JdbcOdbcObject.java      1.35 01/12/03
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

//----------------------------------------------------------------------------
//
// Module:      JdbcOdbcObject.java
//
// Description: Base class for all JdbcOdbc Classes.  This class implements
//              the tracing facility.
//
// Product:     JDBCODBC (Java DataBase Connectivity using
//              Open DataBase Connectivity)
//
// Author:      Karl Moss
//
// Date:        March, 1996
//
//----------------------------------------------------------------------------

package sun.jdbc.odbc;

import java.sql.*;
import sun.io.*;

public class JdbcOdbcObject extends Object {

        //--------------------------------------------------------------------
        // Constructor
        // Perform any necessary initialization.
        //--------------------------------------------------------------------

        public JdbcOdbcObject ()
        {
        }
        //--------------------------------------------------------------------
        // dumpByte
        // Dumps the given byte array to the tracing output stream.  Both
        // hex and ascii are traced
        //--------------------------------------------------------------------

        protected static void dumpByte (
                byte b[],
                int len)
        {
                int line;
                int i;
                String s;
                int offset;
                String asciiLine;

                //trace ("Dump (" + len + " bytes):");

        // Loop for each required line

                for (line = 0; (line * 16) < len; line++) {

                        // Dump buffer offset
                        s = toHex (line * 16);

                        // Trim hex string (take off 0x) and pad to 8
                        // characters

                        //trace (" " + hexPad (s, 8) + "  ", false);
                        asciiLine = "";

                        // Create hex portion
                        for (i=0; i < 16; i++) {
                                offset = (line * 16) + i;

                                // Past the end of the buffer, output spaces
                                if (offset >= len) {
                                        s = "  ";
                                        asciiLine += " ";
                                }
                                else {
                                        s = toHex (b[offset]);
                                        s = hexPad (s, 2);
                                        if (b[offset] < 32) {
                                                asciiLine += ".";
                                        }
                                        else {
                                                asciiLine += new String (b, offset, 1);
                                        }
                                }
                                //trace (s + " ", false);
                        }

                        //trace ("   " + asciiLine);
                }
        }

        //--------------------------------------------------------------------
        // hexPad
        // Trim hex string (take off 0x) and pad (left justifying) to the
        // given number of characters
        //--------------------------------------------------------------------
        public static String hexPad (
                String inString,
                int toLen)
        {
                // If the input string is not hex, just return it

                if (!inString.startsWith ("0x")) {
                        return inString;
                }

                // Remove first 2 characters (0x)

                String s = inString.substring (2);
                int l = s.length ();

                // If we have more string than we want, truncate it
                if (l > toLen) {
                        s = s.substring (l - toLen);
                }
                else if (l < toLen) {
                        // We need to pad
                        String z = "0000000000000000";
                        String work = z.substring(0,toLen - l) + s;
                        s = work;
                }
                s = s.toUpperCase ();
                return s;
        }

        //--------------------------------------------------------------------
        // toHex
        // Convert the given int to a hex string
        //--------------------------------------------------------------------
        public static String toHex (
                int n)
        {
                char c[] = new char[8];
                String digits = "0123456789ABCDEF";
                byte oneByte;

                // Loop for each byte

                for (int i = 0; i < 4; i++) {
                        oneByte = (byte) (n & 0xFF);
                        c[6 - (i * 2)] = digits.charAt ((oneByte >> 4) & 0x0F);
                        c[7 - (i * 2)] = digits.charAt (oneByte & 0x0F);

                        // Shift over

                        n >>= 8;
                }

                return "0x" + new String (c);
        }

        //--------------------------------------------------------------------
        // hexStringToByteArray
        // Converts a hex string into a byte array.  It is assumed that
        // 2 hex characters make up 1 byte.
        //--------------------------------------------------------------------
        public static byte[] hexStringToByteArray (
                String inString)
                throws java.lang.NumberFormatException
        {
                byte b[];
                int fromLen = inString.length ();
                int toLen = (fromLen + 1) / 2;

                // Allocate the byte array
                b = new byte[toLen];

                // Loop through the string and convert each character
                // pair into a single byte value

                for (int i = 0; i < toLen; i++) {
                        b[i] = (byte) hexPairToInt (
                                inString.substring (i * 2, (i + 1) * 2));
                }
                return b;
        }

        //--------------------------------------------------------------------
        // hexPairToInt
        // Converts a 2 character hexadecimal pair into an integer (the
        // first 2 characters of the string are used)
        //--------------------------------------------------------------------

        public static int hexPairToInt (
                String inString)
                throws java.lang.NumberFormatException
        {
                String digits = "0123456789ABCDEF";
                String s = inString.toUpperCase ();
                int n = 0;
                int thisDigit = 0;
                int sLen = s.length ();

                if (sLen > 2) {
                        sLen = 2;
                }

                // Loop through both digits

                for (int i = 0; i < sLen; i++) {
                        thisDigit = digits.indexOf (s.substring (i, i + 1));

                        // Invalid hex character
                        if (thisDigit < 0) {
                                throw new java.lang.NumberFormatException ();
                        }

                        if (i == 0) {
                                thisDigit *= 0x10;
                        }
                        n += thisDigit;
                }
                return n;
        }

        //--------------------------------------------------------------------
        // BytesToChars
      // Converts an array of bytes into the user input char array.
        //--------------------------------------------------------------------
        public String BytesToChars (String charSet, byte[] inBytes)
                throws java.io.UnsupportedEncodingException
        {
        String retString = new String(inBytes, charSet);
        int ix = retString.indexOf('\0');
        return ix >= 0 ? retString.substring(0, ix) : retString;

        /* String retString=new String();

        // Set up output buffer.
        int outBufLen = 300;
        char[] outBuf = new char [outBufLen];

        // Set up input buffer and index.
        int inBufLen = 300;
        byte[] inBuf = new byte [inBufLen];
        int inStart = 0;

        ByteToCharConverter toUnicode = ByteToCharConverter.getConverter (charSet);

        // Use the ByteToCharConverter's substitution mode.
        // Substitute '?' for unknown characters.
        char[] subChars = { '?' };
        toUnicode.setSubstitutionMode(true);
        toUnicode.setSubstitutionChars(subChars);

        // If we don't know where this converter is coming from,
        // it's a good idea to reset it.
        toUnicode.reset();
        // Read an input buffer's worth.
        int indx=0;

        // Fix for 4400343, 4486489

        while (true) {

            for (int j=0; indx < (inBytes.length) && j < 300; j++) {
                inBuf[j]=inBytes[indx];
                inBufLen=j;
                indx++;
            }

            try {
                // convert it.
                toUnicode.convert(inBuf, inStart, inBufLen+1, outBuf, 0,
                    outBufLen);
                inStart = 0;

            } catch (MalformedInputException e) {
                // Throw away bad input.
                inStart += toUnicode.getBadInputLength();

            } catch (ConversionBufferFullException e) {
                System.err.println(e);
                inStart = toUnicode.nextByteIndex();

            } catch (sun.io.UnknownCharacterException e) {
                System.err.println(e);
                outBuf[inStart]='?';
                inStart = toUnicode.getBadInputLength();

            } finally {
                int index = toUnicode.nextCharIndex();
                String tmp = new String(outBuf, 0, index);
                retString = (retString + tmp);

                if ( indx >= inBytes.length ) {
                    break;
                }
            }
        }

        //The following trims off extra space on the end from the conversion above
        char[] bTmp = retString.toCharArray();
        int ix;
        byte tempo;
        //boolean NotNull=true;
        //find the first null character
        for (ix=0; ix < bTmp.length; ix++)
        {
                //tempo=(byte)bTmp[ix];
                //if (tempo==0) NotNull=false;
                if (bTmp[ix] == Character.MIN_VALUE)
                {
                        break;
                }
        }
        //create a new string of the propper length
        char[] bTmp2 = new char[ix];
        System.arraycopy(bTmp, 0, bTmp2, 0, ix);
        retString=new String(bTmp2);
        return retString; */
        }

        //--------------------------------------------------------------------
        // CharsToBytes
        // Converts an array of chars into the user input type of byte array.
        //--------------------------------------------------------------------

        public byte[] CharsToBytes (String charSet, char[] inChars)
                throws java.io.UnsupportedEncodingException
        {
        byte[] bytes = new String(inChars).getBytes(charSet);
        byte[] retBytes = new byte [bytes.length + 1];
        System.arraycopy(bytes, 0, retBytes, 0, bytes.length);

        /* byte[] retBytes = new byte [0];  //need to get length data

        // Set up output buffer.
        int outBufLen = 900;
        byte[] outBuf = new byte [outBufLen];

        // Set up input buffer and index.
        int inBufLen = 300;
        char[] inBuf = new char [inBufLen];
        int inStart = 0;

        CharToByteConverter toBytes = CharToByteConverter.getConverter (charSet);

        // Use the CharToByteConverter's substitution mode.
        // Substitute '?' for unknown characters.
//      char[] subChars = { '?' };
//      toBytes.substitution(true);
//      toBytes.SetSubBytes(0x3F); //'?' is the sub byte.

        // If we don't know where this converter is coming from,
        // it's a good idea to reset it.
        toBytes.reset();

        // Read an input buffer's worth.
        int indx=0;
        int j=0;
        boolean addNull = false;
        while (!addNull) {

                if (inChars.length - indx < 300)
                        addNull = true;

                for (j=0; indx < inChars.length && j < 300; j++) {
                        inBuf[j]=inChars[indx];
                        indx++;
                }
                inBufLen=j;

                if (addNull){
                        inBuf[j]=0;
                        inBufLen++;
                }

                try {// try to convert it.
                        toBytes.convert(inBuf, inStart, inBufLen, outBuf, 0, outBufLen);
                        inStart = 0;

                } catch (MalformedInputException e) {
                        // Throw away bad input.
                        inStart += toBytes.getBadInputLength();

                } catch(ConversionBufferFullException e) {
                        inStart = toBytes.nextCharIndex();

                } catch (sun.io.UnknownCharacterException e) {
                        System.err.println(e);
                        outBuf[inStart]=0x3F;
                        inStart = toBytes.getBadInputLength();

                } finally {
                        //concat converted array onto return array
                        //byte[] tRetBytes=new byte[retBytes.length + toBytes.nextCharIndex()];
                        //fix for sun bug 4215746 (tracker id 2227)
                        byte[] tRetBytes=new byte[retBytes.length + toBytes.nextByteIndex()];
                        System.arraycopy(retBytes, 0, tRetBytes, 0, retBytes.length);
                        //fix for sun bug 4215746 (tracker id 2227)
                        //System.arraycopy(outBuf, 0, tRetBytes, retBytes.length, toBytes.nextCharIndex());
                        System.arraycopy(outBuf, 0, tRetBytes, retBytes.length, toBytes.nextByteIndex());
                        retBytes=new byte [tRetBytes.length];
                        System.arraycopy(tRetBytes, 0, retBytes, 0, tRetBytes.length);
                }
        }*/

        return retBytes;
        }

}
