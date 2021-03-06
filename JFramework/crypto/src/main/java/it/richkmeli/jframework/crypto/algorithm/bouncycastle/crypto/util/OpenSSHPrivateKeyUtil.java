package it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.util;

import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.*;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.pkcs.RSAPrivateKey;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.sec.ECPrivateKey;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.x9.ECNamedCurveTable;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.x9.X9ECParameters;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.CryptoServicesRegistrar;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.params.*;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.util.Arrays;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.util.BigIntegers;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.util.Strings;

import java.io.IOException;
import java.math.BigInteger;


/**
 * A collection of utility methods for parsing OpenSSH private keys.
 */
public class OpenSSHPrivateKeyUtil {
    private OpenSSHPrivateKeyUtil() {

    }

    /**
     * Magic value for proprietary OpenSSH private key.
     **/
    static final byte[] AUTH_MAGIC = Strings.toByteArray("openssh-key-v1\0"); // C string so null terminated

    /**
     * Encode a cipher parameters into an OpenSSH private key.
     * This does not add headers like ----BEGIN RSA PRIVATE KEY----
     *
     * @param params the cipher parameters.
     * @return a byte array
     */
    public static byte[] encodePrivateKey(AsymmetricKeyParameter params)
            throws IOException {
        if (params == null) {
            throw new IllegalArgumentException("param is null");
        }

        if (params instanceof RSAPrivateCrtKeyParameters) {
            PrivateKeyInfo pInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(params);

            return pInfo.parsePrivateKey().toASN1Primitive().getEncoded();
        } else if (params instanceof ECPrivateKeyParameters) {
            PrivateKeyInfo pInfo = PrivateKeyInfoFactory.createPrivateKeyInfo(params);

            return pInfo.parsePrivateKey().toASN1Primitive().getEncoded();
        } else if (params instanceof DSAPrivateKeyParameters) {
            DSAPrivateKeyParameters dsaPrivKey = (DSAPrivateKeyParameters) params;
            DSAParameters dsaParams = dsaPrivKey.getParameters();

            ASN1EncodableVector vec = new ASN1EncodableVector();
            vec.add(new ASN1Integer(0));
            vec.add(new ASN1Integer(dsaParams.getP()));
            vec.add(new ASN1Integer(dsaParams.getQ()));
            vec.add(new ASN1Integer(dsaParams.getG()));

            // public key = g.modPow(x, p);
            BigInteger pubKey = dsaParams.getG().modPow(dsaPrivKey.getX(), dsaParams.getP());
            vec.add(new ASN1Integer(pubKey));

            vec.add(new ASN1Integer(dsaPrivKey.getX()));
            try {
                return new DERSequence(vec).getEncoded();
            } catch (Exception ex) {
                throw new IllegalStateException("unable to encode DSAPrivateKeyParameters " + ex.getMessage());
            }
        } else if (params instanceof Ed25519PrivateKeyParameters) {
            Ed25519PublicKeyParameters publicKeyParameters = ((Ed25519PrivateKeyParameters) params).generatePublicKey();

            SSHBuilder builder = new SSHBuilder();
            builder.writeBytes(AUTH_MAGIC);
            builder.writeString("none");    // cipher name
            builder.writeString("none");    // KDF name
            builder.writeString("");        // KDF options

            builder.u32(1); // Number of keys

            {
                byte[] pkEncoded = OpenSSHPublicKeyUtil.encodePublicKey(publicKeyParameters);
                builder.writeBlock(pkEncoded);
            }

            {
                SSHBuilder pkBuild = new SSHBuilder();

                int checkint = CryptoServicesRegistrar.getSecureRandom().nextInt();
                pkBuild.u32(checkint);
                pkBuild.u32(checkint);

                pkBuild.writeString("ssh-ed25519");

                // Public key (as part of private key pair)
                byte[] pubKeyEncoded = publicKeyParameters.getEncoded();
                pkBuild.writeBlock(pubKeyEncoded);

                // The private key in SSH is 64 bytes long and is the concatenation of the private and the public keys
                pkBuild.writeBlock(Arrays.concatenate(((Ed25519PrivateKeyParameters) params).getEncoded(), pubKeyEncoded));

                pkBuild.writeString("");    // Comment for this private key (empty)

                builder.writeBlock(pkBuild.getPaddedBytes());
            }

            return builder.getBytes();
        }

        throw new IllegalArgumentException("unable to convert " + params.getClass().getName() + " to openssh private key");

    }

    /**
     * Parse a private key.
     * <p>
     * This method accepts the body of the OpenSSH private key.
     * The easiest way to extract the body is to use PemReader, for example:
     * <p>
     * byte[] blob = new PemReader([reader]).readPemObject().getContent();
     * CipherParameters params = parsePrivateKeyBlob(blob);
     *
     * @param blob The key.
     * @return A cipher parameters instance.
     */
    public static AsymmetricKeyParameter parsePrivateKeyBlob(byte[] blob) {
        AsymmetricKeyParameter result = null;

        if (blob[0] == 0x30) {
            ASN1Sequence sequence = ASN1Sequence.getInstance(blob);

            if (sequence.size() == 6) {
                if (allIntegers(sequence) && ((ASN1Integer) sequence.getObjectAt(0)).getPositiveValue().equals(BigIntegers.ZERO)) {
                    // length of 6 and all Integers -- DSA
                    result = new DSAPrivateKeyParameters(
                            ((ASN1Integer) sequence.getObjectAt(5)).getPositiveValue(),
                            new DSAParameters(
                                    ((ASN1Integer) sequence.getObjectAt(1)).getPositiveValue(),
                                    ((ASN1Integer) sequence.getObjectAt(2)).getPositiveValue(),
                                    ((ASN1Integer) sequence.getObjectAt(3)).getPositiveValue())
                    );
                }
            } else if (sequence.size() == 9) {
                if (allIntegers(sequence) && ((ASN1Integer) sequence.getObjectAt(0)).getPositiveValue().equals(BigIntegers.ZERO)) {
                    // length of 8 and all Integers -- RSA
                    RSAPrivateKey rsaPrivateKey = RSAPrivateKey.getInstance(sequence);

                    result = new RSAPrivateCrtKeyParameters(
                            rsaPrivateKey.getModulus(),
                            rsaPrivateKey.getPublicExponent(),
                            rsaPrivateKey.getPrivateExponent(),
                            rsaPrivateKey.getPrime1(),
                            rsaPrivateKey.getPrime2(),
                            rsaPrivateKey.getExponent1(),
                            rsaPrivateKey.getExponent2(),
                            rsaPrivateKey.getCoefficient());
                }
            } else if (sequence.size() == 4) {
                if (sequence.getObjectAt(3) instanceof ASN1TaggedObject
                        && sequence.getObjectAt(2) instanceof ASN1TaggedObject) {
                    ECPrivateKey ecPrivateKey = ECPrivateKey.getInstance(sequence);
                    ASN1ObjectIdentifier curveOID = (ASN1ObjectIdentifier) ecPrivateKey.getParameters();
                    X9ECParameters x9Params = ECNamedCurveTable.getByOID(curveOID);
                    result = new ECPrivateKeyParameters(
                            ecPrivateKey.getKey(),
                            new ECNamedDomainParameters(
                                    curveOID,
                                    x9Params.getCurve(),
                                    x9Params.getG(),
                                    x9Params.getN(),
                                    x9Params.getH(),
                                    x9Params.getSeed()));
                }
            }
        } else {
            SSHBuffer kIn = new SSHBuffer(AUTH_MAGIC, blob);

            String cipherName = kIn.readString();
            if (!"none".equals(cipherName)) {
                throw new IllegalStateException("encrypted keys not supported");
            }

            // KDF name
            kIn.skipBlock();

            // KDF options
            kIn.skipBlock();

            int publicKeyCount = kIn.readU32();
            if (publicKeyCount != 1) {
                throw new IllegalStateException("multiple keys not supported");
            }

            // Burn off public key.
            OpenSSHPublicKeyUtil.parsePublicKey(kIn.readBlock());

            byte[] privateKeyBlock = kIn.readPaddedBlock();

            if (kIn.hasRemaining()) {
                throw new IllegalArgumentException("decoded key has trailing data");
            }

            SSHBuffer pkIn = new SSHBuffer(privateKeyBlock);
            int check1 = pkIn.readU32();
            int check2 = pkIn.readU32();

            if (check1 != check2) {
                throw new IllegalStateException("private key check values are not the same");
            }

            String keyType = pkIn.readString();
            if (!"ssh-ed25519".equals(keyType)) {
                throw new IllegalStateException("can not parse private key of type " + keyType);
            }

            // Skip public key
            pkIn.skipBlock();

            byte[] edPrivateKey = pkIn.readBlock();
            if (edPrivateKey.length != Ed25519PrivateKeyParameters.KEY_SIZE + Ed25519PublicKeyParameters.KEY_SIZE) {
                throw new IllegalStateException("private key value of wrong length");
            }

            result = new Ed25519PrivateKeyParameters(edPrivateKey, 0);

            // Comment for private key
            pkIn.skipBlock();

            if (pkIn.hasRemaining()) {
                throw new IllegalArgumentException("private key block has trailing data");
            }
        }

        if (result == null) {
            throw new IllegalArgumentException("unable to parse key");
        }

        return result;
    }

    /**
     * allIntegers returns true if the sequence holds only ASN1Integer types.
     **/
    private static boolean allIntegers(ASN1Sequence sequence) {
        for (int t = 0; t < sequence.size(); t++) {
            if (!(sequence.getObjectAt(t) instanceof ASN1Integer)) {
                return false;
            }
        }
        return true;
    }
}
