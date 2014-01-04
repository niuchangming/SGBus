package com.niu.sgbus;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ScrollView;

public class SildingUpDialog extends ScrollView{
	private final String TAG = "SildingUpDialog";
	private Context context;
	
	private Animation silingUpAnim;
	private Animation silingDownAnim;
	
	public SildingUpDialog(Context context, View view) {
		super(context);
		this.context = context;
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		this.addView(view);
		this.startAnimation(startSildingAnim());
	}
	
	public SildingUpDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	}

	public Animation startSildingAnim(){
		if(silingUpAnim == null){
			silingUpAnim = AnimationUtils.loadAnimation(context, R.anim.silding_up_anim);
			silingUpAnim.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					SildingUpDialog.this.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {}
			});
		}
		return silingUpAnim;
	}
	
	private Animation stopSildingAnim(){
		if(silingDownAnim == null){
			silingDownAnim = AnimationUtils.loadAnimation(context, R.anim.silding_down_anim);
			silingDownAnim.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					SildingUpDialog.this.setVisibility(View.GONE);
				}
			});
		}
		return silingDownAnim;
	}
	
	public void show(){
		if(this.getVisibility() == View.GONE){
			this.startAnimation(startSildingAnim());
		}
	}
	
	public void hide(){
		if(this.getVisibility() == View.VISIBLE){
			this.startAnimation(stopSildingAnim());
		}
	}
	
}
