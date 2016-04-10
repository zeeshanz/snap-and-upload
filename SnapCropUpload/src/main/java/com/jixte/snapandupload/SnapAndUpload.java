package com.jixte.snapandupload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 *
 * Created by Zeeshan A Zakaria on 2016-04-09.
 * www.zeeshanz.com
 */
public class SnapAndUpload extends BroadcastReceiver {
    static ISnapAndUploadListener mListener;

    /**
     * Calling the module with a callback. This call back returns the path of the image saved locally. See
     * the sample for example.
     *
     * @param context       the calling application's context
     * @param imageFolder   the subfolder under Pictures folder where the images will be stored. Null for no subfolder
     * @param cropWidth     the crop width in pixels. A value of 0 will result in no crop.
     * @param cropHeight    the crop height in pixels. A value of 0 will result in no crop.
     * @param imageType     the image output type: 0 is JPG, 1 is PNG, 2 is BMP
     * @param url           the URL where to upload the image
     * @param overwrite     set it to true if you want to over write the image everytime, otherwise set it to false.
     * @param listener      This callback will return the path of the final image. You might need this to display the image as a thumbnail.
     */

    public void setListener(Context context, String imageFolder, int cropWidth, int cropHeight, int imageType, String url, boolean overwrite, ISnapAndUploadListener listener) {
        mListener = listener;
        Intent intent = new Intent(context, PhotoActivity.class);
        intent.putExtra(Constants.IMAGE_FOLDER, imageFolder);
        intent.putExtra(Constants.CROP_WIDTH, cropWidth);
        intent.putExtra(Constants.CROP_HEIGHT, cropHeight);
        intent.putExtra(Constants.IMAGE_TYPE, imageType);
        intent.putExtra(Constants.URL, url);
        intent.putExtra(Constants.OVERWRITE_IMAGE, overwrite);
        context.startActivity(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.m2x.test.intent.MESSAGE_RECEIVED")) {
            String imagePath = intent.getStringExtra(Constants.IMAGE_PATH);
            if (mListener != null)
                mListener.onReceiveImagePath(imagePath);
            Log.d(Constants.TAG, "imagePath: " + imagePath);
        }
    }

    /**
     * Calling the module statically with no callback
     *
     * @param context       the calling application's context
     * @param imageFolder   the subfolder under Pictures folder where the images will be stored. Null for no subfolder
     * @param cropWidth     the crop width in pixels. A value of 0 will result in no crop.
     * @param cropHeight    the crop height in pixels. A value of 0 will result in no crop.
     * @param imageType     the image output type: 0 is JPG, 1 is PNG, 2 is BMP
     * @param url           the URL where to upload the image
     */
    public static void init(Context context, String imageFolder, int cropWidth, int cropHeight, int imageType, String url, boolean overwrite) {
        Intent intent = new Intent(context, PhotoActivity.class);
        intent.putExtra(Constants.IMAGE_FOLDER, imageFolder);
        intent.putExtra(Constants.CROP_WIDTH, cropWidth);
        intent.putExtra(Constants.CROP_HEIGHT, cropHeight);
        intent.putExtra(Constants.IMAGE_TYPE, imageType);
        intent.putExtra(Constants.URL, url);
        intent.putExtra(Constants.OVERWRITE_IMAGE, overwrite);
        context.startActivity(intent);
    }
}