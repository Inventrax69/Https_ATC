package com.inventrax.atc.pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

/**
 * Created by karthik.m on 06/19/2018.
 */

public class WMSExceptionMessage {
    @SerializedName("WMSMessage")
    private String WMSMessage;
    @SerializedName("WMSExceptionCode ")
    private String WMSExceptionCode;
    @SerializedName("ShowAsError")
    private boolean ShowAsError;
    @SerializedName("ShowAsWarning")
    private boolean ShowAsWarning;
    @SerializedName("ShowAsSuccess")
    private boolean ShowAsSuccess;
    @SerializedName("ShowAsCriticalError")
    private boolean ShowAsCriticalError;
    @SerializedName("ShowUserConfirmDialogue")
    private boolean ShowUserConfirmDialogue;
    public WMSExceptionMessage()
    {

    }
    public WMSExceptionMessage(Set<? extends Map.Entry<?, ?>> entries) {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "WMSMessage":
                    if (entry.getValue() != null) {
                        this.setWMSMessage(entry.getValue().toString());
                    }
                    break;
                case "ShowAsError":
                    if (entry.getValue() != null) {
                        this.setShowAsError(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "ShowAsWarning":
                    if (entry.getValue() != null) {
                        this.setShowAsWarning(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "ShowAsSuccess":
                    if (entry.getValue() != null) {
                        this.setShowAsSuccess(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;

                case "ShowAsCriticalError":
                    if (entry.getValue() != null) {
                        this.setShowAsCriticalError(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "ShowUserConfirmDialogue":
                    if (entry.getValue() != null) {
                        this.setShowUserConfirmDialogue(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "WMSExceptionCode":
                    if (entry.getValue() != null) {
                        this.setWMSExceptionCode(entry.getValue().toString());
                    }
                    break;
            }
        }
    }

    public boolean isShowAsError() {
        return ShowAsError;
    }

    public void setShowAsError(boolean showAsError) {
        ShowAsError = showAsError;
    }

    public boolean isShowAsWarning() {
        return ShowAsWarning;
    }

    public void setShowAsWarning(boolean showAsWarning) {
        ShowAsWarning = showAsWarning;
    }

    public boolean isShowAsSuccess() {
        return ShowAsSuccess;
    }

    public void setShowAsSuccess(boolean showAsSuccess) {
        ShowAsSuccess = showAsSuccess;
    }

    public boolean isShowAsCriticalError() {
        return ShowAsCriticalError;
    }

    public void setShowAsCriticalError(boolean showAsCriticalError) {
        ShowAsCriticalError = showAsCriticalError;
    }

    public boolean isShowUserConfirmDialogue() {
        return ShowUserConfirmDialogue;
    }

    public void setShowUserConfirmDialogue(boolean showUserConfirmDialogue) {
        ShowUserConfirmDialogue = showUserConfirmDialogue;
    }

    public String getWMSMessage() {
        return WMSMessage;
    }

    public void setWMSMessage(String WMSMessage) {
        this.WMSMessage = WMSMessage;
    }

    public String getWMSExceptionCode() {
        return WMSExceptionCode;
    }

    public void setWMSExceptionCode(String WMSExceptionCode) {
        this.WMSExceptionCode = WMSExceptionCode;
    }

}
