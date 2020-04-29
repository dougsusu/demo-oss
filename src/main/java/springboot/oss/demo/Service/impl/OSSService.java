package springboot.oss.demo.Service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.Bucket;
import com.aliyun.oss.model.CreateBucketRequest;
import com.aliyun.oss.model.DataRedundancyType;
import com.aliyun.oss.model.StorageClass;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import springboot.oss.demo.Service.IOSSService;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Description: TODO
 *
 * @author ezreal
 * @version V1.0
 * @date 2020/4/29 10:41
 */
@Service
public class OSSService implements IOSSService {




    @Override
    public Object createBucket(String bucketName, OSS ossClient) {

        // 创建CreateBucketRequest对象。
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);

        // 如果创建存储空间的同时需要指定存储类型以及数据容灾类型, 可以参考以下代码。
        // 此处以设置存储空间的存储类型为标准存储为例。
        createBucketRequest.setStorageClass(StorageClass.Standard);
        // 默认情况下，数据容灾类型为本地冗余存储，即DataRedundancyType.LRS。如果需要设置数据容灾类型为同城冗余存储，请替换为DataRedundancyType.ZRS。
        createBucketRequest.setDataRedundancyType(DataRedundancyType.ZRS);
        // 创建存储空间。
        Bucket bucket = ossClient.createBucket(createBucketRequest);
        System.out.println(bucket.toString());
        //ossClient.shutdown();
        return bucketName + "创建成功";
    }

    @Override
    public boolean doesBucketExist(String bucketName, OSS ossClient) {
        boolean exists = ossClient.doesBucketExist(bucketName);
       // ossClient.shutdown();
        return exists;
    }

    @Override
    public Object deleteBucket(String buckName, OSS ossClient) {
        // 删除存储空间。
        ossClient.deleteBucket(buckName);
        // 关闭OSSClient。
        //ossClient.shutdown();
        return true;
    }

    @Override
    public void uploadFile(String bucketName, String objectName, OSS ossClient, MultipartFile file) {
        // 上传文件流。
        InputStream inputStream1 = null;
        try {
            inputStream1 = file.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ossClient.putObject(bucketName, objectName, inputStream1);
        // 关闭OSSClient。
        //ossClient.shutdown();
    }
}
