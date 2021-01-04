package com.inventrax.atc.pojos;

import com.google.gson.annotations.SerializedName;

/**
 * Created by karthik.m on 05/31/2018.
 */

public class LoginUserDTO {

    @SerializedName("MailID")
    private String mailID;
    @SerializedName("PasswordEncrypted")
    private String passwordEncrypted;
    @SerializedName("ClientMAC")
    private String clientMAC;
    @SerializedName("SessionIdentifier")
    private String sessionIdentifier;
    @SerializedName("CookieIdentifier")
    private String cookieIdentifier;

    public String getMailID() {
        return mailID;
    }

    public void setMailID(String mailID) {
        this.mailID = mailID;
    }

    public String getPasswordEncrypted() {
        return passwordEncrypted;
    }

    public void setPasswordEncrypted(String passwordEncrypted) {
        this.passwordEncrypted = passwordEncrypted;
    }

    public String getClientMAC() {
        return clientMAC;
    }

    public void setClientMAC(String clientMAC) {
        this.clientMAC = clientMAC;
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
}
