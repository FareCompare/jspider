package net.javacoding.jspider.core.storage.jdbc;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import net.javacoding.jspider.core.util.config.PropertySet;
import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;

import java.beans.PropertyVetoException;
import java.net.URL;
import java.sql.*;
import java.util.Properties;

/**
 * $Id: DBUtil.java,v 1.7 2003/04/11 16:37:05 vanrogu Exp $
 */
class DBUtil {

    public static final String DRIVER = "driver";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String URL = "url";

//    protected Connection connection;
    private ComboPooledDataSource comboPooledDataSource;

    public DBUtil(PropertySet props) {
//        connection = getConnection(props);
        setUpConnectionPool( props );
    }

    public static String format(String string) {
        return "'" + string + "'";
    }

    public static String format(boolean bool) {
        return bool ? "1" : "0";
    }

    public static String format(int i) {
        return "" + i;
    }

    public static String format(URL url) {
        return format("" + url);
    }

    public Connection getConnection() {
        try {
            Connection connection = comboPooledDataSource.getConnection();
            return connection;
        } catch ( SQLException e ) {
            throw new RuntimeException( "Failed to getConnection", e );
        }
//        return connection;
    }

    public void setUpConnectionPool( PropertySet props ) {
        try {
            Log log = LogFactory.getLog(DBUtil.class);
            String driverClassName = props.getString( DRIVER, "" );
            String jdbcUrl = props.getString( URL, "" );
            String user = props.getString( USER, "" );
            String password = props.getString( PASSWORD, "" );


            comboPooledDataSource = new ComboPooledDataSource();
            comboPooledDataSource.setDriverClass( driverClassName ); //loads the jdbc driver
            comboPooledDataSource.setJdbcUrl( jdbcUrl );
            comboPooledDataSource.setUser( user );
            comboPooledDataSource.setPassword( password );
            comboPooledDataSource.setMaxStatements( 35000 );
            comboPooledDataSource.setMinPoolSize( 10 );
            comboPooledDataSource.setMaxPoolSize( 300 );
            comboPooledDataSource.setTestConnectionOnCheckout( true );
            log.info( String.format( "Connection pool created for %s user=%s", jdbcUrl, user ) );
        } catch ( PropertyVetoException e ) {
            throw new RuntimeException( "Failed to setup the c3p0 connection pool!", e );
        }
    }

//    public Connection getConnection(PropertySet props) {
//        Connection connection = null;
//        Log log = LogFactory.getLog(DBUtil.class);
//        try {
//            String driverClassName = props.getString(DRIVER, "");
//            Class.forName(driverClassName);
//            log.debug("jdbc driver = " + driverClassName);
//            Properties p = new Properties();
//            p.setProperty("user", props.getString(USER, ""));
//            log.debug("jdbc user = " + props.getString(USER, ""));
//            p.setProperty("password", props.getString(PASSWORD, ""));
//            connection = DriverManager.getConnection(props.getString(URL, ""), p);
//        } catch (ClassNotFoundException e) {
//            log.error("JDBC Driver Class Not Found", e);
//            throw new RuntimeException("JDBC Driver Class Not Found");
//        } catch (SQLException e) {
//            log.error("SQL Exception during JDBC Connect", e);
//            throw new RuntimeException("SQL Exception during JDBC Connect");
//        }
//        return connection;
//    }

    public void safeClose(ResultSet rs, Log log) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("error closing resultset", e);
            }
        }
    }

    public void safeClose(Statement st, Log log) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                log.error("error closing resultset", e);
            }
        }
    }

}