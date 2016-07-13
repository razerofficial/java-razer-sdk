package tv.ouya.examples.android.virtualcontroller;

import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.razerzone.store.sdk.BaseActivity;
import com.razerzone.store.sdk.CancelIgnoringResponseListener;
import com.razerzone.store.sdk.Controller;
import com.razerzone.store.sdk.InputMapper;
import com.razerzone.store.sdk.StoreFacade;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.HashMap;

import tv.ouya.sdk.*;

public class MainActivity extends BaseActivity {
	
	private static final String TAG = MainActivity.class.getSimpleName();

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
	
	private static final Float AXIS_SCALER = 4f;
	
	private TextView txtSystem = null;
	private TextView txtController = null;
	private TextView txtKeyCode = null;
	private TextView txtKeyCode2 = null;
	private ImageView imgControllerO = null;
	private ImageView imgControllerU = null;
	private ImageView imgControllerY = null;
	private ImageView imgControllerA = null;
	private ImageView imgControllerL1 = null;
	private ImageView imgControllerL2 = null;
	private ImageView imgControllerL3 = null;
	private ImageView imgControllerR1 = null;
	private ImageView imgControllerR2 = null;
	private ImageView imgControllerR3 = null;
	private ImageView imgControllerDpad = null;
	private ImageView imgControllerDpadDown = null;
	private ImageView imgControllerDpadLeft = null;
	private ImageView imgControllerDpadRight = null;
	private ImageView imgControllerDpadUp = null;
	private ImageView imgControllerBack = null;
	private ImageView imgControllerHome = null;
	private ImageView imgControllerMenu = null;
	private ImageView imgControllerNext = null;
	private ImageView imgControllerPower = null;
	private ImageView imgControllerPrevious = null;
	private ImageView imgControllerLS = null;
	private ImageView imgControllerRS = null;
	private ImageView imgButtonMenu = null;	
	private ImageView imgButtonA = null;
	private ImageView imgDpadDown = null;
	private ImageView imgDpadLeft = null;
	private ImageView imgDpadRight = null;
	private ImageView imgDpadUp = null;
	private ImageView imgLeftStick = null;
	private ImageView imgLeftBumper = null;
	private ImageView imgLeftTrigger = null;
	private ImageView imgButtonO = null;
	private ImageView imgRightStick = null;
	private ImageView imgRightBumper = null;
	private ImageView imgRightTrigger = null;
	private ImageView imgLeftThumb = null;
	private ImageView imgRightThumb = null;
	private ImageView imgButtonU = null;
	private ImageView imgButtonY = null;
	
	// keep track when menu button was seen
	private Boolean mWaitToExit = true;
	private float mMenuDetected = 0f;
	
	private StoreFacade mStoreFacade = null;
	
	private static SparseArray<HashMap<Integer, Float>> sAxisValues = new SparseArray<HashMap<Integer, Float>>();
	private static SparseArray<HashMap<Integer, Boolean>> sButtonValues = new SparseArray<HashMap<Integer, Boolean>>();
	
	static {
		for (int index = 0; index < Controller.MAX_CONTROLLERS; ++index) {
			HashMap<Integer, Float> axisMap = new HashMap<Integer, Float>();
			axisMap.put(MotionEvent.AXIS_HAT_X, 0f);
			axisMap.put(MotionEvent.AXIS_HAT_Y, 0f);
			sAxisValues.put(index, axisMap);
			HashMap<Integer, Boolean> buttonMap = new HashMap<Integer, Boolean>();
			sButtonValues.put(index, buttonMap);
		}
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		setContentView(R.layout.activity_main);
		super.onCreate(savedInstanceState);
		
		ViewGroup mainLayout = (ViewGroup)this.findViewById(android.R.id.content);		
		mainLayout.setOnClickListener(mClickListener);
		
		mainLayout.setKeepScreenOn(true);

        Bundle developerInfo = null;
        try {
            developerInfo = StoreFacade.createInitBundle(SECRET_API_KEY);
        } catch (InvalidParameterException e) {
            Log.e(TAG, e.getMessage());
            finish();
        }

        Log.d(TAG, "developer_id=" + developerInfo.getString(StoreFacade.DEVELOPER_ID));
        Log.d(TAG, "developer_public_key length=" + developerInfo.getByteArray(StoreFacade.DEVELOPER_PUBLIC_KEY).length);
		
		mStoreFacade = StoreFacade.getInstance();
		
		mStoreFacade.init(this, developerInfo);
		
		txtSystem = (TextView)findViewById(R.id.txtSystem);
		txtController = (TextView)findViewById(R.id.txtController);
		imgButtonMenu = (ImageView)findViewById(R.id.imgButtonMenu);
		txtKeyCode = (TextView)findViewById(R.id.txtKeyCode);
		txtKeyCode2 = (TextView)findViewById(R.id.txtKeyCode2);
		imgControllerO = (ImageView)findViewById(R.id.imgControllerO);
		imgControllerU = (ImageView)findViewById(R.id.imgControllerU);
		imgControllerY = (ImageView)findViewById(R.id.imgControllerY);
		imgControllerA = (ImageView)findViewById(R.id.imgControllerA);
		imgControllerL1 = (ImageView)findViewById(R.id.imgControllerL1);
		imgControllerL2 = (ImageView)findViewById(R.id.imgControllerL2);
		imgControllerL3 = (ImageView)findViewById(R.id.imgControllerl3);
		imgControllerR1 = (ImageView)findViewById(R.id.imgControllerR1);
		imgControllerR2 = (ImageView)findViewById(R.id.imgControllerR2);
		imgControllerR3 = (ImageView)findViewById(R.id.imgControllerR3);
		imgControllerDpad = (ImageView)findViewById(R.id.imgControllerDpad);
		imgControllerDpadDown = (ImageView)findViewById(R.id.imgControllerDpadDown);
		imgControllerDpadLeft = (ImageView)findViewById(R.id.imgControllerDpadLeft);
		imgControllerDpadRight = (ImageView)findViewById(R.id.imgControllerDpadRight);
		imgControllerDpadUp = (ImageView)findViewById(R.id.imgControllerDpadUp);
		imgControllerBack = (ImageView)findViewById(R.id.imgControllerBack);
		imgControllerHome = (ImageView)findViewById(R.id.imgControllerHome);
		imgControllerMenu = (ImageView)findViewById(R.id.imgControllerMenu);
		imgControllerNext = (ImageView)findViewById(R.id.imgControllerNext);
		imgControllerPrevious = (ImageView)findViewById(R.id.imgControllerPrevious);
		imgControllerPower = (ImageView)findViewById(R.id.imgControllerPower);
		imgControllerLS = (ImageView)findViewById(R.id.imgControllerLS);
		imgControllerRS = (ImageView)findViewById(R.id.imgControllerRS);
		imgButtonA = (ImageView)findViewById(R.id.imgButtonA);
		imgDpadDown = (ImageView)findViewById(R.id.imgDpadDown);
		imgDpadLeft = (ImageView)findViewById(R.id.imgDpadLeft);
		imgDpadRight = (ImageView)findViewById(R.id.imgDpadRight);
		imgDpadUp = (ImageView)findViewById(R.id.imgDpadUp);
		imgLeftStick = (ImageView)findViewById(R.id.imgLeftStick);
		imgLeftBumper = (ImageView)findViewById(R.id.imgLeftBumper);
		imgLeftTrigger = (ImageView)findViewById(R.id.imgLeftTrigger);
		imgButtonO = (ImageView)findViewById(R.id.imgButtonO);
		imgRightStick = (ImageView)findViewById(R.id.imgRightStick);
		imgRightBumper = (ImageView)findViewById(R.id.imgRightBumper);
		imgRightTrigger = (ImageView)findViewById(R.id.imgRightTrigger);
		imgLeftThumb = (ImageView)findViewById(R.id.imgLeftThumb);
		imgRightThumb = (ImageView)findViewById(R.id.imgRightThumb);
		imgButtonU = (ImageView)findViewById(R.id.imgButtonU);
		imgButtonY = (ImageView)findViewById(R.id.imgButtonY);
		
        InputMapper.setEnableControllerDispatch(true);

    	// spawn thread to toggle menu button
        Thread timer = new Thread()
        {
	        public void run()
	        {
	        	while (mWaitToExit)
	        	{
	        		if (mMenuDetected != 0 &&
	        			mMenuDetected < System.nanoTime())
	        		{
	        			mMenuDetected = 0;
	        			Runnable runnable = new Runnable()
	        			{
		        			public void run()
		        			{
		        				imgButtonMenu.setVisibility(View.INVISIBLE);
		        			}
	        			};
	        			runOnUiThread(runnable);
	        			
	        		}
	        		try
	        		{
	        			Thread.sleep(50);
	        		}
	        		catch (InterruptedException e)
	        		{
	        		}
		        }
			}
        };
		timer.start();
	}

	@Override
	protected void onStart() {
		super.onStart();
		txtSystem.setText("Brand=" + android.os.Build.BRAND + " Model=" + android.os.Build.MODEL + " Device=" + Build.DEVICE +
				" Version=" + android.os.Build.VERSION.SDK_INT +
				" isRunningOnSupportedHardware="+mStoreFacade.isRunningOnSupportedHardware());
		
		setDrawable(imgControllerO, Controller.BUTTON_O);
		setDrawable(imgControllerU, Controller.BUTTON_U);
		setDrawable(imgControllerY, Controller.BUTTON_Y);
		setDrawable(imgControllerA, Controller.BUTTON_A);
		setDrawable(imgControllerL1, Controller.BUTTON_L1);
		setDrawable(imgControllerL2, KeyEvent.KEYCODE_BUTTON_L2);
		setDrawable(imgControllerL3, Controller.BUTTON_L3);
		setDrawable(imgControllerR1, Controller.BUTTON_R1);
		setDrawable(imgControllerR2, KeyEvent.KEYCODE_BUTTON_R2);
		setDrawable(imgControllerR3, Controller.BUTTON_R3);
		setDrawable(imgControllerDpad, Controller.BUTTON_DPAD);
		setDrawable(imgControllerDpadDown, Controller.BUTTON_DPAD_DOWN);
		setDrawable(imgControllerDpadLeft, Controller.BUTTON_DPAD_LEFT);
		setDrawable(imgControllerDpadRight, Controller.BUTTON_DPAD_RIGHT);
		setDrawable(imgControllerDpadUp, Controller.BUTTON_DPAD_UP);
		setDrawable(imgControllerBack, KeyEvent.KEYCODE_BACK);
		setDrawable(imgControllerHome, Controller.BUTTON_HOME);
		setDrawable(imgControllerMenu, Controller.BUTTON_MENU);
		setDrawable(imgControllerNext, KeyEvent.KEYCODE_BUTTON_START);
		setDrawable(imgControllerPower, KeyEvent.KEYCODE_BUTTON_MODE);
		setDrawable(imgControllerPrevious, KeyEvent.KEYCODE_BUTTON_SELECT);
		setDrawable(imgControllerLS, Controller.AXIS_LS_X);
		setDrawable(imgControllerRS, Controller.AXIS_RS_X);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mStoreFacade.shutdown(new CancelIgnoringResponseListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Shutdown Success!");
                mWaitToExit = false; //let timer exit
            }

            @Override
            public void onFailure(int i, String s, Bundle bundle) {
                Log.d(TAG, "Shutdown Failed!");
            }
        });
	}
	
	private OnClickListener mClickListener = new OnClickListener() {
	    public void onClick(View v) {
	    	txtKeyCode.setText("Click detected");
	    	txtController.setText("");
	    }
	};
	
	private void setDrawable(ImageView imageView, int keyCode) {
		Controller.ButtonData data = Controller.getButtonData(keyCode);
		if (null == data) {
			Log.e(TAG, "Button Data is null keycode="+keyCode+" name="+DebugInput.debugGetButtonName(keyCode));
			return;
		}

		if (null == data.buttonDrawable) {
			Log.e(TAG, "Button Drawable is null keycode="+keyCode+" name="+DebugInput.debugGetButtonName(keyCode));
			return;
		}

		Log.i(TAG, "Button name="+data.buttonName);
		if (null == imageView) {
			Log.e(TAG, "Button ImageView is null keycode="+keyCode+" name="+DebugInput.debugGetButtonName(keyCode));
			return;
		}

		imageView.setImageDrawable(data.buttonDrawable);
	}
	
	@Override
	public boolean dispatchGenericMotionEvent(MotionEvent motionEvent) {
		if (null != txtKeyCode) {
			InputDevice device = motionEvent.getDevice();
			if (null != device) {
				txtKeyCode.setText("Original MotionEvent device=" + device.getName());
			}
		}
		//DebugInput.debugMotionEvent(motionEvent);		
		return super.dispatchGenericMotionEvent(motionEvent);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent keyEvent) {
		//Log.i(TAG, "dispatchKeyEvent");
		if (null != txtKeyCode) {
			InputDevice device = keyEvent.getDevice();
			if (null != device) {
				txtKeyCode.setText("Original KeyEvent device=" + device.getName() + " KeyCode=(" + keyEvent.getKeyCode() + ") "
						+ DebugInput.debugGetButtonName(keyEvent.getKeyCode())+" source="+keyEvent.getSource());
			}
		}
		return super.dispatchKeyEvent(keyEvent);
	}
	
	void updateDPad(int playerNum) {
		if (null == sButtonValues.get(playerNum).get(Controller.BUTTON_DPAD_LEFT) &&
			null == sButtonValues.get(playerNum).get(Controller.BUTTON_DPAD_RIGHT)) {
			float dpadX = sAxisValues.get(playerNum).get(MotionEvent.AXIS_HAT_X);
			if (dpadX > 0.25f) {
		    	imgDpadRight.setVisibility(View.VISIBLE);
		    } else {
		    	imgDpadRight.setVisibility(View.INVISIBLE);
		    }		    
		    if (dpadX < -0.25f) {
		    	imgDpadLeft.setVisibility(View.VISIBLE);
		    } else {
		    	imgDpadLeft.setVisibility(View.INVISIBLE);
		    }
		}
		
		if (null == sButtonValues.get(playerNum).get(Controller.BUTTON_DPAD_DOWN) &&
			null == sButtonValues.get(playerNum).get(Controller.BUTTON_DPAD_UP)) {
		    float dpadY = sAxisValues.get(playerNum).get(MotionEvent.AXIS_HAT_Y);	    
		    if (dpadY > 0.25f) {
		    	imgDpadDown.setVisibility(View.VISIBLE);
		    } else {
		    	imgDpadDown.setVisibility(View.INVISIBLE);
		    }
		    
		    if (dpadY < -0.25f) {
		    	imgDpadUp.setVisibility(View.VISIBLE);
		    } else {
		    	imgDpadUp.setVisibility(View.INVISIBLE);
		    }
		}
	}
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent motionEvent) {
		//Log.i(TAG, "onGenericMotionEvent");
		//DebugInput.debugOuyaMotionEvent(motionEvent);
		
		int playerNum = 0;
		if (null != txtKeyCode2) {
			InputDevice device = motionEvent.getDevice();
			if (null != device) {
				txtKeyCode.setText("Original MotionEvent device=" + device.getName());
				Controller controller = Controller.getControllerByDeviceId(device.getId());
				if (null == controller) {
					txtKeyCode2.setText("Remapped MotionEvent device=unknown");
				} else {
					playerNum = controller.getPlayerNum();
					txtKeyCode2.setText("Remapped MotionEvent device=" + controller.getDeviceName()+" playerNum="+controller.getPlayerNum());
				}
			}
		}
		if (playerNum < 0 || playerNum >= Controller.MAX_CONTROLLERS) {
			Log.e(TAG, "PlayerNum is not assigned!");
			playerNum = 0;
		}
		
		float dpadX = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_X);
		float dpadY = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_Y);
		sAxisValues.get(playerNum).put(MotionEvent.AXIS_HAT_X, dpadX);
		sAxisValues.get(playerNum).put(MotionEvent.AXIS_HAT_Y, dpadY);
		updateDPad(playerNum);
		
		float lsX = motionEvent.getAxisValue(Controller.AXIS_LS_X);
	    float lsY = motionEvent.getAxisValue(Controller.AXIS_LS_Y);
	    float rsX = motionEvent.getAxisValue(Controller.AXIS_RS_X);
	    float rsY = motionEvent.getAxisValue(Controller.AXIS_RS_Y);
	    float l2 = motionEvent.getAxisValue(Controller.AXIS_L2);
	    float r2 = motionEvent.getAxisValue(Controller.AXIS_R2);
	    
	    //rotate input by N degrees to match image
        float degrees = 135;
        float radians = degrees / 180f * 3.14f;
        float cos = (float)Math.cos(radians);
        float sin = (float)Math.sin(radians);
	    
	    imgLeftStick.setX(AXIS_SCALER * (lsX * cos - lsY * sin));
	    imgLeftThumb.setY(AXIS_SCALER * (lsX * cos - lsY * sin));
	    
	    imgLeftStick.setY(AXIS_SCALER * (lsX * sin + lsY * cos));
	    imgLeftThumb.setY(AXIS_SCALER * (lsX * sin + lsY * cos));
	    
	    imgRightStick.setX(AXIS_SCALER * (rsX * cos - rsY * sin));
	    imgRightThumb.setX(AXIS_SCALER * (rsX * cos - rsY * sin));
	    
	    imgRightStick.setY(AXIS_SCALER * (rsX * sin + rsY * cos));
	    imgRightThumb.setY(AXIS_SCALER * (rsX * sin + rsY * cos));
		
		//Log.i(TAG, "Unrecognized GenericMotionEvent="+motionEvent.getAction());
	    
	    if (l2 > 0.25f) {
	    	imgLeftTrigger.setVisibility(View.VISIBLE);
	    } else {
	    	imgLeftTrigger.setVisibility(View.INVISIBLE);
	    }
	    
	    if (r2 > 0.25f) {
	    	imgRightTrigger.setVisibility(View.VISIBLE);
	    } else {
	    	imgRightTrigger.setVisibility(View.INVISIBLE);
	    }
	    
		return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
		Log.i(TAG, "onKeyDown keyCode="+keyCode+" source="+keyEvent.getSource());
		
		if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK) {
			return false;
		}
		
		Controller controller = Controller.getControllerByDeviceId(keyEvent.getDeviceId());
		if (null != controller) {
			txtKeyCode2.setText("Remapped onKeyDown device=" + controller.getDeviceName() + " KeyCode=(" + keyCode + ") "
					+ DebugInput.debugGetButtonName(keyCode) + " playerNum="+controller.getPlayerNum());
		} else {
			txtKeyCode2.setText("Remapped onKeyDown device=unknown KeyCode=(" + keyCode + ") "
					+ DebugInput.debugGetButtonName(keyCode));
		}
		
		int playerNum = Controller.getPlayerNumByDeviceId(keyEvent.getDeviceId());
	    if (playerNum < 0 || playerNum >= Controller.MAX_CONTROLLERS) {
	    	Log.e(TAG, "PlayerNum is not assigned!");
	    	playerNum = 0;
	    }
		
		switch (keyCode)
		{
		case Controller.BUTTON_L1:
			imgLeftBumper.setVisibility(View.VISIBLE);
			break;
		case KeyEvent.KEYCODE_BUTTON_L2:
			imgLeftTrigger.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_L3:
			imgLeftStick.setVisibility(View.INVISIBLE);
			imgLeftThumb.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_R1:
			imgRightBumper.setVisibility(View.VISIBLE);
			break;
		case KeyEvent.KEYCODE_BUTTON_R2:
			imgRightTrigger.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_R3:
			imgRightStick.setVisibility(View.INVISIBLE);
			imgRightThumb.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_O:
			imgButtonO.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_U:
			imgButtonU.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_Y:
			imgButtonY.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_A:
			imgButtonA.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_DPAD_DOWN:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_DOWN, true);
				imgDpadDown.setVisibility(View.VISIBLE);
			}
			break;
		case Controller.BUTTON_DPAD_LEFT:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_LEFT, true);
				imgDpadLeft.setVisibility(View.VISIBLE);
			}
			break;
		case Controller.BUTTON_DPAD_RIGHT:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_RIGHT, true);
				imgDpadRight.setVisibility(View.VISIBLE);
			}
			break;
		case Controller.BUTTON_DPAD_UP:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_UP, true);
				imgDpadUp.setVisibility(View.VISIBLE);
			}
			break;
		case Controller.BUTTON_MENU:
			imgButtonMenu.setVisibility(View.VISIBLE);
			mMenuDetected = System.nanoTime() + 1000000000;
			break;
		default:
			Log.i(TAG, "Unrecognized KeyDown="+keyCode);
			break;
		}
		
		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
		Log.i(TAG, "onKeyUp keyCode="+keyCode+" source="+keyEvent.getSource());
		
		if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK) {
			return false;
		}

        Controller controller = Controller.getControllerByDeviceId(keyEvent.getDeviceId());
		if (null != controller) {
			txtKeyCode2.setText("Remapped onKeyUp device=" + controller.getDeviceName() + " KeyCode=(" + keyCode + ") "
					+ DebugInput.debugGetButtonName(keyCode) + " playerNum="+controller.getPlayerNum());
		}
		
		int playerNum = Controller.getPlayerNumByDeviceId(keyEvent.getDeviceId());
	    if (playerNum < 0 || playerNum >= Controller.MAX_CONTROLLERS) {
	    	Log.e(TAG, "PlayerNum is not assigned!");
	    	playerNum = 0;
	    }
		
		switch (keyCode)
		{
		case Controller.BUTTON_L1:
			imgLeftBumper.setVisibility(View.INVISIBLE);
			break;
		case KeyEvent.KEYCODE_BUTTON_L2:
			imgLeftTrigger.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_L3:
			imgLeftStick.setVisibility(View.VISIBLE);
			imgLeftThumb.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_R1:
			imgRightBumper.setVisibility(View.INVISIBLE);
			break;
		case KeyEvent.KEYCODE_BUTTON_R2:
			imgRightTrigger.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_R3:
			imgRightStick.setVisibility(View.VISIBLE);
			imgRightThumb.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_O:
			imgButtonO.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_U:
			imgButtonU.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_Y:
			imgButtonY.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_A:
			imgButtonA.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_DPAD_DOWN:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_DOWN, false);
				imgDpadDown.setVisibility(View.INVISIBLE);
			}
			break;
		case Controller.BUTTON_DPAD_LEFT:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_LEFT, false);
				imgDpadLeft.setVisibility(View.INVISIBLE);
			}
			break;
		case Controller.BUTTON_DPAD_RIGHT:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_RIGHT, false);
				imgDpadRight.setVisibility(View.INVISIBLE);
			}
			break;
		case Controller.BUTTON_DPAD_UP:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_UP, false);
				imgDpadUp.setVisibility(View.INVISIBLE);
			}
			break;
		case Controller.BUTTON_MENU:
			//wait 1 second
			break;
		}
		
		return true;
	}	
}