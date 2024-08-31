package com.bp.middleware.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class DateUtil {
	
	///.........................................................///
	
	
	//Date conversion like this 10/21/2023 11:51:43 am
	public static String dateFormat() throws ParseException {
		String aRevisedDate = null;
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));   // This line converts the given date into UTC time zone
		DateFormat dateFormat =sdf; 
		String strDate = dateFormat.format(new Date());  
		Date dateObj=sdf.parse(strDate);
		aRevisedDate = new SimpleDateFormat("MM/dd/yyyy KK:mm:ss a").format(dateObj);
		return aRevisedDate;
	}
	
	//No.of days Adding one date to another date in DATE formate like 10/21/2023 11:51:43 am
	public static String addDate(int days,Date fromDate) throws ParseException {
		
		String aRevisedDate = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		Calendar c = Calendar.getInstance();
		c.setTime(fromDate); // Using today's date
		c.add(Calendar.DATE, days); // Adding 5 days
		String output = sdf.format(c.getTime());
		Date dateObj=sdf.parse(output);
		aRevisedDate = new SimpleDateFormat("MM/dd/yyyy KK:mm:ss a").format(dateObj);
		System.out.println(output);
		
		return aRevisedDate;
		
	}
	
	//Convert from String 30/01/2023 to 2023-01-30
	public String dateStructureConverter(String date) throws Exception{
		
		DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate localDate = LocalDate.parse(date,inputFormat);
		
		DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		String formattedDate = localDate.format(outputFormat);
		
		return formattedDate;
	}
	
	///........................................................///
	
	
	
	
	private static final  Logger logger = LoggerFactory.getLogger(DateUtil.class);


	private static final String SDF_SHORT_DATE_YMD = "yyyy-MM-dd";
	private static final String SDFSHORTDATEALT = "MM/dd/yy";
	private static final String SDF_SHORT_DATE_DMY = "dd/MM/yyyy";
	private static final String SDF_SHORT_DATE_DMY_HM = "yyyy-MM-dd HH:mm:ss";
	private static final String DATE_TO_STRING = "MM/dd/yy HH:mm a";
	private static final String SDF_FULL_DATE = "dd/MM/yyyy HH:mm a";
	private static final String DATE_MONTH = "dd MMM yy";
	private static final String DATE_MONTH_YEAR = "yyyyMMdd";
	private static final String WEB_SDF = "hh : mm : ss";
	private static final String DATE_FORMAT_HMS = "hh:mm:ss";
	private static final String DATE_FORMAT_HM = "hh:mm";
	private static final String SDF_SETTLEMENT = "dd-MM-yyyy";
	private static final String SDF_MMDDYYYYHHMMSS = "MMddyyyyhhmmss";
	
	public static Date getDateFromShortDateYMDString(String tempDate) throws ParseException 
	{
		Date  temp = new Date();
		try{
			temp = new SimpleDateFormat(SDF_SHORT_DATE_YMD).parse(tempDate);
			GregorianCalendar greorianCalender  = new GregorianCalendar();
			greorianCalender.setTime(temp);
			int year = greorianCalender.get(Calendar.YEAR);
			if ( year < 1970 ) {
				temp = new SimpleDateFormat(SDFSHORTDATEALT).parse(tempDate);
			}
		} catch ( ParseException pe ) {
			logger.info(AppConstants.TECHNICAL_ERROR,pe);
		}
		return temp;
	}

	public java.sql.Date getCurrentDate(){
		Calendar today = Calendar.getInstance();
		today.add(Calendar.DATE, 0);
		return new java.sql.Date(today.getTimeInMillis());
	}

	public static java.sql.Date getLastWeekDate(){
		Calendar today = Calendar.getInstance();  
		today.add(Calendar.DATE, -7);  
		return new java.sql.Date(today.getTimeInMillis());
	}

	public java.sql.Date getYesterdayDate(){
		Calendar today = Calendar.getInstance();  
		today.add(Calendar.DATE, -1);  
		return new java.sql.Date(today.getTimeInMillis());
	}
	public String  getDMYFormat(String dateString){

		try{
			SimpleDateFormat format = new SimpleDateFormat(SDF_SHORT_DATE_YMD);
			Date date =format.parse(dateString);
			SimpleDateFormat sdfDestination = new SimpleDateFormat(SDF_SHORT_DATE_DMY);
			dateString = sdfDestination.format(date);

		}catch(ParseException e){
			logger.info(AppConstants.TECHNICAL_ERROR,e);
			return null;
		}
		return dateString;
	}

	public static Date indianDate(String timezone)
	{
		if(timezone==null || timezone.equals("")){
			timezone="IST";
		}
		Date theResult = null;
		try {  

			theResult = new SimpleDateFormat(SDF_SHORT_DATE_DMY_HM).parse(stringIndainDate(timezone));
			return theResult;
		} catch (ParseException e) {
			logger.info(AppConstants.TECHNICAL_ERROR,e);
			return null;
		}

	}
	public static String stringIndainDate(String timezone) {
		String date=null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(SDF_SHORT_DATE_DMY_HM); 
			dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
			date =dateFormat.format(new Date());

			return date;
		} catch (Exception e) {
			logger.info(AppConstants.TECHNICAL_ERROR,e);
		}
		return date;		  
	}
	public static String stringIndainDate() {
		String date=null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(SDF_SHORT_DATE_DMY_HM); 
			dateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
			date =dateFormat.format(new Date());
			return date;
		} catch (Exception e) {
			logger.info(AppConstants.TECHNICAL_ERROR,e);
		}
		return date;		  
	}
	public static String indainDateOnly()
	{
		String date=null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(SDF_SHORT_DATE_YMD); 
			dateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
			date =dateFormat.format(new Date());
			return date;
		} catch (Exception e) {
			logger.info(AppConstants.TECHNICAL_ERROR,e);
		}
		return date;		  
	}	


	public static Date convertFullstringToDate(String userdate){
		Date date=null;
		try {
			date = new SimpleDateFormat(SDF_FULL_DATE).parse(userdate);
		} catch (ParseException e) {
			logger.info(AppConstants.TECHNICAL_ERROR,e);
		}
		return date;
	}


	public static Date convertStringToDateDMY(String userdate){
		Date date=null;
		try {
			date = new SimpleDateFormat(SDF_SHORT_DATE_DMY).parse(userdate);
		} catch (ParseException e) {
			logger.info(AppConstants.TECHNICAL_ERROR,e);
		}
		return date;
	}

	public static Date convertstringToDates(String userdate){
		Date date=null;
		try {
			date = new SimpleDateFormat(SDF_SHORT_DATE_DMY_HM).parse(userdate);
		} catch (ParseException e) {
			logger.info(AppConstants.TECHNICAL_ERROR,e);
		}
		return date;
	}


	public static Date stringToInvoiceDate(String userdate){
		Date date=null;
		try {
			date = new SimpleDateFormat(SDF_SHORT_DATE_YMD).parse(userdate);
		} catch (ParseException e) {
			logger.info(AppConstants.TECHNICAL_ERROR,e);
		}
		return date;
	}	

	public static String convertDatetoStringFormatDateMonth(Date userdate){
		String date=null;
		date = new SimpleDateFormat(DATE_MONTH).format(userdate);
		return date;
	}

	public static String convertFullDatetoString(Date userdate){

		long timestamp = userdate.getTime();
		Calendar cl = Calendar.getInstance(Locale.getDefault());
		cl.setTimeInMillis(timestamp); //here your time in miliseconds
		String convertDate = null;

		Timestamp ts = new Timestamp(timestamp);
		Date date = new Date(ts.getTime());
		convertDate = new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(date);

		DateFormat gmtFormat = new SimpleDateFormat();
		TimeZone gmtTime = TimeZone.getTimeZone("GMT");
		gmtFormat.setTimeZone(gmtTime);
		return convertDate;
	}

	public static String convertDateMonthYeartoString(Date userdate){
		String date=null;
		date = new SimpleDateFormat(DATE_MONTH_YEAR).format(userdate);
		return date;
	}




	public static String convertFullDatetoStringFormat(Date userdate){
		String date=null;
		date = new SimpleDateFormat(SDF_FULL_DATE).format(userdate);
		return date;
	}

	public static String convertDatetoStringformat(Date userdate){
		String date=null;
		date = new SimpleDateFormat(DATE_TO_STRING).format(userdate);
		return date;
	}


	public static double getTwodigitAmount(Double amount){
		DecimalFormat df = new DecimalFormat("#.##");
		return Double.parseDouble(df.format(amount));
	}

	public static int getDay(Date date){
		int value=0;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		value = cal.get(Calendar.DATE);
		return value;
	}

	public static int getNextMonth(Date date){
		int value=0;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH,1); 
		value = cal.get(Calendar.MONTH);
		value=value+1;
		return value;
	}
	public static Date getPervious(Date date){
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.MONTH,-1); 
		return cal.getTime();
	}
	public static int getYear(Date date){
		int value=0;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		value = cal.get(Calendar.YEAR);

		return value;
	}

	public static int getMonth(Date date){
		int value=0;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		value = cal.get(Calendar.MONTH);
		value = value+1;
		return value;
	}


	public static int dateCompare(Date date1,Date date2) 
	{  		
		int diff=0;
		try{
			diff=date1.compareTo(date2);
		}catch (Exception e) {
			logger.info(AppConstants.TECHNICAL_ERROR,e);
		}
		return diff;		  
	}
	public static Date indaindate()
	{
		Date theResult = null;
		try {
			Calendar currentdate = Calendar.getInstance();
			String strdate = null;
			DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			strdate = formatter.format(currentdate.getTime());
			TimeZone obj = TimeZone.getTimeZone("IST");
			theResult = formatter.parse(strdate);
			formatter.setTimeZone(obj);
		} catch (ParseException e) {
			logger.info(AppConstants.TECHNICAL_ERROR,e);
		}
		return theResult;		  
	}

	public static int getNextYear(Date date){
		int value=0;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		value = cal.get(Calendar.YEAR);
		value = value+1;
		return value;
	}

	@SuppressWarnings("deprecation")
	public static BigDecimal truncateDecimal(double x,int numberofDecimals)
	{
		if ( x > 0) {
			return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_FLOOR);
		} else {
			return new BigDecimal(String.valueOf(x)).setScale(numberofDecimals, BigDecimal.ROUND_CEILING);
		}
	}
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public static Date convertStringtoTime(String time) throws ParseException{
		return new SimpleDateFormat(DATE_FORMAT_HMS).parse(time);
	}

	public static Date convertStringtoTimeNew(String time) throws ParseException{
		return new SimpleDateFormat(DATE_FORMAT_HM).parse(time);
	}


	public static Date convertWebStringtoTime(String time) throws ParseException{
		return new SimpleDateFormat(WEB_SDF).parse(time);
	}

	public static Date currentHours() throws ParseException{
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss"); 
		dateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		String date =dateFormat.format(new Date());

		return dateFormat.parse(date);
	}
	public static Date indianYesterDayDate(String timezone)
	{
		if(timezone==null || timezone.equals("")){
			timezone="IST";
		}
		Date theResult = null;
		try {  

			theResult = new SimpleDateFormat(SDF_SHORT_DATE_DMY_HM).parse(stringIndainYesterday(timezone));
			return theResult;
		} catch (ParseException e) {
			logger.info(AppConstants.TECHNICAL_ERROR,e);
			return null;
		}

	}
	public static String stringIndainYesterday(String timezone)
	{
		String date=null;
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(SDF_SHORT_DATE_DMY_HM); 
			dateFormat.setTimeZone(TimeZone.getTimeZone(timezone));
			final Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -1);
			date =dateFormat.format(cal.getTime());

			return date;
		} catch (Exception e) {
			logger.info(AppConstants.TECHNICAL_ERROR,e);
		}
		return date;		  
	}

	public static String getFullDate(Date userdate){
		String date=null;
		try {
			date = new SimpleDateFormat(SDF_SHORT_DATE_DMY_HM).format(userdate);
		} catch (Exception e) {
			logger.info(AppConstants.TECHNICAL_ERROR,e);
		}
		return date;
	}

	public static String convertToCurrentTimeZone(String date) {
		String convertedDate = "";
		try {
			if (!date.contains("/")) {
				DateFormat utcFormat = new SimpleDateFormat(SDF_SHORT_DATE_DMY_HM);
				utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				Date format = utcFormat.parse(date);
				DateFormat currentTFormat = new SimpleDateFormat(SDF_SHORT_DATE_DMY_HM);
				currentTFormat.setTimeZone(TimeZone.getTimeZone(getCurrentTimeZone()));
				convertedDate = currentTFormat.format(format);
			}
		} catch (Exception e) {
			logger.info(AppConstants.TECHNICAL_ERROR,e);
		}
		return convertedDate;
	}

	public static String getCurrentTimeZone() {
		TimeZone tz = Calendar.getInstance().getTimeZone();
		return tz.getID();
	}

	public static String dateToString( Date dateTime) {
		return new SimpleDateFormat(SDF_SHORT_DATE_DMY_HM).format(dateTime);
	}

	public static Date stringToDate(String dateTime) {
		Date date = null;
		if (dateTime != null && !dateTime.equals("")) {
			SimpleDateFormat formatter = new SimpleDateFormat(SDF_SHORT_DATE_DMY_HM);
			try {
				date = formatter.parse(dateTime);
			} catch (java.text.ParseException ex) {
				logger.info(AppConstants.TECHNICAL_ERROR,ex);
			}
		}
		return date;
	}

	public static String stringToDateFormat(String dateformate)
	{   String dateString=null;
	Date date=null;
	try {
		date = new SimpleDateFormat(SDF_SHORT_DATE_DMY).parse(dateformate);
		dateString= new SimpleDateFormat(SDF_SHORT_DATE_YMD).format(date);
		return dateString;
	} catch (ParseException e) {
		logger.info(AppConstants.TECHNICAL_ERROR,e);
	}
	return dateString;    
	}

	public static String convertSettlementDatetoString(Date userdate){
		String date=null;
		date = new SimpleDateFormat(SDF_SETTLEMENT).format(userdate);
		return date;
	}

	public static String convertDateToString(LocalDate userdate){
		String date=null;
		date = new SimpleDateFormat(SDF_SHORT_DATE_YMD).format(userdate);
		return date;
	}

	public static String convertDateToStringYMD(Date userdate){
		String date=null;
		date = new SimpleDateFormat(SDF_SHORT_DATE_YMD).format(userdate);
		return date;
	}

	public static String convertDateToStringDMY(Date userdate){
		String date=null;
		date = new SimpleDateFormat(SDF_SHORT_DATE_DMY).format(userdate);
		return date;
	}

	public static String generateDateFormat(Date userdate){
		String date=null;
		date = new SimpleDateFormat(SDF_MMDDYYYYHHMMSS).format(userdate);
		return date;
	}

	public static String decimalFormat(double amount)
	{
		final DecimalFormat df = new DecimalFormat("#,##0.00");
		df.setRoundingMode(RoundingMode.DOWN);
		return df.format(amount);
	}
	
	
	
	//Seconds difference between two dates
	public static long secondsDifferenceCalculator(Date requestDatetime,Date currentDatetime) {
		
		    Calendar cal1 = Calendar.getInstance();  
	        Calendar cal2 = Calendar.getInstance();  
	        cal1.setTime(requestDatetime);  
	        cal2.setTime(currentDatetime);  
	        // calculate the time difference in milliseconds  
	        long diff = cal2.getTimeInMillis() - cal1.getTimeInMillis();  
	        // convert the time difference to seconds  
	        long diffSeconds = diff / 1000;  
	        System.out.println("Time difference in seconds: " + diffSeconds); 
	        
	        return diffSeconds;
	}
	
	//DATE CONVERSIONS
	
	public static LocalDate convertDateToLocalDateViaSql(Date dateToConvert) {
	    return new java.sql.Date(dateToConvert.getTime()).toLocalDate();
	}
	
	public static Date convertLocaldateToDateViaSqlDate(LocalDate dateToConvert) {
	    return java.sql.Date.valueOf(dateToConvert);
	}

	public static LocalDate stringToLocalDate(String dateInString) {
		
		String localDate = FileUtils.stringTolocalDateStructure(dateInString);
		
	    return LocalDate.parse(localDate);
	}
	
	public static LocalDate convertUtilDateToLocalDate(Date dateToConvert) {
	    return dateToConvert.toInstant()
	      .atZone(ZoneId.systemDefault())
	      .toLocalDate();
	}
	

	
	
	
	
	//OVERLAP DETECTION - RETURNS TRUE IF OVERLAP DETECTED
    public static boolean localDateOverLapDetector(LocalDate newStartDate, LocalDate newEndDate, LocalDate alreadyExistingStartDate, LocalDate alreadyExistingEndDate) {
        return !(newEndDate.isBefore(alreadyExistingStartDate) || newStartDate.isAfter(alreadyExistingEndDate));
    }
	
	
	
	
}
