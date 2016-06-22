package TTS;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javazoom.jl.player.Player;

import org.json.JSONArray;
import org.json.JSONObject;

public class BaiduTTS {
	private static final String serverURL = "http://vop.baidu.com/server_api";
	private static String token = "";
	// modified here
	// put your own params here
	private static final String apiKey = "37UIxlA6yrSZzkyBr7h9GdoO";
	private static final String secretKey = "16d3d4cc27c83cea672c1545a78a1a8a";
	private static final String cuid = "7824195";
	private boolean saveFile = false;

	public BaiduTTS(String text, boolean flag) {
		this.saveFile = flag;

		try {
			getToken();
			String temmp3 = new String("d:/audio/" + text +".mp3");
			getVoice(text, temmp3);
			playAudio(temmp3);
			if (!saveFile) {
				new File(temmp3).delete();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		getToken();
		// getVoiceFromBrower("呵呵");
		//getVoice("西安电子科技大学软件学院G520实验室");
		playAudio("d:/audio/123.mp3");
		new BaiduTTS("献涛",true);
//		String str = "[{\"faceId\":\"c466b74d-1570-41fa-9a86-411d6f137e31\",\"faceRectangle\":{\"top\":66,\"left\":446,\"width\":89,\"height\":89},\"faceAttributes\":{\"gender\":\"female\",\"age\":23.3}}]";
//		System.out.println(str);
//		JSONObject jso = new JSONArray(str).getJSONObject(0);
//		System.out.println(jso.getJSONObject("faceAttributes").getString("gender"));
	}

	private static void  getToken() throws Exception {

		String getTokenURL = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials"
				+ "&client_id=" + apiKey + "&client_secret=" + secretKey;
		HttpURLConnection conn = (HttpURLConnection) new URL(getTokenURL)
				.openConnection();
		token = new JSONObject(printResponse(conn)).getString("access_token");
	}

	private static String printResponse(HttpURLConnection conn)
			throws Exception {

		if (conn.getResponseCode() != 200) {
			// request error
			return "server connected error"; // xiantao added here
		}
		InputStream is = conn.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is,
				"UTF-8"));
		String line;
		StringBuffer response = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			response.append(line);
			response.append('\r');
		}
		rd.close();
		try {
			// System.out.println(response.toString());
			System.out.println(new JSONObject(response.toString()).toString(4));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response.toString();
	}

	public  void getVoice(String voicetext, String path) throws Exception {
		// getToken();

		String getVoiceURL = "http://tsn.baidu.com/text2audio";
		String param = "tex=" + voicetext + "&lan=zh" + "&tok=" + token
				+ "&ctp=1" + "&cuid=" + cuid;
		// + "&spd=5" + "&pit=5" + "&vol=5"
		// + "&per=1";
		// String param =
		// "tex=献涛&lan=zh&cuid=7824195&ctp=1&tok=24.62c36aafdf9677c132223925e45e3ca6.2592000.1460190157.282335-7824195";

		HttpURLConnection conn = (HttpURLConnection) new URL(getVoiceURL)
				.openConnection();
		conn.setRequestProperty("Content-type", "audio/mp3");
		conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
		conn.setDoOutput(true);
		conn.setDoInput(true);
		PrintWriter out = new PrintWriter(conn.getOutputStream());
		out.print(param);
		out.flush();
		out.close();

		System.out.println("response code : " + conn.getResponseCode());
		if (conn.getResponseCode() != 200) {
			System.out.println("error!");
			return;
		}

		BufferedInputStream is = new BufferedInputStream(conn.getInputStream());

		if (conn.getContentType().equalsIgnoreCase("application/json")) {
			// System.out.println(conn.getContent().toString());
			printResponse(conn);
		} else if (conn.getContentType().equalsIgnoreCase("audio/mp3")) {

			int size = 0;
			int BUFFER_SIZE = 1024;
			byte[] buf = new byte[BUFFER_SIZE];
			FileOutputStream fos = new FileOutputStream(path);
			while ((size = is.read(buf)) != -1) {
				fos.write(buf, 0, size);
			}
			fos.flush();
			fos.close();
			System.out.println("done!");
			// AudioStream as = new AudioStream(is);
			// AudioInputStream as = (AudioInputStream) conn.getInputStream();
			// AudioPlayer.player.start(as);
			// AudioPlayer.player.stop(as);
			is.close();
		}
	}

	private static void getVoiceFromBrower(String text) {
		URL url = null;
		try {
			url = new URL(
					"http://tsn.baidu.com/text2audio?lan=zh&cuid=7824195&ctp=1"
							+ "&tok=24.62c36aafdf9677c132223925e45e3ca6.2592000.1460190157.282335-7824195&"
							+ "tex=" + text);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Runtime.getRuntime().exec(
					"rundll32 url.dll,FileProtocolHandler " + url);
			// Desktop.getDesktop().browse(new URI(url));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static  void playAudio(String path) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(path);
			Player playMP3 = new Player(fis);
			playMP3.play();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
