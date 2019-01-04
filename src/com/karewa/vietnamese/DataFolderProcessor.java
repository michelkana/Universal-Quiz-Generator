package com.karewa.vietnamese;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

import com.karewa.vietnamese.R.raw;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

public class DataFolderProcessor {
	private String dataFolderPath;
	private String dataFilename;
	private static final int MAX_HEADER = 16;
	private static final int MAX_LINE = 4096;
	private static final int MAX_AUDIO = 4096;
	private static final int MAX_IMAGE = 4096;
	private static final int MAX_QUIZ = MAX_HEADER * (MAX_HEADER-1);
	private String[][] dataHeaders = new String[MAX_HEADER][3];
	private int dataHeadersLength = 0;
	private String[][] audioFilenames = new String[MAX_LINE][2];
	private int audioFilenamesLength = 0;
	private String[][] imageFilenames = new String[MAX_IMAGE][2];
	private int imageFilenamesLength = 0;
	private String[][] db = new String[MAX_LINE][MAX_HEADER];
	private String[][] dbOriginal = new String[MAX_LINE][MAX_HEADER];
	private int dbLength = 0;
	private int dbLengthOriginal = 0;
	private String parsingResults ="";
	private String[][] quizSetup = new String[MAX_QUIZ][2];
	private int quizSetupLength = 0;
	private String currentPool = "";
    int indexGroupHeader = 0;
    int indexScoreHeader = 0;
    boolean existsScoreHeader = false;
    boolean existsGroupHeader = false;
    Context context = null;
    boolean embeddedRessource = false;
    
	public DataFolderProcessor(String path){
		dataFolderPath = path;
		embeddedRessource = false;
	}
	
	public DataFolderProcessor(Context c){
		context = c;
		embeddedRessource = true;
	}	
	
	public boolean getEmbeddedRessource(){
		return embeddedRessource;
	}
	
	public boolean isValidDataFolder(){
		if (context != null)
			return true;
        File dir = new File(dataFolderPath);
        String[] files = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".csv") || name.toLowerCase().endsWith(".txt");
            }
        });
        if (files == null || files.length==0 || files.length>1){
        	parsingResults += "SYNTAX ERROR: the data folder contains none or more than one csv or txt file " + System.getProperty("line.separator");
        	return false;
        }        	
        dataFilename = dataFolderPath + "/" +files[0];
        files = dir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return ! name.toLowerCase().endsWith(".csv") && ! name.toLowerCase().endsWith(".txt") && ! name.toLowerCase().endsWith(".bak")  
                    && ! name.toLowerCase().endsWith(".jpg") && ! name.toLowerCase().endsWith(".gif") && ! name.toLowerCase().endsWith(".png") && ! name.toLowerCase().endsWith(".bmp") && ! name.toLowerCase().endsWith(".webp") 
                	&& ! name.toLowerCase().endsWith(".mp3") && ! name.toLowerCase().endsWith(".3gp") && ! name.toLowerCase().endsWith(".wav") && ! name.toLowerCase().endsWith(".flac") && ! name.toLowerCase().endsWith(".aac");
            }
        });
        if (files.length>0){ 
        	parsingResults += "SYNTAX ERROR: the folder contains files other than text, image or audio " + System.getProperty("line.separator");
        	return false;
    	}        	
		return true;
	}
	public String getDataFolderPath(){
		return dataFolderPath;
	}
	public void parseDataHeaders(){
        try {
        	// Open the data file
        	BufferedReader bufRdr;
        	if (context != null){
        		bufRdr  = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.words_text)));
        	}else{
        		bufRdr = new BufferedReader(new InputStreamReader(new FileInputStream(dataFilename)));   
        	}
			String headersLine;
			if ((headersLine = bufRdr.readLine()) != null){ // Read the first line
				// Expected format is {Field1 Role}{Field1 Name}{Field1 Type}|{Field2 Role}{Field2 Name}{Field2 Type}
				// Field Role is either SRC, DEST, SRC/DEST,  CAT, GRP or SCORE
				// Field Name is any string
				// Field Type is either TXT, MCQ, AUDIO, IMG, GRP, CAT
				StringTokenizer st = new StringTokenizer(headersLine,"|");
				if (st.countTokens()==0){
            		parsingResults += "SYNTAX ERROR: the header line is empty " + System.getProperty("line.separator");					
				}else{
		            int row = 0;
		            int countGroupHeaders=0;
		            int countScoreHeaders=0;
		            while (st.hasMoreTokens())     
		            {     
		            	String headerField[] = st.nextToken().replaceAll("\\}\\{", "~").replaceAll("\\{", "").replaceAll("\\}", "").split("~");
		            	if (headerField.length!=3){
		            		parsingResults += "SYNTAX ERROR: the header " + row + " does not have 3 fields " + System.getProperty("line.separator");							            		
		            	}else{  
			            	String headerRoleSubField = headerField[0];
			            	String headerNameSubField = headerField[1];
			            	String headerTypeSubField = headerField[2];  
			            	if (! (headerRoleSubField.equals("SRC") || headerRoleSubField.equals("DEST") || headerRoleSubField.equals("SRC/DEST") || headerRoleSubField.equals("CAT") || headerRoleSubField.equals("GRP") || headerRoleSubField.equals("SCORE"))){
			            		parsingResults += "SYNTAX ERROR: the first field of header " + row + "has an unsupported value. Field Role should be either SRC, DEST, SRC/DEST,  CAT, GRP or SCORE" + System.getProperty("line.separator");
			            	}
			            	if (! (headerTypeSubField.equals("TXT") || headerTypeSubField.equals("MCQ") || headerTypeSubField.equals("AUDIO") || headerTypeSubField.equals("IMG") || headerTypeSubField.equals("CAT") || headerRoleSubField.equals("GRP"))){
			            		parsingResults += "SYNTAX ERROR: the first field of header " + row + "has an unsupported value. Field Type should be either TXT, MCQ, AUDIO, IMG, CAT or GRP " + System.getProperty("line.separator");
			            	}
			            	if (headerRoleSubField.equals("SCORE")){
			            		existsScoreHeader = true;
			            		indexScoreHeader = row;
			            		countScoreHeaders++;
			            		if (countScoreHeaders>1){
			            			parsingResults += "SYNTAX ERROR: More than one header of field role SCORE were found" + System.getProperty("line.separator");
			            		}
			            		if(! headerTypeSubField.equals("TXT")){
			            			parsingResults += "SYNTAX ERROR: A header of field role SCORE should have the type TXT" + System.getProperty("line.separator");
			            		}
			            	}
			            	if (headerRoleSubField.equals("GRP")){
			            		existsGroupHeader = true;
			            		indexGroupHeader = row;
			            		countGroupHeaders++;
			            		if (countGroupHeaders>1){
			            			parsingResults += "SYNTAX ERROR: More than one header of field type GRP were found" + System.getProperty("line.separator");
			            		}
			            		if(! headerTypeSubField.equals("GRP")){
			            			parsingResults += "SYNTAX ERROR: A header of field role GRP should have the type GRP" + System.getProperty("line.separator");
			            		}
			            	}		
			            	if (headerTypeSubField.equals("MCQ") && ! headerRoleSubField.equals("DEST") ){ 
			            		parsingResults += "SYNTAX ERROR: A header of field type MCQ should have the role DEST" + System.getProperty("line.separator");
			            	}
			            	if (parsingResults.indexOf("ERROR")==-1){
			            		dataHeaders[row][0] = headerRoleSubField;
			            		dataHeaders[row][1] = headerNameSubField;
			            		dataHeaders[row][2] = headerTypeSubField;
			            		row++;     
			            	}
		            	}
		            }
		            if (! existsScoreHeader){
			            dataHeaders[row][0] = "SCORE";
	            		dataHeaders[row][1] = "Score";
	            		dataHeaders[row][2] = "TXT";
	            		indexScoreHeader = row;
			            row++;
		            }
		            if (! existsGroupHeader){
			            dataHeaders[row][0] = "GRP";
	            		dataHeaders[row][1] = "Group";
	            		dataHeaders[row][2] = "GRP";
	            		indexGroupHeader = row;
			            row++;
		            }
		            dataHeadersLength = row;
				}
				if (parsingResults.indexOf("ERROR")!=-1){
            		dataHeaders = null;
            	}
		        bufRdr.close();
			}
			
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	public int getCountDataHeadersFound(){
		return dataHeadersLength;
	}
	public String[][] getDataHeaders(){
		return dataHeaders;
	}
	public void parseTextData(){
		String[] lineFields;
        try {
	 		// Open the data file 
        	BufferedReader bufRdr;
        	if (context != null){
        		bufRdr  = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.words_text)));
        	}else{
        		bufRdr = new BufferedReader(new InputStreamReader(new FileInputStream(dataFilename)));   
        	}
			String line;
			line = bufRdr.readLine(); // Skip the headers line
			int row = 1;
			while ((line = bufRdr.readLine()) != null){ // loop until the end of file
				lineFields = line.split("\\|"); 
				// check number if number of fields corresponds to the number of headers
	            if (lineFields.length + (! existsScoreHeader?1:0) + (! existsGroupHeader?1:0) != dataHeadersLength){
	            	parsingResults += "SYNTAX ERROR: unexpected number of fields found in row " + row + " " + System.getProperty("line.separator");
	            }else{
		            for (int i=0; i<dataHeadersLength; i++){
		            	if (dataHeaders[i][2].equals("MCQ")){ // the field Nr i contain multiple choices
		            		String mcqStr = lineFields[i];
		            		String[] mcq = mcqStr.split("~");
		            		if (mcq.length != 3){
		            			parsingResults += "SYNTAX ERROR: unexpected number of multiple choices found in row " + row + " on field " + i + ". 3 multiple choices are expected." + System.getProperty("line.separator");
		            		}
		            	}
		            }
	            }
	            row++;
			}					
			bufRdr.close();
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	public void parseAudioData(){
		String audioInformation;
		String[] lineFields;
		String audioFile;
		String audioTiming;
        try {
    		for (int i=0; i<dataHeadersLength; i++){
    			if (dataHeaders[i][2].equals("AUDIO")){ // the field Nr i will contain audio information
 		        	// Open the data file in order to fetch audio information
    				BufferedReader bufRdr;
    	        	if (context != null){
    	        		bufRdr  = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.words_text)));
    	        	}else{
    	        		bufRdr = new BufferedReader(new InputStreamReader(new FileInputStream(dataFilename)));   
    	        	}
					//BufferedReader bufRdr = new BufferedReader(new InputStreamReader(new FileInputStream(dataFilename)));        			
					String line;
					line = bufRdr.readLine(); // Skip the headers line
					int row = 1;
					while ((line = bufRdr.readLine()) != null){ // loop until the end of file
						lineFields = line.split("\\|");   
			            audioInformation = lineFields[i];
			            //audio information has the format AudioFilename,StartTime-Duration
			            //e.g. "lesson1.mp3,16-2" refers to the portion of audio from file lesson1.mp3 starting at the 16th second, duration 2 seconds
			            if (audioInformation.isEmpty()){
			            	parsingResults += "WARNING: audio information was expected on line " + row + " and field " + i + " but was not found " + System.getProperty("line.separator");
			            }else{
			            	lineFields = audioInformation.split(",");			            	
			            	if (lineFields.length!=2){
				            	parsingResults += "SYNTAX ERROR: either audio file name or audio timing is not correctly defined on line " + row + " for field " + i + System.getProperty("line.separator");
				            }else{
				            	audioFile = lineFields[0];
				            	audioTiming= lineFields[1];
				            	lineFields = audioTiming.split("-");
				            	if (lineFields.length!=2){
					            	parsingResults += "SYNTAX ERROR: audio timing is not correctly defined on line " + row + " for field " + i + System.getProperty("line.separator");				            		
				            	}else if (! isInteger(lineFields[0]) || ! isInteger(lineFields[1])){
					            	parsingResults += "SYNTAX ERROR: audio start time or duration is not a number on line " + row + " for field " + i + System.getProperty("line.separator");
				            	}else{
				            		File file = new File(dataFolderPath+"/"+audioFile);
				                    if (! file.exists() && context == null){
						            	parsingResults += "SYNTAX ERROR: audio file referred on row " + row + " and field " + i + " was not found on the file system " + System.getProperty("line.separator");				            						                    	
				                    }else if (! inArray(audioFilenames,audioFilenamesLength,audioFile) ){
				                    	audioFilenames[audioFilenamesLength][0]=audioFile;
				                    	audioFilenamesLength++;
				                    }
				            	}
				            }
			            }				        
					}
					bufRdr.close();
    			}
    		}	
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	private boolean inArray(String[][] arr, int size, String str) {
		for (int i=0; i<size; i++){
			if (str.equals(arr[i][0]))
				return true;			
		}
		return false;
	}
	public String[][] getAudioFilenames(){
		return audioFilenames;
	}
	public void parseImageData(){
		String[] lineFields;
		String imageFile;
        try {
    		for (int i=0; i<dataHeadersLength; i++){
    			if (dataHeaders[i][2].equals("IMG")){ // the field Nr i will contain image information
 		        	// Open the data file in order to fetch audio information
    				BufferedReader bufRdr;
    	        	if (context != null){
    	        		bufRdr  = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.words_text)));
    	        	}else{
    	        		bufRdr = new BufferedReader(new InputStreamReader(new FileInputStream(dataFilename)));   
    	        	}
					//BufferedReader bufRdr = new BufferedReader(new InputStreamReader(new FileInputStream(dataFilename)));        			
					String line;
					line = bufRdr.readLine(); // Skip the headers line
					int row = 1;
					while ((line = bufRdr.readLine()) != null){ // loop until the end of file
						lineFields = line.split("\\|");   
						imageFile = lineFields[i];
			            //image information equals "ImageFilename"
			            if (imageFile.isEmpty()){
			            	parsingResults += "ERROR: image filename was expected on line " + row + " and field " + i + " but was not found " + System.getProperty("line.separator");
			            }else{			            	
			            	File file = new File(dataFolderPath+"/"+imageFile);
		                    if (! file.exists() && context != null){
					            	parsingResults += "SYNTAX ERROR: image file referred on row " + row + " and field " + i + " was not found on the file system " + System.getProperty("line.separator");				            						                    	
			                    }else if (! Arrays.asList(imageFilenames).contains(imageFile)){
			                    	imageFilenames[imageFilenamesLength][0]=imageFile;
			                    	imageFilenamesLength++;
			                    }
				            }
					}					
					bufRdr.close();
    			}
    		}	
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	public String[][] getImageFilenames(){
		return imageFilenames;
	}
	public int getCountAudioFilesFound(){
		return audioFilenamesLength;
	}
	public int getCountImageFilesFound(){
		return imageFilenamesLength;
	}
	public int getCountQuizSetupFound(){
		return quizSetupLength;
	}
	public int getCountLinesFound(){
		return dbLength;
	}
	
	public void parseData(){
		if (isValidDataFolder()){
			parseDataHeaders();
			if (parsingResults.indexOf("ERROR")==-1){
				parseTextData();
				if (parsingResults.indexOf("ERROR")==-1){
					parseAudioData();
					if (parsingResults.indexOf("ERROR")==-1){
						parseImageData();	
						if (parsingResults.indexOf("ERROR")==-1){
							parsingResults = "SYNTAX OK";					
						}
					}
				}
			}
		}
	}
	public String getParsingResults(){
		return parsingResults;
	}	
	public void loadData(){ 
		if (parsingResults.equals(""))
			parseData();
		if (parsingResults.indexOf("ERROR")==-1){     
			try {
		 		// Open the data file
				BufferedReader bufRdr;
	        	if (context != null){
	        		bufRdr  = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.words_text)));
	        	}else{
	        		bufRdr = new BufferedReader(new InputStreamReader(new FileInputStream(dataFilename)));   
	        	}
				//BufferedReader bufRdr = new BufferedReader(new InputStreamReader(new FileInputStream(dataFilename)));        			
				String line;
				line = bufRdr.readLine(); // Skip the headers line
				int row = 0;
	            int col = 0;
				while ((line = bufRdr.readLine()) != null){ // loop until the end of file
					StringTokenizer st = new StringTokenizer(line,"|");   
		            col = 0;
		            while (st.hasMoreTokens())     
		            {     
		            	db[row][col] = st.nextToken();
		            	col++;
		            }	
		            if (db[row][indexScoreHeader] == null){
		            	db[row][indexScoreHeader] = "0";
		            }
		            if (db[row][indexGroupHeader] == null){
		            	db[row][indexGroupHeader] = "ALL";
		            }
		            row++;     
				}		
				dbLength = row;
				bufRdr.close();
	        } catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}	
	        
	        dbOriginal = db;
	        dbLengthOriginal = dbLength;
	        
	        for (int i=0; i<dataHeadersLength; i++){
	        	if (dataHeaders[i][0].equals("SRC") || dataHeaders[i][0].equals("SRC/DEST")){
	        		for (int j=0; j<dataHeadersLength; j++){
	                	if (i!=j && (dataHeaders[j][0].equals("DEST") || dataHeaders[j][0].equals("SRC/DEST")) ){
	                		if (! Arrays.asList(quizSetup).contains(i + " - " + j)){
		                    	quizSetup[quizSetupLength][0] = i + "-" + j;
		                    	quizSetup[quizSetupLength][1] = dataHeaders[i][1]+ " -> " + dataHeaders[j][1];
		                    	quizSetupLength++;
		                    }
	                	}
	                }
	        	}
	        }
		}

	}
	public String[][] getData(){
		return db;
	}
	
	public String getDataFilename(){
		return dataFilename;
	}
	
	public void setData(String[][] data){
		db = data;
	}
	
	public void resetData(){
		db = dbOriginal;
		dbLength = dbLengthOriginal;
	}
	
	public void updateScoreData(int row, int increment){
		db[row][indexScoreHeader] = String.valueOf(Integer.valueOf(db[row][indexScoreHeader]) + increment);
		// get link to original row in case data was filtered
		String originalRowIndex = db[row][dataHeadersLength];
		if (originalRowIndex != null){
			dbOriginal[Integer.valueOf(originalRowIndex)][indexScoreHeader] = db[row][indexScoreHeader];
		}
	}
	
	public void setDataLength(int dataLength){
		dbLength = dataLength;
	}
	
	public String[][] getQuizSetup(){
		return quizSetup;
	}
	
	public String getCurrentPool(){
		return currentPool;
	}
	
	public void setCurrentPool(String p){
		currentPool = p;
	}
	
	public int getIndexScoreHeader(){
		return indexScoreHeader;
	}
	
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    // only got here if we didn't return false
	    return true;
	}
	
    static void shuffleArray(int[] ar)
    {
      Random rnd = new Random();
      for (int i = ar.length - 1; i > 0; i--)
      {
        int index = rnd.nextInt(i + 1);
        // Simple swap
        int a = ar[index];
        ar[index] = ar[i];
        ar[i] = a;
      }
    }
    
    private int getRandomInt(int minInt, int maxInt, int excludeInt1, int excludeInt2){
    	Random generator = new Random();
    	int index = minInt+generator.nextInt(maxInt);
    	int i=0;
    	while( (index == excludeInt1 || index == excludeInt2) && i<100){
    		index = minInt+generator.nextInt(maxInt);
    		i++;
    	}
    	return index;    		
    } 
    
    public String[] generate_quiz_vector(int quizSize, int answerHeaderIndex) {
    	Random generator = new Random();
    	String[] quizVector = new String[quizSize];

    	int[] dbIndexArrayShuffled = new int[dbLength];
    	for(int i=0; i<dbLength; i++){
    		dbIndexArrayShuffled[i] = i;
    	}
    	shuffleArray(dbIndexArrayShuffled);
    	
    	int questionIndex;
    	int answer1Index;
    	int answer2Index;
    	int step = (int) dbLength/quizSize;
    	for (int i=0; i<quizSize; i++){
    		questionIndex = i*step; 
    		answer1Index = findNextAnswerIndex(questionIndex, questionIndex, dbIndexArrayShuffled, answerHeaderIndex, false);
    		answer2Index = findNextAnswerIndex(questionIndex, answer1Index, dbIndexArrayShuffled, answerHeaderIndex, false);
            switch (1+generator.nextInt(3)){
	            case 1: quizVector[i] = dbIndexArrayShuffled[questionIndex] + "-" + dbIndexArrayShuffled[questionIndex] + "-" + dbIndexArrayShuffled[answer1Index] + "-" + dbIndexArrayShuffled[answer2Index];
			    		break;
	            case 2: quizVector[i] = dbIndexArrayShuffled[questionIndex] + "-" + dbIndexArrayShuffled[answer1Index] + "-" + dbIndexArrayShuffled[questionIndex] + "-" + dbIndexArrayShuffled[answer2Index];
			    		break;
	            case 3: quizVector[i] = dbIndexArrayShuffled[questionIndex] + "-" + dbIndexArrayShuffled[answer1Index] + "-" + dbIndexArrayShuffled[answer2Index] + "-" + dbIndexArrayShuffled[questionIndex];
			    		break;			    		
            } 
    	}    	
		return quizVector;
	}
    
    private int findNextAnswerIndex(int questionIndex, int answer1Index, int[] indexArray, int answerHeaderIndex, boolean findSimilar){
		String quizGroup = db[indexArray[questionIndex]][indexGroupHeader];
		int i=0;
		int answerIndex = questionIndex;		
		if (! findSimilar){
			boolean found = false;
	    	while (i<dbLength && ! found){
	    		int index = getRandomInt(0, dbLength-1, questionIndex, answer1Index);
	    		String grp = db[indexArray[index]][indexGroupHeader];
				if (quizGroup.equals(grp)){
					found = true;
					answerIndex = index;
				}
				i++;
			}  
		}else{
			String questionStr = decomposeString(db[indexArray[questionIndex]][answerHeaderIndex]);
			int lvDist1 = 100;
        	int lvDist2 = 100;
			while (i<dbLength){
	    		String grp = db[indexArray[i]][indexGroupHeader];
	    		String answerStr = decomposeString(db[indexArray[i]][answerHeaderIndex]);
	    		lvDist2 = LevenshteinDistance.computeLevenshteinDistance(answerStr, questionStr);
				if (i != questionIndex && i!= answer1Index && quizGroup.equals(grp) && lvDist2 < lvDist1){
					lvDist1 = lvDist2;
	    			answerIndex = i;
				}
				i++;
			} 
		}
		return answerIndex;
    }
   
    public static String decomposeString(String s) {
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
    }
    
    public void saveDataToFile(){
    	try {
				BufferedWriter bufWr = new BufferedWriter(new FileWriter(new File(dataFilename), false));  
				for (int i=0; i<dataHeadersLength-1; i++){
					bufWr.write("{" + dataHeaders[i][0] + "}"+ "{" + dataHeaders[i][1] + "}" + "{" + dataHeaders[i][2] + "}|");
		        }
				bufWr.write("{" + dataHeaders[dataHeadersLength-1][0] + "}"+ "{" + dataHeaders[dataHeadersLength-1][1] + "}" + "{" + dataHeaders[dataHeadersLength-1][2] + "}");
				bufWr.newLine();
				for(int i=0; i<dbLengthOriginal; i++){
					for (int j=0; j<dataHeadersLength-1; j++){
						bufWr.write(dbOriginal[i][j] + "|");
			        }
					bufWr.write(dbOriginal[i][dataHeadersLength-1]);
					bufWr.newLine();
		    	}
		        bufWr.close();	
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static class LevenshteinDistance {
        private static int minimum(int a, int b, int c) {
                return Math.min(Math.min(a, b), c);
        }       
        public static int computeLevenshteinDistance(CharSequence str1,
                        CharSequence str2) {
        	int res = 10000;
        	try{
        		
	        	int[][] distance = new int[str1.length() + 1][str2.length() + 1];
	 
	                for (int i = 0; i <= str1.length(); i++)
	                        distance[i][0] = i;
	                for (int j = 1; j <= str2.length(); j++)
	                        distance[0][j] = j;
	 
	                for (int i = 1; i <= str1.length(); i++)
	                        for (int j = 1; j <= str2.length(); j++)
	                                distance[i][j] = minimum(
	                                                distance[i - 1][j] + 1,
	                                                distance[i][j - 1] + 1,
	                                                distance[i - 1][j - 1]
	                                                                + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
	                                                                                : 1));
	 
	                res = distance[str1.length()][str2.length()];   
        	}catch (Exception e) {
    		}
			return res;
        	
        }
    }
}
