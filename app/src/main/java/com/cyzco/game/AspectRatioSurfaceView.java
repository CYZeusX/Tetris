package com.cyzco.game;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.util.Log;

public class AspectRatioSurfaceView extends SurfaceView
{

    // Define aspect ratio (Width / Height)
    private static final float TARGET_ASPECT_RATIO = 10.0f / 18.0f;

    public AspectRatioSurfaceView(Context context) {
        super(context);
    }

    public AspectRatioSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

        int calculatedWidth;
        int calculatedHeight;

        // Determine the dominant dimension based on the available space and ratio
        // Calculate width based on height
        int widthBasedOnHeight = (int) (originalHeight * TARGET_ASPECT_RATIO);
        // Calculate height based on width
        int heightBasedOnWidth = (int) (originalWidth / TARGET_ASPECT_RATIO);

        // Choose the calculation that fits within the constraints
        // This logic tries to maximize size while respecting the ratio and parent constraints
        if (originalWidth > 0 && originalHeight > 0)
        {
            // Both dimensions constrained by parent
            if (widthBasedOnHeight <= originalWidth) {
                // Using height as the limiter fits within the width constraint
                calculatedWidth = widthBasedOnHeight;
                calculatedHeight = originalHeight;
            }

            else {
                // Using width as the limiter fits within the height constraint
                calculatedWidth = originalWidth;
                calculatedHeight = heightBasedOnWidth;
            }
        }

        else if (originalWidth > 0) {
            // Only width is constrained
            calculatedWidth = originalWidth;
            calculatedHeight = heightBasedOnWidth;
        }

        else if (originalHeight > 0) {
            // Only height is constrained
            calculatedWidth = widthBasedOnHeight;
            calculatedHeight = originalHeight;
        }

        else {
            // No constraints, let super handle (or set a default size)
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            Log.w("AspectRatioSurfaceView", "No constraints provided to onMeasure.");
            return;
        }

        // Ensure we don't exceed the original measure spec if modes were AT_MOST
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST && calculatedWidth > originalWidth) {
            calculatedWidth = originalWidth;
            calculatedHeight = (int) (calculatedWidth / TARGET_ASPECT_RATIO);
        }

        if (heightMode == MeasureSpec.AT_MOST && calculatedHeight > originalHeight) {
            calculatedHeight = originalHeight;
            calculatedWidth = (int) (calculatedHeight * TARGET_ASPECT_RATIO);
        }

        setMeasuredDimension(calculatedWidth, calculatedHeight);
    }
}