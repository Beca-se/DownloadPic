package com.zh.learn;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO
 * @Author hhy
 * @Date 2020/9/23 19:18
 */
public class DownloadImage {
    static final ThreadPoolExecutor POOL_EXECUTOR = new ThreadPoolExecutor(10, 10, 20, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

    private CountDownLatch countDownLatch;

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }

    public void downImage(String urlStr, String path, String filename, String res) throws IOException {
        POOL_EXECUTOR.execute(() -> {
            try {
                downImage(urlStr, path, filename, res, "");
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    downImage(urlStr, path, filename, res, "");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } finally {
                countDownLatch.countDown();
            }
        });
    }

    public void downImage(String urlStr, String path, String filename, String rest, String ss) throws IOException {
        URL url = new URL(urlStr);
        //这里是可以设置代理的
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1080)));
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        //设置超时时间 5s
        conn.setConnectTimeout(15000);
        trustAllHosts(conn);
        conn.setHostnameVerifier(DO_NOT_VERIFY);
        conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        //设置引用来源，可有可无的设置，但是我这里还是设置一下
        conn.setRequestProperty("referer", "https://www.pixiv.net/artworks/84430212");
        conn.setRequestProperty("cookie", "");

        //从IO流中拿到数据
        InputStream in = conn.getInputStream();
        byte[] getData = readInputStream(in);

        //文件的保存位置 如果文件不存在的话，那就创建一个
        File fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }
        path = path + "/" + rest;
        fileDir = new File(path);
        if (!fileDir.exists()) {
            fileDir.mkdir();
        }

        File file = new File(fileDir + File.separator + filename);
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(getData);
        //关闭流
        if (fos != null) fos.close();
        if (in != null) in.close();

        System.out.println("文件下载成功。。。。");
//        countDownLatch.countDown();


    }

    //从输入流中读取字节
    private byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

    /**
     * 信任所有
     *
     * @param connection
     * @return
     */
    private static SSLSocketFactory trustAllHosts(HttpsURLConnection connection) {
        SSLSocketFactory oldFactory = connection.getSSLSocketFactory();
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory newFactory = sc.getSocketFactory();
            connection.setSSLSocketFactory(newFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oldFactory;
    }

    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }
    }};
    /**
     * 设置不验证主机
     */
    private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };


}
