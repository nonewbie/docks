package Recognizer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
	public String lan = "zh";//当前的语言环境，默认是中文环境
	private String getToken() throws Exception {
		String getTokenURL = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials"
				+ "&client_id=" + apiKey + "&client_secret=" + secretKey;
		HttpURLConnection conn = (HttpURLConnection) new URL(getTokenURL)
				.openConnection();
		return new JSONObject(printResponse(conn)).getString("access_token");
	}

	protected String printResponse(HttpURLConnection conn) throws Exception {
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
			//System.out.println(response.toString());
			System.out.println(new JSONObject(response.toString()).toString(4));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this.full2HalfChange(response.toString());
	}

	public RawBaiduRecognizer() throws Exception {
		// TODO Auto-generated constructor stub
		RawBaiduRecognizer.token = this.getToken();
	}

	private String getRecognizationResult(String txt) {//从百度返回的语音结果中提取之别结果。txt:百度返回的文本结果
		JSONObject jsonTxt = new JSONObject(txt);
		// System.out.println(jsonTxt.getJSONArray("result").optString(0,
		// "null"));
		if (jsonTxt.isNull("result")) {
			return "SAY AGAIN";
		}
		String str = jsonTxt.getJSONArray("result").optString(0);
		return toNum(str.substring(0, str.length()-1));
	}
	public static String toNum(String str){//将汉字转换成阿拉伯数字,主要在快递查询中只用
		StringBuffer res = new StringBuffer("");
		for (int i = 0;i < str.length();i++){
			if(str.charAt(i) == '一'){
				res.append('1');
			}else if(str.charAt(i) == '二'){
				res.append('2');
			}else if(str.charAt(i) == '三'){
				res.append('3');
			}else if(str.charAt(i) == '四'){
				res.append('4');
			}else if(str.charAt(i) == '五'){
				res.append('5');
			}else if(str.charAt(i) == '六'){
				res.append('6');
			}else if(str.charAt(i) == '七'){
				res.append('7');
			}else if(str.charAt(i) == '八'){
				res.append('8');
			}else if(str.charAt(i) == '九'){
				res.append('9');
			}else if(str.charAt(i) == '零'){
				res.append('0');
			}else{
				res.append(str.charAt(i));
			}
		}
		return res.toString();
	} 
	@SuppressWarnings("resource")
	public String getTxtResultByVac() {//实时的从电脑麦克风录入语音进行语音识别。
		HttpURLConnection conn = null;
		VoiceActivityDetector vac = null;
		try {
			conn = (HttpURLConnection) new URL(serverURL).openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/json; charset=utf-8");
			conn.setDoInput(true);
			conn.setDoOutput(true);

			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			int buffer_size = 4000;
			LocalMicrophone microphone = new LocalMicrophone();
			vac = new VoiceActivityDetector(microphone, "LocalMicrophone");
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
			if(this.lan.equals("zh")){//切换语言使用，未来的扩展
				params.put("lan", "zh");
			}else if(this.lan.equals("en")){
				params.put("lan", "en");
			}

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
								new ByteArrayInputStream(streamBuffer),
								vac.getFormat(), count);
						flag = false;
						AudioSystem.write(ais, AudioFileFormat.Type.WAVE, boas);

						if (true) {// save the wav file
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
							System.out.println("upload time :" + System.currentTimeMillis());
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
			if (Frontend.LocalMicrophone.line.isOpen()) {
				Frontend.LocalMicrophone.line.close();
			}
			// vac.close();
			return getRecognizationResult(printResponse(conn));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String getTxtResultByFile(String testFileName) {//从wav文件进行语音识别
		try {
			File wavFile = new File(testFileName);
			HttpURLConnection conn = (HttpURLConnection) new URL(serverURL)
					.openConnection();
			// construct params
			JSONObject params = new JSONObject();
			params.put("format", "wav");
			params.put("rate", 16000);
			params.put("channel", "1");
			params.put("token", token);
			params.put("cuid", cuid);
			params.put("len", wavFile.length());
			params.put("speech",
					DatatypeConverter.printBase64Binary(loadFile(wavFile)));

			// add request header
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type",
					"application/json; charset=utf-8");

			conn.setDoInput(true);
			conn.setDoOutput(true);

			// send request
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(params.toString());
			wr.flush();
			wr.close();

			return getRecognizationResult(printResponse(conn));
		} catch (Exception e) {
			System.out.println(e);
		}
		return null;

	}

	 private  byte[] loadFile(File file) throws IOException {//加载音频文件
	        InputStream is = new FileInputStream(file);

	        long length = file.length();
	        byte[] bytes = new byte[(int) length];

	        int offset = 0;
	        int numRead = 0;
	        while (offset < bytes.length
	                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
	            offset += numRead;
	        }

	        if (offset < bytes.length) {
	            is.close();
	            throw new IOException("Could not completely read file " + file.getName());
	        }

	        is.close();
	        return bytes;
	    }
	
	public final String full2HalfChange(String QJstr) {//百度识别返回的结果中有全角的字符，转换成半角。
		StringBuffer outStrBuf = new StringBuffer("");
		String Tstr = "";
		byte[] b = null;
		for (int i = 0; i < QJstr.length(); i++) {
			Tstr = QJstr.substring(i, i + 1);
			// 全角空格转换成半角空格
			if (Tstr.equals("　")) {
				outStrBuf.append(" ");
				continue;
			}

			try {
				b = Tstr.getBytes("unicode");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// 得到 unicode 字节数据
			if (b[2] == -1) {
				// 表示全角？
				b[3] = (byte) (b[3] + 32);
				b[2] = 0;
				try {
					outStrBuf.append(new String(b, "unicode"));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				outStrBuf.append(Tstr);
			}
		} // end for.
		return outStrBuf.toString();
	}
	public static void testRecognizeFromFile(String wavPath){//从录制好的wav语音文件进行测试
		try {
				RawBaiduRecognizer rbr = new RawBaiduRecognizer();
				System.out.println(rbr.getTxtResultByFile(wavPath));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void testRecognizeFromVac(){//测试从本机电脑进行百度语音识别，原始语音格式目前只支持8k/16k采样率16bit位深的单声道语音
		try {
			while (true) {
				RawBaiduRecognizer rbr = new RawBaiduRecognizer();
				System.out.println(rbr.getTxtResultByVac());
				System.in.read(new byte[100]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		testRecognizeFromVac();
		//testRecognizeFromFile("D:\\audio\\1461744720790.wav");
	}
}