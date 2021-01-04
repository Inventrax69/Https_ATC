package com.inventrax.atc.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

/**
 * Created by Prasanna.Ch on 5/15/2019.
 */

public class EntryDTO {

    @SerializedName("DockNumber")
    private String DockNumber;

    @SerializedName("VehicleNumber")
    private String VehicleNumber;

    @SerializedName("DockID")
    private String DockID;

    public EntryDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "DockNumber":
                    if (entry.getValue() != null) {
                        this.setDockNumber(entry.getValue().toString());
                    }
                    break;
                case "VehicleNumber":
                    if (entry.getValue() != null) {
                        this.setVehicleNumber(entry.getValue().toString());
                    }
                    break;
                case "DockID":
                    if (entry.getValue() != null) {
                        this.setDockID(entry.getValue().toString());
                    }
                    break;
            }
        }
    }


    public String getDockNumber() {
        return DockNumber;
    }

    public void setDockNumber(String dockNumber) {
        DockNumber = dockNumber;
    }

    public String getVehicleNumber() {
        return VehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        VehicleNumber = vehicleNumber;
    }

    public String getDockID() {
        return DockID;
    }

    public void setDockID(String dockID) {
        DockID = dockID;
    }
}
