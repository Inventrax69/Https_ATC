package com.inventrax.atc.model;

import java.util.List;

/**
 * Created by karthik.m on 06/25/2018.
 */

public class PalletInfo {

    public String PalletCode;

    public String getPalletCode() {
        return PalletCode;
    }

    public void setPalletCode(String palletCode) {
        PalletCode = palletCode;
    }

    public List<MaterialInfo> Materialinfo;

    public List<MaterialInfo> getMaterialinfo() {
        return Materialinfo;
    }

    public void setMaterialinfo(List<MaterialInfo> materialinfo) {
        Materialinfo = materialinfo;
    }
}
