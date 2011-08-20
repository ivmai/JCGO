/*
 * @(#) $(JCGO)/goclsp/vm/java/util/VMTimeZone.java --
 * VM specific methods for getting system timezone id.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 **
 * Class specification origin: GNU Classpath v0.93 vm/reference
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

package java.util;

import gnu.classpath.SystemProperties;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.VMAccessorJavaIo;

import java.text.DateFormatSymbols;

final class VMTimeZone
{

 private static final String[] TZ_ABBRS_MAP =
 {
  "ACT", "Australia/Darwin",
  "AET", "Australia/Sydney",
  "AGT", "America/Cayenne",
  "ART", "Africa/Cairo",
  "AST", "America/Anchorage",
  "BET", "America/Sao_Paulo",
  "BST", "Asia/Dhaka",
  "CAT", "Africa/Harare",
  "CET", "Europe/Amsterdam",
  "CNT", "America/St_Johns",
  "CST", "America/Chicago",
  "CTT", "Asia/Shanghai",
  "Cuba", "America/Havana",
  "EAT", "Africa/Addis_Ababa",
  "ECT", "Europe/Paris",
  "EET", "Europe/Athens",
  "EST", "America/New_York",
  "Egypt", "Africa/Cairo",
  "Eire", "Europe/Dublin",
  "GB", "Europe/London",
  "GB-Eire", "Europe/Dublin",
  "Greenwich", "GMT",
  "HST", "Pacific/Honolulu",
  "Hongkong", "Asia/Hong_Kong",
  "IET", "America/Indianapolis",
  "IST", "Asia/Calcutta",
  "Iceland", "Atlantic/Reykjavik",
  "Iran", "Asia/Tehran",
  "Israel", "Asia/Jerusalem",
  "JST", "Asia/Tokyo",
  "Jamaica", "America/Jamaica",
  "Japan", "Asia/Tokyo",
  "Kwajalein", "Pacific/Kwajalein",
  "Libya", "Africa/Tripoli",
  "MET", "Asia/Tehran",
  "MIT", "Pacific/Apia",
  "MST", "America/Denver",
  "NET", "Asia/Yerevan",
  "NST", "Pacific/Auckland",
  "NZ", "Pacific/Auckland",
  "NZ-CHAT", "Pacific/Chatham",
  "Navajo", "America/Shiprock",
  "PLT", "Asia/Karachi",
  "PNT", "America/Phoenix",
  "PRC", "Asia/Harbin",
  "PRT", "America/Puerto_Rico",
  "PST", "America/Los_Angeles",
  "Poland", "Europe/Warsaw",
  "Portugal", "Europe/Lisbon",
  "ROC", "Asia/Taipei",
  "ROK", "Asia/Seoul",
  "SST", "Pacific/Guadalcanal",
  "Singapore", "Asia/Singapore",
  "Turkey", "Asia/Istanbul",
  "UCT", "GMT",
  "UTC", "GMT",
  "Universal", "GMT",
  "VST", "Asia/Saigon",
  "W-SU", "Europe/Moscow",
  "WET", "Europe/Lisbon",
  "Zulu", "GMT",
 };

 private VMTimeZone() {}

 static TimeZone getDefaultTimeZoneId()
 {
  TimeZone[] defZoneArr = new TimeZone[1];
  TimeZone zone = convertTzIdToZone(defZoneArr,
                   VMAccessorJavaIo.getenvPlatformVMFile("TZ"));
  if (zone == null)
  {
   boolean isEtcLocaltime = false;
   zone = defZoneArr[0];
   if (!"\\".equals(File.separator) && (zone = convertTzIdToZone(defZoneArr,
       readSysconfigClockFile("/etc/default/init"))) == null &&
       (zone = convertTzIdToZone(defZoneArr,
       readTimezoneFile("/etc/timezone"))) == null &&
       (zone = convertTzIdToZone(defZoneArr,
       readSysconfigClockFile("/etc/sysconfig/clock"))) == null)
   {
    isEtcLocaltime = true;
    if ((zone = convertTzIdToZone(defZoneArr,
        processLocaltimeFile("/etc/localtime"))) == null)
     zone = defZoneArr[0];
   }
   if ((isEtcLocaltime || zone == null) && (defZoneArr[0] =
       getTimeZoneFor(getSystemTimeZoneId())) != null)
   {
    int rawOffset;
    if (zone == null || ((rawOffset = defZoneArr[0].getRawOffset()) != 0 &&
        (zone.getRawOffset() != rawOffset || zone.useDaylightTime() !=
        defZoneArr[0].useDaylightTime())))
     zone = defZoneArr[0];
   }
  }
  if (zone != null && (zone.getID().indexOf('/', 0) > 0 ||
      TimeZone.getTimeZone("GMT") == zone))
   SystemProperties.setProperty("user.timezone", zone.getID());
  return zone;
 }

 private static TimeZone convertTzIdToZone(TimeZone[] defZoneArr, String tzid)
 {
  TimeZone zone = null;
  if (tzid != null)
  {
   tzid = removeTzIdPrefix(tzid.trim());
   zone = getTimeZoneFor(tzid);
   if (zone == null && defZoneArr[0] == null && tzid.length() > 0)
    defZoneArr[0] = TimeZone.getDefaultTimeZone(tzid);
  }
  return zone;
 }

 private static String removeTzIdPrefix(String tzid)
 {
  if (tzid.startsWith(":") && tzid.length() > 1)
   tzid = tzid.substring(1);
  if ((tzid.startsWith("posix/") || tzid.startsWith("right/")) &&
      tzid.length() > 7)
   tzid = tzid.substring(6);
  return tzid;
 }

 private static String readTimezoneFile(String filename)
 {
  InputStreamReader in = null;
  StringBuilder sb = null;
  try
  {
   in = new InputStreamReader(new BufferedInputStream(new FileInputStream(
         filename)));
   sb = new StringBuilder();
   int c;
   while ((c = in.read()) >= 0 && ((c >= 'A' && c <= 'Z') ||
          (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') ||
          c == '/' || c == '-' || c == '_'))
    sb.append((char) c);
  }
  catch (IOException e) {}
  if (in != null)
  {
   try
   {
    in.close();
   }
   catch (IOException e) {}
  }
  return sb != null ? sb.toString() : null;
 }

 private static String readSysconfigClockFile(String filename)
 {
  BufferedReader in = null;
  String line = null;
  try
  {
   in = new BufferedReader(new InputStreamReader(new FileInputStream(
         filename)));
   while ((line = in.readLine()) != null)
   {
    int pos = line.indexOf('=', 0);
    if (pos > 0)
    {
     String key = line.substring(0, pos).trim();
     if (key.equals("ZONE") || key.equals("TIMEZONE") || key.equals("TZ"))
     {
      line = line.substring(pos + 1).trim();
      if (line.length() > 1)
       break;
     }
    }
   }
  }
  catch (IOException e) {}
  if (in != null)
  {
   try
   {
    in.close();
   }
   catch (IOException e) {}
   if (line != null)
   {
    char ch = line.charAt(0);
    int pos;
    if (ch == '"')
     line = (pos = line.indexOf('"', 1)) > 2 ? line.substring(1, pos) : null;
     else
     {
      if (ch == '\'')
       line = (pos = line.indexOf('\'', 1)) > 2 ?
               line.substring(1, pos) : null;
       else
       {
        pos = line.indexOf(' ', 1);
        if (pos > 1)
         line = line.substring(0, pos);
       }
     }
   }
  }
  return line;
 }

 private static String processLocaltimeFile(String filename)
 {
  String str = filename;
  try
  {
   str = VMAccessorJavaIo.realPathVMFile(filename);
  }
  catch (IOException e) {}
  if (!filename.equals(str))
  {
   String zoneinfoPrefix = "/zoneinfo/";
   int pos = str.indexOf(zoneinfoPrefix);
   if (pos >= 0)
   {
    str = str.substring(pos + zoneinfoPrefix.length());
    if (str.length() > 1)
     return str;
   }
  }
  return null;
 }

 private static TimeZone getTimeZoneFor(String tzid)
 {
  int len = tzid.length();
  TimeZone defZone = null;
  if (len > 0)
  {
   TimeZone zone;
   if (!tzid.startsWith("GMT") && (zone =
       TimeZone.getTimeZone(tzid)) != null && tzid.equals(zone.getID()))
   {
    if (tzid.indexOf('/', 0) > 0)
     return zone;
    defZone = zone;
   }
   int pos = 0;
   char ch;
   do
   {
    ch = tzid.charAt(pos);
   } while (((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') ||
            (ch == '-' && pos + 1 < len && tzid.charAt(pos + 1) > '9')) &&
            ++pos < len);
   if ((pos >= 3 && pos <= 5) || (pos == len && pos >= 2))
   {
    String stdName = tzid.substring(0, pos);
    int rawOffset = -1 >>> 1;
    String dstName = null;
    if (pos < len)
    {
     boolean isneg = ch == '-';
     if (ch == '+' || isneg)
     {
      if (++pos >= len)
       return null;
      ch = tzid.charAt(pos);
     }
     if (ch < '0' || ch > '9')
      return null;
     rawOffset = (ch - '0') * 60;
     int i = pos;
     while (++pos < len)
      if ((ch = tzid.charAt(pos)) < '0' || ch > '9')
       break;
     i = pos - i;
     if (i > 4)
      return null;
     if ((i & 1) == 0)
      rawOffset = (tzid.charAt(pos - i + 1) - '0') * 60 + rawOffset * 10;
     if (ch == ':')
     {
      if (i > 2 || ++pos >= len ||
          (ch = tzid.charAt(pos)) < '0' || ch > '9' ||
          (++pos < len && (ch = tzid.charAt(pos)) >= '0' && ch <= '9' &&
          ++pos < len && (ch = tzid.charAt(pos)) >= '0' && ch <= '9'))
       return null;
      i = 4;
     }
     if (i > 2)
     {
      ch = tzid.charAt(pos - 2);
      rawOffset += tzid.charAt(pos - 1) - '0';
      if (ch != ':')
      {
       if (ch > '5')
        return null;
       rawOffset += (ch - '0') * 10;
      }
     }
     rawOffset = rawOffset * (60 * 1000);
     if (!isneg)
      rawOffset = -rawOffset;
     if (pos < len)
     {
      i = pos;
      do
      {
       ch = tzid.charAt(pos);
      } while (((ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')) &&
               ++pos < len);
      if (i < pos)
       dstName = tzid.substring(i, pos);
     }
    }
    if (dstName == null && stdName.equalsIgnoreCase("GMT"))
     return TimeZone.getTimeZone(toGMTFormat(rawOffset != (-1 >>> 1) ?
             rawOffset : 0));
    String[] table = TZ_ABBRS_MAP;
    int count = table.length;
    for (int i = 0; i < count; i++)
     if (stdName.equalsIgnoreCase(table[i++]) &&
         (zone = TimeZone.getTimeZone(table[i])) != null &&
         zone.getID().equals(table[i]) && (rawOffset == (-1 >>> 1) ||
         (zone.getRawOffset() == rawOffset && (zone.useDaylightTime() ?
         dstName != null && dstName.equalsIgnoreCase(zone.getDisplayName(true,
         TimeZone.SHORT, Locale.US)) : dstName == null))))
      return zone;
    String[][] zoneStrings = new DateFormatSymbols(Locale.US).getZoneStrings();
    if (zoneStrings != null)
    {
     count = zoneStrings.length;
     for (int i = 0; i < count; i++)
     {
      String[] names = zoneStrings[i];
      if (names != null && names.length > 4 &&
          stdName.equalsIgnoreCase(names[2]) && names[0] != null &&
          (dstName == null || dstName.equalsIgnoreCase(names[4])) &&
          (zone = TimeZone.getTimeZone(names[0])) != null &&
          names[0].equals(zone.getID()) && (rawOffset == (-1 >>> 1) ||
          (zone.getRawOffset() == rawOffset &&
          zone.useDaylightTime() == (dstName != null))))
       return zone;
     }
    }
   }
  }
  return defZone;
 }

 private static String getSystemTimeZoneId()
 {
  int rawOffset = getCTimezoneAndDaylight0();
  if (rawOffset != 0)
  {
   boolean useDaylight = (rawOffset & 1) != 0;
   rawOffset = (rawOffset >> 1) * 1000;
   String[] ids = TimeZone.getAvailableIDs(-rawOffset);
   int count = ids.length;
   String tzid;
   for (int i = 0; i < count; i++)
    if ((tzid = ids[i]) != null)
    {
     TimeZone zone = TimeZone.getTimeZone(tzid);
     if (zone != null && tzid.equals(zone.getID()) &&
         zone.useDaylightTime() == useDaylight && !zone.getDisplayName(false,
         TimeZone.SHORT, Locale.US).startsWith("GMT"))
      return zone.getID();
    }
   for (int i = 0; i < count; i++)
    if ((tzid = ids[i]) != null)
    {
     TimeZone zone = TimeZone.getTimeZone(tzid);
     if (zone != null && tzid.equals(zone.getID()) &&
         zone.useDaylightTime() == useDaylight)
      return zone.getID();
    }
  }
  return toGMTFormat(rawOffset);
 }

 private static String toGMTFormat(int rawOffset)
 {
  StringBuilder sb = new StringBuilder();
  rawOffset = rawOffset / (60 * 1000);
  sb.append("GMT");
  if (rawOffset != 0)
  {
   if (rawOffset < 0)
   {
    sb.append('-');
    rawOffset = -rawOffset;
   }
    else sb.append('+');
   int hours = rawOffset / 60;
   if (hours < 10)
    sb.append('0');
   sb.append(String.valueOf(hours));
   sb.append(':');
   int minutes = rawOffset % 60;
   if (minutes < 10)
    sb.append('0');
   sb.append(String.valueOf(minutes));
  }
  return sb.toString();
 }

 private static native int getCTimezoneAndDaylight0();
}
