package com.hqz.ai.doc.http.file.server.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hqz.ai.doc.http.file.server.config.FileServerProperties;

import ai.yue.library.base.util.StringUtils;
import ai.yue.library.base.util.URIUtils;
import ai.yue.library.base.util.UUIDUtils;
import ai.yue.library.base.util.servlet.ServletUtils;
import ai.yue.library.base.view.Result;
import ai.yue.library.base.view.ResultInfo;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author  孙金川
 * @version 创建时间：2018年1月18日
 */
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

	@Autowired
	FileServerProperties fileServerProperties;
	@Autowired
	HttpServletResponse response;
	
	/**
	 * 文件上传
	 * @param files
	 * @param projectName
	 * @return
	 */
	@PostMapping("/upload")
	public Result<?> upload(@RequestParam("files") MultipartFile[] files,
			@RequestParam("projectName") String projectName) {
		// 1. 参数校验
		if (ArrayUtil.isEmpty(files)) {
			return ResultInfo.fileEmpty();
		}
		
		// 2. 确认支持上传的项目
		List<String> project_name_list = fileServerProperties.getProjectName();
		if (!ArrayUtil.contains(Convert.toStrArray(project_name_list), projectName)) {
			return ResultInfo.paramValueInvalid(projectName);
		}
		
		// 3. 获得文件信息
		String file_server_home = fileServerProperties.getFileServerHome();
		int index = 0;
		JSONArray fileAddressJSONArray = new JSONArray();
		String serverDownloadURL = ServletUtils.getServerURL() + "/file/download/";
		try {
			for (MultipartFile multipartFile : files) {
				// 4. 获得文件名后缀
				String originalFilename = multipartFile.getOriginalFilename();
				int fileSuffixIndex = originalFilename.lastIndexOf(".");
				String fileSuffix = originalFilename.substring(fileSuffixIndex);
				String fileURI = projectName + "/" + UUIDUtils.lowerCaseUUID() + fileSuffix;
				String filePath = file_server_home + fileURI;
				String fileURL = serverDownloadURL + fileURI;
				
				// 5. 保存文件地址
				String fileName = originalFilename.substring(0, fileSuffixIndex);
				JSONObject fileAddressJSON = new JSONObject();
				fileAddressJSON.put("fileName", fileName);
				fileAddressJSON.put("filePath", filePath);
				fileAddressJSON.put("fileURL", fileURL);
				fileAddressJSONArray.add(fileAddressJSON);
				
				// 6. 检测目录是否存在
				File serverFile = new File(filePath);
				File parentFile = serverFile.getParentFile();
				if (!parentFile.exists()) {
					parentFile.mkdirs();// 新建文件夹
				}
			    
			    // 7. 将接收到的文件传输到给定的目标文件。
			    multipartFile.transferTo(serverFile);
			    index++;
			}
		} catch (IllegalStateException | IOException e) {
			log.error(e.getMessage());
			return ResultInfo.error("第 " + index + " 个文件上传失败  ==> " + e.getMessage());
		}
		
		// 8. 返回结果
		return ResultInfo.success(fileAddressJSONArray);
	}
	
	/**
	 * 文件下载
	 * @param projectName
	 * @param fileURL
	 * @param downloadFileName
	 * @param isForceDownload
	 */
	@GetMapping("/download/{projectName}/{fileURL}")
	public void download(@PathVariable String projectName, @PathVariable String fileURL,
			@RequestParam(value = "downloadFileName", required = false) String downloadFileName,
			@RequestParam(value = "isForceDownload", required = false) boolean isForceDownload) {
		// 1. 获得文件信息
		String file_server_home = fileServerProperties.getFileServerHome();
		File file = new File(file_server_home + projectName + "/" + fileURL);
		
		// 2. 设置文件名
		String fileName = file.getName();
		if (StringUtils.isNotEmpty(downloadFileName)) {
			if (!downloadFileName.contains(".")) {
				downloadFileName += fileName.substring(fileName.lastIndexOf("."));
			}
		} else {
			downloadFileName = fileName;
		}
		downloadFileName = URIUtils.encode(downloadFileName);
		
		// 3. 确认文件是否存在
		if (!file.exists()) {
			return;
		}
		
		// 4. 是否强制下载
		String[] suffixes = { ".jpg", ".png" };
		if (!StrUtil.endWithAny(fileURL, suffixes) || isForceDownload) {
			response.setContentType("application/force-download");// 设置强制下载不打开
			response.addHeader("Content-Disposition", "attachment;fileName=" + downloadFileName);// 设置文件名
		}
		
		// 5. 写入到HTTP响应输出流
		OutputStream out;
		try {
			out = response.getOutputStream();
			IoUtil.write(out, false, FileUtil.readBytes(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
