package com.mateu.cats.utils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * minio
 */
public class MinioUtil {

    //minio key
    public final static String MINIO_KEY="";
    //minio SECRET
    public final static String MINIO_SECRET="";
    //minio ip 端口号
    public final static String MINIO_ENDPOINT=""; //服务器
    //minio 桶
    public final static String MINIO_BUCKET_NAME="";//桶名字

    private static AmazonS3 s3;
    private ExecutorService executorCached;
    private volatile static MinioUtil minioUtil;

    public synchronized static MinioUtil getInstance() {
        if (minioUtil == null)
            minioUtil = new MinioUtil();
        if (s3 == null) {
            s3 = new AmazonS3Client(new AWSCredentials() {
                @Override
                public String getAWSAccessKeyId() {
                    return MINIO_KEY;
                }

                @Override
                public String getAWSSecretKey() {
                    return MINIO_SECRET;
                }
            }, Region.getRegion(Regions.CN_NORTH_1), new ClientConfiguration());
            s3.setEndpoint(MINIO_ENDPOINT);
        }
        return minioUtil;
    }

    public void minioUploadFileStart(File files, OnMinioListener listener) {
        if (executorCached == null) {
            executorCached = Executors.newCachedThreadPool();
        }
        executorCached.execute(new Runnable() {
            @Override
            public void run() {
                minioUploadFile(files, listener);
            }
        });
    }

    private URL url = null;

    public void minioUploadFile(File files, OnMinioListener listener) {
        url = null;
        // 获取文件名
        String fileName = "amaz_" + System.currentTimeMillis() + "." + files.getName().substring(files.getName().lastIndexOf(".") + 1);
        //(桶名,fileName文件在桶中存放地址,文件file)
        s3.putObject(new PutObjectRequest(MINIO_BUCKET_NAME, fileName, files).withGeneralProgressListener(new ProgressListener() {
            int readedbyte = 0;
            @Override
            public void progressChanged(ProgressEvent progressEvent) {
                readedbyte += progressEvent.getBytesTransferred();
                float loadNum = (readedbyte / (float) files.length()) * 100;
                if (loadNum == 100f) {
                    if (url == null) {
//                    获取文件上传后访问地址url:(http://xxx/地址?密钥)
                        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(MINIO_BUCKET_NAME, fileName);
                        url = s3.generatePresignedUrl(urlRequest);
                        if (listener != null)
                            listener.OnMinioCallback(loadNum, fileName, url + "", true);
                    }
                } else {
                    if (listener != null) {
                        listener.OnMinioCallback(loadNum, fileName, "", false);
                    }
                }
            }
        }));
    }


    public interface OnMinioListener {
        void OnMinioCallback(float speed, String key, String url, boolean isEnd);
    }

    /**
     * 获取文件访问url
     *
     * @param key 文件名
     */
    public String getMinioKeyUrl(String key) {
        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(MINIO_BUCKET_NAME, key);
        URL url = s3.generatePresignedUrl(urlRequest);
        return url + "";
    }

}
