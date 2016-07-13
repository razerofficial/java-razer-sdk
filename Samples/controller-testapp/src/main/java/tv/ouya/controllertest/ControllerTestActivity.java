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

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import com.razerzone.store.sdk.Controller;

public class ControllerTestActivity extends Activity {
	
	private PlotFPS mPlot = null;

    /**
     * Array holding all the controller views for easy lookup.
     */
    private View[] mControllerViews;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Controller.init(this);
        
        mPlot = (PlotFPS) findViewById(R.id.ouyaPlotFPS1);
        mPlot.mFpsText = (TextView) findViewById(R.id.fpsText);
        mPlot.mCpu1Text = (TextView) findViewById(R.id.cpu1Text);
        mPlot.mCpu2Text = (TextView) findViewById(R.id.cpu2Text);
        mPlot.mCpu3Text = (TextView) findViewById(R.id.cpu3Text);
        mPlot.mCpu4Text = (TextView) findViewById(R.id.cpu4Text);
        mPlot.mKeyDownText = (TextView) findViewById(R.id.keyDownTime);
        mPlot.mKeyUpText = (TextView) findViewById(R.id.keyUpTime);
        mPlot.mGenericMotionText = (TextView) findViewById(R.id.genericMotionTime);

        mControllerViews = new View[4];
        mControllerViews[0] = findViewById(R.id.controllerView1);
        mControllerViews[1] = findViewById(R.id.controllerView2);
        mControllerViews[2] = findViewById(R.id.controllerView3);
        mControllerViews[3] = findViewById(R.id.controllerView4);
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();   	

		PlotFPS plot = (PlotFPS) findViewById(R.id.ouyaPlotFPS1);
		plot.Quit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	mPlot.mKeyDownTime = System.nanoTime() / 1000000000.0 - event.getEventTime() / 1000.0;
        View controllerView = getControllerView(event);
        controllerView.setVisibility(View.VISIBLE);
        return controllerView.onKeyDown(keyCode,  event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
    	mPlot.mKeyUpTime = System.nanoTime() / 1000000000.0 - event.getEventTime() / 1000.0;
        View controllerView = getControllerView(event);
        controllerView.setVisibility(View.VISIBLE);
        return controllerView.onKeyUp(keyCode,  event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
    	
    	mPlot.mGenericMotionTime = System.nanoTime() / 1000000000.0 - event.getEventTime() / 1000.0;
    	
        if((event.getSource() & InputDevice.SOURCE_CLASS_JOYSTICK) == 0){
            //Not a joystick movement, so ignore it.
            return false;
        }
        View controllerView = getControllerView(event);
        controllerView.setVisibility(View.VISIBLE);
        return controllerView.onGenericMotionEvent(event);
    }

    private View getControllerView(InputEvent event) {
        int playerNum = Controller.getPlayerNumByDeviceId(event.getDeviceId());
        if(playerNum >=0 && playerNum < mControllerViews.length) {
            return mControllerViews[playerNum];
        }
        return mControllerViews[0];
    }
}
