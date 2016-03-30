package Recognizer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;

import Frontend.LocalMicrophone;
import Frontend.VoiceActivityDetector;

public class RawBaiduRecognizer {
	private static final String serverURL = "http://vop.baidu.com/server_api";
	private static String token = "";
	private static final String apiKey = "37UIxlA6yrSZzkyBr7h9GdoO";
	private static final String secretKey = "16d3d4cc27c83cea672c1545a78a1a8a";
	private static final String cuid = "7824195";
	private String getToken() throws Exception {
		String getTokenURL = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials"
				+ "&client_id=" + apiKey + "&client_secret=" + secretKey;
		HttpURLConnection conn = (HttpURLConnection) new URL(getTokenURL)
				.openConnection();
		return new JSONObject(printResponse(conn)).getString("access_token");
	}
	protected static String printResponse(HttpURLConnection conn)
			throws Exception {
		if (conn.getResponseCode() != 200) {
			// request error
			return "server connected error"; // xiantao added here
		}
		InputStream is = conn.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is,"UTF-8"));
		String line;
		StringBuffer response = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			response.append(line);
			response.append('\r');
		}
		rd.close();
		try {
			//System.out.println(response.toString());
			System.out.println(new JSONObject(response.toString()).toString(4));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response.toString();
	}
	
	public RawBaiduRecognizer() throws Exception {
		// TODO Auto-generated constructor stub
		RawBaiduRecognizer.token = this.getToken();
	}
	
	private  String getRecognizationResult(String txt){
		JSONObject jsonTxt = new JSONObject(txt);
		//System.out.println(jsonTxt.getJSONArray("result").optString(0, "null"));
		if(jsonTxt.isNull("result")){
			return "SAY AGAIN";
		}
		return jsonTxt.getJSONArray("result").optString(0);
	}
	@SuppressWarnings("resource")
	public String getTxtResultByVac(){
		HttpURLConnection conn = null;
		VoiceActivityDetector vac = null;
		try {
			conn = (HttpURLConnection) new URL(serverURL).openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type","application/json; charset=utf-8");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			int buffer_size = 4000;
			LocalMicrophone microphone = new LocalMicrophone();
			vac = new VoiceActivityDetector(microphone,"LocalMicrophone");
			byte[] tempBuffer = new byte[buffer_size];
			byte[] streamBuffer = new byte[buffer_size * 50];
			JSONObject params = null;
			// while (true) {
			// construct params
			params = new JSONObject();
			params.put("format", "wav");
			params.put("rate", 16000);
			params.put("channel", "1");
			params.put("token", token);
			params.put("cuid", cuid);
			params.put("lan", "zh");// xiantao added here

			int count = 0;
			boolean flag = true;

			ByteArrayOutputStream boas = new ByteArrayOutputStream();

			while (flag) {
				int cnt = -1;
				cnt = vac.read(tempBuffer, 0, buffer_size);
				if (cnt > 0) {// if there is data
					System.arraycopy(tempBuffer, 0, streamBuffer, count, cnt);
					count += cnt;
				} else {
					if (count > 0) {
						AudioInputStream ais = new AudioInputStream(
								new ByteArrayInputStream(streamBuffer),vac.getFormat(), count);
						flag = false;
						AudioSystem.write(ais, AudioFileFormat.Type.WAVE, boas);

						if (true){// save the wav file
							AudioInputStream ais2 = new AudioInputStream(
									new ByteArrayInputStream(streamBuffer),
									vac.getFormat(), count);
							String tempaudio = new String("d:/audio/"
									+ System.currentTimeMillis() + ".wav");
							FileOutputStream fos = new FileOutputStream(
									tempaudio);
							
							AudioSystem.write(ais2, AudioFileFormat.Type.WAVE,
									fos);
							ais2.close();
							fos.flush();
							fos.close();
						}
						ais.close();
					}
				}
			}// while

			// System.out.println("**********len: " + audioFile.length());
			params.put("len", boas.size());
			params.put("speech",
					DatatypeConverter.printBase64Binary(boas.toByteArray()));
			boas.close();
			// ((Closeable) audioFile).close();//never sure
			wr.writeBytes(params.toString());
			wr.flush();
			wr.close();
			if(Frontend.LocalMicrophone.line.isOpen()){
				Frontend.LocalMicrophone.line.close();
			}
			//vac.close();
			return getRecognizationResult( printResponse(conn) );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			RawBaiduRecognizer rbr = new RawBaiduRecognizer();
			System.out.println(rbr.getTxtResultByVac());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
