package com.rp.docs.eap5.generating_docx;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.codehaus.jackson.map.ObjectMapper;
import org.docx4j.relationships.Relationship;
import org.docx4j.toc.TocGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.io.FileUtils;

import com.rp.docs.eap5.collecting_data.ReportUtil;

public class GenerateReportEap5 {
	public static void main(String[] args) {
		Logger log = LoggerFactory.getLogger(GenerateReportEap5.class);
		
		RpDocxTool rpDocxTool = new RpDocxTool();
		
		// Default json 파일명
		String  JsonDataFile = "data/services_result.json";
		if (args.length > 0) {
			if (args[0] != null) {
				log.debug("JsonDataFile:" + JsonDataFile);
				JsonDataFile = args[0];
			}
		}
		
		// 프로퍼티 로딩
		ReportUtil.loadProperties();
		
		// 헤더 및 Footer 로고 이미지
		rpDocxTool.logo_path = System.getProperty("user.dir") + File.separator + "data" + File.separator + "logo.png";
		
		//WordprocessingMLPackage wordMLPackage = rpDocxTool.createWordprocessingMLPackage();
		try {
			//rpDocxTool.loadWordprocessingMLPackage();
			rpDocxTool.createWordprocessingMLPackage();
			
			// 결과 Json데이터 읽기
			//JDK7
			//byte[]JsonData = Files.readAllBytes(Paths.get("/home/hsyang/programs/eclipse-luna/eclipse/workspace/rpdoc/data/test_result.json"));
			// JsonData = Files.readAllBytes(Paths.get(JsonDataFile)); 
			
			 // JDK6
			File file = new File(JsonDataFile);
			byte[] JsonData = FileUtils.readFileToByteArray(file);
			
        	HashMap<String, Object> map = new ObjectMapper().readValue(JsonData, HashMap.class) ;
			rpDocxTool.json_map = map;
			
			log.info("Parsing Json Data: {}", JsonDataFile);
			
			// 용지설정 - properties 파일로 대체
			//rpDocxTool.setPageSize();
			
			// 점검일자
			Date date = new Date();
			SimpleDateFormat sdformat  = new SimpleDateFormat("yyyy-M-dd");
			rpDocxTool.InspectionDate = sdformat.format(date).toString();
			
			//고객사명
			LinkedHashMap<String, Object> customer_map = (LinkedHashMap<String, Object>) rpDocxTool.json_map.get("customer");
			// 점검파일명을 고객사 명으로 지정
			rpDocxTool.DocxFileName = "JBoss-EAP-" + ((String) customer_map.get("name")).replaceAll(" ", "") + "-" + rpDocxTool.InspectionDate + ".docx";
			
			
			//머리글
			Relationship relationship =rpDocxTool.createHeaderPart();
			//Relationship relationship = rpDocxTool.createTextHeaderPart("Rockplace", JcEnumeration.CENTER);
			rpDocxTool.createHeaderReference(relationship);
			
			//Page 나눔
			rpDocxTool.addPageBreak();
					
			
			// 요약페이지		
			rpDocxTool.addOverviewPage();
			
			//Page 나눔
			//rpDocxTool.addPageBreak();
			
			//정기점검결과서(서버별)
			rpDocxTool.addInspectionResult();
			
			
			//Page 나눔
			//rpDocxTool.addPageBreak();
			
			//꼬릿말
			relationship = rpDocxTool.createFooterPageNumPart();
			rpDocxTool.createFooterReference(relationship);
			
			//목차
			TocGenerator tocGenerator = new TocGenerator(rpDocxTool.wordMLPackage);
			//tocGenerator.generateToc( 0,    "TOC \\h \\z \\t \"comh1,1,comh2,2,comh3,3,comh4,4\" ", true);
	       //tocGenerator.generateToc( 0,    "TOC \\o \"1-3\" \\h \\z \\u ", true);
	       tocGenerator.generateToc(0, "TOC \\o \"1-3\" \\h \\z \\u ", true);
			       
	       rpDocxTool.saveWordPackage(rpDocxTool.wordMLPackage, new File("docx/" + rpDocxTool.DocxFileName));
	       
	       log.info("Generating Document is completed");
	       log.info("==============================================");
	       log.info("Location: {}", "docx/" + rpDocxTool.DocxFileName);
	       log.info("==============================================");
	       
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
