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
	public static int day = 0;
	private final String apiKey = "f67b75e4fd233c494efbd7ea47b92b67";
	private final String serverURL = "http://www.tuling123.com/openapi/api";
	public Tuling(String txt) {
		// TODO Auto-generated constructor stub
		this.inTxt = txt;
		System.out.println(txt);
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
		//System.out.println(txt);
		String encodeTxt = URLEncoder.encode(txt,"utf-8");
		String post = "key=" + apiKey + "&info=" + encodeTxt + "&userid=123213"; 
		
		wr.writeBytes(post);
		
		//get result
		InputStream is = conn.getInputStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is,"UTF-8"));
	
		String buff = "";
		buff = rd.readLine();
		//System.out.println("RAW: " + buff);
		if(this.getErrorCode(buff) == 100000){//class text
			//System.out.println("code is 100000");
			//图灵机器人对于天气，返回连续4天的，不会智能判断指定的某天，天气处理
			if(txt.contains("明天")){
				day = 1;
			}
			if(txt.contains("后天")){
				day = 2;
			}
			if(txt.contains("大后天")){
				day = 3;
			}
			if(txt.contains("今天")){
				day = 0;
			}//针对天气的日期处理
			
			return this.weaterDeal(this.getAnswer(buff,"text").replaceAll("<br>", "\n"),day);//如果是天气，只查询明天的
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
			Integer i;
			for ( i = 1;i < jList.length();i++){
				allNews += i.toString() + ":" + ((JSONObject) jList.get(i)).get("article") + "；\n";
			}
			allNews += i.toString() + ":" + ((JSONObject) jList.get(jList.length()-1)).get("article");
			return this.getAnswer(buff,"text") + ".\n" + allNews;
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
	private String weaterDeal(String text,int day){ //day 是今天(0)，明天(1)，后天(2)，大后天(3)的索引
		 if (text.split(";").length == 4){
			 String [] strs = text.split(";");
			 String res = strs[day];
			 if(day > 0){
				 res = strs[0].substring(0, 2) + ": " + res;
			 }
			 res = res.replaceFirst("^0*", "");
			 res = res.replace("/", "月");
			 res = res.replace("-", "至");
			 res = res.replace("°", "度");
			 return res;
		 }
		return text ;
	}
	public static void main(String []args){
		try {
			RawBaiduRecognizer rbr = new RawBaiduRecognizer();
//			while(true){
//				System.out.println("Press any key to continue");
//				System.in.read(new byte[100]);
//
//				System.out.println("start time :" + System.currentTimeMillis());
//				String baiduResult = rbr.getTxtResultByVac();
//				System.out.println("语音识别结果：" + baiduResult);
//				if(baiduResult.equals("SAY AGAIN")){
//					continue;
//				}
//				System.out.println(new Tuling(baiduResult).getResult());
//				System.out.println("end time :" + System.currentTimeMillis());
//				System.out.println("end time :" + System.currentTimeMillis());
//			}
			while(true){
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in ));
				System.out.println(new Tuling(br.readLine()).getResult());// 1.聊天类
			}
//			System.out.println(new Tuling("你能陪我去跑步吗").getResult());// 1.聊天类
//			System.out.println(new Tuling("星期日是几号").getResult());// 2.日期查询
//			System.out.println(new Tuling("帮忙查一下快递：880894304303103401").getResult());// 3.快递类
//			System.out.println(new Tuling("西安今天的新闻").getResult());// 4.新闻类
//			System.out.println(new Tuling("西安今天的天气").getResult());// 5.天气类
//			System.out.println(new Tuling("白头发能拔吗").getResult());// 6.生活百科类
//			System.out.println(new Tuling("西安今天的新闻").getResult());// 7.飞机票类列车类
//			System.out.println(new Tuling("伊娃和瓦力").getResult());// 8.知识库类
//			System.out.println(new Tuling("西安今天的新闻").getResult());// 9.计算器
//			System.out.println(new Tuling("讲一个冷笑话").getResult());// 10.笑话类
//			System.out.println(new Tuling("讲一个故事").getResult());// 11.故事类
//			System.out.println(new Tuling("成语接龙").getResult());// 12.成语接龙
//			System.out.println(new Tuling("重庆鸡公煲怎么做").getResult());// 13.菜谱类
//			System.out.println(new Tuling("2015年9月10号是什么星座").getResult());// 14.星座
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
