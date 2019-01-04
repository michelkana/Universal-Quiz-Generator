package com.karewa.vietnamese;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import com.karewa.vietnamese.FilterDataFragment.OnFilterDataListener;
import com.karewa.vietnamese.LoadDataFragment.OnLoadDataListener;
import com.karewa.vietnamese.QuizFragment.OnQuizAnswerListener;
import com.karewa.vietnamese.R.id;
import com.karewa.vietnamese.R.layout;
import com.karewa.vietnamese.R.menu;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
ActionBar.TabListener, OnLoadDataListener, OnFilterDataListener, OnQuizAnswerListener{



	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	DataFolderProcessor dfProcessor=null;
	
	 private ViewPager viewPager;
	 private TabsPagerAdapter mAdapter;
	 private ActionBar actionBar;
	 //private String[] tabs = { "Quiz", "Learn", "Load Data", "Filter Data" };
	 private String[] tabs = { "Quiz", "Learn","Filter Data" };
	 public static final String PREF_FILE_NAME = "QuizGeneratorPrefs.txt";
	   

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
				/*
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);*/
		
		// Initialization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
 
        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);       
 
        // Adding Tabs
        tabs[0] = this.getApplicationContext().getResources().getString(R.string.tab_quiz_title);
		tabs[1] = this.getApplicationContext().getResources().getString(R.string.tab_learn_title);
		tabs[2] = this.getApplicationContext().getResources().getString(R.string.tab_filter_title);
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }
        
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
        	 
            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
                switch (position) {
                case 0:
                	QuizFragmentInterface quizFragment = (QuizFragmentInterface) mAdapter.instantiateItem(viewPager, position);
                    if (quizFragment != null) {
                    	quizFragment.fragmentBecameVisible();
                    };
                    break;
                case 1:
                	LearnFragmentInterface learnFragment = (LearnFragmentInterface) mAdapter.instantiateItem(viewPager, position);
                    if (learnFragment != null) {
                    	learnFragment.fragmentBecameVisible();
                    };
                    break;
                /*case 2:
                	LoadDataFragmentInterface loadDataFragment = (LoadDataFragmentInterface) mAdapter.instantiateItem(viewPager, position);
                    if (loadDataFragment != null) {
                    	loadDataFragment.fragmentBecameVisible();
                    };
                    break;*/
        	    case 2:
        	    	FilterDataFragmentInterface filterDataFragment = (FilterDataFragmentInterface) mAdapter.instantiateItem(viewPager, position);
                    if (filterDataFragment != null) {
                    	filterDataFragment.fragmentBecameVisible();
                    };
                    break;
        	    }
                 
            }
         
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }
         
            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        //BufferedReader bfr = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(Uri.parse("android.resource://com.karewa.languagequizgenerator/raw/words_text"))));
		dfProcessor = new DataFolderProcessor(this);
		dfProcessor.loadData();
        
        
        /*SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, 0);
        String lastDataDirectory = preferences.getString("LastDataDirectory", null);
        Toast.makeText(this, "Loading data", Toast.LENGTH_SHORT).show();    	
        
        if (lastDataDirectory != null && dfProcessor == null){
        	dfProcessor = new DataFolderProcessor(lastDataDirectory);
        	dfProcessor.loadData();
        }*/

	}
	
	public String getStr(int id){
    	return getResources().getString(id);
    }
	
	public interface QuizFragmentInterface {
	    void fragmentBecameVisible();
	}
	public interface LearnFragmentInterface {
	    void fragmentBecameVisible();
	}
	public interface FilterDataFragmentInterface {
	    void fragmentBecameVisible();
	}
	
	public interface LoadDataFragmentInterface {
	    void fragmentBecameVisible();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	@Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {
    }
 
    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());        
    }
 
    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {
    }
      

	@Override
	public void onLoadData(DataFolderProcessor df) {
		/*LearnFragment learnFragment = (LearnFragment)getSupportFragmentManager().
		        findFragmentByTag("android:switcher:" + viewPager.getId() + ":1");
		learnFragment.generate_layout(df, 1);
		FilterDataFragment filderDataFragment = (FilterDataFragment)getSupportFragmentManager().
		        findFragmentByTag("android:switcher:" + viewPager.getId() + ":3");
		filderDataFragment.generate_layout(df);*/
		dfProcessor = df;
		SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, 0);
		SharedPreferences.Editor editor = preferences.edit();
		editor.clear();
		editor.putString("LastDataDirectory", dfProcessor.getDataFolderPath()); 
		editor.commit();
	}
	
	@Override
	public void onFilterData(DataFolderProcessor df) {
		/*LearnFragment learnFragment = (LearnFragment)getSupportFragmentManager().
		        findFragmentByTag("android:switcher:" + viewPager.getId() + ":1");
		learnFragment.generate_layout(df, 1);*/		
		dfProcessor = df;
	}
	
	public DataFolderProcessor getDataFolderProcessor(){
		return dfProcessor;
	}


	@Override
	public void onQuizAnswer(DataFolderProcessor df) {
		dfProcessor = df;		
	}

   
}
