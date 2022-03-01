package com.zh.learn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;


public class Main {

    static String test = "presentation";
    static String path = "G:\\PPIC";

    public static void main(String[] args) throws Exception {

        int counti = 100;
        String name = "TestName";
        for (int i = 0; i < counti; i++) {

            String countFolder = "" + (i + 0);
            File file = new File("C:\\Users\\Administrator\\Desktop/html/" + i + ".html");
            if (!file.exists()) {
                continue;
            }
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                if (str.contains(test)) {
                    break;
                }
            }
            bufferedReader.close();

            String[] split = str.split(test);
            List<String> list = new ArrayList<>();
            for (String s : split) {
                if (s.contains("href")) {
                    try {
                        s = s.substring(s.indexOf("href") + 6, s.indexOf("png") + 3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        s = s.substring(s.indexOf("href") + 6, s.indexOf("jpg") + 3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println(s);
                    list.add(s);
                }
            }


            String temp = path + "/" + name;
            String url = null;
            int count = 0;
            String filename = null;
            System.out.println("图片总数为: " + list.size());
            CountDownLatch countDownLatch = new CountDownLatch(list.size());
            DownloadImage downloadImage = new DownloadImage();
            downloadImage.setCountDownLatch(countDownLatch);
            file.delete();
            for (String s : list) {
                url = s;
                filename = (count++) + ".jpg";
                //需要传入三个参数，一个是图片的下载地址，一个是文件存放的位置，还有一个是文件名
                downloadImage.downImage(url, temp, filename, countFolder);
                Thread.sleep(10L);
            }
            countDownLatch.await();

        }
        System.exit(0);
    }

}
