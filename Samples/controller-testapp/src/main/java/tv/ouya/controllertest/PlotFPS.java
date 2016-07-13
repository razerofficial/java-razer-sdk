package tv.ouya.controllertest;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

public class PlotFPS extends View {

	private int mPlotSize = 128;
	public TextView mFpsText = null;
	public TextView mCpu1Text = null;
	public TextView mCpu2Text = null;
	public TextView mCpu3Text = null;
	public TextView mCpu4Text = null;
	public TextView mKeyDownText = null;
	public TextView mKeyUpText = null;
	public TextView mGenericMotionText = null;
	private double[] mCpuStats = new double[4];

	public double mKeyDownTime = 0.0;
	public double mKeyUpTime = 0.0;
	public double mGenericMotionTime = 0.0;
	
	private MetricsCPU mMetricsCPU = null;

    private long mTimer = -1;
    private Double[] mCounts = null;
    private int mTime = 0;

	private void Init()
        throws IOException
	{
		mMetricsCPU = new MetricsCPU();

		mCounts = new Double[mPlotSize];
		for (int index = 0; index < mPlotSize; ++index)
		{
			mCounts[index] = 0.0;
		}
	}
	
	public void Quit()
	{
        try {
            mMetricsCPU.close();
        } catch (IOException e) {
            Log.e("OUYAPlotFPS", "Error closing metrics stream", e);
        }
		Log.i("OuyaPlotFPS", "Quitting...", null);
	}
	
    public PlotFPS(Context context, AttributeSet attrs)
        throws IOException {
        super(context, attrs);
        Init();
    }

    public PlotFPS(Context context, AttributeSet attrs, int defStyle)
            throws IOException  {
        super(context, attrs, defStyle);
        Init();
    }

    public PlotFPS(Context context)
            throws IOException  {
        super(context);
        Init();
    }
    
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		if (null == mCounts)
		{
			return;
		}
		
		//timer to update fps label
		if (mTimer < System.nanoTime())
		{
			mTimer = System.nanoTime() + 1000000000;
			if (null != mFpsText)
			{
				mFpsText.setText(String.format("FPS: %.2f", 1.0 / (System.nanoTime() / 1000000000.0 - getDrawingTime() / 1000.0)));
			}

            mMetricsCPU.readUsage(mCpuStats);
			if (null != mCpu1Text)
			{
				mCpu1Text.setText(String.format("CPU1: %.2f", mCpuStats[0]));
			}
			if (null != mCpu2Text)
			{
				mCpu2Text.setText(String.format("CPU2: %.2f", mCpuStats[1]));
			}
			if (null != mCpu3Text)
			{
				mCpu3Text.setText(String.format("CPU3: %.2f", mCpuStats[2]));
			}
			if (null != mCpu4Text)
			{
				mCpu4Text.setText(String.format("CPU4: %.2f", mCpuStats[3]));
			}
			if (null != mKeyDownText)
			{
				mKeyDownText.setText(String.format("KeyDown: %.2f ms", 1000.0 * mKeyDownTime));
			}
			if (null != mKeyUpText)
			{
				mKeyUpText.setText(String.format("KeyUp: %.2f ms", 1000.0 * mKeyUpTime));
			}
			if (null != mGenericMotionText)
			{
				mGenericMotionText.setText(String.format("Trackpad: %.2f ms", 1000.0 * mGenericMotionTime));
			}
		}
			
		mCounts[mTime] = 1.0 / (System.nanoTime() / 1000000000.0 - getDrawingTime() / 1000.0);
		++mTime;
		if (mTime >= mPlotSize)
		{
			mTime = 0;
		}
		
		/*
		drawBackground(canvas);
		
		Paint paint1 = new Paint();		
		paint1.setShader(new LinearGradient(0, 0, 4, canvas.getHeight() / 2, Color.argb(255, 255, 128, 0), Color.argb(255, 64, 0, 0), TileMode.MIRROR));
		
		Paint paint2 = new Paint();
		paint2.setShader(new LinearGradient(0, 0, 4, canvas.getHeight() / 2, Color.BLACK, Color.argb(255, 128, 255, 255), TileMode.MIRROR));
		
		Rect ourRect = new Rect();
		for (int index = 0; index < mPlotSize; ++index)
		{
			int readIndex = (index + mTime) % mPlotSize;
			Double size = mCounts[readIndex] / 4;
			ourRect.set(index*8, (int)(canvas.getHeight()), (index+1)*8, (int)(canvas.getHeight() - size));
			
			if ((readIndex/8 % 2) == 0)
			{
				canvas.drawRect(ourRect, paint1);
			}
			else
			{
				canvas.drawRect(ourRect, paint2);
			}
		}
		*/
		
		invalidate();		
		
	}
}
