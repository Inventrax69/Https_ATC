package com.inventrax.atc.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

/**
 * Created by Padmaja.B on 6/11/2018.
 */

public class StorageLocationDTO {

    @SerializedName("SLocName")
    private String SLOCName;
    @SerializedName("SLocCode")
    private String SLOCcode;
    @SerializedName("SLocID")
    private String slocID;
    @SerializedName("IsLostAndFound")
    private boolean IsLostAndFound;
    @SerializedName("IsDefault")
    private String IsDefault;
    public StorageLocationDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "SLocName":
                    if (entry.getValue() != null) {
                        this.setSLOCName(entry.getValue().toString());
                    }
                    break;
                case "SLocCode":
                    if (entry.getValue() != null) {
                        this.setSLOCcode(entry.getValue().toString());
                    }
                    break;
                case "IsLostAndFound":
                    if (entry.getValue() != null) {
                        this.setLostAndFound(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "SLocID":
                    if (entry.getValue() != null) {
                        this.setSlocID(entry.getValue().toString().toString());
                    }
                    break;
                case "IsDefault":
                    if (entry.getValue() != null) {
                        this.setIsDefault(entry.getValue().toString().toString());
                    }
                    break;
            }
        }
    }

    public String getIsDefault() {
        return IsDefault;
    }

    public void setIsDefault(String isDefault) {
        IsDefault = isDefault;
    }

    public String getSLOCName() {
        return SLOCName;
    }

    public void setSLOCName(String SLOCName) {
        this.SLOCName = SLOCName;
    }

    public String getSLOCcode() {
        return SLOCcode;
    }

    public void setSLOCcode(String SLOCcode) {
        this.SLOCcode = SLOCcode;
    }

    public String getSlocID() {
        return slocID;
    }

    public void setSlocID(String slocID) {
        this.slocID = slocID;
    }

    public boolean isLostAndFound() {
        return IsLostAndFound;
    }

    public void setLostAndFound(boolean lostAndFound) {
        IsLostAndFound = lostAndFound;
    }
}
