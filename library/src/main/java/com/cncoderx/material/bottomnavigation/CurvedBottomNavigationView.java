package com.cncoderx.material.bottomnavigation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.List;

import static com.google.android.material.shape.MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS;

public class CurvedBottomNavigationView extends BottomNavigationView {
    private final MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
    private final Rect fabContentRect = new Rect();

    public CurvedBottomNavigationView(Context context) {
        this(context, null);
    }

    public CurvedBottomNavigationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CurvedBottomNavigationView);
        int elevation = a.getDimensionPixelSize(R.styleable.CurvedBottomNavigationView_elevation, 0);
        float fabCradleMargin = a.getDimensionPixelOffset(R.styleable.CurvedBottomNavigationView_fabCradleMargin, 0);
        float fabCornerRadius =
                a.getDimensionPixelOffset(R.styleable.CurvedBottomNavigationView_fabCradleRoundedCornerRadius, 0);
        float fabVerticalOffset =
                a.getDimensionPixelOffset(R.styleable.CurvedBottomNavigationView_fabCradleVerticalOffset, 0);
        a.recycle();

        BottomNavigationTopEdgeTreatment topEdgeTreatment =
                new BottomNavigationTopEdgeTreatment(fabCradleMargin, fabCornerRadius, fabVerticalOffset);
        ShapeAppearanceModel shapeAppearanceModel =
                ShapeAppearanceModel.builder().setTopEdge(topEdgeTreatment).build();
        materialShapeDrawable.setShapeAppearanceModel(shapeAppearanceModel);
        materialShapeDrawable.setShadowCompatibilityMode(SHADOW_COMPAT_MODE_ALWAYS);
        materialShapeDrawable.setPaintStyle(Paint.Style.FILL);
        materialShapeDrawable.initializeElevationOverlay(context);
        setElevation(elevation);
        DrawableCompat.setTint(materialShapeDrawable, ContextCompat.getColor(context, android.R.color.white));
        ViewCompat.setBackground(this, materialShapeDrawable);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        MaterialShapeUtils.setParentAbsoluteElevation(this, materialShapeDrawable);

        // Automatically don't clip children for the parent view of BottomAppBar. This allows the shadow
        // to be drawn outside the bounds.
        if (getParent() instanceof ViewGroup) {
            ((ViewGroup) getParent()).setClipChildren(false);
        }
    }

    @Override
    public void setElevation(float elevation) {
        if (materialShapeDrawable != null) {
            materialShapeDrawable.setElevation(elevation);
        }
    }

    boolean setFabDiameter(@Px int diameter) {
        if (diameter != getTopEdgeTreatment().getFabDiameter()) {
            getTopEdgeTreatment().setFabDiameter(diameter);
            materialShapeDrawable.invalidateSelf();
            return true;
        }
        return false;
    }

    @NonNull
    private BottomNavigationTopEdgeTreatment getTopEdgeTreatment() {
        return (BottomNavigationTopEdgeTreatment)
                materialShapeDrawable.getShapeAppearanceModel().getTopEdge();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        View dependentView = findDependentView();
        if (dependentView != null && !ViewCompat.isLaidOut(dependentView)) {
            setCutoutState();
        }
    }

    @Nullable
    private View findDependentView() {
        if (!(getParent() instanceof CoordinatorLayout)) {
            // If we aren't in a CoordinatorLayout we won't have a dependent FAB.
            return null;
        }

        List<View> dependents = ((CoordinatorLayout) getParent()).getDependents(this);
        for (View v : dependents) {
            if (v instanceof FloatingActionButton/* || v instanceof ExtendedFloatingActionButton*/) {
                return v;
            }
        }

        return null;
    }

    private void setCutoutState() {
        // Layout all elements related to the positioning of the fab.
        getTopEdgeTreatment().setHorizontalOffset(0);
        FloatingActionButton fab = (FloatingActionButton) findDependentView();
        if (fab != null) {
            materialShapeDrawable.setInterpolation(fab.isOrWillBeShown() ? 1 : 0);
            float fabTranslationY = getTopEdgeTreatment().getCradleVerticalOffset();
            fab.setTranslationY(fabTranslationY);
            fab.getMeasuredContentRect(fabContentRect);
            int height = fabContentRect.height();
            // Set the cutout diameter based on the height of the fab.
            setFabDiameter(height);
        } else {
            materialShapeDrawable.setInterpolation(0);
        }
    }
}
