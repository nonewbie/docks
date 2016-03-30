package qa;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import Recognizer.RawBaiduRecognizer;

public class Tuling {
	private String inTxt;
	private final String apiKey = "f67b75e4fd233c494efbd7ea47b92b67";
	private final String serverURL = "http://www.tuling123.com/openapi/api";
	public Tuling(String txt) {
		// TODO Auto-generated constructor stub
		this.inTxt = txt;
	}
	public Tuling() {
		// TODO Auto-generated constructor stub
		this.inTxt = "";
	}
	public int setTxt(String txt){
		this.inTxt = txt;
		return 1;
	}
	public String getResult() throws IOException{
		return this.getResult(this.inTxt);
	}
	
	public String getResult(String txt) throws IOException{
		HttpURLConnection conn = (HttpURLConnection) new URL(serverURL).openConnection();
		conn.setRequestMethod("POST");
		//conn.setRequestProperty("Content-Type","text/plain; charset=g");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		String encodeTxt = URLEncoder.encode(txt,"utf-8");
		String post = "key=" + apiKey + "&info=" + encodeTxt; 
		
		wr.writeBytes(post);
		
		//get result
		InputStream is = conn.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is,"UTF-8"));
	
		String buff = "";
		buff = rd.readLine();
		System.out.println("RAW: " + buff);
		if(this.getErrorCode(buff) == 100000){//class text
			//System.out.println("code is 100000");
			return this.getAnswer(buff,"text");
		}else if(this.getErrorCode(buff) == 200000 ){//class url
			//System.out.println("code is 200000");
			String url = new JSONObject(buff).getString("url");
			Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			return this.getAnswer(buff,"text") + ".Show you.";
		}else if(this.getErrorCode(buff) == 302000){//class news
			//System.out.println("code is 302000");
			JSONArray jList = new JSONObject(buff).getJSONArray("list");
			//System.out.println(new JSONObject(buff).get("list"));
			//System.out.println(jList.length());
			String allNews = "";
			for (int i = 0;i < jList.length();i++){
				allNews += ((JSONObject) jList.get(i)).get("article") + "\n";
			}
			return this.getAnswer(buff,"text") + "\n" +allNews;
		}else if(this.getErrorCode(buff) == 308000){//class recipe
			JSONArray jList = new JSONObject(buff).getJSONArray("list");
			//System.out.println(new JSONObject(buff).get("list"));
			//System.out.println(jList.length());
			String CookInfo = "";
			if(jList.length() > 0){
				CookInfo = ( (JSONObject) jList.get(0) ).getString("info");
			}
			return this.getAnswer(buff,"text") + "\n" + CookInfo;
		}else if(this.getErrorCode(buff) == 313000){//class child song
			System.out.println("code is 313000");
			return null;
		}else if(this.getErrorCode(buff) == 314000){//class child poetry
			System.out.println("code is 314000");
			return null;
		}else{
			return null;
		}
		//System.out.println(buff);
	}
	
	private int getErrorCode(String str){// get the error code of the reuslt
		return new JSONObject(str).getInt("code");
	}
	
	private String getAnswer(String str,String field){// get the answer of the reuslt
		return new JSONObject(str).getString(field);
		
	}
	public static void main(String []args){
		try {
			RawBaiduRecognizer rbr = new RawBaiduRecognizer();
			while(true){
				String baiduResult = rbr.getTxtResultByVac();
				System.out.println("语音识别结果：" + baiduResult);
				if(baiduResult.equals("SAY AGAIN")){
					continue;
				}
				System.out.println(new Tuling(baiduResult).getResult());
			}
			
//			System.out.println(new Tuling("西电的照片").getResult());
//			System.out.println(new Tuling("").getResult());
//			System.out.println(new Tuling("西电的照片").getResult());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
