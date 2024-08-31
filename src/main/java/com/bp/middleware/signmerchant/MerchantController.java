package com.bp.middleware.signmerchant;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bp.middleware.responsestructure.ResponseStructure;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AppConstants;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/merchant")
@CrossOrigin(origins = {AppConstants.CROSS_ORIGIN})
public class MerchantController {
	
	@Autowired
	private MerchantService service;
	
	@PostMapping("/insert")
	public ResponseStructure addMerchantDetails(@RequestBody RequestModel model) throws Exception {
		return service.addMerchantDetails(model);
	}
	
	@PostMapping("/insertall")
	public ResponseStructure addMerchantAll(@RequestBody List<RequestModel> model) {
		return service.insertAllMerchant(model);
	}
	
	@GetMapping("/viewpicture/{merchantId}")
	public ResponseEntity<Resource> getMerchantPdf(@PathVariable("merchantId") int merchantId, HttpServletRequest request){
		return service.viewImage(merchantId, request);
		
	}
	@PostMapping("/uploadpdf")
	public ResponseStructure uploadLogo(@RequestParam("pdfDocument") MultipartFile pdfDocument,@RequestParam("documentTitle") String documentTitle,
			@RequestParam("description") String description,@RequestParam("adminId") int userId) throws ParseException {
		return service.uploadMerchantAgreement(pdfDocument,documentTitle,description,userId);
	}
	
	@GetMapping("/viewpdf/{merchantId}")
	public ResponseEntity<Resource> getKycImage(@PathVariable("merchantId") int merchantId, HttpServletRequest request){
		return service.viewPdf(merchantId, request);
	
	}
	
	@GetMapping("/viewall")
	public ResponseStructure getAllMerchants() {
		return service.viewAllMerchants();
	}
	
	@GetMapping("/list")
	public ResponseStructure listall() {
		return service.listAll();
	}
	
	@GetMapping("/listbyspecificmerchant/{userId}")
	public ResponseStructure listBySpecificMerchant(@PathVariable("userId")int userId) {
		return service.listBySpecificMerchant(userId);
	}
	
	
	
	@PutMapping("/updateexpiry/{merchantId}")
	public ResponseStructure updateExpiryDays(@PathVariable("merchantId") int merchantId,@RequestBody RequestModel model) {
		return service.updateExpiryDate(merchantId,model);
	}
	
	
	
	@GetMapping("/makeWholeDocExpire/{merchantId}")
	public ResponseStructure makeWholeDocExpire(@PathVariable("merchantId") int merchantId) {
		return service.makeWholeDocExpire(merchantId);
	}
	
	
	
	@GetMapping("/viewbyid/{merchantId}")
	public ResponseStructure viewByMerchantId(@PathVariable("merchantId") int merchantId) {
		return service.viewByMerchantId(merchantId);
	}
	
	@GetMapping("/path")
	public String getPathName() throws IOException, ParseException {
		return service.getPathName();
	}
	
	
	@GetMapping("/viewbyuserid/{userId}")
	public ResponseStructure viewMerchantByUserId(@PathVariable("userId") int userId) {
		return service.viewMerchantByUserId(userId);
	}
	
	@GetMapping("/viewbybondid/{bondId}")
	public ResponseStructure viewMerchantByBondId(@PathVariable("bondId") int bondId) {
		return service.viewMerchantByBondId(bondId);
	}
	
	@PostMapping("/bulkupload/{userId}")
	public ResponseStructure bulkUpload(@PathVariable("userId") int userId,@RequestBody List<SignerDto> modelList) {  
		return service.bulkUpload(modelList,userId);
	}
	
	@GetMapping("/mime")
	public void image(@RequestParam("image") MultipartFile image) {
		byte[] byteString;
		try {
			byteString = image.getBytes();
			StringBuilder hasString=new StringBuilder();
			for (byte b : byteString) {
				hasString.append(String.format("%02X", b));
			}
			System.err.println("Byte Image :"+hasString.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String type=image.getContentType();
		System.out.println("MimeType :"+type);
	}
	
	
	
	
	
	
	
	
	@PostMapping("/get")
	public String addSignatureToPdf() throws IOException {
		//1.
//		float x = 5.0f;
//	    float y = 10.0f;
		//2.
//		float x = 152.0f;
//	    float y = 10.0f;
	    //3.
//		float x = 299.0f;
//	    float y = 10.0f;
		//4.
		float x = 446.0f;
	    float y = 10.0f;
		
	    String name = "Saravanan Chandrasakaran";
	    String company="Saravanan Chandrasakaran";

	    // Get the current date and time in the desired format
	    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    String currentDateTime = dateFormat.format(new Date());
	    String concat=currentDateTime;

	    // Create an image from the input data (name and date-time)
	    BufferedImage signatureImage = createSignatureImage(name, concat,company);

	    String originalPdfPath = "C:\\Users\\Basispay\\Documents\\SpringBootProjects\\mercant_esign\\src\\main\\webapp\\WEB-INF\\merchantpdf\\SB_application_Form.pdf";

	    try (PDDocument document = PDDocument.load(new File(originalPdfPath))) {
	        for (PDPage page : document.getPages()) {
	            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true)) {
	                // Convert the BufferedImage to a PDImageXObject
	                PDImageXObject imageXObject = LosslessFactory.createFromImage(document, signatureImage);

	                // Set the position (x, y) for your image
	                contentStream.drawImage(imageXObject, x, y);

	                // You can also add text if needed
	                contentStream.setFont(PDType1Font.TIMES_ITALIC, 8);
	                contentStream.beginText();
	                contentStream.newLineAtOffset(x, y - 30);
	                contentStream.showText(name);
	                contentStream.endText();
	            }
	        }

	        document.save(originalPdfPath); // Override the original PDF
	    } catch (IllegalArgumentException e) {
	        // Handle the exception, or log it for debugging
	        e.printStackTrace();
	    }
	    return "Done";
	}

// Create an image from text data with watermark, underline for date-time, and custom font for the human name
	private BufferedImage createSignatureImage(String name, String dateTime, String humanName) {
	    int imageWidth = 142;
	    int imageHeight = 42;

	    BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D graphics = image.createGraphics();

	    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

	    // Load the watermark image
	    try {
	        BufferedImage watermark = ImageIO.read(new File("C:\\Users\\Basispay\\Documents\\SpringBootProjects\\mercant_esign\\src\\main\\webapp\\WEB-INF\\merchantpdf\\watermark.jpg")); // Replace with the path to your watermark image
	        graphics.drawImage(watermark, 0, 0, null);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    // Font settings for the human name
	    Font humanNameFont = new Font("Gabriola", Font.BOLD, 13); // Replace "CustomFont" with your desired font
	    graphics.setFont(humanNameFont);
	    int humanNameX = 5;
	    int humanNameY = 15;
	    graphics.setColor(Color.BLUE); // Set the color for the human name
	    graphics.drawString(humanName, humanNameX, humanNameY);
	    
	    // Font settings for the name
	    Font nameFont = new Font("Arial", Font.BOLD, 8);
	    Map<TextAttribute, Object> attributes = new HashMap<>();
	    attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
	    Font underlinedName = nameFont.deriveFont(attributes);
	    graphics.setFont(underlinedName);
	    graphics.setColor(Color.BLACK);
	    int nameX = 5;
	    int nameY = 28;
	    graphics.drawString(name, nameX, nameY);

	    // Font settings for the date-time with underline
	    Font dateTimeFont = new Font("Arial", Font.ITALIC, 8);
	    
	    graphics.setFont(dateTimeFont);
	    int dateTimeY = 38;
	    graphics.drawString(dateTime, nameX, dateTimeY);
	    
	    String sign="eSign by";

	    Font signBy = new Font("Arial", Font.BOLD, 7); // Replace "CustomFont" with your desired font
	    graphics.setFont(signBy);
	    int signX = 110;
	    int signY = 30;
	    graphics.setColor(Color.DARK_GRAY); // Set the color for the human name
	    graphics.drawString(sign, signX, signY);
	    
	    String signCompany="Basispay";

	    Font signedCompany = new Font("Arial", Font.BOLD, 7); // Replace "CustomFont" with your desired font
	    graphics.setFont(signedCompany);
	    int signedX = 108;
	    int signedY = 40;
	    graphics.setColor(Color.DARK_GRAY); // Set the color for the human name
	    graphics.drawString(signCompany, signedX, signedY);

	    graphics.dispose();
	    return image;
	}
	
	
//	@PutMapping("/update/{merchantId}")
//	public ResponseStructure updateMerchant(@PathVariable("merchantId") int merchantId,@RequestBody RequestModel model) {
//		return service.updateMerchant(merchantId,model);
//	}

}
