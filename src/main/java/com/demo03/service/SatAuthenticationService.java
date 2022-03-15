package com.demo03.service;

import com.demo03.Utils;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import org.w3c.dom.Document;

import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

@Log4j2
@Service
public class SatAuthenticationService {

    private final WebServiceTemplate webServiceTemplate;

    @Autowired
    public SatAuthenticationService(@Qualifier("webServiceTemplateAuthentication") WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    public String authentication() {

        HttpComponentsMessageSender httpComponentsMessageSender = new HttpComponentsMessageSender();

        HttpClientBuilder httpClientBuilder = null;
        try {
            httpClientBuilder = HttpClients.custom()
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }
        httpClientBuilder.addInterceptorFirst(
                (HttpRequestInterceptor) (request, context) -> request.removeHeaders(HTTP.CONTENT_LEN));

        httpComponentsMessageSender.setHttpClient(httpClientBuilder.build());
        webServiceTemplate.setMessageSender(httpComponentsMessageSender);
        return webServiceTemplate.sendAndReceive(message -> {

            try {

                SaajSoapMessage saajSoapMessage = (SaajSoapMessage) message;
                SOAPMessage soapMessage = saajSoapMessage.getSaajMessage();
                SOAPPart soapPart = soapMessage.getSOAPPart();
                soapPart.setContent(new StreamSource(new StringReader(generate(Utils.getCertificate(), Utils.getPrivateKey()))));
                ((SoapMessage) message).setSoapAction("http://DescargaMasivaTerceros.gob.mx/IAutenticacion/Autentica");
            } catch (Exception e) {
                log.throwing(e);
            }
        }, message -> {
            Document doc = ((SaajSoapMessage) message).getDocument();
            return doc.getElementsByTagName("AutenticaResult").item(0).getTextContent();
        });

    }

    /**
     * Generate XML to send through SAT's web service
     *
     * @param certificate
     * @param privateKey
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws InvalidKeyException
     * @throws CertificateEncodingException
     */
    public String generate(X509Certificate certificate, PrivateKey privateKey)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, CertificateEncodingException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Calendar calendarNow = Calendar.getInstance();

        String created = simpleDateFormat.format(calendarNow.getTime());
        calendarNow.add(Calendar.SECOND, 300); // Add 300 seconds which equals 5 minutes
        String expires = simpleDateFormat.format(calendarNow.getTime());
        String uuid = "uuid-" + UUID.randomUUID() + "-1";

        String canonicalTimestamp = "<u:Timestamp xmlns:u=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" u:Id=\"_0\">" +
                "<u:Created>" + created + "</u:Created>" +
                "<u:Expires>" + expires + "</u:Expires>" +
                "</u:Timestamp>";

        String digest = Utils.createDigest(canonicalTimestamp);

        String canonicalSignedInfo = "<SignedInfo xmlns=\"http://www.w3.org/2000/09/xmldsig#\">" +
                "<CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"></CanonicalizationMethod>" +
                "<SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"></SignatureMethod>" +
                "<Reference URI=\"#_0\">" +
                "<Transforms>" +
                "<Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"></Transform>" +
                "</Transforms>" +
                "<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"></DigestMethod>" +
                "<DigestValue>" + digest + "</DigestValue>" +
                "</Reference>" +
                "</SignedInfo>";

        String signature = Utils.sign(canonicalSignedInfo, privateKey);

        return "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:u=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">" +
                "<s:Header>" +
                "<o:Security s:mustUnderstand=\"1\" xmlns:o=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">" +
                "<u:Timestamp u:Id=\"_0\">" +
                "<u:Created>" + created + "</u:Created>" +
                "<u:Expires>" + expires + "</u:Expires>" +
                "</u:Timestamp>" +
                "<o:BinarySecurityToken u:Id=\"" + uuid + "\" ValueType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3\" EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\">" +
                Base64.getEncoder().encodeToString(certificate.getEncoded()) +
                "</o:BinarySecurityToken>" +
                "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">" +
                "<SignedInfo>" +
                "<CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>" +
                "<SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/>" +
                "<Reference URI=\"#_0\">" +
                "<Transforms>" +
                "<Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>" +
                "</Transforms>" +
                "<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>" +
                "<DigestValue>" + digest + "</DigestValue>" +
                "</Reference>" +
                "</SignedInfo>" +
                "<SignatureValue>" + signature + "</SignatureValue>" +
                "<KeyInfo>" +
                "<o:SecurityTokenReference>" +
                "<o:Reference ValueType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3\" URI=\"#" + uuid + "\"/>" +
                "</o:SecurityTokenReference>" +
                "</KeyInfo>" +
                "</Signature>" +
                "</o:Security>" +
                "</s:Header>" +
                "<s:Body>" +
                "<Autentica xmlns=\"http://DescargaMasivaTerceros.gob.mx\"/>" +
                "</s:Body>" +
                "</s:Envelope>";
    }

}
