package com.bp.middleware.invoiceupdate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.bp.middleware.admin.AdminServiceImplementation;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidRepository;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.user.EntityModel;
import com.bp.middleware.user.RequestModel;
import com.bp.middleware.util.AmountToWords;
import com.bp.middleware.util.FileUtils;

import jakarta.servlet.ServletContext;

@Component
public class PrepaidInvoiceEdit {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrepaidInvoiceEdit.class);

	@Autowired
	private PrepaidRepository prepaidRepository;
	@Autowired
	private FileUtils fu;

	public String updatePrepaidCgstInvoice(TransactionDto transactionDto, PrepaidPayment prepaid, RequestModel model,
			Path con) throws Exception {

//		String invoiceNumber = transactionDto.getInvoiceNumber();
//		LocalDate paidDate = prepaid.getPaidDate();
//		LocalDate fromDate = prepaid.getPaidDate();
//		LocalDate generatedDate = prepaid.getInvoiceGeneratedDate();
//		LocalDate toDate = prepaid.getInvoiceGeneratedDate();
//		String month = prepaid.getInvoiceGeneratedDate().getMonth().toString();
//		int totalHits = prepaid.getTotalHits();
//		double walletBalance = prepaid.getWalletBalance();
//		double usedAmount = prepaid.getUsedAmount();
//		double cgst = usedAmount==0?0:usedAmount*9/100;
//		double sgst = usedAmount==0?0:usedAmount*9/100;
//		double grandTotal = usedAmount+cgst+sgst;
//		String entityName = entity.getName();
//		String city = entity.getCityName();
//		String state = entity.getStateName();
//		String pincode = entity.getPincode();
//		String country = entity.getCountryName();
//		String gstNo = entity.getGst();

		String generatedIn = "";
		
		String invoiceNumber = transactionDto.getInvoiceNumber();
		String fromDate = model.getFromDate();
		LocalDate generatedDate = model.getGeneratedDate();
		String toDate = model.getToDate();
		String month = model.getMonthName();
		int totalHits = model.getTotalHits();
		double usedAmount = model.getUsedAmount();
		
		double cgst = model.getUsedAmount()*9/100;
		double sgst = model.getUsedAmount()*9/100;
		
		double grandTotal = fu.twoDecimelDouble(usedAmount+cgst+sgst);
		
		String entityName = model.getName();
		String city = model.getCityName();
		String state = model.getStateName();
		String pincode = model.getPincode();
		String country = model.getCountryName();
		String gstNo = model.getGst();
		String bankAccount = model.getBankAccountNumber();
		String hsnCode = model.getHsnNumber();
		String url = "http://157.245.105.135:5030/prepaid-by-month/"+prepaid.getEntityModel().getUserId();
		
		int amount = (int)grandTotal;
		
		String amountInWord = AmountToWords.convertAmountToWords(amount)+ " only";
		
		String html = "<!DOCTYPE html>\r\n" + "<html lang=\"en\">\r\n" + "\r\n" + "<head>\r\n"
				+ "<title>Invoice</title>\r\n"

				+ "<style type=\"text/css\">" + "  body {\r\n" + "      margin: 0;\r\n" + "      padding: 0;\r\n"
				+ "      background: #c0c0c0;\r\n" + "    }" + " div,\r\n" + "    p,\r\n" + "    a,\r\n"
				+ "    li,\r\n" + "    td {\r\n" + "      -webkit-text-size-adjust: none;\r\n" + "    }"
				+ " .ReadMsgBody {\r\n" + "      width: 100%;\r\n" + "      background-color: #636363;\r\n"
				+ "    }\r\n" + "\r\n" + "    .ExternalClass {\r\n" + "      width: 100%;\r\n"
				+ "      background-color: #a09797;\r\n" + "    }\r\n" + "\r\n" + "    body {\r\n"
				+ "      width: 100%;\r\n" + "      height: 100%;\r\n" + "      background-color: #eee5e5;\r\n"
				+ "      margin: 0;\r\n" + "      padding: 0;\r\n"
				+ "      -webkit-font-smoothing: antialiased;\r\n" + "    }\r\n" + "\r\n" + "    html {\r\n"
				+ "      width: 100%;\r\n" + "    }\r\n" + "\r\n" + "    p {\r\n"
				+ "      padding: 0 !important;\r\n" + "      margin-top: 0 !important;\r\n"
				+ "      margin-right: 0 !important;\r\n" + "      margin-bottom: 0 !important;\r\n"
				+ "      margin-left: 0 !important;\r\n" + "    }\r\n" + "\r\n" + "    .visibleMobile {\r\n"
				+ "      display: none;\r\n" + "    }\r\n" + "\r\n" + "    .hiddenMobile {\r\n"
				+ "      display: block;\r\n" + "    }\r\n" + "\r\n"
				+ "    @media only screen and (max-width: 600px) {\r\n" + "      body {\r\n"
				+ "        width: auto !important;\r\n" + "      }\r\n" + "\r\n"
				+ "      table[class=fullTable] {\r\n" + "        width: 96% !important;\r\n"
				+ "        clear: both;\r\n" + "      }\r\n" + "\r\n" + "      table[class=fullPadding] {\r\n"
				+ "        width: 85% !important;\r\n" + "        clear: both;\r\n" + "      }\r\n" + "\r\n"
				+ "      table[class=col] {\r\n" + "        width: 45% !important;\r\n" + "      }\r\n" + "\r\n"
				+ "      .erase {\r\n" + "        display: none;\r\n" + "      }\r\n" + "    }\r\n" + "\r\n"
				+ "    @media only screen and (max-width: 420px) {\r\n" + "      table[class=fullTable] {\r\n"
				+ "        width: 100% !important;\r\n" + "        clear: both;\r\n" + "      }\r\n" + "\r\n"
				+ "      table[class=fullPadding] {\r\n" + "        width: 85% !important;\r\n"
				+ "        clear: both;\r\n" + "      }\r\n" + "\r\n" + "      table[class=col] {\r\n"
				+ "        width: 100% !important;\r\n" + "        clear: both;\r\n" + "      }\r\n" + "\r\n"
				+ "      table[class=col] td {\r\n" + "        text-align: left !important;\r\n" + "      }\r\n"
				+ "\r\n" + "      .erase {\r\n" + "        display: none;\r\n" + "        font-size: 0;\r\n"
				+ "        max-height: 0;\r\n" + "        line-height: 0;\r\n" + "        padding: 0;\r\n"
				+ "      }\r\n" + "\r\n" + "      .visibleMobile {\r\n" + "        display: block !important;\r\n"
				+ "      }\r\n" + "\r\n" + "      .hiddenMobile {\r\n" + "        display: none !important;\r\n"
				+ "      }\r\n" + "    }" + "</style>"

				+ "</head>\r\n" + "\r\n" + "<body>\r\n" + "\r\n" + "\r\n"

				+ "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">\r\n"
				+ "<tr>\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>" + "<tr>" + "<td>"
				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#ffffff\"\r\n"
				+ "style=\"border-radius: 10px 10px 0 0;\">" + "<tr class=\"hiddenMobile\">\r\n"
				+ "<td height=\"40\"></td>\r\n" + "</tr>" + "<tr class=\"visibleMobile\">\r\n"
				+ "<td height=\"30\"></td>\r\n" + "</tr>" + " <tr>\r\n" + "<td>\r\n"
				+ " <table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
				+ "\r\n" + "<tbody>\r\n" + "<tr>\r\n" + "<td>\r\n"
				+ "<table width=\"220\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"left\" class=\"col\">\r\n"
				+ "<tbody>\r\n" + "<tr>\r\n"
				+ "<td align=\"left\"> <img src=\"src/main/resources/logo/fclogo.png\" width=\"125\" height=\"85\" alt=\"logo\" border=\"0\" />\r\n"
				+ "</td>" + "<br></br>" + "</tr>" + "<tr class=\"hiddenMobile\">\r\n"
				+ "<td height=\"40\"></td>\r\n" + "</tr>\r\n" + "<tr class=\"visibleMobile\">\r\n"
				+ "<td height=\"20\"></td>\r\n" + "</tr>" + "</tbody>" + "</table>"

				+ "<table width=\"220\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"right\" class=\"col\">\r\n"
				+ "<tbody>\r\n" + "<tr class=\"visibleMobile\">\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>\r\n"
				+ "<tr>\r\n" + "<td height=\"5\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 26px; color: #E60000; letter-spacing: -3px; font-weight: bold; font-family: 'Open Sans', sans-serif; line-height: 1; vertical-align: top; text-align: right;\">\r\n"
				+ "Baabuji Ventures\r\n" + "</td>\r\n" + "</tr>" + "<tr class=\"hiddenMobile\">\r\n"
				+ "<td height=\"50\"></td>\r\n" + "</tr>\r\n" + "<tr class=\"visibleMobile\">\r\n"
				+ "<td height=\"20\"></td>\r\n" + "</tr>" + "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 15px; color: #5b5b5b; font-family: 'Open Sans', sans-serif; line-height: 18px; vertical-align: top; text-align: right;\">\r\n"
				+ "<small>Invoice No: </small>" + invoiceNumber + "<br />\r\n" + "<small>Generated Date: </small>"
				+ generatedDate + "<br />\r\n"
				
				+ "<small style=\"color: #070707;\">Month: </small> <b style=\"color: #070707;\">"
				+ month + "</b><br />\r\n" + "</td>\r\n" + "</tr>" + "</tbody>\r\n" + "</table>"
				+ "</td>" + "</tr>" + "</tbody>" + " </table>" + "</td>" + " </tr>" + "</table>" + "</td>" + "</tr>"
				+ " </table>"

				+ "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">"
				+ "<tbody>" + "<tr>" + "<td>"
				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#ffffff\">"
				+ "<tbody>" + "<tr class=\"hiddenMobile\">\r\n" + "<td height=\"60\"></td>\r\n" + "</tr>\r\n"
				+ "<tr class=\"visibleMobile\">\r\n" + "<td height=\"40\"></td>\r\n" + "</tr>" + "<tr>" + "<td>"
				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
				+ "<tbody>" + "<tr>\r\n" + "<th\r\n"
				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; font-weight: normal; line-height: 1; vertical-align: top; padding: 0 10px 7px 0;\"\r\n"
				+ "align=\"left\">\r\n" + "<strong>Description</strong>\r\n" + "</th>\r\n" + "<th\r\n"
				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; font-weight: normal; line-height: 1; vertical-align: top; padding: 0 0 7px;\"\r\n"
				+ "align=\"left\">\r\n" + "\r\n" + "</th>\r\n" + "<th\r\n"
				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; font-weight: normal; line-height: 1; vertical-align: top; padding: 0 0 7px;\"\r\n"
				+ "align=\"center\">\r\n" + "<strong>No of Hit</strong>\r\n" + "</th>\r\n" + "<th\r\n"
				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #1e2b33; font-weight: normal; line-height: 1; vertical-align: top; padding: 0 0 7px;\"\r\n"
				+ "align=\"right\">\r\n" + "<strong>Total Amount</strong>\r\n" + "</th>\r\n" + "</tr>\r\n" + "<tr>\r\n"
				+ "<td height=\"1\" style=\"background: #bebebe;\" colspan=\"4\"></td>\r\n" + "</tr>\r\n"
				+ "<tr>\r\n" + "<td height=\"10\" colspan=\"4\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #646a6e;  line-height: 18px;  vertical-align: top; padding:10px 0;\"\r\n"
				+ "class=\"article\">\r\n" + "<b>" + "API KYC Verification services dated from "
				+ fromDate + " to " + toDate+"</b>\r\n" + "</td>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e;  line-height: 18px;  vertical-align: top; padding:10px 0;\">\r\n"
				+ "<b>" + "</b>\r\n" + "</td>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e;  line-height: 18px;  vertical-align: top; padding:10px 0;\"\r\n"
				+ "align=\"center\"><b>" + totalHits + "</b></td>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #1e2b33;  line-height: 18px;  vertical-align: top; padding:10px 0;\"\r\n"
				+ "align=\"right\"><b>" + grandTotal + "</b></td>\r\n" + "</tr>\r\n" + "<tr>\r\n"
				+ "<td height=\"1\" colspan=\"4\" style=\"border-bottom:1px solid #e4e4e4\"></td>\r\n" + "</tr>\r\n"
				+ "\r\n" + "<tr>\r\n"
				+ "<td height=\"1\" colspan=\"4\" style=\"border-bottom:1px solid #e4e4e4\"></td>\r\n" + "</tr>"
				+ "</tbody>" + "</table>" + "</td>" + "</tr>" + " <tr>\r\n" + "<td height=\"20\"></td>\r\n"
				+ "</tr>" + "</tbody>" + "</table>" + "</td>" + "</tr>" + "</tbody>" + "</table>"

				+ "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">\r\n"
				+ "<tbody>" + "<tr>" + "<td>"
				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\"\r\n"
				+ " bgcolor=\"#ffffff\">" + "<tbody>" + "<tr>" + "<td>"
				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
				+ " <tbody>"

				+ " <tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
				+ "Amount used in "+month+"\r\n" + "</td>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; white-space:nowrap;\"\r\n"
				+ "width=\"80\">\r\n" + "" + usedAmount + "\r\n" + "</td>\r\n" + "</tr>"

				+ " <tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
				+ "Cgst (9%)\r\n" + "</td>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
				+ "" + cgst + "\r\n" + "</td>\r\n" + "</tr>"

				+ " <tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
				+ "Sgst (9%)\r\n" + "</td>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; white-space:nowrap;\"\r\n"
				+ "width=\"80\">\r\n" + "" + sgst + "\r\n" + "</td>\r\n" + "</tr>"


				+ "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #000; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
				+ "<strong>" +amountInWord +"</strong>\r\n" + "</td>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #000; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
				+ "<strong>" + grandTotal + "</strong>\r\n" + "</td>\r\n" + "</tr>" + "</tbody>" + "</table>"
				+ "</td>" + "</tr>" + "</tbody>" + "</table>" + "</td>" + "</tr>" + "</tbody>" + "</table>"
				+ "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">\r\n"
				+ "<tbody>" + "<tr>" + "<td>"
				
				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\"\r\n"
				+ "bgcolor=\"#ffffff\">" + "<tbody>" + " <tr class=\"hiddenMobile\">\r\n"
				+ "<td height=\"60\"></td>\r\n" + "</tr>\r\n" + "<tr class=\"visibleMobile\">\r\n"
				+ "<td height=\"40\"></td>\r\n" + "</tr>" + "<tr>" + "<td>"
				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
				+ "<tbody>" + "<tr>" + "<td>"
				+ "<table width=\"290\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"left\" class=\"col\">\r\n"
				+ "<tbody>" + "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 11px; font-family: 'Open Sans', sans-serif; color: #000000; line-height: 1; vertical-align: top; \">\r\n"
				+ "<strong>BILLING INFORMATION</strong>\r\n" + "</td>\r\n" + "</tr>" + " <tr>\r\n"
				+ "<td width=\"100%\" height=\"10\"></td>\r\n" + "</tr>" + "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; line-height: 20px; vertical-align: top; \">\r\n"
				+ "<b>Baabuji Ventures Pvt Ltd</b><br></br>\r\n" + "New No.9,Old No.11,Ground Floor<br> </br>\r\n"
				+ "Palayakaran Street,Kalaimagal Nagar,<br></br>\r\n" + "Ekkatuthangal,Chennai 600032<br></br>\r\n"
				
				+ "<small style=\"color: #000000;\">Gst No :</small><b>33AAFCB2854F2ZY</b><br></br>\r\n" 
				+ "<small style=\"color: #000000;\">Account No :</small><b>"+bankAccount+"</b><br></br>\r\n"
				+ "<small style=\"color: #000000;\">HSN Code :</small><b>"+hsnCode+"</b>\r\n"+ "</td>\r\n"
				
				+ "</tr>" + "</tbody>" + "</table>"
				+ " <table width=\"160\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"right\" class=\"col\">\r\n"
				+ "<tbody>\r\n" + "<tr class=\"visibleMobile\">\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>\r\n"
				+ "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 11px; font-family: 'Open Sans', sans-serif; color: #000000; line-height: 1; vertical-align: top; \">\r\n"
				+ "<strong>RECIPIENT ADDRESS</strong>\r\n" + "</td>\r\n" + "</tr>\r\n" + "<tr>\r\n"
				+ "<td width=\"100%\" height=\"10\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; line-height: 20px; vertical-align: top; \">\r\n"
				+ "<b>" + entityName + "</b> <br></br>\r\n" + "" + city + "-"
				+ pincode + "<br></br>\r\n" + "" + state + "<br></br>"
				+ country + "<br></br>\r\n" + "<small style=\"color: #000000;\">Gst No :</small><b>"
				+ gstNo + "</b>\r\n" + "\r\n" + "\r\n" + "</td>\r\n" + "</tr>\r\n" + "</tbody>\r\n" + "</table>"
				+ "</td>" + "</tr>" + "</tbody>" + "</table>" + "</td>" + "</tr>" + "<tr>" + "<td>"
				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
				+ "<tbody>\r\n" + "<tr>\r\n" + "<td>\r\n"
				+ "<table width=\"470\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"left\" class=\"col\">\r\n"
				+ "<tbody>\r\n" + "<tr class=\"hiddenMobile\">\r\n" + "<td height=\"35\"></td>\r\n" + "</tr>\r\n"
				+ "<tr class=\"visibleMobile\">\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n"
				+ "<td\r\n"
				+ "style=\"font-size: 11px; font-family: 'Open Sans', sans-serif; color: #0e0e0e; line-height: 1; vertical-align: top; \">\r\n"
				+ "<strong>DESCRIPTION</strong>\r\n" + "</td>\r\n" + "</tr>\r\n" + "<tr>\r\n"
				+ "<td width=\"100%\" height=\"10\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; line-height: 20px; vertical-align: top; \">\r\n"
				
				+ "This invoice details the costs of services used this month.<br></br>\r\n"
				
				+ "\r\n" + "</td>\r\n" + "</tr>\r\n"
				
				+ "</tbody>\r\n" + "</table>\r\n" + "</td>\r\n" + "</tr>\r\n" + "</tbody>\r\n" + "</table>"
				+ "</td>" + "</tr>" + "<tr class=\"hiddenMobile\">\r\n" + "<td height=\"60\">\r\n" + "</td>\r\n"
				+ "</tr>\r\n" + "<tr class=\"visibleMobile\">\r\n" + "<td height=\"30\"></td>\r\n" + "</tr>"
				+ "</tbody>" + "</table>" + "</td>" + "</tr>" + "</tbody>" + "</table>"

				+ " <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">\r\n"
				+ "\r\n" + "<tr>\r\n" + "<td>\r\n"
				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#ffffff\"\r\n"
				+ "style=\"border-radius: 0 0 10px 10px;\">\r\n" + "<tr>\r\n" + "<td>\r\n"
				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
				+ "<tbody>\r\n" + "<tr>\r\n"
				
				+ "<td\r\n"
				+ "style=\"font-size: 12px; color: #02ff89; font-family: 'Open Sans', sans-serif; line-height: 18px; vertical-align: top; text-align: left;\">\r\n"
				+ "<a href=\""+url+"\">click here to know more about payment</a>\r\n"
				+ "</td>\r\n"

				+ "</tr>\r\n" + "</tbody>\r\n" + "</table>\r\n" + "</td>\r\n" + "</tr>\r\n"
				+ "<tr class=\"spacer\">\r\n" + "<td height=\"50\"></td>\r\n" + "</tr>\r\n" + "\r\n"
				+ "</table>\r\n" + "</td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td height=\"20\"></td>\r\n"
				+ "</tr>\r\n" + "</table>" + "</body>\r\n" + "\r\n" + "</html>";
		

//		String html = "<!DOCTYPE html>\r\n" + "<html lang=\"en\">\r\n" + "\r\n" + "<head>\r\n"
//				+ "<title>Invoice</title>\r\n"
//
//				+ "<style type=\"text/css\">" + "  body {\r\n" + "      margin: 0;\r\n" + "      padding: 0;\r\n"
//				+ "      background: #c0c0c0;\r\n" + "    }" + " div,\r\n" + "    p,\r\n" + "    a,\r\n" + "    li,\r\n"
//				+ "    td {\r\n" + "      -webkit-text-size-adjust: none;\r\n" + "    }" + " .ReadMsgBody {\r\n"
//				+ "      width: 100%;\r\n" + "      background-color: #636363;\r\n" + "    }\r\n" + "\r\n"
//				+ "    .ExternalClass {\r\n" + "      width: 100%;\r\n" + "      background-color: #a09797;\r\n"
//				+ "    }\r\n" + "\r\n" + "    body {\r\n" + "      width: 100%;\r\n" + "      height: 100%;\r\n"
//				+ "      background-color: #eee5e5;\r\n" + "      margin: 0;\r\n" + "      padding: 0;\r\n"
//				+ "      -webkit-font-smoothing: antialiased;\r\n" + "    }\r\n" + "\r\n" + "    html {\r\n"
//				+ "      width: 100%;\r\n" + "    }\r\n" + "\r\n" + "    p {\r\n" + "      padding: 0 !important;\r\n"
//				+ "      margin-top: 0 !important;\r\n" + "      margin-right: 0 !important;\r\n"
//				+ "      margin-bottom: 0 !important;\r\n" + "      margin-left: 0 !important;\r\n" + "    }\r\n"
//				+ "\r\n" + "    .visibleMobile {\r\n" + "      display: none;\r\n" + "    }\r\n" + "\r\n"
//				+ "    .hiddenMobile {\r\n" + "      display: block;\r\n" + "    }\r\n" + "\r\n"
//				+ "    @media only screen and (max-width: 600px) {\r\n" + "      body {\r\n"
//				+ "        width: auto !important;\r\n" + "      }\r\n" + "\r\n" + "      table[class=fullTable] {\r\n"
//				+ "        width: 96% !important;\r\n" + "        clear: both;\r\n" + "      }\r\n" + "\r\n"
//				+ "      table[class=fullPadding] {\r\n" + "        width: 85% !important;\r\n"
//				+ "        clear: both;\r\n" + "      }\r\n" + "\r\n" + "      table[class=col] {\r\n"
//				+ "        width: 45% !important;\r\n" + "      }\r\n" + "\r\n" + "      .erase {\r\n"
//				+ "        display: none;\r\n" + "      }\r\n" + "    }\r\n" + "\r\n"
//				+ "    @media only screen and (max-width: 420px) {\r\n" + "      table[class=fullTable] {\r\n"
//				+ "        width: 100% !important;\r\n" + "        clear: both;\r\n" + "      }\r\n" + "\r\n"
//				+ "      table[class=fullPadding] {\r\n" + "        width: 85% !important;\r\n"
//				+ "        clear: both;\r\n" + "      }\r\n" + "\r\n" + "      table[class=col] {\r\n"
//				+ "        width: 100% !important;\r\n" + "        clear: both;\r\n" + "      }\r\n" + "\r\n"
//				+ "      table[class=col] td {\r\n" + "        text-align: left !important;\r\n" + "      }\r\n"
//				+ "\r\n" + "      .erase {\r\n" + "        display: none;\r\n" + "        font-size: 0;\r\n"
//				+ "        max-height: 0;\r\n" + "        line-height: 0;\r\n" + "        padding: 0;\r\n"
//				+ "      }\r\n" + "\r\n" + "      .visibleMobile {\r\n" + "        display: block !important;\r\n"
//				+ "      }\r\n" + "\r\n" + "      .hiddenMobile {\r\n" + "        display: none !important;\r\n"
//				+ "      }\r\n" + "    }" + "</style>"
//
//				+ "</head>\r\n" + "\r\n" + "<body>\r\n" + "\r\n" + "\r\n"
//
//				+ "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">\r\n"
//				+ "<tr>\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>" + "<tr>" + "<td>"
//				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#ffffff\"\r\n"
//				+ "style=\"border-radius: 10px 10px 0 0;\">" + "<tr class=\"hiddenMobile\">\r\n"
//				+ "<td height=\"40\"></td>\r\n" + "</tr>" + "<tr class=\"visibleMobile\">\r\n"
//				+ "<td height=\"30\"></td>\r\n" + "</tr>" + " <tr>\r\n" + "<td>\r\n"
//				+ " <table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
//				+ "\r\n" + "<tbody>\r\n" + "<tr>\r\n" + "<td>\r\n"
//				+ "<table width=\"220\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"left\" class=\"col\">\r\n"
//				+ "<tbody>\r\n" + "<tr>\r\n"
//				+ "<td align=\"left\"> <img src=\"src/main/resources/logo/BVPlxFC.JPG\" width=\"125\" height=\"85\" alt=\"logo\" border=\"0\" />\r\n"
//				+ "</td>" + "<br></br>" + "</tr>" + "<tr class=\"hiddenMobile\">\r\n" + "<td height=\"40\"></td>\r\n"
//				+ "</tr>\r\n" + "<tr class=\"visibleMobile\">\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>"
//				+ "</tbody>" + "</table>"
//
//				+ "<table width=\"220\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"right\" class=\"col\">\r\n"
//				+ "<tbody>\r\n" + "<tr class=\"visibleMobile\">\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>\r\n"
//				+ "<tr>\r\n" + "<td height=\"5\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 26px; color: #E60000; letter-spacing: -3px; font-weight: bold; font-family: 'Open Sans', sans-serif; line-height: 1; vertical-align: top; text-align: right;\">\r\n"
//				+ "Baabuji Ventures\r\n" + "</td>\r\n" + "</tr>" + "<tr class=\"hiddenMobile\">\r\n"
//				+ "<td height=\"50\"></td>\r\n" + "</tr>\r\n" + "<tr class=\"visibleMobile\">\r\n"
//				+ "<td height=\"20\"></td>\r\n" + "</tr>" + "<tr>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 15px; color: #5b5b5b; font-family: 'Open Sans', sans-serif; line-height: 18px; vertical-align: top; text-align: right;\">\r\n"
//				+ "<small>Invoice No: </small>" + invoiceNumber + "<br />\r\n" + "<small>Generated Date: </small>"
//				+ generatedDate + "<br />\r\n"
//
//				+ "<small style=\"color: #070707;\">Month : </small> <b style=\"color: #070707;\">" + month
//				+ "</b><br />\r\n" + "</td>\r\n" + "</tr>" + "</tbody>\r\n" + "</table>" + "</td>" + "</tr>"
//				+ "</tbody>" + " </table>" + "</td>" + " </tr>" + "</table>" + "</td>" + "</tr>" + " </table>"
//
//				+ "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">"
//				+ "<tbody>" + "<tr>" + "<td>"
//				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#ffffff\">"
//				+ "<tbody>" + "<tr class=\"hiddenMobile\">\r\n" + "<td height=\"60\"></td>\r\n" + "</tr>\r\n"
//				+ "<tr class=\"visibleMobile\">\r\n" + "<td height=\"40\"></td>\r\n" + "</tr>" + "<tr>" + "<td>"
//				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
//				+ "<tbody>" + "<tr>\r\n" + "<th\r\n"
//				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; font-weight: normal; line-height: 1; vertical-align: top; padding: 0 10px 7px 0;\"\r\n"
//				+ "align=\"left\">\r\n" + "<strong>Description</strong>\r\n" + "</th>\r\n" + "<th\r\n"
//				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; font-weight: normal; line-height: 1; vertical-align: top; padding: 0 0 7px;\"\r\n"
//				+ "align=\"left\">\r\n" + "\r\n" + "</th>\r\n" + "<th\r\n"
//				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; font-weight: normal; line-height: 1; vertical-align: top; padding: 0 0 7px;\"\r\n"
//				+ "align=\"center\">\r\n" + "<strong>No of Hit</strong>\r\n" + "</th>\r\n" + "<th\r\n"
//				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #1e2b33; font-weight: normal; line-height: 1; vertical-align: top; padding: 0 0 7px;\"\r\n"
//				+ "align=\"right\">\r\n" + "<strong>Wallet Balance</strong>\r\n" + "</th>\r\n" + "</tr>\r\n" + "<tr>\r\n"
//				+ "<td height=\"1\" style=\"background: #bebebe;\" colspan=\"4\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n"
//				+ "<td height=\"10\" colspan=\"4\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #646a6e;  line-height: 18px;  vertical-align: top; padding:10px 0;\"\r\n"
//				+ "class=\"article\">\r\n" + "<b>" + "API KYC Verification services dated from " + fromDate + " to "
//				+ toDate + " and the services used are "+usedServices+ "</b>\r\n" + "</td>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e;  line-height: 18px;  vertical-align: top; padding:10px 0;\">\r\n"
//				+ "<b>" + "</b>\r\n" + "</td>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e;  line-height: 18px;  vertical-align: top; padding:10px 0;\"\r\n"
//				+ "align=\"center\"><b>" + totalHits + "</b></td>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #1e2b33;  line-height: 18px;  vertical-align: top; padding:10px 0;\"\r\n"
//				+ "align=\"right\"><b>" + walletBalance + "</b></td>\r\n" + "</tr>\r\n" + "<tr>\r\n"
//				+ "<td height=\"1\" colspan=\"4\" style=\"border-bottom:1px solid #e4e4e4\"></td>\r\n" + "</tr>\r\n"
//				+ "\r\n" + "<tr>\r\n"
//				+ "<td height=\"1\" colspan=\"4\" style=\"border-bottom:1px solid #e4e4e4\"></td>\r\n" + "</tr>"
//				+ "</tbody>" + "</table>" + "</td>" + "</tr>" + " <tr>\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>"
//				+ "</tbody>" + "</table>" + "</td>" + "</tr>" + "</tbody>" + "</table>"
//
//				+ "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">\r\n"
//				+ "<tbody>" + "<tr>" + "<td>"
//				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\"\r\n"
//				+ " bgcolor=\"#ffffff\">" + "<tbody>" + "<tr>" + "<td>"
//				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
//				+ " <tbody>"
//
//				+ " <tr>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
//				+ "Amount used in " + month + "\r\n" + "</td>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; white-space:nowrap;\"\r\n"
//				+ "width=\"80\">\r\n" + "" + usedAmount + "\r\n" + "</td>\r\n" + "</tr>"
//
//				+ " <tr>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
//				+ "Cgst (9%)\r\n" + "</td>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
//				+ "" + cgst + "\r\n" + "</td>\r\n" + "</tr>"
//
//				+ " <tr>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
//				+ "Sgst (9%)\r\n" + "</td>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; white-space:nowrap;\"\r\n"
//				+ "width=\"80\">\r\n" + "" + sgst + "\r\n" + "</td>\r\n" + "</tr>"
//
//				+ "<tr>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #000; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
//				+ "<strong>Grand Total</strong>\r\n" + "</td>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #000; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
//				+ "<strong>" + grandTotal + "</strong>\r\n" + "</td>\r\n" + "</tr>" + "</tbody>" + "</table>" + "</td>"
//				+ "</tr>" + "</tbody>" + "</table>" + "</td>" + "</tr>" + "</tbody>" + "</table>"
//				+ "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">\r\n"
//				+ "<tbody>" + "<tr>" + "<td>"
//
//				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\"\r\n"
//				+ "bgcolor=\"#ffffff\">" + "<tbody>" + " <tr class=\"hiddenMobile\">\r\n"
//				+ "<td height=\"60\"></td>\r\n" + "</tr>\r\n" + "<tr class=\"visibleMobile\">\r\n"
//				+ "<td height=\"40\"></td>\r\n" + "</tr>" + "<tr>" + "<td>"
//				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
//				+ "<tbody>" + "<tr>" + "<td>"
//				+ "<table width=\"290\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"left\" class=\"col\">\r\n"
//				+ "<tbody>" + "<tr>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 11px; font-family: 'Open Sans', sans-serif; color: #000000; line-height: 1; vertical-align: top; \">\r\n"
//				+ "<strong>BILLING INFORMATION</strong>\r\n" + "</td>\r\n" + "</tr>" + " <tr>\r\n"
//				+ "<td width=\"100%\" height=\"10\"></td>\r\n" + "</tr>" + "<tr>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; line-height: 20px; vertical-align: top; \">\r\n"
//				+ "<b>Baabuji Ventures Pvt Ltd</b><br></br>\r\n" + "New No.9,Old No.11,Ground Floor<br> </br>\r\n"
//				+ "Palayakaran Street,Kalaimagal Nagar,<br></br>\r\n" + "Ekkatuthangal,Chennai 600032<br></br>\r\n"
//
//				+ "<small style=\"color: #000000;\">Gst No :</small><b>33AAFCB2854F2ZY</b><br></br>\r\n"
//				+ "<small style=\"color: #000000;\">HSN Code :</small><b>9031</b>\r\n" + "</td>\r\n"
//
//				+ "</tr>" + "</tbody>" + "</table>"
//				+ " <table width=\"160\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"right\" class=\"col\">\r\n"
//				+ "<tbody>\r\n" + "<tr class=\"visibleMobile\">\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>\r\n"
//				+ "<tr>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 11px; font-family: 'Open Sans', sans-serif; color: #000000; line-height: 1; vertical-align: top; \">\r\n"
//				+ "<strong>RECIPIENT ADDRESS</strong>\r\n" + "</td>\r\n" + "</tr>\r\n" + "<tr>\r\n"
//				+ "<td width=\"100%\" height=\"10\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; line-height: 20px; vertical-align: top; \">\r\n"
//				+ "<b>" + entityName + "</b> <br></br>\r\n" + "" + city + "-" + pincode + "<br></br>\r\n" + "" + state
//				+ "<br></br>" + country + "<br></br>\r\n" + "<small style=\"color: #000000;\">Gst No :</small><b>"
//				+ gstNo + "</b>\r\n" + "\r\n" + "\r\n" + "</td>\r\n" + "</tr>\r\n" + "</tbody>\r\n" + "</table>"
//				+ "</td>" + "</tr>" + "</tbody>" + "</table>" + "</td>" + "</tr>" + "<tr>" + "<td>"
//				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
//				+ "<tbody>\r\n" + "<tr>\r\n" + "<td>\r\n"
//				+ "<table width=\"470\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"left\" class=\"col\">\r\n"
//				+ "<tbody>\r\n" + "<tr class=\"hiddenMobile\">\r\n" + "<td height=\"35\"></td>\r\n" + "</tr>\r\n"
//				+ "<tr class=\"visibleMobile\">\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n"
//				+ "<td\r\n"
//				+ "style=\"font-size: 11px; font-family: 'Open Sans', sans-serif; color: #0e0e0e; line-height: 1; vertical-align: top; \">\r\n"
//				+ "<strong>DESCRIPTION</strong>\r\n" + "</td>\r\n" + "</tr>\r\n" + "<tr>\r\n"
//				+ "<td width=\"100%\" height=\"10\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td\r\n"
//				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; line-height: 20px; vertical-align: top; \">\r\n"
//
//				+ "This notice is to inform you that your prepaid recharge for the facheck services has been successfully received and has been updated in your wallet.<br></br>\r\n"
//
//				+ "\r\n" + "</td>\r\n" + "</tr>\r\n"
//
//				+ "</tbody>\r\n" + "</table>\r\n" + "</td>\r\n" + "</tr>\r\n" + "</tbody>\r\n" + "</table>" + "</td>"
//				+ "</tr>" + "<tr class=\"hiddenMobile\">\r\n" + "<td height=\"60\">\r\n" + "</td>\r\n" + "</tr>\r\n"
//				+ "<tr class=\"visibleMobile\">\r\n" + "<td height=\"30\"></td>\r\n" + "</tr>" + "</tbody>" + "</table>"
//				+ "</td>" + "</tr>" + "</tbody>" + "</table>"
//
//				+ " <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">\r\n"
//				+ "\r\n" + "<tr>\r\n" + "<td>\r\n"
//				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#ffffff\"\r\n"
//				+ "style=\"border-radius: 0 0 10px 10px;\">\r\n" + "<tr>\r\n" + "<td>\r\n"
//				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
//				+ "<tbody>\r\n" + "<tr>\r\n"
//
//				+ "</tr>\r\n" + "</tbody>\r\n" + "</table>\r\n" + "</td>\r\n" + "</tr>\r\n"
//				+ "<tr class=\"spacer\">\r\n" + "<td height=\"50\"></td>\r\n" + "</tr>\r\n" + "\r\n" + "</table>\r\n"
//				+ "</td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>\r\n" + "</table>"
//				+ "</body>\r\n" + "\r\n" + "</html>";

		String generatedString = "";

		if (prepaid.getInvoiceTwo() == null || prepaid.getInvoiceTwo().equalsIgnoreCase("Deleted")) {

			generatedString = invoiceNumber + "-A" + ".pdf";

			System.out.println("IV : " + generatedString);

			generatedIn = "A";
			
			String fileName = con + File.separator + "invoice/" + StringUtils.cleanPath(generatedString);

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
						e.printStackTrace();
						LOGGER.info("IOException", e);
					}
				}
			}

			prepaid.setInvoiceTwo(generatedString);

		} else if(prepaid.getInvoiceThree() == null || prepaid.getInvoiceThree().equalsIgnoreCase("Deleted")){

			generatedString = invoiceNumber + "-B" + ".pdf";

			System.out.println("IV : " + generatedString);
			
			generatedIn = "B";

			String fileName = con + File.separator + "invoice/" + StringUtils.cleanPath(generatedString);

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
						e.printStackTrace();
						LOGGER.info("IOException", e);
					}
				}
			}
			prepaid.setInvoiceThree(generatedString);
		}else {
			generatedIn = "C";
		}

		prepaidRepository.save(prepaid);
		
		return generatedIn;
	}

	public String updatePrepaidIgstInvoice(TransactionDto transactionDto,
			PrepaidPayment prepaid,
			RequestModel model, Path con) throws Exception {

		
		String generatedIn = "";
		
		String invoiceNumber = transactionDto.getInvoiceNumber();
		String fromDate = model.getFromDate();
		LocalDate generatedDate = model.getGeneratedDate();
		String toDate = model.getToDate();
		String month = model.getMonthName();
		int totalHits = model.getTotalHits();
		double usedAmount = model.getUsedAmount();
		
		double igst = model.getUsedAmount()*18/100;
		
		double grandTotal = fu.twoDecimelDouble(usedAmount+igst);
		
		String entityName = model.getName();
		String city = model.getCityName();
		String state = model.getStateName();
		String pincode = model.getPincode();
		String country = model.getCountryName();
		String gstNo = model.getGst();
		String bankAccount = model.getBankAccountNumber();
		String hsnCode = model.getHsnNumber();
		String url = "http://157.245.105.135:5030/prepaid-by-month/"+prepaid.getEntityModel().getUserId();
		
		int amount = (int)grandTotal;
		
		String amountInWord = AmountToWords.convertAmountToWords(amount)+ " only";

		String html = "<!DOCTYPE html>\r\n" + "<html lang=\"en\">\r\n" + "\r\n" + "<head>\r\n"
				+ "<title>Invoice</title>\r\n"

				+ "<style type=\"text/css\">" + "  body {\r\n" + "      margin: 0;\r\n" + "      padding: 0;\r\n"
				+ "      background: #c0c0c0;\r\n" + "    }" + " div,\r\n" + "    p,\r\n" + "    a,\r\n"
				+ "    li,\r\n" + "    td {\r\n" + "      -webkit-text-size-adjust: none;\r\n" + "    }"
				+ " .ReadMsgBody {\r\n" + "      width: 100%;\r\n" + "      background-color: #636363;\r\n"
				+ "    }\r\n" + "\r\n" + "    .ExternalClass {\r\n" + "      width: 100%;\r\n"
				+ "      background-color: #a09797;\r\n" + "    }\r\n" + "\r\n" + "    body {\r\n"
				+ "      width: 100%;\r\n" + "      height: 100%;\r\n" + "      background-color: #eee5e5;\r\n"
				+ "      margin: 0;\r\n" + "      padding: 0;\r\n"
				+ "      -webkit-font-smoothing: antialiased;\r\n" + "    }\r\n" + "\r\n" + "    html {\r\n"
				+ "      width: 100%;\r\n" + "    }\r\n" + "\r\n" + "    p {\r\n"
				+ "      padding: 0 !important;\r\n" + "      margin-top: 0 !important;\r\n"
				+ "      margin-right: 0 !important;\r\n" + "      margin-bottom: 0 !important;\r\n"
				+ "      margin-left: 0 !important;\r\n" + "    }\r\n" + "\r\n" + "    .visibleMobile {\r\n"
				+ "      display: none;\r\n" + "    }\r\n" + "\r\n" + "    .hiddenMobile {\r\n"
				+ "      display: block;\r\n" + "    }\r\n" + "\r\n"
				+ "    @media only screen and (max-width: 600px) {\r\n" + "      body {\r\n"
				+ "        width: auto !important;\r\n" + "      }\r\n" + "\r\n"
				+ "      table[class=fullTable] {\r\n" + "        width: 96% !important;\r\n"
				+ "        clear: both;\r\n" + "      }\r\n" + "\r\n" + "      table[class=fullPadding] {\r\n"
				+ "        width: 85% !important;\r\n" + "        clear: both;\r\n" + "      }\r\n" + "\r\n"
				+ "      table[class=col] {\r\n" + "        width: 45% !important;\r\n" + "      }\r\n" + "\r\n"
				+ "      .erase {\r\n" + "        display: none;\r\n" + "      }\r\n" + "    }\r\n" + "\r\n"
				+ "    @media only screen and (max-width: 420px) {\r\n" + "      table[class=fullTable] {\r\n"
				+ "        width: 100% !important;\r\n" + "        clear: both;\r\n" + "      }\r\n" + "\r\n"
				+ "      table[class=fullPadding] {\r\n" + "        width: 85% !important;\r\n"
				+ "        clear: both;\r\n" + "      }\r\n" + "\r\n" + "      table[class=col] {\r\n"
				+ "        width: 100% !important;\r\n" + "        clear: both;\r\n" + "      }\r\n" + "\r\n"
				+ "      table[class=col] td {\r\n" + "        text-align: left !important;\r\n" + "      }\r\n"
				+ "\r\n" + "      .erase {\r\n" + "        display: none;\r\n" + "        font-size: 0;\r\n"
				+ "        max-height: 0;\r\n" + "        line-height: 0;\r\n" + "        padding: 0;\r\n"
				+ "      }\r\n" + "\r\n" + "      .visibleMobile {\r\n" + "        display: block !important;\r\n"
				+ "      }\r\n" + "\r\n" + "      .hiddenMobile {\r\n" + "        display: none !important;\r\n"
				+ "      }\r\n" + "    }" + "</style>"

				+ "</head>\r\n" + "\r\n" + "<body>\r\n" + "\r\n" + "\r\n"

				+ "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">\r\n"
				+ "<tr>\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>" + "<tr>" + "<td>"
				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#ffffff\"\r\n"
				+ "style=\"border-radius: 10px 10px 0 0;\">" + "<tr class=\"hiddenMobile\">\r\n"
				+ "<td height=\"40\"></td>\r\n" + "</tr>" + "<tr class=\"visibleMobile\">\r\n"
				+ "<td height=\"30\"></td>\r\n" + "</tr>" + " <tr>\r\n" + "<td>\r\n"
				+ " <table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
				+ "\r\n" + "<tbody>\r\n" + "<tr>\r\n" + "<td>\r\n"
				+ "<table width=\"220\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"left\" class=\"col\">\r\n"
				+ "<tbody>\r\n" + "<tr>\r\n"
				+ "<td align=\"left\"> <img src=\"src/main/resources/logo/fclogo.png\" width=\"125\" height=\"85\" alt=\"logo\" border=\"0\" />\r\n"
				+ "</td>" + "<br></br>" + "</tr>" + "<tr class=\"hiddenMobile\">\r\n"
				+ "<td height=\"40\"></td>\r\n" + "</tr>\r\n" + "<tr class=\"visibleMobile\">\r\n"
				+ "<td height=\"20\"></td>\r\n" + "</tr>" + "</tbody>" + "</table>"

				+ "<table width=\"220\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"right\" class=\"col\">\r\n"
				+ "<tbody>\r\n" + "<tr class=\"visibleMobile\">\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>\r\n"
				+ "<tr>\r\n" + "<td height=\"5\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 26px; color: #E60000; letter-spacing: -3px; font-weight: bold; font-family: 'Open Sans', sans-serif; line-height: 1; vertical-align: top; text-align: right;\">\r\n"
				+ "Baabuji Ventures\r\n" + "</td>\r\n" + "</tr>" + "<tr class=\"hiddenMobile\">\r\n"
				+ "<td height=\"50\"></td>\r\n" + "</tr>\r\n" + "<tr class=\"visibleMobile\">\r\n"
				+ "<td height=\"20\"></td>\r\n" + "</tr>" + "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 15px; color: #5b5b5b; font-family: 'Open Sans', sans-serif; line-height: 18px; vertical-align: top; text-align: right;\">\r\n"
				+ "<small>Invoice No: </small>" + invoiceNumber + "<br />\r\n" + "<small>Generated Date: </small>"
				+ generatedDate + "<br />\r\n"
				
				+ "<small style=\"color: #070707;\">Month: </small> <b style=\"color: #070707;\">"
				+ month + "</b><br />\r\n" + "</td>\r\n" + "</tr>" + "</tbody>\r\n" + "</table>"
				+ "</td>" + "</tr>" + "</tbody>" + " </table>" + "</td>" + " </tr>" + "</table>" + "</td>" + "</tr>"
				+ " </table>"

				+ "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">"
				+ "<tbody>" + "<tr>" + "<td>"
				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#ffffff\">"
				+ "<tbody>" + "<tr class=\"hiddenMobile\">\r\n" + "<td height=\"60\"></td>\r\n" + "</tr>\r\n"
				+ "<tr class=\"visibleMobile\">\r\n" + "<td height=\"40\"></td>\r\n" + "</tr>" + "<tr>" + "<td>"
				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
				+ "<tbody>" + "<tr>\r\n" + "<th\r\n"
				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; font-weight: normal; line-height: 1; vertical-align: top; padding: 0 10px 7px 0;\"\r\n"
				+ "align=\"left\">\r\n" + "<strong>Description</strong>\r\n" + "</th>\r\n" + "<th\r\n"
				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; font-weight: normal; line-height: 1; vertical-align: top; padding: 0 0 7px;\"\r\n"
				+ "align=\"left\">\r\n" + "\r\n" + "</th>\r\n" + "<th\r\n"
				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; font-weight: normal; line-height: 1; vertical-align: top; padding: 0 0 7px;\"\r\n"
				+ "align=\"center\">\r\n" + "<strong>No of Hit</strong>\r\n" + "</th>\r\n" + "<th\r\n"
				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #1e2b33; font-weight: normal; line-height: 1; vertical-align: top; padding: 0 0 7px;\"\r\n"
				+ "align=\"right\">\r\n" + "<strong>Total Amount</strong>\r\n" + "</th>\r\n" + "</tr>\r\n" + "<tr>\r\n"
				+ "<td height=\"1\" style=\"background: #bebebe;\" colspan=\"4\"></td>\r\n" + "</tr>\r\n"
				+ "<tr>\r\n" + "<td height=\"10\" colspan=\"4\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 10px; font-family: 'Open Sans', sans-serif; color: #646a6e;  line-height: 18px;  vertical-align: top; padding:10px 0;\"\r\n"
				+ "class=\"article\">\r\n" + "<b>" + "API KYC Verification services dated from "
				+ fromDate + " to " + toDate  +"</b>\r\n" + "</td>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e;  line-height: 18px;  vertical-align: top; padding:10px 0;\">\r\n"
				+ "<b>" + "</b>\r\n" + "</td>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e;  line-height: 18px;  vertical-align: top; padding:10px 0;\"\r\n"
				+ "align=\"center\"><b>" + totalHits + "</b></td>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #1e2b33;  line-height: 18px;  vertical-align: top; padding:10px 0;\"\r\n"
				+ "align=\"right\"><b>" + grandTotal + "</b></td>\r\n" + "</tr>\r\n" + "<tr>\r\n"
				+ "<td height=\"1\" colspan=\"4\" style=\"border-bottom:1px solid #e4e4e4\"></td>\r\n" + "</tr>\r\n"
				+ "\r\n" + "<tr>\r\n"
				+ "<td height=\"1\" colspan=\"4\" style=\"border-bottom:1px solid #e4e4e4\"></td>\r\n" + "</tr>"
				+ "</tbody>" + "</table>" + "</td>" + "</tr>" + " <tr>\r\n" + "<td height=\"20\"></td>\r\n"
				+ "</tr>" + "</tbody>" + "</table>" + "</td>" + "</tr>" + "</tbody>" + "</table>"

				+ "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">\r\n"
				+ "<tbody>" + "<tr>" + "<td>"
				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\"\r\n"
				+ " bgcolor=\"#ffffff\">" + "<tbody>" + "<tr>" + "<td>"
				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
				+ " <tbody>"

				+ " <tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
				+ "Amount used in "+month+"\r\n" + "</td>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; white-space:nowrap;\"\r\n"
				+ "width=\"80\">\r\n" + "" + usedAmount + "\r\n" + "</td>\r\n" + "</tr>"


				+ " <tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
				+ "Igst (18%)\r\n" + "</td>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #646a6e; line-height: 22px; vertical-align: top; text-align:right; white-space:nowrap;\"\r\n"
				+ "width=\"80\">\r\n" + "" + igst + "\r\n" + "</td>\r\n" + "</tr>"


				+ "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #000; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
				+ "<strong>" +amountInWord +"</strong>\r\n" + "</td>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #000; line-height: 22px; vertical-align: top; text-align:right; \">\r\n"
				+ "<strong>" + grandTotal + "</strong>\r\n" + "</td>\r\n" + "</tr>" + "</tbody>" + "</table>"
				+ "</td>" + "</tr>" + "</tbody>" + "</table>" + "</td>" + "</tr>" + "</tbody>" + "</table>"
				+ "<table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">\r\n"
				+ "<tbody>" + "<tr>" + "<td>"
				
				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\"\r\n"
				+ "bgcolor=\"#ffffff\">" + "<tbody>" + " <tr class=\"hiddenMobile\">\r\n"
				+ "<td height=\"60\"></td>\r\n" + "</tr>\r\n" + "<tr class=\"visibleMobile\">\r\n"
				+ "<td height=\"40\"></td>\r\n" + "</tr>" + "<tr>" + "<td>"
				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
				+ "<tbody>" + "<tr>" + "<td>"
				+ "<table width=\"290\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"left\" class=\"col\">\r\n"
				+ "<tbody>" + "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 11px; font-family: 'Open Sans', sans-serif; color: #000000; line-height: 1; vertical-align: top; \">\r\n"
				+ "<strong>BILLING INFORMATION</strong>\r\n" + "</td>\r\n" + "</tr>" + " <tr>\r\n"
				+ "<td width=\"100%\" height=\"10\"></td>\r\n" + "</tr>" + "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; line-height: 20px; vertical-align: top; \">\r\n"
				+ "<b>Baabuji Ventures Pvt Ltd</b><br></br>\r\n" + "New No.9,Old No.11,Ground Floor<br> </br>\r\n"
				+ "Palayakaran Street,Kalaimagal Nagar,<br></br>\r\n" + "Ekkatuthangal,Chennai 600032<br></br>\r\n"
				
				+ "<small style=\"color: #000000;\">Gst No :</small><b>33AAFCB2854F2ZY</b><br></br>\r\n" 
				+ "<small style=\"color: #000000;\">Account No :</small><b>"+bankAccount+"</b><br></br>\r\n"
				+ "<small style=\"color: #000000;\">HSN Code :</small><b>"+hsnCode+"</b>\r\n"+ "</td>\r\n"
				
				+ "</tr>" + "</tbody>" + "</table>"
				+ " <table width=\"160\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"right\" class=\"col\">\r\n"
				+ "<tbody>\r\n" + "<tr class=\"visibleMobile\">\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>\r\n"
				+ "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 11px; font-family: 'Open Sans', sans-serif; color: #000000; line-height: 1; vertical-align: top; \">\r\n"
				+ "<strong>RECIPIENT ADDRESS</strong>\r\n" + "</td>\r\n" + "</tr>\r\n" + "<tr>\r\n"
				+ "<td width=\"100%\" height=\"10\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; line-height: 20px; vertical-align: top; \">\r\n"
				+ "<b>" + entityName + "</b> <br></br>\r\n" + "" + city + "-"
				+ pincode + "<br></br>\r\n" + "" + state + "<br></br>"
				+ country + "<br></br>\r\n" + "<small style=\"color: #000000;\">Gst No :</small><b>"
				+ gstNo + "</b>\r\n" + "\r\n" + "\r\n" + "</td>\r\n" + "</tr>\r\n" + "</tbody>\r\n" + "</table>"
				+ "</td>" + "</tr>" + "</tbody>" + "</table>" + "</td>" + "</tr>" + "<tr>" + "<td>"
				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
				+ "<tbody>\r\n" + "<tr>\r\n" + "<td>\r\n"
				+ "<table width=\"470\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"left\" class=\"col\">\r\n"
				+ "<tbody>\r\n" + "<tr class=\"hiddenMobile\">\r\n" + "<td height=\"35\"></td>\r\n" + "</tr>\r\n"
				+ "<tr class=\"visibleMobile\">\r\n" + "<td height=\"20\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n"
				+ "<td\r\n"
				+ "style=\"font-size: 11px; font-family: 'Open Sans', sans-serif; color: #0e0e0e; line-height: 1; vertical-align: top; \">\r\n"
				+ "<strong>DESCRIPTION</strong>\r\n" + "</td>\r\n" + "</tr>\r\n" + "<tr>\r\n"
				+ "<td width=\"100%\" height=\"10\"></td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td\r\n"
				+ "style=\"font-size: 12px; font-family: 'Open Sans', sans-serif; color: #5b5b5b; line-height: 20px; vertical-align: top; \">\r\n"
				
				+ "This invoice details the costs of services used this month.<br></br>\r\n"
				
				+ "\r\n" + "</td>\r\n" + "</tr>\r\n"
				
				+ "</tbody>\r\n" + "</table>\r\n" + "</td>\r\n" + "</tr>\r\n" + "</tbody>\r\n" + "</table>"
				+ "</td>" + "</tr>" + "<tr class=\"hiddenMobile\">\r\n" + "<td height=\"60\">\r\n" + "</td>\r\n"
				+ "</tr>\r\n" + "<tr class=\"visibleMobile\">\r\n" + "<td height=\"30\"></td>\r\n" + "</tr>"
				+ "</tbody>" + "</table>" + "</td>" + "</tr>" + "</tbody>" + "</table>"

				+ " <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#e1e1e1\">\r\n"
				+ "\r\n" + "<tr>\r\n" + "<td>\r\n"
				+ "<table width=\"600\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullTable\" bgcolor=\"#ffffff\"\r\n"
				+ "style=\"border-radius: 0 0 10px 10px;\">\r\n" + "<tr>\r\n" + "<td>\r\n"
				+ "<table width=\"480\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" align=\"center\" class=\"fullPadding\">\r\n"
				+ "<tbody>\r\n" + "<tr>\r\n"

				+ "<td\r\n"
				+ "style=\"font-size: 12px; color: #02ff89; font-family: 'Open Sans', sans-serif; line-height: 18px; vertical-align: top; text-align: left;\">\r\n"
				+ "<a href=\""+url+"\">click here to know more about payment</a>\r\n"
				+ "</td>\r\n"

				+ "</tr>\r\n" + "</tbody>\r\n" + "</table>\r\n" + "</td>\r\n" + "</tr>\r\n"
				+ "<tr class=\"spacer\">\r\n" + "<td height=\"50\"></td>\r\n" + "</tr>\r\n" + "\r\n"
				+ "</table>\r\n" + "</td>\r\n" + "</tr>\r\n" + "<tr>\r\n" + "<td height=\"20\"></td>\r\n"
				+ "</tr>\r\n" + "</table>" + "</body>\r\n" + "\r\n" + "</html>";

		String generatedString = "";

		if (prepaid.getInvoiceTwo() == null || prepaid.getInvoiceTwo().equalsIgnoreCase("Deleted")) {

			generatedString = invoiceNumber + "-A" + ".pdf";

			System.out.println("IV : " + generatedString);
			
			generatedIn = "A";

			String fileName = con + File.separator + "invoice/" + StringUtils.cleanPath(generatedString);

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
						e.printStackTrace();
						LOGGER.info("IOException", e);
					}
				}
			}

			prepaid.setInvoiceTwo(generatedString);

		} else if(prepaid.getInvoiceThree() == null || prepaid.getInvoiceThree().equalsIgnoreCase("Deleted")){

			generatedString = invoiceNumber + "-B" + ".pdf";

			System.out.println("IV : " + generatedString);
			
			generatedIn = "B";

			String fileName = con + File.separator + "invoice/" + StringUtils.cleanPath(generatedString);

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
						e.printStackTrace();
						LOGGER.info("IOException", e);
					}
				}
			}
			prepaid.setInvoiceThree(generatedString);
		}else {
			generatedIn = "C";
		}

		prepaidRepository.save(prepaid);
		
		return generatedIn;
	}

}
