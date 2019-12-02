package ai.yue.open.dwz.controller.login;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import ai.yue.library.base.crypto.client.SecureSingleton;
import ai.yue.library.base.crypto.config.properties.CryptoProperties;
import ai.yue.library.base.util.CurrentLineInfo;
import ai.yue.library.base.util.ParamUtils;
import ai.yue.library.base.view.Result;
import ai.yue.library.base.view.ResultInfo;
import ai.yue.library.base.view.ResultPrompt;
import ai.yue.library.data.redis.client.User;
import ai.yue.open.dwz.constant.RoleEnum;
import ai.yue.open.dwz.dao.AdminDAO;
import ai.yue.open.dwz.dataobject.AdminDO;
import lombok.extern.slf4j.Slf4j;

/**
 * @author	孙金川
 * @since	2017年10月22日
 */
@Slf4j
@RestController
@RequestMapping("/open")
public class LoginController {
	
	@Autowired
	User user;
	@Autowired
	AdminDAO adminDAO;
	@Autowired
	HttpServletRequest request;
	@Autowired
	CryptoProperties cryptoProperties;
	
    /**
     * 登录
     * 
     * @param messageBody
     * @return
     */
	@PostMapping("/login")
	public Result<?> login(@RequestBody String messageBody) {
		// 1. 解析参数
		JSONObject paramJson = SecureSingleton.rsaUriDecodingAndDecrypt(messageBody);
		String captcha = paramJson.getString("captcha");
		user.captchaValidate(captcha);
		
		// 2. 校验参数
		String[] keys = {"username", "password"};
		ParamUtils.paramValidate(paramJson, keys);
		String username = paramJson.getString("username");
		String password = paramJson.getString("password");
		
		// 3. 查询
        password = SecureSingleton.getAES().encryptBase64(password);// 加密明文匹配
        paramJson.replace("password", password);
        AdminDO userDO = adminDAO.get(username, password);
        if (userDO == null) {
        	return ResultInfo.dev_defined(ResultPrompt.USERNAME_OR_PASSWORD_ERROR);
        }
		if (!adminDAO.isAdminStatus(username)) {
			return ResultInfo.dev_defined(ResultPrompt.USER_STOP);
		}
		
		// 4. 登录
		user.login(userDO);
        
        // 5. 打印日志
		Long user_id = userDO.getUser_id();
		RoleEnum role = userDO.getRole();
		log.info("uri={}", request.getRequestURI());
		log.info("user_id={}", user_id);
		log.info("username={}", username);
		log.info("role_name={}", role);
		log.info("method={}", request.getMethod());
		log.info("ip={}", request.getRemoteHost());
		log.info("class_method={}", LoginController.class.getName() + "." + CurrentLineInfo.getMethodName() + "()");
		
		// 6. 返回结果
		return ResultInfo.success(userDO);
	}
	
	/**
	 * 登出
	 * 
	 * @return
	 */
	@GetMapping("/logout")
    public Result<?> logout() {
		return user.logout();
    }
	
}
