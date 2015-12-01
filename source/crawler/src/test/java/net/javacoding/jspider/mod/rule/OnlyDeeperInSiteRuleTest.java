package net.javacoding.jspider.mod.rule;

import net.javacoding.jspider.api.model.Decision;
import net.javacoding.jspider.core.SpiderContext;
import net.javacoding.jspider.core.impl.SpiderContextImpl;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;

/**
 * <p><tt>OnlyDeeperInSiteRuleTest</tt> </p>
 *
 * @author <a href="mailto:cstillwell@farecompare.com">cstillwell</a>
 * @version $Revision: 1.1 $
 */
public class OnlyDeeperInSiteRuleTest {

    public OnlyDeeperInSiteRuleTest() {
    }

    @Test
    public void test01() throws Exception {
        URL urlAccept = new URL("http://alpha.farecompare.com/es/vuelos/Filadelfia-PHL/Nueva_York-NYC/market.html");
        URL urlForbidden = new URL("http://alpha.farecompare.com/flights/Atlanta-ATL/Fort_Lauderdale-FLL/market.html");
        URL baseUrl = new URL("http://alpha.farecompare.com/es");
        SpiderContext context = new SpiderContextImpl( baseUrl, null, null, null );

        OnlyDeeperInSiteRule rule = new OnlyDeeperInSiteRule();

        Decision decision = rule.apply( context, null, urlAccept );
        assertEquals(Decision.RULE_ACCEPT, decision.getDecision() );

        decision = rule.apply( context, null, urlForbidden );
        assertEquals(Decision.RULE_FORBIDDEN, decision.getDecision() );
    }
}
