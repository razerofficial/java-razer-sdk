package tv.ouya.examples.android.virtualcontroller;

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
	
	private TextView mTxtSystem = null;
	private TextView mTxtController = null;
	private TextView mTxtKeyCode = null;
	private TextView mTxtKeyCode2 = null;
	private ImageView mImgControllerO = null;
	private ImageView mImgControllerU = null;
	private ImageView mImgControllerY = null;
	private ImageView mImgControllerA = null;
	private ImageView mImgControllerL1 = null;
	private ImageView mImgControllerL2 = null;
	private ImageView mImgControllerL3 = null;
	private ImageView mImgControllerR1 = null;
	private ImageView mImgControllerR2 = null;
	private ImageView mImgControllerR3 = null;
	private ImageView mImgControllerDpad = null;
	private ImageView mImgControllerDpadDown = null;
	private ImageView mImgControllerDpadLeft = null;
	private ImageView mImgControllerDpadRight = null;
	private ImageView mImgControllerDpadUp = null;
	private ImageView mImgControllerBack = null;
	private ImageView mImgControllerHome = null;
	private ImageView mImgControllerMenu = null;
	private ImageView mImgControllerNext = null;
	private ImageView mImgControllerPower = null;
	private ImageView mImgControllerPrevious = null;
	private ImageView mImgControllerLS = null;
	private ImageView mImgControllerRS = null;
	private ImageView mImgButtonMenu = null;
	private ImageView mImgButtonA = null;
	private ImageView mImgDpadDown = null;
	private ImageView mImgDpadLeft = null;
	private ImageView mImgDpadRight = null;
	private ImageView mImgDpadUp = null;
	private ImageView mImgLeftStick = null;
	private ImageView mImgLeftBumper = null;
	private ImageView mImgLeftTrigger = null;
	private ImageView mImgButtonO = null;
	private ImageView mImgRightStick = null;
	private ImageView mImgRightBumper = null;
	private ImageView mImgRightTrigger = null;
	private ImageView mImgLeftThumb = null;
	private ImageView mImgRightThumb = null;
	private ImageView mImgButtonU = null;
	private ImageView mImgButtonY = null;
	
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
		
		mTxtSystem = (TextView)findViewById(R.id.txtSystem);
		mTxtController = (TextView)findViewById(R.id.txtController);
		mImgButtonMenu = (ImageView)findViewById(R.id.imgButtonMenu);
		mTxtKeyCode = (TextView)findViewById(R.id.txtKeyCode);
		mTxtKeyCode2 = (TextView)findViewById(R.id.txtKeyCode2);
		mImgControllerO = (ImageView)findViewById(R.id.imgControllerO);
		mImgControllerU = (ImageView)findViewById(R.id.imgControllerU);
		mImgControllerY = (ImageView)findViewById(R.id.imgControllerY);
		mImgControllerA = (ImageView)findViewById(R.id.imgControllerA);
		mImgControllerL1 = (ImageView)findViewById(R.id.imgControllerL1);
		mImgControllerL2 = (ImageView)findViewById(R.id.imgControllerL2);
		mImgControllerL3 = (ImageView)findViewById(R.id.imgControllerl3);
		mImgControllerR1 = (ImageView)findViewById(R.id.imgControllerR1);
		mImgControllerR2 = (ImageView)findViewById(R.id.imgControllerR2);
		mImgControllerR3 = (ImageView)findViewById(R.id.imgControllerR3);
		mImgControllerDpad = (ImageView)findViewById(R.id.imgControllerDpad);
		mImgControllerDpadDown = (ImageView)findViewById(R.id.imgControllerDpadDown);
		mImgControllerDpadLeft = (ImageView)findViewById(R.id.imgControllerDpadLeft);
		mImgControllerDpadRight = (ImageView)findViewById(R.id.imgControllerDpadRight);
		mImgControllerDpadUp = (ImageView)findViewById(R.id.imgControllerDpadUp);
		mImgControllerBack = (ImageView)findViewById(R.id.imgControllerBack);
		mImgControllerHome = (ImageView)findViewById(R.id.imgControllerHome);
		mImgControllerMenu = (ImageView)findViewById(R.id.imgControllerMenu);
		mImgControllerNext = (ImageView)findViewById(R.id.imgControllerNext);
		mImgControllerPrevious = (ImageView)findViewById(R.id.imgControllerPrevious);
		mImgControllerPower = (ImageView)findViewById(R.id.imgControllerPower);
		mImgControllerLS = (ImageView)findViewById(R.id.imgControllerLS);
		mImgControllerRS = (ImageView)findViewById(R.id.imgControllerRS);
		mImgButtonA = (ImageView)findViewById(R.id.imgButtonA);
		mImgDpadDown = (ImageView)findViewById(R.id.imgDpadDown);
		mImgDpadLeft = (ImageView)findViewById(R.id.imgDpadLeft);
		mImgDpadRight = (ImageView)findViewById(R.id.imgDpadRight);
		mImgDpadUp = (ImageView)findViewById(R.id.imgDpadUp);
		mImgLeftStick = (ImageView)findViewById(R.id.imgLeftStick);
		mImgLeftBumper = (ImageView)findViewById(R.id.imgLeftBumper);
		mImgLeftTrigger = (ImageView)findViewById(R.id.imgLeftTrigger);
		mImgButtonO = (ImageView)findViewById(R.id.imgButtonO);
		mImgRightStick = (ImageView)findViewById(R.id.imgRightStick);
		mImgRightBumper = (ImageView)findViewById(R.id.imgRightBumper);
		mImgRightTrigger = (ImageView)findViewById(R.id.imgRightTrigger);
		mImgLeftThumb = (ImageView)findViewById(R.id.imgLeftThumb);
		mImgRightThumb = (ImageView)findViewById(R.id.imgRightThumb);
		mImgButtonU = (ImageView)findViewById(R.id.imgButtonU);
		mImgButtonY = (ImageView)findViewById(R.id.imgButtonY);
		
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
		        				mImgButtonMenu.setVisibility(View.INVISIBLE);
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
		mTxtSystem.setText("Brand=" + android.os.Build.BRAND + " Model=" + android.os.Build.MODEL + " Device=" + Build.DEVICE +
				" Version=" + android.os.Build.VERSION.SDK_INT +
				" isRunningOnSupportedHardware="+mStoreFacade.isRunningOnSupportedHardware());
		
		setDrawable(mImgControllerO, Controller.BUTTON_O);
		setDrawable(mImgControllerU, Controller.BUTTON_U);
		setDrawable(mImgControllerY, Controller.BUTTON_Y);
		setDrawable(mImgControllerA, Controller.BUTTON_A);
		setDrawable(mImgControllerL1, Controller.BUTTON_L1);
		setDrawable(mImgControllerL2, KeyEvent.KEYCODE_BUTTON_L2);
		setDrawable(mImgControllerL3, Controller.BUTTON_L3);
		setDrawable(mImgControllerR1, Controller.BUTTON_R1);
		setDrawable(mImgControllerR2, KeyEvent.KEYCODE_BUTTON_R2);
		setDrawable(mImgControllerR3, Controller.BUTTON_R3);
		setDrawable(mImgControllerDpad, Controller.BUTTON_DPAD);
		setDrawable(mImgControllerDpadDown, Controller.BUTTON_DPAD_DOWN);
		setDrawable(mImgControllerDpadLeft, Controller.BUTTON_DPAD_LEFT);
		setDrawable(mImgControllerDpadRight, Controller.BUTTON_DPAD_RIGHT);
		setDrawable(mImgControllerDpadUp, Controller.BUTTON_DPAD_UP);
		setDrawable(mImgControllerBack, KeyEvent.KEYCODE_BACK);
		setDrawable(mImgControllerHome, Controller.BUTTON_HOME);
		setDrawable(mImgControllerMenu, Controller.BUTTON_MENU);
		setDrawable(mImgControllerNext, KeyEvent.KEYCODE_BUTTON_START);
		setDrawable(mImgControllerPower, KeyEvent.KEYCODE_BUTTON_MODE);
		setDrawable(mImgControllerPrevious, KeyEvent.KEYCODE_BUTTON_SELECT);
		setDrawable(mImgControllerLS, Controller.AXIS_LS_X);
		setDrawable(mImgControllerRS, Controller.AXIS_RS_X);
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
	    	mTxtKeyCode.setText("Click detected");
	    	mTxtController.setText("");
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
		if (null != mTxtKeyCode) {
			InputDevice device = motionEvent.getDevice();
			if (null != device) {
				mTxtKeyCode.setText("Original MotionEvent device=" + device.getName());
			}
		}
		//DebugInput.debugMotionEvent(motionEvent);		
		return super.dispatchGenericMotionEvent(motionEvent);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent keyEvent) {
		//Log.i(TAG, "dispatchKeyEvent");
		if (null != mTxtKeyCode) {
			InputDevice device = keyEvent.getDevice();
			if (null != device) {
				mTxtKeyCode.setText("Original KeyEvent device=" + device.getName() + " KeyCode=(" + keyEvent.getKeyCode() + ") "
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
		    	mImgDpadRight.setVisibility(View.VISIBLE);
		    } else {
		    	mImgDpadRight.setVisibility(View.INVISIBLE);
		    }		    
		    if (dpadX < -0.25f) {
		    	mImgDpadLeft.setVisibility(View.VISIBLE);
		    } else {
		    	mImgDpadLeft.setVisibility(View.INVISIBLE);
		    }
		}
		
		if (null == sButtonValues.get(playerNum).get(Controller.BUTTON_DPAD_DOWN) &&
			null == sButtonValues.get(playerNum).get(Controller.BUTTON_DPAD_UP)) {
		    float dpadY = sAxisValues.get(playerNum).get(MotionEvent.AXIS_HAT_Y);	    
		    if (dpadY > 0.25f) {
		    	mImgDpadDown.setVisibility(View.VISIBLE);
		    } else {
		    	mImgDpadDown.setVisibility(View.INVISIBLE);
		    }
		    
		    if (dpadY < -0.25f) {
		    	mImgDpadUp.setVisibility(View.VISIBLE);
		    } else {
		    	mImgDpadUp.setVisibility(View.INVISIBLE);
		    }
		}
	}
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent motionEvent) {
		//Log.i(TAG, "onGenericMotionEvent");
		//DebugInput.debugOuyaMotionEvent(motionEvent);
		
		int playerNum = 0;
		if (null != mTxtKeyCode2) {
			InputDevice device = motionEvent.getDevice();
			if (null != device) {
				mTxtKeyCode.setText("Original MotionEvent device=" + device.getName());
				Controller controller = Controller.getControllerByDeviceId(device.getId());
				if (null == controller) {
					mTxtKeyCode2.setText("Remapped MotionEvent device=unknown");
				} else {
					playerNum = controller.getPlayerNum();
					mTxtKeyCode2.setText("Remapped MotionEvent device=" + controller.getDeviceName()+" playerNum="+controller.getPlayerNum());
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
	    
	    mImgLeftStick.setX(AXIS_SCALER * (lsX * cos - lsY * sin));
	    mImgLeftThumb.setY(AXIS_SCALER * (lsX * cos - lsY * sin));
	    
	    mImgLeftStick.setY(AXIS_SCALER * (lsX * sin + lsY * cos));
	    mImgLeftThumb.setY(AXIS_SCALER * (lsX * sin + lsY * cos));
	    
	    mImgRightStick.setX(AXIS_SCALER * (rsX * cos - rsY * sin));
	    mImgRightThumb.setX(AXIS_SCALER * (rsX * cos - rsY * sin));
	    
	    mImgRightStick.setY(AXIS_SCALER * (rsX * sin + rsY * cos));
	    mImgRightThumb.setY(AXIS_SCALER * (rsX * sin + rsY * cos));
		
		//Log.i(TAG, "Unrecognized GenericMotionEvent="+motionEvent.getAction());
	    
	    if (l2 > 0.25f) {
	    	mImgLeftTrigger.setVisibility(View.VISIBLE);
	    } else {
	    	mImgLeftTrigger.setVisibility(View.INVISIBLE);
	    }
	    
	    if (r2 > 0.25f) {
	    	mImgRightTrigger.setVisibility(View.VISIBLE);
	    } else {
	    	mImgRightTrigger.setVisibility(View.INVISIBLE);
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
			mTxtKeyCode2.setText("Remapped onKeyDown device=" + controller.getDeviceName() + " KeyCode=(" + keyCode + ") "
					+ DebugInput.debugGetButtonName(keyCode) + " playerNum="+controller.getPlayerNum());
		} else {
			mTxtKeyCode2.setText("Remapped onKeyDown device=unknown KeyCode=(" + keyCode + ") "
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
			mImgLeftBumper.setVisibility(View.VISIBLE);
			break;
		case KeyEvent.KEYCODE_BUTTON_L2:
			mImgLeftTrigger.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_L3:
			mImgLeftStick.setVisibility(View.INVISIBLE);
			mImgLeftThumb.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_R1:
			mImgRightBumper.setVisibility(View.VISIBLE);
			break;
		case KeyEvent.KEYCODE_BUTTON_R2:
			mImgRightTrigger.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_R3:
			mImgRightStick.setVisibility(View.INVISIBLE);
			mImgRightThumb.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_O:
			mImgButtonO.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_U:
			mImgButtonU.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_Y:
			mImgButtonY.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_A:
			mImgButtonA.setVisibility(View.VISIBLE);
			break;
		case Controller.BUTTON_DPAD_DOWN:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_DOWN, true);
				mImgDpadDown.setVisibility(View.VISIBLE);
			}
			break;
		case Controller.BUTTON_DPAD_LEFT:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_LEFT, true);
				mImgDpadLeft.setVisibility(View.VISIBLE);
			}
			break;
		case Controller.BUTTON_DPAD_RIGHT:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_RIGHT, true);
				mImgDpadRight.setVisibility(View.VISIBLE);
			}
			break;
		case Controller.BUTTON_DPAD_UP:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_UP, true);
				mImgDpadUp.setVisibility(View.VISIBLE);
			}
			break;
		case Controller.BUTTON_MENU:
			mImgButtonMenu.setVisibility(View.VISIBLE);
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
			mTxtKeyCode2.setText("Remapped onKeyUp device=" + controller.getDeviceName() + " KeyCode=(" + keyCode + ") "
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
			mImgLeftBumper.setVisibility(View.INVISIBLE);
			break;
		case KeyEvent.KEYCODE_BUTTON_L2:
			mImgLeftTrigger.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_L3:
			mImgLeftStick.setVisibility(View.VISIBLE);
			mImgLeftThumb.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_R1:
			mImgRightBumper.setVisibility(View.INVISIBLE);
			break;
		case KeyEvent.KEYCODE_BUTTON_R2:
			mImgRightTrigger.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_R3:
			mImgRightStick.setVisibility(View.VISIBLE);
			mImgRightThumb.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_O:
			mImgButtonO.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_U:
			mImgButtonU.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_Y:
			mImgButtonY.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_A:
			mImgButtonA.setVisibility(View.INVISIBLE);
			break;
		case Controller.BUTTON_DPAD_DOWN:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_DOWN, false);
				mImgDpadDown.setVisibility(View.INVISIBLE);
			}
			break;
		case Controller.BUTTON_DPAD_LEFT:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_LEFT, false);
				mImgDpadLeft.setVisibility(View.INVISIBLE);
			}
			break;
		case Controller.BUTTON_DPAD_RIGHT:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_RIGHT, false);
				mImgDpadRight.setVisibility(View.INVISIBLE);
			}
			break;
		case Controller.BUTTON_DPAD_UP:
			if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK ) {
				updateDPad(playerNum);
			} else {
				sButtonValues.get(playerNum).put(Controller.BUTTON_DPAD_UP, false);
				mImgDpadUp.setVisibility(View.INVISIBLE);
			}
			break;
		case Controller.BUTTON_MENU:
			//wait 1 second
			break;
		}
		
		return true;
	}	
}