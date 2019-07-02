package ai.yue.open.devops.deploy.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @author 	孙金川
 * @version 创建时间：2018年11月6日
 */
@Data
@Configuration
@ConfigurationProperties("yue.devops.deploy")
public class DevopsDeployProperties {
	
	/**
	 * 钉钉DevOps机器人Webhook
	 */
	private String dingtalkDevopsRobotWebhook;
	
	/**
	 * 钉钉通知@群成员手机号
	 */
	private List<String> dingtalkAtMobiles;
	
	/**
	 * Rancher 工作负载API地址
	 */
	private String workloadApiUrl;
	
	/**
	 * Rancher 开发环境 bearerToken
	 */
	private String devBearerToken;
	
	/**
	 * Rancher 预发环境 bearerToken
	 */
	private String pretestBearerToken;
	
	/**
	 * Rancher 生产环境 bearerToken
	 */
	private String masterBearerToken;
	
}
