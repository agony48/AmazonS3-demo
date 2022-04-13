# MateU-Android
AmazonS3 上传 获取访问地址操作


    //minio 上传依赖
    implementation group: 'com.amazonaws', name: 'aws-android-sdk-s3', version: '2.22.5'
    implementation group: 'com.amazonaws', name: 'aws-android-sdk-mobile-client', version: '2.22.5'


## 说明
//上传
    /**
     * 获取文件访问url
     *
     * @param key 文件名
     */
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
//根据key 获取访问地址 （key 即是存储在桶里的名字

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
）
