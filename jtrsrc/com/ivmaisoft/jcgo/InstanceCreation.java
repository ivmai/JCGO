/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/InstanceCreation.java --
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

/**
 * Grammar production for a constructor call.
 ** 
 * Formats: NEW ClassOrIfaceType LPAREN [ArgumentList] RPAREN [ClassBody]
 * Primary DOT NEW ID LPAREN [ArgumentList] RPAREN [ClassBody]
 */

final class InstanceCreation extends LexNode {

    private boolean analysisDone;

    private Term classBody;

    private ClassDefinition cd;

    private MethodDefinition md;

    private boolean isClinitSafe;

    private int primaryIndex;

    private boolean forceCheck;

    private LeftBrace noLeaksScope;

    private boolean isConditional;

    private boolean insideAssertStmt;

    private String stackObjCode;

    private boolean needsLocalVolatile;

    private ExpressionType reflectedClass;

    private String reflectedMethodId;

    private ObjVector reflectedParmSig;

    InstanceCreation(Term b, Term d, Term f) {
        super(Empty.newTerm(), b, d);
        classBody = f;
    }

    InstanceCreation(Term a, Term d, Term f, Term h) {
        super(a, d, f);
        classBody = h;
    }

    void processPass0(Context c) {
        terms[0].processPass0(c);
        terms[2].processPass0(c);
        if (classBody.notEmpty()) {
            String id = c.currentClass.nextAnonymousId();
            cd = Main.dict.get(c.currentClass.name() + "$" + id);
            cd.definePass0(c, c.modifiers & AccModifier.STATIC, id, terms[0]
                    .notEmpty() ? new ClassOrIfaceType(new LexTerm(LexTerm.ID,
                    terms[1].dottedName())) : terms[1], Empty.newTerm(),
                    classBody, false);
        }
    }

    void processPass1(Context c) {
        if (!analysisDone) {
            assertCond(c.currentClass != null);
            analysisDone = true;
            primaryIndex = -1;
            if ((c.forceVmExc & ClassDefinition.NULL_PTR_EXC) != 0) {
                forceCheck = true;
            }
            insideAssertStmt = c.insideAssertStmt;
            terms[2].processPass1(c);
            Term param;
            ExpressionType exprType0;
            ClassDefinition aclass;
            if (terms[0].notEmpty()) {
                param = new Argument(terms[0]);
                param.processPass1(c);
                exprType0 = terms[0].exprType();
                if (exprType0.objectSize() != Type.CLASSINTERFACE) {
                    fatalError(c,
                            "Illegal type of expression for qualified 'new'");
                }
                String name = exprType0.receiverClass().resolveInnerClass(
                        terms[1].dottedName(), false, c.forClass);
                aclass = name != null ? Main.dict.get(name) : c.resolveClass(
                        terms[1].dottedName(), true, false);
                terms[0] = Empty.newTerm();
            } else {
                terms[1].processPass1(c);
                aclass = c.typeClassDefinition;
                param = null;
                exprType0 = null;
            }
            if (cd != null) {
                cd.changeExtendsTerm(aclass, exprType0 != null);
                if (aclass.isInterface()) {
                    aclass = Main.dict.get(Names.JAVA_LANG_OBJECT);
                }
            }
            Term paramList;
            do {
                paramList = Empty.newTerm();
                ObjVector locals = aclass.outerLocals(c.forClass);
                for (int i = locals.size() - 1; i >= 0; i--) {
                    Term t = new Argument(new Expression((new QualifiedName(
                            new LexTerm(LexTerm.ID,
                                    ((VariableDefinition) locals.elementAt(i))
                                            .id()))).setLineInfoFrom(this)));
                    t.processPass1(c);
                    paramList = ParameterList.prepend(t, paramList);
                }
                paramList = terms[2].joinParamLists(paramList);
                if (cd == aclass) {
                    if (exprType0 != null) {
                        paramList = ParameterList.prepend(param, paramList);
                        primaryIndex = 0;
                    }
                    param = null;
                }
                if (!aclass.isStaticClass() && param == null) {
                    ClassDefinition outerClass = aclass.outerClass();
                    if (cd != null) {
                        for (ClassDefinition cdOuter = cd.outerClass(); cdOuter != null; cdOuter = cdOuter
                                .outerClass()) {
                            if (outerClass.isAssignableFrom(cdOuter, 0,
                                    c.forClass)) {
                                outerClass = cdOuter;
                                break;
                            }
                        }
                    }
                    param = new Argument((new This(new ClassOrIfaceType(
                            outerClass))).setLineInfoFrom(this));
                    param.processPass1(c);
                }
                if (param != null) {
                    paramList = ParameterList.prepend(param, paramList);
                    if (exprType0 != null) {
                        primaryIndex++;
                    }
                }
                ObjVector parmSig = paramList.getSignature();
                if (cd == null && aclass.isAbstractOrInterface()) {
                    fatalError(c,
                            "Cannot create an instance of an abstract class: "
                                    + aclass.name());
                }
                md = aclass.matchConstructor(parmSig, c.forClass);
                if (md == null) {
                    undefinedConstructor(aclass, parmSig, c);
                    terms[2] = paramList;
                    return;
                }
                if (cd == null || cd == aclass)
                    break;
                aclass = cd;
                cd.setConstrSuperExpr(exprType0, md, param != null ? 1 : 0,
                        locals.size());
                cd.processPass1(c);
            } while (true);
            terms[2] = paramList;
            md.markNew();
            if (!c.currentClass.name().equals(Names.JAVA_LANG_STRING)
                    || md.definingClass().used()
                    || !aclass.name().equals(
                            Names.JAVA_LANG_STRINGINDEXOUTOFBOUNDSEXCEPTION)
                    || (paramList.notEmpty() && !md.methodSignature()
                            .signatureString().equals(Names.SIGN_INIT_INT))) {
                isClinitSafe = c.addAccessedClass(aclass);
                md.markUsed(aclass, isClinitSafe);
                if (!c.currentClass.name().equals(
                        Names.JAVAX_SWING_UIDEFAULTS_PROXYLAZYVALUE)) {
                    processReflection(c.forClass);
                }
                md.processBranch(c, false);
                isConditional = c.isConditional;
                noLeaksScope = c.localScope;
            } else {
                Main.dict.markStrIndexOutInit = true;
            }
            md.incCallsCount(c.currentMethod);
            md.setArgsFormalType(paramList, md.used() ? c : null);
        }
    }

    ExpressionType exprType() {
        assertCond(analysisDone);
        return md != null ? md.exprType() : Main.dict
                .get(Names.JAVA_LANG_OBJECT);
    }

    ExpressionType actualExprType() {
        return md != null ? md.exprType().receiverClass().asExactClassType()
                : Main.dict.get(Names.JAVA_LANG_OBJECT);
    }

    boolean isNotNull() {
        return md != null && md.used();
    }

    int tokenCount() {
        return terms[2].tokenCount() + 3;
    }

    void allocRcvr(int[] curRcvrs) {
        if (md == null || md.used()) {
            Term t2 = terms[2];
            if (primaryIndex >= 0) {
                Term t0 = (primaryIndex > 0 ? t2.getTermAt(1) : t2)
                        .getTermAt(0);
                if (t0.isNotNull()) {
                    primaryIndex = -1;
                } else {
                    if (t0 == t2) {
                        int[] curRcvrs2 = OutputContext.copyRcvrs(curRcvrs);
                        t2.markParamRcvr(-2, curRcvrs2);
                        t2.allocParamRcvr(curRcvrs,
                                OutputContext.copyRcvrs(curRcvrs), curRcvrs2);
                        return;
                    }
                    t0.markParamRcvr(-2, new int[Type.VOID]);
                }
            }
            t2.allocRcvr(curRcvrs);
        }
    }

    void discoverObjLeaks() {
        assertCond(analysisDone);
        if (md != null && md.used()) {
            md.copyObjLeaksTo(terms[2]);
            ClassDefinition ourClass = md.definingClass();
            MethodDefinition mf = ourClass.getMethod(Names.SIGN_FINALIZE);
            ClassDefinition vrtmClass;
            if ((mf != null
                    && mf.definingClass().superClass() != null
                    && (vrtmClass = Main.dict.get(Names.JAVA_LANG_VMRUNTIME))
                            .used()
                    && (mf = vrtmClass.getMethod(Names.SIGN_FINALIZEOBJECT0X)) != null && mf
                    .isClassMethod())
                    || md.hasThisObjLeak(false)
                    || !ourClass.discoverInstanceObjLeaks()) {
                noLeaksScope = null;
            } else if (md.isThisStackObjVolatile()
                    || ourClass.isInitThisStackObjVolatile()) {
                needsLocalVolatile = true;
            }
        }
        terms[2].discoverObjLeaks();
    }

    void setStackObjVolatile() {
        needsLocalVolatile = true;
    }

    void setObjLeaks(VariableDefinition v) {
        if (v == VariableDefinition.RETURN_VAR && md != null && !isClinitSafe
                && md.needsTrigClinit()
                && md.definingClass().classInitializerNotCalledYet()) {
            v = null;
        }
        noLeaksScope = VariableDefinition.addSetObjLeaksTerm(noLeaksScope, v,
                this, isConditional);
    }

    static String writeStackObjectDefn(OutputContext oc,
            ClassDefinition ourClass, boolean needsTrigClinit,
            boolean insideAssertStmt, boolean needsLocalVolatile) {
        assertCond(ourClass != null);
        String stackObjName = oc.nextStackObjName();
        if (insideAssertStmt) {
            oc.cPrint("\n#ifdef JCGO_ASSERTION\010");
        }
        oc.cPrint("struct ");
        oc.cPrint(ourClass.castName());
        oc.cPrint("_s ");
        oc.cPrint(stackObjName);
        oc.cPrint(";");
        if (insideAssertStmt) {
            oc.cPrint("\n#endif\010");
        }
        return "JCGO_STACKOBJ"
                + (needsLocalVolatile ? "VLT" : "")
                + "_NEW"
                + (needsTrigClinit && ourClass.classInitializerNotCalledYet() ? "TRIG("
                        : "(") + stackObjName + ", " + ourClass.vTableCName()
                + ")";
    }

    String writeStackObjDefn(OutputContext oc, boolean needsLocalVolatile) {
        assertCond(md != null);
        return writeStackObjectDefn(oc, md.definingClass(), false,
                insideAssertStmt, needsLocalVolatile || this.needsLocalVolatile);
    }

    void writeStackObjs(OutputContext oc, Term scopeTerm) {
        terms[2].writeStackObjs(oc, scopeTerm);
        if (noLeaksScope == scopeTerm && md != null && md.used()) {
            assertCond(scopeTerm != null);
            stackObjCode = writeStackObjectDefn(oc, md.definingClass(),
                    !isClinitSafe && md.needsTrigClinit(), insideAssertStmt,
                    needsLocalVolatile);
        }
    }

    void writeStackObjTrigClinit(OutputContext oc) {
        if (!isClinitSafe && md != null && md.used() && md.needsTrigClinit()) {
            md.definingClass().writeTrigClinit(oc);
        }
    }

    ExpressionType writeStackObjRetCode(OutputContext oc) {
        assertCond(md != null && noLeaksScope == null);
        stackObjCode = MethodDefinition.STACKOBJ_RETNAME;
        oc.cPrint(md.definingClass().cNewObjectCode());
        return exprType();
    }

    boolean isAtomary() {
        return true;
    }

    static void writeNewRoutineCall(OutputContext oc, MethodDefinition md,
            String stackObjCode) {
        oc.cPrint(md != null ? (stackObjCode != null ? md.routineCName() : md
                .newRoutineCName()) : MethodDefinition.UNKNOWN_NAME);
        oc.cPrint("(");
        if (stackObjCode != null) {
            oc.cPrint("\010 ");
            oc.cPrint(stackObjCode);
            if (md.hasParameters()) {
                oc.cPrint(", ");
            }
        } else if (md.hasParameters()) {
            oc.cPrint("\010 ");
        }
        Main.dict.normalCalls++;
    }

    void processOutput(OutputContext oc) {
        assertCond(analysisDone);
        Term t2 = terms[2];
        if (md == null || md.used()) {
            if (t2.notEmpty()) {
                oc.cPrint("(");
                t2.produceRcvr(oc);
                if (primaryIndex >= 0) {
                    oc.cPrint(forceCheck ? "JCGO_CALL_EFINALF"
                            : "JCGO_CALL_FINALF");
                    oc.cPrint("(");
                    OutputContext oc2 = new OutputContext();
                    (primaryIndex > 0 ? t2.getTermAt(1) : t2).getTermAt(0)
                            .parameterOutput(oc2, true, Type.CLASSINTERFACE);
                    oc.cPrint(oc2.instanceToString().substring(2));
                    oc.cPrint(") ");
                }
            }
            ClassDefinition ourClass;
            writeNewRoutineCall(
                    oc,
                    md,
                    stackObjCode != null || md == null ? stackObjCode
                            : (ourClass = md.definingClass())
                                    .classInitializerNotCalledYet()
                                    && isClinitSafe && md.needsTrigClinit() ? ourClass
                                    .cNewObjectCode() : null);
            oc.cPrint(OutputContext.paramStringOutputNoComma(t2, true));
            oc.cPrint(")");
            if (t2.notEmpty()) {
                oc.cPrint(")");
            }
        } else {
            oc.cPrint(LexTerm.NULL_STR);
        }
    }

    private void processReflection(ClassDefinition forClass) {
        if (!md.isPublic())
            return;
        if (md.definingClass().name()
                .equals(Names.JAVAX_SWING_UIDEFAULTS_PROXYLAZYVALUE)) {
            String sigString = md.methodSignature().signatureString();
            if (sigString.equals(Names.SIGN_INIT_STRING)
                    || sigString.equals(Names.SIGN_INIT_STRING_OBJECTS)) {
                ExpressionType exprType = MethodInvocation
                        .decodeClassForNameArg(decodeArgAsString(0), null);
                if (exprType != null) {
                    ClassDefinition aclass = exprType.receiverClass();
                    aclass.markUsed();
                    reflectConstructors(
                            aclass,
                            sigString.equals(Names.SIGN_INIT_STRING) ? new ObjVector()
                                    : decodeArgAsValuesArray(1));
                }
                return;
            }
            if (sigString.equals(Names.SIGN_INIT_STRING_STRING)
                    || sigString.equals(Names.SIGN_INIT_STRING_STRING_OBJECTS)) {
                ExpressionType exprType = MethodInvocation
                        .decodeClassForNameArg(decodeArgAsString(0), null);
                if (exprType != null) {
                    ClassDefinition aclass = exprType.receiverClass();
                    aclass.markUsed();
                    Term t = terms[2].getArgumentTerm(1);
                    ObjVector parmSig = sigString
                            .equals(Names.SIGN_INIT_STRING_STRING) ? new ObjVector()
                            : decodeArgAsValuesArray(2);
                    if (t != null
                            && t.actualExprType().objectSize() == Type.NULLREF) {
                        reflectConstructors(aclass, parmSig);
                    } else {
                        reflectMethods(aclass, decodeArgAsString(1), parmSig);
                    }
                }
            }
        }
    }

    private String decodeArgAsString(int index) {
        Term t = terms[2].getArgumentTerm(index);
        return t != null ? t.strLiteralValueGuess() : null;
    }

    private ObjVector decodeArgAsValuesArray(int index) {
        Term t = terms[2].getArgumentTerm(index);
        if (t == null)
            return null;
        ObjVector parmSig = new ObjVector();
        return t.storeClassLiteralsGuess(parmSig, true)
                || t.actualExprType().objectSize() == Type.NULLREF ? parmSig
                : null;
    }

    private void reflectConstructors(ClassDefinition literalClass,
            ObjVector parmSig) {
        if (reflectedMethodId == null) {
            literalClass.reflectConstructors(
                    false,
                    parmSig != null ? (new MethodSignature("<init>", parmSig))
                            .signatureString() : null, true);
            reflectedClass = literalClass.asExactClassType();
            reflectedMethodId = "<init>";
            reflectedParmSig = parmSig;
        }
    }

    private void reflectMethods(ClassDefinition literalClass, String id,
            ObjVector parmSig) {
        if (reflectedMethodId == null) {
            if (id != null && id.length() == 0) {
                id = null;
            }
            literalClass.reflectMethods(id, false, parmSig, true);
            reflectedClass = literalClass;
            reflectedMethodId = id != null ? id : "";
            reflectedParmSig = parmSig;
        }
    }

    ExpressionType traceClassInit() {
        terms[2].traceClassInit();
        if (md != null) {
            ObjVector parmTraceSig = null;
            if (terms[2].notEmpty()) {
                parmTraceSig = new ObjVector();
                terms[2].getTraceSignature(parmTraceSig);
            }
            md.methodTraceClassInit(false, null, parmTraceSig);
            ClassDefinition ourClass = md.definingClass();
            MethodDefinition mf = ourClass.getMethod(Names.SIGN_FINALIZE);
            if (mf != null && mf.definingClass().superClass() != null) {
                ClassDefinition vrtmClass = Main.dict
                        .get(Names.JAVA_LANG_VMRUNTIME);
                MethodDefinition mr;
                if (vrtmClass.used()
                        && (mr = vrtmClass
                                .getMethod(Names.SIGN_FINALIZEOBJECT0X)) != null
                        && mr.isClassMethod()) {
                    mr.methodTraceClassInit(true, null, null);
                }
                mf.methodTraceClassInit(true, ourClass.asExactClassType(), null);
            }
            if (reflectedClass != null) {
                ClassDefinition aclass = reflectedClass.signatureClass();
                if (reflectedMethodId.equals("<init>")) {
                    aclass.traceReflectedConstructor(
                            false,
                            reflectedParmSig != null ? (new MethodSignature(
                                    "<init>", reflectedParmSig))
                                    .signatureString() : null,
                            reflectedClass != aclass);
                } else {
                    aclass.traceReflectedMethod(
                            reflectedMethodId.length() > 0 ? reflectedMethodId
                                    : null, false, reflectedParmSig);
                }
            }
        }
        return null;
    }
}
