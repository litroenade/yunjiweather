package com.litroenade.yunjiweather.ui.home;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class WeatherAnimationView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private WeatherAnimationType animationType = WeatherAnimationType.CLOUDY;
    private ValueAnimator animator;
    private float progress;

    public WeatherAnimationView(Context context) {
        super(context);
    }

    public WeatherAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setIconCode(String iconCode) {
        animationType = WeatherAnimationType.fromIconCode(iconCode);
        invalidate();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimationLoop();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopAnimationLoop();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) {
            return;
        }
        if (animationType == WeatherAnimationType.SUNNY) {
            drawSunny(canvas, width, height);
        } else if (animationType == WeatherAnimationType.NIGHT) {
            drawNight(canvas, width, height);
        } else if (animationType == WeatherAnimationType.RAIN) {
            drawRain(canvas, width, height);
        } else if (animationType == WeatherAnimationType.SNOW) {
            drawSnow(canvas, width, height);
        } else {
            drawCloudy(canvas, width, height);
        }
    }

    private void drawSunny(Canvas canvas, int width, int height) {
        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = Math.min(width, height) * 0.23f;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        paint.setColor(Color.rgb(255, 184, 64));
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30f + progress * 360f);
            float startX = centerX + (float) Math.cos(angle) * radius * 1.35f;
            float startY = centerY + (float) Math.sin(angle) * radius * 1.35f;
            float stopX = centerX + (float) Math.cos(angle) * radius * 1.85f;
            float stopY = centerY + (float) Math.sin(angle) * radius * 1.85f;
            canvas.drawLine(startX, startY, stopX, stopY, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(255, 205, 82));
        canvas.drawCircle(centerX, centerY, radius, paint);
    }

    private void drawNight(Canvas canvas, int width, int height) {
        float centerX = width * 0.48f;
        float centerY = height * 0.46f;
        float radius = Math.min(width, height) * 0.24f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(242, 247, 255));
        canvas.drawCircle(centerX, centerY, radius, paint);
        paint.setColor(Color.rgb(28, 48, 82));
        canvas.drawCircle(centerX + radius * 0.42f, centerY - radius * 0.18f, radius * 0.88f, paint);
        paint.setColor(Color.rgb(210, 230, 255));
        for (int i = 0; i < 4; i++) {
            float starX = width * (0.22f + i * 0.15f);
            float starY = height * (0.24f + ((progress + i * 0.27f) % 1f) * 0.18f);
            canvas.drawCircle(starX, starY, 2.4f + i % 2, paint);
        }
    }

    private void drawCloudy(Canvas canvas, int width, int height) {
        float offset = (progress - 0.5f) * width * 0.08f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(226, 238, 248));
        canvas.drawCircle(width * 0.38f + offset, height * 0.55f, width * 0.18f, paint);
        canvas.drawCircle(width * 0.52f + offset, height * 0.45f, width * 0.23f, paint);
        canvas.drawCircle(width * 0.68f + offset, height * 0.56f, width * 0.16f, paint);
        canvas.drawRoundRect(width * 0.25f + offset, height * 0.53f, width * 0.80f + offset, height * 0.72f, 24f, 24f, paint);
    }

    private void drawRain(Canvas canvas, int width, int height) {
        drawCloudy(canvas, width, height);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setColor(Color.rgb(64, 142, 236));
        for (int i = 0; i < 5; i++) {
            float baseX = width * (0.28f + i * 0.12f);
            float y = height * 0.72f + ((progress + i * 0.18f) % 1f) * height * 0.20f;
            canvas.drawLine(baseX, y, baseX - width * 0.04f, y + height * 0.10f, paint);
        }
    }

    private void drawSnow(Canvas canvas, int width, int height) {
        drawCloudy(canvas, width, height);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        for (int i = 0; i < 6; i++) {
            float x = width * (0.25f + i * 0.10f);
            float y = height * 0.70f + ((progress + i * 0.14f) % 1f) * height * 0.22f;
            canvas.drawCircle(x, y, 4f, paint);
        }
    }

    private void startAnimationLoop() {
        if (animator != null) {
            return;
        }
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1800L);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    private void stopAnimationLoop() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }
}
