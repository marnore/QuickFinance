package lt.marius.converter.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.util.Arrays;

/**
 * Created by marius on 8/2/14.
 */
public class BorderedImageView extends ImageView {

    public BorderedImageView(Context context) {
        super(context);
        init();
    }

    public BorderedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BorderedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private static Paint paint;

    private void init() {
        if (paint == null) {
            paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.parseColor("#999999"));
            paint.setAntiAlias(true);
            paint.setStrokeWidth(0);
        }
    }

    private int actW, actH;
    private RectF bounds;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        bounds = null;
    }

    private void computeBounds() {
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        getImageMatrix().getValues(f);

        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        final float scaleX = f[Matrix.MSCALE_X];
        final float scaleY = f[Matrix.MSCALE_Y];

        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        final Drawable d = getDrawable();
        final int origW = d.getIntrinsicWidth();
        final int origH = d.getIntrinsicHeight();

        // Calculate the actual dimensions
        actW = Math.round(origW * scaleX);
        actH = Math.round(origH * scaleY);

        int measuredH = getMeasuredHeight();
        int measuredW = getMeasuredWidth();

        bounds = new RectF();
        bounds.top = (measuredH - actH) / 2.f + 1;
        bounds.bottom = bounds.top + actH - 1;
        bounds.left = (measuredW - actW) / 2.f + 1;
        bounds.right = bounds.left + actW - 1;
    }

        @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (bounds == null) {
            computeBounds();
        }
        canvas.drawRect(bounds, paint);
    }
}
