package com.inventrax.atc.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

/**
 * Created by Padmaja Rani. B on 04/01/2019.
 */

public class PutawayDTO {

    @SerializedName("SuggestedPutawayID")
    private String SuggestedPutawayID;
    @SerializedName("MaterialMasterId")
    private String MaterialMasterId;
    @SerializedName("MCode")
    private String MCode;
    @SerializedName("MDescription")
    private String MDescription;
    @SerializedName("CartonCode")
    private String CartonCode;
    @SerializedName("CartonID")
    private String CartonID;
    @SerializedName("Location")
    private String Location;
    @SerializedName("LocationID")
    private String LocationID;
    @SerializedName("MfgDate")
    private String MfgDate;
    @SerializedName("ExpDate")
    private String ExpDate;
    @SerializedName("SerialNo")
    private String SerialNo;
    @SerializedName("BatchNo")
    private String BatchNo;
    @SerializedName("ProjectRefNo")
    private String ProjectRefNo;
    @SerializedName("AssignedQuantity")
    private String AssignedQuantity;
    @SerializedName("SuggestedQty")
    private String SuggestedQty;
    @SerializedName("SuggestedReceivedQty")
    private String SuggestedReceivedQty;
    @SerializedName("SuggestedRemainingQty")
    private String SuggestedRemainingQty;
    @SerializedName("TransferRequestDetailsId")
    private String TransferRequestDetailsId;
    @SerializedName("PickedLocationID")
    private String PickedLocationID;
    @SerializedName("GMDRemainingQty")
    private String GMDRemainingQty;
    @SerializedName("PutAwayQty")
    private String PutAwayQty;
    @SerializedName("InboundId")
    private String InboundId;
    @SerializedName("Result")
    private String Result;
    @SerializedName("SkipQty")
    private String SkipQty;
    @SerializedName("UserID")
    private String UserID;
    @SerializedName("TotalQty")
    private String TotalQty;
    @SerializedName("SkipReason")
    private String SkipReason;
    @SerializedName("ScannedLocation")
    private String ScannedLocation;

    @SerializedName("TransferRefId")
    private String transferRefId;
    @SerializedName("MRP")
    private String MRP;
    @SerializedName("TransferRequestId")
    private String TransferRequestId;
    @SerializedName("Dock")
    private String Dock;

    @SerializedName("StorageCode")
    private String StorageCode;

    @SerializedName("HUNo")
    public String HUNo;

    @SerializedName("HUSize")
    public String HUSize;

    public PutawayDTO() {
    }


    public PutawayDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "SuggestedPutawayID":
                    if (entry.getValue() != null) {
                        this.setSuggestedPutawayID(entry.getValue().toString());
                    }
                    break;
                case "MaterialMasterId":
                    if (entry.getValue() != null) {
                        this.setMaterialMasterId(entry.getValue().toString());
                    }
                    break;
                case "MCode":
                    if (entry.getValue() != null) {
                        this.setMCode(entry.getValue().toString());
                    }
                    break;
                case "MDescription":
                    if (entry.getValue() != null) {
                        this.setMDescription(entry.getValue().toString());
                    }
                    break;
                case "CartonCode":
                    if (entry.getValue() != null) {
                        this.setCartonCode(entry.getValue().toString());
                    }
                    break;
                case "CartonID":
                    if (entry.getValue() != null) {
                        this.setCartonID(entry.getValue().toString());
                    }
                    break;
                case "Location":
                    if (entry.getValue() != null) {
                        this.setLocation(entry.getValue().toString());
                    }
                    break;
                case "LocationID":
                    if (entry.getValue() != null) {
                        this.setLocationID(entry.getValue().toString());
                    }
                    break;
                case "MfgDate":
                    if (entry.getValue() != null) {
                        this.setMfgDate(entry.getValue().toString());
                    }
                    break;
                case "ExpDate":
                    if (entry.getValue() != null) {
                        this.setExpDate(entry.getValue().toString());
                    }
                    break;
                case "SerialNo":
                    if (entry.getValue() != null) {
                        this.setSerialNo(entry.getValue().toString());
                    }
                    break;
                case "BatchNo":
                    if (entry.getValue() != null) {
                        this.setBatchNo(entry.getValue().toString());
                    }
                    break;
                case "ProjectRefNo":
                    if (entry.getValue() != null) {
                        this.setProjectRefNo(entry.getValue().toString());
                    }
                    break;
                case "AssignedQuantity":
                    if (entry.getValue() != null) {
                        this.setAssignedQuantity(entry.getValue().toString());
                    }
                    break;
                case "SuggestedQty":
                    if (entry.getValue() != null) {
                        this.setSuggestedQty(entry.getValue().toString());
                    }
                    break;
                case "SuggestedReceivedQty":
                    if (entry.getValue() != null) {
                        this.setSuggestedReceivedQty(entry.getValue().toString());
                    }
                    break;
                case "SuggestedRemainingQty":
                    if (entry.getValue() != null) {
                        this.setSuggestedRemainingQty(entry.getValue().toString());
                    }
                    break;
                case "TransferRequestDetailsId":
                    if (entry.getValue() != null) {
                        this.setTransferRequestDetailsId(entry.getValue().toString());
                    }
                    break;
                case "PickedLocationID":
                    if (entry.getValue() != null) {
                        this.setPickedLocationID(entry.getValue().toString());
                    }
                    break;
                case "GMDRemainingQty":
                    if (entry.getValue() != null) {
                        this.setGMDRemainingQty(entry.getValue().toString());
                    }
                    break;
                case "PutAwayQty":
                    if (entry.getValue() != null) {
                        this.setPutAwayQty(entry.getValue().toString());
                    }
                    break;

                case "InboundId":
                    if (entry.getValue() != null) {
                        this.setInboundId(entry.getValue().toString());
                    }
                    break;
                case "Result":
                    if (entry.getValue() != null) {
                        this.setResult(entry.getValue().toString());
                    }
                    break;
                case "SkipQty":
                    if (entry.getValue() != null) {
                        this.setSkipQty(entry.getValue().toString());
                    }
                    break;
                case "UserID":
                    if (entry.getValue() != null) {
                        this.setUserID(entry.getValue().toString());
                    }
                    break;
                case "TotalQty":
                    if (entry.getValue() != null) {
                        this.setTotalQty(entry.getValue().toString());
                    }
                    break;
                case "SkipReason":
                    if (entry.getValue() != null) {
                        this.setSkipReason(entry.getValue().toString());
                    }
                    break;

                case "TransferRefId":
                    if (entry.getValue() != null) {
                        this.setTransferRefId(entry.getValue().toString());
                    }
                    break;
                case "MRP":
                    if (entry.getValue() != null) {
                        this.setMRP(entry.getValue().toString());
                    }
                    break;
                case "TransferRequestId":
                    if (entry.getValue() != null) {
                        this.setTransferRequestId(entry.getValue().toString());
                    }
                    break;
                case "Dock":
                    if (entry.getValue() != null) {
                        this.setDock(entry.getValue().toString());
                    }
                    break;
                case "StorageCode":
                    if (entry.getValue() != null) {
                        this.setStorageCode(entry.getValue().toString());
                    }
                    break;
                case "ScannedLocation":
                    if (entry.getValue() != null) {
                        this.setScannedLocation(entry.getValue().toString());
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

    public String getTransferRefId() {
        return transferRefId;
    }

    public void setTransferRefId(String transferRefId) {
        this.transferRefId = transferRefId;
    }

    public String getSuggestedPutawayID() {
        return SuggestedPutawayID;
    }

    public void setSuggestedPutawayID(String suggestedPutawayID) {
        SuggestedPutawayID = suggestedPutawayID;
    }

    public String getMaterialMasterId() {
        return MaterialMasterId;
    }

    public void setMaterialMasterId(String materialMasterId) {
        MaterialMasterId = materialMasterId;
    }

    public String getMCode() {
        return MCode;
    }

    public void setMCode(String MCode) {
        this.MCode = MCode;
    }

    public String getMDescription() {
        return MDescription;
    }

    public void setMDescription(String MDescription) {
        this.MDescription = MDescription;
    }

    public String getCartonCode() {
        return CartonCode;
    }

    public void setCartonCode(String cartonCode) {
        CartonCode = cartonCode;
    }

    public String getCartonID() {
        return CartonID;
    }

    public void setCartonID(String cartonID) {
        CartonID = cartonID;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getLocationID() {
        return LocationID;
    }

    public void setLocationID(String locationID) {
        LocationID = locationID;
    }

    public String getMfgDate() {
        return MfgDate;
    }

    public void setMfgDate(String mfgDate) {
        MfgDate = mfgDate;
    }

    public String getExpDate() {
        return ExpDate;
    }

    public void setExpDate(String expDate) {
        ExpDate = expDate;
    }

    public String getSerialNo() {
        return SerialNo;
    }

    public void setSerialNo(String serialNo) {
        SerialNo = serialNo;
    }

    public String getBatchNo() {
        return BatchNo;
    }

    public void setBatchNo(String batchNo) {
        BatchNo = batchNo;
    }

    public String getProjectRefNo() {
        return ProjectRefNo;
    }

    public void setProjectRefNo(String projectRefNo) {
        ProjectRefNo = projectRefNo;
    }

    public String getAssignedQuantity() {
        return AssignedQuantity;
    }

    public void setAssignedQuantity(String assignedQuantity) {
        AssignedQuantity = assignedQuantity;
    }

    public String getSuggestedQty() {
        return SuggestedQty;
    }

    public void setSuggestedQty(String suggestedQty) {
        SuggestedQty = suggestedQty;
    }

    public String getSuggestedReceivedQty() {
        return SuggestedReceivedQty;
    }

    public void setSuggestedReceivedQty(String suggestedReceivedQty) {
        SuggestedReceivedQty = suggestedReceivedQty;
    }

    public String getSuggestedRemainingQty() {
        return SuggestedRemainingQty;
    }

    public void setSuggestedRemainingQty(String suggestedRemainingQty) {
        SuggestedRemainingQty = suggestedRemainingQty;
    }

    public String getTransferRequestDetailsId() {
        return TransferRequestDetailsId;
    }

    public void setTransferRequestDetailsId(String transferRequestDetailsId) {
        TransferRequestDetailsId = transferRequestDetailsId;
    }

    public String getPickedLocationID() {
        return PickedLocationID;
    }

    public void setPickedLocationID(String pickedLocationID) {
        PickedLocationID = pickedLocationID;
    }

    public String getGMDRemainingQty() {
        return GMDRemainingQty;
    }

    public void setGMDRemainingQty(String GMDRemainingQty) {
        this.GMDRemainingQty = GMDRemainingQty;
    }

    public String getPutAwayQty() {
        return PutAwayQty;
    }

    public void setPutAwayQty(String putAwayQty) {
        PutAwayQty = putAwayQty;
    }

    public String getInboundId() {
        return InboundId;
    }

    public void setInboundId(String inboundId) {
        InboundId = inboundId;
    }

    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }

    public String getSkipQty() {
        return SkipQty;
    }

    public void setSkipQty(String skipQty) {
        SkipQty = skipQty;
    }

    public String getUserID() {
        return UserID;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public String getTotalQty() {
        return TotalQty;
    }

    public void setTotalQty(String totalQty) {
        TotalQty = totalQty;
    }

    public String getSkipReason() {
        return SkipReason;
    }

    public void setSkipReason(String skipReason) {
        SkipReason = skipReason;
    }

    public String getMRP() {
        return MRP;
    }

    public void setMRP(String MRP) {
        this.MRP = MRP;
    }

    public String getTransferRequestId() {
        return TransferRequestId;
    }

    public void setTransferRequestId(String transferRequestId) {
        TransferRequestId = transferRequestId;
    }

    public String getDock() {
        return Dock;
    }

    public void setDock(String dock) {
        Dock = dock;
    }

    public String getStorageCode() {
        return StorageCode;
    }

    public void setStorageCode(String storageCode) {
        StorageCode = storageCode;
    }

    public String getScannedLocation() {
        return ScannedLocation;
    }

    public void setScannedLocation(String scannedLocation) {
        ScannedLocation = scannedLocation;
    }

}
