/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)JdbcOdbcCallableStatement.java   @(#)JdbcOdbcCallableStatement.java      1.49 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

//----------------------------------------------------------------------------
//
// Module:      JdbcOdbcCallableStatement.java
//
// Description: Impementation of the CallableStatement interface class
//
// Product:     JDBCODBC (Java DataBase Connectivity using
//              Open DataBase Connectivity)
//
// Author:      Karl Moss
//
// Date:        March, 1996
//
//----------------------------------------------------------------------------

package sun.jdbc.odbc;

import java.sql.*;
import java.util.Map;
import java.util.Calendar;
import java.math.BigDecimal;

public class JdbcOdbcCallableStatement
        extends         JdbcOdbcPreparedStatement
        implements      java.sql.CallableStatement {

        //====================================================================
        // We need to save registerOutParameter's scale parameters
        // for calls to getObject(BigDecimal);
        // We will use an array of bytes to save space (scale will never be
        // more than 127.
           public byte scalez[]=new byte[200];
        //====================================================================

        //====================================================================
        // Public methods
        //====================================================================

        //--------------------------------------------------------------------
        // Constructor
        // Perform any necessary initialization.
        //--------------------------------------------------------------------

        public JdbcOdbcCallableStatement (
                JdbcOdbcConnectionInterface con)
        {
                super (con);
                lastParameterNull = false;
        }

        //--------------------------------------------------------------------
        // registerOutParameter
        // Before executing a stored procedure call you must explicitly
        // call registerOutParameter to register the java.sql.Type of each
        // out parameter.
        // Note that all parameters are bound to a SQL_C_CHAR data type,
        // so the resulting buffer is a string.  When retrieving the value
        // of the output parameter (with getXXXX), the buffer will be converted
        // to a String first, then the appropriate data type for the
        // getXXXX method.
        //--------------------------------------------------------------------

        public void registerOutParameter (
                int parameterIndex,
                int sqlType)
                throws SQLException
        {
                registerOutParameter (parameterIndex, sqlType, 0);
        }

        // You must also specify the scale for numeric/decimal types:
        public void registerOutParameter (
                int parameterIndex,
                int sqlType,
                int scale)
                throws SQLException
        {
                // Set the sql type for this OUT parameter

                setSqlType (parameterIndex, sqlType);

                // Save the scale in scalez[]
                 if (parameterIndex <= 200)
                    scalez[parameterIndex]=(byte)scale;

                // Indicate that this is an output parameter
                setOutputParameter (parameterIndex, true);

                // First, allocate a buffer to bind the output parameter
                // to.

                int maxLen;

                switch (sqlType) {
                case Types.DATE:
                        maxLen = 10;
                        break;
                case Types.TIME:
                        maxLen = 8;
                        break;
                case Types.TIMESTAMP:
                        maxLen = 15;
                        if (scale > 0) {
                                maxLen += (scale + 1);
                        }
                        break;
                case Types.BIT:
                        maxLen = 3;
                        break;
                case Types.TINYINT:
                        maxLen = 4;
                        break;
                case Types.SMALLINT:
                        maxLen = 6;
                        break;
                case Types.INTEGER:
                        maxLen = 11;
                        break;
                case Types.BIGINT:
                        maxLen = 20;
                        break;
                case Types.REAL:
                        maxLen = 13;
                        break;
                case Types.FLOAT:
                case Types.DOUBLE:
                        maxLen = 22;
                        break;
                case Types.DECIMAL:
                case Types.NUMERIC:
                        maxLen = 38;
                        break;
                default:
                        // Get the precision for the SQL type.
                        maxLen = getPrecision (sqlType);
                        if ((maxLen <= 0) ||
                                (maxLen > JdbcOdbcLimits.DEFAULT_IN_PRECISION)) {
                                maxLen = JdbcOdbcLimits.DEFAULT_IN_PRECISION;
                        }
                }



                // Get the buffer to be used for the output length

                byte dataLen[] = getLengthBuf (parameterIndex);

                // Convert the given Java SQL type to an ODBC SQL type
                sqlType = OdbcDef.jdbcTypeToOdbc (sqlType);
                long buffers[]=new long[4];
                buffers[0]=0;
                buffers[1]=0;
                buffers[2]=0;
                buffers[3]=0;
                byte[] dataBuf = null;

                if (boundParams[parameterIndex - 1].isInOutParameter() && boundParams[parameterIndex - 1].boundValue == null) {
                        dataBuf = null;
                } else {
                        dataBuf = allocBindBuf (parameterIndex, maxLen + 1);
                }

                if (boundParams[parameterIndex - 1].isInOutParameter()) {
                        if (boundParams[parameterIndex - 1].boundValue == null) {
                                OdbcApi.SQLBindInOutParameterNull (hStmt, parameterIndex,
                                    sqlType, maxLen, boundParams[parameterIndex - 1].scale,
                                        dataLen, buffers);
                        } else if(sqlType == Types.INTEGER) { // bug 4412437 - begin
                                if(boundParams[parameterIndex - 1].boundType == Types.INTEGER) {
                                        OdbcApi.SQLBindInOutParameterFixed(hStmt, parameterIndex, sqlType, maxLen, dataBuf,
                                                dataLen, buffers);
                                } else {
                                        throw new SQLException("Type mismatch between the set function and registerOutParameter");
                                }
                        } else if(sqlType == Types.BIT) {
                                if(boundParams[parameterIndex - 1].boundType == Types.BIT) {
                                        OdbcApi.SQLBindInOutParameterFixed(hStmt, parameterIndex, sqlType, maxLen, dataBuf,
                                                dataLen, buffers);
                                } else {
                                        throw new SQLException("Type mismatch between the set function and registerOutParameter");
                                }
                        } else if(sqlType == Types.TINYINT) {
                                if(boundParams[parameterIndex - 1].boundType == Types.TINYINT) {
                                        OdbcApi.SQLBindInOutParameterFixed(hStmt, parameterIndex, sqlType, maxLen, dataBuf,
                                                dataLen, buffers);
                                } else {
                                        throw new SQLException("Type mismatch between the set function and registerOutParameter");
                                }
                        } else if(sqlType == Types.SMALLINT) {
                                if(boundParams[parameterIndex - 1].boundType == Types.SMALLINT) {
                                        OdbcApi.SQLBindInOutParameterFixed(hStmt, parameterIndex, sqlType, maxLen, dataBuf,
                                                dataLen, buffers);
                                } else {
                                        throw new SQLException("Type mismatch between the set function and registerOutParameter");
                                }
                        // Fix 4532167. Types.FLOAT is equivalent to Types.DOUBLE.
                        } else if(sqlType == Types.DOUBLE || sqlType == Types.FLOAT || sqlType == Types.REAL) {
                                if(boundParams[parameterIndex - 1].boundType == Types.DOUBLE ||
                                   boundParams[parameterIndex - 1].boundType == Types.FLOAT ||
                                   boundParams[parameterIndex - 1].boundType == Types.REAL) {
                                        OdbcApi.SQLBindInOutParameterFixed(hStmt, parameterIndex, Types.DOUBLE, maxLen, dataBuf,
                                                dataLen, buffers);
                                } else {
                                        throw new SQLException("Type mismatch between the set function and registerOutParameter");
                                }
                        } else if(sqlType == Types.BIGINT) {//4532162
                                if(boundParams[parameterIndex - 1].boundType == Types.BIGINT) {
                                        OdbcApi.SQLBindInOutParameterFixed(hStmt, parameterIndex, sqlType, maxLen, dataBuf,
                                                dataLen, buffers);
                                } else {
                                        throw new SQLException("Type mismatch between the set function and registerOutParameter");
                                }
                        } else if(sqlType == Types.CHAR || sqlType == Types.VARCHAR) {
                                if(boundParams[parameterIndex - 1].boundType == Types.CHAR) {
                                        byte[] newDataBuf = new byte[maxLen+1];
                                        for(int i = 0; i < newDataBuf.length; i++) {
                                                newDataBuf[i] = '\0';
                                        }
                                        for(int i = 0; i < dataBuf.length; i++) {
                                                newDataBuf[i] = dataBuf[i];
                                        }
                                        boundParams[parameterIndex-1].resetBindDataBuffer(newDataBuf);
                                        OdbcApi.SQLBindInOutParameterStr(hStmt, parameterIndex, sqlType, maxLen, newDataBuf,
                                                dataLen, buffers, OdbcDef.SQL_NTS);
                                } else {
                                        throw new SQLException("Type mismatch between the set function and registerOutParameter");
                                }
                        } else if(sqlType == Types.BINARY || sqlType == Types.VARBINARY ) {
                                if(boundParams[parameterIndex - 1].boundType == Types.BINARY ||
                                        boundParams[parameterIndex - 1].boundType == Types.VARBINARY ) { // 4532171
                                        byte[] newDataBuf = new byte[maxLen+1];
                                        for(int i = 0; i < newDataBuf.length; i++) {
                                                newDataBuf[i] = 0;
                                        }
                                        for(int i = 0; i < dataBuf.length; i++) {
                                                newDataBuf[i] = dataBuf[i];
                                        }
                                        boundParams[parameterIndex-1].resetBindDataBuffer(newDataBuf);
                                        OdbcApi.SQLBindInOutParameterBin(hStmt, parameterIndex, sqlType, maxLen, newDataBuf,
                                                dataLen, buffers, dataBuf.length);
                                } else {
                                        throw new SQLException("Type mismatch between the set function and registerOutParameter");
                                }
                        } else if(sqlType == Types.NUMERIC || sqlType == Types.DECIMAL) {
                                String bigDecimalString = null;
                                try {
                                        bigDecimalString = BytesToChars(OdbcApi.charSet, dataBuf);
                                } catch (java.io.UnsupportedEncodingException exx) {
                                        // no action required because dataBuf is obtained from a previous CharsToBytes.
                                        throw (Error) (new InternalError("SQL")).initCause(exx);
                                }

                                BigDecimal bd = new BigDecimal(bigDecimalString);

                                byte[] newDataBuf = null; // for resetting the bind buffer for the parameter number
                                byte[] tempBuf = null;

                                if(scale >= bd.scale()) {

                                        BigDecimal bdIncreasedScale = (bd.movePointRight(scale)).movePointLeft(scale);

                                        // fill the newDataBuf with '0'
                                        newDataBuf = new byte[maxLen];
                                        for(int i = 0; i < newDataBuf.length; i++) {
                                                newDataBuf[i] = '0';
                                        }

                                        try {
                                                /**
                                                 * get the byte array from the big decimal with increased scale and copy the
                                                   byte array into newDataBuf such that newDataBuf is left padded with '0' and
                                                   contains the elements in tempDataBuf towards the lower end of the array.
                                                 */
                                                tempBuf = CharsToBytes(OdbcApi.charSet, bdIncreasedScale.toString().toCharArray());
                                                for(int i = newDataBuf.length - tempBuf.length; i < newDataBuf.length; i++) {
                                                        newDataBuf[i] = tempBuf[i - (newDataBuf.length - tempBuf.length)];
                                                }
                                        } catch (java.io.UnsupportedEncodingException exx) {
                                                throw (Error) (new InternalError("SQL")).initCause(exx);
                                        }

                                        boundParams[parameterIndex - 1].resetBindDataBuffer(newDataBuf);
                                        boundParams[parameterIndex - 1].scale = bdIncreasedScale.scale();
                                }
                                OdbcApi.SQLBindInOutParameterString (hStmt, parameterIndex, sqlType, maxLen,
                                        boundParams[parameterIndex - 1].scale, newDataBuf, dataLen, buffers);
                        } else if (sqlType == OdbcDef.SQL_TIMESTAMP) {//4532162
                                if (boundParams[parameterIndex - 1].boundType == Types.TIMESTAMP) {
                                        //OdbcApi.SQLBindInOutParameterTimeStamp (hStmt, parameterIndex,
                                        //sqlType, maxLen, boundParams[parameterIndex - 1].scale,
                                        //dataBuf, dataLen, buffers);

                                        int scal, precision;
                                        scal=0;precision=0;
                                        Timestamp ts = (Timestamp)boundParams[parameterIndex - 1].boundValue;

                                        Calendar c = Calendar.getInstance();
                                        c.setTime(ts);

                                        int year = c.get(Calendar.YEAR);
                                        int month = c.get(Calendar.MONTH);
                                        int day = c.get(Calendar.DAY_OF_MONTH);
                                        int hour = c.get(Calendar.HOUR_OF_DAY);
                                        int minutes = c.get(Calendar.MINUTE);
                                        int seconds = c.get(Calendar.SECOND);
                                        int nanos = ts.getNanos();

                                        month = month + 1;

                                        byte[] newDataBuf = new byte[16]; // sizeof(TIMESTAMP_STRUCT)
                                        OdbcApi.getTimestampStruct(newDataBuf, year, month, day, hour, minutes, seconds, nanos);

                                        boundParams[parameterIndex - 1].resetBindDataBuffer(newDataBuf);

                                        Integer iTs =  new Integer(nanos);
                                        String sTs  =  iTs.toString();

                                        char cNanos[] = sTs.toCharArray();

                                        for (scal = cNanos.length; scal > 0; scal--){
                                                if (cNanos[scal-1] != '0'){
                                                        break;
                                                }
                                        }

                                        if (nanos == 0) {
                                        scal = 1;
                                        }

                                        precision = 20 + scal;

                                        OdbcApi.SQLBindInOutParameterTimestamp (hStmt, parameterIndex, 29,
                                                9, newDataBuf, dataLen, buffers);

                                } else {
                                        throw new SQLException("Type mismatch between the set function and registerOutParameter");
                                }
                        } else if (sqlType == OdbcDef.SQL_DATE) {
                                if (boundParams[parameterIndex - 1].boundType == Types.DATE) {
                                        //OdbcApi.SQLBindInOutParameterTimeStamp (hStmt, parameterIndex,
                                        //sqlType, maxLen, boundParams[parameterIndex - 1].scale,
                                        //dataBuf, dataLen, buffers);
                                        Date d = (Date)boundParams[parameterIndex - 1].boundValue;
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(d);

                                        int year = c.get(Calendar.YEAR);
                                        int month = c.get(Calendar.MONTH);
                                        int day = c.get(Calendar.DAY_OF_MONTH);

                                        month = month + 1;

                                        byte[] newDataBuf = new byte[6]; // sizeof(DATE_STRUCT)
                                        OdbcApi.getDateStruct(newDataBuf, year, month, day);

                                        boundParams[parameterIndex - 1].resetBindDataBuffer(newDataBuf);

                                        OdbcApi.SQLBindInOutParameterDate (hStmt, parameterIndex,
                                                boundParams[parameterIndex - 1].scale, newDataBuf, dataLen, buffers);

                                } else {
                                        throw new SQLException("Type mismatch between the set function and registerOutParameter");
                                }
                        } else if (sqlType == OdbcDef.SQL_TIME) {
                                if (boundParams[parameterIndex - 1].boundType == Types.TIME) {
                                        //OdbcApi.SQLBindInOutParameterTimeStamp (hStmt, parameterIndex,
                                        //sqlType, maxLen, boundParams[parameterIndex - 1].scale,
                                        //dataBuf, dataLen, buffers);
                                        Time t = (Time)boundParams[parameterIndex - 1].boundValue;
                                        Calendar c = Calendar.getInstance();
                                        c.setTime(t);

                                        int hour = c.get(Calendar.HOUR_OF_DAY);
                                        int minutes = c.get(Calendar.MINUTE);
                                        int seconds = c.get(Calendar.SECOND);


                                        byte[] newDataBuf = new byte[6]; // sizeof(TIME_STRUCT)
                                        OdbcApi.getTimeStruct(newDataBuf, hour, minutes, seconds);

                                        boundParams[parameterIndex - 1].resetBindDataBuffer(newDataBuf);

                                        OdbcApi.SQLBindInOutParameterTime (hStmt, parameterIndex,
                                                boundParams[parameterIndex - 1].scale, newDataBuf, dataLen, buffers);

                                } else {
                                        throw new SQLException("Type mismatch between the set function and registerOutParameter");
                                }
                        } else if (sqlType == Types.LONGVARCHAR) { //4641016 - begin
                                if(boundParams[parameterIndex - 1].boundType == Types.LONGVARCHAR
                                        || boundParams[parameterIndex - 1].boundType == Types.VARCHAR
                                                || boundParams[parameterIndex - 1].boundType == Types.CHAR) {

                                        if(boundParams[parameterIndex - 1].boundValue instanceof java.io.InputStream) {
                                                byte[] newDataBuf = new byte[JdbcOdbcLimits.DEFAULT_IN_PRECISION];
                                                for(int i = 0; i < newDataBuf.length; i++) {
                                                        newDataBuf[i] = '\0';
                                                }
                                                for(int i = 0; i < dataBuf.length; i++) {
                                                        newDataBuf[i] = dataBuf[i];
                                                }

                                                boundParams[parameterIndex - 1].resetBindDataBuffer(newDataBuf);

                                                byte[] lenBuf = getLengthBuf (parameterIndex);

                                                OdbcApi.SQLBindInOutParameterAtExec(hStmt, parameterIndex,
                                                        (int)OdbcDef.SQL_C_CHAR, Types.LONGVARCHAR, newDataBuf.length, newDataBuf,
                                                                boundParams[parameterIndex - 1].getInputStreamLen(), lenBuf, buffers);
                                        } else if(boundParams[parameterIndex - 1].boundValue instanceof String) {
                                                byte[] strBuf = null;
                                                try {
                                                        strBuf = ((String)(boundParams[parameterIndex - 1].boundValue)).getBytes(OdbcApi.charSet);
                                                } catch(java.io.UnsupportedEncodingException uee) {
                                                        throw new SQLException(uee.getMessage());
                                                }
                                                java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(strBuf);
                                                boundParams[parameterIndex - 1].setInputStream(bais, strBuf.length);

                                                byte[] newDataBuf = new byte[JdbcOdbcLimits.DEFAULT_IN_PRECISION];
                                                for(int i = 0; i < newDataBuf.length; i++) {
                                                        newDataBuf[i] = '\0';
                                                }

                                                OdbcApi.intTo4Bytes(parameterIndex, newDataBuf);

                                                boundParams[parameterIndex - 1].resetBindDataBuffer(newDataBuf);

                                                byte[] lenBuf = getLengthBuf (parameterIndex);

                                                OdbcApi.SQLBindInOutParameterAtExec(hStmt, parameterIndex,
                                                        (int)OdbcDef.SQL_C_CHAR, Types.LONGVARCHAR, newDataBuf.length, newDataBuf,
                                                                boundParams[parameterIndex - 1].getInputStreamLen(), lenBuf, buffers);
                                        } else {
                                                throw new UnsupportedOperationException();
                                        }
                                } else {
                                        throw new SQLException("Type mismatch between the set function and registerOutParameter");
                                }
                        } else if (sqlType == Types.LONGVARBINARY) { //4641016
                                if(boundParams[parameterIndex - 1].boundType == Types.LONGVARBINARY
                                                || boundParams[parameterIndex - 1].boundType == Types.VARBINARY
                                                        || boundParams[parameterIndex - 1].boundType == Types.BINARY) {

                                        if(boundParams[parameterIndex - 1].boundValue instanceof java.io.InputStream) {
                                                byte[] newDataBuf = new byte[JdbcOdbcLimits.DEFAULT_IN_PRECISION];
                                                for(int i = 0; i < newDataBuf.length; i++) {
                                                        newDataBuf[i] = '0';
                                                }
                                                for(int i = 0; i < dataBuf.length; i++) {
                                                        newDataBuf[i] = dataBuf[i];
                                                }

                                                boundParams[parameterIndex - 1].resetBindDataBuffer(newDataBuf);

                                                byte[] lenBuf = getLengthBuf (parameterIndex);

                                                OdbcApi.SQLBindInOutParameterAtExec(hStmt, parameterIndex,
                                                        (int)OdbcDef.SQL_C_BINARY, Types.LONGVARBINARY, newDataBuf.length, newDataBuf,
                                                                boundParams[parameterIndex - 1].getInputStreamLen(), lenBuf, buffers);
                                        }  else if(boundParams[parameterIndex - 1].boundValue instanceof byte[]) {
                                                byte[] b = ((byte[])(boundParams[parameterIndex - 1].boundValue));
                                                boundParams[parameterIndex - 1].setInputStream(new java.io.ByteArrayInputStream(b), b.length);

                                                byte[] newDataBuf = new byte[JdbcOdbcLimits.DEFAULT_IN_PRECISION];
                                                for(int i = 0; i < newDataBuf.length; i++) {
                                                        newDataBuf[i] = '0';
                                                }

                                                OdbcApi.intTo4Bytes(parameterIndex, newDataBuf);

                                                boundParams[parameterIndex - 1].resetBindDataBuffer(newDataBuf);

                                                byte[] lenBuf = getLengthBuf (parameterIndex);

                                                OdbcApi.SQLBindInOutParameterAtExec(hStmt, parameterIndex,
                                                        (int)OdbcDef.SQL_C_BINARY, Types.LONGVARBINARY, newDataBuf.length, newDataBuf,
                                                                boundParams[parameterIndex - 1].getInputStreamLen(), lenBuf, buffers);

                                        } else {
                                                throw new UnsupportedOperationException();
                                        }
                                } else {
                                        throw new SQLException("Type mismatch between the set function and registerOutParameter");
                                } // 4641016 - end
                        } else {
                                throw new UnsupportedOperationException();
                        } // bug 4412437 - end
                } else {  // if out parameter
                // check if maxLen is 8000(i.e DEFAULT)
                // Modify maxLen to be appropriate value it should be.
                        if (sqlType == Types.NULL ) { //4532162
                                        OdbcApi.SQLBindOutParameterNull (hStmt, parameterIndex,
                                        sqlType, maxLen, boundParams[parameterIndex - 1].scale,
                                        dataLen, buffers);
                        }
                        else if (sqlType == Types.DOUBLE || sqlType == Types.FLOAT || sqlType == Types.REAL) { //4532162
                                        OdbcApi.SQLBindOutParameterFixed (hStmt, parameterIndex,
                                                Types.DOUBLE, maxLen, dataBuf, dataLen, buffers);
                        }
                        else if (sqlType == Types.INTEGER) { //4532162
                                        OdbcApi.SQLBindOutParameterFixed (hStmt, parameterIndex,
                                                sqlType, maxLen, dataBuf, dataLen, buffers);
                        }
                        else if (sqlType == Types.TINYINT) { //4532162
                                        OdbcApi.SQLBindOutParameterFixed (hStmt, parameterIndex,
                                                sqlType, maxLen, dataBuf, dataLen, buffers);
                        }
                        else if (sqlType == Types.SMALLINT) { //4532162
                                        OdbcApi.SQLBindOutParameterFixed (hStmt, parameterIndex,
                                                sqlType, maxLen, dataBuf, dataLen, buffers);
                        }
                        else if (sqlType == Types.BIT) { //4532162
                                        OdbcApi.SQLBindOutParameterFixed (hStmt, parameterIndex,
                                                sqlType, 1, dataBuf, dataLen, buffers);
                        }
                        else if (sqlType == Types.BIGINT) { //4532162
                                        OdbcApi.SQLBindOutParameterFixed (hStmt, parameterIndex,
                                                sqlType, maxLen, dataBuf, dataLen, buffers);
                        }
                        else if(sqlType == Types.NUMERIC || sqlType == Types.DECIMAL) {//4532162
                                        //4709519
                                        //changing boundParams[parameterIndex - 1].scale to scale
                                        OdbcApi.SQLBindOutParameterString (hStmt, parameterIndex, sqlType,
                                                scale, dataBuf, dataLen, buffers);
                        }
                        else if (sqlType == OdbcDef.SQL_TIMESTAMP) { //4532162
                                        OdbcApi.SQLBindOutParameterTimestamp (hStmt, parameterIndex,
                                                maxLen, dataBuf, dataLen, buffers);
                        }
                        else if (sqlType == OdbcDef.SQL_DATE) {//4532162
                                        OdbcApi.SQLBindOutParameterDate (hStmt, parameterIndex,
                                                boundParams[parameterIndex - 1].scale, dataBuf, dataLen, buffers);
                        }
                        else if (sqlType == OdbcDef.SQL_TIME) {//4532162
                                        OdbcApi.SQLBindOutParameterTime (hStmt, parameterIndex,
                                                boundParams[parameterIndex - 1].scale, dataBuf, dataLen, buffers);
                        }
                        else if(sqlType == Types.BINARY || sqlType == Types.VARBINARY || sqlType == Types.LONGVARBINARY) {//4532162
                                        OdbcApi.SQLBindOutParameterBinary(hStmt, parameterIndex, sqlType,
                                                maxLen, scale, dataBuf, dataLen, buffers);

                        }
                        else if(sqlType == Types.CHAR || sqlType == Types.VARCHAR || sqlType == Types.LONGVARCHAR) {//4532162
                                        OdbcApi.SQLBindOutParameterString (hStmt, parameterIndex,
                                                sqlType, scale, dataBuf, dataLen, buffers);
                        }
                        else {//4532162
                                         OdbcApi.SQLBindOutParameterString (hStmt, parameterIndex,
                                                sqlType, scale, dataBuf, dataLen, buffers);

                        }
                } //if OutParameter

                //save the native pointers from garbage collection
                boundParams[parameterIndex - 1].pA1=buffers[0];
                boundParams[parameterIndex - 1].pA2=buffers[1];
                boundParams[parameterIndex - 1].pB1=buffers[2];
                boundParams[parameterIndex - 1].pB2=buffers[3];
}

        //--------------------------------------------------------------------
        // wasNull
        // An OUT parameter may have the value of SQL NULL; wasNull reports
        // whether the last value read has this special value.
        //--------------------------------------------------------------------

        public boolean wasNull ()
                throws SQLException
        {
                return lastParameterNull;
        }

        //--------------------------------------------------------------------
        // getString
        //--------------------------------------------------------------------

        public String getString (
                int parameterIndex)
                throws SQLException
        {
                // If the value is null, return a null object
                if (isNull (parameterIndex)) {
                        return null;
                }

                // Get the SQL type of the parameter

                int sqlType = getSqlType (parameterIndex);

                // Convert the data buffer for the output parameter into
                // a String
                String s=null;
                try
                {
                        byte[] bindBuf = getDataBuf(parameterIndex);
                        if (bindBuf != null)
                        {
                                s = BytesToChars (OdbcApi.charSet, bindBuf);
                        }
                }
                catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }
                // Remove trailing spaces

                //s = s.trim ();

                if (OdbcApi.getTracer().isTracing ()) {
                        OdbcApi.getTracer().trace ("String value for OUT parameter " +
                                   parameterIndex + "=" + s);
                }
                return s;
        }

        //--------------------------------------------------------------------
        // Since the output parameter was bound as a specific C type
        // (always a SQL_C_CHAR), we will have to get the output parameter
        // as a character, then convert it to the proper return type.
        //--------------------------------------------------------------------

        //--------------------------------------------------------------------
        // getBoolean
        //--------------------------------------------------------------------

        public boolean getBoolean (
                int parameterIndex)
                throws SQLException
        {
                // If the value is null, return false
                if (isNull (parameterIndex)) {
                        return false;
                }

                        //4532162
                        if(getInt(parameterIndex) == 1)
                                return true;
                        else
                                return false;
        }

        //--------------------------------------------------------------------
        // getByte
        //--------------------------------------------------------------------

        public byte getByte (
                int parameterIndex)
                throws SQLException
        {
                // Cast getInt to a byte value
                //4532162
                return (byte) getInt (parameterIndex);
        }

        //--------------------------------------------------------------------
        // getShort
        //--------------------------------------------------------------------

        public short getShort (
                int parameterIndex)
                throws SQLException
        {
                // Cast getInt to a short value
                //4532162
                return (short) getInt (parameterIndex);
        }

        //--------------------------------------------------------------------
        // getInt
        //--------------------------------------------------------------------

        public int getInt (
                int parameterIndex)
                throws SQLException
        {
                // If the value is null, return zero
                if (isNull (parameterIndex)) {
                        return 0;
                }

                // bug 4412437  //4532162
                return OdbcApi.bufferToInt(getDataBuf(parameterIndex));
        }

        //--------------------------------------------------------------------
        // getLong
        //--------------------------------------------------------------------

        public long getLong (
                int parameterIndex)
                throws SQLException
        {
                // If the value is null, return zero
                if (isNull (parameterIndex)) {
                        return 0;
                }
                //4532162
                // Create a Long object from the output parameter string
                return OdbcApi.bufferToLong(getDataBuf(parameterIndex));

        }

        //--------------------------------------------------------------------
        // getFloat
        //--------------------------------------------------------------------

        public float getFloat (
                int parameterIndex)
                throws SQLException
        {
                // If the value is null, return zero
                if (isNull (parameterIndex)) {
                        return 0;
                }

                // Fix 4532167. using bufferToDouble to get even the minimum value of java
                return (float) OdbcApi.bufferToDouble(getDataBuf(parameterIndex));
        }

        //--------------------------------------------------------------------
        // getDouble
        //--------------------------------------------------------------------

        public double getDouble (
                int parameterIndex)
                throws SQLException
        {
                // If the value is null, return zero
                if (isNull (parameterIndex)) {
                        return 0;
                }

                // bug 4412437   //4532162
                return OdbcApi.bufferToDouble(getDataBuf(parameterIndex));
        }

        //--------------------------------------------------------------------
        // getBigDecimal
        //--------------------------------------------------------------------

        public BigDecimal getBigDecimal (
                int parameterIndex,
                int scale)
                throws SQLException
        {
                // If the value is null, return null
                if (isNull (parameterIndex)) {
                        return null;
                }
                // Create a BigDecimal object from the output parameter string
                BigDecimal num = new BigDecimal(getString (parameterIndex).trim());
                /* NOTE: In the old-style Bignum there was the notion of
                   a default rounding mode. There is no longer this
                   notion, and so we default to something reasonable. */
                return num.setScale(scale, BigDecimal.ROUND_HALF_EVEN);
        }

        //--------------------------------------------------------------------
        // getBytes
        // Returns a byte array of the bound parameter data.
        //--------------------------------------------------------------------

        public byte[] getBytes (
                int parameterIndex)
                throws SQLException
        {
                // If the value is null, return null
                if (isNull (parameterIndex)) {
                        return null;
                }

                if (boundParams[parameterIndex-1].isInOutParameter() || boundParams[parameterIndex-1].isOutputParameter() )
                {
                //4532162
                int paramLength = getParamLength(parameterIndex);
                byte[] bindBuf = getDataBuf(parameterIndex);
                if (paramLength < bindBuf.length)
                {
                        byte[] outBuf = new byte[paramLength];
                        for (int i=0; i<getParamLength(parameterIndex); i++)
                                outBuf[i] = bindBuf[i];
                        boundParams[parameterIndex-1].resetBindDataBuffer(outBuf);
                        return outBuf;
                }
                return bindBuf;
                }
                // Convert the bound character string (in hexadecimal)
                // into a byte array
                return hexStringToByteArray (getString (parameterIndex).trim());
        }

        //--------------------------------------------------------------------
        // getDate
        //--------------------------------------------------------------------

        public java.sql.Date getDate (
                int parameterIndex)
                throws SQLException
        {

                // If the value is null, return null
                if (isNull (parameterIndex)) {
                        return null;
                }

                //4532162
                byte[] dataBuf = getDataBuf(parameterIndex);
                byte[] dateString = new byte[11];

                OdbcApi.convertDateString(dataBuf, dateString);
                return java.sql.Date.valueOf((new String(dateString)).trim());
        }

        //--------------------------------------------------------------------
        // getTime
        //--------------------------------------------------------------------

        public java.sql.Time getTime (
                int parameterIndex)
                throws SQLException
        {
                // If the value is null, return zero
                if (isNull (parameterIndex)) {
                        return null;
                }
                //4532162
                byte[] dataBuf = getDataBuf(parameterIndex);
                byte[] timeString = new byte[9];

                OdbcApi.convertTimeString(dataBuf, timeString);
                return java.sql.Time.valueOf((new String(timeString)).trim());
        }

        //--------------------------------------------------------------------
        // getTimestamp
        //--------------------------------------------------------------------

        public java.sql.Timestamp getTimestamp (
                int parameterIndex)
                throws SQLException
        {
                if (isNull (parameterIndex)) {
                        return null;
                }
                //4532162
                byte[] dataBuf = getDataBuf(parameterIndex);
                byte[] timestampString = new byte[30];

                OdbcApi.convertTimestampString(dataBuf, timestampString);
                return java.sql.Timestamp.valueOf((new String(timestampString)).trim());

        }


        //--------------------------------------------------------------------
        // getObject
        // Returns a Java object for the parameter.
        // See the JDBC spec's "Dynamic Programming" chapter for details.
        //--------------------------------------------------------------------

        public Object getObject (
                int parameterIndex)
                throws SQLException
        {
                Object  value = null;

                // Get the SQL type of the parameter
                int sqlType = getSqlType (parameterIndex);

                // If the column is null, always return a null object
                if (isNull (parameterIndex)) {
                        return null;
                }

                // For each SQL type, call the appropriate routine and
                // convert to the proper object type

                switch (sqlType) {

                case Types.CHAR:
                case Types.VARCHAR:
                case Types.LONGVARCHAR:

//                      // Get the data as a string, convert it to a byte
//                      // array, and return an InputStream
//
//                      String s = getLongVarChar (parameterIndex);
//                      if (s != null) {
//                              byte b[] = new byte[s.length ()];
//                              s.getBytes (0, s.length (), b, 0);
//                              value = (Object) new JdbcOdbcInputStream (
//                                      OdbcApi, hStmt, parameterIndex, b);
//                      }

                        value = (Object) getString (parameterIndex);
                        break;

                case Types.NUMERIC:
                case Types.DECIMAL:

                        // The getNumeric method requires us to provide
                        // a scale.  The only way to get the scale is to
                        // call DatabaseMetaData.getProcedureColumns
                        // (which may not be supported for the data source)
                        // We'll use the scale stored in scalez[]
                          if (parameterIndex<=200)
                             value = (Object) getBigDecimal(parameterIndex, scalez[parameterIndex]);
                          else
                             value = (Object) getBigDecimal(parameterIndex, JdbcOdbcLimits.DEFAULT_OUT_SCALE);
                        break;

                case Types.BIT:
                        value = (Object) new Boolean (
                                        getBoolean (parameterIndex));
                        break;

                case Types.TINYINT:
                        value = (Object) new Integer (getByte (
                                        parameterIndex));
                        break;

                case Types.SMALLINT:
                        value = (Object) new Integer (getShort (
                                        parameterIndex));
                        break;

                case Types.INTEGER:
                        value = (Object) new Integer (getInt (
                                        parameterIndex));
                        break;

                case Types.BIGINT:
                        value = (Object) new Long (getLong (parameterIndex));
                        break;

                case Types.REAL:
                        value = (Object) new Float (getFloat (parameterIndex));
                        break;
                //4532167
                case Types.FLOAT:
                case Types.DOUBLE:
                        value = (Object) new Double (getDouble (
                                        parameterIndex));
                        break;

                case Types.BINARY:
                case Types.VARBINARY:
                case Types.LONGVARBINARY:

//                      // Get the data as a byte array and convert to an
//                      // InputStream
//
//                      byte b[] = getLongVarBinary (parameterIndex);
//                      if (b != null) {
//                              value = (Object) new JdbcOdbcInputStream (
//                                      OdbcApi, hStmt, parameterIndex, b);
//                      }
                        value = (Object) getBytes (parameterIndex);
                        break;

                case Types.DATE:
                        value = (Object) getDate (parameterIndex);
                        break;

                case Types.TIME:
                        value = (Object) getTime (parameterIndex);
                        break;

                case Types.TIMESTAMP:
                        value = (Object) getTimestamp (parameterIndex);
                        break;

                }

                return value;
        }


        // New JDBC 2.0 API

        public BigDecimal getBigDecimal (
                int parameterIndex)
                throws SQLException
        {
                // If the value is null, return null
                if (isNull (parameterIndex))
                {
                        return null;
                }
                // Create a BigDecimal object from the output parameter string
                BigDecimal num = new BigDecimal(getString (parameterIndex).trim());

                /* NOTE: for 2.0 version the decimal value return a full precision.
                   no scale is specified. */

                return num;
        }


        public Object getObject (
                int i,
                Map map)
                throws SQLException
        { return null; }

        public Ref getRef (
                int i)
                throws SQLException
        { return null; }

        public Blob getBlob (
                int i)
                throws SQLException
        { return null; }

        public Clob getClob (
                int i)
                throws SQLException
        { return null; }

        public Array getArray (
                int i)
                throws SQLException
        { return null; }

        public Date getDate (
                int parameterIndex,
                Calendar cal)
                throws SQLException
        {
                long lValue = 0L;
                //4532162
                //store date vaue in a java.sql.Date value
                if (getDate(parameterIndex) == null){
                        return null;
                }
                else if (getDate(parameterIndex) != null){
                        // Fix 4380653.
                        lValue = utils.convertFromGMT(getDate(parameterIndex) , cal);
                }

                if (lValue == 0L){
                        return null;
                }
                else
                        return new java.sql.Date(lValue);
        }

        public Time getTime (
                int parameterIndex,
                Calendar cal)
                throws SQLException
        {
                long lValue = 0L;
                //4532162
                if ( getTime(parameterIndex) == null) {
                        return null;
                }
                else if (getTime(parameterIndex) != null){
                        try{
                            // Fix 4389653
                            lValue = utils.convertFromGMT(getTime(parameterIndex), cal);
                        }catch (Exception te){}
                }

                if (lValue == 0L){
                        return null;
                }
                else
                        return new java.sql.Time(lValue);

        }

        public Timestamp getTimestamp (
                int parameterIndex,
                Calendar cal)
                throws SQLException
        {
                long lValue = 0L;
                //4532162
                if ( (getTimestamp (parameterIndex)) == null){
                        return null;
                }

                try{
                    // Fix 4380653
                    lValue = utils.convertFromGMT(getTimestamp (parameterIndex), cal);
                }catch (Exception te){}

                if (lValue == 0L){
                        return null;
                }
                else
                return new java.sql.Timestamp(lValue);
        }

        public void registerOutParameter (
                int paramIndex,
                int sqlType,
                String typeName)
                throws SQLException
        {       //4532162
                throw new UnsupportedOperationException();
        }

        //--------------------------------------------------------------------
        // isNull
        // Helper method to determine if an output parameter is null.  Also
        // sets the lastParameterNull attribute for use with wasNull.
        //--------------------------------------------------------------------

        protected boolean isNull (
                int parameterIndex)
                throws SQLException
        {
                // Verify that the given parameter is for OUTPUT

                if (!isOutputParameter (parameterIndex)) {
                        throw new SQLException ("Parameter " + parameterIndex +
                                        " is not an OUTPUT parameter");
                }

                boolean rc = false;
                rc = (getParamLength(parameterIndex) == OdbcDef.SQL_NULL_DATA);
                if (OdbcApi.getTracer().isTracing ()) {
                        OdbcApi.getTracer().trace ("Output Parameter " + parameterIndex +
                                        " null: " + rc);
                }
                lastParameterNull = rc;
                return rc;
        }

/*
        //THIS MAY NEED TO BE MOVE TO JdbcOdbcPreparedStatement class!!
        // now also used by executeBatchUpdate for PreparedStatement.
        //--------------------------------------------------------------------
        // setSqlType
        // Sets the Java sql type for when registering an OUT parameter
        //--------------------------------------------------------------------

        protected void setSqlType (
                int index,
                int type)
        {
                // Sanity check the parameter number

                if ((index >= 1) &&
                    (index <= numParams)) {
                        boundParams[index - 1].setSqlType (type);
                }
        }


        //THIS MAY NEED TO BE MOVE TO JdbcOdbcPreparedStatement class!!
        // now also used by executeBatchUpdate for PreparedStatement.
        //--------------------------------------------------------------------
        // getSqlType
        // Gets the Java sql type for when registering an OUT parameter
        //--------------------------------------------------------------------

        protected int getSqlType (
                int index)
        {
                int type = Types.OTHER;

                // Sanity check the parameter number

                if ((index >= 1) &&
                    (index <= numParams)) {
                        type = boundParams[index - 1].getSqlType ();
                }
                return type;
        }
*/

        //--------------------------------------------------------------------
        // setOutputParameter
        // Sets the output parameter flag
        //--------------------------------------------------------------------

        protected void setOutputParameter (
                int index,
                boolean output)
        {
                // Sanity check the parameter number

                if ((index >= 1) &&
                    (index <= numParams)) {
                        boundParams[index - 1].setOutputParameter (output);
                }
        }

        //--------------------------------------------------------------------
        // isOutputParameter
        // Returns true if the given parameter is for OUTPUT
        //--------------------------------------------------------------------

        protected boolean isOutputParameter (
                int index)
        {
                boolean output = false;

                // Sanity check the parameter number

                if ((index >= 1) &&
                    (index <= numParams)) {
                        output = boundParams[index - 1].isOutputParameter ();
                }
                return output;
        }



        public synchronized void close ()
                throws SQLException
        {
                if (OdbcApi.getTracer().isTracing ()) {
                        OdbcApi.getTracer().trace ("*Statement.close");
                }

                // Close/clear our result set

                clearMyResultSet ();

                // Reset last warning message

                try {
                        clearWarnings ();
                        if (hStmt != OdbcDef.SQL_NULL_HSTMT) {
                                //4524683
                                if(closeCalledFromFinalize == true) {
                                        if( myConnection.isFreeStmtsFromConnectionOnly() == false) {
                                                OdbcApi.SQLFreeStmt (hStmt, OdbcDef.SQL_DROP);
                                        }
                                }
                                else {
                                        OdbcApi.SQLFreeStmt (hStmt, OdbcDef.SQL_DROP);
                                }
                                hStmt = OdbcDef.SQL_NULL_HSTMT;
                                FreeParams();
                                for (int pindex=1; boundParams != null && pindex <= boundParams.length; pindex++)
                                {
                                        boundParams[pindex-1].binaryData = null;
                                        boundParams[pindex-1].initialize ();
                                        boundParams[pindex-1].paramInputStream = null;
                                        boundParams[pindex-1].inputParameter = false;
                                }
                        }
                }
                catch (SQLException ex) {
                        // If we get an error, ignore
                }

                // Remove this Statement object from the Connection object's
                // list

                myConnection.deregisterStatement (this);
        }

public synchronized void FreeParams()
        throws NullPointerException
{
        try {
                for (int pindex=1; pindex <= boundParams.length; pindex++)
                {
                        if (boundParams[pindex-1].pA1!=0)
                        {
                                OdbcApi.ReleaseStoredBytes (boundParams[pindex-1].pA1, boundParams[pindex-1].pA2);
                                boundParams[pindex-1].pA1=0;
                                boundParams[pindex-1].pA2=0;
                        }
                        if (boundParams[pindex-1].pB1!=0)
                        {
                                OdbcApi.ReleaseStoredBytes (boundParams[pindex-1].pB1, boundParams[pindex-1].pB2);
                                boundParams[pindex-1].pB1=0;
                                boundParams[pindex-1].pB2=0;
                        }
                        if (boundParams[pindex-1].pC1!=0)
                        {
                                OdbcApi.ReleaseStoredBytes (boundParams[pindex-1].pC1, boundParams[pindex-1].pC2);
                                boundParams[pindex-1].pC1=0;
                                boundParams[pindex-1].pC2=0;
                        }
                        if (boundParams[pindex-1].pS1!=0)
                        {
                                OdbcApi.ReleaseStoredChars (boundParams[pindex-1].pS1, boundParams[pindex-1].pS2);
                                boundParams[pindex-1].pS1=0;
                                boundParams[pindex-1].pS2=0;
                        }
                }
        }
        catch (NullPointerException npx)
        {
                //Do nothing
        }
}//FreeParams

    //----------------------------------------------------------------
    // JDBC 3.0 API Changes
    //----------------------------------------------------------------


    public java.net.URL getURL(int parameterIndex) throws SQLException  {
        throw new UnsupportedOperationException();
    }

    public void setURL(String parameterName, java.net.URL val) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setNull(String parameterName, int sqlType) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setBoolean(String parameterName, boolean x) throws SQLException {
        throw new UnsupportedOperationException();
    }



    public void setByte(String parameterName, byte x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setShort(String parameterName, short x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setInt(String parameterName, int x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setLong(String parameterName, long x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setFloat(String parameterName, float x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setDouble(String parameterName, double x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setString(String parameterName, String x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setBytes(String parameterName, byte x[]) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setDate(String parameterName, java.sql.Date x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setTime(String parameterName, java.sql.Time x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setTimestamp(String parameterName, java.sql.Timestamp x)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setAsciiStream(String parameterName, java.io.InputStream x, int length)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setBinaryStream(String parameterName, java.io.InputStream x,
                         int length) throws SQLException {
       throw new UnsupportedOperationException();
    }

    public void setObject(String parameterName, Object x, int targetSqlType, int scale)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setObject(String parameterName, Object x, int targetSqlType)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setObject(String parameterName, Object x) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setCharacterStream(String parameterName,
                            java.io.Reader reader,
                            int length) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setDate(String parameterName, java.sql.Date x, Calendar cal)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setTime(String parameterName, java.sql.Time x, Calendar cal)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setTimestamp(String parameterName, java.sql.Timestamp x, Calendar cal)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void setNull (String parameterName, int sqlType, String typeName)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void registerOutParameter(String parameterName, int sqlType)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void registerOutParameter(String parameterName, int sqlType, int scale)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public void registerOutParameter (String parameterName, int sqlType, String typeName)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public String getString(String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public boolean getBoolean(String parameterName) throws SQLException{
        throw new UnsupportedOperationException();
    }

    public byte getByte(String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public short getShort(String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public int getInt(String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public long getLong(String parameterString) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public float getFloat(String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public double getDouble(String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public byte[] getBytes(String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public java.sql.Date getDate(String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public java.sql.Time getTime(String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public java.sql.Timestamp getTimestamp(String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Object getObject(String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Object  getObject (String parameterName, java.util.Map map) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Ref getRef (String ParameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Blob getBlob (String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Clob getClob (String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public Array getArray (String parameterName) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public java.sql.Date getDate(String parameterName, Calendar cal)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public java.sql.Time getTime(String parameterName, Calendar cal)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public java.sql.Timestamp getTimestamp(String parameterName, Calendar cal)
        throws SQLException {
        throw new UnsupportedOperationException();
    }

    public java.net.URL getURL(String parameterName)
        throws SQLException {
        throw new UnsupportedOperationException();
    }


    //====================================================================
    // Data attributes
    //====================================================================

    private boolean lastParameterNull;  // true if the last parameter
    //  referenced by a getXXX
    //  function was null

}
