package ai.yue.open.dwz.controller.dwz;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.unbescape.uri.UriEscape;

import com.alibaba.fastjson.JSONObject;

import ai.yue.library.base.config.properties.ConstantProperties;
import ai.yue.library.base.util.RSAUtils;
import ai.yue.library.base.util.StringUtils;
import ai.yue.library.base.view.Result;
import ai.yue.library.base.view.ResultInfo;
import ai.yue.open.dwz.dao.DwzDAO;
import ai.yue.open.dwz.service.DwzService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author  孙金川
 * @version 创建时间：2018年3月20日
 */
@Slf4j
@RestController
public class DwzController {

	@Autowired
	DwzService dwzService;
	@Autowired
	DwzDAO dwzDAO;
	@Value("${admin.key}")
	String adminKey;
	@Value("${admin.keyt}")
	String adminKeyt;
	@Autowired
	ConstantProperties constantProperties;
	
	/**
	 * 生成短网址并返回
	 * @param message
	 * @return
	 */
	@PostMapping("/shorten")
	Result<?> shorten(@RequestBody String message) {
		// 1. 解析加密报文
		String str = RSAUtils.decrypt(UriEscape.unescapeUriFragmentId(message), constantProperties.getRsa_private_keyt());
		JSONObject json = JSONObject.parseObject(str);
		// 2. 提取鉴权信息
		if (!adminKey.equals(json.getString("adminKey")) || !adminKeyt.equals(json.getString("adminKeyt"))) {
			return ResultInfo.forbidden();
		}
		
		String originalUrl = json.getString("url");
		String urlTrim = originalUrl.trim();
		String url = "";
		if (urlTrim.contains("http")) {
			url = urlTrim;
		}else {
			url = "http://" + urlTrim;
		}
		if (!url.equals(originalUrl)) {
			json.replace("url", url);
		}
		return dwzService.shorten(json);
	}
	
	/**
	 * 短网址请求，重定向到真实地址
	 * @param dwz_code
	 * @param response
	 * @throws IOException
	 */
	@GetMapping("/{dwz_code}")
	void request(@PathVariable String dwz_code, HttpServletResponse response) throws IOException {
		log.info("短网址请求... {}", dwz_code);
		if (dwz_code.length() == 6) {
			String url = dwzDAO.getUrl(dwz_code);
			if (!StringUtils.isEmpty(url)) {
				log.info("重定向URL: {}", url);
				response.sendRedirect(url);
			}
		}
	}
	
}
