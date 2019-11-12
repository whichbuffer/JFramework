package it.richkmeli.jframework.crypto.algorithm.bouncycastle.pqc.crypto.util;

import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.ASN1InputStream;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.ASN1ObjectIdentifier;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.ASN1OctetString;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.ASN1Primitive;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.bc.BCObjectIdentifiers;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.x509.AlgorithmIdentifier;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.params.AsymmetricKeyParameter;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.pqc.asn1.*;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.pqc.crypto.newhope.NHPrivateKeyParameters;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.pqc.crypto.qtesla.QTESLAPrivateKeyParameters;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.pqc.crypto.sphincs.SPHINCSPrivateKeyParameters;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.pqc.crypto.xmss.*;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.util.Pack;

import java.io.IOException;
import java.io.InputStream;

/**
 * Factory for creating private key objects from PKCS8 PrivateKeyInfo objects.
 */
public class PrivateKeyFactory {
    /**
     * Create a private key parameter from a PKCS8 PrivateKeyInfo encoding.
     *
     * @param privateKeyInfoData the PrivateKeyInfo encoding
     * @return a suitable private key parameter
     * @throws IOException on an error decoding the key
     */
    public static AsymmetricKeyParameter createKey(byte[] privateKeyInfoData) throws IOException {
        return createKey(PrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray(privateKeyInfoData)));
    }

    /**
     * Create a private key parameter from a PKCS8 PrivateKeyInfo encoding read from a
     * stream.
     *
     * @param inStr the stream to read the PrivateKeyInfo encoding from
     * @return a suitable private key parameter
     * @throws IOException on an error decoding the key
     */
    public static AsymmetricKeyParameter createKey(InputStream inStr) throws IOException {
        return createKey(PrivateKeyInfo.getInstance(new ASN1InputStream(inStr).readObject()));
    }

    /**
     * Create a private key parameter from the passed in PKCS8 PrivateKeyInfo object.
     *
     * @param keyInfo the PrivateKeyInfo object containing the key material
     * @return a suitable private key parameter
     * @throws IOException on an error decoding the key
     */
    public static AsymmetricKeyParameter createKey(PrivateKeyInfo keyInfo) throws IOException {
        AlgorithmIdentifier algId = keyInfo.getPrivateKeyAlgorithm();
        ASN1ObjectIdentifier algOID = algId.getAlgorithm();

        if (algOID.on(BCObjectIdentifiers.qTESLA)) {
            ASN1OctetString qTESLAPriv = ASN1OctetString.getInstance(keyInfo.parsePrivateKey());

            return new QTESLAPrivateKeyParameters(Utils.qTeslaLookupSecurityCategory(keyInfo.getPrivateKeyAlgorithm()), qTESLAPriv.getOctets());
        } else if (algOID.equals(BCObjectIdentifiers.sphincs256)) {
            return new SPHINCSPrivateKeyParameters(ASN1OctetString.getInstance(keyInfo.parsePrivateKey()).getOctets(),
                    Utils.sphincs256LookupTreeAlgName(SPHINCS256KeyParams.getInstance(keyInfo.getPrivateKeyAlgorithm().getParameters())));
        } else if (algOID.equals(BCObjectIdentifiers.newHope)) {
            return new NHPrivateKeyParameters(convert(ASN1OctetString.getInstance(keyInfo.parsePrivateKey()).getOctets()));
        } else if (algOID.equals(BCObjectIdentifiers.xmss)) {
            XMSSKeyParams keyParams = XMSSKeyParams.getInstance(keyInfo.getPrivateKeyAlgorithm().getParameters());
            ASN1ObjectIdentifier treeDigest = keyParams.getTreeDigest().getAlgorithm();

            XMSSPrivateKey xmssPrivateKey = XMSSPrivateKey.getInstance(keyInfo.parsePrivateKey());

            try {
                XMSSPrivateKeyParameters.Builder keyBuilder = new XMSSPrivateKeyParameters
                        .Builder(new XMSSParameters(keyParams.getHeight(), Utils.getDigest(treeDigest)))
                        .withIndex(xmssPrivateKey.getIndex())
                        .withSecretKeySeed(xmssPrivateKey.getSecretKeySeed())
                        .withSecretKeyPRF(xmssPrivateKey.getSecretKeyPRF())
                        .withPublicSeed(xmssPrivateKey.getPublicSeed())
                        .withRoot(xmssPrivateKey.getRoot());

                if (xmssPrivateKey.getVersion() != 0) {
                    keyBuilder.withMaxIndex(xmssPrivateKey.getMaxIndex());
                }

                if (xmssPrivateKey.getBdsState() != null) {
                    BDS bds = (BDS) XMSSUtil.deserialize(xmssPrivateKey.getBdsState(), BDS.class);
                    keyBuilder.withBDSState(bds.withWOTSDigest(treeDigest));
                }

                return keyBuilder.build();
            } catch (ClassNotFoundException e) {
                throw new IOException("ClassNotFoundException processing BDS state: " + e.getMessage());
            }
        } else if (algOID.equals(PQCObjectIdentifiers.xmss_mt)) {
            XMSSMTKeyParams keyParams = XMSSMTKeyParams.getInstance(keyInfo.getPrivateKeyAlgorithm().getParameters());
            ASN1ObjectIdentifier treeDigest = keyParams.getTreeDigest().getAlgorithm();

            try {
                XMSSMTPrivateKey xmssMtPrivateKey = XMSSMTPrivateKey.getInstance(keyInfo.parsePrivateKey());

                XMSSMTPrivateKeyParameters.Builder keyBuilder = new XMSSMTPrivateKeyParameters
                        .Builder(new XMSSMTParameters(keyParams.getHeight(), keyParams.getLayers(), Utils.getDigest(treeDigest)))
                        .withIndex(xmssMtPrivateKey.getIndex())
                        .withSecretKeySeed(xmssMtPrivateKey.getSecretKeySeed())
                        .withSecretKeyPRF(xmssMtPrivateKey.getSecretKeyPRF())
                        .withPublicSeed(xmssMtPrivateKey.getPublicSeed())
                        .withRoot(xmssMtPrivateKey.getRoot());

                if (xmssMtPrivateKey.getVersion() != 0) {
                    keyBuilder.withMaxIndex(xmssMtPrivateKey.getMaxIndex());
                }

                if (xmssMtPrivateKey.getBdsState() != null) {
                    BDSStateMap bdsState = (BDSStateMap) XMSSUtil.deserialize(xmssMtPrivateKey.getBdsState(), BDSStateMap.class);
                    keyBuilder.withBDSState(bdsState.withWOTSDigest(treeDigest));
                }

                return keyBuilder.build();
            } catch (ClassNotFoundException e) {
                throw new IOException("ClassNotFoundException processing BDS state: " + e.getMessage());
            }
        } else {
            throw new RuntimeException("algorithm identifier in private key not recognised");
        }
    }

    private static short[] convert(byte[] octets) {
        short[] rv = new short[octets.length / 2];

        for (int i = 0; i != rv.length; i++) {
            rv[i] = Pack.littleEndianToShort(octets, i * 2);
        }

        return rv;
    }
}