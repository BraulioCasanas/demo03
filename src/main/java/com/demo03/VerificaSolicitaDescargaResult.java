package com.demo03;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@Builder
public class VerificaSolicitaDescargaResult {
    private String status;
    private String msg;
    private String estadoSolicitud;
    private String numeroCFDIs;
    private List<String> idsPaquetes;

}
