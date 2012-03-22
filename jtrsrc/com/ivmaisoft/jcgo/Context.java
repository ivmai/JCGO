/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/Context.java --
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
 * This is the pass one context record.
 */

final class Context {

    String fileName;

    private/* final */ObjQueue importsSingle;

    private/* final */ObjQueue importsOnDemand;

    private BranchContext currentBranch;

    String packageName = "";

    ClassDefinition currentClass;

    Term passZeroMethodDefnTerm;

    MethodDefinition currentMethod;

    ExpressionType currentVarType;

    ClassDefinition typeClassDefinition;

    int typeDims;

    int modifiers;

    Term varInitializer;

    boolean lvalue;

    boolean setOnly;

    boolean hasBreakSimple;

    boolean hasBreakDeep;

    boolean hasContinueSimple;

    boolean hasContinueDeep;

    boolean isConditional;

    LeftBrace localScope;

    TryStatement currentTry;

    ObjHashSet curTryChangedVars;

    BreakableStmt currentLabelStmt;

    TryStatement lastBreakableTry;

    boolean breakableHidden;

    int arrInitCount = -1;

    boolean hasConstructor;

    boolean insideAssertStmt;

    ClassDefinition forClass;

    int forceVmExc;

    Context() {
        importsSingle = new ObjQueue();
        importsOnDemand = new ObjQueue();
    }

    private Context(ObjQueue importsSingle, ObjQueue importsOnDemand) {
        this.importsSingle = importsSingle;
        this.importsOnDemand = importsOnDemand;
    }

    void addImport(String name) {
        if (name.endsWith(".*")) {
            importsOnDemand.addLast(name.substring(0, name.length() - 1));
        } else {
            importsSingle.addLast(name);
        }
    }

    Context cloneForClass(ClassDefinition classDefn, ClassDefinition forClass) {
        Context context = new Context(importsSingle, importsOnDemand);
        context.fileName = fileName;
        context.packageName = packageName;
        context.currentClass = classDefn;
        context.forClass = forClass;
        return context;
    }

    void initBranch() {
        currentBranch = new BranchContext();
        addAccessedClass(Main.dict.get(Names.JAVA_LANG_CLASS));
        currentBranch.accessedClasses.addElement(Main.dict
                .get(Names.JAVA_LANG_STRING));
        addAccessedClass(currentClass);
    }

    BranchContext saveBranch() {
        BranchContext oldBranch = currentBranch;
        Term.assertCond(oldBranch != null);
        currentBranch = new BranchContext(oldBranch);
        return oldBranch;
    }

    BranchContext swapBranch(BranchContext oldBranch) {
        BranchContext otherBranch = currentBranch;
        Term.assertCond(otherBranch != null);
        currentBranch = oldBranch;
        return otherBranch;
    }

    void intersectBranch(BranchContext otherBranch) {
        Term.assertCond(currentBranch != null && otherBranch != null);
        currentBranch.intersectWith(otherBranch, forClass);
    }

    void unionBranch(BranchContext otherBranch) {
        Term.assertCond(currentBranch != null && otherBranch != null);
        currentBranch.unionWith(otherBranch, false, false);
    }

    void unionBranchExceptLocals(BranchContext otherBranch, boolean isThis) {
        Term.assertCond(currentBranch != null);
        currentBranch.unionWith(otherBranch, true, isThis);
    }

    boolean containsAccessedClass(ClassDefinition cd) {
        Term.assertCond(currentBranch != null);
        ObjVector accessedClasses = currentBranch.accessedClasses;
        int i = accessedClasses.identityLastIndexOf(cd);
        return i >= 0 && (i == 0 || accessedClasses.elementAt(i - 1) != cd);
    }

    boolean addAccessedClass(ClassDefinition cd) {
        Term.assertCond(currentBranch != null && cd != null);
        ObjVector accessedClasses = currentBranch.accessedClasses;
        if (cd.objectSize() != Type.CLASSINTERFACE)
            return true;
        ClassDefinition cd2 = cd;
        do {
            int i = accessedClasses.identityLastIndexOf(cd2);
            if (i >= 0) {
                if (i == 0 || accessedClasses.elementAt(i - 1) != cd2)
                    return cd2 == cd;
                accessedClasses.removeElementAt(i);
            } else {
                accessedClasses.addElement(cd2);
            }
        } while (!cd.isInterface() && (cd2 = cd2.superClass(forClass)) != null);
        return false;
    }

    boolean addAccessedClassField(VariableDefinition v) {
        ClassDefinition cd = v.definingClass();
        if (containsAccessedClass(cd))
            return true;
        if (!v.isLiteral()) {
            addAccessedClass(cd);
            return false;
        }
        if (v.isJavaConstant())
            return true;
        ObjVector accessedClasses = currentBranch.accessedClasses;
        if (accessedClasses.identityLastIndexOf(cd) >= 0)
            return true;
        accessedClasses.addElement(cd);
        accessedClasses.addElement(cd);
        if (!cd.isInterface()) {
            while ((cd = cd.superClass(forClass)) != null
                    && accessedClasses.identityLastIndexOf(cd) < 0) {
                accessedClasses.addElement(cd);
                accessedClasses.addElement(cd);
            }
        }
        return false;
    }

    void setVarNotNull(VariableDefinition v) {
        Term.assertCond(currentBranch != null);
        if (v.isLocalOrParam() || v.isFinalVariable()) {
            ObjVector nonNullVars = currentBranch.nonNullVars;
            if (nonNullVars.identityLastIndexOf(v) < 0) {
                nonNullVars.addElement(v);
            }
        }
    }

    void clearVarNotNull(VariableDefinition v) {
        Term.assertCond(currentBranch != null);
        ObjVector nonNullVars = currentBranch.nonNullVars;
        int index = nonNullVars.identityLastIndexOf(v);
        if (index >= 0) {
            nonNullVars.removeElementAt(index);
        }
    }

    boolean isVarNotNull(VariableDefinition v) {
        Term.assertCond(currentBranch != null);
        return currentBranch.nonNullVars.identityLastIndexOf(v) >= 0;
    }

    void setActualType(VariableDefinition v, ExpressionType actualType) {
        Term.assertCond(currentBranch != null && actualType != null);
        int s0;
        if ((v.isLocalOrParam() || v.isFinalVariable())
                && ((s0 = v.exprType().objectSize()) == Type.CLASSINTERFACE || s0 == Type.OBJECTARRAY)) {
            ObjVector actTypeVars = currentBranch.actTypeVars;
            int index = actTypeVars.identityLastIndexOf(v);
            if (index >= 0) {
                currentBranch.varActualTypes.setElementAt(actualType, index);
            } else {
                actTypeVars.addElement(v);
                currentBranch.varActualTypes.addElement(actualType);
            }
        }
    }

    void resetVarForLoopOrTry(VariableDefinition v) {
        clearVarNotNull(v);
        ObjVector actTypeVars = currentBranch.actTypeVars;
        int index = actTypeVars.identityLastIndexOf(v);
        if (index >= 0) {
            actTypeVars.removeElementAt(index);
            currentBranch.varActualTypes.removeElementAt(index);
        }
    }

    ExpressionType getActualType(VariableDefinition v) {
        Term.assertCond(currentBranch != null);
        return currentBranch.getActualType(v);
    }

    ClassDefinition resolveClass(String name, boolean allowNotFound,
            boolean checkFields) {
        int i = name.indexOf('.', 0);
        String first = i >= 0 ? name.substring(0, i) : name;
        String qualified = null;
        if (localScope != null) {
            qualified = localScope.resolveLocalClass(first);
        }
        if (qualified == null) {
            ClassDefinition cd = currentClass;
            while (cd != null) {
                LeftBrace scope = cd.outerScope();
                if (scope != null) {
                    qualified = scope.resolveLocalClass(first);
                    if (qualified != null)
                        break;
                }
                qualified = cd.resolveInnerClass(first, checkFields, forClass);
                if (qualified != null)
                    break;
                cd = cd.outerClass();
            }
        }
        if (qualified != null && qualified.length() == 0)
            return null;
        if (qualified == null || Main.dict.get(qualified).outerClass() == null) {
            first = getQualifiedName(first);
            if (first != null) {
                qualified = first;
            }
        }
        if (i < 0) {
            if (qualified == null) {
                if (!allowNotFound)
                    return null;
                qualified = packageName + name;
            }
            return Main.dict.get(qualified);
        }
        do {
            if (qualified != null && Main.dict.exists(qualified)) {
                qualified = Main.dict.get(qualified).resolveInnerClass(
                        name.substring(i + 1), checkFields, forClass);
                if (qualified != null)
                    return qualified.length() > 0 ? Main.dict.get(qualified)
                            : null;
            }
            i = name.indexOf('.', i + 1);
            if (i < 0)
                break;
            qualified = name.substring(0, i);
        } while (true);
        return allowNotFound || Main.dict.exists(name) ? Main.dict.get(name)
                : null;
    }

    private String getQualifiedName(String id) {
        String dotId = "." + id;
        String qualified;
        Enumeration en = importsSingle.elements();
        while (en.hasMoreElements()) {
            qualified = (String) en.nextElement();
            if (qualified.endsWith(dotId)) {
                id = qualified;
                dotId = "";
                int i;
                while (!Main.dict.exists(qualified)) {
                    i = qualified.lastIndexOf('.');
                    if (i < 0)
                        return id;
                    dotId = "$" + qualified.substring(i + 1) + dotId;
                    qualified = qualified.substring(0, i);
                }
                return qualified + dotId;
            }
        }
        qualified = packageName + id;
        if (!Main.dict.exists(qualified)) {
            Enumeration en2 = importsOnDemand.elements();
            while (en2.hasMoreElements()) {
                qualified = (String) en2.nextElement();
                int i = qualified.length() - 1;
                qualified = qualified + id;
                if (Main.dict.exists(qualified)
                        && Main.dict.get(qualified).isPublic())
                    return qualified;
                do {
                    qualified = qualified.substring(0, i) + "$"
                            + qualified.substring(i + 1);
                    if (Main.dict.existsOrInner(qualified))
                        return qualified;
                    i = qualified.lastIndexOf('.', i - 1);
                } while (i > 0);
            }
            qualified = Names.JAVA_LANG_0 + id;
            if (!Main.dict.exists(qualified)) {
                qualified = Main.dict.exists(id) ? id : null;
            }
        }
        return qualified;
    }
}
