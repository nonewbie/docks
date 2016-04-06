package Recognizer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import Frontend.LocalMicrophone;
import Frontend.VoiceActivityDetector;

public class ListenNao {
	//private static final String IP = "192.168.1.100";
	private static final int PORT = 8086;
	private Socket soc  = null;
	public BufferedReader br = null;
	ListenNao(){
		ServerSocket ss;
		try {
			ss = new ServerSocket(PORT);
			this.soc = ss.accept();
			this.br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new BaiduRcecognizer();
		ListenNao serReco = new ListenNao();
		String buff = null;
		int count = 1;
		try {
			System.out.println("start\n");
			VoiceActivityDetector vac = new VoiceActivityDetector(new LocalMicrophone(),
					"LocalMicrophone");
			while(!serReco.soc.isClosed()){
				if(serReco.br.ready()){
					buff = serReco.br.readLine();
					System.out.println(buff + ": " + count++);
					
					BaiduRcecognizer.method1(vac);
					
					
					vac = new VoiceActivityDetector(new LocalMicrophone(),
							"LocalMicrophone");
				}
			
			}
			System.out.println("connetion done");
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}