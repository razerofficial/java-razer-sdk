package tv.ouya.sample.cc;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.razerzone.store.sdk.Controller;
import com.razerzone.store.sdk.StoreFacade;

import java.security.InvalidParameterException;

public class CCSampleApplication extends Application {

    private static final String TAG = CCSampleApplication.class.getSimpleName();

    private static final String SECRET_API_KEY =
            "eyJkZXZlbG9wZXJfaWQiOiIzMTBhOGY1MS00ZDZlLTRhZTUtYmRhMC1iOTM4" +
                    "NzhlNWY1ZDAiLCJkZXZlbG9wZXJfcHVibGljX2tleSI6Ik1JR2ZNQTBHQ1Nx" +
                    "R1NJYjNEUUVCQVFVQUE0R05BRENCaVFLQmdRRDY4Z3dmSGpxZzVnVjVXdWhP" +
                    "OEl5cXl4RCt4ekxSUG84ZnBFcXR0UDY2VVovNG9RZitvZG5vSmRZak9mbVNP" +
                    "amNTaUNqaEtOdnFHbFZRZVdyU3hHUHlsNFhLZjUzVmhrRTR5aXc0UmlDVmtP" +
                    "WHNmV01ZeGdWZ3p3TUhSdG84Nk53bjYwMkxZY1J6S0NIdTNPRzhWcnhaVnRx" +
                    "OS9WTHdHVXRHQTJvUWtFeVJBUUlEQVFBQiJ9";

    @Override
    public void onCreate() {
        super.onCreate();

        Bundle developerInfo = null;
        try {
            developerInfo = StoreFacade.createInitBundle(SECRET_API_KEY);
        } catch (InvalidParameterException e) {
            Log.e(TAG, e.getMessage());
            return;
        }

        Log.d(TAG, "developer_id=" + developerInfo.getString(StoreFacade.DEVELOPER_ID));
        Log.d(TAG, "developer_public_key length=" + developerInfo.getByteArray(StoreFacade.DEVELOPER_PUBLIC_KEY).length);

        StoreFacade.getInstance().init(this, developerInfo);
        Controller.init(this);
    }
}
