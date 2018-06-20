package me.shl.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class webTools {
    public static int startID = 0;
    public static int endID = 0;


    public static boolean taskRun(String cookie) {
        if ((startID + endID) == 0 || startID > endID) {
            return false;
        }
        int startid = startID;
        int endid = endID;
        startID = 0;
        endID = 0;

        while (startid <= endid) {
            sendPost(String.valueOf(startid), "1", cookie);
            sendPost(String.valueOf(startid), "2", cookie);

            startid++;
        }
        return true;
    }

    public static String sendPost(String id, String isOver, String cookie) {
        OutputStreamWriter out = null;
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        try {
            URL httpUrl = new URL("http://yit.minghuaetc.com/study/updateDurationVideo.mooc");
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestProperty("Referer", "http://yit.minghuaetc.com/study/unit/109191.mooc");
            conn.setRequestProperty("Cookie", cookie);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.connect();
            out = new OutputStreamWriter(
                    conn.getOutputStream());
            out.write("itemId=" + id + "&isOver=" + isOver + "&currentPosition=840040&duration=840040");
            out.flush();
            reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String lines;
            while ((lines = reader.readLine()) != null) {
                lines = new String(lines.getBytes(), "utf-8");
                response.append(lines);
            }
            reader.close();
            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return response.toString();
    }
}