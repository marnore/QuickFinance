package lt.marius.converter.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

import java.util.Locale;

/**
 * Created by marius on 7/30/14.
 */
public class CompatButton extends Button {

    public CompatButton(Context context) {
        super(context);
    }

    public CompatButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CompatButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text.toString().toUpperCase(Locale.getDefault()), type);
    }
}
