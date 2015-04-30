package net.javacoding.jspider.core.storage.jdbc;

import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.model.FolderInternal;
import net.javacoding.jspider.core.model.SiteInternal;
import net.javacoding.jspider.core.storage.spi.FolderDAOSPI;
import net.javacoding.jspider.core.storage.spi.StorageSPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * $Id: FolderDAOImpl.java,v 1.2 2003/04/11 16:37:06 vanrogu Exp $
 */
class FolderDAOImpl implements FolderDAOSPI {

    public static final String ATTRIBUTE_ID = "id";
    public static final int ATTRIBUTE_ID_NDX = 1;
    public static final String ATTRIBUTE_PARENT = "parent";
    public static final int ATTRIBUTE_PARENT_NDX = 2;
    public static final String ATTRIBUTE_SITE = "site";
    public static final int ATTRIBUTE_SITE_NDX = 3;
    public static final String ATTRIBUTE_NAME = "name";
    public static final int ATTRIBUTE_NAME_NDX = 4;

    protected Log log;

    protected StorageSPI storage;
    protected DBUtil dbUtil;

    public FolderDAOImpl ( StorageSPI storage, DBUtil dbUtil ) {
        this.log = LogFactory.getLog(FolderDAOSPI.class);
        this.storage = storage;
        this.dbUtil = dbUtil;
    }

    public FolderInternal[] findSiteRootFolders(SiteInternal site) {
        ArrayList al = new ArrayList ();

        String sql = "select * from jspider_folder where parent=0 and site=?";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1 , site.getId() );
            rs = ps.executeQuery();
            while ( rs.next() ) {
                al.add(createFolderFromRecord(rs));
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            dbUtil.safeClose(rs, log);
        }
        return (FolderInternal[])al.toArray(new FolderInternal[al.size()]);
    }

    public FolderInternal[] findSubFolders(FolderInternal folder) {
        ArrayList al = new ArrayList ( );

        String sql = "select * from jspider_folder where parent=?";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, folder.getId() );
            rs = ps.executeQuery();
            while ( rs.next() ) {
                al.add(createFolderFromRecord(rs));
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            dbUtil.safeClose(rs, log);
        }
        return (FolderInternal[])al.toArray(new FolderInternal[al.size()]);
    }

    public FolderInternal findById(int folderId) {
        FolderInternal folder = null;
        String sql = "select * from jspider_folder where id=?";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, folderId );
            rs = ps.executeQuery();
            if ( rs.next() ) {
                folder = createFolderFromRecord(rs);
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            dbUtil.safeClose(rs, log);
        }
        return folder;
    }

    public FolderInternal createFolder(int id, FolderInternal parent, String name) {
        return createFolder ( id, parent.getSiteId(), parent.getId(), name );
    }

    public FolderInternal createFolder(int id, SiteInternal site, String name) {
        return createFolder ( id, site.getId(), 0, name );
    }

    public FolderInternal createFolder ( int id, int siteId, int parentId, String name ) {
        FolderInternal folder = null;

        String sql = "insert into jspider_folder ( id, parent, site, name ) values (?,?,?,?)";

        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            int i = 0;
            ps.setInt( ++i, id );
            ps.setInt( ++i, parentId );
            ps.setInt( ++i, siteId );
            ps.setString( ++i, name );
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
        folder = findById(id);
        return folder;
    }


    protected FolderInternal createFolderFromRecord( ResultSet rs ) throws SQLException {
        int id = rs.getInt( ATTRIBUTE_ID_NDX );
        int parent = rs.getInt( ATTRIBUTE_PARENT_NDX );
        int site = rs.getInt( ATTRIBUTE_SITE_NDX );
        String name = rs.getString( ATTRIBUTE_NAME_NDX );

        return new FolderInternal( storage, id, parent, name, site );
    }

}
