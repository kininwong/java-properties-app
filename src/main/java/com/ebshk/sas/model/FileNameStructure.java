package com.ebshk.sas.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ebshk.sas.util.DateUtil;

public class FileNameStructure {
	private String filename;
	private String dateString;
	private Date date;
	
	public FileNameStructure() {
	}
	
	public FileNameStructure(String filename, String dateString) {
		
	}
	
	public FileNameStructure(String filename, String dateString, SimpleDateFormat sdf) {
		this.filename = filename;
		this.dateString = dateString;
		this.date = DateUtil.toDate(dateString, sdf);
	}
	
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getDateString() {
		return dateString;
	}
	public void setDateString(String dateString) {
		this.dateString = dateString;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "FileNameStructure [filename=" + filename + ", dateString=" + dateString + ", date=" + date + "]";
	}
	
	
}
