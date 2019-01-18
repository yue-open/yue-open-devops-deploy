package ai.yue.open.devops.deploy.service;

import java.io.IOException;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;

import ai.yue.library.base.util.DateUtils;
import ai.yue.library.base.view.Result;
import ai.yue.library.base.view.ResultInfo;
import ai.yue.open.devops.deploy.config.ConstantProperties;
import ai.yue.open.devops.deploy.config.EnvProperties;
import ai.yue.open.devops.deploy.constant.EnvEnum;
import ai.yue.open.devops.deploy.dingtalk.chatbot.DingtalkChatbotClient;
import ai.yue.open.devops.deploy.dingtalk.chatbot.SendResult;
import ai.yue.open.devops.deploy.dingtalk.chatbot.message.LinkMessage;
import ai.yue.open.devops.deploy.dingtalk.chatbot.message.TextMessage;

/**
 * @author  孙金川
 * @version 创建时间：2018年11月12日
 */
@Service
public class DevopsService {

	@Autowired
	RestTemplate restTemplate;
	@Autowired
	DingtalkChatbotClient dingtalkChatbotClient;
	@Autowired
	EnvProperties envProperties;
	@Autowired
	ConstantProperties constantProperties;
	
	/**
	 * 重新部署
	 * @param url
	 * @param envEnum
	 * @return
	 */
	public Result<?> redeploy(String url, EnvEnum envEnum) {
		// 1. 处理请求异常
		SendResult sendResult;
		try {
			// 发送请求
			redeployRequest(url, envEnum);
			// 发送通知
			sendResult = sendLinkMessage(url, envEnum);
		} catch (Exception e) {
			// 发送失败通知
			sendResult = sendTextMessage(url, envEnum);
			// 打印请求异常
			e.printStackTrace();
		}
		
		// 2. 返回结果
		System.out.println(sendResult);
		return ResultInfo.success(sendResult);
	}
	
	/**
	 * 重新部署请求
	 * @param url
	 * @param envEnum
	 * @throws IOException 
	 */
	void redeployRequest(String url, EnvEnum envEnum) throws IOException {
		String bearerToken = null;
		if (envEnum == EnvEnum.DEV) {
			bearerToken = envProperties.getDevBearerToken();
		} else if (envEnum == EnvEnum.PRETEST) {
			bearerToken = envProperties.getPretestBearerToken();
		} else if (envEnum == EnvEnum.MASTER) {
			bearerToken = envProperties.getMasterBearerToken();
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
	 * 钉钉发送链接消息
	 * @param url
	 * @param envEnum
	 * @return
	 */
	SendResult sendLinkMessage(String url, EnvEnum envEnum) {
		String env = envEnum.name();
		int beginIndex = url.lastIndexOf(":") + 1;
		String workloadName = url.substring(beginIndex);
		String workload_url = workloadApiUrlToWorkloadUrl(url);
		String dateTime = DateUtils.get_y_M_d_H_m_s();
        LinkMessage message = new LinkMessage();
        message.setTitle("Rancher DevOps");
        message.setMessageUrl(workload_url);
		message.setText(dateTime + "\n哇哦...工作负载【" + workloadName + ":" + env + "】正在升级，赶快看看吧！");
		String dingtalkDevopsRobotWebhook = constantProperties.getDingtalkDevopsRobotWebhook();
        SendResult result = dingtalkChatbotClient.send(dingtalkDevopsRobotWebhook, message);
        return result;
    }
    
	/**
	 * 钉钉发送文本消息
	 * @param url
	 * @param envEnum
	 * @return
	 */
	SendResult sendTextMessage(String url, EnvEnum envEnum) {
		String env = envEnum.name();
		int beginIndex = url.lastIndexOf(":") + 1;
		String workloadName = url.substring(beginIndex);
		String workload_url = workloadApiUrlToWorkloadUrl(url);
		String workloadApiUrl = constantProperties.getWorkloadApiUrl();
		String webhook_workload_url = workloadApiUrlToWorkloadUrl(workloadApiUrl);
		String dateTime = DateUtils.get_y_M_d_H_m_s();
		TextMessage message = new TextMessage(
				dateTime
				+ "\n警告...警告！工作负载【" + workloadName + ":" + env + "】升级失败...\n"
				+ "请点击以下链接检查部署日志：\n"
				+ webhook_workload_url + "\n"
				+ "若需要手动进行工作负载升级，请访问如下地址：\n"
				+ workload_url + "\n");
		List<String> atMobiles = constantProperties.getDingtalkAtMobiles();
		message.setAtMobiles(atMobiles);
		String dingtalkDevopsRobotWebhook = constantProperties.getDingtalkDevopsRobotWebhook();
		SendResult result = dingtalkChatbotClient.send(dingtalkDevopsRobotWebhook, message);
		return result;
	}
	
	String workloadApiUrlToWorkloadUrl(String url) {
		return url.replaceFirst("v3/project", "p").replaceFirst("workloads", "workload");
	}
	
}
