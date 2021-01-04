package com.inventrax.atc.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class ScanDTO {

    @SerializedName("ScanInput")
    public String ScanInput ;
    @SerializedName("ScanResult")
    public Boolean ScanResult ;
    @SerializedName("Result")
    public Boolean Result ;
    @SerializedName("Message")
    public String Message ;
    @SerializedName("WarehouseID")
    public String WarehouseID ;
    @SerializedName("TenantID")
    public String TenantID ;
    @SerializedName("SkuCode")
    public String SkuCode;

    @SerializedName("Batch")
    public String Batch ;

    @SerializedName("SerialNumber")
    public String SerialNumber;

    @SerializedName("ExpDate")
    public String ExpDate ;

    @SerializedName("MfgDate")
    public String MfgDate;

    @SerializedName("PrjRef")
    public String PrjRef;

    @SerializedName("KitID")
    public String KitID;

    @SerializedName("LineNumber")
    public String LineNumber;

     @SerializedName("UserID")
    public String UserID;

    @SerializedName("InboundID")
    public String InboundID;

    @SerializedName("AccountID")
    public String AccountID;

    @SerializedName("Mrp")
    public String Mrp;

    @SerializedName("ObdNumber")
    public String ObdNumber;

    @SerializedName("VlpdNumber")
    public String VlpdNumber;

    @SerializedName("IsCycleCount")
    public Boolean IsCycleCount;

    @SerializedName("SupplierInvoiceDetailsID")
    public String SupplierInvoiceDetailsID;

    @SerializedName("HUNo")
    public String HUNo;

    @SerializedName("HUSize")
    public String HUSize;



    public ScanDTO(){

    }

    public ScanDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "ScanInput":
                    if (entry.getValue() != null) {
                        this.setScanInput(entry.getValue().toString());
                    }
                    break;
                case "ScanResult":
                    if (entry.getValue() != null) {
                        this.setScanResult(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "Message":
                    if (entry.getValue() != null) {
                        this.setMessage(entry.getValue().toString());
                    }
                    break;
                case "WarehouseID":
                    if (entry.getValue() != null) {
                        this.setWarehouseID(entry.getValue().toString());
                    }
                    break;
                case "TenantID":
                    if (entry.getValue() != null) {
                        this.setTenantID(entry.getValue().toString());
                    }
                    break;
                case "SkuCode":
                    if (entry.getValue() != null) {
                        this.setSkuCode(entry.getValue().toString());
                    }
                    break;
                case "Batch":
                    if (entry.getValue() != null) {
                        this.setBatch(entry.getValue().toString());
                    }
                    break;
                 case "SerialNumber":
                    if (entry.getValue() != null) {
                        this.setSerialNumber(entry.getValue().toString());
                    }
                    break;
                 case "ExpDate":
                    if (entry.getValue() != null) {
                        this.setExpDate(entry.getValue().toString());
                    }
                    break;
                 case "MfgDate":
                    if (entry.getValue() != null) {
                        this.setMfgDate(entry.getValue().toString());
                    }
                    break;
                 case "PrjRef":
                    if (entry.getValue() != null) {
                        this.setPrjRef(entry.getValue().toString());
                    }
                    break;
                 case "KitID":
                    if (entry.getValue() != null) {
                        this.setKitID(entry.getValue().toString());
                    }
                    break;
                 case "LineNumber":
                    if (entry.getValue() != null) {
                        this.setLineNumber(entry.getValue().toString());
                    }
                    break;
                case "UserID":
                    if (entry.getValue() != null) {
                        this.setUserID(entry.getValue().toString());
                    }
                    break;
                case "AccountID":
                    if (entry.getValue() != null) {
                        this.setAccountID(entry.getValue().toString());
                    }
                    break;
                 case "InboundID":
                    if (entry.getValue() != null) {
                        this.setInboundID(entry.getValue().toString());
                    }
                    break;
                case "Mrp":
                    if (entry.getValue() != null) {
                        this.setMrp(entry.getValue().toString());
                    }
                    break;
                case "ObdNumber":
                    if (entry.getValue() != null) {
                        this.setObdNumber(entry.getValue().toString());
                    }
                    break;
                case "VlpdNumber":
                    if (entry.getValue() != null) {
                        this.setVlpdNumber(entry.getValue().toString());
                    }
                    break;

                case "SupplierInvoiceDetailsID":
                    if (entry.getValue() != null) {
                        this.setSupplierInvoiceDetailsID(entry.getValue().toString());
                    }
                    break;
                case "IsCycleCount":
                    if (entry.getValue() != null) {
                        this.setCycleCount(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;

                case "HUSize":
                    if (entry.getValue() != null) {
                        this.setHUSize(entry.getValue().toString());
                    }
                    break;
                 case "HUNo":
                    if (entry.getValue() != null) {
                        this.setHUNo(entry.getValue().toString());
                    }
                    break;


            }

        }
    }

    public String getHUNo() {
        return HUNo;
    }

    public void setHUNo(String HUNo) {
        this.HUNo = HUNo;
    }

    public String getHUSize() {
        return HUSize;
    }

    public void setHUSize(String HUSize) {
        this.HUSize = HUSize;
    }

    public String getScanInput() {
        return ScanInput;
    }

    public void setScanInput(String scanInput) {
        ScanInput = scanInput;
    }


    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getWarehouseID() {
        return WarehouseID;
    }

    public void setWarehouseID(String warehouseID) {
        WarehouseID = warehouseID;
    }

    public String getTenantID() {
        return TenantID;
    }

    public void setTenantID(String tenantID) {
        TenantID = tenantID;
    }

    public String getSkuCode() {
        return SkuCode;
    }

    public void setSkuCode(String skuCode) {
        SkuCode = skuCode;
    }

    public String getBatch() {
        return Batch;
    }

    public void setBatch(String batch) {
        Batch = batch;
    }

    public String getSerialNumber() {
        return SerialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        SerialNumber = serialNumber;
    }

    public String getExpDate() {
        return ExpDate;
    }

    public void setExpDate(String expDate) {
        ExpDate = expDate;
    }

    public String getMfgDate() {
        return MfgDate;
    }

    public void setMfgDate(String mfgDate) {
        MfgDate = mfgDate;
    }

    public String getPrjRef() {
        return PrjRef;
    }

    public void setPrjRef(String prjRef) {
        PrjRef = prjRef;
    }

    public String getKitID() {
        return KitID;
    }

    public void setKitID(String kitID) {
        KitID = kitID;
    }

    public String getLineNumber() {
        return LineNumber;
    }

    public void setLineNumber(String lineNumber) {
        LineNumber = lineNumber;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getInboundID() {
        return InboundID;
    }

    public void setInboundID(String inboundID) {
        InboundID = inboundID;
    }

    public String getAccountID() {
        return AccountID;
    }

    public void setAccountID(String accountID) {
        AccountID = accountID;
    }

    public Boolean getScanResult() {
        return ScanResult;
    }

    public void setScanResult(Boolean scanResult) {
        ScanResult = scanResult;
    }

    public String getMrp() {
        return Mrp;
    }

    public void setMrp(String mrp) {
        Mrp = mrp;
    }


    public String getObdNumber() {
        return ObdNumber;
    }

    public void setObdNumber(String obdNumber) {
        ObdNumber = obdNumber;
    }

    public String getVlpdNumber() {
        return VlpdNumber;
    }

    public void setVlpdNumber(String vlpdNumber) {
        VlpdNumber = vlpdNumber;
    }


    public String getSupplierInvoiceDetailsID() {
        return SupplierInvoiceDetailsID;
    }

    public void setSupplierInvoiceDetailsID(String supplierInvoiceDetailsID) {
        SupplierInvoiceDetailsID = supplierInvoiceDetailsID;
    }

    public Boolean getCycleCount() {
        return IsCycleCount;
    }

    public void setCycleCount(Boolean cycleCount) {
        IsCycleCount = cycleCount;
    }

}
