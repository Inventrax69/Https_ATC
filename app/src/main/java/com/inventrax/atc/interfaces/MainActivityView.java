package com.inventrax.atc.interfaces;

/**
 * Created by android on 3/15/2016.
 */
public interface MainActivityView {

    void showSKUListStatus(String message, int status);

    void showOutletListStatus(String message, int status);

    void showDiscountListStatus(String message, int status);

    void showDeliveryListStatus(String message, int status);

    void showLoadOutListStatus(String message, int status);


}
