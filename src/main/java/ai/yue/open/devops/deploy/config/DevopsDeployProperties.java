package ai.yue.open.devops.deploy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * 配置属性定义
 *
 * @author ylyue
 * @since 2018年11月6日
 */
@Data
@Configuration
@ConfigurationProperties("yue.devops.deploy")
public class DevopsDeployProperties {

	/**
	 * Rancher bearerToken，用于调用Rancher API。
	 * <p>格式：key: value</p>
	 * <p>示例：token1: Bearer token-nl2g5:5v1zk6n6w66lz86fz26m22gsj511211r5lt29lf1n5666dr1r2n8zk</p>
	 * <p>必填的</p>
	 */
	private Map<String, String> bearerTokens;

	/**
	 * <p>yue-open-devops-deploy 部署在 Rancher 中的工作负载地址
	 * <p>用于快速查看 yue-open-devops-deploy 容器日志，有助于方便排查 CD 失败时的异常详情。
	 * <p>必填的</p>
	 */
	private String yueOpenDevopsDeployWorkloadUrl;

	/**
	 * 钉钉DevOps机器人Webhook
	 * <p>可选的</p>
	 */
	private String dingtalkDevopsRobotWebhook;
	
	/**
	 * 钉钉DevOps机器人密钥，机器人安全设置页面，加签一栏下面显示的SEC开头的字符串
	 * <p>不采用加签验证时可不填
	 * <p>可选的</p>
	 */
	private String dingtalkDevopsRobotSignSecret;
	
	/**
	 * 钉钉通知@群成员手机号
	 * <p>可选的</p>
	 */
	private List<String> dingtalkAtMobiles;

}
