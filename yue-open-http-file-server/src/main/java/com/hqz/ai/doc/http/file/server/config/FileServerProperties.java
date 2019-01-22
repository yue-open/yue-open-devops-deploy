package com.hqz.ai.doc.http.file.server.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import cn.hutool.core.io.FileUtil;
import lombok.Data;

/**
 * @author 	孙金川
 * @version 创建时间：2018年11月6日
 */
@Data
@Configuration
@ConfigurationProperties("yue.file-server")
public class FileServerProperties {
	
	/**
	 * 文件服务器路径（绝对路径）
	 */
	final String fileServerHome = FileUtil.normalize(FileUtil.getUserHomePath() + "/file-server-home/");
	
	/**
	 * 支持文件上传的项目名称
	 */
	volatile List<String> projectName;
	
}
