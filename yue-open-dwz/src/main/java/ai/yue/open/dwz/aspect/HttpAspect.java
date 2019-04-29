package ai.yue.open.dwz.aspect;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import ai.yue.library.base.constant.TokenConstant;
import ai.yue.library.base.exception.AttackException;
import ai.yue.library.base.exception.AuthorizeException;
import ai.yue.library.base.exception.ForbiddenException;
import ai.yue.library.base.util.CookieUtils;
import ai.yue.library.base.util.HttpUtils;
import ai.yue.library.base.util.IPUtils;
import ai.yue.library.base.util.ObjectUtils;
import ai.yue.open.dwz.constant.RoleEnum;
import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author  孙金川
 * @version 创建时间：2017年10月14日
 */
@Slf4j
@Aspect
@Component
public class HttpAspect {
	
	@Autowired
	HttpServletRequest request;
	@Autowired
	Environment environment;
	
	@Pointcut(HttpUtils.POINTCUT)
	public void pointcut() {}
	
	@Before("verify()")
	public void doVerifyBefore(JoinPoint joinPoint) {
		HttpSession session = request.getSession();
		String ip = request.getRemoteHost();
		String uri = request.getRequestURI();
		
		// 1. 检验环境禁止外网访问后台
		String[] env = environment.getActiveProfiles();
		if (ArrayUtil.contains(env, "master") && !IPUtils.isInnerIP()) {
			throw new AttackException("禁止外网访问后台");
		}
		if (uri.equals("/") || uri.startsWith("/open")) {
			return;
		}
		
		// 2. 查询cookie
        Cookie cookie = CookieUtils.get(TokenConstant.COOKIE_TOKEN_KEY);
        String token = "";
		if (cookie == null) {
			log.warn("【登录校验】Cookie中查不到token");
			throw new AuthorizeException(null);
		} else {
			token = cookie.getValue();
		}
        var tokenValue = session.getAttribute(token);
        if (tokenValue == null) {
        	log.warn("【登录校验】session中查不到token");
        	throw new AuthorizeException(null);
        }
        
        // 5. 权限检查
        Long userId = (Long) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        RoleEnum roleEnum = ObjectUtils.toJavaObject(session.getAttribute("role_name"), RoleEnum.class);
        
        if (roleEnum == RoleEnum.普通管理员) {
        	if (uri.contains("admin") && !uri.contains("password")) {
        		throw new ForbiddenException("普通管理员");
        	}
        }
        
        // 6. 开发环境-打印日志
		log.info("uri={}", uri);
		log.info("userId={}", userId);
		log.info("username={}", username);
		log.info("roleEnum={}", roleEnum);
		log.info("method={}", request.getMethod());
		log.info("ip={}", ip);
		log.info("class_method={}", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName() + "()");
	}
	
}
