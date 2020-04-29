package springboot.oss.demo.controller;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import springboot.oss.demo.Service.IOSSService;
import springboot.oss.demo.configuration.OSSBean;
import springboot.oss.demo.entity.OSSBase64CallbackBody;
import springboot.oss.demo.entity.OSSCallbackResponse;
import springboot.oss.demo.entity.OSSUploadPolicy;
import springboot.oss.demo.utils.OSSUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Description: TODO
 * https://blog.csdn.net/chire_jr/article/details/82733828
 * @author ezreal
 * @version V1.0
 * @date 2020/4/29 10:12
 */
@Controller
public class OSSController {


    @Resource
    private OSSBean ossBean;
    @Resource
    private OSSUtils ossUtils;

    @Autowired
    private IOSSService iossService;

    @RequestMapping("/hello")
    @ResponseBody
    public String hello(){
        return "你好！OSS";
    }

    @RequestMapping(value = "createBucket",method = RequestMethod.POST)
    @ResponseBody
    public Object createBucket(String buckName){
        Object bucket = iossService.createBucket(buckName, ossUtils.getInstance());
        return bucket;
    }

    @RequestMapping(value = "deleteBucket",method = RequestMethod.POST)
    @ResponseBody
    public Object deleteBucket(String buckName){
        Object bucket = iossService.deleteBucket(buckName, ossUtils.getInstance());
        return bucket;
    }
    @RequestMapping(value = "doesBucketExist",method = RequestMethod.POST)
    @ResponseBody
    public String uploadBlog(String buckName){
        boolean b = iossService.doesBucketExist(buckName, ossUtils.getInstance());
        return "是否存在：" + b;
    }

    @RequestMapping(value = "uploadFile",method = RequestMethod.POST)
    @ResponseBody
    public String uploadFile(MultipartFile file,String objectName){
        iossService.uploadFile(ossBean.getBucketName(), objectName,ossUtils.getInstance(), file);

        return "success";
    }

    @RequestMapping(value = "getUploadRequest")
    @ResponseBody
    public Object getUploadRequest(HttpServletRequest request, HttpServletResponse response){
        OSS client = ossUtils.getInstance();
        try {
            long expireTime = 300;
            //设置过期时间 当前时间的后300秒
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            //Policy条件
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, ossBean.getDirPrefix());
            //postPolicy的数据结构例子{"expiration":"2020-04-29T06:59:01.653Z","conditions":[["content-length-range",0,1048576000],["starts-with","$key","wcr/"]]}
            String postPolicy = client.generatePostPolicy(expiration, policyConds);
            //将postPolicy内容按utf-8进行base64的转码
            byte[] binaryData = postPolicy.getBytes("utf-8");
            //编码后的postPolicy
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            //获得签名
            String postSignature = client.calculatePostSignature(postPolicy);
            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", ossBean.getAccessKeyId());
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", ossBean.getDirPrefix());
            respMap.put("host", ossBean.getHost());
            //过期的时间，单位秒
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            // respMap.put("expire", formatISO8601Date(expiration));
            JSONObject jasonCallback = new JSONObject();
            jasonCallback.put("callbackUrl", ossBean.getCallbackUrl());
            jasonCallback.put("callbackBody",
                    "filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}");
            jasonCallback.put("callbackBodyType", "application/x-www-form-urlencoded");
            System.out.println("**************jasonCallback.toString()*************");
            System.out.println(jasonCallback.toString());
            String base64CallbackBody = BinaryUtil.toBase64String(jasonCallback.toString().getBytes());
            respMap.put("callback", base64CallbackBody);
            JSONObject jal = JSONObject.fromObject(respMap);
            /*System.out.println("*********官方例子********");
            System.out.println(jal.toString());*/
            //return jal.toString();
            /*封装成pojo*/
            OSSUploadPolicy ossUploadPolicy = new OSSUploadPolicy();
            ossUploadPolicy.setAccessid(ossBean.getAccessKeyId());
            ossUploadPolicy.setPolicy(encodedPolicy);
            ossUploadPolicy.setSignature(postSignature);
            ossUploadPolicy.setHost(ossBean.getHost());
            ossUploadPolicy.setDir(ossBean.getDirPrefix());
            ossUploadPolicy.setExpire(String.valueOf(expireEndTime / 1000));
            OSSBase64CallbackBody ossBase64CallbackBody = new OSSBase64CallbackBody();
            ossBase64CallbackBody.setCallbackUrl(ossBean.getCallbackUrl());
            ossBase64CallbackBody.setCallbackBody("filename=${object}&size=${size}&mimeType=${mimeType}&height=${imageInfo.height}&width=${imageInfo.width}");
            ossBase64CallbackBody.setCallbackBodyType("application/x-www-form-urlencoded");
            String ossBase64CallbackBodyStr = JSON.toJSONString(ossBase64CallbackBody);
            System.out.println("****************JSON.toJSONString*****************");
            System.out.println(ossBase64CallbackBodyStr);
            String base64CallbackBodyB = BinaryUtil.toBase64String(ossBase64CallbackBodyStr.toString().getBytes());
            ossUploadPolicy.setCallback(base64CallbackBodyB);
           /* System.out.println("*********封装成pojo********");
            System.out.println(JSON.toJSON(ossUploadPolicy));*/

            return JSON.toJSON(ossUploadPolicy);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "fail";
    }

    protected boolean verifyOSSCallbackRequest(HttpServletRequest request, String ossCallbackBody)throws NumberFormatException, IOException{
        boolean ret = false;
        String autorizationInput = new String(request.getHeader("Authorization"));
        String pubKeyInput = request.getHeader("x-oss-pub-key-url");
        byte[] authorization = BinaryUtil.fromBase64String(autorizationInput);
        byte[] pubKey = BinaryUtil.fromBase64String(pubKeyInput);
        String pubKeyAddr = new String(pubKey);
        if (!pubKeyAddr.startsWith("http://gosspublic.alicdn.com/")
                && !pubKeyAddr.startsWith("https://gosspublic.alicdn.com/")) {
            System.out.println("pub key addr must be oss addrss");
            return false;
        }
        String retString = executeGet(pubKeyAddr);
        retString = retString.replace("-----BEGIN PUBLIC KEY-----", "");
        retString = retString.replace("-----END PUBLIC KEY-----", "");
        String queryString = request.getQueryString();
        String uri = request.getRequestURI();
        String decodeUri = java.net.URLDecoder.decode(uri, "UTF-8");
        String authStr = decodeUri;
        if (queryString != null && !queryString.equals("")) {
            authStr += "?" + queryString;
        }
        authStr += "\n" + ossCallbackBody;
        ret = doCheck(authStr, authorization, retString);
        return ret;
    }


    @RequestMapping(value = "callback")
    @ResponseBody
    public String callback(HttpServletRequest request,HttpServletResponse response) throws NumberFormatException, IOException {
        String ossCallbackBody = GetPostBody(request.getInputStream(),
                Integer.parseInt(request.getHeader("content-length")));
        boolean ret = verifyOSSCallbackRequest(request, ossCallbackBody);
        System.out.println("verify result : " + ret);
        // System.out.println("OSS Callback Body:" + ossCallbackBody);
        /*if (ret) {
            response(request, response, "{\"Status\":\"OK\"}", HttpServletResponse.SC_OK);
        } else {
            response(request, response, "{\"Status\":\"verdify not ok\"}", HttpServletResponse.SC_BAD_REQUEST);
        }*/
        JSONObject jsonObject = new JSONObject();
        if (ret) {
            jsonObject.put("status", "200");
        } else {
            jsonObject.put("status", "verify not ok");
        }
        return jsonObject.toString();

    }
    /**
     * 服务器响应结果
     *
     * @param request
     * @param response
     * @param results
     * @param status
     * @throws IOException
     */
    private void response(HttpServletRequest request, HttpServletResponse response, String results, int status)
            throws IOException {
        String callbackFunName = request.getParameter("callback");
        response.addHeader("Content-Length", String.valueOf(results.length()));
        if (callbackFunName == null || callbackFunName.equalsIgnoreCase("")){
            response.getWriter().println(results);
        }else{
            response.getWriter().println(callbackFunName + "( " + results + " )");
        }

        response.setStatus(status);
        response.flushBuffer();
    }

    /**
     * 服务器响应结果
     */
    private void response(HttpServletRequest request, HttpServletResponse response, String results) throws IOException {
        String callbackFunName = request.getParameter("callback");
        if (callbackFunName == null || callbackFunName.equalsIgnoreCase("")){
            response.getWriter().println(results);
        }
        else{
            response.getWriter().println(callbackFunName + "( " + results + " )");
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.flushBuffer();
    }

    /**
     * 验证RSA
     *
     * @param content
     * @param sign
     * @param publicKey
     * @return
     */
    public static boolean doCheck(String content, byte[] sign, String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = BinaryUtil.fromBase64String(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));
            java.security.Signature signature = java.security.Signature.getInstance("MD5withRSA");
            signature.initVerify(pubKey);
            signature.update(content.getBytes());
            boolean bverify = signature.verify(sign);
            return bverify;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    /**
     * 获取public key
     * @param url
     * @return
     */
    public String executeGet(String url) {
        BufferedReader in = null;

        String content = null;
        try {
            // 定义HttpClient
            @SuppressWarnings("resource")
            DefaultHttpClient client = new DefaultHttpClient();
            // 实例化HTTP方法
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            HttpResponse response = client.execute(request);

            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();
            content = sb.toString();
        } catch (Exception e) {
        } finally {
            if (in != null) {
                try {
                    in.close();// 最后要关闭BufferedReader
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return content;
        }
    }

    /**
     * 获取Post消息体
     *
     * @param is
     * @param contentLen
     * @return
     */
    public String GetPostBody(InputStream is, int contentLen) {
        if (contentLen > 0) {
            int readLen = 0;
            int readLengthThisTime = 0;
            byte[] message = new byte[contentLen];
            try {
                while (readLen != contentLen) {
                    readLengthThisTime = is.read(message, readLen, contentLen - readLen);
                    if (readLengthThisTime == -1) {// Should not happen.
                        break;
                    }
                    readLen += readLengthThisTime;
                }
                return new String(message);
            } catch (IOException e) {
            }
        }
        return "";
    }
}
