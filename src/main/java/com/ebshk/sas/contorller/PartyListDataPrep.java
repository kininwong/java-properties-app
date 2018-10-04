package com.ebshk.sas.contorller;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ebshk.sas.model.FileNameStructure;

public class PartyListDataPrep {
	private static final String CONFIG_PATH = "Backup_config.properties";
	private static final String SAS_APP_NC_DIRECTORY_KEY = "sas_app_namecheck_directory";
	private static final String SAS_APP_NC_PARTYLIST_DIRECTORY_KEY = "sas_app_namecheck_landing_partylist_directory";
	private static final String SAS_APP_NC_TEMP_PARTYLIST_DIR_KEY = "temp_partylist_directory";
	private static final String DATA_ARC = "data/archive/";
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	Logger log = LoggerFactory.getLogger(PartyListDataPrep.class.getName());
	
	public void init() {
		Properties properties = new Properties();
		try {
			//properties.load(new FileReader(CONFIG_PATH));
			InputStream in = getClass().getClassLoader().getResourceAsStream(CONFIG_PATH);
			if(in == null) {
				log.debug("config file not found:{}", CONFIG_PATH);
				return;
			}
			properties.load(in);
			
			String namecheck_root = properties.getProperty(SAS_APP_NC_DIRECTORY_KEY);
			
			//archive directory: F:\SAS_APPLICATION_NAMECHECK\ncsc\data\archive\yyyymmdd\landing
			//feed downloaded partylist directory: F:\Shared_Folder\partylist
			//destinated partylist directory: F:\SAS_APPLICATION_NAMECHECK\ncsc\data\landing\partylist
			
			// where yyyymmdd is the latest date in the dir
			//1. find the latest date subdir under data/archive/
			//2. for each (*.xml)files in the latest date sas archive directory, 
			//3. get the next date file in feed downloaded partylist directory
			//4. copy the file found in step 2 to partylist directory 
			
			File dataArch = new File(namecheck_root.concat(DATA_ARC));
			File[] subdirs = dataArch.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
			File theNewestDir = null;
			if(subdirs.length > 0) {
				Arrays.sort(subdirs, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
				theNewestDir = subdirs[0];
			}
			
			log.info("theNewestDir_path: {}", theNewestDir.getPath());
			log.info("theNewestDir_absolute: {}", theNewestDir.getAbsolutePath());
			log.info("theNewestDir_canonical: {}", theNewestDir.getCanonicalPath());
			
			String landingPath = theNewestDir.getPath().concat("/landing");
			File partyList = new File(landingPath);
			
			File[] partyListFiles = partyList.listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".xml") && !name.startsWith("PFA2_");// && name.startsWith("ABrokersAny");
				}
			});
			
			List<File> finalfiles = new ArrayList<File>();
			
			for (File file : partyListFiles) {
				System.out.println(file.getName() + ":" + file.getName().substring(file.getName().length()-12, file.getName().length()-4));
				final FileNameStructure filenameDetails = extractFilenameAndDate(file);
				
				log.info("checking: {}", filenameDetails);

				File tempPath = new File(properties.getProperty(SAS_APP_NC_TEMP_PARTYLIST_DIR_KEY));
				File[] nextFile = tempPath.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.startsWith(filenameDetails.getFilename()) && !name.startsWith("PFA2_");
					}
				});

				//Sort the data files by the date string in the filename
				Arrays.sort(nextFile, new Comparator<File>() {
					@Override
					public int compare(File o1, File o2) {
						final FileNameStructure o1_filenameDetails = extractFilenameAndDate(o1);
						final FileNameStructure o2_filenameDetails = extractFilenameAndDate(o2);
						return o1_filenameDetails.getDate().compareTo(o2_filenameDetails.getDate());
					}
				});
				
				for (File targetFile : nextFile) {
					FileNameStructure filenameDetails2 = extractFilenameAndDate(targetFile);
					log.debug("####: " + filenameDetails2.getFilename() + ";" + filenameDetails2.getDateString() +";"+ filenameDetails2.getDate());
					if(filenameDetails2.getDate().after(filenameDetails.getDate())) {
						finalfiles.add(targetFile);
						log.info("Next Date data file found: {}", targetFile);
						break;
					}
				}
			
			}
			
			//Copy file
			copyFiles(finalfiles, properties.getProperty(SAS_APP_NC_PARTYLIST_DIRECTORY_KEY));
			// e.g. 
			// ABrokersAnywhere_Daily_yyyymmdd.xml, adsr-daily-yyyymmdd.xml, BrokersAnywhere_Daily_yyyymmdd.xml
			// FORTEX-DAILY-20180827.xml, GMI-DAILY-20180827.xml, GP-DAILY-20180827.xml, IASIA_GD-DAILY-20180827.xml
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//List<File> finalfiles = new ArrayList<File>();
	private void copyFiles(List<File> finalfiles, String destDirString) {
		File destDir = new File(destDirString);
		for (File srcFile : finalfiles) {
			try {
				FileUtils.copyFileToDirectory(srcFile, destDir, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private FileNameStructure extractFilenameAndDate(File file) {
		String fileName = file.getName().substring(0, file.getName().length()-12);
		String dateString = file.getName().substring(file.getName().length()-12, file.getName().length()-4);
		FileNameStructure filenameStructure = new FileNameStructure(fileName, dateString, sdf);
		return filenameStructure;
	}

}
