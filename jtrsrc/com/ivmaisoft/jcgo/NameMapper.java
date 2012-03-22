/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/NameMapper.java --
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
 * This class maps Java names to C valid ones.
 */

final class NameMapper {

    private static final String[] reservedPrefixArr = {
            "CHKALL_", "ERROR_", "GC_", "HAVE_", "JCGO_", "JNI_", "Java_",
            "MAXT_", "NOJAVA_", "OBJT_", "PTHREAD_", "_", "jcgo_" };

    private static final String[] reservedSuffixArr = {
            "_", "_methods", "_s", "_t" };

    private static final String[] specClassSignsMap = {
            Names.JAVA_LANG_CLASS, "c", Names.JAVA_LANG_OBJECT, "o",
            Names.JAVA_LANG_STRING, "s", Names.JAVA_LANG_THROWABLE, "t" };

    private final ObjHashtable shortLowNames = new ObjHashtable();

    private final ObjHashtable usedNames = new ObjHashtable();

    private final ObjHashtable methodCNames = new ObjHashtable();

    private final ObjHashtable classVarCNames = new ObjHashtable();

    private final ObjHashtable localVarCNames = new ObjHashtable();

    NameMapper() {
        tablePutKeys(shortLowNames, Names.specShortLowNameArr);
        tablePutKeys(usedNames, Names.reservCNameArr);
    }

    private static void tablePutKeys(ObjHashtable table, String[] keys) {
        int len = keys.length;
        for (int i = 0; i < len; i++) {
            table.put(keys[i], "");
        }
    }

    String classToSign(String name) {
        String[] signsMap = specClassSignsMap;
        for (int i = signsMap.length - 1; i > 0; i -= 2) {
            if (name.equals(signsMap[i - 1]))
                return signsMap[i];
        }
        return hashToPackedStr(hashString(name), 5);
    }

    private static int hashString(String str) {
        SecHashAlg sha = new SecHashAlg();
        sha.updateUTF(str);
        sha.finishUpdate();
        return sha.engineDigestVal(null);
    }

    private static String hashToPackedStr(int value, int len) {
        char[] chars = new char[len];
        if (len > 0) {
            int pos = len;
            while (--pos > 0) {
                int digit = value;
                value = (value >>> 1) / 18;
                digit -= value * 36;
                chars[pos] = (char) ((digit < 10 ? '0' : 'a' - 10) + digit);
            }
            chars[0] = (char) ((value - ((value >>> 1) / 5) * 10) + '0');
        }
        return new String(chars);
    }

    String cnameToShort(String cname) {
        String basename = cname;
        int pos = cname.length();
        while ((pos = basename.lastIndexOf('_', pos - 1)) >= 0) {
            if (basename.length() - pos > 3
                    && isLetter(basename.charAt(pos + 1))) {
                basename = basename.substring(pos + 1);
                break;
            }
            basename = basename.substring(0, pos) + basename.substring(pos + 1);
        }
        String shortname = shortenName(basename, 8);
        String lowername = toLowerCase(shortname);
        pos = shortname.length();
        String cname2;
        if (pos <= 1
                || ((cname2 = (String) shortLowNames.get(lowername)) != null && !cname2
                        .equals(cname)) || lowername.startsWith("jcgo")) {
            int hash = hashString(cname);
            if (pos >= 6) {
                pos = 6;
            }
            do {
                shortname = shortenName(basename, pos)
                        + ConstValue.toPaddedHex(hash, true).substring(pos);
                lowername = toLowerCase(shortname);
                if (!lowername.startsWith("jcgo")
                        && ((cname2 = (String) shortLowNames.get(lowername)) == null || cname2
                                .equals(cname)))
                    break;
                if (pos > 3 || ++hash == 0) {
                    pos--;
                }
            } while (pos > 0);
        }
        shortLowNames.put(lowername, cname);
        return shortname;
    }

    static boolean isLetter(char ch) {
        return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z');
    }

    private static String toLowerCase(String str) {
        char[] chars = str.toCharArray();
        int i = chars.length;
        boolean replaced = false;
        char ch;
        while (i-- > 0) {
            if ((ch = chars[i]) >= 'A' && ch <= 'Z') {
                chars[i] = (char) (ch + ('a' - 'A'));
                replaced = true;
            }
        }
        return replaced ? new String(chars) : str;
    }

    private static String shortenName(String str, int maxlen) {
        char[] chars = str.toCharArray();
        int len = chars.length;
        if (len > maxlen) {
            int i;
            int j = len;
            for (i = len; i > 1; i--) {
                char ch = chars[i - 1];
                if (ch >= 'A' && ch <= 'Z' && j - i >= len - maxlen)
                    break;
                if (ch != 'a' && ch != 'e' && ch != 'i' && ch != 'o'
                        && ch != 'u' && ch != 'y') {
                    chars[--j] = ch;
                }
            }
            if (j > i && j < len) {
                System.arraycopy(chars, j, chars, i, len - j);
            }
            len -= j - i;
            if (len > maxlen) {
                j = len;
                for (i = len; i > 1; i--) {
                    if (j - i >= len - maxlen)
                        break;
                    char ch = chars[i - 1];
                    if ((ch >= 'A' && ch <= 'Z')
                            || (chars[i - 2] >= 'A' && chars[i - 2] <= 'Z')) {
                        chars[--j] = ch;
                    }
                }
                if (j > i && j < len) {
                    System.arraycopy(chars, j, chars, i, len - j);
                }
                len -= j - i;
                if (len >= maxlen) {
                    len = maxlen;
                }
            }
        }
        if (len > 0) {
            char ch = chars[0];
            if (ch >= 'a' && ch <= 'z') {
                chars[0] = (char) (ch - ('a' - 'A'));
            }
            int i = len;
            while (--i > 0) {
                if ((ch = chars[i]) >= 'a' && ch <= 'z')
                    break;
            }
            if (i == 0)
                while (++i < len) {
                    if ((ch = chars[i]) >= 'A' && ch <= 'Z') {
                        chars[i] = (char) (ch + ('a' - 'A'));
                    }
                }
        }
        return new String(chars, 0, len);
    }

    private static String convertInvalidChars(String id) {
        int len = id.length();
        StringBuffer sb = new StringBuffer(len + 1);
        boolean changed = false;
        sb.append('0');
        for (int pos = 0; pos < len; pos++) {
            char ch = id.charAt(pos);
            if (ch != '_' && (ch < '0' || ch > '9') && (ch < 'A' || ch > 'Z')
                    && (ch < 'a' || ch > 'z')) {
                sb.append(Integer.toHexString(ch));
                changed = true;
            } else {
                sb.append(ch);
            }
        }
        return changed ? sb.toString() : id;
    }

    private String convertId(String id) {
        String name = convertInvalidChars(id.replace('$', '_')
                .replace('.', '_'));
        boolean changed = false;
        if (name.length() > 0) {
            char ch = name.charAt(0);
            if (ch >= '0' && ch <= '9') {
                name = "x" + name;
                changed = true;
            }
        }
        int i = reservedPrefixArr.length;
        while (i-- > 0) {
            if (name.startsWith(reservedPrefixArr[i])) {
                name = "a" + name;
                changed = true;
                break;
            }
        }
        i = reservedSuffixArr.length;
        while (i-- > 0) {
            if (name.endsWith(reservedSuffixArr[i])) {
                changed = true;
                break;
            }
        }
        i = name.length() - 1;
        while ((i = name.lastIndexOf('_', i)) > 0) {
            int j = i;
            do {
                i--;
            } while (name.charAt(i) == '_');
            if (j - i > 1) {
                name = name.substring(0, i + 1) + name.substring(j);
                changed = true;
            }
        }
        if (changed) {
            name += hashToPackedStr(hashString(id), 3);
        }
        return name;
    }

    String classToCName(String name) {
        if (name.indexOf('.', 0) < 0) {
            name = "package." + name;
        }
        String cname = convertId(name);
        String name2 = (String) usedNames.get(cname);
        if (name2 != null && !name2.equals(name)) {
            cname = cname + "_" + hashToPackedStr(hashString(name), 3);
            while ((name2 = (String) usedNames.get(cname)) != null
                    && !name2.equals(name)) {
                cname = cname + "0";
            }
        }
        usedNames.put(cname, name);
        return cname;
    }

    String nameToJniName(String name) {
        int pos = -1;
        while ((pos = name.indexOf('_', pos + 1) + 1) > 0) {
            name = name.substring(0, pos) + "1" + name.substring(pos);
        }
        while ((pos = name.indexOf('$', pos)) >= 0) {
            name = name.substring(0, pos) + "_00024" + name.substring(pos + 1);
            pos += 5;
        }
        return name.replace('.', '_');
    }

    String nameToProxyNamePart(String name) {
        int pos = -1;
        while ((pos = name.indexOf('$', pos + 1) + 1) > 0) {
            name = name.substring(0, pos) + "$" + name.substring(pos);
        }
        pos = -1;
        while ((pos = name.indexOf('.', pos + 1) + 1) > 0) {
            name = name.substring(0, pos - 1) + "$0" + name.substring(pos);
        }
        return "$00" + name;
    }

    ObjQueue decodeProxyClassName(String className) {
        className = className.substring(className.lastIndexOf('.') + 1);
        if (!className.startsWith("$Proxy$00"))
            return className.equals("$Proxy") ? new ObjQueue() : null;
        ObjQueue ifaceNames = new ObjQueue();
        className = className.substring(9);
        do {
            int next = className.indexOf("$00");
            String name = next >= 0 ? className.substring(0, next) : className;
            if (name.length() == 0)
                return null;
            int pos = -1;
            while ((pos = name.indexOf("$0", pos + 1)) >= 0) {
                name = name.substring(0, pos) + "." + name.substring(pos + 2);
            }
            pos = -1;
            while ((pos = name.indexOf("$$", pos + 1)) >= 0) {
                name = name.substring(0, pos) + name.substring(pos + 1);
            }
            ifaceNames.addLast(name);
            if (next < 0)
                break;
            className = className.substring(next + 3);
        } while (true);
        return ifaceNames;
    }

    String methodToCSign(String id, String jsign, String parmCSign,
            int hiddenCount, String classCName) {
        if (hiddenCount > 0) {
            id = id + "$0" + Integer.toString(hiddenCount + 1);
        }
        String csign = convertId(id) + parmCSign;
        do {
            String fullname = classCName != null ? classCName + "__" + csign
                    : csign;
            String jsign2 = (String) methodCNames.get(fullname);
            if (jsign2 == null) {
                methodCNames.put(fullname, jsign);
                break;
            }
            if (jsign2.equals(jsign))
                break;
            csign = csign + "0";
        } while (true);
        return csign;
    }

    String classVarToOutputName(String classCName, String originalName) {
        String outputName = convertId(originalName.endsWith("_")
                && originalName.length() > 1 ? originalName.substring(0,
                originalName.length() - 1) : originalName);
        do {
            String fullname = classCName + "__" + outputName;
            String name2 = (String) classVarCNames.get(fullname);
            if (name2 == null) {
                if (usedNames.get(outputName) == null) {
                    classVarCNames.put(fullname, originalName);
                    break;
                }
            } else if (name2.equals(originalName))
                break;
            outputName += "0";
        } while (true);
        return outputName;
    }

    String localVarToOutputName(String classCName, String originalName) {
        String outputName = convertId(originalName.startsWith("this_")
                || originalName.startsWith("val_") ? "_" + originalName
                : originalName);
        classCName = classCName + ".";
        String cname;
        String name2;
        if (usedNames.get(outputName) != null
                || ((name2 = (String) localVarCNames.get(cname = classCName
                        + outputName)) != null && !name2.equals(originalName))) {
            outputName = outputName + "_"
                    + hashToPackedStr(hashString(originalName), 3);
            while (usedNames.get(outputName) != null
                    || ((name2 = (String) localVarCNames.get(cname = classCName
                            + outputName)) != null && !name2
                            .equals(originalName))) {
                outputName += "0";
            }
        }
        if (name2 == null) {
            localVarCNames.put(cname, originalName);
        }
        return outputName;
    }

    String fieldToOutputName(ClassDefinition aclass, String originalName,
            int hiddenCount) {
        String outputName = convertId((originalName.startsWith("this_")
                || originalName.startsWith("val_") ? "_" : "")
                + (originalName.endsWith("_") && originalName.length() > 1 ? originalName
                        .substring(0, originalName.length() - 1) : originalName)
                + (hiddenCount > 0 ? "$0" + Integer.toString(hiddenCount + 1)
                        : ""));
        if (usedNames.get(outputName) != null
                || !aclass.addFieldOutputName(outputName)) {
            outputName = outputName + "_"
                    + hashToPackedStr(hashString(originalName), 3);
            while (usedNames.get(outputName) != null
                    || !aclass.addFieldOutputName(outputName)) {
                outputName += "0";
            }
        }
        return outputName;
    }
}
