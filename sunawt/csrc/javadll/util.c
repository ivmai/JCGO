/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)util.c   1.81 00/02/02
 *
 * Copyright 1994-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */

#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include <stddef.h>
#include <stdarg.h>

// #include "util.h"
typedef enum { FALSE, TRUE } bool_t;
#define sysAssert(x)

/*
 * jio_snprintf(): sprintf with buffer limit.
 */

typedef struct InstanceData {
    char *buffer;
    char *end;
} InstanceData;

#define ERROR_RETVAL -1
#undef  SUCCESS
#define SUCCESS 0
#undef  CheckRet
#define CheckRet(x) { if ((x) == ERROR_RETVAL) return ERROR_RETVAL; }

int
jio_vfprintf (FILE *handle, const char *format, va_list args)
{
    /* vfprintf_hook may not have been installed in javah and javap */
    // if (vfprintf_hook)
       // return vfprintf_hook(handle, format, args);
    // else
        return vfprintf(handle, format, args);
}

static int
put_char(InstanceData *This, int c)
{
    if (iscntrl(0xff & c) && c != '\n' && c != '\t') {
        c = '@' + (c & 0x1F);
        if (This->buffer >= This->end) {
            return ERROR_RETVAL;
        }
        *This->buffer++ = '^';
    }
    if (This->buffer >= This->end) {
        return ERROR_RETVAL;
    }
    *This->buffer++ = (char)c;
    return SUCCESS;
}

static int
format_string(InstanceData *This, char *str, int left_justify, int min_width,
              int precision)
{
    int pad_length;
    char *p;

    if (str == 0) {
        return ERROR_RETVAL;
    }

    if ((int)strlen(str) < precision) {
        pad_length = min_width - (int)strlen(str);
    } else {
        pad_length = min_width - precision;
    }
    if (pad_length < 0)
        pad_length = 0;
    if (left_justify) {
        while (pad_length > 0) {
            CheckRet(put_char(This, ' '));
            --pad_length;
        }
    }

    for (p = str; *p != '\0' && --precision >= 0; p++) {
        CheckRet(put_char(This, *p));
    }

    if (!left_justify) {
        while (pad_length > 0) {
            CheckRet(put_char(This, ' '));
            --pad_length;
        }
    }
    return SUCCESS;
}

#define MAX_DIGITS 32

static int
format_number(InstanceData *This, long value, int format_type,
              int left_justify, int min_width, int precision, bool_t zero_pad)
{
    int sign_value = 0;
    unsigned long uvalue;
    char convert[MAX_DIGITS+1];
    int place = 0;
    int pad_length = 0;
    /* static char digits[] */ const char *const digits = "0123456789abcdef";
    int base = 0;
    bool_t caps = FALSE;
    bool_t add_sign = FALSE;

    switch (format_type) {
      case 'o': case 'O':
          base = 8;
          break;
      case 'd': case 'D':
          add_sign = TRUE; /*FALLTHROUGH*/
      case 'u': case 'U':
          base = 10;
          break;
      case 'X':
          caps = TRUE; /*FALLTHROUGH*/
      case 'x':
          base = 16;
          break;
      case 'p':
          caps = TRUE;  /*FALLTHROUGH*/
          base = 16;
          break;
    }
    sysAssert(base > 0 && base <= 16);

    uvalue = value;
    if (add_sign) {
        if (value < 0) {
            sign_value = '-';
            uvalue = -value;
        }
    }

    do {
        convert[place] = digits[uvalue % (unsigned)base];
        if (caps) {
            convert[place] = (char)toupper(convert[place]);
        }
        place++;
        uvalue = (uvalue / (unsigned)base);
        if (place > MAX_DIGITS) {
            return ERROR_RETVAL;
        }
    } while(uvalue);
    convert[place] = 0;

    pad_length = min_width - place;
    if (pad_length < 0) {
        pad_length = 0;
    }
    if (left_justify) {
        if (zero_pad && pad_length > 0) {
            if (sign_value) {
                CheckRet(put_char(This, sign_value));
                --pad_length;
                sign_value = 0;
            }
            while (pad_length > 0) {
                CheckRet(put_char(This, '0'));
                --pad_length;
            }
        } else {
            while (pad_length > 0) {
                CheckRet(put_char(This, ' '));
                --pad_length;
            }
        }
    }
    if (sign_value) {
        CheckRet(put_char(This, sign_value));
    }

    while (place > 0 && --precision >= 0) {
        CheckRet(put_char(This, convert[--place]));
    }

    if (!left_justify) {
        while (pad_length > 0) {
            CheckRet(put_char(This, ' '));
            --pad_length;
        }
    }
    return SUCCESS;
}

int
jio_vsnprintf(char *str, size_t count, const char *fmt, va_list args)
{
    char *strvalue;
    long value;
    InstanceData This;
    bool_t left_justify, zero_pad, long_flag, fPrecision;
    int min_width, precision, ch;

    if (str == NULL) {
        return ERROR_RETVAL;
    }
    str[0] = '\0';

    This.buffer = str;
    This.end = str + count - 1;
    *This.end = '\0'; /* ensure null-termination in case of failure */

    while ((ch = *fmt++) != 0) {
        if (ch == '%') {
            zero_pad = long_flag = fPrecision = FALSE;
            left_justify = TRUE;
            min_width = 0;
            precision = (int)(This.end - This.buffer);
        next_char:
            ch = *fmt++;
            switch (ch) {
              case 0:
                  return ERROR_RETVAL;
              case '-':
                  left_justify = FALSE;
                  goto next_char;
              case '0': /* set zero padding if min_width not set */
                  if (min_width == 0)
                      zero_pad = TRUE;
                  /*FALLTHROUGH*/
              case '1': case '2': case '3':
              case '4': case '5': case '6':
              case '7': case '8': case '9':
                  if (fPrecision == TRUE) {
                      precision = precision * 10 + (ch - '0');
                  } else {
                      min_width = min_width * 10 + (ch - '0');
                  }
                  goto next_char;
              case '.':
                  fPrecision = TRUE;
                  precision = 0;
                  goto next_char;
              case 'l':
                  long_flag = TRUE;
                  goto next_char;
              case 's':
                  strvalue = va_arg(args, char *);
                  CheckRet(format_string(&This, strvalue, left_justify,
                                         min_width, precision));
                  break;
              case 'c':
                  ch = va_arg(args, int);
                  CheckRet(put_char(&This, ch));
                  break;
              case '%':
                  CheckRet(put_char(&This, '%'));
                  break;
              case 'd': case 'D':
              case 'u': case 'U':
              case 'o': case 'O':
              case 'x': case 'X':
                  value = long_flag ? va_arg(args, long) : va_arg(args, int);
                  CheckRet(format_number(&This, value, ch, left_justify,
                                         min_width, precision, zero_pad));
                  break;
              case 'p':
                  value = (long) (ptrdiff_t)va_arg(args, char *);
                  CheckRet(format_number(&This, value, ch, left_justify,
                                         min_width, precision, zero_pad));
                  break;
              default:
                  return ERROR_RETVAL;
            }
        } else {
            CheckRet(put_char(&This, ch));
        }
    }
    *This.buffer = '\0';
    return (int)strlen(str);
}
