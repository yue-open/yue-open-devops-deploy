//package ai.yue.open.dwz.controller.admin;
//
//import java.util.Map;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.alibaba.fastjson.JSONObject;
//import com.sbs.he.campus.library.base.util.AESUtils;
//import com.sbs.he.campus.library.base.util.MapUtils;
//import com.sbs.he.campus.library.base.view.Result;
//import com.sbs.he.campus.library.base.view.ResultInfo;
//import com.sbs.he.campus.library.publics.constant.KeytConstants;
//
//import ai.yue.open.dwz.service.AdminService;
//
///**
// * @author  孙金川
// * @version 创建时间：2018年3月21日
// */
//@RequestMapping("/admin")
//@RestController
//public class AdminViewController {
//
//	@Autowired
//	AdminService adminService;
//	
//	/**
//	 * 插入数据
//	 * @param paramMap
//	 * @return
//	 */
//	@PostMapping("/insert")
//	public Result<?> insert(@RequestParam Map<String, Object> paramMap) {
//		return adminService.insert(paramMap);
//	}
//	
//	/**
//	 * 分页查询
//	 * @param paramMap
//	 * @return
//	 */
//	@GetMapping("/page")
//	public Map<String, Object> page(@RequestParam Map<String, Object> paramMap) {
//		return adminService.page(new JSONObject(paramMap));
//	}
//	
//	/**
//	 * 更新数据
//	 * @param paramMap
//	 * @return
//	 */
//	@PutMapping("/update")
//	public Result<?> update(@RequestParam Map<String, Object> paramMap) {
//		return adminService.update(paramMap);
//	}
//	
//	/**
//	 * 更新密码
//	 * @param paramMap
//	 * @return
//	 */
//	@PutMapping("/passwordUpdate")
//	public Result<?> passwordUpdate(@RequestParam Map<String, Object> paramMap) {
//		String[] keys = {"id", "password", "oldPassword"};
//		if (MapUtils.isKeys(paramMap, keys)) {
//	        String password = AESUtils.encrypt((String) paramMap.get("password"), KeytConstants.AES_PASSWORD);// 加密明文匹配
//	        String oldPassword = AESUtils.encrypt((String) paramMap.get("oldPassword"), KeytConstants.AES_PASSWORD);
//	        paramMap.replace("password", password);
//	        paramMap.replace("oldPassword", oldPassword);
//			return adminService.passwordUpdate(paramMap);
//		}
//		return ResultInfo.attack();
//	}
//	
//	/**
//	 * 删除数据
//	 * @param paramMaps
//	 * @return
//	 */
//	@DeleteMapping("/delete")
//	public Result<?> delete(@RequestBody Map<String, Object>[] paramMaps) {
//		return adminService.delete(paramMaps);
//	}
//	
//}
