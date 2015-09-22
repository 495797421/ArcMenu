package com.lisao.arcmenu.view;

import com.lisao.arcmenu.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class ArcMenu extends ViewGroup implements View.OnClickListener {

	// 中心按钮的四个位置
	private static final int POS_LEFT_TOP = 0;
	private static final int POS_LEFT_BOTTOM = 1;
	private static final int POS_RIIGHT_TOP = 2;
	private static final int POS_RIGHT_BOTTOM = 3;
	// 默认位置为左下角
	private Position mPosition = Position.LEFT_BOTTOM;
	// 菜单展开半径
	private int mRadius;
	// 菜单的状态
	private Status mCurrentStatus = Status.CLOSE;

	// 中心按钮
	private View mCButton;
	// Item的点击事件
	private OnMenuItemClickListenner mMenuItemClickListenner;

	public void setOnMenuItemClickListenner(
			OnMenuItemClickListenner mMenuItemClickListenner) {
		this.mMenuItemClickListenner = mMenuItemClickListenner;
	}

	public interface OnMenuItemClickListenner {
		void OnClick(View view, int pos);
	}

	// 枚举类型，菜单的打开状态
	public enum Status {
		OPEN, CLOSE
	}

	/**
	 * 枚举类型的按钮位置
	 * 
	 * @author lisao
	 */
	public enum Position {
		LEFT_TOP, LEFT_BOTTOM, RIGHT_TOP, RIGHT_BOTTOM
	}

	// 构造方法
	public ArcMenu(Context context) {
		this(context, null);
	}

	public ArcMenu(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ArcMenu(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				100, getResources().getDisplayMetrics());
		// 获取自定义属性的值
		TypedArray array = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.ArcMenu, defStyleAttr, 0);
		// 获取属性名为position的值
		int pos = array.getInt(R.styleable.ArcMenu_position, POS_LEFT_BOTTOM);
		switch (pos) {
		case POS_LEFT_TOP:
			mPosition = Position.LEFT_TOP;
			break;

		case POS_LEFT_BOTTOM:
			mPosition = Position.LEFT_BOTTOM;
			break;

		case POS_RIIGHT_TOP:
			mPosition = Position.RIGHT_TOP;
			break;

		case POS_RIGHT_BOTTOM:
			mPosition = Position.RIGHT_BOTTOM;
			break;
		}
		mRadius = (int) array.getDimension(R.styleable.ArcMenu_radius, mRadius);
		// 回收
		array.recycle();
	}

	// 1.测量——onMeasure()：决定View的大小
	// 2.布局——onLayout()：决定View在ViewGroup中的位置
	// 3.绘制——onDraw()：如何绘制这个View。
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			// 测量child
			measureChild(getChildAt(i), widthMeasureSpec, heightMeasureSpec);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed) {
			layoutCButton();
			int count = getChildCount();
			for (int i = 0; i < count - 1; i++) {
				View child = getChildAt(i + 1);
				// 设置子按钮为不可见状态
				child.setVisibility(View.GONE);//
				int cl = (int) (mRadius * Math.sin(Math.PI / 2 / (count - 2)
						* i));// x
				int ct = (int) (mRadius * Math.cos(Math.PI / 2 / (count - 2)
						* i));// y

				int cWidth = child.getMeasuredWidth();// 获取子控件的宽度
				int cHight = child.getMeasuredHeight();// 获取子控件的高度
				// 左下，右下
				if (mPosition == Position.LEFT_BOTTOM
						|| mPosition == Position.RIGHT_BOTTOM) {
					ct = getMeasuredHeight() - cHight - ct;
				}
				// 右上，右下
				if (mPosition == Position.RIGHT_TOP
						|| mPosition == Position.RIGHT_BOTTOM) {
					cl = getMeasuredWidth() - cWidth - cl;
				}
				child.layout(cl, ct, cl + cWidth, ct + cHight);
			}
		}
	}

	private void layoutCButton() {
		mCButton = getChildAt(0);// 获得中心按钮
		mCButton.setOnClickListener(this);// 设置中心按钮的点击事件
		int l = 0;// X轴的坐标
		int t = 0;// Y轴的坐标
		int width = mCButton.getMeasuredWidth();// 获得按钮的宽度
		int height = mCButton.getMeasuredHeight();// 获得按钮的高度
		// 判断按钮的位置。并设置按钮的显示位置
		switch (mPosition) {
		case LEFT_TOP:
			l = 0;
			t = 0;
			break;
		case LEFT_BOTTOM:
			l = 0;
			t = getMeasuredHeight() - height;
			break;
		case RIGHT_TOP:
			l = getMeasuredWidth() - width;
			t = 0;
			break;
		case RIGHT_BOTTOM:
			l = getMeasuredWidth() - width;
			t = getMeasuredHeight() - height;
			break;
		}
		mCButton.layout(l, t, l + width, t + height);
	}

	@Override
	public void onClick(View v) {
		mCButton = findViewById(R.id.id_button);
		// 设置中心按钮的旋转动画
		rotateButton(v, 0f, 360f, 300);
		// 设置Item展开动画
		toggleMenu(300);

	}

	// 切换菜单
	public void toggleMenu(int duration) {
		// 为menuItem添加位移动画和旋转动画
		int count = getChildCount();// 获取ViewGroup中的所有View
		// 第一个为中心按钮
		for (int i = 0; i < count - 1; i++) {
			final View child = getChildAt(i + 1);
			child.setVisibility(View.VISIBLE);
			int cl = (int) (mRadius * Math.sin(Math.PI / 2 / (count - 2) * i));
			int ct = (int) (mRadius * Math.cos(Math.PI / 2 / (count - 2) * i));
			int xflag = 1;
			int yflag = 1;
			// 左上，左下，X坐标要减小
			if (mPosition == Position.LEFT_TOP
					|| mPosition == Position.LEFT_BOTTOM) {
				xflag = -1;
			}
			// 左上，右上，Y轴坐标要减小
			if (mPosition == Position.LEFT_TOP
					|| mPosition == Position.RIGHT_TOP) {
				yflag = -1;
			}
			AnimationSet animSet = new AnimationSet(true);
			// 设置位移动画
			Animation tranAnim = null;
			if (mCurrentStatus == Status.CLOSE) {
				tranAnim = new TranslateAnimation(xflag * cl, 0, yflag * ct, 0);
				child.setClickable(true);
				child.setFocusable(true);
			} else {
				tranAnim = new TranslateAnimation(0, xflag * cl, 0, yflag * ct);
				child.setClickable(false);
				child.setFocusable(false);
			}
			// tranAnim.setFillAfter(true);
			// tranAnim.setDuration(duration);
			tranAnim.setStartOffset((i * 30));
			tranAnim.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					if (mCurrentStatus == Status.CLOSE) {
						child.setVisibility(View.GONE);
					}

				}
			});
			// 旋转动画
			RotateAnimation rotaAnim = new RotateAnimation(0, 720,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			rotaAnim.setStartOffset((i * 30));
			// rotaAnim.setDuration(duration);
			// rotaAnim.setFillAfter(true);
			animSet.addAnimation(rotaAnim);
			animSet.addAnimation(tranAnim);
			animSet.setDuration(duration);
			// animSet.setFillAfter(true);
			child.startAnimation(animSet);
			final int pos = i;
			child.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mMenuItemClickListenner != null) {
						mMenuItemClickListenner.OnClick(v, pos);
					}
					// Item点击动画
					meunItemAnim(pos);
					// 改变状态
					changeStatus();
				}
			});
		}
		changeStatus();
	}

	private void meunItemAnim(int pos) {
		for (int i = 0; i < getChildCount() - 1; i++) {
			View child = getChildAt(i + 1);
			if (i == pos) {
				child.startAnimation(scaleBigAnim(300));
			} else {
				child.startAnimation(scaleSmallAnim(300));
			}
		}

	}

	// 旋转缩小动画
	private Animation scaleSmallAnim(int duration) {
		AnimationSet set = new AnimationSet(true);
		ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.0f, 1.0f,
				0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
		set.addAnimation(alphaAnimation);
		set.addAnimation(scaleAnimation);
		set.setDuration(duration);
		set.setFillAfter(true);
		return set;
	}

	// 旋转放大动画
	private Animation scaleBigAnim(int duration) {
		AnimationSet set = new AnimationSet(true);
		ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 4.0f, 1.0f,
				4.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);
		AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
		set.addAnimation(alphaAnimation);
		set.addAnimation(scaleAnimation);
		set.setDuration(duration);
		set.setFillAfter(true);
		return set;
	}

	private void changeStatus() {
		// 当前如果状态是关闭的，则打开
		mCurrentStatus = (mCurrentStatus == Status.CLOSE ? Status.OPEN
				: Status.CLOSE);

	}

	/**
	 * 中心按钮的旋转动画
	 * 
	 * @param v
	 *            中心按钮
	 * @param start
	 *            开始角度
	 * @param end
	 *            结束角度
	 * @param duration
	 *            持续时间
	 */
	private void rotateButton(View v, float start, float end, int duration) {
		//
		RotateAnimation anim = new RotateAnimation(start, end,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		anim.setDuration(duration);
		// 设置为true,动画 结束后不会回到原来的位置
		anim.setFillAfter(true);
		v.startAnimation(anim);
	}

}
