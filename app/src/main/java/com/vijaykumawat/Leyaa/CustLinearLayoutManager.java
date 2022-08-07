package com.vijaykumawat.Leyaa;

import android.content.Context;
import android.util.AttributeSet;

import androidx.recyclerview.widget.LinearLayoutManager;

public class CustLinearLayoutManager extends LinearLayoutManager {

    public CustLinearLayoutManager(Context context) {
        super(context);
    }

    public CustLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CustLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

// Something is happening here

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }
}