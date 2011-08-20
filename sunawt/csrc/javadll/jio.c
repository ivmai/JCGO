/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)jio.c    1.4 00/02/02
 *
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */

#include <stdlib.h>

#include "jni.h"


/* This is a temporary solution until we figure out how to let native
 * libraries use jio_* without linking with the VM.
 */

extern int
jio_vsnprintf(char *str, size_t count, const char *fmt, va_list args);

JNIEXPORT int
jio_snprintf(char *str, size_t count, const char *fmt, ...)
{
    int len;

    va_list args;
    va_start(args, fmt);
    len = jio_vsnprintf(str, count, fmt, args);
    va_end(args);

    return len;
}

extern int
jio_vfprintf(FILE *, const char *fmt, va_list args);

JNIEXPORT int
jio_fprintf(FILE *fp, const char *fmt, ...)
{
    int len;

    va_list args;
    va_start(args, fmt);
    len = jio_vfprintf(fp, fmt, args);
    va_end(args);

    return len;
}
