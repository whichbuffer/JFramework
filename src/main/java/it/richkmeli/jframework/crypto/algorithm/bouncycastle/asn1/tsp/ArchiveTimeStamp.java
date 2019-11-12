package it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.tsp;

import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.*;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.cms.Attributes;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.cms.CMSObjectIdentifiers;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.cms.ContentInfo;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.cms.SignedData;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * Implementation of the Archive Timestamp type defined in RFC4998.
 * {@see <a href="https://tools.ietf.org/html/rfc4998">RFC 4998</a>}
 * <p>
 * ASN.1 Archive Timestamp
 * <p>
 * ArchiveTimeStamp ::= SEQUENCE {
 * digestAlgorithm [Ø] AlgorithmIdentifier OPTIONAL,
 * attributes      [1] Attributes OPTIONAL,
 * reducedHashtree [2] SEQUENCE OF PartialHashtree OPTIONAL,
 * timeStamp       ContentInfo}
 * <p>
 * PartialHashtree ::= SEQUENCE OF OCTET STRING
 * <p>
 * Attributes ::= SET SIZE (1..MAX) OF Attribute
 */
public class ArchiveTimeStamp
        extends ASN1Object {
    private AlgorithmIdentifier digestAlgorithm;
    private Attributes attributes;
    private ASN1Sequence reducedHashTree;
    private ContentInfo timeStamp;

    /**
     * Return an ArchiveTimestamp from the given object.
     *
     * @param obj the object we want converted.
     * @return an ArchiveTimestamp instance, or null.
     * @throws IllegalArgumentException if the object cannot be converted.
     */
    public static ArchiveTimeStamp getInstance(final Object obj) {
        if (obj instanceof ArchiveTimeStamp) {
            return (ArchiveTimeStamp) obj;
        } else if (obj != null) {
            return new ArchiveTimeStamp(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    public ArchiveTimeStamp(
            AlgorithmIdentifier digestAlgorithm,
            PartialHashtree[] reducedHashTree,
            ContentInfo timeStamp) {
        this.digestAlgorithm = digestAlgorithm;
        this.reducedHashTree = new DERSequence(reducedHashTree);
        this.timeStamp = timeStamp;
    }

    public ArchiveTimeStamp(
            AlgorithmIdentifier digestAlgorithm,
            Attributes attributes,
            PartialHashtree[] reducedHashTree,
            ContentInfo timeStamp) {
        this.digestAlgorithm = digestAlgorithm;
        this.attributes = attributes;
        this.reducedHashTree = new DERSequence(reducedHashTree);
        this.timeStamp = timeStamp;
    }

    public ArchiveTimeStamp(
            ContentInfo timeStamp) {
        this.timeStamp = timeStamp;
    }

    private ArchiveTimeStamp(final ASN1Sequence sequence) {
        if (sequence.size() < 1 || sequence.size() > 4) {
            throw new IllegalArgumentException("wrong sequence size in constructor: " + sequence.size());
        }

        for (int i = 0; i < sequence.size() - 1; i++) {
            Object obj = sequence.getObjectAt(i);

            if (obj instanceof ASN1TaggedObject) {
                ASN1TaggedObject taggedObject = ASN1TaggedObject.getInstance(obj);

                switch (taggedObject.getTagNo()) {
                    case 0:
                        digestAlgorithm = AlgorithmIdentifier.getInstance(taggedObject, false);
                        break;
                    case 1:
                        attributes = Attributes.getInstance(taggedObject, false);
                        break;
                    case 2:
                        reducedHashTree = ASN1Sequence.getInstance(taggedObject, false);
                        break;
                    default:
                        throw new IllegalArgumentException("invalid tag no in constructor: "
                                + taggedObject.getTagNo());
                }
            }
        }

        timeStamp = ContentInfo.getInstance(sequence.getObjectAt(sequence.size() - 1));
    }

    public AlgorithmIdentifier getDigestAlgorithmIdentifier() {
        if (digestAlgorithm != null) {
            return digestAlgorithm;
        } else {
            if (timeStamp.getContentType().equals(CMSObjectIdentifiers.signedData)) {
                SignedData tsData = SignedData.getInstance(timeStamp.getContent());
                if (tsData.getEncapContentInfo().getContentType().equals(PKCSObjectIdentifiers.id_ct_TSTInfo)) {
                    TSTInfo tstData = TSTInfo.getInstance(tsData.getEncapContentInfo());

                    return tstData.getMessageImprint().getHashAlgorithm();
                } else {
                    throw new IllegalStateException("cannot parse time stamp");
                }
            } else {
                throw new IllegalStateException("cannot identify algorithm identifier for digest");
            }
        }
    }

    /**
     * Return the contents of the digestAlgorithm field - null if not set.
     *
     * @return the contents of the digestAlgorithm field, or null if not set.
     */
    public AlgorithmIdentifier getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public PartialHashtree[] getReducedHashTree() {
        if (reducedHashTree == null) {
            return null;
        }

        PartialHashtree[] rv = new PartialHashtree[reducedHashTree.size()];

        for (int i = 0; i != rv.length; i++) {
            rv[i] = PartialHashtree.getInstance(reducedHashTree.getObjectAt(i));
        }

        return rv;
    }

    public ContentInfo getTimeStamp() {
        return timeStamp;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector v = new ASN1EncodableVector(4);

        if (digestAlgorithm != null) {
            v.add(new DERTaggedObject(false, 0, digestAlgorithm));
        }

        if (attributes != null) {
            v.add(new DERTaggedObject(false, 1, attributes));
        }

        if (reducedHashTree != null) {
            v.add(new DERTaggedObject(false, 2, reducedHashTree));
        }

        v.add(timeStamp);

        return new DERSequence(v);
    }
}