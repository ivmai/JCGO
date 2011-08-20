/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)FieldUtil.java
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package sun.reflect.misc;

import java.lang.reflect.Field;

public final class FieldUtil
{

    private FieldUtil() {
    }

    public static Field[] getFields(Class klass) {
        ReflectUtil.checkPackageAccess(klass);
        return klass.getFields();
    }

    public static Field getField(Class klass, String name)
        throws NoSuchFieldException {
        ReflectUtil.checkPackageAccess(klass);
        return klass.getField(name);
    }
}
