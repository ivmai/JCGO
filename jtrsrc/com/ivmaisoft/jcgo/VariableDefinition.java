/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/VariableDefinition.java --
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
 * Variable (field) definition entity.
 */

final class VariableDefinition {

    private static/* final */int TERMS_BIG_LITERAL = 16;

    private static/* final */int TERMS_LITERAL_LIMIT = 400;

    static final String UNKNOWN_NAME = "<unknown?>".toString();

    static final VariableDefinition RETURN_VAR = new VariableDefinition("",
            false);

    static final VariableDefinition WRITABLE_ARRAY_VAR = new VariableDefinition(
            "", false);

    static final VariableDefinition THIS_VAR = new VariableDefinition("this",
            true);

    private static final ObjVector EMPTY_VECT = new ObjVector();

    private static final MethodSignature EMPTY_MSIG = new MethodSignature("",
            EMPTY_VECT);

    private static final MethodInvocation EMPTY_INVOCATION = new MethodInvocation(
            Empty.newTerm(), Empty.newTerm());

    private static final String NO_STR_LITERAL = new String("");

    private ClassDefinition defnClass;

    private String name;

    private String outputCName;

    private int modifiers;

    private ExpressionType resType;

    private ExpressionType actualType;

    private Term initializerTerm;

    private Term singleAssignedTerm;

    private LeftBrace ourScope;

    private InitializerPart init;

    private String initTermStrOutValue;

    private boolean used;

    private boolean initUsedOnly;

    private boolean hasSetVal;

    private boolean hasValueGet;

    private boolean analysisDone;

    private boolean outputDone;

    private boolean isJavaConstant;

    private boolean isJavaConstantSet;

    private boolean isNotNull;

    private boolean isNotNullSet;

    private boolean isLiteral;

    private boolean isImmutableValSet;

    private int tokensExpCount;

    private MethodInvocation classNewInstanceCall;

    private boolean isStackObjVolatile;

    private boolean isValueChanged;

    private boolean needsLocalVolatile;

    private boolean isUnassigned;

    private String strLiteralValueGuess;

    private ExpressionType classLiteralValue;

    private ObjVector classLiteralsActualArr;

    private ObjVector classLiteralsValueArr;

    private MethodSignature reflectedSign;

    private ConstValue constVal;

    private ExpressionType curTraceType;

    private VariableDefinition(String name, boolean isNotNull) {
        this.name = name;
        modifiers = AccModifier.FINAL;
        initializerTerm = Empty.newTerm();
        this.isNotNull = isNotNull;
        isNotNullSet = true;
    }

    VariableDefinition(ClassDefinition defnClass, String name, int modifiers,
            ExpressionType resType, Term initializerTerm, boolean isNotNull) {
        Term.assertCond(defnClass != null);
        this.defnClass = defnClass;
        this.name = name;
        if ((modifiers & (AccModifier.PRIVATE | AccModifier.PROTECTED | AccModifier.PUBLIC)) == (AccModifier.PRIVATE | AccModifier.PROTECTED)) {
            modifiers &= ~(AccModifier.PRIVATE | AccModifier.PROTECTED);
        }
        this.modifiers = modifiers;
        this.resType = resType;
        this.initializerTerm = initializerTerm;
        this.isNotNull = isNotNull;
        isNotNullSet = isNotNull;
        Term.assertCond(initializerTerm != null);
        if (defnClass.objectSize() != Type.CLASSINTERFACE) {
            analysisDone = true;
            hasSetVal = true;
        }
        if (!isLocalOrParam()) {
            if (defnClass.isInterface()) {
                this.modifiers |= AccModifier.PUBLIC | AccModifier.STATIC
                        | AccModifier.FINAL;
            }
            if (hasInitializer()) {
                init = defnClass.addInitializer(initializerTerm,
                        isClassVariable());
            }
        }
    }

    void setLocalScope(LeftBrace ourScope) {
        if ((modifiers & AccModifier.LOCALVAR) != 0) {
            (this.ourScope = ourScope).addLocal(this);
            setUnassigned(true);
        }
    }

    void setChangedSpecial() {
        isValueChanged = true;
    }

    private int sizeOrder() {
        if (resType.signatureDimensions() > 0)
            return 0;
        int s = resType.objectSize();
        if (s == Type.LONG) {
            s = Type.FLOAT;
        } else if (s == Type.FLOAT) {
            s = Type.LONG;
        }
        return Type.CLASSINTERFACE - s;
    }

    String id() {
        return name;
    }

    ClassDefinition definingClass() {
        Term.assertCond(defnClass != null);
        return defnClass;
    }

    boolean hasInitializer() {
        return initializerTerm.notEmpty();
    }

    ExpressionType exprType() {
        if (resType == null) {
            resType = Main.dict.get(Names.JAVA_LANG_OBJECT);
        }
        return resType;
    }

    ExpressionType actualExprType() {
        int s1 = exprType().objectSize();
        if (s1 == Type.CLASSINTERFACE || s1 == Type.OBJECTARRAY) {
            producePassOne();
            if (actualType != null)
                return actualType;
        }
        return resType;
    }

    void resetTypeForLoop() {
        if (!isFinalVariable()) {
            curTraceType = Main.dict.classTable[Type.VOID];
        }
    }

    boolean isNotNull() {
        if (!isNotNullSet) {
            isNotNullSet = true;
            if (hasInitializer() && isFinalVariable()
            /* && (isLocalOrParam() || isClassVariable()) */) {
                producePassOne();
                isNotNull = initializerTerm.isNotNull();
            }
        }
        return isNotNull;
    }

    void markInitializerOnly() {
        Term.assertCond((modifiers & (AccModifier.LOCALVAR | AccModifier.PARAMETER)) == 0);
        if (!used && !initUsedOnly && init != null
                && !initializerTerm.isSafeWithThrow()) {
            producePassOne();
            if (!used) {
                initUsedOnly = true;
                init.setUsedVar(this);
            }
        }
    }

    void markUsed(boolean lvalue, boolean setOnly) {
        if (isLocalOrParam()) {
            markUsed();
        } else if (lvalue) {
            if (!setOnly || hasValueGet) {
                markUsed();
            } else {
                hasSetVal = true;
            }
        } else if (hasSetVal) {
            markUsed();
        } else if (!used && !hasValueGet) {
            hasValueGet = true;
            if (hasInitializer()) {
                producePassOne();
                if (isInitializerNonZero() || isLiteral()) {
                    markUsed();
                }
            }
        }
    }

    int markUsed() {
        if (used)
            return 0;
        used = true;
        initUsedOnly = false;
        if (!isLocalOrParam()) {
            Term.assertCond(defnClass != null);
            if (name.equals(Main.dict.failOnFieldName)
                    && (Main.dict.failOnClassName == null || defnClass.name()
                            .equals(Main.dict.failOnClassName)))
                throw new AssertException("Specified field is required!");
            defnClass.markUsed();
        }
        if (init != null) {
            init.setUsedVar(this);
        }
        return 1;
    }

    boolean used() {
        return used;
    }

    boolean isInitializerUsedOnly() {
        return initUsedOnly;
    }

    void setUnassigned(boolean isUnassigned) {
        this.isUnassigned = isUnassigned;
    }

    boolean isUnassigned() {
        return isUnassigned;
    }

    int getJavaModifiers() {
        Term.assertCond((modifiers & (AccModifier.LOCALVAR | AccModifier.PARAMETER)) == 0);
        return modifiers;
    }

    boolean isLocalOrParam() {
        return (modifiers & (AccModifier.LOCALVAR | AccModifier.PARAMETER)) != 0;
    }

    boolean isClassVariable() {
        return (modifiers & AccModifier.STATIC) != 0;
    }

    boolean isTransient() {
        return (modifiers & AccModifier.TRANSIENT) != 0;
    }

    boolean isPrivate() {
        return (modifiers & AccModifier.PRIVATE) != 0;
    }

    boolean isPublic() {
        return (modifiers & AccModifier.PUBLIC) != 0;
    }

    boolean isProtectedOrPublic() {
        return (modifiers & (AccModifier.PUBLIC | AccModifier.PROTECTED)) != 0;
    }

    boolean isFinalVariable() {
        return (modifiers & AccModifier.FINAL) != 0;
    }

    boolean isJavaConstant() {
        if (!isJavaConstantSet) {
            isJavaConstantSet = true;
            if (isFinalVariable() && hasInitializer() && isClassVariable()) {
                isJavaConstant = initializerTerm.isJavaConstant(defnClass);
            }
        }
        return isJavaConstant;
    }

    ConstValue evaluateConstValue() {
        if (!isImmutableValSet) {
            isImmutableValSet = true;
            if (isFinalVariable() && hasInitializer()) {
                producePassOne();
                constVal = initializerTerm.evaluateConstValue();
                if (constVal != null) {
                    constVal = constVal.castTo(resType.objectSize());
                }
            }
        }
        return constVal;
    }

    boolean isLiteral() {
        if (tokensExpCount == 0) {
            if (isFinalVariable() && hasInitializer()) {
                tokensExpCount = -1 >>> 1;
                producePassOne();
                if (initializerTerm.isLiteral()) {
                    if ((tokensExpCount = initializerTerm.tokenCount()
                            + initializerTerm.tokensExpandedCount()) < 0) {
                        tokensExpCount = -1 >>> 1;
                    }
                    if (tokensExpCount < TERMS_LITERAL_LIMIT) {
                        isLiteral = true;
                        if (tokensExpCount >= TERMS_BIG_LITERAL) {
                            evaluateConstValue();
                        }
                        return true;
                    }
                    if (evaluateConstValue() == null)
                        return false;
                    isLiteral = true;
                    return true;
                }
            }
            tokensExpCount = 1;
        }
        return isLiteral;
    }

    int tokensExpandedCount() {
        isLiteral();
        return tokensExpCount;
    }

    boolean isImmutable(ClassDefinition callerClass) {
        return isFinalVariable()
                && (hasInitializer() || callerClass != defnClass
                        || callerClass == null
                        || (modifiers & AccModifier.PARAMETER) != 0
                        || defnClass.objectSize() != Type.CLASSINTERFACE || (isClassVariable() ? name
                        .startsWith("$SwitchMap$")
                        : (modifiers & AccModifier.LOCALVAR) == 0
                                && name.startsWith("val$")));
    }

    void setStrLiteralValue(String str) {
        if (strLiteralValueGuess == null
                || (strLiteralValueGuess == NO_STR_LITERAL && str != null)) {
            strLiteralValueGuess = str;
        }
    }

    String strLiteralValueGuess() {
        if (strLiteralValueGuess == null) {
            strLiteralValueGuess = NO_STR_LITERAL;
            producePassOne();
            setStrLiteralValue(initializerTerm.strLiteralValueGuess());
        }
        return strLiteralValueGuess != NO_STR_LITERAL ? strLiteralValueGuess
                : null;
    }

    void setClassLiteralValGuess(ExpressionType exprType) {
        if (classLiteralValue == null
                || (classLiteralValue == Main.dict.classTable[Type.VOID] && exprType != null)) {
            classLiteralValue = exprType;
        }
    }

    ExpressionType classLiteralValGuess() {
        if (classLiteralValue == null) {
            classLiteralValue = Main.dict.classTable[Type.VOID];
            producePassOne();
            setClassLiteralValGuess(initializerTerm.classLiteralValGuess());
        }
        return classLiteralValue != Main.dict.classTable[Type.VOID] ? classLiteralValue
                : null;
    }

    void setClassNewInstanceCall(MethodInvocation mcall) {
        if (classNewInstanceCall == null
                || (classNewInstanceCall == EMPTY_INVOCATION && mcall != null)) {
            classNewInstanceCall = mcall;
        }
    }

    MethodInvocation getClassNewInstanceCall() {
        if (classNewInstanceCall == null) {
            classNewInstanceCall = EMPTY_INVOCATION;
            producePassOne();
            setClassNewInstanceCall(initializerTerm.getClassNewInstanceCall());
        }
        return classNewInstanceCall != EMPTY_INVOCATION ? classNewInstanceCall
                : null;
    }

    void setConstructorInstanceSign(MethodSignature msig) {
        if (reflectedSign == null
                || (reflectedSign == EMPTY_MSIG && msig != null)) {
            reflectedSign = msig;
        }
    }

    MethodSignature getConstructorInstanceSign() {
        if (reflectedSign == null) {
            reflectedSign = EMPTY_MSIG;
            producePassOne();
            setConstructorInstanceSign(initializerTerm
                    .getConstructorInstanceSign());
        }
        return reflectedSign != EMPTY_MSIG ? reflectedSign : null;
    }

    void setStoredClassLiteralsGuess(ObjVector parmSig, boolean isActual) {
        if (isActual) {
            if (classLiteralsActualArr == null
                    || classLiteralsActualArr == EMPTY_VECT) {
                classLiteralsActualArr = parmSig;
            }
        } else if (classLiteralsValueArr == null
                || classLiteralsValueArr == EMPTY_VECT) {
            classLiteralsValueArr = parmSig;
        }
    }

    boolean storeClassLiteralsGuess(ObjVector parmSig, boolean isActual) {
        ObjVector classLiterals = isActual ? classLiteralsActualArr
                : classLiteralsValueArr;
        if (classLiterals != null) {
            if (classLiterals == EMPTY_VECT)
                return false;
            Enumeration en = classLiterals.elements();
            while (en.hasMoreElements()) {
                parmSig.addElement(en.nextElement());
            }
            return true;
        }
        if (isActual) {
            classLiteralsActualArr = EMPTY_VECT;
        } else {
            classLiteralsValueArr = EMPTY_VECT;
        }
        producePassOne();
        if (!initializerTerm.storeClassLiteralsGuess(parmSig, isActual))
            return false;
        if (isActual) {
            if (classLiteralsActualArr == EMPTY_VECT) {
                classLiteralsActualArr = parmSig;
            }
        } else if (classLiteralsValueArr == EMPTY_VECT) {
            classLiteralsValueArr = parmSig;
        }
        return true;
    }

    int producePassOne() {
        if (analysisDone)
            return 0;
        if (init != null) {
            Context c = defnClass.cloneContextFor(null);
            Term.assertCond(c.currentClass == defnClass);
            Term.assertCond(c.currentMethod == null);
            if (isClassVariable()) {
                c.currentMethod = defnClass.clinitForStaticField();
            }
            c.initBranch();
            initializerPassOne(c);
        }
        analysisDone = true;
        return 1;
    }

    void initializerPassOne(Context c) {
        if (!analysisDone) {
            analysisDone = true;
            if (hasInitializer()) {
                ExpressionType oldCurrentVarType = c.currentVarType;
                c.currentVarType = resType;
                int oldModifiers = c.modifiers;
                c.modifiers = modifiers;
                initializerTerm.processPass1(c);
                int s0 = initializerTerm.exprType().objectSize();
                int s1 = resType.objectSize();
                if (s0 == Type.BOOLEAN ? s1 != Type.BOOLEAN
                        : s0 >= Type.CLASSINTERFACE ? s1 < Type.CLASSINTERFACE
                                : s0 >= Type.LONG && s1 < s0) {
                    initializerTerm
                            .fatalError(c,
                                    "Incompatible types in variable definition assignment");
                }
                if (isFinalVariable()) {
                    actualType = initializerTerm.actualExprType();
                } else {
                    c.setActualType(this, initializerTerm.actualExprType());
                    if (initializerTerm.isNotNull()) {
                        c.setVarNotNull(this);
                    }
                }
                c.modifiers = oldModifiers;
                c.currentVarType = oldCurrentVarType;
            }
        }
    }

    static LeftBrace addSetObjLeaksTerm(LeftBrace noLeaksScope,
            VariableDefinition v, Term t, boolean isConditional) {
        if (noLeaksScope != null) {
            if (v != RETURN_VAR) {
                if (v != null) {
                    if (v == WRITABLE_ARRAY_VAR)
                        return noLeaksScope;
                    if (v.curTraceType == null && v.isLocalOrParam()) {
                        if (noLeaksScope.outerScope() == null) {
                            v.attachNoLeaksTerm(t);
                            return noLeaksScope;
                        }
                        for (LeftBrace scope = v.ourScope; scope != null; scope = scope
                                .outerScope()) {
                            if (scope == noLeaksScope) {
                                v.attachNoLeaksTerm(t);
                                return noLeaksScope;
                            }
                        }
                        while (!noLeaksScope.isBoolAssign()) {
                            if ((noLeaksScope = noLeaksScope.outerScope()) == v.ourScope
                                    || noLeaksScope.outerScope() == null) {
                                v.attachNoLeaksTerm(t);
                                return noLeaksScope;
                            }
                        }
                    } else if (v.isClassVariable()) {
                        if (v.singleAssignedTerm == t)
                            return noLeaksScope;
                        if (v.singleAssignedTerm == null) {
                            if (noLeaksScope == ArrayInitializer.IMMUTABLE_SCOPE) {
                                v.singleAssignedTerm = t;
                                return noLeaksScope;
                            }
                            v.singleAssignedTerm = Empty.newTerm();
                            return null;
                        }
                        if (v.singleAssignedTerm.notEmpty()) {
                            v.singleAssignedTerm
                                    .setObjLeaks(WRITABLE_ARRAY_VAR);
                            v.singleAssignedTerm = Empty.newTerm();
                        }
                    }
                }
            } else {
                MethodDefinition md;
                if (!isConditional && (md = Main.dict.ourMethod) != null) {
                    md.setStackObjRetTerm(t);
                }
            }
        } else {
            MethodDefinition md;
            if (v != RETURN_VAR && (md = Main.dict.ourMethod) != null) {
                md.attachStackObjRetTerm(v, t);
            }
        }
        return null;
    }

    boolean addSetObjLeaksTerm(Term t) {
        if (this != WRITABLE_ARRAY_VAR) {
            if (!isLocalOrParam() || curTraceType != null)
                return false;
            attachNoLeaksTerm(t);
        }
        return true;
    }

    private void attachNoLeaksTerm(Term t) {
        ObjHashtable assignedLocals = Main.dict.assignedLocals;
        Term.assertCond(assignedLocals != null && this != WRITABLE_ARRAY_VAR);
        ObjVector terms = (ObjVector) assignedLocals.get(this);
        if (terms == null) {
            assignedLocals.put(this, terms = new ObjVector());
        }
        if (terms.identityLastIndexOf(t) < 0) {
            terms.addElement(t);
            if (isStackObjVolatile) {
                t.setStackObjVolatile();
            }
        }
    }

    void setStackObjVolatile() {
        if (isLocalOrParam()) {
            ObjHashtable assignedLocals = Main.dict.assignedLocals;
            Term.assertCond(assignedLocals != null);
            if (!isStackObjVolatile) {
                isStackObjVolatile = true;
                ObjVector terms = (ObjVector) assignedLocals.get(this);
                if (terms != null) {
                    int i = terms.size();
                    while (i-- > 0) {
                        ((Term) terms.elementAt(i)).setStackObjVolatile();
                    }
                }
            }
        }
    }

    void setWritableArray(VariableDefinition v) {
        if (isClassVariable() && v != this) {
            if (v == RETURN_VAR) {
                MethodDefinition md;
                if ((singleAssignedTerm == null || singleAssignedTerm
                        .notEmpty())
                        && ((md = Main.dict.ourMethod) == null || !md
                                .attachRetWritableArray(this))) {
                    if (singleAssignedTerm != null) {
                        singleAssignedTerm.setObjLeaks(WRITABLE_ARRAY_VAR);
                    }
                    singleAssignedTerm = Empty.newTerm();
                }
            } else {
                if (hasInitializer()
                        && singleAssignedTerm == null
                        && initializerTerm.exprType().objectSize() >= Type.CLASSINTERFACE) {
                    initializerTerm.setObjLeaks(this);
                }
                if (singleAssignedTerm != null) {
                    singleAssignedTerm.setObjLeaks(v);
                } else {
                    singleAssignedTerm = Empty.newTerm();
                }
            }
        }
    }

    void discoverLocalVolatile() {
        if (isValueChanged && isLocalOrParam()) {
            needsLocalVolatile = true;
        }
    }

    String outputName() {
        if (outputCName == null) {
            Term.assertCond(defnClass != null);
            outputCName = isClassVariable() ? Main.dict.nameMapper
                    .classVarToOutputName(defnClass.castName(), name)
                    : isLocalOrParam() ? Main.dict.nameMapper
                            .localVarToOutputName(defnClass.castName(), name)
                            : defnClass.objectSize() != Type.CLASSINTERFACE ? name
                                    : Main.dict.nameMapper.fieldToOutputName(
                                            defnClass, name,
                                            defnClass.countHiddenFields(name));
        }
        return outputCName;
    }

    String stringOutputForStatic(boolean isClinitSafe, boolean lvalue) {
        Term.assertCond((modifiers & AccModifier.STATIC) != 0);
        if (!used) {
            Term.assertCond(!lvalue);
            return stringOutputZero();
        }
        if (!isClinitSafe && !defnClass.classInitializerNotCalledYet()) {
            isClinitSafe = true;
        }
        String classCName = defnClass.castName();
        return wrapStrOutputForVolatile(
                lvalue || !isThreadVolatile(),
                resType,
                (isLiteral() ? (isClinitSafe ? "" : "JCGO_CLINIT_LITERACC("
                        + classCName + "__class, ")
                        + classCName + "__"
                        : (isClinitSafe ? "" : "JCGO_CLINIT_VARACC("
                                + classCName + "__class, ")
                                + classCName
                                + (defnClass.isReflectedField(name) ? "__class."
                                        : "__"))
                        + outputName() + (isClinitSafe ? "" : ")"));
    }

    private static String wrapStrOutputForVolatile(boolean lvalueOrNonVolatile,
            ExpressionType resType, String str) {
        int s1 = resType.objectSize();
        return lvalueOrNonVolatile ? str
                : s1 >= Type.CLASSINTERFACE ? "JCGO_VLT_LFETCH("
                        + resType.castName() + ", " + str + ")"
                        : "JCGO_VLT_FETCH" + Type.sig[s1] + "(" + str + ")";
    }

    private String stringOutputZero() {
        int s1 = resType.objectSize();
        return s1 >= Type.CLASSINTERFACE ? "((" + resType.castName() + ")"
                + LexTerm.NULL_STR + ")" : s1 == Type.BOOLEAN ? LexTerm
                .outputBoolean(false) : "(" + resType.castName() + ")0";
    }

    String stringOutput(boolean allowLiteral, boolean lvalue) {
        if (isClassVariable())
            return stringOutputForStatic(true, lvalue);
        if (!allowLiteral || !used || !isLiteral())
            return stringOutput(This.CNAME, 1, lvalue);
        Term.assertCond(analysisDone);
        Term.assertCond(!lvalue);
        String code = initTermStringOutput(true);
        if (!initializerTerm.isAtomary()) {
            code = "(" + code + ")";
        }
        if (initializerTerm.exprType() != resType) {
            code = "((" + resType.castName() + ")" + code + ")";
        }
        return code;
    }

    private String initTermStringOutput(boolean needsLiteral) {
        String strOutValue = initTermStrOutValue;
        if (strOutValue == null) {
            if (needsLiteral) {
                initializerTerm.requireLiteral();
            }
            strOutValue = tokensExpCount >= TERMS_BIG_LITERAL
                    && constVal != null ? constVal.stringOutput()
                    : initializerTerm.stringOutput();
            if (needsLiteral) {
                initTermStrOutValue = strOutValue;
            }
        }
        return strOutValue;
    }

    String stringOutput(String prefix, int isNotNull, boolean lvalue) {
        Term.assertCond(defnClass != null);
        Term.assertCond((modifiers & AccModifier.STATIC) == 0);
        return isLocalOrParam() ? outputName()
                : used ? (defnClass.objectSize() != Type.CLASSINTERFACE ? (isNotNull > 0 ? "JCGO_ARRAY_NZLENGTH("
                        : isNotNull != 0 ? "JCGO_ARRAY_ELENGTH("
                                : "JCGO_ARRAY_LENGTH(")
                        + prefix + ")"
                        : wrapStrOutputForVolatile(
                                lvalue || !isThreadVolatile(),
                                resType,
                                (isNotNull > 0 ? "JCGO_FIELD_NZACCESS("
                                        : (isNotNull != 0 ? "JCGO_FIELD_EACCESS("
                                                : "JCGO_FIELD_ACCESS(")
                                                + defnClass.castName() + ", ")
                                        + prefix + ", " + outputName() + ")"))
                        : isNotNull > 0 ? stringOutputZero()
                                : "("
                                        + (isNotNull != 0 ? "JCGO_CALL_EFINALF"
                                                : "JCGO_CALL_FINALF")
                                        + "("
                                        + prefix
                                        + ") ("
                                        + resType.castName()
                                        + ")"
                                        + (resType.objectSize() >= Type.CLASSINTERFACE ? LexTerm.NULL_STR
                                                : "0") + ")";
    }

    static void outputJniParam(OutputContext oc, ExpressionType exprType,
            String outputCName) {
        if (exprType.objectSize() >= Type.CLASSINTERFACE) {
            String jniTypeName = exprType.getJniName();
            if (!jniTypeName.equals(Type.jniName[Type.CLASSINTERFACE])) {
                oc.cPrint("(");
                oc.cPrint(jniTypeName);
                oc.cPrint(")");
            }
            oc.cPrint("JCGO_JNI_TOLOCALREF(");
            oc.cPrint(Integer.toString(++oc.arrInitCount));
            oc.cPrint(", ");
            oc.cPrint(outputCName);
            oc.cPrint(")");
        } else {
            oc.cPrint(outputCName);
        }
    }

    boolean isThreadVolatile() {
        return (modifiers & AccModifier.VOLATILE) != 0
                && (isValueChanged || !isClassVariable());
    }

    private String stringOutputDecl(String typeName, String ourOutputName) {
        if ((!needsLocalVolatile && !isThreadVolatile())
                || (modifiers & (AccModifier.LOCALVAR | AccModifier.PARAMETER)) == AccModifier.PARAMETER)
            return typeName + " " + ourOutputName;
        String volatileDecl = isLocalOrParam() ? "JCGO_TRY_VOLATILE "
                : "JCGO_THRD_VOLATILE ";
        return resType.objectSize() < Type.CLASSINTERFACE ? volatileDecl
                + typeName + " " + ourOutputName : typeName + " "
                + volatileDecl + ourOutputName;
    }

    boolean outputParamNoClobber(OutputContext oc) {
        Term.assertCond((modifiers & (AccModifier.LOCALVAR | AccModifier.PARAMETER)) != 0);
        if (!needsLocalVolatile
                || !used
                || (modifiers & (AccModifier.LOCALVAR | AccModifier.PARAMETER)) != AccModifier.PARAMETER)
            return false;
        oc.cPrint("JCGO_TRY_NOCLOBBER(");
        oc.cPrint(outputName());
        oc.cPrint(");");
        return true;
    }

    void parameterOutput(OutputContext oc, boolean asArg, int type) {
        if (type == Type.VOID
                || resType.objectSize() == type
                || (type == Type.NULLREF && resType.objectSize() >= Type.CLASSINTERFACE)) {
            Term.assertCond((modifiers & AccModifier.PARAMETER) != 0);
            oc.cPrint(", ");
            if (!asArg) {
                oc.cPrint(stringOutputDecl(
                        type == Type.VOID ? resType.getJniName() : resType
                                .castName(), outputName()));
            } else if (type == Type.VOID) {
                outputJniParam(oc, resType, outputName());
            } else {
                oc.cPrint(outputName());
            }
        }
    }

    void cdefinition(OutputContext oc) {
        Term.assertCond((modifiers & (AccModifier.LOCALVAR | AccModifier.PARAMETER)) != 0);
        if ((used || hasInitializer()) && !isLiteral()) {
            oc.cPrint(stringOutputDecl(resType.castName(), outputName()));
            oc.cPrint(";");
        }
    }

    void outerAssignment(OutputContext oc, MethodDefinition md) {
        Term.assertCond((modifiers & (AccModifier.STATIC | AccModifier.LOCALVAR | AccModifier.PARAMETER)) == 0
                && md != null);
        if (used) {
            oc.cPrint(stringOutput(This.CNAME, 1, true));
            oc.cPrint("= ");
            VariableDefinition parmV = md.getLocalVar(name);
            Term.assertCond(parmV != null);
            oc.cPrint(parmV.outputName());
            oc.cPrint(";");
        }
    }

    boolean needsOutputFor() {
        return (used || initUsedOnly) && !outputDone;
    }

    boolean hasSomeCode() {
        return (used || initUsedOnly) && !initializerTerm.isLiteral();
    }

    void produceOutput(OutputContext oc) {
        Term.assertCond(analysisDone
                && (modifiers & (AccModifier.LOCALVAR | AccModifier.PARAMETER)) == 0);
        if (outputDone)
            return;
        outputDone = true;
        if (isLiteral() && isClassVariable()) {
            String code = initTermStringOutput(true);
            boolean sameType = initializerTerm.exprType() == resType;
            boolean atomary = initializerTerm.isAtomary();
            oc.hPrint("#define ");
            oc.hPrint(defnClass.castName());
            oc.hPrint("__");
            oc.hPrint(outputName());
            oc.hPrint(" ");
            if (!sameType) {
                oc.hPrint("((");
                oc.hPrint(resType.castName());
                oc.hPrint(")");
            }
            if (!atomary) {
                oc.hPrint("(");
            }
            oc.hPrint(code);
            if (!atomary) {
                oc.hPrint(")");
            }
            if (!sameType) {
                oc.hPrint(")");
            }
            oc.hPrint("\n\n");
        } else {
            if (!isClassVariable() && used) {
                oc.instancePrint(stringOutputDecl(resType.castName(),
                        outputName()));
                oc.instancePrint(";");
            }
            if (!isClassVariable() || !initializerTerm.isLiteral()) {
                int[] curRcvrs = new int[Type.VOID];
                initializerTerm.allocRcvr(curRcvrs);
                String code = initTermStringOutput(false);
                if (code.length() > 0) {
                    Term.assertCond(init != null);
                    if ((!code.equals(LexTerm.NULL_STR)
                            && !code.equals("JLONG_C(0)") && !code.equals("0") && !code
                            .equals(LexTerm.outputBoolean(false)))
                            || !init.isPrevSafeWithThrow()) {
                        if (initializerTerm.exprType() != resType) {
                            code = "("
                                    + resType.castName()
                                    + ")"
                                    + (initializerTerm.isAtomary() ? code : "("
                                            + code + ")");
                        }
                        init.setCode(code, curRcvrs);
                        isValueChanged = true;
                    }
                }
            }
        }
    }

    String fieldOffsetStr() {
        Term.assertCond(used);
        return "(JCGO_OBJSIZE_T)JCGO_OFFSET_OF(struct " + defnClass.castName()
                + (isClassVariable() ? "_class_s, " : "_s, ") + outputName()
                + ")";
    }

    void outputClassVar(OutputContext oc, boolean isReflected) {
        Term.assertCond(used && outputDone
                && (modifiers & AccModifier.STATIC) != 0);
        if (!isReflected) {
            if (resType.objectSize() < Type.CLASSINTERFACE || !isValueChanged) {
                oc.hPrint("JCGO_SEP_EXTERN ");
                oc.cPrint("JCGO_NOSEP_DATA ");
            } else {
                oc.hPrint("JCGO_SEP_GCEXTERN ");
                oc.cPrint("JCGO_NOSEP_GCDATA ");
            }
        }
        if (!isValueChanged) {
            Term.assertCond(!isReflected && !needsLocalVolatile);
            oc.cAndHPrint("CONST ");
        }
        String cast = resType.castName();
        String code;
        boolean isNonZero = false;
        if (isLiteral()) {
            code = stringOutputForStatic(true, true);
        } else {
            code = "";
            if (initializerTerm.isLiteral()) {
                code = initTermStringOutput(true);
                if (code.length() > 0) {
                    if (!isReflected && isValueChanged) {
                        isNonZero = isInitializerNonZero();
                    }
                    if (initializerTerm.exprType() != resType) {
                        code = "("
                                + cast
                                + ")"
                                + (initializerTerm.isAtomary() ? code : "("
                                        + code + ")");
                    }
                }
            }
            if (code.length() == 0) {
                code = resType.objectSize() < Type.CLASSINTERFACE ? "(" + cast
                        + ")0" : LexTerm.NULL_STR;
            }
        }
        if (isReflected) {
            {
                oc.hPrint(stringOutputDecl(cast, outputName()));
            }
        } else {
            oc.cAndHPrint(stringOutputDecl(cast, defnClass.castName() + "__"
                    + outputName()));
            if (isValueChanged) {
                oc.cPrint(resType.objectSize() < Type.CLASSINTERFACE ? (isNonZero ? " ATTRIBNONGC"
                        : "")
                        : isNonZero ? " ATTRIBGCDATA" : " ATTRIBGCBSS");
            }
            oc.cPrint("= ");
        }
        oc.hPrint(";");
        oc.cPrint(code);
    }

    static void sortBySize(VariableDefinition[] vars, int cnt) {
        if (cnt > 1) {
            int i;
            int[] sizes = new int[cnt];
            for (i = 0; i < cnt; i++) {
                sizes[i] = vars[i].sizeOrder();
            }
            for (i = 1; i < cnt; i++) {
                int s = sizes[i];
                VariableDefinition v = vars[i];
                int j;
                for (j = i; j > 0 && sizes[j - 1] > s; j--) {
                    sizes[j] = sizes[j - 1];
                    vars[j] = vars[j - 1];
                }
                sizes[j] = s;
                vars[j] = v;
            }
        }
    }

    static void orderFirstFields(VariableDefinition[] vars, int cnt,
            String[] names) {
        if (cnt > 1) {
            int ofs = 0;
            for (int i = 0; i < names.length; i++) {
                String id = names[i];
                int k;
                for (k = ofs; k < cnt; k++) {
                    if (id.equals(vars[k].name))
                        break;
                }
                if (k < cnt) {
                    if (k > ofs) {
                        VariableDefinition v = vars[k];
                        System.arraycopy(vars, ofs, vars, ofs + 1, k - ofs);
                        vars[ofs] = v;
                    }
                    ofs++;
                }
            }
        }
    }

    void setTraceExprType(ExpressionType curType, boolean reset) {
        if (isLocalOrParam()
                && resType.objectSize() >= Type.CLASSINTERFACE
                && (curTraceType == null || curTraceType.objectSize() != Type.VOID)) {
            if (!curType.hasRealInstances()) {
                curType = Main.dict.classTable[Type.NULLREF];
            }
            if (!reset && curTraceType != null) {
                curTraceType = ClassDefinition.maxCommonExprOf(curTraceType,
                        curType, null);
                ExpressionType actual = actualType != null ? actualType
                        : resType;
                if (!ClassDefinition.isAssignableFrom(actual, curTraceType,
                        null)) {
                    curTraceType = actual;
                }
            } else {
                curTraceType = curType;
            }
        }
    }

    private boolean isInitializerNonZero() {
        ConstValue constVal0;
        int s0 = initializerTerm.exprType().objectSize();
        return s0 != Type.NULLREF
                && ((constVal0 = initializerTerm.evaluateConstValue()) != null ? constVal0
                        .isNonZero() : (s0 != Type.FLOAT && s0 != Type.DOUBLE)
                        || !initializerTerm.isFPZero());
    }

    ExpressionType traceClassInit() {
        if (used && isClassVariable() && !isLiteral()) {
            defnClass
                    .classTraceClassInit(Main.dict.classInitWeakDepend
                            || (hasInitializer() && initializerTerm.isLiteral() && isInitializerNonZero()));
        }
        if (curTraceType == null || curTraceType.objectSize() == Type.VOID)
            return null;
        ExpressionType actual = actualType != null ? actualType : exprType();
        return actual.hasRealInstances() ? (ClassDefinition.isAssignableFrom(
                actual, curTraceType, null) && actual != curTraceType ? curTraceType
                : null)
                : Main.dict.classTable[Type.NULLREF];
    }
}
