package net.javacoding.jspider.core.storage.jdbc;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.util.config.PropertySet;

import java.beans.PropertyVetoException;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * $Id: DBUtil.java,v 1.7 2003/04/11 16:37:05 vanrogu Exp $
 */
class DBUtil {
    public static final String DRIVER = "driver";
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final String URL = "url";
    private ComboPooledDataSource comboPooledDataSource;

    public DBUtil( PropertySet props ) {
        setUpConnectionPool( props );
    }

    public static String format( String string ) {
        return "'" + string + "'";
    }

    public static String format( boolean bool ) {
        return bool ? "1" : "0";
    }

    public static String format( int i ) {
        return "" + i;
    }

    public static String format( URL url ) {
        return format( "" + url );
    }

    public Connection getConnection() {
        try {
            return comboPooledDataSource.getConnection();
        }
        catch ( SQLException e ) {
            throw new RuntimeException( "Failed to getConnection", e );
        }
    }

    public void setUpConnectionPool( PropertySet props ) {
        try {
            Log log = LogFactory.getLog( DBUtil.class );
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
        }
        catch ( PropertyVetoException e ) {
            throw new RuntimeException( "Failed to setup the c3p0 connection pool!", e );
        }
    }

    public void safeClose( ResultSet rs, Log log ) {
        if ( rs != null ) {
            try {
                rs.close();
            }
            catch ( SQLException e ) {
                log.error( "error closing resultset", e );
            }
        }
    }

    public void safeClose( Statement st, Log log ) {
        if ( st != null ) {
            try {
                st.close();
            }
            catch ( SQLException e ) {
                log.error( "error closing resultset", e );
            }
        }
    }
}