/*
 * @(#) $(JCGO)/jtrsrc/com/ivmaisoft/jcgo/Names.java --
 * a part of JCGO translator.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2013 Ivan Maidanski <ivmai@mail.ru>
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
 * This class contains Java library hard-coded names and C reserved names.
 */

final class Names {

    private static final String COM_0 = "com.".toString();

    private static final String COM_IVMAISOFT_0 = COM_0 + "ivmaisoft.";

    private static final String COM_IVMAISOFT_JPROPJAV_0 = COM_IVMAISOFT_0
            + "jpropjav.";

    static final String COM_IVMAISOFT_JPROPJAV_STRLISTRESOURCEBUNDLE = COM_IVMAISOFT_JPROPJAV_0
            + "StrListResourceBundle";

    static final String GNU_0 = "gnu.".toString();

    private static final String GNU_CLASSPATH_0 = GNU_0 + "classpath.";

    static final String GNU_CLASSPATH_SERVICEFACTORY_SERVICEITERATOR = GNU_CLASSPATH_0
            + "ServiceFactory$ServiceIterator";
    static final String GNU_CLASSPATH_SERVICEPROVIDERLOADINGACTION = GNU_CLASSPATH_0
            + "ServiceProviderLoadingAction";
    private static final String GNU_CLASSPATH_VMSTACKWALKER = GNU_CLASSPATH_0
            + "VMStackWalker";

    private static final String GNU_CLASSPATH_JDWP_0 = GNU_CLASSPATH_0
            + "jdwp.";

    private static final String GNU_JAVA_0 = GNU_0 + "java.";

    private static final String GNU_JAVA_LANG_0 = GNU_JAVA_0 + "lang.";

    static final String GNU_JAVA_LANG_CHARDATA = GNU_JAVA_LANG_0 + "CharData";

    static final String JAVA_0 = "java.".toString();

    private static final String JAVA_IO_0 = JAVA_0 + "io.";

    static final String JAVA_IO_EXTERNALIZABLE = JAVA_IO_0 + "Externalizable";
    static final String JAVA_IO_OBJECTINPUTSTREAM = JAVA_IO_0
            + "ObjectInputStream";
    static final String JAVA_IO_OBJECTOUTPUTSTREAM = JAVA_IO_0
            + "ObjectOutputStream";
    static final String JAVA_IO_OBJECTSTREAMCLASS = JAVA_IO_0
            + "ObjectStreamClass";
    private static final String JAVA_IO_PRINTSTREAM = JAVA_IO_0 + "PrintStream";
    static final String JAVA_IO_SERIALIZABLE = JAVA_IO_0 + "Serializable";
    static final String JAVA_IO_VMFILE = JAVA_IO_0 + "VMFile";

    static final String JAVA_LANG_0 = JAVA_0 + "lang.";

    static final String JAVA_LANG_ARRAYINDEXOUTOFBOUNDSEXCEPTION = JAVA_LANG_0
            + "ArrayIndexOutOfBoundsException";
    static final String JAVA_LANG_ARRAYSTOREEXCEPTION = JAVA_LANG_0
            + "ArrayStoreException";
    static final String JAVA_LANG_ASSERTIONERROR = JAVA_LANG_0
            + "AssertionError";
    static final String JAVA_LANG_BOOLEAN = JAVA_LANG_0 + "Boolean";
    static final String JAVA_LANG_BYTE = JAVA_LANG_0 + "Byte";
    static final String JAVA_LANG_CHARACTER = JAVA_LANG_0 + "Character";
    static final String JAVA_LANG_CLASS = JAVA_LANG_0 + "Class";
    static final String JAVA_LANG_CLASSCASTEXCEPTION = JAVA_LANG_0
            + "ClassCastException";
    private static final String JAVA_LANG_CLASSLOADER = JAVA_LANG_0
            + "ClassLoader";
    static final String JAVA_LANG_CLASSLOADER_STATICDATA = JAVA_LANG_0
            + "ClassLoader$StaticData";
    static final String JAVA_LANG_CLONEABLE = JAVA_LANG_0 + "Cloneable";
    static final String JAVA_LANG_DOUBLE = JAVA_LANG_0 + "Double";
    static final String JAVA_LANG_ENUM = JAVA_LANG_0 + "Enum";
    static final String JAVA_LANG_ERROR = JAVA_LANG_0 + "Error";
    static final String JAVA_LANG_EXCEPTION = JAVA_LANG_0 + "Exception";
    static final String JAVA_LANG_FLOAT = JAVA_LANG_0 + "Float";
    static final String JAVA_LANG_INDEXOUTOFBOUNDSEXCEPTION = JAVA_LANG_0
            + "IndexOutOfBoundsException";
    static final String JAVA_LANG_INTEGER = JAVA_LANG_0 + "Integer";
    static final String JAVA_LANG_LONG = JAVA_LANG_0 + "Long";
    static final String JAVA_LANG_NOSUCHFIELDERROR = JAVA_LANG_0
            + "NoSuchFieldError";
    static final String JAVA_LANG_NULLPOINTEREXCEPTION = JAVA_LANG_0
            + "NullPointerException";
    static final String JAVA_LANG_OBJECT = JAVA_LANG_0 + "Object";
    static final String JAVA_LANG_RUNNABLE = JAVA_LANG_0 + "Runnable";
    static final String JAVA_LANG_RUNTIMEEXCEPTION = JAVA_LANG_0
            + "RuntimeException";
    static final String JAVA_LANG_SHORT = JAVA_LANG_0 + "Short";
    static final String JAVA_LANG_STRING = JAVA_LANG_0 + "String";
    static final String JAVA_LANG_STRINGBUILDER = JAVA_LANG_0 + "StringBuilder";
    static final String JAVA_LANG_STRINGINDEXOUTOFBOUNDSEXCEPTION = JAVA_LANG_0
            + "StringIndexOutOfBoundsException";
    static final String JAVA_LANG_SYSTEM = JAVA_LANG_0 + "System";
    static final String JAVA_LANG_THREAD = JAVA_LANG_0 + "Thread";
    private static final String JAVA_LANG_THREADGROUP = JAVA_LANG_0
            + "ThreadGroup";
    static final String JAVA_LANG_THROWABLE = JAVA_LANG_0 + "Throwable";
    static final String JAVA_LANG_VMCLASS = JAVA_LANG_0 + "VMClass";
    static final String JAVA_LANG_VMCLASSLOADER = JAVA_LANG_0 + "VMClassLoader";
    static final String JAVA_LANG_VMMATH = JAVA_LANG_0 + "VMMath";
    static final String JAVA_LANG_VMOBJECT = JAVA_LANG_0 + "VMObject";
    private static final String JAVA_LANG_VMPROCESS = JAVA_LANG_0 + "VMProcess";
    static final String JAVA_LANG_VMRUNTIME = JAVA_LANG_0 + "VMRuntime";
    static final String JAVA_LANG_VMSTRING = JAVA_LANG_0 + "VMString";
    static final String JAVA_LANG_VMSYSTEM = JAVA_LANG_0 + "VMSystem";
    static final String JAVA_LANG_VMTHREAD = JAVA_LANG_0 + "VMThread";
    static final String JAVA_LANG_VMTHREAD_EXITMAIN = JAVA_LANG_VMTHREAD
            + "$ExitMain";
    static final String JAVA_LANG_VMTHROWABLE = JAVA_LANG_0 + "VMThrowable";

    private static final String JAVA_LANG_MANAGEMENT_0 = JAVA_LANG_0
            + "management.";

    static final String JAVA_LANG_REF_0 = JAVA_LANG_0 + "ref.";

    static final String JAVA_LANG_REFLECT_0 = JAVA_LANG_0 + "reflect.";

    static final String JAVA_LANG_REFLECT_CONSTRUCTOR = JAVA_LANG_REFLECT_0
            + "Constructor";
    static final String JAVA_LANG_REFLECT_FIELD = JAVA_LANG_REFLECT_0 + "Field";
    static final String JAVA_LANG_REFLECT_INVOCATIONHANDLER = JAVA_LANG_REFLECT_0
            + "InvocationHandler";
    static final String JAVA_LANG_REFLECT_METHOD = JAVA_LANG_REFLECT_0
            + "Method";
    static final String JAVA_LANG_REFLECT_PROXY = JAVA_LANG_REFLECT_0 + "Proxy";
    static final String JAVA_LANG_REFLECT_VMCONSTRUCTOR = JAVA_LANG_REFLECT_0
            + "VMConstructor";
    static final String JAVA_LANG_REFLECT_VMFIELD = JAVA_LANG_REFLECT_0
            + "VMField";
    static final String JAVA_LANG_REFLECT_VMMETHOD = JAVA_LANG_REFLECT_0
            + "VMMethod";
    static final String JAVA_LANG_REFLECT_VMPROXY = JAVA_LANG_REFLECT_0
            + "VMProxy";

    private static final String JAVA_NET_0 = JAVA_0 + "net.";

    private static final String JAVA_NIO_0 = JAVA_0 + "nio.";

    static final String JAVA_NIO_VMDIRECTBYTEBUFFER = JAVA_NIO_0
            + "VMDirectByteBuffer";

    private static final String JAVA_UTIL_0 = JAVA_0 + "util.";

    static final String JAVA_UTIL_LISTRESOURCEBUNDLE = JAVA_UTIL_0
            + "ListResourceBundle";

    private static final String JAVA_UTIL_LOGGING_0 = JAVA_UTIL_0 + "logging.";

    static final String JAVA_UTIL_LOGGING_LOGMANAGER = JAVA_UTIL_LOGGING_0
            + "LogManager";

    static final String JAVAX_0 = "javax.".toString();

    private static final String JAVAX_SWING_0 = JAVAX_0 + "swing.";

    static final String JAVAX_SWING_UIDEFAULTS_PROXYLAZYVALUE = JAVAX_SWING_0
            + "UIDefaults$ProxyLazyValue";

    private static final String JAVAX_SWING_PLAF_0 = JAVAX_SWING_0 + "plaf.";

    static final String JAVAX_SWING_PLAF_COLORUIRESOURCE = JAVAX_SWING_PLAF_0
            + "ColorUIResource";

    private static final String SUN_0 = "sun.".toString();

    private static final String SUN_MISC_0 = SUN_0 + "misc.";

    private static final String SUN_MISC_UNSAFE = SUN_MISC_0 + "Unsafe";

    static final String[] specVmClasses = {
            GNU_CLASSPATH_VMSTACKWALKER,
            GNU_CLASSPATH_JDWP_0 + "VMFrame",
            GNU_CLASSPATH_JDWP_0 + "VMMethod",
            GNU_CLASSPATH_JDWP_0 + "VMVirtualMachine",
            GNU_0 + JAVA_LANG_0 + "VMInstrumentationImpl",
            GNU_0 + JAVA_LANG_MANAGEMENT_0 + "VMClassLoadingMXBeanImpl",
            GNU_0 + JAVA_LANG_MANAGEMENT_0 + "VMCompilationMXBeanImpl",
            GNU_0 + JAVA_LANG_MANAGEMENT_0 + "VMGarbageCollectorMXBeanImpl",
            GNU_0 + JAVA_LANG_MANAGEMENT_0 + "VMMemoryMXBeanImpl",
            GNU_0 + JAVA_LANG_MANAGEMENT_0 + "VMMemoryManagerMXBeanImpl",
            GNU_0 + JAVA_LANG_MANAGEMENT_0 + "VMMemoryPoolMXBeanImpl",
            GNU_0 + JAVA_LANG_MANAGEMENT_0 + "VMOperatingSystemMXBeanImpl",
            GNU_0 + JAVA_LANG_MANAGEMENT_0 + "VMRuntimeMXBeanImpl",
            GNU_0 + JAVA_LANG_MANAGEMENT_0 + "VMThreadMXBeanImpl",
            GNU_0 + JAVA_NET_0 + "VMPlainSocketImpl",
            JAVA_IO_EXTERNALIZABLE,
            JAVA_IO_OBJECTINPUTSTREAM,
            JAVA_IO_OBJECTOUTPUTSTREAM,
            JAVA_IO_OBJECTSTREAMCLASS,
            JAVA_IO_PRINTSTREAM,
            JAVA_IO_SERIALIZABLE,
            JAVA_IO_VMFILE,
            JAVA_LANG_ASSERTIONERROR,
            JAVA_LANG_BOOLEAN,
            JAVA_LANG_BYTE,
            JAVA_LANG_CHARACTER,
            JAVA_LANG_CLASSLOADER,
            JAVA_LANG_CLASSLOADER_STATICDATA,
            JAVA_LANG_CLONEABLE,
            JAVA_LANG_DOUBLE,
            JAVA_LANG_ENUM,
            JAVA_LANG_FLOAT,
            JAVA_LANG_INTEGER,
            JAVA_LANG_LONG,
            JAVA_LANG_RUNNABLE,
            JAVA_LANG_SHORT,
            JAVA_LANG_0 + "StringBuffer",
            JAVA_LANG_STRINGBUILDER,
            JAVA_LANG_SYSTEM,
            JAVA_LANG_THREAD,
            JAVA_LANG_THREADGROUP,
            JAVA_LANG_VMCLASS,
            JAVA_LANG_VMCLASSLOADER,
            JAVA_LANG_VMCLASSLOADER + "$ClassParser",
            JAVA_LANG_0 + "VMCompiler",
            JAVA_LANG_0 + "VMDouble",
            JAVA_LANG_0 + "VMFloat",
            JAVA_LANG_VMMATH,
            JAVA_LANG_VMOBJECT,
            JAVA_LANG_VMPROCESS,
            JAVA_LANG_VMRUNTIME,
            JAVA_LANG_VMRUNTIME + "$TermHandler",
            JAVA_LANG_VMSTRING,
            JAVA_LANG_VMSYSTEM,
            JAVA_LANG_VMTHREAD,
            JAVA_LANG_VMTHREAD_EXITMAIN,
            JAVA_LANG_VMTHROWABLE,
            JAVA_LANG_MANAGEMENT_0 + "VMManagementFactory",
            JAVA_LANG_REF_0 + "ReferenceQueue",
            JAVA_LANG_REF_0 + "SoftReference",
            JAVA_LANG_REF_0 + "VMReference",
            JAVA_LANG_REFLECT_CONSTRUCTOR,
            JAVA_LANG_REFLECT_FIELD,
            JAVA_LANG_REFLECT_INVOCATIONHANDLER,
            JAVA_LANG_REFLECT_METHOD,
            JAVA_LANG_REFLECT_PROXY,
            JAVA_LANG_REFLECT_0 + "VMArray",
            JAVA_LANG_REFLECT_VMCONSTRUCTOR,
            JAVA_LANG_REFLECT_VMFIELD,
            JAVA_LANG_REFLECT_VMMETHOD,
            JAVA_LANG_REFLECT_VMPROXY,
            JAVA_NET_0 + "VMInetAddress",
            JAVA_NIO_VMDIRECTBYTEBUFFER,
            JAVA_UTIL_0 + "VMTimeZone",
            SUN_MISC_UNSAFE
    };

    static final String[] specVmExceptions = {
            JAVA_LANG_0 + "ArithmeticException",
            JAVA_LANG_ARRAYINDEXOUTOFBOUNDSEXCEPTION,
            JAVA_LANG_ARRAYSTOREEXCEPTION,
            JAVA_LANG_CLASSCASTEXCEPTION,
            JAVA_LANG_ERROR,
            JAVA_LANG_EXCEPTION,
            JAVA_LANG_0 + "ExceptionInInitializerError",
            JAVA_LANG_0 + "IncompatibleClassChangeError",
            JAVA_LANG_INDEXOUTOFBOUNDSEXCEPTION,
            JAVA_LANG_0 + "InstantiationException",
            JAVA_LANG_0 + "LinkageError",
            JAVA_LANG_0 + "NegativeArraySizeException",
            JAVA_LANG_0 + "NoClassDefFoundError",
            JAVA_LANG_NOSUCHFIELDERROR,
            JAVA_LANG_0 + "NoSuchMethodError",
            JAVA_LANG_NULLPOINTEREXCEPTION,
            JAVA_LANG_0 + "OutOfMemoryError",
            JAVA_LANG_RUNTIMEEXCEPTION,
            JAVA_LANG_STRINGINDEXOUTOFBOUNDSEXCEPTION,
            JAVA_LANG_THROWABLE,
            JAVA_LANG_0 + "UnsatisfiedLinkError",
            JAVA_LANG_0 + "VirtualMachineError"
    };

    static final String SIGN_APPEND_STRING = "append("
            + signame(JAVA_LANG_STRING) + ")";

    static final String SIGN_ARRAYCLASSOF0X = "arrayClassOf0X("
            + signame(JAVA_LANG_CLASS) + Type.sig[Type.INT] + ")";

    static final String SIGN_ARRAYCOPY = "arraycopy("
            + signame(JAVA_LANG_OBJECT) + Type.sig[Type.INT]
            + signame(JAVA_LANG_OBJECT) + Type.sig[Type.INT]
            + Type.sig[Type.INT] + ")";

    static final String SIGN_CLONE0 = "clone0(" + signame(JAVA_LANG_OBJECT)
            + ")";

    static final String SIGN_CONCAT0X = "concat0X(" + signame(JAVA_LANG_STRING)
            + signame(JAVA_LANG_STRING) + ")";

    static final String SIGN_DESTROYJAVAVM0X = "destroyJavaVM0X("
            + signame(JAVA_LANG_OBJECT) + Type.sig[Type.INT] + ")";

    static final String SIGN_FINALIZE = "finalize()".toString();

    static final String SIGN_FINALIZEOBJECT0X = "finalizeObject0X("
            + signame(JAVA_LANG_OBJECT) + ")";

    static final String SIGN_FORNAME = "forName(" + signame(JAVA_LANG_STRING)
            + ")";
    static final String SIGN_FORNAME_2 = "forName(" + signame(JAVA_LANG_STRING)
            + Type.sig[Type.BOOLEAN] + signame(JAVA_LANG_CLASSLOADER) + ")";

    static final String SIGN_GETCLASS = "getClass()".toString();

    static final String SIGN_GETCONSTRUCTOR = "getConstructor(["
            + signame(JAVA_LANG_CLASS) + ")";

    static final String SIGN_GETCONSTRUCTORS = "getConstructors()".toString();

    static final String SIGN_GETDECLAREDCONSTRUCTOR = "getDeclaredConstructor(["
            + signame(JAVA_LANG_CLASS) + ")";

    static final String SIGN_GETDECLAREDCONSTRUCTORS = "getDeclaredConstructors()"
            .toString();

    static final String SIGN_GETDECLAREDFIELD = "getDeclaredField("
            + signame(JAVA_LANG_STRING) + ")";

    static final String SIGN_GETDECLAREDFIELDS = "getDeclaredFields()"
            .toString();

    static final String SIGN_GETDECLAREDMETHOD = "getDeclaredMethod("
            + signame(JAVA_LANG_STRING) + "[" + signame(JAVA_LANG_CLASS) + ")";

    static final String SIGN_GETDECLAREDMETHODS = "getDeclaredMethods()"
            .toString();

    static final String SIGN_GETFIELD = "getField(" + signame(JAVA_LANG_STRING)
            + ")";

    static final String SIGN_GETFIELDS = "getFields()".toString();

    static final String SIGN_GETINTERFACES = "getInterfaces()".toString();

    static final String SIGN_GETMETHOD = "getMethod("
            + signame(JAVA_LANG_STRING) + "[" + signame(JAVA_LANG_CLASS) + ")";

    static final String SIGN_GETMETHODS = "getMethods()".toString();

    static final String SIGN_GETNAME = "getName()".toString();

    static final String SIGN_GETPROXYCLASS = "getProxyClass("
            + signame(JAVA_LANG_CLASSLOADER) + "[" + signame(JAVA_LANG_CLASS)
            + ")";

    static final String SIGN_INVOKEPROXYHANDLER0X = "invokeProxyHandler0X("
            + signame(JAVA_LANG_OBJECT) + signame(JAVA_LANG_CLASS) + "["
            + Type.sig[Type.INT] + "[" + Type.sig[Type.LONG] + "["
            + Type.sig[Type.FLOAT] + "[" + Type.sig[Type.DOUBLE] + "["
            + signame(JAVA_LANG_OBJECT) + Type.sig[Type.INT]
            + Type.sig[Type.INT] + ")";

    static final String SIGN_MAIN = "main([" + signame(JAVA_LANG_STRING) + ")";

    static final String SIGN_NEXT = "next()".toString();

    static final String SIGN_NEWINSTANCE = "newInstance()".toString();
    static final String SIGN_NEWINSTANCE_CTOR = "newInstance(["
            + signame(JAVA_LANG_OBJECT) + ")";

    static final String SIGN_NEWPROXYINSTANCE = "newProxyInstance("
            + signame(JAVA_LANG_CLASSLOADER) + "[" + signame(JAVA_LANG_CLASS)
            + signame(JAVA_LANG_REFLECT_INVOCATIONHANDLER) + ")";

    static final String SIGN_ORDINAL = "ordinal()".toString();

    static final String SIGN_READOBJECT = "readObject("
            + signame(JAVA_IO_OBJECTINPUTSTREAM) + ")";

    static final String SIGN_READRESOLVE = "readResolve()".toString();

    static final String SIGN_RUN = "run()".toString();

    static final String SIGN_INITSYSTEMERR = "initSystemErr()".toString();

    static final String SIGN_SETSYSTEMOUT = "setSystemOut("
            + signame(JAVA_IO_PRINTSTREAM) + ")";

    static final String SIGN_VALUEOF_ENUM = "valueOf("
            + signame(JAVA_LANG_CLASS) + signame(JAVA_LANG_STRING) + ")";

    static final String SIGN_WRITEOBJECT = "writeObject("
            + signame(JAVA_IO_OBJECTOUTPUTSTREAM) + ")";

    static final String SIGN_WRITEREPLACE = "writeReplace()".toString();

    static final String SIGN_INIT_INT = "<init>(" + Type.sig[Type.INT] + ")";

    static final String SIGN_INIT_INVOCATIONHANDLER = "<init>("
            + signame(JAVA_LANG_REFLECT_INVOCATIONHANDLER) + ")";

    static final String SIGN_INIT_STRING = "<init>("
            + signame(JAVA_LANG_STRING) + ")";

    static final String SIGN_INIT_STRING_OBJECTS = "<init>("
            + signame(JAVA_LANG_STRING) + "[" + signame(JAVA_LANG_OBJECT) + ")";

    static final String SIGN_INIT_STRING_STRING = "<init>("
            + signame(JAVA_LANG_STRING) + signame(JAVA_LANG_STRING) + ")";

    static final String SIGN_INIT_STRING_STRING_OBJECTS = "<init>("
            + signame(JAVA_LANG_STRING) + signame(JAVA_LANG_STRING) + "["
            + signame(JAVA_LANG_OBJECT) + ")";

    static final String SIGN_INIT_STRINGBUILDER = "<init>("
            + signame(JAVA_LANG_STRINGBUILDER) + ")";

    static final String SIGN_INIT_THREADGROUP_2 = "<init>("
            + signame(JAVA_LANG_THREADGROUP) + signame(JAVA_LANG_RUNNABLE)
            + signame(JAVA_LANG_STRING) + Type.sig[Type.LONG] + ")";

    static final String TOSTRING = "toString".toString();

    static final String VALUEOF = "valueOf".toString();

    static final String ASSERTIONSDISABLED = "$assertionsDisabled".toString();

    static final String SERIALPERSISTENTFIELDS = "serialPersistentFields"
            .toString();

    static final String SERIALVERSIONUID = "serialVersionUID".toString();

    static final String SYSTEMCLASSLOADER = "systemClassLoader".toString();

    static final String[] fieldsOrderClass = {
            "vmdata", "name", "superclass", "interfaces", "modifiers" };

    static final String[] fieldsOrderString = {
            "value", "offset", "count", "cachedHashCode" };

    static final String[] specShortLowNameArr = {
            "alloc",
            "arpa",
            "assert",
            "attr",
            "basetsd",
            "bits",
            "bitset",
            "bsedos",
            "buffer",
            "cache",
            "calendar",
            "colordlg",
            "complex",
            "config",
            "conio",
            "control",
            "crtrsxnt",
            "ctype",
            "cygwin",
            "dde",
            "dialog",
            "dir",
            "direct",
            "dirent",
            "dos",
            "emx",
            "env",
            "errno",
            "error",
            "event",
            "excpt",
            "fcntl",
            "features",
            "file",
            "filio",
            "filter",
            "float",
            "fpu_cont",
            "frame",
            "fstream",
            "function",
            "gc",
            "gc_amiga",
            "gc_confi",
            "gc_gcj",
            "gc_local",
            "gc_mark",
            "gc_pthre",
            "gc_versi",
            "generic",
            "handle",
            "ieeefp",
            "ime",
            "in",
            "inet",
            "internal",
            "io",
            "ioctl",
            "iostream",
            "ipc",
            "iterator",
            "javaxfc",
            "jawt",
            "jawt_md",
            "jni",
            "jni_md",
            "limits",
            "link",
            "list",
            "locale",
            "locking",
            "lwp",
            "machine",
            "macros",
            "main",
            "malloc",
            "map",
            "math",
            "md2",
            "md4",
            "md5",
            "mem",
            "memory",
            "menu",
            "meta",
            "mmsystem",
            "monitor",
            "nerrno",
            "netdb",
            "netinet",
            "oid",
            "ole",
            "os2",
            "os2def",
            "page",
            "panel",
            "param",
            "parser",
            "paths",
            "pool",
            "port",
            "print",
            "process",
            "profile",
            "provider",
            "pthread",
            "pwd",
            "queue",
            "ref",
            "resource",
            "sched",
            "search",
            "security",
            "segment",
            "select",
            "semaphor",
            "set",
            "setjmp",
            "sha256",
            "share",
            "signal",
            "slist",
            "socket",
            "stack",
            "stat",
            "stdarg",
            "stddef",
            "stdint",
            "stdio",
            "stdiostr",
            "stdlib",
            "stream",
            "string",
            "strings",
            "synch",
            "sys",
            "syscalls",
            "tchar",
            "tcp",
            "thread",
            "time",
            "timeb",
            "typeinfo",
            "types",
            "unistd",
            "util",
            "utime",
            "utimes",
            "utsname",
            "values",
            "varargs",
            "variant",
            "vector",
            "ver",
            "wait",
            "wchar",
            "winbase",
            "windef",
            "windows",
            "windowsx",
            "winnt",
            "winsock",
            "winsock2",
            "ws2tcpip"
    };

    static final String[] reservCNameArr = {
            "ABS",
            "AF_INET",
            "AF_INET6",
            "AF_UNSPEC",
            "APIENTRY",
            "ARM",
            "ATTRIBGCBSS",
            "ATTRIBGCDATA",
            "ATTRIBMALLOC",
            "ATTRIBNONGC",
            "AbnormalTermination",
            "BASETYPES",
            "BIG_ENDIAN",
            "BOOL",
            "BUILTINEXPECTR",
            "BYTE",
            "BYTE_ORDER",
            "CALLBACK",
            "CDECL",
            "CERROR",
            "CFASTCALL",
            "CHAR_BIT",
            "CHAR_MAX",
            "CHAR_MIN",
            "CHILD_MAX",
            "CLIBDECL",
            "CLIB_API",
            "CLK_TCK",
            "CLOCKS_PER_SEC",
            "CLOCK_ALLOWED",
            "CLOCK_DISABLED",
            "CLOCK_DISALLOWED",
            "CLOCK_ENABLED",
            "CLOCK_PROCESS_CPUTIME",
            "CLOCK_REALTIME",
            "CLOCK_THREAD_CPUTIME",
            "CONST",
            "CONTEXT_CONTROL",
            "CONTEXT_INTEGER",
            "CREATE_ALWAYS",
            "CREATE_NEW",
            "CREATE_SUSPENDED",
            "CREATE_UNICODE_ENVIRONMENT",
            "Catch",
            "Complex",
            "ComplexInf",
            "ComplexNaN",
            "CreateDirectory",
            "CreateEvent",
            "CreateFile",
            "CreateProcess",
            "DBL_MANT_DIG",
            "DBL_MAX_EXP",
            "DBL_MIN",
            "DBL_MIN_EXP",
            "DEBUG",
            "DECIMAL_DIG",
            "DECLSPECNORET",
            "DEFAULT_TYPE",
            "DIR",
            "DMAXPOWTWO",
            "DOS386",
            "DSIGNIF",
            "DWORD",
            "DWORD_PTR",
            "DeleteFile",
            "EACCES",
            "EADDRINUSE",
            "EADDRNOTAVAIL",
            "EAFNOSUPPORT",
            "EAGAIN",
            "EALREADY",
            "EBADF",
            "EBUSY",
            "ECONNABORTED",
            "ECONNREFUSED",
            "ECONNRESET",
            "EDEADLK",
            "EDESTADDRREQ",
            "EEXIST",
            "EHOSTDOWN",
            "EHOSTUNREACH",
            "EH_EXIT_UNWIND",
            "EH_NESTED_CALL",
            "EH_NONCONTINUABLE",
            "EH_SIGFTERM",
            "EH_STACK_INVALID",
            "EH_UNWINDING",
            "EINPROGRESS",
            "EINTR",
            "EINVAL",
            "EISCONN",
            "EMFILE",
            "EMSGSIZE",
            "END_OF_CHAIN",
            "ENETDOWN",
            "ENETRESET",
            "ENETUNREACH",
            "ENFILE",
            "ENOBUFS",
            "ENOENT",
            "ENOEXEC",
            "ENOMEM",
            "ENOPROTOOPT",
            "ENOSPC",
            "ENOTCONN",
            "ENOTDIR",
            "ENOTSOCK",
            "EOF",
            "EOPNOTSUPP",
            "EPERM",
            "EPFNOSUPPORT",
            "EPIPE",
            "EPROTONOSUPPORT",
            "EPROTOTYPE",
            "ERANGE",
            "EROFS",
            "ESHUTDOWN",
            "ESOCKTNOSUPPORT",
            "ESPIPE",
            "ESRCH",
            "ETIMEDOUT",
            "ETOOMANYREFS",
            "EWOULDBLOCK",
            "EXCEPTION_CONTINUABLE",
            "EXCEPTION_CONTINUE_EXECUTION",
            "EXCEPTION_CONTINUE_SEARCH",
            "EXCEPTION_DISPOSITION",
            "EXCEPTION_EXECUTE_HANDLER",
            "EXCEPTION_MAXIMUM_PARAMETERS",
            "EXCEPTION_NONCONTINUABLE",
            "EXIT_FAILURE",
            "EXIT_SUCCESS",
            "EXTERN",
            "EXTRASTATIC",
            "FALSE",
            "FAR",
            "FASTCALL",
            "FA_HIDDEN",
            "FA_LABEL",
            "FD_CLOEXEC",
            "FD_CLR",
            "FD_ISSET",
            "FD_SET",
            "FD_SETSIZE",
            "FD_ZERO",
            "FILENAME_MAX",
            "FILE_ATTRIBUTE_DIRECTORY",
            "FILE_ATTRIBUTE_HIDDEN",
            "FILE_ATTRIBUTE_NORMAL",
            "FILE_ATTRIBUTE_READONLY",
            "FILE_BEGIN",
            "FILE_CURRENT",
            "FILE_END",
            "FILE_FLAG_BACKUP_SEMANTICS",
            "FILE_FLAG_RANDOM_ACCESS",
            "FILE_FLAG_WRITE_THROUGH",
            "FILE_SHARE_READ",
            "FILE_SHARE_WRITE",
            "FIONBIO",
            "FIONREAD",
            "FLT_MANT_DIG",
            "FPINIT",
            "FP_PD",
            "FP_PE",
            "FSIGNIF",
            "F_GETFD",
            "F_OK",
            "F_RDLCK",
            "F_SETFD",
            "F_SETLK",
            "F_SETLK64",
            "F_SETLKW",
            "F_SETLKW64",
            "F_UNLCK",
            "F_WRLCK",
            "FindFirstFile",
            "FindNextFile",
            "GCBSSFIRSTSYM",
            "GCBSSLASTSYM",
            "GCDATAFIRSTSYM",
            "GCDATALASTSYM",
            "GCSTATICDATA",
            "GENERIC_READ",
            "GENERIC_WRITE",
            "GetExceptionCode",
            "GetExceptionInformation",
            "GetFileAttributes",
            "GetLastError",
            "GetTempPath",
            "GetUserHomeFolder",
            "GetUserName",
            "GetVersionEx",
            "H8300",
            "HANDLE",
            "HANDLE_FLAG_INHERIT",
            "HIDE_POINTER",
            "HINSTANCE",
            "HUGE_VAL",
            "HUGE_VALF",
            "HUGE_VALL",
            "IC_AFFINE",
            "IN",
            "IN6ADDR_ANY_INIT",
            "INADDR_ANY",
            "INFINITE",
            "INFINITY",
            "INLINE",
            "INT_MAX",
            "INT_MIN",
            "INVALID_FILE_ATTRIBUTES",
            "INVALID_HANDLE_VALUE",
            "IOCPARM_MASK",
            "IOC_IN",
            "IOC_OUT",
            "IOV_MAX",
            "IPPROTO_IP",
            "IPPROTO_IPV6",
            "IPPROTO_TCP",
            "IPV6_ADD_MEMBERSHIP",
            "IPV6_DROP_MEMBERSHIP",
            "IPV6_JOIN_GROUP",
            "IPV6_LEAVE_GROUP",
            "IPV6_MULTICAST_IF",
            "IP_ADD_MEMBERSHIP",
            "IP_DROP_MEMBERSHIP",
            "IP_MULTICAST_IF",
            "IP_MULTICAST_LOOP",
            "IP_MULTICAST_TTL",
            "IP_TOS",
            "Inf",
            "JAVADEFPROPS",
            "JLONG_C",
            "JNICALL",
            "JNICALL_INVOKE",
            "JNIEXPORT",
            "JNIEXPORT_INVOKE",
            "JNIEnv",
            "JNIEnv_",
            "JNIGlobalRefType",
            "JNIIMPORT",
            "JNIInvalidRefType",
            "JNIInvokeInterface_",
            "JNILocalRefType",
            "JNINativeInterface_",
            "JNINativeMethod",
            "JNIONLOAD",
            "JNIONLOADDECLS",
            "JNIONLOADLIST",
            "JNIONUNLOAD",
            "JNIONUNLOADLIST",
            "JNIWeakGlobalRefType",
            "JNUBIGEXPORT",
            "JavaVM",
            "JavaVM_",
            "JavaVMAttachArgs",
            "JavaVMInitArgs",
            "JavaVMOption",
            "LANGID",
            "LANGIDFROMLCID",
            "LCID",
            "LC_CTYPE",
            "LDBL_MAX_EXP",
            "LITTLE_ENDIAN",
            "LLONG_MAX",
            "LLONG_MIN",
            "LN_MAXDOUBLE",
            "LN_MAXLDOUBLE",
            "LN_MINDOUBLE",
            "LOCKFILE_EXCLUSIVE_LOCK",
            "LOCKFILE_FAIL_IMMEDIATELY",
            "LOGIN_NAME_MAX",
            "LONG",
            "LONGLONG",
            "LONG_LONG_MAX",
            "LONG_LONG_MIN",
            "LONG_MAX",
            "LONG_MIN",
            "LPCSTR",
            "LPCWSTR",
            "LPSTR",
            "LPWSTR",
            "MAINENTRY",
            "MALLOC_ALIGNMENT",
            "MAX",
            "MAXHOSTNAMELEN",
            "MAXPATH",
            "MAXPATHLEN",
            "MAX_PATH",
            "MB_CUR_MAX",
            "MB_LEN_MAX",
            "MCW_EM",
            "MCW_IC",
            "MCW_PC",
            "MCW_RC",
            "MIDL_PASS",
            "MIN",
            "MIPS",
            "MMNOTIMER",
            "MSDOS",
            "MSG_OOB",
            "MSG_PEEK",
            "MUTEX_TYPE_COUNTING_FAST",
            "MUTEX_TYPE_FAST",
            "M_E",
            "M_El",
            "M_LN10",
            "M_LN10l",
            "M_LN2",
            "M_LN2l",
            "M_PI",
            "M_PIl",
            "M_SQRT2",
            "M_SQRT2l",
            "MoveFile",
            "NAME_MAX",
            "NAN",
            "NBBY",
            "NDEBUG",
            "NEAR",
            "NFDBITS",
            "NGROUPS_MAX",
            "NONLS",
            "NSIG",
            "NULL",
            "NZERO",
            "NaN",
            "OPEN_ALWAYS",
            "OPEN_EXISTING",
            "OPEN_MAX",
            "OPTIONAL",
            "OS2",
            "OUT",
            "O_APPEND",
            "O_BINARY",
            "O_CREAT",
            "O_DSYNC",
            "O_EXCL",
            "O_NOINHERIT",
            "O_RDONLY",
            "O_RDWR",
            "O_SYNC",
            "O_TEXT",
            "O_TRUNC",
            "O_WRONLY",
            "PASCAL",
            "PATH_MAX",
            "PC_53",
            "PC_64",
            "PIPE_BUF",
            "PPC",
            "PRIMARYLANGID",
            "P_NOWAIT",
            "P_tmpdir",
            "RC_INVOKED",
            "RC_NEAR",
            "REVEAL_POINTER",
            "RTSIG_MAX",
            "R_OK",
            "RemoveDirectory",
            "SA_NOCLDSTOP",
            "SA_NODEFER",
            "SA_NOMASK",
            "SA_ONESHOT",
            "SA_RESETHAND",
            "SA_RESTART",
            "SA_SIGINFO",
            "SCHAR_MAX",
            "SCHAR_MIN",
            "SCHED_FIFO",
            "SCHED_IA",
            "SCHED_OTHER",
            "SCHED_RR",
            "SCHED_SPORADIC",
            "SCHED_SYS",
            "SEEK_CUR",
            "SEEK_END",
            "SEEK_SET",
            "SH3",
            "SH4",
            "SHRT_MAX",
            "SHRT_MIN",
            "SHUT_RD",
            "SHUT_RDWR",
            "SHUT_WR",
            "SHx",
            "SIGABRT",
            "SIGALRM",
            "SIGBUS",
            "SIGCHLD",
            "SIGCLD",
            "SIGCONT",
            "SIGEMT",
            "SIGEV_NONE",
            "SIGEV_SIGNAL",
            "SIGEV_THREAD",
            "SIGFPE",
            "SIGHUP",
            "SIGILL",
            "SIGINT",
            "SIGIO",
            "SIGIOT",
            "SIGKILL",
            "SIGLOST",
            "SIGPIPE",
            "SIGPOLL",
            "SIGPROF",
            "SIGQUIT",
            "SIGRTMAX",
            "SIGRTMIN",
            "SIGSEGV",
            "SIGSTOP",
            "SIGSYS",
            "SIGTERM",
            "SIGTSTP",
            "SIGTTIN",
            "SIGTTOU",
            "SIGURG",
            "SIGUSR1",
            "SIGUSR2",
            "SIGVTALRM",
            "SIGWINCH",
            "SIGXCPU",
            "SIGXFSZ",
            "SIG_ACK",
            "SIG_BLOCK",
            "SIG_DFL",
            "SIG_HOLD",
            "SIG_IGN",
            "SIG_SETMASK",
            "SIG_UNBLOCK",
            "SI_ASYNCIO",
            "SI_MESGQ",
            "SI_QUEUE",
            "SI_TIMER",
            "SI_USER",
            "SOCK_DGRAM",
            "SOCK_STREAM",
            "SOL_SOCKET",
            "SO_BROADCAST",
            "SO_KEEPALIVE",
            "SO_LINGER",
            "SO_NOSIGPIPE",
            "SO_OOBINLINE",
            "SO_RCVBUF",
            "SO_REUSEADDR",
            "SO_SNDBUF",
            "SSIZE_MAX",
            "STARTF_USESTDHANDLES",
            "STATIC",
            "STATICDATA",
            "STILL_ACTIVE",
            "STRICT",
            "S_IEXEC",
            "S_IFDIR",
            "S_IFLNK",
            "S_IFMT",
            "S_IFREG",
            "S_IREAD",
            "S_IRGRP",
            "S_IROTH",
            "S_IRUSR",
            "S_IWGRP",
            "S_IWOTH",
            "S_IWRITE",
            "S_IWUSR",
            "S_IXGRP",
            "S_IXOTH",
            "S_IXUSR",
            "SetFileAttributes",
            "TCP_NODELAY",
            "TEXT",
            "THREADSINIT",
            "THREADSTACKSZ",
            "THREAD_PRIORITY_HIGHEST",
            "THREAD_PRIORITY_LOWEST",
            "THREAD_PRIORITY_TIME_CRITICAL",
            "TIMER_ABSTIME",
            "TIMER_MAX",
            "TIMER_RELTIME",
            "TIME_ZONE_ID_INVALID",
            "TLS_OUT_OF_INDEXES",
            "TRUE",
            "TRUNCATE_EXISTING",
            "TTY_NAME_MAX",
            "This",
            "Throw",
            "UCHAR_MAX",
            "UINT",
            "UINT_MAX",
            "UINT_PTR",
            "ULLONG_MAX",
            "ULONG",
            "ULONG_LONG_MAX",
            "ULONG_MAX",
            "ULONG_PTR",
            "UNICODE",
            "UNLEN",
            "UNWIND_ALL",
            "USHRT_MAX",
            "WAIT_FAILED",
            "WCHAR",
            "WEOF",
            "WEXITSTATUS",
            "WIFEXITED",
            "WIN32",
            "WINADVAPI",
            "WINAPI",
            "WINBASEAPI",
            "WINMMAPI",
            "WINNT",
            "WINSOCK_API_LINKAGE",
            "WINVER",
            "WNOHANG",
            "WORD",
            "WSAAPI",
            "WSABASEERR",
            "WSADESCRIPTION_LEN",
            "WSAEACCES",
            "WSAEADDRINUSE",
            "WSAEADDRNOTAVAIL",
            "WSAEAFNOSUPPORT",
            "WSAEALREADY",
            "WSAEBADF",
            "WSAECONNABORTED",
            "WSAECONNREFUSED",
            "WSAECONNRESET",
            "WSAEDESTADDRREQ",
            "WSAEFAULT",
            "WSAEHOSTDOWN",
            "WSAEHOSTUNREACH",
            "WSAEINPROGRESS",
            "WSAEINTR",
            "WSAEINVAL",
            "WSAEISCONN",
            "WSAELOOP",
            "WSAEMFILE",
            "WSAEMSGSIZE",
            "WSAENAMETOOLONG",
            "WSAENETDOWN",
            "WSAENETRESET",
            "WSAENETUNREACH",
            "WSAENOBUFS",
            "WSAENOPROTOOPT",
            "WSAENOTCONN",
            "WSAENOTSOCK",
            "WSAEOPNOTSUPP",
            "WSAEPFNOSUPPORT",
            "WSAEPROTONOSUPPORT",
            "WSAEPROTOTYPE",
            "WSAESHUTDOWN",
            "WSAESOCKTNOSUPPORT",
            "WSAETIMEDOUT",
            "WSAETOOMANYREFS",
            "WSAEWOULDBLOCK",
            "WSASYS_STATUS_LEN",
            "WTERMSIG",
            "W_OK",
            "X_OK",
            "abnormal_termination",
            "alloca",
            "and",
            "and_eq",
            "array",
            "asm",
            "assert",
            "attribute",
            "auto",
            "bitand",
            "bitor",
            "bool",
            "calloc",
            "cdecl",
            "compl",
            "complex",
            "const",
            "const_cast",
            "constructor",
            "define",
            "defined",
            "delete",
            "destructor",
            "dynamic_cast",
            "endif",
            "enum",
            "errno",
            "except",
            "exception_code",
            "exception_info",
            "explicit",
            "export",
            "extern",
            "external",
            "far",
            "fastcall",
            "fcloseall",
            "fd_set",
            "fileno",
            "finite",
            "finitef",
            "finitel",
            "flushall",
            "foreach",
            "fprintf",
            "fpsetprec",
            "free",
            "friend",
            "fscanf",
            "fsync",
            "ftime",
            "getdate_err",
            "goto",
            "h_errno",
            "howmany",
            "huge",
            "hypot",
            "hypotl",
            "i386",
            "ifdef",
            "ifndef",
            "import",
            "inline",
            "interrupt",
            "isfinite",
            "isgreater",
            "isgreaterequal",
            "isless",
            "islessequal",
            "islessgreater",
            "isnan",
            "isnanf",
            "isnanl",
            "isunordered",
            "jObject",
            "jObjectArr",
            "jarray",
            "jboolean",
            "jbooleanArr",
            "jbooleanArray",
            "jbyte",
            "jbyteArr",
            "jbyteArray",
            "jchar",
            "jcharArr",
            "jcharArray",
            "jclass",
            "jdouble",
            "jdoubleArr",
            "jdoubleArray",
            "jfalse",
            "jfieldID",
            "jfloat",
            "jfloatArr",
            "jfloatArray",
            "jint",
            "jintArr",
            "jintArray",
            "jlong",
            "jlongArr",
            "jlongArray",
            "jmethodID",
            "jmp_buf",
            "jnull",
            "jobject",
            "jobjectArray",
            "jobjectRefType",
            "jshort",
            "jshortArr",
            "jshortArray",
            "jsize",
            "jstring",
            "jthrowable",
            "jtrue",
            "jvalue",
            "jvtable",
            "jweak",
            "label",
            "leave",
            "lint",
            "longjmp",
            "longjmperror",
            "max",
            "memmove",
            "memset",
            "min",
            "mutable",
            "namespace",
            "near",
            "noreturn",
            "not",
            "not_eq",
            "offsetof",
            "operator",
            "or",
            "or_eq",
            "pJniEnv",
            "pascal",
            "physadr",
            "pragma",
            "printf",
            "pthread_attr_default",
            "pthread_cleanup_pop",
            "pthread_cleanup_push",
            "pthread_condattr_default",
            "pthread_mutexattr_default",
            "quad",
            "register",
            "reinterpret_cast",
            "restrict",
            "s6_addr",
            "s_addr",
            "sa_handler",
            "sa_sigaction",
            "scanf",
            "setjmp",
            "sigaddset",
            "sigdelset",
            "sigemptyset",
            "sigfillset",
            "sigismember",
            "sigjmp_buf",
            "siglongjmp",
            "signbit",
            "signed",
            "sigsetjmp",
            "sizeof",
            "stati64",
            "static_cast",
            "stdcall",
            "stderr",
            "stdin",
            "stdout",
            "string",
            "struct",
            "sun",
            "template",
            "typedef",
            "typeid",
            "typename",
            "typeof",
            "tzname",
            "u_jbyte",
            "u_jint",
            "u_jlong",
            "undef",
            "union",
            "unix",
            "unsigned",
            "using",
            "utime",
            "va_arg",
            "va_copy",
            "va_end",
            "va_list",
            "va_start",
            "virtual",
            "wDIR",
            "warn",
            "wclosedir",
            "wcsdup",
            "wcsicmp",
            "wcslwr",
            "wcsnicmp",
            "wcsnset",
            "wcsrev",
            "wcsset",
            "wcsupr",
            "wdirent",
            "wopendir",
            "wreaddir",
            "xor",
            "xor_eq"
    };

    private Names() {
    }

    static boolean isVMCoreClass(String name) {
        return name.startsWith(GNU_CLASSPATH_JDWP_0)
                || name.equals(GNU_CLASSPATH_VMSTACKWALKER)
                || name.startsWith(GNU_0 + JAVA_LANG_0)
                || (name.startsWith(JAVA_LANG_0) && !name
                        .equals(JAVA_LANG_VMPROCESS))
                || name.equals(JAVA_NIO_VMDIRECTBYTEBUFFER)
                || name.equals(SUN_MISC_UNSAFE);
    }

    private static String signame(String name) {
        return "L" + name.replace('.', '/') + ";";
    }
}
