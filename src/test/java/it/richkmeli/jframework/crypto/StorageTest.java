package it.richkmeli.jframework.crypto;

import it.richkmeli.jframework.crypto.algorithm.AES;
import it.richkmeli.jframework.crypto.algorithm.DiffieHellman;
import it.richkmeli.jframework.crypto.data.SecureDataManager;
import it.richkmeli.jframework.crypto.model.ClientSecureData;
import it.richkmeli.jframework.crypto.model.DiffieHellmanPayload;
import it.richkmeli.jframework.crypto.model.ServerSecureData;
import org.junit.Test;

import javax.crypto.SecretKey;
import java.io.File;
import java.math.BigInteger;
import java.security.*;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class StorageTest {
    @Test
    public void clientSecureDataJSON() {
        File csdFile = new File("clientSecureData.txt");
        String clientKey = "testkeyClient";

        System.out.println("clientSecureData delete: " + csdFile.delete());

        // init and load random data
        SecureDataManager.initClientSecureData(csdFile, clientKey);
        ClientSecureData clientSecureData = fillRandomlyClientSecureData();
        // SAVE TO FILE
        String clientSecureDataJSON = clientSecureData.getClientSecureDataJSON();
        SecureDataManager.setClientSecureData(csdFile, clientKey, clientSecureData);
        // LOAD FROM FILE
        ClientSecureData loadedClientSecureData = SecureDataManager.getClientSecureData(csdFile, clientKey);
        String loadedClientSecureDataJSON = loadedClientSecureData.getClientSecureDataJSON();

        assertEquals(clientSecureDataJSON, loadedClientSecureDataJSON);
    }

    @Test
    public void serverSecureDataJSON() {
        File sdsFile = new File("serverSecureData.txt");
        String clientID = "USER-001";
        String serverKey = "testkeyServer";

        System.out.println("serverSecureData delete: " + sdsFile.delete());

        SecureDataManager.initServerSecureData(sdsFile, serverKey);

        ServerSecureData serverSecureData = fillRandomlyServerSecureData();
        String serverSecureDataJSON = serverSecureData.getServerSecureDataJSON();
        SecureDataManager.setServerSecureData(sdsFile, serverKey, serverSecureData);

        ServerSecureData serverSecureData2 = SecureDataManager.getServerSecureData(sdsFile, serverKey);
        String serverSecureData2JSON = serverSecureData2.getServerSecureDataJSON();

        assertEquals(serverSecureDataJSON, serverSecureData2JSON);
    }


    private ClientSecureData fillRandomlyClientSecureData() {
        try {
            List<BigInteger> pg = DiffieHellman.DH_0_A();
            KeyPair keys_A = DiffieHellman.DH_1(pg);
            DiffieHellmanPayload diffieHellmanPayload = DiffieHellman.DH_2_A(pg, keys_A.getPublic());
            SecretKey secretKey_A = DiffieHellman.DH_3(keys_A.getPrivate(), keys_A.getPublic(), AES.ALGORITHM);
            return new ClientSecureData(keys_A, diffieHellmanPayload, keys_A.getPublic(), secretKey_A);
        } catch (InvalidAlgorithmParameterException | NoSuchProviderException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ServerSecureData fillRandomlyServerSecureData() {
        try {
            List<BigInteger> pg = DiffieHellman.DH_0_A();
            KeyPair keys_A = DiffieHellman.DH_1(pg);
            DiffieHellmanPayload diffieHellmanPayload = DiffieHellman.DH_2_A(pg, keys_A.getPublic());

            KeyPair keys_B = DiffieHellman.DH_1(diffieHellmanPayload.getPQ());

            PublicKey publicKey = keys_B.getPublic();
            ServerSecureData serverSecureData = new ServerSecureData(pg, keys_B);
            serverSecureData.addDiffieHellmanPayload("ID", diffieHellmanPayload);

            SecretKey secretKey_B = DiffieHellman.DH_3(serverSecureData.getKeyPairServer().getPrivate(),
                    serverSecureData.getDiffieHellmanPayload("ID").getA(),
                    AES.ALGORITHM);
            serverSecureData.addSecretKey("ID", secretKey_B);

            return serverSecureData;
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

}