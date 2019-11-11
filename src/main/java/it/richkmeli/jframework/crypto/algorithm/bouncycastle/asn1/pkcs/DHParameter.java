package it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.pkcs;

import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.*;

import java.math.BigInteger;
import java.util.Enumeration;

public class DHParameter
        extends ASN1Object {
    ASN1Integer p, g, l;

    public DHParameter(
            BigInteger p,
            BigInteger g,
            int l) {
        this.p = new ASN1Integer(p);
        this.g = new ASN1Integer(g);

        if (l != 0) {
            this.l = new ASN1Integer(l);
        } else {
            this.l = null;
        }
    }

    public static DHParameter getInstance(
            Object obj) {
        if (obj instanceof DHParameter) {
            return (DHParameter) obj;
        }

        if (obj != null) {
            return new DHParameter(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    private DHParameter(
            ASN1Sequence seq) {
        Enumeration e = seq.getObjects();

        p = ASN1Integer.getInstance(e.nextElement());
        g = ASN1Integer.getInstance(e.nextElement());

        if (e.hasMoreElements()) {
            l = (ASN1Integer) e.nextElement();
        } else {
            l = null;
        }
    }

    public BigInteger getP() {
        return p.getPositiveValue();
    }

    public BigInteger getG() {
        return g.getPositiveValue();
    }

    public BigInteger getL() {
        if (l == null) {
            return null;
        }

        return l.getPositiveValue();
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector(3);

        v.add(p);
        v.add(g);

        if (this.getL() != null) {
            v.add(l);
        }

        return new DERSequence(v);
    }
}
