package com.inventrax.atc.pojos;

import com.google.gson.annotations.SerializedName;
/**
 * Created by karthik.m on 05/31/2018.
 */

public class WMSCoreAuthentication {
    @SerializedName("AuthKey")
    private String authKey;

    @SerializedName("UserID")
    private String userId;

    @SerializedName("AuthValue")
    private String authValue;

    @SerializedName("LoginTimeStamp")
    private String loginTimeStamp;

    @SerializedName("AuthToken")
    private String authToken;

    @SerializedName("RequestNumber")
    private int requestNumber;

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuthValue() {
        return authValue;
    }

    public void setAuthValue(String authValue) {
        this.authValue = authValue;
    }

    public String getLoginTimeStamp() {
        return loginTimeStamp;
    }

    public void setLoginTimeStamp(String loginTimeStamp) {
        this.loginTimeStamp = loginTimeStamp;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public int getRequestNumber() {
        return requestNumber;
    }

    public void setRequestNumber(int requestNumber) {
        this.requestNumber = requestNumber;
    }
}