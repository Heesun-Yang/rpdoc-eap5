package com.rp.test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rp.docs.eap5.collecting_data.ReportUtil;

public class JsonTest {
	static Logger log = LoggerFactory.getLogger(JsonTest.class);
	

	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		// TODO Auto-generated method stub
		// Jackson ObjectMapper
		ObjectMapper mapper = new ObjectMapper();
		final String  ServiceFile = "data/services.json";
		
		// Load Properties (설치 및 패치 파일명)
		ReportUtil.loadProperties();
				
		File file = new File(ServiceFile);
		byte[] JsonData;
		
		// services.json 파일을 읽어 services_map에 저장한다.
		JsonData = FileUtils.readFileToByteArray(file);
		
		
		// services.json 파일의 내용을 HashMap 으로 변환한다.
		HashMap<String, Object> services_map = mapper.readValue(JsonData, HashMap.class) ;
		services_map.put("server_log", "한글");
		
		log.info("services_map:{}", services_map);
		log.info("server_log_file_error_string_map:{}", ReportUtil.server_log_file_error_string_map);
					
		ReportUtil.services_map = services_map;
		
		// 파일 생성
		FileUtils.writeStringToFile(new File("data/services_result.json"), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ReportUtil.services_map));
					
	}

}
