package it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.tls;

/**
 * @deprecated Migrate to the (D)TLS API in it.richkmeli.jframework.crypto.algorithm.bouncycastle.tls (bctls jar).
 */
public abstract class ServerOnlyTlsAuthentication
        implements TlsAuthentication {
    public final TlsCredentials getClientCredentials(CertificateRequest certificateRequest) {
        return null;
    }
}
