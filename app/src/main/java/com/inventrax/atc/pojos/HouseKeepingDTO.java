package com.inventrax.atc.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class HouseKeepingDTO {



    @SerializedName("UserId")
    private String UserId;
    @SerializedName("AccountID")
    private String AccountID;
    @SerializedName("TenantName")
    private String TenantName;
    @SerializedName("TenantID")
    private String TenantID;
    @SerializedName("Warehouse")
    private String Warehouse;
    @SerializedName("WarehouseId")
    private String WarehouseId;
    @SerializedName("CartonNo")
    private String CartonNo;
    @SerializedName("Result")
    private String Result;


    public HouseKeepingDTO() {
    }

    public HouseKeepingDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "AccountID":
                    if (entry.getValue() != null) {
                        this.setAccountID(entry.getValue().toString());
                    }
                    break;
                case "TenantName":
                    if (entry.getValue() != null) {
                        this.setTenantName(entry.getValue().toString());
                    }
                    break;
                case "TenantID":
                    if (entry.getValue() != null) {
                        this.setTenantID(entry.getValue().toString());
                    }
                    break;
                case "Warehouse":
                    if (entry.getValue() != null) {
                        this.setWarehouse(entry.getValue().toString());
                    }
                    break;
                case "WarehouseId":
                    if (entry.getValue() != null) {
                        this.setWarehouseId(entry.getValue().toString());
                    }
                    break;
                case "CartonNo":
                    if (entry.getValue() != null) {
                        this.setCartonNo(entry.getValue().toString());
                    }
                    break;
                case "Result":
                    if (entry.getValue() != null) {
                        this.setResult(entry.getValue().toString());
                    }
                    break;
                case "UserId":
                    if (entry.getValue() != null) {
                        this.setUserId(entry.getValue().toString());
                    }
                    break;


            }
        }
    }


    public String getAccountID() {
        return AccountID;
    }

    public void setAccountID(String accountID) {
        AccountID = accountID;
    }

    public String getTenantName() {
        return TenantName;
    }

    public void setTenantName(String tenantName) {
        TenantName = tenantName;
    }

    public String getTenantID() {
        return TenantID;
    }

    public void setTenantID(String tenantID) {
        TenantID = tenantID;
    }

    public String getWarehouse() {
        return Warehouse;
    }

    public void setWarehouse(String warehouse) {
        Warehouse = warehouse;
    }

    public String getWarehouseId() {
        return WarehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        WarehouseId = warehouseId;
    }

    public String getCartonNo() {
        return CartonNo;
    }

    public void setCartonNo(String cartonNo) {
        CartonNo = cartonNo;
    }

    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }
}
