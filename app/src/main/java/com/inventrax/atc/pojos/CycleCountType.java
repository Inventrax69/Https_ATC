package com.inventrax.atc.pojos;
import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;
/**
 * Created by karthik.m on 07/20/2018.
 */

public class CycleCountType
{
    @SerializedName("CCName")
    private String CCName;
    @SerializedName("IsCCForStockTake")
    private Boolean isCCForStockTake;

    public CycleCountType()
    {

    }

    public CycleCountType(Set<? extends Map.Entry<?, ?>> entries) {
            for (Map.Entry<?, ?> entry : entries) {

                switch (entry.getKey().toString()) {

                    case  "CCName" :
                        if(entry.getValue()!=null) {
                            this.setCCName(entry.getValue().toString());
                        }
                        break;

                    case  "IsCCForStockTake" :
                        if(entry.getValue()!=null) {
                            this.setCCForStockTake(Boolean.parseBoolean(entry.getValue().toString()));
                        }
                        break;
                }
            }
        }

    public String getCCName() {
        return CCName;
    }

    public void setCCName(String CCName) {
        this.CCName = CCName;
    }

    public Boolean getCCForStockTake() {
        return isCCForStockTake;
        }
    public void setCCForStockTake(Boolean CCForStockTake) {
            isCCForStockTake = CCForStockTake;
        }
    }

