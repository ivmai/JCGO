/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/QualifiedName.java --
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
 * Grammar production for a qualified or simple name.
 ** 
 * Formats: ID Empty QualifiedName DOT ID
 */

final class QualifiedName extends LexNode {

    private ClassDefinition callerClass;

    private final ObjVector resultFields = new ObjVector();

    private boolean lvalue;

    private boolean setOnlyOrError;

    private boolean forceCheck;

    private boolean isFirstNotNull;

    private boolean isClinitSafe;

    private ExpressionType firstActualType;

    QualifiedName(Term a) {
        super(a, Empty.newTerm());
    }

    QualifiedName(Term a, Term c) {
        super(a, c);
    }

    String dottedName() {
        return terms[1].notEmpty() ? terms[0].dottedName() + "."
                + terms[1].dottedName() : terms[0].dottedName();
    }

    void storeDottedName(ObjVector v) {
        terms[0].storeDottedName(v);
        terms[1].storeDottedName(v);
    }

    boolean isName() {
        return true;
    }

    boolean isJavaConstant(ClassDefinition ourClass) {
        if (callerClass != null)
            return resultFields.size() == 1
                    && ((VariableDefinition) resultFields.elementAt(0))
                            .isJavaConstant();
        ObjVector vec = new ObjVector();
        storeDottedName(vec);
        String name = (String) vec.elementAt(0);
        ClassDefinition cd = ourClass;
        assertCond(cd != null);
        int lastInd = vec.size() - 1;
        do {
            VariableDefinition v = cd.getField(name, null);
            if (v != null)
                return lastInd == 0 && v.isJavaConstant();
            cd = cd.outerClass();
        } while (cd != null);
        if (lastInd == 0)
            return false;
        Context c = ourClass.passOneContext();
        ClassDefinition aclass = c.resolveClass(name, false, true);
        int i = 1;
        while (aclass == null) {
            if (i >= lastInd)
                return false;
            name = name + "." + (String) vec.elementAt(i++);
            aclass = c.resolveClass(name, false, false);
        }
        while (i < lastInd) {
            name = name + "." + (String) vec.elementAt(i);
            aclass = c.resolveClass(name, false, true);
            if (aclass == null)
                return false;
            i++;
        }
        VariableDefinition v = aclass.getField((String) vec.elementAt(lastInd),
                ourClass);
        return v != null && v.isJavaConstant();
    }

    void processPass1(Context c) {
        if (callerClass == null) {
            callerClass = c.currentClass;
            assertCond(callerClass != null);
            lvalue = c.lvalue;
            setOnlyOrError = c.setOnly;
            ObjVector vec = new ObjVector();
            storeDottedName(vec);
            String name = (String) vec.elementAt(vec.size() - 1);
            VariableDefinition v = null;
            if (vec.size() > 1) {
                ClassDefinition aclass = defineClass(c, vec);
                if (aclass == null) {
                    fatalError(c, "Undefined qualified name: " + dottedName());
                    setUndefined();
                    return;
                }
                v = aclass.getField(name, c.forClass);
                if (v == null) {
                    fatalError(c,
                            "Undefined qualified variable: " + aclass.name()
                                    + "." + name);
                    setUndefined();
                    return;
                }
            } else {
                if (c.currentMethod != null) {
                    v = c.currentMethod.getLocalVar(name);
                }
                if (v == null) {
                    if ((c.forceVmExc & ClassDefinition.NULL_PTR_EXC) != 0) {
                        forceCheck = true;
                    }
                    ClassDefinition cd = callerClass;
                    while ((v = cd.getField(name, c.forClass)) == null) {
                        VariableDefinition outerV = cd.outerThisRef();
                        if (outerV != null) {
                            outerV.markUsed();
                            resultFields.addElement(outerV);
                        }
                        cd = cd.outerClass();
                        if (cd == null) {
                            fatalError(c, "Undefined variable: " + name);
                            setUndefined();
                            return;
                        }
                    }
                    if (!v.isClassVariable() && c.currentMethod != null
                            && c.currentMethod.isClassMethod()) {
                        fatalError(c,
                                "Instance variable used in a static context: "
                                        + name);
                    }
                }
            }
            v.markUsed(lvalue, c.setOnly);
            resultFields.addElement(v);
            if (lvalue) {
                if (v.isLocalOrParam()) {
                    if (c.curTryChangedVars != null) {
                        c.curTryChangedVars.add(v);
                    }
                } else {
                    v.setChangedSpecial();
                }
            }
            processFields(c);
            if (!v.used()
                    && ((resultFields.size() > 1 && lvalue) || !isSafeExpr())) {
                v.markUsed();
            }
            if (c.currentTry != null) {
                c.currentTry.setVarAccessed((VariableDefinition) resultFields
                        .elementAt(0));
            }
        }
    }

    ClassDefinition defineClass(Context c, ObjVector vec) {
        assertCond(vec != null);
        boolean isMethodCall = false;
        if (callerClass == null) {
            callerClass = c.currentClass;
            assertCond(callerClass != null);
            isMethodCall = true;
        }
        int lastInd = vec.size() - 1;
        if (lastInd == 0)
            return null;
        if ((c.forceVmExc & ClassDefinition.NULL_PTR_EXC) != 0) {
            forceCheck = true;
        }
        int i = 1;
        ClassDefinition aclass = null;
        String name = (String) vec.elementAt(0);
        VariableDefinition v = null;
        if (c.currentMethod != null) {
            v = c.currentMethod.getLocalVar(name);
        }
        if (v != null) {
            v.markUsed();
            aclass = v.exprType().receiverClass();
            resultFields.addElement(v);
        } else {
            ClassDefinition cd = callerClass;
            while ((v = cd.getField(name, c.forClass)) == null) {
                VariableDefinition outerV = cd.outerThisRef();
                if (outerV != null) {
                    resultFields.addElement(outerV);
                }
                cd = cd.outerClass();
                if (cd == null)
                    break;
            }
            if (v != null && !v.isClassVariable()) {
                if (c.currentMethod != null && c.currentMethod.isClassMethod()) {
                    fatalError(c,
                            "Instance variable used in a static context: "
                                    + name);
                }
                Enumeration en = resultFields.elements();
                while (en.hasMoreElements()) {
                    ((VariableDefinition) en.nextElement()).markUsed();
                }
            } else {
                for (int j = resultFields.size() - 1; j >= 0; j--) {
                    resultFields.removeElementAt(j);
                }
            }
            if (v != null) {
                v.markUsed(false, false);
                aclass = v.exprType().receiverClass();
                resultFields.addElement(v);
            } else {
                aclass = c.resolveClass(name, false, true);
                while (aclass == null) {
                    if (i >= lastInd)
                        return null;
                    name = name + "." + (String) vec.elementAt(i++);
                    aclass = c.resolveClass(name, false, false);
                }
                while (i < lastInd) {
                    name = name + "." + (String) vec.elementAt(i);
                    if ((cd = c.resolveClass(name, false, true)) == null)
                        break;
                    i++;
                    aclass = cd;
                }
            }
        }
        while (i < lastInd) {
            v = aclass.getField((String) vec.elementAt(i++), c.forClass);
            if (v == null) {
                fatalError(c, "Undefined variable: " + dottedName());
                break;
            }
            v.markUsed(false, false);
            aclass = v.exprType().receiverClass();
            resultFields.addElement(v);
        }
        if (isMethodCall && resultFields.size() > 0) {
            processFields(c);
        }
        return aclass;
    }

    private void setUndefined() {
        setOnlyOrError = true;
        int i = resultFields.size();
        while (i > 0) {
            resultFields.removeElementAt(--i);
        }
    }

    private void processFields(Context c) {
        int i = resultFields.size();
        while (i-- > 0) {
            if (((VariableDefinition) resultFields.elementAt(i))
                    .isClassVariable())
                break;
        }
        while (i > 0) {
            resultFields.removeElementAt(--i);
        }
        VariableDefinition v = (VariableDefinition) resultFields.elementAt(0);
        firstActualType = c.getActualType(v);
        isFirstNotNull = c.isVarNotNull(v);
        if (v.isClassVariable()) {
            isClinitSafe = c.addAccessedClassField(v);
            if (!isClinitSafe) {
                v.markUsed();
            }
        } else if (!v.isLocalOrParam()) {
            c.addAccessedClass(v.definingClass());
        }
        int count = resultFields.size();
        if (count > 1) {
            c.setVarNotNull(v);
        }
        i = 0;
        while (++i < count) {
            c.addAccessedClass(((VariableDefinition) resultFields.elementAt(i))
                    .definingClass());
        }
    }

    ExpressionType exprType() {
        assertCond(callerClass != null);
        return resultFields.size() > 0 ? ((VariableDefinition) resultFields
                .elementAt(resultFields.size() - 1)).exprType() : callerClass;
    }

    ExpressionType actualExprType() {
        assertCond(callerClass != null);
        int count = resultFields.size();
        return count > 0 ? (count == 1 && firstActualType != null ? firstActualType
                : ((VariableDefinition) resultFields.elementAt(count - 1))
                        .actualExprType())
                : exprType();
    }

    ConstValue evaluateConstValue() {
        assertCond(callerClass != null);
        return resultFields.size() == 1 ? ((VariableDefinition) resultFields
                .elementAt(0)).evaluateConstValue() : null;
    }

    boolean isLiteral() {
        assertCond(callerClass != null);
        return resultFields.size() == 1
                && ((VariableDefinition) resultFields.elementAt(0)).isLiteral();
    }

    boolean isImmutable() {
        assertCond(callerClass != null);
        int count = resultFields.size();
        if (count > 0) {
            VariableDefinition v = (VariableDefinition) resultFields
                    .elementAt(0);
            if ((!isClinitSafe && v.isClassVariable() && v.definingClass()
                    .classInitializerNotCalledYet())
                    || !v.isImmutable(callerClass))
                return false;
            for (int i = 1; i < count; i++) {
                if (!((VariableDefinition) resultFields.elementAt(i))
                        .isImmutable(null))
                    return false;
            }
        }
        return true;
    }

    boolean isSafeExpr() {
        assertCond(callerClass != null);
        int lastInd = resultFields.size() - 1;
        if (lastInd >= 0) {
            VariableDefinition v = (VariableDefinition) resultFields
                    .elementAt(0);
            if (v.isClassVariable() && !isClinitSafe && !v.isLiteral()
                    && v.definingClass().classInitializerNotCalledYet())
                return false;
            for (int i = lastInd - 1; i >= 0; i--) {
                if ((i > 0 || !isFirstNotNull)
                        && !((VariableDefinition) resultFields.elementAt(i))
                                .isNotNull())
                    return false;
            }
        }
        return true;
    }

    boolean isSafeWithThrow() {
        return true;
    }

    boolean isFieldAccessed(VariableDefinition v) {
        assertCond(callerClass != null);
        if (resultFields.size() == 0)
            return false;
        VariableDefinition field = (VariableDefinition) resultFields
                .elementAt(0);
        return v != null ? field == v
                : resultFields.size() > 1
                        || (!field.isLocalOrParam() && (!field
                                .isImmutable(callerClass) || (!isClinitSafe
                                && field.isClassVariable() && field
                                .definingClass().classInitializerNotCalledYet())));
    }

    boolean isAnyLocalVarChanged(Term t) {
        assertCond(callerClass != null);
        if (!lvalue || resultFields.size() != 1)
            return false;
        VariableDefinition v = (VariableDefinition) resultFields.elementAt(0);
        return v.isLocalOrParam() && (t == null || t.isFieldAccessed(v));
    }

    boolean isNotNull() {
        assertCond(callerClass != null);
        int count = resultFields.size();
        return count == 0
                || (count == 1 && isFirstNotNull)
                || ((VariableDefinition) resultFields.elementAt(count - 1))
                        .isNotNull();
    }

    VariableDefinition getVariable(boolean allowInstance) {
        assertCond(callerClass != null);
        int count = resultFields.size();
        return count == 0 ? VariableDefinition.THIS_VAR : allowInstance
                || count == 1 ? (VariableDefinition) resultFields
                .elementAt(count - 1) : null;
    }

    String strLiteralValueGuess() {
        assertCond(callerClass != null);
        return resultFields.size() > 0 ? ((VariableDefinition) resultFields
                .elementAt(resultFields.size() - 1)).strLiteralValueGuess()
                : null;
    }

    ExpressionType classLiteralValGuess() {
        assertCond(callerClass != null);
        return resultFields.size() > 0 ? ((VariableDefinition) resultFields
                .elementAt(resultFields.size() - 1)).classLiteralValGuess()
                : null;
    }

    boolean storeClassLiteralsGuess(ObjVector parmSig, boolean isActual) {
        assertCond(callerClass != null);
        return resultFields.size() > 0
                && ((VariableDefinition) resultFields.elementAt(resultFields
                        .size() - 1))
                        .storeClassLiteralsGuess(parmSig, isActual);
    }

    MethodInvocation getClassNewInstanceCall() {
        assertCond(callerClass != null);
        return resultFields.size() > 0 ? ((VariableDefinition) resultFields
                .elementAt(resultFields.size() - 1)).getClassNewInstanceCall()
                : null;
    }

    MethodSignature getConstructorInstanceSign() {
        assertCond(callerClass != null);
        return resultFields.size() > 0 ? ((VariableDefinition) resultFields
                .elementAt(resultFields.size() - 1))
                .getConstructorInstanceSign() : null;
    }

    void discoverObjLeaks() {
        if (resultFields.size() > 0) {
            assertCond(callerClass != null);
            ((VariableDefinition) resultFields.elementAt(0))
                    .discoverLocalVolatile();
        }
    }

    void setStackObjVolatile() {
        assertCond(callerClass != null);
        if (resultFields.size() == 1) {
            ((VariableDefinition) resultFields.elementAt(0))
                    .setStackObjVolatile();
        } else if (resultFields.size() == 0) {
            if (Main.dict.ourMethod != null) {
                Main.dict.ourMethod.setThisStackObjVolatile();
            } else {
                callerClass.setInitThisStackObjVolatile();
            }
        }
    }

    void setObjLeaks(VariableDefinition v) {
        assertCond(callerClass != null);
        if (resultFields.size() == 1) {
            VariableDefinition field = (VariableDefinition) resultFields
                    .elementAt(0);
            if (field != v) {
                if (field.isLocalOrParam()) {
                    ObjHashtable assignedLocals = Main.dict.assignedLocals;
                    assertCond(assignedLocals != null);
                    ObjVector assignedTerms = (ObjVector) assignedLocals
                            .get(field);
                    if (assignedTerms != null) {
                        int i = assignedTerms.size();
                        while (i-- > 0) {
                            Term t = (Term) assignedTerms.elementAt(i);
                            assignedTerms.removeElementAt(i);
                            t.setObjLeaks(v);
                            if (assignedTerms.identityLastIndexOf(t) < 0) {
                                assignedTerms.addElement(t);
                            }
                        }
                    }
                } else {
                    field.setWritableArray(v);
                }
            }
        } else if (resultFields.size() == 0
                && (v == null || !v.addSetObjLeaksTerm(this))) {
            if (Main.dict.ourMethod != null) {
                Main.dict.ourMethod
                        .setThisObjLeak(v == VariableDefinition.RETURN_VAR);
            } else {
                callerClass.setInstanceInitLeaks();
            }
        }
    }

    int tokenCount() {
        int count = resultFields.size();
        if (count == 0)
            return 1;
        VariableDefinition v = (VariableDefinition) resultFields.elementAt(0);
        return (v.isClassVariable() || v.isLocalOrParam() || !v.used() ? 0 : 1)
                + count;
    }

    int tokensExpandedCount() {
        return resultFields.size() == 1 ? ((VariableDefinition) resultFields
                .elementAt(0)).tokensExpandedCount() : 0;
    }

    boolean isAtomary() {
        return true;
    }

    void processOutput(OutputContext oc) {
        oc.cPrint(stringOutput());
    }

    String stringOutput() {
        assertCond(callerClass != null);
        int count = resultFields.size();
        if (count == 0)
            return setOnlyOrError ? VariableDefinition.UNKNOWN_NAME
                    : This.CNAME;
        VariableDefinition v = (VariableDefinition) resultFields.elementAt(0);
        String resultString = v.isClassVariable() ? v.stringOutputForStatic(
                isClinitSafe, lvalue && count == 1) : v.stringOutput(true,
                lvalue && count == 1);
        if (count > 1) {
            boolean isPrevNotNull = isFirstNotNull || v.isNotNull();
            int i = 1;
            do {
                v = (VariableDefinition) resultFields.elementAt(i);
                if (++i >= count)
                    break;
                resultString = v.stringOutput(resultString, isPrevNotNull ? 1
                        : forceCheck ? -1 : 0, false);
                isPrevNotNull = v.isNotNull();
            } while (true);
            resultString = v.stringOutput(resultString, isPrevNotNull ? 1
                    : forceCheck ? -1 : 0, lvalue);
        }
        return resultString;
    }

    ExpressionType traceClassInit() {
        assertCond(callerClass != null);
        int lastInd = resultFields.size() - 1;
        for (int i = 0; i < lastInd; i++) {
            ((VariableDefinition) resultFields.elementAt(i)).traceClassInit();
        }
        return setOnlyOrError ? null : lastInd < 0 ? Main.dict.curTraceInfo
                .curThisClass() : ((VariableDefinition) resultFields
                .elementAt(lastInd)).traceClassInit();
    }
}
