/***
 * Copyright (c) 2012 CommonsWare, LLC
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.inventrax.atc.services.appupdate;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Parcel;

/**
 * Implementation of ConfirmationStrategy that simply
 * returns true from confirm(), for cases where user
 * confirmation is not necessary.
 */
public class ImmediateConfirmationStrategy implements
        ConfirmationStrategy {
    /**
     * Required to complete Parcelable interface. Creates
     * an ImmediateConfirmationStrategy instance or array
     * upon demand.
     */
    public static final Creator<ImmediateConfirmationStrategy> CREATOR =
            new Creator<ImmediateConfirmationStrategy>() {
                public ImmediateConfirmationStrategy createFromParcel(Parcel in) {
                    return (new ImmediateConfirmationStrategy());
                }

                public ImmediateConfirmationStrategy[] newArray(int size) {
                    return (new ImmediateConfirmationStrategy[size]);
                }
            };

    /* (non-Javadoc)
     * @see com.commonsware.cwac.updater.ConfirmationStrategy#confirm(android.content.Context, android.app.PendingIntent)
     */
    @Override
    public boolean confirm(Context ctxt, PendingIntent contentIntent) {
        return (true);
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return (0);
    }

    /* (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // no-op
    }
}
