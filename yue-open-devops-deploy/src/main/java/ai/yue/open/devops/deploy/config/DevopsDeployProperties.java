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
	 * 钉钉DevOps机器人密钥，机器人安全设置页面，加签一栏下面显示的SEC开头的字符串
	 * <p>不采用加签验证时可不填
	 */
	private String dingtalkDevopsRobotSignSecret;
	
	/**
	 * 钉钉通知@群成员手机号
	 */
	private List<String> dingtalkAtMobiles;
	
	/**
	 * <p>yue-open-devops-deploy 部署在 Rancher 中的工作负载地址
	 * <p>用于快速查看 yue-open-devops-deploy 容器日志，有助于方便排查 CD 失败时的异常详情。
	 */
	private String yueOpenDevopsDeployWorkloadUrl;
	
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
