package com.rp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rp.docs.eap5.JschUtil;
import com.rp.docs.eap5.ReportUtil;


public class ReportGeneratorEAP5 {
	public static void main(String[] args) {
		Logger log = LoggerFactory.getLogger(ReportGeneratorEAP5.class);
		
		final String  ServiceFile = "data/services.json";
		
		// Load Properties (설치 및 패치 파일명)
		ReportUtil.loadProperties();
		
		// JDK6
		File file = new File(ServiceFile);
		byte[] JsonData;
		
		
		try {
			// services.json 파일을 읽어 services_map에 저장한다.
			JsonData = FileUtils.readFileToByteArray(file);
			HashMap<String, Object> services_map = new ObjectMapper().readValue(JsonData, HashMap.class) ;
			
			
			// host_default_values 정보
			LinkedHashMap<String, Object> host_default_values = (LinkedHashMap<String, Object>) services_map.get("host_default_values");
			
			// instance_default_values 정보
			LinkedHashMap<String, Object> instance_default_values = (LinkedHashMap<String, Object>) services_map.get("instance_default_values");
						
						
			// Template 호스트에 접속하여 템플릿 생성
			// Template 호스트 접속
			JschUtil jschUtil = new JschUtil();
			ReportUtil.jschUtil = jschUtil;
			ReportUtil.services_map = services_map;
			
			
			// Target Host에 디렉토리가 생성되어 있는지 확인
			ReportUtil.testServiceHosts();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
			
}
