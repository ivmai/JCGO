/*
 * @(#) $(JCGO)/include/jcgobchk.h --
 * a part of the JCGO runtime subsystem.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2009 Ivan Maidanski <ivmai@ivmaisoft.com>
 * All rights reserved.
 */

/**
 * This file is compiled together with the files produced by the JCGO
 * translator (do not include and/or compile this file directly).
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

#ifdef JCGO_VER

#undef JCGO_CALL_VFUNC
#undef JCGO_CALL_FINALF

#define JCGO_CALL_VFUNC(obj) JCGO_CALL_EVFUNC(obj)
#define JCGO_CALL_FINALF(obj) JCGO_CALL_EFINALF(obj)

#undef JCGO_ARRAY_LENGTH
#undef JCGO_FIELD_ACCESS

#define JCGO_ARRAY_LENGTH(arr) jcgo_arrayLength((jObject)(arr))
#define JCGO_FIELD_ACCESS(reftype, obj, field) JCGO_FIELD_NZACCESS((reftype)jcgo_checkNull((jObject)(obj)), field)

#undef JCGO_ARRAY_NZLACCESS
#undef JCGO_ARRAY_NZZACCESS
#undef JCGO_ARRAY_NZBACCESS
#undef JCGO_ARRAY_NZCACCESS
#undef JCGO_ARRAY_NZSACCESS
#undef JCGO_ARRAY_NZIACCESS
#undef JCGO_ARRAY_NZJACCESS
#undef JCGO_ARRAY_NZFACCESS
#undef JCGO_ARRAY_NZDACCESS

#define JCGO_ARRAY_NZLACCESS(arr, index) jcgo_jObjectArrAccessNZ(arr, index)[0]
#define JCGO_ARRAY_NZZACCESS(arr, index) jcgo_jbooleanArrAccessNZ(arr, index)[0]
#define JCGO_ARRAY_NZBACCESS(arr, index) jcgo_jbyteArrAccessNZ(arr, index)[0]
#define JCGO_ARRAY_NZCACCESS(arr, index) jcgo_jcharArrAccessNZ(arr, index)[0]
#define JCGO_ARRAY_NZSACCESS(arr, index) jcgo_jshortArrAccessNZ(arr, index)[0]
#define JCGO_ARRAY_NZIACCESS(arr, index) jcgo_jintArrAccessNZ(arr, index)[0]
#define JCGO_ARRAY_NZJACCESS(arr, index) jcgo_jlongArrAccessNZ(arr, index)[0]
#define JCGO_ARRAY_NZFACCESS(arr, index) jcgo_jfloatArrAccessNZ(arr, index)[0]
#define JCGO_ARRAY_NZDACCESS(arr, index) jcgo_jdoubleArrAccessNZ(arr, index)[0]

#undef JCGO_ARRAY_LACCESS
#undef JCGO_ARRAY_ZACCESS
#undef JCGO_ARRAY_BACCESS
#undef JCGO_ARRAY_CACCESS
#undef JCGO_ARRAY_SACCESS
#undef JCGO_ARRAY_IACCESS
#undef JCGO_ARRAY_JACCESS
#undef JCGO_ARRAY_FACCESS
#undef JCGO_ARRAY_DACCESS

#define JCGO_ARRAY_LACCESS(arr, index) jcgo_jObjectArrAccess(arr, index)[0]
#define JCGO_ARRAY_ZACCESS(arr, index) jcgo_jbooleanArrAccess(arr, index)[0]
#define JCGO_ARRAY_BACCESS(arr, index) jcgo_jbyteArrAccess(arr, index)[0]
#define JCGO_ARRAY_CACCESS(arr, index) jcgo_jcharArrAccess(arr, index)[0]
#define JCGO_ARRAY_SACCESS(arr, index) jcgo_jshortArrAccess(arr, index)[0]
#define JCGO_ARRAY_IACCESS(arr, index) jcgo_jintArrAccess(arr, index)[0]
#define JCGO_ARRAY_JACCESS(arr, index) jcgo_jlongArrAccess(arr, index)[0]
#define JCGO_ARRAY_FACCESS(arr, index) jcgo_jfloatArrAccess(arr, index)[0]
#define JCGO_ARRAY_DACCESS(arr, index) jcgo_jdoubleArrAccess(arr, index)[0]

#undef JCGO_CAST_OBJECT0
#undef JCGO_CAST_OBJECT
#undef JCGO_ARRAY_OBJSET
#undef JCGO_ARRAY_NZOBJSET

#define JCGO_CAST_OBJECT0(objId, maxId, obj) jcgo_checkCast0(objId, maxId, (jObject)(obj))
#define JCGO_CAST_OBJECT(objId, maxId, dims, obj) jcgo_checkCast(objId, maxId, dims, (jObject)(obj))
#define JCGO_ARRAY_OBJSET(reftype, arr, index, obj) (reftype)jcgo_objArraySet(arr, index, (jObject)(obj))
#define JCGO_ARRAY_NZOBJSET(reftype, arr, index, obj) JCGO_ARRAY_OBJSET(reftype, arr, index, obj)

#endif
