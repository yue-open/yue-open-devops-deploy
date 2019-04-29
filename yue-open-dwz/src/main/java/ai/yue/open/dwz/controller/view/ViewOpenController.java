package ai.yue.open.dwz.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ai.yue.library.base.ipo.CaptchaIPO;
import ai.yue.library.base.util.CaptchaUtils;

/**
 * @author  孙金川
 * @version 创建时间：2018年3月22日
 */
@Controller
@RequestMapping("/open")
public class ViewOpenController {
	
	@GetMapping("/captcha")
	public void captcha() {
		CaptchaUtils.createCaptchaImage(CaptchaIPO.builder().build()).writeResponseAndSetSession();
	}
	
	@GetMapping("/welcome")
	public String welcome() {
		return "/welcome";
	}
	
}
