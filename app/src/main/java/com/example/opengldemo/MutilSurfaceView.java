package com.example.opengldemo;

import android.content.Context;
import android.util.AttributeSet;
import org.jetbrains.annotations.NotNull;

/**
 * 共享纹理的SurfaceView
 */
public class MutilSurfaceView extends CustomGlSurfaceView {
    private MutilRender mutilRender;

    public MutilSurfaceView(@NotNull Context ctx) {
        super(ctx);
        init(ctx);
    }

    public MutilSurfaceView(@NotNull Context ctx, @NotNull AttributeSet attrs) {
        super(ctx, attrs);
        init(ctx);
    }

    public MutilSurfaceView(@NotNull Context ctx, @NotNull AttributeSet attrs, int defStyleAttr) {
        super(ctx, attrs, defStyleAttr);
        init(ctx);
    }

    private void init(Context ctx) {
        mutilRender = new MutilRender(ctx);
        setRender(mutilRender);
    }

    public void setTextureId(int textureId, int index) {
        if (mutilRender != null) {
            mutilRender.setTextureId(textureId,index);
        }
    }


}
