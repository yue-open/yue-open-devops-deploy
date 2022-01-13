package ai.yue.open.devops.deploy.controller;

import ai.yue.library.base.view.Result;
import ai.yue.open.devops.deploy.service.DevopsService;
import cn.hutool.core.lang.Console;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rancher CD
 *
 * @author ylyue
 * @since 2018年11月12日
 */
@RestController
@RequestMapping("/devops")
public class DevopsController {

	@Autowired
	private DevopsService devopsService;

	/**
	 * 重新部署
	 *
	 * @param workloadApiUrl  需要重新部署的工作负载API地址
	 * @param bearerTokenName 配置在yue-open-devops-deploy中，所对应的bearerToken名
	 * @param imageTag        docker镜像版本
	 */
	@PutMapping("/redeploy")
	public Result<?> redeploy(@RequestParam("workloadApiUrl") String workloadApiUrl,
							  @RequestParam("bearerTokenName") String bearerTokenName,
							  @RequestParam(name = "imageTag", required = false) String imageTag) {
		// 1. 打印日志
		JSONObject logInfo = new JSONObject();
		logInfo.put("workloadApiUrl", workloadApiUrl);
		logInfo.put("bearerTokenName", bearerTokenName);
		logInfo.put("imageTag", imageTag);
		Console.log("【工作负载-重新部署】请求参数：{}", logInfo.toString(SerializerFeature.PrettyFormat));
		
		// 2. 重新部署
		return devopsService.redeploy(workloadApiUrl, bearerTokenName, imageTag);
	}
	
}
