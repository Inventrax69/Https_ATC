package com.inventrax.atc.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

/**
 * Created by Padmaja.B on 6/11/2018.
 */

public class ColorDTO {
    @SerializedName("ColourCode")
    private String colorcode;
    @SerializedName("ColourID")
    private String colorId;
    @SerializedName("ColourName")
    private String colourName;

    public ColorDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "ColourCode":
                    if (entry.getValue() != null) {
                        this.setColorcode(entry.getValue().toString());
                    }
                    break;
                case "ColourID":
                    if (entry.getValue() != null) {
                        this.setColorId(entry.getValue().toString());
                    }
                    break;
                case "ColourName":
                    if (entry.getValue() != null) {
                        this.setColourName(entry.getValue().toString());
                    }
                    break;
            }
        }
    }

    public String getColourName() {
        return colourName;
    }

    public void setColourName(String colourName) {
        this.colourName = colourName;
    }

    public String getColorcode() {
        return colorcode;
    }

    public void setColorcode(String colorcode) {
        this.colorcode = colorcode;
    }

    public String getColorId() {
        return colorId;
    }

    public void setColorId(String colorId) {
        this.colorId = colorId;
    }
}
