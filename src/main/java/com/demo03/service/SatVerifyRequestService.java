package com.demo03.service;

import com.demo03.Utils;
import com.demo03.VerificaSolicitaDescargaResult;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.*;

@Log4j2
@Service
public class SatVerifyRequestService {

    private final WebServiceTemplate webServiceTemplate;

    @Autowired
    public SatVerifyRequestService(@Qualifier("webServiceTemplateVerifyRequest") WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
    }

    public VerificaSolicitaDescargaResult request(String token, String idRequest) {

        HttpComponentsMessageSender httpComponentsMessageSender = new HttpComponentsMessageSender();

        Header header = new BasicHeader(HttpHeaders.AUTHORIZATION, "WRAP access_token=" + "\"" + token + "\"");
        List<Header> headers = Collections.singletonList(header);
        HttpClientBuilder httpClientBuilder = null;
        try {
            httpClientBuilder = HttpClients.custom()
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            e.printStackTrace();
        }
        httpClientBuilder.setDefaultHeaders(headers).addInterceptorFirst(
                (HttpRequestInterceptor) (request, context) -> request.removeHeaders(HTTP.CONTENT_LEN));

        httpComponentsMessageSender.setHttpClient(httpClientBuilder.build());
        webServiceTemplate.setMessageSender(httpComponentsMessageSender);

        return webServiceTemplate.sendAndReceive(message -> {

            try {

                String rfc = Utils.rfc;

                SaajSoapMessage saajSoapMessage = (SaajSoapMessage) message;
                SOAPMessage soapMessage = saajSoapMessage.getSaajMessage();
                SOAPPart soapPart = soapMessage.getSOAPPart();
                soapPart.setContent(new StreamSource(new StringReader(generate(Utils.getCertificate(), Utils.getPrivateKey(), idRequest, rfc))));
                ((SoapMessage) message).setSoapAction("http://DescargaMasivaTerceros.sat.gob.mx/IVerificaSolicitudDescargaService/VerificaSolicitudDescarga");
            } catch (Exception e) {
                log.throwing(e);
            }
        }, message -> {
            Document doc = ((SaajSoapMessage) message).getDocument();
            NamedNodeMap verificaSolicitudDescargaResult = doc.getElementsByTagName("VerificaSolicitudDescargaResult")
                    .item(0)
                    .getAttributes();
            String status = verificaSolicitudDescargaResult.getNamedItem("CodEstatus").getTextContent();
            String msg = verificaSolicitudDescargaResult.getNamedItem("Mensaje").getTextContent();
            String estadoSolicitud = verificaSolicitudDescargaResult.getNamedItem("EstadoSolicitud").getTextContent();
            String numeroCFDIs = verificaSolicitudDescargaResult.getNamedItem("NumeroCFDIs").getTextContent();

            List<String> idPaquetesLst = new ArrayList<>();
            if (Objects.equals(estadoSolicitud, "3")) {
                NodeList idsPaquetes = doc.getElementsByTagName("IdsPaquetes");
                for (int i = 0; i < idsPaquetes.getLength(); i++) {
                    idPaquetesLst.add(idsPaquetes.item(i).getTextContent());
                }
            }

            return VerificaSolicitaDescargaResult.builder()
                    .status(status)
                    .msg(msg)
                    .estadoSolicitud(estadoSolicitud)
                    .numeroCFDIs(numeroCFDIs)
                    .idsPaquetes(idPaquetesLst)
                    .build();
        });

    }

    /**
     * Generate XML to send through SAT's web service
     *
     * @param certificate
     * @param privateKey
     * @param idRequest
     * @param rfcSolicitante
     * @throws NoSuchAlgorithmException
     * @throws SignatureException
     * @throws InvalidKeyException
     * @throws CertificateEncodingException
     */
    public String generate(X509Certificate certificate, PrivateKey privateKey, String idRequest, String rfcSolicitante)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, CertificateEncodingException {
        String canonicalTimestamp = "<des:VerificaSolicitudDescarga xmlns:des=\"http://DescargaMasivaTerceros.sat.gob.mx\">" +
                "<des:solicitud IdSolicitud=\"" + idRequest + "\" RfcSolicitante=\"" + rfcSolicitante + ">" +
                "</des:solicitud>" +
                "</des:VerificaSolicitudDescarga>";

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

        return "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:u=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\" xmlns:des=\"http://DescargaMasivaTerceros.sat.gob.mx\" xmlns:xd=\"http://www.w3.org/2000/09/xmldsig#\">" +
                "<s:Header/>" +
                "<s:Body>" +
                "<des:VerificaSolicitudDescarga>" +
                "<des:solicitud IdSolicitud=\"" + idRequest + "\" RfcSolicitante=\"" + rfcSolicitante + "\">" +
                "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">" +
                "<SignedInfo>" +
                "<CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/>" +
                "<SignatureMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#rsa-sha1\"/>" +
                "<Reference URI=\"#_0\">" +
                "<Transforms>" +
                "<Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/></Transforms>" +
                "<DigestMethod Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/>" +
                "<DigestValue>" + digest + "</DigestValue>" +
                "</Reference>" +
                "</SignedInfo>" +
                "<SignatureValue>" + signature + "</SignatureValue>" +
                "<KeyInfo>" +
                "<X509Data>" +
                "<X509IssuerSerial>" +
                "<X509IssuerName>" + certificate.getIssuerX500Principal() + "</X509IssuerName>" +
                "<X509SerialNumber>" + certificate.getSerialNumber() + "</X509SerialNumber>" +
                "</X509IssuerSerial>" +
                "<X509Certificate>" + Base64.getEncoder().encodeToString(certificate.getEncoded()) + "</X509Certificate>" +
                "</X509Data>" +
                "</KeyInfo>" +
                "</Signature>" +
                "</des:solicitud>" +
                "</des:VerificaSolicitudDescarga>" +
                "</s:Body>" +
                "</s:Envelope>";
    }

}
