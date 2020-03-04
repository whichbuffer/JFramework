package it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.params;

import it.richkmeli.jframework.crypto.algorithm.bouncycastle.crypto.KeyGenerationParameters;

import java.security.SecureRandom;

public class X448KeyGenerationParameters
        extends KeyGenerationParameters {
    public X448KeyGenerationParameters(SecureRandom random) {
        super(random, 448);
    }
}