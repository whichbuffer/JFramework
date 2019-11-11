package it.richkmeli.jframework.crypto.algorithm.bouncycastle.math.ec.custom.gm;

import it.richkmeli.jframework.crypto.algorithm.bouncycastle.math.raw.Nat;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.math.raw.Nat256;

import java.math.BigInteger;

public class SM2P256V1Field {
    private static final long M = 0xFFFFFFFFL;

    // 2^256 - 2^224 - 2^96 + 2^64 - 1
    static final int[] P = new int[]{0xFFFFFFFF, 0xFFFFFFFF, 0x00000000, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF,
            0xFFFFFFFF, 0xFFFFFFFE};
    static final int[] PExt = new int[]{00000001, 0x00000000, 0xFFFFFFFE, 0x00000001, 0x00000001,
            0xFFFFFFFE, 0x00000000, 0x00000002, 0xFFFFFFFE, 0xFFFFFFFD, 0x00000003, 0xFFFFFFFE, 0xFFFFFFFF, 0xFFFFFFFF,
            0x00000000, 0xFFFFFFFE};
    private static final int P7s1 = 0xFFFFFFFE >>> 1;
    private static final int PExt15s1 = 0xFFFFFFFE >>> 1;

    public static void add(int[] x, int[] y, int[] z) {
        int c = Nat256.add(x, y, z);
        if (c != 0 || ((z[7] >>> 1) >= P7s1 && Nat256.gte(z, P))) {
            addPInvTo(z);
        }
    }

    public static void addExt(int[] xx, int[] yy, int[] zz) {
        int c = Nat.add(16, xx, yy, zz);
        if (c != 0 || ((zz[15] >>> 1) >= PExt15s1 && Nat.gte(16, zz, PExt))) {
            Nat.subFrom(16, PExt, zz);
        }
    }

    public static void addOne(int[] x, int[] z) {
        int c = Nat.inc(8, x, z);
        if (c != 0 || ((z[7] >>> 1) >= P7s1 && Nat256.gte(z, P))) {
            addPInvTo(z);
        }
    }

    public static int[] fromBigInteger(BigInteger x) {
        int[] z = Nat256.fromBigInteger(x);
        if ((z[7] >>> 1) >= P7s1 && Nat256.gte(z, P)) {
            Nat256.subFrom(P, z);
        }
        return z;
    }

    public static void half(int[] x, int[] z) {
        if ((x[0] & 1) == 0) {
            Nat.shiftDownBit(8, x, 0, z);
        } else {
            int c = Nat256.add(x, P, z);
            Nat.shiftDownBit(8, z, c);
        }
    }

    public static void multiply(int[] x, int[] y, int[] z) {
        int[] tt = Nat256.createExt();
        Nat256.mul(x, y, tt);
        reduce(tt, z);
    }

    public static void multiplyAddToExt(int[] x, int[] y, int[] zz) {
        int c = Nat256.mulAddTo(x, y, zz);
        if (c != 0 || ((zz[15] >>> 1) >= PExt15s1 && Nat.gte(16, zz, PExt))) {
            Nat.subFrom(16, PExt, zz);
        }
    }

    public static void negate(int[] x, int[] z) {
        if (Nat256.isZero(x)) {
            Nat256.zero(z);
        } else {
            Nat256.sub(P, x, z);
        }
    }

    public static void reduce(int[] xx, int[] z) {
        long xx08 = xx[8] & M, xx09 = xx[9] & M, xx10 = xx[10] & M, xx11 = xx[11] & M;
        long xx12 = xx[12] & M, xx13 = xx[13] & M, xx14 = xx[14] & M, xx15 = xx[15] & M;

        long t0 = xx08 + xx09;
        long t1 = xx10 + xx11;
        long t2 = xx12 + xx15;
        long t3 = xx13 + xx14;
        long t4 = t3 + (xx15 << 1);

        long ts = t0 + t3;
        long tt = t1 + t2 + ts;

        long cc = 0;
        cc += (xx[0] & M) + tt + xx13 + xx14 + xx15;
        z[0] = (int) cc;
        cc >>= 32;
        cc += (xx[1] & M) + tt - xx08 + xx14 + xx15;
        z[1] = (int) cc;
        cc >>= 32;
        cc += (xx[2] & M) - ts;
        z[2] = (int) cc;
        cc >>= 32;
        cc += (xx[3] & M) + tt - xx09 - xx10 + xx13;
        z[3] = (int) cc;
        cc >>= 32;
        cc += (xx[4] & M) + tt - t1 - xx08 + xx14;
        z[4] = (int) cc;
        cc >>= 32;
        cc += (xx[5] & M) + t4 + xx10;
        z[5] = (int) cc;
        cc >>= 32;
        cc += (xx[6] & M) + xx11 + xx14 + xx15;
        z[6] = (int) cc;
        cc >>= 32;
        cc += (xx[7] & M) + tt + t4 + xx12;
        z[7] = (int) cc;
        cc >>= 32;

//        assert cc >= 0;

        reduce32((int) cc, z);
    }

    public static void reduce32(int x, int[] z) {
        long cc = 0;

        if (x != 0) {
            long xx08 = x & M;

            cc += (z[0] & M) + xx08;
            z[0] = (int) cc;
            cc >>= 32;
            if (cc != 0) {
                cc += (z[1] & M);
                z[1] = (int) cc;
                cc >>= 32;
            }
            cc += (z[2] & M) - xx08;
            z[2] = (int) cc;
            cc >>= 32;
            cc += (z[3] & M) + xx08;
            z[3] = (int) cc;
            cc >>= 32;
            if (cc != 0) {
                cc += (z[4] & M);
                z[4] = (int) cc;
                cc >>= 32;
                cc += (z[5] & M);
                z[5] = (int) cc;
                cc >>= 32;
                cc += (z[6] & M);
                z[6] = (int) cc;
                cc >>= 32;
            }
            cc += (z[7] & M) + xx08;
            z[7] = (int) cc;
            cc >>= 32;

//          assert cc == 0 || cc == 1;
        }

        if (cc != 0 || ((z[7] >>> 1) >= P7s1 && Nat256.gte(z, P))) {
            addPInvTo(z);
        }
    }

    public static void square(int[] x, int[] z) {
        int[] tt = Nat256.createExt();
        Nat256.square(x, tt);
        reduce(tt, z);
    }

    public static void squareN(int[] x, int n, int[] z) {
//        assert n > 0;

        int[] tt = Nat256.createExt();
        Nat256.square(x, tt);
        reduce(tt, z);

        while (--n > 0) {
            Nat256.square(z, tt);
            reduce(tt, z);
        }
    }

    public static void subtract(int[] x, int[] y, int[] z) {
        int c = Nat256.sub(x, y, z);
        if (c != 0) {
            subPInvFrom(z);
        }
    }

    public static void subtractExt(int[] xx, int[] yy, int[] zz) {
        int c = Nat.sub(16, xx, yy, zz);
        if (c != 0) {
            Nat.addTo(16, PExt, zz);
        }
    }

    public static void twice(int[] x, int[] z) {
        int c = Nat.shiftUpBit(8, x, 0, z);
        if (c != 0 || ((z[7] >>> 1) >= P7s1 && Nat256.gte(z, P))) {
            addPInvTo(z);
        }
    }

    private static void addPInvTo(int[] z) {
        long c = (z[0] & M) + 1;
        z[0] = (int) c;
        c >>= 32;
        if (c != 0) {
            c += (z[1] & M);
            z[1] = (int) c;
            c >>= 32;
        }
        c += (z[2] & M) - 1;
        z[2] = (int) c;
        c >>= 32;
        c += (z[3] & M) + 1;
        z[3] = (int) c;
        c >>= 32;
        if (c != 0) {
            c += (z[4] & M);
            z[4] = (int) c;
            c >>= 32;
            c += (z[5] & M);
            z[5] = (int) c;
            c >>= 32;
            c += (z[6] & M);
            z[6] = (int) c;
            c >>= 32;
        }
        c += (z[7] & M) + 1;
        z[7] = (int) c;
//        c >>= 32;
    }

    private static void subPInvFrom(int[] z) {
        long c = (z[0] & M) - 1;
        z[0] = (int) c;
        c >>= 32;
        if (c != 0) {
            c += (z[1] & M);
            z[1] = (int) c;
            c >>= 32;
        }
        c += (z[2] & M) + 1;
        z[2] = (int) c;
        c >>= 32;
        c += (z[3] & M) - 1;
        z[3] = (int) c;
        c >>= 32;
        if (c != 0) {
            c += (z[4] & M);
            z[4] = (int) c;
            c >>= 32;
            c += (z[5] & M);
            z[5] = (int) c;
            c >>= 32;
            c += (z[6] & M);
            z[6] = (int) c;
            c >>= 32;
        }
        c += (z[7] & M) - 1;
        z[7] = (int) c;
//        c >>= 32;
    }
}
