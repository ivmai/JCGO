/*
 * @(#) com/ivmaisoft/jpropjav/Main.java --
 * a java properties file to java class source file converter utility.
 **
 * Copyright (C) 2007-2009 Ivan Maidanski <ivmai@mail.ru> All rights reserved.
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

/**
 * This utility reads standard java properties files and generates
 * corresponding java class source files. Every generated class is
 * a subclass of either the space-optimized StrListResourceBundle
 * or the standard ListResourceBundle, and could be instantated by
 * ResourceBundle class.
 */

package com.ivmaisoft.jpropjav;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import java.util.Vector;

public final class Main
{

 static /* final */ String PROGNAME = "JPropJav";

 static /* final */ String VERSION = "1.2";

 static /* final */ String DEFAULT_EXT = ".properties";

 private Main() {}

 public static final void main(String args[])
 {
  if (args.length == 0 || args[0].equals("-help"))
  {
   System.out.println(PROGNAME + " utility v" + VERSION);
   System.out.println(
    "Copyright (C) 2007-2009 Ivan Maidanski <ivmai@mail.ru>");
   System.out.println("Free software. No warranties. All rights reserved.");
   System.out.println();
   System.out.println("This utility generates java class source files from" +
    " the java properties files.");
   System.out.println();
   System.out.println("Arguments:" +
    " [-l] -d <outdir> [-sourcepath <pathlist>] file1" + DEFAULT_EXT +
    " ...");
   System.out.println(
    " Use \"-l\" option to produce classes extending ListResourceBundle");
   System.out.println(
    "(for compatibility only, not recommended for general use).");
   System.out.println(
    " It is also allowed to specify the java source file name or the");
   System.out.println(
    "corresponding java class name instead of a properties file name.");
   System.out.println(
    " To read command-line arguments (a list of the files to be processed)");
   System.out.println("from a response file, prefix its name with '@'.");
   return;
  }
  int i = 0;
  File basedir = null;
  String pathlist = "";
  boolean isStrList = true;
  do
  {
   String arg = args[i];
   if (arg.equals("-l"))
   {
    if (!isStrList || i + 1 >= args.length)
    {
     System.err.println("Invalid arguments!");
     System.exit(1);
    }
    isStrList = false;
   }
    else
    {
     if (arg.equals("-d"))
     {
      if (basedir != null || i + 2 >= args.length)
      {
       System.err.println("Invalid arguments!");
       System.exit(1);
      }
      basedir = new File(args[++i]);
     }
      else
      {
       if (!arg.equals("-sourcepath"))
        break;
       if (i + 2 >= args.length)
       {
        System.err.println("Invalid arguments!");
        System.exit(1);
       }
       i++;
       pathlist = pathlist.length() > 0 ? pathlist +
                   File.pathSeparator + args[i] : args[i];
      }
    }
   i++;
  } while (true);
  if (basedir == null)
  {
   System.err.println("Invalid arguments - no output directory specified!");
   System.exit(1);
  }
  System.out.println("Reading properties files...");
  Vector classNameList = new Vector();
  Vector propLinesList = new Vector();
  do
  {
   String arg = args[i];
   if (arg.startsWith("@"))
   {
    Vector lines = readFileLines(new File(arg.length() == 1 &&
                    args.length - 1 > i ? args[++i] : arg.substring(1)));
    for (int j = 0; lines.size() > j; j++)
    {
     String line = ((String) lines.elementAt(j)).trim();
     if (line.length() > 0 && line.charAt(0) != '#')
      classNameList.addElement(propFileRead(propLinesList, line, pathlist));
    }
   }
    else classNameList.addElement(propFileRead(propLinesList, arg, pathlist));
  } while (++i < args.length);
  for (i = 0; classNameList.size() > i; i++)
   generateFileFor((String) classNameList.elementAt(i),
    (Vector) propLinesList.elementAt(i), basedir, isStrList);
  System.out.println("Done.");
 }

 private static String propFileRead(Vector propLinesList, String propFName,
   String pathlist)
 {
  String className = null;
  if (propFName.endsWith(".java"))
   propFName = propFName.substring(0, propFName.length() - 5) + DEFAULT_EXT;
   else
   {
    if (!propFName.endsWith(DEFAULT_EXT) &&
        (new File(propFName)).getPath().indexOf(File.separatorChar, 0) < 0)
    {
     className = propFName;
     propFName = propFName.replace('.', File.separatorChar) + DEFAULT_EXT;
    }
   }
  File propFile = searchFile(propFName, pathlist);
  Vector propLines = readLatinOneFileLines(propFile);
  if (className == null)
  {
   if (!propFName.endsWith(DEFAULT_EXT) || propFName.lastIndexOf('.',
       propFName.length() - DEFAULT_EXT.length() - 1) >= 0)
   {
    System.err.println("Not a properties file: " + propFName);
    System.exit(1);
   }
   className = makeClassName(propFile, countParentDirs(new File(propFName)));
  }
  propLinesList.addElement(propLines);
  return className;
 }

 private static File searchFile(String name, String pathlist)
 {
  int pos = 0;
  do
  {
   int next = pathlist.indexOf(File.pathSeparatorChar, pos);
   if (next < 0)
    next = pathlist.length();
   if (pos != next)
   {
    File file = new File(pathlist.substring(pos, next), name);
    if (file.exists())
     return file;
   }
   pos = next + 1;
  } while (pathlist.length() > pos);
  return new File(name);
 }

 private static int countParentDirs(File file)
 {
  int count;
  String parent;
  for (count = 0; (parent = file.getParent()) != null; count++)
   file = new File(parent);
  return count;
 }

 private static String makeClassName(File file, int pkgCount)
 {
  try
  {
   file = new File(file.getCanonicalPath());
  }
  catch (IOException e) {}
  String className = file.getName();
  int dotPos = className.lastIndexOf('.');
  if (dotPos >= 0)
   className = className.substring(0, dotPos);
  String parent;
  while (pkgCount-- > 0 && (parent = file.getParent()) != null)
  {
   file = new File(parent);
   className = file.getName() + "." + className;
  }
  return className;
 }

 private static void generateFileFor(String className, Vector propLines,
   File basedir, boolean isStrList)
 {
  System.out.println("Generating file for class: " + className);
  writeFileSafe(basedir, className.replace('.', File.separatorChar) + ".java",
   producePropClass(className, propLines, isStrList));
 }

 private static Vector producePropClass(String className, Vector propLines,
   boolean isStrList)
 {
  int lineInd = 0;
  Vector lines = new Vector();
  lines.addElement("/* DO NOT EDIT THIS FILE - it is machine generated (" +
   PROGNAME + " v" + VERSION + ") */");
  if (isStrList)
   lines.addElement("/* Extends: StrListResourceBundle */");
  lines.addElement("");
  boolean isNext = false;
  do
  {
   while (propLines.size() > lineInd &&
          leftTrim((String) propLines.elementAt(lineInd)).length() == 0)
    lineInd++;
   int cnt;
   for (cnt = 0; propLines.size() - cnt > lineInd; cnt++)
   {
    String line = leftTrim((String) propLines.elementAt(lineInd + cnt));
    if (line.length() == 0)
     break;
    char ch;
    if ((ch = line.charAt(0)) != '#' && ch != '!')
    {
     if (!isNext && (cnt == 0 || ((String) propLines.elementAt(
         lineInd + cnt - 1)).trim().length() == 1))
      break;
     cnt = 0;
     break;
    }
   }
   if (cnt == 0)
    break;
   boolean isBlank = true;
   do
   {
    String line =
     ((String) propLines.elementAt(lineInd++)).trim().substring(1);
    if (line.length() > 0)
    {
     lines.addElement("/" + "/" + line);
     isBlank = false;
    }
     else
     {
      if (!isBlank)
      {
       lines.addElement("");
       isBlank = true;
      }
     }
   } while (--cnt > 0);
   isNext = true;
   if (!isBlank)
    lines.addElement("");
  } while (true);
  int lastDot = className.lastIndexOf('.');
  if (lastDot >= 0)
  {
   lines.addElement("package " + className.substring(0, lastDot) + ";");
   lines.addElement("");
  }
  lines.addElement("public final class " + className.substring(lastDot + 1));
  lines.addElement(isStrList ?
   " extends com.ivmaisoft.jpropjav.StrListResourceBundle" :
   " extends java.util.ListResourceBundle");
  lines.addElement("{");
  lines.addElement("");
  lines.addElement(" protected " + (isStrList ?
   "java.lang.String" : "java.lang.Object[]") + "[] getContents()");
  lines.addElement(" {");
  lines.addElement(" " + " return contents;");
  lines.addElement(" }");
  lines.addElement("");
  lines.addElement(" private static final " + (isStrList ?
   "java.lang.String" : "java.lang.Object[]") + "[] contents =");
  lines.addElement(" {");
  while (propLines.size() > lineInd)
  {
   String line = leftTrim((String) propLines.elementAt(lineInd++));
   if (line.length() > 0)
   {
    char ch = line.charAt(0);
    if (ch == '#' || ch == '!')
     lines.addElement("/" + "/" + line.trim().substring(1));
     else
     {
      StringBuffer sb = new StringBuffer(line.length() + 12);
      sb.append(' ');
      if (!isStrList)
       sb.append(" {");
      sb.append(" \"");
      int pos = 1;
      boolean isValue = false;
      do
      {
       if (ch == '\\')
       {
        if (line.length() == pos)
        {
         if (propLines.size() == lineInd)
          break;
         line += leftTrim((String) propLines.elementAt(lineInd++));
        }
         else
         {
          if ((ch = line.charAt(pos++)) == 't' || ch == 'n' || ch == 'f' ||
              ch == 'r' || ch == 'u' || ch == '\\' || ch == '"')
          {
           sb.append('\\');
           sb.append(ch);
          }
           else appendUnicodeChar(sb, ch);
         }
       }
        else
        {
         if (!isValue && (ch == '=' || ch == ':' || isWhitespace(ch)))
         {
          if (ch == '=' || ch == ':')
           isValue = true;
          while (line.length() > pos)
          {
           ch = line.charAt(pos);
           if (!isWhitespace(ch))
           {
            if (isValue || (ch != '=' && ch != ':'))
             break;
            isValue = true;
           }
           pos++;
          }
          sb.append("\", \"");
          isValue = true;
         }
          else
          {
           if (ch == '"')
           {
            sb.append('\\');
            sb.append('"');
           }
            else appendUnicodeChar(sb, ch);
          }
        }
       if (line.length() == pos)
        break;
       ch = line.charAt(pos++);
      } while (true);
      if (!isValue)
       sb.append("\", \"");
      sb.append(isStrList ? "\"," : "\" },");
      lines.addElement(sb.toString());
     }
   }
  }
  lines.addElement(" };");
  lines.addElement("}");
  return lines;
 }

 private static String leftTrim(String str)
 {
  int len = str.length();
  int pos = 0;
  while (pos < len && isWhitespace(str.charAt(pos)))
   pos++;
  return str.substring(pos);
 }

 private static boolean isWhitespace(char ch)
 {
  return ch <= ' ' && (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\f' ||
          ch == '\r');
 }

 private static void appendUnicodeChar(StringBuffer sb, char ch)
 {
  if (ch < ' ' || ch >= 0x7f)
  {
   sb.append("\\u");
   String str = Integer.toHexString(ch);
   sb.append("000".substring(str.length() - 1));
   sb.append(str);
  }
   else sb.append(ch);
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

 private static Vector readLatinOneFileLines(File file)
 {
  Vector lines = new Vector();
  try
  {
   DataInputStream infile =
    new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
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

 private static Vector readFileLines(File file)
 {
  Vector lines = new Vector();
  try
  {
   BufferedReader infile = new BufferedReader(new FileReader(file));
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

 private static void writeFileLines(File file, Vector lines)
 {
  try
  {
   PrintStream outfile = new PrintStream(new BufferedOutputStream(
                          new FileOutputStream(file)));
   for (int i = 0, count = lines.size(); i < count; i++)
    outfile.println((String) lines.elementAt(i));
   outfile.close();
  }
  catch (IOException e)
  {
   System.err.println("Cannot write file: " + file.getPath());
   System.exit(3);
  }
 }
}
