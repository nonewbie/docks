package MicroSoft;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;

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
	public double age;
	public String gender;
	private String path;
	public FaceDetactor(String path){
		this.path = path;
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
				JSONObject faceAttributes = new JSONArray(
						EntityUtils.toString(entity)).getJSONObject(0)
						.getJSONObject("faceAttributes");
				this.age = faceAttributes.getDouble("age");
				this.gender = faceAttributes.getString("gender");
				System.out.println("gender: " + this.gender);
				System.out.println("age: " + this.age);
				System.out.println("end time :" + System.currentTimeMillis());
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	public static void main(String[] args) {
		try {
			FaceDetactor fd = new FaceDetactor("d:/audio/detection1.jpg");
			fd.getRes();
//			//FaceDetactor.fileRecv();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	private static String fileRecv() {
		byte[] inputByte = null;
		int length = 0;
		DataInputStream dis = null;
		FileOutputStream fos = null;
		Socket soc = null;
		String filePath = "d:/audio/" + System.currentTimeMillis() + ".png";
		try {
			try {
				ServerSocket ss = new ServerSocket(8000);
				soc = ss.accept();
				System.out.println("connect to the python");
				ss.close();
				dis = new DataInputStream(soc.getInputStream());
				File f = new File("d:/audio/","wb");
				if (!f.exists()) {
					f.mkdir();
				}
				/*
				 * 文件存储位置
				 */
				fos = new FileOutputStream(new File(filePath));
				inputByte = new byte[1024];
				//System.out.println("开始接收数据...");
				while ((length = dis.read(inputByte, 0, inputByte.length)) > 0) {
					fos.write(inputByte, 0, length);
					fos.flush();
				}
				//System.out.println("完成接收：" + filePath);
			} finally {
				if (fos != null)
					fos.close();
				if (dis != null)
					dis.close();
				if (soc != null)
					soc.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filePath;
	}

}
