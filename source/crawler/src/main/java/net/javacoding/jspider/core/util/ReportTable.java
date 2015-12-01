package net.javacoding.jspider.core.util;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p><tt>ReportTable</tt> </p>
 * A convenient report generator that formats headers and data into more readable format.
 *
 * @author <a href="mailto:chengwei.lim@farecompare.com">Chengwei Lim</a>
 * @version $Revision$
 */
public class ReportTable {
    private List<String> _headers;
    private List<String[]> _rows;
    private Map<Integer, Integer> _columnSize;
    private String _delimiter;

    private String _alternatingRowColor;

    public ReportTable( String delimiter ) {
        _headers = new ArrayList<String>();
        _columnSize = new HashMap<Integer, Integer>();
        _rows = new ArrayList<String[]>();
        _delimiter = delimiter;
        _alternatingRowColor = "FFDEAD";
    }

    public void clearRows() {
        _rows.clear();
    }

    public boolean hasHeaders() {
        return !_headers.isEmpty();
    }

    public void addHeaders( String... headers ) {
        for ( int i = 0; i < headers.length; i++ ) {
            _headers.add( headers[i] );
            setColumnSize( i, headers[i].length() );
        }
    }

    public void addHeaders( Collection<String> headersAsCollection ) {
        List<String> headers = new ArrayList<String>();
        headers.addAll( headersAsCollection );

        for ( int i = 0; i < headers.size(); i++ ) {
            _headers.add( headers.get( i ) );
            setColumnSize( i, headers.get( i ).length() );
        }
    }

    private void setColumnSize( int columnIndex, int columnSize ) {
        Integer stored = _columnSize.get( columnIndex );
        if ( stored == null || stored < columnSize ) {
            _columnSize.put( columnIndex, columnSize );
        }
    }

    public void addRow( String... data ) {
        _rows.add( data );
        for ( int i = 0; i < data.length; i++ ) {
            if ( data[i] != null ) {
                setColumnSize( i, data[i].length() );
            }
        }
    }

    public String getAlternatingRowColor() {
        return _alternatingRowColor;
    }

    public void setAlternatingRowColor( String alternatingRowColor ) {
        _alternatingRowColor = alternatingRowColor;
    }

    public void addRow( Collection<String> rowAsCollection ) {
        String[] row = new String[rowAsCollection.size()];
        int i = 0;
        for ( String value : rowAsCollection ) {
            row[i++] = value;
        }
        addRow( row );
    }

    public boolean isEmpty() {
        return _rows.isEmpty();
    }

    public String buildReport() {
        StringBuilder sb = new StringBuilder( "\n" );
        // header
        for ( int i = 0; i < _headers.size(); i++ ) {
            if ( i > 0 ) {
                sb.append( _delimiter );
            }
            sb.append( StringUtils.rightPad( _headers.get( i ), _columnSize.get( i ) ) );
        }
        sb.append( "\n" );
        for ( int i = 0; i < _headers.size(); i++ ) {
            if ( i > 0 ) {
                sb.append( _delimiter );
            }
            sb.append( StringUtils.rightPad( "", _columnSize.get( i ),"-" ) );
        }
        sb.append( "\n" );
        // rows
        for ( int i = 0; i < _rows.size(); i++ ) {
            String[] row = _rows.get( i );
            for ( int j = 0; j < row.length; j++ ) {
                if ( j > 0 ) {
                    sb.append( _delimiter );
                }
                if ( row[j] == null ) {
                    row[j] = "";
                }
                sb.append( StringUtils.rightPad( row[j], _columnSize.get( j ) ) );
            }
            sb.append( "\n" );
        }

        if ( sb.length() == 0 ) {
            return "Table contains no data";
        }
        return sb.toString();
    }

    public String buildCSVReport() {
        StringBuilder sb = new StringBuilder();
        // header
        if (!_headers.isEmpty()) {
            for ( int i = 0; i < _headers.size(); i++ ) {
                if ( i > 0 ) {
                    sb.append( _delimiter );
                }
                sb.append( _headers.get( i ) );
            }
            sb.append( "\n" );
        }
        // rows
        for ( int i = 0; i < _rows.size(); i++ ) {
            String[] row = _rows.get( i );
            for ( int j = 0; j < row.length; j++ ) {
                if ( j > 0 ) {
                    sb.append( _delimiter );
                }
                if ( row[j] == null ) {
                    row[j] = "";
                }
                sb.append( row[j] );
            }
            sb.append( "\n" );
        }

        if ( sb.length() == 0 ) {
            return "Table contains no data";
        }
        return sb.toString();
    }

    public String buildHtmlReport() {
        StringBuilder sb = new StringBuilder(  );
        sb.append( "<table border='1' cellpadding='2'>\n" );
        addHtmlTableHeaders( sb );
        sb.append( "\n" );
        // rows
        for ( int i = 0; i < _rows.size(); i++ ) {
            String[] row = _rows.get( i );
            addHtmlTableRow( sb, row, i % 2 == 1 );
            sb.append( "\n" );
        }
        sb.append( "</table>" );
        return sb.toString();
    }

    protected void addHtmlTableHeaders( StringBuilder sb ) {
        sb.append( "<tr>" );
        for ( int i = 0; i < _headers.size(); i++ ) {
            sb.append( "<th>" ).append( StringUtils.rightPad( _headers.get( i ), _columnSize.get( i ) ) ).append( "</th>" );
        }
        sb.append( "</tr>" );
    }

    protected void addHtmlTableRow( StringBuilder sb, String[] row, boolean shade ) {
        sb.append( "<tr " );
        if ( shade ) sb.append( " bgcolor=\"#" ).append( _alternatingRowColor ).append( "\";" );
        sb.append( ">" );
        for ( int j = 0; j < row.length; j++ ) {
            if ( row[j] == null ) {
                row[j] = "";
            }
            sb.append( "<td>" ).append( StringUtils.rightPad( row[j], _columnSize.get( j ) ) ).append( "</td>");
        }
        sb.append( "</tr>" );
    }
}
