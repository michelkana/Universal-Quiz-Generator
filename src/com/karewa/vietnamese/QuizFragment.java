package com.karewa.vietnamese;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.karewa.vietnamese.LoadDataFragment.OnLoadDataListener;
import com.karewa.vietnamese.MainActivity.QuizFragmentInterface;
import com.karewa.vietnamese.R.drawable;
import com.karewa.vietnamese.R.id;
import com.karewa.vietnamese.R.layout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
 
public class QuizFragment extends Fragment implements QuizFragmentInterface {
	protected static final int SCORE_PLUS = +1;
	protected static final int SCORE_MINUS = -1;
	DataFolderProcessor dfProcessor;
	static Random generator = new Random();
	String[] quizVector;
	int quizIndex;
	int quizSize;
	boolean manualQuiz;
	int questionHeaderIndex;
	int answerHeaderIndex;
	String questionHeaderType;
	String answerHeaderType;
	int score;
	OnQuizAnswerListener mListener;
	int questionIndex;
	int countAnswerTrials;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_quiz, container, false);
       
        

        //rootView.findViewById(R.id.bt_load_quiz).setOnClickListener(clickListener);
        rootView.findViewById(R.id.bt_start_quiz).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                       start_quiz();   
                }
            });
              
               
        rootView.findViewById(R.id.bt_start_quiz).setEnabled(false);
        rootView.findViewById(R.id.bt_question).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.bt_answer1).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.bt_answer2).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.bt_answer3).setVisibility(View.INVISIBLE);  
        rootView.findViewById(R.id.bt_play_answer1).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.bt_play_answer2).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.bt_play_answer3).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.bt_answer_text).setVisibility(View.INVISIBLE);
        rootView.findViewById(R.id.et_answer_text).setVisibility(View.INVISIBLE);                
        rootView.findViewById(R.id.et_answer_text).setVisibility(View.INVISIBLE);      
                       
        return rootView;
    }
    
    public String getStr(int id){
    	return getResources().getString(id);
    }
    
    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fragmentBecameVisible();
    }
    
    public void load_quiz(){
    	if (dfProcessor!=null){
	    	String[][] quizSetup = dfProcessor.getQuizSetup();
	    	int quizSetupLength = dfProcessor.getCountQuizSetupFound();
	    	int dbLength = dfProcessor.getCountLinesFound();
	    	ArrayList<String> spinnerArray = new ArrayList<String>();
	    	for (int i=0; i<quizSetupLength; i++){
	    		spinnerArray.add(quizSetup[i][1]);
	    	}    	
	    	Spinner sp = (Spinner) this.getActivity().findViewById(R.id.sp_quiz_list);
	    	ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(this.getActivity(),
	    			R.layout.spinner_item,
	    	            spinnerArray);
	    	sp.setAdapter(spinnerArrayAdapter);    
	    	
	    	spinnerArray = new ArrayList<String>();
	    	for (int i=10; i<dbLength+10; i=i+10){
	    		spinnerArray.add(i+"");
	    	}    	
	    	sp = (Spinner) this.getActivity().findViewById(R.id.sp_quiz_size_list);
	    	spinnerArrayAdapter = new ArrayAdapter(this.getActivity(),
	    	        R.layout.spinner_item,
	    	            spinnerArray);
	    	sp.setAdapter(spinnerArrayAdapter);
	    	sp.setSelection(sp.getCount()-1);
	    	
	    	this.getActivity().findViewById(R.id.bt_start_quiz).setEnabled(true);
    	}
    }

    public void start_quiz(){
    	String[][] dataHeaders = dfProcessor.getDataHeaders();
    	String[][] quizSetup = dfProcessor.getQuizSetup();
    	int dbLength = dfProcessor.getCountLinesFound();
    	if (dbLength>0){
	    	Spinner sp = (Spinner) this.getActivity().findViewById(R.id.sp_quiz_list);    
	    	Spinner et = (Spinner) this.getActivity().findViewById(R.id.sp_quiz_size_list);
	    	CheckBox cb = (CheckBox) this.getActivity().findViewById(R.id.cb_quiz_manual);
	    	
	    	String selectedQuiz = quizSetup[sp.getSelectedItemPosition()][0];    	
	    	quizSize = Math.min(Integer.valueOf(et.getSelectedItem().toString()), dbLength);
	    	manualQuiz = cb.isChecked();
	    	
	    	questionHeaderIndex = Integer.valueOf(selectedQuiz.split("-")[0]);
	    	answerHeaderIndex = Integer.valueOf(selectedQuiz.split("-")[1]);
	    	questionHeaderType = dataHeaders[questionHeaderIndex][2];
	    	answerHeaderType = dataHeaders[answerHeaderIndex][2]; 	
	    	    	
	    	//quizVector = generate_quiz_vector(dbLength, quizSize);    
	    	quizVector = dfProcessor.generate_quiz_vector(quizSize, answerHeaderIndex);   
	    	quizIndex = -1;
	    	
	    	score = 0;
	    	
	    	if (! (manualQuiz && ! answerHeaderType.equals("TXT"))){
	    		next_question();
	    	}else{
	    		Toast.makeText(this.getView().getContext(), getStr(R.string.tab_quiz_msg_textualinput), Toast.LENGTH_SHORT).show();
	    	}
    	}else{
    		Toast.makeText(this.getView().getContext(), getStr(R.string.global_msg_loaddata), Toast.LENGTH_SHORT).show();
    	}
    }
    
    public void next_question(){
    	String[][] db = dfProcessor.getData();
    	
    	Button bt_question = (Button) this.getActivity().findViewById(R.id.bt_question);
    	Button bt_answer1 = (Button) this.getActivity().findViewById(R.id.bt_answer1);
    	Button bt_answer2 = (Button) this.getActivity().findViewById(R.id.bt_answer2);
    	Button bt_answer3 = (Button) this.getActivity().findViewById(R.id.bt_answer3);  
    	Button bt_play_answer1 = (Button) this.getActivity().findViewById(R.id.bt_play_answer1);
    	Button bt_play_answer2 = (Button) this.getActivity().findViewById(R.id.bt_play_answer2);
    	Button bt_play_answer3 = (Button) this.getActivity().findViewById(R.id.bt_play_answer3);
    	Button bt_answer_text = (Button) this.getActivity().findViewById(R.id.bt_answer_text);
    	EditText et_answer_text = (EditText) this.getActivity().findViewById(R.id.et_answer_text);    	  
    	
    	countAnswerTrials = 0;
 
    	if (quizIndex < quizSize-1){
    		
    		quizIndex++;	
    		String[] quizString = quizVector[quizIndex].split("-");
        	questionIndex = Integer.valueOf(quizString[0]); 
        	int answer1Index = Integer.valueOf(quizString[1]); 
        	int answer2Index = Integer.valueOf(quizString[2]); 
        	int answer3Index = Integer.valueOf(quizString[3]); 
        	
	    	if (questionHeaderType.equals("TXT")){
	    		bt_question.setOnClickListener(null);
	    		setButton(bt_question, db[questionIndex][questionHeaderIndex], null, null, "N/A", null);
	    	}else if (questionHeaderType.equals("AUDIO")){
	    		bt_question.setOnClickListener(null);
	    		setButton(bt_question, null, db[questionIndex][questionHeaderIndex], null, "N/A", null);    		
	    	}else if (questionHeaderType.equals("IMG")){
	    		bt_question.setOnClickListener(null);
	    		setButton(bt_question, null, null, db[questionIndex][questionHeaderIndex], "N/A", null);    		
	    	}
	    	
	    	if (manualQuiz && answerHeaderType.equals("TXT")){
	    		bt_play_answer1.setVisibility(View.INVISIBLE);
	    		bt_play_answer2.setVisibility(View.INVISIBLE);
	    		bt_play_answer3.setVisibility(View.INVISIBLE);
	    		bt_answer1.setVisibility(View.INVISIBLE);
	    		bt_answer2.setVisibility(View.INVISIBLE);
	    		bt_answer3.setVisibility(View.INVISIBLE);
	    		et_answer_text.setVisibility(View.VISIBLE);
	    		setButton(bt_answer_text, "Submit", null, null, db[questionIndex][answerHeaderIndex], et_answer_text);
	    	}else{
	    		et_answer_text.setVisibility(View.INVISIBLE);
	    		bt_answer_text.setVisibility(View.INVISIBLE);
		    	if (answerHeaderType.equals("TXT")){
		    		setButton(bt_answer1, db[answer1Index][answerHeaderIndex], null, null, answer1Index==questionIndex?"YES":"NO", null);
		    		bt_play_answer1.setVisibility(View.INVISIBLE);
		    		setButton(bt_answer2, db[answer2Index][answerHeaderIndex], null, null, answer2Index==questionIndex?"YES":"NO", null);
		    		bt_play_answer2.setVisibility(View.INVISIBLE);
		    		setButton(bt_answer3, db[answer3Index][answerHeaderIndex], null, null, answer3Index==questionIndex?"YES":"NO", null);
		    		bt_play_answer3.setVisibility(View.INVISIBLE);
		    		//setButton(bt_answer_text, "Submit", null, null, db[questionIndex][answerHeaderIndex], et_answer_text);
		    		//et_answer_text.setVisibility(View.VISIBLE);
		    	}else if (answerHeaderType.equals("AUDIO")){
		    		setButton(bt_play_answer1, null, db[answer1Index][answerHeaderIndex], null, "N/A", null);
		    		setButton(bt_answer1, "Answer1", null, null, answer1Index==questionIndex?"YES":"NO", null);
		    		setButton(bt_play_answer2, null, db[answer2Index][answerHeaderIndex], null, "N/A", null);
		    		setButton(bt_answer2, "Answer2", null, null, answer2Index==questionIndex?"YES":"NO", null);
		    		setButton(bt_play_answer3, null, db[answer3Index][answerHeaderIndex], null, "N/A", null);
		    		setButton(bt_answer3, "Answer3", null, null, answer3Index==questionIndex?"YES":"NO", null);    		
		    		bt_answer_text.setVisibility(View.INVISIBLE);
		    		et_answer_text.setVisibility(View.INVISIBLE);
		    	}else if (answerHeaderType.equals("IMG")){
		    		setButton(bt_answer1, null, null, db[answer1Index][answerHeaderIndex], answer1Index==questionIndex?"YES":"NO", null);
		    		bt_play_answer1.setVisibility(View.INVISIBLE);
		    		setButton(bt_answer2, null, null, db[answer2Index][answerHeaderIndex], answer2Index==questionIndex?"YES":"NO", null);
		    		bt_play_answer2.setVisibility(View.INVISIBLE);
		    		setButton(bt_answer3, null, null, db[answer3Index][answerHeaderIndex], answer3Index==questionIndex?"YES":"NO", null);
		    		bt_play_answer3.setVisibility(View.INVISIBLE);  		
		    		bt_answer_text.setVisibility(View.INVISIBLE);
		    		et_answer_text.setVisibility(View.INVISIBLE);
		    	}else if (answerHeaderType.equals("MCQ")){
		    		String mcqStr = db[questionIndex][answerHeaderIndex];
            		String[] mcq = mcqStr.split("~");
            		int correctButton = 1+generator.nextInt(3);
            		switch (correctButton){
            			case 1: setButton(bt_answer1, mcq[0], null, null, "YES", null);
    		    				bt_play_answer1.setVisibility(View.INVISIBLE);    
    		    				setButton(bt_answer2, mcq[1], null, null, "NO", null);
    		    				bt_play_answer2.setVisibility(View.INVISIBLE); 
    		    				setButton(bt_answer3, mcq[2], null, null, "NO", null);
    		    				bt_play_answer3.setVisibility(View.INVISIBLE); 
    		    				break;
            			case 2: setButton(bt_answer1, mcq[1], null, null, "NO", null);
			    				bt_play_answer1.setVisibility(View.INVISIBLE);    
			    				setButton(bt_answer2, mcq[0], null, null, "YES", null);
			    				bt_play_answer2.setVisibility(View.INVISIBLE); 
			    				setButton(bt_answer3, mcq[2], null, null, "NO", null);
			    				bt_play_answer3.setVisibility(View.INVISIBLE); 
			    				break;
            			case 3: setButton(bt_answer1, mcq[1], null, null, "NO", null);
			    				bt_play_answer1.setVisibility(View.INVISIBLE);    
			    				setButton(bt_answer2, mcq[2], null, null, "NO", null);
			    				bt_play_answer2.setVisibility(View.INVISIBLE); 
			    				setButton(bt_answer3, mcq[0], null, null, "YES", null);
			    				bt_play_answer3.setVisibility(View.INVISIBLE); 
			    				break;
            		}
		    	}
	    	}
	    	
    	}else{
    		DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
    		    @Override
    		    public void onClick(DialogInterface dialog, int which) {
    		        switch (which){
    		        case DialogInterface.BUTTON_POSITIVE:
    		            //Yes button clicked
    		        	quizIndex = 0;
    		        	next_question();
    		            break;

    		        case DialogInterface.BUTTON_NEGATIVE:
    		            //No button clicked
    		            break;
    		        }
    		    }
    		};

    		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
    		builder.setMessage(getStr(R.string.tab_quiz_msg_score1) + score + getStr(R.string.tab_quiz_msg_score2) + quizSize + System.getProperty("line.separator") + getStr(R.string.tab_quiz_msg_score3)).setPositiveButton(getStr(R.string.tab_quiz_msgbox_yes), dialogClickListener)
    		    .setNegativeButton(getStr(R.string.tab_quiz_msgbox_no), dialogClickListener).show();
    	}
    	
    }
    
    private void setButton (Button bt, String text, String audioInformation, String imageFile, final String correctlyAnswered, final EditText et_answer_text){
    	bt.setVisibility(View.VISIBLE);
    	if (correctlyAnswered.equals("YES")){
    		bt.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(final View v) {
	            	Toast.makeText(v.getContext(), getStr(R.string.tab_quiz_msg_correct), Toast.LENGTH_SHORT).show();
	            	if (countAnswerTrials==0){
		            	score = score + SCORE_PLUS;
		            	dfProcessor.updateScoreData(questionIndex, SCORE_PLUS);
	            	}
	            	next_question();
	            }
	        });
    	}else if (correctlyAnswered.equals("NO")){
    		bt.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(final View v) {
	            	Toast.makeText(v.getContext(), getStr(R.string.tab_quiz_msg_wrong), Toast.LENGTH_SHORT).show();
	            	score = score + SCORE_MINUS;
	            	dfProcessor.updateScoreData(questionIndex, SCORE_MINUS);
	            	countAnswerTrials++;
	            	mListener.onQuizAnswer(dfProcessor);
	            }
	        });
    	}else if (! correctlyAnswered.equals("N/A")){
    		bt.setOnClickListener(new OnClickListener() {
	            @Override
	            public void onClick(final View v) {
	            	if (et_answer_text.getText().toString().equals(correctlyAnswered)){
	            		Toast.makeText(v.getContext(), getStr(R.string.tab_quiz_msg_correct), Toast.LENGTH_SHORT).show();
	            		if (countAnswerTrials==0){
		            		score = score + SCORE_PLUS;
			            	dfProcessor.updateScoreData(questionIndex, SCORE_PLUS);
			            	mListener.onQuizAnswer(dfProcessor);
	            		}
	            		next_question();
	            	}else{
	            		Toast.makeText(v.getContext(), getStr(R.string.tab_quiz_msg_wrong), Toast.LENGTH_SHORT).show();
	            		score = score + SCORE_MINUS;
		            	dfProcessor.updateScoreData(questionIndex, SCORE_MINUS);
		            	countAnswerTrials++;
	            		mListener.onQuizAnswer(dfProcessor);
	            	}
	            }
	        });
    	}
    	if (text != null){
    		bt.setBackgroundResource(android.R.drawable.btn_default);
    		bt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    		bt.setText(text);
    	}else if (imageFile != null){
			Drawable bgrImage = Drawable.createFromPath(dfProcessor.getDataFolderPath() + "/" + imageFile);
			Bitmap bmp = BitmapFactory.decodeFile(dfProcessor.getDataFolderPath() + "/" + imageFile);
			//bt.setBackgroundDrawable(bgrImage);
			bt.setCompoundDrawablesWithIntrinsicBounds(null, bgrImage, null, null);
			//bt.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT, 1f));
			bt.setText("");
    	}
    	else if (audioInformation != null){
    		//bt.setBackgroundResource(R.drawable.play);
    		bt.setCompoundDrawablesWithIntrinsicBounds(this.getView().getContext().getResources().getDrawable(R.drawable.play), null, null, null);
    		bt.setText(getStr(R.string.tab_quiz_play_bt));
			String audioFile = audioInformation.split(",")[0];
			String audioTiming = audioInformation.split(",")[1];
        	final int startTime = Integer.valueOf(audioTiming.split("-")[0]);
        	final int duration = Integer.valueOf(audioTiming.split("-")[1]);
        	try {
        		final MediaPlayer mp;
				if (dfProcessor.getEmbeddedRessource()){
					mp = MediaPlayer.create(this.getActivity(), Uri.parse("android.resource://com.karewa.vietnamese/raw/"+audioFile.replaceFirst("[.][^.]+$", "")));
					//mp = MediaPlayer.create(getActivity(), R.raw.lesson1_words_audio);
				}else{
					mp = new MediaPlayer();
					mp.setDataSource(dfProcessor.getDataFolderPath() + "/" + audioFile);
					mp.prepare();
				} 
				bt.setOnClickListener(new OnClickListener() {
		            @Override
		            public void onClick(final View v) {
		            	if (startTime>0){
		            		mp.seekTo(startTime*1000);
		            	}
		            	mp.start();  
		    	        new CountDownTimer(duration*1000, 1000) {
		    	            public void onTick(long millisUntilFinished) {
		    	            }
		    	            public void onFinish() {
		    	            	mp.pause();
		    	            }
		    	         }.start();
		            }
		        });
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
    }
    
    @Override
	public void fragmentBecameVisible() {
		dfProcessor = ((MainActivity)this.getActivity()).getDataFolderProcessor();
		load_quiz();		
	}
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnQuizAnswerListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnQuizAnswerListener");
        }
    }
    
	public interface OnQuizAnswerListener {
        public void onQuizAnswer(DataFolderProcessor dfProcessor);
    }
	
}
