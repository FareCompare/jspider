package net.javacoding.jspider.core.storage.jdbc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * $Id: FolderDAOImpl.java,v 1.2 2003/04/11 16:37:06 vanrogu Exp $
 */
class FolderDAOImpl implements FolderDAOSPI {

    public static final int ATTRIBUTE_ID_NDX = 1;
    public static final int ATTRIBUTE_PARENT_NDX = 2;
    public static final int ATTRIBUTE_SITE_NDX = 3;
    public static final int ATTRIBUTE_NAME_NDX = 4;

    protected Log log;

    protected StorageSPI storage;
    protected DBUtil dbUtil;

    private Cache<Integer, FolderInternal> byId;
    private Cache<Integer, Set<FolderInternal>> byParent;
    private Cache<Integer, Set<FolderInternal>> siteRoots;

    public FolderDAOImpl ( StorageSPI storage, DBUtil dbUtil ) {
        this.log = LogFactory.getLog(FolderDAOSPI.class);
        this.storage = storage;
        this.dbUtil = dbUtil;
        byId = CacheBuilder.newBuilder()
                        .maximumSize( 100000 )
                        .expireAfterAccess( 15, TimeUnit.MINUTES )
                        .recordStats()
                        .concurrencyLevel( 32 )
                        .build();
        byParent = CacheBuilder.newBuilder()
                        .maximumSize( 100000 )
                        .expireAfterAccess( 15, TimeUnit.MINUTES )
                        .recordStats()
                        .concurrencyLevel( 32 )
                        .build();
        siteRoots = CacheBuilder.newBuilder()
                        .maximumSize( 100000 )
                        .expireAfterAccess( 15, TimeUnit.MINUTES )
                        .recordStats()
                        .concurrencyLevel( 32 )
                        .build();
    }

    public FolderInternal[] findSiteRootFolders(SiteInternal site) {
        Set<FolderInternal> rootFolders = siteRoots.getIfPresent( site.getId() );
        if ( rootFolders != null ) {
            synchronized (rootFolders) {
                return rootFolders.toArray( new FolderInternal[rootFolders.size()] );
            }
        }
        rootFolders = new HashSet<>(  );

        String sql = "select * from jspider_folder where parent=0 and site=?";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1 , site.getId() );
            rs = ps.executeQuery();
            while ( rs.next() ) {
                rootFolders.add( createFolderFromRecord( rs ) );
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            dbUtil.safeClose(rs, log);
        }
        siteRoots.put( site.getId(), rootFolders );
        synchronized (rootFolders) {
            return rootFolders.toArray(new FolderInternal[rootFolders.size()]);
        }
    }

    public FolderInternal[] findSubFolders(FolderInternal folder) {
        Set<FolderInternal> subFolders = byParent.getIfPresent( folder.getParentId() );
        if ( subFolders != null ) {
            synchronized (subFolders) {
                return subFolders.toArray( new FolderInternal[subFolders.size()] );
            }
        }

        subFolders = new HashSet<>( );
        String sql = "select * from jspider_folder where parent=?";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, folder.getId() );
            rs = ps.executeQuery();
            while ( rs.next() ) {
                subFolders.add(createFolderFromRecord(rs));
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            dbUtil.safeClose(rs, log);
        }
        byParent.put( folder.getParentId(), subFolders );
        synchronized (subFolders) {
            return subFolders.toArray(new FolderInternal[subFolders.size()]);
        }
    }

    public FolderInternal findById(int folderId) {
        FolderInternal folder = byId.getIfPresent( folderId );
        if ( folder != null ) {
            return folder;
        }
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
                byId.put( folderId, folder );
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            dbUtil.safeClose(rs, log);
        }
        return folder;
    }

    public FolderInternal createFolder(int id, FolderInternal parent, String name) {
        FolderInternal folder = createFolder( id, parent.getSiteId(), parent.getId(), name );
        addSubFolder( parent.getId(), folder );
        return folder;
    }

    public FolderInternal createFolder(int id, SiteInternal site, String name) {
        FolderInternal folder = createFolder( id, site.getId(), 0, name );
        addSiteRoot( site.getId(), folder );
        return folder;
    }

    public FolderInternal createFolder ( int id, int siteId, int parentId, String name ) {
        FolderInternal folder = new FolderInternal( storage, id, parentId, name, siteId );
        byId.put( id, folder );
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

        return folder;
    }

    private void addSiteRoot( int siteId, FolderInternal folder ) {
        Set<FolderInternal> rootFolders = siteRoots.getIfPresent( siteId );
        if ( rootFolders == null ) {
            rootFolders = new HashSet<>(  );
            siteRoots.put( siteId, rootFolders );
        }
        synchronized (rootFolders) {
            rootFolders.add( folder );
        }
    }

    private void addSubFolder( int parent, FolderInternal folder ) {
        Set<FolderInternal> subFolders = byParent.getIfPresent( parent );
        if ( subFolders == null ) {
            subFolders = new HashSet<>(  );
            byParent.put( parent, subFolders );
        }
        synchronized (subFolders) {
            subFolders.add( folder );
        }
    }


    protected FolderInternal createFolderFromRecord( ResultSet rs ) throws SQLException {
        int id = rs.getInt( ATTRIBUTE_ID_NDX );
        int parent = rs.getInt( ATTRIBUTE_PARENT_NDX );
        int site = rs.getInt( ATTRIBUTE_SITE_NDX );
        String name = rs.getString( ATTRIBUTE_NAME_NDX );

        return new FolderInternal( storage, id, parent, name, site );
    }

}
