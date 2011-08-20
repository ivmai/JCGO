/*
 * @(#) $(JCGO)/minihdr/common/limits.h --
 * a part of the minimalist "libc" headers for JCGO.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2008 Ivan Maidanski <ivmai@ivmaisoft.com>
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

#ifndef _LIMITS_H
#define _LIMITS_H

#ifndef CHAR_BIT
#define CHAR_BIT 8
#endif

#ifndef MB_LEN_MAX
#define MB_LEN_MAX 6
#endif

#ifndef PATH_MAX
#define PATH_MAX 1024 /* or 255, or 259, or 4096 */
#endif

#ifndef NAME_MAX
#define NAME_MAX 255 /* or 12, or 31 */
#endif

#ifndef PIPE_BUF
#define PIPE_BUF 512 /* or 4096 */
#endif

#ifndef OPEN_MAX
#define OPEN_MAX 256 /* or 16, or 64, or 1024, or 10240 */
#endif

#endif
