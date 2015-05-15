package net.javacoding.jspider.core.storage.jdbc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.javacoding.jspider.core.event.impl.*;
import net.javacoding.jspider.core.model.*;
import net.javacoding.jspider.core.storage.spi.ResourceDAOSPI;
import net.javacoding.jspider.core.storage.spi.StorageSPI;
import net.javacoding.jspider.core.storage.exception.InvalidStateTransitionException;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.logging.Log;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * $Id: ResourceDAOImpl.java,v 1.11 2003/04/19 19:00:46 vanrogu Exp $
 */
class ResourceDAOImpl implements ResourceDAOSPI {

    public static final String ATTRIBUTE_ID = "id";
    public static final int ATTRIBUTE_ID_NDX = 1;
    public static final String ATTRIBUTE_URL = "url";
    public static final int ATTRIBUTE_URL_NDX = 2;
    public static final String ATTRIBUTE_STATE = "state";
    public static final int ATTRIBUTE_STATE_NDX = 3;
    public static final String ATTRIBUTE_HTTP_STATUS = "httpstatus";
    public static final int ATTRIBUTE_HTTP_STATUS_NDX = 4;
    public static final String ATTRIBUTE_SITE = "site";
    public static final int ATTRIBUTE_SITE_NDX = 5;
    public static final String ATTRIBUTE_TIME = "timems";
    public static final int ATTRIBUTE_TIME_NDX = 6;
    public static final String ATTRIBUTE_MIME = "mimetype";
    public static final int ATTRIBUTE_MIME_NDX = 7;
    public static final String ATTRIBUTE_SIZE = "size";
    public static final int ATTRIBUTE_SIZE_NDX = 8;
    public static final String ATTRIBUTE_FOLDER = "folder";
    public static final int ATTRIBUTE_FOLDER_NDX = 9;

    protected DBUtil dbUtil;
    protected StorageSPI storage;
    protected Log log;

    /** Resource cache. */
    protected Cache<URL, ResourceInternal> knownURLs;
    protected Cache<Integer, ResourceInternal> byId;


    public ResourceDAOImpl(StorageSPI storage, DBUtil dbUtil) {
        this.storage = storage;
        this.dbUtil = dbUtil;
        this.log = LogFactory.getLog(ResourceDAOImpl.class);
        knownURLs = CacheBuilder.newBuilder()
                        .maximumSize( 100000 )
                        .expireAfterAccess( 15, TimeUnit.MINUTES )
                        .recordStats()
                        .concurrencyLevel( 32 )
                        .build();
        byId = CacheBuilder.newBuilder()
                        .maximumSize( 100000 )
                        .expireAfterAccess( 15, TimeUnit.MINUTES )
                        .recordStats()
                        .concurrencyLevel( 32 )
                        .build();
    }

    public void registerURLReference(URL url, URL refererURL) {
        ResourceInternal resource = getResource(url);
        Statement st = null;
        ResultSet rs = null;
        if (refererURL != null) {
            ResourceInternal referer = getResource(refererURL);
            String sql = "INSERT INTO jspider_resource_reference ( referer, referee, count ) VALUES (?,?,1) " +
                         "    ON DUPLICATE KEY UPDATE count = count+1";
            try (
                    Connection connection = dbUtil.getConnection();
                    PreparedStatement ps = connection.prepareStatement( sql )
            ) {
                int from = referer.getId();
                int to = resource.getId();
                ps.setInt( 1, from );
                ps.setInt( 2, to );
                ps.executeUpdate();
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                dbUtil.safeClose(rs, log);
                dbUtil.safeClose(st, log);
            }
        }
    }

    public ResourceInternal[] findAllResources() {
        ArrayList al = new ArrayList();
        String sql = "select * from jspider_resource";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            rs = ps.executeQuery();
            while (rs.next()) {
                al.add(createResourceFromRecord(rs));
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            dbUtil.safeClose(rs, log);
        }
        return (ResourceInternal[]) al.toArray(new ResourceInternal[al.size()]);
    }

    public ResourceInternal[] getRefereringResources(ResourceInternal resource) {
        ArrayList al = new ArrayList();
        String sql = "select * from jspider_resource, jspider_resource_reference where jspider_resource.id = jspider_resource_reference.referer and jspider_resource_reference.referee = ?";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, resource.getId() );
            rs = ps.executeQuery();
            while (rs.next()) {
                al.add(createResourceFromRecord(rs));
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            dbUtil.safeClose(rs, log);
        }
        return (ResourceInternal[]) al.toArray(new ResourceInternal[al.size()]);
    }

    public ResourceReferenceInternal[] getOutgoingReferences(ResourceInternal resource) {
        ArrayList al = new ArrayList();
        String sql = "select referer.url as referer, referee.url as referee, count from jspider_resource referer, jspider_resource referee, jspider_resource_reference where jspider_resource_reference.referer = ? and jspider_resource_reference.referee = referee.id and jspider_resource_reference.referer = referer.id";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, resource.getId() );
            rs = ps.executeQuery();
            while (rs.next()) {
                al.add(createResourceReferenceFromRecord(rs));
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            dbUtil.safeClose(rs, log);
        }
        return (ResourceReferenceInternal[]) al.toArray(new ResourceReferenceInternal[al.size()]);
    }

    public ResourceReferenceInternal[] getIncomingReferences(ResourceInternal resource) {
        ArrayList al = new ArrayList();
        String sql = "select referer.url as referer, referee.url as referee, count from jspider_resource referer, jspider_resource referee, jspider_resource_reference where jspider_resource_reference.referee = ? and jspider_resource_reference.referee = referee.id and jspider_resource_reference.referer = referer.id";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, resource.getId() );
            rs = ps.executeQuery();
            while (rs.next()) {
                al.add(createResourceReferenceFromRecord(rs));
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            dbUtil.safeClose(rs, log);
        }
        return (ResourceReferenceInternal[]) al.toArray(new ResourceReferenceInternal[al.size()]);
    }

    public ResourceInternal[] getReferencedResources(ResourceInternal resource) {
        ArrayList al = new ArrayList();
        String sql = "select * from jspider_resource, jspider_resource_reference where jspider_resource.id = jspider_resource_reference.referee and jspider_resource_reference.referer = ?";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, resource.getId() );
            rs = ps.executeQuery();
            while ( rs.next() ) {
                al.add( createResourceFromRecord( rs ) );
            }
        } catch ( SQLException e ) {
            log.error( "SQLException", e );
        } finally {
            dbUtil.safeClose( rs, log );
        }
        return (ResourceInternal[]) al.toArray( new ResourceInternal[al.size()] );
    }

    public ResourceInternal[] findByFolder(FolderInternal folder) {
        ArrayList al = new ArrayList();
        String sql = "select * from jspider_resource where folder=?";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, folder.getId() );
            rs = ps.executeQuery();
            while (rs.next()) {
                al.add(createResourceFromRecord(rs));
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            dbUtil.safeClose(rs, log);
        }
        return (ResourceInternal[]) al.toArray(new ResourceInternal[al.size()]);
    }

    public ResourceInternal[] getBySite(SiteInternal site) {
        ArrayList al = new ArrayList();
        String sql = "select * from jspider_resource where site=?";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, site.getId() );
            rs = ps.executeQuery();
            while (rs.next()) {
                al.add(createResourceFromRecord(rs));
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            dbUtil.safeClose(rs, log);
        }
        return (ResourceInternal[]) al.toArray(new ResourceInternal[al.size()]);
    }

    public ResourceInternal[] getRootResources(SiteInternal site) {
        ArrayList al = new ArrayList();
        String sql = "select * from jspider_resource where site=? and folder=0";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, site.getId() );
            rs = ps.executeQuery();
            while (rs.next()) {
                al.add(createResourceFromRecord(rs));
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            dbUtil.safeClose(rs, log);
        }
        return (ResourceInternal[]) al.toArray(new ResourceInternal[al.size()]);
    }

    public synchronized void setSpidered(URL url, URLSpideredOkEvent event) {
        ResourceInternal resource = getResource(url);
        resource.setFetched(event.getHttpStatus(), event.getSize(), event.getTimeMs(), event.getMimeType(), null, event.getHeaders());
        save(resource);
        resource.setBytes(event.getBytes());
    }

    public synchronized void setIgnoredForParsing(URL url) throws InvalidStateTransitionException {
        ResourceInternal resource = getResource(url);
        resource.setParseIgnored();
        save(resource);
    }

    public synchronized void setIgnoredForFetching(URL url, URLFoundEvent event) throws InvalidStateTransitionException {
        ResourceInternal resource = getResource(url);
        resource.setFetchIgnored();
        save(resource);
    }

    public synchronized void setForbidden(URL url, URLFoundEvent event) throws InvalidStateTransitionException {
        ResourceInternal resource = getResource(url);
        resource.setForbidden();
        save(resource);
    }

    public synchronized void setError(URL url, ResourceParsedErrorEvent event) throws InvalidStateTransitionException {
        ResourceInternal resource = getResource(url);
        resource.setParseError();
        save(resource);
    }

    public synchronized void setParsed(URL url, ResourceParsedOkEvent event) throws InvalidStateTransitionException {
        ResourceInternal resource = getResource(url);
        resource.setParsed();
        save(resource);
    }

    public synchronized void setError(URL url, URLSpideredErrorEvent event) throws InvalidStateTransitionException {
        ResourceInternal resource = getResource(url);
        resource.setFetchError(event.getHttpStatus(), event.getHeaders());
        save(resource);
    }

    public ResourceInternal getResource(int id) {
        ResourceInternal resource = byId.getIfPresent( id );
        if ( resource != null ) {
            return resource;
        }

        String sql = "select * from jspider_resource where id=?";
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, id );
            rs = ps.executeQuery( );
            if (rs.next()) {
                resource = createResourceFromRecord(rs);
                byId.put( id, resource );
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            dbUtil.safeClose(rs, log);
        }
        return resource;
    }

    public ResourceInternal getResource(URL url) {
        ResourceInternal resource = knownURLs.getIfPresent( url );
        if ( resource != null ) {
            return resource;
        }

        String sql = "select * from jspider_resource where url=?";
        ResultSet rs = null;
        if (url != null) {
            try (
                    Connection connection = dbUtil.getConnection();
                    PreparedStatement ps = connection.prepareStatement( sql )
            ) {
                ps.setString( 1, url.toString() );
                rs = ps.executeQuery();
                if (rs.next()) {
                    resource = createResourceFromRecord(rs);
                    knownURLs.put( url, resource );
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                dbUtil.safeClose(rs, log);
            }
        }
        return resource;
    }

    public void create(int id, ResourceInternal resource) {
        // cache in memory
        knownURLs.put( resource.getURL(), resource );
        byId.put( id, resource );

        String sql = "insert into jspider_resource (id,url,site,state,httpstatus,timems,folder) values (?,?,?,?,?,?,?)";

        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            int i = 0;
            ps.setInt( ++i, id );
            ps.setString( ++i, String.valueOf( resource.getURL() ) );
            ps.setInt( ++i, resource.getSiteId() );
            ps.setInt( ++i, resource.getState() );
            ps.setInt( ++i, resource.getHttpStatusInternal() );
            ps.setInt( ++i, resource.getTimeMsInternal() );
            FolderInternal folder = (FolderInternal) resource.getFolder();
            int folderId = (folder == null) ? 0 : folder.getId();
            ps.setInt( ++i, folderId );
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException( "Failed to create ResourceInternal: " + resource + ", id=" + id, e );
        }
    }

    public void save(ResourceInternal resource) {
        // cache in memory
        knownURLs.put( resource.getURL(), resource );
        byId.put( resource.getId(), resource );

        String sql = "update jspider_resource set state=?,mimetype=?,httpstatus=?,size=?,timems=? where id=?";
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            int i = 0;
            ps.setInt( ++i, resource.getState() );
            ps.setString( ++i, resource.getMimeInternal() );
            ps.setInt( ++i, resource.getHttpStatusInternal() );
            ps.setInt( ++i, resource.getSizeInternal() );
            ps.setInt( ++i, resource.getTimeMsInternal() );
            ps.setInt( ++i, resource.getId() );
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException( "Failed to save ResourceInternal: " + resource, e );
        }
    }

    protected ResourceInternal createResourceFromRecord(ResultSet rs) throws SQLException {
        int id = rs.getInt(ATTRIBUTE_ID_NDX);
        int folderId = rs.getInt(ATTRIBUTE_FOLDER_NDX);
        int siteId = rs.getInt(ATTRIBUTE_SITE_NDX);
        String urlString = rs.getString(ATTRIBUTE_URL_NDX);
        int state = rs.getInt(ATTRIBUTE_STATE_NDX);
        String mime = rs.getString(ATTRIBUTE_MIME_NDX);
        int time = rs.getInt(ATTRIBUTE_TIME_NDX);
        int size = rs.getInt(ATTRIBUTE_SIZE_NDX);
        int httpStatus = rs.getInt(ATTRIBUTE_HTTP_STATUS_NDX);

        FolderInternal folder = storage.getFolderDAO().findById(folderId);

        URL url = null;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            log.error("MalformedURLException", e);
        }
        ResourceInternal ri = new ResourceInternal(storage, id, siteId, url, null, folder);
        ri.setSize(size);
        ri.setTime(time);
        ri.setState(state);
        ri.setMime(mime);
        ri.setHttpStatus(httpStatus);
        return ri;
    }

    protected ResourceReferenceInternal createResourceReferenceFromRecord(ResultSet rs) throws SQLException {
        ResourceReferenceInternal rr = null;
        try {
            String refererURL = rs.getString("referer");
            String refereeURL = rs.getString("referee");
            URL referer = new URL(refererURL);
            URL referee = new URL(refereeURL);
            int count = rs.getInt("count");
            rr = new ResourceReferenceInternal(storage, referer, referee, count);
        } catch (MalformedURLException e) {
            log.error("MalformedURLException", e);
        }
        return rr;
    }

}
