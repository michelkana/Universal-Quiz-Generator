package com.karewa.vietnamese;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.karewa.vietnamese.MainActivity.LearnFragmentInterface;
import com.karewa.vietnamese.R.drawable;
import com.karewa.vietnamese.R.style;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
 
public class LearnFragment extends Fragment implements LearnFragmentInterface {
	private static final int ITEMS_PER_PAGE=10;
	private Map<String, MediaPlayer> audioPlayers = new HashMap<String, MediaPlayer>();
	DataFolderProcessor dfProcessor;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        //View rootView = inflater.inflate(R.layout.fragment_learn, container, false);
    	
    	
    	
    	ScrollView  rootView = new ScrollView (this.getActivity());
        LinearLayout ll = new LinearLayout(this.getActivity());
        ll.setOrientation(LinearLayout.VERTICAL);
    	ll.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
    	
        ll.setId(1);
        TextView tv = new TextView(this.getActivity());
        tv.setId(11);
        tv.setText(getStr(R.string.global_msg_loaddata));
        ll.addView(tv);
        rootView.addView(ll);
        return rootView;
    }
    
    public String getStr(int id){
    	return getResources().getString(id);
    }
    
    public void generate_layout(final DataFolderProcessor dfProcessor, final int pageId){
    	if (dfProcessor!=null){
	    	String[][] dataHeaders = dfProcessor.getDataHeaders();
	    	int dataHeadersLength = dfProcessor.getCountDataHeadersFound();
	    	final String[][] db = dfProcessor.getData();
	    	int dbLength = dfProcessor.getCountLinesFound();
	    	int nbPage = (int) Math.ceil((double)dbLength/ITEMS_PER_PAGE);
	    	
	    	ScrollView  newView = new ScrollView (this.getActivity());
	    	
	    	LinearLayout mainLinearLayout = new LinearLayout(this.getActivity());
	    	mainLinearLayout.setOrientation(LinearLayout.VERTICAL);
	    	mainLinearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
	    	mainLinearLayout.setWeightSum(3f);
	    		
	    	LinearLayout buttonLinearLayout = new LinearLayout(this.getActivity());
	    	buttonLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
	    	buttonLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
	    	
	    	Button bt = new Button(this.getActivity());
	    	bt.setText(getStr(R.string.tab_learn_previous_bt));
	    	if (pageId<=1){
	    		bt.setEnabled(false);
	    	}else{
	    		bt.setEnabled(true);
		    	bt.setOnClickListener(new OnClickListener() {
		            @Override
		            public void onClick(final View v) {
		            	generate_layout(dfProcessor, pageId-1);
		            }
		        });
	    	}
	    	buttonLinearLayout.addView(bt);
	    	
	    	bt = new Button(this.getActivity());
	    	bt.setText(getStr(R.string.tab_learn_next_bt));
	    	if (pageId>=nbPage){
	    		bt.setEnabled(false);
	    	}else{
	    		bt.setEnabled(true);
		    	bt.setOnClickListener(new OnClickListener() {
		            @Override
		            public void onClick(final View v) {
		            	generate_layout(dfProcessor, pageId+1);
		            }
		        });
	    	}
	    	buttonLinearLayout.addView(bt);
	    	
	    	TextView tv = new TextView(this.getActivity());
	        tv.setText(pageId + "/" + nbPage);
	        buttonLinearLayout.addView(tv);
	        
	        mainLinearLayout.addView(buttonLinearLayout);
	    	
	        LinearLayout headersLinearLayout = new LinearLayout(this.getActivity());
	        headersLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
	        headersLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));	        
	        int displayedHeadersCount = 0;
	    	for (int i=0; i<dataHeadersLength; i++){
	    		if (dataHeaders[i][2].equals("TXT") || dataHeaders[i][2].equals("MCQ") || dataHeaders[i][2].equals("AUDIO") || dataHeaders[i][2].equals("IMG")){
					tv = new TextView(this.getActivity());
			        tv.setText(dataHeaders[i][1]);
			        tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
			        headersLinearLayout.addView(tv);
			        displayedHeadersCount++;
	    		}
			}
	    	headersLinearLayout.setWeightSum(displayedHeadersCount);
	    	mainLinearLayout.addView(headersLinearLayout);
	    	
	    	LinearLayout rowsLinearLayout = new LinearLayout(this.getActivity());
	    	rowsLinearLayout.setOrientation(LinearLayout.VERTICAL);
	    	rowsLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, Math.min(pageId*ITEMS_PER_PAGE,dbLength)-(pageId-1)*ITEMS_PER_PAGE+1));
			for (int i=(pageId-1)*ITEMS_PER_PAGE; i<Math.min(pageId*ITEMS_PER_PAGE,dbLength); i++){
				LinearLayout rowLinearLayout = new LinearLayout(this.getActivity());
		    	rowLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		    	rowLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
		    	rowLinearLayout.setWeightSum(displayedHeadersCount);
				for (int j=0; j<dataHeadersLength; j++){
					if (dataHeaders[j][2].equals("TXT") || dataHeaders[j][2].equals("MCQ")){
						tv = new TextView(this.getActivity());
				        tv.setText(db[i][j]);
				        tv.setTextAppearance(this.getActivity().getApplicationContext(), R.style.learnText);
				        tv.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
				        rowLinearLayout.addView(tv);
					}else if (dataHeaders[j][2].equals("AUDIO")){
						String[] audioInformation = db[i][j].split(",");   
						final String audioFile = audioInformation[0];
						String audioTiming = audioInformation[1];
		            	final int startTime = Integer.valueOf(audioTiming.split("-")[0]);
		            	final int duration = Integer.valueOf(audioTiming.split("-")[1]);
						if (! audioPlayers.containsKey(audioFile)){						
							try {
								MediaPlayer mp;
								if (dfProcessor.getEmbeddedRessource()){
									mp = MediaPlayer.create(this.getActivity(), Uri.parse("android.resource://com.karewa.vietnamese/raw/"+audioFile.replaceFirst("[.][^.]+$", "")));
								}else{
									mp = new MediaPlayer();
									mp.setDataSource(dfProcessor.getDataFolderPath() + "/" + audioFile);
									mp.prepare();
								}								
								audioPlayers.put(audioFile, mp);
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (SecurityException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalStateException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}						
						}
						ImageButton ib = new ImageButton(this.getActivity());
						ib.setImageResource(R.drawable.play);
				    	ib.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
				    	ib.setScaleType(ImageView.ScaleType.FIT_CENTER);
				    	if (! audioPlayers.containsKey(audioFile)){
				    		ib.setEnabled(false);
				    	}else{
				    		ib.setOnClickListener(new OnClickListener() {
					            @Override
					            public void onClick(final View v) {
					            	if (startTime>0){
					            		audioPlayers.get(audioFile).seekTo(startTime*1000);
					            	}
					            	audioPlayers.get(audioFile).start();  
					    	        new CountDownTimer(duration*1000, 1000) {
					    	            public void onTick(long millisUntilFinished) {
					    	            }
					    	            public void onFinish() {
					    	            	audioPlayers.get(audioFile).pause();
					    	            }
					    	         }.start();
					            }
					        });
				    	}
				    	rowLinearLayout.addView(ib);
					}else if (dataHeaders[j][2].equals("IMG")){
						String imageFile = dfProcessor.getDataFolderPath() + "/" + db[i][j];
						Bitmap bmp = BitmapFactory.decodeFile(imageFile);
						ImageView img = new ImageView(this.getActivity());
						img.setImageBitmap(bmp);
						img.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, 1f));
						rowLinearLayout.addView(img);
					}
				}
				rowsLinearLayout.addView(rowLinearLayout);
			}
			
			mainLinearLayout.addView(rowsLinearLayout);
			
			newView.addView(mainLinearLayout);
			View currentView = this.getView();
			 LinearLayout rl = (LinearLayout) currentView.findViewById(1);
			    rl.removeAllViews();
			    rl.addView(newView);
    	}
	}
    
	@Override
	public void fragmentBecameVisible() {
		dfProcessor = ((MainActivity)this.getActivity()).getDataFolderProcessor();
		generate_layout(dfProcessor, 1);	
	} 
}
