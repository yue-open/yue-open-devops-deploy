package ai.yue.open.dwz.controller.view;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ai.yue.library.base.util.ObjectUtils;
import ai.yue.open.dwz.constant.RoleEnum;
import ai.yue.open.dwz.service.AdminService;
import ai.yue.open.dwz.service.DwzService;

/**
 * @author  孙金川
 * @version 创建时间：2018年3月22日
 */
@Controller
public class ViewController {
	
	@Autowired
	DwzService dwzService;
	@Autowired
	AdminService adminService;
	
	@GetMapping("/")
	public String login() {
		return "/login";
	}
	
	@GetMapping("/index")
	public String index(HttpSession session, ModelMap modelMap) {
		Long userId = (Long) session.getAttribute("userId");
		String username = (String) session.getAttribute("username");
		String role_name = (String) session.getAttribute("role_name");
		modelMap.addAttribute("userId", userId);
		modelMap.addAttribute("username", username);
		modelMap.addAttribute("role_name", role_name);
		return "/index";
	}
	
	@GetMapping("/dwz/list")
	public String dwz_list() {
		return "/dwz/list";
	}
	
	@GetMapping("/dwz/edit")
	public String dwzEdit(@RequestParam("id") Long id, ModelMap modelMap) {
		if (id != 0) {
			modelMap.addAttribute("edit", dwzService.getEdit(id));
			modelMap.addAttribute("title", "更新");
		}else {
			modelMap.addAttribute("title", "增加");
		}
		return "/dwz/edit";
	}
	
	@GetMapping("/admin/list")
	public String adminList(HttpSession session) {
		RoleEnum roleEnum = ObjectUtils.toJavaObject(session.getAttribute("role_name"), RoleEnum.class);
		if (roleEnum == RoleEnum.普通管理员) {
			return "/index";
		}
		
		return "/admin/list";
	}
	
	@GetMapping("/admin/edit")
	public String adminEdit(@RequestParam("id") Long id, ModelMap modelMap) {
		if (id != 0) {
			modelMap.addAttribute("edit", adminService.getEdit(id));
			modelMap.addAttribute("title", "更新");
		}else {
			modelMap.addAttribute("title", "增加");
		}
		return "/admin/edit";
	}
	
	@GetMapping("/admin/password")
	public String adminPassword(@RequestParam("id") Integer id, ModelMap modelMap) {
		modelMap.addAttribute("id", id);
		modelMap.addAttribute("title", "更新");
		return "/admin/password";
	}
	
}
