package com.bp.middleware.technical;

import okhttp3.*;

import java.awt.Image;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.util.AppConstants;
import com.itextpdf.io.codec.Base64.InputStream;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import jakarta.servlet.ServletContext;

@Service
public class ImageUploader {

	private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

	OkHttpClient client = new OkHttpClient();
	
	@Autowired
	ServletContext context;
	
	@Autowired
	ResourceLoader resourceLoader;

	public ResponseStructure uploadImage(MultipartFile imageFile) {
		
		ResponseStructure structure = new ResponseStructure();
		
		try {
		
		String token = AppConstants.SUREPASS_TOKEN;

		String filePath = "C:/Users/DELL/Pictures/Aadhar/SekarAadhar.JPG";
		String fileInProjectPath = "src/main/webapp/WEB-INF/ocrcheck/SekarAadhar.JPG";
		String url = "https://sandbox.surepass.io/api/v1/ocr/aadhaar";
		
		File file = convertMultiartToFile(imageFile);
		
		RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("image", "image.png", RequestBody.create(MEDIA_TYPE_PNG, file )).build();

		// Creating headers
		Headers headers = new Headers.Builder().add("Authorization",token)
				.add("Content-Type", "multipart/form-data") // Or adjust content type as per requirement
				.build();

		Request request = new Request.Builder().url(url).headers(headers) // Adding headers to the request
				.post(requestBody).build();

		client.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				e.printStackTrace();
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (!response.isSuccessful()) {
					throw new IOException("Unexpected code " + response);
				} else {
					// Handle successful response
					System.out.println(response.body().string());
				}
			}


		});
		
		structure.setData("CHECK CONSOLE !");
		
		}catch(Exception e) {
			
			e.printStackTrace();
			structure.setData("SOMETHINGS WRONG !!");
		}
		
		return structure;
	}

	private File convertMultiartToFile(MultipartFile imageFile) throws Exception{
	
		    File convFile = new File( imageFile.getOriginalFilename() );
		    FileOutputStream fos = new FileOutputStream( convFile );
		    fos.write( imageFile.getBytes() );
		    fos.close();
		    return convFile;
	}

	
	
	
	
	public ResponseStructure OcrUniRest(MultipartFile image) {


		ResponseStructure structure = new ResponseStructure();
		
		try {
			
			String token = AppConstants.SUREPASS_TOKEN;
			
			String url = "https://sandbox.surepass.io/api/v1/ocr/pan";
			
			
//			final Resource resource = resourceLoader
//					.getResource("/WEB-INF/ocrcheck/" + "Pan.JPG");
//			File absPath = resource.getFile();
			
			File absPath = convertMultiartToFile(image);
			//Image image = ImageIO.read(absPath);
			
			//InputStream stream = context.getResourceAsStream("");
			
			if(image != null) {
				
				HttpResponse<String> response = Unirest.post(url).header("Authorization",token).field("file", absPath).asString();
				
				System.out.println("RESP STATUS : "+response.getStatus());
				System.out.println("RESP BODY : "+response.getBody());
				
				structure.setData("Success");
				
			}else {
				
				structure.setMessage("Image not fetched");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			structure.setData("Error");
		}
		
		return structure;
	}
}
