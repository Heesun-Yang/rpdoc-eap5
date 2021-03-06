package com.rp.docs.eap5.collecting_data;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;

public class JschUtil {
	static Logger log = LoggerFactory.getLogger(JschUtil.class);
	
	private String hostname;
	private String username;
	private String identity=null;
	private String password=null;
	private boolean isDebugMode=true;
	private boolean pty=true;
	private int connectTimeout = 3000;
	
	
	public ChannelExec channelExec;

	public void enableDebug(){
		isDebugMode=true;
	}

	public void disableDebug(){
		isDebugMode=false;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
		this.password =null;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
		this.identity=null;
	}
	

	public boolean isPty() {
		return pty;
	}

	public void setPty(boolean pty) {
		this.pty = pty;
	}
	
	

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	@Override
	public String toString() {
		return "JschUtil [hostname=" + hostname + ", username=" + username
				+ ", identity=" + identity + ", password=" + password
				+ ", isDebugMode=" + isDebugMode + ", pty=" + pty
				+ ", channelExec=" + channelExec + "]";
	}

	public JschUtil(){ }

	public JschUtil(String username,String hostname){
		this.username = username;
		this.hostname = hostname;
	}

	public void setPortForwardingL(int port,String host,int hostport){

	}

	private Session getSession() throws JSchException{
		JSch jsch=new JSch();
		if (identity!=null) {
			jsch.addIdentity(identity);
			//jsch.setKnownHosts(new ByteArrayInputStream(hostname.getBytes()));
		}

		Session session=jsch.getSession(username, hostname, 22);
		session.setConfig("StrictHostKeyChecking", "no");
		if (password!=null)	session.setPassword(password);
		return session;
	}

	// 단일명령 수행
	public String exec(String command){
		return exec(new String[] {command}).get(0);
	}
	
	// 여러개의 명령 수행
	public List<String> exec(List<String> commands){
		return exec(commands.toArray(new String[]{}));
	}


	public List<String> exec(String[] commands) {
		List<String> ret = new ArrayList<String>();
		Session session = null;
		channelExec = null;
		//String resultLogLevel = "debug";
		try{
			session = getSession();
			session.connect();
			for (String command:commands){
				// 현재 수행중인 Command 출력
				log.info("command : {}", command);
				
				channelExec = (ChannelExec) session.openChannel("exec");
				channelExec.setPty(isPty());
				
				// command
				channelExec.setCommand(command);
				//InputStream inputStream = channelExec.getInputStream();
				channelExec.setInputStream(null);
				InputStream stdout = channelExec.getInputStream();
		       InputStream stderr = channelExec.getErrStream();
				
				channelExec.connect(connectTimeout);
				
				String result = "";
				result = IOUtils.toString(stdout, "UTF-8");
				
				
				// 실행결과 성공이면 debug로 출력, 실패면 에러로 출력
				// 일부 명령 실행시 성공 종료 후 -1이 리턴되는 경우가 생김.. jsch의 기본값이 -1 이다.
				if (channelExec.getExitStatus() > 0) {
					log.error("--{}--", result);
					
				} else {    // error 인 경우
					//result = IOUtils.toString(stderr, "UTF-8");
					//log.error("channelExec.getExitStatus():{}", channelExec.getExitStatus() );
					
					log.debug(result);
					//throw new Exception(result);
				}
		        
				// 실행결과를 ArrayList에 담는다.
				ret.add(StringUtils.chop(result));
				

			}
			
			//session.disconnect();
		}catch(Exception e){
			log.error("JschUtil: " + toString());
			e.printStackTrace();
			
		} finally {
			if (session.isConnected()) session.disconnect();
			if (channelExec.isConnected()) channelExec.disconnect();
		}
		return ret;
	}

	public List<String> exec_org(String[] commands) {
		List<String> ret = new ArrayList<String>();
		Session session = null;
		channelExec = null;
		try{
			session = getSession();
			session.connect();
			for (String command:commands){
				channelExec = (ChannelExec) session.openChannel("exec");
				channelExec.setPty(isPty());
				
				// command 출력
				if (isDebugMode) System.out.println("command : "+command);
				
				// command
				channelExec.setCommand(command);
				//InputStream inputStream = channelExec.getInputStream();
				InputStream stdout = channelExec.getInputStream();
		       InputStream stderr = channelExec.getErrStream();
				//InputStream ext = channelExec.getExtInputStream();
				//InputStream err = channelExec.getErrStream();
				channelExec.connect(connectTimeout);

				if (isDebugMode) System.out.println("stdout : ");
				String output="";
				byte[] buf = new byte[1024];
				int length;
				
				while ((length=stdout.read(buf)) != -1 ){
					output+=new String(buf,0,length);
					if (isDebugMode) System.out.print(new String(buf,0,length));
				}
				if (isDebugMode) {
					System.out.println("\nerr :"+IOUtils.toString(stderr) + "==");
					if (!"".equals(IOUtils.toString(stderr)))
					System.out.println("\nerr : "+IOUtils.toString(stderr));
				}
				ret.add(StringUtils.chop(output));
				
			}
			
			//session.disconnect();
		}catch(Exception e){
			log.error("JschUtil: " + toString());
			e.printStackTrace();
			
		} finally {
			if (session.isConnected()) session.disconnect();
			if (channelExec.isConnected()) channelExec.disconnect();
		}
		return ret;
	}


	/* ---- generate ssh keypair  --------- */

	public Map<String,String> keyGen(String algorithm, String passphrase,String comment){
		String privateKeyString = "";
		String publicKeyString = "";

		int type=0;
		if(algorithm.toLowerCase().equals("rsa")) type=KeyPair.RSA;
		else if(algorithm.toLowerCase().equals("dsa"))type=KeyPair.DSA;
		else {
			System.err.println("does not support "+algorithm+" algorithm");
			return null;
		}

		JSch jsch=new JSch();
		try{
			KeyPair kpair=KeyPair.genKeyPair(jsch, type);
			kpair.setPassphrase(passphrase);
			ByteArrayOutputStream priout = new ByteArrayOutputStream();
			ByteArrayOutputStream pubout = new ByteArrayOutputStream();
			kpair.writePrivateKey(priout);
			kpair.writePublicKey(pubout, comment);

			privateKeyString = priout.toString();
			publicKeyString = pubout.toString();
			if (isDebugMode) {
				System.out.println("Private Key : \n"+privateKeyString);
				System.out.println("Public Key : \n"+publicKeyString);
				System.out.println("Finger print: "+kpair.getFingerPrint());
			}
			kpair.dispose();
		}
		catch(Exception e){
			e.printStackTrace();
			//System.out.println(e);
		}

		Map<String,String> map = new HashMap<String,String>();
		map.put("privateKey",privateKeyString);
		map.put("publicKey",publicKeyString);
		return map;
	}

	/* Scp ----------------------- */

	public String scpFrom(String rfile){
		String str ="";
		try{
			File lfile = File.createTempFile("temp", ".tmp");
			BufferedReader br = new BufferedReader(new FileReader(lfile));
			scpFrom(rfile,lfile);
			String line;
			while((line=br.readLine())!=null) str+=line+"\n";
			br.close();
			lfile.delete();
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
		return str;
	}
	public void scpFrom(String rfile,File lfile){
		//usage: java ScpFrom user@remotehost:file1 file2
		FileOutputStream fos=null;
		Session session = null;
		Channel channel = null;
		try{

			session = getSession();
			// username and password will be given via UserInfo interface.
			session.connect();

			// exec 'scp -f rfile' remotely
			String command="scp -f "+rfile;
			channel=session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out=channel.getOutputStream();
			InputStream in=channel.getInputStream();

			channel.connect();

			byte[] buf=new byte[1024];

			// invoke '\0'
			buf[0]=0; out.write(buf, 0, 1); out.flush();

			while(true){
				int c=checkAck(in);
				if(c!='C'){
					break;
				}

				// read '0644 '
				in.read(buf, 0, 5);

				long filesize=0L;
				while(true){
					if(in.read(buf, 0, 1)<0){
						// error
						break;
					}
					if(buf[0]==' ')break;
					filesize=filesize*10L+(long)(buf[0]-'0');
				}

				String file=null;
				for(int i=0;;i++){
					in.read(buf, i, 1);
					if(buf[i]==(byte)0x0a){
						file=new String(buf, 0, i);
						break;
					}
				}

				//System.out.println("filesize="+filesize+", file="+file);

				// invoke '\0'
				buf[0]=0; out.write(buf, 0, 1); out.flush();

				// read a content of lfile

				fos=new FileOutputStream(lfile);
				int foo;
				while(true){
					if(buf.length<filesize) foo=buf.length;
					else foo=(int)filesize;
					foo=in.read(buf, 0, foo);
					if(foo<0){
						// error
						break;
					}
					fos.write(buf, 0, foo);
					filesize-=foo;
					if(filesize==0L) break;
				}
				fos.close();
				fos=null;

				if(checkAck(in)!=0){
					return;
				}

				// invoke '\0'
				buf[0]=0; out.write(buf, 0, 1); out.flush();
			}

			//session.disconnect();
		}
		catch(Exception e){
			System.out.println(e);
			try{if(fos!=null)fos.close();}catch(Exception ee){}
		}finally {
			if (channel.isConnected()) channel.disconnect();
			if (session.isConnected()) session.disconnect();
		}
	}

	// 문자열을 파일로 생성하여 원격으로 복사
	public void scpTo(String content, String rfile){
		try{
			File tfile = File.createTempFile("prefix", ".tmp");
			FileWriter fw = new FileWriter(tfile);
			fw.write(content);
			fw.close();
			scpTo(tfile,rfile);
			tfile.delete();
		}catch(IOException e){
			e.printStackTrace();
			return;
		}
	}

	// 파일을 원격으로 복사
	public void scpTo(File lfile, String rfile){
		// ScpTo file1 user@remotehost:file2

		FileInputStream fis=null;
		Session session = null;
		Channel channel = null;
		try{
			session = getSession();
			session.connect();

			boolean ptimestamp = true;

			// exec 'scp -t rfile' remotely
			String command="scp " + (ptimestamp ? "-p" :"") +" -t "+rfile;
			channel=session.openChannel("exec");
			((ChannelExec)channel).setCommand(command);

			// get I/O streams for remote scp
			OutputStream out=channel.getOutputStream();
			InputStream in=channel.getInputStream();

			channel.connect();

			if(checkAck(in)!=0){
				//System.exit(0);
				return;
			}

			String filename = lfile.getName();
			if(ptimestamp){
				command="T"+(lfile.lastModified()/1000)+" 0";
				// The access time should be sent here,
				// but it is not accessible with JavaAPI ;-<
				command+=(" "+(lfile.lastModified()/1000)+" 0\n");
				out.write(command.getBytes()); out.flush();
				if(checkAck(in)!=0){
					//System.exit(0);
					return;
				}
			}

			// invoke "C0644 filesize filename", where filename should not include '/'
			long filesize=lfile.length();
			command="C0644 "+filesize+" ";
			if(filename.lastIndexOf('/')>0){
				command+=filename.substring(filename.lastIndexOf('/')+1);
			}
			else{
				command+=filename;
			}
			command+="\n";
			out.write(command.getBytes()); out.flush();
			if(checkAck(in)!=0){
				//System.exit(0);
				return;
			}

			// invoke a content of lfile
			fis=new FileInputStream(lfile);
			byte[] buf=new byte[1024];
			while(true){
				int len=fis.read(buf, 0, buf.length);
				if(len<=0) break;
				out.write(buf, 0, len); //out.flush();
			}
			fis.close();
			fis=null;
			// invoke '\0'
			buf[0]=0; out.write(buf, 0, 1); out.flush();
			if(checkAck(in)!=0){
				return;
			}
			out.close();

			//channel.disconnect();
			//session.disconnect();
		}
		catch(Exception e){
			System.out.println(e);
			try {if(fis!=null)fis.close();}
			catch(Exception ee){}
		} finally {
			if (channel.isConnected()) channel.disconnect();
			if (session.isConnected()) session.disconnect();
		}
	}

	private int checkAck(InputStream in) throws IOException{
		int b=in.read();
		// b may be 0 for success,
		//          1 for error,
		//          2 for fatal error,
		//          -1
		if(b==0) return b;
		if(b==-1) return b;

		if(b==1 || b==2){
			StringBuffer sb=new StringBuffer();
			int c;
			do {
				c=in.read();
				sb.append((char)c);
			}
			while(c!='\n');
			if(b==1){ // error
				System.out.print(sb.toString());
			}
			if(b==2){ // fatal error
				System.out.print(sb.toString());
			}
		}
		return b;
	}
}
