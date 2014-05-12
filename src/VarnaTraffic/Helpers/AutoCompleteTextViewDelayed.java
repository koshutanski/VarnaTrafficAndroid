package VarnaTraffic.Helpers;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * Created 12.5.2014 г..
 */
public class AutoCompleteTextViewDelayed extends AutoCompleteTextView {

    // FIELDS
    private final static int DELAY = 750;
    Handler mHandler = new Handler();
    int mDelay;
    Runnable mRunnable;

    // CONSTRUCTOR
    public AutoCompleteTextViewDelayed(Context context) {
        this(context, null);
    }

    public AutoCompleteTextViewDelayed(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDelay(DELAY);
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    // PUBLIC METHODS
    @Override
    protected void performFiltering(final CharSequence text, final int keyCode) {
        mHandler.removeCallbacks(mRunnable);
        mRunnable = new Runnable() {
            @Override
            public void run() {
                AutoCompleteTextViewDelayed.super.performFiltering(text, keyCode);
            }
        };

        // restart handler
        mHandler.postDelayed(mRunnable, mDelay);
    }

    public int getDelay() {
        return mDelay;
    }

    public void setDelay(int delay) {
        mDelay = delay;
    }

}
