package springboot.oss.demo.Service;

import com.aliyun.oss.OSS;
import org.springframework.web.multipart.MultipartFile;

public interface IOSSService {

    Object createBucket(String bucketName, OSS ossClient);

    boolean doesBucketExist(String bucketName, OSS ossClient);

    Object deleteBucket(String buckName, OSS instance);

    void uploadFile(String bucketName, String objectName, OSS instance, MultipartFile file);
}
