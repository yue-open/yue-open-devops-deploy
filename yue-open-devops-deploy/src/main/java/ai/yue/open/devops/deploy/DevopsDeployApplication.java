package ai.yue.open.devops.deploy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Rancher持续部署
 *
 * @author ylyue
 * @since 2018年11月8日
 */
@SpringBootApplication
public class DevopsDeployApplication {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(DevopsDeployApplication.class, args);
	}

}
