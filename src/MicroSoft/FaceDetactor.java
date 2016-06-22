package MicroSoft;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class FaceDetactor {
	private double age;
	private String gender;
	private String path;
	public FaceDetactor(String path){
		this.path = path;
		this.gender = ("NULL");
		this.age = 0;
	}
	public double getAge(){
		return this.age;
	}
	public String getGender(){
		return this.gender;
	}
	public void getRes() {
		// TODO Auto-generated constructor stub
		HttpClient httpclient = HttpClients.createDefault();
		System.out.println("start time :" + System.currentTimeMillis());
		try {
			URIBuilder builder = new URIBuilder(
					"https://api.projectoxford.ai/face/v1.0/detect");
			builder.setParameter("returnFaceId", "true");
			builder.setParameter("returnFaceLandmarks", "false");
			builder.setParameter("returnFaceAttributes", "age,gender");// age,gender,headPose,smile,facialHair,glasses

			URI uri = builder.build();
			HttpPost request = new HttpPost(uri);
			// request.setHeader("Content-Type", "application/json");
			request.setHeader("Content-Type", "application/octet-stream");
			request.setHeader("Ocp-Apim-Subscription-Key",
					"2f7df6536a134babb51be5e3145f4002");
			FileEntity reqEntity = new FileEntity(new File(this.path),
					ContentType.APPLICATION_FORM_URLENCODED);
			request.setEntity(reqEntity);
			HttpResponse response = httpclient.execute(request);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				//System.out.println(EntityUtils.toString(entity));
				JSONObject faceAttributes = new JSONArray(
						EntityUtils.toString(entity)).getJSONObject(0)
						.getJSONObject("faceAttributes");
				this.age = faceAttributes.getDouble("age");
				this.gender = faceAttributes.getString("gender");
				System.out.println("gender: " + this.gender);
				System.out.println("age: " + this.age);
				System.out.println("end time :" + System.currentTimeMillis());
			}
		} catch (org.json.JSONException e) {//由于没有拍到脸
			//e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		try {
			FaceDetactor fd = new FaceDetactor("D:\\VMshare\\audio\\face0.png");//d:/audio/detection1.jpg
			fd.getRes();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
}
