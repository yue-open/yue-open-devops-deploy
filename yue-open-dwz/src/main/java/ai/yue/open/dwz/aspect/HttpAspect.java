package ai.yue.open.dwz.aspect;

import javax.servlet.http.HttpServletRequest;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import ai.yue.library.base.exception.AttackException;
import ai.yue.library.base.exception.ForbiddenException;
import ai.yue.library.base.util.NetUtils;
import ai.yue.library.base.util.servlet.ServletUtils;
import ai.yue.library.data.redis.client.User;
import ai.yue.open.dwz.constant.RoleEnum;
import ai.yue.open.dwz.dataobject.AdminDO;
import cn.hutool.core.util.ArrayUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author	ylyue
 * @since	2017年10月14日
 */
@Slf4j
@Aspect
@Component
public class HttpAspect {
	
	@Autowired
	User user;
	@Autowired
	private Environment environment;
	@Autowired
	private HttpServletRequest request;
	
	@Pointcut(ServletUtils.POINTCUT)
	public void pointcut() {}
	
	@Before("pointcut()")
	public void doPointcutBefore(JoinPoint joinPoint) {
		// 1. 获得请求上下文信息
		String ip = request.getRemoteHost();
		String uri = request.getRequestURI();
		
		// 2. 检验环境禁止外网访问后台
		String[] env = environment.getActiveProfiles();
		if (ArrayUtil.contains(env, "master") && !NetUtils.isInnerIP()) {
			throw new AttackException("禁止外网访问后台");
		}
		if (uri.equals("/") || uri.startsWith("/open")) {
			return;
		}
		
		// 3. 权限检查
		AdminDO userDO = user.getUser(AdminDO.class);
        Long user_id = userDO.getUser_id();
        String username = userDO.getUsername();
        RoleEnum role = userDO.getRole();
        if (role == RoleEnum.普通管理员) {
        	if (uri.contains("admin") && !uri.contains("password")) {
        		throw new ForbiddenException("普通管理员");
        	}
        }
        
        // 4. 开发环境-打印日志
		log.info("uri={}", uri);
		log.info("userId={}", user_id);
		log.info("username={}", username);
		log.info("role={}", role);
		log.info("method={}", request.getMethod());
		log.info("ip={}", ip);
		log.info("class_method={}", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName() + "()");
	}
	
}
