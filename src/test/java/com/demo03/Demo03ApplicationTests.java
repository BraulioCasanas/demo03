package com.demo03;

import com.demo03.service.SatAuthenticationService;
import com.demo03.service.SatDownloadService;
import com.demo03.service.SatRequestService;
import com.demo03.service.SatVerifyRequestService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Objects;

@SpringBootTest
@RunWith(SpringRunner.class)
class Demo03ApplicationTests {

    @Autowired
    private SatAuthenticationService satAuthenticationService;

    @Autowired
    private SatRequestService satRequestService;

    @Autowired
    private SatVerifyRequestService satVerifyRequestService;

    @Autowired
    private SatDownloadService satDownloadService;

    @Test
    void contextLoads() {
        String token = satAuthenticationService.authentication();

        SolicitaDescargaResult request = satRequestService.request(token);
        System.out.println(request);

        if (Objects.equals(request.getStatus(), "5000")) {
            String idRequest = request.getIdRequest();
            VerificaSolicitaDescargaResult request1 = satVerifyRequestService.request(token, idRequest);
            System.out.println(request1);

            if ( Objects.equals(request1.getStatus(), "5000") && Objects.equals(request1.getEstadoSolicitud(), "3") ) {
                request1.getIdsPaquetes()
                                .forEach(file -> {
                                    DescargaResult request2 = satDownloadService.request(token, file);
                                    System.out.println(request2);
                                });
            }
        }
    }

    @Test
    void download () {
        String token = satAuthenticationService.authentication();
        DescargaResult request2 = satDownloadService.request(token, "703EC6D8-C51E-41DC-996B-EC221AE2000C_01");
        System.out.println(request2);
    }

}
