package com.inventrax.atc.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

/**
 * Created by karthik.m on 07/13/2018.
 */

public class LoadDTO {

    @SerializedName("VehicleNumber")
    private String vehicleNumber;
    @SerializedName("DockNumber")
    private String dockNumber;
    @SerializedName("BoxQty")
    private Double boxQty;
    @SerializedName("Volume")
    private Double volume;
    @SerializedName("Weight")
    private Double weight;
    @SerializedName("LoadSheetNumber")
    private String LoadSheetNo;
    @SerializedName("LoadedQuantity")
    private String LoadedQuantity;
    @SerializedName("LoadSheetQuantity")
    private String LoadSheetQuantity;
    @SerializedName("CustomerCode")
    private String CustomerCode;
    public LoadDTO()
    {

    }
    public LoadDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "LoadSheetNumber":
                    if(entry.getValue()!=null) {
                        this.setLoadSheetNo(entry.getValue().toString());
                    }
                    break;
                case "VehicleNumber":
                    if(entry.getValue()!=null) {
                        this.setVehicleNumber(entry.getValue().toString());
                    }
                    break;

                case "DockNumber":
                    if(entry.getValue()!=null) {
                        this.setDockNumber(entry.getValue().toString());
                    }
                    break;
                case "BoxQty":
                    if(entry.getValue()!=null) {
                        this.setBoxQty(Double.parseDouble(entry.getValue().toString()));
                    }
                    break;
                case "Volume":
                    if(entry.getValue()!=null) {
                        this.setVolume(Double.parseDouble(entry.getValue().toString()));
                    }
                    break;
                case "Weight":
                    if(entry.getValue()!=null) {
                        this.setWeight(Double.parseDouble(entry.getValue().toString()));
                    }
                    break;
                case "LoadedQuantity":
                    if(entry.getValue()!=null) {
                        this.setLoadedQuantity(entry.getValue().toString());
                    }
                    break;
                case "LoadSheetQuantity":
                    if(entry.getValue()!=null) {
                        this.setLoadSheetQuantity(entry.getValue().toString());
                    }
                    break;
                case "CustomerCode":
                    if(entry.getValue()!=null) {
                        this.setCustomerCode(entry.getValue().toString());
                    }
                    break;

            }
        }
    }

    public String getCustomerCode() {
        return CustomerCode;
    }

    public void setCustomerCode(String customerCode) {
        CustomerCode = customerCode;
    }

    public String getLoadedQuantity() {
        return LoadedQuantity;
    }

    public void setLoadedQuantity(String loadedQuantity) {
        LoadedQuantity = loadedQuantity;
    }

    public String getLoadSheetQuantity() {
        return LoadSheetQuantity;
    }

    public void setLoadSheetQuantity(String loadSheetQuantity) {
        LoadSheetQuantity = loadSheetQuantity;
    }

    public String getDockNumber() {
        return dockNumber;
    }

    public void setDockNumber(String dockNumber) {
        this.dockNumber = dockNumber;
    }


    public String getLoadSheetNo() {
        return LoadSheetNo;
    }

    public void setLoadSheetNo(String loadSheetNo) {
        LoadSheetNo = loadSheetNo;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }
    public Double getBoxQty() {
        return boxQty;
    }

    public void setBoxQty(Double boxQty) {
        this.boxQty = boxQty;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
}
