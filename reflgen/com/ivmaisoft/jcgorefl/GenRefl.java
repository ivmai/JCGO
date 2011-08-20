/*
 * @(#) $(JCGO)/reflgen/com/ivmaisoft/jcgorefl/GenRefl.java --
 * "GenRefl" utility source (part of JCGO).
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * Use is subject to license terms. No warranties. All rights reserved.
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
 */

/**
 * The utility reads data file (eg., produced by "TraceJni" utility) and
 * generates helper VMReflector class source files for JCGO translator.
 * The data file contains method/field reflection dependency info, i.e.
 * which reflected constructors, methods, fields (and proxy classes) are
 * used and by which methods.
 */

package com.ivmaisoft.jcgorefl;

import java.io.*;

import java.util.Enumeration;
import java.util.Vector;

public final class GenRefl
{

 static /* final */ String PROGNAME = "JCGO GenRefl";

 static /* final */ String VERSION = "1.2";

 private GenRefl() {}

 private static Vector readFileLines(File file)
 {
  Vector lines = new Vector();
  try
  {
   BufferedReader infile = new BufferedReader(new FileReader(file));
 // Portability - use the following for Java 1.0
 // DataInputStream infile = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
   String str;
   while ((str = infile.readLine()) != null)
    lines.addElement(str);
   infile.close();
  }
  catch (IOException e)
  {
   System.err.println("Cannot read file: " + file.getPath());
   System.exit(2);
  }
  return lines;
 }

 public static final void main(String args[])
 {
  if (args.length <= 2 || !args[0].equals("-d"))
  {
   System.out.println(PROGNAME + " utility v" + VERSION);
   System.out.println(
    "Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>");
   System.out.println(
    "This is free software. All rights reserved. See README file.");
   System.out.println(
    "The utility generates VMReflector class source files for JCGO.");
   System.out.println();
   System.out.println("Arguments: -d <outdir> file1.dat ...");
   if (args.length > 0)
    System.exit(1);
   return;
  }
  int i = 2;
  Vector packages = new Vector();
  do
  {
   readAndDecodeFile(packages, args[i]);
  } while (++i < args.length);
  System.out.println("Writing helper java files...");
  writeAllPackages(new File(args[1]), packages);
  System.out.println("Done.");
 }

 private static void readAndDecodeFile(Vector packages, String filename)
 {
  System.out.println("Reading data file: " + filename);
  Vector lines = readFileLines(new File(filename));
  System.out.println("Decoding data...");
  decodeData(packages, lines);
 }

 private static void writeFileSafe(File basedir, String filename,
   Vector lines)
 {
  File file = new File(basedir, filename);
  String parent = file.getParent();
  if (parent != null && basedir.exists())
   (new File(parent)).mkdirs();
  writeFileLines(file, lines);
 }

 private static void writeFileLines(File file, Vector lines)
 {
  try
  {
   PrintStream outfile = new PrintStream(new BufferedOutputStream(
                          new FileOutputStream(file)));
   Enumeration en = lines.elements();
   while (en.hasMoreElements())
    outfile.println((String) en.nextElement());
   outfile.close();
  }
  catch (IOException e)
  {
   System.err.println("Cannot write file: " + file.getPath());
   System.exit(3);
  }
 }

 private static void decodeData(Vector packages, Vector lines)
 {
  Enumeration en = lines.elements();
  for (int lineNum = 1; en.hasMoreElements(); lineNum++)
  {
   String str = ((String) en.nextElement()).trim();
   if (str.length() > 0 && str.charAt(0) != '#' && (!str.endsWith(".*") ||
       !isValidName(str.substring(0, str.length() - 2))) &&
       !decodeDataLine(packages, str))
   {
    System.err.println("Data file error at line: " + lineNum);
    System.exit(4);
   }
  }
 }

 private static boolean decodeDataLine(Vector packages, String str)
 {
  String parts[] = splitDataLine(str);
  if (parts == null)
   return false;
  if (parts[1].equals("?") || parts[2].equals("?") ||
      parts[4].equals("<clinit>()"))
   return true;
  Object apackage[] = getElementArrByName(packages, parts[0]);
  Object aclass[] = getElementArrByName((Vector) apackage[1], parts[1]);
  Object method[] = getElementArrByName((Vector) aclass[1],
                     parts[2].equals("*") ? "_" :
                     parts[2].equals("<native>") ? "_native" :
                     parts[2].equals("<clinit>") ? "_clinit" : parts[2]);
  Object klass[] = getElementArrByName((Vector) method[1], parts[3]);
  return listInsertStr((Vector) klass[1], parts[4]) ||
          !parts[2].equals(method[0]);
 }

 private static String[] splitDataLine(String str)
 {
  String parts[] = new String[5];
  int colonPos = str.indexOf(':');
  if (colonPos <= 3 || str.charAt(colonPos - 3) != '(' ||
      str.charAt(colonPos - 2) != '*' || str.charAt(colonPos - 1) != ')')
   return null;
  int aclassEndPos = str.lastIndexOf('.', colonPos - 4);
  if (aclassEndPos < 0)
   return null;
  int pkgEndPos = str.lastIndexOf('.', aclassEndPos - 1);
  if (pkgEndPos >= 0)
  {
   parts[0] = str.substring(0, pkgEndPos);
   if (!isValidName(parts[0]))
    return null;
  }
   else parts[0] = "";
  parts[1] = str.substring(pkgEndPos + 1, aclassEndPos);
  if (!parts[1].equals("?") && !isValidId(parts[1]))
   return null;
  String id = str.substring(aclassEndPos + 1, colonPos - 3);
  if (!id.equals("?") && !id.equals("<init>") && !id.equals("<clinit>") &&
      !id.equals("<native>") && !id.equals("*") && !isValidId(id))
   return null;
  parts[2] = id;
  int sigPos = str.indexOf('(', colonPos);
  if (sigPos < 0)
   sigPos = str.length();
  int klassEndPos = str.lastIndexOf('.', sigPos - 1);
  if (klassEndPos > colonPos)
  {
   parts[3] = str.substring(colonPos + 1, klassEndPos);
   if (!isValidName(parts[3]) ||
       parts[3].equals(str.substring(0, aclassEndPos)))
    return null;
  }
   else
   {
    parts[3] = str.substring(0, aclassEndPos);
    klassEndPos = colonPos;
   }
  id = str.substring(klassEndPos + 1, sigPos);
  if (id.equals("<init>") || id.equals("<clinit>"))
  {
   if (sigPos >= str.length())
    return null;
  }
   else if (!id.equals("*") && !isValidId(id))
    return null;
  if (sigPos < str.length())
  {
   if (str.charAt(str.length() - 1) != ')')
    return null;
   if (str.length() - sigPos != 3 || str.charAt(sigPos + 1) != '*')
   {
    if (str.indexOf('.', sigPos + 1) >= 0)
     return null;
    char ch;
    while ((ch = str.charAt(++sigPos)) != ')')
    {
     while (ch == '[')
      ch = str.charAt(++sigPos);
     if (ch == 'L')
     {
      int pos = str.indexOf(';', sigPos + 1);
      if (pos < 0 ||
          !isValidName(str.substring(sigPos + 1, pos).replace('/', '.')))
       return null;
      sigPos = pos;
     }
      else if ("ZBCSIJFD".indexOf(ch) < 0)
       return null;
    }
   }
  }
  parts[4] = str.substring(klassEndPos + 1);
  return parts;
 }

 private static Object[] getElementArrByName(Vector vect, String name)
 {
  int low = 0;
  int high = vect.size() - 1;
  Object element[];
  while (low <= high)
  {
   int mid = (low + high) >> 1;
   element = (Object[]) vect.elementAt(mid);
   int cmp = strCompare(name, (String) element[0]);
   if (cmp == 0)
    return element;
   if (cmp > 0)
    low = mid + 1;
    else high = mid - 1;
  }
  element = new Object[2];
  element[0] = name;
  element[1] = new Vector();
  vect.insertElementAt(element, low);
  return element;
 }

 private static boolean listInsertStr(Vector vect, String str)
 {
  int pos = listSearchStr(vect, str);
  if (pos >= 0)
   return false;
  vect.insertElementAt(str, -pos - 1);
  return true;
 }

 private static boolean listContainsStartsWith(Vector vect, String str)
 {
  int pos = listSearchStr(vect, str);
  if (pos >= 0)
   return true;
  pos = -pos - 1;
  return vect.size() > pos && ((String) vect.elementAt(pos)).startsWith(str);
 }

 private static int listSearchStr(Vector vect, String str)
 {
  int low = 0;
  int high = vect.size() - 1;
  while (low <= high)
  {
   int mid = (low + high) >> 1;
   int cmp = strCompare(str, (String) vect.elementAt(mid));
   if (cmp == 0)
    return mid;
   if (cmp > 0)
    low = mid + 1;
    else high = mid - 1;
  }
  return -low - 1;
 }

 private static boolean isValidName(String name)
 {
  int pos = 0;
  do
  {
   int next = name.indexOf('.', pos);
   if (!isValidId(name.substring(pos, next >= 0 ? next : name.length())))
    return false;
   pos = next + 1;
  } while (pos > 0);
  return true;
 }

 private static boolean isValidId(String id)
 {
  int pos = id.length();
  if (pos == 0)
   return false;
  char ch;
  do
  {
   ch = id.charAt(--pos);
   if ((ch < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z') &&
       (ch < '0' || ch > '9') && ch != '_' && ch != '$')
    return false;
  } while (pos > 0);
  return ch < '0' || ch > '9';
 }

 private static void writeAllPackages(File basedir, Vector packages)
 {
  Enumeration en = packages.elements();
  while (en.hasMoreElements())
  {
   Object apackage[] = (Object[]) en.nextElement();
   writeFileForPackage(basedir, (String) apackage[0], (Vector) apackage[1]);
  }
 }

 private static void writeFileForPackage(File basedir, String id,
   Vector classes)
 {
  System.out.println("Producing VMReflector for package: " +
   (id.length() > 0 ? id : "<unspecified>"));
  writeFileSafe(basedir, (id.length() > 0 ?
   id.replace('.', File.separatorChar) + File.separator : "") +
   "VMReflector.java", produceLinesForPackage(id, classes));
 }

 private static Vector produceLinesForPackage(String id, Vector classes)
 {
  Vector lines = new Vector();
  lines.addElement("/* DO NOT EDIT THIS FILE - it is machine generated (" +
   PROGNAME + " v" + VERSION + ") */");
  lines.addElement("");
  if (id.length() > 0)
  {
   lines.addElement("package " + id + ";");
   lines.addElement("");
  }
  lines.addElement("final class VMReflector");
  lines.addElement("{");
  Enumeration en = classes.elements();
  while (en.hasMoreElements())
  {
   Object aclass[] = (Object[]) en.nextElement();
   produceLinesForClass(lines, (String) aclass[0], (Vector) aclass[1]);
  }
  lines.addElement("}");
  return lines;
 }

 private static void produceLinesForClass(Vector lines, String id,
   Vector methods)
 {
  lines.addElement("");
  lines.addElement(" static final class _" + id);
  lines.addElement(" {");
  Enumeration en = methods.elements();
  while (en.hasMoreElements())
  {
   Object method[] = (Object[]) en.nextElement();
   produceLinesForMethod(lines, id, (String) method[0], (Vector) method[1]);
  }
  lines.addElement(" }");
 }

 private static void produceLinesForMethod(Vector lines, String classId,
   String id, Vector klasses)
 {
  lines.addElement("");
  lines.addElement(" " + " " + (id.equals("<init>") ? "_" + classId :
   "void " + id) + "()");
  lines.addElement(" " + " " + " throws java.lang.Exception");
  lines.addElement(" " + " {");
  Enumeration en = klasses.elements();
  while (en.hasMoreElements())
  {
   Object klass[] = (Object[]) en.nextElement();
   produceGetDeclaredFor(lines, (String) klass[0], (Vector) klass[1]);
  }
  lines.addElement(" " + " }");
 }

 private static void produceGetDeclaredFor(Vector lines, String klassName,
   Vector nameSigs)
 {
  boolean allFields = listSearchStr(nameSigs, "*") >= 0;
  boolean allCtors = listSearchStr(nameSigs, "<init>(*)") >= 0;
  boolean allMethods = listContainsStartsWith(nameSigs, "*(");
  if (allFields)
   produceGetDeclaredField(lines, klassName, "*");
  if (allCtors)
  {
   String ifacesSig = decodeProxyClassName(klassName);
   if (ifacesSig != null)
    produceGetProxyClass(lines, ifacesSig);
    else produceGetDeclaredConstr(lines, klassName, "*");
  }
  if (allMethods)
   produceGetDeclaredMethod(lines, klassName, "*", "*");
  Enumeration en = nameSigs.elements();
  while (en.hasMoreElements())
  {
   String nameSig = (String) en.nextElement();
   if (nameSig.endsWith(")"))
   {
    int sigPos = nameSig.indexOf('(');
    if (nameSig.startsWith("<init>"))
    {
     if (!allCtors)
     {
      String ifacesSig;
      if (nameSig.equals("<init>(Ljava/lang/reflect/InvocationHandler;)") &&
          (ifacesSig = decodeProxyClassName(klassName)) != null)
       produceGetProxyClass(lines, ifacesSig);
       else produceGetDeclaredConstr(lines, klassName,
             nameSig.substring(sigPos + 1, nameSig.length() - 1));
     }
    }
     else
     {
      if (!allMethods && (nameSig.endsWith("(*)") ||
          listSearchStr(nameSigs, nameSig.substring(0, sigPos) + "(*)") < 0))
       produceGetDeclaredMethod(lines, klassName, nameSig.substring(0,
        sigPos), nameSig.substring(sigPos + 1, nameSig.length() - 1));
     }
   }
    else
    {
     if (!allFields)
      produceGetDeclaredField(lines, klassName, nameSig);
    }
  }
 }

 private static String decodeProxyClassName(String klassName)
 {
  klassName = cutStrPrefix(klassName.substring(
               klassName.lastIndexOf('.') + 1), "$Proxy");
  if (klassName == null)
   return null;
  String ifacesSig = "";
  int next = klassName.length();
  while (next > 0)
  {
   int pos = klassName.lastIndexOf("$00", next - 1);
   if (pos < 0)
    return null;
   String name = klassName.substring(pos + 3, next);
   if (name.length() == 0)
    return null;
   next = pos;
   pos = -1;
   while ((pos = name.indexOf("$0", pos + 1)) >= 0)
    name = name.substring(0, pos) + "/" + name.substring(pos + 2);
   pos = -1;
   while ((pos = name.indexOf("$$", pos + 1)) >= 0)
    name = name.substring(0, pos) + name.substring(pos + 1);
   ifacesSig = "L" + name + ";" + ifacesSig;
  }
  return ifacesSig;
 }

 private static String cutStrPrefix(String str, String prefix)
 {
  return str.startsWith(prefix) ? str.substring(prefix.length()) : null;
 }

 private static void produceGetProxyClass(Vector lines, String ifacesSig)
 {
  lines.addElement(" " + " " +
   " java.lang.reflect.Proxy.getProxyClass(null, new java.lang.Class[]" +
   (ifacesSig.length() > 0 ? "" : " {});"));
  if (ifacesSig.length() > 0)
   produceGetDeclaredTypesTail(lines, ifacesSig);
 }

 private static void produceGetDeclaredField(Vector lines, String klassName,
   String id)
 {
  produceGetDeclaredHead(lines, klassName, true);
  lines.addElement(" " + " " + " " + " " + (id.equals("*") ?
   "getDeclaredFields(" : "getDeclaredField(\"" + id + "\"") + ");");
 }

 private static void produceGetDeclaredHead(Vector lines, String klassName,
   boolean initialize)
 {
  lines.addElement(" " + " " + " " + produceClassLiteral(klassName, 0,
   initialize) + ".");
 }

 private static void produceGetDeclaredConstr(Vector lines, String klassName,
   String sig)
 {
  produceGetDeclaredHead(lines, klassName, false);
  lines.addElement(" " + " " + " " + " " + (sig.equals("*") ?
   "getDeclaredConstructors();" : "getDeclaredConstructor(" +
   "new java.lang.Class[]" + (sig.length() > 0 ? "" : " {});")));
  if (sig.length() > 0 && !sig.equals("*"))
   produceGetDeclaredTypesTail(lines, sig);
 }

 private static void produceGetDeclaredMethod(Vector lines, String klassName,
   String id, String sig)
 {
  produceGetDeclaredHead(lines, klassName, true);
  lines.addElement(" " + " " + " " + " " + (id.equals("*") ?
   "getDeclaredMethods();" : "getDeclaredMethod(\"" + id + "\", " +
    (sig.equals("*") ? "null);" : "new java.lang.Class[]" +
    (sig.length() > 0 ? "" : " {});"))));
  if (sig.length() > 0 && !id.equals("*") && !sig.equals("*"))
   produceGetDeclaredTypesTail(lines, sig);
 }

 private static void produceGetDeclaredTypesTail(Vector lines,
   String sig)
 {
  lines.addElement(" " + " " + " " + " {");
  int sigPos = 0;
  while (sig.length() > sigPos)
  {
   int dims = 0;
   char ch;
   while ((ch = sig.charAt(sigPos++)) == '[')
    dims++;
   String typeName;
   switch (ch)
   {
   case 'Z':
    typeName = "boolean";
    break;
   case 'B':
    typeName = "byte";
    break;
   case 'C':
    typeName = "char";
    break;
   case 'S':
    typeName = "short";
    break;
   case 'I':
    typeName = "int";
    break;
   case 'J':
    typeName = "long";
    break;
   case 'F':
    typeName = "float";
    break;
   case 'D':
    typeName = "double";
    break;
   default:
    int pos = sig.indexOf(';', sigPos);
    typeName = sig.substring(sigPos, pos).replace('/', '.');
    sigPos = pos + 1;
    break;
   }
   lines.addElement(" " + " " + " " + " " + " " +
    produceClassLiteral(typeName, dims, false) + ",");
  }
  lines.addElement(" " + " " + " " + " });");
 }

 private static String produceClassLiteral(String klassName, int dims,
   boolean initialize)
 {
  return isCoreClassName(klassName) ? klassName + strFill("[]", dims) +
          ".class" : "java.lang.Class.forName(\"" + (dims > 0 ?
          strFill("[", dims) + "L" + klassName + ";" : klassName) + "\"" +
          (initialize || dims > 0 ? "" : ", false, null") + ")";
 }

 private static boolean isCoreClassName(String klassName)
 {
  return klassName.equals("boolean") || klassName.equals("byte") ||
          klassName.equals("char") || klassName.equals("short") ||
          klassName.equals("int") || klassName.equals("long") ||
          klassName.equals("float") || klassName.equals("double") ||
          klassName.equals("java.lang.Class") ||
          klassName.equals("java.lang.Object") ||
          klassName.equals("java.lang.String");
 }

 private static int strCompare(String str, String str2)
 {
  int cmp = 0;
  int len1 = str.length();
  int len2 = str2.length();
  int len = len1 < len2 ? len1 : len2;
  for (int i = 0; i < len; i++)
   if ((cmp = str.charAt(i) - str2.charAt(i)) != 0)
    break;
  if (cmp == 0)
   cmp = len1 - len2;
  return cmp;
 }

 private static String strFill(String str, int cnt)
 {
  String resStr = "";
  while (cnt-- > 0)
   resStr += str;
  return resStr;
 }
}
