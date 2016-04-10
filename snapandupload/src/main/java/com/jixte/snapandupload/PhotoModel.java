package com.jixte.snapandupload;

import android.os.AsyncTask;
import android.os.Handler;

/**
 *
 * Created by Zeeshan A Zakaria on 2016-04-08.
 * www.zeeshanz.com
 */
public class PhotoModel implements IPhoto {

    String mLabel;
    String mPrice;
    String mDescription;
    String mImagePath;
    String mServerUrl;

    public String getServerUrl() {
        return mServerUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.mServerUrl= serverUrl;
    }

    public void setmImagePath(String mImagePath) {
        this.mImagePath = mImagePath;
    }

    public PhotoModel() {}

    @Override
    public void uploadImageToTheServer() {
    }

    public void loadAsync(final Handler.Callback onDoneCallback) {
        if (mServerUrl == null) return;
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                int serverResponseCode = Utils.uploadFile(mImagePath, mServerUrl);
                onDoneCallback.handleMessage(null);
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                onDoneCallback.handleMessage(null);
            }
        }.execute();
    }
}