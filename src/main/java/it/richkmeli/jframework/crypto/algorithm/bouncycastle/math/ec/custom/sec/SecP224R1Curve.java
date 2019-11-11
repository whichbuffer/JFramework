package it.richkmeli.jframework.crypto.algorithm.bouncycastle.math.ec.custom.sec;

import it.richkmeli.jframework.crypto.algorithm.bouncycastle.math.ec.*;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.math.raw.Nat224;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;

public class SecP224R1Curve extends ECCurve.AbstractFp {
    public static final BigInteger q = SecP224R1FieldElement.Q;

    private static final int SECP224R1_DEFAULT_COORDS = COORD_JACOBIAN;
    private static final ECFieldElement[] SECP224R1_AFFINE_ZS = new ECFieldElement[]{new SecP224R1FieldElement(ECConstants.ONE)};

    protected SecP224R1Point infinity;

    public SecP224R1Curve() {
        super(q);

        this.infinity = new SecP224R1Point(this, null, null);

        this.a = fromBigInteger(new BigInteger(1,
                Hex.decodeStrict("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFFFFFFFFFFFFFFFFFFFE")));
        this.b = fromBigInteger(new BigInteger(1,
                Hex.decodeStrict("B4050A850C04B3ABF54132565044B0B7D7BFD8BA270B39432355FFB4")));
        this.order = new BigInteger(1, Hex.decodeStrict("FFFFFFFFFFFFFFFFFFFFFFFFFFFF16A2E0B8F03E13DD29455C5C2A3D"));
        this.cofactor = BigInteger.valueOf(1);

        this.coord = SECP224R1_DEFAULT_COORDS;
    }

    protected ECCurve cloneCurve() {
        return new SecP224R1Curve();
    }

    public boolean supportsCoordinateSystem(int coord) {
        switch (coord) {
            case COORD_JACOBIAN:
                return true;
            default:
                return false;
        }
    }

    public BigInteger getQ() {
        return q;
    }

    public int getFieldSize() {
        return q.bitLength();
    }

    public ECFieldElement fromBigInteger(BigInteger x) {
        return new SecP224R1FieldElement(x);
    }

    protected ECPoint createRawPoint(ECFieldElement x, ECFieldElement y) {
        return new SecP224R1Point(this, x, y);
    }

    protected ECPoint createRawPoint(ECFieldElement x, ECFieldElement y, ECFieldElement[] zs) {
        return new SecP224R1Point(this, x, y, zs);
    }

    public ECPoint getInfinity() {
        return infinity;
    }

    public ECLookupTable createCacheSafeLookupTable(ECPoint[] points, int off, final int len) {
        final int FE_INTS = 7;

        final int[] table = new int[len * FE_INTS * 2];
        {
            int pos = 0;
            for (int i = 0; i < len; ++i) {
                ECPoint p = points[off + i];
                Nat224.copy(((SecP224R1FieldElement) p.getRawXCoord()).x, 0, table, pos);
                pos += FE_INTS;
                Nat224.copy(((SecP224R1FieldElement) p.getRawYCoord()).x, 0, table, pos);
                pos += FE_INTS;
            }
        }

        return new AbstractECLookupTable() {
            public int getSize() {
                return len;
            }

            public ECPoint lookup(int index) {
                int[] x = Nat224.create(), y = Nat224.create();
                int pos = 0;

                for (int i = 0; i < len; ++i) {
                    int MASK = ((i ^ index) - 1) >> 31;

                    for (int j = 0; j < FE_INTS; ++j) {
                        x[j] ^= table[pos + j] & MASK;
                        y[j] ^= table[pos + FE_INTS + j] & MASK;
                    }

                    pos += (FE_INTS * 2);
                }

                return createPoint(x, y);
            }

            public ECPoint lookupVar(int index) {
                int[] x = Nat224.create(), y = Nat224.create();
                int pos = index * FE_INTS * 2;

                for (int j = 0; j < FE_INTS; ++j) {
                    x[j] = table[pos + j];
                    y[j] = table[pos + FE_INTS + j];
                }

                return createPoint(x, y);
            }

            private ECPoint createPoint(int[] x, int[] y) {
                return createRawPoint(new SecP224R1FieldElement(x), new SecP224R1FieldElement(y), SECP224R1_AFFINE_ZS);
            }
        };
    }
}
