package ai.yue.open.devops.deploy.service;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;

import ai.yue.library.base.util.DateUtils;
import ai.yue.library.base.view.Result;
import ai.yue.library.base.view.ResultInfo;
import ai.yue.open.devops.deploy.config.DevopsDeployProperties;
import ai.yue.open.devops.deploy.constant.EnvEnum;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;

/**
 * @author  孙金川
 * @version 创建时间：2018年11月12日
 */
@Service
public class DevopsService {

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private DevopsDeployProperties devopsDeployProperties;
	private DingTalkClient dingTalkClient;
	
	/**
	 * 初始化client
	 */
	@PostConstruct
	private void init() {
		String dingtalkDevopsRobotWebhook = devopsDeployProperties.getDingtalkDevopsRobotWebhook();
		dingTalkClient = new DefaultDingTalkClient(dingtalkDevopsRobotWebhook);
	}
	
	/**
	 * 重新部署
	 * @param url
	 * @param envEnum
	 * @return
	 */
	public Result<?> redeploy(String url, EnvEnum envEnum) {
		// 1. 处理请求异常
		OapiRobotSendResponse oapiRobotSendResponse;
		try {
			// 发送请求
			redeployRequest(url, envEnum);
			// 发送通知
			oapiRobotSendResponse = sendLinkMessage(url, envEnum);
		} catch (Exception e) {
			// 发送失败通知
			oapiRobotSendResponse = sendTextMessage(url, envEnum);
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
	 * @param url
	 * @param envEnum
	 * @throws IOException 
	 */
	private void redeployRequest(String url, EnvEnum envEnum) throws IOException {
		String bearerToken = null;
		if (envEnum == EnvEnum.DEV) {
			bearerToken = devopsDeployProperties.getDevBearerToken();
		} else if (envEnum == EnvEnum.PRETEST) {
			bearerToken = devopsDeployProperties.getPretestBearerToken();
		} else if (envEnum == EnvEnum.MASTER) {
			bearerToken = devopsDeployProperties.getMasterBearerToken();
		}
		
		// 1. 获取工作负载信息
		RequestEntity<Void> requestEntity = RequestEntity.get(URI.create(url))
    	.header(HttpHeaders.AUTHORIZATION, bearerToken)
    	.build();
		ResponseEntity<JSONObject> result = restTemplate.exchange(requestEntity, JSONObject.class);
		
		// 2. 替换请求参数
		JSONObject body = result.getBody();
		String cattle_io_timestamp = LocalDateTime.now(Clock.systemUTC()).format(DateUtils.FORMATTER) + "Z";
		JSONObject annotations = body.getJSONObject("annotations");
		annotations.replace("cattle.io/timestamp", cattle_io_timestamp);
		body.replace("annotations", annotations);
		
		// 3. 重新部署工作负载
    	RequestEntity<JSONObject> redeployRequestEntity = RequestEntity.put(URI.create(url))
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
	 * @param url
	 * @param envEnum
	 * @return
	 */
	private OapiRobotSendResponse sendTextMessage(String url, EnvEnum envEnum) {
		// 1. 初始化文本消息
		OapiRobotSendRequest request = new OapiRobotSendRequest();
		request.setMsgtype("text");
		OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
		
		// 2. 组装消息体
		String env = envEnum.name();
		int beginIndex = url.lastIndexOf(":") + 1;
		String workloadName = url.substring(beginIndex);
		String workload_url = workloadApiUrlToWorkloadUrl(url);
		String workloadApiUrl = devopsDeployProperties.getWorkloadApiUrl();
		String dateTime = DateUtils.getDatetimeFormatter();
    	text.setContent(
				dateTime
				+ "\n警告...警告！工作负载【" + workloadName + ":" + env + "】升级失败...\n"
				+ "请点击以下链接检查部署日志：\n"
				+ workloadApiUrl + "\n"
				+ "若需要手动进行工作负载升级，请访问如下地址：\n"
				+ workload_url + "\n"
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
	 * @param url
	 * @param envEnum
	 * @return
	 */
	private OapiRobotSendResponse sendLinkMessage(String url, EnvEnum envEnum) {
		// 1. 初始化链接消息
    	OapiRobotSendRequest request = new OapiRobotSendRequest();
    	request.setMsgtype("link");
    	OapiRobotSendRequest.Link link = new OapiRobotSendRequest.Link();
    	
    	// 2. 组装消息体
		String env = envEnum.name();
		int beginIndex = url.lastIndexOf(":") + 1;
		String workloadName = url.substring(beginIndex);
		String workload_url = workloadApiUrlToWorkloadUrl(url);
		String dateTime = DateUtils.getDatetimeFormatter();
    	link.setMessageUrl(workload_url);
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
	 * @param request
	 * @return
	 */
	private OapiRobotSendResponse sendRequest(OapiRobotSendRequest request) {
		OapiRobotSendResponse oapiRobotSendResponse = null;
    	try {
    		oapiRobotSendResponse = dingTalkClient.execute(request);
		} catch (ApiException e) {
			e.printStackTrace();
		}
    	
    	return oapiRobotSendResponse;
	}
    
	private String workloadApiUrlToWorkloadUrl(String url) {
		return url.replaceFirst("v3/project", "p").replaceFirst("workloads", "workload");
	}
	
}
