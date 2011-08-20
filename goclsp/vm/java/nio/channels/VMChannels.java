/*
 * @(#) $(JCGO)/goclsp/vm/java/nio/channels/VMChannels.java --
 * VM specific methods for "Channels" class.
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

package java.nio.channels;

import gnu.java.nio.ChannelInputStream;
import gnu.java.nio.ChannelOutputStream;
import gnu.java.nio.FileChannelImpl;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.VMAccessorJavaIo;

final class VMChannels
{

 private VMChannels() {}

 static InputStream newInputStream(ReadableByteChannel ch)
 {
  if (ch instanceof FileChannelImpl)
   return VMAccessorJavaIo.newFileInputStream((FileChannelImpl) ch);
  return new ChannelInputStream(ch);
 }

 static OutputStream newOutputStream(WritableByteChannel ch)
 {
  if (ch instanceof FileChannelImpl)
   return VMAccessorJavaIo.newFileOutputStream((FileChannelImpl) ch);
  return new ChannelOutputStream(ch);
 }
}
