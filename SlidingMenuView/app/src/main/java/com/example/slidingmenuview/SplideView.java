package com.example.slidingmenuview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by Administrator on 2015/10/10.
 */
public class SplideView extends ViewGroup {

    private static final String TAG = "SplideView";
    private  View mainContent;
    private  View leftMenu;
    private int leftMenuWidth;
    private float mDownX;
    private Scroller mScroller;
    private boolean isPageOn;

    public SplideView(Context context) {
        this(context, null);
    }

    public SplideView(Context context, AttributeSet attrs) {

        super(context, attrs);

        mScroller = new Scroller(context);

    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //布局解析完成的时候回调
        //完成后获取自定义容器中的的子控件

        leftMenu = getChildAt(1);
        mainContent = getChildAt(0);
//        Log.d(TAG,leftMenu+"=="+mainContent);
        LayoutParams params = leftMenu.getLayoutParams();
        leftMenuWidth = params.width;
    }

    //打开关闭侧拉菜单的方法
    public void setToggle() {
        isPageOn = !isPageOn;
        animationScroll();
    }

    public void animationScroll(){
        int startX = getScrollX();
        int endX = 0;
        if (isPageOn) {
            //打开侧拉页面
            endX = -leftMenuWidth;
        } else {
            endX = 0;
        }

        int dex = endX - startX;

        int duration = Math.abs(dex) * 5;
        if(duration > 500) {
            duration = 500;
        }

        //开始移动屏幕的方法  只需要起始位置和移动的距离
        mScroller.startScroll(startX, 0, dex, 0, duration);
        invalidate();
    }


    /**
     * 计算位置
     */
    @Override
    public void computeScroll() {
        if(mScroller.computeScrollOffset()){
            //获取当前scroll位置
            int currX = mScroller.getCurrX();
            //让屏幕一点一点的移动过去
            scrollTo(currX, 0);
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //测量主内容 带模式的测量
        mainContent.measure(widthMeasureSpec, heightMeasureSpec);
        int leftWidth = MeasureSpec.makeMeasureSpec(leftMenuWidth,MeasureSpec.EXACTLY);
        //测量侧拉页面 不带模式测量
        leftMenu.measure(leftWidth, heightMeasureSpec);
        //两个参数都是不带模式的
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int dex = 0;
        //安排两个子view的位置
        //maincontent的位置
        int mainContentLeft = 0 + dex;
        int mainContentTop = 0;
        int mainContentRight = mainContent.getMeasuredWidth() + dex;
        int mainContentBottom = mainContent.getMeasuredHeight();
        mainContent.layout(mainContentLeft, mainContentTop, mainContentRight, mainContentBottom);
        //leftMenu的位置

        int leftMenuLeft = -leftMenuWidth +dex;
        int leftMenuTop = 0;
        int leftMenuRight = 0 + dex;
        int leftMenuButton = leftMenu.getMeasuredHeight();
        leftMenu.layout(leftMenuLeft, leftMenuTop, leftMenuRight, leftMenuButton);

    }

    float downX = 0;
    float downY = 0;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {


        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //记录按下时候的X Y坐标
                downX = ev.getX();
                downY = ev.getY();
                mDownX = downX;  //这里为什么要赋值
                break;
            case MotionEvent.ACTION_MOVE:
                //记录下移动的时候X Y的坐标
                float moveX = ev.getX();
                float moveY = ev.getY();

                //计算 X Y轴移动的距离
                float deX = moveX - downX;
                float deY = moveY - downY;

                if(Math.abs(deX) > Math.abs(deY)){
                    //X轴向移动的距离大于Y轴的距离
                    // 那就是横向移动 拦截掉事件的分发，让本类自己去处理这个事件，也就是侧拉菜单
                    return true;
                }
                    //否者还是让事件分发下去，让子view去执行响应的处理
            break;

        }
        return super.onInterceptTouchEvent(ev);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:  //按下
                //记录按下的位置
                mDownX = event.getX();

                break;
            case MotionEvent.ACTION_MOVE: //移动
                //记录下移动的位置
                float moveX = event.getX();
                //计算出移动的间距（因为移动的是屏幕，所以计算间距可以直接按下的位置-移动的位置）
                int dex = Math.round(mDownX - moveX);

                //获取屏幕当前的位置
                int screenX = getScrollX();
                //移动屏幕的两个方法 scrollBy() 相对位置 scrollTo();绝对位置

                scrollBy(dex, 0);

                //防止出边界
                if (screenX + dex < -leftMenu.getMeasuredWidth()) {
                    //那就移动到刚好leftMenu出现完的位置
                    scrollTo(-leftMenu.getMeasuredWidth(),0);
                }else if (screenX + dex > 0) {
                    scrollTo(0, 0);
                }

                mDownX = moveX;
                break;
            case MotionEvent.ACTION_UP: //弹起
            //松开的时候获取此刻屏幕的位置
                int scrX = getScrollX();
                if(scrX > -leftMenu.getMeasuredWidth() / 2) {
//                    scrollTo(0, 0);
                    //关闭侧拉页面
                    isPageOn = false;
                }else if (scrX < -leftMenu.getMeasuredWidth() / 2) {
//                    scrollTo(-leftMenuWidth, 0);
                    //阿打开侧拉页面
                    isPageOn = true;
                }
                //松开的时候就根据记录打开和关闭 去执行屏幕的移动
            animationScroll();
                break;

        }

        //返回true自己消费不必想上回复
        return true;
    }
}
