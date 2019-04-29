package ai.yue.open.dwz.controller.login;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import ai.yue.library.base.config.properties.ConstantProperties;
import ai.yue.library.base.constant.TokenConstant;
import ai.yue.library.base.util.AESUtils;
import ai.yue.library.base.util.CookieUtils;
import ai.yue.library.base.util.CurrentLineInfo;
import ai.yue.library.base.util.ParamUtils;
import ai.yue.library.base.util.StringUtils;
import ai.yue.library.base.util.URIUtils;
import ai.yue.library.base.view.Result;
import ai.yue.library.base.view.ResultInfo;
import ai.yue.open.dwz.dao.AdminDAO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author  孙金川
 * @version 创建时间：2017年10月22日
 */
@Slf4j
@RestController
@RequestMapping("/open")
public class LoginController {
	
	@Autowired
	AdminDAO adminDAO;
	@Autowired
	ConstantProperties constantProperties;
	@Autowired
	HttpServletRequest request;
	@Autowired
	HttpServletResponse response;
	@Autowired
	HttpSession session;
	
    /**
     * 登录
     * TODO 会话机制安全，如关闭后分布式session未失效，再次打开无需登录。登录HTTPS加密传输等。IP锁。自动更新redis中session，自动更新cookie中token
     * TODO 要求判定session时间为30分钟，每次刷新重新计时。关闭浏览器任然不失效。若保存session一周，就判定为一周离线时长，每次刷新重新计时。
     * @param text
     * @return
     */
	@PostMapping("/login")
	public Result<?> login(@RequestBody String text) {
		// 1. 解析参数
		JSONObject paramJSON = URIUtils.rsaUriDecodingAndDecrypt(text, constantProperties.getRsa_private_keyt());
		String captcha = paramJSON.getString("captcha");
		String randCaptcha = (String) session.getAttribute("randCaptcha");
		if (StringUtils.isEmpty(randCaptcha) || !randCaptcha.equals(captcha)) {
			return ResultInfo.captcha_error();
		}
		session.removeAttribute("randCaptcha");
		
		// 1. 校验参数
		String[] keys = {"username", "password"};
		ParamUtils.paramValidate(paramJSON, keys);
		String username = paramJSON.getString("username");
		String password = paramJSON.getString("password");
		
		// 2. 查询
        password = AESUtils.encrypt(password, constantProperties.getAes_keyt());// 加密明文匹配
        paramJSON.replace("password", password);
        JSONObject admin = adminDAO.get(username, password);
        if (admin == null) {
        	return ResultInfo.user_or_password_error();
        }
		if (!adminDAO.isAdminStatus(username)) {
			return ResultInfo.user_stop();
		}
		
		// 3. 设置token至redis
		String token = UUID.randomUUID().toString();
		session.setAttribute(TokenConstant.COOKIE_TOKEN_KEY, token);
		
		// 4. 设置token至cookie
		CookieUtils.set(TokenConstant.COOKIE_TOKEN_KEY, token, constantProperties.getToken_timeout());
        
        // 5. 打印日志
		Long userId = admin.getLong("id");
		String role_name = admin.getString("role_name");
		session.setAttribute("userId", userId);
		session.setAttribute("username", username);
		session.setAttribute("role_name", role_name);
		log.info("uri={}", request.getRequestURI());
		log.info("userId={}", userId);
		log.info("username={}", username);
		log.info("role_name={}", role_name);
		log.info("method={}", request.getMethod());
		log.info("ip={}", request.getRemoteHost());
		log.info("class_method={}", LoginController.class.getName() + "." + CurrentLineInfo.getMethodName() + "()");
		
		// 6. 返回结果
		return ResultInfo.success();
	}
	
	/**
	 * 登出
	 * @return
	 */
	@GetMapping("/logout")
    public Result<?> logout() {
		// 1. 查询cookie
		Cookie cookie = CookieUtils.get(TokenConstant.COOKIE_TOKEN_KEY);
		String token = "";
        if (null == cookie) {
        	token = request.getHeader(TokenConstant.COOKIE_TOKEN_KEY);
        }else {
        	token = cookie.getValue();
        }
		if (StringUtils.isEmpty(token)) {
			return ResultInfo.unauthorized();
		}
		
		// 2. 清除cookie
		CookieUtils.set(TokenConstant.COOKIE_TOKEN_KEY, null, 0);
		
		// 3. 返回结果
		return ResultInfo.success();
    }
	
}
