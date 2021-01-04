package com.inventrax.atc.pojos;
import com.inventrax.atc.common.constants.EndpointConstants;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by karthik.m on 05/31/2018.
 */

public class WMSCoreMessage {
    @SerializedName("AuthToken")
    @Expose
    private WMSCoreAuthentication authToken;
    @SerializedName("EntityObject")
    @Expose
    private Object entityObject;
    @SerializedName("Type")
    @Expose
    private EndpointConstants Type;
    @SerializedName("WMSMessages")
    @Expose
     private List<WMSExceptionMessage> WMSMessages;

    public List<WMSExceptionMessage> getWMSMessages() {
        return WMSMessages;
    }

    public void setWMSMessages(List<WMSExceptionMessage> WMSMessages) {
        this.WMSMessages = WMSMessages;
    }

    public EndpointConstants getType() {
        return Type;
    }

    public void setType(EndpointConstants type) {
        Type = type;
    }

    public WMSCoreAuthentication getAuthToken() {
        return authToken;
    }

    public void setAuthToken(WMSCoreAuthentication authToken) {
        this.authToken = authToken;
    }

    public Object getEntityObject() {
        return entityObject;
    }

    public void setEntityObject(Object entityObject) {
        this.entityObject = entityObject;
    }
}
