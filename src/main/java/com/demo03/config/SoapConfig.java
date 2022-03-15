package com.demo03.config;

import com.demo03.LogHttpHeaderClientInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;

@Configuration
public class SoapConfig {

//    @Value("${AUTH_URL:https://cfdidescargamasivasolicitud.clouda.sat.gob.mx/Autenticacion/Autenticacion.svc}")
    @Value("${AUTH_URL:https://pruebacfdidesmastersolicitud.cloudapp.net/Autenticacion/Autenticacion.svc}")
    private String authUrl;

//    @Value("${REQ_URL:https://cfdidescargamasivasolicitud.clouda.sat.gob.mx/SolicitaDescargaService.svc}")
    @Value("${REQ_URL:https://pruebacfdidesmastersolicitud.cloudapp.net/SolicitaDescargaService.svc}")
    private String req_url;

//    @Value("${REQ_URL:https://cfdidescargamasivasolicitud.clouda.sat.gob.mx/VerificaSolicitudDescargaService.svc}")
    @Value("${REQ_URL:https://pruebacfdidesmastersolicitud.cloudapp.net/VerificaSolicitudDescargaService.svc}")
    private String verify_req_url;

    @Value("${REQ_URL:https://cfdidescargamasiva.clouda.sat.gob.mx/DescargaMasivaService.svc}")
//    @Value("${REQ_URL:https://pruebacfdidesmastersolicitud.cloudapp.net/DescargaMasivaService.svc}")
    private String download_url;

    @Bean
    public LogHttpHeaderClientInterceptor logHttpHeaderClientInterceptor() {
        return new LogHttpHeaderClientInterceptor();
    }

/*    @Bean
    Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setContextPath("com.naat.constancy.ws.cecoban");
        return jaxb2Marshaller;
    }*/

    @Bean
    public WebServiceTemplate webServiceTemplateAuthentication(
//            , Jaxb2Marshaller jaxb2Marshaller
    ) {

        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        ClientInterceptor[] interceptors = new ClientInterceptor[]{logHttpHeaderClientInterceptor()};
        webServiceTemplate.setInterceptors(interceptors);
//        webServiceTemplate.setMarshaller(jaxb2Marshaller);
//        webServiceTemplate.setUnmarshaller(jaxb2Marshaller);
        webServiceTemplate.setDefaultUri(authUrl);
        return webServiceTemplate;
    }

    @Bean
    public WebServiceTemplate webServiceTemplateRequest(
//            , Jaxb2Marshaller jaxb2Marshaller
    ) {

        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        ClientInterceptor[] interceptors = new ClientInterceptor[]{logHttpHeaderClientInterceptor()};
        webServiceTemplate.setInterceptors(interceptors);
//        webServiceTemplate.setMarshaller(jaxb2Marshaller);
//        webServiceTemplate.setUnmarshaller(jaxb2Marshaller);
        webServiceTemplate.setDefaultUri(req_url);
        return webServiceTemplate;
    }

    @Bean
    public WebServiceTemplate webServiceTemplateVerifyRequest(
//            , Jaxb2Marshaller jaxb2Marshaller
    ) {

        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        ClientInterceptor[] interceptors = new ClientInterceptor[]{logHttpHeaderClientInterceptor()};
        webServiceTemplate.setInterceptors(interceptors);
//        webServiceTemplate.setMarshaller(jaxb2Marshaller);
//        webServiceTemplate.setUnmarshaller(jaxb2Marshaller);
        webServiceTemplate.setDefaultUri(verify_req_url);
        return webServiceTemplate;
    }

    @Bean
    public WebServiceTemplate webServiceTemplateDownloadRequest(
//            , Jaxb2Marshaller jaxb2Marshaller
    ) {

        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        ClientInterceptor[] interceptors = new ClientInterceptor[]{logHttpHeaderClientInterceptor()};
        webServiceTemplate.setInterceptors(interceptors);
//        webServiceTemplate.setMarshaller(jaxb2Marshaller);
//        webServiceTemplate.setUnmarshaller(jaxb2Marshaller);
        webServiceTemplate.setDefaultUri(download_url);
        return webServiceTemplate;
    }
}
