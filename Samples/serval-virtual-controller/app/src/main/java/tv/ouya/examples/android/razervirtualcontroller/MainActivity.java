package tv.ouya.examples.android.razervirtualcontroller;

import android.app.Activity;
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
import java.lang.reflect.Method;
import java.util.HashMap;

import com.razerzone.store.sdk.engine.java.DebugInput;
import com.razerzone.store.sdk.engine.java.RazerController;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();

	private static final Float AXIS_SCALER = 4f;

	private static final int BUTTON_MENU = 82;

	private TextView mTxtSystem = null;
	private TextView mTxtController = null;
	private TextView mTxtKeyCode = null;
	private TextView mTxtKeyCode2 = null;
	private ImageView mImgDpadDown = null;
	private ImageView mImgDpadLeft = null;
	private ImageView mImgDpadRight = null;
	private ImageView mImgDpadUp = null;
	private ImageView mImgLeftStick = null;
	private ImageView mImgLeftBumper = null;
	private ImageView mImgLeftTrigger = null;
	private ImageView mImgRightStick = null;
	private ImageView mImgRightBumper = null;
	private ImageView mImgRightTrigger = null;
	private ImageView mImgLeftThumb = null;
	private ImageView mImgRightThumb = null;
	private ImageView mImgButtonA = null;
	private ImageView mImgButtonB = null;
	private ImageView mImgButtonX = null;
	private ImageView mImgButtonY = null;
	private ImageView mImgButtonBack = null;
	private ImageView mImgButtonNext = null;
	private ImageView mImgButtonHome = null;
	private ImageView mImgButtonPrevious = null;
	private ImageView mImgButtonPower = null;

	// keep track when menu button was seen
	private Boolean mWaitToExit = true;
	private float mHomeDetected = 0f;
	private float mPowerDetected = 0f;

	private static SparseArray<HashMap<Integer, Float>> sAxisValues = new SparseArray<HashMap<Integer, Float>>();
	private static SparseArray<HashMap<Integer, Boolean>> sButtonValues = new SparseArray<HashMap<Integer, Boolean>>();

	static {
		for (int index = 0; index < RazerController.MAX_CONTROLLERS; ++index) {
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

		ViewGroup mainLayout = (ViewGroup) this.findViewById(android.R.id.content);
		mainLayout.setOnClickListener(mClickListener);

		mainLayout.setKeepScreenOn(true);

		mTxtSystem = (TextView) findViewById(R.id.txtSystem);
		mTxtController = (TextView) findViewById(R.id.txtController);
		mTxtKeyCode = (TextView) findViewById(R.id.txtKeyCode);
		mTxtKeyCode2 = (TextView) findViewById(R.id.txtKeyCode2);
		mImgButtonA = (ImageView) findViewById(R.id.imgButtonA);
		mImgDpadDown = (ImageView) findViewById(R.id.imgDpadDown);
		mImgDpadLeft = (ImageView) findViewById(R.id.imgDpadLeft);
		mImgDpadRight = (ImageView) findViewById(R.id.imgDpadRight);
		mImgDpadUp = (ImageView) findViewById(R.id.imgDpadUp);
		mImgLeftStick = (ImageView) findViewById(R.id.imgLeftStick);
		mImgLeftBumper = (ImageView) findViewById(R.id.imgLeftBumper);
		mImgLeftTrigger = (ImageView) findViewById(R.id.imgLeftTrigger);
		mImgRightStick = (ImageView) findViewById(R.id.imgRightStick);
		mImgRightBumper = (ImageView) findViewById(R.id.imgRightBumper);
		mImgRightTrigger = (ImageView) findViewById(R.id.imgRightTrigger);
		mImgLeftThumb = (ImageView) findViewById(R.id.imgLeftThumb);
		mImgRightThumb = (ImageView) findViewById(R.id.imgRightThumb);
		mImgButtonA = (ImageView) findViewById(R.id.imgButtonA);
		mImgButtonB = (ImageView) findViewById(R.id.imgButtonB);
		mImgButtonX = (ImageView) findViewById(R.id.imgButtonX);
		mImgButtonY = (ImageView) findViewById(R.id.imgButtonY);
		mImgButtonBack = (ImageView) findViewById(R.id.imgButtonBack);
		mImgButtonNext = (ImageView) findViewById(R.id.imgButtonNext);
		mImgButtonHome = (ImageView) findViewById(R.id.imgButtonHome);
		mImgButtonPrevious = (ImageView) findViewById(R.id.imgButtonPrevious);
		mImgButtonPower = (ImageView) findViewById(R.id.imgButtonPower);

		// spawn thread to toggle menu button
		Thread timer = new Thread() {
			public void run() {
				while (mWaitToExit) {
					if (mHomeDetected != 0 &&
							mHomeDetected < System.nanoTime()) {
						mHomeDetected = 0;
						Runnable runnable = new Runnable() {
							public void run() {
								mImgButtonHome.setVisibility(View.INVISIBLE);
							}
						};
						runOnUiThread(runnable);

					}
					if (mPowerDetected != 0 &&
							mPowerDetected < System.nanoTime()) {
						mPowerDetected = 0;
						Runnable runnable = new Runnable() {
							public void run() {
								mImgButtonPower.setVisibility(View.INVISIBLE);
							}
						};
						runOnUiThread(runnable);

					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
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
				" Version=" + android.os.Build.VERSION.SDK_INT);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWaitToExit = false; //let timer exit
	}

	private OnClickListener mClickListener = new OnClickListener() {
		public void onClick(View v) {
			mTxtKeyCode.setText("Click detected");
			mTxtController.setText("");
		}
	};

	void updateDPad(int playerNum) {
		if (null == sButtonValues.get(playerNum).get(RazerController.BUTTON_DPAD_LEFT) &&
				null == sButtonValues.get(playerNum).get(RazerController.BUTTON_DPAD_RIGHT)) {
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

		if (null == sButtonValues.get(playerNum).get(RazerController.BUTTON_DPAD_DOWN) &&
				null == sButtonValues.get(playerNum).get(RazerController.BUTTON_DPAD_UP)) {
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
		DebugInput.debugMotionEvent(motionEvent);
		//DebugInput.debugOuyaMotionEvent(motionEvent);

		int playerNum = 0;

		float dpadX = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_X);
		float dpadY = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_Y);
		sAxisValues.get(playerNum).put(MotionEvent.AXIS_HAT_X, dpadX);
		sAxisValues.get(playerNum).put(MotionEvent.AXIS_HAT_Y, dpadY);
		updateDPad(playerNum);

		float lsX = motionEvent.getAxisValue(RazerController.AXIS_LS_X);
		float lsY = motionEvent.getAxisValue(RazerController.AXIS_LS_Y);
		float rsX = motionEvent.getAxisValue(RazerController.AXIS_RS_X);
		float rsY = motionEvent.getAxisValue(RazerController.AXIS_RS_Y);
		float l2 = motionEvent.getAxisValue(RazerController.AXIS_L2);
		float r2 = motionEvent.getAxisValue(RazerController.AXIS_R2);

		//rotate input by N degrees to match image
		float degrees = 135;
		float radians = degrees / 180f * 3.14f;
		float cos = (float) Math.cos(radians);
		float sin = (float) Math.sin(radians);

		mImgLeftStick.setX(AXIS_SCALER * (lsX * cos - lsY * sin));
		mImgLeftThumb.setY(AXIS_SCALER * (lsX * cos - lsY * sin));

		mImgLeftStick.setY(AXIS_SCALER * (lsX * sin + lsY * cos));
		mImgLeftThumb.setY(AXIS_SCALER * (lsX * sin + lsY * cos));

		mImgRightStick.setX(AXIS_SCALER * (rsX * cos - rsY * sin));
		mImgRightThumb.setX(AXIS_SCALER * (rsX * cos - rsY * sin));

		mImgRightStick.setY(AXIS_SCALER * (rsX * sin + rsY * cos));
		mImgRightThumb.setY(AXIS_SCALER * (rsX * sin + rsY * cos));

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
		//Log.i(TAG, "onKeyDown keyCode="+keyEvent.getKeyCode());

		InputDevice device = keyEvent.getDevice();
		if (null != device) {
			String text = "KeyCode=(" + keyCode + ") "
					+ DebugInput.debugGetButtonName(keyCode) + " source=" + keyEvent.getSource()
					+ " fallback=" + (keyEvent.getFlags() == KeyEvent.FLAG_FALLBACK);
			if (android.os.Build.VERSION.SDK_INT >= 19) {
				try {
					Method m = device.getClass().getDeclaredMethod("getVendorId");
					text += " vendor=" + Integer.toHexString((Integer) m.invoke(device, (Object[]) null));
				} catch (Exception e) {
				}

				try {
					Method m = device.getClass().getDeclaredMethod("getProductId");
					text += " product=" + Integer.toHexString((Integer) m.invoke(device, (Object[]) null));
				} catch (Exception e) {
				}
			}
			mTxtKeyCode2.setText(text);
		}

		int playerNum = 0;

		switch (keyCode) {
			case RazerController.BUTTON_DPAD_DOWN:
				if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK) {
					updateDPad(playerNum);
				} else {
					sButtonValues.get(playerNum).put(keyCode, true);
					mImgDpadDown.setVisibility(View.VISIBLE);
				}
				break;
			case RazerController.BUTTON_DPAD_LEFT:
				if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK) {
					updateDPad(playerNum);
				} else {
					sButtonValues.get(playerNum).put(keyCode, true);
					mImgDpadLeft.setVisibility(View.VISIBLE);
				}
				break;
			case RazerController.BUTTON_DPAD_RIGHT:
				if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK) {
					updateDPad(playerNum);
				} else {
					sButtonValues.get(playerNum).put(keyCode, true);
					mImgDpadRight.setVisibility(View.VISIBLE);
				}
				break;
			case RazerController.BUTTON_DPAD_UP:
				if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK) {
					updateDPad(playerNum);
				} else {
					sButtonValues.get(playerNum).put(keyCode, true);
					mImgDpadUp.setVisibility(View.VISIBLE);
				}
				break;
			case RazerController.BUTTON_L1:
				mImgLeftBumper.setVisibility(View.VISIBLE);
				break;
			case RazerController.BUTTON_L2:
				mImgLeftTrigger.setVisibility(View.VISIBLE);
				break;
			case RazerController.BUTTON_L3:
				mImgLeftStick.setVisibility(View.INVISIBLE);
				mImgLeftThumb.setVisibility(View.VISIBLE);
				break;
			case RazerController.BUTTON_R1:
				mImgRightBumper.setVisibility(View.VISIBLE);
				break;
			case RazerController.BUTTON_R2:
				mImgRightTrigger.setVisibility(View.VISIBLE);
				break;
			case RazerController.BUTTON_R3:
				mImgRightStick.setVisibility(View.INVISIBLE);
				mImgRightThumb.setVisibility(View.VISIBLE);
				break;
			case RazerController.BUTTON_A:
				mImgButtonA.setVisibility(View.VISIBLE);
				break;
			case RazerController.BUTTON_B:
				mImgButtonB.setVisibility(View.VISIBLE);
				break;
			case RazerController.BUTTON_X:
				mImgButtonX.setVisibility(View.VISIBLE);
				break;
			case RazerController.BUTTON_Y:
				mImgButtonY.setVisibility(View.VISIBLE);
				break;
			case RazerController.BUTTON_BACK:
				mImgButtonBack.setVisibility(View.VISIBLE);
				break;
			case RazerController.BUTTON_NEXT:
				mImgButtonNext.setVisibility(View.VISIBLE);
				break;
			case BUTTON_MENU:
			case RazerController.BUTTON_HOME:
				mImgButtonHome.setVisibility(View.VISIBLE);
				mHomeDetected = System.nanoTime() + 1000000000;
				break;
			case RazerController.BUTTON_PREVIOUS:
				mImgButtonPrevious.setVisibility(View.VISIBLE);
				break;
			case RazerController.BUTTON_POWER:
				mImgButtonPower.setVisibility(View.VISIBLE);
				mPowerDetected = System.nanoTime() + 1000000000;
				break;
			default:
				Log.i(TAG, "Unrecognized KeyDown=" + keyCode);
				break;
		}

		return true;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
		//Log.i(TAG, "onKeyUp keyCode="+keyEvent.getKeyCode());

		int playerNum = 0;

		switch (keyCode) {
			case RazerController.BUTTON_DPAD_DOWN:
				if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK) {
					updateDPad(playerNum);
				} else {
					sButtonValues.get(playerNum).put(keyCode, false);
					mImgDpadDown.setVisibility(View.INVISIBLE);
				}
				break;
			case RazerController.BUTTON_DPAD_LEFT:
				if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK) {
					updateDPad(playerNum);
				} else {
					sButtonValues.get(playerNum).put(keyCode, false);
					mImgDpadLeft.setVisibility(View.INVISIBLE);
				}
				break;
			case RazerController.BUTTON_DPAD_RIGHT:
				if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK) {
					updateDPad(playerNum);
				} else {
					sButtonValues.get(playerNum).put(keyCode, false);
					mImgDpadRight.setVisibility(View.INVISIBLE);
				}
				break;
			case RazerController.BUTTON_DPAD_UP:
				if (keyEvent.getSource() == InputDevice.SOURCE_JOYSTICK) {
					updateDPad(playerNum);
				} else {
					sButtonValues.get(playerNum).put(keyCode, false);
					mImgDpadUp.setVisibility(View.INVISIBLE);
				}
				break;
			case RazerController.BUTTON_L1:
				mImgLeftBumper.setVisibility(View.INVISIBLE);
				break;
			case RazerController.BUTTON_L2:
				mImgLeftTrigger.setVisibility(View.INVISIBLE);
				break;
			case RazerController.BUTTON_L3:
				mImgLeftStick.setVisibility(View.VISIBLE);
				mImgLeftThumb.setVisibility(View.INVISIBLE);
				break;
			case RazerController.BUTTON_R1:
				mImgRightBumper.setVisibility(View.INVISIBLE);
				break;
			case RazerController.BUTTON_R2:
				mImgRightTrigger.setVisibility(View.INVISIBLE);
				break;
			case RazerController.BUTTON_R3:
				mImgRightStick.setVisibility(View.VISIBLE);
				mImgRightThumb.setVisibility(View.INVISIBLE);
				break;
			case RazerController.BUTTON_A:
				mImgButtonA.setVisibility(View.INVISIBLE);
				break;
			case RazerController.BUTTON_B:
				mImgButtonB.setVisibility(View.INVISIBLE);
				break;
			case RazerController.BUTTON_X:
				mImgButtonX.setVisibility(View.INVISIBLE);
				break;
			case RazerController.BUTTON_Y:
				mImgButtonY.setVisibility(View.INVISIBLE);
				break;
			case RazerController.BUTTON_BACK:
				mImgButtonBack.setVisibility(View.INVISIBLE);
				break;
			case RazerController.BUTTON_NEXT:
				mImgButtonNext.setVisibility(View.INVISIBLE);
				break;
			case RazerController.BUTTON_HOME:
				//wait 1 sec
				break;
			case RazerController.BUTTON_PREVIOUS:
				mImgButtonPrevious.setVisibility(View.INVISIBLE);
				break;
			case RazerController.BUTTON_POWER:
				//wait 1 sec
				break;
		}

		return true;
	}
}
