/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/CFormattedStream.java --
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * This is the output C formatter class.
 */

final class CFormattedStream {

    static/* final */int CHARBUF_SIZE = 0x200;

    static/* final */int BYTEBUF_SIZE = 0x400;

    static/* final */int LINEWRAP_MINPOS = 66;

    private static final String nlStr = System.getProperty("line.separator",
            "\n");

    private PrintStream out;

    private String filename;

    private final char[] buffer = new char[CHARBUF_SIZE];

    private int bufpos;

    private boolean opened;

    private int columnNum;

    private int tabPos;

    private char lastCh;

    private boolean inString;

    private boolean newlineOnNext;

    private boolean isHashSeen;

    CFormattedStream(String filename) {
        this.filename = filename;
        (new File(Main.dict.outPath, filename)).delete();
    }

    void fileClose() {
        if (out != null) {
            out.close();
            if (out.checkError())
                throw new TranslateException("File write error!");
            out = null;
        }
    }

    void close() {
        if (out == null) {
            if (bufpos == 0)
                return;
            fileOpen();
        }
        Term.assertCond(columnNum == 0);
        char[] buf = buffer;
        if (bufpos < CHARBUF_SIZE) {
            buf = new char[bufpos];
            System.arraycopy(buffer, 0, buf, 0, bufpos);
        }
        out.print(buf);
        bufpos = 0;
        fileClose();
    }

    void print(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            print(s.charAt(i));
        }
    }

    private void print(char ch) {
        if (ch == '\003') {
            inString = !inString;
            return;
        }
        columnNum++;
        switch (ch) {
        case '\n':
            columnNum = 0;
            isHashSeen = false;
            newlineOnNext = false;
            writeRaw(nlStr.charAt(0));
            if (nlStr.length() > 1) {
                writeRaw(nlStr.charAt(1));
            }
            break;
        case '#':
            if (!isHashSeen && !inString) {
                isHashSeen = true;
                if (columnNum > 1) {
                    newlineOnNext = true;
                }
                flushIndent();
            }
            writeRaw('#');
            break;
        case ',':
            writeRaw(',');
            break;
        case ';':
            if (lastCh == ';' && !inString)
                break;
            if (lastCh == '\010' || lastCh == '{'
                    || (lastCh == '}' && columnNum > 1)) {
                flushIndent();
            }
            /* fall through */
        case ':':
            writeRaw(ch);
            if (isHashSeen)
                break;
            /* fall through */
        case '\010': /* '\n' with indentation */
            isHashSeen = false;
            if (!inString) {
                newlineOnNext = true;
            }
            break;
        case '{':
            if (!inString) {
                newlineOnNext = true;
                flushIndent();
            }
            writeRaw('{');
            if (!inString) {
                tabPos++;
                newlineOnNext = true;
            }
            break;
        case '}':
            if (!inString) {
                tabPos--;
                newlineOnNext = true;
                flushIndent();
            }
            writeRaw('}');
            if (!inString) {
                newlineOnNext = true;
            }
            break;
        case ' ':
            if (!inString && !isHashSeen) {
                if (columnNum >= LINEWRAP_MINPOS) {
                    newlineOnNext = true;
                }
                flushIndent();
                writeRaw(' ');
                break;
            }
            /* fall through */
        default:
            if (!isHashSeen) {
                flushIndent();
            }
            writeRaw(ch);
            break;
        }
        lastCh = ch;
    }

    private void flushIndent() {
        if (newlineOnNext && !inString) {
            writeRaw(nlStr.charAt(0));
            if (nlStr.length() > 1) {
                writeRaw(nlStr.charAt(1));
            }
            columnNum = 0;
            if (!isHashSeen) {
                for (int i = 0; i < tabPos; i++) {
                    writeRaw(' ');
                    columnNum++;
                }
            }
            newlineOnNext = false;
        }
    }

    private void fileOpen() {
        File f = new File(Main.dict.outPath, filename);
        try {
            out = new PrintStream(new BufferedOutputStream(
                    new FileAppendOutput(f, opened), BYTEBUF_SIZE));
            if (!opened) {
                opened = true;
                out.print("/* DO NOT EDIT THIS FILE - it is machine generated (");
                out.print(Main.VER_ABBR);
                out.print(") */");
                out.print(nlStr);
                out.print(nlStr);
                Main.dict.outFilesCount++;
            }
        } catch (IOException e) {
            System.err.println("Could not create file: " + f.getPath());
            System.exit(4);
        }
    }

    private void writeRaw(char ch) {
        int bufpos = this.bufpos;
        if (bufpos == CHARBUF_SIZE) {
            if (out == null) {
                fileOpen();
            }
            out.print(buffer);
            bufpos = 0;
        }
        buffer[bufpos] = ch;
        this.bufpos = bufpos + 1;
    }
}
