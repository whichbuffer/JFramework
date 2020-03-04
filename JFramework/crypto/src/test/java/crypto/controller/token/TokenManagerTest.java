package crypto.controller.token;

import crypto.algorithm.algorithmTestUtil;
import it.richkmeli.jframework.crypto.controller.token.TokenManager;
import org.junit.Test;

public class TokenManagerTest {

    @Test
    public void generate_verify() {
        for (int i : algorithmTestUtil.plainTextLengths) {
            String value = algorithmTestUtil.genString(i);
            String token = TokenManager.generate(value);

            //System.out.println("TEST: " + token);

            assert TokenManager.verify(token, value);
        }
    }

    @Test
    public void generate_verify_temporized() {
        for (int i : algorithmTestUtil.plainTextLengths) {
            String value = algorithmTestUtil.genString(i);
            for (int minOfVal = 0; minOfVal < 5; minOfVal++) {
                String token = TokenManager.generateTemporized(value, minOfVal);

                //System.out.println("TEST temp: " + token);

                assert TokenManager.verifyTemporized(token, value, minOfVal);
            }
        }

        // expired token
        String token = "9f12cce3aaa6de6C75362a4E8ee74b2k70a9e84U89b9de8Y5b7faddnf5fe7c9Id9072e5xc"; /*17 nov 19 - 12:45*/
        //System.out.println("TEST temp: " + token);
        assert !TokenManager.verifyTemporized(token, "expiredToken", 1);
    }

}