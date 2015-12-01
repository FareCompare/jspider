package net.javacoding.jspider.core.storage.jdbc;


import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.storage.spi.ContentDAOSPI;
import net.javacoding.jspider.core.storage.spi.CookieDAOSPI;
import net.javacoding.jspider.core.storage.spi.DecisionDAOSPI;
import net.javacoding.jspider.core.storage.spi.EMailAddressDAOSPI;
import net.javacoding.jspider.core.storage.spi.FolderDAOSPI;
import net.javacoding.jspider.core.storage.spi.ResourceDAOSPI;
import net.javacoding.jspider.core.storage.spi.SiteDAOSPI;
import net.javacoding.jspider.core.storage.spi.StorageSPI;
import net.javacoding.jspider.core.util.config.PropertySet;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * $Id: JdbcStorageImpl.java,v 1.8 2003/04/11 16:37:06 vanrogu Exp $
 *
 * @author G�nther Van Roey
 * @todo DAO's need caching - definitely !
 * @todo find a good way of cleaning the store at spidering start or layering it per spider session
 */
public class JdbcStorageImpl implements StorageSPI {

    protected static final String[] TABLES = new String[]{
            "jspider_content",
            "jspider_cookie",
            "jspider_decision",
            "jspider_decision_step",
            "jspider_site",
            "jspider_resource",
            "jspider_resource_reference",
            "jspider_email_address",
            "jspider_email_address_reference",
            "jspider_folder"
    };

    protected SiteDAOSPI siteDAO;
    protected ResourceDAOSPI resourceDAO;
    protected ContentDAOSPI contentDAO;
    protected DecisionDAOSPI decisionDAO;
    protected CookieDAOSPI cookieDAO;
    protected EMailAddressDAOSPI emailAddressDAO;
    protected FolderDAOSPI folderDAO;
    private final DBUtil dbUtil;

    public JdbcStorageImpl( PropertySet props ) {
        dbUtil = new DBUtil( props );
        siteDAO = new SiteDAOImpl( this, dbUtil );
        resourceDAO = new ResourceDAOImpl( this, dbUtil );
        contentDAO = new ContentDAOImpl( this, dbUtil );
        decisionDAO = new DecisionDAOImpl( this, dbUtil );
        cookieDAO = new CookieDAOImpl( this, dbUtil );
        emailAddressDAO = new EMailAddressDAOImpl( this, dbUtil );
        folderDAO = new FolderDAOImpl( this, dbUtil );
        clearDatabase( dbUtil );
    }

    protected void clearDatabase( DBUtil dbUtil ) {
        Log log = LogFactory.getLog( getClass() );
        try (
                Connection connection = dbUtil.getConnection();
        ) {
            Statement st = connection.createStatement();
            for ( String table : TABLES ) {
                log.info( "Clearing table " + table );
                st.executeUpdate( "truncate " + table );
            }
        } catch ( SQLException e ) {
            LogFactory.getLog( JdbcStorageImpl.class ).error( "SQLException during emtpy of database", e );
        }
    }

    public DBUtil getDbUtil() {
        return dbUtil;
    }

    public FolderDAOSPI getFolderDAO() {
        return folderDAO;
    }

    public SiteDAOSPI getSiteDAO() {
        return siteDAO;
    }

    public ResourceDAOSPI getResourceDAO() {
        return resourceDAO;
    }

    public ContentDAOSPI getContentDAO() {
        return contentDAO;
    }

    public DecisionDAOSPI getDecisionDAO() {
        return decisionDAO;
    }

    public CookieDAOSPI getCookieDAO() {
        return cookieDAO;
    }

    public EMailAddressDAOSPI getEMailAddressDAO() {
        return emailAddressDAO;
    }

}
