/*
 * @(#) $(JCGO)/include/jcgochke.h --
 * a part of the JCGO runtime subsystem.
 **
 * Project: JCGO (http://www.ivmaisoft.com/jcgo/)
 * Copyright (C) 2001-2012 Ivan Maidanski <ivmai@ivmaisoft.com>
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

#ifdef JCGO_CAST_OBJECT
#undef JCGO_CALL_VFUNC
#undef JCGO_CALL_FINALF
#undef JCGO_ARRAY_LENGTH
#undef JCGO_FIELD_ACCESS
#undef JCGO_ARRAY_NZLACCESS
#undef JCGO_ARRAY_NZZACCESS
#undef JCGO_ARRAY_NZBACCESS
#undef JCGO_ARRAY_NZCACCESS
#undef JCGO_ARRAY_NZSACCESS
#undef JCGO_ARRAY_NZIACCESS
#undef JCGO_ARRAY_NZJACCESS
#undef JCGO_ARRAY_NZFACCESS
#undef JCGO_ARRAY_NZDACCESS
#undef JCGO_ARRAY_LACCESS
#undef JCGO_ARRAY_ZACCESS
#undef JCGO_ARRAY_BACCESS
#undef JCGO_ARRAY_CACCESS
#undef JCGO_ARRAY_SACCESS
#undef JCGO_ARRAY_IACCESS
#undef JCGO_ARRAY_JACCESS
#undef JCGO_ARRAY_FACCESS
#undef JCGO_ARRAY_DACCESS
#undef JCGO_CAST_OBJECT0
#undef JCGO_CAST_OBJECT
#undef JCGO_ARRAY_OBJSET
#undef JCGO_ARRAY_NZOBJSET
#endif

#ifdef JCGO_SFTNULLP
#define JCGO_CALL_VFUNC(obj) JCGO_METHODS_OF(JCGO_EXPECT_TRUE((obj) != jnull) ? (obj) : (jcgo_throwNullPtrExcX(), obj))
#define JCGO_CALL_FINALF(obj) (void)(JCGO_EXPECT_TRUE((obj) != jnull) ? -1 : (jcgo_throwNullPtrExcX(), 0)),
#else
#define JCGO_CALL_VFUNC(obj) JCGO_METHODS_OF(obj)
#ifdef JCGO_NOSEGV
#define JCGO_CALL_FINALF(obj) /* empty */
#else
#ifdef JCGO_CALLFINALF_DEREFER_ONLY
#define JCGO_CALL_FINALF(obj) *(volatile int *)(obj),
#else
#define JCGO_CALL_FINALF(obj) *(volatile int *)&jcgo_trashVar = *(volatile int *)(obj),
#endif
#endif
#endif

#ifdef JCGO_SFTNULLP
#define JCGO_ARRAY_LENGTH(arr) jcgo_arrayLengthX((jObject)(arr))
#define JCGO_FIELD_ACCESS(reftype, obj, field) JCGO_FIELD_NZACCESS((reftype)jcgo_checkNullX((jObject)(obj)), field)
#else
#define JCGO_ARRAY_LENGTH(arr) JCGO_ARRAY_NZLENGTH(arr)
#ifdef JCGO_HWNULLZ
#define JCGO_FIELD_ACCESS(reftype, obj, field) JCGO_FIELD_NZACCESS((reftype)jcgo_checkNullX((jObject)(obj)), field)
#else
#define JCGO_FIELD_ACCESS(reftype, obj, field) JCGO_FIELD_NZACCESS((reftype)(obj), field)
#endif
#endif

#ifdef JCGO_INDEXCHK
#define JCGO_ARRAY_NZLACCESS(arr, index) jcgo_jObjectArrAccessNZX(arr, index)[0]
#define JCGO_ARRAY_NZZACCESS(arr, index) jcgo_jbooleanArrAccessNZX(arr, index)[0]
#define JCGO_ARRAY_NZBACCESS(arr, index) jcgo_jbyteArrAccessNZX(arr, index)[0]
#define JCGO_ARRAY_NZCACCESS(arr, index) jcgo_jcharArrAccessNZX(arr, index)[0]
#define JCGO_ARRAY_NZSACCESS(arr, index) jcgo_jshortArrAccessNZX(arr, index)[0]
#define JCGO_ARRAY_NZIACCESS(arr, index) jcgo_jintArrAccessNZX(arr, index)[0]
#define JCGO_ARRAY_NZJACCESS(arr, index) jcgo_jlongArrAccessNZX(arr, index)[0]
#define JCGO_ARRAY_NZFACCESS(arr, index) jcgo_jfloatArrAccessNZX(arr, index)[0]
#define JCGO_ARRAY_NZDACCESS(arr, index) jcgo_jdoubleArrAccessNZX(arr, index)[0]
#ifdef JCGO_SFTNULLP
#define JCGO_ARRAY_LACCESS(arr, index) jcgo_jObjectArrAccessX(arr, index)[0]
#define JCGO_ARRAY_ZACCESS(arr, index) jcgo_jbooleanArrAccessX(arr, index)[0]
#define JCGO_ARRAY_BACCESS(arr, index) jcgo_jbyteArrAccessX(arr, index)[0]
#define JCGO_ARRAY_CACCESS(arr, index) jcgo_jcharArrAccessX(arr, index)[0]
#define JCGO_ARRAY_SACCESS(arr, index) jcgo_jshortArrAccessX(arr, index)[0]
#define JCGO_ARRAY_IACCESS(arr, index) jcgo_jintArrAccessX(arr, index)[0]
#define JCGO_ARRAY_JACCESS(arr, index) jcgo_jlongArrAccessX(arr, index)[0]
#define JCGO_ARRAY_FACCESS(arr, index) jcgo_jfloatArrAccessX(arr, index)[0]
#define JCGO_ARRAY_DACCESS(arr, index) jcgo_jdoubleArrAccessX(arr, index)[0]
#else
#define JCGO_ARRAY_LACCESS(arr, index) JCGO_ARRAY_NZLACCESS(arr, index)
#define JCGO_ARRAY_ZACCESS(arr, index) JCGO_ARRAY_NZZACCESS(arr, index)
#define JCGO_ARRAY_BACCESS(arr, index) JCGO_ARRAY_NZBACCESS(arr, index)
#define JCGO_ARRAY_CACCESS(arr, index) JCGO_ARRAY_NZCACCESS(arr, index)
#define JCGO_ARRAY_SACCESS(arr, index) JCGO_ARRAY_NZSACCESS(arr, index)
#define JCGO_ARRAY_IACCESS(arr, index) JCGO_ARRAY_NZIACCESS(arr, index)
#define JCGO_ARRAY_JACCESS(arr, index) JCGO_ARRAY_NZJACCESS(arr, index)
#define JCGO_ARRAY_FACCESS(arr, index) JCGO_ARRAY_NZFACCESS(arr, index)
#define JCGO_ARRAY_DACCESS(arr, index) JCGO_ARRAY_NZDACCESS(arr, index)
#endif
#else
#define JCGO_ARRAY_NZLACCESS(arr, index) JCGO_ARR_INTERNALACC(jObject, arr, index)
#define JCGO_ARRAY_NZZACCESS(arr, index) JCGO_ARR_INTERNALACC(jboolean, arr, index)
#define JCGO_ARRAY_NZBACCESS(arr, index) JCGO_ARR_INTERNALACC(jbyte, arr, index)
#define JCGO_ARRAY_NZCACCESS(arr, index) JCGO_ARR_INTERNALACC(jchar, arr, index)
#define JCGO_ARRAY_NZSACCESS(arr, index) JCGO_ARR_INTERNALACC(jshort, arr, index)
#define JCGO_ARRAY_NZIACCESS(arr, index) JCGO_ARR_INTERNALACC(jint, arr, index)
#define JCGO_ARRAY_NZJACCESS(arr, index) JCGO_ARR_INTERNALACC(jlong, arr, index)
#define JCGO_ARRAY_NZFACCESS(arr, index) JCGO_ARR_INTERNALACC(jfloat, arr, index)
#define JCGO_ARRAY_NZDACCESS(arr, index) JCGO_ARR_INTERNALACC(jdouble, arr, index)
#ifdef JCGO_SFTNULLP
#define JCGO_ARRAY_LACCESS(arr, index) JCGO_ARR_INTERNALACC(jObject, (jObjectArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_ZACCESS(arr, index) JCGO_ARR_INTERNALACC(jboolean, (jbooleanArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_BACCESS(arr, index) JCGO_ARR_INTERNALACC(jbyte, (jbyteArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_CACCESS(arr, index) JCGO_ARR_INTERNALACC(jchar, (jcharArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_SACCESS(arr, index) JCGO_ARR_INTERNALACC(jshort, (jshortArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_IACCESS(arr, index) JCGO_ARR_INTERNALACC(jint, (jintArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_JACCESS(arr, index) JCGO_ARR_INTERNALACC(jlong, (jlongArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_FACCESS(arr, index) JCGO_ARR_INTERNALACC(jfloat, (jfloatArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_DACCESS(arr, index) JCGO_ARR_INTERNALACC(jdouble, (jdoubleArr)jcgo_checkNullX((jObject)(arr)), index)
#else
#ifdef JCGO_HWNULLZ
#define JCGO_ARRAY_LACCESS(arr, index) JCGO_ARR_INTERNALACC(jObject, (jObjectArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_ZACCESS(arr, index) JCGO_ARR_INTERNALACC(jboolean, (jbooleanArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_BACCESS(arr, index) JCGO_ARR_INTERNALACC(jbyte, (jbyteArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_CACCESS(arr, index) JCGO_ARR_INTERNALACC(jchar, (jcharArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_SACCESS(arr, index) JCGO_ARR_INTERNALACC(jshort, (jshortArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_IACCESS(arr, index) JCGO_ARR_INTERNALACC(jint, (jintArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_JACCESS(arr, index) JCGO_ARR_INTERNALACC(jlong, (jlongArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_FACCESS(arr, index) JCGO_ARR_INTERNALACC(jfloat, (jfloatArr)jcgo_checkNullX((jObject)(arr)), index)
#define JCGO_ARRAY_DACCESS(arr, index) JCGO_ARR_INTERNALACC(jdouble, (jdoubleArr)jcgo_checkNullX((jObject)(arr)), index)
#else
#define JCGO_ARRAY_LACCESS(arr, index) JCGO_ARRAY_NZLACCESS(arr, index)
#define JCGO_ARRAY_ZACCESS(arr, index) JCGO_ARRAY_NZZACCESS(arr, index)
#define JCGO_ARRAY_BACCESS(arr, index) JCGO_ARRAY_NZBACCESS(arr, index)
#define JCGO_ARRAY_CACCESS(arr, index) JCGO_ARRAY_NZCACCESS(arr, index)
#define JCGO_ARRAY_SACCESS(arr, index) JCGO_ARRAY_NZSACCESS(arr, index)
#define JCGO_ARRAY_IACCESS(arr, index) JCGO_ARRAY_NZIACCESS(arr, index)
#define JCGO_ARRAY_JACCESS(arr, index) JCGO_ARRAY_NZJACCESS(arr, index)
#define JCGO_ARRAY_FACCESS(arr, index) JCGO_ARRAY_NZFACCESS(arr, index)
#define JCGO_ARRAY_DACCESS(arr, index) JCGO_ARRAY_NZDACCESS(arr, index)
#endif
#endif
#endif

#ifdef JCGO_CHKCAST
#define JCGO_CAST_OBJECT0(objId, maxId, obj) jcgo_checkCast0X(objId, maxId, (jObject)(obj))
#define JCGO_CAST_OBJECT(objId, maxId, dims, obj) jcgo_checkCastX(objId, maxId, dims, (jObject)(obj))
#define JCGO_ARRAY_OBJSET(reftype, arr, index, obj) (reftype)jcgo_objArraySetX(arr, index, (jObject)(obj))
#define JCGO_ARRAY_NZOBJSET(reftype, arr, index, obj) JCGO_ARRAY_OBJSET(reftype, arr, index, obj)
#else
#define JCGO_CAST_OBJECT0(objId, maxId, obj) ((jObject)(obj))
#define JCGO_CAST_OBJECT(objId, maxId, dims, obj) ((jObject)(obj))
#define JCGO_ARRAY_OBJSET(reftype, arr, index, obj) (reftype)(JCGO_ARRAY_LACCESS(arr, index) = (jObject)(obj))
#define JCGO_ARRAY_NZOBJSET(reftype, arr, index, obj) (reftype)(JCGO_ARRAY_NZLACCESS(arr, index) = (jObject)(obj))
#endif

#endif
