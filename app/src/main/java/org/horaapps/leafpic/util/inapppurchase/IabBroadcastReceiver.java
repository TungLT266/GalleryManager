
package org.horaapps.leafpic.util.inapppurchase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class IabBroadcastReceiver extends BroadcastReceiver {

    public interface IabBroadcastListener {
        void receivedBroadcast();
    }


    public static final String ACTION = "com.android.vending.billing.PURCHASES_UPDATED";

    private final IabBroadcastListener mListener;

    public IabBroadcastReceiver(IabBroadcastListener listener) {
        mListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mListener != null) {
            mListener.receivedBroadcast();
        }
    }
}
