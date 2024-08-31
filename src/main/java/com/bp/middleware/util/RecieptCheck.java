package com.bp.middleware.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.xhtmlrenderer.pdf.ITextRenderer;

import com.bp.middleware.prepaidpostpaid.PostpaidPayment;
import com.bp.middleware.prepaidpostpaid.PrepaidPayment;
import com.bp.middleware.transaction.TransactionDto;
import com.bp.middleware.user.EntityModel;

import jakarta.servlet.ServletContext;

public class RecieptCheck {

	private static final Logger logger = LoggerFactory.getLogger(RecieptCheck.class);

	public static String receiptGenerator(Path currentWorkingDir,TransactionDto transactionObj,
			EntityModel entity, PostpaidPayment postpaid,PrepaidPayment prepaidPayment) {

		try {

			String html = "<!DOCTYPE html>\r\n"
					+ "<html lang=\"en\">\r\n"
					+ "  <head>\r\n"
					+ "    <title>Receipt</title>\r\n"
					+ "    <style>\r\n"
					+ "      * {\r\n"
					+ "        box-sizing: border-box;\r\n"
					+ "        -moz-box-sizing: border-box;\r\n"
					+ "      }\r\n"
					+ "    </style>\r\n"
					+ "  </head>\r\n"
					+ "  <body style=\" font-family:Arial,Helvetica,sans-serif;font-weight:370;\r\n"
					+ "  font-size:10px;line-height:18px;print-color-adjust: exact;\"\r\n"
					+ "  >\r\n"
					+ "    <table style=\"width: 100%; border: 0px solid\">\r\n"
					+ "      <tr>\r\n"
					+ "        <td style=\"width: 100%; text-align: center\"><h1 style=\"color:#963DB2; font-size: xx-large;\">BASISPAY eKYC</h1></td>\r\n"
					+ "        <td style=\"width: 30%; text-align: right\">\r\n"
					+ "          <img\r\n"
					+ "            src=\"src/main/resources/logo/Basispay.jpg\"\r\n"
					+ "            alt=\"Logo\"\r\n"
					+ "            style=\"width: 100px; height: 80px; object-fit: cover\"\r\n"
					+ "          />\r\n"
					+ "        </td>\r\n"
					+ "      </tr>\r\n"
					+ "    </table>\r\n"
					+ "    <br/>\r\n"
					+ "\r\n"
					+ "    <table\r\n"
					+ "      style=\"width: 100%; border: 1px solid grey; background-color: #F1E6F4\"\r\n"
					+ "    >\r\n"
					+ "      <tr>\r\n"
					+ "        <td style=\"width: 10%\">Company Name</td>\r\n"
					+ "        <td style=\"width: 50%\"><b>: Baabuji Ventures Pvt</b></td>\r\n"
					+ "        <td style=\"width: 10%\">Receipt Number</td>\r\n"
					+ "        <td style=\"width: 20%\"><b>: RECPT0012 </b></td>\r\n"
					+ "      </tr>\r\n"
					+ "      <tr>\r\n"
					+ "        <td>Contact Name</td>\r\n"
					+ "        <td><b>: M.Balasubramanian </b></td>\r\n"
					+ "        <td>Receipt Date</td>\r\n"
					+ "        <td><b>: 02-9-2023</b></td>\r\n"
					+ "      </tr>\r\n"
					+ "      <tr>\r\n"
					+ "        <td>Payment mode</td>\r\n"
					+ "        <td><b>:Online Payment </b></td>\r\n"
					+ "        <td>Due Date</td>\r\n"
					+ "        <td><b>: 12-9-2023 </b></td>\r\n"
					+ "      </tr>\r\n"
					+ "      <tr></tr>\r\n"
					+ "      <tr>\r\n"
					+ "        <td>Total Hits</td>\r\n"
					+ "        <td><b>: 220</b></td>\r\n"
					+ "        <td>Paid By</td>\r\n"
					+ "        <td><b>: M.Balasubramanian </b></td>\r\n"
					+ "      </tr>\r\n"
					+ "    </table>\r\n"
					+ "    <br/>\r\n"
					+ "\r\n"
					+ "    <div\r\n"
					+ "      style=\"\r\n"
					+ "        width: 100%;\r\n"
					+ "        height: 80%;\r\n"
					+ "        border-collapse: collapse;\r\n"
					+ "        border: 1px solid grey;\r\n"
					+ "        text-align: center;\r\n"
					+ "        padding: 10px;\r\n"
					+ "        font-size: 10px;\r\n"
					+ "        background-color: #CE9FDD;\r\n"
					+ "        color: white;\r\n"
					+ "        font-size: small;\"\r\n"
					+ "    >\r\n"
					+ "      <div><strong>Receipt</strong></div>\r\n"
					+ "    </div>\r\n"
					+ "    <br />\r\n"
					+ "\r\n"
					+ "    <table\r\n"
					+ "      style=\"width: 100%; border-collapse: collapse; border: 1px solid #ddd\"\r\n"
					+ "    >\r\n"
					+ "      <tr>\r\n"
					+ "        <th style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          Description\r\n"
					+ "        </th>\r\n"
					+ "        <th style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          Charge Date\r\n"
					+ "        </th>\r\n"
					+ "        <th style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          Invoice no\r\n"
					+ "        </th>\r\n"
					+ "        <th style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          Due Date\r\n"
					+ "        </th>\r\n"
					+ "        <th style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          Charged amount\r\n"
					+ "        </th>\r\n"
					+ "        <th style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          SGST (9%)\r\n"
					+ "        </th>\r\n"
					+ "        <th style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          CGST (9%)\r\n"
					+ "        </th>\r\n"
					+ "        <th style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          IGST (18%)\r\n"
					+ "        </th>\r\n"
					+ "\r\n"
					+ "        <th style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          Amount\r\n"
					+ "        </th>\r\n"
					+ "      </tr>\r\n"
					+ "      <tr>\r\n"
					+ "        <td style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          September Month Total Request Amount\r\n"
					+ "        </td>\r\n"
					+ "        <td style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          02-09-2023\r\n"
					+ "        </td>\r\n"
					+ "        <td style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          2143152486\r\n"
					+ "        </td>\r\n"
					+ "        <td style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          12-09-2023\r\n"
					+ "        </td>\r\n"
					+ "        <td style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          3016\r\n"
					+ "        </td>\r\n"
					+ "        <td style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          211.12\r\n"
					+ "        </td>\r\n"
					+ "        <td style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          211.12\r\n"
					+ "        </td>\r\n"
					+ "        <td style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          0.00\r\n"
					+ "        </td>\r\n"
					+ "\r\n"
					+ "        <td style=\"border: 1px solid grey; text-align: left; padding: 5px\">\r\n"
					+ "          3438.42\r\n"
					+ "        </td>\r\n"
					+ "      </tr>\r\n"
					+ "    </table>\r\n"
					+ "    <br />\r\n"
					+ "\r\n"
					+ "    <table\r\n"
					+ "      style=\"width: 100%; border-collapse: collapse; border: 1px solid grey\"\r\n"
					+ "    >\r\n"
					+ "      <tr>\r\n"
					+ "        <th\r\n"
					+ "          style=\"\r\n"
					+ "            width: 80%;\r\n"
					+ "            border-right: 1px solid hsl(272,66.67%,82.35%);\r\n"
					+ "            text-align: left;\r\n"
					+ "            font-size: 10px;\r\n"
					+ "            color: white;\r\n"
					+ "            background-color: #CE9FDD;\r\n"
					+ "            text-align: center;\r\n"
					+ "            font-size: small;\"\r\n"
					+ "        >\r\n"
					+ "          Total amount Received\r\n"
					+ "        </th>\r\n"
					+ "      </tr>\r\n"
					+ "    </table>\r\n"
					+ "    <br />\r\n"
					+ "\r\n"
					+ "    <table\r\n"
					+ "      style=\"width: 100%; border: 1px solid grey; background-color: #F1E6F4\"\r\n"
					+ "    >\r\n"
					+ "      <tr>\r\n"
					+ "        <td style=\"width: 40%\">Account</td>\r\n"
					+ "        <td><b>: 30145215451255</b></td>\r\n"
					+ "      </tr>\r\n"
					+ "      <tr>\r\n"
					+ "        <td>Transaction Id</td>\r\n"
					+ "        <td><b>: 3243253464364 </b></td>\r\n"
					+ "      </tr>\r\n"
					+ "      <tr>\r\n"
					+ "        <td>Payee Name</td>\r\n"
					+ "        <td><b>: M.Balasubramanian</b></td>\r\n"
					+ "      </tr>\r\n"
					+ "      <tr>\r\n"
					+ "        <td>Desc</td>\r\n"
					+ "        <td><b>: Online Payment </b></td>\r\n"
					+ "      </tr>\r\n"
					+ "    </table>\r\n"
					+ "  </body>\r\n"
					+ "</html>\r\n"
					+ "";
			String receiptNumber = FileUtils.getRandomOrderNumer();

			String generatedString = receiptNumber + ".pdf";

			String fileName = currentWorkingDir + File.separator + "receipt/" + StringUtils.cleanPath(generatedString);

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
						logger.info("IOException", e);
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
