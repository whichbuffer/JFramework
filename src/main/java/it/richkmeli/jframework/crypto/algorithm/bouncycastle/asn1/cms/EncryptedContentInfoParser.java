package it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.cms;

import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.ASN1Encodable;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.ASN1ObjectIdentifier;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.ASN1SequenceParser;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.ASN1TaggedObjectParser;
import it.richkmeli.jframework.crypto.algorithm.bouncycastle.asn1.x509.AlgorithmIdentifier;

import java.io.IOException;

/**
 * Parser for <a href="http://tools.ietf.org/html/rfc5652#section-6.1">RFC 5652</a> EncryptedContentInfo object.
 * <p>
 * <pre>
 * EncryptedContentInfo ::= SEQUENCE {
 *     contentType ContentType,
 *     contentEncryptionAlgorithm ContentEncryptionAlgorithmIdentifier,
 *     encryptedContent [0] IMPLICIT EncryptedContent OPTIONAL
 * }
 * </pre>
 */
public class EncryptedContentInfoParser {
    private ASN1ObjectIdentifier _contentType;
    private AlgorithmIdentifier _contentEncryptionAlgorithm;
    private ASN1TaggedObjectParser _encryptedContent;

    public EncryptedContentInfoParser(
            ASN1SequenceParser seq)
            throws IOException {
        _contentType = (ASN1ObjectIdentifier) seq.readObject();
        _contentEncryptionAlgorithm = AlgorithmIdentifier.getInstance(seq.readObject().toASN1Primitive());
        _encryptedContent = (ASN1TaggedObjectParser) seq.readObject();
    }

    public ASN1ObjectIdentifier getContentType() {
        return _contentType;
    }

    public AlgorithmIdentifier getContentEncryptionAlgorithm() {
        return _contentEncryptionAlgorithm;
    }

    public ASN1Encodable getEncryptedContent(
            int tag)
            throws IOException {
        return _encryptedContent.getObjectParser(tag, false);
    }
}