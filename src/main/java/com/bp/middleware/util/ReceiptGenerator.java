package com.bp.middleware.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.transactionamountmodel.TransactionAmountModel;
import com.bp.middleware.user.EntityModel;

@Component
public class ReceiptGenerator {

	@Autowired
	FileUtils fu;
	
	public  String prepaidRecieptGeneratorCgstSgst(Path con, TransactionDto transaction, PrepaidPayment prepaid,
			EntityModel entity, String receiptNo, String formattedDateTime) {

		try {

			TemplateEngine templateEngine = new TemplateEngine();

			ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
			templateResolver.setPrefix("template-engines/");
			templateResolver.setSuffix(".html");
			templateResolver.setTemplateMode("HTML");
			templateResolver.setCacheable(false);

			templateEngine.setTemplateResolver(templateResolver);
			
			
			String paymentMode = transaction.getModeOfPaymentPg() !=null ? transaction.getModeOfPaymentPg().getModeOfPayment():"N/A";
			String name = transaction.getEntity().getName();
			double exclusiveAmout = fu.twoDecimelDouble(transaction.getExclusiveAmount());
			double sgst = transaction.getSgstAmount();
			double cgst =  transaction.getCgstAmount();
			double igst = cgst + sgst;
			double convenienceFee = 0;
			double convCgst = 0;
			double convSgst = 0;
			
			String amountInWords = AmountToWords.convertAmountToWords(exclusiveAmout);
			
			
			if(transaction.getTransactionAmountModel() !=null) {
				
				TransactionAmountModel amountModel  = transaction.getTransactionAmountModel();
				
				exclusiveAmout = fu.twoDecimelDouble(amountModel.getExclusiveAmount());
				sgst = fu.twoDecimelDouble(amountModel.getSgst());
				cgst = fu.twoDecimelDouble(amountModel.getCgst());
				igst = fu.twoDecimelDouble(amountModel.getIgst());
				convenienceFee = fu.twoDecimelDouble(amountModel.getConvenianceFee()+amountModel.getFixedFee());
				convCgst = fu.twoDecimelDouble(amountModel.getOtherGst()/2);
				convSgst = fu.twoDecimelDouble(amountModel.getOtherGst()/2);
				
			}

			// Create a context with dynamic values
			
			Context context = new Context();
			
			context.setVariable("receiptNo", receiptNo);
			context.setVariable("invoiceNumber", transaction.getInvoiceNumber());
			context.setVariable("receiptIssuanceDate", LocalDate.now());
			context.setVariable("transactionType", "Prepaid");

			if (transaction.getBankReference() != null) {
				context.setVariable("transactionRefefrenceNo", transaction.getBankReference());
			} else {
				context.setVariable("transactionRefefrenceNo", "N/A");
			}
			
			context.setVariable("paymentType", paymentMode);
			context.setVariable("paymentDate", formattedDateTime);
			context.setVariable("paidBy", name);
			context.setVariable("amount", exclusiveAmout);
			context.setVariable("totalAmountInWords",amountInWords +" only");
//			context.setVariable("sgst", sgst);
//			context.setVariable("cgst", cgst);
//			context.setVariable("convenienceFee", convenienceFee);
//			context.setVariable("convSgst", convSgst);
//			context.setVariable("convCgst", convCgst);
//			context.setVariable("totalAmount", transaction.getPaidAmount());

			String html = templateEngine.process("prepaidreciept-cgst.html", context);

			String generatedString = FileUtils.getReceiptName(8) + ".pdf";

			File fileName = new File(con + File.separator + "/receipt/" + StringUtils.cleanPath(generatedString));

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(fileName);

				ITextRenderer renderer = new ITextRenderer();
				renderer.setDocumentFromString(html);
				renderer.layout();
				renderer.createPDF(fos, true);

			} finally {
				if (fos != null) {
					try {
						fos.close();

					} catch (IOException e) {
					}
				}
			}

			return generatedString;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	
	
//	public static String prepaidRecieptGeneratorIgst(Path con, TransactionDto transaction, PrepaidPayment prepaid,
//			EntityModel entity, String receiptNo, String formattedDateTime) {
//
//		try {
//
//			TemplateEngine templateEngine = new TemplateEngine();
//
//			ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
//			templateResolver.setPrefix("template-engines/");
//			templateResolver.setSuffix(".html");
//			templateResolver.setTemplateMode("HTML");
//			templateResolver.setCacheable(false);
//
//			templateEngine.setTemplateResolver(templateResolver);
//			
//			String paymentMode = transaction.getModeOfPaymentPg() !=null ? transaction.getModeOfPaymentPg().getModeOfPayment():"N/A";
//			String name = transaction.getEntity().getName();
//			double exclusiveAmout = transaction.getExclusiveAmount();
//			double sgst = transaction.getSgstAmount();
//			double cgst =  transaction.getCgstAmount();
//			double igst = cgst + sgst;
//			double convenienceFee = 0;
//			double convIgst = 0;
//			
//			String amountInWords = AmountToWords.convertAmountToWords(exclusiveAmout);
//			
//			if(transaction.getTransactionAmountModel() !=null) {
//				
//				TransactionAmountModel amountModel  = transaction.getTransactionAmountModel();
//				
//				exclusiveAmout = amountModel.getExclusiveAmount();
//				sgst = amountModel.getSgst();
//				cgst = amountModel.getCgst();
//				igst = amountModel.getIgst();
//				convenienceFee = amountModel.getConvenianceFee()+amountModel.getFixedFee();
//				convIgst = amountModel.getOtherGst();
//				
//			}
//			
//
//			// Create a context with dynamic values
//			Context context = new Context();
//			context.setVariable("receiptNo", receiptNo);
//			context.setVariable("invoiceNumber", transaction.getInvoiceNumber());
//			context.setVariable("receiptIssuanceDate", LocalDate.now());
//			context.setVariable("transactionType", "Prepaid");
//			context.setVariable("amountInWord",amountInWords);
//
//			if (transaction.getOrderReference() != null) {
//				context.setVariable("transactionRefefrenceNo", transaction.getOrderReference());
//			} else {
//				context.setVariable("transactionRefefrenceNo", "N/A");
//			}
//			
//			context.setVariable("paymentType", paymentMode);
//			context.setVariable("paymentDate", formattedDateTime);
//			context.setVariable("paidBy", name);
//			context.setVariable("amount", exclusiveAmout);
////			context.setVariable("igst", igst);
////			context.setVariable("convenienceFee", convenienceFee);
////			context.setVariable("convIgst", convIgst);
////			context.setVariable("totalAmount", transaction.getPaidAmount());
//
//			String html = templateEngine.process("prepaidreciept-cgst.html", context);// prepaidreciept-Igst.html
//
//			String generatedString = FileUtils.getReceiptName(8) + ".pdf";
//
//			File fileName = new File(con + File.separator + "/receipt/" + StringUtils.cleanPath(generatedString));
//
//			FileOutputStream fos = null;
//			try {
//				fos = new FileOutputStream(fileName);
//
//				ITextRenderer renderer = new ITextRenderer();
//				renderer.setDocumentFromString(html);
//				renderer.layout();
//				renderer.createPDF(fos, true);
//
//			} finally {
//				if (fos != null) {
//					try {
//						fos.close();
//
//					} catch (IOException e) {
//					}
//				}
//			}
//
//			return generatedString;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return null;
//		}
//	}
	
	
	

	public  String postpaidRecieptGeneratorCgstSgst(Path con, TransactionDto transaction, PostpaidPayment postpaid,
			EntityModel entity, String receiptNo, String formattedDateTime) {

		try {

			TemplateEngine templateEngine = new TemplateEngine();

			ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
			templateResolver.setPrefix("template-engines/");
			templateResolver.setSuffix(".html");
			templateResolver.setTemplateMode("HTML");
			templateResolver.setCacheable(false);

			templateEngine.setTemplateResolver(templateResolver);
			
			String paymentMode = transaction.getModeOfPaymentPg() !=null ? transaction.getModeOfPaymentPg().getModeOfPayment():"N/A";
			String name = transaction.getEntity().getName();
			double exclusiveAmout = fu.twoDecimelDouble(transaction.getExclusiveAmount());
			double sgst = fu.twoDecimelDouble(transaction.getSgstAmount());
			double cgst =  fu.twoDecimelDouble(transaction.getCgstAmount());
			double igst = fu.twoDecimelDouble(cgst + sgst);
			double convenienceFee = 0;
			double convCgst = 0;
			double convSgst = 0;
			
			
			if(transaction.getTransactionAmountModel() !=null) {
				
				TransactionAmountModel amountModel  = transaction.getTransactionAmountModel();
				
				exclusiveAmout = amountModel.getExclusiveAmount();
				sgst = amountModel.getSgst();
				cgst = amountModel.getCgst();
				igst = amountModel.getIgst();
				convenienceFee = fu.twoDecimelDouble(amountModel.getConvenianceFee()+amountModel.getFixedFee());
				convCgst = fu.twoDecimelDouble(amountModel.getOtherGst()/2);
				convSgst = fu.twoDecimelDouble(amountModel.getOtherGst()/2);
				
			}
			
			String amountInWords = AmountToWords.convertAmountToWords(fu.twoDecimelDouble(transaction.getPaidAmount()));

			// Create a context with dynamic values
			
			Context context = new Context();
			
			context.setVariable("receiptNo", receiptNo);
			context.setVariable("invoiceNumber", transaction.getInvoiceNumber());
			context.setVariable("receiptIssuanceDate", LocalDate.now());
			context.setVariable("transactionType", "Postpaid");

			if (transaction.getBankReference() != null) {
				context.setVariable("transactionRefefrenceNo", transaction.getBankReference());
			} else {
				context.setVariable("transactionRefefrenceNo", "N/A");
			}
			
			context.setVariable("paymentType", paymentMode);
			context.setVariable("paymentDate", formattedDateTime);
			context.setVariable("paidBy", name);
			context.setVariable("amount", exclusiveAmout);
			context.setVariable("sgst", sgst);
			context.setVariable("cgst", cgst);
			context.setVariable("convenienceFee", convenienceFee);
			context.setVariable("convSgst", convSgst);
			context.setVariable("convCgst", convCgst);
			context.setVariable("totalAmount", fu.twoDecimelDouble(transaction.getPaidAmount()));
			context.setVariable("totalAmountInWords", amountInWords+" only");

			String html = templateEngine.process("postpaidreciept-cgst.html", context);

			String generatedString = FileUtils.getReceiptName(8) + ".pdf";

			File fileName = new File(con + File.separator + "/receipt/" + StringUtils.cleanPath(generatedString));

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(fileName);

				ITextRenderer renderer = new ITextRenderer();
				renderer.setDocumentFromString(html);
				renderer.layout();
				renderer.createPDF(fos, true);

			} finally {
				if (fos != null) {
					try {
						fos.close();

					} catch (IOException e) {
					}
				}
			}

			return generatedString;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	

	public  String postpaidRecieptGeneratorIgst(Path con, TransactionDto transaction, PostpaidPayment postpaid,
			EntityModel entity, String receiptNo, String formattedDateTime) {

		try {

			TemplateEngine templateEngine = new TemplateEngine();

			ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
			templateResolver.setPrefix("template-engines/");
			templateResolver.setSuffix(".html");
			templateResolver.setTemplateMode("HTML");
			templateResolver.setCacheable(false);

			templateEngine.setTemplateResolver(templateResolver);
			
			String paymentMode = transaction.getModeOfPaymentPg() !=null ? transaction.getModeOfPaymentPg().getModeOfPayment():"N/A";
			String name = transaction.getEntity().getName();
			double exclusiveAmout = fu.twoDecimelDouble(transaction.getExclusiveAmount());
			double sgst = fu.twoDecimelDouble(transaction.getSgstAmount());
			double cgst =  fu.twoDecimelDouble(transaction.getCgstAmount());
			double igst = fu.twoDecimelDouble(cgst + sgst);
			double convenienceFee = 0;
			double convIgst = 0;
			
			if(transaction.getTransactionAmountModel() !=null) {
				
				TransactionAmountModel amountModel  = transaction.getTransactionAmountModel();
				
				exclusiveAmout = amountModel.getExclusiveAmount();
				sgst = amountModel.getSgst();
				cgst = amountModel.getCgst();
				igst = amountModel.getIgst();
				convenienceFee = fu.twoDecimelDouble(amountModel.getConvenianceFee()+amountModel.getFixedFee());
				convIgst = fu.twoDecimelDouble(amountModel.getOtherGst());
				
			}
			
			String amountInWords = AmountToWords.convertAmountToWords(fu.twoDecimelDouble(transaction.getPaidAmount()));

			// Create a context with dynamic values
			Context context = new Context();
			context.setVariable("receiptNo", receiptNo);
			context.setVariable("invoiceNumber", transaction.getInvoiceNumber());
			context.setVariable("receiptIssuanceDate", LocalDate.now());
			context.setVariable("transactionType", "Postpaid");

			if (transaction.getBankReference() != null) {
				context.setVariable("transactionRefefrenceNo", transaction.getBankReference());
			} else {
				context.setVariable("transactionRefefrenceNo", "N/A");
			}
			
			context.setVariable("paymentType", paymentMode);
			context.setVariable("paymentDate", formattedDateTime);
			context.setVariable("paidBy", name);
			context.setVariable("amount", exclusiveAmout);
			context.setVariable("igst", igst);
			context.setVariable("convenienceFee", convenienceFee);
			context.setVariable("convIgst", convIgst);
			context.setVariable("totalAmount", transaction.getPaidAmount());
			context.setVariable("totalAmountInWords", amountInWords+" only");

			String html = templateEngine.process("postpaidreciept-Igst.html", context);

			String generatedString = FileUtils.getReceiptName(8) + ".pdf";

			File fileName = new File(con + File.separator + "/receipt/" + StringUtils.cleanPath(generatedString));

			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(fileName);

				ITextRenderer renderer = new ITextRenderer();
				renderer.setDocumentFromString(html);
				renderer.layout();
				renderer.createPDF(fos, true);

			} finally {
				if (fos != null) {
					try {
						fos.close();

					} catch (IOException e) {
					}
				}
			}

			return generatedString;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
