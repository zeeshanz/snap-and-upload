package com.jixte.snapcropupload;

import android.content.DialogInterface;

/**
 *
 * Created by Zeeshan A Zakaria on 2016-04-05.
 * www.zeeshanz.com
 */
public interface IPhotoActivityPresenter {
    void takePhotoOptionClicked();
    void openGalleryOptionClicked();
    void cancelOptionClicked(DialogInterface dialog);
    void uploadPhotoButtonClicked();
    void goBackButtonPresses();
    void setServerUrl(String url);
    void imageProcessingComplete(String imagePath);
    String processImage();
}
