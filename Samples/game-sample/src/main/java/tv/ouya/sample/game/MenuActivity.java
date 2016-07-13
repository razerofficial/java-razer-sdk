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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.razerzone.store.sdk.BaseActivity;
import com.razerzone.store.sdk.CancelIgnoringResponseListener;
import com.razerzone.store.sdk.StoreFacade;

public class MenuActivity extends BaseActivity {

    private static final String TAG = MenuActivity.class.getSimpleName();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        Button newGame = (Button) findViewById(R.id.new_game_button);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, GameActivity.class));
            }
        });

        Button options = (Button) findViewById(R.id.options_button);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MenuActivity.this, OptionsActivity.class));
            }
        });

        Button quit = (Button) findViewById(R.id.quit_game_button);
        quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StoreFacade.getInstance().shutdown(new CancelIgnoringResponseListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Shutdown success.");
                        finish();
                    }

                    @Override
                    public void onFailure(int i, String s, Bundle bundle) {
                        Log.e(TAG, "Shutdown failure!");
                    }
                });
            }
        });
    }
}
