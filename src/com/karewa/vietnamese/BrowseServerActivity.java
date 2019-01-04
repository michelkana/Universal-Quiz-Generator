package com.karewa.vietnamese;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.karewa.vietnamese.R.drawable;
import com.karewa.vietnamese.R.id;
import com.karewa.vietnamese.R.layout;
import com.karewa.vietnamese.R.style;
 
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/*
 * http://codeoncloud.blogspot.cz/2013/07/android-mysql-php-json-tutorial.html
 * http://theopentutorials.com/tutorials/android/listview/android-custom-listview-with-image-and-text-using-arrayadapter/
 * http://android-developers.blogspot.cz/2009/02/android-layout-tricks-1.html
 * http://www.josecgomez.com/2010/05/03/android-putting-custom-objects-in-listview/
 * http://blog-emildesign.rhcloud.com/?p=495
 */
public class BrowseServerActivity extends ListActivity {
	ArrayList<RepositoryItemData> repositoryItemsList;
	private String jsonResult; 
	private String server = "www.karewa.com";
	//private String server = "10.0.0.6";
	private String url = "http://" + server + "/quiz/get_subcategory.php";
	String cat_id = "0";
	private String urlQuizData = "http://" + server + "/quiz/get_quizdata.php";
	private String urlQuizFiles = "http://" + server + "/quiz";
	private String folder_location = "";
	private static final int MAX_FILES = 1024;
	ProgressDialog progressDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    //setContentView(R.layout.main);
	    
	    Bundle extras = getIntent().getExtras();
	    if(extras != null) {
	        cat_id = extras.getString("cat_id");
	    }
	    
	    accessWebService();
	    
	    //Toast.makeText(getApplicationContext(),repositoryItemsList.size(), Toast.LENGTH_LONG).show();
	    
	    //RepositoryItemArrayAdapter repositoryItemArrayAdapter = new RepositoryItemArrayAdapter(this, R.id.tv_item_name, repositoryItemsList);
	    
	    /*ArrayAdapter<String> repositoryItemArrayAdapter = new ArrayAdapter<String>(this, 
	            android.R.layout.simple_list_item_1, 
	            android.R.id.text1, 
	            new String[] { "Android", "iPhone", "WindowsMobile",
	                    "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
	                    "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
	                    "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
	                    "Android", "iPhone", "WindowsMobile" });

	    this.setListAdapter(repositoryItemArrayAdapter);*/
	    
	    progressDialog = new ProgressDialog(BrowseServerActivity.this);
	    progressDialog.setMessage("Download progress");
	    progressDialog.setIndeterminate(true);
	    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    progressDialog.setCancelable(true);
	    

	}

	@Override
	protected void onListItemClick(ListView l, final View v, int position, long id) {
	    super.onListItemClick(l, v, position, id);	    
	    RepositoryItemData item = repositoryItemsList.get(position);
	    if (item.getItemType().equals("category")){
		    Intent mViewChaptersIntent = new Intent(this, BrowseServerActivity.class);
			mViewChaptersIntent.putExtra("cat_id", item.getItemId());
		    startActivity(mViewChaptersIntent);
	    }
	}
	
	 // Async Task to access the web
	 private class JsonReadTask extends AsyncTask<String, Void, String> {
		 String taskType = "browseTask";
		 String destinationDir = "";
		 String dataSizeKB;
		  @Override
		  protected String doInBackground(String... params) {
		   HttpClient httpclient = new DefaultHttpClient();
		   HttpPost httppost = new HttpPost(params[0]);
		   taskType = params[1];
		   destinationDir = params[2];
		   dataSizeKB = params[3];
		   try {
		    HttpResponse response = httpclient.execute(httppost);
		    jsonResult = inputStreamToString(
		      response.getEntity().getContent()).toString();
		   }
		 
		   catch (ClientProtocolException e) {
		    e.printStackTrace();
		   } catch (IOException e) {
		    e.printStackTrace();
		   }
		   if (taskType.equals("downloadTask")){
			   final DownloaderTask task = new DownloaderTask();
			   task.execute(new String[] { destinationDir, dataSizeKB});
			   progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			        @Override
			        public void onCancel(DialogInterface dialog) {
			        	task.cancel(true);
			        }
			    });
		   }
		   return null;
		  }
		 
		  private StringBuilder inputStreamToString(InputStream is) {
		   String rLine = "";
		   StringBuilder answer = new StringBuilder();
		   BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		 
		   try {
		    while ((rLine = rd.readLine()) != null) {
		     answer.append(rLine);
		    }
		   }
		 
		   catch (IOException e) {
		    // e.printStackTrace();
		    Toast.makeText(getApplicationContext(),
		      "Error..." + e.toString(), Toast.LENGTH_LONG).show();
		   }
		   return answer;
		  }
		 
		  @Override
		  protected void onPostExecute(String result) {
			  if (taskType.equals("browseTask")){
				  ListDrawer();
			  }
		  }
	 }// end async task
	 
	 private class DownloaderTask extends AsyncTask<String, Integer, String> {
		 WakeLock mWakeLock;
		 @Override
		    protected void onPreExecute()
		    {	
			 super.onPreExecute();
			 PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
		        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
		             getClass().getName());
		        mWakeLock.acquire();
		        
		    }; 
		    
		    
		  @Override
		  protected String doInBackground(String... params) {
			   String destinationDir = params[0];
			   int totalSize = Integer.valueOf(params[1])*1024;
			   try {
				   JSONObject jsonResponse = new JSONObject(jsonResult);
				   JSONArray jsonMainNode = jsonResponse.optJSONArray("quiz_data");
				   JSONObject jsonChildNode = jsonMainNode.getJSONObject(0);
				   String quiz_key = jsonChildNode.optString("quiz_key");
				   //String quiz_title = jsonChildNode.optString("quiz_title");
				   String[] urls = new String[MAX_FILES];
				   urls[0] = urlQuizFiles + "/" + quiz_key + "/" + jsonChildNode.optString("quiz_filename");
				   int urlCount = 1;
				   jsonMainNode = jsonResponse.optJSONArray("quiz_audio_files");
				   if (jsonMainNode != null)
					   for (int i = 0; i < jsonMainNode.length(); i++) {
						    jsonChildNode = jsonMainNode.getJSONObject(i);						    
						    urls[urlCount] = urlQuizFiles + "/" + quiz_key + "/" + jsonChildNode.optString("af_filename");
						    urlCount++;
					   }
				   jsonMainNode = jsonResponse.optJSONArray("quiz_image_files");
				   if (jsonMainNode != null)
					   for (int i = 0; i < jsonMainNode.length(); i++) {
						    jsonChildNode = jsonMainNode.getJSONObject(i);
						    urls[urlCount] = urlQuizFiles + "/" + quiz_key + "/" + jsonChildNode.optString("if_filename");
						    urlCount++;
					   }
				   //variable to store total downloaded bytes
				   int downloadedSize = 0;
				   for (int i = 0; i < urlCount; i++){
					   try {
						    String fileURL = urls[i];
						    String filename = fileURL.substring( fileURL.lastIndexOf('/')+1, fileURL.length() );
							//set the download URL, a url that points to a file on the internet
							//this is the file to be downloaded
							URL url = new URL(fileURL);
	
							//create the new connection
							HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
	
							//set up some things on the connection
							urlConnection.setRequestMethod("GET");
							urlConnection.setDoOutput(true);
	
							//and connect!
							urlConnection.connect();
	
							//set the path where we want to save the file
							//create a new file, specifying the path, and the filename
							//which we want to save the file as.
							File file = new File(destinationDir,filename);
	
							//this will be used to write the downloaded data into the file we created
							FileOutputStream fileOutput = new FileOutputStream(file);
	
							//this will be used in reading the data from the internet
							InputStream inputStream = urlConnection.getInputStream();
	
							//create a buffer...
							byte[] buffer = new byte[1024];
							int bufferLength = 0; //used to store a temporary size of the buffer
	
							//now, read through the input buffer and write the contents to the file
							while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
								//add the data in the buffer to the file in the file output stream (the file on the sd card
								fileOutput.write(buffer, 0, bufferLength);
								//add up the size so we know how much is downloaded
								downloadedSize += bufferLength;
								//this is where you would do something to report the prgress, like this maybe
								publishProgress((int) (totalSize<=0?100:Math.min(downloadedSize,totalSize) * 100 / totalSize));
							}
							//close the output stream when done
							fileOutput.close();
	
						//catch some possible errors...
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
				   }		   
					    
				 }catch (JSONException e) {
					   Toast.makeText(getApplicationContext(), "Error" + e.toString(),
							     Toast.LENGTH_SHORT).show();
				 }			
		   return null;
		  }
		  @Override
		    protected void onProgressUpdate(Integer... progress) {
		        super.onProgressUpdate(progress);
		        // if we get here, length is known, now set indeterminate to false
		        progressDialog.setIndeterminate(false);
		        progressDialog.setMax(100);
		        progressDialog.setProgress(progress[0]);
		        /*new Handler().postDelayed(new Runnable() {
		            public void run() {
		            	progressDialog.show();
		            }
		        },1000);*/
		        progressDialog.show();
		    }
		  @Override
		  protected void onPostExecute(String result) {
			  mWakeLock.release();
			  progressDialog.dismiss();
			  Toast.makeText(getApplicationContext(),"Download completed", Toast.LENGTH_LONG).show();		  
		  }
	 }// end async task
	 
	 public void accessWebService() {
		  JsonReadTask task = new JsonReadTask();
		  // passes values for the urls string array
		  url = url + "?cat_parentid=" + cat_id;
		 //Toast.makeText(getApplicationContext(),url, Toast.LENGTH_LONG).show();
		  task.execute(new String[] { url, "browseTask", "", "" });
	 }
	 
	 public void accessWebServiceDownload(String quiz_id, String destinationDir, String dataSizeKB) {
		  JsonReadTask task = new JsonReadTask();
		  // passes values for the urls string array
		  urlQuizData = urlQuizData + "?quiz_id=" + quiz_id;
		 //Toast.makeText(getApplicationContext(),url, Toast.LENGTH_LONG).show();
		  task.execute(new String[] { urlQuizData, "downloadTask",  destinationDir, dataSizeKB});
	 }
	 
	 public void Downloader(String destinationDir) {
		 try {
			   JSONObject jsonResponse = new JSONObject(jsonResult);
			   JSONArray jsonMainNode = jsonResponse.optJSONArray("quiz_data");
			   JSONObject jsonChildNode = jsonMainNode.getJSONObject(0);
			   String quiz_key = jsonChildNode.optString("quiz_key");
			   //String quiz_title = jsonChildNode.optString("quiz_title");
			   String quiz_filename = jsonChildNode.optString("quiz_filename");
			   downloadFile(urlQuizFiles + "/" + quiz_key + "/" + quiz_filename, quiz_filename, destinationDir);
			   jsonMainNode = jsonResponse.optJSONArray("quiz_audio_files");
			   if (jsonMainNode != null)
				   for (int i = 0; i < jsonMainNode.length(); i++) {
					    jsonChildNode = jsonMainNode.getJSONObject(i);
					    String af_filename = jsonChildNode.optString("af_filename");
					    downloadFile(urlQuizFiles + "/" + quiz_key + "/" + af_filename, af_filename, destinationDir);
				   }
			   jsonMainNode = jsonResponse.optJSONArray("quiz_image_files");
			   if (jsonMainNode != null)
				   for (int i = 0; i < jsonMainNode.length(); i++) {
					    jsonChildNode = jsonMainNode.getJSONObject(i);
					    String if_filename = jsonChildNode.optString("if_filename");
					    downloadFile(urlQuizFiles + "/" + quiz_key + "/" + if_filename, if_filename, destinationDir);
				   }
				    
			 }catch (JSONException e) {
				   Toast.makeText(getApplicationContext(), "Error" + e.toString(),
						     Toast.LENGTH_SHORT).show();
			 }
		 
	}
	 
	 public static void downloadFile(String fileURL, String filename, String saveDir){
		 try {
				//set the download URL, a url that points to a file on the internet
				//this is the file to be downloaded
				URL url = new URL(fileURL);

				//create the new connection
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

				//set up some things on the connection
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoOutput(true);

				//and connect!
				urlConnection.connect();

				//set the path where we want to save the file
				//create a new file, specifying the path, and the filename
				//which we want to save the file as.
				File file = new File(saveDir,filename);

				//this will be used to write the downloaded data into the file we created
				FileOutputStream fileOutput = new FileOutputStream(file);

				//this will be used in reading the data from the internet
				InputStream inputStream = urlConnection.getInputStream();

				//this is the total size of the file
				int totalSize = urlConnection.getContentLength();
				//variable to store total downloaded bytes
				int downloadedSize = 0;

				//create a buffer...
				byte[] buffer = new byte[1024];
				int bufferLength = 0; //used to store a temporary size of the buffer

				//now, read through the input buffer and write the contents to the file
				while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
					//add the data in the buffer to the file in the file output stream (the file on the sd card
					fileOutput.write(buffer, 0, bufferLength);
					//add up the size so we know how much is downloaded
					downloadedSize += bufferLength;
					//this is where you would do something to report the prgress, like this maybe
					
				}
				//close the output stream when done
				fileOutput.close();

			//catch some possible errors...
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// see http://androidsnippets.com/download-an-http-file-to-sdcard-with-progress-notification
	 }
	 
	 
	// build hash set for list view
	 public void ListDrawer() {
 
	  try {
		   repositoryItemsList = new ArrayList<RepositoryItemData>();
		   JSONObject jsonResponse = new JSONObject(jsonResult);
		   JSONArray jsonMainNode = jsonResponse.optJSONArray("items_list");	 
		   for (int i = 0; i < jsonMainNode.length(); i++) {
		    JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
		    RepositoryItemData repositoryItem = new RepositoryItemData(jsonChildNode.optString("item_id"),
		    														   jsonChildNode.optString("item_type"),
		    														   jsonChildNode.optString("item_name"),
		    														   jsonChildNode.optString("item_description"),
		    														   jsonChildNode.optString("item_category_count"),
		    														   jsonChildNode.optString("item_quiz_count"),
		    														   jsonChildNode.optString("item_quizlines_count"),
		    														   jsonChildNode.optString("item_quizaudiofiles_count"),
		    														   jsonChildNode.optString("item_quizaudiofiles_size"),
		    														   jsonChildNode.optString("item_quizimagefiles_count"),
		    														   jsonChildNode.optString("item_quizimagefiles_size"));
		    repositoryItemsList.add(repositoryItem);
		    //Toast.makeText(getApplicationContext(),jsonChildNode.optString("item_description") + " fetched from rep", Toast.LENGTH_LONG).show();
		   }		  
		  RepositoryItemArrayAdapter repositoryItemArrayAdapter = new RepositoryItemArrayAdapter(this, R.layout.quiz_list_item, repositoryItemsList);
		  this.setListAdapter(repositoryItemArrayAdapter);
	  } catch (JSONException e) {
	   Toast.makeText(getApplicationContext(), "Error" + e.toString(),
	     Toast.LENGTH_SHORT).show();
	  }
	 }

	 public class RepositoryItemData{
		 private String item_type;
		 private String item_id;
		 private String item_name;
		 private String item_description;
		 private String item_category_count;
		 private String item_quiz_count;
		 private String item_quizlines_count;
		 private String item_quizaudiofiles_count;
		 private String item_quizaudiofiles_size;
		 private String item_quizimagefiles_count;
		 private String item_quizimagefiles_size;
		 public RepositoryItemData(String id, String type, String name, String description, 
				 					String category_count, String quiz_count, String quizlines_count, 
				                    String quizaudiofiles_count, String quizaudiofiles_size,
				                    String quizimagefiles_count, String quizimagefiles_size){
			 item_type = type;
			 item_id = id;
			 item_name = name;
			 item_description = description;
			 item_category_count = category_count;
			 item_quiz_count = quiz_count;
			 item_quizlines_count = quizlines_count;
			 item_quizaudiofiles_count = quizaudiofiles_count;
			 item_quizaudiofiles_size = quizaudiofiles_size;
			 item_quizimagefiles_count = quizimagefiles_count;
			 item_quizimagefiles_size = quizimagefiles_size;
		 }
		 public String getItemName(){
			 return item_name;
		 }
		 public String getItemDescription(){
			 return item_description;
		 }
		 public String getItemType(){
			 return item_type;
		 }
		 public String getItemId(){
			 return item_id;
		 }
		 public String getItemCategoryCount(){
			 return item_category_count;
		 }
		 public String getItemQuizCount(){
			 return item_quiz_count;
		 }
		 public String getItemQuizlinesCount(){
			 return item_quizlines_count;
		 }
		 public String getItemQuizaudiofilesCount(){
			 return item_quizaudiofiles_count;
		 }
		 public String getItemQuizaudiofilesSize(){
			 return item_quizaudiofiles_size;
		 }
		 public String getItemQuizimagefilesCount(){
			 return item_quizimagefiles_count;
		 }
		 public String getItemQuizimagefilesSize(){
			 return item_quizimagefiles_size;
		 }
	 }
	 
	 
	 private class RepositoryItemArrayAdapter extends ArrayAdapter<RepositoryItemData>
	 {  
	     //private ArrayList<RepositoryItemData> list;
		 Context context;
	      
	     //this custom adapter receives an ArrayList of RowData objects.
	     //RowData is my class that represents the data for a single row and could be anything.
	     public RepositoryItemArrayAdapter(Context context, int resourceId, ArrayList<RepositoryItemData> rowDataList)
	     {
	         //populate the local list with data.
	         super(context, resourceId, rowDataList);
	         this.context = context;
	         //this.list = new ArrayList<RepositoryItemData>();
	         //this.list.addAll(rowDataList);
	     }
	          
		 private class ViewHolder
		 {
		     TextView tvItemName;
		     TextView tvItemDescription;
		     ImageView ivItemIcon;
		     TextView tvItemSize;
		     ImageView ivItemDownload;
		 }
		 
	     public View getView(final int position, View convertView, ViewGroup parent)
	     {
	    	 
	         //creating the ViewHolder we defined earlier.
	         ViewHolder holder = null;
	         final RepositoryItemData item = getItem(position);
	          
	         //creating LayoutInflator for inflating the row layout.
	         LayoutInflater inflator = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	  
	         if (convertView == null) {
		         //inflating the row layout we defined earlier.
	        	 
	        	 holder = new ViewHolder();	        	 
	        	 if (item.getItemType().equals("category")){	
	        		 convertView = inflator.inflate(R.layout.quizcategory_list_item, null);
	        		 holder.tvItemSize = (TextView) convertView.findViewById(R.id.tv_item_size); 
		        	 holder.ivItemIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
	        		 holder.ivItemIcon.setImageResource(R.drawable.category);
		        	 if (Integer.valueOf(item.getItemCategoryCount())+Integer.valueOf(item.getItemQuizCount())==0){
			        	 convertView.setEnabled(false);
			        	 convertView.setOnClickListener(null);
			         }		        	  
		        	 holder.tvItemSize.setText("Subcategories: " + item.getItemCategoryCount()  + ", Quiz: " + item.getItemCategoryCount());
		        	 holder.tvItemSize.setTextAppearance(getApplicationContext(), R.style.normalText);
	        	 }else {	        	
	        		 convertView = inflator.inflate(R.layout.quiz_list_item, null);
	        		 convertView.setEnabled(false);
		        	 convertView.setOnClickListener(null);
	        		 holder.tvItemSize = (TextView) convertView.findViewById(R.id.tv_item_size); 
		        	 holder.ivItemIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
	        		 holder.ivItemIcon.setImageResource(R.drawable.quiz);
	        		 holder.ivItemDownload = (ImageView) convertView.findViewById(R.id.iv_download);
	        		 holder.ivItemDownload.setImageResource(R.drawable.download);
	        		 holder.ivItemDownload.setOnClickListener(new OnClickListener() {
	 		            @Override
			            public void onClick(final View v) {
	 		            	DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
	 		            	    @Override
	 		            	    public void onClick(DialogInterface dialog, int which) {
	 		            	        switch (which){
	 		            	        case DialogInterface.BUTTON_POSITIVE:
	 		            	            //Yes button clicked
	 		            	        	DirectoryChooserDialog directoryChooserDialog = 
	 		 		   	    	        new DirectoryChooserDialog(v.getContext(), 
	 		 		   	    	            new DirectoryChooserDialog.ChosenDirectoryListener() 
	 		 		   	    		        {
	 		 		   	    		            @Override
	 		 		   	    		            public void onChosenDir(String chosenDir) 
	 		 		   	    		            {
	 		 		   	    		                folder_location = chosenDir;
	 		 		   	    		                Toast.makeText(
	 		 		   	    		                  v.getContext(), "Downloading to directory: " + 
	 		 		   	    		                  chosenDir, Toast.LENGTH_SHORT).show();
	 		 		   	    		                accessWebServiceDownload(item.getItemId(), chosenDir, String.valueOf(Integer.valueOf(item.getItemQuizaudiofilesSize()) + Integer.valueOf(item.getItemQuizimagefilesSize())));
	 		 		   	    		            }
	 		 		   	    		        }); 
	 		 		   	    	        // Toggle new folder button enabling
	 		 		   	    	        directoryChooserDialog.setNewFolderEnabled(true);
	 		 		   	    	        // Load directory chooser dialog for initial 'm_chosenDir' directory.
	 		 		   	    	        // The registered callback will be called upon final directory selection.
	 		 		   	    	        directoryChooserDialog.chooseDirectory("/mnt/shared/data");
	 		            	            break;

	 		            	        case DialogInterface.BUTTON_NEGATIVE:
	 		            	            //No button clicked
	 		            	            break;
	 		            	        }
	 		            	    }
	 		            	};
	 		            	AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
	 		            	builder.setMessage("Proceed with quiz download (" + (Integer.valueOf(item.getItemQuizaudiofilesSize()) + Integer.valueOf(item.getItemQuizimagefilesSize())) + " KBytes)?").setPositiveButton("Yes", dialogClickListener)
	 		            	    .setNegativeButton("No", dialogClickListener).show();	 		            	
			            }
			        });
	        		 holder.tvItemSize.setText("Lines: " + item.getItemQuizlinesCount()  + 
	        				                   ", Audio files: " + item.getItemQuizaudiofilesCount() + ", Audio files size: " + item.getItemQuizaudiofilesSize() + " KBytes" +
	        				                   ", Image files: " + item.getItemQuizimagefilesCount() + ", Image files size: " + item.getItemQuizimagefilesSize() + " KBytes");
		        	 holder.tvItemSize.setTextAppearance(getApplicationContext(), R.style.normalText);
	        	 }
		         //setting the views into the ViewHolder.		         
		         holder.tvItemName = (TextView) convertView.findViewById(R.id.tv_item_name);
		         holder.tvItemDescription = (TextView) convertView.findViewById(R.id.tv_item_description);		         
		         holder.tvItemName.setText(item.getItemName());
		         holder.tvItemName.setTextAppearance(getApplicationContext(), R.style.boldText);
		         holder.tvItemDescription.setText(item.getItemDescription());	
		         holder.tvItemDescription.setTextAppearance(getApplicationContext(), R.style.normalText);
		         	         
		         convertView.setTag(holder);
		         
		         //Toast.makeText(getApplicationContext(),RepositoryItemData.getItemName() + " added to list", Toast.LENGTH_LONG).show();
		         
	         }else{
	        	 //setting data into the the ViewHolder.
	        	 holder = (ViewHolder) convertView.getTag();
		         holder.tvItemName.setText(item.getItemName());
		         holder.tvItemDescription.setText(item.getItemDescription());
	         }
	         //return the row view.
	         return convertView;
	     }
	 }
}
