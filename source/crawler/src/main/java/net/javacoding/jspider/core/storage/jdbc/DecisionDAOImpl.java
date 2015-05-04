package net.javacoding.jspider.core.storage.jdbc;

import net.javacoding.jspider.core.storage.DecisionDAO;
import net.javacoding.jspider.core.storage.spi.DecisionDAOSPI;
import net.javacoding.jspider.core.storage.spi.StorageSPI;
import net.javacoding.jspider.core.logging.LogFactory;
import net.javacoding.jspider.core.logging.Log;
import net.javacoding.jspider.core.model.*;
import net.javacoding.jspider.api.model.*;

import java.sql.*;

/**
 * $Id: DecisionDAOImpl.java,v 1.3 2003/04/11 16:37:06 vanrogu Exp $
 */
class DecisionDAOImpl implements DecisionDAOSPI {

    public static final int SUBJECT_SPIDER = 1;
    public static final int SUBJECT_PARSE = 2;

    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_SUBJECT = "subject";
    public static final String ATTRIBUTE_TYPE = "type";
    public static final String ATTRIBUTE_COMMENT = "comment";
    public static final String ATTRIBUTE_DECISION = "decision";
    public static final String ATTRIBUTE_RULE = "rule";

    protected Log log;

    protected DBUtil dbUtil;
    protected StorageSPI storage;

    public DecisionDAOImpl ( StorageSPI storage, DBUtil dbUtil ) {
        this.log = LogFactory.getLog(DecisionDAO.class);
        this.dbUtil = dbUtil;
        this.storage = storage;
    }

    public void saveSpiderDecision(ResourceInternal resource, DecisionInternal decision) {
        saveDecision(SUBJECT_SPIDER,resource, decision);
    }

    public void saveParseDecision(ResourceInternal resource, DecisionInternal decision) {
        saveDecision(SUBJECT_PARSE,resource, decision);
    }

    protected void saveDecision ( int subject, ResourceInternal resource, DecisionInternal decision ) {
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement("insert into jspider_decision ( resource, subject, type, comment ) values (?,?,?,?)");
        ) {
            ps.setInt(1, resource.getId());
            ps.setInt(2, (subject));
            ps.setInt(3, (decision.getDecision()));
            ps.setString(4, (decision.getComment()));
            ps.executeUpdate();

            DecisionStep[] steps = decision.getSteps();
            try (
                    PreparedStatement ps2 = connection.prepareStatement("insert into jspider_decision_step ( resource, subject, sequence, type, rule, decision, comment ) values (?,?,?,?,?,?,?)");
            ){
                for (int i = 0; i < steps.length; i++) {
                    DecisionStep step = steps[i];
                    ps2.setInt(1, resource.getId());
                    ps2.setInt(2, subject);
                    ps2.setInt(3, i);
                    ps2.setInt(4, (step.getRuleType())) ;
                    ps2.setString(5, (step.getRule()));
                    ps2.setInt(6, (step.getDecision()));
                    ps2.setString(7, (step.getComment()));
                    ps2.executeUpdate();
                }
            } catch ( SQLException e ) {
                throw e;
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        }
    }

    public DecisionInternal findSpiderDecision(ResourceInternal resource) {
        return findDecision ( SUBJECT_SPIDER, resource);
    }

    public DecisionInternal findParseDecision(ResourceInternal resource) {
        return findDecision ( SUBJECT_PARSE, resource);
    }

    protected DecisionInternal findDecision ( int subject, ResourceInternal resource ) {
        DecisionInternal decision = null;
        ResultSet rs = null;
        ResultSet rs2 = null;
        try (
                Connection connection = dbUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement("select type, comment from jspider_decision where resource=? and subject=?");
                PreparedStatement ps2 = connection.prepareStatement("select rule, type, decision, comment from jspider_decision_step where resource=? and subject=? order by sequence");
        ) {
            ps.setInt(1, resource.getId());
            ps.setInt(2, subject);
            rs = ps.executeQuery();
            if ( rs.next() ) {
                int type = rs.getInt(1);
                String comment = rs.getString(2);
                decision = new DecisionInternal ( type, comment );

                ps2.setInt(1, resource.getId());
                ps2.setInt(2, subject);
                rs2 = ps2.executeQuery();
                while ( rs2.next ( ) ) {
                    String rule = rs2.getString(1);
                    int stepType = rs2.getInt(2);
                    int stepDecision = rs2.getInt(3);
                    String stepComment = rs2.getString(4);
                    decision.addStep(rule,stepType, stepDecision, stepComment);
                }
            }
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally{
            dbUtil.safeClose(rs, log);
            dbUtil.safeClose(rs2, log);
        }
        return decision;
    }

}
