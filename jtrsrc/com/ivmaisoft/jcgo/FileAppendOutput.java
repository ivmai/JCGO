/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/FileAppendOutput.java --
 * a part of JCGO translator.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2012 Ivan Maidanski <ivmai@mail.ru>
 * All rights reserved.
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

package com.ivmaisoft.jcgo;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * This class is for writing the output files. A file may be closed and
 * re-opened again many times.
 */

final class FileAppendOutput extends OutputStream {

    private RandomAccessFile raf;

    FileAppendOutput(File f, boolean opened) throws IOException {
        raf = new RandomAccessFile(f, "rw");
        long len = raf.length();
        if ((len == 0L) == opened)
            throw new IOException();
        raf.seek(len);
    }

    public void write(int v) throws IOException {
        raf.write(v);
        Main.dict.outBytesCount++;
    }

    public void write(byte[] b, int off, int len) throws IOException {
        raf.write(b, off, len);
        Main.dict.outBytesCount += len;
    }

    public void close() throws IOException {
        raf.close();
    }
}
