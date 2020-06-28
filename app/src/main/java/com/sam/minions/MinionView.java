package com.sam.minions;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

/**
 * @author SamWang(199004)
 *         2016/1/18 10:30
 */
public class MinionView extends View {

    public MinionView(Context context) {
        super(context);
    }

    public MinionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MinionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MinionView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec, true), measure(heightMeasureSpec, false));
    }

    private static final int DEFAULT_SIZE = 200; // View 默认大小
    private int widthForUnspecified;
    private int heightForUnspecified;

    /**
     * @param origin
     * @param isWidth 是否在测量宽，true 宽，false 高
     * @return
     */
    private int measure(int origin, boolean isWidth) {
        int result;
        int specMode = MeasureSpec.getMode(origin);
        int specSize = MeasureSpec.getSize(origin);
        switch (specMode) {
            case MeasureSpec.EXACTLY:
            case MeasureSpec.AT_MOST:
                result = specSize;
                if (isWidth) {
                    widthForUnspecified = result;
                } else {
                    heightForUnspecified = result;
                }
                break;
            case MeasureSpec.UNSPECIFIED:
            default:
                if (isWidth) {// 宽或高未指定的情况下，可以由另一端推算出来 - -. 如果两边都没指定就用默认值
                    result = (int) (heightForUnspecified * BODY_WIDTH_HEIGHT_SCALE);
                } else {
                    result = (int) (widthForUnspecified / BODY_WIDTH_HEIGHT_SCALE);
                }
                if (result == 0) {
                    result = DEFAULT_SIZE;
                }

                break;
        }

        return result;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initParams();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawFeetShadow(canvas); // 脚下的阴影
        drawFeet(canvas);       // 脚
        drawHands(canvas);      // 手
        drawBody(canvas);       // 身体
        drawClothes(canvas);    // 衣服
        drawEyesMouth(canvas);  // 眼睛,嘴巴
        drawBodyStroke(canvas); // 最后画身体的描边，可以摭住一些过渡的棱角
    }

    private Paint mPaint = new Paint();
    {
        mPaint.setAntiAlias(true);// 抗锯齿
    }

    private float bodyWidth;
    private float bodyHeight;
    private static final float BODY_SCALE = 0.6f; // 身体主干占整个view的比重
    private static final float BODY_WIDTH_HEIGHT_SCALE = 0.6f; // 身体的比例设定为 w:h = 3:5

    private float mStrokeWidth = 4; // 描边宽度
    private float offset; // 计算时，部分需要 考虑描边偏移
    private float radius; // 身体上下半圆的半径
    private int colorClothes = Color.rgb(32, 116, 160); // 衣服的颜色
    private int colorBody = Color.rgb(249, 217, 70); // 身体的颜色
    private int colorStroke = Color.BLACK;
    private RectF bodyRect = new RectF();
    private float handsHeight;// 计算出吊带的高度时，可以用来做手的高度
    private float footHeight; // 脚的高度，用来画脚部阴影时用

    private void initParams() {
        bodyWidth = Math.min(getWidth(), getHeight() * BODY_WIDTH_HEIGHT_SCALE) * BODY_SCALE;
        bodyHeight = Math.min(getWidth(), getHeight() * BODY_WIDTH_HEIGHT_SCALE) / BODY_WIDTH_HEIGHT_SCALE * BODY_SCALE;

        mStrokeWidth = Math.max(bodyWidth / 50, mStrokeWidth);
        offset = mStrokeWidth / 2;

        bodyRect.left = (getWidth() - bodyWidth) / 2;
        bodyRect.top = (getHeight() - bodyHeight) / 2;
        bodyRect.right = bodyRect.left + bodyWidth;
        bodyRect.bottom = bodyRect.top + bodyHeight;

        radius = bodyWidth / 2;
        footHeight = radius * 0.4333f;

        handsHeight =  (getHeight() + bodyHeight) / 2   + offset - radius * 1.65f ;
    }

    private void drawBody(Canvas canvas) {
        mPaint.setColor(colorBody);
        mPaint.setStyle(Paint.Style.FILL);

        canvas.drawRoundRect(bodyRect, radius, radius, mPaint);
    }

    private void drawBodyStroke(Canvas canvas) {
        mPaint.setColor(colorStroke);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(bodyRect, radius, radius, mPaint);
    }

    private RectF rect = new RectF();
    private float[] pts = new float[20];// 5 条线
    private Path path = new Path();
    private void drawClothes(Canvas canvas) {

        rect.left = (getWidth() - bodyWidth) / 2 + offset;
        rect.top = (getHeight() + bodyHeight) / 2 - radius * 2 + offset;
        rect.right = rect.left + bodyWidth - offset * 2;
        rect.bottom = rect.top + radius * 2 - offset * 2;

        mPaint.setColor(colorClothes);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(mStrokeWidth);
        canvas.drawArc(rect, 0, 180, true, mPaint);

        int h = (int) (radius * 0.5);
        int w = (int) (radius * 0.3);

        rect.left += w;
        rect.top = rect.top + radius - h;
        rect.right -= w;
        rect.bottom = rect.top + h;

        canvas.drawRect(rect, mPaint);

        // 画横线，可优化：用 Path 来绘制，每个点用 rLineTo 去连接
        mPaint.setColor(colorStroke);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(mStrokeWidth);

        pts[0] = rect.left - w;
        pts[1] = rect.top + h;
        pts[2] = pts[0] + w;
        pts[3] = pts[1];

        pts[4] = pts[2];
        pts[5] = pts[3] + offset;
        pts[6] = pts[4];
        pts[7] = pts[3] - h;

        pts[8] = pts[6] - offset;
        pts[9] = pts[7];
        pts[10] = pts[8] + (radius - w) * 2;
        pts[11] = pts[9];

        pts[12] = pts[10];
        pts[13] = pts[11] - offset;
        pts[14] = pts[12];
        pts[15] = pts[13] + h;

        pts[16] = pts[14] - offset;
        pts[17] = pts[15];
        pts[18] = pts[16] + w;
        pts[19] = pts[17];
        canvas.drawLines(pts, mPaint);

        // 画左吊带
        mPaint.setColor(colorClothes);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.FILL);
        path.reset();
        path.moveTo(rect.left - w - offset, handsHeight);
        path.lineTo(rect.left + h / 4f, rect.top + h / 2f);
        final float smallW = w / 2f * (float) Math.sin(Math.PI / 4);
        path.lineTo(rect.left + h / 4f + smallW, rect.top + h / 2f - smallW);
        final float smallW2 = w / (float) Math.sin(Math.PI / 4) / 2;
        path.lineTo(rect.left - w - offset, handsHeight - smallW2);
        canvas.drawPath(path, mPaint);

        mPaint.setColor(colorStroke);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, mPaint);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(rect.left + h / 5f, rect.top + h / 4f, mStrokeWidth * 0.7f, mPaint);

        // 画右吊带，代码和左吊带差不多，坐标对称
        mPaint.setColor(colorClothes);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.FILL);
        path.reset();
        path.moveTo(rect.left - w + 2 * radius - offset, handsHeight);
        path.lineTo(rect.right - h / 4f, rect.top + h / 2f);
        path.lineTo(rect.right - h / 4f - smallW, rect.top + h / 2f - smallW);
        path.lineTo(rect.left - w + 2 * radius - offset, handsHeight- smallW2);

        canvas.drawPath(path, mPaint);
        mPaint.setColor(colorStroke);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path, mPaint);

        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(rect.right - h / 5f, rect.top + h / 4f, mStrokeWidth * 0.7f, mPaint);

        // 中间口袋
        mPaint.setColor(colorStroke);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);

        path.reset();
        float radiusBigPocket = w / 2.0f;
        path.moveTo(rect.left + 1.5f * w, rect.bottom - h / 4f);
        path.lineTo(rect.right - 1.5f * w, rect.bottom - h / 4f);
        path.lineTo(rect.right - 1.5f * w, rect.bottom + h / 4f);
        path.addArc(rect.right - 1.5f * w - radiusBigPocket * 2, rect.bottom + h / 4f - radiusBigPocket,
                rect.right - 1.5f * w, rect.bottom + h / 4f + radiusBigPocket, 0, 90);
        path.lineTo(rect.left + 1.5f * w + radiusBigPocket, rect.bottom + h / 4f + radiusBigPocket);

        path.addArc(rect.left + 1.5f * w, rect.bottom + h / 4f - radiusBigPocket,
                rect.left + 1.5f * w + 2 * radiusBigPocket, rect.bottom + h / 4f + radiusBigPocket, 90, 90);
        path.lineTo(rect.left + 1.5f * w, rect.bottom - h / 4f - offset);
        canvas.drawPath(path, mPaint);

        // 下边一竖，分开裤子
        canvas.drawLine(bodyRect.left + bodyWidth / 2, bodyRect.bottom - h * 0.8f, bodyRect.left + bodyWidth / 2, bodyRect.bottom, mPaint);
        // 左边的小口袋
        float radiusSmallPocket = w * 1.2f;
        canvas.drawArc(bodyRect.left - radiusSmallPocket, bodyRect.bottom - radius - radiusSmallPocket,
                bodyRect.left + radiusSmallPocket, bodyRect.bottom - radius + radiusSmallPocket, 80, -60, false, mPaint);
        // 右边小口袋
        canvas.drawArc(bodyRect.right - radiusSmallPocket, bodyRect.bottom - radius - radiusSmallPocket,
                bodyRect.right + radiusSmallPocket, bodyRect.bottom - radius + radiusSmallPocket, 100, 60, false, mPaint);
    }

    private void drawEyesMouth(Canvas canvas) {
        // 眼睛中心处于上半圆直径 往上的高度偏移
        float eyesOffset = radius * 0.1f;
        mPaint.setStrokeWidth(mStrokeWidth * 5);

        // 计算眼镜带弧行的半径 分两段，以便眼睛中间有隔开的效果
        float radiusGlassesRibbon = (float) (radius / Math.sin(Math.PI / 20));
        rect.left = bodyRect.left + radius - radiusGlassesRibbon;
        rect.top = bodyRect.top + radius - (float) (radius / Math.tan(Math.PI / 20)) - radiusGlassesRibbon - eyesOffset;
        rect.right = rect.left + radiusGlassesRibbon * 2;
        rect.bottom = rect.top + radiusGlassesRibbon * 2;
        canvas.drawArc(rect, 81, 3, false, mPaint);
        canvas.drawArc(rect, 99, -3, false, mPaint);

        // 眼睛半径
        float radiusEyes = radius / 3;
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(bodyRect.left + bodyWidth / 2 - radiusEyes - offset, bodyRect.top + radius - eyesOffset, radiusEyes, mPaint);
        canvas.drawCircle(bodyRect.left + bodyWidth / 2 + radiusEyes + offset, bodyRect.top + radius - eyesOffset, radiusEyes, mPaint);

        mPaint.setColor(colorStroke);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(bodyRect.left + bodyWidth / 2 - radiusEyes - offset, bodyRect.top + radius - eyesOffset, radiusEyes, mPaint);
        canvas.drawCircle(bodyRect.left + bodyWidth / 2 + radiusEyes + offset, bodyRect.top + radius - eyesOffset, radiusEyes, mPaint);

        final float radiusEyeballBlack = radiusEyes / 3;
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(bodyRect.left + bodyWidth / 2 - radiusEyes - offset, bodyRect.top + radius - eyesOffset, radiusEyeballBlack, mPaint);
        canvas.drawCircle(bodyRect.left + bodyWidth / 2 + radiusEyes + offset, bodyRect.top + radius - eyesOffset, radiusEyeballBlack, mPaint);

        mPaint.setColor(Color.WHITE);
        final float radiusEyeballWhite = radiusEyeballBlack / 2;
        canvas.drawCircle(bodyRect.left + bodyWidth / 2 - radiusEyes + radiusEyeballWhite - offset * 2,
                bodyRect.top + radius - radiusEyeballWhite + offset - eyesOffset,
                radiusEyeballWhite, mPaint);
        canvas.drawCircle(bodyRect.left + bodyWidth / 2 + radiusEyes + radiusEyeballWhite,
                bodyRect.top + radius - radiusEyeballWhite + offset - eyesOffset,
                radiusEyeballWhite, mPaint);

        // 画嘴巴，因为位置和眼睛有相对关系，所以写在一块
        mPaint.setColor(colorStroke);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        float radiusMonth = radius;
        rect.left = bodyRect.left;
        rect.top = bodyRect.top - radiusMonth / 2.5f;
        rect.right = rect.left + radiusMonth * 2;
        rect.bottom = rect.top + radiusMonth * 2;
        canvas.drawArc(rect, 95, -20, false, mPaint);

    }


    private void drawFeet(Canvas canvas) {
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(colorStroke);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        float radiusFoot = radius / 3 * 0.4f;
        float leftFootStartX = bodyRect.left + radius - offset * 2;
        float leftFootStartY = bodyRect.bottom - offset;
        float footWidthA = radius * 0.5f;//脚宽度大-到半圆结束
        float footWidthB = footWidthA / 3;//脚宽度-比较细的部分

        // 左脚
        path.reset();
        path.moveTo(leftFootStartX, leftFootStartY);
        path.lineTo(leftFootStartX, leftFootStartY + footHeight);
        path.lineTo(leftFootStartX - footWidthA + radiusFoot, leftFootStartY + footHeight);

        rect.left = leftFootStartX - footWidthA;
        rect.top = leftFootStartY + footHeight - radiusFoot * 2;
        rect.right = rect.left + radiusFoot * 2;
        rect.bottom = rect.top + radiusFoot * 2;
        path.addArc(rect, 90, 180);
        path.lineTo(rect.left + radiusFoot + footWidthB, rect.top);
        path.lineTo(rect.left + radiusFoot + footWidthB, leftFootStartY);
        path.lineTo(leftFootStartX, leftFootStartY);
        canvas.drawPath(path, mPaint);

        // 右脚
        float rightFootStartX = bodyRect.left + radius + offset * 2;
        float rightFootStartY = leftFootStartY;
        path.reset();
        path.moveTo(rightFootStartX, rightFootStartY);
        path.lineTo(rightFootStartX, rightFootStartY + footHeight);
        path.lineTo(rightFootStartX + footWidthA - radiusFoot, rightFootStartY + footHeight);

        rect.left = rightFootStartX + footWidthA - radiusFoot * 2;
        rect.top = rightFootStartY + footHeight - radiusFoot * 2;
        rect.right = rect.left + radiusFoot * 2;
        rect.bottom = rect.top + radiusFoot * 2;
        path.addArc(rect, 90, -180);
        path.lineTo(rect.right - radiusFoot - footWidthB, rect.top);
        path.lineTo(rect.right - radiusFoot - footWidthB, rightFootStartY);
        path.lineTo(rightFootStartX, rightFootStartY);
        canvas.drawPath(path, mPaint);
    }

    private void drawFeetShadow(Canvas canvas) {
        mPaint.setColor(getResources().getColor(android.R.color.darker_gray));
        canvas.drawOval(bodyRect.left + bodyWidth * 0.15f,
                bodyRect.bottom - offset + footHeight,
                bodyRect.right - bodyWidth * 0.15f,
                bodyRect.bottom - offset + footHeight + mStrokeWidth * 1.3f, mPaint);
    }

    private void drawHands(Canvas canvas) {
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(colorBody);

        // 左手
        path.reset();
        float hypotenuse = bodyRect.bottom - radius - handsHeight;
        float radiusHand = hypotenuse / 6;
        mPaint.setPathEffect(new CornerPathEffect(radiusHand));

        path.moveTo(bodyRect.left, handsHeight);
        path.lineTo(bodyRect.left - hypotenuse / 2, handsHeight + hypotenuse / 2);
        path.lineTo(bodyRect.left +offset, bodyRect.bottom - radius +offset);
        path.lineTo(bodyRect.left, handsHeight);//增加兼容性,path没闭合在一起机子上会使手的下面的点没办法与裤子重合
        canvas.drawPath(path, mPaint);

        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(colorStroke);
        canvas.drawPath(path, mPaint);

        // 右手
        path.reset();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(colorBody);

        path.moveTo(bodyRect.right, handsHeight);
        path.lineTo(bodyRect.right + hypotenuse / 2, handsHeight + hypotenuse / 2);
        path.lineTo(bodyRect.right  -offset, bodyRect.bottom - radius +offset);
        path.lineTo(bodyRect.right, handsHeight);
        canvas.drawPath(path, mPaint);

        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(colorStroke);
        canvas.drawPath(path, mPaint);

        // 一个慢动作  - -||| 手臂内侧拐点
        path.reset();
        mPaint.setStyle(Paint.Style.FILL);
        path.moveTo(bodyRect.left, handsHeight + hypotenuse / 2 - mStrokeWidth);
        path.lineTo(bodyRect.left - mStrokeWidth * 2, handsHeight + hypotenuse / 2 + mStrokeWidth * 2);
        path.lineTo(bodyRect.left, handsHeight + hypotenuse / 2 + mStrokeWidth);
        path.lineTo(bodyRect.left, handsHeight + hypotenuse / 2 - mStrokeWidth);
        canvas.drawPath(path, mPaint);

        path.reset();
        path.moveTo(bodyRect.right, handsHeight + hypotenuse / 2 - mStrokeWidth);
        path.lineTo(bodyRect.right + mStrokeWidth * 2, handsHeight + hypotenuse / 2 + mStrokeWidth * 2);
        path.lineTo(bodyRect.right, handsHeight + hypotenuse / 2 + mStrokeWidth);
        path.lineTo(bodyRect.right, handsHeight + hypotenuse / 2 - mStrokeWidth);
        canvas.drawPath(path, mPaint);

        mPaint.setPathEffect(null); //避免影响其它绘制
    }

    public void randomBodyColor() {
        Random random = new Random();
        colorBody = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
        invalidate();
    }
}
