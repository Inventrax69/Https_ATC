package com.inventrax.atc.pojos;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Padmaja.B on 20/12/2018.
 */

public class InboundDTO {
    @SerializedName("UserId")
    private String UserId;
    @SerializedName("InboundID")
    private String InboundID;
    @SerializedName("StoreRefNo")
    private String StoreRefNo;
    @SerializedName("PalletNo")
    private String PalletNo;
    @SerializedName("AccountID")
    private String AccountID;
    @SerializedName("StorageLocation")
    private String StorageLocation;
    @SerializedName("Result")
    private String Result;
    @SerializedName("Mcode")
    private String Mcode;
    @SerializedName("ReceivedQty")
    private String ReceivedQty;
    @SerializedName("ItemPendingQty")
    private String ItemPendingQty;
    @SerializedName("BatchNo")
    private String BatchNo;
    @SerializedName("SerialNo")
    private String SerialNo;
    @SerializedName("MfgDate")
    private String MfgDate;
    @SerializedName("ExpDate")
    private String ExpDate;
    @SerializedName("ProjectRefno")
    private String ProjectRefno;
    @SerializedName("Qty")
    private String Qty;
    @SerializedName("LineNo")
    private String LineNo;
    @SerializedName("HasDisc")
    private String HasDisc;
    @SerializedName("CartonNo")
    private String CartonNo;
    @SerializedName("CreatedBy")
    private String CreatedBy;
    @SerializedName("IsDam")
    private String IsDam;
    @SerializedName("SkipType")
    private String SkipType;
    @SerializedName("SkipReason")
    private String SkipReason;

    @SerializedName("InvoiceQty")
    private String InvoiceQty;

    @SerializedName("IsOutbound")
    private String isOutbound;
    @SerializedName("MRP")
    private String MRP;
    @SerializedName("Dock")
    private String Dock;

    @SerializedName("Entry")
    private List<EntryDTO> Entry;

    @SerializedName("VehicleNo")
    private String VehicleNo;

    @SerializedName("SupplierInvoiceDetailsID")
    public String SupplierInvoiceDetailsID;

    @SerializedName("HUNo")
    public String HUNo;

    @SerializedName("HUSize")
    public String HUSize;

    public InboundDTO() {
    }


    public InboundDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

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

                case "UserId":
                    if (entry.getValue() != null) {
                        this.setUserId(entry.getValue().toString());
                    }
                    break;
                case "InboundID":
                    if (entry.getValue() != null) {
                        this.setInboundID(entry.getValue().toString());
                    }
                    break;
                case "StoreRefNo":
                    if (entry.getValue() != null) {
                        this.setStoreRefNo(entry.getValue().toString());
                    }
                    break;
                case "PalletNo":
                    if (entry.getValue() != null) {
                        this.setPalletNo(entry.getValue().toString());
                    }
                    break;
                case "AccountID":
                    if (entry.getValue() != null) {
                        this.setAccountID(entry.getValue().toString());
                    }
                    break;
                case "StorageLocation":
                    if (entry.getValue() != null) {
                        this.setStorageLocation(entry.getValue().toString());
                    }
                    break;
                case "Result":
                    if (entry.getValue() != null) {
                        this.setResult(entry.getValue().toString());
                    }
                    break;
                case "Mcode":
                    if (entry.getValue() != null) {
                        this.setMcode(entry.getValue().toString());
                    }
                    break;
                case "ReceivedQty":
                    if (entry.getValue() != null) {
                        this.setReceivedQty(entry.getValue().toString());
                    }
                    break;
                case "ItemPendingQty":
                    if (entry.getValue() != null) {
                        this.setItemPendingQty(entry.getValue().toString());
                    }
                    break;
                case "BatchNo":
                    if (entry.getValue() != null) {
                        this.setBatchNo(entry.getValue().toString());
                    }
                    break;
                case "SerialNo":
                    if (entry.getValue() != null) {
                        this.setSerialNo(entry.getValue().toString());
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
                case "ProjectRefno":
                    if (entry.getValue() != null) {
                        this.setProjectRefno(entry.getValue().toString());
                    }
                    break;
                case "Qty":
                    if (entry.getValue() != null) {
                        this.setQty(entry.getValue().toString());
                    }
                    break;
                case "LineNo":
                    if (entry.getValue() != null) {
                        this.setLineNo(entry.getValue().toString());
                    }
                    break;
                case "HasDisc":
                    if (entry.getValue() != null) {
                        this.setHasDisc(entry.getValue().toString());
                    }
                    break;
                case "CartonNo":
                    if (entry.getValue() != null) {
                        this.setCartonNo(entry.getValue().toString());
                    }
                    break;
                case "CreatedBy":
                    if (entry.getValue() != null) {
                        this.setCreatedBy(entry.getValue().toString());
                    }
                    break;
                case "IsDam":
                    if (entry.getValue() != null) {
                        this.setIsDam(entry.getValue().toString());
                    }
                    break;
                case "SkipType":
                    if (entry.getValue() != null) {
                        this.setSkipType(entry.getValue().toString());
                    }
                    break;
                case "SkipReason":
                    if (entry.getValue() != null) {
                        this.setSkipReason(entry.getValue().toString());
                    }
                    break;

                case "InvoiceQty":
                    if (entry.getValue() != null) {
                        this.setInvoiceQty(entry.getValue().toString());
                    }
                    break;

                case "IsOutbound":
                    if (entry.getValue() != null) {
                        this.setIsOutbound(entry.getValue().toString());
                    }
                    break;

                case "MRP":
                    if (entry.getValue() != null) {
                        this.setMRP(entry.getValue().toString());
                    }
                    break;
                case "Dock":
                    if (entry.getValue() != null) {
                        this.setDock(entry.getValue().toString());
                    }
                    break;
                case "VehicleNo":
                    if (entry.getValue() != null) {
                        this.setVehicleNo(entry.getValue().toString());
                    }
                    break;

                case "Entry":
                    if (entry.getValue() != null) {
                        List<LinkedTreeMap<?, ?>> entryLst = (List<LinkedTreeMap<?, ?>>) entry.getValue();
                        List<EntryDTO> lstentry = new ArrayList<EntryDTO>();
                        for (int i = 0; i < entryLst.size(); i++) {
                            EntryDTO dto = new EntryDTO(entryLst.get(i).entrySet());
                            lstentry.add(dto);
                            //Log.d("Message", core.getEntityObject().toString());

                        }

                        this.setEntry(lstentry);
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

    public String getInvoiceQty() {
        return InvoiceQty;
    }

    public void setInvoiceQty(String invoiceQty) {
        InvoiceQty = invoiceQty;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getInboundID() {
        return InboundID;
    }

    public void setInboundID(String inboundID) {
        InboundID = inboundID;
    }

    public String getStoreRefNo() {
        return StoreRefNo;
    }

    public void setStoreRefNo(String storeRefNo) {
        StoreRefNo = storeRefNo;
    }

    public String getPalletNo() {
        return PalletNo;
    }

    public void setPalletNo(String palletNo) {
        PalletNo = palletNo;
    }

    public String getAccountID() {
        return AccountID;
    }

    public void setAccountID(String accountID) {
        AccountID = accountID;
    }

    public String getStorageLocation() {
        return StorageLocation;
    }

    public void setStorageLocation(String storageLocation) {
        StorageLocation = storageLocation;
    }

    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }

    public String getMcode() {
        return Mcode;
    }

    public void setMcode(String mcode) {
        Mcode = mcode;
    }

    public String getReceivedQty() {
        return ReceivedQty;
    }

    public void setReceivedQty(String receivedQty) {
        ReceivedQty = receivedQty;
    }

    public String getItemPendingQty() {
        return ItemPendingQty;
    }

    public void setItemPendingQty(String itemPendingQty) {
        ItemPendingQty = itemPendingQty;
    }

    public String getBatchNo() {
        return BatchNo;
    }

    public void setBatchNo(String batchNo) {
        BatchNo = batchNo;
    }

    public String getSerialNo() {
        return SerialNo;
    }

    public void setSerialNo(String serialNo) {
        SerialNo = serialNo;
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

    public String getProjectRefno() {
        return ProjectRefno;
    }

    public void setProjectRefno(String projectRefno) {
        ProjectRefno = projectRefno;
    }

    public String getQty() {
        return Qty;
    }

    public void setQty(String qty) {
        Qty = qty;
    }

    public String getLineNo() {
        return LineNo;
    }

    public void setLineNo(String lineNo) {
        LineNo = lineNo;
    }

    public String getHasDisc() {
        return HasDisc;
    }

    public void setHasDisc(String hasDisc) {
        HasDisc = hasDisc;
    }

    public String getCartonNo() {
        return CartonNo;
    }

    public void setCartonNo(String cartonNo) {
        CartonNo = cartonNo;
    }

    public String getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(String createdBy) {
        CreatedBy = createdBy;
    }

    public String getIsDam() {
        return IsDam;
    }

    public void setIsDam(String isDam) {
        IsDam = isDam;
    }

    public String getSkipType() {
        return SkipType;
    }

    public void setSkipType(String skipType) {
        SkipType = skipType;
    }

    public String getSkipReason() {
        return SkipReason;
    }

    public void setSkipReason(String skipReason) {
        SkipReason = skipReason;
    }

    public String getIsOutbound() {
        return isOutbound;
    }

    public void setIsOutbound(String isOutbound) {
        this.isOutbound = isOutbound;
    }

    public String getMRP() {
        return MRP;
    }

    public void setMRP(String MRP) {
        this.MRP = MRP;
    }

    public String getDock() {
        return Dock;
    }

    public void setDock(String dock) {
        Dock = dock;
    }

    public List<EntryDTO> getEntry() {
        return Entry;
    }

    public void setEntry(List<EntryDTO> entry) {
        Entry = entry;
    }

    public String getVehicleNo() {
        return VehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        VehicleNo = vehicleNo;
    }

    public String getSupplierInvoiceDetailsID() {
        return SupplierInvoiceDetailsID;
    }

    public void setSupplierInvoiceDetailsID(String supplierInvoiceDetailsID) {
        SupplierInvoiceDetailsID = supplierInvoiceDetailsID;
    }

}