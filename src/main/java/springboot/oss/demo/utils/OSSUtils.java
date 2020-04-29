package springboot.oss.demo.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.stereotype.Component;
import springboot.oss.demo.configuration.OSSBean;

import javax.annotation.Resource;

/**
 * Description: TODO
 *
 * @author ezreal
 * @version V1.0
 * @date 2020/4/29 10:34
 */

@Component
public class OSSUtils {

    @Resource
    private OSSBean ossBean;

    private OSS ossClient;

    public OSS getInstance() {
        if(ossClient==null){
            synchronized(OSSUtils.class){
                if(ossClient==null){
                    ossClient = new OSSClientBuilder().build(ossBean.getEndpoint(), ossBean.getAccessKeyId(), ossBean.getAccessKeySecret());
                }
            }
        }
        return ossClient;
    }

}
