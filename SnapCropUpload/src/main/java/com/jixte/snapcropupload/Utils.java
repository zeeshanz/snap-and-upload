package com.jixte.snapcropupload;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 *
 * Created by Zeeshan A Zakaria on 2016-04-08.
 * www.zeeshanz.com
 */
public final class Utils {
    /**
     * Since we don't want to modify the original gallery files, so we'll copy the original to a
     * temporary location first and then modify the copied one.
     *
     * @param sourceFile    the source file
     * @param destFile      the destination file
     */
    public static boolean copyFile(String sourceFile, String destFile) {
        try {
            FileInputStream in = new FileInputStream(sourceFile);
            FileOutputStream out = new FileOutputStream(destFile);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Create image file names, depending upon if they are temporary names or final.
     *
     * tempOrFinal  flag to determine if the name is temporary or final
     *
     * @return  the filename
     * @throws IOException
     */
    public static File createImageFile(String tempOrFinal, String imageFolder, int imageType, boolean overwriteImage) throws IOException {
        String imageFileName;
        String fileExtension;
        File imageFile;
        File storageDir = new File (imageFolder);

        if(!storageDir.exists()) {
            if (storageDir.mkdir()) {
                Log.d(Constants.TAG, "Created new image folder: " + imageFolder);
            }
        }

        switch (imageType) {
            case 0:
                fileExtension = ".jpg";
                break;
            case 1:
                fileExtension = ".png";
                break;
            default:
                fileExtension = ".webp";
        }

        if (overwriteImage) {
            imageFileName = "/photo" + (tempOrFinal.equals("temp") ? "_" : "") + fileExtension;
            File oldImageFile = new File(imageFileName);
            imageFile = new File(imageFolder + imageFileName);
            if(oldImageFile.exists()) {
                if (oldImageFile.delete()) {
                    Log.d(Constants.TAG, "old image deleted");
                }
            }
        }
        else {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ", Locale.CANADA).format(new Date(System.currentTimeMillis()));
            imageFileName = timestamp + (tempOrFinal.equals("temp") ? "_" : "");
            imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    fileExtension,  /* suffix */
                    storageDir      /* directory */
            );
        }

        return imageFile;
    }

    /**
     * Rescale and then crop the image
     *
     * @param source        the image source
     * @param cropWidth     the width of the cropped image
     * @param cropHeight    the height of the cropped source
     * @return              the returned image
     */
    public static Bitmap scaleCenterCrop(Bitmap source, int cropWidth, int cropHeight) {

        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        float xScale = (float) cropWidth / sourceWidth;
        float yScale = (float) cropHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        // Now get the size of the source bitmap when scaled
        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        // Let's find out the upper left coordinates if the scaled bitmap
        // should be centered in the new size give by the parameters
        float left = (cropWidth - scaledWidth) / 2;
        float top = (cropHeight - scaledHeight) / 2;

        // The target rectangle for the new, scaled version of the source bitmap will now
        // be
        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        // Finally, we create a new bitmap of the specified size and draw our new,
        // scaled bitmap onto it.
        Bitmap finalImage = Bitmap.createScaledBitmap(source, cropWidth, cropHeight, false);
//        Bitmap finalImage = Bitmap.createBitmap(cropWidth, cropHeight, source.getConfig());
        Canvas canvas = new Canvas(finalImage);
        canvas.drawBitmap(source, null, targetRect, null);

        return finalImage;
    }

    /**
     * This method crops the image and saves it
     *
     * @param imagePath         the path to image to process
     * @param cropWidth         the crop width
     * @param cropHeight        the crop height
     * @param imageFolder       the image folder
     * @param imageType         the image type
     * @param overwriteImage    the flag whether to overwrite the previous image
     * @return                  the String value of the path to the new and final image
     */
    public static String processImage(String imagePath, int cropWidth, int cropHeight, String imageFolder, int imageType, boolean overwriteImage) {
        File tempImage = new File(imagePath).getAbsoluteFile();
        String finalImagePath = null;
        if (tempImage.canRead()) {
            File finalImage;
            Bitmap croppedImage = BitmapFactory.decodeFile(imagePath);

            if (croppedImage != null) {
                croppedImage = scaleCenterCrop(croppedImage, cropWidth, cropHeight);
                try {
                    finalImage = createImageFile("", imageFolder, imageType, overwriteImage);

                    if (finalImage != null) {
                        finalImagePath = finalImage.getAbsolutePath();
                        FileOutputStream fos = new FileOutputStream(finalImage);
                        switch (imageType) {
                            case 0:
                                croppedImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                break;
                            case 1:
                                croppedImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                break;
                            case 2:
                                croppedImage.compress(Bitmap.CompressFormat.WEBP, 100, fos);
                                break;
                        }
                        if (tempImage.delete()) {
                            Log.d(Constants.TAG, "Temp image deleted");
                        }
                        fos.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        } else {
            Log.e(Constants.TAG, "Image file not created");
            return null;
        }
        return finalImagePath;
    }

    /**
     *
     * @param sourceFileUri     the image path on the device
     * @param uploadServerUri   the server URL where to upload the file
     * @return                  the response code
     */
    public static int uploadFile(String sourceFileUri, String uploadServerUri) {
        int serverResponseCode = 0;
        HttpURLConnection conn;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        try {
            // Open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(uploadServerUri);

            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", sourceFileUri);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + sourceFileUri + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            // Create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            // Send multipart form data necessary after file data
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i(Constants.TAG, "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

            if(serverResponseCode == 200){
                Log.v(Constants.TAG, "File upload successful");
            }

            // Close the streams
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            Log.e(Constants.TAG, "error: " + ex.getMessage(), ex);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serverResponseCode;
    }
}