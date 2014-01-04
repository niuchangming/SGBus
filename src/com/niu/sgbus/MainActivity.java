package com.niu.sgbus;

import java.util.ArrayList;
import java.util.List;

import com.niu.network.BaseRequestListener;
import com.niu.network.RestfulCall;
import com.niu.sildingmenu.SlidingMenu;
import com.niu.tools.Constants;
import com.niu.tools.LocationHelper;
import com.niu.tools.Utils;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	private final String TAG = "MainActivity";
	public double radius = 0.6;
	private int[] titles = new int[]{R.string.nearby, R.string.all, R.string.bookmark};
	public static List<Fragment> fragments;
	private ViewPager viewPager;
	private TitlePageIndicator titleIndicator;
	public EditText searchTextView;
	public SlidingMenu rightMenu;
	public SlidingMenu leftMenu;
	private Button mapBtn;
	private Button menuBtn;
	public boolean canOpenMap;
	private EditText searchBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		if(!Utils.isConnected(this)){
			Toast.makeText(this, "Network connection error...", Toast.LENGTH_LONG).show();
			return;
		}
		initViews();
		initValues();
	}
	
	public void initValues(){
		initLeftSildingMenu();
		initRightSildingMenu();
		canOpenMap = false;
	}
	
	private void initRightSildingMenu() {
		rightMenu = new SlidingMenu(this);
		rightMenu.setMode(SlidingMenu.RIGHT);
		rightMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		rightMenu.setShadowWidthRes(R.dimen.shadow_width);
		rightMenu.setShadowDrawable(R.drawable.right_shadow);
		rightMenu.setBehindOffset(60);
		rightMenu.setFadeDegree(0.5f);
		rightMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		rightMenu.setMenu(R.layout.right_menu_layout);
		BusMapFragment fragment = (BusMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
		((NearbyListFragment)fragments.get(0)).updateMarkerListener = fragment;
		
		mapBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(canOpenMap){
					rightMenu.showMenu();
				}
			}
		});
	}
	
	private void initLeftSildingMenu() {
		leftMenu = new SlidingMenu(this);
		leftMenu.setMode(SlidingMenu.LEFT);
		leftMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		leftMenu.setShadowWidthRes(R.dimen.shadow_width);
		leftMenu.setShadowDrawable(R.drawable.left_shadow);
		leftMenu.setBehindOffset(60);
		leftMenu.setFadeDegree(0.5f);
		leftMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		leftMenu.setMenu(R.layout.left_menu_layout);

		menuBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				leftMenu.showMenu();
			}
		});
		
		searchBar = (EditText) leftMenu.findViewById(R.id.left_search_editext);
		Button searchGoBtn = (Button) leftMenu.findViewById(R.id.search_go_btn);
		searchGoBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				performSearch();
			}});
		searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
		    @Override
		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
		        	performSearch();
		            return true;
		        }
		        return false;
		    }
		});
	}
	
	private void performSearch(){
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, SearchActivity.class);
		intent.putExtra("search_text", searchBar.getText().toString());
		MainActivity.this.startActivity(intent);
	}

	private void initViews(){
		searchTextView = (EditText) findViewById(R.id.search_edittext);
		mapBtn = (Button) findViewById(R.id.global_btn);
		
		menuBtn = (Button) findViewById(R.id.menu_search_btn);
	
		fragments = new ArrayList<Fragment>();
		
		NearbyListFragment neabyFragment = new NearbyListFragment();
		neabyFragment.setRetainInstance(true);
		fragments.add(neabyFragment);
		
		AllServiceFragment allFragment = new AllServiceFragment();
		allFragment.setRetainInstance(true);
		fragments.add(allFragment);
		
		BookmarkFragment bookmarkFragment = new BookmarkFragment();
		bookmarkFragment.setRetainInstance(true);
		fragments.add(bookmarkFragment);
		
		viewPager = (ViewPager)findViewById(R.id.main_viewpager);
		viewPager.setAdapter(new NearbyBusPagerAdapter(getSupportFragmentManager()));
		titleIndicator = (TitlePageIndicator)findViewById(R.id.main_title_indicator);
		titleIndicator.setFooterIndicatorStyle(IndicatorStyle.Triangle);
		titleIndicator.setTextColor(getResources().getColor(R.color.indicator));
		titleIndicator.setSelectedColor(getResources().getColor(R.color.black));
		titleIndicator.setViewPager(viewPager);
	}
	
	public class NearbyBusPagerAdapter extends FragmentStatePagerAdapter {
		public NearbyBusPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			return super.instantiateItem(container, position);
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return getResources().getString(titles[position]);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
			
		}

	}

	@Override
	protected void onDestroy() {
		LocationHelper.getInstance(this).clear();
		super.onDestroy();
	}

}
