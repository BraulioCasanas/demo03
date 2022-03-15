package com.demo03;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@ToString
//@Builder
@XmlRootElement
public class DescargaResult {

    private String codEstatus;
    private String mensaje;
    private String paquete;
}
