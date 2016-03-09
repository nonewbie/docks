package Recognizer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javaFlacEncoder.FLACFileWriter;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.bind.DatatypeConverter;

import org.json.JSONObject;

import Data.Result;
import Frontend.LocalMicrophone;
import Frontend.VoiceActivityDetector;

public class BaiduRcecognizer implements StandardRecognizer {

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

	private static AudioFormat getAudioFormat() {
		float sampleRate = 16000;
		int sampleSizeInBits = 16;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = false;
		AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
				channels, signed, bigEndian);
		return format;
	}

	private static void getToken() throws Exception {
		String getTokenURL = "https://openapi.baidu.com/oauth/2.0/token?grant_type=client_credentials"
				+ "&client_id=" + apiKey + "&client_secret=" + secretKey;
		HttpURLConnection conn = (HttpURLConnection) new URL(getTokenURL)
				.openConnection();
		token = new JSONObject(printResponse(conn)).getString("access_token");
	}

	private static void method1() throws Exception {

		HttpURLConnection conn = (HttpURLConnection) new URL(serverURL)
				.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type",
				"application/json; charset=utf-8");

		conn.setDoInput(true);
		conn.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

		boolean flag = true;
		VoiceActivityDetector vac = new VoiceActivityDetector(
				new LocalMicrophone(), "LocalMicrophone");
		// get stream from connection
		// DataOutputStream stream = new
		// DataOutputStream(conn.getOutputStream());
		int buffer_size = 4000;
		byte tempBuffer[] = new byte[buffer_size];
		FLACFileWriter ffw = new FLACFileWriter();
		ByteArrayOutputStream boas = new ByteArrayOutputStream();
		// construct params
		JSONObject params = new JSONObject();
		params.put("format", "x-flac");
		params.put("rate", 16000);
		params.put("channel", "1");
		params.put("token", token);
		params.put("cuid", cuid);
		params.put("lan", "en");// xiantao added here
		while (true) {
			while (flag) {
				int cnt = -1;
				cnt = vac.read(tempBuffer, 0, buffer_size);
				if (cnt > 0) {// if there is data
					// Printer.reset();
					ByteArrayInputStream byteInputStream = new ByteArrayInputStream(
							tempBuffer);
					AudioInputStream ais = new AudioInputStream(
							byteInputStream, getAudioFormat(), cnt); // open a
																		// new
																		// audiostream
					try {
						ffw.write(ais, FLACFileWriter.FLAC, boas);// convert
																	// audio
																	// data to
																	// FLAC
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					params.put("len", boas.size());
					params.put("speech", DatatypeConverter
							.printBase64Binary(boas.toByteArray()));

					wr.writeBytes(params.toString());
					wr.flush();
					wr.close();

					printResponse(conn);
				} else
					flag = false;
			}//while
		}//while
	}//method

	@Override
	public Result recognizeFromResult(Result r) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result recognizeFromFile(String fileName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getReferenceRecognizer() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	private static String printResponse(HttpURLConnection conn)
			throws Exception {
		if (conn.getResponseCode() != 200) {
			// request error
			return "server connected error"; // xiantao added here
		}
		InputStream is = conn.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is,
				"UTF-8"));// xiantao modified here
		String line;
		StringBuffer response = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			response.append(line);
			response.append('\r');
		}
		rd.close();
		System.out.println(new JSONObject(response.toString()).toString(4));
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

	public static void main() {

	}
}
