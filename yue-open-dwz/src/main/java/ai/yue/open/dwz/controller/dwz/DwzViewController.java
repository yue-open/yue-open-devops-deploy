//package ai.yue.open.dwz.controller.dwz;
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
//import com.sbs.he.campus.library.base.view.Result;
//
//import ai.yue.open.dwz.service.DwzService;
//
///**
// * @author  孙金川
// * @version 创建时间：2018年3月22日
// */
//@RequestMapping("/dwz")
//@RestController
//public class DwzViewController {
//
//	@Autowired
//	DwzService dwzService;
//	
//	/**
//	 * 插入数据
//	 * @param paramMap
//	 * @return
//	 */
//	@PostMapping("/insert")
//	public Result<?> insert(@RequestParam Map<String, Object> paramMap) {
//		return dwzService.shorten(paramMap);
//	}
//	
//	/**
//	 * 短网址分页查询
//	 * @param paramMap
//	 * @return
//	 */
//	@GetMapping("/page")
//	public Map<String, Object> page(@RequestParam Map<String, Object> paramMap) {
//		return dwzService.page(new JSONObject(paramMap));
//	}
//	
//	/**
//	 * 更新数据
//	 * @param paramMap
//	 * @return
//	 */
//	@PutMapping("/update")
//	public Result<?> update(@RequestParam Map<String, Object> paramMap) {
//		return dwzService.update(paramMap);
//	}
//	
//	/**
//	 * 删除数据
//	 * @param paramMaps
//	 * @return
//	 */
//	@DeleteMapping("/delete")
//	public Result<?> delete(@RequestBody Map<String, Object>[] paramMaps) {
//		return dwzService.delete(paramMaps);
//	}
//	
//}
