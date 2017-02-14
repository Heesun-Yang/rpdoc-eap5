package com.rp.test;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateTest {
	static Logger log = LoggerFactory.getLogger(DateTest.class);
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		Date now = new Date();

		//기본 Locale
		SimpleDateFormat format = new SimpleDateFormat("yyyy MMM dd hh:mm a");
		System.out.println(format.format(now));
		
		//EN Locale
		format = new SimpleDateFormat("yyyy MMM dd hh:mm a", Locale.ENGLISH);
		System.out.println(format.format(now));
		
	}
}
