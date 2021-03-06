package it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.tls;

import it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.params.SRP6GroupParameters;

import java.math.BigInteger;

/**
 * @deprecated Migrate to the (D)TLS API in it.richkmeli.jframework.crypto.algorithm.bouncycastle.tls (bctls jar).
 */
public class TlsSRPLoginParameters {
    protected SRP6GroupParameters group;
    protected BigInteger verifier;
    protected byte[] salt;

    public TlsSRPLoginParameters(SRP6GroupParameters group, BigInteger verifier, byte[] salt) {
        this.group = group;
        this.verifier = verifier;
        this.salt = salt;
    }

    public SRP6GroupParameters getGroup() {
        return group;
    }

    public byte[] getSalt() {
        return salt;
    }

    public BigInteger getVerifier() {
        return verifier;
    }
}
