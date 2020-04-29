package springboot.oss.demo.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Description: 解析配置文件里oss的信息
 *
 * @author ezreal
 * @version V1.0
 * @date 2020/4/29 10:09
 */
@Component
@Data
@ConfigurationProperties(prefix = "aliyun-oss")
public class OSSBean {
    private  String bucketName;
    private String region ;
    private String endpoint ;
    private String host ;
    private String accessKeyId;
    private String accessKeySecret;
    private String callbackUrl;
    private String dirPrefix;
}
