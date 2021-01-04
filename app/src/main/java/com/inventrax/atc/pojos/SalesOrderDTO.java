package com.inventrax.atc.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class SalesOrderDTO {

    @SerializedName("SOHeaderID")
    public String SOHeaderID;

    @SerializedName("SONumber")
    public String SONumber;

    public SalesOrderDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "SOHeaderID":
                    if (entry.getValue() != null) {
                        this.setSOHeaderID(entry.getValue().toString());
                    }
                    break;

                case "SONumber":
                    if (entry.getValue() != null) {
                        this.setSONumber(entry.getValue().toString());
                    }
                    break;

            }
        }
    }

    public String getSOHeaderID() {
        return SOHeaderID;
    }

    public void setSOHeaderID(String SOHeaderID) {
        this.SOHeaderID = SOHeaderID;
    }

    public String getSONumber() {
        return SONumber;
    }

    public void setSONumber(String SONumber) {
        this.SONumber = SONumber;
    }


}
