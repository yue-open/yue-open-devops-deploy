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
@ConfigurationProperties("yue.devops.deploy.constant")
public class ConstantProperties {
	
	/**
	 * 工作负载API地址
	 */
	volatile String workloadApiUrl;
	
	/**
	 * 钉钉DevOps机器人Webhook
	 */
	volatile String dingtalkDevopsRobotWebhook;
	
	/**
	 * 钉钉通知@群成员手机号
	 */
	volatile List<String> dingtalkAtMobiles;
	
}
