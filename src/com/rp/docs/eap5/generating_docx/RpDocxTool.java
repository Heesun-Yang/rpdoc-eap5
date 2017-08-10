package com.rp.docs.eap5.generating_docx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.jaxb.Context;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.utils.BufferUtil;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTHeight;
import org.docx4j.wml.CTShd;
import org.docx4j.wml.Color;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.FldChar;
import org.docx4j.wml.FooterReference;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STBrType;
import org.docx4j.wml.STFldCharType;
import org.docx4j.wml.STHeightRule;
import org.docx4j.wml.STHint;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.SectPr.PgMar;
import org.docx4j.wml.SectPr.PgSz;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.TcPrInner.GridSpan;
import org.docx4j.wml.TcPrInner.VMerge;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.docx4j.wml.TrPr;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ser.std.NumberSerializer;
import com.rp.docs.eap5.collecting_data.ReportUtil;

public class RpDocxTool {
	Logger log = LoggerFactory.getLogger(RpDocxTool.class);
	public WordprocessingMLPackage wordMLPackage;
	public MainDocumentPart documentPart;
	public ObjectFactory factory;
	public RPrTemplate rPrTemplate;
	public HashMap<String, Object> json_map = new HashMap<String, Object>();
	public String DocxFileName;
	public String InspectionDate;
	public String logo_path;
	
	public void setPageSize() {
		// 용지설정
		SectPr sectPr = factory.createSectPr();    
		 
		PgMar pgMr = new PgMar();
		pgMr.setTop(BigInteger.valueOf(1700));  // 3cm
		pgMr.setBottom(BigInteger.valueOf(1450));// 2.26cm
		pgMr.setLeft(BigInteger.valueOf(1450));
		pgMr.setRight(BigInteger.valueOf(1450));
		 
		sectPr.setPgMar(pgMr);     
		 
		PgSz pgSz = factory.createSectPrPgSz();
		
		
		// A4 size 
		// https://github.com/plutext/docx4j/blob/master/src/main/java/org/docx4j/model/structure/PageDimensions.java#L285
		pgSz.setCode(BigInteger.valueOf(9));
		pgSz.setW(BigInteger.valueOf(11907));
		pgSz.setH(BigInteger.valueOf(16839));
		
		sectPr.setPgSz(pgSz);
		 
		documentPart.getJaxbElement().getBody().setSectPr(sectPr);
		
	}
	
	// 최초 요약 페이지
	public void addOverviewPage() throws Exception {
		log.info("Progressing: {}", "addOverviewPage");
		
		this.addParagraph( "유지보수 점검 확인서", this.rPrTemplate.titleRPr, false, JcEnumeration.CENTER);

		//점검일자 테이블
		this.createInspectDateTable();
		
		this.addParagraph("-", this.rPrTemplate.smallRPr, true, JcEnumeration.CENTER);// table 분리용
		
		//documentPart.addStyledParagraphOfText("Heading1", "기본사항");
		
		// 고객사 테이블
		this.createCustomerInfoTable();
		
		//this.addParagraph(" ", this.rPrTemplate.smallRPr, true, JcEnumeration.CENTER);// table 분리용
		
		documentPart.addStyledParagraphOfText("Heading1", "유지 보수 대상 장비 LIST");
		//this.addParagraph( "유지 보수 대상 장비 LIST", this.rPrTemplate.subTitleRPr, false, JcEnumeration.LEFT);
		
		//표 생성
		this.createServerListTable();
		
		// 작업내용 table
		documentPart.addStyledParagraphOfText("Heading1", "작업내용");
		this.createTechSupportContentTable();
		
		documentPart.addStyledParagraphOfText("Heading1", "기타 지원 및 요청 사항");
		this.createMaintenanceRemarkTable();
		
		
				
	}
	
	//점검일자 테이블
	public void createInspectDateTable() throws Exception {
		
		// 테이블 시작
		Tbl table = factory.createTbl();
		addBorders(table, "2");
		Tr row = factory.createTr();
		
		
		addTableCellWidth(factory, wordMLPackage, row, "점검일자", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 15);
		addTableCellWidth(factory, wordMLPackage, row, this.InspectionDate , rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 35);
		addTableCellWidth(factory, wordMLPackage, row, "점검구분", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 15);
		addTableCellWidth(factory, wordMLPackage, row, "정기점검", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 35);
		
		table.getContent().add(row);
		
		setTableAlign(factory, table, JcEnumeration.CENTER);
		documentPart.addObject(table);
	}
	
	//고객사 테이블
	public void createCustomerInfoTable() throws Exception {
		
		// 테이블 시작
		Tbl table = factory.createTbl();
		addBorders(table, "2");
		
		// Table Row 시작
		Tr row1 = factory.createTr();
		LinkedHashMap<String, Object> customer_map = (LinkedHashMap<String, Object>) json_map.get("customer");
		LinkedHashMap<String, Object> engineer_map = (LinkedHashMap<String, Object>) json_map.get("engineer");
		
		addTableCellWidth(factory, wordMLPackage, row1, "고객사", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10);
		addTableCellWidth(factory, wordMLPackage, row1, customer_map.get("name")+"" , rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, row1, "점검업체", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10);
		addTableCellWidth(factory, wordMLPackage, row1, "락플레이스", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, row1, "담당엔지니어", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 15);
		addTableCellWidth(factory, wordMLPackage, row1, engineer_map.get("name")+"", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 25);
		
		table.getContent().add(row1);
		
		
		
		// Table Row 시작
		Tr row2 = factory.createTr();
		
		addTableCellWidth(factory, wordMLPackage, row2, "장소", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10);
		addTableCellWidth(factory, wordMLPackage, row2, customer_map.get("inspection_place") + "" , rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, row2, "점검부분", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10);
		addTableCellWidth(factory, wordMLPackage, row2, "JBoss", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, row2, "연락처", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 15);
		addTableCellWidth(factory, wordMLPackage, row2, engineer_map.get("tel_no")+"", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 25);
		
		table.getContent().add(row2);
				
		setTableAlign(factory, table, JcEnumeration.CENTER);
		documentPart.addObject(table);
	}
	
	// 유지보수 대상장비 테이블
	public void createServerListTable() throws Exception {

		// 서비스 리스트
		List<HashMap<String,Object>> serviceList = new ArrayList<HashMap<String,Object>>();
		
		ArrayList<Object> services = (ArrayList<Object>) json_map.get("services");
		//System.out.println("services.size():" + services.size());
		
		
		
		// 테이블 시작
		Tbl table = factory.createTbl();
		addBorders(table, "2");
		
		// Table Header Row 시작
		Tr row1 = factory.createTr();
		
		addTableCellWidth(factory, wordMLPackage, row1, "서비스", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 20);
		addTableCellWidth(factory, wordMLPackage, row1, "Host명", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 20);
		addTableCellWidth(factory, wordMLPackage, row1, "IP", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10);
		addTableCellWidth(factory, wordMLPackage, row1, "OS종류", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10);
		addTableCellWidth(factory, wordMLPackage, row1, "JBoss 버전", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 20);
		addTableCellWidth(factory, wordMLPackage, row1, "인스턴스 개수", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 20);
		addTableCellWidth(factory, wordMLPackage, row1, "비고", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 20);
		
		table.getContent().add(row1);
				
		
		for (Object objService : services){
			LinkedHashMap<String, Object> service = (LinkedHashMap<String, Object>) objService;
			
			// 서비스별 호스트 전체 데이터를 가지고 있는 변수
			ArrayList<Object> hosts = (ArrayList<Object>) service.get("jboss_hosts");
			int host_cnt = 0;
			for (Object objHost : hosts){
				//rowSpan 체크를 위한 변수
				host_cnt++;
				String rowSpan = "";
				
				//host정보
				LinkedHashMap<String, Object> host = (LinkedHashMap<String, Object>) objHost;
				ArrayList<Object> instances = (ArrayList<Object>) host.get("instances");
				//System.out.println("  host:" + host.get("ip"));
				//첫번째 인스턴스
				LinkedHashMap<String, Object> instance = (LinkedHashMap<String, Object>)instances.get(0);
				
				Tr row = factory.createTr();
				
				//JBoss Version
				StringBuffer JBossVersion = new StringBuffer();
				LinkedHashMap<String, String> JBossVersionMap = new LinkedHashMap<String, String>();
				JBossVersionMap = (LinkedHashMap<String, String>) ((LinkedHashMap<String, Object>)instance.get("result")).get("jboss_version");
				JBossVersion.append( JBossVersionMap.get("VersionName")).append(JBossVersionMap.get("VersionNumber"));
									
				if ( host_cnt == 1 ) {
					rowSpan = "restart";
				} else if (host_cnt < hosts.size()){
					rowSpan = "";
				} else {
					rowSpan = "close";
				}
				addTableCellWidth(factory, wordMLPackage, row, service.get("name") + "", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 10, 0, rowSpan);
				addTableCellWidth(factory, wordMLPackage, row, host.get("hostname") + "", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
				addTableCellWidth(factory, wordMLPackage, row, host.get("ip") + "" , rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
				addTableCellWidth(factory, wordMLPackage, row, host.get("operating_system") + "", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
				addTableCellWidth(factory, wordMLPackage, row, JBossVersion.toString(), rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 10);
				addTableCellWidth(factory, wordMLPackage, row, instances.size() + "", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
				addTableCellWidth(factory, wordMLPackage, row, "-", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 15);
				
				table.getContent().add(row);
				
			}
			
		}
			
		setTableAlign(factory, table, JcEnumeration.CENTER);
		documentPart.addObject(table);
	}
	
	public void createTechSupportContentTable() throws Exception {

		// 테이블 시작
		Tbl table = factory.createTbl();
		addBorders(table, "2");
		
		// Table Header Row 시작
		Tr row = factory.createTr();
		
		//Row Height
		TrPr trPr = new TrPr();
		CTHeight ctHeight = new CTHeight();
		ctHeight.setHRule(STHeightRule.EXACT);
		ctHeight.setVal(BigInteger.valueOf( 2000)); //6500
		JAXBElement<CTHeight> jaxbElement = factory.createCTTrPrBaseTrHeight(ctHeight);
		trPr.getCnfStyleOrDivIdOrGridBefore().add(jaxbElement);
		row.setTrPr(trPr);
		
		//System.out.println("height:"+wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getPgSz().getH());
		
		List<Object> contentList = new ArrayList<Object>();
		
		contentList.add("1. 월 정기점검");
		contentList.add(factory.createBr());
		contentList.add("2. 점검 내용 : 뒷면 장비별 점검일지 참조");
		contentList.add(factory.createBr());
		contentList.add("3. 점검 이력 : 월 정기점검 특이 사항 추가");
		contentList.add(factory.createBr());
		
		addTableCellWidth(factory, wordMLPackage, row, contentList, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 100);
		
		table.getContent().add(row);
		
		setTableAlign(factory, table, JcEnumeration.CENTER);
		documentPart.addObject(table);
		
	}
	
	public void createMaintenanceRemarkTable() throws Exception {

		// 테이블 시작
		Tbl table = factory.createTbl();
		addBorders(table, "2");
		
		// Table Header Row 시작
		Tr row = factory.createTr();
		
		//Row Height
		TrPr trPr = new TrPr();
		CTHeight ctHeight = new CTHeight();
		ctHeight.setHRule(STHeightRule.EXACT);
		ctHeight.setVal(BigInteger.valueOf( 3000));
		JAXBElement<CTHeight> jaxbElement = factory.createCTTrPrBaseTrHeight(ctHeight);
		trPr.getCnfStyleOrDivIdOrGridBefore().add(jaxbElement);
		row.setTrPr(trPr);
		
		//System.out.println("height:"+wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getPgSz().getH());
		
		List<Object> contentList = new ArrayList<Object>();
		/*
		contentList.add("1. 월 정기점검");
		contentList.add(factory.createBr());
		contentList.add("2. 점검 내용 : 뒷면 장비별 점검일지 참조");
		contentList.add(factory.createBr());
		contentList.add("3. 점검 이력 : 월 정기점검 특이 사항 추가");
		contentList.add(factory.createBr());
		*/
		addTableCellWidth(factory, wordMLPackage, row, contentList, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 100);
		
		table.getContent().add(row);
		
		setTableAlign(factory, table, JcEnumeration.CENTER);
		documentPart.addObject(table);
	
	}
	
	// 정기점검확인서
	public void addInspectionResult() throws Exception {
		log.info("Progressing: {}", "addInspectionResult");
		ArrayList<Object> services = (ArrayList<Object>) json_map.get("services");
		//System.out.println("services.size():" + services.size());
		log.debug("===================================");
		log.debug("services count: " + services.size());
		log.debug("===================================");
		
		for (Object objService : services){
			LinkedHashMap<String, Object> service = (LinkedHashMap<String, Object>) objService;
			log.info("------------------------------------");
			log.info("service name: {}", service.get("name"));
			log.info("------------------------------------");
			
			// 서비스별 호스트 전체 데이터를 가지고 있는 변수
			ArrayList<Object> hosts = (ArrayList<Object>) service.get("jboss_hosts");
			
			for (Object objHost : hosts){
				LinkedHashMap<String, Object> host = (LinkedHashMap<String, Object>) objHost;
				ArrayList<Object> instances = (ArrayList<Object>) host.get("instances");
				log.info("  host: {}", host.get("ip"));
				
				for (Object objInstance : instances){
					//Page 나눔
					this.addPageBreak();
					
					LinkedHashMap<String, Object> instance = (LinkedHashMap<String, Object>) objInstance;
					log.info("    instance: {}", instance.get("instance_name"));
					//tableInstanceList.add(instance.get("instance_name").toString());
					
					HashMap<String, Object> instance_result_map = new HashMap<String, Object>();
					
					//결과 데이터 Map
					LinkedHashMap<String, Object> result = (LinkedHashMap<String, Object>)instance.get("result");
					
					log.debug("engine_disk_usage:{}",getCliResultValue(result, "disk_usage", "engine_disk_usage"));
					
					instance_result_map.put("service_name", service.get("name")+"");
					instance_result_map.put("hostname", host.get("hostname")+"");
					instance_result_map.put("instance_name", instance.get("instance_name")+"");
					instance_result_map.put("engine_disk_usage", getCliResultValue(result, "disk_usage", "engine_disk_usage"));
					instance_result_map.put("log_disk_usage", getCliResultValue(result, "disk_usage", "log_disk_usage"));
					
					//JBoss Version
					instance_result_map.put("jboss_version", 
												getCliResultValue(result, "jboss_version", "VersionName") 
												+ getCliResultValue(result, "jboss_version", "VersionNumber"));
					
					// Java Version
					instance_result_map.put("java_version", getCliResultValue(result, "java_version", "JavaVersion"));
					
					// ip Address
					instance_result_map.put("instance_ip", host.get("ip") + "");
					/*
					// input-arguments
					 * 
					 */
					ArrayList<Object> input_arguments_list = getCliResultArrayList(result, "", "input_arguments");
					instance_result_map.put("input_arguments_list", input_arguments_list);
					
					//System properties
					LinkedHashMap<String, Object> system_properties_map = getCliResultMap(result, "system_properties");
					instance_result_map.put("system_properties_map", system_properties_map);
					
					//Deployments
					ArrayList<Object> deployments_list = getCliResultArrayList(result, "", "deployments");
					instance_result_map.put("deployments_list", deployments_list);
					log.debug("deployments_list:{}", deployments_list);
					
					List<Object> admin_console_list = new ArrayList<Object>();
					
					for (Object deployment : deployments_list){
						HashMap<String, Object> deployment_map = (HashMap<String, Object>)deployment;
						if (deployment_map.containsKey("jboss.web.deployment:war=/admin-console")){
							admin_console_list.add((String)deployment_map.get("jboss.web.deployment:war=/admin-console"));
							admin_console_list.add(factory.createBr());
						}
						if (deployment_map.containsKey("jboss.web.deployment:war=/jmx-console")){
							admin_console_list.add((String)deployment_map.get("jboss.web.deployment:war=/jmx-console"));
							admin_console_list.add(factory.createBr());
						}
						if (deployment_map.containsKey("jboss.web.deployment:war=/web-console")){
							admin_console_list.add((String)deployment_map.get("jboss.web.deployment:war=/web-console"));
							admin_console_list.add(factory.createBr());
						}
					}
					instance_result_map.put("admin_console_list", admin_console_list);
					
					//memory
					// 현재사용율
					/*
					LinkedHashMap<String, Object> heap_memory_usage_map = getCliResultMap(result, "java_version");
					instance_result_map.put("heap_memory_usage_map", heap_memory_usage_map);
					LinkedHashMap<String, Object> non_heap_memory_usage_map = getCliResultMap((LinkedHashMap<String, Object>)instance.get("result"), "core-service.platform-mbean.type.memory.non-heap-memory-usage");
					instance_result_map.put("non_heap_memory_usage_map", non_heap_memory_usage_map);
					*/
					
					// Memory Pool - GC 종류에 따라 다름
					LinkedHashMap<String, Object> memory_pool_map = getCliResultMap(result, "java_memory_pools");
					String heap_old_memory_pool_path = "";
					String perm_memory_pool_path = "";
					if (memory_pool_map.containsKey("PSOldGen")) {  // +UseParallelGC, +UseParallelOldGC
						//System.out.println("     Memory Pool:PS_Old_Gen");
						heap_old_memory_pool_path = "java_memory_pools.PSOldGen.peak_usage";
						perm_memory_pool_path = "java_memory_pools.PSPermGen.peak_usage";
					} else if (memory_pool_map.containsKey("CMSOldGen")) {  // +UseConcMarkSweepGC
						//System.out.println("     Memory CMS_Old_Gen");
						heap_old_memory_pool_path = "java_memory_pools.CMSOldGen.peak_usage";
						perm_memory_pool_path = "java_memory_pools.CMSPermGen.peak_usage";
					} else if (memory_pool_map.containsKey("G1OldGen")) {  // +UseG1GC
						//System.out.println("     Memory G1_Old_Gen");
						heap_old_memory_pool_path = "java_memory_pools.G1OldGen.peak_usage";
						perm_memory_pool_path = "java_memory_pools.G1PermGen.peak_usage";
					} else {
						log.error("Invalid memory pool name with GC Type. Check GC Type. ");
						log.error("memory_pool_map: {}", memory_pool_map);
						throw new RuntimeException();
					}
					
					log.debug("heap_old_memory_pool_path:" + heap_old_memory_pool_path);
					log.debug("perm_memory_pool_path:" + perm_memory_pool_path);
					
					// 피크 사용율 - Heap Old
					LinkedHashMap<String, Object> heap_old_memory_peak_usage_map = getCliResultMap(result, heap_old_memory_pool_path);
					instance_result_map.put("heap_old_memory_peak_usage_map", heap_old_memory_peak_usage_map);
					
					log.debug("heap_old_memory_peak_usage_map:" + heap_old_memory_peak_usage_map);
					
					
					// 피크 사용율 - Perm
					LinkedHashMap<String, Object> perm_memory_peak_usage_map = getCliResultMap(result, perm_memory_pool_path);
					instance_result_map.put("perm_memory_peak_usage_map", perm_memory_peak_usage_map);
					
					//Datasources
					LinkedHashMap<String, Object> datasources = getCliResultMap(result, "datasources");
					instance_result_map.put("datasources", datasources);
					
					//Connectors
					LinkedHashMap<String, Object> connectors = getCliResultMap(result, "thread_pools");
					instance_result_map.put("connectors", connectors);
					
					//server_log_inspection
					LinkedHashMap<String, Object> server_log_file_inspection = getCliResultMap(result, "server_log_file_inspection");
					instance_result_map.put("server_log_file_inspection", server_log_file_inspection);
					
					//gc_log_inspection
					LinkedHashMap<String, Object> gc_log_file_inspection = getCliResultMap(result, "gc_log_file_inspection");
					instance_result_map.put("gc_log_file_inspection", gc_log_file_inspection);
					
					// 정기점검결과서 title
					documentPart.addStyledParagraphOfText("Heading1", "정기점검확인서 (" + instance.get("instance_name") + ")");
					
					// Instance 정보 테이블
					this.createInstanceResultTable(instance_result_map);
					
					
					
				}
				
				// Hostname만 row에 담기
				//tableHostList.add(host.get("hostname").toString() + "(" + host.get("ip") + ")");
				//tableHostList.add(factory.createBr());
			}
			
		}
		
		
		
	}
	
	
	
	
	//Instance정보 테이블
	public void createInstanceResultTable(HashMap<String, Object> map) throws Exception {
		// Rowspan을 위한 변수
		int RowspanCnt = 0;
		String RestartFlag = "";
		String CloseFlag = "";
		
		String service_name = map.get("service_name") + "";
		String hostname = map.get("hostname") + "";
		String instance_name = map.get("instance_name") + "";
		String jboss_version = map.get("jboss_version") + "";
		String java_version = map.get("java_version") + "";
		String instance_ip = map.get("instance_ip") + "";
		
		// 인스턴스 요약 테이블 시작
		Tbl table1 = factory.createTbl();
		addBorders(table1, "2");
		
		Tr table1_row1 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table1_row1, "Service", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 17);
		addTableCellWidth(factory, wordMLPackage, table1_row1, "Hostname" , rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 17);
		addTableCellWidth(factory, wordMLPackage, table1_row1, "인스턴스명", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 17);
		addTableCellWidth(factory, wordMLPackage, table1_row1, "JBoss Ver.", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, "C6D9F1", 17);
		addTableCellWidth(factory, wordMLPackage, table1_row1, "JAVA  Ver.", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, "C6D9F1", 16);
		addTableCellWidth(factory, wordMLPackage, table1_row1, "IP Adress", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, "C6D9F1", 16);
		
		table1.getContent().add(table1_row1);
		
		Tr table1_row2 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table1_row2, service_name, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 15);
		addTableCellWidth(factory, wordMLPackage, table1_row2, hostname, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 35);
		addTableCellWidth(factory, wordMLPackage, table1_row2, instance_name, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 15);
		addTableCellWidth(factory, wordMLPackage, table1_row2, jboss_version, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 35);
		addTableCellWidth(factory, wordMLPackage, table1_row2, java_version, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 35);
		addTableCellWidth(factory, wordMLPackage, table1_row2, instance_ip, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 35);
		
		table1.getContent().add(table1_row2);
		
		setTableAlign(factory, table1, JcEnumeration.CENTER);
		documentPart.addObject(table1);
		
		this.addParagraph("-", this.rPrTemplate.smallRPr, true, JcEnumeration.CENTER);// table 분리용
		
		// 인스턴스 경로 테이블 시작
		Tbl table2 = factory.createTbl();
		addBorders(table2, "2");
		
		Tr table2_row1 = factory.createTr();
		LinkedHashMap<String, Object> system_properties_map = (LinkedHashMap<String, Object>)map.get("system_properties_map");  // 시스템 프로퍼티
		addTableCellWidth(factory, wordMLPackage, table2_row1, "JBOSS HOME", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 20);
		addTableCellWidth(factory, wordMLPackage, table2_row1, (String)system_properties_map.get("jboss.home.dir"), rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 90);
		table2.getContent().add(table2_row1);
		Tr table2_row2 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table2_row2, "SERVER HOME", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 20);
		addTableCellWidth(factory, wordMLPackage, table2_row2, (String)system_properties_map.get("jboss.server.base.dir"), rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 90);
		table2.getContent().add(table2_row2);
		Tr table2_row3 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table2_row3, "LOG HOME", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 20);
		addTableCellWidth(factory, wordMLPackage, table2_row3, (String)system_properties_map.get("jboss.server.log.dir"), rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 90);
		table2.getContent().add(table2_row3);
		Tr table2_row4 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table2_row4, "Port Offset", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 20);
		addTableCellWidth(factory, wordMLPackage, table2_row4, (String)system_properties_map.get("jboss.service.binding.set"), rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 90);
		table2.getContent().add(table2_row4);
		
		setTableAlign(factory, table2, JcEnumeration.CENTER);
		documentPart.addObject(table2);
		
		this.addParagraph("-", this.rPrTemplate.smallRPr, true, JcEnumeration.CENTER);// table 분리용
		
		// 점검 세부 항목
		Tbl table3 = factory.createTbl();
		addBorders(table3, "2");
		
		Tr table3_row1 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table3_row1, "점검세부항목", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 100, 6, null);
		
		table3.getContent().add(table3_row1);
		
		Tr table3_row2 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table3_row2, "항목", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10);
		addTableCellWidth(factory, wordMLPackage, table3_row2, "세부항목", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 20);
		addTableCellWidth(factory, wordMLPackage, table3_row2, "설명", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 20);
		addTableCellWidth(factory, wordMLPackage, table3_row2, "기준값", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 20);
		addTableCellWidth(factory, wordMLPackage, table3_row2, "점검결과값", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 20);
		addTableCellWidth(factory, wordMLPackage, table3_row2, "점검결과", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 20);
		table3.getContent().add(table3_row2);
		
		// 엔진 디스크사용율
		int engine_disk_usage = Integer.parseInt((map.get("engine_disk_usage")+"").replace("%", ""));
		String engine_disk_usage_result ="정상";
		if (engine_disk_usage > 80){
			engine_disk_usage_result = "비정상";
		} else if(engine_disk_usage == 0){ // 실제값을 확인하지 못한 경우 0 으로 세팅됨
			engine_disk_usage_result = "확인필요";
		}
		
		Tr table3_row3 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table3_row3, "OS", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "restart");
		addTableCellWidth(factory, wordMLPackage, table3_row3, "Engine Disk Usage", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row3, "JBoss Engine Disk 사용량(Use%)", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row3, "80% 이하", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row3, map.get("engine_disk_usage")+"", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row3, engine_disk_usage_result, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		table3.getContent().add(table3_row3);
		
		// 로그 디스크사용율
		int log_disk_usage = Integer.parseInt((map.get("log_disk_usage")+"").replace("%", ""));
		String log_disk_usage_result ="정상";
		if (log_disk_usage > 80){
			log_disk_usage_result = "비정상";
		} else if(log_disk_usage == 0){  // 실제값을 확인하지 못한 경우 0 으로 세팅됨
			log_disk_usage_result = "확인필요";
		}
		Tr table3_row3_1 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table3_row3_1, "OS", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "close");
		addTableCellWidth(factory, wordMLPackage, table3_row3_1, "Log Disk Usage", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row3_1, "JBoss Log Disk 사용량(Use%)", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row3_1, "80% 이하", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row3_1, map.get("log_disk_usage")+"", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row3_1, log_disk_usage_result, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		table3.getContent().add(table3_row3_1);
		
		// 사용중인 관리콘솔 사용목록 - Deploy된 관리콘솔의 목록 표시
		List<Object> admin_console_list = (ArrayList<Object>)map.get("admin_console_list");
		
		Tr table3_row4 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table3_row4, "Console", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10);
		addTableCellWidth(factory, wordMLPackage, table3_row4, "Management Console", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row4, "관리콘솔", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row4, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row4, admin_console_list, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row4, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		table3.getContent().add(table3_row4);
		
		
		// 메모리 설정값
		ArrayList<Object> input_arguments_list = (ArrayList<Object>)map.get("input_arguments_list");
		
		String heap_memory_max = "";
		String perm_memory_max = "";
		// Max Perm/Heap Size : java option에서 추출
		for (Object obj : input_arguments_list){
			if (obj instanceof String){
				if (((String) obj).startsWith("-Xmx")){
					heap_memory_max = ((String) obj).substring("-Xmx".length());
				}
				if (((String) obj).startsWith("-XX:MaxPermSize")){
					perm_memory_max = ((String) obj).substring("-XX:MaxPermSize".length()+1);
				}
			}
		}
		
		//memory
		// 현재사용율
		LinkedHashMap<String, Object> non_heap_memory_usage_map = (LinkedHashMap<String, Object>) map.get("non_heap_memory_usage_map");
		LinkedHashMap<String, Object> heap_memory_usage_map = (LinkedHashMap<String, Object>) map.get("heap_memory_usage_map");
		//System.out.println("heap_memory_usage_map:"+ heap_memory_usage_map);
		//float non_heap_memory_usage_max = Float.parseFloat( non_heap_memory_usage_map.get("max")+"") / 1024 / 1024;
		//float heap_memory_usage_max = Float.parseFloat( heap_memory_usage_map.get("max")+"") / 1024 / 1024; 
		// 피크 사용율 old, perm
		LinkedHashMap<String, Object> heap_old_memory_peak_usage_map = (LinkedHashMap<String, Object>) map.get("heap_old_memory_peak_usage_map");
		LinkedHashMap<String, Object> perm_memory_peak_usage_map = (LinkedHashMap<String, Object>) map.get("perm_memory_peak_usage_map");
		//항목별 점검결과
		String item_memory_usage_rate_result = "N/A";
		//System.out.println("                heap_old_memory_peak_usage_map:"+ heap_old_memory_peak_usage_map);
		log.debug("                perm_memory_peak_usage_map:{}", perm_memory_peak_usage_map);
		
				
		
		Tr table3_row5 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table3_row5, "Perm Memory", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "restart");
		addTableCellWidth(factory, wordMLPackage, table3_row5, "Perm Memory Max", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row5, "Perm Memory Max 설정 값", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row5, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row5, perm_memory_max, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row5, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		table3.getContent().add(table3_row5);
		
		
		Tr table3_row5_1 = factory.createTr();
		float perm_memory_peak_usage_percent = Float.parseFloat( perm_memory_peak_usage_map.get("used")+"") / Float.parseFloat(perm_memory_peak_usage_map.get("max") +"") * 100;
		if (perm_memory_peak_usage_percent <= 90){ item_memory_usage_rate_result = "정상"; } else { item_memory_usage_rate_result = "확인필요"; }
		addTableCellWidth(factory, wordMLPackage, table3_row5_1, " ", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "close");
		addTableCellWidth(factory, wordMLPackage, table3_row5_1, "Perm Memory Peak Usage", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row5_1, "Perm Memory 최대사용율", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row5_1, "95% 이하", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row5_1, String.format("%.2f", perm_memory_peak_usage_percent) + "%", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row5_1, item_memory_usage_rate_result, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		table3.getContent().add(table3_row5_1);
		
		Tr table3_row6 = factory.createTr();
		float heap_old_memory_peak_usage_percent = Float.parseFloat(heap_old_memory_peak_usage_map.get("used")+"") / Float.parseFloat(heap_old_memory_peak_usage_map.get("max")+"") * 100;
		if (heap_old_memory_peak_usage_percent <= 90){ item_memory_usage_rate_result = "정상"; } else { item_memory_usage_rate_result = "확인필요"; }
		addTableCellWidth(factory, wordMLPackage, table3_row6, "Heap Memory", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "restart");
		addTableCellWidth(factory, wordMLPackage, table3_row6, "Heap Memory Max", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row6, "Heap Memory Max 설정 값", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row6, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row6, heap_memory_max, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row6, item_memory_usage_rate_result, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		table3.getContent().add(table3_row6);
		
		Tr table3_row6_1 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table3_row6_1, "", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "close");
		addTableCellWidth(factory, wordMLPackage, table3_row6_1, "Heap Old Memory Peak Usage", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row6_1, "Heap Old Memory 최대사용율", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row6_1, "95% 이하", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row6_1, String.format("%.2f", heap_old_memory_peak_usage_percent) + "%", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row6_1, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		table3.getContent().add(table3_row6_1);
		
		//Datasource 개수만큼 루프
		
		LinkedHashMap<String, Object> datasources_map = (LinkedHashMap<String, Object>)map.get("datasources");
		
		// Default Datasource를 표시하지 않는 경우
		if ("no".equalsIgnoreCase(ReportUtil.display_default_datasource)) {
			datasources_map.remove("DefaultDS");
			datasources_map.remove("JmsXA");
		}
		// 초기화
		RowspanCnt = 0;
		
		for (String datasource : datasources_map.keySet()){
			//System.out.println("      datasource:" + datasource);
			LinkedHashMap<String, Object> datasource_map = (LinkedHashMap<String, Object>)datasources_map.get(datasource);
			//log.info("datasource_map:" + datasource_map);
			
			
			// 첫째 라인이면 Rowspan 시작
			if (RowspanCnt == 0) {
				RestartFlag = "restart";
			} else {
				RestartFlag = "";
			}
			
			
			//JNDI Name =============
			// Datasource 의 첫번째 라인이므로 색깔을 진하게 변경( E7EAF0 )
			Tr table3_row7_0 = factory.createTr();
			addTableCellWidth(factory, wordMLPackage, table3_row7_0, "Datasources", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, RestartFlag);
			addTableCellWidth(factory, wordMLPackage, table3_row7_0, "JNDI Name", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, "E7EAF0", 20);
			addTableCellWidth(factory, wordMLPackage, table3_row7_0, "Data Source Name", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, "E7EAF0", 20);
			addTableCellWidth(factory, wordMLPackage, table3_row7_0, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, "E7EAF0", 20);
			addTableCellWidth(factory, wordMLPackage, table3_row7_0, datasource, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, "E7EAF0", 20);
			//점검결과컬럼
			addTableCellWidth(factory, wordMLPackage, table3_row7_0, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, "E7EAF0", 20);
			
			table3.getContent().add(table3_row7_0);
			
			
			
			// item별 점검결과
			String itemInspectionResult = ""; 
					
			float InUseCount = Float.parseFloat((String)datasource_map.get("InUseConnectionCount"));  // 통계가 활성화 되지 않은 경우 무조건 "0"
			float MaxUsedCount = Float.parseFloat((String)datasource_map.get("MaxConnectionsInUseCount"));
			float max_pool_size = Float.parseFloat((datasource_map.get("MaxSize") != null) ? (String)datasource_map.get("MaxSize") : "-1");
			
			//Connection pool Max 설정 값=============
			Tr table3_row7_2 = factory.createTr();
			addTableCellWidth(factory, wordMLPackage, table3_row7_2, "", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "");
			addTableCellWidth(factory, wordMLPackage, table3_row7_2, "Max Pool Size", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row7_2, "Connection pool Max 설정 값", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row7_2, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row7_2, datasource_map.get("MaxSize") + "", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			//점검결과컬럼
			if ( max_pool_size == -1 ){
				itemInspectionResult = "미설정";
			} else {
				itemInspectionResult = "정상";
			}
			addTableCellWidth(factory, wordMLPackage, table3_row7_2, itemInspectionResult, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			
			table3.getContent().add(table3_row7_2);
			
			//현재 사용되는 Connection 개수=============
			Tr table3_row8 = factory.createTr();
			addTableCellWidth(factory, wordMLPackage, table3_row8, "", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "");
			addTableCellWidth(factory, wordMLPackage, table3_row8, "In Use Connection Count", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row8, "현재 사용되는 Connection 개수", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row8, "Max Pool Size 미만", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row8, datasource_map.get("InUseConnectionCount")+"", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			//점검결과컬럼
			//System.out.println("        statistics-enabled:" + (boolean)statictics_pool_map.get("statistics-enabled"));
			if (max_pool_size > 0  ){
				if (InUseCount < max_pool_size){
					itemInspectionResult = "정상";
				} else {
					itemInspectionResult = "Max값 도달";
				}
			} else {
				itemInspectionResult = "Max값 설정필요";
			}
			
			addTableCellWidth(factory, wordMLPackage, table3_row8, itemInspectionResult, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			table3.getContent().add(table3_row8);
			
			//현재까지 사용된 최대 Connection 개수=============
			Tr table3_row9 = factory.createTr();
			addTableCellWidth(factory, wordMLPackage, table3_row9, "", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "");
			addTableCellWidth(factory, wordMLPackage, table3_row9, "Max Connections In Use Count", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row9, "현재까지 사용된 최대 Connection 개수", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row9, "Max Pool Size 미만", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row9, datasource_map.get("MaxConnectionsInUseCount")+"", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			//점검결과컬럼
			if (max_pool_size > 0 ){
				if (MaxUsedCount < max_pool_size * 0.9){  //90%
					itemInspectionResult = "정상";
				} else {
					itemInspectionResult = "90%이상 사용";
				}
			} else {
				itemInspectionResult = "Max값 설정필요";
			}
			addTableCellWidth(factory, wordMLPackage, table3_row9, itemInspectionResult, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			table3.getContent().add(table3_row9);
			
			//최대 connection 사용률(%)=============
			Tr table3_row10 = factory.createTr();
			
			int maxUsedRate = 0;
			String maxUsedRateString = "";
			//점검결과컬럼
			if (max_pool_size  > 0 ){
				maxUsedRate = (int) (MaxUsedCount / max_pool_size * 100);
				maxUsedRateString = maxUsedRate + "%";
				if (maxUsedRate < 90){  //90%
					itemInspectionResult = "정상";
				} else {
					itemInspectionResult = "90%이상 사용";
				}
			} else {
				itemInspectionResult = "Max값 설정필요";
				maxUsedRateString = "N/A";
			}
			// 마지막라인이면 Rowspan 종료
			if (RowspanCnt == datasources_map.size() - 1) {
				CloseFlag = "close";
			} else {
				CloseFlag = "";
			}
			
			
			addTableCellWidth(factory, wordMLPackage, table3_row10, "", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, CloseFlag);
			addTableCellWidth(factory, wordMLPackage, table3_row10, "Max In Use / Max Pool Size", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row10, "최대 connection 사용률(%)", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row10, "90% 이하", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row10, maxUsedRateString, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row10, itemInspectionResult, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			table3.getContent().add(table3_row10);
			
			RowspanCnt++;
		}
		
		// Connector
		//connector 개수만큼 루프
		LinkedHashMap<String, Object> connectors_map = (LinkedHashMap<String, Object>)map.get("connectors");
		
		// 초기화
		RowspanCnt = 0;
		
		for (String connector_name : connectors_map.keySet()){
			
			//System.out.println("      connector_name:" + connector_name);
			LinkedHashMap<String, Object> connector_map = (LinkedHashMap<String, Object>)connectors_map.get(connector_name);
			//LinkedHashMap<String, Object> statictics_pool_map = getCliResultMap(connector_map, "statistics.pool");
			
			log.debug("      connector_name:" + connector_name);
			log.debug("      connector_map:" + connector_map);
			
			String item_thread_inspection_result = "정상";
			
			// 첫째 라인이면 Rowspan 시작
			if (RowspanCnt == 0) {
				RestartFlag = "restart";
			} else {
				RestartFlag = "";
			}
			
			// Connector Name
			Tr table3_row12 = factory.createTr();
			addTableCellWidth(factory, wordMLPackage, table3_row12, "Connectors", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, RestartFlag);
			addTableCellWidth(factory, wordMLPackage, table3_row12, "Name", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, "E7EAF0", 20);
			addTableCellWidth(factory, wordMLPackage, table3_row12, "Connector 명", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, "E7EAF0", 20);
			addTableCellWidth(factory, wordMLPackage, table3_row12, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, "E7EAF0", 20);
			addTableCellWidth(factory, wordMLPackage, table3_row12, connector_name, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, "E7EAF0", 20);
			addTableCellWidth(factory, wordMLPackage, table3_row12, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, "E7EAF0", 20);
			table3.getContent().add(table3_row12);
						
			// max-threads
			Tr table3_row15 = factory.createTr();
			addTableCellWidth(factory, wordMLPackage, table3_row15, "", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "");
			addTableCellWidth(factory, wordMLPackage, table3_row15, "Max Threads", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15, "동시실행 가능한 최대 쓰레드 수", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15, connector_map.get("maxThreads") + "", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			table3.getContent().add(table3_row15);
			
			// current-thread-count
			Tr table3_row15_1 = factory.createTr();
			addTableCellWidth(factory, wordMLPackage, table3_row15_1, "", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "");
			addTableCellWidth(factory, wordMLPackage, table3_row15_1, "Current Thread Count", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15_1, "현재 생성된 쓰레드 수", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15_1, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15_1, connector_map.get("currentThreadCount") + "", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15_1, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			table3.getContent().add(table3_row15_1);
			
			// active-count
			Tr table3_row15_2 = factory.createTr();
			addTableCellWidth(factory, wordMLPackage, table3_row15_2, "", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "");
			addTableCellWidth(factory, wordMLPackage, table3_row15_2, "Active Thread Count", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15_2, "현재 실행중인 쓰레드 수", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15_2, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15_2, connector_map.get("currentThreadsBusy") + "", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15_2, "N/A", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			table3.getContent().add(table3_row15_2);
			
			// largest-thread-count
			Tr table3_row15_4 = factory.createTr();
			float max_thread_usage_rate_percent = Float.parseFloat( connector_map.get("currentThreadCount")+"") / Float.parseFloat( connector_map.get("maxThreads")+"") * 100 ;
			if (max_thread_usage_rate_percent > 90){
				item_thread_inspection_result = "비정상";
			}
			
			// 마지막라인이면 Rowspan 종료
			if (RowspanCnt == datasources_map.size() - 1) {
				CloseFlag = "close";
			} else {
				CloseFlag = "";
			}
			
			addTableCellWidth(factory, wordMLPackage, table3_row15_4, "", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, CloseFlag);
			addTableCellWidth(factory, wordMLPackage, table3_row15_4, "Max Thread Usage Rate", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15_4, "최대 thread 사용률(%)", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15_4, "90% 이하", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15_4, String.format("%.2f", max_thread_usage_rate_percent) + "%", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row15_4, item_thread_inspection_result, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			table3.getContent().add(table3_row15_4);
			
			// rowspan
			RowspanCnt++;
			
		}
		
		// 초기화
		RowspanCnt = 0;
		RestartFlag = "";
		
		// server Log 검토결과
		LinkedHashMap<String, Object> server_log_file_inspection_map = (LinkedHashMap<String, Object>)map.get("server_log_file_inspection");
		
		
		//for (String server_log_file_error_string : server_log_file_inspection_map.keySet()){
		for (String server_log_file_error_string : ReportUtil.server_log_file_error_string_map.keySet()){
			// 첫째 라인이면 Rowspan 시작
			if (RowspanCnt == 0) {
				RestartFlag = "restart";
			} else if (RowspanCnt == ReportUtil.server_log_file_error_string_map.size() - 1){
				RestartFlag = "close";
			} else {
				RestartFlag = "";
			}
			// 에러에 대한 설명
			String ErrorStringDescription = ReportUtil.server_log_file_error_string_map.get(server_log_file_error_string);
			String ErrorOccurCount = "-";
			String InspectionResultNote = "-";
			
			// server 로그를 확인할 경우
			if ("yes".equalsIgnoreCase(ReportUtil.server_log_file_inspection)){
				
				ErrorOccurCount = server_log_file_inspection_map.get(server_log_file_error_string) + "";
				
				//결과가 숫자가 아니면 "-" 로 표시
				if (!NumberUtils.isDigits(ErrorOccurCount)){
					ErrorOccurCount = "-";
					InspectionResultNote = "-";
					log.error("\"{}\" is not number format.");
				} else {
					if (Integer.parseInt(ErrorOccurCount) > 0){
						InspectionResultNote = "확인필요";
					} else {
						InspectionResultNote = "정상";
					}
				}
			} 
			
			Tr table3_row20 = factory.createTr();
			
			addTableCellWidth(factory, wordMLPackage, table3_row20, "Server Log", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, RestartFlag);
			addTableCellWidth(factory, wordMLPackage, table3_row20, server_log_file_error_string, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row20, ErrorStringDescription, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row20, "1회이상 발생시 기록", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row20, ErrorOccurCount, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			addTableCellWidth(factory, wordMLPackage, table3_row20, InspectionResultNote, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
			table3.getContent().add(table3_row20);
			
			// Rowspan증가
			RowspanCnt++;
		}
		
		
		// GC Log
		LinkedHashMap<String, Object> gc_log_file_inspection_map = (LinkedHashMap<String, Object>)map.get("gc_log_file_inspection");
		String FullGcAvgInterval = "-";
		String MaxGcDuration = "-";
		
		int intFullGcAvgInterval = 0;
		int intMaxGcDuration = 0;
		
		String FullGcAvgIntervalResultNote = "-";
		String MaxGcDurationResultNote = "-";
		
		// GC 로그를 확인할 경우
		if ("yes".equalsIgnoreCase(ReportUtil.gc_log_file_inspection)){
			FullGcAvgInterval = gc_log_file_inspection_map.get("FullGcAvgInterval") + "";
			MaxGcDuration = gc_log_file_inspection_map.get("MaxGcDuration") + "";
			
			// Full GC 평균간격
			if (NumberUtils.isParsable(FullGcAvgInterval)){
				float floatFullGcAvgInterval = Float.parseFloat(FullGcAvgInterval);
				if (floatFullGcAvgInterval >= 3000){
					FullGcAvgIntervalResultNote = "정상";
				} else if (floatFullGcAvgInterval == -1){
					FullGcAvgIntervalResultNote = "발생않함";
				} else {
					FullGcAvgIntervalResultNote = "확인필요";
				}
			} else {
				log.error("Invalid Number Format data for FullGcAvgInterval: {}", FullGcAvgInterval);
			}
			
			// Full GC 최대 수행시간
			if (NumberUtils.isParsable(MaxGcDuration)){
				float floatMaxGcDuration = Float.parseFloat(MaxGcDuration);
				if (floatMaxGcDuration > 0 && floatMaxGcDuration <= 5){
					MaxGcDurationResultNote = "정상";
				} else if (floatMaxGcDuration == 0){
					MaxGcDurationResultNote = "발생않함";
				} else {
					MaxGcDurationResultNote = "확인필요";
				}
			} else {
				log.error("Invalid Number Format data for MaxGcDuration: {}", MaxGcDuration);
			}
			
		}
		
		
		Tr table3_row21 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table3_row21, "GC Log", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "restart");
		addTableCellWidth(factory, wordMLPackage, table3_row21, "Full GC Average Interval ", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row21, "FULL GC 평균 발생 간격", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row21, "3,000초 이상", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row21, FullGcAvgInterval, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row21, FullGcAvgIntervalResultNote, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		table3.getContent().add(table3_row21);
		
		Tr table3_row21_1 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table3_row21_1, "", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "");
		addTableCellWidth(factory, wordMLPackage, table3_row21_1, "Max Full GC Duration", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row21_1, "최대 Full GC 소요 시간 ", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row21_1, "5초 이하", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row21_1, MaxGcDuration, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		addTableCellWidth(factory, wordMLPackage, table3_row21_1, MaxGcDurationResultNote, rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20);
		table3.getContent().add(table3_row21_1);
		
		// GC 로그를 확인할 경우
		if ("yes".equalsIgnoreCase(ReportUtil.gc_log_file_inspection)){
			// GC Duration chart
			Tr table3_row21_2 = factory.createTr();
			addTableCellWidth(factory, wordMLPackage, table3_row21_2, "", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "");
			addTableCellImageWidth(factory, wordMLPackage, table3_row21_2, "data/" + instance_name + "-GCDuration.png", rPrTemplate.tableContentRPr, 		JcEnumeration.CENTER, true, null, 20, 5, null);
			table3.getContent().add(table3_row21_2);
			
			// GC HeapMemory chart
			Tr table3_row21_3 = factory.createTr();
			addTableCellWidth(factory, wordMLPackage, table3_row21_3, "", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "close");
			addTableCellImageWidth(factory, wordMLPackage, table3_row21_3, "data/" + instance_name + "-HeapMemory.png", rPrTemplate.tableContentRPr, 		JcEnumeration.CENTER, true, null, 20, 5, null);
			table3.getContent().add(table3_row21_3);
			
		} else {
			Tr table3_row21_2 = factory.createTr();
			addTableCellWidth(factory, wordMLPackage, table3_row21_2, "", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10, 0, "close");
			addTableCellWidth(factory, wordMLPackage, table3_row21_2, "-", rPrTemplate.tableContentRPr, 		JcEnumeration.CENTER, true, null, 20, 5, null);
			table3.getContent().add(table3_row21_2);
			
		}
		
		// 특이사항
		Tr table3_row22 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table3_row22, "특이사항", rPrTemplate.tableHeaderRPr, JcEnumeration.CENTER, true, "C6D9F1", 10);
		addTableCellWidth(factory, wordMLPackage, table3_row22, "-", rPrTemplate.tableContentRPr, JcEnumeration.CENTER, true, null, 20, 5, null);
		
		table3.getContent().add(table3_row22);
		
		// 점검결과
		Tr table3_row23 = factory.createTr();
		addTableCellWidth(factory, wordMLPackage, table3_row23, "점검결과", rPrTemplate.tableHeaderRPr, 	JcEnumeration.CENTER, true, "C6D9F1", 10);
		addTableCellWidth(factory, wordMLPackage, table3_row23, "-", rPrTemplate.tableContentRPr, 		JcEnumeration.CENTER, true, null, 20, 5, null);
		table3.getContent().add(table3_row23);
		
		
		setTableAlign(factory, table3, JcEnumeration.CENTER);
		documentPart.addObject(table3);
		
		
	}
	
	// Cli Result Navigation
	// 사용예 String data = jsonTest.getCliResultValue((LinkedHashMap<String, Object>)instance.get("result"), "core-service.platform-mbean.type.runtime", "vm-name");
	public String getCliResultValue(LinkedHashMap<String, Object> map, String path, String property){
		// path  core-service=platform-mbean/type=runtime
		String[] paths = path.split("\\.");
		LinkedHashMap<String, Object> lmap = map;
		
		for (String name : paths){
			//System.out.println("      name:" + name);
			
			lmap = (LinkedHashMap<String, Object>)lmap.get(name);
			//System.out.println("      lmap:" + lmap);
			
		}
		//System.out.println("      property:" + lmap.get(property));
		return (String)lmap.get(property);
	}
	
	// Cli 결과가 LinkedHashMap 인 경우 (subsystem.datasources.data-source)
	public LinkedHashMap<String, Object> getCliResultMap(LinkedHashMap<String, Object> map, String path){
		// path  core-service=platform-mbean/type=runtime
		String[] paths = path.split("\\.");
		LinkedHashMap<String, Object> lmap = map;
		
		try {
			for (String name : paths){
				//System.out.println("      name:" + name);
				lmap = (LinkedHashMap<String, Object>)lmap.get(name);
				//System.out.println("      lmap:" + lmap);
				
			}
		} catch (NullPointerException e) {
			System.out.println("Check path:\"" + path + "\"");
			e.printStackTrace();
		}
		
		return lmap;
		
	}
	// Cli 결과가 ArrayList 인 경우 (core-service.platform-mbean.type.runtime.input-arguments)
	public ArrayList<Object> getCliResultArrayList(LinkedHashMap<String, Object> map, String path, String property){
		// path  core-service=platform-mbean/type=runtime
		String[] paths = path.split("\\.");
		LinkedHashMap<String, Object> lmap = map;
		
		if (path.length() > 0) {
			for (String name : paths){
				//System.out.println("      name:" + name);
				
				lmap = (LinkedHashMap<String, Object>)lmap.get(name);
				//System.out.println("      lmap:" + lmap);
				
			}
		}
		
		
		return (ArrayList<Object>)lmap.get(property);
		
	}
		
	
	public void addParagraph(	String paragraphText,
									RPr rpr,
									boolean removeSpace,
									JcEnumeration ParagraphAlign
									) throws Exception {
		
		P paragraph = factory.createP();
		setParagraphAlign(factory, paragraph, ParagraphAlign);
		Text txt = factory.createText();
		txt.setValue(paragraphText);
		R run = factory.createR();
		run.getContent().add(txt);
		run.setRPr(rpr);
		paragraph.getContent().add(run);
		
		/*P paragraph = factory.createP();
		setParagraphAlign(paragraph, JcEnumeration.CENTER);
		
		Text text = factory.createText();
		text.setValue(paragraphText);
		R run = factory.createR();
		run.getContent().add(text);
		run.setRPr(rpr);
		paragraph.getContent().add(run);
		
		
		*/
		// 단락 설정 (Paragraph Property) - 단락 끝 공간 제거
		if (removeSpace){
			PPr ppr = factory.createPPr();
			PPrBase.Spacing pprbasespacing = factory.createPPrBaseSpacing(); 
			pprbasespacing.setAfter( BigInteger.valueOf( 0) ); 
			ppr.setSpacing(pprbasespacing); 
			paragraph.setPPr(ppr);
		}
		
		documentPart.addObject(paragraph);
	
	}
	
	
	public void createWordprocessingMLPackage() throws Exception {
		this.wordMLPackage = WordprocessingMLPackage.createPackage();
		this.documentPart = wordMLPackage.getMainDocumentPart();
		this.factory = Context.getWmlObjectFactory();
		this.rPrTemplate = new RPrTemplate(factory);
		
	}
	public void loadWordprocessingMLPackage() throws Exception {
		String inputfilepath = "/media/hsyang/DATA-P3/Temp/docx/template.docx";
		this.wordMLPackage = WordprocessingMLPackage.load(new java.io.File(inputfilepath));
		this.documentPart = wordMLPackage.getMainDocumentPart();
		this.factory = Context.getWmlObjectFactory();
		this.rPrTemplate = new RPrTemplate(factory); // predefined fonts
		
		
		//return WordprocessingMLPackage.load(new java.io.File(inputfilepath));
	}


	public void saveWordPackage(WordprocessingMLPackage wordPackage, File file)
			throws Exception {
		wordPackage.save(file);
	}

	// 페이지
	public void addPageBreak() {
		MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		Br breakObj = new Br();
		breakObj.setType(STBrType.PAGE);
		P paragraph = factory.createP();
		paragraph.getContent().add(breakObj);
		documentPart.addObject(paragraph);
	}

	
	// 표 증가 경계
	public void addBorders(Tbl table, String borderSize) {
		table.setTblPr(new TblPr());
		CTBorder border = new CTBorder();
		border.setColor("auto");
		border.setSz(new BigInteger(borderSize));
		border.setSpace(new BigInteger("0"));
		border.setVal(STBorder.SINGLE);
		TblBorders borders = new TblBorders();
		borders.setBottom(border);
		borders.setLeft(border);
		borders.setRight(border);
		borders.setTop(border);
		borders.setInsideH(border);
		borders.setInsideV(border);
		table.getTblPr().setTblBorders(borders);
	}

	// 수평 정렬 방식
	// TODO 수직 정렬 안 쓰다
	public void setParagraphAlign(ObjectFactory factory, P p, JcEnumeration jcEnumeration) {
		PPr pPr = p.getPPr();
		if (pPr == null) {
			pPr = factory.createPPr();
		}
		Jc jc = pPr.getJc();
		if (jc == null) {
			jc = new Jc();
		}
		jc.setVal(jcEnumeration);
		pPr.setJc(jc);
		p.setPPr(pPr);
	}
	public void setParagraphAlign(P p, JcEnumeration jcEnumeration) {
		PPr pPr = p.getPPr();
		if (pPr == null) {
			pPr = factory.createPPr();
		}
		Jc jc = pPr.getJc();
		if (jc == null) {
			jc = new Jc();
		}
		jc.setVal(jcEnumeration);
		pPr.setJc(jc);
		p.setPPr(pPr);
	}
	// 표 수평 정렬 방식
	// TODO 수직 정렬 안 쓰다
	public void setTableAlign(ObjectFactory factory, Tbl table, JcEnumeration jcEnumeration) {
		TblPr tablePr = table.getTblPr();
		if (tablePr == null) {
			tablePr = factory.createTblPr();
		}
		Jc jc = tablePr.getJc();
		if (jc == null) {
			jc = new Jc();
		}
		jc.setVal(jcEnumeration);
		tablePr.setJc(jc);
		table.setTblPr(tablePr);
	}

	// 새로운 셀- width
	public void addTableCellWidth(ObjectFactory factory,
								WordprocessingMLPackage wordMLPackage, 
								Tr tableRow, 
								String content,
								RPr rpr, 
								JcEnumeration jcEnumeration, 
								boolean hasBgColor,
								String backgroudColor,
								int cellWidthPercentage
								) {
		
		// Table Cell
		Tc tableCell = factory.createTc();
		
		//Paragraph
		P p = factory.createP();
		setParagraphAlign(factory, p, jcEnumeration);
		//text.setSpace("preserve");
		
		// 표 글꼴 설정 스타일
		R run = factory.createR();
		run.setRPr(rpr);
		
		// text 추가
		Text text = factory.createText();
		text.setValue(content);
		run.getContent().add(text);
		p.getContent().add(run);
		
		tableCell.getContent().add(p);
		
		// 단락 설정 (Paragraph Property) - 단락 끝 공간 제거
		PPr ppr = factory.createPPr();
		PPrBase.Spacing pprbasespacing = factory.createPPrBaseSpacing(); 
		pprbasespacing.setAfter( BigInteger.valueOf( 0) ); 
		ppr.setSpacing(pprbasespacing); 
		p.setPPr(ppr);
		
		
		// 배경색이 있으면
		if (hasBgColor) {
			TcPr tcPr = tableCell.getTcPr();
			if (tcPr == null) {
				tcPr = factory.createTcPr();
			}
			CTShd shd = tcPr.getShd();
			if (shd == null) {
				shd = factory.createCTShd();
			}
			shd.setColor("auto");
			shd.setFill(backgroudColor);
			
			tcPr.setShd(shd);
			tableCell.setTcPr(tcPr);
			
		}
		
		// Table width 비율이 있는 경우
		if (cellWidthPercentage > 0) {
			TcPr tcPr = tableCell.getTcPr();
			if (tcPr == null) {
				tcPr = factory.createTcPr();
			}
			
			TblWidth tblWidth = factory.createTblWidth();
			// 비율에 따른 실제 width 계산
			int cellWidth = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips() * cellWidthPercentage / 100;
			tcPr.setTcW(tblWidth);
			tblWidth.setType("dxa");
			tblWidth.setW(BigInteger.valueOf(cellWidth));
		}
		
		// row에 cell 삽입
		tableRow.getContent().add(tableCell);
	}
	// 새로운 셀- width, Colspan, rowspan
	public void addTableCellWidth(ObjectFactory factory,
								WordprocessingMLPackage wordMLPackage, 
								Tr tableRow, 
								String content,
								RPr rpr, 
								JcEnumeration jcEnumeration, 
								boolean hasBgColor,
								String backgroudColor,
								int cellWidthPercentage,
								int colSpan,
								String rowSpan
								) {
		
		// Table Cell
		Tc tableCell = factory.createTc();
		
		//Paragraph
		P p = factory.createP();
		setParagraphAlign(factory, p, jcEnumeration);
		//text.setSpace("preserve");
		
		// 표 글꼴 설정 스타일
		R run = factory.createR();
		
		run.setRPr(rpr);
		
		// text 추가
		Text text = factory.createText();
		text.setValue(content);
		run.getContent().add(text);
		p.getContent().add(run);
		
		tableCell.getContent().add(p);
		
		// 단락 설정 (Paragraph Property) - 단락 끝 공간 제거
		PPr ppr = factory.createPPr();
		PPrBase.Spacing pprbasespacing = factory.createPPrBaseSpacing(); 
		pprbasespacing.setAfter( BigInteger.valueOf( 0) ); 
		ppr.setSpacing(pprbasespacing); 
		p.setPPr(ppr);
		
		
		// 배경색이 있으면
		if (hasBgColor) {
			TcPr tcPr = tableCell.getTcPr();
			if (tcPr == null) {
				tcPr = factory.createTcPr();
			}
			CTShd shd = tcPr.getShd();
			if (shd == null) {
				shd = factory.createCTShd();
			}
			shd.setColor("auto");
			shd.setFill(backgroudColor);
			
			tcPr.setShd(shd);
			tableCell.setTcPr(tcPr);
			
		}
		
		// Table width 비율이 있는 경우
		if (cellWidthPercentage > 0) {
			TcPr tcPr = tableCell.getTcPr();
			if (tcPr == null) {
				tcPr = factory.createTcPr();
			}
			
			TblWidth tblWidth = factory.createTblWidth();
			// 비율에 따른 실제 width 계산
			int cellWidth = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips() * cellWidthPercentage / 100;
			tcPr.setTcW(tblWidth);
			tblWidth.setType("dxa");
			tblWidth.setW(BigInteger.valueOf(cellWidth));
		}
		
		// colSpan처리
		if (colSpan > 0){
			//System.out.println("colSpan:" + colSpan);
			GridSpan gridSpan = new GridSpan();
			gridSpan.setVal(new BigInteger(colSpan + ""));
			TcPr tcPr = tableCell.getTcPr();
			if (tcPr == null) {
					tcPr = factory.createTcPr();
	        }
			tcPr.setGridSpan(gridSpan);
		}
		
		// rowSpan처리
		if (rowSpan != null){
			TcPr tcpr = tableCell.getTcPr(); 
			if (tcpr == null) { 
				tcpr = new TcPr(); 
				tableCell.setTcPr(tcpr); 
			} 
			VMerge merge = new VMerge(); 
			if (!"close".equals(rowSpan)) { 
				merge.setVal(rowSpan); 
			} 
			tcpr.setVMerge(merge); 
			 
		}
		// row에 cell 삽입
		tableRow.getContent().add(tableCell);
	}
	// 새로운 셀- width (출력할 내용을 List에 담아서 출력한다. Br 출력 용도)
	public void addTableCellWidth(ObjectFactory factory,
								WordprocessingMLPackage wordMLPackage, 
								Tr tableRow, 
								List<Object> contentList,
								RPr rpr, 
								JcEnumeration jcEnumeration, 
								boolean hasBgColor,
								String backgroudColor,
								int cellWidthPercentage
								) {
		
		// Table Cell
		Tc tableCell = factory.createTc();
		
		//Paragraph
		P p = factory.createP();
		setParagraphAlign(factory, p, jcEnumeration);
		//text.setSpace("preserve");
		
		// 표 글꼴 설정 스타일
		R run = factory.createR();
		run.setRPr(rpr);
		
		// List 끝 Br 제거
		if (contentList.size() > 0){
			if (contentList.get(contentList.size() - 1).getClass().getName() == "org.docx4j.wml.Br"){ 
				contentList.remove(contentList.size() - 1);
			}
		}
		
		for (Object obj : contentList){
			log.debug("obj.getClass().getName():"+ obj.getClass().getName());
			
			if (obj.getClass().getName() == "java.lang.String"){
				String content = (String)obj;
				// text 추가
				Text text = factory.createText();
				text.setValue(content);
				run.getContent().add(text);
				
				log.debug("content:"+ content);
				
			} else if (obj.getClass().getName() == "org.docx4j.wml.Br"){  // Br 추가
				run.getContent().add((Br)obj);
				log.debug("createBr()");
			}
			
		}
		
		p.getContent().add(run);
		tableCell.getContent().add(p);
		
		// 단락 설정 (Paragraph Property) - 단락 끝 공간 제거
		PPr ppr = factory.createPPr();
		PPrBase.Spacing pprbasespacing = factory.createPPrBaseSpacing(); 
		pprbasespacing.setAfter( BigInteger.valueOf( 0) ); 
		ppr.setSpacing(pprbasespacing); 
		p.setPPr(ppr);
		
		
		// 배경색이 있으면
		if (hasBgColor) {
			TcPr tcPr = tableCell.getTcPr();
			if (tcPr == null) {
				tcPr = factory.createTcPr();
			}
			CTShd shd = tcPr.getShd();
			if (shd == null) {
				shd = factory.createCTShd();
			}
			shd.setColor("auto");
			shd.setFill(backgroudColor);
			
			tcPr.setShd(shd);
			tableCell.setTcPr(tcPr);
			
		}
		
		// Table width 비율이 있는 경우
		if (cellWidthPercentage > 0) {
			TcPr tcPr = tableCell.getTcPr();
			if (tcPr == null) {
				tcPr = factory.createTcPr();
			}
			
			TblWidth tblWidth = factory.createTblWidth();
			// 비율에 따른 실제 width 계산
			int cellWidth = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips() * cellWidthPercentage / 100;
			tcPr.setTcW(tblWidth);
			tblWidth.setType("dxa");
			tblWidth.setW(BigInteger.valueOf(cellWidth));
		}
		
		// row에 cell 삽입
		tableRow.getContent().add(tableCell);
	}
	// 새로운 셀- width, Colspan, rowspan
	public void addTableCellImageWidth(ObjectFactory factory,
								WordprocessingMLPackage wordMLPackage, 
								Tr tableRow, 
								String ImgPath,
								RPr rpr, 
								JcEnumeration jcEnumeration, 
								boolean hasBgColor,
								String backgroudColor,
								int cellWidthPercentage,
								int colSpan,
								String rowSpan
								) {
		
		// Table Cell
		Tc tableCell = factory.createTc();
		
		//Paragraph
		P p = factory.createP();
		setParagraphAlign(factory, p, jcEnumeration);
		//text.setSpace("preserve");
		
		// 표 글꼴 설정 스타일
		R run = factory.createR();
		
		run.setRPr(rpr);
		/*
		// text 추가
		Text text = factory.createText();
		text.setValue(content);
		*/
		// Image 추가
		File file = new File(ImgPath);
		java.io.InputStream is = null;
		try {
			is = new java.io.FileInputStream(file);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			run.getContent().add(newImage(wordMLPackage, factory, documentPart,
					BufferUtil.getBytesFromInputStream(is), "filename",
					"이것은 꼬릿말", 1, 2, JcEnumeration.CENTER));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		p.getContent().add(run);
		
		tableCell.getContent().add(p);
		
		// 단락 설정 (Paragraph Property) - 단락 끝 공간 제거
		PPr ppr = factory.createPPr();
		PPrBase.Spacing pprbasespacing = factory.createPPrBaseSpacing(); 
		pprbasespacing.setAfter( BigInteger.valueOf( 0) ); 
		ppr.setSpacing(pprbasespacing); 
		p.setPPr(ppr);
		
		
		// 배경색이 있으면
		if (hasBgColor) {
			TcPr tcPr = tableCell.getTcPr();
			if (tcPr == null) {
				tcPr = factory.createTcPr();
			}
			CTShd shd = tcPr.getShd();
			if (shd == null) {
				shd = factory.createCTShd();
			}
			shd.setColor("auto");
			shd.setFill(backgroudColor);
			
			tcPr.setShd(shd);
			tableCell.setTcPr(tcPr);
			
		}
		
		// Table width 비율이 있는 경우
		if (cellWidthPercentage > 0) {
			TcPr tcPr = tableCell.getTcPr();
			if (tcPr == null) {
				tcPr = factory.createTcPr();
			}
			
			TblWidth tblWidth = factory.createTblWidth();
			// 비율에 따른 실제 width 계산
			int cellWidth = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips() * cellWidthPercentage / 100;
			tcPr.setTcW(tblWidth);
			tblWidth.setType("dxa");
			tblWidth.setW(BigInteger.valueOf(cellWidth));
		}
		
		// colSpan처리
		if (colSpan > 0){
			//System.out.println("colSpan:" + colSpan);
			GridSpan gridSpan = new GridSpan();
			gridSpan.setVal(new BigInteger(colSpan + ""));
			TcPr tcPr = tableCell.getTcPr();
			if (tcPr == null) {
					tcPr = factory.createTcPr();
	        }
			tcPr.setGridSpan(gridSpan);
		}
		
		// rowSpan처리
		if (rowSpan != null){
			TcPr tcpr = tableCell.getTcPr(); 
			if (tcpr == null) { 
				tcpr = new TcPr(); 
				tableCell.setTcPr(tcpr); 
			} 
			VMerge merge = new VMerge(); 
			if (!"close".equals(rowSpan)) { 
				merge.setVal(rowSpan); 
			} 
			tcpr.setVMerge(merge); 
			 
		}
		// row에 cell 삽입
		tableRow.getContent().add(tableCell);
	}
	// 새로운 셀 - image 넣기- width
	/*public void addTableCellImageWidth(ObjectFactory factory,
								WordprocessingMLPackage wordMLPackage, 
								Tr tableRow, 
								String content,
								RPr rpr, 
								JcEnumeration jcEnumeration, 
								boolean hasBgColor,
								String backgroudColor,
								int cellWidthPercentage
								) {
		
		// Table Cell
		Tc tableCell = factory.createTc();
		
		//Paragraph
		P p = factory.createP();
		setParagraphAlign(factory, p, jcEnumeration);
		//text.setSpace("preserve");
		
		// 표 글꼴 설정 스타일
		R run = factory.createR();
		run.setRPr(rpr);
		
		// text 추가
		Text text = factory.createText();
		text.setValue(content);
		
		// Image 추가
		File file = new File("data/chart.png");
		java.io.InputStream is = null;
		try {
			is = new java.io.FileInputStream(file);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			run.getContent().add(newImage(wordMLPackage, factory, documentPart,
					BufferUtil.getBytesFromInputStream(is), "filename",
					"이것은 꼬릿말", 1, 2, JcEnumeration.CENTER));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		p.getContent().add(run);
		
		tableCell.getContent().add(p);
		
		// 단락 설정 (Paragraph Property) - 단락 끝 공간 제거
		PPr ppr = factory.createPPr();
		PPrBase.Spacing pprbasespacing = factory.createPPrBaseSpacing(); 
		pprbasespacing.setAfter( BigInteger.valueOf( 0) ); 
		ppr.setSpacing(pprbasespacing); 
		p.setPPr(ppr);
		
		
		// 배경색이 있으면
		if (hasBgColor) {
			TcPr tcPr = tableCell.getTcPr();
			if (tcPr == null) {
				tcPr = factory.createTcPr();
			}
			CTShd shd = tcPr.getShd();
			if (shd == null) {
				shd = factory.createCTShd();
			}
			shd.setColor("auto");
			shd.setFill(backgroudColor);
			
			tcPr.setShd(shd);
			tableCell.setTcPr(tcPr);
			
		}
		
		// Table width 비율이 있는 경우
		if (cellWidthPercentage > 0) {
			TcPr tcPr = tableCell.getTcPr();
			if (tcPr == null) {
				tcPr = factory.createTcPr();
			}
			
			TblWidth tblWidth = factory.createTblWidth();
			// 비율에 따른 실제 width 계산
			int cellWidth = wordMLPackage.getDocumentModel().getSections().get(0).getPageDimensions().getWritableWidthTwips() * cellWidthPercentage / 100;
			tcPr.setTcW(tblWidth);
			tblWidth.setType("dxa");
			tblWidth.setW(BigInteger.valueOf(cellWidth));
		}
		
		// row에 cell 삽입
		tableRow.getContent().add(tableCell);
	}*/
	// 문자 페이지
	public Relationship createTextHeaderPart(
			String content,
			JcEnumeration jcEnumeration) throws Exception {
		HeaderPart headerPart = new HeaderPart();
		Relationship rel = documentPart.addTargetPart(headerPart);
		headerPart.setJaxbElement(getTextHdr(wordMLPackage, factory,
				headerPart, content, jcEnumeration));
		return rel;
	}

	// 문서 꼬릿말
	public Relationship createTextFooterPart(
			WordprocessingMLPackage wordprocessingMLPackage,
			MainDocumentPart documentPart, ObjectFactory factory, String content,
			JcEnumeration jcEnumeration) throws Exception {
		FooterPart footerPart = new FooterPart();
		Relationship rel = documentPart.addTargetPart(footerPart);
		footerPart.setJaxbElement(getTextFtr(wordprocessingMLPackage, factory,
				footerPart, content, jcEnumeration));
		return rel;
	}

	// 그림 머리글
	public Relationship createHeaderPart() throws Exception {
		HeaderPart headerPart = new HeaderPart();
		Relationship rel = documentPart.addTargetPart(headerPart);
		// After addTargetPart, so image can be added properly
		headerPart.setJaxbElement(getHdr(wordMLPackage, factory,
				headerPart));
		return rel;
	}

	// 그림 꼬릿말
	public Relationship createFooterPart(
			WordprocessingMLPackage wordprocessingMLPackage,
			MainDocumentPart documentPart, ObjectFactory factory) throws Exception {
		FooterPart footerPart = new FooterPart();
		Relationship rel = documentPart.addTargetPart(footerPart);
		footerPart.setJaxbElement(getFtr(wordprocessingMLPackage, factory,
				footerPart));
		return rel;
	}

	public Relationship createFooterPageNumPart() throws Exception {
		FooterPart footerPart = new FooterPart();
		footerPart.setPackage(wordMLPackage);
		footerPart.setJaxbElement(createFooterWithPageNr(factory));
		return documentPart.addTargetPart(footerPart);
	}

	public Ftr createFooterWithPageNr(ObjectFactory factory) {
		Ftr ftr = factory.createFtr();
		P paragraph = factory.createP();
		RPr fontRPr = this.rPrTemplate.regularRPr;
		R run = factory.createR();
		run.setRPr(fontRPr);
		paragraph.getContent().add(run);

		addPageTextField(factory, paragraph, "- "); // 제
		addFieldBegin(factory, paragraph);
		addPageNumberField(factory, paragraph);
		addFieldEnd(factory, paragraph);
		addPageTextField(factory, paragraph, " / ");

		addPageTextField(factory, paragraph, ""); // Total
		addFieldBegin(factory, paragraph);
		addTotalPageNumberField(factory, paragraph);
		addFieldEnd(factory, paragraph);
		addPageTextField(factory, paragraph, " -");  // page
		setParagraphAlign(factory, paragraph, JcEnumeration.CENTER);
		ftr.getContent().add(paragraph);
		return ftr;
	}

	public void addFieldBegin(ObjectFactory factory, P paragraph) {
		R run = factory.createR();
		FldChar fldchar = factory.createFldChar();
		fldchar.setFldCharType(STFldCharType.BEGIN);
		run.getContent().add(fldchar);
		paragraph.getContent().add(run);
	}

	public void addFieldEnd(ObjectFactory factory, P paragraph) {
		FldChar fldcharend = factory.createFldChar();
		fldcharend.setFldCharType(STFldCharType.END);
		R run3 = factory.createR();
		run3.getContent().add(fldcharend);
		paragraph.getContent().add(run3);
	}

	public void addPageNumberField(ObjectFactory factory, P paragraph) {
		R run = factory.createR();
		Text txt = new Text();
		txt.setSpace("preserve");
		txt.setValue("PAGE  \\* MERGEFORMAT ");
		run.getContent().add(factory.createRInstrText(txt));
		paragraph.getContent().add(run);
	}

	public void addTotalPageNumberField(ObjectFactory factory, P paragraph) {
		R run = factory.createR();
		Text txt = new Text();
		txt.setSpace("preserve");
		txt.setValue("NUMPAGES  \\* MERGEFORMAT ");
		run.getContent().add(factory.createRInstrText(txt));
		paragraph.getContent().add(run);
	}

	private void addPageTextField(ObjectFactory factory, P paragraph,
			String value) {
		R run = factory.createR();
		Text txt = new Text();
		txt.setSpace("preserve");
		txt.setValue(value);
		run.getContent().add(txt);
		paragraph.getContent().add(run);
	}

	public void createHeaderReference(Relationship relationship)
			throws InvalidFormatException {
		List<SectionWrapper> sections = wordMLPackage.getDocumentModel().getSections();
		SectPr sectPr = sections.get(sections.size() - 1).getSectPr();
		// There is always a section wrapper, but it might not contain a sectPr
		if (sectPr == null) {
			sectPr = factory.createSectPr();
			documentPart.addObject(sectPr);
			sections.get(sections.size() - 1).setSectPr(sectPr);
		}
		HeaderReference headerReference = factory.createHeaderReference();
		headerReference.setId(relationship.getId());
		headerReference.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(headerReference);
	}

	public void createFooterReference(Relationship relationship)
			throws InvalidFormatException {
		List<SectionWrapper> sections = wordMLPackage
				.getDocumentModel().getSections();
		SectPr sectPr = sections.get(sections.size() - 1).getSectPr();
		// There is always a section wrapper, but it might not contain a sectPr
		if (sectPr == null) {
			sectPr = factory.createSectPr();
			documentPart.addObject(sectPr);
			sections.get(sections.size() - 1).setSectPr(sectPr);
		}
		FooterReference footerReference = factory.createFooterReference();
		footerReference.setId(relationship.getId());
		footerReference.setType(HdrFtrRef.DEFAULT);
		sectPr.getEGHdrFtrReferences().add(footerReference);
	}

	public Hdr getTextHdr(WordprocessingMLPackage wordprocessingMLPackage,
			ObjectFactory factory, Part sourcePart, String content,
			JcEnumeration jcEnumeration) throws Exception {
		Hdr hdr = factory.createHdr();
		P headP = factory.createP();
		Text text = factory.createText();
		text.setValue(content);
		R run = factory.createR();
		run.getContent().add(text);
		headP.getContent().add(run);

		PPr pPr = headP.getPPr();
		if (pPr == null) {
			pPr = factory.createPPr();
		}
		Jc jc = pPr.getJc();
		if (jc == null) {
			jc = new Jc();
		}
		jc.setVal(jcEnumeration);
		pPr.setJc(jc);
		headP.setPPr(pPr);
		hdr.getContent().add(headP);
		return hdr;
	}

	public Ftr getTextFtr(WordprocessingMLPackage wordprocessingMLPackage,
			ObjectFactory factory, Part sourcePart, String content,
			JcEnumeration jcEnumeration) throws Exception {
		Ftr ftr = factory.createFtr();
		P footerP = factory.createP();
		Text text = factory.createText();
		text.setValue(content);
		R run = factory.createR();
		run.getContent().add(text);
		footerP.getContent().add(run);

		PPr pPr = footerP.getPPr();
		if (pPr == null) {
			pPr = factory.createPPr();
		}
		Jc jc = pPr.getJc();
		if (jc == null) {
			jc = new Jc();
		}
		jc.setVal(jcEnumeration);
		pPr.setJc(jc);
		footerP.setPPr(pPr);
		ftr.getContent().add(footerP);
		return ftr;
	}

	public Hdr getHdr(WordprocessingMLPackage wordprocessingMLPackage,
			ObjectFactory factory, Part sourcePart) throws Exception {
		Hdr hdr = factory.createHdr();
		File file = new File(this.logo_path);
		java.io.InputStream is = new java.io.FileInputStream(file);
		hdr.getContent().add(
				newImage(wordprocessingMLPackage, factory, sourcePart,
						BufferUtil.getBytesFromInputStream(is), "filename",
						"이것은 머릿말 부분", 1, 2, JcEnumeration.CENTER));
		return hdr;
	}

	public Ftr getFtr(WordprocessingMLPackage wordprocessingMLPackage,
			ObjectFactory factory, Part sourcePart) throws Exception {
		Ftr ftr = factory.createFtr();
		File file = new File(this.logo_path);
		java.io.InputStream is = new java.io.FileInputStream(file);
		ftr.getContent().add(
				newImage(wordprocessingMLPackage, factory, sourcePart,
						BufferUtil.getBytesFromInputStream(is), "filename",
						"이것은 꼬릿말", 1, 2, JcEnumeration.CENTER));
		return ftr;
	}

	public P newImage(WordprocessingMLPackage wordMLPackage,
			ObjectFactory factory, Part sourcePart, byte[] bytes,
			String filenameHint, String altText, int id1, int id2,
			JcEnumeration jcEnumeration) throws Exception {
		BinaryPartAbstractImage imagePart = BinaryPartAbstractImage
				.createImagePart(wordMLPackage, sourcePart, bytes);
		Inline inline = imagePart.createImageInline(filenameHint, altText, id1,
				id2, false);
		P p = factory.createP();
		R run = factory.createR();
		p.getContent().add(run);
		Drawing drawing = factory.createDrawing();
		run.getContent().add(drawing);
		drawing.getAnchorOrInline().add(inline);
		PPr pPr = p.getPPr();
		if (pPr == null) {
			pPr = factory.createPPr();
		}
		Jc jc = pPr.getJc();
		if (jc == null) {
			jc = new Jc();
		}
		jc.setVal(jcEnumeration);
		pPr.setJc(jc);
		p.setPPr(pPr);
		return p;
	}

}
