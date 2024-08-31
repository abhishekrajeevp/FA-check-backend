package com.bp.middleware.google;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.PrivateKeySignature;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Calendar;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;


@RestController
@RequestMapping("/pdf")
public class PdfEsignTest {

	
	 @PostMapping("/itext")
	 public  String pdfIText(){
        try {
        	System.err.println("S T A R T");
            // Load the PDF document you want to sign
            PdfReader reader = new PdfReader("C:\\Users\\DELL\\Desktop\\sample images\\sample.pdf");
            
            // Create a signature appearance
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream("C:\\Users\\DELL\\Desktop\\sample images\\aaa.pdf"));
            PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
            
            // Set signature appearance details (optional)
            appearance.setReason("Middleware confirmation");
            appearance.setLocation("India");
            
            // Load your keystore with private key and certificate
            String keystorePath = "your_keystore.p12"; // Update with your keystore path
            String keystorePassword = "your_keystore_password"; // Update with your keystore password
            
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
            
            String alias = ""; // Update with the alias of your certificate
            PrivateKey privateKey = (PrivateKey) keystore.getKey(alias, keystorePassword.toCharArray());
            Certificate[] chain = keystore.getCertificateChain(alias);
            
            // Create a digital signature
            ExternalSignature pks = new PrivateKeySignature(privateKey, "SHA-256", BouncyCastleProvider.PROVIDER_NAME);
            ExternalDigest digest = new BouncyCastleDigest();
            MakeSignature.signDetached(appearance, digest, pks, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);
            
            stamper.close();
            
            System.out.println("PDF digitally signed successfully!");
            
            return "Done";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        
    }
	 
	 
	 @PostMapping("/pdfbox")
	 public String apdachePdfBox() {
		 
		 try {
	            // Load the PDF document you want to sign
	            PDDocument document = PDDocument.load(new File("unsigned_example.pdf"));

	            // Create a signature field
	           // PDSignatureField signatureField = new PDSignatureField();

	            // Create a signature
	            PDSignature signature = new PDSignature();
	            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
	            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
	            signature.setName("MySignature");
	            signature.setReason("Digital Signature Reason");
	            signature.setLocation("Signature Location");

	            // Set the signature field's rectangle
	            PDPage firstPage = document.getPages().get(0);
	            PDRectangle rect = new PDRectangle();
	            rect.setLowerLeftX(100);
	            rect.setLowerLeftY(100);
	            rect.setUpperRightX(200);
	            rect.setUpperRightY(150);
	           // signatureField.getWidgets().get(0).setRectangle(rect);

	            // Add the signature field to the document
	           // document.getDocumentCatalog().getAcroForm().getFields().add(signatureField);

	            // Prepare the signature
	            List<PDSignature> signatureList = new ArrayList<>();
	            signatureList.add(signature);
	           // document.addSignature(signatureList);

	            // Save the signed PDF
	            document.save("signed_example.pdf");
	            document.close();

	            System.out.println("PDF digitally signed successfully!");
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

		 return null;
	 }

}
