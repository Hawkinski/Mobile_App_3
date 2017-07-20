package com.example.sachin.fms.classes;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Sachin on 04,June,2017
 * Hawkinski,
 * Dubai, UAE.
 */
public class DownloadFile extends AsyncTask<String,String,String> {


    String urlLink,dirName,fileName;
    public DownloadFile(String urlLink, String dirName, String fileName){

        this.urlLink=urlLink;
        this.dirName =dirName;
        this.fileName =fileName;

    }


    @Override
    protected String doInBackground(String... params) {
        try {
            URL url = new URL(urlLink);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            //connect
            urlConnection.connect();
            int lenghtOfFile = urlConnection.getContentLength();
            InputStream is = url.openStream();
            File testDirectory = new File(Environment.getExternalStorageDirectory() + "/"+dirName);
            if (!testDirectory.exists()) {
                testDirectory.mkdirs();
            }
            File temp = new File(Environment.getExternalStorageDirectory() + "/"+dirName +"/"+fileName);

            if(temp.exists()){
                temp.delete();
            }
            FileOutputStream fos = new FileOutputStream(testDirectory +"/"+fileName);
            byte data[] = new byte[1024];
            int count = 0;
            long total = 0;
            int progress = 0;
            while ((count = is.read(data)) != -1) {
                total += count;
                int progress_temp = (int) total * 100 / lenghtOfFile;
                if (progress_temp % 10 == 0 && progress != progress_temp) {
                    progress = progress_temp;
                }
                fos.write(data, 0, count);
            }
            is.close();
            fos.close();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;

    }
}
