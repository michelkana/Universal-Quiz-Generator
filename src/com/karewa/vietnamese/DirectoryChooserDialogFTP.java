package com.karewa.vietnamese;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.Editable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

public class DirectoryChooserDialogFTP 
{
 private boolean m_isNewFolderEnabled = true;
 private String m_sdcardDirectory = "";
 private Context m_context;
 private TextView m_titleView;

 private String m_dir = "";
 private List<String> m_subdirs = null;
 private ChosenDirectoryListener m_chosenDirectoryListener = null;
 private ArrayAdapter<String> m_listAdapter = null;
 
 private String folder_location = "";

 //////////////////////////////////////////////////////
 // Callback interface for selected directory
 //////////////////////////////////////////////////////
 public interface ChosenDirectoryListener 
 {
     public void onChosenDir(String chosenDir);
 }

 public DirectoryChooserDialogFTP(Context context, ChosenDirectoryListener chosenDirectoryListener)
 {
     m_context = context;
     m_sdcardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
     m_chosenDirectoryListener = chosenDirectoryListener;

     try
     {
         m_sdcardDirectory = new File(m_sdcardDirectory).getCanonicalPath();
     }
     catch (IOException ioe)
     {
     }
 }

 ///////////////////////////////////////////////////////////////////////
 // setNewFolderEnabled() - enable/disable new folder button
 ///////////////////////////////////////////////////////////////////////

 public void setNewFolderEnabled(boolean isNewFolderEnabled)
 {
     m_isNewFolderEnabled = isNewFolderEnabled;
 }

 public boolean getNewFolderEnabled()
 {
     return m_isNewFolderEnabled;
 }

 ///////////////////////////////////////////////////////////////////////
 // chooseDirectory() - load directory chooser dialog for initial
 // default sdcard directory
 ///////////////////////////////////////////////////////////////////////

 public void chooseDirectory()
 {
     // Initial directory is sdcard directory
     chooseDirectory(m_sdcardDirectory);
 }

 ////////////////////////////////////////////////////////////////////////////////
 // chooseDirectory(String dir) - load directory chooser dialog for initial 
 // input 'dir' directory
 ////////////////////////////////////////////////////////////////////////////////

 public void chooseDirectory(String dir)
 {
     /*File dirFile = new File(dir);
     if (! dirFile.exists() || ! dirFile.isDirectory())
     {
         dir = m_sdcardDirectory;
     }

     try
     {
         dir = new File(dir).getCanonicalPath();
     }
     catch (IOException ioe)
     {
         return;
     }*/

     m_dir = dir;
     m_subdirs = getDirectories(dir);

     class DirectoryOnClickListener implements DialogInterface.OnClickListener
     {
         public void onClick(DialogInterface dialog, int item) 
         {
             // Navigate into the sub-directory
             //m_dir += "/" + ((AlertDialog) dialog).getListView().getAdapter().getItem(item);
             m_dir += ((AlertDialog) dialog).getListView().getAdapter().getItem(item);
             updateDirectory();
         }
     }

 AlertDialog.Builder dialogBuilder = 
 createDirectoryChooserDialog(dir, m_subdirs, new DirectoryOnClickListener());

 dialogBuilder.setPositiveButton("OK", new OnClickListener() 
 {
     @Override
     public void onClick(DialogInterface dialog, int which) 
     {
         // Current directory chosen
         if (m_chosenDirectoryListener != null)
         {   
        	 DirectoryChooserDialog directoryChooserDialog = 
        		        new DirectoryChooserDialog(m_context, 
        		            new DirectoryChooserDialog.ChosenDirectoryListener() 
        			        {
        			            @Override
        			            public void onChosenDir(String chosenDir) 
        			            {
        			                folder_location = chosenDir;
        			                Toast.makeText(
        			                m_context, "Files will be saved in : " + 
        			                  chosenDir, Toast.LENGTH_LONG).show();
        			                new DownloadFTPFolderTask().execute(m_dir,folder_location);
        			                m_chosenDirectoryListener.onChosenDir(folder_location+"/"+m_dir);
        			            }
        			        }); 
        		        // Toggle new folder button enabling
        		        directoryChooserDialog.setNewFolderEnabled(false);
        		        // Load directory chooser dialog for initial 'm_chosenDir' directory.
        		        // The registered callback will be called upon final directory selection.
        		        directoryChooserDialog.chooseDirectory("/mnt/shared/data");
        		    	
         }
     }
 }).setNegativeButton("Cancel", null);

 final AlertDialog dirsDialog = dialogBuilder.create();

 dirsDialog.setOnKeyListener(new OnKeyListener() 
 {
     @Override
     public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) 
     {
         if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
         {
             // Back button pressed
             if ( m_dir.equals(m_sdcardDirectory) )
             {
                 // The very top level directory, do nothing
                 return false;
             }
             else
             {
                 // Navigate back to an upper directory
                 m_dir = new File(m_dir).getParent();
                 updateDirectory();
             }
 
             return true;
         }
         else
         {
             return false;
         }
     }
 });

 // Show directory chooser dialog
 dirsDialog.show();
}

private boolean createSubDir(String newDir)
{
 File newDirFile = new File(newDir);
 if (! newDirFile.exists() )
 {
     return newDirFile.mkdir();
 }

 return false;
}


class RetreiveFTPFoldersTask extends AsyncTask<String, Void, List<String>> {

    private Exception exception;
    FTPClient mFTPClient = new FTPClient();  
    List<String> dirs = new ArrayList<String>();

    protected List<String> doInBackground(String... urls) {
        try {
        	mFTPClient.connect("ftp.karewa.com", 21);
    		boolean status = mFTPClient.login("quizgen@karewa.com", "quizgen");
    		// now check the reply code, if positive mean connection success  
    	      if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {  
    	           /* Set File Transfer Mode  
    	            *  
    	            * To avoid corruption issue you must specified a correct  
    	            * transfer mode, such as ASCII_FILE_TYPE, BINARY_FILE_TYPE,  
    	            * EBCDIC_FILE_TYPE .etc. Here, I use BINARY_FILE_TYPE  
    	            * for transferring text, image, and compressed files.  
    	            */  
    	           mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);  
    	           mFTPClient.enterLocalPassiveMode();  
    	           FTPFile[] ftpFiles = mFTPClient.listDirectories();  
    	           int length = ftpFiles.length;  
    	           //Toast.makeText(m_context, length + " files found", Toast.LENGTH_SHORT).show();
    	           for (int i = 0; i < length; i++) {  
    	             String dirName = ftpFiles[i].getName();  
    	             boolean isDir = ftpFiles[i].isDirectory();  
    	             if (isDir && ! dirName.equals(".") && ! dirName.equals("..")) {  
    	            	 dirs.add(dirName);  
    	             }  
    	           }
    	           mFTPClient.logout();  
    	           mFTPClient.disconnect();
    	           return dirs;
    	      }else{
    	    	  Toast.makeText(m_context, "not connected", Toast.LENGTH_SHORT).show();
    	    	  return null;
    	      }
        } catch (Exception e) {
            this.exception = e;
            return null;
        }
    }

    protected void onPostExecute(List<String> dirs) {
        // TODO: check this.exception 
        // TODO: do something with the feed
    }
}

class DownloadFTPFolderTask extends AsyncTask<String, Void, Integer> {

    FTPClient mFTPClient = new FTPClient();  

    protected Integer doInBackground(String... dirs) {
        try {
        	mFTPClient.connect("ftp.karewa.com", 21);
    		mFTPClient.login("quizgen@karewa.com", "quizgen");
    		// now check the reply code, if positive mean connection success  
    	      if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {  
    	           /* Set File Transfer Mode  
    	            *  
    	            * To avoid corruption issue you must specified a correct  
    	            * transfer mode, such as ASCII_FILE_TYPE, BINARY_FILE_TYPE,  
    	            * EBCDIC_FILE_TYPE .etc. Here, I use BINARY_FILE_TYPE  
    	            * for transferring text, image, and compressed files.  
    	            */  
    	           mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);  
    	           mFTPClient.setFileTransferMode(FTPClient.BLOCK_TRANSFER_MODE);
    	           mFTPClient.enterLocalPassiveMode();  
    	           //mFTPClient.changeWorkingDirectory(dirs[0]);
    	           FTPFileFilter filter = new FTPFileFilter() {
    	        	   
    	        	    @Override
    	        	    public boolean accept(FTPFile ftpFile) {
    	        	 
    	        	        return ftpFile.isFile();
    	        	 
    	        	    }
    	        	};
    	           FTPFile[] ftpFiles = mFTPClient.listFiles(dirs[0], filter);  
    	           int length = ftpFiles.length;  
    	           //Toast.makeText(m_context, length + " files found", Toast.LENGTH_SHORT).show();
    	           String destDir = dirs[1] + "/" +  dirs[0]; 
    	           createSubDir(destDir);
    	           for (int i = 0; i < length; i++) {  
    	        	 
    	        	 OutputStream desFileStream = new BufferedOutputStream(new FileOutputStream(new File(destDir + "/" + ftpFiles[i].getName())));  
    	        	 mFTPClient.retrieveFile(ftpFiles[i].getName(), desFileStream);  
    	        	 desFileStream.close();    	               
    	           }
    	           mFTPClient.logout();  
    	           mFTPClient.disconnect();
    	           return length;
    	      }else{
    	    	  Toast.makeText(m_context, "not connected", Toast.LENGTH_SHORT).show();
    	    	  return null;
    	      }
        } catch (Exception e) {
            return null;
        }
    }

    protected void onPostExecute(List<String> dirs) {
        // TODO: check this.exception 
        // TODO: do something with the feed
    }
}


private List<String> getDirectories(String dir)
{

	List<String> dirs = new ArrayList<String>();
	
	try {
		dirs = new RetreiveFTPFoldersTask().execute().get();
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ExecutionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
 Collections.sort(dirs, new Comparator<String>()
 {
     public int compare(String o1, String o2) 
     {
         return o1.compareTo(o2);
     }
 });

 return dirs;
}

private AlertDialog.Builder createDirectoryChooserDialog(String title, List<String> listItems,
     DialogInterface.OnClickListener onClickListener)
{
 AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(m_context);

 // Create custom view for AlertDialog title containing 
 // current directory TextView and possible 'New folder' button.
 // Current directory TextView allows long directory path to be wrapped to multiple lines.
 LinearLayout titleLayout = new LinearLayout(m_context);
 titleLayout.setOrientation(LinearLayout.VERTICAL);

 m_titleView = new TextView(m_context);
 m_titleView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 m_titleView.setTextAppearance(m_context, android.R.style.TextAppearance_Large);
 m_titleView.setTextColor( m_context.getResources().getColor(android.R.color.white) );
 m_titleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
 m_titleView.setText(title);

 Button newDirButton = new Button(m_context);
 newDirButton.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
 newDirButton.setText("New folder");
 newDirButton.setOnClickListener(new View.OnClickListener() 
 {
     @Override
     public void onClick(View v) 
     {
         final EditText input = new EditText(m_context);

         // Show new folder name input dialog
         new AlertDialog.Builder(m_context).
         setTitle("New folder name").
         setView(input).setPositiveButton("OK", new DialogInterface.OnClickListener() 
         {
             public void onClick(DialogInterface dialog, int whichButton) 
             {
                 Editable newDir = input.getText();
                 String newDirName = newDir.toString();
                 // Create new directory
                 if ( createSubDir(m_dir + "/" + newDirName) )
                 {
                     // Navigate into the new directory
                     m_dir += "/" + newDirName;
                     updateDirectory();
                 }
                 else
                 {
                     Toast.makeText(
                     m_context, "Failed to create '" + newDirName + 
                       "' folder", Toast.LENGTH_SHORT).show();
                 }
             }
         }).setNegativeButton("Cancel", null).show(); 
     }
 });

 if (! m_isNewFolderEnabled)
 {
     newDirButton.setVisibility(View.GONE);
 }

 titleLayout.addView(m_titleView);
 titleLayout.addView(newDirButton);

 dialogBuilder.setCustomTitle(titleLayout);

 m_listAdapter = createListAdapter(listItems);

 dialogBuilder.setSingleChoiceItems(m_listAdapter, -1, onClickListener);
 dialogBuilder.setCancelable(false);

 return dialogBuilder;
}

private void updateDirectory()
{
 m_subdirs.clear();
 m_subdirs.addAll( getDirectories(m_dir) );
 m_titleView.setText(m_dir);

 m_listAdapter.notifyDataSetChanged();
}

private ArrayAdapter<String> createListAdapter(List<String> items)
{
 return new ArrayAdapter<String>(m_context, 
   android.R.layout.select_dialog_item, android.R.id.text1, items)
 {
     @Override
     public View getView(int position, View convertView,
     ViewGroup parent) 
     {
         View v = super.getView(position, convertView, parent);

         if (v instanceof TextView)
         {
             // Enable list item (directory) text wrapping
             TextView tv = (TextView) v;
             tv.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
             tv.setEllipsize(null);
         }
         return v;
     }
 };
}
}

