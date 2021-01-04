package com.inventrax.atc.model;

import java.io.Serializable;

public class Model implements Serializable {

    private String mCode;
    private String pickedQty;
    private String pickedId;
    private boolean isSelected;

    public Model(String mCode, String pickedQty, String pickedId, boolean isSelected) {
        this.mCode = mCode;
        this.pickedQty =pickedQty;
        this.pickedId =pickedId;
        this.isSelected = isSelected;
    }

    public String getmCode() {
        return mCode;
    }

    public void setmCode(String mCode) {
        this.mCode = mCode;
    }

    public String getPickedQty() {
        return pickedQty;
    }

    public void setPickedQty(String pickedQty) {
        this.pickedQty = pickedQty;
    }

    public String getPickedId() {
        return pickedId;
    }

    public void setPickedId(String pickedId) {
        this.pickedId = pickedId;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}