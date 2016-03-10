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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;

import Frontend.LocalMicrophone;
import Frontend.VoiceActivityDetector;

public class BaiduRcecognizer {

	public BaiduRcecognizer() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws Exception {
		getToken();
		method1();
		// method2();
	}

	private static final String serverURL = "http://vop.baidu.com/server_api";
	private static String token = "";
	// modified here
	// put your own params here
	private static final String apiKey = "37UIxlA6yrSZzkyBr7h9GdoO";
	private static final String secretKey = "16d3d4cc27c83cea672c1545a78a1a8a";
	private static final String cuid = "7824195";

	private static void getToken() throws Exception {
		String getTokenURL = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials"
				+ "&client_id=" + apiKey + "&client_secret=" + secretKey;
		HttpURLConnection conn = (HttpURLConnection) new URL(getTokenURL)
				.openConnection();
		token = new JSONObject(printResponse(conn)).getString("access_token");
	}

	private static void method1() throws Exception {

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

			vac = new VoiceActivityDetector(new LocalMicrophone(),
					"LocalMicrophone");
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
								new ByteArrayInputStream(streamBuffer),
								vac.getFormat(), count);
						flag = false;
						AudioSystem.write(ais, AudioFileFormat.Type.WAVE, boas);

						{// save the wav file
							String tempaudio = new String("d:/audio/"
									+ System.currentTimeMillis() + ".wav");
							FileOutputStream fos = new FileOutputStream(
									tempaudio);
							AudioSystem.write(ais, AudioFileFormat.Type.WAVE,
									fos);
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
			printResponse(conn);

			// }// while
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}

	}// method

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

	private static byte[] loadFile(File file) throws IOException {
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
			throw new IOException("Could not completely read file "
					+ file.getName());
		}

		is.close();
		return bytes;
	}

	public void saveToFile(AudioInputStream audioInputStream, File audioFile,
			AudioFileFormat.Type fileType) {
		// reset to the beginnning of the captured data
		try {
			audioInputStream.reset();
		} catch (Exception e) {
			System.out.println("Unable to reset stream " + e);
			return;
		}

		try {
			if (AudioSystem.write(audioInputStream, fileType, audioFile) == -1) {
				throw new IOException("Problems writing to file");
			}
		} catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}

}
