package com.bankingsystem.bankingsystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class KycProofRequest {

    @NotBlank
    @Size(max = 50)
    private String idprooftype;

    @NotBlank
    @Size(max = 100)
    private String idproofnumber;

    public String getIdprooftype() { return idprooftype; }
    public void setIdprooftype(String idprooftype) { this.idprooftype = idprooftype; }

    public String getIdproofnumber() { return idproofnumber; }
    public void setIdproofnumber(String idproofnumber) { this.idproofnumber = idproofnumber; }
}