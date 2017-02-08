package com.jixte.snapcropupload;

import android.net.Uri;

/**
 *
 * Created by Zeeshan A Zakaria on 2016-04-09.
 * www.zeeshanz.com
 */
public interface ISnapCropUploadListener {
    void onReceiveImagePath(Uri imageUri);
}