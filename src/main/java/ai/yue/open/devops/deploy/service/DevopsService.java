package ai.yue.open.devops.deploy.service;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;

import ai.yue.library.base.crypto.client.SecureCommon;
import ai.yue.library.base.util.DateUtils;
import ai.yue.library.base.util.StringUtils;
import ai.yue.library.base.view.Result;
import ai.yue.library.base.view.ResultInfo;
import ai.yue.open.devops.deploy.config.DevopsDeployProperties;
import ai.yue.open.devops.deploy.constant.EnvEnum;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;

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
     * @param workloadApiUrl 请求的路径
     * @param envEnum        环境信息
     * @param tag            版本
     * @return
     */
    public Result<?> redeploy(String workloadApiUrl, EnvEnum envEnum, String tag) {
        // 1. 处理请求异常
        OapiRobotSendResponse oapiRobotSendResponse;
        try {
            // 发送请求
            redeployRequest(workloadApiUrl, envEnum, tag);
            // 发送通知
            oapiRobotSendResponse = sendLinkMessage(workloadApiUrl, envEnum);
        } catch (Exception e) {
			oapiRobotSendResponse = sendTextMessage(workloadApiUrl, envEnum);
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
        String ResultMsg = StrUtil.format("【钉钉】通知结果：{}", logInfo.toString());
        Console.log(ResultMsg);
        
        // 3. 返回结果
        return ResultInfo.success(ResultMsg);
    }

    /**
     * 重新部署请求
     *
     * @param workloadApiUrl 请求的路径
     * @param envEnum        环境信息
     * @param tag            版本标签
     * @throws IOException
     */
    private void redeployRequest(String workloadApiUrl, EnvEnum envEnum, String tag) throws IOException {
        String bearerToken = null;
        if (envEnum == EnvEnum.DEV) {
            bearerToken = devopsDeployProperties.getDevBearerToken();
        } else if (envEnum == EnvEnum.PRETEST) {
            bearerToken = devopsDeployProperties.getPretestBearerToken();
        } else if (envEnum == EnvEnum.MASTER) {
            bearerToken = devopsDeployProperties.getMasterBearerToken();
        }

        // 1. 获取工作负载信息
        RequestEntity<Void> requestEntity = RequestEntity.get(URI.create(workloadApiUrl))
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .build();
        ResponseEntity<JSONObject> result = restTemplate.exchange(requestEntity, JSONObject.class);

        // 2. 替换请求参数
        JSONObject body = result.getBody();
        String cattle_io_timestamp = LocalDateTime.now(Clock.systemUTC()).format(DateUtils.FORMATTER) + "Z";
        JSONObject annotations = body.getJSONObject("annotations");
        annotations.replace("cattle.io/timestamp", cattle_io_timestamp);
        body.replace("annotations", annotations);

		// 如果版本信息不为空，替换镜像值
        if (StringUtils.isNotEmpty(tag)) {
            JSONArray containersArray = body.getJSONArray("containers");
            JSONObject containers = containersArray.getJSONObject(0);
            String image = containers.getString("image");
            int index = image.lastIndexOf(':');
            containers.replace("image", image.substring(0, index) + ":" + tag);
        }

        // 3. 重新部署工作负载
        RequestEntity<JSONObject> redeployRequestEntity = RequestEntity.put(URI.create(workloadApiUrl))
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                .body(body);
        ResponseEntity<String> redeployResponseEntity = restTemplate.exchange(redeployRequestEntity, String.class);
        if (redeployResponseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException(redeployResponseEntity.toString());
        }
    }

    /**
     * 钉钉发送文本消息
     *
     * @param workloadApiUrl
     * @param envEnum
     * @return
     */
    private OapiRobotSendResponse sendTextMessage(String workloadApiUrl, EnvEnum envEnum) {
        // 1. 初始化文本消息
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("text");
        OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();

        // 2. 组装消息体
        String env = envEnum.name();
        int beginIndex = workloadApiUrl.lastIndexOf(":") + 1;
        String workloadName = workloadApiUrl.substring(beginIndex);
        String workloadUrl = workloadApiUrlToWorkloadUrl(workloadApiUrl);
        String yueOpenDevopsDeployWorkloadUrl = devopsDeployProperties.getYueOpenDevopsDeployWorkloadUrl();
        String dateTime = DateUtils.getDatetimeFormatter();
        text.setContent(
                dateTime
                        + "\n警告...警告！工作负载【" + workloadName + ":" + env + "】升级失败...\n"
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
     * 钉钉发送链接消息
     *
     * @param workloadApiUrl
     * @param envEnum
     * @return
     */
    private OapiRobotSendResponse sendLinkMessage(String workloadApiUrl, EnvEnum envEnum) {
        // 1. 初始化链接消息
        OapiRobotSendRequest request = new OapiRobotSendRequest();
        request.setMsgtype("link");
        OapiRobotSendRequest.Link link = new OapiRobotSendRequest.Link();

        // 2. 组装消息体
        String env = envEnum.name();
        int beginIndex = workloadApiUrl.lastIndexOf(":") + 1;
        String workloadName = workloadApiUrl.substring(beginIndex);
        String workloadUrl = workloadApiUrlToWorkloadUrl(workloadApiUrl);
        String dateTime = DateUtils.getDatetimeFormatter();
        link.setMessageUrl(workloadUrl);
        link.setPicUrl("");
        link.setTitle("Rancher DevOps");
        link.setText(
                dateTime + "\n哇哦...工作负载【" + workloadName + ":" + env + "】正在升级，赶快看看吧！"
        );
        request.setLink(link);

        // 3. 发送消息
        return sendRequest(request);
    }

    /**
     * 发送请求
     *
     * @param request
     * @return
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
