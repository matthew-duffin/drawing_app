/**
 * Authors: Jiedi Mo, Matthew Duffin, Adrian Regalado
 * Project: Group Project Phase 3
 * Date: April 19, 2024
 * Overview: This is a helper for the drawing app that handles image processing functions
 */
#include <jni.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string>

#define  LOG_TAG    "libimageprocessing"
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

/**
 * This function inverts the bitmaps colors
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_drawingapp_CanvasModel_invertBitmap(JNIEnv *env, jobject thiz, jobject bitmap) {
    AndroidBitmapInfo info;
    void *pixels;
    LOGE("hi",2);
    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, &pixels);

    int xx, yy;
    uint32_t* line;

    for(yy = 0; yy < info.height; yy++) {
        line = (uint32_t *) pixels;
        for (xx = 0; xx < info.width; xx++) {
            uint32_t color = line[xx];
            uint32_t invertedColor = 0xFFFFFFFF - color;
            // Preserve the alpha channel
            uint32_t alpha = color & 0xFF000000;
            line[xx] = invertedColor | alpha;
        }
        pixels = (char*)pixels + info.stride;
    }
    AndroidBitmap_unlockPixels(env, bitmap);
}

/**
 * This function handles the saturating of the image
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_example_drawingapp_CanvasModel_saturateBitmap(JNIEnv *env, jobject thiz, jobject bitmap, jfloat saturationValue) {
    AndroidBitmapInfo info;
    void *pixels;
    LOGE("hi",2);
    AndroidBitmap_getInfo(env, bitmap, &info);
    AndroidBitmap_lockPixels(env, bitmap, &pixels);

    int xx, yy, red, blue, green;
    uint32_t* line;

    for(yy = 0; yy < info.height; yy++) {
        line = (uint32_t *) pixels;
        for (xx = 0; xx < info.width; xx++) {

            //extract the RGB values from the pixel
            red = (int) (line[xx] & 0x000000FF);
            green = (int) ((line[xx] & 0x0000FF00) >> 8);
            blue = (int) ((line[xx] & 0x00FF0000) >> 16);

            if (red >= green && red > blue) {
                red = std::min(255, red * 2);
                green /= 2;
                blue /= 2;
            }
            else if (green > red && green >= blue) {
                //red = std::max(1, red / 2);
                green = std::min(255, green * 2);
                //blue = std::max(1, blue / 2);
                red /= 2;
                blue /= 2;
            }
            else if (blue >= red && blue > green) {
                blue = std::min(255, blue * 2);
                red /= 2;
                green /= 2;
            }

            uint32_t saturatedColor = (red) | (green << 8) | (blue << 16);
            // Preserve the alpha channel
            uint32_t alpha = saturatedColor | 0xFF000000;
            line[xx] = alpha;
        }
        pixels = (char*)pixels + info.stride;
    }
    AndroidBitmap_unlockPixels(env, bitmap);
}