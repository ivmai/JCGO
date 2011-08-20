/*
 * @(#) $(JCGO)/minihdr/unix/sys/ioctl.h --
 * a part of the minimalist "libc" headers for JCGO (Unix-specific).
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

#ifndef _SYS_IOCTL_H
#define _SYS_IOCTL_H

#ifndef _SYS_TYPES_H
#include <sys/types.h>
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#ifndef _IOCTL_CMD_TYPE
#define _IOCTL_CMD_TYPE int /* or long */
#endif

#ifndef _IOCPARM_MASK
#define _IOCPARM_MASK 0x1fff /* or 0x7f */
#endif

#ifndef _IOR
#define _IOC_OUT (_IOCTL_CMD_TYPE)0x40000000L
#define _IOC_IN (_IOCTL_CMD_TYPE)0x80000000L
#define _IOC(inout, group, num, len) ((inout) | ((_IOCTL_CMD_TYPE)((len) & _IOCPARM_MASK) << 16) | ((_IOCTL_CMD_TYPE)(group) << 8) | (num))
#define _IOR(g, n, t) _IOC(_IOC_OUT, g, n, sizeof(t))
#define _IOW(g, n, t) _IOC(_IOC_IN, g, n, sizeof(t))
#endif

#ifndef _IOCTL_NO_FIOCMDS
#ifndef FIONREAD
#define FIONREAD _IOR('f', 127, int)
#endif
#ifndef FIONBIO
#define FIONBIO _IOW('f', 126, int)
#endif
#endif

_EXPFUNC int _RTLENTRY ioctl(int, _IOCTL_CMD_TYPE, ...);

#ifdef __cplusplus
}
#endif

#endif
