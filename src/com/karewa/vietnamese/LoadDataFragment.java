package com.karewa.vietnamese;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.karewa.vietnamese.MainActivity.LoadDataFragmentInterface;
import com.karewa.vietnamese.R.id;
import com.karewa.vietnamese.R.layout;
import com.karewa.vietnamese.R.string;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener; 

 
public class LoadDataFragment extends Fragment implements LoadDataFragmentInterface{
	private String folder_location = "";
	DataFolderProcessor dfProcessor;
	OnLoadDataListener mListener;

	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_load_data, container, false);
   
        OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(final View v) {
                switch(v.getId()) {
                   case R.id.bt_browse_ftp_folders:
                      // which is supposed to be called automatically
                      // in your activity, which has now changed to a fragment.
                      browse_ftp_folders(v);   
                      break;
                   case R.id.bt_browse_local_folders:
                       // which is supposed to be called automatically
                       // in your activity, which has now changed to a fragment.
                       browse_local_folders(v);   
                       break;
                   case R.id.bt_check_data:
                       // which is supposed to be called automatically
                       // in your activity, which has now changed to a fragment.
                       check_data(v);   
                       break;
                   case R.id.bt_load_replace:
                       // which is supposed to be called automatically
                       // in your activity, which has now changed to a fragment.
                       load_replace_data(v);   
                       break;
                   case R.id.bt_save_data:
                       // which is supposed to be called automatically
                       // in your activity, which has now changed to a fragment.
                       save_data(v);   
                       break;
                }
            }
        };

        rootView.findViewById(R.id.bt_browse_local_folders).setOnClickListener(clickListener);
        rootView.findViewById(R.id.bt_browse_ftp_folders).setOnClickListener(clickListener);
        rootView.findViewById(R.id.bt_check_data).setOnClickListener(clickListener);
        rootView.findViewById(R.id.bt_load_replace).setOnClickListener(clickListener);
        rootView.findViewById(R.id.bt_save_data).setOnClickListener(clickListener);
        rootView.findViewById(R.id.bt_load_replace).setEnabled(false);
        rootView.findViewById(R.id.bt_save_data).setEnabled(false);

        return rootView;
    }
    

    
    public void set_et_folder_location(String f) 
    {
        final EditText et_folder_location = (EditText) this.getView().findViewById(R.id.et_folder_location);
        et_folder_location.setText(f);
    }
    
    public void browse_local_folders(final View v){
    	this.getView().findViewById(R.id.bt_load_replace).setEnabled(false);
    	this.getView().findViewById(R.id.bt_save_data).setEnabled(false);
        // Create DirectoryChooserDialog and register a callback 
        DirectoryChooserDialog directoryChooserDialog = 
        new DirectoryChooserDialog(v.getContext(), 
            new DirectoryChooserDialog.ChosenDirectoryListener() 
	        {
	            @Override
	            public void onChosenDir(String chosenDir) 
	            {
	                folder_location = chosenDir;
	                Toast.makeText(
	                v.getContext(), "Chosen directory: " + 
	                  chosenDir, Toast.LENGTH_SHORT).show();
	                set_et_folder_location(chosenDir);
	            }
	        }); 
        // Toggle new folder button enabling
        directoryChooserDialog.setNewFolderEnabled(false);
        // Load directory chooser dialog for initial 'm_chosenDir' directory.
        // The registered callback will be called upon final directory selection.
        directoryChooserDialog.chooseDirectory("/mnt/shared/data");
    }
    
    public void browse_ftp_folders(final View v){
    	Intent browseServerIntent = new Intent(this.getActivity(), BrowseServerActivity.class);
	    startActivity(browseServerIntent);
        /*
	    // Create DirectoryChooserDialog and register a callback 
        DirectoryChooserDialogFTP directoryChooserDialog = 
        new DirectoryChooserDialogFTP(v.getContext(), 
            new DirectoryChooserDialogFTP.ChosenDirectoryListener() 
	        {
	            @Override
	            public void onChosenDir(String chosenDir) 
	            {
	                folder_location = chosenDir;
	                Toast.makeText(
	                v.getContext(), "Chosen directory: " + 
	                  chosenDir, Toast.LENGTH_SHORT).show();
	                set_et_folder_location(chosenDir);
	            }
	        }); 
        // Toggle new folder button enabling
        directoryChooserDialog.setNewFolderEnabled(false);
        // Load directory chooser dialog for initial 'm_chosenDir' directory.
        // The registered callback will be called upon final directory selection.
        directoryChooserDialog.chooseDirectory("");
        */
    }
    
    public void check_data(View v) 
    {
    	EditText et_folder_location = (EditText) this.getView().findViewById(R.id.et_folder_location);
        folder_location = et_folder_location.getText().toString();
    	dfProcessor = new DataFolderProcessor(folder_location);
    	dfProcessor.parseData();
    	String parsingResults = dfProcessor.getParsingResults();
		Toast.makeText(v.getContext(), parsingResults, Toast.LENGTH_SHORT).show();		
		if (parsingResults.equals("SYNTAX OK")){
			TextView textView = (TextView) this.getView().findViewById(R.id.tv_syntax);
			textView.setText("SYNTAX OK");
			textView = (TextView) this.getView().findViewById(R.id.tv_audio);
			textView.setText(dfProcessor.getCountAudioFilesFound() + " Audio Files found");
			textView = (TextView) this.getView().findViewById(R.id.tv_image);
			textView.setText(dfProcessor.getCountImageFilesFound() + " Image Files found");	
			Button  button = (Button) this.getView().findViewById(R.id.bt_load_replace);
			button.setEnabled(true);
		}else{
			TextView textView = (TextView) this.getView().findViewById(R.id.tv_syntax);
			textView.setText("SYNTAX FAILED");
			textView = (TextView) this.getView().findViewById(R.id.tv_audio);
			textView.setText("SYNTAX FAILED");
			textView = (TextView) this.getView().findViewById(R.id.tv_image);
			textView.setText("SYNTAX FAILED");		
			Button  button = (Button) this.getView().findViewById(R.id.bt_load_replace);
			button.setEnabled(false);
		}
        
    }
    
    public void load_replace_data(View v){
    	dfProcessor.loadData();
    	Toast.makeText(v.getContext(), dfProcessor.getCountLinesFound() + " lines loaded", Toast.LENGTH_SHORT).show();
    	/*poolStringArray.add(dfProcessor.getDataFolderPath() + ": " + dfProcessor.getCountLinesFound() + " lines, " + dfProcessor.getCountDataHeadersFound() + " headers, " + dfProcessor.getCountAudioFilesFound() + " audio files, " + dfProcessor.getCountImageFilesFound() + " image files");
    	ListView lv_pool = (ListView) v.findViewById(R.id.lv_pool);
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.fragment_load_data_listview, poolStringArray);
    	lv_pool.setAdapter(adapter);*/
    	TextView tv_pool = (TextView) this.getView().findViewById(R.id.tv_pool);
    	//String currentPool = dfProcessor.getCurrentPool();
    	String currentPool = tv_pool.getText().toString();
    	currentPool += System.getProperty("line.separator") +  "LOADED: " + dfProcessor.getDataFolderPath()
    			    +  System.getProperty("line.separator") + getString(R.string.tab) + dfProcessor.getCountDataHeadersFound() + " headers, "
    			    +  dfProcessor.getCountLinesFound() + " lines, "
    			    +  dfProcessor.getCountAudioFilesFound() + " audio files, "
    			    +  dfProcessor.getCountImageFilesFound() + " image files";
    	tv_pool.setText(currentPool);    	
    	dfProcessor.setCurrentPool(currentPool);
    	mListener.onLoadData(dfProcessor);
    	
    	Button  button = (Button) this.getView().findViewById(R.id.bt_save_data);
		button.setEnabled(true);
    }

    public void save_data(View v){
    	dfProcessor.saveDataToFile();
    	Toast.makeText(v.getContext(), "Data has been written to file " + dfProcessor.getDataFilename(), Toast.LENGTH_SHORT).show();    	
    }
    
    public interface OnLoadDataListener {
        public void onLoadData(DataFolderProcessor dfProcessor);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnLoadDataListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnLoadDataListener");
        }
    }

	@Override
	public void fragmentBecameVisible() {
		dfProcessor = ((MainActivity)this.getActivity()).getDataFolderProcessor();
		TextView tv_pool = (TextView) this.getActivity().findViewById(R.id.tv_pool);
    	if (dfProcessor!=null){
    		tv_pool.setText(dfProcessor.getCurrentPool());
    		this.getView().findViewById(R.id.bt_save_data).setEnabled(true);
    		this.getView().findViewById(R.id.bt_load_replace).setEnabled(true);   
    		EditText et_folder_location = (EditText) this.getView().findViewById(R.id.et_folder_location);
    		et_folder_location.setText(dfProcessor.getDataFolderPath());
    	}
	}
	
}
