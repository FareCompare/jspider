package net.javacoding.jspider.core.storage.jdbc;

import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.storage.spi.ContentDAOSPI;
import net.javacoding.jspider.core.storage.spi.StorageSPI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * $Id: ContentDAOImpl.java,v 1.3 2003/04/11 16:37:05 vanrogu Exp $
 */
class ContentDAOImpl implements ContentDAOSPI {
    public static final Charset CHARSET_UTF8 = Charset.forName( "UTF-8" );
    protected Log log;
    protected DBUtil dbUtil;
    protected StorageSPI storage;
    /** Use as cache. */
    protected Map<Integer, byte[]> contents;

    public ContentDAOImpl( StorageSPI storage, DBUtil dbUtil ) {
        this.log = LogFactory.getLog( this.getClass() );
        this.dbUtil = dbUtil;
        this.storage = storage;
        this.contents = new ConcurrentHashMap<>( );
    }

    private void setBytesInCache(int id, byte[] bytes) {
        contents.put( id, bytes);
    }

    public void setBytes( int id, byte[] bytes ) {
        setBytesInCache( id, bytes );
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO jspider_content ( id, content ) VALUES ( ?, ? )" );
        ) {
            ps.setInt( 1, id );
            ps.setBytes( 2, compressBytes( bytes ) );
            try {
                ps.execute();
            }
            catch ( IllegalArgumentException e ) {
                log.error( "IllegalArgumentException", e );
            }
        }
        catch ( Exception e ) {
            log.error( "Failed to insert content id=" + id + " into table. " + bytes.length + " bytes long", e );
        }
    }

    private InputStream getInputStreamFromCache(int id) {
        byte[] bytes = contents.get( id );
        if ( bytes != null ) {
            return new ByteArrayInputStream ( bytes );
        } else {
            return null;
        }
    }

    public InputStream getInputStream( int id ) {
        InputStream inputStream = getInputStreamFromCache( id );
        if ( inputStream != null ) {
            return inputStream;
        }

        byte[] bytes = null;
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( "SELECT content FROM jspider_content WHERE id=?" );
        ) {
            ps.setInt( 1, id );
            rs = ps.executeQuery();
            if ( rs.next() ) {
                byte[] compressedBytes = rs.getBytes( "content" );
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
