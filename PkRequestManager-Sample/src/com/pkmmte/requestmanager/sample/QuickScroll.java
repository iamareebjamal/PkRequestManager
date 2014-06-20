package com.pkmmte.requestmanager.sample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.nineoldandroids.view.ViewHelper;


/** Scroller used for the Advanced Sample **/
public class QuickScroll extends View
{
	// IDs
	private static final int ID_PIN = 512;
	private static final int ID_PIN_TEXT = 513;
	// type statics
	public static final int TYPE_POPUP = 0;
	public static final int TYPE_INDICATOR = 1;
	public static final int TYPE_POPUP_WITH_HANDLE = 2;
	public static final int TYPE_INDICATOR_WITH_HANDLE = 3;
	// style statics
	public static final int STYLE_NONE = 0;
	public static final int STYLE_HOLO = 1;
	// base colors
	public static final int GREY_DARK = Color.parseColor("#e0585858");
	public static final int GREY_LIGHT = Color.parseColor("#f0888888");
	public static final int GREY_SCROLLBAR = Color.parseColor("#64404040");
	public static final int BLUE_LIGHT = Color.parseColor("#FF33B5E5");
	public static final int BLUE_LIGHT_SEMITRANSPARENT = Color.parseColor("#8033B5E5");
	private static final int mScrollbarMargin = 10;
	// base variables
	private boolean mScrolling;
	private AlphaAnimation mFadeIn, mFadeOut;
	private TextView mScrollIndicatorText;
	private Scrollable mScrollable;
	private GridView mList;
	private View mScrollbar;
	private int mGroupPosition;
	private int mItemCount;
	private long mFadeDuration = 150;
	private int mType;
	private boolean mInitialized = false;
	private static final int mTextPadding = 4;
	// handlebar variables
	private View mHandlebar;
	// indicator variables
	private RelativeLayout mScrollIndicator;
	
	// default constructors
	public QuickScroll(Context context)
	{
		super(context);
	}
	
	public QuickScroll(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	
	public QuickScroll(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	
	/**
	 * Initializing the QuickScroll, this function must be called.
	 * <p/>
	 * 
	 * @param type
	 *            the QuickScroll type. Available inputs: <b>QuickScroll.TYPE_POPUP</b> or <b>QuickScroll.TYPE_INDICATOR</b>
	 * @param list
	 *            the GridView
	 * @param scrollable
	 *            the adapter, must implement Scrollable interface
	 */
	public void init(final int type, final GridView list, final Scrollable scrollable, final int style)
	{
		if (mInitialized)
			return;
		
		mType = type;
		mList = list;
		mScrollable = scrollable;
		mGroupPosition = -1;
		mFadeIn = new AlphaAnimation(.0f, 1.0f);
		mFadeIn.setFillAfter(true);
		mFadeOut = new AlphaAnimation(1.0f, .0f);
		mFadeOut.setFillAfter(true);
		mFadeOut.setAnimationListener(new AnimationListener()
		{
			
			public void onAnimationStart(Animation animation)
			{
			}
			
			public void onAnimationRepeat(Animation animation)
			{
			}
			
			public void onAnimationEnd(Animation animation)
			{
				mScrolling = false;
			}
		});
		mScrolling = false;
		
		final float density = getResources().getDisplayMetrics().density;
		
		mList.setOnTouchListener(new OnTouchListener()
		{
			
			public boolean onTouch(View v, MotionEvent event)
			{
				if (mScrolling && (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN))
				{
					return true;
				}
				return false;
			}
		});
		
		final RelativeLayout.LayoutParams containerparams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		final RelativeLayout container = new RelativeLayout(getContext());
		container.setBackgroundColor(Color.TRANSPARENT);
		containerparams.addRule(RelativeLayout.ALIGN_TOP, getId());
		containerparams.addRule(RelativeLayout.ALIGN_BOTTOM, getId());
		container.setLayoutParams(containerparams);
		
		if (mType == TYPE_POPUP || mType == TYPE_POPUP_WITH_HANDLE)
		{
			
			mScrollIndicatorText = new TextView(getContext());
			mScrollIndicatorText.setTextColor(Color.WHITE);
			mScrollIndicatorText.setVisibility(View.INVISIBLE);
			mScrollIndicatorText.setGravity(Gravity.CENTER);
			final RelativeLayout.LayoutParams popupparams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			
			popupparams.addRule(RelativeLayout.CENTER_IN_PARENT);
			mScrollIndicatorText.setLayoutParams(popupparams);
			
			setPopupColor(GREY_LIGHT, GREY_DARK, 1, Color.WHITE, 1);
			setTextPadding(mTextPadding, mTextPadding, mTextPadding, mTextPadding);
			
			container.addView(mScrollIndicatorText);
		}
		else if (mType == TYPE_INDICATOR || mType == TYPE_INDICATOR_WITH_HANDLE)
		{
			mScrollIndicator = createPin();
			mScrollIndicatorText = (TextView) mScrollIndicator.findViewById(ID_PIN_TEXT);
			
			(mScrollIndicator.findViewById(ID_PIN)).getLayoutParams().width = 25;
			
			container.addView(mScrollIndicator);
		}
		
		// setting scrollbar width
		getLayoutParams().width = (int) (30 * density);
		mScrollIndicatorText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 32);
		
		// scrollbar setup
		if (style != STYLE_NONE)
		{
			final RelativeLayout layout = new RelativeLayout(getContext());
			final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			params.addRule(RelativeLayout.ALIGN_LEFT, getId());
			params.addRule(RelativeLayout.ALIGN_TOP, getId());
			params.addRule(RelativeLayout.ALIGN_RIGHT, getId());
			params.addRule(RelativeLayout.ALIGN_BOTTOM, getId());
			layout.setLayoutParams(params);
			
			mScrollbar = new View(getContext());
			mScrollbar.setBackgroundColor(GREY_SCROLLBAR);
			final RelativeLayout.LayoutParams scrollbarparams = new RelativeLayout.LayoutParams(1, LayoutParams.MATCH_PARENT);
			scrollbarparams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			scrollbarparams.topMargin = mScrollbarMargin;
			scrollbarparams.bottomMargin = mScrollbarMargin;
			mScrollbar.setLayoutParams(scrollbarparams);
			layout.addView(mScrollbar);
			((ViewGroup) mList.getParent()).addView(layout);
			// creating the handlebar
			if (mType == TYPE_INDICATOR_WITH_HANDLE || mType == TYPE_POPUP_WITH_HANDLE)
			{
				mHandlebar = new View(getContext());
				setHandlebarColor(BLUE_LIGHT, BLUE_LIGHT, BLUE_LIGHT_SEMITRANSPARENT);
				final RelativeLayout.LayoutParams handleparams = new RelativeLayout.LayoutParams((int) (12 * density), (int) (36 * density));
				mHandlebar.setLayoutParams(handleparams);
				((RelativeLayout.LayoutParams) mHandlebar.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
				layout.addView(mHandlebar);
				
				mList.setOnScrollListener(new OnScrollListener()
				{
					
					public void onScrollStateChanged(AbsListView view, int scrollState)
					{
						
					}
					
					@SuppressLint("NewApi")
					public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
					{
						if (!mScrolling && totalItemCount - visibleItemCount > 0)
						{
							moveHandlebar(getHeight() * firstVisibleItem / (totalItemCount - visibleItemCount));
						}
					}
				});
			}
		}
		
		mInitialized = true;
		
		((ViewGroup) mList.getParent()).addView(container);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (mList.getAdapter() == null)
			return false;
		mItemCount = mList.getAdapter().getCount();
		if (mItemCount == 0)
			return false;
		
		if (mScrolling && event.getAction() == MotionEvent.ACTION_CANCEL)
		{
			if (mType == TYPE_POPUP || mType == TYPE_POPUP_WITH_HANDLE)
			{
				
				mScrollIndicatorText.startAnimation(mFadeOut);
			}
			else
			{
				
				mScrollIndicator.startAnimation(mFadeOut);
			}
			if (mType == TYPE_INDICATOR_WITH_HANDLE || mType == TYPE_POPUP_WITH_HANDLE)
				mHandlebar.setSelected(false);
		}
		
		switch (mType)
		{
			case TYPE_POPUP:
				return PopupTouchEvent(event);
			case TYPE_POPUP_WITH_HANDLE:
				return PopupTouchEvent(event);
			case TYPE_INDICATOR:
				return IndicatorTouchEvent(event);
			case TYPE_INDICATOR_WITH_HANDLE:
				return IndicatorTouchEvent(event);
			default:
				break;
		}
		return false;
	}
	
	private boolean IndicatorTouchEvent(final MotionEvent event)
	{
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				
				mScrollIndicator.startAnimation(mFadeIn);
				mScrollIndicator.setPadding(0, 0, getWidth(), 0);
				scroll(event.getY());
				mScrolling = true;
				return true;
			case MotionEvent.ACTION_MOVE:
				scroll(event.getY());
				return true;
			case MotionEvent.ACTION_UP:
				
				mScrollIndicator.startAnimation(mFadeOut);
				
				if (mType == TYPE_INDICATOR_WITH_HANDLE || mType == TYPE_POPUP_WITH_HANDLE)
					mHandlebar.setSelected(false);
				return true;
			default:
				break;
		}
		return false;
	}
	
	private boolean PopupTouchEvent(final MotionEvent event)
	{
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				
				mScrollIndicatorText.startAnimation(mFadeIn);
				mScrolling = true;
				scroll(event.getY());
				return true;
			case MotionEvent.ACTION_MOVE:
				scroll(event.getY());
				return true;
			case MotionEvent.ACTION_UP:
				
				mScrollIndicatorText.startAnimation(mFadeOut);
				
				if (mType == TYPE_INDICATOR_WITH_HANDLE || mType == TYPE_POPUP_WITH_HANDLE)
					mHandlebar.setSelected(false);
				return true;
			default:
				break;
		}
		return false;
	}
	
	@SuppressLint("NewApi")
	private void scroll(final float height)
	{
		if (mType == TYPE_INDICATOR || mType == TYPE_INDICATOR_WITH_HANDLE)
		{
			float move = height - (mScrollIndicator.getHeight() / 2);
			
			if (move < 0)
				move = 0;
			else if (move > getHeight() - mScrollIndicator.getHeight())
				move = getHeight() - mScrollIndicator.getHeight();
			
			ViewHelper.setTranslationY(mScrollIndicator, move);
		}
		
		if (mType == TYPE_INDICATOR_WITH_HANDLE || mType == TYPE_POPUP_WITH_HANDLE)
		{
			mHandlebar.setSelected(true);
			moveHandlebar(height - (mHandlebar.getHeight() / 2));
		}
		
		int postition = (int) ((height / getHeight()) * mItemCount);
		
		if (postition < 0)
			postition = 0;
		else if (postition >= mItemCount)
			postition = mItemCount - 1;
		mScrollIndicatorText.setText(mScrollable.getIndicatorForPosition(postition, mGroupPosition));
		mList.setSelection(mScrollable.getScrollPosition(postition, mGroupPosition));
	}
	
	private void moveHandlebar(final float where)
	{
		float move = where;
		if (move < mScrollbarMargin)
			move = mScrollbarMargin;
		else if (move > getHeight() - mHandlebar.getHeight() - mScrollbarMargin)
			move = getHeight() - mHandlebar.getHeight() - mScrollbarMargin;
		
		ViewHelper.setTranslationY(mHandlebar, move);
	}
	
	/**
	 * Sets the fade in and fade out duration of the indicator; default is 150 ms.
	 * <p/>
	 * 
	 * @param millis
	 *            the fade duration in milliseconds
	 */
	public void setFadeDuration(long millis)
	{
		mFadeDuration = millis;
		mFadeIn.setDuration(mFadeDuration);
		mFadeOut.setDuration(mFadeDuration);
	}
	
	/**
	 * Sets the indicator colors, when QuickScroll.TYPE_INDICATOR is selected as type.
	 * <p/>
	 * 
	 * @param background
	 *            the background color of the square
	 * @param tip
	 *            the background color of the tip triangle
	 * @param text
	 *            the color of the text
	 */
	public void setIndicatorColor(final int background, final int tip, final int text)
	{
		if (mType == TYPE_INDICATOR || mType == TYPE_INDICATOR_WITH_HANDLE)
		{
			((Pin) mScrollIndicator.findViewById(ID_PIN)).setColor(tip);
			mScrollIndicatorText.setTextColor(text);
			mScrollIndicatorText.setBackgroundColor(background);
		}
	}
	
	/**
	 * Sets the popup colors, when QuickScroll.TYPE_POPUP is selected as type.
	 * <p/>
	 * 
	 * @param backgroundcolor
	 *            the background color of the TextView
	 * @param bordercolor
	 *            the background color of the border surrounding the TextView
	 * @param textcolor
	 *            the color of the text
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void setPopupColor(final int backgroundcolor, final int bordercolor, final int borderwidthDPI, final int textcolor, float cornerradiusDPI)
	{
		
		final GradientDrawable popupbackground = new GradientDrawable();
		popupbackground.setCornerRadius(cornerradiusDPI * getResources().getDisplayMetrics().density);
		popupbackground.setStroke((int) (borderwidthDPI * getResources().getDisplayMetrics().density), bordercolor);
		popupbackground.setColor(backgroundcolor);
		
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
			mScrollIndicatorText.setBackgroundDrawable(popupbackground);
		else
			mScrollIndicatorText.setBackground(popupbackground);
		
		mScrollIndicatorText.setTextColor(textcolor);
	}
	
	/**
	 * Sets the width and height of the TextView containing the indicatortext. Default is WRAP_CONTENT, WRAP_CONTENT.
	 * <p/>
	 * 
	 * @param widthDP
	 *            width in DP
	 * @param heightDP
	 *            height in DP
	 */
	public void setSize(final int widthDP, final int heightDP)
	{
		final float density = getResources().getDisplayMetrics().density;
		mScrollIndicatorText.getLayoutParams().width = (int) (widthDP * density);
		mScrollIndicatorText.getLayoutParams().height = (int) (heightDP * density);
	}
	
	/**
	 * Sets the padding of the TextView containing the indicatortext. Default is 4 dp.
	 * <p/>
	 * 
	 * @param paddingLeftDP
	 *            left padding in DP
	 * @param paddingTopDP
	 *            top param in DP
	 * @param paddingBottomDP
	 *            bottom param in DP
	 * @param paddingRightDP
	 *            right param in DP
	 */
	public void setTextPadding(final int paddingLeftDP, final int paddingTopDP, final int paddingBottomDP, final int paddingRightDP)
	{
		final float density = getResources().getDisplayMetrics().density;
		mScrollIndicatorText.setPadding((int) (paddingLeftDP * density), (int) (paddingTopDP * density), (int) (paddingRightDP * density), (int) (paddingBottomDP * density));
		
	}
	
	/**
	 * Turns on fixed size for the TextView containing the indicatortext. Do not use with setSize()! This mode looks good if the indicatortext length is fixed, e.g. it's always two characters long.
	 * <p/>
	 * 
	 * @param sizeEMS
	 *            number of characters in the indicatortext
	 */
	public void setFixedSize(final int sizeEMS)
	{
		mScrollIndicatorText.setEms(sizeEMS);
	}
	
	/**
	 * Set the textsize of the TextView containing the indicatortext.
	 * 
	 * @param unit
	 *            - use TypedValue statics
	 * @param size
	 *            - the size according to the selected unit
	 */
	public void setTextSize(final int unit, final float size)
	{
		mScrollIndicatorText.setTextSize(unit, size);
	}
	
	/**
	 * Set the colors of the handlebar.
	 * 
	 * @param inactive
	 *            - color of the inactive handlebar
	 * @param activebase
	 *            - base color of the active handlebar
	 * @param activestroke
	 *            - stroke of the active handlebar
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	public void setHandlebarColor(final int inactive, final int activebase, final int activestroke)
	{
		if (mType == TYPE_INDICATOR_WITH_HANDLE || mType == TYPE_POPUP_WITH_HANDLE)
		{
			final float density = getResources().getDisplayMetrics().density;
			final GradientDrawable bg_inactive = new GradientDrawable();
			bg_inactive.setCornerRadius(density);
			bg_inactive.setColor(inactive);
			bg_inactive.setStroke((int) (5 * density), Color.TRANSPARENT);
			final GradientDrawable bg_active = new GradientDrawable();
			bg_active.setCornerRadius(density);
			bg_active.setColor(activebase);
			bg_active.setStroke((int) (5 * density), activestroke);
			final StateListDrawable states = new StateListDrawable();
			states.addState(new int[] { android.R.attr.state_selected }, bg_active);
			states.addState(new int[] { android.R.attr.state_enabled }, bg_inactive);
			
			if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
				mHandlebar.setBackgroundDrawable(states);
			else
				mHandlebar.setBackground(states);
		}
	}
	
	private RelativeLayout createPin()
	{
		final RelativeLayout pinLayout = new RelativeLayout(getContext());
		pinLayout.setVisibility(View.INVISIBLE);
		
		final Pin pin = new Pin(getContext());
		pin.setId(ID_PIN);
		final RelativeLayout.LayoutParams pinParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		pinParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		pinParams.addRule(RelativeLayout.ALIGN_BOTTOM, ID_PIN_TEXT);
		pinParams.addRule(RelativeLayout.ALIGN_TOP, ID_PIN_TEXT);
		pin.setLayoutParams(pinParams);
		pinLayout.addView(pin);
		
		final TextView indicatorTextView = new TextView(getContext());
		indicatorTextView.setId(ID_PIN_TEXT);
		final RelativeLayout.LayoutParams indicatorParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		indicatorParams.addRule(RelativeLayout.LEFT_OF, ID_PIN);
		indicatorTextView.setLayoutParams(indicatorParams);
		indicatorTextView.setTextColor(Color.WHITE);
		indicatorTextView.setGravity(Gravity.CENTER);
		indicatorTextView.setBackgroundColor(GREY_LIGHT);
		pinLayout.addView(indicatorTextView);
		
		return pinLayout;
	}
	
	public static class Pin extends View
	{
		
		private static final int mPinColor = Color.argb(224, 66, 66, 66);
		private Paint mPaint;
		private Path mPath;
		
		public Pin(Context context)
		{
			super(context);
			init();
		}
		
		public Pin(Context context, AttributeSet attrs)
		{
			super(context, attrs);
			init();
		}
		
		public Pin(Context context, AttributeSet attrs, int defStyle)
		{
			super(context, attrs, defStyle);
			init();
		}
		
		public void setColor(int color)
		{
			mPaint.setColor(color);
		}
		
		private void init()
		{
			mPath = new Path();
			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setStyle(Paint.Style.FILL);
			setColor(mPinColor);
		}
		
		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom)
		{
			if (changed)
			{
				mPath.reset();
				mPath.moveTo(0, getHeight());
				mPath.lineTo(getWidth(), getHeight() / 2);
				mPath.lineTo(0, 0);
				mPath.close();
			}
			super.onLayout(changed, left, top, right, bottom);
		}
		
		@Override
		protected void onDraw(Canvas canvas)
		{
			canvas.drawPath(mPath, mPaint);
			super.onDraw(canvas);
		}
		
	}

	
	public interface Scrollable
	{
		
		/**
		 * This function returns the corresponding String to display at any given position
		 * <p>
		 * 
		 * @param childposition
		 *            equals childposition if used with ExpandableListView, position otherwise.
		 * @param groupposition
		 *            equals groupposition if used with ExpandableListView, zero otherwise.
		 */
		String getIndicatorForPosition(final int childposition, final int groupposition);
		
		/**
		 * This second function is responsible for is for implementing scroll behaviour. This can be used to perform special tasks, e.g. if you want to snap to the first item starting with a letter in an
		 * alphabetically ordered list or jump between groups in an ExpandableListView. If you want the normal approach, simply return childposition.
		 * <p>
		 * 
		 * @param childposition
		 *            equals childposition if used with ExpandableListView, position otherwise.
		 * @param groupposition
		 *            equals groupposition if used with ExpandableListView, zero otherwise.
		 */
		int getScrollPosition(final int childposition, final int groupposition);
		
	}
}
