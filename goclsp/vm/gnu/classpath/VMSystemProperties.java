/*
 * @(#) $(JCGO)/goclsp/vm/gnu/classpath/VMSystemProperties.java --
 * VM specific methods for VM system properties initialization.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2013 Ivan Maidanski <ivmai@mail.ru>
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

package gnu.classpath;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.VMAccessorJavaIo;

import java.util.Properties;

final class VMSystemProperties
{

 private static final String[] LOCALE_INFO_TABLE =
 {
  "Abkhazian", "ab", "AB",
  "Albanian", "sq", "AL",
  "Arabic", "ar", "EG",
  "Armenian", "hy", "AM",
  "Azerbaijani", "az", "AZ",
  "Bashkir", "ba", "RU",
  "Basque", "eu", "ES",
  "Belarusian", "be", "BY",
  "Breton", "br", "FR",
  "Bulgarian", "bg", "BG",
  "Catalan", "ca", "ES",
  "Chinese", "zh", "CN",
  "Croatian", "hr", "HR",
  "Czech", "cs", "CZ",
  "Danish", "da", "DK",
  "Dutch", "nl", "NL",
  "English", "en", "US",
  "Esperanto", "eo", "",
  "Estonian", "et", "EE",
  "Faroese", "fo", "FO",
  "Fijian", "fj", "FJ",
  "Finnish", "fi", "FI",
  "French", "fr", "FR",
  "Gallegan", "gl", "ES",
  "Georgian", "ka", "GE",
  "German", "de", "DE",
  "Greek", "el", "GR",
  "Hebrew", "he", "IL",
  "Hindi", "hi", "IN",
  "Hungarian", "hu", "HU",
  "Icelandic", "is", "IS",
  "Indonesian", "id", "ID",
  "Interlingua", "ia", "",
  "Interlingue", "ie", "",
  "Irish", "ga", "IE",
  "Italian", "it", "IT",
  "Japanese", "ja", "JP",
  "Kalaallisut", "kl", "GL",
  "Kannada", "kn", "CA",
  "Kashmiri", "ks", "IN",
  "Kazakh", "kk", "KZ",
  "Kirghiz", "ky", "KG",
  "Korean", "ko", "KR",
  "Kurdish", "ku", "TR",
  "Latin", "la", "",
  "Latvian", "lv", "LV",
  "Lithuanian", "lt", "LT",
  "Macedonian", "mk", "MK",
  "Moldavian", "mo", "MD",
  "Mongolian", "mn", "MN",
  "Nepali", "ne", "NP",
  "Norwegian", "no", "NO",
  "Occitan", "oc", "RU",
  "Pashto", "ps", "AF",
  "Polish", "pl", "PL",
  "Portuguese", "pt", "PT",
  "Romanian", "ro", "RO",
  "Russian", "ru", "RU",
  "Samoan", "sm", "WS",
  "Scottish", "gd", "GB",
  "Serbian", "sr", "YU",
  "Serbo", "sh", "YU",
  "Slovak", "sk", "SK",
  "Slovenian", "sl", "SI",
  "Somali", "so", "SO",
  "Spanish", "es", "ES",
  "Sundanese", "su", "SD",
  "Swedish", "sv", "SE",
  "Tajik", "tg", "TJ",
  "Tatar", "tt", "RU",
  "Thai", "th", "TH",
  "Tibetan", "bo", "CN",
  "Turkish", "tr", "TR",
  "Turkmen", "tk", "TM",
  "Ukrainian", "uk", "UA",
  "Uzbek", "uz", "UZ",
  "Vietnamese", "vi", "VN",
  "Welsh", "cy", "GB",
  "Yiddish", "yi", "IL",
 };

 private static volatile String fileEncoding;

 private static volatile String consoleEncoding;

 private VMSystemProperties() {}

 static void preInit(Properties properties)
 {
  preInitOsSpec(properties);
  SystemProperties.setProperties(null);
  preInitLocaleInfo(properties);
  SystemProperties.setProperties(null);
  preInitJavaInfo(properties);
  preInitClassNames(properties);
  preInitDirectories(properties);
 }

 static void postInit(Properties properties)
 {
  postInitSunSpec(properties);
  CustomProperties.initCustomProps(properties);
  postInitEnvCustom(properties);
 }

 static final String getFileEncoding()
 { /* used by VM classes only */
  if (fileEncoding == null)
  {
   String encoding = getenvPlatform("CODEPAGE");
   fileEncoding = mapEncodingValue(encoding == null &&
                   ((encoding = getFileConsoleEncoding0(0)) == null ||
                   encoding.length() == 0) && (encoding =
                   getEncodingFromLocale(getCTypeLocale())).length() == 0 ?
                   mapCodePageToEncoding(getFileConsoleCodePage0(0)) :
                   encoding);
  }
  return fileEncoding;
 }

 static final String getConsoleEncoding(OutputStream out)
 { /* used by VM classes only */
  if (consoleEncoding == null)
  {
   String encoding = getenvPlatform("CONSOLE_CODEPAGE");
   if (encoding != null ? encoding.equals("0") :
       ((encoding = getFileConsoleEncoding0(1)) == null ||
       encoding.length() == 0) && (encoding = mapCodePageToEncoding(
       getFileConsoleCodePage0(1))).length() == 0)
    consoleEncoding = getFileEncoding();
    else
    {
     if (out != null)
     {
      try
      {
       new OutputStreamWriter(out, getFileEncoding());
      }
      catch (UnsupportedEncodingException e) {}
     }
     consoleEncoding = mapEncodingValue(encoding);
    }
  }
  return consoleEncoding;
 }

 private static void preInitOsSpec(Properties properties)
 {
  properties.setProperty("gnu.cpu.endian",
   isCpuUnicodeEndianLittle0(0) != 0 ? "little" : "big");
  properties.setProperty("line.separator",
   VMAccessorJavaIo.getLineSeparatorVMFile());
  properties.setProperty("file.separator",
   VMAccessorJavaIo.getFileSeparatorVMFile());
  properties.setProperty("path.separator",
   VMAccessorJavaIo.getPathSeparatorVMFile());
  String osName = getOsNameVersion0(0);
  properties.setProperty("os.name", osName);
  int archDataModel = getArchDataModel0();
  properties.setProperty("os.arch", getOsArch(osName, archDataModel));
  properties.setProperty("os.version", getOsVersion());
  properties.setProperty("sun.arch.data.model",
   String.valueOf(archDataModel));
 }

 private static void preInitLocaleInfo(Properties properties)
 {
  properties.setProperty("user.name", getUserName());
  String cLocale = getCTypeLocale();
  String lang = getUserLanguage(cLocale);
  String country = getUserCountry(cLocale, lang);
  properties.setProperty("user.language", adjustUserLanguage(lang, country));
  properties.setProperty("user.country", country);
  properties.setProperty("user.region", country);
  properties.setProperty("file.encoding", getFileEncoding());
  properties.setProperty("user.variant", getUserVariant(cLocale));
  properties.setProperty("user.timezone", getUserTimezone());
 }

 private static void preInitJavaInfo(Properties properties)
 {
  properties.setProperty("java.class.version",
   VMVendorInfo.JAVA_CLASS_VERSION);
  properties.setProperty("java.runtime.name", VMVendorInfo.JAVA_RUNTIME_NAME);
  properties.setProperty("java.runtime.version",
   VMVendorInfo.JAVA_RUNTIME_VERSION);
  properties.setProperty("java.specification.name",
   VMVendorInfo.JAVA_SPECIFICATION_NAME);
  properties.setProperty("java.specification.vendor",
   VMVendorInfo.JAVA_SPECIFICATION_VENDOR);
  properties.setProperty("java.specification.version",
   VMVendorInfo.JAVA_SPECIFICATION_VERSION);
  properties.setProperty("java.vendor", VMVendorInfo.JAVA_VENDOR);
  properties.setProperty("java.vendor.url", VMVendorInfo.JAVA_VENDOR_URL);
  properties.setProperty("java.vendor.url.bug",
   VMVendorInfo.JAVA_VENDOR_URL_BUG);
  properties.setProperty("java.version", VMVendorInfo.JAVA_VERSION);
  properties.setProperty("java.vm.info", VMVendorInfo.JAVA_VM_INFO);
  properties.setProperty("java.vm.name", VMVendorInfo.JAVA_VM_NAME);
  properties.setProperty("java.vm.specification.name",
   VMVendorInfo.JAVA_VM_SPECIFICATION_NAME);
  properties.setProperty("java.vm.specification.vendor",
   VMVendorInfo.JAVA_VM_SPECIFICATION_VENDOR);
  properties.setProperty("java.vm.specification.version",
   VMVendorInfo.JAVA_VM_SPECIFICATION_VERSION);
  properties.setProperty("java.vm.vendor", VMVendorInfo.JAVA_VM_VENDOR);
  properties.setProperty("java.vm.vendor.url",
   VMVendorInfo.JAVA_VM_VENDOR_URL);
  properties.setProperty("java.vm.version",
   VMAccessorJavaLang.getJavaVmVersionVMRuntime());
 }

 private static void preInitClassNames(Properties properties)
 {
  properties.setProperty("java.compiler", getJavaCompiler());
 }

 private static void preInitDirectories(Properties properties)
 {
  String fileSep = properties.getProperty("file.separator");
  String pathSep = properties.getProperty("path.separator");
  String userDir = getUserDir();
  properties.setProperty("user.dir", userDir);
  properties.setProperty("user.home", getUserHome(userDir, fileSep));
  properties.setProperty("java.io.tmpdir", getJavaIoTmpdir(fileSep));
  String progPathname = getProgramExePathname();
  String progHome = getProgramHome(progPathname, fileSep);
  String javaHome = getJavaHome(progHome);
  properties.setProperty("java.home", javaHome);
  properties.setProperty("java.boot.class.path",
   getJavaBootClassPath(javaHome, progHome, fileSep, pathSep));
  properties.setProperty("java.class.path",
   getJavaClassPath(progHome, progPathname, userDir, fileSep, pathSep));
  properties.setProperty("gnu.classpath.boot.library.path",
   getGnuClasspathBootLibraryPath(javaHome, fileSep, pathSep));
  properties.setProperty("java.library.path",
   getJavaLibraryPath(javaHome, fileSep, pathSep));
  properties.setProperty("java.ext.dirs", getJavaExtDirs(javaHome, fileSep));
  String classpathHome = getGnuClasspathHome();
  if (classpathHome != null)
   properties.setProperty("gnu.classpath.home", classpathHome);
 }

 private static void postInitSunSpec(Properties properties)
 {
  properties.setProperty("sun.boot.class.path",
   properties.getProperty("java.boot.class.path"));
  properties.setProperty("sun.boot.library.path",
   properties.getProperty("gnu.classpath.boot.library.path"));
  String cpuEndian = properties.getProperty("gnu.cpu.endian");
  properties.setProperty("sun.cpu.endian", cpuEndian);
  properties.setProperty("sun.io.unicode.encoding",
   isUnicodeEncodingLittle(properties.getProperty("os.name"), cpuEndian) ?
   "UnicodeLittle" : "UnicodeBig");
 }

 private static boolean isUnicodeEncodingLittle(String osName,
   String cpuEndian)
 {
  String encoding = getenvPlatform("UNICODE_ENCODING");
  int isLittle;
  return "UnicodeLittle".equals(encoding) || "little".equals(encoding) ||
          (!"UnicodeBig".equals(encoding) && !"big".equals(encoding) &&
          ((isLittle = isCpuUnicodeEndianLittle0(1)) > 0 || (isLittle < 0 &&
          (osName.startsWith("Windows") || (!osName.startsWith("SunOS") &&
          !osName.startsWith("Solaris") && cpuEndian.equals("little"))))));
 }

 private static void postInitEnvCustom(Properties properties)
 {
  String propsLine = VMAccessorJavaLang.getCustomJavaPropsVMRuntime();
  int len = propsLine.length();
  if (len > 0)
  {
   int start = -2;
   String name = null;
   boolean quoted = false;
   int pos = 0;
   do
   {
    char ch = propsLine.charAt(pos);
    if (ch == '"' || (ch == '\\' && pos + 1 < len &&
        (propsLine.charAt(pos + 1) == '\\' ||
        propsLine.charAt(pos + 1) == '"')))
    {
     if (start >= 0)
     {
      propsLine = propsLine.substring(start, pos) +
                   propsLine.substring(pos + 1);
      pos -= start + 1;
      len -= start + 1;
      start = 0;
     }
     if (ch == '"')
     {
      quoted = !quoted;
      continue;
     }
     ch = propsLine.charAt(++pos);
    }
    if (start == -1 && ((ch != ' ' && ch != '\t') || quoted))
     start = pos;
    if (start >= 0)
    {
     if (ch == ' ' || ch == '\t')
     {
      if (!quoted)
      {
       String value = propsLine.substring(start, pos);
       if (name != null)
       {
        if (name.length() > 0)
         properties.setProperty(name, value);
        value = null;
       }
       start = -2;
       name = value;
      }
     }
      else
      {
       if ((ch == '=' || ch == ':') && name == null)
       {
        name = propsLine.substring(start, pos);
        start = -1;
       }
      }
    }
     else
     {
      if (start < -1)
      {
       if (ch != ' ' && ch != '\t')
       {
        if (name != null)
        {
         start = -1;
         if (ch != '=' && ch != ':')
         {
          if (name.length() > 0)
           properties.setProperty(name, "");
          start--;
          name = null;
         }
        }
        if (start == -2)
        {
         start--;
         if (ch == '-' && pos + 1 < len && propsLine.charAt(pos + 1) == 'D')
         {
          start = -1;
          pos++;
         }
        }
       }
        else
        {
         if (start < -2 && !quoted)
          start++;
        }
      }
       else
       {
        if (name != null && pos + 1 < len && propsLine.charAt(pos + 1) == '-')
        {
         if (name.length() > 0)
          properties.setProperty(name, "");
         start--;
         name = null;
        }
       }
     }
   } while (++pos < len);
   if (start >= 0 || name != null)
   {
    String value = "";
    if (start >= 0)
    {
     value = propsLine.substring(start);
     if (name == null)
     {
      name = value;
      value = "";
     }
    }
    if (name.length() > 0)
     properties.setProperty(name, value);
   }
  }
 }

 private static String getOsArch(String osName, int archDataModel)
 {
  String arch = getenvPlatform("PROCESSOR_ARCHITECTURE");
  if (arch == null)
  {
   String hostType = getenvPlatform("HOSTTYPE");
   if ((arch = getenvPlatform("MACHTYPE")) != null)
   {
    if (hostType != null && (arch.startsWith(hostType) ||
        arch.indexOf('-', 0) >= 0))
     arch = hostType;
   }
    else if ((arch = hostType) == null || hostType.equals(osName))
     arch = getOsArch0();
  }
  arch = toLowerCaseLatin(arch, false);
  if (arch.equals("i386"))
   arch = "x86";
   else if (arch.equals("powerpc"))
    arch = "ppc";
  if (archDataModel >= 64)
  {
   if (arch.equals("x86") || arch.equals("x86_64"))
    arch = "amd64";
    else if (arch.equals("ppc"))
     arch = "ppc64";
     else if (arch.equals("sparc"))
      arch = "sparcv9";
  }
  return arch;
 }

 private static String getOsVersion()
 {
  String version = getOsNameVersion0(1);
  if (version == null || version.length() == 0)
  {
   int major = getOsVerMajorMinor0(0);
   if (major >= 0)
   {
    version = String.valueOf(major);
    int minor = getOsVerMajorMinor0(1);
    if (minor >= 0)
     version = version + "." + String.valueOf(minor);
   }
    else version = "unknown";
  }
  return version;
 }

 private static String getCTypeLocale()
 {
  String cLocale = getCTypeLocale0();
  if (cLocale != null)
  {
   int i = cLocale.indexOf('=', 0);
   if (i >= 0)
    cLocale = cLocale.substring(i + 1);
   if ((i = cLocale.indexOf('\r', 0)) >= 0)
    cLocale = cLocale.substring(0, i);
   if ((i = cLocale.indexOf('\n', 0)) >= 0)
    cLocale = cLocale.substring(0, i);
   if (cLocale.length() > 0 && !cLocale.equals("C") &&
       !cLocale.equals("POSIX"))
    return cLocale;
  }
  cLocale = getenvPlatform("LANG");
  return cLocale != null && !cLocale.equals("C") &&
          !cLocale.equals("POSIX") ? cLocale : "";
 }

 private static String getUserLanguage(String cLocale)
 {
  String lang = getenvPlatform("USERLANG");
  if (lang == null || (lang = mapLanguageName(lang)).length() == 0)
  {
   lang = cLocale;
   int i = cLocale.indexOf('_', 0);
   if (i >= 0 || (i = cLocale.indexOf('.', 0)) >= 0 ||
       (i = cLocale.indexOf('@', 0)) >= 0)
    lang = cLocale.substring(0, i);
   if ((lang = mapLanguageName(lang)).length() == 0 || lang.equals("en"))
   {
    String langAbbr = getUserLanguage0();
    if (langAbbr != null &&
        (langAbbr = mapLanguageName(langAbbr)).length() > 0)
     lang = langAbbr;
   }
  }
  return lang;
 }

 private static String mapLanguageName(String lang)
 {
  int len = lang.length();
  String[] table = LOCALE_INFO_TABLE;
  if (len != 2)
  {
   if (len == 3)
   {
    lang = toLowerCaseLatin(lang, false);
    return lang.equals("eng") || lang.equals("usa") ? "en" : lang;
   }
   if (len > 2)
   {
    lang = toLowerCaseLatin(lang, true);
    len = table.length;
    for (int i = 0; i < len; i += 3)
     if (lang.equals(table[i]))
      return table[i + 1];
   }
   return "";
  }
  lang = toLowerCaseLatin(lang, false);
  if (lang.equals("en"))
   return "en";
  if (lang.equals("cz"))
   return "cs";
  if (lang.equals("iw"))
   return "he";
  if (lang.equals("in"))
   return "id";
  if (lang.equals("ji"))
   return "yi";
  len = table.length;
  for (int i = 1; i < len; i += 3)
  {
   String langAbbr = table[i];
   if (lang.equals(langAbbr))
    return langAbbr;
  }
  return lang;
 }

 private static String toLowerCaseLatin(String str, boolean firstUpper)
 {
  char[] chars = str.toCharArray();
  int i = 0;
  int len = chars.length;
  boolean replaced = false;
  char ch;
  if (firstUpper && len != 0)
  {
   if ((ch = chars[0]) >= 'a' && ch <= 'z')
   {
    chars[0] = (char) (ch - ('a' - 'A'));
    replaced = true;
   }
   i = 1;
  }
  while (i < len)
  {
   if ((ch = chars[i]) >= 'A' && ch <= 'Z')
   {
    chars[i] = (char) (ch + ('a' - 'A'));
    replaced = true;
   }
   i++;
  }
  return replaced ? new String(chars) : str;
 }

 private static String getUserCountry(String cLocale, String lang)
 {
  String country = getenvPlatform("USERCTRY");
  if (country == null || (country = mapCountryName(country)).length() == 0)
  {
   int i = cLocale.indexOf('_', 0);
   if (i >= 0)
   {
    int j = cLocale.indexOf('.', i + 1);
    if (j < 0 && (j = cLocale.indexOf('@', i + 1)) < 0)
     j = cLocale.length();
    country = mapCountryName(cLocale.substring(i + 1, j));
   }
   if (country == null || country.length() == 0 ?
       ((country = getUserCountry0()) == null ||
       (country = mapCountryName(country)).length() == 0) &&
       (lang.length() == 0 || lang.equals("en") ||
       (country = mapLangToCountry(lang)).length() == 0) :
       country.equals("US") && ((country = getUserCountry0()) == null ||
       (country = mapCountryName(country)).length() == 0))
    country = "US";
  }
  return country;
 }

 private static String mapCountryName(String country)
 {
  if (country.length() != 2)
   return "";
  country = VMAccessorJavaLang.toUpperCaseLatinVMSystem(country);
  if (country.equals("US"))
   return "US";
  if (country.equals("UK"))
   return "GB";
  String[] table = LOCALE_INFO_TABLE;
  int len = table.length;
  for (int i = 2; i < len; i += 3)
  {
   String countryAbbr = table[i];
   if (country.equals(countryAbbr))
    return countryAbbr;
  }
  return country;
 }

 private static String mapLangToCountry(String lang)
 {
  String[] table = LOCALE_INFO_TABLE;
  int len = table.length;
  for (int i = 1; i < len; i += 3)
   if (lang.equals(table[i]))
    return table[i + 1];
  return "";
 }

 private static String adjustUserLanguage(String lang, String country)
 {
  return lang.length() > 0 || (!country.equals("US") &&
          (lang = mapCountryToLang(country)).length() > 0) ? lang : "en";
 }

 private static String mapCountryToLang(String country)
 {
  String lang = "";
  String[] table = LOCALE_INFO_TABLE;
  int len = table.length;
  for (int i = 2; i < len; i += 3)
   if (country.equals(table[i]))
   {
    if (country.equals(
        VMAccessorJavaLang.toUpperCaseLatinVMSystem(table[i - 1])))
    {
     lang = table[i - 1];
     break;
    }
    if (lang.length() == 0)
     lang = table[i - 1];
   }
  return lang;
 }

 private static String getEncodingFromLocale(String cLocale)
 {
  int i = cLocale.indexOf('.', 0) + 1;
  if (i <= 0)
   return "";
  int j = cLocale.indexOf('@', i);
  String encoding = cLocale.substring(i, j >= 0 ? j : cLocale.length());
  return encoding.startsWith("C-") ? encoding.substring(2) : encoding;
 }

 private static String mapCodePageToEncoding(int codePage)
 {
  return codePage < 10 ? "" : (codePage < 100 ? "Cp0" : "Cp") + codePage;
 }

 private static String mapEncodingValue(String encoding)
 {
  if (encoding.length() == 0 || encoding.equals("ISO8859_1") ||
      encoding.equals("csISOLatin1") || encoding.equals("iso-ir-100") ||
      encoding.equals("l1") || encoding.equals("latin1"))
   return "ISO8859_1";
  if (encoding.equals("ISO_646.irv:1991") || encoding.equals("ascii7") ||
      encoding.equals("csASCII") || encoding.equals("iso-ir-6") ||
      encoding.equals("iso_646.irv:1983") || encoding.equals("us"))
   return "US-ASCII";
  String upperEnc = VMAccessorJavaLang.toUpperCaseLatinVMSystem(encoding);
  if (upperEnc.equals("ISO_8859-1:1987"))
   return "ISO8859_1";
  if (upperEnc.equals("US-ASCII") || upperEnc.equals("ANSI_X3.4-1968") ||
      upperEnc.equals("ANSI_X3.4-1986") || upperEnc.equals("ASCII") ||
      upperEnc.equals("ISO646-US"))
   return "US-ASCII";
  if (upperEnc.equals("UTF-8") || upperEnc.equals("UTF8"))
   return "UTF-8";
  if (upperEnc.equals("X-UTF-16BE") || upperEnc.equals("UTF-16BE") ||
      upperEnc.equals("UTF16BE"))
   return "UTF-16BE";
  if (upperEnc.equals("X-UTF-16LE") || upperEnc.equals("UTF-16LE") ||
      upperEnc.equals("UTF16LE"))
   return "UTF-16LE";
  if (upperEnc.startsWith("CP"))
   upperEnc = upperEnc.substring(2);
   else if (upperEnc.startsWith("IBM") || upperEnc.startsWith("ISO"))
    upperEnc = upperEnc.substring(3);
    else if (upperEnc.startsWith("WINDOWS"))
     upperEnc = upperEnc.substring(7);
  if (upperEnc.startsWith("-") || upperEnc.startsWith("_"))
   upperEnc = upperEnc.substring(1);
  return upperEnc.equals("819") || upperEnc.equals("8859-1") ||
          upperEnc.equals("8859_1") ? "ISO8859_1" :
          upperEnc.equals("367") || upperEnc.equals("646") ||
          upperEnc.equals("20127") ? "US-ASCII" :
          upperEnc.equals("1208") || upperEnc.equals("1209") ||
          upperEnc.equals("5304") || upperEnc.equals("5305") ||
          upperEnc.equals("65001") ? "UTF-8" :
          upperEnc.equals("1201") || upperEnc.equals("5297") ||
          upperEnc.equals("13488") || upperEnc.equals("17584") ? "UTF-16BE" :
          upperEnc.equals("1202") || upperEnc.equals("13490") ||
          upperEnc.equals("17586") ? "UTF-16LE" :
          upperEnc.length() == 5 && upperEnc.startsWith("2859") &&
          (char) (upperEnc.charAt(4) - '1') <= (char) ('9' - '1') ?
          "ISO8859_" + upperEnc.charAt(4) :
          (upperEnc.length() == 3 || upperEnc.length() == 4) &&
          (char) (upperEnc.charAt(0) - '0') <= (char) ('9' - '0') &&
          (char) (upperEnc.charAt(1) - '0') <= (char) ('9' - '0') &&
          (char) (upperEnc.charAt(2) - '0') <= (char) ('9' - '0') ?
          "Cp" + upperEnc : encoding;
 }

 private static String getUserVariant(String cLocale)
 {
  String variant = getUserVariant0();
  if (variant == null || variant.length() == 0)
  {
   int i = cLocale.indexOf('@', 0);
   if (i < 0 || (variant = cLocale.substring(i + 1)).length() == 0)
    return "";
  }
  return mapLocaleVariant(variant);
 }

 private static String mapLocaleVariant(String variant)
 {
  String upperVariant = VMAccessorJavaLang.toUpperCaseLatinVMSystem(variant);
  if (upperVariant.equals("ARAB"))
   return "Arab";
  if (upperVariant.equals("CYRL"))
   return "Cyrl";
  if (upperVariant.equals("EURO"))
   return "EURO";
  if (upperVariant.equals("HANS"))
   return "Hans";
  if (upperVariant.equals("HANT"))
   return "Hant";
  if (upperVariant.equals("LATN"))
   return "Latn";
  if (upperVariant.equals("NY"))
   return "NY";
  if (upperVariant.equals("POSIX"))
   return "POSIX";
  if (upperVariant.equals("REVISED"))
   return "REVISED";
  if (upperVariant.equals("SAAHO"))
   return "SAAHO";
  if (upperVariant.equals("UNIX"))
   return "UNIX";
  return variant;
 }

 private static String getUserTimezone()
 {
  /* dummy */
  return "";
 }

 private static String getJavaCompiler()
 {
  /* dummy */
  return "";
 }

 private static String getUserName()
 {
  String name = getenvPlatform("LOGNAME");
  return name != null || (name = getenvPlatform("USERNAME")) != null ||
          ((name = getUserName0()) != null && name.length() > 0) ||
          (name = getenvPlatform("USER")) != null ? name : "anonymous";
 }

 private static String getUserDir()
 {
  String path = VMAccessorJavaIo.makeAbsPathVMFile(".");
  if (path == null)
   return ".";
  try
  {
   path = VMAccessorJavaIo.canonPathCaseVMFile(path);
  }
  catch (IOException e) {}
  return path;
 }

 private static String getUserHome(String userDir, String fileSep)
 {
  String path = getenvPlatform("HOME");
  if (path != null || ((path = getUserHome0()) != null && path.length() > 0))
   return normPlatformPath(path);
  if ((path = getenvPlatform("HOMEPATH")) != null)
  {
   if (path.length() <= 1 || path.charAt(1) != ':')
   {
    String drive = getenvPlatform("HOMEDRIVE");
    if (drive != null && drive.length() > 1 &&
        drive.charAt(drive.length() - 1) == ':')
     path = drive + path;
   }
   return normPlatformPath(path);
  }
  if ((path = getenvPlatform("USERPROFILE")) != null)
   return normPlatformPath(path);
  if (userDir.equals("."))
   return VMAccessorJavaIo.makeAbsPathVMFile(fileSep);
  while ((path = VMAccessorJavaIo.pathParentOfVMFile(userDir)) != null)
   userDir = path;
  return userDir;
 }

 private static String getJavaIoTmpdir(String fileSep)
 {
  String path = getenvPlatform("TMP");
  if (path == null && (path = getenvPlatform("TMPDIR")) == null &&
      (path = getenvPlatform("TEMP")) == null)
  {
   path = getJavaIoTmpdir0();
   if (path.length() == 0 ||
       !VMAccessorJavaIo.isDirectoryVMFile(path = normPlatformPath(path)))
    return "." + fileSep;
  }
   else path = normPlatformPath(path);
  return path.endsWith(fileSep) ? path : path + fileSep;
 }

 private static String getProgramExePathname()
 {
  String path = VMAccessorJavaLang.getJavaExePathnameVMRuntime();
  if (!path.equals("."))
  {
   if ((path = VMAccessorJavaIo.makeAbsPathVMFile(
       normPlatformPath(path))) == null)
    return ".";
   path = VMAccessorJavaIo.collapsePathDotsVMFile(path);
   try
   {
    path = VMAccessorJavaIo.canonPathCaseVMFile(path);
   }
   catch (IOException e) {}
  }
  return path;
 }

 private static String getProgramHome(String progPathname, String fileSep)
 {
  String path = VMAccessorJavaIo.pathParentOfVMFile(progPathname);
  if (path == null)
   return ".";
  if (fileSep.length() == 1)
  {
   String name = toLowerCaseLatin(path.substring(
                  path.lastIndexOf(fileSep.charAt(0)) + 1), false);
   char ch;
   if (name.startsWith("bin") && (name.length() == 3 ||
       (ch = name.charAt(3)) == '-' || ch == '_'))
   {
    if (path.charAt(0) != '\\')
     path = path + fileSep + "..";
     else if ((path = VMAccessorJavaIo.pathParentOfVMFile(path)) == null)
      path = ".";
   }
  }
  return path;
 }

 private static String getJavaHome(String progHome)
 {
  String path = getenvPlatform("PROG_JAVA_HOME");
  return path != null ? normPlatformPath(path) : progHome;
 }

 private static String getJavaBootClassPath(String javaHome, String progHome,
   String fileSep, String pathSep)
 {
  String javaHomeSep = javaHome.endsWith(fileSep) ? javaHome :
                        javaHome + fileSep;
  String progHomeSep = progHome.endsWith(fileSep) ? progHome :
                        progHome + fileSep;
  return progHomeSep + "lib" + fileSep + "tools.jar" + pathSep +
          javaHomeSep + "lib" + fileSep + "rt.jar" + pathSep +
          javaHomeSep + "share" + fileSep + "classpath" + fileSep +
          "glibj.zip" + pathSep + progHomeSep + "classes";
 }

 private static String getJavaClassPath(String progHome, String progPathname,
   String userDir, String fileSep, String pathSep)
 {
  progHome = VMAccessorJavaIo.collapsePathDotsVMFile(progHome);
  String pathList = getenvPlatform("CLASSPATH");
  if (pathList != null)
   pathList = VMAccessorJavaIo.normPlatformListOfPathsVMFile(pathList);
  String fileName;
  if (!progPathname.equals(".") &&
      (fileName = stripPathAndExeExt(progPathname, fileSep)).length() > 0)
   pathList = (pathList != null ? pathList + pathSep : "") +
               (progHome.equals(".") || (progHome.equals(userDir) &&
               progHome.charAt(0) != '\\') ? "" : progHome.endsWith(fileSep) ?
               progHome : progHome + fileSep) + fileName + ".jar";
   else if (pathList == null)
    pathList = progHome;
  return pathList;
 }

 private static String stripPathAndExeExt(String path, String fileSep)
 {
  path = path.substring(path.lastIndexOf(fileSep.charAt(0)) + 1);
  int pos = path.lastIndexOf('.');
  if (pos < 0)
   return path;
  int i = path.length();
  if (pos == 0)
   return i > 1 ? path : "";
  char ch;
  while (--i > pos)
   if (((ch = path.charAt(i)) < 'A' || ch > 'Z') && (ch < 'a' || ch > 'z'))
    return path;
  while (path.charAt(pos - 1) == '.')
   if (--pos == 0)
    break;
  return path.substring(0, pos);
 }

 private static String getGnuClasspathHome()
 {
  String path = getenvPlatform("CLASSPATH_HOME");
  return path != null ? normPlatformPath(path) : null;
 }

 private static String getGnuClasspathBootLibraryPath(String javaHome,
   String fileSep, String pathSep)
 {
  String javaHomeSep = javaHome.endsWith(fileSep) ? javaHome :
                        javaHome + fileSep;
  String sepDots = fileSep + "..";
  return (javaHome.endsWith(sepDots) && javaHome.length() > sepDots.length() ?
          javaHome.substring(0, javaHome.length() - sepDots.length()) :
          javaHomeSep + "bin") + pathSep +
          javaHomeSep + "lib" + fileSep + "classpath";
 }

 private static String getJavaLibraryPath(String javaHome, String fileSep,
   String pathSep)
 {
  String pathList = getenvPlatform("LD_LIBRARY_PATH");
  if (pathList != null && !pathList.equals("."))
   return VMAccessorJavaIo.normPlatformListOfPathsVMFile(pathList);
  if ((pathList = getenvPlatform("PATH")) != null)
   pathList = VMAccessorJavaIo.normPlatformListOfPathsVMFile(pathList);
   else pathList = ".";
  String binPath;
  if (!javaHome.endsWith(fileSep + "..") ||
      (binPath = VMAccessorJavaIo.pathParentOfVMFile(javaHome)) == null)
   binPath = javaHome;
  String dotPathSep = "." + pathSep;
  return (binPath.equals(".") ? "" : binPath + pathSep) +
          (pathList.equals(".") || pathList.startsWith(dotPathSep) ||
          pathList.endsWith(pathSep + ".") || pathList.indexOf(pathSep +
          dotPathSep) >= 0 ? "" : dotPathSep) + pathList;
 }

 private static String getJavaExtDirs(String javaHome, String fileSep)
 {
  return (javaHome.endsWith(fileSep) ? javaHome : javaHome + fileSep) +
          "lib" + fileSep + "ext";
 }

 private static String getenvPlatform(String name)
 {
  return VMAccessorJavaIo.getenvPlatformVMFile(name);
 }

 private static String normPlatformPath(String path)
 {
  return VMAccessorJavaIo.normPlatformPathVMFile(path);
 }

 private static native int isCpuUnicodeEndianLittle0(int isUnicode);

 private static native int getArchDataModel0();

 private static native String getOsArch0();

 private static native String getOsNameVersion0(int isVersion);

 private static native int getOsVerMajorMinor0(int isMinor);

 private static native String getCTypeLocale0();

 private static native String getUserLanguage0();

 private static native String getUserCountry0();

 private static native String getUserVariant0();

 private static native String getFileConsoleEncoding0(int isConsole);

 private static native int getFileConsoleCodePage0(int isConsole);

 private static native String getUserName0();

 private static native String getUserHome0();

 private static native String getJavaIoTmpdir0();
}
