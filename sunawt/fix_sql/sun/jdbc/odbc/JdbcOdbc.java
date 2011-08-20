/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)JdbcOdbc.java    @(#)JdbcOdbc.java       1.52 03/01/23
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

//----------------------------------------------------------------------------
//
// Module:      JdbcOdbc.java
//
// Description: Defines the JdbcOdbc class.  This class provides a java
//              interface access to the ODBC API routines
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

import java.math.BigDecimal;
import java.util.*;
import java.sql.*;
import sun.io.*;

public class JdbcOdbc
        extends JdbcOdbcObject
{
        //4524683
        public static void addHstmt(long hStmt, long hDbc) {
                hstmtMap.put(new Long(hStmt), new Long(hDbc));
        }

        //====================================================================
        // Public methods
        //====================================================================

        //--------------------------------------------------------------------
        // Constructor
        // Perform any initialization, including loading the jdbcodbc
        // bridge library
        //--------------------------------------------------------------------

        JdbcOdbc (JdbcOdbcTracer tracer, String prefix) throws SQLException
        {
                // RFE 4641013.
                this.tracer = tracer;

                try {

                        if (tracer.isTracing ()) {
                                java.util.Date curDate = new java.util.Date ();

                                String s = "";
                                int minor = MinorVersion;

                                // Format the minor version to have 4 digits,
                                // with leading 0's if necessary

                                if (minor < 1000) s += "0";
                                if (minor < 100)  s += "0";
                                if (minor < 10)   s += "0";
                                s += "" + minor;

                                tracer.trace ("JDBC to ODBC Bridge " + MajorVersion + "." + s);
                                tracer.trace ("Current Date/Time: " + curDate.toString ());
                                tracer.trace ("Loading " + prefix + "JdbcOdbc library");
                        }
                        java.security.AccessController.doPrivileged(
                            new sun.security.action.LoadLibraryAction (prefix + "JdbcOdbc"));

                        //4524683
                        if(hstmtMap == null) {
                                hstmtMap = Collections.synchronizedMap(new java.util.HashMap());
                        }

                } catch (UnsatisfiedLinkError e) {

                        if (tracer.isTracing ()) {
                                tracer.trace ("Unable to load " + prefix + "JdbcOdbc library");
                        }

                        throw new SQLException (
                                "Unable to load " + prefix + "JdbcOdbc library");
                }

        }

        //--------------------------------------------------------------------
        // Public java methods (wrappers around native C calls)
        //--------------------------------------------------------------------

        //--------------------------------------------------------------------
        // SQLAllocConnect
        //--------------------------------------------------------------------

        public  long SQLAllocConnect (
                long hEnv)
                throws SQLException
        {
                long    hDbc = 0;
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Allocating Connection handle (SQLAllocConnect)");
                }

                errorCode = new byte[1];
                hDbc = allocConnect (hEnv, errorCode);

                if (errorCode[0] != 0) {
                        throwGenericSQLException ();
                }
                else {
                        if (tracer.isTracing ()) {
                                tracer.trace ("hDbc=" + hDbc);
                        }
                }
                return hDbc;
        }

        //--------------------------------------------------------------------
        // SQLAllocEnv
        //--------------------------------------------------------------------

        public  long SQLAllocEnv ()
                throws SQLException
        {
                long    hEnv = 0;
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Allocating Environment handle (SQLAllocEnv)");
                }

                errorCode = new byte[1];
                hEnv = allocEnv (errorCode);

                if (errorCode[0] != 0) {
                        throwGenericSQLException ();
                }
                else {
                        if (tracer.isTracing ()) {
                                tracer.trace ("hEnv=" + hEnv);
                        }
                }
                return hEnv;
        }

        //--------------------------------------------------------------------
        // SQLAllocStmt
        //--------------------------------------------------------------------

        public  long SQLAllocStmt (
                        long hDbc)
                        throws SQLException
        {
                long    hStmt = 0;
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Allocating Statement Handle (SQLAllocStmt), hDbc=" + hDbc);
                }

                errorCode = new byte[1];
                hStmt = allocStmt (hDbc, errorCode);

                if (errorCode[0] != 0) {
                        throwGenericSQLException ();
                }
                else {
                        if (tracer.isTracing ()) {
                                tracer.trace ("hStmt=" + hStmt);
                        }
                }
                addHstmt(hStmt, hDbc); //4524683
                return hStmt;
        }

        //--------------------------------------------------------------------
        // SQLBindColAtExec
        //--------------------------------------------------------------------
        public  void SQLBindColAtExec (
                long hStmt,
                int icol,
                int SQLtype,
                //4691886
                byte[] lenInd,
                byte[] dataBuf,
                long[] buffers)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding Column DATA_AT_EXEC (SQLBindCol), hStmt=" + hStmt + ", icol=" + icol + ", SQLtype=" + SQLtype);
                }

                errorCode = new byte[1];

                bindColAtExec (hStmt, icol, SQLtype, lenInd, dataBuf, buffers, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                       OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindColBinary
        //--------------------------------------------------------------------

        public  void SQLBindColBinary (
                long hStmt,
                int icol,
                Object[] values,
                //4691886
                byte[] lenInd,
                int descLen,
                byte[] dataBuf,
                long[] buffers)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Bind column binary (SQLBindColBinary), hStmt=" + hStmt + ", icol=" + icol);
                }

                errorCode = new byte[1];

                bindColBinary (hStmt, icol, values, lenInd, descLen, dataBuf, buffers, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                       OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindColDate
        //--------------------------------------------------------------------

        public  void SQLBindColDate (
                long hStmt,
                int icol,
                Object[] value,
                //4691886
                byte[] lenInd,
                byte[] dataBuf,
                long[] buffers)
                throws SQLException
        {
                if (tracer.isTracing ()) {
                        tracer.trace ("Bound Column Date (SQLBindColDate), hStmt=" + hStmt + ", icol=" + icol);
                }

                byte    errorCode[];

                java.sql.Date currDate = null;

                int arraySize = value.length;

                int yrs[] = new int[arraySize];
                int mth[] = new int[arraySize];
                int dts[] = new int[arraySize];

                errorCode = new byte[1];

                Calendar cal = Calendar.getInstance ();

                for ( int i = 0; i < arraySize; i++)
                {
                    if (value[i] != null)
                    {
                        currDate = (java.sql.Date)value[i];

                        cal.setTime (currDate);

                        yrs[i] = cal.get (Calendar.YEAR);
                        mth[i] = cal.get (Calendar.MONTH) + 1;
                        dts[i] = cal.get (Calendar.DATE);
                    }

                }

                bindColDate (hStmt, icol,
                                yrs,
                                mth,
                                dts,
                                lenInd,
                                dataBuf,
                                buffers,
                                errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }


        //--------------------------------------------------------------------
        // SQLBindColDefault
        //--------------------------------------------------------------------

        public  void SQLBindColDefault (
                long hStmt,
                int ipar,
                byte rgbValue[],
                byte pcbValue[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding default column (SQLBindCol), hStmt=" + hStmt + ", ipar=" + ipar + ",                     length=" + rgbValue.length);
                }

                errorCode = new byte[1];
                bindColDefault (hStmt, ipar, rgbValue, pcbValue, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                       OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindColDouble
        //--------------------------------------------------------------------

        public  void SQLBindColDouble (
                long hStmt,
                int icol,
                Object[] value,
                //4691886
                byte[] lenInd,
                byte[] dataBuf,
                long[] buffers)
                throws SQLException
        {
                if (tracer.isTracing ()) {
                        tracer.trace ("Bind column Double (SQLBindColDouble), hStmt=" + hStmt + ", icol=" + icol);
                }

                byte    errorCode[];

                double[] newDataSet = new double[value.length];

                for (int i = 0; i < value.length; i++)
                {
                    if (value[i] != null)
                        newDataSet[i] = ((Double) value[i]).doubleValue();
                }

                errorCode = new byte[1];

                bindColDouble (hStmt, icol, newDataSet, lenInd, dataBuf, buffers, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                       OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindColFloat
        //--------------------------------------------------------------------

        public  void SQLBindColFloat (
                long hStmt,
                int icol,
                Object[] values,
                //4691886
                byte[] lenInd,
                byte[] dataBuf,
                long[] buffers)
                throws SQLException
        {
                byte    errorCode[];
                byte    cpyBuf[];

                float[] newDataSet = new float[values.length];

                for (int i = 0; i < values.length; i++)
                {
                    if (values[i] != null)
                        newDataSet[i] = ((Float) values[i]).floatValue();
                }


                if (tracer.isTracing ()) {
                        tracer.trace ("Binding default column (SQLBindCol Float), hStmt=" + hStmt + ", icol=" + icol);
                }

                errorCode = new byte[1];
                bindColFloat (hStmt, icol, newDataSet, lenInd, dataBuf, buffers, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                       OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }
        //--------------------------------------------------------------------
        // SQLBindColInteger
        //--------------------------------------------------------------------

        public  void SQLBindColInteger (
                long hStmt,
                int icol,
                Object[] values,
                //4691886
                byte[] lenInd,
                byte[] dataBuf,
                long[] buffers)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding default column (SQLBindCol Integer), hStmt=" + hStmt + ", icol=" + icol);
                }

                int[] newDataSet = new int[values.length];

                for (int i = 0; i < values.length; i++)
                {
                    if (values[i] != null)
                        newDataSet[i] = ((Integer) values[i]).intValue();
                }

                errorCode = new byte[1];

                bindColInteger (hStmt, icol, newDataSet, lenInd, dataBuf, buffers, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                       OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindColString
        //--------------------------------------------------------------------

        public  void SQLBindColString (
                long hStmt,
                int icol,
                int SQLtype,
                Object[] values,
                int descLen,
                //4691886
                byte[] lenInd,
                byte[] dataBuf,
                long[] buffers)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding string/decimal Column (SQLBindColString), hStmt=" +
                               hStmt + ", icol=" + icol + ", SQLtype=" + SQLtype +
                               ", rgbValue=" + values);
                }

                errorCode = new byte[1];

                bindColString (hStmt, icol, OdbcDef.jdbcTypeToOdbc (SQLtype),
                               values, descLen, lenInd, dataBuf, buffers, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindColTime
        //--------------------------------------------------------------------

        public  void SQLBindColTime (
                long hStmt,
                int icol,
                Object[] value,
                //4691886
                byte[] lenInd,
                byte[] dataBuf,
                long[] buffers)
                throws SQLException
        {
                if (tracer.isTracing ()) {
                        tracer.trace ("Bind column Time (SQLBindColTime), hStmt=" + hStmt + ", icol=" + icol);
                }

                byte    errorCode[];

                Calendar cal;

                java.sql.Time currTime = null;

                int arraySize = value.length;

                int hrs[] = new int[arraySize];
                int min[] = new int[arraySize];
                int sec[] = new int[arraySize];

                errorCode = new byte[1];

                cal = Calendar.getInstance ();

                for ( int i = 0; i < arraySize; i++)
                {
                    if (value[i] != null)
                    {
                        currTime = (java.sql.Time)value[i];

                        cal.setTime (currTime);

                        hrs[i] = cal.get (Calendar.HOUR_OF_DAY);
                        min[i] = cal.get (Calendar.MINUTE);
                        sec[i] = cal.get (Calendar.SECOND);
                    }
                }

                bindColTime (hStmt, icol,
                                 hrs,
                                 min,
                                 sec,
                                 lenInd,
                                 dataBuf,
                                 buffers,
                                 errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindColTimestamp
        //--------------------------------------------------------------------

        public  void SQLBindColTimestamp (
                long hStmt,
                int icol,
                Object[] value,
                //4691886
                byte[] lenInd,
                byte[] dataBuf,
                long[] buffers)
                throws SQLException
        {

                if (tracer.isTracing ()) {
                        tracer.trace ("Bind Column Timestamp (SQLBindColTimestamp), hStmt=" + hStmt + ", icol=" + icol);
                }

                byte    errorCode[];

                Calendar cal;

                java.sql.Timestamp currTime = null;

                int arraySize = value.length;

                int yrs[] = new int[arraySize];
                int mth[] = new int[arraySize];
                int dts[] = new int[arraySize];
                int hrs[] = new int[arraySize];
                int min[] = new int[arraySize];
                int sec[] = new int[arraySize];
                int nan[] = new int[arraySize];

                errorCode = new byte[1];

                cal = Calendar.getInstance ();

                for ( int i = 0; i < arraySize; i++)
                {
                    if (value[i] != null)
                    {
                        currTime = (java.sql.Timestamp)value[i];

                        cal.setTime (currTime);

                        yrs[i] = cal.get (Calendar.YEAR);
                        mth[i] = cal.get (Calendar.MONTH) + 1;
                        dts[i] = cal.get (Calendar.DATE);
                        hrs[i] = cal.get (Calendar.HOUR_OF_DAY);
                        min[i] = cal.get (Calendar.MINUTE);
                        sec[i] = cal.get (Calendar.SECOND);
                        nan[i] = currTime.getNanos ();
                    }
                }

                bindColTimestamp (hStmt, icol,
                                          yrs,
                                          mth,
                                          dts,
                                          hrs,
                                          min,
                                          sec,
                                          nan,
                                          lenInd, dataBuf, buffers, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }


        //--------------------------------------------------------------------
        // SQLBindInParameterAtExec
        //--------------------------------------------------------------------

        public  void SQLBindInParameterAtExec (
                long hStmt,
                int ipar,
                int SQLtype,
                int len,
                byte dataBuf[],
                byte lenBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding DATA_AT_EXEC parameter (SQLBindParameter), hStmt=" + hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype + ", len=" + len);
                }

                errorCode = new byte[1];
                bindInParameterAtExec (hStmt, ipar, SQLtype, len, dataBuf,
                                       lenBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                       OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        // 4641016
        //--------------------------------------------------------------------
        // SQLBindInOutParameterAtExec
        //--------------------------------------------------------------------

        public  void SQLBindInOutParameterAtExec (
                long hStmt,
                int ipar,
                int CType,
                int SQLtype,
                int dataBufLen,
                byte dataBuf[],
                int streamLength,
                byte lenBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding DATA_AT_EXEC parameter (SQLBindParameter), hStmt=" + hStmt +
                                ", ipar=" + ipar + ", SQLtype=" + SQLtype + ", streamLength = " + streamLength +
                                        " ,dataBufLen = " + dataBufLen);
                }

                errorCode = new byte[1];
                bindInOutParameterAtExec (hStmt, ipar, CType, SQLtype, dataBufLen, dataBuf,
                                       streamLength, lenBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                       OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInParameterBinary
        //--------------------------------------------------------------------

        public  void SQLBindInParameterBinary (
                long hStmt,
                int ipar,
                int SQLtype,
                byte value[],
                byte dataBuf[],
                byte lenBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                // Since we don't know the precision of the column that
                // we are about to bind, we will set it to this arbitrary
                // value. (it was DEFAULT_IN_PRECISION, But some databases
                // could return an "invalid precision" error)

                int     precision = 0;

                // Since we don't know the limits of the column size
                // we assume the data's length to be less or at least
                // equal to the columns max_length. if the data length
                // is > precision allowed. Then this is not our problem.
                // But we try stay within our limits.

                if (dataBuf.length < JdbcOdbcLimits.DEFAULT_IN_PRECISION)
                        precision = dataBuf.length;
                else
                        precision = JdbcOdbcLimits.DEFAULT_IN_PRECISION;

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN binary parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype);
//                      dumpByte (value, value.length);
                }

                errorCode = new byte[1];
                bindInParameterBinary (hStmt, ipar,
                                       OdbcDef.jdbcTypeToOdbc (SQLtype),
                                       value, precision, dataBuf, lenBuf,
                                       errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                       OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInParameterDate
        //--------------------------------------------------------------------

        public  void SQLBindInParameterDate (
                long hStmt,
                int ipar,
                java.sql.Date value,
                byte dataBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter date (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", rgbValue=" + value.toString ());
                }

                errorCode = new byte[1];

                Calendar cal = Calendar.getInstance ();
                cal.setTime (value);

                bindInParameterDate (hStmt, ipar,
                                     cal.get (Calendar.YEAR),
                                     cal.get (Calendar.MONTH) + 1,
                                     cal.get (Calendar.DATE),
                                     dataBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                       OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInParameterCalendarDate
        //--------------------------------------------------------------------

        public  void SQLBindInParameterCalendarDate (
                long hStmt,
                int ipar,
                Calendar cal,
                byte dataBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter date (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", rgbValue=" + cal.toString ());
                }

                errorCode = new byte[1];

                bindInParameterDate (hStmt, ipar,
                                     cal.get (Calendar.YEAR),
                                     cal.get (Calendar.MONTH) + 1,
                                     cal.get (Calendar.DATE),
                                     dataBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                       OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }


        //--------------------------------------------------------------------
        // SQLBindInParameterDouble
        //--------------------------------------------------------------------

        public  void SQLBindInParameterDouble (
                long hStmt,
                int ipar,
                int SQLtype,
                int scale,
                double value,
                byte dataBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter double (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype + ", scale=" +
                               scale + ", rgbValue=" + value);
                }

                errorCode = new byte[1];
                bindInParameterDouble (hStmt, ipar,
                                       OdbcDef.jdbcTypeToOdbc (SQLtype),
                                       scale, value, dataBuf,
                                       errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                       OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInParameterFloat
        //--------------------------------------------------------------------

        public  void SQLBindInParameterFloat (
                long hStmt,
                int ipar,
                int SQLtype,
                int scale,
                float value,
                byte dataBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter float (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype + ", scale=" +
                               scale + ", rgbValue=" + value);
                }

                errorCode = new byte[1];
                // Fix 4532167.
                bindInParameterFloat (hStmt, ipar,
                                OdbcDef.jdbcTypeToOdbc (SQLtype),
                                scale, (double)value, dataBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInParameterInteger
        //--------------------------------------------------------------------

        public  void SQLBindInParameterInteger (
                long hStmt,
                int ipar,
                int SQLtype,
                int value,
                byte dataBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter integer (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + "SQLtype=" + SQLtype + ", rgbValue=" + value);
                }

                errorCode = new byte[1];
                bindInParameterInteger (hStmt, ipar,
                                OdbcDef.jdbcTypeToOdbc (SQLtype),
                                value, dataBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInParameterNull
        //--------------------------------------------------------------------

        public  void SQLBindInParameterNull (
                long hStmt,
                int ipar,
                int SQLtype,
                int prec,
                int scale,
                byte lenBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN NULL parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype);
                }

                errorCode = new byte[1];
                bindInParameterNull (hStmt, ipar, OdbcDef.jdbcTypeToOdbc (SQLtype), prec, scale, lenBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInParameterString
        //--------------------------------------------------------------------

        public  void SQLBindInParameterString (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                int scale,
                byte value[],
                byte dataBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN string parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype +
                               ", precision=" + precision + ", scale=" + scale +
                               ", rgbValue=" + value);
                }

                errorCode = new byte[1];
                bindInParameterString (hStmt, ipar,
                                       OdbcDef.jdbcTypeToOdbc (SQLtype),
                                       value, precision, scale, dataBuf,
                                       errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInParameterTime
        //--------------------------------------------------------------------

        public  void SQLBindInParameterTime (
                long hStmt,
                int ipar,
                java.sql.Time value,
                byte dataBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter time (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", rgbValue=" + value.toString ());
                }

                errorCode = new byte[1];

                Calendar cal = Calendar.getInstance ();
                cal.setTime (value);

                bindInParameterTime (hStmt, ipar,
                                     cal.get (Calendar.HOUR_OF_DAY),
                                     cal.get (Calendar.MINUTE),
                                     cal.get (Calendar.SECOND),
                                     dataBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInParameterCalendarTime
        //--------------------------------------------------------------------

        public  void SQLBindInParameterCalendarTime (
                long hStmt,
                int ipar,
                Calendar cal,
                byte dataBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter time (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", rgbValue=" + cal.toString ());
                }

                errorCode = new byte[1];


                bindInParameterTime (hStmt, ipar,
                                     cal.get (Calendar.HOUR_OF_DAY),
                                     cal.get (Calendar.MINUTE),
                                     cal.get (Calendar.SECOND),
                                     dataBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }


        //--------------------------------------------------------------------
        // SQLBindInParameterTimestamp
        //--------------------------------------------------------------------

        public  void SQLBindInParameterTimestamp (
                long hStmt,
                int ipar,
                java.sql.Timestamp value,
                byte dataBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter timestamp (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", rgbValue=" + value.toString ());
                }

                errorCode = new byte[1];

                Calendar cal = Calendar.getInstance ();
                cal.setTime (value);

                bindInParameterTimestamp (hStmt, ipar,
                                          cal.get (Calendar.YEAR),
                                          cal.get (Calendar.MONTH) + 1,
                                          cal.get (Calendar.DATE),
                                          cal.get (Calendar.HOUR_OF_DAY),
                                          cal.get (Calendar.MINUTE),
                                          cal.get (Calendar.SECOND),
                                          value.getNanos (),
                                          dataBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInParameterCalendarTimestamp
        //--------------------------------------------------------------------

        public  void SQLBindInParameterCalendarTimestamp (
                long hStmt,
                int ipar,
                Calendar cal,
                byte dataBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter timestamp (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", rgbValue=" + cal.toString ());
                }

                errorCode = new byte[1];

                // Fix 4238983. Get the nanos from millisecond.
                int nanos = (cal.get (Calendar.MILLISECOND)) * 1000000;

                bindInParameterTimestamp (hStmt, ipar,
                                          cal.get (Calendar.YEAR),
                                          cal.get (Calendar.MONTH) + 1,
                                          cal.get (Calendar.DATE),
                                          cal.get (Calendar.HOUR_OF_DAY),
                                          cal.get (Calendar.MINUTE),
                                          cal.get (Calendar.SECOND),
                                          nanos,
                                          dataBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //Bug 4495452
        //-------------------------------------------------------------------
        // SQLBindInParameterBigint
        //-------------------------------------------------------------------

        public  void SQLBindInParameterBigint (
                long hStmt,
                int ipar,
                int SQLtype,
                int scale,
                long value,
                byte dataBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];
                        if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter bigint (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype + ", scale=" +
                               scale + ", rgbValue=" + value);
                }

                errorCode = new byte[1];
                bindInParameterBigint (hStmt, ipar,
                                OdbcDef.jdbcTypeToOdbc (SQLtype),
                                scale, value, dataBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindOutParameterString
        //--------------------------------------------------------------------

        public  void SQLBindOutParameterString (
                long hStmt,
                int ipar,
                int SQLtype,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                long [] buffers)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding OUT string parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype +
                               ", prec=" + (dataBuf.length - 1) + ", scale=" + scale);
                }

                errorCode = new byte[1];
                bindOutParameterString (hStmt, ipar,
                                        OdbcDef.jdbcTypeToOdbc (SQLtype),
                                        scale, dataBuf, lenBuf,errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        // bug 4412437
        //--------------------------------------------------------------------
        // SQLBindInOutParameterDate
        //--------------------------------------------------------------------

        public  void SQLBindInOutParameterDate (
                        long hStmt,
                        int ipar,
                        int scale,
                        byte dataBuf[],
                        byte lenBuf[],
                        long [] buffers)
                        throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN OUT date parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar +
                               ", prec=" + (dataBuf.length - 1) + ", scale=" + scale);
                }

                errorCode = new byte[1];

                bindInOutParameterDate (hStmt, ipar, scale, dataBuf,
                                        lenBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        // bug 4412437
        //--------------------------------------------------------------------
        // SQLBindInOutParameterTime
        //--------------------------------------------------------------------

        public  void SQLBindInOutParameterTime (
                        long hStmt,
                        int ipar,
                        int scale,
                        byte dataBuf[],
                        byte lenBuf[],
                        long [] buffers)
                        throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN OUT time parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar +
                               ", prec=" + (dataBuf.length - 1) + ", scale=" + scale);
                }

                errorCode = new byte[1];

                bindInOutParameterTime (hStmt, ipar, scale, dataBuf,
                                        lenBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }
        //4532162
        //--------------------------------------------------------------------
        // SQLBindInOutParameterTimestamp
        //--------------------------------------------------------------------

        public  void SQLBindInOutParameterTimestamp (
                        long hStmt,
                        int ipar,
                        int precision,
                        int scale,
                        byte dataBuf[],
                        byte lenBuf[],
                        long [] buffers)
                        throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN OUT time parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", scale=" + scale +"length = "+
                               (dataBuf.length - 1) + ", precision=" + precision);
                }

                errorCode = new byte[1];

                bindInOutParameterTimestamp (hStmt, ipar, precision, scale, dataBuf,
                                                        lenBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }


        //--------------------------------------------------------------------
        // SQLBindInOutParameterString
        //--------------------------------------------------------------------

        public  void SQLBindInOutParameterString (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding INOUT string parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype +
                               ", precision=" + precision + ", scale=" + scale +
                               ", rgbValue=" + dataBuf + ", lenBuf=" + lenBuf);
                }

                errorCode = new byte[1];
                bindInOutParameterString (hStmt, ipar,
                                       OdbcDef.jdbcTypeToOdbc (SQLtype),
                                       precision, scale, dataBuf, lenBuf,
                                       errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        // bug 4412437
        //--------------------------------------------------------------------
        // SQLBindInOutParameterStr
        //--------------------------------------------------------------------

        /**
          The following function takes care of the data types CHAR and VARCHAR. The difference between this
          function and SQLBindInOutParameterString is the parameter strLenInd.
          StrLenInd is the buffer in which the native function SQLBindParameter is passed the length of the input
          parameter string or SQL_NTS and in which the length of the string returned from the called procedure is returned.
         */

        public  void SQLBindInOutParameterStr (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                byte dataBuf[],
                byte lenBuf[],
                long buffers[],
                int strLenInd)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding INOUT string parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype +
                               ", precision=" + precision +
                               ", rgbValue=" + dataBuf + ", lenBuf=" + lenBuf);
                }

                errorCode = new byte[1];
                bindInOutParameterStr (hStmt, ipar,
                                       OdbcDef.jdbcTypeToOdbc (SQLtype),
                                       precision, dataBuf, lenBuf,
                                       errorCode, buffers, strLenInd);


                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        // bug 4412437
        //--------------------------------------------------------------------
        // SQLBindInOutParameterBin
        //--------------------------------------------------------------------

        /**
          The following function takes care of the data types BINARY and VARBINARY. The difference between this
          function and SQLBindInOutParameterBinary is the parameter strLenInd.
          StrLenInd is the buffer in which the function native function SQLBindParameter is passed the length of the
          input binary parameter and in which the length of the binary string returned from the called procedure
          is returned.
         */

        public  void SQLBindInOutParameterBin (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                byte dataBuf[],
                byte lenBuf[],
                long buffers[],
                int strLenInd)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding INOUT binary parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype +
                               ", precision=" + precision +
                               ", rgbValue=" + dataBuf + ", lenBuf=" + lenBuf);
                }

                errorCode = new byte[1];
                bindInOutParameterBin (hStmt, ipar,
                                       OdbcDef.jdbcTypeToOdbc (SQLtype),
                                       precision, dataBuf, lenBuf,
                                       errorCode, buffers, strLenInd);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }


        //--------------------------------------------------------------------
        // SQLBindInOutParameterBinary
        //--------------------------------------------------------------------

        public  void SQLBindInOutParameterBinary (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding INOUT binary parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype +
                               ", precision=" + precision + ", scale=" + scale +
                               ", rgbValue=" + dataBuf + ", lenBuf=" + lenBuf);
                }

                errorCode = new byte[1];
                bindInOutParameterBinary (hStmt, ipar,
                                       OdbcDef.jdbcTypeToOdbc (SQLtype),
                                       precision, scale, dataBuf, lenBuf,
                                       errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        // bug 4412437
        //--------------------------------------------------------------------
        // SQLBindInOutParameterFixed
        //--------------------------------------------------------------------

        /**
          The following function takes care of the data types - BIT, DOUBLE, FLOAT, INTEGER, REAL, SMALLINT, TINYINT.
          Proper odbc c data types and sql data types while binding the parameters using SQLBindParameter.
          The function jdbcTypeToCType returns the odbc c data type given the jdbc type.
         */

        public  void SQLBindInOutParameterFixed (
                long hStmt,
                int ipar,
                int SQLtype,
                int maxLen,
                byte dataBuf[],
                byte lenBuf[],
                long buffers[])
                throws SQLException // the value is assumed to be already present in dataBuf
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN OUT parameter for fixed types (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + "SQLtype=" + SQLtype + ", maxLen=" + maxLen);
                }

                errorCode = new byte[1];
                bindInOutParameterFixed (hStmt, ipar, OdbcDef.jdbcTypeToCType(SQLtype),
                                OdbcDef.jdbcTypeToOdbc (SQLtype), maxLen,
                                dataBuf, lenBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }



        //--------------------------------------------------------------------
        // SQLBindInOutParameterTimeStamp
        //--------------------------------------------------------------------

        public  void SQLBindInOutParameterTimeStamp (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding INOUT string parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype +
                               ", precision=" + precision + ", scale=" + scale +
                               ", rgbValue=" + dataBuf + ", lenBuf=" + lenBuf);
                }

                errorCode = new byte[1];
                bindInOutParameterTimeStamp (hStmt, ipar,
                                       OdbcDef.jdbcTypeToOdbc (SQLtype),
                                       precision, scale, dataBuf, lenBuf,
                                       errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }
        //--------------------------------------------------------------------
        // SQLBindInOutParameter
        //--------------------------------------------------------------------

        public  void SQLBindInOutParameter (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                int scale,
                double value,
                byte dataBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding INOUT parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype +
                               ", precision=" + precision + ", scale=" + scale +
                               ", rgbValue=" + value);
                }

                errorCode = new byte[1];
                bindInOutParameter (hStmt, ipar,
                                       OdbcDef.jdbcTypeToOdbc (SQLtype),
                                       precision, scale, value, dataBuf,
                                       errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInOutParameterNull
        //--------------------------------------------------------------------

        public  void SQLBindInOutParameterNull (
                long hStmt,
                int ipar,
                int SQLtype,
                int prec,
                int scale,
                byte lenBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN OUT NULL parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype);
                }

                errorCode = new byte[1];
                bindInOutParameterNull (hStmt, ipar, OdbcDef.jdbcTypeToOdbc (SQLtype), prec, scale, lenBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInParameterStringArray
        //--------------------------------------------------------------------

        public  void SQLBindInParameterStringArray (
                long hStmt,
                int ipar,
                int SQLtype,
                Object[] dataBuf,
                int colDesc,
                int colScale,
                int[] buffers)
                throws SQLException
        {

                int setValues = dataBuf.length;

                Object[] stringArray = new Object[setValues];

                if ( (SQLtype == Types.NUMERIC) || (SQLtype == Types.DECIMAL) )
                {

                        for (int i = 0; i < setValues; i++)
                        {
                            if (dataBuf[i] != null)
                            {

                                BigDecimal decObj = (BigDecimal)dataBuf[i];

                                String strObj = decObj.toString();

                                // This is to prevent the user from having to pad
                                // a decimal value with zeros to match the scale length.

                                int fixscale = strObj.indexOf('.');

                                if (fixscale != -1)
                                {
                                    String tempStrObj = strObj.substring(fixscale + 1, strObj.length());

                                    int decLen = tempStrObj.length();

                                    if (decLen < colScale)
                                    {
                                        for (int j = 0; j < (colScale - decLen); j++)
                                        {
                                            strObj+= "0";
                                        }
                                    }

                                }

                                stringArray[i] = (String)strObj;
                            }
                        }

                }
                else stringArray = dataBuf;


                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter timestamp (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar);
                }

                byte    errorCode[];
                byte    strBuf[];

                errorCode = new byte[1];
                strBuf = new byte[(colDesc + 1) * setValues];

                bindInParameterStringArray (hStmt, ipar,
                                            OdbcDef.jdbcTypeToOdbc (SQLtype),
                                            stringArray, strBuf, colDesc,
                                            colScale, buffers, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }

        }

        //--------------------------------------------------------------------
        // SQLBindInParameterIntegerArray
        //--------------------------------------------------------------------

        public  void SQLBindInParameterIntegerArray (
                long hStmt,
                int ipar,
                int SQLtype,
                Object[] dataBuf,
                int[] buffers)
                throws SQLException
        {

                int[] newDataSet = new int[dataBuf.length];


                for (int i = 0; i < dataBuf.length; i++)
                {
                    if (dataBuf[i] != null)
                            newDataSet[i] = ((Integer) dataBuf[i]).intValue();
                }


                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter Integer Array (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar);
                }

                errorCode = new byte[1];

                bindInParameterIntegerArray (hStmt, ipar, OdbcDef.jdbcTypeToOdbc (SQLtype),
                                             newDataSet, buffers, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }

        }

        //--------------------------------------------------------------------
        // SQLBindInParameterFloatArray
        //--------------------------------------------------------------------

        public  void SQLBindInParameterFloatArray (
                long hStmt,
                int ipar,
                int SQLtype,
                Object[] dataBuf,
                int[] buffers)
                throws SQLException
        {

                float[] newDataSet = new float[dataBuf.length];

                for (int i = 0; i < dataBuf.length; i++)
                {
                    if (dataBuf[i] != null)
                        newDataSet[i] = ((Float) dataBuf[i]).floatValue();
                }

                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter timestamp (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar);
                }

                errorCode = new byte[1];

                bindInParameterFloatArray (hStmt, ipar, OdbcDef.jdbcTypeToOdbc (SQLtype),
                                           0, newDataSet, buffers, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }

        }

        //--------------------------------------------------------------------
        // SQLBindInParameterDoubleArray
        //--------------------------------------------------------------------

        public  void SQLBindInParameterDoubleArray (
                long hStmt,
                int ipar,
                int SQLtype,
                Object[] dataBuf,
                int[] buffers)
                throws SQLException
        {

                double[] newDataSet = new double[dataBuf.length];

                for (int i = 0; i < dataBuf.length; i++)
                {
                    if (dataBuf[i] != null)
                        newDataSet[i] = ((Double) dataBuf[i]).doubleValue();
                }

                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter timestamp (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar);
                }

                errorCode = new byte[1];

                bindInParameterDoubleArray (hStmt, ipar, OdbcDef.jdbcTypeToOdbc (SQLtype),
                                            0, newDataSet, buffers, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }

        }

        //--------------------------------------------------------------------
        // SQLBindInParameterDateArray
        //--------------------------------------------------------------------

        public  void SQLBindInParameterDateArray (
                long hStmt,
                int ipar,
                Object value[],
                int buffers[])
                throws SQLException
        {

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter timestamp (SQLBindParameterDateArray), hStmt=" +
                               hStmt + ", ipar=" + ipar);
                }

                int arraySize = value.length;

                int yrs[] = new int[arraySize];
                int mth[] = new int[arraySize];
                int dts[] = new int[arraySize];

                byte    errorCode[];
                byte    dataBuf[];

                errorCode = new byte[1];
                dataBuf = new byte[(10 + 1) * arraySize];

                Calendar cal;

                if ((java.sql.Date) value[0] != null)
                {

                    cal = Calendar.getInstance ();

                    java.sql.Date currDate = null;

                    for ( int i = 0; i < arraySize; i++)
                    {
                        if (value[i] != null)
                        {
                            currDate = (java.sql.Date)value[i];

                            cal.setTime (currDate);

                            yrs[i] = cal.get (Calendar.YEAR);
                            mth[i] = cal.get (Calendar.MONTH) + 1;
                            dts[i] = cal.get (Calendar.DATE);
                        }

                    }


                }
                else
                {

                    for ( int i = 0; i < arraySize; i++)
                    {
                        if (value[i] != null)
                        {
                            cal = (Calendar)value[i];

                            yrs[i] = cal.get (Calendar.YEAR);
                            mth[i] = cal.get (Calendar.MONTH) + 1;
                            dts[i] = cal.get (Calendar.DATE);
                        }

                    }

                }


                bindInParameterDateArray (hStmt, ipar,
                                          yrs,
                                          mth,
                                          dts,
                                          dataBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInParameterTimeArray
        //--------------------------------------------------------------------

        public  void SQLBindInParameterTimeArray (
                long hStmt,
                int ipar,
                Object value[],
                int buffers[])
                throws SQLException
        {

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter timestamp (SQLBindParameterTimeArray), hStmt=" +
                               hStmt + ", ipar=" + ipar);
                }

                int arraySize = value.length;

                int hrs[] = new int[arraySize];
                int min[] = new int[arraySize];
                int sec[] = new int[arraySize];

                byte    errorCode[];
                byte    dataBuf[];

                errorCode = new byte[1];
                dataBuf = new byte[(8 + 1) * arraySize];

                Calendar cal;

                if ((java.sql.Time) value[0] != null)
                {
                    cal = Calendar.getInstance ();

                    java.sql.Time currTime = null;

                    for ( int i = 0; i < arraySize; i++)
                    {
                        if (value[i] != null)
                        {
                            currTime = (java.sql.Time)value[i];

                            cal.setTime (currTime);

                            hrs[i] = cal.get (Calendar.HOUR_OF_DAY);
                            min[i] = cal.get (Calendar.MINUTE);
                            sec[i] = cal.get (Calendar.SECOND);
                        }
                    }


                }
                else
                {

                    for ( int i = 0; i < arraySize; i++)
                    {
                        if (value[i] != null)
                        {
                            cal = (Calendar)value[i];

                            hrs[i] = cal.get (Calendar.HOUR_OF_DAY);
                            min[i] = cal.get (Calendar.MINUTE);
                            sec[i] = cal.get (Calendar.SECOND);
                        }
                    }

                }


                bindInParameterTimeArray (hStmt, ipar,
                                          hrs,
                                          min,
                                          sec,
                                          dataBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInParameterTimestampArray
        //--------------------------------------------------------------------

        public  void SQLBindInParameterTimestampArray (
                long hStmt,
                int ipar,
                Object value[],
                int buffers[])
                throws SQLException
        {

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN parameter timestamp (SQLBindParameterTimestampArray), hStmt=" +
                               hStmt + ", ipar=" + ipar);
                }

                int arraySize = value.length;

                int yrs[] = new int[arraySize];
                int mth[] = new int[arraySize];
                int dts[] = new int[arraySize];
                int hrs[] = new int[arraySize];
                int min[] = new int[arraySize];
                int sec[] = new int[arraySize];
                int nan[] = new int[arraySize];

                byte    errorCode[];
                byte    dataBuf[];

                errorCode = new byte[1];
                dataBuf = new byte[(29 + 1) * arraySize];

                Calendar cal;

                if ((java.sql.Timestamp) value[0] != null)
                {

                    cal = Calendar.getInstance ();

                    java.sql.Timestamp currTime = null;

                    for ( int i = 0; i < arraySize; i++)
                    {
                        if (value[i] != null)
                        {
                            currTime = (java.sql.Timestamp)value[i];

                            cal.setTime (currTime);

                            yrs[i] = cal.get (Calendar.YEAR);
                            mth[i] = cal.get (Calendar.MONTH) + 1;
                            dts[i] = cal.get (Calendar.DATE);
                            hrs[i] = cal.get (Calendar.HOUR_OF_DAY);
                            min[i] = cal.get (Calendar.MINUTE);
                            sec[i] = cal.get (Calendar.SECOND);
                            nan[i] = currTime.getNanos ();
                        }

                    }

                }
                else
                {

                    for ( int i = 0; i < arraySize; i++)
                    {
                        if (value[i] != null)
                        {
                            cal = (Calendar)value[i];

                            yrs[i] = cal.get (Calendar.YEAR);
                            mth[i] = cal.get (Calendar.MONTH) + 1;
                            dts[i] = cal.get (Calendar.DATE);
                            hrs[i] = cal.get (Calendar.HOUR_OF_DAY);
                            min[i] = cal.get (Calendar.MINUTE);
                            sec[i] = cal.get (Calendar.SECOND);
                            nan[i] = cal.get (Calendar.MILLISECOND);
                        }

                    }

                }


                bindInParameterTimestampArray (hStmt, ipar,
                                          yrs,
                                          mth,
                                          dts,
                                          hrs,
                                          min,
                                          sec,
                                          nan,
                                          dataBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLBindInParameterBinaryArray
        //--------------------------------------------------------------------

        public  void SQLBindInParameterBinaryArray (
                long hStmt,
                int ipar,
                int SQLtype,
                Object value[],
                int bufSize,
                int buffers[])
                throws SQLException
        {
                byte    errorCode[];
                byte    dataBuf[];

                int recordSize = value.length;

                // Since we don't know the precision of the column that
                // we are about to bind, we will set it to this arbitrary
                // value.

                int     precision = JdbcOdbcLimits.DEFAULT_IN_PRECISION;

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN binary parameter (SQLBindParameterBinaryArray), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype);
                        //dumpByte (value, value.length);
                }

                errorCode = new byte[1];
                dataBuf = new byte[bufSize * recordSize];

                bindInParameterBinaryArray (hStmt, ipar,
                                       OdbcDef.jdbcTypeToOdbc (SQLtype),
                                       value, bufSize, dataBuf, buffers, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                       OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }

        }

        //--------------------------------------------------------------------
        // SQLBindInParameterAtExecArray
        //--------------------------------------------------------------------

        public  void SQLBindInParameterAtExecArray (
                long hStmt,
                int ipar,
                int SQLtype,
                int len,
                int[] lenBuf)
                throws SQLException
        {
                byte    errorCode[];
                byte    dataBuf[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding DATA_AT_EXEC Array parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype + ", len=" + len);
                }

                errorCode = new byte[1];
                dataBuf = new byte[lenBuf.length];

                bindInParameterAtExecArray (hStmt, ipar, SQLtype, len, dataBuf, lenBuf, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                       OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }


        //4532162
        //--------------------------------------------------------------------
        // SQLBindOutParameterNull
        //--------------------------------------------------------------------

        public  void SQLBindOutParameterNull (
                long hStmt,
                int ipar,
                int SQLtype,
                int prec,
                int scale,
                byte lenBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding OUT NULL parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype);
                }

                errorCode = new byte[1];
                bindOutParameterNull (hStmt, ipar, OdbcDef.jdbcTypeToOdbc (SQLtype),
                                prec, scale, lenBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //4532162
        //--------------------------------------------------------------------
        // SQLBindOutParameterFixed
        //--------------------------------------------------------------------

        public  void SQLBindOutParameterFixed (
                        long hStmt,
                        int ipar,
                        int SQLtype,
                        int maxLen,
                        byte dataBuf[],
                        byte lenBuf[],
                        long [] buffers)
                        throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding OUT string parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype +
                               ", maxLen=" + maxLen);
                }

                errorCode = new byte[1];

                bindOutParameterFixed (hStmt, ipar, OdbcDef.jdbcTypeToCType(SQLtype), OdbcDef.jdbcTypeToOdbc(SQLtype), maxLen, dataBuf,
                                        lenBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //4532162
        //--------------------------------------------------------------------
        // SQLBindOutParameterBinary
        //--------------------------------------------------------------------

        public  void SQLBindOutParameterBinary (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                long buffers[])
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding INOUT binary parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar + ", SQLtype=" + SQLtype +
                               ", precision=" + precision + ", scale=" + scale +
                               ", rgbValue=" + dataBuf + ", lenBuf=" + lenBuf);
                }

                errorCode = new byte[1];
                bindOutParameterBinary (hStmt, ipar,
                                       OdbcDef.jdbcTypeToOdbc (SQLtype),
                                       precision, scale, dataBuf, lenBuf,
                                       errorCode, buffers);

                if (errorCode[0] != 0) {
                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }


        //4532162
        //--------------------------------------------------------------------
        // SQLBindOutParameterDate
        //--------------------------------------------------------------------

        public  void SQLBindOutParameterDate (
                        long hStmt,
                        int ipar,
                        int scale,
                        byte dataBuf[],
                        byte lenBuf[],
                        long [] buffers)
                        throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN OUT date parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar +
                               ", prec=" + (dataBuf.length - 1) + ", scale=" + scale);
                }

                errorCode = new byte[1];

                bindOutParameterDate (hStmt, ipar, scale, dataBuf,
                                        lenBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }


        //4532162
        //--------------------------------------------------------------------
        // SQLBindOutParameterTime
        //--------------------------------------------------------------------

        public  void SQLBindOutParameterTime (
                        long hStmt,
                        int ipar,
                        int scale,
                        byte dataBuf[],
                        byte lenBuf[],
                        long [] buffers)
                        throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding IN OUT time parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar +
                               ", prec=" + (dataBuf.length - 1) + ", scale=" + scale);
                }

                errorCode = new byte[1];

                bindOutParameterTime (hStmt, ipar, scale, dataBuf,
                                        lenBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //4532162
        //--------------------------------------------------------------------
        // SQLBindOutParameterTimestamp
        //--------------------------------------------------------------------

        public  void SQLBindOutParameterTimestamp (
                        long hStmt,
                        int ipar,
                        int precision,
                        byte dataBuf[],
                        byte lenBuf[],
                        long [] buffers)
                        throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Binding OUT time parameter (SQLBindParameter), hStmt=" +
                               hStmt + ", ipar=" + ipar +
                               ", prec=" + (dataBuf.length - 1) + ", precision=" + precision);
                }

                errorCode = new byte[1];

                bindOutParameterTimestamp (hStmt, ipar, precision, dataBuf,
                                        lenBuf, errorCode, buffers);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }



        //--------------------------------------------------------------------
        // SQLBrowseConnect
        // Returns the attributes and attribute values still needed to
        // connect.  Returns null if a successful connection has been
        // established
        //--------------------------------------------------------------------

        public  String SQLBrowseConnect (
                long hDbc,
                String connectString)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];
                byte    connStrOut[];
                String  attributes = null;

                if (tracer.isTracing ()) {
                        tracer.trace ("Connecting (SQLBrowseConnect), hDbc=" + hDbc + ", szConnStrIn=" + connectString);
                }

                errorCode = new byte[1];
                connStrOut = new byte[JdbcOdbcLimits.MAX_BROWSE_RESULT_LENGTH];

                byte[] bConnectString = null;
                char[] cConnectString = null;
                if (connectString != null)
                        cConnectString = connectString.toCharArray();
                try {
                        if (connectString != null)
                                bConnectString = CharsToBytes (charSet, cConnectString);
                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }

                browseConnect (hDbc, bConnectString, connStrOut, errorCode);

                // Required/optional attributes needed

                if (errorCode[0] == OdbcDef.SQL_NEED_DATA) {
                        attributes = new String (connStrOut);
                        attributes = attributes.trim ();
                        errorCode[0] = 0;
                }

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                       hDbc, OdbcDef.SQL_NULL_HSTMT);
                }

                if (tracer.isTracing ()) {
                        tracer.trace ("Attributes=" + attributes);
                }

                return attributes;
        }


        //--------------------------------------------------------------------
        // SQLCancel
        //--------------------------------------------------------------------

        public  void SQLCancel (
                long hStmt)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Cancelling (SQLCancel), hStmt=" + hStmt);
                }

                errorCode = new byte[1];
                cancel (hStmt, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLColAttributes
        //--------------------------------------------------------------------

        public  int SQLColAttributes (
                long hStmt,
                int column,
                int type)
                throws SQLException, JdbcOdbcSQLWarning
        {
                int     value = 0;
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Column attributes (SQLColAttributes), hStmt=" +
                               hStmt + ", icol=" + column + ", type=" + type);
                }

                errorCode = new byte[1];
                value = colAttributes (hStmt, column, type, errorCode);

                if (errorCode[0] != 0) {
                        // Process the error
                        try {
                                standardError (errorCode[0],
                                        OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                        }
                        catch (JdbcOdbcSQLWarning ex) {
                                if (tracer.isTracing ()) {
                                        tracer.trace ("value (int)=" + value);
                                }

                                // If we caught a warning, we need to save
                                // off the original return value

                                ex.value = (Object) BigDecimal.valueOf(value);

                                // Re-throw the warning, with the saved value

                                throw ex;
                        }

                }
                else {
                        if (tracer.isTracing ()) {
                                tracer.trace ("value (int)=" + value);
                        }
                }

                return value;
        }

        //--------------------------------------------------------------------
        // SQLColAttributesString
        //--------------------------------------------------------------------

        public  String SQLColAttributesString (
                long hStmt,
                int column,
                int type)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    rgbDesc[];
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Column attributes (SQLColAttributes), hStmt=" +
                               hStmt + ", icol=" + column + ", type=" + type);
                }

                errorCode = new byte[1];
                rgbDesc = new byte[JdbcOdbcLimits.DEFAULT_BUFFER_LENGTH];
                colAttributesString (hStmt, column, type, rgbDesc, errorCode);

                if (errorCode[0] != 0) {
                        // Process the error
                        try {
                                standardError (errorCode[0],
                                        OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                        }
                        catch (JdbcOdbcSQLWarning ex) {

                                // If we caught a warning, we need to save
                                // off the original return value

                                String sValue=new String();
                                try {
                                        sValue = BytesToChars (charSet, rgbDesc);
                                } catch (java.io.UnsupportedEncodingException e) {
                                        System.err.println (e);
                                }

                                if (tracer.isTracing ()) {
                                        tracer.trace ("value (String)=" +
                                               sValue.trim ());
                                }
                                ex.value = sValue.trim ();

                                // Re-throw the warning, with the saved value

                                throw ex;
                        }

                }
                String sValue=new String();
                try {
                        sValue = BytesToChars (charSet, rgbDesc);
                } catch (java.io.UnsupportedEncodingException e) {
                        System.err.println (e);
                }

                if (tracer.isTracing ()) {
                        tracer.trace ("value (String)=" + sValue.trim ());
                }
                return sValue.trim ();
        }

        //--------------------------------------------------------------------
        // SQLColumns
        //--------------------------------------------------------------------

        public  void SQLColumns (
                long hStmt,
                String catalog,
                String schema,
                String table,
                String column)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("(SQLColumns), hStmt=" + hStmt + ", catalog=" +
                               catalog + ", schema=" + schema + ", table=" +
                               table + ", column=" + column);
                }

                errorCode = new byte[1];

                byte[] bCatalog=null;
                byte[] bSchema=null;
                byte[] bTable=null;
                byte[] bColumn=null;
                char[] cCatalog=null;
                char[] cSchema=null;
                char[] cTable=null;
                char[] cColumn=null;
                if (catalog != null)
                        cCatalog=catalog.toCharArray();
                if (schema != null)
                        cSchema=schema.toCharArray();
                if (table != null)
                        cTable=table.toCharArray();
                if (column != null)
                        cColumn=column.toCharArray();
                try {
                        if (catalog != null)
                                bCatalog = CharsToBytes (charSet, cCatalog);
                        if (schema != null)
                                bSchema = CharsToBytes (charSet, cSchema);
                        if (table != null)
                                bTable = CharsToBytes (charSet, cTable);
                        if (column != null)
                                bColumn = CharsToBytes (charSet, cColumn);
                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }


                columns (hStmt,
                         bCatalog, (catalog == null),
                         bSchema, (schema == null),
                         bTable, (table == null),
                         bColumn, (column == null),
                         errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLColumnPrivileges
        //--------------------------------------------------------------------

        public  void SQLColumnPrivileges (
                long hStmt,
                String catalog,
                String schema,
                String table,
                String column)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("(SQLColumnPrivileges), hStmt=" + hStmt + ", catalog=" +
                               catalog + ", schema=" + schema + ", table=" +
                               table + ", column=" + column);
                }

                errorCode = new byte[1];

                byte[] bCatalog=null;
                byte[] bSchema=null;
                byte[] bTable=null;
                byte[] bColumn=null;
                char[] cCatalog=null;
                char[] cSchema=null;
                char[] cTable=null;
                char[] cColumn=null;
                if (catalog != null)
                        cCatalog=catalog.toCharArray();
                if (schema != null)
                        cSchema=schema.toCharArray();
                if (table != null)
                        cTable=table.toCharArray();
                if (column != null)
                        cColumn=column.toCharArray();
                try {
                        if (catalog != null)
                                bCatalog = CharsToBytes (charSet, cCatalog);
                        if (schema != null)
                                bSchema = CharsToBytes (charSet, cSchema);
                        if (table != null)
                                bTable = CharsToBytes (charSet, cTable);
                        if (column != null)
                                bColumn = CharsToBytes (charSet, cColumn);
                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }

                columnPrivileges (hStmt,
                                  bCatalog, (catalog == null),
                                  bSchema, (schema == null),
                                  bTable, (table == null),
                                  bColumn, (column == null),
                                  errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLDescribeParamNullable
        //--------------------------------------------------------------------

        public  boolean SQLDescribeParamNullable (
                long hStmt,
                int param)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                int     value;
                boolean nullable = false;

                if (tracer.isTracing ()) {
                        tracer.trace ("Parameter nullable (SQLDescribeParam), hStmt=" +
                               hStmt + ", ipar=" + param);
                }

                errorCode = new byte[1];

                // Call SQLDescribeParam and return 4th output parameter
                // (nullable)

                value = describeParam (hStmt, param, 4, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }

                if (value == OdbcDef.SQL_NULLABLE) {
                        nullable = true;
                }

                if (tracer.isTracing ()) {
                        tracer.trace ("nullable=" + nullable);
                }

                return nullable;
        }
        //--------------------------------------------------------------------
        // SQLDescribeParamPrecision
        //--------------------------------------------------------------------

        public  int SQLDescribeParamPrecision (
                long hStmt,
                int param)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                int     precision;

                if (tracer.isTracing ()) {
                        tracer.trace ("Parameter precision (SQLDescribeParam), hStmt=" +
                               hStmt + ", ipar=" + param);
                }

                errorCode = new byte[1];

                // Call SQLDescribeParam and return 2nd output parameter
                // (precision)

                precision = describeParam (hStmt, param, 2, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }

                if (tracer.isTracing ()) {
                        tracer.trace ("precision=" + precision);
                }

                return precision;
        }

        //--------------------------------------------------------------------
        // SQLDescribeParamScale
        //--------------------------------------------------------------------

        public  int SQLDescribeParamScale (
                long hStmt,
                int param)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                int     scale;

                if (tracer.isTracing ()) {
                        tracer.trace ("Parameter scale (SQLDescribeParam), hStmt=" +
                               hStmt + ", ipar=" + param);
                }

                errorCode = new byte[1];

                // Call SQLDescribeParam and return 3rd output parameter
                // (scale)

                scale = describeParam (hStmt, param, 3, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }

                if (tracer.isTracing ()) {
                        tracer.trace ("scale=" + scale);
                }

                return scale;
        }

        //--------------------------------------------------------------------
        // SQLDescribeParamType
        //--------------------------------------------------------------------

        public  int SQLDescribeParamType (
                long hStmt,
                int param)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                int     type;

                if (tracer.isTracing ()) {
                        tracer.trace ("Parameter type (SQLDescribeParam), hStmt=" +
                               hStmt + ", ipar=" + param);
                }

                errorCode = new byte[1];

                // Call SQLDescribeParam and return 1st output parameter
                // (type)

                type = describeParam (hStmt, param, 1, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                       OdbcDef.SQL_NULL_HDBC, hStmt);
                }

                if (tracer.isTracing ()) {
                        tracer.trace ("type=" + type);
                }

                return type;
        }

        //--------------------------------------------------------------------
        // SQLDisconnect
        //--------------------------------------------------------------------

        public  void SQLDisconnect (
                long hDbc)
                throws SQLException
        {
                byte    errorCode[];
                if (tracer.isTracing ()) {
                        tracer.trace ("Disconnecting (SQLDisconnect), hDbc=" + hDbc);
                }

                //4524683
                java.util.Set hstmtSet = hstmtMap.keySet();
                Object[] hstmtArray = hstmtSet.toArray();
                int hstmtArrayLen = hstmtArray.length;

                for(int i=0; i<hstmtArrayLen; i++) {
                        Long hDbcObj = (Long)hstmtMap.get(hstmtArray[i]);
                        if(hDbcObj != null) {
                                if(hDbcObj.longValue() == hDbc) {
                                        SQLFreeStmt(((Long)hstmtArray[i]).longValue(), OdbcDef.SQL_DROP);
                                }
                        }
                }

                errorCode = new byte[1];
                disconnect (hDbc, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                       hDbc, OdbcDef.SQL_NULL_HSTMT);
                }
        }

        //--------------------------------------------------------------------
        // SQLDriverConnect
        //--------------------------------------------------------------------

        public  void SQLDriverConnect (
                long hDbc,
                String connectString)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Connecting (SQLDriverConnect), hDbc=" +
                               hDbc + ", szConnStrIn=" + connectString);
                }

                errorCode = new byte[1];
                byte[] bConnectString=null;
                char[] cConnectString=null;

                if (connectString != null)
                        cConnectString=connectString.toCharArray();

                try {
                        if (connectString != null)
                                bConnectString = CharsToBytes (charSet, cConnectString);

                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }

                driverConnect (hDbc, bConnectString, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                       hDbc, OdbcDef.SQL_NULL_HSTMT);
                }
        }

        //--------------------------------------------------------------------
        // SQLExecDirect
        //--------------------------------------------------------------------
        public  void SQLExecDirect (
                long hStmt,
                String query)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Executing (SQLExecDirect), hStmt=" +
                               hStmt + ", szSqlStr=" + query);
                }

                errorCode = new byte[1];

                byte[] bQuery=null;
                char[] cQuery=null;
                if (query != null)
                        cQuery=query.toCharArray();
                try {
                        if (query != null)
                                bQuery = CharsToBytes (charSet, cQuery);
                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }

                execDirect (hStmt, bQuery, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLExecute
        // Returns true if more data is required (i.e. there were data-at-
        // execution parameters)
        //--------------------------------------------------------------------
        public  boolean SQLExecute (
                long hStmt)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];
                boolean needData = false;

                if (tracer.isTracing ()) {
                        tracer.trace ("Executing (SQLExecute), hStmt=" + hStmt);
                }

                errorCode = new byte[1];
                execute (hStmt, errorCode);

                // If SQL_NEED_DATA (data-at-execution parameters) is
                // returned, set the return code.

                if (errorCode[0] == OdbcDef.SQL_NEED_DATA) {
                        if (tracer.isTracing ()) {
                                tracer.trace ("SQL_NEED_DATA returned");
                        }
                        needData = true;
                        errorCode[0] = 0;
                }

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
                return needData;
        }

        //--------------------------------------------------------------------
        // SQLFetch
        //--------------------------------------------------------------------
        public  boolean SQLFetch (
                long hStmt)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];
                boolean rc = true;

                if (tracer.isTracing ()) {
                        tracer.trace ("Fetching (SQLFetch), hStmt=" + hStmt);
                }

                errorCode = new byte[1];
                fetch (hStmt, errorCode);

                // Check for SQL_NO_DATA_FOUND, indicating that this is the
                // end of the result set.  Set the return code to false and
                // clear the error

                if (errorCode[0] == OdbcDef.SQL_NO_DATA_FOUND) {
                        rc = false;
                        errorCode[0] = 0;
                        if (tracer.isTracing ()) {
                                tracer.trace ("End of result set (SQL_NO_DATA)");
                        }
                }

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
                return rc;
        }

        //--------------------------------------------------------------------
        // SQLFetchScroll
        //--------------------------------------------------------------------
        public  boolean SQLFetchScroll (
                long hStmt,
                short orientation,
                int offset)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];
                boolean rc = true;

                if (tracer.isTracing ()) {
                        tracer.trace ("Fetching (SQLFetchScroll), hStmt=" + hStmt);
                }

                errorCode = new byte[1];
                fetchScroll (hStmt, orientation, offset, errorCode);

                // Check for SQL_NO_DATA_FOUND, indicating that this is the
                // end of the result set.  Set the return code to false and
                // clear the error

                if (errorCode[0] == OdbcDef.SQL_NO_DATA_FOUND) {
                        rc = false;
                        errorCode[0] = 0;
                        if (tracer.isTracing ()) {
                                tracer.trace ("End of result set (SQL_NO_DATA)");
                        }
                }

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
                return rc;
        }

        //--------------------------------------------------------------------
        // SQLForeignKeys
        //--------------------------------------------------------------------

        public  void SQLForeignKeys (
                long hStmt,
                String PKcatalog,
                String PKschema,
                String PKtable,
                String FKcatalog,
                String FKschema,
                String FKtable)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("(SQLForeignKeys), hStmt=" + hStmt + ", Pcatalog=" +
                               PKcatalog + ", Pschema=" + PKschema + ", Ptable=" +
                               PKtable + ", Fcatalog=" + FKcatalog + ", Fschema=" +
                               FKschema + ", Ftable=" + FKtable);
                }

                errorCode = new byte[1];

                byte[] bPKCatalog=null;
                byte[] bPKSchema=null;
                byte[] bPKTable=null;
                byte[] bFKCatalog=null;
                byte[] bFKSchema=null;
                byte[] bFKTable=null;
                char[] cPKCatalog=null;
                char[] cPKSchema=null;
                char[] cPKTable=null;
                char[] cFKCatalog=null;
                char[] cFKSchema=null;
                char[] cFKTable=null;
                if (PKcatalog != null)
                        cPKCatalog=PKcatalog.toCharArray();
                if (PKschema != null)
                        cPKSchema=PKschema.toCharArray();
                if (PKtable != null)
                        cPKTable=PKtable.toCharArray();
                if (FKcatalog != null)
                        cFKCatalog=FKcatalog.toCharArray();
                if (FKschema != null)
                        cFKSchema=FKschema.toCharArray();
                if (FKtable != null)
                        cFKTable=FKtable.toCharArray();
                try {
                        if (PKcatalog != null)
                                bPKCatalog = CharsToBytes (charSet, cPKCatalog);
                        if (PKschema != null)
                                bPKSchema = CharsToBytes (charSet, cPKSchema);
                        if (PKtable != null)
                                bPKTable = CharsToBytes (charSet, cPKTable);
                        if (FKcatalog != null)
                                bFKCatalog = CharsToBytes (charSet, cFKCatalog);
                        if (FKschema != null)
                                bFKSchema = CharsToBytes (charSet, cFKSchema);
                        if (FKtable != null)
                                bFKTable = CharsToBytes (charSet, cFKTable);
                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }

                foreignKeys (hStmt,
                        bPKCatalog, (PKcatalog == null),
                        bPKSchema, (PKschema == null),
                        bPKTable, (PKtable == null),
                        bFKCatalog, (FKcatalog == null),
                        bFKSchema, (FKschema == null),
                        bFKTable, (FKtable == null),
                        errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLFreeConnect
        //--------------------------------------------------------------------

        public  void SQLFreeConnect (
                long hDbc)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Closing connection (SQLFreeConnect), hDbc=" + hDbc);
                }

                errorCode = new byte[1];
                freeConnect (hDbc, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        hDbc, OdbcDef.SQL_NULL_HSTMT);
                }
        }

        //--------------------------------------------------------------------
        // SQLFreeEnv
        //--------------------------------------------------------------------

        public  void SQLFreeEnv (
                long hEnv)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Closing environment (SQLFreeEnv), hEnv=" + hEnv);
                }

                errorCode = new byte[1];
                freeEnv (hEnv, errorCode);

                if (errorCode[0] != 0) {
                        throwGenericSQLException ();
                }
        }

        //--------------------------------------------------------------------
        // SQLFreeStmt
        //--------------------------------------------------------------------

        // 4524683
        public  synchronized void SQLFreeStmt (
                long hStmt,
                int fOption)
                throws SQLException
        {
                byte    errorCode[];

                errorCode = new byte[1];

                //4524683
                Long hStmtObj = new Long(hStmt);
                if(fOption == OdbcDef.SQL_DROP) {
                        if(hstmtMap.containsKey(hStmtObj)) {
                                hstmtMap.remove(hStmtObj);
                                freeStmt (hStmt, fOption, errorCode);
                                if (tracer.isTracing ()) {
                                        tracer.trace ("Free statement (SQLFreeStmt), hStmt=" +
                                                hStmt + ", fOption=" + fOption);
                                }
                        }
                } else {
                        freeStmt (hStmt, fOption, errorCode);
                        if (tracer.isTracing ()) {
                                tracer.trace ("Free statement (SQLFreeStmt), hStmt=" +
                                        hStmt + ", fOption=" + fOption);
                        }
                }

                if (errorCode[0] != 0) {
                        throwGenericSQLException ();
                }
        }

        //--------------------------------------------------------------------
        // SQLGetConnectOption
        //--------------------------------------------------------------------

        public  long SQLGetConnectOption (
                long   hDbc,
                short fOption)
                throws SQLException
        {
                byte    errorCode[];
                long    vParam;

                if (tracer.isTracing ()) {
                        tracer.trace ("Connection Option (SQLGetConnectOption), hDbc=" +
                               hDbc + ", fOption=" + fOption);
                }

                errorCode = new byte[1];
                vParam = getConnectOption (hDbc, fOption, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        hDbc, OdbcDef.SQL_NULL_HSTMT);
                }

                if (tracer.isTracing ()) {
                        tracer.trace ("option value (int)=" + vParam);
                }

                return vParam;
        }

        //--------------------------------------------------------------------
        // SQLGetConnectOptionString
        //--------------------------------------------------------------------

        public  String SQLGetConnectOptionString (
                long   hDbc,
                short fOption)
                throws SQLException
        {
                byte    errorCode[];
                byte    szParam[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Connection Option (SQLGetConnectOption), hDbc=" +
                               hDbc + ", fOption=" + fOption);
                }

                errorCode = new byte[1];
                szParam = new byte[JdbcOdbcLimits.DEFAULT_BUFFER_LENGTH];

                getConnectOptionString (hDbc, fOption, szParam, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        hDbc, OdbcDef.SQL_NULL_HSTMT);
                }

                String  sValue = new String ();
                try {
                        sValue = BytesToChars (charSet, szParam);
                } catch (java.io.UnsupportedEncodingException exxxx) {
                        throw (Error) (new InternalError("SQL")).initCause(exxxx);
                }

                if (tracer.isTracing ()) {
                        tracer.trace ("option value (int)=" + sValue.trim ());
                }

                return sValue.trim ();
        }

        //--------------------------------------------------------------------
        // SQLGetCursorName
        //--------------------------------------------------------------------

        public  String SQLGetCursorName (
                long hStmt)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    szCursor[];
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Cursor name (SQLGetCursorName), hStmt=" + hStmt);
                }

                errorCode = new byte[1];
                szCursor = new byte[JdbcOdbcLimits.DEFAULT_BUFFER_LENGTH];
                getCursorName (hStmt, szCursor, errorCode);

                if (errorCode[0] != 0) {
                        // Process the error
                        try {
                                standardError (errorCode[0],
                                        OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                        }
                        catch (JdbcOdbcSQLWarning ex) {

                                // If we caught a warning, we need to save
                                // off the original return value

                                String  sValue = new String ();
                                try {
                                        sValue = BytesToChars (charSet, szCursor);
                                } catch (java.io.UnsupportedEncodingException exxxx) {
                                        throw (Error) (new InternalError("SQL")).initCause(exxxx);
                                }

                                if (tracer.isTracing ()) {
                                        tracer.trace ("value=" + sValue.trim ());
                                }
                                ex.value = sValue.trim ();

                                // Re-throw the warning, with the saved value

                                throw ex;
                        }

                }
                String  sValue = new String (szCursor);
                if (tracer.isTracing ()) {
                        tracer.trace ("value=" + sValue.trim ());
                }
                return sValue.trim ();
        }

        //--------------------------------------------------------------------
        // SQLGetDataBinary
        // Returns number of bytes read, -1 for eof
        //--------------------------------------------------------------------
        public  int SQLGetDataBinary (
                long hStmt,
                int column,
                byte b[])
                throws SQLException, JdbcOdbcSQLWarning
        {
                return SQLGetDataBinary (hStmt, column, OdbcDef.SQL_C_BINARY,
                                                                b, b.length);
        }

        //--------------------------------------------------------------------
        // SQLGetDataBinary
        // Returns number of bytes read, -1 for eof
        //--------------------------------------------------------------------
        public  int SQLGetDataBinary (
                long hStmt,
                int column,
                int cType,
                byte b[],
                int length)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                int     bytesRead = 0;

                if (tracer.isTracing ()) {
                        tracer.trace ("Get binary data (SQLGetData), hStmt=" + hStmt +
                               ", column=" + column + ", type=" + cType +
                               ", length=" + length);
                }

                // Use a 2-byte error code.  The first byte is for the
                // actual SQL return code, the second byte is a null-value
                // indicator (0=not null)

                errorCode = new byte[2];
                bytesRead = getDataBinary (hStmt, column, cType, b, length,
                                           errorCode);

                // If no data was found, return a -1

                if (errorCode[0] == OdbcDef.SQL_NO_DATA) {
                        bytesRead = -1;
                        errorCode[0] = 0;
                }

                if (errorCode[0] != 0) {

                        // Process the error
                        try {
                                standardError (errorCode[0],
                                        OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                        }

                        catch (JdbcOdbcSQLWarning ex) {

                                if (tracer.isTracing ()) {
                                        if (bytesRead == -1) {
                                                tracer.trace ("NULL");
                                        }
                                        else {
                                                if (tracer.isTracing ()) {
                                                        tracer.trace ("Bytes: " + bytesRead);
                                                }
//                                              dumpByte (b, bytesRead);
                                        }
                                }

                                // If we caught a warning, we need to save
                                // off the original return value

                                ex.value = (Object) new Integer (bytesRead);

                                // Re-throw the warning, with the saved value
                                throw ex;
                        }
                }
                if (tracer.isTracing ()) {
                        if (bytesRead == -1) {
                                tracer.trace ("NULL");
                        }
                        else {
                                if (tracer.isTracing ()) {
                                        tracer.trace ("Bytes: " + bytesRead);
                                }
//                              dumpByte (b, bytesRead);
                        }
                }
                return bytesRead;
        }

        //--------------------------------------------------------------------
        // SQLGetDataDouble
        //--------------------------------------------------------------------
        public  Double SQLGetDataDouble (
                long hStmt,
                int column)
                throws SQLException, JdbcOdbcSQLWarning
                {
                byte    errorCode[];
                double  value;

                if (tracer.isTracing ()) {
                        tracer.trace ("Get double data (SQLGetData), hStmt=" +
                               hStmt + ", column=" + column);
                }

                // Use a 2-byte error code.  The first byte is for the
                // actual SQL return code, the second byte is a null-value
                // indicator (0=not null)

                errorCode = new byte[2];
                value = getDataDouble (hStmt, column, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        try {
                                standardError (errorCode[0],
                                        OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                        }

                        catch (JdbcOdbcSQLWarning ex) {

                                // If we caught a warning, we need to save
                                // off the original return value

                                if (errorCode[1] == 0) {
                                        if (tracer.isTracing ()) {
                                                tracer.trace ("value=" + value);
                                        }

                                        // The value is not null
                                        ex.value = (Object) new Double (
                                                        value);
                                }
                                else {
                                        if (tracer.isTracing ()) {
                                                tracer.trace ("NULL");
                                        }

                                        // The value is null
                                        ex.value = null;
                                }

                                // Re-throw the warning, with the saved value
                                throw ex;
                        }
                }

                // Check for a NULL value

                if (errorCode[1] == 0) {
                        if (tracer.isTracing ()) {
                                tracer.trace ("value=" + value);
                        }
                        // The value is not null
                        return new Double (value);
                }
                else {
                        if (tracer.isTracing ()) {
                                tracer.trace ("NULL");
                        }
                        // The value is null
                        return null;
                }
        }

        //--------------------------------------------------------------------
        // SQLGetDataFloat
        //--------------------------------------------------------------------
        public  Float SQLGetDataFloat (
                long hStmt,
                int column)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                float   value;

                if (tracer.isTracing ()) {
                        tracer.trace ("Get float data (SQLGetData), hStmt=" +
                               hStmt + ", column=" + column);
                }

                // Use a 2-byte error code.  The first byte is for the
                // actual SQL return code, the second byte is a null-value
                // indicator (0=not null)

                errorCode = new byte[2];
                // Fix 4532167.
                value = (float) getDataFloat (hStmt, column, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        try {
                                standardError (errorCode[0],
                                        OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                        }

                        catch (JdbcOdbcSQLWarning ex) {

                                // If we caught a warning, we need to save
                                // off the original return value

                                if (errorCode[1] == 0) {
                                        if (tracer.isTracing ()) {
                                                tracer.trace ("value=" + value);
                                        }
                                        // The value is not null
                                        ex.value = (Object) new Float (value);
                                }
                                else {
                                        if (tracer.isTracing ()) {
                                                tracer.trace ("NULL");
                                        }
                                        // The value is null
                                        ex.value = null;
                                }

                                // Re-throw the warning, with the saved value
                                throw ex;
                        }
                }

                // Check for a NULL value

                if (errorCode[1] == 0) {
                        // The value is not null
                        if (tracer.isTracing ()) {
                                tracer.trace ("value=" + value);
                        }
                        return new Float (value);
                }
                else {
                        if (tracer.isTracing ()) {
                                tracer.trace ("NULL");
                        }
                        // The value is null
                        return null;
                }
        }

        //--------------------------------------------------------------------
        // SQLGetDataInteger
        //--------------------------------------------------------------------
        public  Integer SQLGetDataInteger (
                long hStmt,
                int column)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                int     value;

                if (tracer.isTracing ()) {
                        tracer.trace ("Get integer data (SQLGetData), hStmt=" +
                               hStmt + ", column=" + column);
                }

                // Use a 2-byte error code.  The first byte is for the
                // actual SQL return code, the second byte is a null-value
                // indicator (0=not null)

                errorCode = new byte[2];
                value = getDataInteger (hStmt, column, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        try {
                                standardError (errorCode[0],
                                        OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                        }

                        catch (JdbcOdbcSQLWarning ex) {

                                // If we caught a warning, we need to save
                                // off the original return value

                                if (errorCode[1] == 0) {
                                        if (tracer.isTracing ()) {
                                                tracer.trace ("value=" + value);
                                        }
                                        // The value is not null
                                        ex.value = (Object) new Integer (
                                                        value);
                                }
                                else {
                                        if (tracer.isTracing ()) {
                                                tracer.trace ("NULL");
                                        }
                                        // The value is null
                                        ex.value = null;
                                }

                                // Re-throw the warning, with the saved value
                                throw ex;
                        }
                }

                // Check for a NULL value

                if (errorCode[1] == 0) {
                        if (tracer.isTracing ()) {
                                tracer.trace ("value=" + value);
                        }
                        // The value is not null
                        return new Integer (value);
                }
                else {
                        if (tracer.isTracing ()) {
                                tracer.trace ("NULL");
                        }
                        // The value is null
                        return null;
                }
        }

        //--------------------------------------------------------------------
        // SQLGetDataString
        //--------------------------------------------------------------------
        public  String SQLGetDataString (
                long hStmt,
                int column,
                int maxLen,
                boolean trimString)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                byte    rgbValue[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Get string data (SQLGetData), hStmt=" +
                               hStmt + ", column=" + column + ", maxLen=" + maxLen);
                }

                // Use a 2-byte error code.  The first byte is for the
                // actual SQL return code, the second byte is a null-value
                // indicator (0=not null)

                errorCode = new byte[2];
                rgbValue = new byte[maxLen];

                int rlen = getDataString (hStmt, column, rgbValue, errorCode);

                //changing for sun bug review id 98028 -- Sundari
                // If we read less than 0 bytes, treat it as a null

                if (rlen < 0) {
                        errorCode[1] = 1;
                }

                // Sanity check the return length.  For some drivers, the length is
                // returned as the total length of the column when the data was
                // truncated.  Set the length to be the maximum length of the
                // buffer in this case.

                if (rlen > maxLen) {
                        rlen = maxLen;
                }

                if (errorCode[0] != 0) {

                        // Process the error
                        try {
                                standardError (errorCode[0],
                                        OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                        }

                        catch (JdbcOdbcSQLWarning ex) {

                                // If we caught a warning, we need to save
                                // off the original return value

                                if (errorCode[1] == 0) {

                                        // The value is not null.  If we know the return length,
                                        // create the String the proper size
                                        char [] csValue = new char[rlen];
                                        String sValue=new String();
                                        try {
                                                sValue=BytesToChars(charSet, rgbValue);
                                        } catch (java.io.UnsupportedEncodingException e) {
                                                System.err.println(e);
                                        }

                                        if (tracer.isTracing ()) {
                                                tracer.trace (sValue.trim ());
                                        }
                                        if (trimString ) {
                                                ex.value = sValue.trim ();
                                        }
                                        else {
                                                ex.value = sValue;
                                        }
                                }
                                else {
                                        if (tracer.isTracing ()) {
                                                tracer.trace ("NULL");
                                        }
                                        // The value is null
                                        ex.value = null;
                                }

                                // Re-throw the warning, with the saved value
                                throw ex;
                        }
                }

                // Check for a NULL value

                if (errorCode[1] == 0) {
                        // The value is not null.  If we know the return length,
                        // create the String the proper size

                        String sValue=new String();
                        char [] csValue = new char[rlen];
                        try {
                                sValue=BytesToChars(charSet, rgbValue);
                        } catch (java.io.UnsupportedEncodingException e){
                                System.err.println(e);
                        }

                        if (tracer.isTracing ()) {
                                tracer.trace (sValue.trim ());
                        }

                        // If the string should be trimmed, so it now

                        if (trimString) {
                                return sValue.trim ();
                        }
                        else {
                                return sValue;
                        }
                }
                else {
                        if (tracer.isTracing ()) {
                                tracer.trace ("NULL");
                        }
                        return null;
                }
        }

        //--------------------------------------------------------------------
        // SQLGetDataStringDate
        //--------------------------------------------------------------------
        public  String SQLGetDataStringDate (
                long hStmt,
                int column)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                byte    rgbValue[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Get date data (SQLGetData), hStmt=" +
                               hStmt + ", column=" + column);
                }

                // Use a 2-byte error code.  The first byte is for the
                // actual SQL return code, the second byte is a null-value
                // indicator (0=not null)

                errorCode = new byte[2];
                rgbValue = new byte[11];

                getDataStringDate (hStmt, column, rgbValue, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        try {
                                standardError (errorCode[0],
                                        OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                        }

                        catch (JdbcOdbcSQLWarning ex) {

                                // If we caught a warning, we need to save
                                // off the original return value

                                if (errorCode[1] == 0) {
                                        // The value is not null
                                        String  sValue = new String ();
                                        try {
                                                sValue = BytesToChars(charSet, rgbValue);
                                        } catch (java.io.UnsupportedEncodingException exx) {
                                                throw (Error) (new InternalError("SQL")).initCause(exx);
                                        }

                                        if (tracer.isTracing ()) {
                                                tracer.trace (sValue.trim ());
                                        }
                                        ex.value = sValue.trim ();
                                }
                                else {
                                        if (tracer.isTracing ()) {
                                                tracer.trace ("NULL");
                                        }
                                        // The value is null
                                        ex.value = null;
                                }

                                // Re-throw the warning, with the saved value
                                throw ex;
                        }
                }

                // Check for a NULL value

                if (errorCode[1] == 0) {
                        String  sValue = new String ();
                        try {
                                sValue = BytesToChars(charSet, rgbValue);
                        } catch (java.io.UnsupportedEncodingException exx) {
                                throw (Error) (new InternalError("SQL")).initCause(exx);
                        }

                        if (tracer.isTracing ()) {
                                tracer.trace (sValue.trim ());
                        }

                        // If the string should be trimmed, so it now

                        return sValue.trim ();
                }
                else {
                        if (tracer.isTracing ()) {
                                tracer.trace ("NULL");
                        }
                        return null;
                }
        }

        //--------------------------------------------------------------------
        // SQLGetDataStringTime
        //--------------------------------------------------------------------
        public  String SQLGetDataStringTime (
                long hStmt,
                int column)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                byte    rgbValue[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Get time data (SQLGetData), hStmt=" +
                                        hStmt + ", column=" + column);
                }

                // Use a 2-byte error code.  The first byte is for the
                // actual SQL return code, the second byte is a null-value
                // indicator (0=not null)

                errorCode = new byte[2];
                rgbValue = new byte[9];

                getDataStringTime (hStmt, column, rgbValue, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        try {
                                standardError (errorCode[0],
                                        OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                        }

                        catch (JdbcOdbcSQLWarning ex) {

                                // If we caught a warning, we need to save
                                // off the original return value

                                if (errorCode[1] == 0) {
                                        // The value is not null
                                        String  sValue = new String ();
                                        try {
                                                sValue = BytesToChars(charSet, rgbValue);
                                        } catch (java.io.UnsupportedEncodingException exx) {
                                                throw (Error) (new InternalError("SQL")).initCause(exx);
                                        }

                                        if (tracer.isTracing ()) {
                                                tracer.trace (sValue.trim ());
                                        }
                                        ex.value = sValue.trim ();
                                }
                                else {
                                        if (tracer.isTracing ()) {
                                                tracer.trace ("NULL");
                                        }
                                        // The value is null
                                        ex.value = null;
                                }

                                // Re-throw the warning, with the saved value
                                throw ex;
                        }
                }

                // Check for a NULL value

                if (errorCode[1] == 0) {
                        String  sValue = new String ();
                        try {
                                sValue = BytesToChars(charSet, rgbValue);
                        } catch (java.io.UnsupportedEncodingException exx) {
                                throw (Error) (new InternalError("SQL")).initCause(exx);
                        }

                        if (tracer.isTracing ()) {
                                tracer.trace (sValue.trim ());
                        }

                        // If the string should be trimmed, so it now

                        return sValue.trim ();
                }
                else {
                        if (tracer.isTracing ()) {
                                tracer.trace ("NULL");
                        }
                        return null;
                }
        }

        //--------------------------------------------------------------------
        // SQLGetDataStringTimestamp
        //--------------------------------------------------------------------
        public  String SQLGetDataStringTimestamp (
                long hStmt,
                int column)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                byte    rgbValue[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Get timestamp data (SQLGetData), hStmt=" +
                               hStmt + ", column=" + column);
                }

                // Use a 2-byte error code.  The first byte is for the
                // actual SQL return code, the second byte is a null-value
                // indicator (0=not null)

                errorCode = new byte[2];
                rgbValue = new byte[30];

                getDataStringTimestamp (hStmt, column, rgbValue, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        try {
                                standardError (errorCode[0],
                                        OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                        }

                        catch (JdbcOdbcSQLWarning ex) {

                                // If we caught a warning, we need to save
                                // off the original return value

                                if (errorCode[1] == 0) {
                                        // The value is not null
                                        String  sValue = new String ();
                                        try {
                                                sValue = BytesToChars(charSet, rgbValue);
                                        } catch (java.io.UnsupportedEncodingException exx) {
                                                throw (Error) (new InternalError("SQL")).initCause(exx);
                                        }

                                        if (tracer.isTracing ()) {
                                                tracer.trace (sValue.trim ());
                                        }
                                        ex.value = sValue.trim ();
                                }
                                else {
                                        if (tracer.isTracing ()) {
                                                tracer.trace ("NULL");
                                        }
                                        // The value is null
                                        ex.value = null;
                                }

                                // Re-throw the warning, with the saved value
                                throw ex;
                        }
                }

                // Check for a NULL value

                if (errorCode[1] == 0) {
                        String  sValue = new String ();
                        try {
                                sValue = BytesToChars(charSet, rgbValue);
                        } catch (java.io.UnsupportedEncodingException exx) {
                                throw (Error) (new InternalError("SQL")).initCause(exx);
                        }

                        if (tracer.isTracing ()) {
                                tracer.trace (sValue.trim ());
                        }

                        // If the string should be trimmed, so it now

                        return sValue.trim ();
                }
                else {
                        if (tracer.isTracing ()) {
                                tracer.trace ("NULL");
                        }
                        return null;
                }
        }

        //--------------------------------------------------------------------
        // SQLGetInfo
        //--------------------------------------------------------------------

        public  int SQLGetInfo (
                long   hDbc,
                short fInfoType)
                throws SQLException
        {
                byte    errorCode[];
                int     infoValue;

                if (tracer.isTracing ()) {
                        tracer.trace ("Get connection info (SQLGetInfo), hDbc=" +
                                hDbc + ", fInfoType=" + fInfoType);
                }

                errorCode = new byte[1];
                infoValue = getInfo (hDbc, fInfoType, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        hDbc, OdbcDef.SQL_NULL_HSTMT);
                }
                if (tracer.isTracing ()) {
                        tracer.trace (" int value=" + infoValue);
                }
                return infoValue;
        }

        //--------------------------------------------------------------------
        // SQLGetInfoShort
        //--------------------------------------------------------------------

        public  int SQLGetInfoShort (
                long   hDbc,
                short fInfoType)
                throws SQLException
        {
                byte    errorCode[];
                int     infoValue;

                if (tracer.isTracing ()) {
                        tracer.trace ("Get connection info (SQLGetInfo), hDbc=" +
                                hDbc + ", fInfoType=" + fInfoType);
                }

                errorCode = new byte[1];
                infoValue = getInfoShort (hDbc, fInfoType, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        hDbc, OdbcDef.SQL_NULL_HSTMT);
                }
                if (tracer.isTracing ()) {
                        tracer.trace (" short value=" + infoValue);
                }
                return infoValue;
        }

        //--------------------------------------------------------------------
        // SQLGetInfoString
        //--------------------------------------------------------------------

        public  String SQLGetInfoString (
                long   hDbc,
                short fInfoType)
                throws SQLException
        {
                // Get info with result buffer
                 return SQLGetInfoString (hDbc, fInfoType,
                                JdbcOdbcLimits.DEFAULT_BUFFER_LENGTH);
        }

        //--------------------------------------------------------------------
        // SQLGetInfoString
        // Optional interface to supply the size of the result buffer
        //--------------------------------------------------------------------

        public  String SQLGetInfoString (
                long   hDbc,
                short fInfoType,
                int   buffSize)
                throws SQLException
        {
                byte    errorCode[];
                byte    szParam[];
                String  vParam;

                if (tracer.isTracing ()) {
                        tracer.trace ("Get connection info string (SQLGetInfo), hDbc=" +
                               hDbc + ", fInfoType=" + fInfoType + ", len=" + buffSize);
                }

                errorCode = new byte[1];
                szParam = new byte[buffSize];

                getInfoString (hDbc, fInfoType, szParam, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        hDbc, OdbcDef.SQL_NULL_HSTMT);
                }

                String  sValue = new String ();
                try {
                        sValue = BytesToChars (charSet, szParam);
                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }

                if (tracer.isTracing ()) {
                        tracer.trace (sValue.trim ());
                }
                return sValue.trim ();
        }

        //--------------------------------------------------------------------
        // SQLGetStmtOption
        //--------------------------------------------------------------------

        public  long SQLGetStmtOption (
                long hStmt,
                short fOption)
                throws SQLException, JdbcOdbcSQLWarning
        {
                long    value = 0;
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Get statement option (SQLGetStmtOption), hStmt=" +
                               hStmt + ", fOption=" + fOption);
                }

                errorCode = new byte[1];
                value = getStmtOption (hStmt, fOption, errorCode);

                if (errorCode[0] != 0) {
                        // Process the error
                        try {
                                standardError (errorCode[0],
                                        OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                        }
                        catch (JdbcOdbcSQLWarning ex) {

                                if (tracer.isTracing ()) {
                                        tracer.trace ("value=" + value);
                                }
                                // If we caught a warning, we need to save
                                // off the original return value

                                ex.value = (Object) BigDecimal.valueOf(value);

                                // Re-throw the warning, with the saved value

                                throw ex;
                        }

                }
                if (tracer.isTracing ()) {
                        tracer.trace ("value=" + value);
                }
                return value;
        }

        //--------------------------------------------------------------------
        // SQLGetStmtAttr
        //--------------------------------------------------------------------

        public  int SQLGetStmtAttr (
                long   hStmt,
                int   sOption)
                throws SQLException
        {
                byte    errorCode[];
                int     attrValue;

                if (tracer.isTracing ()) {
                        tracer.trace ("Get Statement Attribute (SQLGetStmtAttr), hDbc=" +
                                hStmt + ", AttrType=" + sOption);
                }

                errorCode = new byte[1];
                attrValue = getStmtAttr (hStmt, sOption, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                    try {

                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                    }
                    catch (JdbcOdbcSQLWarning ex) {

                        if (tracer.isTracing ()) {
                                tracer.trace ("value=" + attrValue);
                        }

                        // If we caught a warning, we need to save
                        // off the original return value

                        ex.value = (Object) BigDecimal.valueOf(attrValue);

                        // Re-throw the warning, with the saved value

                        throw ex;
                    }


                }
                if (tracer.isTracing ()) {
                        tracer.trace (" int value=" + attrValue);
                }
                return attrValue;
        }


        //--------------------------------------------------------------------
        // SQLGetTypeInfo
        //--------------------------------------------------------------------

        public  void SQLGetTypeInfo (
                long   hStmt,
                short fSqlType)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Get type info (SQLGetTypeInfo), hStmt=" +
                               hStmt + ", fSqlType=" + fSqlType);
                }

                errorCode = new byte[1];
                getTypeInfo (hStmt, fSqlType, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLMoreResults
        //--------------------------------------------------------------------
        public  boolean SQLMoreResults (
                long hStmt)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];
                boolean rc = true;

                if (tracer.isTracing ()) {
                        tracer.trace ("Get more results (SQLMoreResults), hStmt=" + hStmt);
                }

                errorCode = new byte[1];
                moreResults (hStmt, errorCode);

                // Check for SQL_NO_DATA_FOUND, indicating that this is the
                // end of the result sets.  Set the return code to false and
                // clear the error

                if (errorCode[0] == OdbcDef.SQL_NO_DATA_FOUND) {
                        rc = false;
                        errorCode[0] = 0;
                }

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
                if (tracer.isTracing ()) {
                        tracer.trace ("More results: " + rc);
                }
                return rc;
        }

        //--------------------------------------------------------------------
        // SQLNativeSql
        //--------------------------------------------------------------------

        public  String SQLNativeSql (
                long hDbc,
                String query)
                throws SQLException
        {
                byte    errorCode[];
                byte    nativeQuery[];

                errorCode = new byte[1];

                // Make some estimate as to how large the convert SQL string
                // might be.

                int nativeLen = JdbcOdbcLimits.DEFAULT_NATIVE_SQL_LENGTH;
                  if (query.length () * 4 > nativeLen) {
                        nativeLen = query.length () * 4;
                        if (nativeLen > 32768) {
                                nativeLen = 32768;
                        }
                }

                if (tracer.isTracing ()) {
                        tracer.trace ("Convert native SQL (SQLNativeSql), hDbc=" +
                               hDbc + ", nativeLen=" + nativeLen + ", SQL=" +
                               query);
                }

                nativeQuery = new byte[nativeLen];
                byte[] bQuery = null;
                char[] cQuery = null;
                if (query != null)
                        cQuery = query.toCharArray();
                try {
                        if (query != null)
                                bQuery = CharsToBytes (charSet, cQuery);
                } catch (java.io.UnsupportedEncodingException exxx) {
                        throw (Error) (new InternalError("SQL")).initCause(exxx);
                }

                nativeSql (hDbc, bQuery, nativeQuery, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        hDbc, OdbcDef.SQL_NULL_HSTMT);
                }

                String  sValue = new String ();
                try {
                        sValue = BytesToChars (charSet, nativeQuery);
                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }

                if (tracer.isTracing ()) {
                        tracer.trace ("Native SQL=" + sValue.trim ());
                }
                return sValue.trim ();
        }

        //--------------------------------------------------------------------
        // SQLNumParams
        //--------------------------------------------------------------------
        public  int SQLNumParams (
                long   hStmt)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                int     numParams = 0;

                if (tracer.isTracing ()) {
                        tracer.trace ("Number of parameter markers (SQLNumParams), hStmt=" + hStmt);
                }

                errorCode = new byte[1];
                numParams = numParams (hStmt, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
                if (tracer.isTracing ()) {
                        tracer.trace ("value=" + numParams);
                }
                return numParams;
        }

        //--------------------------------------------------------------------
        // SQLNumResultCols
        //--------------------------------------------------------------------
        public  int SQLNumResultCols (
                long   hStmt)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                int     numCols = 0;

                if (tracer.isTracing ()) {
                        tracer.trace ("Number of result columns (SQLNumResultCols), hStmt=" + hStmt);
                }

                errorCode = new byte[1];
                numCols = numResultCols (hStmt, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        try {
                                standardError (errorCode[0],
                                        OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                        }
                        catch (JdbcOdbcSQLWarning ex) {
                                if (tracer.isTracing ()) {
                                        tracer.trace ("value=" + numCols);
                                }

                                // If we caught a warning, we need to save
                                // off the original return value

                                ex.value = (Object)BigDecimal.valueOf(numCols);

                                // Re-throw the warning, with the saved value

                                throw ex;
                        }
                }
                if (tracer.isTracing ()) {
                        tracer.trace ("value=" + numCols);
                }
                return numCols;
        }

        //--------------------------------------------------------------------
        // SQLParamData
        // Returns -1 if no more data is needed
        //--------------------------------------------------------------------
        public  int SQLParamData (
                long   hStmt)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                int     param = 0;

                if (tracer.isTracing ()) {
                        tracer.trace ("Get parameter number (SQLParamData), hStmt=" +
                               hStmt);
                }

                errorCode = new byte[1];
                param = paramData (hStmt, errorCode);

                // If we need data, return the parameter number that we got
                // This is not an error condition.

                if (errorCode[0] == OdbcDef.SQL_NEED_DATA) {
                        errorCode[0] = 0;
                }
                else {

                        // We don't need any more data, return -1 to signal
                        // this
                        param = -1;
                }

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }

                if (tracer.isTracing ()) {
                        tracer.trace ("Parameter needing data=" + param);
                }

                return param;
        }


        //--------------------------------------------------------------------
        // SQLParamDataInBlock
        // Returns -1 if no more data is needed
        //--------------------------------------------------------------------
        public  int SQLParamDataInBlock (
                long   hStmt,
                int   rowPos)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                int     param = 0;

                if (tracer.isTracing ()) {
                        tracer.trace ("Get parameter number (SQLParamData in block-cursor), hStmt=" +
                               hStmt);
                }

                errorCode = new byte[1];
                param = paramDataInBlock (hStmt, rowPos, errorCode);

                // If we need data, return the parameter number that we got
                // This is not an error condition.

                if (errorCode[0] == OdbcDef.SQL_NEED_DATA) {
                        errorCode[0] = 0;
                }
                else {

                        // We don't need any more data, return -1 to signal
                        // this
                        param = -1;
                }

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }

                if (tracer.isTracing ()) {
                        tracer.trace ("Parameter needing data=" + param);
                }

                return param;
        }

        //--------------------------------------------------------------------
        // SQLPrepare
        //--------------------------------------------------------------------
        public  void SQLPrepare (
                long hStmt,
                String query)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Preparing (SQLPrepare), hStmt=" +
                               hStmt + ", szSqlStr=" + query);
                }

                errorCode = new byte[1];

                byte[] bQuery = null;
                char[] cQuery = null;
                if (query != null)
                        cQuery = query.toCharArray();
                try {
                        if (query != null)
                                bQuery = CharsToBytes (charSet, cQuery);
                } catch (java.io.UnsupportedEncodingException exxx) {
                        throw (Error) (new InternalError("SQL")).initCause(exxx);
                }

                prepare (hStmt, bQuery, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLPutData
        //--------------------------------------------------------------------

        public  void SQLPutData (
                long hStmt,
                byte dataBuf[],
                int dataLen)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Putting data (SQLPutData), hStmt=" +
                               hStmt + ", len=" + dataLen);
//                      dumpByte (dataBuf, dataLen);
                }

                errorCode = new byte[1];
                putData (hStmt, dataBuf, dataLen, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        standardError (errorCode[0],
                                OdbcDef.SQL_NULL_HENV,
                                OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLPrimaryKeys
        //--------------------------------------------------------------------

        public  void SQLPrimaryKeys (
                long hStmt,
                String catalog,
                String schema,
                String table)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Primary keys (SQLPrimaryKeys), hStmt=" +
                               hStmt + ", catalog=" + catalog + ", schema=" +
                               schema + ", table=" + table);
                }

                byte[] bCatalog=null;
                byte[] bSchema=null;
                byte[] bTable=null;
                char[] cCatalog=null;
                char[] cSchema=null;
                char[] cTable=null;
                if (catalog != null)
                        cCatalog=catalog.toCharArray();
                if (schema != null)
                        cSchema=schema.toCharArray();
                if (table != null)
                        cTable=table.toCharArray();
                try {
                        if (catalog != null)
                                bCatalog = CharsToBytes (charSet, cCatalog);
                        if (schema != null)
                                bSchema = CharsToBytes (charSet, cSchema);
                        if (table != null)
                                bTable = CharsToBytes (charSet, cTable);
                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }

                errorCode = new byte[1];
                primaryKeys (hStmt,
                        bCatalog, (catalog == null),
                        bSchema, (schema == null),
                        bTable, (table == null),
                        errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLProcedures
        //--------------------------------------------------------------------

        public  void SQLProcedures (
                long hStmt,
                String catalog,
                String schema,
                String procedure)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Procedures (SQLProcedures), hStmt=" +
                               hStmt + ", catalog=" + catalog + ", schema=" +
                               schema + ", procedure=" + procedure);
                }

                errorCode = new byte[1];

                byte[] bCatalog=null;
                byte[] bSchema=null;
                byte[] bProcedure=null;
                char[] cCatalog=null;
                char[] cSchema=null;
                char[] cProcedure=null;
                if (catalog != null)
                        cCatalog=catalog.toCharArray();
                if (schema != null)
                        cSchema=schema.toCharArray();
                if (procedure != null)
                        cProcedure=procedure.toCharArray();
                try {
                        if (catalog != null)
                                bCatalog = CharsToBytes (charSet, cCatalog);
                        if (schema != null)
                                bSchema = CharsToBytes (charSet, cSchema);
                        if (procedure != null)
                                bProcedure = CharsToBytes (charSet, cProcedure);
                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }

                procedures (hStmt,
                        bCatalog, (catalog == null),
                        bSchema, (schema == null),
                        bProcedure, (procedure == null),
                        errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLProcedureColumns
        //--------------------------------------------------------------------

        public  void SQLProcedureColumns (
                long hStmt,
                String catalog,
                String schema,
                String procedure,
                String column)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Procedure columns (SQLProcedureColumns), hStmt=" +
                               hStmt + ", catalog=" + catalog + ", schema=" +
                               schema + ", procedure=" + procedure + ", column=" + column);
                }

                errorCode = new byte[1];

                byte[] bCatalog=null;
                byte[] bSchema=null;
                byte[] bProcedure=null;
                byte[] bColumn=null;
                char[] cCatalog=null;
                char[] cSchema=null;
                char[] cProcedure=null;
                char[] cColumn=null;
                if (catalog != null)
                        cCatalog=catalog.toCharArray();
                if (schema != null)
                        cSchema=schema.toCharArray();
                if (procedure != null)
                        cProcedure=procedure.toCharArray();
                if (column != null)
                        cColumn=column.toCharArray();
                try {
                        if (catalog != null)
                                bCatalog = CharsToBytes (charSet, cCatalog);
                        if (schema != null)
                                bSchema = CharsToBytes (charSet, cSchema);
                        if (procedure != null)
                                bProcedure = CharsToBytes (charSet, cProcedure);
                        if (column != null)
                                bColumn = CharsToBytes (charSet, cColumn);
                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }

                procedureColumns (hStmt,
                        bCatalog, (catalog == null),
                        bSchema, (schema == null),
                        bProcedure, (procedure == null),
                        bColumn, (column == null),
                        errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLRowCount
        //--------------------------------------------------------------------
        public  int SQLRowCount (
                long   hStmt)
                throws SQLException, JdbcOdbcSQLWarning
        {
                byte    errorCode[];
                int     numRows = 0;

                if (tracer.isTracing ()) {
                        tracer.trace ("Number of affected rows (SQLRowCount), hStmt=" + hStmt);
                }

                errorCode = new byte[1];
                numRows = rowCount (hStmt, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error
                        try {
                                standardError (errorCode[0],
                                        OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                        }
                        catch (JdbcOdbcSQLWarning ex) {

                                if (tracer.isTracing ()) {
                                        tracer.trace ("value=" + numRows);
                                }

                                // If we caught a warning, we need to save
                                // off the original return value

                                ex.value = (Object)BigDecimal.valueOf(numRows);

                                // Re-throw the warning, with the saved value

                                throw ex;
                        }
                }
                if (tracer.isTracing ()) {
                        tracer.trace ("value=" + numRows);
                }
                return numRows;
        }

        //--------------------------------------------------------------------
        // SQLSetConnectOption
        //--------------------------------------------------------------------

        public  void SQLSetConnectOption (
                long   hDbc,
                short fOption,
                int   vParam)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Setting connection option (SQLSetConnectOption), hDbc=" +
                               hDbc + ", fOption=" + fOption + ", vParam=" + vParam);
                }

                errorCode = new byte[1];
                setConnectOption (hDbc, fOption, vParam, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        hDbc, OdbcDef.SQL_NULL_HSTMT);
                }
        }

        //--------------------------------------------------------------------
        // SQLSetConnectOption
        //--------------------------------------------------------------------

        public  void SQLSetConnectOption (
                long   hDbc,
                short fOption,
                String vParam)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Setting connection option string (SQLSetConnectOption), hDbc=" +
                               hDbc + ", fOption=" + fOption + ", vParam=" + vParam);
                }

                errorCode = new byte[1];
                byte[] bVparam = null;
                char[] cVparam = null;
                if (vParam != null)
                        cVparam = vParam.toCharArray();
                try {
                        if (vParam != null)
                                bVparam = CharsToBytes (charSet, cVparam);
                } catch (java.io.UnsupportedEncodingException ex) {
                        throw (Error) (new InternalError("SQL")).initCause(ex);
                }

                setConnectOptionString (hDbc, fOption, bVparam, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        hDbc, OdbcDef.SQL_NULL_HSTMT);
                }
        }

        //--------------------------------------------------------------------
        // SQLSetCursorName
        //--------------------------------------------------------------------

        public  void SQLSetCursorName (
                long   hStmt,
                String szCursor)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Setting cursor name (SQLSetCursorName), hStmt=" +
                               hStmt + ", szCursor=" + szCursor);
                }

                errorCode = new byte[1];

                byte[] bSzcursor = null;
                char[] cSzcursor = null;
                if (szCursor != null)
                        cSzcursor = szCursor.toCharArray();
                try {
                        if (szCursor != null)
                                bSzcursor = CharsToBytes (charSet, cSzcursor);
                } catch (java.io.UnsupportedEncodingException ex) {
                        throw (Error) (new InternalError("SQL")).initCause(ex);
                }

                setCursorName (hStmt, bSzcursor, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLSetStmtOption
        //--------------------------------------------------------------------

        public  void SQLSetStmtOption (
                long   hStmt,
                short fOption,
                int   vParam)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Setting statement option (SQLSetStmtOption), hStmt=" +
                               hStmt + ", fOption=" + fOption + ", vParam=" + vParam);
                }

                errorCode = new byte[1];
                setStmtOption (hStmt, fOption, vParam, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }


        //--------------------------------------------------------------------
        // SQLSetStmtAttr
        //--------------------------------------------------------------------
        public  void SQLSetStmtAttr (
                long   hStmt,
                int fOption,
                int  vParam,
                int   len)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Setting statement option (SQLSetStmtAttr), hStmt=" +
                               hStmt + ", fOption=" + fOption + ", vParam=" + vParam);
                }

                errorCode = new byte[1];

                setStmtAttr (hStmt, fOption, vParam, len, errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }


        }


        //--------------------------------------------------------------------
        // SQLSetStmtAttr Overloaded!
        //--------------------------------------------------------------------
        public  void SQLSetStmtAttrPtr (
                long   hStmt,
                int fOption,
                int[]   vParam,
                int   len,
                long[] buffers) //4486684
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Setting statement option (SQLSetStmtAttr), hStmt=" +
                               hStmt + ", fOption=" + fOption);
                }

                errorCode = new byte[1];
                setStmtAttrPtr (hStmt, fOption, vParam, len, errorCode, buffers); //4486684

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }


        }

        //--------------------------------------------------------------------
        // SQLSetPos
        //--------------------------------------------------------------------
        public boolean SQLSetPos (
                long hStmt,
                int row,
                int operation,
                int lockType)
                throws SQLException
        {
                byte    errorCode[];
                boolean needData = false;

                if (tracer.isTracing ()) {
                        tracer.trace ("Setting row position (SQLSetPos), hStmt=" +
                               hStmt + ", operation = " + operation);
                }

                errorCode = new byte[1];

                setPos (hStmt, row, operation, lockType, errorCode);

                // If SQL_NEED_DATA (data-at-execution parameters) is
                // returned, set the return code.

                if (errorCode[0] == OdbcDef.SQL_NEED_DATA) {
                        if (tracer.isTracing ()) {
                                tracer.trace ("SQL_NEED_DATA returned");
                        }
                        needData = true;
                        errorCode[0] = 0;
                }

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }

                return needData;

        }

        //--------------------------------------------------------------------
        // SQLSpecialColumns
        //--------------------------------------------------------------------

        public  void SQLSpecialColumns (
                long hStmt,
                short fColType,
                String catalog,
                String schema,
                String table,
                int fScope,
                boolean fNullable)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Special columns (SQLSpecialColumns), hStmt=" +
                               hStmt + ", fColType=" + fColType + ",catalog=" +
                               catalog + ", schema=" + schema + ", table=" +
                               table + ", fScope=" + fScope + ", fNullable=" + fNullable);
                }

                errorCode = new byte[1];

                byte[] bCatalog=null;
                byte[] bSchema=null;
                byte[] bTable=null;
                char[] cCatalog=null;
                char[] cSchema=null;
                char[] cTable=null;
                if (catalog != null)
                        cCatalog=catalog.toCharArray();
                if (schema != null)
                        cSchema=schema.toCharArray();
                if (table != null)
                        cTable=table.toCharArray();
                try {
                        if (catalog != null)
                                bCatalog = CharsToBytes (charSet, cCatalog);
                        if (schema != null)
                                bSchema = CharsToBytes (charSet, cSchema);
                        if (table != null)
                                bTable = CharsToBytes (charSet, cTable);
                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }

                specialColumns (hStmt, fColType,
                        bCatalog, (catalog == null),
                        bSchema, (schema == null),
                        bTable, (table == null),
                        fScope, fNullable,
                        errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLStatistics
        //--------------------------------------------------------------------

        public  void SQLStatistics (
                long hStmt,
                String catalog,
                String schema,
                String table,
                boolean unique,
                boolean approximate)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Statistics (SQLStatistics), hStmt=" +
                               hStmt + ",catalog=" + catalog + ", schema=" +
                               schema + ", table=" + table + ", unique=" +
                               unique + ", approximate=" + approximate);
                }

                errorCode = new byte[1];

                byte[] bCatalog=null;
                byte[] bSchema=null;
                byte[] bTable=null;
                char[] cCatalog=null;
                char[] cSchema=null;
                char[] cTable=null;
                if (catalog != null)
                        cCatalog=catalog.toCharArray();
                if (schema != null)
                        cSchema=schema.toCharArray();
                if (table != null)
                        cTable=table.toCharArray();
                try {
                        if (catalog != null)
                                bCatalog = CharsToBytes (charSet, cCatalog);
                        if (schema != null)
                                bSchema = CharsToBytes (charSet, cSchema);
                        if (table != null)
                                bTable = CharsToBytes (charSet, cTable);
                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }

                statistics (hStmt,
                        bCatalog, (catalog == null),
                        bSchema, (schema == null),
                        bTable, (table == null),
                        unique, approximate,
                        errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLTables
        //--------------------------------------------------------------------

        public  void SQLTables (
                long hStmt,
                String catalog,
                String schema,
                String table,
                String types)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Tables (SQLTables), hStmt=" +
                               hStmt + ",catalog=" + catalog + ", schema=" +
                               schema + ", table=" + table + ", types=" + types);
                }

                errorCode = new byte[1];

                byte[] bCatalog=null;
                byte[] bSchema=null;
                byte[] bTable=null;
                byte[] bTypes=null;
                char[] cCatalog=null;
                char[] cSchema=null;
                char[] cTable=null;
                char[] cTypes=null;
                if (catalog != null)
                        cCatalog=catalog.toCharArray();
                if (schema != null)
                        cSchema=schema.toCharArray();
                if (table != null)
                        cTable=table.toCharArray();
                if (types != null)
                        cTypes=types.toCharArray();
                try {
                        if (catalog != null)
                                bCatalog = CharsToBytes (charSet, cCatalog);
                        if (schema != null)
                                bSchema = CharsToBytes (charSet, cSchema);
                        if (table != null)
                                bTable = CharsToBytes (charSet, cTable);
                        if (types != null)
                                bTypes = CharsToBytes (charSet, cTypes);
                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }

                tables (hStmt,
                        bCatalog, (catalog == null),
                        bSchema, (schema == null),
                        bTable, (table == null),
                        bTypes, (types == null),
                        errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLTablePrivileges
        //--------------------------------------------------------------------

        public  void SQLTablePrivileges (
                long hStmt,
                String catalog,
                String schema,
                String table)
                throws SQLException, SQLWarning
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Tables (SQLTables), hStmt=" +
                               hStmt + ",catalog=" + catalog + ", schema=" +
                               schema + ", table=" + table);
                }

                errorCode = new byte[1];

                byte[] bCatalog=null;
                byte[] bSchema=null;
                byte[] bTable=null;
                char[] cCatalog=null;
                char[] cSchema=null;
                char[] cTable=null;
                if (catalog != null)
                        cCatalog=catalog.toCharArray();
                if (schema != null)
                        cSchema=schema.toCharArray();
                if (table != null)
                        cTable=table.toCharArray();
                try {
                        if (catalog != null)
                                bCatalog = CharsToBytes (charSet, cCatalog);
                        if (schema != null)
                                bSchema = CharsToBytes (charSet, cSchema);
                        if (table != null)
                                bTable = CharsToBytes (charSet, cTable);
                } catch (java.io.UnsupportedEncodingException exx) {
                        throw (Error) (new InternalError("SQL")).initCause(exx);
                }

                tablePrivileges (hStmt,
                        bCatalog, (catalog == null),
                        bSchema, (schema == null),
                        bTable, (table == null),
                        errorCode);

                if (errorCode[0] != 0) {

                        // Process the error

                        standardError (errorCode[0], OdbcDef.SQL_NULL_HENV,
                                        OdbcDef.SQL_NULL_HDBC, hStmt);
                }
        }

        //--------------------------------------------------------------------
        // SQLTransact
        //--------------------------------------------------------------------

        public  void SQLTransact (
                long hEnv,
                long hDbc,
                short fType)
                throws SQLException
        {
                byte    errorCode[];

                if (tracer.isTracing ()) {
                        tracer.trace ("Transaction (SQLTransact), hEnv=" +
                               hEnv + ", hDbc=" + hDbc + ", fType=" + fType);
                }

                errorCode = new byte[1];
                transact (hEnv, hDbc, fType, errorCode);

                if (errorCode[0] != 0) {
                        throwGenericSQLException ();
                }
        }

        //====================================================================
        // Non-SQL methods
        //====================================================================

        //--------------------------------------------------------------------
        // bufferToInt
        // Returns an integer from the native buffer supplied.  In some
        // cases, data has to be bound within ODBC.  The data that is placed
        // in the bound buffers is in native format - we don't want to have
        // to interpret this from within Java.  So, we'll let the native
        // bridge code convert it for us.
        //--------------------------------------------------------------------

        // 4412437 - begin

        public native int bufferToInt (
                byte b[]);
        public native float bufferToFloat (
                byte b[]);

        public native double bufferToDouble (
                byte b[]);
        //4532162
        public native long bufferToLong (
                byte b[]);

        public native void convertDateString(byte dataBuf[], byte dateString[]);

        public native void getDateStruct(byte dataBuf[], int year, int month, int day);

        public native void convertTimeString(byte dataBuf[], byte timeString[]);

        public native void getTimeStruct(byte dataBuf[], int hour, int minutes, int seconds);

        // 4412437 - end
        //4532162
        public native void getTimestampStruct(byte dataBuf[], int year, int month, int day, int hour, int minutes, int seconds, long nanos);
        //4532162
        public native void convertTimestampString(byte dataBuf[], byte timestampString[]);

        // 4691886
        public static native int getSQLLENSize();
        public static native void intToBytes(int i, byte b[]);
        public static native void longToBytes(long l, byte b[]);

        // 4641016
        public static native void intTo4Bytes(int i, byte b[]);


        //--------------------------------------------------------------------
        // convertWarning
        // Given a SQLWarning, look for certain SQLStates and generate the
        // proper SQLWarning sub-types (i.e. DataTruncation)
        //--------------------------------------------------------------------

        public static SQLWarning convertWarning (
                JdbcOdbcSQLWarning w)
        {
                SQLWarning convertedWarning = w;

                // If a data truncation warning was generated, convert
                // it to a DataTruncation exception type

                if ((w.getSQLState ()).equals ("01004")) {
                        DataTruncation dt = new DataTruncation (-1, false, true,
                                                0, 0);
                        convertedWarning = dt;
                }
                return convertedWarning;
        }

        //====================================================================
        // Protected methods
        //====================================================================

        //--------------------------------------------------------------------
        // allocConnect
        //--------------------------------------------------------------------
        protected native long  allocConnect (
                long hEnv,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // allocEnv
        //--------------------------------------------------------------------
        protected native long  allocEnv (
                byte errorCode[]);

        //--------------------------------------------------------------------
        // allocStmt
        //--------------------------------------------------------------------
        protected native long allocStmt (
                long hStmt,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // allocStmt
        //--------------------------------------------------------------------
        protected native void cancel (
                long hStmt,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // bindColAtExec
        //--------------------------------------------------------------------
        protected native void bindColAtExec (
                long hStmt,
                int ipar,
                int SQLtype,
                //4691886
                byte lenInd[],
                byte dataBuf[],
                long buffers[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // bindColBinary
        //--------------------------------------------------------------------
        protected native void bindColBinary (
                long hStmt,
                int icol,
                Object value[],
                //4691886
                byte lenInd[],
                int colDesc,
                byte dataBuf[],
                long buffers[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // bindColDate
        //--------------------------------------------------------------------
        protected native void bindColDate (
                long hStmt,
                int icol,
                int year[],
                int month[],
                int day[],
                //4691886
                byte lenInd[],
                byte dataBuf[],
                long buffers[],
                byte errorCode[]);


        //--------------------------------------------------------------------
        // bindColDefault
        //--------------------------------------------------------------------
        protected native void bindColDefault (
                long hStmt,
                int ipar,
                byte rgbValue[],
                byte pcbValue[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // bindColDouble
        //--------------------------------------------------------------------
        protected native void bindColDouble (
                long hStmt,
                int icol,
                double value[],
                //4691886
                byte lenInd[],
                byte dataBuf[],
                long buffers[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // bindColFloat
        //--------------------------------------------------------------------
        protected native void bindColFloat (
                long hStmt,
                int icol,
                float values[],
                //4691886
                byte lenInd[],
                byte dataBuf[],
                long buffers[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // bindColInteger
        //--------------------------------------------------------------------
        protected native void bindColInteger (
                long hStmt,
                int icol,
                int value[],
                //4691886
                byte lenInd[],
                byte dataBuf[],
                long buffers[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // bindColString
        //--------------------------------------------------------------------
        protected native void bindColString (
                long hStmt,
                int icol,
                int type,
                Object value[],
                int descLen,
                //4691886
                byte lenInd[],
                byte dataBuf[],
                long buffers[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // bindColTime
        //--------------------------------------------------------------------
        protected native void bindColTime (
                long hStmt,
                int icol,
                int hour[],
                int min[],
                int sec[],
                //4691886
                byte lenInd[],
                byte dataBuf[],
                long buffers[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // bindColTimestamp
        //--------------------------------------------------------------------
        protected native void bindColTimestamp (
                long hStmt,
                int icol,
                int year[],
                int month[],
                int day[],
                int hour[],
                int min[],
                int sec[],
                int nanos[],
                //4691886
                byte lenInd[],
                byte dataBuf[],
                long buffers[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // bindInParameterAtExec
        //--------------------------------------------------------------------
        protected native void bindInParameterAtExec (
                long hStmt,
                int ipar,
                int type,
                int len,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        // 4641016
        //--------------------------------------------------------------------
        // bindInOutParameterAtExec
        //--------------------------------------------------------------------
        protected native void bindInOutParameterAtExec (
                long hStmt,
                int ipar,
                int CType,
                int SQLType,
                int dataBufLen,
                byte dataBuf[],
                int streamLength,
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        //--------------------------------------------------------------------
        // bindInParameterBinary
        //--------------------------------------------------------------------
        protected native void bindInParameterBinary (
                long hStmt,
                int ipar,
                int SQLtype,
                byte value[],
                int precision,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);
        //--------------------------------------------------------------------
        // bindInParameterDate
        //--------------------------------------------------------------------
        protected native void bindInParameterDate (
                long hStmt,
                int ipar,
                int year,
                int month,
                int day,
                byte dataBuf[],
                byte errorCode[],
                long buffers[]);

        //--------------------------------------------------------------------
        // bindInParameterDouble
        //--------------------------------------------------------------------
        protected native void bindInParameterDouble (
                long hStmt,
                int ipar,
                int SQLtype,
                int scale,
                double value,
                byte dataBuf[],
                byte errorCode[],
                long buffers[]);

        //--------------------------------------------------------------------
        // bindInParameterFloat
        //--------------------------------------------------------------------
        // Fix 4532167. Changed float to double
        protected native void bindInParameterFloat (
                long hStmt,
                int ipar,
                int SQLtype,
                int scale,
                double value,
                byte dataBuf[],
                byte errorCode[],
                long buffers[]);


        //Bug 4495452
        //-------------------------------------------------------------------
        // bindInParameterBigint
        //-------------------------------------------------------------------
        protected native void bindInParameterBigint (
                long hStmt,
                int ipar,
                int SQLtype,
                int scale,
                long value,
                byte dataBuf[],
                byte errorCode[],
                long buffers[]);


        //--------------------------------------------------------------------
        // bindInParameterInteger
        //--------------------------------------------------------------------
        protected native void bindInParameterInteger (
                long hStmt,
                int ipar,
                int SQLtype,
                int value,
                byte dataBuf[],
                byte errorCode[],
                long buffers[]);

        //--------------------------------------------------------------------
        // bindInParameterNull
        //--------------------------------------------------------------------
        protected native void bindInParameterNull (
                long hStmt,
                int ipar,
                int SQLtype,
                int prec,
                int scale,
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        //--------------------------------------------------------------------
        // bindInParameterString
        //--------------------------------------------------------------------
        protected native void bindInParameterString (
                long hStmt,
                int ipar,
                int SQLtype,
                byte value[],
                int precision,
                int scale,
                byte dataBuf[],
                byte errorCode[],
                long buffers[]);

        //--------------------------------------------------------------------
        // bindInParameterTime
        //--------------------------------------------------------------------
        protected native void bindInParameterTime (
                long hStmt,
                int ipar,
                int hours,
                int minutes,
                int seconds,
                byte dataBuf[],
                byte errorCode[],
                long buffers[]);

        //--------------------------------------------------------------------
        // bindInParameterTimestamp
        //--------------------------------------------------------------------
        protected native void bindInParameterTimestamp (
                long hStmt,
                int ipar,
                int year,
                int month,
                int day,
                int hours,
                int minutes,
                int seconds,
                int nanos,
                byte dataBuf[],
                byte errorCode[],
                long buffers[]);

        //--------------------------------------------------------------------
        // bindOutParameterString
        //--------------------------------------------------------------------
        protected native void bindOutParameterString (
                long hStmt,
                int ipar,
                int SQLtype,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        // bug 4412437
        //--------------------------------------------------------------------
        // bindInOutParameterDate
        //--------------------------------------------------------------------
        protected native void bindInOutParameterDate(
                long hStmt,
                int ipar,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        // bug 4412437
        //--------------------------------------------------------------------
        // bindInOutParameterTime
        //--------------------------------------------------------------------
        protected native void bindInOutParameterTime(
                long hStmt,
                int ipar,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        //--------------------------------------------------------------------
        // bindInOutParameterString
        //--------------------------------------------------------------------
        protected native void bindInOutParameterString (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        // bug 4412437
        //--------------------------------------------------------------------
        // bindInOutParameterStr
        //--------------------------------------------------------------------
        protected native void bindInOutParameterStr (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[],
                int strLenInd);

        // bug 4412437
        //--------------------------------------------------------------------
        // bindInOutParameterBin
        //--------------------------------------------------------------------
        protected native void bindInOutParameterBin (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[],
                int strLenInd);


        //--------------------------------------------------------------------
        // bindInOutParameterBinary
        //--------------------------------------------------------------------
        protected native void bindInOutParameterBinary (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        // bug 4412437
        //--------------------------------------------------------------------
        // bindInOutParameterFixed
        //--------------------------------------------------------------------
        protected native void bindInOutParameterFixed (
                long hStmt,
                int ipar,
                int CType,
                int SQLtype,
                int maxLen,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        //--------------------------------------------------------------------
        // bindInOutParameterTimeStamp
        //--------------------------------------------------------------------
        protected native void bindInOutParameterTimeStamp (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        //--------------------------------------------------------------------
        // bindInOutParameter
        //--------------------------------------------------------------------
        protected native void bindInOutParameter (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                int scale,
                double value,
                byte dataBuf[],
                byte errorCode[],
                long buffers[]);

        //--------------------------------------------------------------------
        // bindInOutParameterNull
        //--------------------------------------------------------------------
        protected native void bindInOutParameterNull (
                long hStmt,
                int ipar,
                int SQLtype,
                int prec,
                int scale,
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        //4532162
        //--------------------------------------------------------------------
        // bindInOutParameterTimestamp
        //--------------------------------------------------------------------
        protected native void bindInOutParameterTimestamp(
                long hStmt,
                int ipar,
                int precision,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        //---------------------------------------------------------------------
        // bindInParameterStringArray
        //---------------------------------------------------------------------
        protected native void bindInParameterStringArray (
                long hStmt,
                int ipar,
                int SQLtype,
                Object dataBuf[],
                byte strBuf[],
                int colDesc,
                int colScale,
                int buffers[],
                byte errorCode[]);

        //---------------------------------------------------------------------
        // bindInParameterIntegerArray
        //---------------------------------------------------------------------
        protected native void bindInParameterIntegerArray (
                long hStmt,
                int ipar,
                int SQLtype,
                int dataBuf[],
                int buffers[],
                byte errorCode[]);

        //---------------------------------------------------------------------
        // bindInParameterFloatArray
        //---------------------------------------------------------------------
        protected native void bindInParameterFloatArray (
                long hStmt,
                int ipar,
                int SQLtype,
                int scale,
                float dataBuf[],
                int buffers[],
                byte errorCode[]);

        //---------------------------------------------------------------------
        // bindInParameterDoubleArray
        //---------------------------------------------------------------------
        protected native void bindInParameterDoubleArray (
                long hStmt,
                int ipar,
                int SQLtype,
                int scale,
                double dataBuf[],
                int buffers[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // bindInParameterDateArray
        //--------------------------------------------------------------------
        protected native void bindInParameterDateArray (
                long hStmt,
                int ipar,
                int year[],
                int month[],
                int day[],
                byte dataBuf[],
                byte errorCode[],
                int buffers[]);

        //--------------------------------------------------------------------
        // bindInParameterTimeArray
        //--------------------------------------------------------------------
        protected native void bindInParameterTimeArray (
                long hStmt,
                int ipar,
                int hours[],
                int minutes[],
                int seconds[],
                byte dataBuf[],
                byte errorCode[],
                int buffers[]);

        //--------------------------------------------------------------------
        // bindInParameterTimestampArray
        //--------------------------------------------------------------------
        protected native void bindInParameterTimestampArray (
                long hStmt,
                int ipar,
                int year[],
                int month[],
                int day[],
                int hours[],
                int minutes[],
                int seconds[],
                int nanos[],
                byte dataBuf[],
                byte errorCode[],
                int buffers[]);

        //---------------------------------------------------------------------
        // bindInParameterBinaryArray
        //---------------------------------------------------------------------
        protected native void bindInParameterBinaryArray (
                long hStmt,
                int ipar,
                int SQLtype,
                Object value[],
                int colDesc,
                byte dataBuf[],
                int buffers[],
                byte errorCode[]);

        //---------------------------------------------------------------------
        // bindInParameterAtExecArray
        //---------------------------------------------------------------------
        protected native void bindInParameterAtExecArray (
                long hStmt,
                int ipar,
                int SQLtype,
                int colDesc,
                byte dataBuf[],
                int lenBuf[],
                byte errorCode[]);

        //4532162
        //--------------------------------------------------------------------
        // bindOutParameterNull
        //--------------------------------------------------------------------
        protected native void bindOutParameterNull (
                long hStmt,
                int ipar,
                int SQLtype,
                int prec,
                int scale,
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        //4532162
        //--------------------------------------------------------------------
        // bindOutParameterFixed
        //--------------------------------------------------------------------
        protected native void bindOutParameterFixed (
                long hStmt,
                int ipar,
                int CType,
                int SQLtype,
                int maxLen,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        //4532162
        //--------------------------------------------------------------------
        // bindOutParameterBinary
        //--------------------------------------------------------------------
        protected native void bindOutParameterBinary (
                long hStmt,
                int ipar,
                int SQLtype,
                int precision,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        //4532162
        //--------------------------------------------------------------------
        // bindOutParameterDate
        //--------------------------------------------------------------------
        protected native void bindOutParameterDate(
                long hStmt,
                int ipar,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        //4532162
        //--------------------------------------------------------------------
        // bindOutParameterTime
        //--------------------------------------------------------------------
        protected native void bindOutParameterTime(
                long hStmt,
                int ipar,
                int scale,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        //4532162
        //--------------------------------------------------------------------
        // bindOutParameterTimestamp
        //--------------------------------------------------------------------
        protected native void bindOutParameterTimestamp (
                long hStmt,
                int ipar,
                int precision,
                byte dataBuf[],
                byte lenBuf[],
                byte errorCode[],
                long buffers[]);

        //--------------------------------------------------------------------
        // browseConnect
        //--------------------------------------------------------------------
        protected native void browseConnect (
                long hDbc,
                byte bConnectString[],
                byte connStrOut[],
                byte errorCode[]);


        //--------------------------------------------------------------------
        // colAttributes
        //--------------------------------------------------------------------
        protected native int colAttributes (
                long hStmt,
                int column,
                int type,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // colAttributesString
        //--------------------------------------------------------------------
        protected native void colAttributesString (
                long hStmt,
                int column,
                int type,
                byte rgbDesc[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // columns
        //--------------------------------------------------------------------
        protected native void columns (
                long hStmt,
                byte[] bCatalog, boolean catalogNull,
                byte[] bSchema, boolean schemaNull,
                byte[] bTable, boolean tableNull,
                byte[] bColumn, boolean columnNull,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // columnPrivileges
        //--------------------------------------------------------------------
        protected native void columnPrivileges (
                long hStmt,
                byte[] bCatalog, boolean catalogNull,
                byte[] bSchema, boolean schemaNull,
                byte[] bTable, boolean tableNull,
                byte[] bColumn, boolean columnNull,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // describeParam
        //--------------------------------------------------------------------
        protected native int describeParam (
                long hStmt,
                int param,
                int returnParam,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // disconnect
        //--------------------------------------------------------------------
        protected native void disconnect (
                long hDbc,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // driverConnect
        //--------------------------------------------------------------------
        protected native void driverConnect (
                long hDbc,
                byte[] bConnectString,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // error
        //--------------------------------------------------------------------
        protected native int error (
                long hEnv,
                long hDbc,
                long hStmt,
                byte sqlState[],
                byte errorMessage[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // execDirect
        //--------------------------------------------------------------------
        protected native void execDirect (
                long hStmt,
                byte[] bQuery,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // execute
        //--------------------------------------------------------------------
        protected native void execute (
                long hStmt,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // fetch
        //--------------------------------------------------------------------
        protected native void fetch (
                long hStmt,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // fetchScroll
        //--------------------------------------------------------------------
        protected native void fetchScroll (
                long hStmt,
                short orientation,
                int offset,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // foriegnKeys
        //--------------------------------------------------------------------
        protected native void foreignKeys (
                long hStmt,
                byte[] bPKcatalog, boolean PKcatalogNull,
                byte[] bPKschema, boolean PKschemaNull,
                byte[] bPKtable, boolean PKtableNull,
                byte[] bFKcatalog, boolean FKcatalogNull,
                byte[] bFKschema, boolean FKschemaNull,
                byte[] bFKtable, boolean FKtableNull,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // freeConnect
        //--------------------------------------------------------------------
        protected native void freeConnect (
                long hDbc,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // freeEnv
        //--------------------------------------------------------------------
        protected native void freeEnv (
                long hEnv,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // freeStmt
        //--------------------------------------------------------------------
        protected native void freeStmt (
                long hEnv,
                int fOption,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getConnectOption
        //--------------------------------------------------------------------
        protected native long getConnectOption (
                long hDbc,
                short fOption,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getConnectOptionString
        //--------------------------------------------------------------------
        protected native void getConnectOptionString (
                long hDbc,
                short fOption,
                byte szParam[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getCursorName
        //--------------------------------------------------------------------
        protected native void getCursorName (
                long hStmt,
                byte szCursor[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getStmtOption
        //--------------------------------------------------------------------
        protected native long getStmtOption (
                long hStmt,
                short fOption,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getStmtAttr
        //--------------------------------------------------------------------
        protected native int getStmtAttr (
                long hStmt,
                int fObtion,
                byte errorCode[]);


        //--------------------------------------------------------------------
        // getDataBinary
        //--------------------------------------------------------------------
        protected native int getDataBinary (
                long hStmt,
                int column,
                int cType,
                byte b[],
                int length,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getDataDouble
        //--------------------------------------------------------------------
        protected native double getDataDouble (
                long hStmt,
                int column,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getDataFloat
        //--------------------------------------------------------------------
        // Fix 4532167. Changed float to double
        protected native double getDataFloat (
                long hStmt,
                int column,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getDataInteger
        //--------------------------------------------------------------------
        protected native int getDataInteger (
                long hStmt,
                int column,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getDataString
        //--------------------------------------------------------------------
        protected native int getDataString (
                long hStmt,
                int column,
                byte rgbValue[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getDataStringDate
        //--------------------------------------------------------------------
        protected native void getDataStringDate (
                long hStmt,
                int column,
                byte rgbValue[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getDataStringTime
        //--------------------------------------------------------------------
        protected native void getDataStringTime (
                long hStmt,
                int column,
                byte rgbValue[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getDataStringTimestamp
        //--------------------------------------------------------------------
        protected native void getDataStringTimestamp (
                long hStmt,
                int column,
                byte rgbValue[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getInfo
        //--------------------------------------------------------------------
        protected native int getInfo (
                long hDbc,
                short fInfoType,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getInfoShort
        //--------------------------------------------------------------------
        protected native int getInfoShort (
                long hDbc,
                short fInfoType,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getInfoString
        //--------------------------------------------------------------------
        protected native void getInfoString (
                long hDbc,
                short fInfoType,
                byte szParam[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // getTypeInfo
        //--------------------------------------------------------------------
        protected native void getTypeInfo (
                long hStmt,
                short fSqlType,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // moreResults
        //--------------------------------------------------------------------
        protected native void moreResults (
                long hStmt,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // nativeSql
        //--------------------------------------------------------------------
        protected native void nativeSql (
                long hDbc,
                byte[] bQuery,
                byte nativeQuery[],
                byte errorCode[]);

        //--------------------------------------------------------------------
        // numParams
        //--------------------------------------------------------------------
        protected native int numParams (
                long hStmt,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // numResultCols
        //--------------------------------------------------------------------
        protected native int numResultCols (
                long hStmt,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // paramData
        //--------------------------------------------------------------------
        protected native int paramData (
                long hStmt,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // paramDataInBlock
        //--------------------------------------------------------------------
        protected native int paramDataInBlock (
                long hStmt,
                int rowPos,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // prepare
        //--------------------------------------------------------------------
        protected native void prepare (
                long hStmt,
                byte[] bQuery,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // primaryKeys
        //--------------------------------------------------------------------
        protected native void primaryKeys (
                long hStmt,
                byte[] bCatalog, boolean catalogNull,
                byte[] bSchema, boolean schemaNull,
                byte[] bTable, boolean tableNull,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // procedures
        //--------------------------------------------------------------------
        protected native void procedures (
                long hStmt,
                byte[] bCatalog, boolean catalogNull,
                byte[] bSchema, boolean schemaNull,
                byte[] bProcedure, boolean procedureNull,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // procedureColumns
        //--------------------------------------------------------------------
        protected native void procedureColumns (
                long hStmt,
                byte[] bCatalog, boolean catalogNull,
                byte[] bSchema, boolean schemaNull,
                byte[] bProcedure, boolean procedureNull,
                byte[] bColumn, boolean columnNull,
                byte errorCode[]);
        //--------------------------------------------------------------------
        // putData
        //--------------------------------------------------------------------
        protected native void putData (
                long hStmt,
                byte dataBuf[],
                int dataLen,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // rowCount
        //--------------------------------------------------------------------
        protected native int rowCount (
                long hStmt,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // setConnectOption
        //--------------------------------------------------------------------
        protected native void setConnectOption (
                long hDbc,
                short fOption,
                int vParam,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // setConnectOptionString
        //--------------------------------------------------------------------
        protected native void setConnectOptionString (
                long hDbc,
                short fOption,
                byte[] bVparam,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // setCursorName
        //--------------------------------------------------------------------
        protected native void setCursorName (
                long hStmt,
                byte[] bSzcursor,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // setStmtOption
        //--------------------------------------------------------------------
        protected native void setStmtOption (
                long hStmt,
                short fOption,
                int vParam,
                byte errorCode[]);


        //--------------------------------------------------------------------
        // setStmtAttr
        //--------------------------------------------------------------------
        protected native void setStmtAttr (
                long hStmt,
                int fOption,
                int vParam,
                int len,
                byte errorCode[]);


        //--------------------------------------------------------------------
        // setStmtAttr Overloaded.
        //--------------------------------------------------------------------
        protected native void setStmtAttrPtr (
                long hStmt,
                int fOption,
                int vParam[],
                int len,
                byte errorCode[],
                long buffers[]); //4486684


        //--------------------------------------------------------------------
        // setPos.
        //--------------------------------------------------------------------
        protected native void setPos (
                long hStmt,
                int row,
                int operation,
                int lockType,
                byte errorCode[]);


        //--------------------------------------------------------------------
        // specialColumns
        //--------------------------------------------------------------------
        protected native void specialColumns (
                long hStmt, short fColType,
                byte[] bCatalog, boolean catalogNull,
                byte[] bSchema, boolean schemaNull,
                byte[] bTable, boolean tableNull,
                int fScope, boolean fNullable,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // statistics
        //--------------------------------------------------------------------
        protected native void statistics (
                long hStmt,
                byte[] bCatalog, boolean catalogNull,
                byte[] bSchema, boolean schemaNull,
                byte[] bTable, boolean tableNull,
                boolean unique, boolean approximate,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // tables
        //--------------------------------------------------------------------
        protected native void tables (
                long hStmt,
                byte[] bCatalog, boolean catalogNull,
                byte[] bSchema, boolean schemaNull,
                byte[] bTable, boolean tableNull,
                byte[] bTypes, boolean typesNull,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // tablePrivileges
        //--------------------------------------------------------------------
        protected native void tablePrivileges (
                long hStmt,
                byte[] bCatalog, boolean catalogNull,
                byte[] bSchema, boolean schemaNull,
                byte[] bTable, boolean tableNull,
                byte errorCode[]);

        //--------------------------------------------------------------------
        // transact
        //--------------------------------------------------------------------
        protected native void transact (
                long hEnv,
                long hDbc,
                short fType,
                byte errorCode[]);


        protected static native void ReleaseStoredBytes (long x1, long x2);
        protected static native void ReleaseStoredChars (long s1, long s2);
        protected static native void ReleaseStoredIntegers(long x1, long x2); //4486684

        //--------------------------------------------------------------------
        // createSQLException
        // Get state, message, and native error code from SQLError.  If
        // multiple errors exist, chain them together.  Return the first
        // error in the list.
        //--------------------------------------------------------------------

        SQLException createSQLException (
                long hEnv,
                long hDbc,
                long hStmt)
        {
                byte    sqlState[];
                byte    errorMsg[];
                byte    errorCode[];
                int     nativeError;
                boolean done = false;
                SQLException    firstEx = null;
                SQLException    lastEx = null;

                if (tracer.isTracing ()) {
                        tracer.trace ("ERROR - Generating SQLException...");
                }

                while (!done) {
                        errorCode = new byte[1];
                        sqlState = new byte[6];
                        errorMsg = new byte[
                                JdbcOdbcLimits.DEFAULT_BUFFER_LENGTH];

                        nativeError = error (hEnv, hDbc, hStmt, sqlState,
                                errorMsg, errorCode);

                        if (errorCode[0] != OdbcDef.SQL_SUCCESS) {
                                done = true;
                        }
                        else {
                                SQLException    ex = null;

                                String msg = new String();
                                String state = new String();
                                try {
                                    msg = BytesToChars (charSet, errorMsg);
                                    state = BytesToChars (charSet, sqlState);
                                } catch (java.io.UnsupportedEncodingException exxx) {
                                    throw (Error) (new InternalError("SQL")).initCause(exxx);
                                }


                                // Create a new exception record

                                ex = new SQLException (msg.trim (),
                                                state.trim (), nativeError);

                                // If we don't have an exception yet, set the
                                // first one the in chain.  Otherwise, set
                                // the chain pointers.

                                if (firstEx == null) {
                                        firstEx = ex;
                                }
                                else {
                                        lastEx.setNextException (ex);
                                }

                                // Save our last exception so we can chain
                                lastEx = ex;
                        }
                }

                // If we didn't get an error message from SQLError, make
                // one for ourselves

                if (firstEx == null) {
                        String msg   = "General error";
                        String state = "S1000";

                        if (tracer.isTracing ()) {
                                tracer.trace ("ERROR - " + state + " " + msg);
                        }
                        firstEx = new SQLException (msg, state);
                }
                return firstEx;
        }

        //--------------------------------------------------------------------
        // createSQLWarning
        // Get state, message, and native error code from SQLError.  If
        // multiple warnings exist, chain them together.  Return the first
        // warning in the list.
        //--------------------------------------------------------------------

        SQLWarning createSQLWarning (
                long hEnv,
                long hDbc,
                long hStmt)
        {
                byte    sqlState[];
                byte    errorMsg[];
                byte    errorCode[];
                int     nativeError;
                boolean done = false;
                SQLWarning      firstEx = null;
                SQLWarning      lastEx = null;

                if (tracer.isTracing ()) {
                        tracer.trace ("WARNING - Generating SQLWarning...");
                }

                while (!done) {
                        errorCode = new byte[1];
                        sqlState = new byte[6];
                        errorMsg = new byte[
                                JdbcOdbcLimits.DEFAULT_BUFFER_LENGTH];

                        nativeError = error (hEnv, hDbc, hStmt, sqlState,
                                errorMsg, errorCode);

                        if (errorCode[0] != OdbcDef.SQL_SUCCESS) {
                                done = true;
                        }
                        else {
                                JdbcOdbcSQLWarning      ex = null;
                                String msg = new String();
                                String state = new String();
                                try {
                                    msg = BytesToChars (charSet, errorMsg);
                                    state = BytesToChars (charSet, sqlState);
                                } catch (java.io.UnsupportedEncodingException exx) {
                                    throw (Error) (new InternalError("SQL")).initCause(exx);
                                }

                                // Create a new warning record

                                ex = new JdbcOdbcSQLWarning (msg.trim (),
                                                state.trim (),
                                                nativeError);

                                // If we don't have a warning yet, set the
                                // first one the in chain.  Otherwise, set
                                // the chain pointers.

                                if (firstEx == null) {
                                        firstEx = ex;
                                }
                                else {
                                        lastEx.setNextWarning (ex);
                                }

                                // Save our last warning so we can chain
                                lastEx = ex;
                        }
                }

                // If we didn't get an warning message from SQLError, make
                // one for ourselves

                if (firstEx == null) {
                        String msg   = "General warning";
                        String state = "S1000";

                        if (tracer.isTracing ()) {
                                tracer.trace ("WARNING - " + state + " " + msg);
                        }
                        firstEx = new JdbcOdbcSQLWarning (msg, state);
                }
                return firstEx;
        }

        //--------------------------------------------------------------------
        // throwGenericSQLException
        // Throw a generic SQLException object.
        //--------------------------------------------------------------------

        void throwGenericSQLException () throws SQLException
        {
                String msg   = "General error";
                String state = "S1000";

                if (tracer.isTracing ()) {
                        tracer.trace ("ERROR - " + state + " " + msg);
                }
                throw new SQLException (msg, state);
        }

        //--------------------------------------------------------------------
        // standardError
        // Checks for standard error codes, and throws either a SQLException
        // or SQLWarning
        //--------------------------------------------------------------------

        void standardError (
                short errorCode,
                long hEnv,
                long hDbc,
                long hStmt)
                throws SQLException, SQLWarning
        {
                String msg;

                if (tracer.isTracing ()) {
                        tracer.trace ("RETCODE = " + errorCode);
                }

                switch (errorCode) {

                // If this is a real error (SQL_ERROR), throw a
                // SQLException

                case OdbcDef.SQL_ERROR:
                        throw createSQLException (hEnv, hDbc, hStmt);

                // If this is a success with addition information
                // (SQL_SUCCESS_WITH_INFO), throw a SQLWarning

                case OdbcDef.SQL_SUCCESS_WITH_INFO:
                        throw createSQLWarning (hEnv, hDbc, hStmt);

                // Check for an invalid handle (SQL_INVALID_HANDLE)

                case OdbcDef.SQL_INVALID_HANDLE:
                        msg = "Invalid handle";
                        if (tracer.isTracing ()) {
                                tracer.trace ("ERROR - " + msg);
                        }
                        throw new SQLException (msg);

                // Check for no data found

                case OdbcDef.SQL_NO_DATA:
                        msg = "No data found";
                        if (tracer.isTracing ()) {
                                tracer.trace ("ERROR - " + msg);
                        }
                        throw new SQLException (msg);

                // If this is some other type of error, throw a
                // generic SQLException

                default:
                        throwGenericSQLException ();
                }
        }

        //--------------------------------------------------------------------
        // RFE 4641013.
        // getTracer
        // This returns the tracer.
        //--------------------------------------------------------------------
        public JdbcOdbcTracer getTracer() {
             return tracer;
        }
        //====================================================================
        // Data attributes
        //====================================================================

        public final static int MajorVersion = 2;
        public final static int MinorVersion = 0001;
        public String charSet;
        public String odbcDriverName;

        private static java.util.Map hstmtMap; //4524683
        public JdbcOdbcTracer tracer = new JdbcOdbcTracer(); // RFE 4641013

}
