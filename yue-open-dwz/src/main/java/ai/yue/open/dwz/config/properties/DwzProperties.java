package ai.yue.open.dwz.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * @author	ylyue
 * @since	2018年11月6日
 */
@Data
@ConfigurationProperties("yue.open.dwz")
public class DwzProperties {
	
	/**
	 * 短网址域名
	 * <p>
	 * 默认：localhost
	 */
	private String domain_name = "localhost";
	
	/**
	 * 鉴权标识-key
	 */
	private String admin_key = "yue-open";
	
	/**
	 * 鉴权标识-密钥
	 */
	private String admin_keyt = "E97FD2C326FC47129D6FB5D8C1C33C8D";
	
}
