package net.onlyid.scan_login;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import net.onlyid.R;

public class ScanLineView extends View {
    static final String TAG = ScanLineView.class.getSimpleName();

    int stepDistance = 5;
    Rect scanRect;
    float scanLineTop;
    Paint paint;
    int scanLineSize = 4;
    int scanLineColor;
    Bitmap scanLineBitmap;
    int animDelayTime = 10;

    public ScanLineView(Context context, AttributeSet attrs) {
        super(context);
        paint = new Paint();
        paint.setAntiAlias(true);
//        scanLineColor = getResources().getColor(R.color.accent);
        // 如果想用代码画线，则取消注释前两行，并注释掉第三行
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(scanLineColor);
        scanLineBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.qrcode_scan_line);
    }

    @Override
    public void onDraw(Canvas canvas) {
        drawScanLine(canvas);

        moveScanLine();
    }

    /**
     * 画扫描线
     */
    private void drawScanLine(Canvas canvas) {
        if (scanLineBitmap != null) {
            RectF lineRect = new RectF(scanRect.left, scanLineTop, scanRect.right, scanLineTop + scanLineBitmap.getHeight());
            canvas.drawBitmap(scanLineBitmap, null, lineRect, paint);
        } else {
            canvas.drawRect(scanRect.left, scanLineTop, scanRect.right, scanLineTop + scanLineSize, paint);
        }
    }

    /**
     * 移动扫描线的位置
     */
    private void moveScanLine() {
        scanLineTop += stepDistance;
        int scanLineSize = this.scanLineSize;
        if (scanLineBitmap != null) {
            scanLineSize = scanLineBitmap.getHeight();
        }

        if (scanLineTop + scanLineSize > scanRect.bottom) {
            scanLineTop = scanRect.top + 0.5f;
        }

        postInvalidateDelayed(animDelayTime, scanRect.left, scanRect.top, scanRect.right, scanRect.bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 分10份
        int left = w / 10;
        // 取中间8份
        int size = left * 8;
        int top = (int) ((h - size) / 2);
        scanRect = new Rect(left, top, left + size, top + size);
        scanLineTop = scanRect.top + 0.5f;
    }
}
