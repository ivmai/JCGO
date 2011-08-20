/*
 * This file is modified by Ivan Maidanski <ivmai@ivmaisoft.com>
 * Project name: JCGO-SUNAWT (http://www.ivmaisoft.com/jcgo/)
 */

/*
 * @(#)JdbcOdbcPreparedStatement.java   1.39 01/07/30
 *
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

//----------------------------------------------------------------------------
//
// Module:      JdbcOdbcPreparedStatement.java
//
// Description: Impementation of the PreparedStatement interface class
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

import java.io.Reader;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.math.*;
import java.sql.*;

public class JdbcOdbcPreparedStatement
        extends         JdbcOdbcStatement
        implements      java.sql.PreparedStatement {

        //====================================================================
        // Public methods
        //====================================================================

        //--------------------------------------------------------------------
        // Constructor
        // Perform any necessary initialization.
        //--------------------------------------------------------------------

        public JdbcOdbcPreparedStatement (
                JdbcOdbcConnectionInterface con)
        {
                super (con);
        }

        //--------------------------------------------------------------------
        // initialize
        // Initialize the result set object.  Give the ODBC API interface
        // object, the connection handle, and optionally the statement
        // handle.  If no statement handle is given, one is created.
        //--------------------------------------------------------------------

        public void initialize (
                JdbcOdbc odbcApi,
                long hdbc,
                long hstmt,
                Hashtable info,
                int resultSetType,
                int resultSetConcurrency)
                throws SQLException
        {
                super.initialize (odbcApi, hdbc, hstmt, info,
                                  resultSetType,
                                  resultSetConcurrency);

        }

        //--------------------------------------------------------------------
        // executeQuery
        // This method executes a "prepared" query statement.
        // See the definition of Statement.executeQuery
        //--------------------------------------------------------------------

        public ResultSet executeQuery ()
                throws SQLException
        {
                if (OdbcApi.getTracer().isTracing ()) {
                        OdbcApi.getTracer().trace ("*PreparedStatement.executeQuery");
                }

                ResultSet rs = null;

                if (execute ()) {

                        rs = getResultSet (false);

                }
                else {
                        // No ResultSet was produced.  Raise an exception

                        throw new SQLException ("No ResultSet was produced");
                }

                return rs;
        }

        //--------------------------------------------------------------------
        // executeQuery
        // executeQuery with a sql String is invalid for prepared statements
        //--------------------------------------------------------------------

        public ResultSet executeQuery (
                String sql)
                throws SQLException
        {
                if (OdbcApi.getTracer().isTracing ()) {
                        OdbcApi.getTracer().trace ("*PreparedStatement.executeQuery (" + sql + ")");
                }
                throw new SQLException ("Driver does not support this function",
                                        "IM001");
        }

        //--------------------------------------------------------------------
        // executeUpdate
        // This method executes a "prepared" modify statement.
        // See the definition of Statement.executeUpdate
        //--------------------------------------------------------------------

        public int executeUpdate ()
                throws SQLException
        {
                if (OdbcApi.getTracer().isTracing ()) {
                        OdbcApi.getTracer().trace ("*PreparedStatement.executeUpdate");
                }
                int numRows = -1;

                // Execute the statement.  If execute returns false, a
                // row count exists.

                if (!execute ()) {
                        numRows = getUpdateCount ();
                }
                else {

                        // No update count was produced (a ResultSet was).  Raise
                        // an exception

                        throw new SQLException ("No row count was produced");
                }

                return numRows;
        }

        //--------------------------------------------------------------------
        // executeUpdate
        // executeUpdate with a sql String is invalid for prepared statements
        //--------------------------------------------------------------------

        public int executeUpdate (
                String sql)
                throws SQLException
        {
                if (OdbcApi.getTracer().isTracing ()) {
                        OdbcApi.getTracer().trace ("*PreparedStatement.executeUpdate (" + sql + ")");
                }
                throw new SQLException ("Driver does not support this function",
                                        "IM001");
        }



        //--------------------------------------------------------------------
        // execute(SQL)
        // execute with a sql String is invalid for Prepared statements.
        //--------------------------------------------------------------------

        public boolean execute (
                String sql)
                throws SQLException
        {
                if (OdbcApi.getTracer().isTracing ()) {
                        OdbcApi.getTracer().trace ("*PreparedStatement.execute (" + sql + ")");
                }
                throw new SQLException ("Driver does not support this function",
                                        "IM001");
        }


        //--------------------------------------------------------------------
        // execute
        // This method executed an arbitrary "prepared" statement.
        // See the definition of Statement.execute
        //--------------------------------------------------------------------

        public synchronized boolean execute ()
                throws SQLException
        {


                if (OdbcApi.getTracer().isTracing ()) {
                        OdbcApi.getTracer().trace ("*PreparedStatement.execute");
                }
                boolean hasResultSet = false;
                SQLWarning      warning = null;
                boolean needData = false;

                // Reset warnings

                clearWarnings ();

                // Reset the statement handle and warning

                reset();

                // Call SQLExecute

                try {
                        needData = OdbcApi.SQLExecute (hStmt);

                        // Now loop while more data is needed (i.e. a data-at-
                        // execution parameter was given).  For each parameter
                        // that needs data, put the data from the input stream.

                        while (needData)
                        {

                                // Get the parameter number that requires data

                                int paramIndex = OdbcApi.SQLParamData (hStmt);

                                // If the parameter index is -1, there is no
                                // more data required

                                if (paramIndex == -1) {
                                        needData = false;
                                }
                                else
                                {
                                        // Now we have the proper parameter
                                        // index, get the data from the input
                                        // stream and do a SQLPutData

                                        if (batchParamsOn) // is Arrays of Parameters On?.
                                        {
                                            java.io.InputStream x = null;

                                            // Now get current row being processed.
                                            int rowIndex = paramsProcessed[0];

                                            // Now we have the proper parameter and row indexes,
                                            // get the data from the stored input stream array
                                            // and set is as the InputStream of boundParams
                                            // before calling SQLPutData.

                                            x = arrayParams.getInputStreamElement( paramIndex, rowIndex );

                                            boundParams[paramIndex - 1].setInputStream (x, arrayParams.getElementLength( paramIndex, rowIndex ));
                                        }

                                        putParamData (paramIndex);
                                }//else need data
                        }//while needdata

                }
                catch (SQLWarning ex) {

                        // Save pointer to warning and save with ResultSet
                        // object once it is created.

                        warning = ex;
                }

                // Now loop while more data is needed (i.e. a data-at-
                // execution parameter was given).  For each parameter
                // that needs data, put the data from the input stream.
                /****************
                while (needData) {

                        // Get the parameter number that requires data

                        int paramIndex = OdbcApi.SQLParamData (hStmt);

                        // If the parameter index is -1, there is no more
                        // data required

                        if (paramIndex == -1) {
                                needData = false;
                        }
                        else {
                                // Now we have the proper parameter index,
                                // get the data from the input stream
                                // and do a SQLPutData
                                putParamData (paramIndex);
                        }
                }
                ****************/
                // Now determine if there is a result set associated with
                // the SQL statement that was executed.  Get the column
                // count, and if it is not zero, there is a result set.

                if (getColumnCount () > 0) {
                        hasResultSet = true;
                }

                return hasResultSet;
        }

        // Methods for setting IN parameters into this statement.
        // Parameters are numbered starting at 1.

        //--------------------------------------------------------------------
        // setNull
        // You always need to specify a SQL type when sending a NULL.
        //--------------------------------------------------------------------

        public void setNull (
                int parameterIndex,
                int sqlType)
                throws SQLException
        {
                if (OdbcApi.getTracer().isTracing ()) {
                        OdbcApi.getTracer().trace ("*PreparedStatement.setNull (" +
                                parameterIndex + "," + sqlType + ")");
                }
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);

                // Get the buffer needed for the length

                byte lenBuf[] = getLengthBuf (parameterIndex);

                long buffers[]=new long[2];
                buffers[0]=0;
                buffers[1]=0;

                // work around ot keep same precision/scale
                // when binding nulls.
                int precision = 0;
                int scale = 0;

                if (sqlType == Types.CHAR || sqlType == Types.VARCHAR)
                {
                    precision = StringDef;
                }
                else if ( sqlType == Types.NUMERIC || sqlType == Types.DECIMAL )
                {
                    precision = NumberDef;
                    scale = NumberScale;
                }
                else if( sqlType == Types.BINARY || sqlType == Types.VARBINARY || sqlType == Types.LONGVARBINARY ) // 4532171
                {
                        sqlType = boundParams[parameterIndex-1].boundType;
                        precision = binaryPrec;
                }

                if (precision <= 0)
                    precision = getPrecision(sqlType);
                if (precision <= 0)
                    precision = 1;

                // Bind the parameter to NULL
                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterNull (hStmt, parameterIndex,
                                    sqlType, precision, scale, lenBuf, buffers);
                }

                //save the native pointers from Garbage Collection
                boundParams[parameterIndex - 1].pA1=buffers[0];
                boundParams[parameterIndex - 1].pA2=buffers[1];
                boundParams[parameterIndex - 1].scale=scale;
                boundParams[parameterIndex - 1].boundType=sqlType;
                boundParams[parameterIndex - 1].boundValue=null;

                //save value and type for Sql Batch
                arrayParams.storeValue( parameterIndex - 1, null, OdbcDef.SQL_NULL_DATA);
                setSqlType(parameterIndex, sqlType);

        }


        //--------------------------------------------------------------------
        // The following methods allow you to set various SQLtypes as
        // parameters.
        // Note that the method include the SQL type in their name.
        //--------------------------------------------------------------------

        //--------------------------------------------------------------------
        // setBoolean
        //--------------------------------------------------------------------

        public void setBoolean (
                int parameterIndex,
                boolean x)
                throws SQLException
        {
                int value = 0;

                // If the parameter is true, set the value to 1
                if (x) {
                        value = 1;
                }
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);

                // Set the parameter as if it were an integer
                //setInt (parameterIndex, value);
                // Allocate a buffer to be used in binding.  This will be
                // a 'permanent' buffer that the bridge will fill in with
                // the bound data in native format.

                byte bindBuf[] = allocBindBuf (parameterIndex, 4);

                long buffers[]=new long[2];
                buffers[0]=0;
                buffers[1]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterInteger (hStmt, parameterIndex,
                                    Types.BIT, value, bindBuf, buffers);
                }

               //save the native pointers from Garbage Collection
               boundParams[parameterIndex - 1].pA1=buffers[0];
               boundParams[parameterIndex - 1].pA2=buffers[1];
                                boundParams[parameterIndex - 1].boundType=Types.BIT;
                                boundParams[parameterIndex - 1].boundValue=new Boolean(x);

               //save value and type for Sql Batch
               arrayParams.storeValue( parameterIndex - 1, new Boolean(x), 0);
               setSqlType(parameterIndex, Types.BIT);

        }

        //--------------------------------------------------------------------
        // setByte
        //--------------------------------------------------------------------

        public void setByte (
                int parameterIndex,
                byte x)
                throws SQLException
        {
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);

                // Allocate a buffer to be used in binding.  This will be
                // a 'permanent' buffer that the bridge will fill in with
                // the bound data in native format.

                byte bindBuf[] = allocBindBuf (parameterIndex, 4);

                long buffers[]=new long[2];
                buffers[0]=0;
                buffers[1]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterInteger (hStmt, parameterIndex,
                                    Types.TINYINT, x, bindBuf, buffers);
                }

               //save the native pointers from Garbage Collection
               boundParams[parameterIndex - 1].pA1=buffers[0];
               boundParams[parameterIndex - 1].pA2=buffers[1];
                                boundParams[parameterIndex - 1].boundType=Types.TINYINT;
                                boundParams[parameterIndex - 1].boundValue=new Byte(x);


               //save value and type for Sql Batch
               arrayParams.storeValue( parameterIndex - 1, new Byte(x), 0);
               setSqlType(parameterIndex, Types.TINYINT);

        }

        //--------------------------------------------------------------------
        // setShort
        //--------------------------------------------------------------------

        public void setShort (
                int parameterIndex,
                short x)
                throws SQLException
        {
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);

                // Allocate a buffer to be used in binding.  This will be
                // a 'permanent' buffer that the bridge will fill in with
                // the bound data in native format.

                byte bindBuf[] = allocBindBuf (parameterIndex, 4);

                long buffers[]=new long[2];
                buffers[0]=0;
                buffers[1]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterInteger (hStmt, parameterIndex,
                                    Types.SMALLINT, x, bindBuf, buffers);
                }

                //save the native pointers from Garbage Collection
                boundParams[parameterIndex - 1].pA1=buffers[0];
                boundParams[parameterIndex - 1].pA2=buffers[1];
                boundParams[parameterIndex - 1].boundType=Types.SMALLINT;
                boundParams[parameterIndex - 1].boundValue=new Short(x);

                //save value and type for Sql Batch
                arrayParams.storeValue( parameterIndex - 1, new Short(x), 0);
                setSqlType(parameterIndex, Types.SMALLINT);
        }

        //--------------------------------------------------------------------
        // setInt
        //--------------------------------------------------------------------

        public void setInt (
                int parameterIndex,
                int x)
                throws SQLException
        {
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);

                // Allocate a buffer to be used in binding.  This will be
                // a 'permanent' buffer that the bridge will fill in with
                // the bound data in native format.

                byte bindBuf[] = allocBindBuf (parameterIndex, 4);

                long buffers[]=new long[2];
                buffers[0]=0;
                buffers[1]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterInteger (hStmt, parameterIndex,
                                    Types.INTEGER, x, bindBuf, buffers);
                }

               //save the native pointers from Garbage Collection
               boundParams[parameterIndex - 1].pA1=buffers[0];
               boundParams[parameterIndex - 1].pA2=buffers[1];
                                boundParams[parameterIndex - 1].boundType=Types.INTEGER;
                                boundParams[parameterIndex - 1].boundValue=new Integer(x);

               //save value and type for Sql Batch
               arrayParams.storeValue( parameterIndex - 1, new Integer(x), 0);
               setSqlType(parameterIndex, Types.INTEGER);

        }

        //--------------------------------------------------------------------
        // setLong
        //--------------------------------------------------------------------

        public void setLong (
                int parameterIndex,
                long x)
                throws SQLException
        {

                clearParameter(parameterIndex);
                // Indicate that this is an input parameter

                //Bug 4495452
                // check for ODBC version
                // use SQL_C_CHAR for ODBC 2.x and SQL_C_SBIGINT for ODBC 3.x
                if (myConnection.getODBCVer () == 2) {
                        setChar (parameterIndex, Types.BIGINT, (new Long(x)).intValue () , String.valueOf(x) );
                }

                else   if (myConnection.getODBCVer () >= 3) {
                        setInputParameter (parameterIndex, true);

                        // Allocate a buffer to be used in binding.  This will be
                        // a 'permanent' buffer that the bridge will fill in with
                        // the bound data in native format.

                        byte bindBuf[] = allocBindBuf (parameterIndex, 8);

                        long buffers[]=new long[2];
                        buffers[0]=0;
                        buffers[1]=0;

                        if (!batchOn)
                        {
                                OdbcApi.SQLBindInParameterBigint (hStmt, parameterIndex,
                                    Types.BIGINT, 0, (long)x, bindBuf, buffers);
                        }

                        //save the native pointers from Garbage Collection
                        boundParams[parameterIndex - 1].pA1=buffers[0];
                        boundParams[parameterIndex - 1].pA2=buffers[1];

                        //save value and type for Sql Batch
                        arrayParams.storeValue( parameterIndex - 1, new BigInteger(String.valueOf(x)), 0);
                        setSqlType(parameterIndex, Types.BIGINT);

                }//end if loop checking ODBC version

                boundParams[parameterIndex - 1].boundType=Types.BIGINT;
                boundParams[parameterIndex - 1].boundValue=new BigInteger(String.valueOf(x));

        }

        //--------------------------------------------------------------------
        // setReal
        //--------------------------------------------------------------------

        public void setReal (
                int parameterIndex,
                float x)
                throws SQLException
        {
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);

                // Allocate a buffer to be used in binding.  This will be
                // a 'permanent' buffer that the bridge will fill in with
                // the bound data in native format.

                byte bindBuf[] = allocBindBuf (parameterIndex, 8);

                long buffers[]=new long[2];
                buffers[0]=0;
                buffers[1]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterFloat (hStmt, parameterIndex,
                                    Types.REAL, 0, x, bindBuf, buffers);
                }

               //save the native pointers from Garbage Collection
               boundParams[parameterIndex - 1].pA1=buffers[0];
               boundParams[parameterIndex - 1].pA2=buffers[1];

               //save value and type for Sql Batch
               arrayParams.storeValue( parameterIndex - 1, new Float(x), 0);
               setSqlType(parameterIndex, Types.REAL);

        }

        //--------------------------------------------------------------------
        // setFloat
        //--------------------------------------------------------------------

        public void setFloat (
                int parameterIndex,
                float x)
                throws SQLException
        {
                // Fix 4532167. setFloat is used for SQL REAL datatype.
                setDouble(parameterIndex, (double)x );
        }

        //--------------------------------------------------------------------
        // setDouble
        //--------------------------------------------------------------------

        public void setDouble (
                int parameterIndex,
                double x)
                throws SQLException
        {
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);

                // Allocate a buffer to be used in binding.  This will be
                // a 'permanent' buffer that the bridge will fill in with
                // the bound data in native format.

                byte bindBuf[] = allocBindBuf (parameterIndex, 8);

                long buffers[]=new long[2];
                buffers[0]=0;
                buffers[1]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterDouble (hStmt, parameterIndex,
                                    Types.DOUBLE, 0, x, bindBuf, buffers);
                }

               //save the native pointers from Garbage Collection
               boundParams[parameterIndex - 1].pA1=buffers[0];
               boundParams[parameterIndex - 1].pA2=buffers[1];
               boundParams[parameterIndex - 1].boundType=Types.DOUBLE;
                                boundParams[parameterIndex - 1].boundValue=new Double(x);

               //save value and type for Sql Batch
               arrayParams.storeValue( parameterIndex - 1, new Double(x), 0);
               setSqlType(parameterIndex, Types.DOUBLE);

        }

        //--------------------------------------------------------------------
        // setBigDecimal
        //--------------------------------------------------------------------

        public void setBigDecimal (
                int parameterIndex,
                BigDecimal x)
                throws SQLException
        {
                clearParameter(parameterIndex);
                // if x is null, call setNull
                if (x == null)
                        setNull (parameterIndex, Types.NUMERIC);
                else
                {
                        // Bind the parameter as a CHAR.  We could bind as a
                        // double, but this may result in a loss of precision
                        setChar (parameterIndex, Types.NUMERIC, x.scale (), x.toString ());
                }
                boundParams[parameterIndex - 1].boundType=Types.NUMERIC;
                boundParams[parameterIndex-1].boundValue = x;
        }

        //--------------------------------------------------------------------
        // setDecimal
        //--------------------------------------------------------------------

        public void setDecimal (
                int parameterIndex,
                BigDecimal x)
                throws SQLException
        {
                clearParameter(parameterIndex);
                // if x is null, call setNull
                if (x == null)
                        setNull (parameterIndex, Types.DECIMAL);
                else
                        // Bind the parameter as a CHAR.  We could bind as a
                        // double, but this may result in a loss of precision
                        setChar (parameterIndex, Types.DECIMAL, x.scale(), x.toString ());
                boundParams[parameterIndex - 1].boundType=Types.DECIMAL;
                boundParams[parameterIndex-1].boundValue = x;

        }

        //--------------------------------------------------------------------
        // setString
        //--------------------------------------------------------------------

        public void setString (
                int parameterIndex,
                String x)
                throws SQLException
        {
                // if x is null, call setNull
                if (x == null) {
                        setNull (parameterIndex, Types.CHAR);
                } else if (x.length() >= 254) {
                    setChar (parameterIndex, Types.LONGVARCHAR, 0, x);
                } else {
                    setChar (parameterIndex, Types.CHAR, 0, x);
                }

        }

        //--------------------------------------------------------------------
        // setBytes
        //--------------------------------------------------------------------

        public void setBytes (
                int parameterIndex,
                byte x[])
                throws SQLException
        {
                // if x is null, call setNull

                if (x == null)
                        setNull (parameterIndex, Types.BINARY);
                else if ( x.length > JdbcOdbcLimits.DEFAULT_IN_PRECISION )
                {
                    setBinaryStream (parameterIndex, new java.io.ByteArrayInputStream(x), x.length);
                }
                else
                {
                    setBinary (parameterIndex, Types.BINARY, x);
                }
                boundParams[parameterIndex-1].boundType = Types.BINARY;
                boundParams[parameterIndex-1].boundValue = x;
        }

        //--------------------------------------------------------------------
        // setDate
        //--------------------------------------------------------------------

        public void setDate (
                int parameterIndex,
                java.sql.Date x)
                throws SQLException
        {
                // if x is null, call setNull
                if (x == null)
                {
                        setNull (parameterIndex, Types.DATE);
                        return;
                }
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);
                // Allocate a buffer to be used in binding.  This will be
                // a 'permanent' buffer that the bridge will fill in with
                // the bound data in native format.

                byte bindBuf[] = allocBindBuf (parameterIndex, 32);

                long buffers[]=new long[2];
                buffers[0]=0;
                buffers[1]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterDate (hStmt, parameterIndex,
                                    x, bindBuf, buffers);
                }

               //save the native pointers from Garbage Collection
               boundParams[parameterIndex - 1].pA1=buffers[0];
               boundParams[parameterIndex - 1].pA2=buffers[1];
                boundParams[parameterIndex - 1].boundType=Types.DATE;
                boundParams[parameterIndex-1].boundValue = x;

                 //save value and type for Sql Batch
               arrayParams.storeValue( parameterIndex - 1, (java.sql.Date)(x), OdbcDef.SQL_NTS);
               setSqlType(parameterIndex, Types.DATE);

        }

        //--------------------------------------------------------------------
        // setTime
        //--------------------------------------------------------------------

        public void setTime (
                int parameterIndex,
                java.sql.Time x)
                throws SQLException
        {
                // if x is null, call setNull
                if (x == null)
                {
                        setNull (parameterIndex, Types.TIME);
                        return;
                }
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);
                // Allocate a buffer to be used in binding.  This will be
                // a 'permanent' buffer that the bridge will fill in with
                // the bound data in native format.

                byte bindBuf[] = allocBindBuf (parameterIndex, 32);

                long buffers[]=new long[2];
                buffers[0]=0;
                buffers[1]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterTime (hStmt, parameterIndex,
                                    x, bindBuf, buffers);
                }

               //save the native pointers from Garbage Collection
               boundParams[parameterIndex - 1].pA1=buffers[0];
               boundParams[parameterIndex - 1].pA2=buffers[1];
                boundParams[parameterIndex - 1].boundType=Types.TIME;
                boundParams[parameterIndex-1].boundValue = x;

                 //save value and type for Sql Batch
               arrayParams.storeValue( parameterIndex - 1, (java.sql.Time)(x), OdbcDef.SQL_NTS);
               setSqlType(parameterIndex, Types.TIME);

        }

        //--------------------------------------------------------------------
        // setTimestamp
        //--------------------------------------------------------------------

        public void setTimestamp (
                int parameterIndex,
                java.sql.Timestamp x)
                throws SQLException
        {
                // if x is null, call setNull
                if (x == null)
                {
                        setNull (parameterIndex, Types.TIMESTAMP);
                        return;
                }
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);
                // Allocate a buffer to be used in binding.  This will be
                // a 'permanent' buffer that the bridge will fill in with
                // the bound data in native format.

                byte bindBuf[] = allocBindBuf (parameterIndex, 32);

                long buffers[]=new long[2];
                buffers[0]=0;
                buffers[1]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterTimestamp (hStmt, parameterIndex,
                                    x, bindBuf, buffers);
                }

               //save the native pointers from Garbage Collection
               boundParams[parameterIndex - 1].pA1=buffers[0];
               boundParams[parameterIndex - 1].pA2=buffers[1];
                        boundParams[parameterIndex - 1].boundValue=x;
                        boundParams[parameterIndex - 1].boundType=Types.TIMESTAMP;

                 //save value and type for Sql Batch
               arrayParams.storeValue( parameterIndex - 1, (java.sql.Timestamp)(x), OdbcDef.SQL_NTS);
               setSqlType(parameterIndex, Types.TIMESTAMP);

        }

        //--------------------------------------------------------------------
        // The normal setString and setBytes methods are suitable for passing
        // normal sized data.  However occasionally it may be necessary to send
        // extremely large values as LONGVARCHAR or LONGVARBINARY parameters.
        // In this case you can pass in a java.io.InputStream object, and the
        // JDBC runtimes will read data from that stream as needed, until they
        // reach end-of-file.  Note that these stream objects can either be
        // standard Java stream objects, or your own subclass that implements
        // the standard interface.
        // setAsciiStreamParameter and setUnicodeStreamParameter imply the use
        // of the SQL LONGVARCHAR type, and setBinaryStreamParameter implies
        // the SQL LONGVARBINARY type.
        // For each stream type you must specify the number of bytes to be
        // read from the stream and sent to the database.
        //--------------------------------------------------------------------

        public void setAsciiStream (
                int ParameterIndex,
                java.io.InputStream x,
                int length)
                throws SQLException
        {
                setStream (ParameterIndex, x, length, Types.LONGVARCHAR,
                                                        JdbcOdbcBoundParam.ASCII);
        }

        public void setUnicodeStream (
                int ParameterIndex,
                java.io.InputStream x,
                int length)
                throws SQLException
        {
                setStream (ParameterIndex, x, length, Types.LONGVARCHAR,
                                                        JdbcOdbcBoundParam.UNICODE);
        }


        public void setBinaryStream (
                int ParameterIndex,
                java.io.InputStream x,
                int length)
                throws SQLException
        {
                setStream (ParameterIndex, x, length, Types.LONGVARBINARY,
                                                        JdbcOdbcBoundParam.BINARY);
                binaryPrec = length; // 4532171
        }


        // Finally, if you need a more dynamic interface, or if you need
        // the driver to perform a supported coercion, then you can use
        // setObjectParameter.
        // You must explicitly specify the SQL type that you want.
        // The supported sub-types of Object are java.lang.Boolean,
        // java.lang.Byte, java.lang.Short, java.lang.Integer, java.lang.Long,
        // java.lang.Float, java.lang.Double, java.lang.String,
        // Numeric, java.io.inputStream, byte[],
        // java.util.Date, and their subtypes.  You can also use a Java null.

        //--------------------------------------------------------------------
        // clearParameters
        // Parameters remain in force for repeated use of the same statement.
        // You can use clearParameters to remove any parameters associated with
        // a statement.
        //--------------------------------------------------------------------

        public void clearParameters ()
                throws SQLException
        {
                if (hStmt != OdbcDef.SQL_NULL_HSTMT)
                {
                    OdbcApi.SQLFreeStmt (hStmt, OdbcDef.SQL_RESET_PARAMS);
                        FreeParams();
                        for (int pindex=1; boundParams != null &&
                            pindex <= boundParams.length; pindex++)
                        {
                                boundParams[pindex-1].binaryData = null;
                                boundParams[pindex-1].initialize ();
                                boundParams[pindex-1].paramInputStream = null;
                                boundParams[pindex-1].inputParameter = false;
                        }
                }
        }

        public void clearParameter (int pindex)
                throws SQLException
        {
                if (hStmt != OdbcDef.SQL_NULL_HSTMT)
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
                        boundParams[pindex-1].binaryData = null;
                        boundParams[pindex-1].initialize ();
                        boundParams[pindex-1].paramInputStream = null;
                        boundParams[pindex-1].inputParameter = false;
                }
        }

        //--------------------------------------------------------------------
        // setObject
        // You can set a parameter as a Java object.  See the JDBC spec's
        // "Dynamic Programming" chapter for information on valid Java types.
        //--------------------------------------------------------------------

        public void setObject (
                int parameterIndex,
                Object x)
                throws SQLException
        {
                setObject (parameterIndex, x, getTypeFromObject (x));
        }

        public void setObject (
                int parameterIndex,
                Object x,
                int sqlType)
                throws SQLException
        {
                setObject (parameterIndex, x, sqlType, 0);
        }

        public void setObject (
                int parameterIndex,
                Object x,
                int sqlType,
                int scale)
                throws SQLException
        {
                // if x is null, call setNull
                if (x == null)
                {
                        setNull (parameterIndex, sqlType);
                        return;
                }

                // For each known SQL Type, call the appropriate
                // set routine

                // bug 4495459
                // getting the class name of the input object

                String className = null;

                if(x instanceof byte[]) {
                        className = new String("byte[]");
                } else {
                        className = new String(x.getClass().getName());
                }

                // bug 4495459
                // setting the integer and the bigInteger values equivalent to the input boolean value

                int bVal = 0;
                java.math.BigInteger bi = null;
                if(className.equalsIgnoreCase("java.lang.Boolean") ) {
                        if(x.toString().equalsIgnoreCase("true") ) {
                                bVal = 1;
                                bi = java.math.BigInteger.ONE;
                        } else {
                                bVal = 0;
                                bi = java.math.BigInteger.ZERO;
                        }
                }


                try {
                        switch (sqlType) {

                                // bug 4495459 - start

                                case Types.CHAR:
                                        //4744654; adding short
                                        if(className.equalsIgnoreCase("java.lang.String")
                                                || className.equalsIgnoreCase("java.math.BigDecimal")
                                                || className.equalsIgnoreCase("java.lang.Boolean")
                                                || className.equalsIgnoreCase("java.lang.Integer")
                                                || className.equalsIgnoreCase("java.lang.Short")
                                                || className.equalsIgnoreCase("java.lang.Long")
                                                || className.equalsIgnoreCase("java.lang.Float")
                                                || className.equalsIgnoreCase("java.lang.Double")
                                                || className.equalsIgnoreCase("java.sql.Date")
                                                || className.equalsIgnoreCase("java.sql.Time")
                                                || className.equalsIgnoreCase("java.sql.Timestamp") ) {

                                                setString (parameterIndex,  x.toString());
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;

                                case Types.VARCHAR:
                                        //4744654; adding short
                                        if(className.equalsIgnoreCase("java.lang.String")
                                                || className.equalsIgnoreCase("java.math.BigDecimal")
                                                || className.equalsIgnoreCase("java.lang.Boolean")
                                                || className.equalsIgnoreCase("java.lang.Integer")
                                                || className.equalsIgnoreCase("java.lang.Short")
                                                || className.equalsIgnoreCase("java.lang.Long")
                                                || className.equalsIgnoreCase("java.lang.Float")
                                                || className.equalsIgnoreCase("java.lang.Double")
                                                || className.equalsIgnoreCase("java.sql.Date")
                                                || className.equalsIgnoreCase("java.sql.Time")
                                                || className.equalsIgnoreCase("java.sql.Timestamp") ) {

                                                setChar (parameterIndex, sqlType, 0, x.toString());
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;

                                case Types.LONGVARCHAR:
                                        //4744654; adding short
                                        if(className.equalsIgnoreCase("java.lang.String")
                                                || className.equalsIgnoreCase("java.math.BigDecimal")
                                                || className.equalsIgnoreCase("java.lang.Boolean")
                                                || className.equalsIgnoreCase("java.lang.Integer")
                                                || className.equalsIgnoreCase("java.lang.Short")
                                                || className.equalsIgnoreCase("java.lang.Long")
                                                || className.equalsIgnoreCase("java.lang.Float")
                                                || className.equalsIgnoreCase("java.lang.Double")
                                                || className.equalsIgnoreCase("java.sql.Date")
                                                || className.equalsIgnoreCase("java.sql.Time")
                                                || className.equalsIgnoreCase("java.sql.Timestamp") ) {

                                                setChar (parameterIndex, sqlType, 0, x.toString());
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;

                                case Types.NUMERIC:
                                        //4744654; adding short
                                        if(className.equalsIgnoreCase("java.lang.Integer")
                                                || className.equalsIgnoreCase("java.lang.Long")
                                                        || className.equalsIgnoreCase("java.lang.Short")) {

                                                BigDecimal bd1 = new BigDecimal(new java.math.BigInteger(x.toString()), 0);
                                                BigDecimal bd2 = bd1.movePointRight(scale);
                                                BigDecimal bd3 = bd2.movePointLeft(scale);

                                                setBigDecimal(parameterIndex, bd3);
                                        } else if(className.equalsIgnoreCase("java.lang.Float")
                                                || className.equalsIgnoreCase("java.lang.Double")
                                                || className.equalsIgnoreCase("java.lang.String")
                                                || className.equalsIgnoreCase("java.math.BigDecimal") ) {
                                                setBigDecimal(parameterIndex, new BigDecimal(x.toString()));
                                        } else if(className.equalsIgnoreCase("java.lang.Boolean") ) {
                                                setBigDecimal(parameterIndex, new BigDecimal(bi.toString()));
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;

                                case Types.DECIMAL:
                                        //4744654; adding short
                                        if(className.equalsIgnoreCase("java.lang.Integer")
                                                || className.equalsIgnoreCase("java.lang.Long")
                                                        || className.equalsIgnoreCase("java.lang.Short")) {

                                                BigDecimal bd1 = new BigDecimal(new java.math.BigInteger(x.toString()), 0);
                                                BigDecimal bd2 = bd1.movePointRight(scale);
                                                BigDecimal bd3 = bd2.movePointLeft(scale);

                                                setDecimal(parameterIndex, bd3);
                                        } else if(className.equalsIgnoreCase("java.lang.Float")
                                                || className.equalsIgnoreCase("java.lang.Double")
                                                || className.equalsIgnoreCase("java.lang.String")
                                                || className.equalsIgnoreCase("java.math.BigDecimal") ) {
                                                setDecimal(parameterIndex, new BigDecimal(x.toString()));
                                        } else if(className.equalsIgnoreCase("java.lang.Boolean") ) {
                                                setDecimal(parameterIndex, new BigDecimal(bi.toString()));
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;

                                case Types.BIT:
                                        /*
                                                the string representation of the boolean value true is "true";
                                                every other string is equivalent to the boolean value "false"
                                        */
                                        //4744654; adding short
                                        if(className.equalsIgnoreCase("java.lang.String")
                                                || className.equalsIgnoreCase("java.lang.Boolean") ) {
                                                if(x.toString().equalsIgnoreCase("true") ) {
                                                        setBoolean(parameterIndex, true);
                                                } else {
                                                        setBoolean(parameterIndex, false);
                                                }
                                        } else if(className.equalsIgnoreCase("java.lang.Integer")
                                                || className.equalsIgnoreCase("java.lang.Long")
                                                || className.equalsIgnoreCase("java.lang.Short")
                                                || className.equalsIgnoreCase("java.math.BigDecimal") ) {
                                                if(x.toString().equalsIgnoreCase("1") ) {
                                                        setBoolean(parameterIndex, true);
                                                } else {
                                                        setBoolean(parameterIndex, false);
                                                }
                                        } else if(className.equalsIgnoreCase("java.lang.Float") ) {
                                                if((new Float(0)).compareTo(x) == 0) {
                                                        setBoolean(parameterIndex, false);
                                                } else {
                                                        setBoolean(parameterIndex, true);
                                                }
                                        } else if(className.equalsIgnoreCase("java.lang.Double") ) {
                                                if((new Double(0)).compareTo(x) == 0) {
                                                        setBoolean(parameterIndex, false);
                                                } else {
                                                        setBoolean(parameterIndex, true);
                                                }
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;

                                case Types.TINYINT:
                                        //4744654; adding short
                                        if(className.equalsIgnoreCase("java.lang.Float") ) {
                                                setByte(parameterIndex, (new Float(x.toString())).byteValue());
                                        } else if(className.equalsIgnoreCase("java.lang.Double") ) {
                                                setByte(parameterIndex, (new Double(x.toString())).byteValue());
                                        } else if(className.equalsIgnoreCase("java.lang.Boolean") ) {
                                                setByte(parameterIndex, (byte)bVal);
                                        } else if(className.equalsIgnoreCase("java.lang.String")
                                                        || className.equalsIgnoreCase("java.lang.Integer")
                                                        || className.equalsIgnoreCase("java.lang.Short")
                                                        || className.equalsIgnoreCase("java.lang.Long")
                                                        || className.equalsIgnoreCase("java.math.BigDecimal") ) {
                                                setByte (parameterIndex, (new Byte(x.toString())).byteValue());
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;

                                case Types.SMALLINT:
                                        //4744654; adding short
                                        if(className.equalsIgnoreCase("java.lang.Float") ) {
                                                setShort(parameterIndex, (new Float(x.toString())).shortValue());
                                        } else if(className.equalsIgnoreCase("java.lang.Double") ) {
                                                setShort(parameterIndex, (new Double(x.toString())).shortValue());
                                        } else if(className.equalsIgnoreCase("java.lang.Boolean") ) {
                                                setShort(parameterIndex, (short)bVal);
                                        } else if(className.equalsIgnoreCase("java.lang.String")
                                                        || className.equalsIgnoreCase("java.lang.Integer")
                                                        || className.equalsIgnoreCase("java.lang.Short")
                                                        || className.equalsIgnoreCase("java.lang.Long")
                                                        || className.equalsIgnoreCase("java.math.BigDecimal") ) {
                                                setShort (parameterIndex, (new Short(x.toString())).shortValue ());
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;

                                case Types.INTEGER:
                                        //4744654; adding short
                                        if(className.equalsIgnoreCase("java.lang.Float") ) {
                                                setInt(parameterIndex, (new Float(x.toString())).intValue());
                                        } else if(className.equalsIgnoreCase("java.lang.Double") ) {
                                                setInt(parameterIndex, (new Double(x.toString())).intValue());
                                        } else if(className.equalsIgnoreCase("java.lang.Boolean") ) {
                                                setInt(parameterIndex, bVal);
                                        } else if(className.equalsIgnoreCase("java.lang.String")
                                                        || className.equalsIgnoreCase("java.lang.Integer")
                                                        || className.equalsIgnoreCase("java.lang.Short")
                                                        || className.equalsIgnoreCase("java.lang.Long")
                                                        || className.equalsIgnoreCase("java.math.BigDecimal") ) {
                                                setInt (parameterIndex, (new Integer(x.toString())).intValue ());
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;


                                case Types.BIGINT:
                                        //4744654; adding short
                                        if(className.equalsIgnoreCase("java.lang.Float") ) {
                                                setLong(parameterIndex, (new Float(x.toString())).longValue());
                                        } else if(className.equalsIgnoreCase("java.lang.Double") ) {
                                                setLong(parameterIndex, (new Double(x.toString())).longValue());
                                        } else if(className.equalsIgnoreCase("java.lang.Boolean") ) {
                                                setLong(parameterIndex, (long)bVal);
                                        } else if(className.equalsIgnoreCase("java.lang.String")
                                                        || className.equalsIgnoreCase("java.lang.Integer")
                                                        || className.equalsIgnoreCase("java.lang.Short")
                                                        || className.equalsIgnoreCase("java.lang.Long")
                                                        || className.equalsIgnoreCase("java.math.BigDecimal") ) {
                                                setLong (parameterIndex, (new Long(x.toString())).longValue ());
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;
                                case Types.REAL:
                                case Types.FLOAT:
                                case Types.DOUBLE:
                                        //4744654; adding short
                                        if(className.equalsIgnoreCase("java.lang.Boolean") ) {
                                                setDouble(parameterIndex, (double)bVal);
                                        } else if(className.equalsIgnoreCase("java.lang.String")
                                                        || className.equalsIgnoreCase("java.lang.Integer")
                                                        || className.equalsIgnoreCase("java.lang.Short")
                                                        || className.equalsIgnoreCase("java.lang.Long")
                                                        || className.equalsIgnoreCase("java.math.BigDecimal")
                                                        || className.equalsIgnoreCase("java.lang.Float")
                                                        || className.equalsIgnoreCase("java.lang.Double") ) {
                                                setDouble (parameterIndex, (new Double(x.toString())).doubleValue ());
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;


                                case Types.BINARY:
                                        if(className.equalsIgnoreCase("java.lang.String") ) {
                                                setBytes(parameterIndex, ((String)x).getBytes());
                                        } else if(className.equalsIgnoreCase("byte[]") ) {
                                                setBytes (parameterIndex, (byte[]) x);
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;

                                case Types.VARBINARY:
                                case Types.LONGVARBINARY:

                                        byte[] y = null;

                                        if(className.equalsIgnoreCase("java.lang.String") ) {
                                                y = ((String)x).getBytes();
                                        } else if(className.equalsIgnoreCase("byte[]") ) {
                                                y = (byte[]) x;
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }

                                        if ( y.length > JdbcOdbcLimits.DEFAULT_IN_PRECISION ) {
                                                setBinaryStream (parameterIndex, new java.io.ByteArrayInputStream(y), y.length);
                                        } else {
                                                setBinary (parameterIndex, sqlType, y);
                                        }
                                        break;

                                case Types.DATE:
                                        if(className.equalsIgnoreCase("java.lang.String") ) {
                                                setDate(parameterIndex, java.sql.Date.valueOf(x.toString()));
                                        } else if(className.equalsIgnoreCase("java.sql.Timestamp") ) {
                                                setDate(parameterIndex, new java.sql.Date(java.sql.Timestamp.valueOf(x.toString()).getTime()));
                                        } else if(className.equalsIgnoreCase("java.sql.Date") ) {
                                                setDate (parameterIndex, (java.sql.Date) x);
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;

                                case Types.TIME:
                                        if(className.equalsIgnoreCase("java.lang.String") ) {
                                                setTime(parameterIndex, java.sql.Time.valueOf(x.toString()));
                                        } else if(className.equalsIgnoreCase("java.sql.Timestamp") ) {
                                                setTime(parameterIndex, new java.sql.Time(java.sql.Timestamp.valueOf(x.toString()).getTime()));
                                        } else if(className.equalsIgnoreCase("java.sql.Time") ) {
                                                setTime (parameterIndex, (java.sql.Time) x);
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;

                                case Types.TIMESTAMP:
                                        if(className.equalsIgnoreCase("java.lang.String") ) {
                                                setTimestamp(parameterIndex, java.sql.Timestamp.valueOf(x.toString()));
                                        } else if(className.equalsIgnoreCase("java.sql.Date") ) {
                                                setTimestamp(parameterIndex, new java.sql.Timestamp(java.sql.Date.valueOf(x.toString()).getTime()));
                                        } else if(className.equalsIgnoreCase("java.sql.Timestamp") ) {
                                                setTimestamp (parameterIndex, (java.sql.Timestamp) x);
                                        } else {
                                                throw new SQLException("Conversion not supported by setObject!!");
                                        }
                                        break;

                                // bug 4495459 - end

                                default:
                                        throw new SQLException ("Unknown SQL Type for PreparedStatement.setObject (SQL Type=" + sqlType);
                        }
                } catch(SQLException sqle) {
                        throw new SQLException("SQL Exception : " + sqle.getMessage());
                }
                catch (Exception e) {
                        throw new SQLException("Unexpected exception : " + e.getMessage());
                }
        }

        // New JDBC 2.0 API


        //--------------------------------------------------------------------
        // addBatch(SQL)
        // addBatch with a sql String is invalid for Prepared statements.
        //--------------------------------------------------------------------

        public void addBatch (
                String sql)
                throws SQLException
        {
                if (OdbcApi.getTracer().isTracing ()) {
                        OdbcApi.getTracer().trace ("*PreparedStatement.addBatch (" + sql + ")");
                }
                throw new SQLException ("Driver does not support this function",
                                        "IM001");
        }

        //--------------------------------------------------------------------
        // clearBatch()
        // clears the parameter list for PreparedStatements.
        //--------------------------------------------------------------------
        public void clearBatch ()
        {
                if (OdbcApi.getTracer().isTracing ())
                {
                        OdbcApi.getTracer().trace ("*PreparedStatement.clearBatch");
                }

            try
            {
                if ( batchSqlVec != null )
                {

                    cleanUpBatch();

                    batchOn = false;
                    batchParamsOn = false;
                }

                //arrayParams.clearParameterSet();

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }


        //--------------------------------------------------------------------
        // addBatch()
        // adds a set Of Parameters for executeBatch Updates.
        //--------------------------------------------------------------------

        public void addBatch() throws SQLException
        {
                if (OdbcApi.getTracer().isTracing ()){
                        OdbcApi.getTracer().trace ("*PreparedStatement.addBatch");
                }

            try
            {
                int storedSize;

                //get from storage Vector and add values to it.
                batchSqlVec = myConnection.getBatchVector(this);

                // If adding parameter for the first time
                // create the vector to hold them.
                if ( batchSqlVec == null )
                {
                    batchSqlVec = new Vector(5,10);

                    storedSize = 0;
                }
                else
                {
                    storedSize = batchSqlVec.size();
                }

                Object[] addBatchParams = arrayParams.getStoredParameterSet();
                int[] addBatchParamsIdx = arrayParams.getStoredIndexSet();

                int batchDataLen        = addBatchParams.length;
                int batchIdxLen         = addBatchParamsIdx.length;

                if ( batchIdxLen == numParams )
                {
                        batchSqlVec.addElement(addBatchParams);

                        myConnection.setBatchVector(batchSqlVec, this);

                        arrayParams.storeRowIndex( storedSize, addBatchParamsIdx );

                        batchOn = true;
                }
                else if ( storedSize == 0 )
                {
                        throw new SQLException("Parameter-Set has missing values.");
                }
                else
                {
                        //myConnection.setBatchVector(batchSqlVec, this);
                        batchOn = true;
                }

            }
            catch (NullPointerException e)
            {
                  //throw new SQLException("Parameter Set has missing values");
                  batchOn = false;
            }

        }

        //--------------------------------------------------------------------
        // executeBatchUpdate()
        // performs execution for Statement.executeBatch().
        //--------------------------------------------------------------------

        public int[] executeBatchUpdate()
                throws BatchUpdateException
        {

                int[] updateCount = {};

                if (numParams <= 0)
                {
                    // Reset normal Bind.
                    batchSize = 0;
                    batchOn = false;
                    batchParamsOn = false;
                    return executeNoParametersBatch();
                }

                batchSqlVec = myConnection.getBatchVector(this);

                if ( batchSqlVec != null )
                {
                    batchSize = batchSqlVec.size();
                }
                else
                {
                    //throw new BatchUpdateException("Nothing to execute", updateCount);
                    updateCount = new int[0];
                    return updateCount;
                }

                if (batchSize > 0)
                {
                    updateCount = new int[batchSize];

                    // 4486684 - Deallocate the previous arrays and the global refs before reallocating them.
                    FreeIntParams();

                    // initialize process and status pointers.
                    paramStatusArray = new int[batchSize];
                    paramsProcessed = new int[batchSize];

                boolean emulateBatch = true;
                int paramReset = 0;

                    try
                    {
                        if (!emulateBatch) {

                    // Even if Batch SQL support is false,
                    // SetStmtAttr can be call to try and set
                    // SQL_ATTR_PARAMSET_SIZE > 1 for arrays of parameters.
                    // Otherwise, executeBatch must be emulated.

                            OdbcApi.SQLSetStmtAttr (hStmt,
                                                    OdbcDef.SQL_ATTR_PARAM_BIND_TYPE,
                                                    OdbcDef.SQL_PARAM_BIND_BY_COLUMN, 0);

                            // If one can set this attribute,
                            // then one can bind arrays of parameters.
                            // otherwise, emulate batch.

                            try
                            {
                                setStmtParameterSize(batchSize);
                                paramReset = getStmtParameterAttr(OdbcDef.SQL_ATTR_PARAMSET_SIZE);
                            }
                            catch (SQLException e)
                            {
                                batchSupport = false;
                            }
                        }


                            if (paramReset != batchSize)
                            {
                                batchSupport = false;

                                try
                                {
                                    setStmtParameterSize(1);
                                }
                                catch (SQLException e)
                                {
                                    //ignore exception -- e.printStackTrace();
                                }

                            }
                            else
                            {
                                //4486684
                                pA2 = new long[2];
                                pA2[0] = 0;
                                pA2[1] = 0;

                                OdbcApi.SQLSetStmtAttrPtr (hStmt,
                                                        OdbcDef.SQL_ATTR_PARAM_STATUS_PTR,
                                                        paramStatusArray, 0, pA2);


                                //4486684
                                pA1 = new long[2];
                                pA1[0] = 0;
                                pA1[1] = 0;

                                OdbcApi.SQLSetStmtAttrPtr (hStmt,
                                                        OdbcDef.SQL_ATTR_PARAMS_PROCESSED_PTR,
                                                        paramsProcessed, 0, pA1);

                                batchSupport = true;
                            }



                    }
                    catch (SQLException e)
                    {
                        batchSupport = false;
                    }

                    if ( batchSupport == true )
                    {
                        batchParamsOn = true;
                        int exceptionCount[] = {};

                        // generate 2D storage values and index arrays
                        arrayParams.builtColumWiseParameteSets( batchSize, batchSqlVec );

                        for (int i = 0; i < numParams; i++)
                        {

                            // re-initialize Bind parameters
                            arrayDef = 0;
                            arrayScale = 0;
                            int sqlType = 0;
                            int parameterIndex = i + 1;

                            try
                            {

                                Object[] paramCol   = arrayParams.getColumnWiseParamSet(parameterIndex);
                                int[] indArrs       = arrayParams.getColumnWiseIndexArray(parameterIndex);

                                // Dedefines the Column's specific precision and scale.
                                // by setting the Global arrayDef and arrayScale.
                                setPrecisionScaleArgs(paramCol, indArrs);

                                sqlType = getSqlType(parameterIndex);

                                // Binds the Object array
                                // with conversion to the
                                // specified SQLType.

                                bindArrayOfParameters(parameterIndex, sqlType,
                                                      arrayDef, arrayScale,
                                                      paramCol, indArrs);


                            }
                            catch (SQLException e)
                            {
                                e.printStackTrace();
                            }


                        }

                        // status and count of individual updates not yet available.
                        try
                        {
                            if (!execute())
                            {
                                paramStatusArray[0] = getUpdateCount();

                                arrayParams.clearStoredRowIndexs(); // must debug for testcases.

                                updateCount = paramStatusArray;

                                // Reset normal Bind.
                                batchOn = false;
                                batchParamsOn = false;

                                cleanUpBatch();

                            }
                            else
                            {
                                cleanUpBatch();
                                throw new JdbcOdbcBatchUpdateException("SQL Attempt to produce a ResultSet from executeBatch", paramStatusArray);
                            }

                        }
                        catch (SQLException e)
                        {
                            try
                            {
                                paramStatusArray[0] = getUpdateCount();

                            }catch(SQLException uCe){}

                            // get the last successful row #.
                            exceptionCount = new int[paramsProcessed[0] - 1];

                            cleanUpBatch();

                            throw new JdbcOdbcBatchUpdateException(e.getMessage(), e.getSQLState(), exceptionCount);
                        }


                    } // end of batch support.
                    else if ( batchSupport == false )
                    {
                        // Reset normal Bind.
                        // and try to emulate batch support.

                        batchOn = false;
                        batchParamsOn = false;
                        return emulateExecuteBatch();
                    }


                }

                return updateCount;

        }

        //--------------------------------------------------------------------
        // executeNoParametersBatch()
        // executes on calls to executeBatch() for Statements w/o Parameters
        //--------------------------------------------------------------------

        protected int[] executeNoParametersBatch()
                throws BatchUpdateException
        {
            int[] singleUpdate = new int[1];

                    try
                    {
                        if (!execute())
                        {
                            cleanUpBatch();
                            singleUpdate[0] = getUpdateCount();
                        }
                        else
                        {
                            cleanUpBatch();
                            throw new JdbcOdbcBatchUpdateException("SQL Attempt to produce a ResultSet from executeBatch", singleUpdate);
                        }

                    }
                    catch (SQLException e)
                    {
                        try
                        {
                            singleUpdate[0] = getUpdateCount();

                        }catch(SQLException uCe){}

                        cleanUpBatch();

                        throw new JdbcOdbcBatchUpdateException(e.getMessage(), e.getSQLState(), singleUpdate);
                    }

                    return singleUpdate;

        }

        //--------------------------------------------------------------------
        // setStmtParameterSize
        // gets the array-size for column wise binding
        //--------------------------------------------------------------------

        protected int getStmtParameterAttr(int option)
                throws SQLException
        {
            try
            {
                clearWarnings ();

                return OdbcApi.SQLGetStmtAttr (hStmt, option);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
                return -1;
            }

        }

        //--------------------------------------------------------------------
        // setStmtParameterSize
        // sets the array-size for column wise binding
        //--------------------------------------------------------------------

        protected void setStmtParameterSize(int size)
                throws SQLException
        {
            try
            {
                clearWarnings ();

                OdbcApi.SQLSetStmtAttr (hStmt, OdbcDef.SQL_ATTR_PARAMSET_SIZE, size, 0);
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }

        }


        //--------------------------------------------------------------------
        // bindArrayOfParameters
        // binds Object Array with indicated SQLType.
        //--------------------------------------------------------------------

        protected void bindArrayOfParameters(
                int parameterIndex,
                int sqlType,
                int arrayDef,
                int arrayScale,
                Object[] paramCol,
                int[] indArrs)
                throws SQLException
        {

            switch (sqlType)
            {
                case Types.CHAR:
                case Types.VARCHAR:
                case Types.NUMERIC:
                case Types.DECIMAL:
                        OdbcApi.SQLBindInParameterStringArray (
                                hStmt, parameterIndex,
                                sqlType, paramCol, arrayDef,
                                arrayScale, indArrs);
                        break;

                case Types.LONGVARCHAR:

                                // check if the Array contain Stream Data.
                            if ( (getTypeFromObjectArray (paramCol) == Types.LONGVARBINARY) )
                            {

                                    arrayParams.setInputStreamElements(parameterIndex, paramCol);


                                    OdbcApi.SQLBindInParameterAtExecArray (
                                            hStmt, parameterIndex,
                                            sqlType, arrayDef, indArrs);
                                    break;
                            }
                            else
                            {

                                    OdbcApi.SQLBindInParameterStringArray (
                                            hStmt, parameterIndex,
                                            sqlType, paramCol, arrayDef,
                                            arrayScale, indArrs);
                                    break;
                            }


                case Types.BIT:
                case Types.TINYINT:
                case Types.SMALLINT:
                case Types.INTEGER:

                        OdbcApi.SQLBindInParameterIntegerArray (
                                hStmt,
                                parameterIndex,
                                sqlType,
                                paramCol,
                                indArrs);

                        break;

                case Types.DOUBLE:

                        OdbcApi.SQLBindInParameterDoubleArray (
                                hStmt,
                                parameterIndex,
                                sqlType,
                                paramCol,
                                indArrs);
                        break;

                case Types.BIGINT:
                case Types.FLOAT:
                case Types.REAL:

                        OdbcApi.SQLBindInParameterFloatArray (
                                hStmt,
                                parameterIndex,
                                sqlType,
                                paramCol,
                                indArrs);

                        break;

                case Types.DATE:

                        OdbcApi.SQLBindInParameterDateArray (
                                hStmt,
                                parameterIndex,
                                paramCol,
                                indArrs);
                        break;

                case Types.TIME:

                        OdbcApi.SQLBindInParameterTimeArray (
                                hStmt,
                                parameterIndex,
                                paramCol,
                                indArrs);
                        break;

                case Types.TIMESTAMP:

                        OdbcApi.SQLBindInParameterTimestampArray (
                                hStmt,
                                parameterIndex,
                                paramCol,
                                indArrs);
                        break;


                case Types.BINARY:
                case Types.VARBINARY:

                        OdbcApi.SQLBindInParameterBinaryArray (
                                hStmt,
                                parameterIndex,
                                sqlType, paramCol,
                                arrayDef, indArrs);
                        break;


                case Types.LONGVARBINARY:

                        arrayParams.setInputStreamElements(parameterIndex, paramCol);

                        OdbcApi.SQLBindInParameterAtExecArray (
                                hStmt, parameterIndex,
                                sqlType, arrayDef, indArrs);
                        break;


            }//end of Bind Array!


        }


        //--------------------------------------------------------------------
        // emulateExecuteBatch.
        // executes stored list of updates one by one.
        //--------------------------------------------------------------------
        protected int[] emulateExecuteBatch()
                throws BatchUpdateException
        {

            int[] emulateCount = new int[batchSize];

            int exceptionCount[] = {};
            int successCount = 0;

            for (int i = 0; i < batchSize; i++)
            {
                // Gets the next record as an Object array!
                // and get the record's length indicator array!
                Object[] params = (Object[]) batchSqlVec.elementAt(i);

                int[] paramIndicator = arrayParams.getStoredRowIndex( i );

                try
                {
                        for (int j = 0 ; j < params.length; j++)
                        {
                                int validateType = Types.OTHER;
                                int streamLen = 0;
                                int streamType = 0;
                                int parameterIndex = j + 1;
                                java.io.InputStream x = null;

                                validateType  = getTypeFromObject (params[j]);
                                int storeType = getSqlType(parameterIndex);

                                // check if we have BINARY streams.
                                // for the following Data Types.
                                if ( validateType == Types.LONGVARBINARY )
                                {
                                        x = (java.io.InputStream)params[j];

                                        streamLen  = paramIndicator[j];

                                        switch(storeType)
                                        {
                                            case Types.LONGVARBINARY:
                                                         streamType = JdbcOdbcBoundParam.BINARY;
                                                    break;

                                            case Types.LONGVARCHAR:
                                                        streamType = boundParams[j].getStreamType ();
                                                    break;
                                        }
                                }

                                // Determine if the object is an InputStream
                                // and bind it with setStream. If not, use setObject.
                                if ( (streamLen > 0) && (0 < streamType ) )
                                {

                                        switch(streamType)
                                        {
                                                case JdbcOdbcBoundParam.ASCII:
                                                case JdbcOdbcBoundParam.UNICODE:

                                                        setStream (parameterIndex, x, streamLen,
                                                                    Types.LONGVARCHAR, streamType);
                                                        break;

                                                case JdbcOdbcBoundParam.BINARY:

                                                        setStream (parameterIndex, x, streamLen,
                                                                    Types.LONGVARBINARY, streamType);
                                                        break;
                                        }//end bind switch;

                                }
                                else
                                {
                                    if(validateType != Types.OTHER)
                                    {

                                        if (validateType != Types.NULL)
                                        {
                                                setObject(parameterIndex , params[j], storeType);
                                        }
                                        else
                                        {
                                                setNull(parameterIndex, storeType);
                                        }

                                    }

                                }


                        }//end internal for loop.
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {

                    if (!execute ())
                    {
                        myConnection.removeBatchVector(this);
                        emulateCount[i] = getUpdateCount ();
                        successCount++;

                        // May be requiered for Drivers that reuse modified
                        // BindParameter values from the first execute.
                        //if (hStmt != OdbcDef.SQL_NULL_HSTMT)
                        //{
                            //System.out.println("reset parameters Prepared free stmt = " + hStmt);
                        //    OdbcApi.SQLFreeStmt (hStmt, OdbcDef.SQL_RESET_PARAMS);
                        //}

                    }
                    else
                    {
                        for (int j = 0; j < i - 1; j++)
                        {
                            exceptionCount = new int[successCount];
                            exceptionCount[j] = emulateCount[j];
                        }
                        cleanUpBatch();

                        throw new JdbcOdbcBatchUpdateException("SQL Attempt to produce a ResultSet from executeBatch", exceptionCount);
                    }


                }
                catch(SQLException e)
                {
                        for (int j = 0; j < i - 1; j++)
                        {
                            exceptionCount = new int[successCount];
                            exceptionCount[j] = emulateCount[j];
                        }
                        cleanUpBatch();

                        throw new JdbcOdbcBatchUpdateException(e.getMessage(), e.getSQLState(), exceptionCount);
                }



            }//end for loop.

            cleanUpBatch();

            return emulateCount;


        }

        //--------------------------------------------------------------------
        // cleanUpBatch()
        // after executeBatch is complete, or if an Exception is thrown:
        // performs clean-up by removing the Batch List from the Connection
        // parent, and resets storage of Parameters Vector and BatchSize to 0.
        //--------------------------------------------------------------------

        protected void cleanUpBatch()
        {

            myConnection.removeBatchVector(this);

            if (batchSqlVec != null)
            {
                batchSqlVec.setSize(0);
                batchSize = 0;
            }
        }


        //--------------------------------------------------------------------
        // setPrecisionScaleArgs()
        // for array of parameters during executeBatch:
        // check if the SQL Type to perform precision and scale calculations.
        //--------------------------------------------------------------------

        protected void setPrecisionScaleArgs(Object[] parmset, int[] lengthInd)
        {

            int colSetType = getTypeFromObjectArray (parmset);

            for (int i = 0; i < batchSize; i++)
            {

                byte[] objByte = null;
                String objStr = null;
                BigDecimal decVal = null;
                int setDef = 0;

                try
                {
                    if ( (colSetType == Types.DECIMAL) ||
                         (colSetType == Types.NUMERIC) )
                    {
                        if (parmset[i] != null)
                        {
                            int setScale = 0;
                            decVal = (BigDecimal)parmset[i];
                            objStr = (decVal).toString();
                            setDef = objStr.indexOf('.');

                            if (setDef == -1)
                            {
                                setDef = objStr.length();
                            }
                            else
                            {
                                setScale = (decVal).scale();
                                setDef += setScale + 1;
                            }

                            //if (objStr.startsWith("-"))
                            //{
                                //setDef--;
                            //}

                            if ( setScale > arrayScale)
                            {
                                arrayScale = setScale;
                            }
                        }
                    }
                    else if ( (colSetType == Types.CHAR) ||
                              (colSetType == Types.VARCHAR) )
                    {
                        if (parmset[i] != null)
                        {
                            objStr = ((String)parmset[i]);
                            setDef = objStr.length();
                        }
                    }
                    else if ( colSetType == Types.LONGVARBINARY )
                    {

                        if ( lengthInd[i] > arrayDef)
                        {
                            arrayDef = lengthInd[i];
                        }

                    }
                    else if ( (colSetType == Types.BINARY) ||
                              (colSetType == Types.VARBINARY) )
                    {
                        if (parmset[i] != null)
                        {
                            objByte = (byte[])parmset[i];
                            setDef = objByte.length;
                        }
                    }

                    // Use the Largest length as the columns precision!
                    if ( setDef > arrayDef)
                    {
                        arrayDef = setDef;
                    }


                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }// end of for loop!


        }


        // MOVED FROM CALLABLE STATEMENT
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

        // MOVED FROM CALLABLE STATEMENT
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



        public void setCharacterStream (
                int parameterIndex,
                Reader reader,
                int length)
                throws SQLException
        {
                clearParameter(parameterIndex);
            //throw new UnsupportedOperationException();

            // Set of Streams to read chars from:
            java.io.BufferedReader bReader = null;
            java.io.BufferedOutputStream bout = null;

            // Set of Streams to write Bytes to:
            java.io.ByteArrayOutputStream outBytes = null;
            java.io.ByteArrayInputStream  inAsBytes = null;

            // Get the connection's character Set.
            String encoding = OdbcApi.charSet;

            // set character buffer size.
            int bufLength = JdbcOdbcLimits.DEFAULT_BUFFER_LENGTH;

            if (length < bufLength)
                bufLength = length;

            int inputLength = 0;

            try
            {
                if (reader != null)
                {
                    int totCount = 0;
                    int count = 0;

                    bReader     = new java.io.BufferedReader(reader);
                    outBytes    = new java.io.ByteArrayOutputStream();
                    bout        = new java.io.BufferedOutputStream(outBytes);

                    char[] charBuffer = new char[bufLength];

                    while ( count != -1 )
                    {
                        byte[] bytesBuffer = new byte[0];

                        count = bReader.read(charBuffer);

                        if (count != -1)
                        {

                            // re-size the char buffer to
                            // prevent conversion of unnessesary
                            // chars.

                            char[] tmpCharBuf  = new char[count];

                            for (int i = 0; i < count; i++)
                            {
                                tmpCharBuf[i] = charBuffer[i];
                            }


                            //totCount += count;
                            //System.out.println("Chars read = " + count);
                            //System.out.println("total chars read = " + totCount);

                            // calculate the length of bytes to write to the outputstream.
                            // For ASCII, A one to one will do. However double or even triple
                            // byte characters will require for the stream to write more bytes.
                            // out of the bytes buffer.

                            bytesBuffer = CharsToBytes (encoding, tmpCharBuf);

                            // Strip off the null terminator byte.
                            int byteCount = bytesBuffer.length - 1;

                            // write bytes to the outputStream
                            bout.write(bytesBuffer, 0, byteCount);

                            // flush any unwriten bytes
                            bout.flush();


                        } //end if.


                    } //end while.

                    // set the InputStream length.
                    inputLength = outBytes.size();

                    // debug prior to sending into the database.
                    // System.out.println("Input length = " + inputLength);
                    // System.out.println("Input value = " + outBytes.toString());

                    inAsBytes = new java.io.ByteArrayInputStream(outBytes.toByteArray());

                }

            }
            catch (java.io.IOException ioe)
            {
                throw new SQLException("CharsToBytes Reader Conversion: " + ioe.getMessage());
            }

            // process Reader as an InputStream.
            //Bug Fix 4494735
            //VARCHAR has been replaced by LONGVARCHAR
            setStream (parameterIndex, inAsBytes, inputLength, Types.LONGVARCHAR,
                                                        JdbcOdbcBoundParam.BINARY);

        }

        public void setRef (
                int i,
                Ref x)
                throws SQLException
        {
                throw new UnsupportedOperationException();
        }

        public void setBlob (
                int i,
                Blob x)
                throws SQLException
        {
                throw new UnsupportedOperationException();
        }

        public void setClob (
                int i,
                Clob x)
                throws SQLException
        {
                throw new UnsupportedOperationException();
        }

        public void setArray (
                int i,
                Array x)
                throws SQLException
        {
                throw new UnsupportedOperationException();
        }

        public ResultSetMetaData getMetaData ()
                throws SQLException
        {
                if (OdbcApi.getTracer().isTracing()) {
                        OdbcApi.getTracer().trace ("*PreparedStatement.getMetaData");
                }

                JdbcOdbcResultSet rs = null;

                // If the Statement set has been closed, throw an exception

                if (hStmt == OdbcDef.SQL_NULL_HSTMT) {
                        throw new SQLException("Statement is closed");
                }

                // Now, create a new ResultSet object, and initialize it

                rs = new JdbcOdbcResultSet ();
                rs.initialize (OdbcApi, hDbc, hStmt, true, null);

                return new JdbcOdbcResultSetMetaData (OdbcApi, rs);

        }


        //--------------------------------------------------------------------
        // setDate w/ Calendar
        // Sets the designated parameter to a java.sql.Date value,
        // using the given Calendar object.
        //--------------------------------------------------------------------

        public void setDate (
                int parameterIndex,
                Date x,
                Calendar cal)
                throws SQLException
        {
                // if x is null, call setNull
                if (x == null)
                {
                        setNull (parameterIndex, Types.DATE);
                        return;
                }

                // Fix 4380653. Convert to GMT before sending to database.
                // Take a fresh Calendar Instance, since we have already done
                // the adjustment.
                long millis = utils.convertToGMT(x,cal);
                x = new Date(millis);
                cal = Calendar.getInstance();

                //package the Date value into the Calendar Object.
                cal.setTime(x);
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);
                // Allocate a buffer to be used in binding.  This will be
                // a 'permanent' buffer that the bridge will fill in with
                // the bound data in native format.

                byte bindBuf[] = allocBindBuf (parameterIndex, 32);

                long buffers[]=new long[2];
                buffers[0]=0;
                buffers[1]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterCalendarDate (hStmt, parameterIndex,
                                    cal, bindBuf, buffers);
                }

                //save the native pointers from Garbage Collection
                boundParams[parameterIndex - 1].pA1=buffers[0];
                boundParams[parameterIndex - 1].pA2=buffers[1];
                boundParams[parameterIndex - 1].boundType=Types.DATE;
                boundParams[parameterIndex-1].boundValue = x;

                 //save value and type for Sql Batch
                arrayParams.storeValue( parameterIndex - 1, (java.util.Calendar)cal, OdbcDef.SQL_NTS);
                setSqlType(parameterIndex, Types.DATE);


        }


        //--------------------------------------------------------------------
        // setTime w/ Calendar
        // Sets the designated parameter to a java.sql.Time value,
        // using the given Calendar object.
        //--------------------------------------------------------------------

        public void setTime (
                int parameterIndex,
                Time x,
                Calendar cal)
                throws SQLException
        {
                // if x is null, call setNull
                if (x == null)
                {
                        setNull (parameterIndex, Types.TIME);
                        return;
                }

                // Fix 4380653. Convert to GMT before sending to database.
                // Take a fresh Calendar Instance, since we have already done
                // the adjustment.
                long millis = utils.convertToGMT(x,cal);
                x = new Time(millis);
                cal = Calendar.getInstance();

                //package Time into the Calendar Object.
                cal.setTime(x);
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);
                // Allocate a buffer to be used in binding.  This will be
                // a 'permanent' buffer that the bridge will fill in with
                // the bound data in native format.

                byte bindBuf[] = allocBindBuf (parameterIndex, 32);

                long buffers[]=new long[2];
                buffers[0]=0;
                buffers[1]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterCalendarTime (hStmt, parameterIndex,
                                    cal, bindBuf, buffers);
                }

                //save the native pointers from Garbage Collection
                boundParams[parameterIndex - 1].pA1=buffers[0];
                boundParams[parameterIndex - 1].pA2=buffers[1];
                boundParams[parameterIndex - 1].boundType=Types.TIME;
                boundParams[parameterIndex-1].boundValue = x;

                 //save value and type for Sql Batch
                arrayParams.storeValue( parameterIndex - 1, (java.util.Calendar)cal, OdbcDef.SQL_NTS);
                setSqlType(parameterIndex, Types.TIME);

        }

        //--------------------------------------------------------------------
        // setTimestamp w/ Calendar
        // Sets the designated parameter to a java.sql.Timestamp value,
        // using the given Calendar object.
        //--------------------------------------------------------------------

        public void setTimestamp (
                int parameterIndex,
                Timestamp x,
                Calendar cal)
                throws SQLException
        {
                // if x is null, call setNull
                if (x == null)
                {
                        setNull (parameterIndex, Types.TIMESTAMP);
                        return;
                }

                // Fix 4380653. Convert to GMT before sending to database.
                // Take a fresh Calendar Instance, since we have already done
                // the adjustment.
                long millis = utils.convertToGMT(x,cal);
                x = new Timestamp(millis);
                cal = Calendar.getInstance();

                //package Timestamp into the Calendar Object.
                cal.setTime(x);
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);
                // Allocate a buffer to be used in binding.  This will be
                // a 'permanent' buffer that the bridge will fill in with
                // the bound data in native format.

                byte bindBuf[] = allocBindBuf (parameterIndex, 32);


                long buffers[]=new long[2];
                buffers[0]=0;
                buffers[1]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterCalendarTimestamp (hStmt, parameterIndex,
                                    cal, bindBuf, buffers);
                }

                //save the native pointers from Garbage Collection
                boundParams[parameterIndex - 1].pA1=buffers[0];
                boundParams[parameterIndex - 1].pA2=buffers[1];
                boundParams[parameterIndex - 1].boundType=Types.TIMESTAMP;
                boundParams[parameterIndex-1].boundValue = x;

                 //save value and type for Sql Batch
                arrayParams.storeValue( parameterIndex - 1, (java.util.Calendar)cal, OdbcDef.SQL_NTS);
                setSqlType(parameterIndex, Types.TIMESTAMP);

        }

        public void setNull (
                int paramIndex,
                int sqlType,
                String typeName)
                throws SQLException
        {
                throw new UnsupportedOperationException();
        }


        //--------------------------------------------------------------------
        // initBoundParam
        // Initialize the bound parameter objects
        //--------------------------------------------------------------------

        public void initBoundParam ()
                throws SQLException
        {
                // Get the number of parameters

                numParams = OdbcApi.SQLNumParams (hStmt);

                // There are parameter markers, allocate the bound
                // parameter objects

                if (numParams > 0) {

                        // Allocate an array of bound parameter objects

                        boundParams = new JdbcOdbcBoundParam[numParams];

                        // Allocate and initialize each bound parameter

                        for (int i = 0; i < numParams; i++) {
                                boundParams[i] = new JdbcOdbcBoundParam ();
                                boundParams[i].initialize ();
                        }

                        // Allocate an Array storage/manipulator
                        // for Batch Update bound parameter objects.
                        arrayParams = new JdbcOdbcBoundArrayOfParams( numParams );

                        // If SQL_PARC_BATCH supported,
                        // signals that Row Counts will be returned
                        // for individual statements.
                        // Else, Batch Update is not supported and
                        // expected to emulate Batch.
                        batchRCFlag = myConnection.getBatchRowCountFlag(1);

                        // double check if row counts are returned.
                        if ( (batchRCFlag > 0) && (batchRCFlag == OdbcDef.SQL_PARC_BATCH) )
                        {
                            batchSupport = true;
                        }
                        else
                        {
                            batchSupport = false;
                        }
                        StringDef = 0;
                        NumberDef = 0;
                        NumberDef = 0;
                        binaryPrec = 0; // 4532171

                }
        }

        //====================================================================
        // Protected methods
        //====================================================================

        //--------------------------------------------------------------------
        // allocBindBuf
        // Allocate storage for the permanent data buffer for the bound
        // parameter.
        //--------------------------------------------------------------------

        protected byte[] allocBindBuf (
                int index,
                int bufLen)
        {
                byte b[] = null;

                // Sanity check the parameter number

                if ((index >= 1) &&
                    (index <= numParams)) {
                        b = boundParams[index - 1].allocBindDataBuffer (
                                                bufLen);
                }

                return b;
        }

        //--------------------------------------------------------------------
        // getDataBuf
        // Gets the data buffer for the given parameter index
        //--------------------------------------------------------------------

        protected byte[] getDataBuf (
                int index)
        {
                byte b[] = null;

                // Sanity check the parameter number

                if ((index >= 1) &&
                    (index <= numParams)) {
                        b = boundParams[index - 1].getBindDataBuffer ();
                }

                return b;
        }

        //--------------------------------------------------------------------
        // getLengthBuf
        // Gets the length buffer for the given parameter index
        //--------------------------------------------------------------------

        protected byte[] getLengthBuf (
                int index)
        {
                byte b[] = null;

                // Sanity check the parameter number

                if ((index >= 1) &&
                    (index <= numParams)) {
                        b = boundParams[index - 1].getBindLengthBuffer ();
                }

                return b;
        }

        //--------------------------------------------------------------------
        // getParamLength
        // Returns the length of the given parameter number.  When each
        // parameter was bound, a 4-byte buffer was given to hold the
        // length (stored in native format).  Get the buffer, convert the
        // buffer from native format, and return it.  If the length is -1,
        // the column is considered to be NULL.
        //--------------------------------------------------------------------

        public int getParamLength (
                int index)
        {
                int paramLen = OdbcDef.SQL_NULL_DATA;

                // Sanity check the parameter number

                if ((index >= 1) &&
                    (index <= numParams)) {

                        // Now get the length of the parameter from the
                        // bound param array.  -1 is returned if it is null.
                        paramLen = OdbcApi.bufferToInt (
                                boundParams[index - 1].getBindLengthBuffer ());
                }
                return paramLen;
        }

        //--------------------------------------------------------------------
        // putParamData
        // Puts parameter data from a previously bound input stream.  The
        // input stream was bound using SQL_LEN_DATA_AT_EXEC.
        //--------------------------------------------------------------------

        protected void putParamData (
                int index)
                throws SQLException, JdbcOdbcSQLWarning
        {
                // We'll transfer up to maxLen at a time
                int     maxLen = JdbcOdbcLimits.MAX_PUT_DATA_LENGTH;
                int     bufLen;
                int realLen;
                byte    buf[] = new byte[maxLen];
                boolean endOfStream = false;


                // Sanity check the parameter index
                if ((index < 1) ||
                    (index > numParams)) {

                        if (OdbcApi.getTracer().isTracing ()) {
                                OdbcApi.getTracer().trace ("Invalid index for putParamData()");
                        }
                        return;
                }


                // Get the information about the input stream

                java.io.InputStream inputStream =
                                        boundParams[index - 1].getInputStream ();
                int inputStreamLen = boundParams[index - 1].getInputStreamLen ();
                int inputStreamType = boundParams[index - 1].getStreamType ();

                // Loop while more data from the input stream

                while (!endOfStream) {

                        // Read some data from the input stream

                        try {
                                if (OdbcApi.getTracer().isTracing ()) {
                                        OdbcApi.getTracer().trace ("Reading from input stream");
                                }
                                bufLen = inputStream.read (buf);
                                if (OdbcApi.getTracer().isTracing ()) {
                                        OdbcApi.getTracer().trace ("Bytes read: " + bufLen);
                                }
                        }
                        catch (java.io.IOException ex) {

                                // If an I/O exception was generated, turn
                                // it into a SQLException

                                throw new SQLException (ex.getMessage ());
                        }

                        // -1 as the number of bytes read indicates that
                        // there is no more data in the input stream

                        if (bufLen == -1) {

                                // Sanity check to ensure that all the data we said we
                                // had was read.  If not, raise an exception

                                if (inputStreamLen != 0) {
                                        throw new SQLException ("End of InputStream reached before satisfying length specified when InputStream was set");
                                }
                                endOfStream = true;
                                break;
                        }

                        // If we got more bytes than necessary, truncate
                        // the buffer by re-setting the buffer length.  Also,
                        // indicate that we don't need to read any more.

                        if (bufLen > inputStreamLen) {
                                bufLen = inputStreamLen;
                                endOfStream = true;
                        }

                        realLen = bufLen;

                        // For UNICODE streams, strip off the high byte and set the
                        // number of actual bytes present.  It is assumed that
                        // there are 2 bytes present for every UNICODE character - if
                        // not, then that's not our problem

                        if (inputStreamType == JdbcOdbcBoundParam.UNICODE) {
                                realLen = bufLen / 2;

                                for (int ii = 0; ii < realLen; ii++) {
                                        buf[ii] = buf[(ii * 2) + 1];
                                }
                        }

                        // Put the data

                        OdbcApi.SQLPutData (hStmt, buf, realLen);

                        // Decrement the number of bytes still needed

                        inputStreamLen -= bufLen;

                        if (OdbcApi.getTracer().isTracing ()) {
                                OdbcApi.getTracer().trace ("" + inputStreamLen + " bytes remaining");
                        }

                        // If there is no more data to be read, exit loop

                        if (inputStreamLen == 0) {
                                endOfStream = true;
                        }
                }
        }

        //--------------------------------------------------------------------
        // setStream
        // Sets an input stream as a parameter, using the given SQL type
        //--------------------------------------------------------------------

        public void setStream (
                int ParameterIndex,
                java.io.InputStream x,
                int length,
                int SQLtype,
                int streamType)
                throws SQLException
        {
                clearParameter(ParameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (ParameterIndex, true);
                // Get the buffer needed for the length

                byte lenBuf[] = getLengthBuf (ParameterIndex);

                // Allocate a new buffer for the parameter data.  This buffer
                // will be returned by SQLParamData (it is set to the parameter
                // number, a 4-byte integer)

                byte dataBuf[] = allocBindBuf (ParameterIndex, 4);

                // Bind the parameter with SQL_LEN_DATA_AT_EXEC

                long buffers[]=new long[4];

                buffers[0]=0;
                buffers[1]=0;
                buffers[2]=0;
                buffers[3]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterAtExec (hStmt, ParameterIndex,
                                    SQLtype, length, dataBuf, lenBuf, buffers);
                }

               //save the native pointers from Garbage Collection
               boundParams[ParameterIndex - 1].pA1=buffers[0];
               boundParams[ParameterIndex - 1].pA2=buffers[1];
               boundParams[ParameterIndex - 1].pB1=buffers[2];
               boundParams[ParameterIndex - 1].pB2=buffers[3];
                                boundParams[ParameterIndex - 1].boundType=SQLtype;
                                boundParams[ParameterIndex-1].boundValue = x;


                // Save the input stream

                boundParams[ParameterIndex - 1].setInputStream (x, length);

                // Set the stream type

                boundParams[ParameterIndex - 1].setStreamType (streamType);

                // save value and type for Sql Batch
                arrayParams.storeValue( ParameterIndex - 1, (java.io.InputStream)x, length);
                setSqlType(ParameterIndex, SQLtype);

        }

        //--------------------------------------------------------------------
        // setChar
        // Binds the given string to the given SQL type
        //--------------------------------------------------------------------

        protected void setChar (
                int parameterIndex,
                int SQLtype,
                int scale,
                String x)
                throws SQLException
        {
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);
                // Allocate a buffer to be used in binding.  This will be
                // a 'permanent' buffer that the bridge will fill in with
                // the bound data in native format.

                int precision = 0;
                int prefPrecision = 0;

                char[] xChars = x.toCharArray();
                byte xValue[] = new byte[0];

                try {
                   xValue = CharsToBytes (OdbcApi.charSet, xChars);
                } catch (java.io.UnsupportedEncodingException exx) {
                   throw (Error) (new InternalError("SQL")).initCause(exx);
                }


                byte bindBuf[] = allocBindBuf (parameterIndex,
                                (xValue.length));


                // Get the precision for this SQL type.  If the precision
                // is out of bounds, set it to our default

                precision = getPrecision(SQLtype);

                /*
                   // COMENTED - OUT for Intersolve Driver
                   // but brakes (ExpressLane's 2.1 ODBC Driver for these Types!)

                    //Added for NUMERIC/DECIMAL Types
                    //Use the String length as the precision
                    //as long as it is <= the database's Type precision.
                    //If there is no precision, default to the bridge limits

                    if ( (SQLtype == Types.NUMERIC) || (SQLtype == Types.DECIMAL) )
                    {
                        if (x != null)
                        {
                            prefPrecision = (x.trim()).length();
                        }

                        if ( prefPrecision > 0 )
                        {
                            //throw new SQLException ("Data precision > precision allowed by the Database for this Type");
                            precision = prefPrecision;
                        }

                    }

                */


                if ((precision < 0) ||
                                 (precision > JdbcOdbcLimits.DEFAULT_IN_PRECISION))
                {
                        //precision = JdbcOdbcLimits.DEFAULT_IN_PRECISION;
                        //put in as a fix for invalid precision problems
                        precision = xValue.length;
                }


                long buffers[]=new long[4];

                buffers[0]=0;
                buffers[1]=0;
                buffers[2]=0;
                buffers[3]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterString (hStmt, parameterIndex,
                                    SQLtype, precision, scale, xValue, bindBuf, buffers);
                }

               //Save the pointers from the trash
               boundParams[parameterIndex - 1].pA1=buffers[0];
               boundParams[parameterIndex - 1].pA2=buffers[1];
               boundParams[parameterIndex - 1].pB1=buffers[2];
               boundParams[parameterIndex - 1].pB2=buffers[3];
                boundParams[parameterIndex - 1].scale = scale;
                boundParams[parameterIndex - 1].boundType=SQLtype;
                boundParams[parameterIndex-1].boundValue = x;

               // save value and type. Also keep track of the
               // largest precision/scale values for Sql Batch.

                if ( (SQLtype == Types.NUMERIC) || (SQLtype == Types.DECIMAL) )
                {
                    arrayParams.storeValue( parameterIndex - 1, new BigDecimal(x.trim()), OdbcDef.SQL_NTS);

                    NumberDef = precision;

                    if (scale > NumberScale)
                        NumberScale = scale;

                }
                //bug 4495452
                else if ( SQLtype == Types.BIGINT )
                {
                    arrayParams.storeValue( parameterIndex - 1, new BigInteger(x.trim()), OdbcDef.SQL_NTS);

                    NumberDef = precision;

                    if (scale > NumberScale)
                        NumberScale = scale;

                }
                else
                {
                    arrayParams.storeValue( parameterIndex - 1, (String)x, OdbcDef.SQL_NTS);
                    StringDef = precision;
                }

                setSqlType(parameterIndex, SQLtype);

        }

        //--------------------------------------------------------------------
        // setBinary
        // Binds the given byte array to the given SQL type
        //--------------------------------------------------------------------

        protected void setBinary (
                int parameterIndex,
                int SQLtype,
                byte x[])
                throws SQLException
        {
                clearParameter(parameterIndex);
                // Indicate that this is an input parameter
                setInputParameter (parameterIndex, true);

                // Allocate a buffer to be used in binding.  This will be
                // a 'permanent' buffer that the bridge will fill in with
                // the bound data in native format.

                byte bindBuf[] = allocBindBuf (parameterIndex,
                                x.length);

                // Get the buffer needed for the length

                byte lenBuf[] = getLengthBuf (parameterIndex);

                long buffers[]=new long[6];

                buffers[0]=0;
                buffers[1]=0;
                buffers[2]=0;
                buffers[3]=0;
                buffers[4]=0;
                buffers[5]=0;

                if (!batchOn)
                {
                    OdbcApi.SQLBindInParameterBinary (hStmt, parameterIndex,
                                    SQLtype, x, bindBuf, lenBuf, buffers);
                }

               //Save the pointers from the trash
               boundParams[parameterIndex - 1].pA1=buffers[0];
               boundParams[parameterIndex - 1].pA2=buffers[1];
               boundParams[parameterIndex - 1].pB1=buffers[2];
               boundParams[parameterIndex - 1].pB2=buffers[3];
               boundParams[parameterIndex - 1].pC1=buffers[4];
               boundParams[parameterIndex - 1].pC2=buffers[5];
                                boundParams[parameterIndex - 1].boundType=SQLtype;
                                boundParams[parameterIndex-1].boundValue = x;

                binaryPrec = x.length; // 4532171
                //save value and type for Sql Batch
                arrayParams.storeValue( parameterIndex - 1, (byte[])x, OdbcDef.SQL_NTS);
                setSqlType(parameterIndex, SQLtype);

        }

        //--------------------------------------------------------------------
        // getTypeFromObjectArray
        // Given an object Array of unknown type, return the Java SQL type
        // representative of all of the Values in the array.
        //--------------------------------------------------------------------

        protected int getTypeFromObjectArray (Object[] x)
        {
            int colSetType = Types.OTHER;

            for (int i = 0; i < batchSize; i++)
            {
                colSetType = getTypeFromObject (x[i]);

                if ( colSetType != Types.NULL )
                {
                    break;
                }
            }

            return colSetType;

        }


       public synchronized void close ()
                throws SQLException
        {
                if (OdbcApi.getTracer().isTracing ()) {
                        OdbcApi.getTracer().trace ("*PreparedStatement.close");
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

                // 4486684 - Deallocate the previous arrays and the global refs that have been allocated in executeBatchUpdate()
                FreeIntParams();

                // Remove this Statement object from the Connection object's
                // list

                myConnection.deregisterStatement (this);

                if (batchOn)
                    clearBatch();

        }

        // 4486684
        public synchronized void FreeIntParams()
        {
                if(pA1 != null)
                {
                        if(pA1[0] != 0)
                        {
                                OdbcApi.ReleaseStoredIntegers(pA1[0], pA1[1]);
                                pA1[0] = 0;
                                pA1[1] = 0;
                        }
                }

                if(pA2 != null)
                {
                        if(pA2[0] != 0)
                        {
                                OdbcApi.ReleaseStoredIntegers(pA2[0], pA2[1]);
                                pA2[0] = 0;
                                pA2[1] = 0;
                        }
                }
        }

        public synchronized void FreeParams()
        throws NullPointerException
        {
                try
                {
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
        }

        //--------------------------------------------------------------------
        // setSql
        // sets the current SQL. Used to build a row count query if needed.
        //--------------------------------------------------------------------

        public void setSql(String sql)
        {
            mySql = sql.toUpperCase();
        }

        //--------------------------------------------------------------------
        // getObject
        // returns and Object[] copy for values in the Parameters. Used in a
        // row count query.
        //--------------------------------------------------------------------

        public Object[] getObjects()
        {
            Object[] objs = new Object[numParams];

            Object[] data = arrayParams.getStoredParameterSet();

            if (data != null)
            {
                try
                {
                    for (int i = 0; i < numParams; i++)
                    {
                        objs[i] = data[i];
                    }
                }
                catch (ArrayIndexOutOfBoundsException e)
                {
                    System.err.println("Exception, while calculating row count: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            return objs;
        }

        //--------------------------------------------------------------------
        // getObjectTypes
        // returns and int[] array with the SQL type for each parameter. Used
        // in a row count query.
        //--------------------------------------------------------------------

        public int[] getObjectTypes()
        {
            int[] types = new int[numParams];

            for (int i = 0; i < numParams; i++)
            {
                types[i] = boundParams[i].getSqlType ();
            }

            return types;
        }

        //--------------------------------------------------------------------
        // getParamCount
        // return the number of Parameters in this Statement.
        //--------------------------------------------------------------------

        public int getParamCount()
        {
            return numParams;
        }

        //--------------------------------------------------------------------
        // setInputParameter
        // Sets the input parameter flag
        //--------------------------------------------------------------------

        protected void setInputParameter (
                int index,
                boolean input)
        {
                // Sanity check the parameter number
                if ((index >= 1) &&
                    (index <= numParams)) {
                        boundParams[index - 1].setInputParameter (input);
                }
        }

    //----------------------------------------------------------------
    // JDBC 3.0 API Changes
    //----------------------------------------------------------------

    public void setURL(int i, java.net.URL url) throws SQLException {
        throw new UnsupportedOperationException();
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw new UnsupportedOperationException();
    }

        //====================================================================
        // Data attributes
        //====================================================================

        protected int numParams;                // Number of parameter markers
                                                //  for the prepared statement

        protected JdbcOdbcBoundParam boundParams[];
                                                // Array of bound parameter
                                                //  objects.  Each parameter
                                                //  marker will have a
                                                //  corresponding object to
                                                //  hold bind information, and
                                                //  resulting data.

        protected JdbcOdbcBoundArrayOfParams
                                arrayParams;    // Array of Parameters manipulator.

        protected Vector batchSqlVec;           // Vector that holds parameter
                                                // values for Batch Updates.

        protected boolean batchSupport;         // Determines if the Batch Updates
                                                // are supported by the ODBC Driver

        protected boolean batchParamsOn;        // Determines if arrays of Parameters
                                                // are being used for Batch Updates.


        protected int batchSize;                // The # of rows for columWise parameter Sets.

        protected int arrayDef;                 // Maximum precision for Arrays of Strings.
        protected int arrayScale;               // Maximum scale for Arrays of Numeric/Decimals.
        protected int StringDef;                // Maximum String precision for emulation.
        protected int NumberDef;                // Maximum Numeric precision for emulation.
        protected int NumberScale;              // Maximum Numeric scale for emulation.

        protected int batchRCFlag;              // Row-Count type indicator for Batch Updates.

        protected int[] paramsProcessed;        // Parameters Processed Pointer for arrays in batch.
        protected int[] paramStatusArray;       // Parameters Status Pointer for arrays in batch.

        //4486684
        protected long[] pA1; //paramsProcessed native pointers
        protected long[] pA2; //paramStatusArray native pointers

        protected int binaryPrec; // 4532171

        protected JdbcOdbcUtils utils = new JdbcOdbcUtils();


}
