package net.javacoding.jspider.core.storage.jdbc;

import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.model.SiteInternal;
import net.javacoding.jspider.core.storage.spi.SiteDAOSPI;
import net.javacoding.jspider.core.storage.spi.StorageSPI;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * $Id: SiteDAOImpl.java,v 1.8 2003/04/11 16:37:06 vanrogu Exp $
 */
class SiteDAOImpl implements SiteDAOSPI {
    protected DBUtil dbUtil;
    protected StorageSPI storage;
    protected Log log;

    public SiteDAOImpl( StorageSPI storage, DBUtil dbUtil ) {
        this.storage = storage;
        this.dbUtil = dbUtil;
        this.log = LogFactory.getLog( SiteDAOImpl.class );
    }

    public SiteInternal find( URL siteURL ) {
        String sql = "select id, host, port, useCookies, useProxy, state, obeyRobotsTxt, baseSite, userAgent, handle\n" +
                     "  from jspider_site\n" +
                     "  where host = ?\n" +
                     "    and port = ?";

        ResultSet rs = null;

        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement( sql )
        ) {
            preparedStatement.setString( 1, siteURL.getHost() );
            preparedStatement.setInt( 2, siteURL.getPort() );

            rs = preparedStatement.executeQuery();

            if ( rs.next() ) {
                return createSiteFromRecord( rs );
            }
        }
        catch ( SQLException e ) {
            log.error( "SQLException", e );
        }
        finally {
            dbUtil.safeClose( rs, log );
        }

        return null;
    }

    public SiteInternal find( int id ) {
        String sql = "select id, host, port, useCookies, useProxy, state, obeyRobotsTxt, baseSite, userAgent, handle\n" +
                     "  from jspider_site\n" +
                     "  where id = ?";

        ResultSet rs = null;

        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement( sql )
        ) {
            preparedStatement.setInt( 1, id );

            rs = preparedStatement.executeQuery();

            if ( rs.next() ) {
                return createSiteFromRecord( rs );
            }
        }
        catch ( SQLException e ) {
            log.error( "SQLException", e );
        }
        finally {
            dbUtil.safeClose( rs, log );
        }

        return null;
    }

    public void create( int id, SiteInternal site ) {
        String sql = "insert into jspider_site (id, host, port, robotsTxtHandled, useCookies, useProxy, state, obeyRobotsTxt, " +
                     "    baseSite, userAgent, handle) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            int index = 0;
            ps.setInt( ++index, id );
            ps.setString( ++index, site.getHost() );
            ps.setInt( ++index, site.getPort() );
            ps.setBoolean( ++index, site.isRobotsTXTHandled() );
            ps.setBoolean( ++index, site.getUseCookies() );
            ps.setBoolean( ++index, site.getUseProxy() );
            ps.setInt( ++index, site.getState() );
            ps.setBoolean( ++index, site.getObeyRobotsTXT() );
            ps.setBoolean( ++index, site.isBaseSite() );
            ps.setString( ++index, site.getUserAgent() );
            ps.setBoolean( ++index, site.mustHandle() );

            ps.executeUpdate();
        }
        catch ( SQLException e ) {
            log.error( "SQLException", e );
        }
    }

    public void save( int id, SiteInternal site ) {
        String sql = "update jspider_site set\n" +
                     "    useCookies = ?,\n" +
                     "    useProxy = ?,\n" +
                     "    state = ?,\n" +
                     "    obeyRobotsTxt = ?,\n" +
                     "    baseSite = ?,\n" +
                     "    userAgent = ?,\n" +
                     "    handle = ?,\n" +
                     "  where id = ?";

        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement( sql )
        ) {
            int index = 0;

            preparedStatement.setBoolean( ++index, site.getUseCookies() );
            preparedStatement.setBoolean( ++index, site.getUseProxy() );
            preparedStatement.setInt( ++index, site.getState() );
            preparedStatement.setBoolean( ++index, site.getObeyRobotsTXT() );
            preparedStatement.setBoolean( ++index, site.isBaseSite() );
            preparedStatement.setString( ++index, site.getUserAgent() );
            preparedStatement.setBoolean( ++index, site.mustHandle() );

            preparedStatement.executeUpdate();
        }
        catch ( SQLException e ) {
            log.error( "SQLException", e );
        }
    }

    public SiteInternal[] findAll() {
        String sql = "SELECT id, host, port, useCookies, useProxy, state, obeyRobotsTxt, baseSite, userAgent, handle\n" +
                     "    FROM jspider_site";

        ArrayList<SiteInternal> al = new ArrayList<>();

        try (
                Connection connection = dbUtil.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery( sql );
        ) {
            while ( resultSet.next() ) {
                al.add( createSiteFromRecord( resultSet ) );
            }
        }
        catch ( SQLException e ) {
            log.error( "SQLException", e );
        }

        return al.toArray( new SiteInternal[al.size()] );
    }

    protected SiteInternal createSiteFromRecord( ResultSet rs ) throws SQLException {
        int index = 0;

        int id = rs.getInt( ++index );
        String host = rs.getString( ++index );
        int port = rs.getInt( ++index );
        boolean useCookies = rs.getBoolean( ++index );
        boolean useProxy = rs.getBoolean( ++index );
        int state = rs.getInt( ++index );
        boolean obeyRobotsTXT = rs.getBoolean( ++index );
        boolean baseSite = rs.getBoolean( ++index );
        String userAgent = rs.getString( ++index );
        boolean handle = rs.getBoolean( ++index );

        URL url = null;
        try {
            url = new URL( "http", host, port, "" );
        }
        catch ( MalformedURLException e ) {
            e.printStackTrace();
        }

        return new SiteInternal( storage, id, handle, url, state, obeyRobotsTXT, useProxy, useCookies, userAgent, baseSite );
    }
}
