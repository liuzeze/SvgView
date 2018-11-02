package com.lz.svgview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * -----------作者----------日期----------变更内容-----
 * -          刘泽      2018-11-02       创建class
 */
public class SvgView extends View {

    private Paint mPaint;
    private int[] colors = {Color.BLUE, Color.YELLOW, Color.GREEN};
    private DrawPathBean mDrawPathBean;


    public SvgView(Context context) {
        super(context);
        init();
    }


    public SvgView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SvgView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    ArrayList<DrawPathBean> pathList = new ArrayList<>();
    private RectF mRectF = new RectF();

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        new HandlerThread(getClass().getName()) {
            @Override
            protected void onLooperPrepared() {
                super.onLooperPrepared();
                try {
                    InputStream inputStream = getResources().openRawResource(R.raw.svgmap);
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    Document parse = documentBuilder.parse(inputStream);
                    NodeList paths = parse.getElementsByTagName("path");
                    pathList.clear();
                    float left = -1, top = -1, right = -1, bottom = -1;
                    for (int i = 0; i < paths.getLength(); i++) {
                        Node item = paths.item(i);
                        String path = ((Element) item).getAttribute("android:pathData");
                        Path pathData = PathParser.createPathFromPathData(path);
                        DrawPathBean bean = new DrawPathBean(pathData);
                        bean.mColor = colors[i % 3];
                        pathList.add(bean);

                        RectF bounds = new RectF();
                        pathData.computeBounds(bounds, true);
                        left = (left == -1) ? bounds.left : Math.min(left, bounds.left);
                        top = (top == -1) ? bounds.top : Math.min(top, bounds.top);
                        right = (right == -1) ? bounds.right : Math.max(right, bounds.right);
                        bottom = (bottom == -1) ? bounds.bottom : Math.max(bottom, bounds.bottom);
                    }
                    mRectF = new RectF(left, top, right, bottom);
                    refreshView();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void refreshView() {
        post(new Runnable() {
            @Override
            public void run() {
                requestLayout();
                postInvalidate();
            }
        });
    }

    private float scale = 1.0f;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //获取宽 - 测量规则的模式和大小
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        获取高 - 测量规则的模式和大小
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        // 设置wrap_content的默认宽 / 高值 // 默认宽/高的设定并无固定依据,根据需要灵活设置
        // 类似TextView,ImageView等针对wrap_content均在onMeasure()对设置默认宽
        // / 高值有特殊处理,具体读者可以自行查看
        int mWidth = getPaddingLeft() + getPaddingRight() + (int) mRectF.width();
        int mHeight = getPaddingTop() + getPaddingBottom() + (int) mRectF.height();
        scale = widthSize / mRectF.width();

        // 当布局参数设置为wrap_content时，设置默认值
        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT
                && getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            scale = mWidth / mRectF.width();
            setMeasuredDimension(mWidth, mHeight);
            // 宽 / 高任意一个布局参数为= wrap_content时，都设置默认值
        } else if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(mWidth, heightSize);
        } else if (getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            setMeasuredDimension(widthSize, mHeight);
        } else {
            setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (pathList.isEmpty()) {
            return;
        }
        canvas.save();
        canvas.scale(scale, scale);
        for (DrawPathBean pathBean : pathList) {
            pathBean.drawMap(canvas, mPaint);
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        for (DrawPathBean drawPathBean : pathList) {
            if (drawPathBean.isClick(event.getX() / scale, event.getY() / scale)) {
                if (mDrawPathBean != null) {
                    mDrawPathBean.isSelect = false;
                }
                drawPathBean.isSelect = true;
                mDrawPathBean = drawPathBean;
                break;
            }
        }
        refreshView();
        return super.onTouchEvent(event);
    }
}
