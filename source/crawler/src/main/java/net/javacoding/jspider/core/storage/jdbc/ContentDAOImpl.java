package net.javacoding.jspider.core.storage.jdbc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.storage.spi.ContentDAOSPI;
import net.javacoding.jspider.core.storage.spi.StorageSPI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * $Id: ContentDAOImpl.java,v 1.3 2003/04/11 16:37:05 vanrogu Exp $
 */
class ContentDAOImpl implements ContentDAOSPI {
    protected Log log;
    protected DBUtil dbUtil;
    protected StorageSPI storage;
    /** Use as cache. */
    protected Cache<Integer, byte[]> contents;

    public ContentDAOImpl( StorageSPI storage, DBUtil dbUtil ) {
        this.log = LogFactory.getLog( this.getClass() );
        this.dbUtil = dbUtil;
        this.storage = storage;
        this.contents = CacheBuilder.newBuilder()
                .maximumSize( 50000 )
                .expireAfterAccess( 15, TimeUnit.MINUTES )
                .recordStats()
                .concurrencyLevel( 32 )
                .build();

    }

    private void setBytesInCache( int id, byte[] bytes ) {
        contents.put( id, bytes );
    }

    public void setBytes( int id, byte[] bytes ) {
        setBytesInCache( id, bytes );
        String sql = "insert into jspider_content ( id, content )\n" +
                     "    values ( ?, ? )\n" +
                     "  on duplicate key update\n" +
                     "    content = values(content)," +
                     "    counter = counter + 1";

        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, id );
            ps.setBytes( 2, compressBytes( bytes ) );
            ps.execute();
        }
        catch ( Exception e ) {
            log.error( "Failed to insert content id=" + id + " into table. " + bytes.length + " bytes long", e );
        }
    }

    private InputStream getInputStreamFromCache( int id ) {
        byte[] bytes = contents.getIfPresent( id );
        if ( bytes != null ) {
            return new ByteArrayInputStream( bytes );
        }
        else {
            return null;
        }
    }

    public InputStream getInputStream( int id ) {
        InputStream inputStream = getInputStreamFromCache( id );
        if ( inputStream != null ) {
            return inputStream;
        }

        String sql = "SELECT content FROM jspider_content WHERE id=?";
        byte[] bytes = new byte[0];
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, id );
            rs = ps.executeQuery();
            if ( rs.next() ) {
                byte[] compressedBytes = rs.getBytes( 1 );
                bytes = deCompressBytes( compressedBytes, 0, compressedBytes.length );
            }
        }
        catch ( Exception e ) {
            log.error( "Failed to read content from table, id=" + id, e );
        }
        finally {
            dbUtil.safeClose( rs, log );
        }
        return new ByteArrayInputStream( bytes );
    }

    byte[] compressBytes( byte[] inputBytes ) throws IOException {
        try (
                ByteArrayOutputStream baos = new ByteArrayOutputStream( inputBytes.length );
                GZIPOutputStream gzipInputStream = new GZIPOutputStream( baos )
        ) {
            gzipInputStream.write( inputBytes );
            gzipInputStream.close();
            return baos.toByteArray();
        }
    }

    byte[] deCompressBytes( byte[] bytes, int offset, int length ) throws IOException {
        InputStream in = new GZIPInputStream( new ByteArrayInputStream( bytes, offset, length ) );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[8192];
        int len;
        while ( ( len = in.read( buffer ) ) > 0 ) {
            baos.write( buffer, 0, len );
        }
        return baos.toByteArray();
    }
}
