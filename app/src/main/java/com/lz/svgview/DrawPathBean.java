package com.lz.svgview;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

/**
 * -----------作者----------日期----------变更内容-----
 * -          刘泽      2018-11-02       创建class
 */
public class DrawPathBean {

    public Path mPath;
    public int mColor = -1;
    public boolean isSelect = false;

    public DrawPathBean(Path path) {
        mPath = path;
    }

    public void drawMap(Canvas canvas, Paint paint) {
        if (isSelect) {
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(mPath, paint);

            paint.setStrokeWidth(1);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath, paint);
        } else {

            paint.setColor(mColor);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(mPath, paint);


            paint.setStrokeWidth(1);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath, paint);
        }
    }

    public boolean isClick(float x, float y) {

        RectF rectF = new RectF();
        mPath.computeBounds(rectF, true);
        Region region = new Region();
        region.setPath(mPath, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
        return region.contains((int) x, (int) y);

    }
}
