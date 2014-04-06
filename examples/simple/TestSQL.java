/*
This sample performs basic database operations using the Sun JDBC-ODBC bridge.

Create the sample JDBC data source:
    - open Control Panel / Administrative tools / Data Sources (ODBC)
    - switch to System DSN tab
    - press the "add" button
    - create new database, selecting "Driver to Microsoft Access(*.mdb)" from the list
    - set data source name "TestBase" and ensure the DB is not read-only
*/

import java.sql.*;  //import all the JDBC classes

public class TestSQL {

    public static void main(String[] args)
    {
        String     URL      = "jdbc:odbc:TestBase";
        String     username = "";
        String     password = "";

        if (args.length < 1) {
            System.out.println("Please use:\n java TestSQL databaseName [username] [password]\n");
            System.out.println("For example:\n java TestSQL TestBase myuser mypass\n");
            return;
        }

        switch (args.length) {
            case 3:
               password = args[2];
            case 2:
               username = args[1];
            default:
                URL = "jdbc:odbc:" + args[0];
        }

        System.out.println("Connection settings:");
        System.out.println(" Database: " + args[0]);
        System.out.println(" Username: " + username);
        System.out.println(" Password: " + password + "\n");

        boolean    created  = false;
        boolean    errors   = false;
        Statement  stmt     = null;
        Connection con      = null;
        ResultSet  result;

        try
        {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
        }
        catch (Exception e)
        {
            System.out.println("Failed to load JDBC/ODBC driver: "
                               + e.getMessage());
            return;
        }


        try
        {
            con = DriverManager.getConnection (
                URL,
                username,
                password);
            stmt = con.createStatement();
        }
        catch (Exception e)
        {
            errors = true;
            System.out.println("problems connecting to " + URL +
                               ": "+e.getMessage());
        }

        // try to drop the table if it was not dropped berore
        try{stmt.execute("drop table TestTable;");}catch(Exception e){}

        try
        {
            // create table 'TestTable'
            System.out.println("-- create table...");
            stmt.execute(
              "create table TestTable (name varchar (32), day date);"
            );
            created = true;
            // insert some data
            System.out.println("-- insert data...");
            stmt.execute(
             "insert into TestTable values ('XX1', '1991-01-11');"
            );
            stmt.execute(
             "insert into TestTable values ('XX2', '1992-02-22');"
            );
            stmt.execute(
             "insert into TestTable values ('XY3', '1993-03-30');"
            );
            // test data
            System.out.println("-- test data...");
            result = stmt.executeQuery("SELECT * FROM TestTable "+
                              "WHERE name LIKE 'XX%';");
            while(result.next())
            {
                System.out.println("XX% name = " + result.getString("name") +
                                      "  day = " + result.getDate("day"));
            }
            result.close();
            System.out.println("-----------");
            result = stmt.executeQuery("SELECT * FROM TestTable "+
                              "WHERE name LIKE 'X%';");
            while(result.next())
            {
                System.out.println("X% name = " + result.getString("name") +
                                     "  day = " + result.getDate("day"));
            }
            result.close();
        }
        catch (Exception e)
        {
            errors = true;
            System.out.println("problems with SQL sent to " + URL +
                               ": " + e.getMessage());
        }

        if (created)
        {
            try
            {
                // drop table 'TestTable'
                stmt.execute(
                  "drop table TestTable;"
                );
            }
            catch (Exception e)
            {
                errors = true;
                System.out.println("can not drop table: " + e.getMessage());
            }

        }

        if (con != null) try {con.close();} catch(Exception e){}

        if (errors)
            System.out.println("----> FAILED");
        else
            System.out.println("----> SUCCESS");

    }
}
