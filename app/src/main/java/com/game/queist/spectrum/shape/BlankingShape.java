package com.game.queist.spectrum.shape;

import android.content.Context;

import com.game.queist.spectrum.activities.PlayScreen;
import com.game.queist.spectrum.utils.ShapeUtils;
import com.game.queist.spectrum.utils.Utility;

public class BlankingShape extends LaneShape {

    public BlankingShape(Context context, float radius, float width, float blendRate) {
        super(context, radius, width, blendRate);
    }

    public BlankingShape(Context context) {
        super(context);
    }

    @Override
    protected void initBufferResources() {
        int[] sideColors = new int[PlayScreen.SIDE_NUM];
        for (int i = 0; i < PlayScreen.SIDE_NUM; i++) {
            sideColors[i] = Utility.getBGRGB(i);
        }

        positions = ShapeUtils.buildDiskPositions(radius, width);
        colors = ShapeUtils.buildDiskColors(sideColors, blendRate);
        normals = ShapeUtils.buildDiskNormals();
        texCoords = ShapeUtils.buildDiskTexCoords();

        indices = ShapeUtils.buildDiskIndices();
    }
}
