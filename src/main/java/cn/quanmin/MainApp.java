package cn.quanmin;

import cn.quanmin.appanalyse.AppMarketWebWorm;

/**
 * A Camel Application
 */
public class MainApp {
    /**
     * A main() so we can easily run these routing rules in our IDE
     */
    public static void main(String... args) throws Exception {
        AppMarketWebWorm yybWebWorm = new AppMarketWebWorm();
        yybWebWorm.fetchPage();
    }
}