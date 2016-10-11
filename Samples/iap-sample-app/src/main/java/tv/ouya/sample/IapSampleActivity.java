/*
 * Copyright (C) 2012-2016 Razer, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.ouya.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.razerzone.store.sdk.CancelIgnoringResponseListener;
import com.razerzone.store.sdk.ErrorCodes;
import com.razerzone.store.sdk.GamerInfo;
import com.razerzone.store.sdk.PurchaseResult;
import com.razerzone.store.sdk.ResponseListener;
import com.razerzone.store.sdk.StoreFacade;
import com.razerzone.store.sdk.purchases.Product;
import com.razerzone.store.sdk.purchases.Purchasable;
import com.razerzone.store.sdk.purchases.Receipt;
import org.json.JSONException;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.security.GeneralSecurityException;
import java.security.InvalidParameterException;
import java.util.*;

import static com.razerzone.store.sdk.Controller.BUTTON_O;

public class IapSampleActivity extends Activity {

    /**
     * The tag for log messages
     */
    private static final String LOG_TAG = "RazerIapSample";

    private static final boolean sEnableLogging = true;

    // Both of these values will be emailed to you by the Razer team after you've been
    // selected for Xiaomi publishing
    public static final String XIAOMI_APP_ID = "2882303761517238170";
    public static final String XIAOMI_APP_KEY = "5571723882170";

    /**
     * The secret api key. This is used to decrypt encrypted receipt responses. This should be replaced with the
     * secret api key obtained from the Razer developers website.
     */

    private static final String SECRET_API_KEY =
            "eyAgDQogICAiZGV2ZWxvcGVyX2lk" +
                    "IjoiMzEwYThmNTEtNGQ2ZS00YWU1" +
                    "LWJkYTAtYjkzODc4ZTVmNWQwIiwN" +
                    "CiAgICJkZXZlbG9wZXJfcHVibGlj" +
                    "X2tleSI6Ik1JR2ZNQTBHQ1NxR1NJ" +
                    "YjNEUUVCQVFVQUE0R05BRENCaVFL" +
                    "QmdRRGN2bDlERkVpeHN3MHZmV2tD" +
                    "MnE0WnpRL0ljRmh5TVBYUkdPcVlQ" +
                    "VkE4eTdJYjkyVk1zSUlPUThSbldB" +
                    "VVkrVVVneXhSS3Q2ZFZnMFZ1WFpQ" +
                    "MzRsMk9PL09UYkREZ0U5VWg4U0dR" +
                    "cE8wSFVYaFdwc3NWbVViRXVNZTky" +
                    "QnlIZHNoVWhTcFJ6MXZTeHpxOUcw" +
                    "SVJaYTRYdTFYMFdGU2YrbGxjdjly" +
                    "K3ZqVEd3d0lEQVFBQiINCn0=";

    /**
     * Before this app will run, you must define some purchasable items on the developer website. Once
     * you have defined those items, put their Product IDs in the List below.
     * <p/>
     * The Product IDs below are those in our developer account. You should change them.
     * <p/>
     * A String array of all possible product IDs.
     */
    public static final String[] ALL_PRODUCT_IDENTIFIERS = new String[] {
            "long_sword",
            "sharp_axe",
            "awesome_sauce",
            "cat_facts",
            "__DECLINED__THIS_PURCHASE"
    };

   /**
     * The receipt adapter will display a previously-purchased item in a cell in a ListView. It's not part of the in-app
     * purchase API. Neither is the ListView itself.
     */
    private ListView mReceiptListView;

    /**
     * Your game talks to the StoreFacade, which hides all the mechanics of doing an in-app purchase.
     */
    private StoreFacade mStoreFacade;

    private List<Product> mProductList;
    private List<Receipt> mReceiptList;

    // listener for shutdown
    private CancelIgnoringResponseListener mShutdownListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle developerInfo = null;
        try {
            developerInfo = StoreFacade.createInitBundle(SECRET_API_KEY);
        } catch (InvalidParameterException e) {
            Log.e(LOG_TAG, e.getMessage());
            finish();
        }

        if (sEnableLogging) {
            Log.d(LOG_TAG, "developer_id=" + developerInfo.getString(StoreFacade.DEVELOPER_ID));
        }

        if (sEnableLogging) {
            Log.d(LOG_TAG, "developer_public_key length=" + developerInfo.getByteArray(StoreFacade.DEVELOPER_PUBLIC_KEY).length);
        }

        developerInfo.putStringArray(StoreFacade.PRODUCT_ID_LIST, ALL_PRODUCT_IDENTIFIERS);

        // "com.xiaomi.app_id"
        developerInfo.putString(StoreFacade.XIAOMI_APPLICATION_ID, XIAOMI_APP_ID);
        // "com.xiaomi.app_key"
        developerInfo.putString(StoreFacade.XIAOMI_APPLICATION_KEY, XIAOMI_APP_KEY);

        mStoreFacade = StoreFacade.getInstance();

        mShutdownListener = new CancelIgnoringResponseListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(LOG_TAG, "ShutdownListener onSuccess: finishing activity...");
                finish();
            }

            @Override
            public void onFailure(int errorCode, String message, Bundle bundle) {
                Log.e(LOG_TAG, "ShutdownListener onFailure failed to shutdown! errorCode="+errorCode+" message="+message);
            }
        };

        mStoreFacade.init(this, developerInfo, new CancelIgnoringResponseListener<Bundle>() {
            @Override
            public void onSuccess(Bundle bundle) {

                if (sEnableLogging) {
                    Log.d(LOG_TAG, "init listener: onSuccess");
                }

                // Request the product list if it could not be restored from the savedInstanceState Bundle
                if(mProductList == null) {
                    requestProducts();
                }
            }

            @Override
            public void onFailure(int errorCode, String errorMessage, Bundle bundle) {
                Log.e(LOG_TAG, "init listener: onFailure errorCode="+errorCode+" errorMessage="+errorMessage);
            }
        });

        setContentView(R.layout.sample_app);

        Button btnLogin = (Button) findViewById(R.id.login_button);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestLogin();
            }
        });

        Button btnProducts = (Button) findViewById(R.id.products_button);
        btnProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestProducts();
            }
        });

        Button btnReceipts = (Button) findViewById(R.id.receipts_button);
        btnReceipts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestReceipts();
            }
        });

        TextView txtVersionCode = (TextView) findViewById(R.id.txtVersionCodeVal);
        TextView txtVersionName = (TextView) findViewById(R.id.txtVersionNameVal);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String strVersionCode = Integer.toString(pInfo.versionCode);
            txtVersionCode.setText(strVersionCode);
            txtVersionName.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
        }

        mReceiptListView = (ListView) findViewById(R.id.receipts);
        mReceiptListView.setFocusable(false);

        /*
         * In order to avoid "application not responding" popups, Android demands that long-running operations
         * happen on a background thread. Listener objects provide a way for you to specify what ought to happen
         * at the end of the long-running operation. Examples of this pattern in Android include
         * android.os.AsyncTask.
         */
        findViewById(R.id.gamer_uuid_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestGamerInfo();
            }
        });

        // Make sure the receipt ListView starts empty if the receipt list could not be restored
        // from the savedInstanceState Bundle.
        if(mReceiptList == null) {
            mReceiptListView.setAdapter(new ReceiptAdapter(this, new Receipt[0]));
        }
    }

    /**
     * Check for the result from a call through to the authentication intent. If the authentication was
     * successful then re-try the purchase.
     */
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d(LOG_TAG, "Processing activity result");

        // Forward this result to the facade, in case it is waiting for any activity results
        if(mStoreFacade.processActivityResult(requestCode, resultCode, data)) {
            Log.d(LOG_TAG, "mStoreFacade processed activity result");
            return;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        int keyCode = keyEvent.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(LOG_TAG, "Back detected, shutting down...");
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    mStoreFacade.shutdown(mShutdownListener);
                }
            };
            runOnUiThread(runnable);

            return true;
        } else {
            return super.dispatchKeyEvent(keyEvent);
        }
    }

    private synchronized void requestLogin() {
        if (sEnableLogging) {
            Log.d(LOG_TAG, "requestLogin:");
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mStoreFacade.requestLogin(IapSampleActivity.this, new ResponseListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (sEnableLogging) {
                            Log.d(LOG_TAG, "requestLogin listener: onSuccess");
                        }
                        Toast.makeText(IapSampleActivity.this, "requestLogin listener: onSuccess", Toast.LENGTH_LONG).show();
                        // Request an up to date list of receipts for the user.
                        requestReceipts();
                    }

                    @Override
                    public void onFailure(int errorCode, String errorMessage, Bundle bundle) {
                        Log.e(LOG_TAG, "requestLogin listener: onFailure errorCode="+errorCode+" errorMessage="+errorMessage);
                        Toast.makeText(IapSampleActivity.this, "requestLogin listener: onFailure errorCode=" + errorCode + " errorMessage=" + errorMessage, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel() {
                        Log.e(LOG_TAG, "requestLogin listener: onCancel");
                        Toast.makeText(IapSampleActivity.this, "requestLogin listener: onCancel", Toast.LENGTH_LONG).show();
                    }
                });
            }
        };

        runOnUiThread(runnable);
    }

    /**
     * Get the list of products the user can purchase from the server.
     */
    public synchronized void requestProducts() {
        Log.d(LOG_TAG, "Requesting products");
        mStoreFacade.requestProductList(this, ALL_PRODUCT_IDENTIFIERS, new ResponseListener<List<Product>>() {
            @Override
            public void onSuccess(final List<Product> products) {
                Toast.makeText(IapSampleActivity.this, "requestProducts listener: onSuccess!", Toast.LENGTH_LONG).show();
                mProductList = products;
                addProducts();
            }

            @Override
            public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
                // Your app probably wants to do something more sophisticated than popping a Toast. This is
                // here to tell you that your app needs to handle this case: if your app doesn't display
                // something, the user won't know of the failure.
                Toast.makeText(IapSampleActivity.this, "requestProducts listener: onFailure errorCode=" + errorCode + " errorMessage=" + errorMessage, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(IapSampleActivity.this, "requestProducts listener: onCancel!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private synchronized void requestGamerInfo() {
        if (sEnableLogging) {
            Log.d(LOG_TAG, "requestGamerinfo:");
        }
        mStoreFacade.requestGamerInfo(this, new ResponseListener<GamerInfo>() {
            @Override
            public void onSuccess(GamerInfo result) {
                if (sEnableLogging) {
                    Log.d(LOG_TAG, "requestGamerInfo onSuccess");
                }
                Toast.makeText(IapSampleActivity.this, "requestGamerInfo listener: onSuccess!", Toast.LENGTH_LONG).show();
                new AlertDialog.Builder(IapSampleActivity.this)
                        .setTitle(getString(R.string.alert_title))
                        .setMessage(getResources().getString(R.string.userinfo, result.getUsername(), result.getUuid()))
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }

            @Override
            public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
                Log.e(LOG_TAG, "requestGamerInfo listener: onFailure errorCode="+errorCode+" errorMessage="+errorMessage);
                Toast.makeText(IapSampleActivity.this, "requestGamerInfo listener: onFailure errorCode="+errorCode+" errorMessage="+errorMessage, Toast.LENGTH_LONG).show();
                requestLogin();
            }

            @Override
            public void onCancel() {
                Log.e(LOG_TAG, "requestGamerInfo listener: onCancel");
                Toast.makeText(IapSampleActivity.this, "requestGamerInfo listener: onCancel!", Toast.LENGTH_LONG).show();
                requestLogin();
            }
        });
    }

    /**
     * Request the receipts from the users previous purchases from the server.
     */
    private synchronized void requestReceipts() {
        Log.d(LOG_TAG, "Requesting receipts");
        mStoreFacade.requestReceipts(this, new ReceiptListener());
    }

    /**
     * Add all of the products for this application to the UI as buttons for the user to click.
     */
    private void addProducts() {
        if(mProductList != null) {
            ViewGroup viewGroup = (ViewGroup)findViewById(R.id.products);
            viewGroup.removeAllViews();
            for (Product product : mProductList) {
                viewGroup.addView(makeButton(product));
            }
        }
    }

    /**
     * Change the Adapter on the receipt ListView to show the currently known receipts.
     */
    private void addReceipts() {
        if(mReceiptList != null) {
            mReceiptListView.setAdapter(
                    new ReceiptAdapter(IapSampleActivity.this, mReceiptList.toArray(new Receipt[mReceiptList.size()]))
            );
        }
    }

    @Deprecated // Testing only
    public void addProducts(List<Product> products) {
        for (Product product : products) {
            ((ViewGroup) findViewById(R.id.products)).addView(makeButton(product));
        }
    }

    /**
     * Create a button to show the user which they can click on to purchase the item.
     *
     * @param item The item that can be purchased by clicking on the button.
     *
     * @return The Button to show in the UI.
     */

    private View makeButton(Product item) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.product_item, null, false);
        String buttonText = item.getName() + " - " + item.getFormattedPrice();
        Button button = (Button) view.findViewById(R.id.purchase_product_button);
        button.setOnClickListener(new RequestPurchaseClickListener());
        button.setText(buttonText);
        button.setTag(item);
        return view;
    }

    /*
     * This will be called when the user clicks on an item in the ListView.
     */
    public void requestPurchase(final Product product)
            throws GeneralSecurityException, UnsupportedEncodingException, JSONException {
        Purchasable purchasable = product.createPurchasable();
        if (sEnableLogging) {
            Log.d(LOG_TAG, "requestPurchase: identifier="+purchasable.getProductId());
        }
        mStoreFacade.requestPurchase(this, purchasable, new PurchaseListener());
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == BUTTON_O) {
            View focusedButton = getCurrentFocus();
            focusedButton.performClick();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * OnClickListener to handle purchase requests.
     */

    public class RequestPurchaseClickListener implements View.OnClickListener {
        @Override
        public void onClick(final View v) {
            new AlertDialog.Builder(IapSampleActivity.this)
                    .setTitle(getString(R.string.alert_title))
                    .setMessage(getResources().getString(R.string.charge_warning))
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                requestPurchase((Product) v.getTag());
                            } catch (Exception ex) {
                                Log.e(LOG_TAG, "Error requesting purchase", ex);
                                showError(ex.getMessage());
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    /**
     * Display an error to the user. We're using a toast for simplicity.
     */

    private void showError(final String errorMessage) {
        Log.e(LOG_TAG, "showError: errorMessage="+errorMessage);
        Toast.makeText(IapSampleActivity.this, errorMessage, Toast.LENGTH_LONG).show();
    }

    /**
     * The callback for when the list of user receipts has been requested.
     */
    private class ReceiptListener implements ResponseListener<Collection<Receipt>>
    {
        /**
         * Handle the successful fetching of the data for the receipts from the server.
         *
         * @param receipts The response from the server.
         */
        @Override
        public void onSuccess(Collection<Receipt> receipts) {
            Toast.makeText(IapSampleActivity.this, "requestReceipts listener: onSuccess", Toast.LENGTH_LONG).show();
            mReceiptList = new ArrayList<Receipt>(receipts);
            IapSampleActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addReceipts();
                }
            });
        }

        /**
         * Handle a failure. Because displaying the receipts is not critical to the application we just show an error
         * message rather than asking the user to authenticate themselves just to start the application up.
         *
         * @param errorCode An HTTP error code between 0 and 999, if there was one. Otherwise, an internal error code from the
         *                  Ouya server, documented in the {@link ErrorCodes} class.
         *
         * @param errorMessage Empty for HTTP error codes. Otherwise, a brief, non-localized, explanation of the error.
         *
         * @param optionalData A Map of optional key/value pairs which provide additional information.
         */

        @Override
        public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
            Toast.makeText(IapSampleActivity.this, "requestReceipts listener: onFailure errorCode="+errorCode+" errorMessage="+errorMessage, Toast.LENGTH_LONG).show();
            showError("requestReceipts listener: onFailure errorCode="+errorCode+" errorMessage="+errorMessage);
        }

        /*
         * Handle user canceling
         */
        @Override
        public void onCancel()
        {
            Toast.makeText(IapSampleActivity.this, "requestReceipts listener: onCancel", Toast.LENGTH_LONG).show();
            showError("User cancelled getting receipts");
        }
    }

    /**
     * The callback for when the user attempts to purchase something. If you're not worried about
     * the user cancelling the purchase, extend CancelIgnoringResponseListener, if
     * you want to handle cancelations differently you should extend ResponseListener and
     * implement an onCancel method.
     *
     * @see com.razerzone.store.sdk.CancelIgnoringResponseListener
     * @see com.razerzone.store.sdk.ResponseListener#onCancel()
     */
    private class PurchaseListener implements ResponseListener<PurchaseResult> {

        /**
         * Handle a successful purchase.
         *
         * @param result The response from the server.
         */
        @Override
        public void onSuccess(PurchaseResult result) {
            Toast.makeText(IapSampleActivity.this, "requestPurchase listener: onSuccess identifier="+result.getProductIdentifier(), Toast.LENGTH_LONG).show();
            requestReceipts();
        }

        @Override
        public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
            Toast.makeText(IapSampleActivity.this, "requestPurchase listener: onFailure errorCode="+errorCode+" errorMessage="+errorMessage, Toast.LENGTH_LONG).show();
        }

        /*
         * Handling the user canceling
         */
        @Override
        public void onCancel()
        {
            Toast.makeText(IapSampleActivity.this, "requestPurchase listener: onCancel", Toast.LENGTH_LONG).show();
            showError("User cancelled purchase");
        }
    }

}
