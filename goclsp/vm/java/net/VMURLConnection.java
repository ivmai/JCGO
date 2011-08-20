/*
 * @(#) $(JCGO)/goclsp/vm/java/net/VMURLConnection.java --
 * VM specific code for "URLConnection".
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2007 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 **
 * Class specification origin: GNU Classpath v0.93 vm/reference
 */

/*
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 **
 * This software is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License (GPL) for more details.
 **
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 **
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module. An independent module is a module which is not derived from
 * or based on this library. If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

package java.net;

import java.io.IOException;
import java.io.InputStream;

final class VMURLConnection
{

 private VMURLConnection() {}

 static String guessContentTypeFromStream(InputStream is)
  throws IOException
 {
  if (is.markSupported())
  {
   is.mark(11);
   int c1 = is.read();
   int c2 = is.read();
   int c3 = is.read();
   int c4 = is.read();
   int c5 = is.read();
   int c6 = is.read();
   int c7 = is.read();
   int c8 = is.read();
   int c9 = is.read();
   int c10 = is.read();
   int c11 = is.read();
   is.reset();
   if (c1 == 0xca && c2 == 0xfe && c3 == 0xba && c4 == 0xbe)
    return "application/java-vm";
   if (c1 == 0xac && c2 == 0xed)
    return "application/x-java-serialized-object";
   if (c1 == '<')
   {
    if (c2 == '!' || (c2 == 'h' && ((c3 == 't' && c4 == 'm' && c5 == 'l') ||
        (c3 == 'e' && c4 == 'a' && c5 == 'd'))) || (c2 == 'H' &&
        ((c3 == 'T' && c4 == 'M' && c5 == 'L') || (c3 == 'E' && c4 == 'A' &&
        c5 == 'D'))) || (c2 == 'b' && c3 == 'o' && c4 == 'd' && c5 == 'y') ||
        (c2 == 'B' && c3 == 'O' && c4 == 'D' && c5 == 'Y'))
     return "text/html";
    if (c2 == '?' && c3 == 'x' && c4 == 'm' && c5 == 'l' && c6 == ' ')
     return "application/xml";
   }
   if ((c1 == 0xfe && c2 == 0xff && c3 == 0 && c4 == '<' && c5 == 0 &&
       c6 == '?' && c7 == 0 && c8 == 'x') || (c1 == 0xff && c2 == 0xfe &&
       c3 == '<' && c4 == 0 && c5 == '?' && c6 == 0 && c7 == 'x' && c8 == 0))
    return "application/xml";
   if (c1 == 'G' && c2 == 'I' && c3 == 'F' && c4 == '8')
    return "image/gif";
   if ((c1 == '#' && c2 == 'd' && c3 == 'e' && c4 == 'f') || (c1 == '!' &&
       c2 == ' ' && c3 == 'X' && c4 == 'P' && c5 == 'M' && c6 == '2'))
    return "image/x-bitmap";
   if (c1 == 0x89 && c2 == 'P' && c3 == 'N' && c4 == 'G' && c5 == 0xd &&
       c6 == 0xa && c7 == 0x1a && c8 == 0xa)
    return "image/png";
   if (c1 == 0xff && c2 == 0xd8 && c3 == 0xff)
   {
    if (c4 == 0xe0 || (c4 == 0xe1 && c7 == 'E' && c8 == 'x' && c9 == 'i' &&
        c10 =='f' && c11 == 0))
     return "image/jpeg";
    if (c4 == 0xee)
     return "image/jpg";
   }
   if ((c1 == '.' && c2 == 's' && c3 == 'n' && c4 == 'd') ||
       (c1 == 'd' && c2 == 'n' && c3 == 's' && c4 == '.'))
    return "audio/basic";
   if (c1 == 'R' && c2 == 'I' && c3 == 'F' && c4 == 'F')
    return "audio/x-wav";
  }
  return null;
 }
}
