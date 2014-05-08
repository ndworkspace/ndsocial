package cn.nd.social.ui.controls;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

/**
 * A {@link ViewPager} that allows pseudo-infinite paging with a wrap-around
 * effect. Should be used with an {@link InfinitePagerAdapter}.
 * 
 */
public class InfiniteViewPager extends AnimateViewPager {

    public InfiniteViewPager(Context context) {
        super(context);
    }

    public InfiniteViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        super.setAdapter(adapter);
        // offset first element so that we can scroll to the left
        setCurrentItem(0);
    }

    @Override
    public void setCurrentItem(int item) {
        // offset the current item to ensure there is space to scroll
        item = getOffsetAmount() + (item % getAdapter().getCount());
        super.setCurrentItem(item);

    }

    @Override
    public void setCurrentItem(int item,boolean smoothscroll) {
        // offset the current item to ensure there is space to scroll
        item = getOffsetAmount() + (item % getAdapter().getCount());
        super.setCurrentItem(item,smoothscroll);

    }
    
    private int getOffsetAmount() {
        if (getAdapter() instanceof InfinitePagerAdapter) {
            InfinitePagerAdapter infAdapter = (InfinitePagerAdapter) getAdapter();
            // allow for 10000 back cycles from the beginning
            // should be enough to create an illusion of infinity
            // warning: scrolling to very high values (1,000,000+) results in
            // strange drawing behaviour
            return infAdapter.getRealCount() * 100000;
        } else {
            return 0;
        }
    }

}