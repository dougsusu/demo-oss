package springboot.oss.demo.entity;

import lombok.Data;

/**
 * Description: 客户端直传的Policy信息
 *
 * @author ezreal
 * @version V1.0
 * @date 2020/4/29 15:24
 */
@Data
public class OSSUploadPolicy {
    private String accessid;
    private String policy;
    private String signature;
    private String dir;
    private String host;
    private String expire;
    private String callback;


}
