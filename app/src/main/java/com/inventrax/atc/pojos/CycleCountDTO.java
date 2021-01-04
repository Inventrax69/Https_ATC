package com.inventrax.atc.pojos;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by karthik.m on 06/27/2018.
 */

public class CycleCountDTO {
    @SerializedName("UserId")
    private String userId;
    @SerializedName("CCType")
    private List<CycleCountType> CCType;
    @SerializedName("Location")
    private String location;
    @SerializedName("BoxQty")
    private Double boxQty;
    @SerializedName("IsSatisfiedBoxQty")
    private Boolean isSatisfiedBoxQty;
    @SerializedName("SerialNo")
    private String serialNo;
    @SerializedName("MRP")
    private String MRP;
    @SerializedName("MOP")
    private String MOP;
    @SerializedName("MaterialCode")
    private String materialCode;
    @SerializedName("BatchNo")
    private String batchNo;
    @SerializedName("ColorCodes")
    private List<ColorDTO> colorCodes;
    @SerializedName("SLOC")
    private List<StorageLocationDTO> SLOC;
    @SerializedName("CCName")
    private String CCName;
    @SerializedName("SelectedSLOC")
    private String SelectedSLOC;
    @SerializedName("SelectedColorCode")
    private String SelectedColorCode;
    @SerializedName("Result")
    private String Result;
    @SerializedName("TotalSystemLocationQuantity")
    private String TotalSystemLocationQuantity;
    @SerializedName("TotalSystemStockAllLocations")
    private String TotalSystemStockAllLocations;
    @SerializedName("TotalNoOfLocationsToScan")
    private String TotalNoOfLocationsToScan;
    @SerializedName("TotalNoOfLocationsScanned")
    private String TotalNoOfLocationsScanned;
    @SerializedName("TotalScannedSKUQuantity")
    private String TotalScannedSKUQuantity;
    @SerializedName("UserConfirmReDo")
    private Boolean UserConfirmReDo;
    @SerializedName("AccountID")
    private String AccountID;
    @SerializedName("Count")
    private String Count;
    @SerializedName("MfgDate")
    private String MfgDate;
    @SerializedName("ExpDate")
    private String ExpDate;
    @SerializedName("PalletNo")
    private String PalletNo;
    @SerializedName("ProjectRefNo")
    private String ProjectRefNo;
    @SerializedName("CCQty")
    private String CCQty;
    @SerializedName("WarehouseID")
    private String WarehouseID;
    @SerializedName("TenantId")
    private String TenantId;
    @SerializedName("Rack")
    private String Rack;
    @SerializedName("Column")
    private String Column;
    @SerializedName("Level")
    private String Level;



    @SerializedName("CycleCountSeqCode")
    private String CycleCountSeqCode;


    @SerializedName("StorageLocation")
    private String StorageLocation;


    public CycleCountDTO() {

    }

    public Boolean getUserConfirmReDo() {
        return UserConfirmReDo;
    }

    public void setUserConfirmReDo(Boolean userConfirmReDo) {
        UserConfirmReDo = userConfirmReDo;
    }

    public CycleCountDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {
                case "UserId":
                    if (entry.getValue() != null) {
                        this.setUserId(entry.getValue().toString());
                    }
                    break;
                case "CCName":
                    if (entry.getValue() != null) {
                        this.setCCName(entry.getValue().toString());
                    }
                    break;
                case "Location":
                    if (entry.getValue() != null) {
                        this.setLocation(entry.getValue().toString());
                    }
                    break;
                case "BoxQty":
                    if (entry.getValue() != null) {
                        this.setBoxQty(Double.parseDouble(entry.getValue().toString()));
                    }
                    break;
                case "IsSatisfiedBoxQty":
                    if (entry.getValue() != null) {
                        this.setSatisfiedBoxQty(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "SerialNo":
                    if (entry.getValue() != null) {
                        this.setSerialNo(entry.getValue().toString());
                    }
                    break;
                case "MRP":
                    if (entry.getValue() != null) {
                        this.setMRP(entry.getValue().toString());
                    }
                    break;
                case "MOP":
                    if (entry.getValue() != null) {
                        this.setMOP(entry.getValue().toString());
                    }
                    break;
                case "MaterialCode":
                    if (entry.getValue() != null) {
                        this.setMaterialCode(entry.getValue().toString());
                    }
                    break;
                case "BatchNo":
                    if (entry.getValue() != null) {
                        this.setBatchNo(entry.getValue().toString());
                    }
                    break;
                case "ColorCodes":
                    if (entry.getValue() != null) {
                        List<LinkedTreeMap<?, ?>> ColortreemapList = (List<LinkedTreeMap<?, ?>>) entry.getValue();
                        List<ColorDTO> lstColorCodes = new ArrayList<ColorDTO>();
                        for (int i = 0; i < ColortreemapList.size(); i++) {
                            ColorDTO dto = new ColorDTO(ColortreemapList.get(i).entrySet());
                            lstColorCodes.add(dto);
                            //Log.d("Message", core.getEntityObject().toString());
                        }

                        this.setColorCodes(lstColorCodes);
                    }

                    break;
                case "SLOC":
                    if (entry.getValue() != null) {
                        List<LinkedTreeMap<?, ?>> SloctreemapList = (List<LinkedTreeMap<?, ?>>) entry.getValue();
                        List<StorageLocationDTO> lstSLOC = new ArrayList<StorageLocationDTO>();
                        for (int i = 0; i < SloctreemapList.size(); i++) {
                            StorageLocationDTO dto = new StorageLocationDTO(SloctreemapList.get(i).entrySet());
                            lstSLOC.add(dto);
                            //Log.d("Message", core.getEntityObject().toString());

                        }

                        this.setSLOC(lstSLOC);
                    }
                    break;

                case "SelectedSLOC":
                    if (entry.getValue() != null) {
                        this.setSelectedSLOC(entry.getValue().toString());
                    }
                    break;
                case "SelectedColorCode":
                    if (entry.getValue() != null) {
                        this.setSelectedColorCode(entry.getValue().toString());
                    }
                    break;
                case "Result":
                    if (entry.getValue() != null) {
                        this.setResult(entry.getValue().toString());

                    }
                    break;
                case "TotalSystemLocationQuantity":
                    if (entry.getValue() != null) {
                        this.setTotalSystemLocationQuantity(entry.getValue().toString());
                    }
                    break;
                case "TotalSystemStockAllLocations":
                    if (entry.getValue() != null) {
                        this.setTotalSystemStockAllLocations(entry.getValue().toString());
                    }
                    break;
                case "TotalNoOfLocationsToScan":
                    if (entry.getValue() != null) {
                        this.setTotalNoOfLocationsToScan(entry.getValue().toString());
                    }
                    break;
                case "TotalNoOfLocationsScanned":
                    if (entry.getValue() != null) {
                        this.setTotalNoOfLocationsScanned(entry.getValue().toString());
                    }
                    break;
                case "TotalScannedSKUQuantity":
                    if (entry.getValue() != null) {
                        this.setTotalScannedSKUQuantity(entry.getValue().toString());
                    }
                    break;


                case "CCType":
                    if (entry.getValue() != null) {
                        List<LinkedTreeMap<?, ?>> ccTypeMapList = (List<LinkedTreeMap<?, ?>>) entry.getValue();
                        List<CycleCountType> lstCycleCountType = new ArrayList<CycleCountType>();
                        for (int i = 0; i < ccTypeMapList.size(); i++) {
                            CycleCountType dto = new CycleCountType(ccTypeMapList.get(i).entrySet());
                            lstCycleCountType.add(dto);
                            //Log.d("Message", core.getEntityObject().toString());

                        }

                        this.setCCType(lstCycleCountType);
                    }
                    break;

                case "UserConfirmReDo":
                    if (entry.getValue() != null) {
                        this.setUserConfirmReDo(Boolean.parseBoolean(entry.getValue().toString()));

                    }
                    break;


                case "AccountID":
                    if (entry.getValue() != null) {
                        this.setAccountID(entry.getValue().toString());
                    }
                    break;
                case "Count":
                    if (entry.getValue() != null) {
                        this.setCount(entry.getValue().toString());

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
                case "ProjectRefNo":
                    if (entry.getValue() != null) {
                        this.setProjectRefNo(entry.getValue().toString());
                    }
                    break;
                case "PalletNo":
                    if (entry.getValue() != null) {
                        this.setPalletNo(entry.getValue().toString());
                    }
                    break;
                case "CCQty":
                    if (entry.getValue() != null) {
                        this.setCCQty(entry.getValue().toString());
                    }
                    break;

                case "WarehouseID":
                    if (entry.getValue() != null) {
                        this.setWarehouseID(entry.getValue().toString());
                    }
                    break;
                case "TenantId":
                    if (entry.getValue() != null) {
                        this.setTenantId(entry.getValue().toString());
                    }
                    break;
                case "Rack":
                    if (entry.getValue() != null) {
                        this.setRack(entry.getValue().toString());
                    }
                    break;
                case "Column":
                    if (entry.getValue() != null) {
                        this.setColumn(entry.getValue().toString());
                    }
                    break;
                case "Level":
                    if (entry.getValue() != null) {
                        this.setLevel(entry.getValue().toString());
                    }
                    break;
                case "StorageLocation":
                    if (entry.getValue() != null) {
                        this.setStorageLocation(entry.getValue().toString());
                    }
                    break;
                case "CycleCountSeqCode":
                    if (entry.getValue() != null) {
                        this.setCycleCountSeqCode(entry.getValue().toString());
                    }
                    break;

            }
        }
    }

    public String getSelectedSLOC() {
        return SelectedSLOC;
    }

    public void setSelectedSLOC(String selectedSLOC) {
        SelectedSLOC = selectedSLOC;
    }

    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }

    public String getTotalScannedSKUQuantity() {
        return TotalScannedSKUQuantity;
    }

    public void setTotalScannedSKUQuantity(String totalScannedSKUQuantity) {
        TotalScannedSKUQuantity = totalScannedSKUQuantity;
    }

    public String getSelectedColorCode() {
        return SelectedColorCode;

    }

    public void setSelectedColorCode(String selectedColorCode) {
        SelectedColorCode = selectedColorCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CycleCountType> getCCType() {
        return CCType;
    }

    public void setCCType(List<CycleCountType> CCType) {
        this.CCType = CCType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getBoxQty() {
        return boxQty;
    }

    public void setBoxQty(Double boxQty) {
        this.boxQty = boxQty;
    }

    public Boolean getSatisfiedBoxQty() {
        return isSatisfiedBoxQty;
    }

    public void setSatisfiedBoxQty(Boolean satisfiedBoxQty) {
        isSatisfiedBoxQty = satisfiedBoxQty;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public String getTotalSystemLocationQuantity() {
        return TotalSystemLocationQuantity;
    }

    public void setTotalSystemLocationQuantity(String totalSystemLocationQuantity) {
        TotalSystemLocationQuantity = totalSystemLocationQuantity;
    }

    public String getTotalSystemStockAllLocations() {
        return TotalSystemStockAllLocations;
    }

    public void setTotalSystemStockAllLocations(String totalSystemStockAllLocations) {
        TotalSystemStockAllLocations = totalSystemStockAllLocations;
    }

    public String getTotalNoOfLocationsToScan() {
        return TotalNoOfLocationsToScan;
    }

    public void setTotalNoOfLocationsToScan(String totalNoOfLocationsToScan) {
        TotalNoOfLocationsToScan = totalNoOfLocationsToScan;
    }

    public String getTotalNoOfLocationsScanned() {
        return TotalNoOfLocationsScanned;
    }

    public void setTotalNoOfLocationsScanned(String totalNoOfLocationsScanned) {
        TotalNoOfLocationsScanned = totalNoOfLocationsScanned;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getMRP() {
        return MRP;
    }

    public void setMRP(String MRP) {
        this.MRP = MRP;
    }

    public String getMOP() {
        return MOP;
    }

    public void setMOP(String MOP) {
        this.MOP = MOP;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public List<ColorDTO> getColorCodes() {
        return colorCodes;
    }

    public void setColorCodes(List<ColorDTO> colorCodes) {
        this.colorCodes = colorCodes;
    }

    public List<StorageLocationDTO> getSLOC() {
        return SLOC;
    }

    public void setSLOC(List<StorageLocationDTO> SLOC) {
        this.SLOC = SLOC;
    }


    public String getAccountID() {
        return AccountID;
    }

    public void setAccountID(String accountID) {
        AccountID = accountID;
    }

    public String getCount() {
        return Count;
    }

    public void setCount(String count) {
        Count = count;
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

    public String getPalletNo() {
        return PalletNo;
    }

    public void setPalletNo(String palletNo) {
        PalletNo = palletNo;
    }

    public String getCCQty() {
        return CCQty;
    }

    public void setCCQty(String CCQty) {
        this.CCQty = CCQty;
    }

    public String getProjectRefNo() {
        return ProjectRefNo;
    }

    public void setProjectRefNo(String projectRefNo) {
        ProjectRefNo = projectRefNo;
    }

    public String getCCName() {
        return CCName;
    }

    public void setCCName(String CCName) {
        this.CCName = CCName;
    }

    public String getWarehouseID() {
        return WarehouseID;
    }

    public void setWarehouseID(String warehouseID) {
        WarehouseID = warehouseID;
    }

    public String getTenantId() {
        return TenantId;
    }

    public void setTenantId(String tenantId) {
        TenantId = tenantId;
    }

    public String getRack() {
        return Rack;
    }

    public void setRack(String rack) {
        Rack = rack;
    }

    public String getColumn() {
        return Column;
    }

    public void setColumn(String column) {
        Column = column;
    }

    public String getLevel() {
        return Level;
    }

    public void setLevel(String level) {
        Level = level;
    }


    public String getStorageLocation() {
        return StorageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        StorageLocation = storageLocation;
    }

    public String getCycleCountSeqCode() {
        return CycleCountSeqCode;
    }

    public void setCycleCountSeqCode(String cycleCountSeqCode) {
        CycleCountSeqCode = cycleCountSeqCode;
    }

}