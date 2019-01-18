package ai.yue.open.devops.deploy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * @author 	孙金川
 * @version 创建时间：2018年11月6日
 */
@Data
@Configuration
@ConfigurationProperties("yue.devops.deploy.env")
public class EnvProperties {

	/**
	 * Rancher 开发环境 bearerToken
	 */
	volatile String devBearerToken;
	
	/**
	 * Rancher 预发环境 bearerToken
	 */
	volatile String pretestBearerToken;
	
	/**
	 * Rancher 生产环境 bearerToken
	 */
	volatile String masterBearerToken;
	
}
