/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)DebugHelper.java.m4      1.8 01/05/16
 *
 * Copyright 1998 by Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Sun Microsystems, Inc. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Sun.
 *
 */

/*
 * This class is produced by using the m4 preprocessor to produce
 * a .java file containing debug or release versions of the
 * DebugHelper class.
 */

package sun.awt;

import java.lang.reflect.*;
import java.util.*;

public abstract class DebugHelper {
    static {
        NativeLibLoader.loadLibraries();
    }

    /* name the DebugHelper member var must be declared as */
    protected static final String       DBG_FIELD_NAME = "dbg";
    protected static final String       DBG_ON_FIELD_NAME = "on";

//ifdef(`SUN_AWT_DEBUG_CLASSES', `
/* DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG */
/*    public boolean              on;

    static void init() {
        DebugHelperImpl.initGlobals();
    }

    public static final DebugHelper create(Class classToDebug) {
        return DebugHelperImpl.factoryCreate(classToDebug);
    }*/
/* DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG DEBUG */
//',`
/* RELEASE RELEASE RELEASE RELEASE RELEASE RELEASE RELEASE RELEASE */
    public static final boolean         on = false;
    private static final DebugHelper    dbgStub = new DebugHelperStub();

    static void init() {
        // nothing to do in release mode
    }

    public static final DebugHelper create(Class classToDebug) {
        return dbgStub;
    }
/* RELEASE RELEASE RELEASE RELEASE RELEASE RELEASE RELEASE RELEASE */
//')

    public abstract void setAssertOn(boolean enabled);
    public abstract void setTraceOn(boolean enabled);
    public abstract void setDebugOn(boolean enabled);
    public abstract void println(Object object);
    public abstract void print(Object object);
    public abstract void printStackTrace();
    public abstract void assertion(boolean expr);
    public abstract void assertion(boolean expr, String msg);
}

final class DebugHelperStub extends DebugHelper
{
    /* stub methods for production builds */
    public void setAssertOn(boolean enabled) {}
    public void setTraceOn(boolean enabled) {}
    public void setDebugOn(boolean enabled) {}
    public void println(Object object) {}
    public void print(Object object) {}
    public void printStackTrace() {}
    public void assertion(boolean expr) {}
    public void assertion(boolean expr, String msg) {}
}
