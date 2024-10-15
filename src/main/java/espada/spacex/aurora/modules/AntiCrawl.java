package espada.spacex.aurora.modules;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;

/**
 * @author OLEPOSSU
 */

public class AntiCrawl extends Modules {
    public AntiCrawl() {
        super(Aurora.AURORA, "Anti Crawl", "Doesn't crawl or sneak when in low space (should be used on 1.12.2).");
    }
}
