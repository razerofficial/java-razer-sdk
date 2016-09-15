# Java Engine

* The [RazerSDK](https://github.com/razerofficial/razer-sdk-docs) can be accessed via the `store-sdk-standard-release.aar` Android Library.

## Forums

[Forge TV on Razer Forums](https://insider.razerzone.com/index.php?forums/razer-forge-tv.126/)

## Audience

This document is for developers that uses Java to make apps for Android. The docs target developers using Android Studio, Eclipse, or IntelliJ. However, most engines also have a Java hook that will be able to reuse the samples.

## Releases

Java apps/games use the `store-sdk-standard-release.aar` library included in the `RazerSDK` downloadable from the [Cortex developer portal](http://devs.ouya.tv).

## Java Samples

### In-App-Purchases Sample

The [In-App-Purchase](https://github.com/razerofficial/java-razer-sdk/tree/master/Samples/iap-sample-app) sample is an `Android Studio` project.

#### StoreFacade

The `StoreFacade` has several listeners for `in-app-purchase` callbacks.

```
public class CustomActivity extends Activity
{
	// The tag for log messages
	private static final String TAG = ActivityCommon.class.getSimpleName();

	// Toggle debug logging
	private static final boolean sEnableLogging = false; 

	// Your game talks to the StoreFacade, which hides all the mechanics of doing an in-app purchase.
	private StoreFacade mStoreFacade = null;
	
	// listener for init complete
	private CancelIgnoringResponseListener<Bundle> mInitCompleteListener = null;

	// listener for fetching gamer info
	private ResponseListener<GamerInfo> mRequestGamerInfoListener = null;

	// listener for getting products
	private ResponseListener<List<Product>> mRequestProductsListener = null;

	// listener for requesting purchase
	private ResponseListener<PurchaseResult> mRequestPurchaseListener = null;

	// listener for getting receipts
	private ResponseListener<Collection<Receipt>> mRequestReceiptsListener = null;

    // listener for shutdown
    private CancelIgnoringResponseListener mShutdownListener = null;
}
```

#### Secret API Key

Each game entry in the [developer portal](https://devs.ouya.tv) has a `Secret API Key` used for store encryption/decryption. The `Secret API Key` for each game can be found in the `Games` section of the developer portal.

```
	private static final String SECRET_API_KEY =
	  "eyJkZXZlbG9wZXJfaWQiOiIzMTBhOGY1MS00ZDZlLTRhZTUtYmRhMC1iOTM4" +
	  "NzhlNWY1ZDAiLCJkZXZlbG9wZXJfcHVibGljX2tleSI6Ik1JR2ZNQTBHQ1Nx" +
	  "R1NJYjNEUUVCQVFVQUE0R05BRENCaVFLQmdRQy9wTTE2MHBWekw4ZG1yNVRq" +
	  "aGZCVS95NjluQVl3TytJTXI3c2tCNFNJSXc1emFpWTNKWE5YMmpESXlRdzVK" +
	  "U0VBYnBaa1JYa0d6YTVGTFArU2MyUktISUVJdVF1bnQ3R1MrU3FPcU5mQXRY" +
	  "d2c3N3lSWU5rTXBvdFpFU0x4d05HVnFjN1g1SHdLdzRKcHJ2aXhZbGFkd0hW" +
	  "dTUwUTc1WHlNQlBWVlF0SFpOd0lEQVFBQiJ9"; 
```

The sample implements the following listeners to pass to the `StoreFacade` IAP methods.

```
		mInitCompleteListener = new CancelIgnoringResponseListener<Bundle>() {
			@Override
			public void onSuccess(Bundle bundle) {
				Log.d(TAG, "InitCompleteListener onSuccess");
			}

			@Override
			public void onFailure(int i, String s, Bundle bundle) {
				Log.e(TAG, "InitCompleteListener onFailure");
			}
		};

		mRequestGamerInfoListener = new ResponseListener<GamerInfo>() {
            @Override
            public void onSuccess(GamerInfo info) {
            	Log.d(TAG, "RequestGamerInfoListener: onSuccess");
            }

            @Override
            public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
            	Log.d(TAG, "RequestGamerInfoListener: onFailure errorCode="+errorCode+" errorMessage="+errorMessage);
            }

			@Override
			public void onCancel() {
				Log.e(TAG, "RequestGamerInfoListener onCancel");
			}
        };
        
		mRequestProductsListener = new ResponseListener<List<Product>>() {
			@Override
			public void onSuccess(final List<Product> products) {
				Log.d(TAG, "RequestProductsListener: onSuccess received "+products.size()+" products");
			}

			@Override
			public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
				Log.d(TAG, "sRequestProductsListener: onFailure errorCode="+errorCode+" errorMessage="+errorMessage);
			}
		};

		mRequestPurchaseListener = new ResponseListener<PurchaseResult>() {

			@Override
			public void onSuccess(PurchaseResult result) {
				Log.d(TAG, "RequestPurchaseListener: onSuccess");
			}

			@Override
			public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
				Log.d(TAG, "RequestPurchaseListener: onFailure errorCode="+errorCode+" errorMessage="+errorMessage);
			}

			@Override
			public void onCancel() {
				Log.d(TAG, "RequestPurchaseListener: onCancel");
			}
		};
		
		mRequestReceiptsListener = new ResponseListener<Collection<Receipt>>() {

			@Override
			public void onSuccess(Collection<Receipt> receipts) {
				Log.d(TAG, "RequestReceiptsListener: onSuccess received "+receipts.size() + " receipts");
			}

			@Override
			public void onFailure(int errorCode, String errorMessage, Bundle optionalData) {
				Log.d(TAG, "RequestReceiptsListener: onFailure: errorCode="+errorCode+" errorMessage="+errorMessage);
			}

			@Override
			public void onCancel() {
				Log.d(TAG, "RequestReceiptsListener: onCancel");
			}
		};

        mShutdownListener = new CancelIgnoringResponseListener() {
            @Override
            public void onSuccess(Object o) {
                Log.d(TAG, "ShutdownListener onSuccess");

				// Wait for the shutdown success event before closing the application
				finish();
            }

            @Override
            public void onFailure(int errorCode, String message, Bundle bundle) {
                Log.e(TAG, "ShutdownListener onFailure failed to shutdown! errorCode="+errorCode+" message="+message);
            }
        };
```

## Razer SDK

The `store-sdk-standard-release.aar` Java library released through the developer portal which provides access to the `RazerSDK`.

## Icons

Content review requires that a `48x48` icon must be provided. I.e. [res/drawable-mdpi/icon.png](https://github.com/razerofficial/java-razer-sdk/blob/master/Samples/iap-sample-app/res/drawable-mdpi/icon.png)

## AndroidManifest.xml

The intent-filter specifies categories for the `leanback launcher` and `Razer` store. Apps use the category `com.razerzone.store.category.APP`. Games use the category `com.razerzone.store.category.GAME`.

```
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
                <category android:name="com.razerzone.store.category.APP" />
            </intent-filter>
```

### Init

Before invoking `StoreFacade` methods, the `StoreFacade` needs to be initialized. The developer info can be created using the `SecretApiKey` from the [developer portal](https://devs.ouya.tv) that is used for initialization.

```
		Bundle developerInfo = null;
		try {
			developerInfo = StoreFacade.createInitBundle(SECRET_API_KEY);
		} catch (InvalidParameterException e) {
			Log.e(TAG, e.getMessage());
		}
```

Register the success and failure callbacks and then initialize the `StoreFacade`. The `init` method takes a context and bundle parameter. The context parameter can use the game activity. The developer info is prepared above.

```
        StoreFacade storeFacade = StoreFacade.getInstance();
        storeFacade.registerInitCompletedListener(new CancelIgnoringResponseListener<Bundle>() {
            @Override
            public void onSuccess(Bundle bundle) {
                Log.d(TAG, "init listener: onSuccess");
            }

            @Override
            public void onFailure(int errorCode, String errorMessage, Bundle bundle) {
                Log.e(TAG, "init listener: onFailure errorCode="+errorCode+" errorMessage="+errorMessage);
            }
        });
        storeFacade.init(this, developerInfo);
```

### OnActivityResult

The activity must implement `onActivityResult`. The activity results must be passed to the `StoreFacade` via processActivityResult.

```
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        // Forward this result to the facade, in case it is waiting for any activity results
        if(mStoreFacade.processActivityResult(requestCode, resultCode, data)) {
            Log.d(LOG_TAG, "onActivityResult: StoreFacade processed activity result");
            return;
        }
    }
```

### Shutdown

The `shutdown` method should only be invoked after the `RazerSDK` has successfully initialized. The `RazerSDK` must be shutdown before exiting the application.

```
        StoreFacade storeFacade = StoreFacade.getInstance();
        storeFacade.shutdown(new CancelIgnoringResponseListener() {
            @Override
            public void onSuccess(Bundle bundle) {
                Log.d(TAG, "shutdown listener: onSuccess");
            }

            @Override
            public void onFailure(int errorCode, String errorMessage, Bundle bundle) {
                Log.e(TAG, "shutdown listener: onFailure errorCode="+errorCode+" errorMessage="+errorMessage);
            }
        });
```
