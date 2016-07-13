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

package tv.ouya.sample.game;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.razerzone.store.sdk.CancelIgnoringResponseListener;
import com.razerzone.store.sdk.StoreFacade;

import java.security.InvalidParameterException;

public class GameApplication extends Application {

    private static final String TAG = GameApplication.class.getSimpleName();

    private static final String SECRET_API_KEY =
            "eyJkZXZlbG9wZXJfaWQiOiIzMTBhOGY1MS00ZDZlLTRhZTUtYmRhMC1iOTM4" +
                    "NzhlNWY1ZDAiLCJkZXZlbG9wZXJfcHVibGljX2tleSI6Ik1JR2ZNQTBHQ1Nx" +
                    "R1NJYjNEUUVCQVFVQUE0R05BRENCaVFLQmdRQy9wTTE2MHBWekw4ZG1yNVRq" +
                    "aGZCVS95NjluQVl3TytJTXI3c2tCNFNJSXc1emFpWTNKWE5YMmpESXlRdzVK" +
                    "U0VBYnBaa1JYa0d6YTVGTFArU2MyUktISUVJdVF1bnQ3R1MrU3FPcU5mQXRY" +
                    "d2c3N3lSWU5rTXBvdFpFU0x4d05HVnFjN1g1SHdLdzRKcHJ2aXhZbGFkd0hW" +
                    "dTUwUTc1WHlNQlBWVlF0SFpOd0lEQVFBQiJ9";

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
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        StoreFacade.getInstance().shutdown(new CancelIgnoringResponseListener() {
            @Override
            public void onSuccess(Object o) {

            }

            @Override
            public void onFailure(int i, String s, Bundle bundle) {

            }
        });
    }
}
