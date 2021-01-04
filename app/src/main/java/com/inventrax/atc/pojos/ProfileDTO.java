package com.inventrax.atc.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Map;
import java.util.Set;


/**
 * Created by karthik.m on 05/31/2018.
 */

public class ProfileDTO {

    @SerializedName("UserName")
    private String userName;
    @SerializedName("UserID")
    private String userID;
    @SerializedName("UserTypeID")
    private int UserTypeID;
    @SerializedName("UserType")
    private String userType;
    @SerializedName("SessionIdentifier")
    private String sessionIdentifier;
    @SerializedName("CookieIdentifier")
    private String cookieIdentifier;
    @SerializedName("ClientIP")
    private String clientIP;
    @SerializedName("ClientMAC")
    private String clientMAC;
    @SerializedName("LoginTimeStamp")
    private Date loginTimeStamp;
    @SerializedName("LastRequestTimestamp")
    private Date lastRequestTimestamp;
    @SerializedName("UserRoleID")
    private int userRoleID;
    @SerializedName("UserRole")
    private String userRole;
    @SerializedName("WarehouseID")
    private Integer warehouseID;
    @SerializedName("TenantID")
    private Integer TenantID;
    @SerializedName("IsLoggedIn")
    private Boolean isLoggedIn;
    @SerializedName("AccountId")
    private String accountId;
    @SerializedName("FirstName")
    private String firstName;
    @SerializedName("LastName")
    private String lastName;


    public ProfileDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "UserName":
                    if (entry.getValue() != null) {
                        this.setUserName(entry.getValue().toString());
                    }
                    break;
                case "UserID":
                    if (entry.getValue() != null) {
                        this.setUserID(entry.getValue().toString());
                    }
                    break;
                case "UserType":
                    if (entry.getValue() != null) {
                        this.setUserType(entry.getValue().toString());
                    }
                    break;

                case "AccountId":
                    if (entry.getValue() != null) {
                        this.setAccountId(entry.getValue().toString());
                    }
                    break;
                case "FirstName":
                    if (entry.getValue() != null) {
                        this.setFirstName(entry.getValue().toString());
                    }
                    break;

                case "LastName":
                    if (entry.getValue() != null) {
                        this.setLastName(entry.getValue().toString());
                    }
                    break;

                case "WarehouseID":
                    if (entry.getValue() != null) {
                        this.setWarehouseID((int)Double.parseDouble(entry.getValue().toString()));
                    }
                    break;
                case "UserRole":
                    if (entry.getValue() != null) {
                        this.setUserRole(entry.getValue().toString());
                    }
                    break;

                case "TenantID":
                    if (entry.getValue() != null) {
                        this.setTenantID((int)Double.parseDouble(entry.getValue().toString()));
                    }
                    break;

            }
        }
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getUserTypeID() {
        return UserTypeID;
    }

    public void setUserTypeID(int userTypeID) {
        UserTypeID = userTypeID;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getSessionIdentifier() {
        return sessionIdentifier;
    }

    public void setSessionIdentifier(String sessionIdentifier) {
        this.sessionIdentifier = sessionIdentifier;
    }

    public String getCookieIdentifier() {
        return cookieIdentifier;
    }

    public void setCookieIdentifier(String cookieIdentifier) {
        this.cookieIdentifier = cookieIdentifier;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public String getClientMAC() {
        return clientMAC;
    }

    public void setClientMAC(String clientMAC) {
        this.clientMAC = clientMAC;
    }

    public Date getLoginTimeStamp() {
        return loginTimeStamp;
    }

    public void setLoginTimeStamp(Date loginTimeStamp) {
        this.loginTimeStamp = loginTimeStamp;
    }

    public Date getLastRequestTimestamp() {
        return lastRequestTimestamp;
    }

    public void setLastRequestTimestamp(Date lastRequestTimestamp) {
        this.lastRequestTimestamp = lastRequestTimestamp;
    }

    public int getUserRoleID() {
        return userRoleID;
    }

    public void setUserRoleID(int userRoleID) {
        this.userRoleID = userRoleID;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public Integer getWarehouseID() {
        return warehouseID;
    }

    public void setWarehouseID(Integer warehouseID) {
        this.warehouseID = warehouseID;
    }

    public Boolean getLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(Boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public Integer getTenantID() {
        return TenantID;
    }

    public void setTenantID(Integer tenantID) {
        TenantID = tenantID;
    }
}
