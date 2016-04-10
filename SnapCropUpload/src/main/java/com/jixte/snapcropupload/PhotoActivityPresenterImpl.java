package com.jixte.snapcropupload;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 *
 * Created by Zeeshan A Zakaria on 2016-04-05.
 * www.zeeshanz.com
 */
public class PhotoActivityPresenterImpl implements IPhotoActivityPresenter {

    PhotoActivityView mPhotoActivityView;
    PhotoModel mPhotoModel;

    public PhotoActivityPresenterImpl(PhotoActivityView photoActivityView) {
        mPhotoActivityView = photoActivityView;
        mPhotoModel = new PhotoModel();
    }

    @Override
    public void takePhotoOptionClicked() {
        mPhotoActivityView.startCamera();
    }

    @Override
    public void openGalleryOptionClicked() {
        mPhotoActivityView.openGallery();
    }

    @Override
    public void cancelOptionClicked(DialogInterface dialog) {
        mPhotoActivityView.cancel(dialog);
    }

    @Override
    public void goBackButtonPresses() {
        mPhotoActivityView.finishActivity();
    }

    @Override
    public String processImage() {
        String processedImagePath = mPhotoActivityView.rescaleCropAndSave();
        mPhotoModel.setmImagePath(processedImagePath);
        return processedImagePath;
    }

    @Override
    public void setServerUrl(String url) {
        mPhotoModel.setServerUrl(url);
    }

    @Override
    public void imageProcessingComplete(String imagePath) {
        mPhotoActivityView.broadcastImagePath(imagePath);
        uploadImageToTheServer();
    }

    @Override
    public void uploadPhotoButtonClicked() {
        mPhotoActivityView.startCamera();
    }

    /**
     * Once the image processing is complete, upload the image to the server
     */
    private void uploadImageToTheServer() {
        mPhotoModel.loadAsync(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                Log.v(Constants.TAG, "upload task finished");
                return false;
            }
        });
    }
}