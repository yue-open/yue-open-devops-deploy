package ai.yue.open.devops.deploy.service;

import ai.yue.library.base.crypto.client.SecureCommon;
import ai.yue.library.base.exception.ParamException;
import ai.yue.library.base.util.DateUtils;
import ai.yue.library.base.util.MapUtils;
import ai.yue.library.base.util.StringUtils;
import ai.yue.library.base.view.R;
import ai.yue.library.base.view.Result;
import ai.yue.open.devops.deploy.config.DevopsDeployProperties;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Rancher CD
 * 
 * @author	ylyue
 * @since	2018年11月12日
 */
@Service
public class DevopsService {

    @Value("${spring.application.name}")
    String applicationName;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DevopsDeployProperties devopsDeployProperties;

    /**
     * 重新部署
     *
     * @param workloadApiUrl  需要重新部署的工作负载API地址
     * @param bearerTokenName 配置在yue-open-devops-deploy中，所对应的bearerToken名
     * @param imageTag        docker镜像版本
     */
    public Result<?> redeploy(String workloadApiUrl, String bearerTokenName, String imageTag) {
        // 1. 处理请求异常
        OapiRobotSendResponse oapiRobotSendResponse;
        try {
            // 发送请求
            redeployRequest(workloadApiUrl, bearerTokenName, imageTag);
            // 发送通知
            oapiRobotSendResponse = sendLinkMessage(workloadApiUrl, bearerTokenName, imageTag);
        } catch (Exception e) {
			oapiRobotSendResponse = sendTextMessage(workloadApiUrl, bearerTokenName, imageTag);
            // 打印请求异常
            e.printStackTrace();
        }

        // 2. 打印日志
        JSONObject logInfo = new JSONObject();
        boolean success = oapiRobotSendResponse.isSuccess();
        Long errcode = oapiRobotSendResponse.getErrcode();
        String errmsg = oapiRobotSendResponse.getErrmsg();
        logInfo.put("success", success);
        logInfo.put("errcode", errcode);
        logInfo.put("errmsg", errmsg);
        String resultMsg = StrUtil.format("【钉钉】通知结果：{}", logInfo.toString());
        Console.log(resultMsg);
        
        // 3. 返回结果
        return R.success(resultMsg);
    }

    /**
     * 重新部署请求
     *
     * @param workloadApiUrl  需要重新部署的工作负载API地址
     * @param bearerTokenName 配置在yue-open-devops-deploy中，所对应的bearerToken名
     * @param imageTag        docker镜像版本
     */
    private void redeployRequest(String workloadApiUrl, String bearerTokenName, String imageTag) {
        // 1. 获取bearerToken
        Map<String, String> bearerTokens = devopsDeployProperties.getBearerTokens();
        String bearerToken = MapUtils.getString(bearerTokens, bearerTokenName);
        if (StrUtil.isBlank(bearerToken)) {
            throw new ParamException("无效的bearerTokenName，获取不到对应的bearerToken");
        }

        // 2. 获取工作负载信息
        RequestEntity<Void> requestEntity = RequestEntity.get(URI.create(workloadApiUrl))
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .build();
        ResponseEntity<JSONObject> result = restTemplate.exchange(requestEntity, JSONObject.class);

        // 3. 替换请求参数
        JSONObject body = result.getBody();
        String cattle_io_timestamp = LocalDateTime.now(Clock.systemUTC()).format(DateUtils.FORMATTER) + "Z";
        JSONObject annotations = body.getJSONObject("annotations");
        annotations.replace("cattle.io/timestamp", cattle_io_timestamp);
        body.replace("annotations", annotations);

		// 如果版本信息不为空，替换镜像值
        if (StringUtils.isNotEmpty(imageTag)) {
            JSONArray containersArray = body.getJSONArray("containers");
            JSONObject containers = containersArray.getJSONObject(0);
            String image = containers.getString("image");
            int index = image.lastIndexOf(':');
            containers.replace("image", image.substring(0, index) + ":" + imageTag);
        }

        // 4. 重新部署工作负载
        RequestEntity<JSONObject> redeployRequestEntity = RequestEntity.put(URI.create(workloadApiUrl))
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body);
        ResponseEntity<String> redeployResponseEntity = restTemplate.exchange(redeployRequestEntity, String.class);
        if (redeployResponseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException(redeployResponseEntity.toString());
        }
    }

    /**
     * 钉钉发送链接消息
     */
    private OapiRobotSendResponse sendLinkMessage(String workloadApiUrl, String bearerTokenName, String imageTag) {
        // 1. 初始化链接消息
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("link");
        OapiRobotSendRequest.Link link = new OapiRobotSendRequest.Link();

        // 2. 组装消息体
        int beginIndex = workloadApiUrl.lastIndexOf(":") + 1;
        String workloadName = workloadApiUrl.substring(beginIndex);
        String workloadUrl = workloadApiUrlToWorkloadUrl(workloadApiUrl);
        String dateTime = DateUtils.getDatetimeFormatter();
        link.setMessageUrl(workloadUrl);
        link.setPicUrl("");
        link.setTitle("Rancher DevOps");
        link.setText(
                dateTime + "\n哇哦...工作负载【" + workloadName + ":" + bearerTokenName + ":" + imageTag + "】正在升级，赶快看看吧！"
        );
        request.setLink(link);

        // 3. 发送消息
        return sendRequest(request);
    }

    /**
     * 钉钉发送文本消息
     */
    private OapiRobotSendResponse sendTextMessage(String workloadApiUrl, String bearerTokenName, String imageTag) {
        // 1. 初始化文本消息
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("text");
        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();

        // 2. 组装消息体
        int beginIndex = workloadApiUrl.lastIndexOf(":") + 1;
        String workloadName = workloadApiUrl.substring(beginIndex);
        String workloadUrl = workloadApiUrlToWorkloadUrl(workloadApiUrl);
        String yueOpenDevopsDeployWorkloadUrl = devopsDeployProperties.getYueOpenDevopsDeployWorkloadUrl();
        String dateTime = DateUtils.getDatetimeFormatter();
        text.setContent(
                dateTime
                        + "\n警告...警告！工作负载【" + workloadName + ":" + bearerTokenName + ":" + imageTag + "】升级失败...\n"
                        + "请点击以下链接检查 " + applicationName + " 部署日志：\n"
                        + yueOpenDevopsDeployWorkloadUrl + "\n"
                        + "若需要手动进行工作负载升级，请访问如下地址：\n"
                        + workloadUrl + "\n"
        );
        request.setText(text);

        // 3. 设置@人
        OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
        List<String> atMobiles = devopsDeployProperties.getDingtalkAtMobiles();
        at.setAtMobiles(atMobiles);
        request.setAt(at);

        // 4. 发送消息
        return sendRequest(request);
    }

    /**
     * 发送请求
     */
    private OapiRobotSendResponse sendRequest(OapiRobotSendRequest request) {
        OapiRobotSendResponse oapiRobotSendResponse = null;
        try {
            String dingtalkDevopsRobotWebhook = devopsDeployProperties.getDingtalkDevopsRobotWebhook();
            String dingtalkDevopsRobotSignSecret = devopsDeployProperties.getDingtalkDevopsRobotSignSecret();
            
			DingTalkClient dingTalkClient;
			if (StrUtil.isNotEmpty(dingtalkDevopsRobotSignSecret)) {
				dingTalkClient = new DefaultDingTalkClient(SecureCommon.dingtalkRobotSign(dingtalkDevopsRobotWebhook, dingtalkDevopsRobotSignSecret));
			} else {
				dingTalkClient = new DefaultDingTalkClient(dingtalkDevopsRobotWebhook);
			}

			oapiRobotSendResponse = dingTalkClient.execute(request);
        } catch (ApiException e) {
            e.printStackTrace();
        }

        return oapiRobotSendResponse;
    }

    private String workloadApiUrlToWorkloadUrl(String workloadApiUrl) {
        return workloadApiUrl.replaceFirst("v3/project", "p").replaceFirst("workloads", "workload");
    }

}
