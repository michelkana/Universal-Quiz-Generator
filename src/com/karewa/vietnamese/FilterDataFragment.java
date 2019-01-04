package com.karewa.vietnamese;

import java.util.ArrayList;

import com.karewa.vietnamese.LoadDataFragment.OnLoadDataListener;
import com.karewa.vietnamese.MainActivity.FilterDataFragmentInterface;
import com.karewa.vietnamese.R.id;
import com.karewa.vietnamese.R.layout;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
 
public class FilterDataFragment extends Fragment implements FilterDataFragmentInterface{
	DataFolderProcessor dfProcessor;
	OnFilterDataListener mListener;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_filter_data, container, false);
        
        
        OnClickListener clickListener = new OnClickListener() {
            @Override
            public void onClick(final View v) {
                switch(v.getId()) {
                   case R.id.bt_filter:
                	   filter_data();   
                      break;
                   case R.id.bt_reset:
                	   reset_data();   
                      break;
                }
            }
        };
        
        rootView.findViewById(R.id.bt_filter).setOnClickListener(clickListener);
        rootView.findViewById(R.id.bt_filter).setEnabled(false);
        rootView.findViewById(R.id.bt_reset).setOnClickListener(clickListener);
        rootView.findViewById(R.id.bt_reset).setEnabled(false);
        
        return rootView;
    }
    
    public String getStr(int id){
    	return getResources().getString(id);
    }
    
    public void generate_layout(DataFolderProcessor df){
    	if (dfProcessor!=null){
	    	dfProcessor = df; 
	    	String[][] dataHeaders = dfProcessor.getDataHeaders();
	    	int dataHeadersLength = dfProcessor.getCountDataHeadersFound();
	    	ArrayList<String> spinnerArray = new ArrayList<String>();
	    	for (int i=0; i<dataHeadersLength; i++){
	    		spinnerArray.add(dataHeaders[i][1]);
	    	}  
	    	View v = this.getView();
	    	Spinner sp = (Spinner) v.findViewById(R.id.sp_header_list);
	    	ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this.getActivity(),
	    			R.layout.spinner_item, 
	    	            spinnerArray);
	    	sp.setAdapter(spinnerArrayAdapter); 
	    	
	    	sp.setOnItemSelectedListener(new OnItemSelectedListener() {
	            @Override
	            public void onItemSelected(AdapterView<?> spinner, View container,
	                    int position, long id) {
	            	fill_values(position);	            	
	            }

	            @Override
	            public void onNothingSelected(AdapterView<?> arg0) {
	                // TODO Auto-generated method stub
	            }
	        });
	    	
	    	spinnerArray = new ArrayList<String>();
	    	spinnerArray.add(getStr(R.string.tab_filter_all_text)); 
	    	spinnerArray.add("Score <= -20".replace("Score", getStr(R.string.tab_filter_score_text)));
	    	spinnerArray.add("-20 < Score <= -10".replace("Score", getStr(R.string.tab_filter_score_text)));
	    	spinnerArray.add("-10 < Score <= 0".replace("Score", getStr(R.string.tab_filter_score_text)));
	    	spinnerArray.add("0 < Score <= 10".replace("Score", getStr(R.string.tab_filter_score_text)));
	    	spinnerArray.add("10 < Score <= 20".replace("Score", getStr(R.string.tab_filter_score_text)));
	    	spinnerArray.add("Score > 20".replace("Score", getStr(R.string.tab_filter_score_text)));
	    	sp = (Spinner) v.findViewById(R.id.sp_score_list);
	    	spinnerArrayAdapter = new ArrayAdapter(this.getActivity(),
	    			R.layout.spinner_item,
	    	            spinnerArray);
	    	sp.setAdapter(spinnerArrayAdapter);
	    	
	    	v.findViewById(R.id.bt_filter).setEnabled(true);
	    	v.findViewById(R.id.bt_reset).setEnabled(true);
    	}
    }
    
    public void fill_values(int headerPosition){
    	final String[][] db = dfProcessor.getData();
    	int dbLength = dfProcessor.getCountLinesFound();
    	ArrayList<String> spinnerArray = new ArrayList<String>();
    	spinnerArray.add(getStr(R.string.tab_filter_all_text));
    	for (int i=0; i<dbLength; i++){
    		if (! spinnerArray.contains(db[i][headerPosition])){
    			spinnerArray.add(db[i][headerPosition]);
    		}
    	}  
    	View v = this.getView();
    	Spinner sp = (Spinner) v.findViewById(R.id.sp_value_list);
    	ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this.getActivity(),
    			R.layout.spinner_item, 
    	            spinnerArray);
    	sp.setAdapter(spinnerArrayAdapter); 
    }
    
    public void filter_data(){
    	EditText et_match_pattern = (EditText) this.getActivity().findViewById(R.id.et_match_pattern);
    	Spinner sp = (Spinner) this.getActivity().findViewById(R.id.sp_header_list);
    	int selectedHeaderIndex = sp.getSelectedItemPosition();
    	String pattern = ".*" + et_match_pattern.getText().toString() + ".*";
    	
    	sp = (Spinner) this.getActivity().findViewById(R.id.sp_score_list);
    	int selectedScoreIndex = sp.getSelectedItemPosition();
    	
    	sp = (Spinner) this.getActivity().findViewById(R.id.sp_value_list);
    	String selectedValue = sp.getSelectedItem().toString();
    	
    	String[][] oldData = dfProcessor.getData();
    	int oldDataLength = dfProcessor.getCountLinesFound();
    	int dataHeadersLength = dfProcessor.getCountDataHeadersFound();
    	String[][] newData = new String[oldDataLength][dataHeadersLength+1];
    	int indexScoreHeader = dfProcessor.getIndexScoreHeader();
    	int newDataLength = 0;
    	for (int i=0; i<oldDataLength; i++){
    		if (oldData[i][selectedHeaderIndex].matches(pattern)
    			&& ( oldData[i][selectedHeaderIndex].equals(selectedValue) || selectedValue.equals(getStr(R.string.tab_filter_all_text)) )
    			&& 	( (selectedScoreIndex == 0 )
    					||
    				  (selectedScoreIndex == 1 && Integer.valueOf(oldData[i][indexScoreHeader]) <=-20)
    					||
    				  (selectedScoreIndex == 2 && Integer.valueOf(oldData[i][indexScoreHeader]) > -20 && Integer.valueOf(oldData[i][indexScoreHeader]) <= -10)
    				    ||
    				  (selectedScoreIndex == 3 && Integer.valueOf(oldData[i][indexScoreHeader]) > -10 && Integer.valueOf(oldData[i][indexScoreHeader]) <= 0)
    				    ||
    				  (selectedScoreIndex == 4 && Integer.valueOf(oldData[i][indexScoreHeader]) > 0 && Integer.valueOf(oldData[i][indexScoreHeader]) <= 10)
    				    ||
    				  (selectedScoreIndex == 5 && Integer.valueOf(oldData[i][indexScoreHeader]) > 10 && Integer.valueOf(oldData[i][indexScoreHeader]) <= 20)
    				    ||
    				  (selectedScoreIndex == 6 && Integer.valueOf(oldData[i][indexScoreHeader]) > 20 )
    			
    				  )
    				){
		    			for (int j=0; j<=dataHeadersLength; j++){
		    				newData[newDataLength][j] = oldData[i][j];    				
    			}
		    	// store link to original line number
		    	if (newData[newDataLength][dataHeadersLength] == null){
		    		newData[newDataLength][dataHeadersLength] = i + "";   		
		    	}
    			newDataLength++;
    		}
    	}
    	dfProcessor.setData(newData);
    	dfProcessor.setDataLength(newDataLength);
    	Toast.makeText(this.getView().getContext(), newDataLength + getStr(R.string.tab_filter_msg_result) + oldDataLength, Toast.LENGTH_SHORT).show();
    	mListener.onFilterData(dfProcessor);
    }
    
    public void reset_data(){
    	dfProcessor.resetData();	
    	Toast.makeText(this.getView().getContext(), dfProcessor.getCountLinesFound() + getStr(R.string.tab_filter_msg_undo), Toast.LENGTH_SHORT).show();
    	mListener.onFilterData(dfProcessor);
    }
    
    public DataFolderProcessor getDataFolderProcessor(){
    	return dfProcessor;
    }
    
    public interface OnFilterDataListener {
        public void onFilterData(DataFolderProcessor dfProcessor);
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFilterDataListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnLoadDataListener");
        }
    }

	@Override
	public void fragmentBecameVisible() {
		dfProcessor = ((MainActivity)this.getActivity()).getDataFolderProcessor();
		generate_layout(dfProcessor);
		
	}
}
