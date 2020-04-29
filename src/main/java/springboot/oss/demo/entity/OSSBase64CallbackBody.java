package springboot.oss.demo.entity;

import lombok.Data;

/**
 * Description: TODO
 *
 * @author ezreal
 * @version V1.0
 * @date 2020/4/29 15:30
 */
@Data
public class OSSBase64CallbackBody {
    private String callbackUrl;
    private String callbackBody;
    private String callbackBodyType;
}
