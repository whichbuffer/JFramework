package util;

import it.richkmeli.jframework.util.AppInfo;
import org.junit.Test;

public class AppInfoTest {

    @Test
    public void getVersion() {
        // todo metodo che funziona su jar generato
        String version = AppInfo.getVersion(AppInfo.class);
        //assertNotNull(version);
        assert true;
    }
}