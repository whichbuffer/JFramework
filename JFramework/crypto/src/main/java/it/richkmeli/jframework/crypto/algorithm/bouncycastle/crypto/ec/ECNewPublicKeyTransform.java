package it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.ec;

import it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.CipherParameters;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.CryptoServicesRegistrar;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.params.ECDomainParameters;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.params.ECPublicKeyParameters;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.params.ParametersWithRandom;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.math.ec.ECAlgorithms;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.math.ec.ECMultiplier;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.math.ec.ECPoint;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.math.ec.FixedPointCombMultiplier;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * this does your basic Elgamal encryption algorithm using EC
 */
public class ECNewPublicKeyTransform
        implements ECPairTransform {
    private ECPublicKeyParameters key;
    private SecureRandom random;

    /**
     * initialise the EC Elgamal engine.
     *
     * @param param the necessary EC key parameters.
     */
    public void init(
            CipherParameters param) {
        if (param instanceof ParametersWithRandom) {
            ParametersWithRandom p = (ParametersWithRandom) param;

            if (!(p.getParameters() instanceof ECPublicKeyParameters)) {
                throw new IllegalArgumentException("ECPublicKeyParameters are required for new public key transform.");
            }
            this.key = (ECPublicKeyParameters) p.getParameters();
            this.random = p.getRandom();
        } else {
            if (!(param instanceof ECPublicKeyParameters)) {
                throw new IllegalArgumentException("ECPublicKeyParameters are required for new public key transform.");
            }

            this.key = (ECPublicKeyParameters) param;
            this.random = CryptoServicesRegistrar.getSecureRandom();
        }
    }

    /**
     * Transform an existing cipher text pair using the ElGamal algorithm. Note: the input cipherText will
     * need to be preserved in order to complete the transformation to the new public key.
     *
     * @param cipherText the EC point to process.
     * @return returns a new ECPair representing the result of the process.
     */
    public ECPair transform(ECPair cipherText) {
        if (key == null) {
            throw new IllegalStateException("ECNewPublicKeyTransform not initialised");
        }

        ECDomainParameters ec = key.getParameters();
        BigInteger n = ec.getN();

        ECMultiplier basePointMultiplier = createBasePointMultiplier();
        BigInteger k = ECUtil.generateK(n, random);

        ECPoint[] gamma_phi = new ECPoint[]{
                basePointMultiplier.multiply(ec.getG(), k),
                key.getQ().multiply(k).add(ECAlgorithms.cleanPoint(ec.getCurve(), cipherText.getY()))
        };

        ec.getCurve().normalizeAll(gamma_phi);

        return new ECPair(gamma_phi[0], gamma_phi[1]);
    }

    protected ECMultiplier createBasePointMultiplier() {
        return new FixedPointCombMultiplier();
    }
}
