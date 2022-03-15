package com.demo03;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

@Log4j2
public class Utils {

    public static String rfc = "XOJI740919U48";
//    public static String rfc = "RAQÑ7701212M3";
    static char[] pwdPFX = "".toCharArray();
//    static String filePath = "/Users/brauliocasanas/Labs/demo03/src/main/resources/FIEL_CACX7605101P8_20190528152826/CACX7605101P8.pfx";
    static String filePath = "/Users/brauliocasanas/Labs/demo03/src/main/resources/FIEL_XOJI740919U48_20190528162708/XOJI740919U48.pfx";
//    static String filePath = "/Users/brauliocasanas/Labs/demo03/src/main/resources/FIEL_RAQÑ7701212M3_20190614165739/RAQÑ7701212M3.pfx";
    static File filePFX = new File(filePath);

    /**
     * Get a certificate through a pfx file
     *
     * @return
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     */
    public static X509Certificate getCertificate()
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(filePFX), pwdPFX);
        String alias = ks.aliases().nextElement();

        return (X509Certificate) ks.getCertificate(alias);
    }

    /**
     * Get a private key through a pfx file
     *
     * @return
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws UnrecoverableKeyException
     */
    public static PrivateKey getPrivateKey()
            throws KeyStoreException,
            IOException,
            CertificateException,
            NoSuchAlgorithmException,
            UnrecoverableKeyException {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(filePFX), pwdPFX);
        String alias = ks.aliases().nextElement();

        return (PrivateKey) ks.getKey(alias, pwdPFX);
    }

    public static String createDigest(String canonicalTimestamp) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(canonicalTimestamp.getBytes());
        } catch (NoSuchAlgorithmException e) {
            log.throwing(e);
        }
        return Base64.getEncoder().encodeToString(digest.digest());
    }



    /**
     * Sign SHA1 with private key and a String and returning a Base64 String
     *
     * @param sourceData
     * @param privateKey
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static String sign(String sourceData, PrivateKey privateKey) throws
            NoSuchAlgorithmException,
            InvalidKeyException,
            SignatureException {
        Signature sig = Signature.getInstance("SHA1WithRSA");
        sig.initSign(privateKey);
        sig.update(sourceData.getBytes());

        return Base64.getEncoder().encodeToString(sig.sign());
    }
}
