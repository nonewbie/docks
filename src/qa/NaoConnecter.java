package qa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import MicroSoft.FaceDetactor;
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

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			System.out.println("naoconnecter");
			RawBaiduRecognizer rbr = new RawBaiduRecognizer();
			NaoConnecter naoConn = new NaoConnecter();
			Tuling tuling = new Tuling();
			String toNao = "";
			String baiduResult;
			while(!naoConn.soc.isClosed()){
				//if(naoConn.br.ready()){//recieve nao's data imform
				String buff = naoConn.br.readLine();
				if(buff== null){//NAO机器人主动断开连接
					System.out.println("结束！");
					return ;
				}
				//System.in.read(new byte[100]);
				if(buff.startsWith("STARTRECORD")){
					System.out.println(buff);
					if(!buff.equals("STARTRECORD")){//方案二 与nao 对接
						String wavpath = buff.substring(12, buff.length());
						System.out.println("path：" + wavpath);
						baiduResult = rbr.getTxtResultByFile(wavpath);
					}else{//第一种实现方案
						baiduResult = rbr.getTxtResultByVac();
					}
					System.out.println("语音识别结果：" + baiduResult);
					
					if(baiduResult.equals("SAY AGAIN")){
						naoConn.wr.write(" ");
						naoConn.wr.flush();
						continue;
					}else if(baiduResult.equals("站起来")){
						System.out.println("站起来");
						naoConn.wr.write("WAKEUP");
						naoConn.wr.flush();
						continue;
					}else if(baiduResult.equals("你好")){
						System.out.println("你好");
						naoConn.wr.write("WAVERHAND");
						naoConn.wr.flush();
					}else if(baiduResult.equals("坐下")){
						System.out.println("坐下");
						naoConn.wr.write("SITDOWN");
						naoConn.wr.flush();
						continue;
					}else if(baiduResult.equals("调大音量")){
						System.out.println("调大音量");
						naoConn.wr.write("SOUNDUP");
						naoConn.wr.flush();
						continue;
					}else if(baiduResult.equals("调小音量")){
						System.out.println("调小音量");
						naoConn.wr.write("SOUNDDOWN");
						naoConn.wr.flush();
						continue;
					}else if(baiduResult.equals("说英语")){
						rbr.lan = "en";
						System.out.println("说英语");
						naoConn.wr.write("INENGLISH");
						naoConn.wr.flush();
						continue;
					}else if(baiduResult.equals("chinese,")){
						System.out.println(baiduResult);
						rbr.lan = "zh";
						System.out.println("说汉语");
						naoConn.wr.write("INCHINESE");
						naoConn.wr.flush();
						continue;
					}else if(baiduResult.equals("拜拜")){
						System.out.println("闭嘴");
						naoConn.wr.write("STOPSPEECH");
						naoConn.wr.flush();
						continue;
					}else if(baiduResult.startsWith("年龄")){
						System.out.println("我看起来多大");
						naoConn.wr.write("HOWOLD");
						naoConn.wr.flush();
						continue;
					}
					toNao = tuling.getResult(baiduResult);
					System.out.println(toNao + "\n" + new String(toNao.getBytes("utf-8"),"utf-8"));
					//System.out.println("\nencode type :　" + getEncoding("你妹"));
					naoConn.wr.write(new String(toNao.getBytes("utf-8"),"utf-8"));
					//naoConn.wr.write(toNao);
					naoConn.wr.flush();
				}else if(buff.startsWith("FACEDETECT")){
					String picpath = buff.substring(11, buff.length());
					System.out.println("path：" + picpath);
					FaceDetactor fd = new FaceDetactor(picpath);
					fd.getRes();
					toNao = "没有图片";
					if(fd.getGender().equals("female")){
						toNao = "美女，你看起来" + fd.getAge() + "岁";
					}else if(fd.getGender().equals("male")){
						toNao = "帅哥，你看起来" + fd.getAge() + "岁";
					}else{//没有找到识别结果，可能由于是图片没有拍到头像
						toNao = "对不起，没有拍到你的头像";
					}
					naoConn.wr.write(new String(toNao.getBytes("utf-8"),"utf-8"));
					//naoConn.wr.write(toNao);
					naoConn.wr.flush();
				}
			}//while
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
