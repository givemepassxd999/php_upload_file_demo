package com.gg.givemepass.phpuploaddemo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by rick.wu on 2017/1/16.
 */

public class FileUpload {
    private String mResponseMsg;
    private boolean isSucess;

    public interface OnFileUploadListener{
        void onFileUploadSuccess(String msg);
        void onFileUploadFail(String msg);
    }

    private OnFileUploadListener mOnFileUploadListener;

    public void setOnFileUploadListener(OnFileUploadListener listener){
        mOnFileUploadListener = listener;
    }

    public boolean isSucess() {
        return isSucess;
    }

    public FileUpload(){
        mResponseMsg = "";
        isSucess = false;
    }

    public void doFileUpload(String path) {
        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        String existingFileName = path;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        String urlString = "http://givemepass.lionfree.net/upload.php";

        try {
            //------------------ CLIENT REQUEST
            FileInputStream fileInputStream = new FileInputStream(new File(existingFileName));
            // open a URL connection to the Servlet
            URL url = new URL(urlString);
            // Open a HTTP connection to the URL
            conn = (HttpURLConnection) url.openConnection();
            // Allow Inputs
            conn.setDoInput(true);
            // Allow Outputs
            conn.setDoOutput(true);
            // Don't use a cached copy.
            conn.setUseCaches(false);
            // Use a post method.
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + existingFileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            // create a buffer of maximum size
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            // close streams
            fileInputStream.close();
            dos.flush();
            dos.close();
            isSucess = true;
        } catch (MalformedURLException e){
            isSucess = false;
        } catch (IOException e) {
            isSucess = false;
        }

        try {
            inStream = new DataInputStream(conn.getInputStream());
            String str;
            while ((str = inStream.readLine()) != null) {
                mResponseMsg = str;
            }
            inStream.close();

        } catch (IOException e) {
            isSucess = false;
            mResponseMsg = e.getMessage();
        }
        if(mOnFileUploadListener != null) {
            if (isSucess) {
                mOnFileUploadListener.onFileUploadSuccess(mResponseMsg);
            } else{
                mOnFileUploadListener.onFileUploadFail(mResponseMsg);
            }
        }
    }
}
