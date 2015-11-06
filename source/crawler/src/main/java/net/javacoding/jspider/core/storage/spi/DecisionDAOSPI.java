package net.javacoding.jspider.core.storage.spi;

import net.javacoding.jspider.core.model.DecisionInternal;
import net.javacoding.jspider.core.model.ResourceInternal;

/**
 * $Id: DecisionDAOSPI.java,v 1.1 2003/04/11 16:37:08 vanrogu Exp $
 */
public interface DecisionDAOSPI {
    void saveSpiderDecision( ResourceInternal resource, DecisionInternal decision );

    void saveParseDecision( ResourceInternal resource, DecisionInternal decision );

    DecisionInternal findSpiderDecision( ResourceInternal resource );

    DecisionInternal findParseDecision( ResourceInternal resource );
}
