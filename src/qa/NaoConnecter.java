package qa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import Recognizer.RawBaiduRecognizer;

public class NaoConnecter {
	private static final int PORT = 8086;
	private Socket soc  = null;
	public BufferedReader br = null;
	public BufferedWriter wr = null;
	public NaoConnecter(){
		// TODO Auto-generated constructor stub
		ServerSocket ss;
		try {
			ss = new ServerSocket(PORT);
			this.soc = ss.accept();
			this.br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
			this.wr = new BufferedWriter(new OutputStreamWriter(soc.getOutputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//	public static String getEncoding(String str) {      
//	       String encode = "GB2312";      
//	      try {      
//	          if (str.equals(new String(str.getBytes(encode), encode))) {      
//	               String s = encode;      
//	              return s;      
//	           }      
//	       } catch (Exception exception) {      
//	       }      
//	       encode = "UTF-8";      
//	      try {      
//	          if (str.equals(new String(str.getBytes(encode), encode))) {      
//	               String s1 = encode;      
//	              return s1;      
//	           }      
//	       } catch (Exception exception1) {      
//	       }      
//	       encode = "ISO-8859-1";      
//	      try {      
//	          if (str.equals(new String(str.getBytes(encode), encode))) {      
//	               String s2 = encode;      
//	              return s2;      
//	           }      
//	       } catch (Exception exception2) {
//	       }      
//	       encode = "GBK";      
//	      try {      
//	          if (str.equals(new String(str.getBytes(encode), encode))) {      
//	               String s3 = encode;      
//	              return s3;      
//	           }      
//	       } catch (Exception exception3) {      
//	       }      
//	      return "";      
//	   }    
	public static void test(String []args){
		try {
			System.out.println("naoconnecter");
//			RawBaiduRecognizer rbr = new RawBaiduRecognizer();
			NaoConnecter naoConn = new NaoConnecter();
			String baiduResult = "test,i just want to test the language";// delete after test
			while(!naoConn.soc.isClosed()){
				if(naoConn.br.ready()){//recieve nao's data imform
					String buff = naoConn.br.readLine();
					System.out.println(buff);
//					String baiduResult = rbr.getTxtResultByVac();
					System.out.println("语音识别结果：" + baiduResult);
					
					if(baiduResult.equals("SAY AGAIN")){
						//do someting: to nao
						naoConn.wr.write("没有听清哦");
						naoConn.wr.flush();
						continue;
					}else if(baiduResult.equals("暂停，")){
						System.out.println("STOPSPEECH");
						naoConn.wr.write("STOPSPEECH");
						naoConn.wr.flush();
						baiduResult = "test,Without you?I'd be a soul without a purpose. \n "
								+ "Without you?I'd be an emotion without a heart \n"
								+ "I'm a face without expression,A heart with no beat.\n"
								+ " Without you by my side,I'm just a flame without the light";
						continue;
					}else if(baiduResult.startsWith("test")){
						System.out.println("test");
						naoConn.wr.write(baiduResult);
						naoConn.wr.flush();
						baiduResult = "暂停，";
						continue;
					}
//					String toNao = new Tuling(baiduResult).getResult();
//					System.out.println(toNao + "\n" + new String(toNao.getBytes("utf-8"),"utf-8"));
//					//System.out.println("\nencode type :　" + getEncoding("你妹"));
//					naoConn.wr.write(new String(toNao.getBytes("utf-8"),"utf-8"));
//					//naoConn.wr.write(toNao);
//					naoConn.wr.flush();
				}//if
			}//while
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			System.out.println("naoconnecter");
			RawBaiduRecognizer rbr = new RawBaiduRecognizer();
			NaoConnecter naoConn = new NaoConnecter();
//			String baiduResult = "test,i just want to test the language";// delete after test
			while(!naoConn.soc.isClosed()){
				if(naoConn.br.ready()){//recieve nao's data imform
					String buff = naoConn.br.readLine();
					System.out.println(buff);
					String baiduResult = rbr.getTxtResultByVac();
					System.out.println("语音识别结果：" + baiduResult);
					
					if(baiduResult.equals("SAY AGAIN")){
						//do someting: to nao
						naoConn.wr.write("没有听清哦");
						naoConn.wr.flush();
						continue;
					}else if(baiduResult.equals("暂停，")){
						System.out.println("STOPSPEECH");
						naoConn.wr.write("STOPSPEECH");
						naoConn.wr.flush();
//						baiduResult = "test,Without you?I'd be a soul without a purpose. \n "
//								+ "Without you?I'd be an emotion without a heart \n"
//								+ "I'm a face without expression,A heart with no beat.\n"
//								+ " Without you by my side,I'm just a flame without the light";
						continue;
					}else if(baiduResult.startsWith("test")){
						System.out.println("test");
						naoConn.wr.write(baiduResult);
						naoConn.wr.flush();
						baiduResult = "暂停，";
						continue;
					}else if(baiduResult.equals("站起来，")){
						System.out.println("站起来");
						naoConn.wr.write("WAKEUP");
						naoConn.wr.flush();
//						baiduResult = "暂停，";
						continue;
					}else if(baiduResult.equals("坐下，")){
						System.out.println("坐下");
						naoConn.wr.write("SITDOWN");
						naoConn.wr.flush();
//						baiduResult = "暂停，";
						continue;
					}else if(baiduResult.equals("闭嘴，")){
						System.out.println("闭嘴");
						naoConn.wr.write("STOPSPEECH");
						naoConn.wr.flush();
//						baiduResult = "暂停，";
						continue;
					}
					String toNao = new Tuling(baiduResult).getResult();
					System.out.println(toNao + "\n" + new String(toNao.getBytes("utf-8"),"utf-8"));
					//System.out.println("\nencode type :　" + getEncoding("你妹"));
					naoConn.wr.write(new String(toNao.getBytes("utf-8"),"utf-8"));
					//naoConn.wr.write(toNao);
					naoConn.wr.flush();
				}//if
			}//while
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
