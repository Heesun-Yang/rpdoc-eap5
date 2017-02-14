package com.rp.docs.eap5;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JPanel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.jfree.chart.ChartUtilities;
import org.jfree.ui.RefineryUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReportUtil {
	static Logger log = LoggerFactory.getLogger(ReportUtil.class);
	
	// ssh 사용
	public static JschUtil jschUtil;
	
	// install.properties 파일
	public static String jdk_file_name;
	
	//// Report 생성 관련 ////////////////////////////
	// instance에서 bin 디렉토리를 별도로 만들었을 경우 "yes"
	public static String display_default_datasource;
	
	// Server 로그파일 검사여부
	public static String server_log_file_inspection;
	
	// 확인할 Server로그파일
	public static String server_log_file_date_expr;
	
	// 확인할 Server로그파일의 최대크기 (총합계)
	public static long server_log_file_max_total_size;
	
	// 확인할 Server 로그내용
	public static HashMap<String, String> server_log_file_error_string_map = new HashMap<String, String>() ;
	
	// Server 로그파일 검사여부
	public static String gc_log_file_inspection;
	
	// GC Log 디렉토리
	public static String gc_log_dir;
	
	// 확인할 Server로그파일
	public static String gc_log_file_date_expr;
	
	// 확인할 Server로그파일의 최대크기 (총합계)
	public static long gc_log_file_max_total_size;
	
	// GC 종류
	public static String GCType;
	
	// Full GC 간격
	public static int full_gc_interval;
		
	// services.json 파일의 내용이 담길 map
	public static HashMap<String, Object> services_map;
		
	
	// 설정값 로딩
	public static void loadProperties() {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			String config_file_path = System.getProperty("user.dir") + File.separator + "config" + File.separator + "properties";

			input = new FileInputStream(config_file_path);

			// load a properties file- 한글 처리를 위해 InputStreamReader를 사용한다.
			//prop.load(input);
			prop.load(new InputStreamReader(input, Charset.forName("UTF-8")));
			
			//// get the property value
			
			// JBoss의 기본 Datasource도 리포트에 표시할지 여부
			display_default_datasource = prop.getProperty("display_default_datasource");
			
			// 로그파일 검사여부
			server_log_file_inspection = prop.getProperty("server_log_file_inspection");
			
			if ("yes".equalsIgnoreCase(server_log_file_inspection)){
				// 검사할 로그파일의 파일명 형태 (정규식)
				server_log_file_date_expr = prop.getProperty("server_log_file_date_expr");
				
				// 검사할 로그파일의 최대 사이즈
				if (NumberUtils.isDigits(prop.getProperty("server_log_file_max_total_size"))){
					server_log_file_max_total_size = Long.parseLong(prop.getProperty("server_log_file_max_total_size"));
				} else {
					log.warn("Property 'server_log_file_max_total_size' is not number");
					log.warn("Property 'server_log_file_max_total_size' will be set to 104857600 (100MB) automatically");
					server_log_file_max_total_size = 104857600;
				}
				
				// 로그파일 내 검사할 문자열
				String server_log_file_error_string = "";
				
				for(int i = 0; (server_log_file_error_string = prop.getProperty("server_log_file_error_string." + i)) != null; i++) {
					// 에러문자열만 추출 (OutOfMemoryError:Heap memory 부족)
					String[] server_log_file_error_string_arr = StringUtils.split(server_log_file_error_string, ":");
					if (server_log_file_error_string_arr.length == 2){
						server_log_file_error_string_map.put(server_log_file_error_string_arr[0], server_log_file_error_string_arr[1]);
					} else {
						
					}
					
			    }
			}
			
			// GC 로그파일 검사여부
			gc_log_file_inspection = prop.getProperty("gc_log_file_inspection");
			
			if ("yes".equalsIgnoreCase(gc_log_file_inspection)){
				// gc log 디렉토리
				gc_log_dir = prop.getProperty("gc_log_dir");
				
				// 검사할 로그파일의 파일명 형태 (정규식)
				gc_log_file_date_expr = prop.getProperty("gc_log_file_date_expr");
				
				// 검사할 로그파일의 최대 사이즈
				if (NumberUtils.isDigits(prop.getProperty("gc_log_file_max_total_size"))){
					gc_log_file_max_total_size = Long.parseLong(prop.getProperty("gc_log_file_max_total_size"));
				} else {
					log.warn("Property 'gc_log_file_max_total_size' is not number");
					log.warn("Property 'gc_og_file_max_total_size' will be set to 104857600 (100MB) automatically");
					gc_log_file_max_total_size = 104857600;
				}
				
			}
			
			// get the property value and print it out
			log.info("Load Properties:{}", "config/properties");
			log.debug("Properties:{}", prop);
			

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// map의 값을 new_map 값으로 대체한다.
	public static void replaceMapEntry(LinkedHashMap<String, Object> map, LinkedHashMap<String, Object> new_map) {
		for (Map.Entry<String, Object> e :new_map.entrySet()){
			
			//map에 key가 없거나 new_map 에 key에 대한 값이 있으면 대체
			if (!"".equals(e.getValue()) || !map.containsKey(e.getKey())){
				map.put(e.getKey(), e.getValue());
			}
			
		}
	}
	
	// test host, 디렉토리, ssh 접속테스트
	public static void testServiceHosts() {
		log.info("------------------------------------");
		log.info("Testing Target Hosts...");
		log.info("------------------------------------");
		
		String command = "";
		
		//// service 목록
		ArrayList<Object> services = (ArrayList<Object>) services_map.get("services");
		
		for (Object objService : services){
			
			LinkedHashMap<String, Object> service = (LinkedHashMap<String, Object>) objService;
			log.info("------------------------------------");
			log.info("service name: {}", service.get("name"));
			log.info("------------------------------------");
			
			// host_default_values 정보
			LinkedHashMap<String, Object> host_default_values = (LinkedHashMap<String, Object>) services_map.get("host_default_values");
					
			// host 기본값과 개별 호스트 정보가 합쳐질 map
			LinkedHashMap<String, Object> unified_map = new LinkedHashMap<String, Object>();
			
			// host Default값 설정
			unified_map.putAll(host_default_values);
			
			// 인스턴스 Default 값 설정
			// instance_default_values맵에 설정값이 있으면 updated_instance_map 의 값을 대체한다. instance_default_values 설정값 우선적용
			replaceMapEntry(unified_map, (LinkedHashMap<String, Object> )services_map.get("instance_default_values"));
			
			
			//// 호스트 목록 
			ArrayList<Object> hosts = (ArrayList<Object>) service.get("jboss_hosts");
			
			// Java가 설치될 디렉토리 확인
			// jboss_home, domain_base 가 설치될 디렉토리 확인
			for (Object objHost : hosts){
				
				LinkedHashMap<String, Object> host = (LinkedHashMap<String, Object>) objHost;
				log.info("  host: {}", host.get("ip"));
				
				replaceMapEntry(unified_map, host);
				
				// 접속정보
				jschUtil.setHostname(unified_map.get("ip") + "");
				
				jschUtil.setUsername(unified_map.get("os_user_id") + "");
				jschUtil.setPassword(unified_map.get("os_user_pw") + "");
				jschUtil.enableDebug();
				
				
				// Host별 디렉토리 생성
				// host맵에 설정값이 있으면 unified_map의 기본값을 대체한다. Host 설정값 우선적용
				replaceMapEntry(unified_map, host);
				
				
				
				// jboss_home 디렉토리 존재여부 확인
				log.info("    Check directory : $JBOSS_HOME");
				checkDirExist(unified_map.get("jboss_home")+"");
				
				// domain_base 디렉토리 확인
				// domain_base 디렉토리가 jboss_home 내에 없는 경우만 확인
				if (!FilenameUtils.wildcardMatch(unified_map.get("domain_base")+"", unified_map.get("jboss_home")+"*")){
					// domain_base 디렉토리 존재여부 확인
					log.info("    Check directory : $DOMAIN_BASE");
					checkDirExist(unified_map.get("domain_base")+"");
				}
				
				
				
				//// 인스턴스 목록
				ArrayList<Object> instances = (ArrayList<Object>) host.get("instances");
				
				for (Object objInstance : instances){
					LinkedHashMap<String, Object> instance = (LinkedHashMap<String, Object>) objInstance;
					
					//log.info("    instance:" + instance.get("instance_name"));
					
					// instance맵에 설정값이 있으면 unified_map의 기본값을 대체한다. instance 설정값 우선적용
					replaceMapEntry(unified_map, instance);
					//log.debug("unified_map:" + unified_map);
					
					// log_home 디렉토리 존재여부 확인
					//checkDir(FilenameUtils.getFullPath((unified_map.get("log_home")+"")));
				}
				
			}
			
		}
		
		log.info("------------------------------------");
		log.info("Testing EAP Target Hosts Completed");
		log.info("------------------------------------");
		
	}
	
	/*
	 * twiddle을 이용하여 인스턴스 정보를 조회하고
	 * service_result.json파일에 저장한다.
	 */
	public static void getResultAllDataIntoJson() {
		log.info("------------------------------------");
		log.info("Getting Result Data From Target Hosts...");
		log.info("------------------------------------");
		
		String command = "";
		String result = "";
		
		//// service 목록
		ArrayList<Object> services = (ArrayList<Object>) services_map.get("services");
		
		for (Object objService : services){
			
			LinkedHashMap<String, Object> service = (LinkedHashMap<String, Object>) objService;
			log.info("------------------------------------");
			log.info("service name: {}", service.get("name"));
			log.info("------------------------------------");
			
			// host_default_values 정보
			LinkedHashMap<String, Object> host_default_values = (LinkedHashMap<String, Object>) services_map.get("host_default_values");
					
			// host 기본값과 개별 호스트 정보가 합쳐질 map
			LinkedHashMap<String, Object> unified_map = new LinkedHashMap<String, Object>();
			
			// host Default값 설정
			unified_map.putAll(host_default_values);
			
			// 인스턴스 Default 값 설정
			// instance_default_values맵에 설정값이 있으면 updated_instance_map 의 값을 대체한다. instance_default_values 설정값 우선적용
			replaceMapEntry(unified_map, (LinkedHashMap<String, Object> )services_map.get("instance_default_values"));
			
			
			//// 호스트 목록 
			ArrayList<Object> hosts = (ArrayList<Object>) service.get("jboss_hosts");
			
			// Java가 설치될 디렉토리 확인
			// jboss_home, domain_base 가 설치될 디렉토리 확인
			for (Object objHost : hosts){
				
				LinkedHashMap<String, Object> host = (LinkedHashMap<String, Object>) objHost;
				log.info("  host: {}", host.get("ip"));
				
				replaceMapEntry(unified_map, host);
				
				// 접속정보
				jschUtil.setHostname(unified_map.get("ip") + "");
				
				jschUtil.setUsername(unified_map.get("os_user_id") + "");
				jschUtil.setPassword(unified_map.get("os_user_pw") + "");
				jschUtil.enableDebug();
				
				
				// Host별 디렉토리 생성
				// host맵에 설정값이 있으면 unified_map의 기본값을 대체한다. Host 설정값 우선적용
				replaceMapEntry(unified_map, host);
				
				// twiddle은 내부적으로 java로 호출하므로 Path에 $JAVA_HOME/bin 에대한 정보가 있어야 한다.
				String java_home = unified_map.get("java_home") + "";
				String java_env = "export JAVA_HOME=" + java_home + ";export PATH=$JAVA_HOME/bin:$PATH";
				
				// twiddle.sh의 경로 관련
				String jboss_home = unified_map.get("jboss_home") + "";
				
				// hostname
				host.put("hostname", getHostname());
				
				// Operating System
				host.put("operating_system", getOSInfo());
				
				
				//// 인스턴스 목록
				ArrayList<Object> instances = (ArrayList<Object>) host.get("instances");
				
				for (Object objInstance : instances){
					LinkedHashMap<String, Object> instance = (LinkedHashMap<String, Object>) objInstance;
					
					//log.info("    instance:" + instance.get("instance_name"));
					
					// instance맵에 설정값이 있으면 unified_map의 기본값을 대체한다. instance 설정값 우선적용
					replaceMapEntry(unified_map, instance);
					//log.debug("unified_map:" + unified_map);
					
					// twiddle command 설정
					String twiddle = jboss_home + "/jboss-as/bin/twiddle.sh -s jnp://" + unified_map.get("domain_ip") + ":" 
										+ unified_map.get("jmx_console_port") + " --user=" 
										+ unified_map.get("jmx_console_id") + " --password=" 
										+ unified_map.get("jmx_console_pw") + " ";
					
					LinkedHashMap<String, Object> InstanceResult = new LinkedHashMap<String, Object>();
					
					// Input Arguments
					ArrayList<Object> InputArguments = getResultInputArguments(unified_map);
					InstanceResult.put("input_arguments", InputArguments);
					
					// JBoss Version
					HashMap<String, String> JbossVersion = getResultSimpleData(java_env, twiddle, " get 'jboss.system:type=Server' VersionName VersionNumber");
					InstanceResult.put("jboss_version", JbossVersion);
					
					// Java Version
					HashMap<String, String> JavaVersion = getResultSimpleData(java_env, twiddle, " get 'jboss.system:type=ServerInfo' JavaVersion");
					InstanceResult.put("java_version", JavaVersion);
					
					// JBoss Directory
					HashMap<String, String> JbossDirectory = getResultSimpleData(java_env, twiddle, " get 'jboss.system:type=ServerConfig' HomeDir ServerHomeDir ServerLogDir");
					InstanceResult.put("jboss_directory", JbossDirectory);
					
					// Java Heap Size
					HashMap<String, String> JavaHeapSize = getResultSimpleData(java_env, twiddle, " get 'jboss.system:type=ServerInfo' MaxMemory TotalMemory FreeMemory");
					InstanceResult.put("java_heap_size", JavaHeapSize);
					
					// Java Memory Pools (Heap, Non-Heap)
					HashMap<String, Object> JavaMemoryPools = getResultMemoryPools(java_env, twiddle, " invoke jboss.system:type=ServerInfo listMemoryPools false");
					
					InstanceResult.put("java_memory_pools", JavaMemoryPools);
					
					// Datasources 정보 조회
					HashMap<String, Object> dsList = getResultDatasources(java_env, twiddle);
					InstanceResult.put("datasources", dsList);
					
					// threadpools 정보 조회
					HashMap<String, Object> threadList = getResultThreadpools(java_env, twiddle);
					InstanceResult.put("thread_pools", threadList);
					
					// deployments 정보 조회 - instance - result - deployments
					ArrayList<HashMap<String, Object>> deploymentList = getResultDeployments(java_env, twiddle);
					InstanceResult.put("deployments", deploymentList);
					
					// SystemProperties 정보 조회
					// JBOSS_CLASSPATH를 설정해야 정상적으로 조회된다.
					String jboss_env = java_env + ";export JBOSS_HOME=" + jboss_home + "/jboss-as";
					HashMap<String, String> SystemProperties = getResultSystemProperties(jboss_env, twiddle, " invoke jboss:name=SystemProperties,type=Service showAll");
					InstanceResult.put("system_properties", SystemProperties);
					
					// disk usage
					HashMap<String, String> DiskUsageMap = new HashMap<String, String>();
					DiskUsageMap.put("engine_disk_usage", getDiskUsage(JbossDirectory.get("HomeDir") + ""));
					DiskUsageMap.put("log_disk_usage", getDiskUsage(JbossDirectory.get("ServerLogDir") + ""));
					InstanceResult.put("disk_usage", DiskUsageMap);
					
					// Server Log
					if ("yes".equalsIgnoreCase(server_log_file_inspection)){
						log.info("Inspecting Jboss Server Log: {}", server_log_file_date_expr);
						HashMap<String, Object> ServerLogMap = getResultServerLogs(JbossDirectory.get("ServerLogDir") + "");
						InstanceResult.put("server_log_file_inspection", ServerLogMap);
					}
					
					// GC Log
					if ("yes".equalsIgnoreCase(gc_log_file_inspection)){
						log.info("Inspecting Jboss GC Log: {}", gc_log_file_date_expr);
						GCLogData gcLogData = getResultGCLogs(JbossDirectory.get("ServerLogDir") + "");
						
						// Draw chart
						drawChartTimestampHeapMemory(instance.get("instance_name") + "", gcLogData);
						drawChartTimestampGCDuration(instance.get("instance_name") + "", gcLogData);
						
						// Avg. GC Interval
						HashMap<String, String> gc_log_file_inspection = new HashMap<String, String>();
						gc_log_file_inspection.put("FullGcAvgInterval", gcLogData.getFullGCAvgInterval() + "");
						gc_log_file_inspection.put("MaxGcDuration", gcLogData.getMaxGcDuration() + "");
						
						
						InstanceResult.put("gc_log_file_inspection", gc_log_file_inspection);
					}
					
					// instance에 추가한다.
					instance.put("result", InstanceResult);
				}
				
			}
			
			String json = "";
			try {
				json = new ObjectMapper().writeValueAsString(services_map);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			log.debug(json);
			
		}
		
		log.info("====================================");
		log.info("Testing EAP Target Hosts Completed");
		log.info("====================================");
		
	}
	/*
	 * Hostname 조회
	 */
	public static String getHostname() {
		log.info("------------------------------------");
		log.info("Getting Hostname...");
		log.info("------------------------------------");
		
		String command = "";
		String result = "";
		
		command = "hostname";
		result = jschUtil.exec(command);
		
		return result;
	}
	/*
	 * Disk Usage
	 */
	public static String getDiskUsage(String dir) {
		log.info("------------------------------------");
		log.info("Getting Disk Usage...");
		log.info("------------------------------------");
		
		String command = "";
		String result = "";
		
		command = "df -TP " + dir + " | awk '{print $6}' | tail -n 1 ";
		result = jschUtil.exec(command);
		
		return result;
	}
	
	/*
	 * Operating System 조회
	 */
	public static String getOSInfo() {
		log.info("------------------------------------");
		log.info("Getting OSInfo...");
		log.info("------------------------------------");
		
		String command = "";
		String result = "";
		
		command = "uname -is";
		result = jschUtil.exec(command);
		
		return result;
	}
	
	/*
	 * Operating System 조회
	 */
	public static ArrayList<Object> getResultInputArguments(LinkedHashMap<String, Object> unified_map) {
		log.info("------------------------------------");
		log.info("Getting VmArguments...");
		log.info("------------------------------------");
		
		String command = "";
		String result = "";
		
		ArrayList<String> command_list = new ArrayList<String>();
		
		//ps 명령을 이용해 Input Arguments를 구한다.
		command_list.add("/bin/ps -ef | grep java | grep \"server=" + unified_map.get("instance_name") + " \" | grep -v grep");
		command = StringUtils.join(command_list, ";");
		
		result = jschUtil.exec(command);
		
		command_list.clear();
		
		//VmArguments 를 저장할 map
		ArrayList<Object> InputArguments = new ArrayList<Object>(); 
		
		
		// " " 로 라인을 나우어 배열로 변경
		String[] attribute_arr = result.split("\\s* \\s*"); 
		for (int i=0; i < attribute_arr.length; i++){
			
			if (attribute_arr[i].startsWith("-")){
				log.info("attribute:{}", attribute_arr[i]);
				
				// binding IP : -b 0.0.0.0
				if ("-b".equals(attribute_arr[i])){
					InputArguments.add("-b " + attribute_arr[i+1]);
				} else {
					InputArguments.add(attribute_arr[i]);
				}
			}

			/*
			 * HashMap일 경우
			String[] entry = attribute_arr[i].split("=");                   // key / value 로 나누기
			
			if (entry.length == 2){
				InputArguments.put(entry[0].trim(), entry[1].trim());
			} else {
				String[] entry_ = attribute_arr[i].split(":"); // ":" 로 나누어져 있는 경우
				if (entry_.length == 2){
					InputArguments.put(entry_[0].trim(), entry_[1].trim());
				}
				
				// -server
				if ("-server".equals(attribute_arr[i])){
					InputArguments.put(attribute_arr[i], "");
				}
				// binding IP : -b 0.0.0.0
				if ("-b".equals(attribute_arr[i])){
					InputArguments.put(attribute_arr[i], attribute_arr[i+1]);
				}
			}
			*/
		}
		
		log.info("InputArguments:" + InputArguments);
		
		
		return InputArguments;
	}
	
	/*
	 * System Properties 조회
	 */
	public static HashMap<String, String> getResultSystemProperties(String jboss_env, String twiddle, String subCommand) {
		log.info("------------------------------------");
		log.info("Getting Simple Data From Instance...");
		log.info("------------------------------------");
		
		String command = "";
		String result = "";
		
		// System Properties 정보조회
		ArrayList<String> command_list = new ArrayList<String>();
		command_list.add(jboss_env);
		command_list.add("JBOSS_CLASSPATH=$JBOSS_CLASSPATH:$JBOSS_HOME/client/jbossall-client.jar ");
		command_list.add("JBOSS_CLASSPATH=$JBOSS_CLASSPATH:$JBOSS_HOME/client/getopt.jar ");
		command_list.add("JBOSS_CLASSPATH=$JBOSS_CLASSPATH:$JBOSS_HOME/client/log4j.jar ");
		command_list.add("JBOSS_CLASSPATH=$JBOSS_CLASSPATH:$JBOSS_HOME/lib/log4j.jar ");
		command_list.add("JBOSS_CLASSPATH=$JBOSS_CLASSPATH:$JBOSS_HOME/lib/jboss-jmx.jar ");
		command_list.add("JBOSS_CLASSPATH=$JBOSS_CLASSPATH:$JBOSS_HOME/lib/dom4j.jar ");
		command_list.add("JBOSS_CLASSPATH=$JBOSS_CLASSPATH:$JBOSS_HOME/lib/jboss-system-jmx.jar ");
		command_list.add("JBOSS_CLASSPATH=$JBOSS_CLASSPATH:$JBOSS_HOME/lib/jboss-common-beans.jar ");
		command_list.add("export JBOSS_CLASSPATH=$JBOSS_CLASSPATH:$JBOSS_HOME/common/lib/properties-plugin.jar ");
		command_list.add(twiddle + " " + subCommand);
		command = StringUtils.join(command_list, ";");
		result = jschUtil.exec(command);
		command_list.clear();
		
		// result가 HTML로 나오므로 불필요한 문자열 삭제
		result=result.replaceAll("<table>", "")
				.replaceAll("<tr><td align=\"left\"><b>", "")
				.replaceAll("</b></td><td align=\"left\">", "=")
				.replaceAll("</td></tr>", "")
				.replaceAll("\r\n", "")
				.replaceAll("\r", "\n")
				.replaceAll("</table>", "");
		log.debug(result);
		
		// JBossVersion의 HashMap을 생성하여 Attribute 값 입력
		HashMap<String, String> SystemProperties = new HashMap<String, String>();
		
		// Attribute가 여러개 나올 수 있으므로 개수 확인
		String[] attribute_arr = result.split("\\s*\n\\s*");  // line별로 나누어 배열로 변경
		
		// attribute 개수 만큼 key/value 저장
		for (String attribute : attribute_arr){
			if (attribute.contains("=")){
				String[] entry = attribute.split("=");                   // key / value 로 나누기
				// line.separator는 value 가 나누어지지 않는다.
				if (entry.length == 2){
					SystemProperties.put(entry[0].trim(), entry[1].trim());
				} else {
					if ("line.separator".equals(entry[0])){
						SystemProperties.put(entry[0].trim(), "");
					}
				}
				
			}
			
		}
		log.debug("SystemProperties:{}", SystemProperties);
		return SystemProperties;
	}
	
	/*
	 * twiddle을 이용하여 simple 정보를 조회
	 */
	public static HashMap<String, String> getResultSimpleData(String java_env, String twiddle, String subCommand) {
		log.info("------------------------------------");
		log.info("Getting Simple Data From Instance...");
		log.info("------------------------------------");
		
		String command = "";
		String result = "";
		
		// Datasource 정보조회
		ArrayList<String> command_list = new ArrayList<String>();
		command_list.add(java_env);
		command_list.add(twiddle + " " + subCommand);
		command = StringUtils.join(command_list, ";");
		result = jschUtil.exec(command);
		log.debug(result);
		command_list.clear();
		
		// JBossVersion의 HashMap을 생성하여 Attribute 값 입력
		HashMap<String, String> SimpleMap = new HashMap<String, String>();
		
		// Attribute가 여러개 나올 수 있으므로 개수 확인
		String[] attribute_arr = result.split("\\s*\n\\s*");  // line별로 나누어 배열로 변경
		
		// Attribute가 1개 인 경우
		if (attribute_arr.length == 1) {   
			String[] entry = result.split("=");                   // key / value 로 나누기 
			if (entry.length == 2) {
				SimpleMap.put(entry[0].trim(), entry[1].trim());
			} else if (entry.length > 2) {  // value 부분에 "=" 이 여러개 있는 경우
				StringBuffer sb = new StringBuffer("");
				for (int i = 1; i < entry.length; i++){
					sb.append(entry[i].trim());
				}
				SimpleMap.put(entry[0].trim(), sb.toString());
			}
		
		// Attribute가 2개 이상인 경우
		} else if (attribute_arr.length >= 2) {   
			// attribute 개수 만큼 key/value 저장
			for (String attribute : attribute_arr){
				String[] entry = attribute.split("=");                   // key / value 로 나누기
				if (entry.length == 2) {
					SimpleMap.put(entry[0].trim(), entry[1].trim());
				} else if (entry.length > 2) {  // value 부분에 "=" 이 여러개 있는 경우
					StringBuffer sb = new StringBuffer("");
					for (int i = 1; i < entry.length; i++){
						sb.append(entry[i].trim());
					}
					SimpleMap.put(entry[0].trim(), sb.toString());
				}
			}
		}
		
		
		return SimpleMap;
	}
	
	/*
	 * twiddle을 이용하여 MemoryPools 정보를 조회
	 */
	public static HashMap<String, Object> getResultMemoryPools(String java_env, String twiddle, String subCommand) {
		log.info("------------------------------------");
		log.info("Getting MemoryPools From Instance...");
		log.info("------------------------------------");
		
		String command = "";
		String result = "";
		
		// Datasource 정보조회
		ArrayList<String> command_list = new ArrayList<String>();
		command_list.add(java_env);
		command_list.add(twiddle + " " + subCommand);
		command = StringUtils.join(command_list, ";");
		result = jschUtil.exec(command);
		command_list.clear();
		
		// JavaMemoryPools 의 HashMap을 생성하여 Attribute 값 입력
		HashMap<String, Object> JavaMemoryPools = new HashMap<String, Object>();
		
		// html이 하나의 라인으로 나온다. 불필요한 Html 태그를 없앤다.
		// <br/>을 newLine으로 변경
		result = result.replaceAll("<br\\/>", "\n")
				.replaceAll("<blockquote>", "\n")
				.replaceAll("</blockquote>", "")
				.replaceAll("<b>", "")
				.replaceAll("</b>", "")
				.replaceAll("</b>", "");
		
		
		//여러 라인의 배열로 만든다.
		String[] attribute_arr = result.split("\\s*\n\\s*");  // line별로 나누어 배열로 변경
		
		//"Pool: " 로 시작하는 라인을 찾아 Pool Name 찾는다.
		// +1, +2 라인에서 사용량 정보를 찾는다.
		for (int line = 0; line < attribute_arr.length; line ++) {
			
			log.debug("attribute_arr:{}", attribute_arr[line]);
			//"Pool: " 로 시작하는 라인
			// 예) Pool: Code Cache (Non-heap memory)
			String Prefix = "Pool: ";
			if (attribute_arr[line].startsWith(Prefix)){
				//"Pool: " 다음부터 "(" 까지 추출
				String PoolName = attribute_arr[line].substring(Prefix.length(), attribute_arr[line].indexOf("(")).replaceAll(" ", "");
				log.debug("PoolName:{}", PoolName);
				
				HashMap<String, Object> JavaMemoryPool = new HashMap<String, Object>();
				//JavaMemoryPool.put("Name", PoolName);
				
				HashMap<String, String> PeakUsage = new HashMap<String, String>();
				HashMap<String, String> CurrentUsage = new HashMap<String, String>();
				
				/////////////////
				// PeakUsage 설정
				// PeakUsage는 현재라인의 +1 라인에 있다.
				// 예)  Peak Usage    : init:2555904, used:7038528, committed:7340032, max:50331648
				String PeakUsageEntry = attribute_arr[line+1].substring(attribute_arr[line+1].indexOf(":") + 1).replaceAll(" ", ""); // 첫번째 ":" 다음 부터 끝까지 자르기
				
				log.debug("PeakUsageEntry:{}", PeakUsageEntry);
				// 데이터 부분을 "," 로 나누어 분리
				String[] PeakUsageEntryData = PeakUsageEntry.split(",");
				for (int i = 0; i < PeakUsageEntryData.length; i++){
					log.debug("PeakUsageEntryData:{}", PeakUsageEntryData[i]);
					
					// init:2555904 데이터를 나누어 key/value로 저장
					String[] EntryData = PeakUsageEntryData[i].split(":");
					PeakUsage.put(EntryData[0], EntryData[1]);
				}
				
				// JavaMemoryPool에 PeakUsage 넣기
				JavaMemoryPool.put("peak_usage", PeakUsage);
				
				//////////////
				// CurrentUsage 설정
				// CurrentUsage는 현재라인의 +2 라인에 있다.
				// 예)  Current Usage    : init:2555904, used:7038528, committed:7340032, max:50331648
				String CurrentUsageEntry = attribute_arr[line+2].substring(attribute_arr[line+2].indexOf(":") + 1).replaceAll(" ", ""); // 첫번째 ":" 다음 부터 끝까지 자르기
				
				log.debug("CurrentUsageEntry:{}", CurrentUsageEntry);
				// 데이터 부분을 "," 로 나누어 분리
				String[] CurrentUsageEntryData = CurrentUsageEntry.split(",");
				for (int i = 0; i < CurrentUsageEntryData.length; i++){
					log.debug("CurrentUsageEntryData:{}", CurrentUsageEntryData[i]);
					
					// init:2555904 데이터를 나누어 key/value로 저장
					String[] EntryData = CurrentUsageEntryData[i].split(":");
					CurrentUsage.put(EntryData[0], EntryData[1]);
				}
				
				// JavaMemoryPool에 CurrentUsage 넣기
				JavaMemoryPool.put("current_usage", CurrentUsage);
				
				
				// JavaMemoryPool을 JavaMemoryPools에 저장
				JavaMemoryPools.put(PoolName, JavaMemoryPool);
			}
		}
		
		
		
		
		return JavaMemoryPools;
	}
	
	
	/*
	 * twiddle을 이용하여 Datasource 정보를 조회
	 * 
	 */
	public static HashMap<String, Object> getResultDatasources(String java_env, String twiddle) {
		log.info("------------------------------------");
		log.info("Getting Datasources Data From Instance...");
		log.info("------------------------------------");
		
		String command = "";
		String result = "";
		
		// Datasource 정보조회
		ArrayList<String> command_list = new ArrayList<String>();
		command_list.add(java_env);
		command_list.add(twiddle + " query 'jboss.jca:service=ManagedConnectionPool,*'");
		command = StringUtils.join(command_list, ";");  // ArrayList를 하나의 문자열로 합친다.
		result = jschUtil.exec(command);
		log.debug(result);
		
		// multi-line 결과를 Array로 변경
		String[] ds_arr = result.split("\\s*\n\\s*");  // line별로 나누어 배열로 변경
		
		// Datasource 정보를 담을 ArrayList 선언
		HashMap<String, Object> datasources = new HashMap<String, Object>();
		
		// datasource별 결과값 조회
		command_list.clear();
		
		for (String ds : ds_arr){
			command_list.add(java_env);
			command_list.add(twiddle + " get '" + ds + "' MaxConnectionsInUseCount InUseConnectionCount ConnectionCount MaxSize");
			command = StringUtils.join(command_list, ";");
			result = jschUtil.exec(command);
			log.info(result);
			command_list.clear();
			
			// datasource당 1개의 HashMap을 생성하여 Attribute 값 입력
			HashMap<String, String> ds_attr_map = new HashMap<String, String>();
			String[] attribute_arr = result.split("\\s*\n\\s*");  // line별로 나누어 배열로 변경
			for (String attribute : attribute_arr){
				String[] entry = attribute.split("=");                   //split the pairs to get key and value 
				
				ds_attr_map.put(entry[0].trim(), entry[1].trim());
			}
			// datasource 이름이 jboss.jca:service=ManagedConnectionPool,name=mysqlds 로 나오므로 실제 이름으로 변경
			ds = ds.split(",")[1].split("=")[1];
			// datasources에 datasource 등록
			datasources.put(ds, ds_attr_map);
		}
		/*
		// 인스턴스 내의 모든 datasource를 map에 넣는다.
		LinkedHashMap<String, Object> datasources = new LinkedHashMap<String, Object>();
		datasources.put("datasources", ds_list);
		*/
		return datasources;
	}
	
	/*
	 * twiddle을 이용하여 Threadpools 정보를 조회
	 * 
	 */
	public static HashMap<String, Object> getResultThreadpools(String java_env, String twiddle) {
		log.info("------------------------------------");
		log.info("Getting Threadpools Data From Instance...");
		log.info("------------------------------------");
		
		String command = "";
		String result = "";
		
		// Datasource 정보조회
		ArrayList<String> command_list = new ArrayList<String>();
		command_list.add(java_env);
		command_list.add(twiddle + " query 'jboss.web:type=ThreadPool,*'");
		command = StringUtils.join(command_list, ";");  // ArrayList를 하나의 문자열로 합친다.
		result = jschUtil.exec(command);
		log.debug(result);
		
		// multi-line 결과를 Array로 변경
		String[] threads_arr = result.split("\\s*\n\\s*");  // line별로 나누어 배열로 변경
		
		// Thread 정보를 담을 HashMap 선언
		HashMap<String, Object> ThreadsMap = new HashMap<String, Object>();
		
		// datasource별 결과값 조회
		command_list.clear();
		
		for (String thread : threads_arr){
			command_list.add(java_env);
			command_list.add(twiddle + " get '" + thread + "' currentThreadsBusy currentThreadCount maxThreads");
			command = StringUtils.join(command_list, ";");
			result = jschUtil.exec(command);
			log.debug("ThreadPool: {}", result);
			command_list.clear();
			
			HashMap<String, Object> thread_map = new HashMap<String, Object>();
			
			// thread당 1개의 HashMap을 생성하여 Attribute 값 입력
			HashMap<String, Integer> thread_attr_map = new HashMap<String, Integer>();
			String[] attribute_arr = result.split("\\s*\n\\s*");  // line별로 나누어 배열로 변경
			for (String attribute : attribute_arr){
				String[] entry = attribute.split("=");                   //split the pairs to get key and value 
				thread_attr_map.put(entry[0].trim(), Integer.parseInt(entry[1].trim()));
			}
			// thread 이름이 jboss.web:type=ThreadPool,name=http-0.0.0.0-8443 로 나오므로 실제 이름으로 변경
			thread = thread.split(",")[1].split("=")[1];
			
			// ThreadsMap에 thread_attr_map 등록
			ThreadsMap.put(thread, thread_attr_map);
		}
		
		return ThreadsMap;
	}
	/*
	 * twiddle을 이용하여 Deployments 정보를 조회
	 * 
	 */
	public static ArrayList<HashMap<String, Object>> getResultDeployments(String java_env, String twiddle) {
		log.info("------------------------------------");
		log.info("Getting Deployments Data From Instance...");
		log.info("------------------------------------");
		
		String command = "";
		String result = "";
		
		// Deployments 정보조회
		ArrayList<String> command_list = new ArrayList<String>();
		command_list.add(java_env);
		command_list.add(twiddle + " query 'jboss.web.deployment:*'");
		command = StringUtils.join(command_list, ";");  // ArrayList를 하나의 문자열로 합친다.
		result = jschUtil.exec(command);
		log.debug(result);
		
		// multi-line 결과를 Array로 변경
		String[] deployments_arr = result.split("\\s*\n\\s*");  // line별로 나누어 배열로 변경
		
		// Deployments 정보를 담을 ArrayList 선언
		ArrayList<HashMap<String, Object>> deployment_list = new ArrayList<HashMap<String, Object>>();
		
		// Deployment별 Context 명
		for (String deployment : deployments_arr){
			
			HashMap<String, Object> deployment_map = new HashMap<String, Object>();
			
			// deployment당 1개의 HashMap을 생성하여 Attribute 값 입력
			String[] entry = deployment.split("=");                   //split the pairs to get key and value 
			
			deployment_map.put(deployment, entry[1].trim());  // value에 Context 명 저장
			
			
			// List 에 deployment_map 등록
			deployment_list.add(deployment_map);
		}
		
		return deployment_list;
	}
	/*
	 * server log 확인
	 * 
	 */
	public static HashMap<String, Object> getResultServerLogs(String ServerLogDir) {
		log.info("------------------------------------");
		log.info("Getting Server Log Data From Instance...");
		log.info("------------------------------------");
		
		String command = "";
		String result = "";
		
		HashMap<String, Object> ServerLogMap = new HashMap<String, Object>();
		
		
		// 대상 로그파일 크기 합계
		ArrayList<String> command_list = new ArrayList<String>();
		command_list.add("cd " + ServerLogDir);
		command_list.add("du -bc " + server_log_file_date_expr + " | tail -n 1 | awk '{print $1}'");
		command = StringUtils.join(command_list, ";");  // ArrayList를 하나의 문자열로 합친다.
		result = jschUtil.exec(command);
		command_list.clear();
		log.debug(result);
		
		long LogFileTotalSize = 0;
		
		// 결과가 숫자이면
		if (NumberUtils.isDigits(result)){
			LogFileTotalSize = Long.parseLong(result);
			
			// Total Size가 제한값 보다 작으면 검사 수행
			if ( LogFileTotalSize < server_log_file_max_total_size ){
				
				// 검사할 에러문자열 만큼 루프
				for (String server_log_file_error_string : server_log_file_error_string_map.keySet()){
					command_list.add("cd " + ServerLogDir);
					command_list.add("grep \"" + server_log_file_error_string + "\" " + server_log_file_date_expr + " | wc -l");
					command = StringUtils.join(command_list, ";");  // ArrayList를 하나의 문자열로 합친다.
					result = jschUtil.exec(command);
					command_list.clear();
					log.debug(result);
					
					// Map에 저장
					ServerLogMap.put(server_log_file_error_string, result);
				}
				
				
			} else {
				log.warn("Total log file size exceeded log_file_max_total_size");
				log.warn("Skipped Server Log Inspection");
			}
		} else {
			log.error("LogFileTotalSize is not number.");
			log.error("Check this command: {}", command);
		}
		
		
		
		return ServerLogMap;
	}
	
	/*
	 * gc log 확인
	 * 
	 */
	public static GCLogData getResultGCLogs(String ServerLogDir) {
		log.info("------------------------------------");
		log.info("Getting Server Log Data From Instance...");
		log.info("------------------------------------");
		
		String command = "";
		String result = "";
		
		GCLogData gcLogData = null;
		
		
		// 대상 로그파일 크기 합계
		ArrayList<String> command_list = new ArrayList<String>();
		command_list.add("cd " + ServerLogDir + File.separator + gc_log_dir);
		command_list.add("du -bc " + gc_log_file_date_expr + " | tail -n 1 | awk '{print $1}'");
		command = StringUtils.join(command_list, ";");  // ArrayList를 하나의 문자열로 합친다.
		result = jschUtil.exec(command);
		command_list.clear();
		log.debug(result);
		
		long LogFileTotalSize = 0;
		
		// 결과가 숫자이면
		if (NumberUtils.isDigits(result)){
			LogFileTotalSize = Long.parseLong(result);
			
			// Total Size가 제한값 보다 작으면 검사 수행
			if ( LogFileTotalSize < gc_log_file_max_total_size ){
				
				String remote_gc_log_file_path = ServerLogDir + File.separator + gc_log_dir + File.separator + gc_log_file_date_expr;
				String local_gc_log_file_path = "data" + File.separator + gc_log_file_date_expr;
				
				// Download GC Log 파일
				jschUtil.scpFrom(remote_gc_log_file_path, new File(local_gc_log_file_path));
				
				// GC Log파일 읽기
				LineIterator it = null;
				try {
					it = FileUtils.lineIterator(new File(local_gc_log_file_path));
					gcLogData = getGCData(it);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
				    LineIterator.closeQuietly(it);
				}
				
				
				log.info("GCLogMap: {}", gcLogData);
				
			} else {
				log.warn("Total log file size exceeded log_file_max_total_size");
				log.warn("Skipped Server Log Inspection");
			}
		} else {
			log.error("LogFileTotalSize is not number.");
			log.error("Check this command: {}", command);
		}
		
		
		
		return gcLogData;
	}
	
	// VM Option값 확인
	public static void setCommandLineFlags(HashMap<String, String> CommandLineFlags, String line){
		// 예) CommandLine flags: -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC  -XX:+UseConcMarkSweepGC -XX:+UseParNewGC 
		String[] flags = StringUtils.split(line, " ");  
		
		for (String flag : flags){
			if ("-XX:+PrintGC".equals(flag)){
				CommandLineFlags.put(flag, "Y");
			}
			if ("-XX:+PrintGCDetails".equals(flag)){
				CommandLineFlags.put(flag, "Y");
			}
			if ("-XX:+PrintGCDateStamps".equals(flag)){
				CommandLineFlags.put(flag, "Y");
			}
			if ("-XX:+PrintGCTimeStamps".equals(flag)){
				CommandLineFlags.put(flag, "Y");
			}
			if ("-XX:+PrintHeapAtGC".equals(flag)){
				CommandLineFlags.put(flag, "Y");
			}
			if ("-XX:+UseParallelGC".equals(flag)){
				CommandLineFlags.put(flag, "Y");
				GCType = "UseParallelGC";
			}
			if ("-XX:+UseParallelOldGC".equals(flag)){
				CommandLineFlags.put(flag, "Y");
				GCType = "UseParallelOldGC";
			} 
			if ("-XX:+UseConcMarkSweepGC".equals(flag)){
				CommandLineFlags.put(flag, "Y");
				GCType = "UseConcMarkSweepGC";
			}
			if ("-XX:+UseG1GC".equals(flag)){
				CommandLineFlags.put(flag, "Y");
				GCType = "UseG1GC";
			}
		}
		
		// GC관련 옵션이 없는 경우 "-XX:+UseParallelGC" 로 세팅
		if (CommandLineFlags.get("-XX:+UseParallelGC") == null 
				&& CommandLineFlags.get("-XX:+UseParallelOldGC") == null
				&& CommandLineFlags.get("-XX:+UseConcMarkSweepGC") == null
				&& CommandLineFlags.get("-XX:+UseG1GC") == null
		){
			CommandLineFlags.put("-XX:+UseParallelGC", "Y");
			GCType = "UseParallelGC";
		}
		
		log.info("CommandLineFlags:{}", CommandLineFlags);
		
	}
	
	// UseParallelGC or UseParallelOldGC 데이터
	public static void addParGcDataToList(
			HashMap<String, String> CommandLineFlags,
			ArrayList<MinorGcData> MinorGCLogList, 
			ArrayList<FullGcData> FullGCLogList, 
			String[] cols, 
			int GCTypeCol,
			int TimeStampCol
			){
		/*Detail이 없을 경우*/
		// 124.161: [GC 472130K->308057K(573440K), 0.0266110 secs]
		// 124.188: [Full GC 308057K->294719K(573440K), 1.3359700 secs]
		
		/*Detail이 있을 경우*/
		//2.657: [GC [PSYoungGen: 132096K->21483K(153600K)] 132096K->23987K(503296K), 0.0257040 secs] [Times: user=0.07 sys=0.01, real=0.03 secs] 
		//6.467: [Full GC [PSYoungGen: 21501K->0K(256000K)] [ParOldGen: 52917K->66173K(349696K)] 74418K->66173K(605696K) [PSPermGen: 24616K->24601K(131072K)], 0.3010820 secs] [Times: user=0.91 sys=0.02, real=0.30 secs] 

		// Minor 또는 Full GC에 따른 구분
		// Minor GC인 경우 
		if (cols[GCTypeCol].startsWith("[GC")){
			
			MinorGcData MinorGCData = new MinorGcData();
			
			// -XX:+PrintGCDetails 설정여부에 따라 포맷이 바뀐다.
			if (CommandLineFlags.get("-XX:+PrintGCDetails") == null){
				// PrintGCDetails 이 설정되지 않은 경우
				// 124.161: [GC 472130K->308057K(573440K), 0.0266110 secs]
				int AfterMinorGCTotalHeapMemCol = GCTypeCol + 1;
				int MinorGCTimeCol = GCTypeCol + 2;
				MinorGCData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", ""))); 
				// After GC, Heap Memory Total 132096K->23936K(503296K)   ===> 23936 만 추출
				MinorGCData.setAfterMinorGCTotalUsedHeapMem(getMemoryValueWithMB(cols[AfterMinorGCTotalHeapMemCol])); 
				
				// GC Time)
				MinorGCData.setMinorGCTime(Double.parseDouble(cols[MinorGCTimeCol]));
			
			} else {
				// PrintGCDetails 이 설정된 경우
				//2.657: [GC [PSYoungGen: 132096K->21483K(153600K)] 132096K->23987K(503296K), 0.0257040 secs] [Times: user=0.07 sys=0.01, real=0.03 secs] 
				
				int AfterMinorGCTotalHeapMemCol = GCTypeCol + 3;
				int MinorGCTimeCol = GCTypeCol + 4;
				MinorGCData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", ""))); 
				// After GC, Heap Memory Total 132096K->23936K(503296K)   ===> 23936 만 추출
				MinorGCData.setAfterMinorGCTotalUsedHeapMem(getMemoryValueWithMB(cols[AfterMinorGCTotalHeapMemCol])); 
				
				// GC Time)
				MinorGCData.setMinorGCTime(Double.parseDouble(cols[MinorGCTimeCol]));
			}
			
			
			
			MinorGCLogList.add(MinorGCData);
		} else if (cols[GCTypeCol].startsWith("[Full")){
			
			FullGcData fullGcData = new FullGcData();
			
			// -XX:+PrintGCDetails 설정여부에 따라 포맷이 바뀐다.
			if (CommandLineFlags.get("-XX:+PrintGCDetails") == null){
				// PrintGCDetails 이 설정되지 않은 경우
				// 124.188: [Full GC 308057K->294719K(573440K), 1.3359700 secs]
				
				int AfterFullGCTotalHeapMemCol = GCTypeCol + 2;
				int AfterFullGCTotalPermMemCol = 0;  // PermMemory 컬럼은 없다.
				int FullGCTimeCol = GCTypeCol + 3;
				
				Double TimeStamp = Double.parseDouble(cols[TimeStampCol].replace(":", ""));
				fullGcData.setTimeStamp(TimeStamp); 
				
				// After GC, Heap Memory Total (132096K->23936K(503296K)   ===> 23936 만 추출)
				fullGcData.setAfterFullGCTotalUsedHeapMem(getMemoryValueWithMB(cols[AfterFullGCTotalHeapMemCol]));
				
				// PermMemory 컬럼은 없다. 0으로 세팅
				fullGcData.setAfterFullGCTotalUsedPermMem(0);
				
				//Full GC Time (1.3359700)
				fullGcData.setFullGCTime(Double.parseDouble(cols[FullGCTimeCol]));
				
			} else {
				// PrintGCDetails 이 설정된 경우
				//6.467: [Full GC [PSYoungGen: 21501K->0K(256000K)] [ParOldGen: 52917K->66173K(349696K)] 74418K->66173K(605696K) [PSPermGen: 24616K->24601K(131072K)], 0.3010820 secs] [Times: user=0.91 sys=0.02, real=0.30 secs] 
				
				int AfterFullGCTotalHeapMemCol = GCTypeCol + 6;
				int AfterFullGCTotalPermMemCol = GCTypeCol + 8;
				int FullGCTimeCol = GCTypeCol + 9;
				
				Double TimeStamp = Double.parseDouble(cols[TimeStampCol].replace(":", ""));
				fullGcData.setTimeStamp(TimeStamp); 
				
				// After GC, Heap Memory Total (132096K->23936K(503296K)   ===> 23936 만 추출)
				fullGcData.setAfterFullGCTotalUsedHeapMem(getMemoryValueWithMB(cols[AfterFullGCTotalHeapMemCol]));
				
				//After GC Perm Memory Total (132096K->23936K(503296K)   ===> 23936 만 추출)
				fullGcData.setAfterFullGCTotalUsedPermMem(getMemoryValueWithMB(cols[AfterFullGCTotalPermMemCol]));
				
				//Full GC Time (user=0.84   ===>   0.84)
				fullGcData.setFullGCTime(Double.parseDouble(cols[FullGCTimeCol]));
			}
			
			
			FullGCLogList.add(fullGcData);
			
			
		} else {
			// Continue
		}
	}
	
	/*
	UseConcMarkSweepGC 데이터
	GC/Full GC 로 나누어 파싱
	-XX:+PrintGCDetails 옵션에 따라 파싱
	*/
	public static void addCmsGcDataToList(
			HashMap<String, String> CommandLineFlags,
			ArrayList<MinorGcData> MinorGCLogList, 
			ArrayList<FullGcData> FullGCLogList, 
			ArrayList<CmsGcData> CmsGCLogList,
			String[] cols, 
			int GCTypeCol,
			int TimeStampCol,
			LineIterator it
			){
		
		int TotalUsedHeapMemCol = GCTypeCol + 4;
		int CmsGCTimeCol = GCTypeCol + 5;
		// Minor 또는 Full GC에 따른 구분
		if (cols[GCTypeCol].startsWith("[GC")){
			
			// -XX:+PrintGCDetails 설정여부에 따라 포맷이 바뀐다.
			if (CommandLineFlags.get("-XX:+PrintGCDetails") == null){
				// Minor GC인 경우 
    			/*
				 * 데이터 종류
				MinorGC - 4.689: [GC 252496K->65497K(496576K), 0.1465310 secs]
				Cms GC - 35.137: [GC 194331K(803776K), 0.0060360 secs]
				*/
				int AfterMinorGCTotalHeapMemCol = GCTypeCol + 1;
    			int MinorGCTimeCol = GCTypeCol + 2;
    			
    			// Minor또는 CMS 구분
    			String GCSubType = cols[AfterMinorGCTotalHeapMemCol];
    			String[] DataArr = StringUtils.split(GCSubType, "->");
    			
    			log.info("DataArr.length:{}", DataArr.length);
    			if (DataArr.length == 2){
    				// Minor GC
    				// 4.689: [GC 252496K->65497K(496576K), 0.1465310 secs]
        			MinorGcData minorGcData = new MinorGcData();
        			
        			minorGcData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", "")));
        			minorGcData.setAfterMinorGCTotalUsedHeapMem(getMemoryValueWithMB(cols[AfterMinorGCTotalHeapMemCol]));
        			
        			// GC Time (0.1465310)
        			minorGcData.setMinorGCTime(Double.parseDouble(cols[MinorGCTimeCol]));
        			MinorGCLogList.add(minorGcData);
        		} else {
        			// CMS GC 
        			//35.137: [GC 194331K(803776K), 0.0060360 secs]
        			CmsGcData cmsGcData = new CmsGcData();
        			
        			cmsGcData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", "")));
					
        			TotalUsedHeapMemCol = GCTypeCol + 1;
        			CmsGCTimeCol = GCTypeCol + 2;
        			
					cmsGcData.setTotalUsedHeapMem(Double.parseDouble(StringUtils.split(cols[TotalUsedHeapMemCol], "K")[0]) / 1024);
					cmsGcData.setCmsGCTime(Double.parseDouble(cols[CmsGCTimeCol]));
					
					CmsGCLogList.add(cmsGcData);
        		}
    			
    			
				
			} else {
				// PrintGCDetails 이 설정된 경우
				// CMS-initial-mark 인 경우
				if ("CMS-initial-mark:".equals(cols[GCTypeCol + 2])){
					// [DateStamp] 14.205: [GC [1 CMS-initial-mark: 142480K(247168K)] 171655K(496576K), 0.0179800 secs] [Times: user=0.03 sys=0.00, real=0.02 secs]
					CmsGcData cmsGcData = new CmsGcData();
					cmsGcData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", "")));
					// 171655K(496576K)  ==> 171655
					cmsGcData.setTotalUsedHeapMem(Double.parseDouble(StringUtils.split(cols[TotalUsedHeapMemCol], "K")[0]) / 1024);
					cmsGcData.setCmsGCTime(Double.parseDouble(cols[CmsGCTimeCol]));
					
					CmsGCLogList.add(cmsGcData);
				} else if ("[GC[YG".equals(cols[GCTypeCol])){
					// [DateStamp] 16.727: [GC[YG occupancy: 139801 K (249408 K)]16.727: [Rescan (parallel) , 0.0479650 secs]16.775: [weak refs processing, 0.0039580 secs]16.779: [scrub string table, 0.0005510 secs] [1 CMS-remark: 166580K(247168K)] 306381K(496576K), 0.0526520 secs] [Times: user=0.16 sys=0.00, real=0.05 secs]
					CmsGcData cmsGcData = new CmsGcData();
					cmsGcData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", "")));
					// 171655K(496576K)  ==> 171655
					int GCYGTotalUsedHeapMemCol = GCTypeCol + 24;
					int GCYGTimeColCol = GCTypeCol + 25;
					cmsGcData.setTotalUsedHeapMem(Double.parseDouble(StringUtils.split(cols[GCYGTotalUsedHeapMemCol], "K")[0]) / 1024 );
					cmsGcData.setCmsGCTime(Double.parseDouble(cols[GCYGTimeColCol]));
					
					CmsGCLogList.add(cmsGcData);
				} else if ("[CMS-concurrent-abortable-preclean:".equals(cols[GCTypeCol + 2])){
					// memory는 다음라인에 있다.
	    			String CmsMinorGCLine = it.nextLine();
	    			String[] CmsCols = StringUtils.split(CmsMinorGCLine, " "); 
	    			
					//53.209: [GC53.209: [ParNew53.307: [CMS-concurrent-abortable-preclean: 0.197/0.297 secs] [Times: user=0.50 sys=0.16, real=0.30 secs] 
					//: 249408K->27712K(249408K), 0.6288650 secs] 566922K->432112K(664064K), 0.6289990 secs] [Times: user=1.01 sys=0.44, real=0.62 secs] 
					
	    			CmsGcData cmsGcData = new CmsGcData();
	    			cmsGcData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", "")));
	    			// After GC, Heap Memory Total 132096K->23936K(503296K)   ===> 23936 만 추출
	    			cmsGcData.setTotalUsedHeapMem(getMemoryValueWithMB(CmsCols[TotalUsedHeapMemCol]));
	    			
	    			// GC Time (user=0.84   ===>   0.84)
	    			cmsGcData.setCmsGCTime(Double.parseDouble(CmsCols[CmsGCTimeCol]));
	    			
	    			CmsGCLogList.add(cmsGcData);
				} else {
					// Minor GC인 경우 
	    			// 6.023: [GC6.023: [ParNew: 249407K->27712K(249408K), 0.1335460 secs] 252453K->61091K(496576K), 0.1336110 secs] [Times: user=0.44 sys=0.01, real=0.13 secs] 
	    			int AfterMinorGCTotalHeapMemCol = GCTypeCol + 5;
	    			int MinorGCTimeCol = GCTypeCol + 6;
	    			
	    			MinorGcData minorGcData = new MinorGcData();
	    			minorGcData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", "")));
	    			// After GC, Heap Memory Total 132096K->23936K(503296K)   ===> 23936 만 추출
	    			minorGcData.setAfterMinorGCTotalUsedHeapMem(getMemoryValueWithMB(cols[AfterMinorGCTotalHeapMemCol]));
	    			
	    			// GC Time (user=0.84   ===>   0.84)
	    			minorGcData.setMinorGCTime(Double.parseDouble(cols[MinorGCTimeCol]));
	    			
	    			MinorGCLogList.add(minorGcData);
				}
			}
			
			
		} else if (cols[GCTypeCol].startsWith("[Full")){
			
			FullGcData fullGcData = new FullGcData();
			
			// -XX:+PrintGCDetails 설정여부에 따라 포맷이 바뀐다.
			if (CommandLineFlags.get("-XX:+PrintGCDetails") == null){
				// PrintGCDetails 이 설정되지 않은 경우
				/*
				 * 데이터 종류
				5.328: [Full GC 138664K->65523K(496576K), 0.1808040 secs]
				*/
				int AfterFullGCTotalHeapMemCol = GCTypeCol + 2;
				int AfterFullGCTotalPermMemCol = 0;  // PermMemory 컬럼은 없다.
				int FullGCTimeCol = GCTypeCol + 3;
    			
    			fullGcData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", "")));
    			
    			// After GC, Heap Memory Total (132096K->23936K(503296K)   ===> 23936 만 추출)
    			fullGcData.setAfterFullGCTotalUsedHeapMem(getMemoryValueWithMB(cols[AfterFullGCTotalHeapMemCol]));
    			
    			// PermMemory 컬럼은 없다. 0으로 세팅
    			fullGcData.setAfterFullGCTotalUsedPermMem(0);
    			
    			//Full GC Time (0.2000050)
    			fullGcData.setFullGCTime(Double.parseDouble(cols[FullGCTimeCol]));
    			
    			FullGCLogList.add(fullGcData);
				
			} else {
				// PrintGCDetails 이 설정된 경우
				
				
				// Full GC에 2가지 케이스가 있다.
				// Full GC 이후에 "[CMS-concurrent-abortable-preclean:" 
				// 또는 "[CMS-concurrent-mark:" 
				// 또는 "[CMS-concurrent-preclean:" 
				// 또는 "[CMS-concurrent-sweep:" 가 나올 경우는 다음라인에 memory 데이터가 있다.
				if (cols[GCTypeCol + 3].startsWith("[CMS-concurrent") ){
					//45.140: [Full GC45.140: [CMS45.242: [CMS-concurrent-abortable-preclean: 1.427/1.706 secs] [Times: user=3.18 sys=0.25, real=1.71 secs] 
	    			//	(concurrent mode interrupted): 262500K->249984K(336596K), 1.1878830 secs] 493889K->249984K(586004K), [CMS Perm : 81516K->81483K(131072K)], 1.1891510 secs] [Times: user=1.08 sys=0.01, real=1.19 secs] 
					
					fullGcData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", "")));
	    			
	    			// memory는 다음라인에 있다.
	    			String CmsFullGCLine = it.nextLine();
	    			log.info("CmsFullGCLine:{}", CmsFullGCLine);
	    			String[] CmsCols = StringUtils.split(CmsFullGCLine, " "); 
	    			
	    			int AfterFullGCTotalHeapMemCol = 6;
	    			int AfterFullGCTotalPermMemCol = 10;
	    			int FullGCTimeCol = 4;
	    			
	    			// After GC, Heap Memory Total (493889K->249984K(586004K)   ===> 249984 만 추출)
	    			fullGcData.setAfterFullGCTotalUsedHeapMem(getMemoryValueWithMB(CmsCols[AfterFullGCTotalHeapMemCol]));
	    			
	    			//After GC Perm Memory Total (81516K->81483K(131072K))   ===> 81483 만 추출)
	    			fullGcData.setAfterFullGCTotalUsedPermMem(getMemoryValueWithMB(CmsCols[AfterFullGCTotalPermMemCol]));
	    			
	    			//Full GC Time (1.1878830)
	    			fullGcData.setFullGCTime(Double.parseDouble(CmsCols[FullGCTimeCol]));
	    			
	    			FullGCLogList.add(fullGcData);
	    			
				// 일반적인 케이스
				} else {
					// [DateStamp] 6.776: [Full GC6.776: [CMS: 33379K->65591K(247168K), 0.1999170 secs] 136584K->65591K(496576K), [CMS Perm : 24624K->24609K(131072K)], 0.2000050 secs] [Times: user=0.20 sys=0.00, real=0.20 secs]
					int AfterFullGCTotalHeapMemCol = GCTypeCol + 6;
					int AfterFullGCTotalPermMemCol = GCTypeCol + 10;
					int FullGCTimeCol = GCTypeCol + 11;
	    			
	    			fullGcData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", "")));
	    			
	    			// After GC, Heap Memory Total (132096K->23936K(503296K)   ===> 23936 만 추출)
	    			fullGcData.setAfterFullGCTotalUsedHeapMem(getMemoryValueWithMB(cols[AfterFullGCTotalHeapMemCol]));
	    			
	    			//After GC Perm Memory Total (132096K->23936K(503296K)   ===> 23936 만 추출)
	    			fullGcData.setAfterFullGCTotalUsedPermMem(getMemoryValueWithMB(cols[AfterFullGCTotalPermMemCol]));
	    			
	    			//Full GC Time (0.2000050)
	    			fullGcData.setFullGCTime(Double.parseDouble(cols[FullGCTimeCol]));
	    			
	    			FullGCLogList.add(fullGcData);
				}
			}
			
			
			
		} else {
			// Continue
		}
		
	}
				
	// UseG1GC 데이터
	public static void addG1GcDataToList(
			HashMap<String, String> CommandLineFlags,
			ArrayList<MinorGcData> MinorGCLogList, 
			ArrayList<FullGcData> FullGCLogList, 
			ArrayList<G1GcData> G1GCLogList,
			String[] cols, 
			int GCTypeCol,
			int TimeStampCol,
			LineIterator it
			){
		log.info("addG1GcDataToList======");
		
		// Minor 또는 Full GC에 따른 구분
		if (cols[GCTypeCol].startsWith("[GC")){
			// GC cleanup 인 경우
			/*
			110.871: [GC cleanup 518M->493M(802M), 0.0079590 secs]
					 [Times: user=0.02 sys=0.00, real=0.01 secs] 
			*/				 
			if ("cleanup".equals(cols[GCTypeCol + 1])){
				int TotalUsedHeapMemCol = GCTypeCol + 2;
				int G1GCTimeCol = GCTypeCol + 3;
				
				G1GcData g1GcData = new G1GcData();
				g1GcData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", "")));
				// 518M->493M(802M)  ==> 493
				g1GcData.setTotalUsedHeapMem(getMemoryValueG1WithMB(cols[TotalUsedHeapMemCol]));
				g1GcData.setG1GCTime(Double.parseDouble(cols[G1GCTimeCol]));
				
				G1GCLogList.add(g1GcData);
			} else if ("pause".equals(cols[GCTypeCol + 1])){
				
				// Minor GC인 경우 
    			// 컬럼 순서 설정
				int AfterMinorGCTotalHeapMemCol = 0;  
    			int MinorGCTimeCol = 0;
    			
    			MinorGcData minorGcData = new MinorGcData();
    			
    			// PrintGCDetails 이 설정되지 않은 경우
    			if (CommandLineFlags.get("-XX:+PrintGCDetails") == null){
    				// PrintGCDetails 이 설정되지 않은 경우 컬럼 순서 설정
    				//   0.549: [GC pause (young) 25M->6534K(512M), 0.0114060 secs]
    				//  15.222: [GC pause (young) 437M->261M(512M), 0.0345720 secs]
    				//6882.316: [GC pause (young) (initial-mark) 333M->267M(512M), 0.0328050 secs]
    				if ("(initial-mark)".equals(cols[GCTypeCol + 3])){
    					AfterMinorGCTotalHeapMemCol = GCTypeCol + 4;
        				MinorGCTimeCol = GCTypeCol + 5;
    				} else {
    					//2.877: [GC pause (young) 104M->33M(512M), 0.0197420 secs] 
    					AfterMinorGCTotalHeapMemCol = GCTypeCol + 3;
        				MinorGCTimeCol = GCTypeCol + 4;
    				}
    				
    				
    				minorGcData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", "")));
        			minorGcData.setAfterMinorGCTotalUsedHeapMem(getMemoryValueG1WithMB(cols[AfterMinorGCTotalHeapMemCol]));
        			
        			// GC Time (user=0.84   ===>   0.84)
        			minorGcData.setMinorGCTime(Double.parseDouble(cols[MinorGCTimeCol]));
        			
    			} else {  // "Y" 인 경우  22 라인의 [Eden: 부분을 읽는다.
    				/*
    				 * // CommandLineFlags -XX:+PrintGCDetails 가 설정된 경우
    				110.624: [GC pause (young), 0.0756740 secs]
    				   [Parallel Time: 74.6 ms, GC Workers: 4]
    				      [GC Worker Start (ms): Min: 110624.3, Avg: 110627.5, Max: 110630.7, Diff: 6.4]
    				      [Ext Root Scanning (ms): Min: 14.6, Avg: 18.6, Max: 22.4, Diff: 7.9, Sum: 74.5]
    				      [SATB Filtering (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
    				      [Update RS (ms): Min: 11.1, Avg: 14.8, Max: 17.2, Diff: 6.1, Sum: 59.4]
    				         [Processed Buffers: Min: 3, Avg: 7.8, Max: 13, Diff: 10, Sum: 31]
    				      [Scan RS (ms): Min: 1.7, Avg: 4.4, Max: 6.2, Diff: 4.5, Sum: 17.5]
    				      [Code Root Scanning (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
    				      [Object Copy (ms): Min: 32.7, Avg: 33.2, Max: 34.0, Diff: 1.3, Sum: 132.8]
    				      [Termination (ms): Min: 0.0, Avg: 0.3, Max: 0.4, Diff: 0.4, Sum: 1.1]
    				      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
    				      [GC Worker Total (ms): Min: 68.2, Avg: 71.3, Max: 74.5, Diff: 6.3, Sum: 285.3]
    				      [GC Worker End (ms): Min: 110698.8, Avg: 110698.8, Max: 110698.8, Diff: 0.0]
    				   [Code Root Fixup: 0.0 ms]
    				   [Code Root Migration: 0.0 ms]
    				   [Clear CT: 0.1 ms]
    				   [Other: 0.9 ms]
    				      [Choose CSet: 0.0 ms]
    				      [Ref Proc: 0.4 ms]
    				      [Ref Enq: 0.0 ms]
    				      [Free CSet: 0.1 ms]
    				   [Eden: 44.0M(32.0M)->0.0B(117.0M) Survivors: 13.0M->6144.0K Heap: 514.3M(799.0M)->481.3M(802.0M)]
    				Heap after GC invocations=81 (full 4):
    				 garbage-first heap   total 821248K, used 492855K [0x00000000bd400000, 0x00000000ef600000, 0x00000000f0000000)
    				  region size 1024K, 6 young (6144K), 6 survivors (6144K)
    				 compacting perm gen  total 131072K, used 77396K [0x00000000f0000000, 0x00000000f8000000, 0x0000000100000000)
    				   the space 131072K,  59% used [0x00000000f0000000, 0x00000000f4b95318, 0x00000000f4b95400, 0x00000000f8000000)
    				No shared spaces configured.
    				*/
    				
    				// Timestamp
    				// 110.624: [GC pause (young), 0.0756740 secs]
    				minorGcData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", "")));
    				
    				// Heap Memory
    				String G1MinorGCLine = "";
    				
    				// 22 라인을 흘린다.
    				for (int i = 0; i < 22; i++){
    					G1MinorGCLine = it.nextLine();
    					if (G1MinorGCLine.startsWith("   [Eden:")){
    						break;
    					}
    				}
    				
    				// 읽은 데이터 : [Eden: 44.0M(32.0M)->0.0B(117.0M) Survivors: 13.0M->6144.0K Heap: 514.3M(799.0M)->481.3M(802.0M)]
    				log.info("G1MinorGCLine:{}", G1MinorGCLine);
        			String[] G1MinorCols = StringUtils.split(G1MinorGCLine, " "); 
        			
        			// 종류
        			// [Eden: 25.0M(25.0M)->0.0B(21.0M) Survivors: 0.0B->4096.0K Heap: 25.0M(512.0M)->6555.0K(512.0M)]
    				// [Eden: 44.0M(32.0M)->0.0B(117.0M) Survivors: 13.0M->6144.0K Heap: 514.3M(799.0M)->481.3M(802.0M)]
        			AfterMinorGCTotalHeapMemCol = 5; 
        			minorGcData.setAfterMinorGCTotalUsedHeapMem(getMemoryValueG1WithMB(G1MinorCols[AfterMinorGCTotalHeapMemCol]));
        			
        			// GC Time
        			// 110.624: [GC pause (young), 0.0756740 secs]
        			//6882.316: [GC pause (young) (initial-mark), 0.0328050 secs]
    				if ("(initial-mark),".equals(cols[GCTypeCol + 3])){  
    					MinorGCTimeCol = GCTypeCol + 4;
    				} else {
    					MinorGCTimeCol = GCTypeCol + 3;
    				}
    				log.info("MinorGCTimeCol:{}", MinorGCTimeCol);
    				minorGcData.setMinorGCTime(Double.parseDouble(cols[MinorGCTimeCol]));
    			}
    			
    			
    			
    			MinorGCLogList.add(minorGcData);
			} else {
				// cleanup 또는 pause 가 아닌 경우
				log.error("Check Minor GC Type, Exception of cleanup or pause");
			}
			
			
		} else if (cols[GCTypeCol].startsWith("[Full")){
			/*
			 52.424: [Full GC 422M->217M(512M), 1.0135750 secs]
			   [Eden: 100.0M(128.0M)->0.0B(172.0M) Survivors: 8192.0K->0.0B Heap: 422.0M(512.0M)->217.5M(512.0M)], [Perm: 77353K->77353K(131072K)]
			*/
			FullGcData fullGcData = new FullGcData();
			
			int AfterFullGCTotalHeapMemCol = GCTypeCol + 2;
			int AfterFullGCTotalPermMemCol = 7;
			int FullGCTimeCol = GCTypeCol + 3;
			
			fullGcData.setTimeStamp(Double.parseDouble(cols[TimeStampCol].replace(":", "")));
			
			// 52.424: [Full GC 422M->217M(512M), 1.0135750 secs]
			fullGcData.setAfterFullGCTotalUsedHeapMem(getMemoryValueG1WithMB(cols[AfterFullGCTotalHeapMemCol]));
			
			// PrintGCDetails 이 설정된 경우
			if ("Y".equals(CommandLineFlags.get("-XX:+PrintGCDetails"))){
				// Heap Memory
				String G1FullGCLine = "";
				
				// 다음 라인.
				G1FullGCLine = it.nextLine();
				
				// 읽은 데이터 :  [Eden: 100.0M(128.0M)->0.0B(172.0M) Survivors: 8192.0K->0.0B Heap: 422.0M(512.0M)->217.5M(512.0M)], [Perm: 77353K->77353K(131072K)]
				log.info("G1FullGCLine:{}", G1FullGCLine);
    			String[] G1FullCols = StringUtils.split(G1FullGCLine, " "); 
    			
				//After GC Perm Memory Total (132096K->23936K(503296K)   ===> 23936 만 추출)
				fullGcData.setAfterFullGCTotalUsedPermMem(getMemoryValueWithMB(G1FullCols[AfterFullGCTotalPermMemCol]));
			} else {
				// PrintGCDetails 이 설정안된 경우는 세부라인이 나오지 않는다.
			}
			
			//Full GC Time (0.2000050)
			fullGcData.setFullGCTime(Double.parseDouble(cols[FullGCTimeCol]));
			
			FullGCLogList.add(fullGcData);
			
			
		} else {
			// Continue
		}
	}
				
	// gc log의 line에서 데이터 추출
	public static GCLogData getGCData(LineIterator it){
		
		ArrayList<MinorGcData> MinorGCLogList = new ArrayList<MinorGcData>();
		ArrayList<FullGcData> FullGCLogList = new ArrayList<FullGcData>();
		ArrayList<CmsGcData> CmsGCLogList = new ArrayList<CmsGcData>();
		ArrayList<G1GcData> G1GCLogList = new ArrayList<G1GcData>();
		
		GCLogData gcLogData = new GCLogData();
		
		HashMap<String, String> CommandLineFlags = new HashMap<String, String>();
		
		// CommandLine flags 는 gclog 파일의 3번째 라인에 있다.
		for (int i=0; i< 3; i++){
			if (it.hasNext()){
				String line = it.nextLine();
				// CommandLine flags: 를 찾아 GC 종류 및 출력형태 확인
		    	if (line.startsWith("CommandLine flags:")){
		    		setCommandLineFlags(CommandLineFlags, line);
		    	}
			}
		}
		int DateStampCol = 0;
		int TimeStampCol = 0;
		int GCTypeCol = 0;
		int AfterMinorGCTotalHeapMemCol = 0;
		int MinorGCTimeCol = 0;
		
		int AfterFullGCTotalHeapMemCol = 0;
		int AfterFullGCTotalPermMemCol = 0;
		int FullGCTimeCol = 0;
		
		// Data line에서 컬럼 순서 세팅
		if ("Y".equals(CommandLineFlags.get("-XX:+PrintGCDateStamps"))){
			DateStampCol = 0;
		}
		if ("Y".equals(CommandLineFlags.get("-XX:+PrintGCTimeStamps"))){
			if (CommandLineFlags.get("-XX:+PrintGCDateStamps") == null){
				TimeStampCol = 0;
			} else {
				TimeStampCol = DateStampCol + 1;
			}
			
		}
		
		// GC 또는 Full GC 종류 컬럼순서
		GCTypeCol = DateStampCol + TimeStampCol + 1;
		
		
		// GC로그의 실제 데이터 부분
	    while (it.hasNext()) {
	    	
			String line = it.nextLine();
	    	
			try {
				// 숫자로 시작하는경우 - 종류별 GC Data 뽑기
		    	if (Character.isDigit(line.charAt(0))){
		    		String[] cols = StringUtils.split(line, " "); 
		    		log.info("cols[GCTypeCol]:{}", cols[GCTypeCol] );
		    		log.info(line);
		    		
		    		// Par 또는 Par-Old 인 경우
		    		if ("UseParallelGC".equals(GCType) || "UseParallelOldGC".equals(GCType)){
		    			// GC 데이터를 MinorGCLogList, FullGCLogList에 추가
		    			addParGcDataToList(CommandLineFlags, MinorGCLogList, FullGCLogList, cols, GCTypeCol, TimeStampCol);
		    			
		    		} else if ("UseConcMarkSweepGC".equals(GCType)){
		    			// GC 데이터를 MinorGCLogList, FullGCLogList, CmsGCLogList 에 추가
		    			addCmsGcDataToList(CommandLineFlags, MinorGCLogList, FullGCLogList, CmsGCLogList, cols, GCTypeCol, TimeStampCol, it);
		    			
		    		} else if ("UseG1GC".equals(GCType)){
		    			// GC 데이터를 MinorGCLogList, FullGCLogList, G1GCLogList 에 추가
		    			addG1GcDataToList(CommandLineFlags, MinorGCLogList, FullGCLogList, G1GCLogList, cols, GCTypeCol, TimeStampCol, it);
		    		}
		    		
		    	}
			} catch (Exception e){
				log.error("Skip Log Line: {}", line);
				log.error("Error: {}", e);
				
			}
	    	
	    	
	    }
	    // GC로그의 실제 데이터 부분 끝
	    
	    gcLogData.setMinorGCLogList(MinorGCLogList);
	    gcLogData.setFullGCLogList(FullGCLogList);
	    gcLogData.setCmsGCLogList(CmsGCLogList);
	    
		
	    return gcLogData;
	}
	
	// 차트 그리기 Timestamp - HeapMemory
	public static void drawChartTimestampHeapMemory(String InstanceName, GCLogData gcLogData){
		DrawChart drawChart = new DrawChart("Scatter Plot");
    	drawChart.setPanelTitle("Heap Usage After GC - " + InstanceName);
    	
	    // 데이터 전달
	    drawChart.setGcLogData(gcLogData);
	    drawChart.setXySeriesCollectionTimestampHeapMemory(); 
    	/*
	    // Chart를 화면으로 보이기
    	JPanel jpanel = drawChart.createPanel();
    	jpanel.setPreferredSize(new Dimension(640, 480));
    	drawChart.add(jpanel);
        
    	drawChart.pack();
    	RefineryUtilities.centerFrameOnScreen(drawChart);
    	drawChart.setVisible(true);
    	*/
	    
	    // Chart를 이미지로 저장
    	try {
			ChartUtilities.saveChartAsPNG(new File("data/" + InstanceName + "-HeapMemory.png"), drawChart.getChartTimestampHeapMemory(), 550, 350);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// 차트 그리기 Timestamp - GCDuration
	public static void drawChartTimestampGCDuration(String InstanceName, GCLogData  gcLogData){
		DrawChart drawChart = new DrawChart("Scatter Plot");
    	drawChart.setPanelTitle("GC Duration - " + InstanceName);
    	
	    // 데이터 전달
	    drawChart.setGcLogData(gcLogData);;
	    drawChart.setXySeriesCollectionTimestampGCDuration(); 
    	/*
	    // Chart를 화면으로 보이기
    	JPanel jpanel = drawChart.createPanelTimestampGCDuration();
    	jpanel.setPreferredSize(new Dimension(640, 480));
    	drawChart.add(jpanel);
        
    	drawChart.pack();
    	RefineryUtilities.centerFrameOnScreen(drawChart);
    	drawChart.setVisible(true);
    	*/
	    
	    // Chart를 이미지로 저장
    	try {
			ChartUtilities.saveChartAsPNG(new File("data/" + InstanceName + "-GCDuration.png"), drawChart.getChartTimestampHeapMemory(), 550, 350);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}
	
	// 132096K->23936K(503296K)   ===> 23936 만 추출
	public static Double getMemoryValueWithMB(String data){
		String value = "";
		log.debug("data:{}", data);
		// 132096K->23936K(503296K)   ===> 23936 만 추출
		String[] data_arr = StringUtils.split(data, "->"); 
		
		// 일반적인 경우
		if (data_arr.length == 2){
			// 23936K(503296K) ===> 23936
			value = StringUtils.split(data_arr[1], "K")[0];
		} else {
			// CMS GC, -XX:+PrintGCDetails설정 않한 경우
			// 321494K(499152K)
			data_arr = StringUtils.split(data, "K"); 
			value = data_arr[0];
		}
		
		
		return Double.parseDouble(value) / 1024;
	}
	// 518M->493M(802M)  ==> 493
	// 25.0M->6413.0K(512.0M)] => 6.4
	public static Double getMemoryValueG1WithMB(String data){
		String value = "";
		double numValue = 0;
		log.debug("data:{}", data);
		
		// 종류
		// 518M->493M(802M)  ==> 493M(802M)
		// 514.3M(799.0M)->481.3M(802.0M)]
		// 25.0M(512.0M)->6555.0K(512.0M)
		String[] data_arr = StringUtils.split(data, "->"); 
		
		// 481.3M(802.0M)] -> 481.3
		String[] data_arr2 = StringUtils.split(data_arr[1], "("); 
		
		// 481.3M ===> 481.3 , M
		value = data_arr2[0].substring(0, data_arr2[0].length() -1);
		char SizeUnit = data_arr2[0].charAt(data_arr2[0].length() -1 );
		
		// Size Unit에 따라 Heap Memory 크기를 MB로 계산한다.
		if (SizeUnit == 'K'){
			numValue = Double.parseDouble(value) / 1024;
		} else if (SizeUnit == 'M'){
			numValue = Double.parseDouble(value);
		} else if (SizeUnit == 'G'){
			numValue = Double.parseDouble(value) * 1024;
		} else {
			log.error("Invalid Memory Size Unit: {}", SizeUnit);
			log.error("Check data: {}", data);
		}
		
		return numValue;
	}
	
	/* 
	 * 디렉토리 확인
	 * */
	public static void checkDirExist(String dir) {
		// 디렉토리 존재여부 확인
		String command = "if test -d " + dir + "; then echo exist; fi";
		String result = jschUtil.exec(command);
		log.info("result:{}", result);
		if ("exist".equals(result)){
			log.info("      {}: Exists", dir);
			
		} else {
			log.info("      {}: Not exists", dir);
			// 디렉토리가 없을 경우 중지
			throw new RuntimeException();
		}
	}
}
