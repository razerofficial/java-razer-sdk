/*
 * Copyright (C) 2012 OUYA, Inc.
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

package tv.ouya.controllertest;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.razerzone.store.sdk.Controller;

import java.util.HashMap;

public class ControllerView extends RelativeLayout {

    private static HashMap<Integer, Integer> sButtons;

    private Drawable mRightStick;
    private Drawable mLeftStick;
    private Drawable mThumbRight;
    private Drawable mThumbLeft;
    private ImageView mRightStickView = new ImageView(getContext());
    private ImageView mLeftStickView = new ImageView(getContext());

    private boolean mRightIgnore = false;
    private boolean mLeftIgnore = false;

    static {
        sButtons = new HashMap<Integer, Integer>();
        sButtons.put(Controller.BUTTON_O, R.drawable.o);
        sButtons.put(Controller.BUTTON_U, R.drawable.u);
        sButtons.put(Controller.BUTTON_Y, R.drawable.y);
        sButtons.put(Controller.BUTTON_A, R.drawable.a);

        sButtons.put(Controller.BUTTON_DPAD_DOWN, R.drawable.dpad_down);
        sButtons.put(Controller.BUTTON_DPAD_LEFT, R.drawable.dpad_left);
        sButtons.put(Controller.BUTTON_DPAD_UP, R.drawable.dpad_up);
        sButtons.put(Controller.BUTTON_DPAD_RIGHT, R.drawable.dpad_right);

        sButtons.put(Controller.BUTTON_R1, R.drawable.rb);
        sButtons.put(Controller.BUTTON_L1, R.drawable.lb);
        sButtons.put(KeyEvent.KEYCODE_BUTTON_R2, R.drawable.rt);
        sButtons.put(KeyEvent.KEYCODE_BUTTON_L2, R.drawable.lt);
    }

    public ControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ControllerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ControllerView(Context context) {
        super(context);
        init();
    }

    private void init() {
        ImageView cutterView = new ImageView(getContext());
        cutterView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.cutter));
        addView(cutterView);

        mLeftStick = getContext().getResources().getDrawable(R.drawable.l_stick);
        mThumbLeft = getContext().getResources().getDrawable(R.drawable.thumbl);
        mLeftStickView.setImageDrawable(mLeftStick);
        addView(mLeftStickView);

        mRightStick = getContext().getResources().getDrawable(R.drawable.r_stick);
        mThumbRight = getContext().getResources().getDrawable(R.drawable.thumbr);
        mRightStickView.setImageDrawable(mRightStick);
        addView(mRightStickView);

        Controller.init(getContext());

        for(Integer button : sButtons.keySet()) {
            Integer resId = sButtons.get(button);
            ImageView buttonView = new ImageView(getContext());

            buttonView.setImageDrawable(getContext().getResources().getDrawable(resId));
            buttonView.setId(button);
            if(button != KeyEvent.KEYCODE_BUTTON_L2 && button != KeyEvent.KEYCODE_BUTTON_R2)
                buttonView.setVisibility(View.INVISIBLE);
            else
                buttonView.setAlpha(0f);

            addView(buttonView);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == Controller.BUTTON_L3) {
            mLeftStickView.setImageDrawable(mThumbLeft);
            return true;
        } else if (keyCode == Controller.BUTTON_R3) {
            mRightStickView.setImageDrawable(mThumbRight);
            return true;
        } else if (findViewById(keyCode) != null && keyCode != KeyEvent.KEYCODE_BUTTON_L2 && keyCode != KeyEvent.KEYCODE_BUTTON_R2) {
            View v = findViewById(keyCode);
            v.setVisibility(View.VISIBLE);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == Controller.BUTTON_L3) {
            mLeftStickView.setImageDrawable(mLeftStick);
            return true;
        } else if (keyCode == Controller.BUTTON_R3) {
            mRightStickView.setImageDrawable(mRightStick);
            return true;
        } else if (findViewById(keyCode) != null && keyCode != KeyEvent.KEYCODE_BUTTON_L2 && keyCode != KeyEvent.KEYCODE_BUTTON_R2) {
            View v = findViewById(keyCode);
            v.setVisibility(View.INVISIBLE);
            return true;
        } else {
            return super.onKeyUp(keyCode, event);
        }
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {

    	//rotate Left Stick input by N degrees to match image orientation
    	float degrees = 135f;
    	float radians = degrees / 180f * 3.14f;
    	float cs = (float)Math.cos(radians);
    	float sn = (float)Math.sin(radians);

    	float x = event.getAxisValue(Controller.AXIS_LS_X);
    	float y = event.getAxisValue(Controller.AXIS_LS_Y);

    	mLeftStickView.setTranslationX((x * cs - y * sn) * 5f);
    	mLeftStickView.setTranslationY((x * sn + y * cs) * 5f);

    	//rotate Right Stick by same degrees to match image orientation
    	x = event.getAxisValue(Controller.AXIS_RS_X);
    	y = event.getAxisValue(Controller.AXIS_RS_Y);

    	mRightStickView.setTranslationX((x * cs - y * sn) * 5f);
    	mRightStickView.setTranslationY((x * sn + y * cs) * 5f);

        float ltrigger = event.getAxisValue(Controller.AXIS_L2);
        if(ltrigger != 0.0f) {
            findViewById(KeyEvent.KEYCODE_BUTTON_L2).setAlpha(ltrigger);
            mLeftIgnore = false;
        } else if(!mLeftIgnore){
            mLeftIgnore = true;
            findViewById(KeyEvent.KEYCODE_BUTTON_L2).setAlpha(0f);
        }

        float rtrigger = event.getAxisValue(Controller.AXIS_R2);
        if(rtrigger != 0.0f) {
            findViewById(KeyEvent.KEYCODE_BUTTON_R2).setAlpha(rtrigger);
            mRightIgnore = false;
        } else if(!mRightIgnore){
            mRightIgnore = true;
            findViewById(KeyEvent.KEYCODE_BUTTON_R2).setAlpha(0f);
        }

        onKeyUp(Controller.BUTTON_DPAD_LEFT, new KeyEvent(Controller.BUTTON_DPAD_LEFT, KeyEvent.ACTION_UP));
        onKeyUp(Controller.BUTTON_DPAD_RIGHT, new KeyEvent(Controller.BUTTON_DPAD_RIGHT, KeyEvent.ACTION_UP));
        if(event.getAxisValue(MotionEvent.AXIS_HAT_X) == -1) {
            onKeyDown(Controller.BUTTON_DPAD_LEFT, new KeyEvent(Controller.BUTTON_DPAD_LEFT, KeyEvent.ACTION_DOWN));
        }
        if(event.getAxisValue(MotionEvent.AXIS_HAT_X) == 1) {
            onKeyDown(Controller.BUTTON_DPAD_RIGHT, new KeyEvent(Controller.BUTTON_DPAD_RIGHT, KeyEvent.ACTION_DOWN));
        }

        onKeyUp(Controller.BUTTON_DPAD_DOWN, new KeyEvent(Controller.BUTTON_DPAD_DOWN, KeyEvent.ACTION_UP));
        onKeyUp(Controller.BUTTON_DPAD_UP, new KeyEvent(Controller.BUTTON_DPAD_UP, KeyEvent.ACTION_UP));
        if(event.getAxisValue(MotionEvent.AXIS_HAT_Y) == -1) {
            onKeyDown(Controller.BUTTON_DPAD_UP, new KeyEvent(Controller.BUTTON_DPAD_UP, KeyEvent.ACTION_DOWN));
        }
        if(event.getAxisValue(MotionEvent.AXIS_HAT_Y) == 1) {
            onKeyDown(Controller.BUTTON_DPAD_DOWN, new KeyEvent(Controller.BUTTON_DPAD_DOWN, KeyEvent.ACTION_DOWN));
        }

        return true;
    }
}
