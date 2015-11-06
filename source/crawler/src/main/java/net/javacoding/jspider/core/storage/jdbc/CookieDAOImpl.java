package net.javacoding.jspider.core.storage.jdbc;

import net.javacoding.jspider.api.model.Cookie;
import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.storage.CookieDAO;
import net.javacoding.jspider.core.storage.spi.CookieDAOSPI;
import net.javacoding.jspider.core.storage.spi.StorageSPI;
import org.apache.commons.lang.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * $Id: CookieDAOImpl.java,v 1.3 2003/04/11 16:37:05 vanrogu Exp $
 */
class CookieDAOImpl implements CookieDAOSPI {
    protected Log log;
    protected StorageSPI storage;
    protected DBUtil dbUtil;
    protected Map cookies;

    public CookieDAOImpl( StorageSPI storage, DBUtil dbUtil ) {
        this.log = LogFactory.getLog( CookieDAO.class );
        this.storage = storage;
        this.dbUtil = dbUtil;
        this.cookies = new HashMap();
    }

    public Cookie[] find( int id ) {
        ArrayList<Cookie> al = new ArrayList<>();
        String sql = "select name, `value`, path, domain, expires from jspider_cookie where site = ?";
        ResultSet rs = null;

        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, id );
            rs = ps.executeQuery();

            while ( rs.next() ) {
                int index = 0;

                String name = rs.getString( ++index );
                String value = rs.getString( ++index );
                String path = rs.getString( ++index );
                String domain = rs.getString( ++index );
                String expires = rs.getString( ++index );

                al.add( new Cookie( name, value, domain, path, expires ) );
            }
        }
        catch ( SQLException e ) {
            log.error( "SQLException", e );
        }
        finally {
            dbUtil.safeClose( rs, log );
        }
        return al.toArray( new Cookie[al.size()] );
    }

    public void save( int id, Cookie[] cookies ) {
        String sql = "insert into jspider_cookie ( site, name, `value`, path, expires, domain )\n" +
                     "    values ({values})\n" +
                     "  on duplicate key update\n" +
                     "    `value` = values(`value`),\n" +
                     "    path = values(path),\n" +
                     "    expires = values(expires),\n" +
                     "    domain = values(domain)";

        sql = StringUtils.replace( sql, "{values}", StringUtils.repeat( "?", ", ", cookies.length * 6 ) );

        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement statement = connection.prepareStatement( sql )
        ) {
            int index = 0;
            for ( Cookie cookie : cookies ) {
                statement.setInt( ++index, id );
                statement.setString( ++index, cookie.getName() );
                statement.setString( ++index, cookie.getValue() );
                statement.setString( ++index, cookie.getPath() );
                statement.setString( ++index, cookie.getExpires() );
                statement.setString( ++index, cookie.getDomain() );
            }

            statement.executeUpdate();
        }
        catch ( SQLException e ) {
            log.error( "SQLException saving cookies", e );
        }
    }
}
