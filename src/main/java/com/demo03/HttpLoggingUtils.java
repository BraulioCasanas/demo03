package com.demo03;

import lombok.extern.log4j.Log4j2;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.soap.saaj.SaajSoapMessage;
import org.springframework.xml.transform.TransformerObjectSupport;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

@Log4j2
public class HttpLoggingUtils extends TransformerObjectSupport {


    private static final String NEW_LINE = System.getProperty("line.separator");

    private HttpLoggingUtils() {
    }

    public static void logMessage(String id, WebServiceMessage webServiceMessage) {
        try {

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            StreamResult result = new StreamResult(new StringWriter());
            Document doc = ((SaajSoapMessage) webServiceMessage).getDocument();
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);
            String xmlString = result.getWriter().toString();

            log.info(NEW_LINE + "----------------------------" + NEW_LINE + id + NEW_LINE
                    + "----------------------------" + NEW_LINE + xmlString + NEW_LINE);
        } catch (Exception e) {
            log.error("Unable to log HTTP message.", e);
        }
    }
}
