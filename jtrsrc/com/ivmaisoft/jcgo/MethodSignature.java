/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/MethodSignature.java --
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

import java.util.Enumeration;

/**
 * A method signature.
 */

final class MethodSignature {

    private String id;

    private ObjVector sign;

    private String jsign;

    private String csign;

    MethodSignature(String id, ObjVector sign) {
        this.id = id;
        this.sign = sign;
    }

    String signatureString() {
        return id + getJavaSignature();
    }

    String getInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append(id);
        sb.append('(');
        boolean next = false;
        Enumeration en = elements();
        while (en.hasMoreElements()) {
            if (next) {
                sb.append(',');
            }
            sb.append(((ExpressionType) en.nextElement()).name());
            next = true;
        }
        sb.append(')');
        return sb.toString();
    }

    String getJavaSignature() {
        if (jsign == null) {
            StringBuffer sb = new StringBuffer();
            sb.append('(');
            Enumeration en = elements();
            while (en.hasMoreElements()) {
                sb.append(((ExpressionType) en.nextElement())
                        .getJavaSignature());
            }
            sb.append(')');
            jsign = sb.toString();
        }
        return jsign;
    }

    public String csign(ClassDefinition ourClass, String classCName) {
        if (csign == null) {
            StringBuffer sb = new StringBuffer();
            sb.append("__");
            Enumeration en = elements();
            while (en.hasMoreElements()) {
                sb.append(((ExpressionType) en.nextElement()).csign());
            }
            csign = Main.dict.nameMapper.methodToCSign(
                    id.equals("<init>") ? "this" : id,
                    getJavaSignature(),
                    sb.toString(),
                    ourClass != null ? ourClass
                            .countHiddenMethods(signatureString()) : 0,
                    classCName);
        }
        return csign;
    }

    public String csignForNew(ClassDefinition ourClass) {
        String str = csign(null, ourClass.castName());
        if (str.startsWith("this__")) {
            str = "new" + str.substring(4);
        }
        return str;
    }

    String getJniNameNoPrefix(ClassDefinition ourClass) {
        StringBuffer sb = new StringBuffer();
        sb.append(ourClass.jniClassName());
        sb.append('_');
        sb.append(Main.dict.nameMapper.nameToJniName(id));
        if (ourClass.hasNativeIdCollision(id)) {
            sb.append("__");
            Enumeration en = elements();
            while (en.hasMoreElements()) {
                ExpressionType expr = (ExpressionType) en.nextElement();
                int dims = expr.signatureDimensions();
                while (dims-- > 0) {
                    sb.append("_3");
                }
                ClassDefinition cd = expr.signatureClass();
                if (cd.objectSize() == Type.CLASSINTERFACE) {
                    sb.append('L');
                    sb.append(cd.jniClassName());
                    sb.append("_2");
                } else {
                    sb.append(cd.csign());
                }
            }
        }
        return sb.toString();
    }

    Enumeration elements() {
        return sign.elements();
    }

    int paramCount() {
        return sign.size();
    }

    boolean isSignEqual(ObjVector parmSig) {
        int size = sign.size();
        if (parmSig.size() != size)
            return false;
        for (int i = 0; i < size; i++) {
            if (!sign.elementAt(i).equals(parmSig.elementAt(i)))
                return false;
        }
        return true;
    }

    int match(MethodSignature msig, ClassDefinition forClass) {
        int res = 0;
        int size = sign.size();
        if (msig.sign.size() != size || !id.equals(msig.id))
            return -1 >>> 1;
        for (int i = 0; i < size; i++) {
            ExpressionType formal = (ExpressionType) sign.elementAt(i);
            ExpressionType actparm = (ExpressionType) msig.sign.elementAt(i);
            ClassDefinition formalClass = formal.signatureClass();
            ClassDefinition actparmClass = actparm.signatureClass();
            int formalSize = formalClass.objectSize();
            int actparmSize = actparmClass.objectSize();
            if (formalSize != Type.CLASSINTERFACE
                    && actparmSize != Type.CLASSINTERFACE) {
                if (actparmSize != Type.NULLREF) {
                    if (actparm.signatureDimensions() != formal
                            .signatureDimensions())
                        return -1 >>> 1;
                    if (actparmSize == formalSize)
                        continue;
                    if (actparmSize > formalSize
                            || actparmSize == Type.BOOLEAN
                            || (actparmSize == Type.BYTE && formalSize == Type.CHAR)
                            || (actparmSize == Type.CHAR && formalSize == Type.SHORT))
                        return -1 >>> 1;
                } else if (formal.signatureDimensions() == 0)
                    return -1 >>> 1;
                res++;
            } else if (formalSize == Type.CLASSINTERFACE
                    && actparmSize == Type.CLASSINTERFACE) {
                if (actparm.signatureDimensions() != formal
                        .signatureDimensions()) {
                    if (!formalClass.isObjectOrCloneable())
                        return -1 >>> 1;
                    res += 0x100;
                    continue;
                }
                if (actparmClass == formalClass)
                    continue;
                int depth;
                if (formalClass.isInterface()) {
                    depth = formalClass.getImplementedByDepth(actparmClass,
                            forClass);
                    if (depth > 0) {
                        res += depth + 1;
                        continue;
                    }
                }
                if (actparmClass.isInterface()) {
                    depth = actparmClass.getImplementedByDepth(formalClass,
                            forClass);
                    if (depth > 0) {
                        res += depth + 1;
                        continue;
                    }
                }
                depth = actparmClass.getSubclassDepth(formalClass, forClass);
                if (depth <= 0)
                    return -1 >>> 1;
                res += depth;
            } else if (actparm.signatureDimensions() > 0) {
                if (!formalClass.isObjectOrCloneable())
                    return -1 >>> 1;
                res += 0x100;
            } else {
                if (actparmSize != Type.NULLREF
                        || formalSize != Type.CLASSINTERFACE)
                    return -1 >>> 1;
                res += 0x1000;
            }
        }
        return res;
    }
}
