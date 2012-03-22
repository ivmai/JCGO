/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/BranchContext.java --
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
 * This is a code branch context record.
 */

final class BranchContext {

    final ObjVector accessedClasses = new ObjVector();

    final ObjVector nonNullVars = new ObjVector();

    final ObjVector actTypeVars = new ObjVector();

    final ObjVector varActualTypes = new ObjVector();

    BranchContext() {
    }

    BranchContext(BranchContext oldBranch) {
        vectorAddFrom(accessedClasses, oldBranch.accessedClasses);
        vectorAddFrom(nonNullVars, oldBranch.nonNullVars);
        vectorAddFrom(actTypeVars, oldBranch.actTypeVars);
        vectorAddFrom(varActualTypes, oldBranch.varActualTypes);
    }

    ExpressionType getActualType(VariableDefinition v) {
        int index = actTypeVars.identityLastIndexOf(v);
        return index >= 0 ? (ExpressionType) varActualTypes.elementAt(index)
                : null;
    }

    void intersectWith(BranchContext otherBranch, ClassDefinition forClass) {
        intersectAccessedClasses(accessedClasses, otherBranch.accessedClasses);
        vectorIntersect(nonNullVars, otherBranch.nonNullVars);
        int count = otherBranch.actTypeVars.size();
        for (int i = 0; i < count; i++) {
            VariableDefinition v = (VariableDefinition) otherBranch.actTypeVars
                    .elementAt(i);
            int index = actTypeVars.identityLastIndexOf(v);
            if (index >= 0) {
                ExpressionType actualType = ClassDefinition.maxCommonExprOf(
                        (ExpressionType) varActualTypes.elementAt(index),
                        (ExpressionType) otherBranch.varActualTypes
                                .elementAt(i), forClass);
                if (!ClassDefinition.isAssignableFrom(v.exprType(), actualType,
                        forClass)) {
                    actualType = v.exprType();
                }
                varActualTypes.setElementAt(actualType, index);
            }
        }
        int index = 0;
        while (actTypeVars.size() > index) {
            if (otherBranch.actTypeVars.identityLastIndexOf(actTypeVars
                    .elementAt(index)) < 0) {
                actTypeVars.removeElementAt(index);
                varActualTypes.removeElementAt(index);
            } else {
                index++;
            }
        }
    }

    void unionWith(BranchContext otherBranch, boolean ignoreLocals,
            boolean isThis) {
        unionAccessedClasses(accessedClasses, otherBranch.accessedClasses);
        if (ignoreLocals) {
            Enumeration en = otherBranch.nonNullVars.elements();
            while (en.hasMoreElements()) {
                VariableDefinition v = (VariableDefinition) en.nextElement();
                if (!v.isLocalOrParam() && (isThis || v.isClassVariable())
                        && nonNullVars.identityLastIndexOf(v) < 0) {
                    nonNullVars.addElement(v);
                }
            }
        } else {
            vectorUnion(nonNullVars, otherBranch.nonNullVars);
        }
        vectorUnionDual(actTypeVars, varActualTypes, otherBranch.actTypeVars,
                otherBranch.varActualTypes, ignoreLocals, isThis);
    }

    private static void vectorUnionDual(ObjVector vect, ObjVector vect2,
            ObjVector other, ObjVector other2, boolean ignoreLocals,
            boolean isThis) {
        int count = other.size();
        for (int i = 0; i < count; i++) {
            VariableDefinition v = (VariableDefinition) other.elementAt(i);
            if ((!ignoreLocals || (!v.isLocalOrParam() && (isThis || v
                    .isClassVariable()))) && vect.identityLastIndexOf(v) < 0) {
                vect.addElement(v);
                vect2.addElement(other2.elementAt(i));
            }
        }
    }

    private static void vectorUnion(ObjVector vect, ObjVector other) {
        Enumeration en = other.elements();
        while (en.hasMoreElements()) {
            Object obj = en.nextElement();
            if (vect.identityLastIndexOf(obj) < 0) {
                vect.addElement(obj);
            }
        }
    }

    private static void vectorAddFrom(ObjVector vect, ObjVector other) {
        Enumeration en = other.elements();
        while (en.hasMoreElements()) {
            vect.addElement(en.nextElement());
        }
    }

    private static void vectorIntersect(ObjVector vect, ObjVector other) {
        int index = 0;
        while (vect.size() > index) {
            if (other.identityLastIndexOf(vect.elementAt(index)) < 0) {
                vect.removeElementAt(index);
            } else {
                index++;
            }
        }
    }

    private static void intersectAccessedClasses(ObjVector vect, ObjVector other) {
        int index = vect.size();
        while (index-- > 0) {
            Object obj = vect.elementAt(index);
            int i = other.identityLastIndexOf(obj);
            if (i < 0) {
                vect.removeElementAt(index);
            }
            if (index == 0 || vect.elementAt(index - 1) != obj) {
                if (i > 0 && other.elementAt(i - 1) == obj) {
                    vect.removeElementAt(index);
                    vect.addElement(obj);
                    vect.addElement(obj);
                }
            } else {
                index--;
                if (i < 0) {
                    vect.removeElementAt(index);
                }
            }
        }
    }

    private static void unionAccessedClasses(ObjVector vect, ObjVector other) {
        int i = other.size();
        while (i-- > 0) {
            Object obj = other.elementAt(i);
            int index = vect.identityLastIndexOf(obj);
            if (i == 0 || other.elementAt(i - 1) != obj) {
                if (index < 0) {
                    vect.addElement(obj);
                } else if (index > 0 && vect.elementAt(index - 1) == obj) {
                    vect.removeElementAt(index);
                }
            } else {
                i--;
                if (index < 0) {
                    vect.addElement(obj);
                    vect.addElement(obj);
                }
            }
        }
    }
}
