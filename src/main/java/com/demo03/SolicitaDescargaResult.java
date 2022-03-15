package com.demo03;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class SolicitaDescargaResult {
    private String status;
    private String msg;
    private String idRequest;
}
