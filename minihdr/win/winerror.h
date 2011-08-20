/*
 * @(#) $(JCGO)/minihdr/win/winerror.h --
 * a part of the minimalist "Win32" headers for JCGO.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
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

#ifndef _WINERROR_H
#define _WINERROR_H

#define ERROR_INVALID_FUNCTION 1L
#define ERROR_FILE_NOT_FOUND 2L
#define ERROR_PATH_NOT_FOUND 3L
#define ERROR_TOO_MANY_OPEN_FILES 4L

#define ERROR_INVALID_HANDLE 6L

#define ERROR_NOT_ENOUGH_MEMORY 8L

#define ERROR_BAD_FORMAT 11L

#define ERROR_INVALID_DATA 13L
#define ERROR_OUTOFMEMORY 14L
#define ERROR_INVALID_DRIVE 15L

#define ERROR_WRITE_PROTECT 19L

#define ERROR_NOT_READY 21L

#define ERROR_BAD_LENGTH 24L

#define ERROR_HANDLE_DISK_FULL 39L

#define ERROR_FILE_EXISTS 80L

#define ERROR_OUT_OF_STRUCTURES 84L
#define ERROR_ALREADY_ASSIGNED 85L

#define ERROR_INVALID_PARAMETER 87L

#define ERROR_NO_PROC_SLOTS 89L

#define ERROR_BROKEN_PIPE 109L
#define ERROR_OPEN_FAILED 110L
#define ERROR_BUFFER_OVERFLOW 111L
#define ERROR_DISK_FULL 112L
#define ERROR_NO_MORE_SEARCH_HANDLES 113L
#define ERROR_INVALID_TARGET_HANDLE 114L

#define ERROR_INSUFFICIENT_BUFFER 122L
#define ERROR_INVALID_NAME 123L

#define ERROR_CHILD_NOT_COMPLETE 129L

#define ERROR_NEGATIVE_SEEK 131L
#define ERROR_SEEK_ON_DEVICE 132L

#define ERROR_BUSY_DRIVE 142L

#define ERROR_PATH_BUSY 148L

#define ERROR_BAD_PATHNAME 161L

#define ERROR_BUSY 170L

#define ERROR_ALREADY_EXISTS 183L

#define ERROR_INVALID_STARTING_CODESEG 188L

#define ERROR_INFLOOP_IN_RELOC_CHAIN 202L

#define ERROR_FILENAME_EXCED_RANGE 206L

#define ERROR_TOO_MANY_MODULES 214L

#define ERROR_EXE_MACHINE_TYPE_MISMATCH 216L

#define ERROR_BAD_PIPE 230L
#define ERROR_PIPE_BUSY 231L
#define ERROR_NO_DATA 232L
#define ERROR_PIPE_NOT_CONNECTED 233L

#define ERROR_DIRECTORY 267L

#endif
