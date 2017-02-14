package com.rp.docs.eap5;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateResultEap5 {
	public static void main(String[] args) {
		Logger log = LoggerFactory.getLogger(CreateResultEap5.class);
		
		final String  ServiceFile = "data/services.json";
		
		// Load Properties (설치 및 패치 파일명)
		ReportUtil.loadProperties();
		
		// JDK6
		File file = new File(ServiceFile);
		byte[] JsonData;
		
		
		try {
			// services.json 파일을 읽어 services_map에 저장한다.
			JsonData = FileUtils.readFileToByteArray(file);
			
			// Jackson ObjectMapper
			ObjectMapper mapper = new ObjectMapper();
			
			// services.json 파일의 내용을 HashMap 으로 변환한다.
			HashMap<String, Object> services_map = mapper.readValue(JsonData, HashMap.class) ;
			
			// JschUtil 생성 
			JschUtil jschUtil = new JschUtil();
			
			// ReportUtil 초기화
			ReportUtil.jschUtil = jschUtil;
			ReportUtil.services_map = services_map;
			
			
			// Target Host에 디렉토리가 생성되어 있는지 확인
			ReportUtil.testServiceHosts();
			
			// Result 데이터 생성
			ReportUtil.getResultAllDataIntoJson();
			
			// 파일 생성
			FileUtils.writeStringToFile(new File("data/services_result.json"), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(ReportUtil.services_map));
			
			
			
				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
