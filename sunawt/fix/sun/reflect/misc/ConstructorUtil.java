/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)ConstructorUtil.java
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.reflect.misc;

import java.lang.reflect.Constructor;

public final class ConstructorUtil
{

    private ConstructorUtil() {
    }

    public static Constructor[] getConstructors(Class klass) {
        ReflectUtil.checkPackageAccess(klass);
        return klass.getConstructors();
    }

    public static Constructor getConstructor(Class klass,
                                        Class[] parameterTypes)
        throws NoSuchMethodException {
        ReflectUtil.checkPackageAccess(klass);
        return klass.getConstructor(parameterTypes);
    }
}
