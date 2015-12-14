package net.javacoding.jspider.core.storage.jdbc;

import net.javacoding.jspider.api.model.DecisionStep;
import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.model.DecisionInternal;
import net.javacoding.jspider.core.model.ResourceInternal;
import net.javacoding.jspider.core.storage.DecisionDAO;
import net.javacoding.jspider.core.storage.spi.DecisionDAOSPI;
import net.javacoding.jspider.core.storage.spi.StorageSPI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * $Id: DecisionDAOImpl.java,v 1.3 2003/04/11 16:37:06 vanrogu Exp $
 */
class DecisionDAOImpl implements DecisionDAOSPI {
    public static final int SUBJECT_SPIDER = 1;
    public static final int SUBJECT_PARSE = 2;
    protected Log log;
    protected DBUtil dbUtil;
    protected StorageSPI storage;

    public DecisionDAOImpl( StorageSPI storage, DBUtil dbUtil ) {
        this.log = LogFactory.getLog( DecisionDAO.class );
        this.dbUtil = dbUtil;
        this.storage = storage;
    }

    public void saveSpiderDecision( ResourceInternal resource, DecisionInternal decision ) {
        saveDecision( SUBJECT_SPIDER, resource, decision );
    }

    public void saveParseDecision( ResourceInternal resource, DecisionInternal decision ) {
        saveDecision( SUBJECT_PARSE, resource, decision );
    }

    protected void saveDecision( int subject, ResourceInternal resource, DecisionInternal decision ) {
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO jspider_decision ( resource, subject, type, comment ) VALUES (?,?,?,?)" +
                        "       ON DUPLICATE KEY UPDATE type = VALUES(type), comment = VALUES(comment)" );
                PreparedStatement ps2 = connection.prepareStatement(
                        "INSERT INTO jspider_decision_step ( resource, subject, sequence, type, rule, decision, comment ) VALUES (?,?,?,?,?,?,?)" +
                        "       ON DUPLICATE KEY UPDATE type = VALUES(type), comment = VALUES(comment)")

        ) {
            ps.setInt( 1, resource.getId() );
            ps.setInt( 2, ( subject ) );
            ps.setInt( 3, ( decision.getDecision() ) );
            ps.setString( 4, ( decision.getComment() ) );
            ps.executeUpdate();

            DecisionStep[] steps = decision.getSteps();
            for ( int i = 0; i < steps.length; i++ ) {
                int index = 0;
                DecisionStep step = steps[i];
                ps2.setInt( ++index, resource.getId() );
                ps2.setInt( ++index, subject );
                ps2.setInt( ++index, i );
                ps2.setInt( ++index, ( step.getRuleType() ) );
                ps2.setString( ++index, ( step.getRule() ) );
                ps2.setInt( ++index, ( step.getDecision() ) );
                ps2.setString( ++index, ( step.getComment() ) );
                ps2.addBatch();
            }
            if ( decision.getSteps().length > 0 ) {
                ps2.executeBatch();
            }
        }
        catch ( SQLException e ) {
            log.error( String.format("SQLException on saveDecision%n\tsubject: %s%n\tresource: %s%n\tdecision: %s",
                                     subject, resource, decision), e );
        }
    }

    public DecisionInternal findSpiderDecision( ResourceInternal resource ) {
        return findDecision( SUBJECT_SPIDER, resource );
    }

    public DecisionInternal findParseDecision( ResourceInternal resource ) {
        return findDecision( SUBJECT_PARSE, resource );
    }

    protected DecisionInternal findDecision( int subject, ResourceInternal resource ) {
        String sql = "select d.type, d.comment, s.rule, s.type, s.decision, s.comment\n" +
                     "  from jspider_decision d\n" +
                     "    left join jspider_decision_step s\n" +
                     "      on d.resource = s.resource\n" +
                     "        and d.subject = s.subject\n" +
                     "  where d.resource = ?\n" +
                     "    and d.subject = ?\n" +
                     "  order by s.sequence";

        DecisionInternal decision = null;
        ResultSet rs = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement( sql )
        ) {
            ps.setInt( 1, resource.getId() );
            ps.setInt( 2, subject );
            rs = ps.executeQuery();
            while ( rs.next() ) {
                if ( decision == null ) {
                    decision = new DecisionInternal( rs.getInt( 1 ), rs.getString( 2 ) );
                }

                decision.addStep( rs.getString( 3 ), rs.getInt( 4 ), rs.getInt( 5 ), rs.getString( 6 ) );
            }
        }
        catch ( SQLException e ) {
            log.error( "SQLException", e );
        }
        finally {
            dbUtil.safeClose( rs, log );
        }
        return decision;
    }
}
