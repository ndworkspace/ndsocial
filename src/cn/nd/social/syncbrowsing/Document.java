package cn.nd.social.syncbrowsing;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import cn.nd.social.privategallery.imageviewer.ImageViewTouchBase;
import cn.nd.social.syncbrowsing.codec.LoadPageCallback;
import cn.nd.social.syncbrowsing.codec.pdf.PdfContext;
import cn.nd.social.syncbrowsing.ui.ClientSyncReadView;
import cn.nd.social.util.Utils;

public class Document {
	private boolean server = false;
	private Source source;
	private View documentView = null;
	private final SparseArray<Page> pages = new SparseArray<Page>();
	private int cacheCount = 0;
	private static final int CACHE_MAX = 10;
	private int currentPage = 0;
	private int setPage = 0;
	private final TextPaint textPaint = textPaint();
	private final Paint fillPaint = fillPaint();

	public static Document openLocalPDF(String fileName, View targetView) {
		int targetWidth = targetView.getWidth();
		int targetHeight = targetView.getHeight();
		LocalSource source = new LocalSource(new PdfContext(), targetWidth,
				targetHeight);
		source.open(fileName);
		Document doc = new Document(targetView, source);
		doc.server = true;
		doc.init();
		return doc;
	}

	public static Document createNetwork(int pageCount, View targetView,
			ClientSyncReadView.NetworkPageLoader loader) {
		NetworkSource source = new NetworkSource(pageCount, loader);
		Document doc = new Document(targetView, source);
		doc.server = false;
		doc.init();
		doc.setClientPageLoaderCbk(loader);
		return doc;
	}
	
	private void setClientPageLoaderCbk(ClientSyncReadView.NetworkPageLoader loader) {
		loader.setCallBack(new LoadPageCallbackImpl());
	}

	public Document(View targetView, Source source) {
		documentView = targetView;
		this.source = source;
	}

	public void draw(Canvas canvas) {
		Rect rect = new Rect(0, 0, documentView.getWidth(),
				documentView.getHeight());
		canvas.drawRect(rect, fillPaint);
		canvas.drawText("Page " + (currentPage + 1), rect.centerX(),
				rect.centerY(), textPaint);
		if (pages.indexOfKey(currentPage) >= 0) {
			Page page = pages.get(currentPage);
			page.draw(canvas, rect);
		}
	}

	public int getPageCount() {
		return source.getPageCount();
	}

	public int getCurrentPage() {
		return setPage;// currentPage;
	}

	public void setCurrentPage(int current) {
		if (current >= 0 && current < getPageCount()) {
			int lastSetPage = setPage;
			setPage = current;

			// check load
			if (!loadPage(current)) {
				currentPage = current;
				if (server) {
					if (lastSetPage < setPage && setPage < (getPageCount() - 1)) {
						loadPage(setPage + 1);
					} else if (lastSetPage > setPage && setPage > 0) {
						loadPage(setPage - 1);
					}
				}
				
				
				((ImageView) documentView).setImageBitmap(pages.get(current)
						.getBitmap());				
				
				if(mLoadListener != null) {
					mLoadListener.onPageLoaded(current);
				}
			}
		}
	}

	public Bitmap getPage(int index) {
		if (pages.indexOfKey(index) >= 0) {
			Page page = pages.get(index);
			Bitmap bitmap = page.getBitmap();
			return bitmap;
		}
		return null;
	}

	/**
	 * DocumentView and ClientDocView will invoke the method tangtaotao add
	 * */
	public void setCurrPageImage(Bitmap bmp) {
		pages.get(currentPage).setBitmap(bmp);
		((ImageViewTouchBase) documentView).setImageBitmapRetain(pages.get(
				currentPage).getBitmap());
	}

	public Bitmap getCurrent() {
		if (pages.indexOfKey(currentPage) >= 0) {
			Page page = pages.get(currentPage);
			Bitmap bitmap = page.getBitmap();
			return bitmap;
		}
		return null;
	}

	private void init() {
		for (int i = 0; i < getPageCount(); i++) {
			pages.put(i, new Page(i));
		}
	}

	private void updateView(Bitmap bitmap) {
		ImageView iv = (ImageView) documentView;

		// recycle old bitmap
		Drawable drawable = iv.getDrawable();
		if (drawable instanceof BitmapDrawable) {
			Bitmap old = ((BitmapDrawable) drawable).getBitmap();
			if (old != null && !old.isRecycled()) {
				old.recycle();
			}
		}

		// update doc view with new bitmap
		iv.setImageBitmap(bitmap);
	}

	private void onPageLoaded(Object object, final int pageNumber,
			final Bitmap bitmap) {
		if (object == null) {// need to retreive the page Object,indicating that
								// this is not current page
			Page page = pages.get(pageNumber);
			if (page != null) {
				page.setBitmap(bitmap);
				page.setLoading(false);
				cacheCount += 1;
			}
			if(setPage == pageNumber) {
				currentPage = setPage;
				updateView(bitmap);
			}
			
		} else {
			Page page = (Page) object;
			page.setBitmap(bitmap);
			page.setLoading(false);
			if (setPage == pageNumber) {
				if(server){
					if (currentPage < setPage && setPage < (getPageCount() - 1)) {
						loadPage(setPage + 1);
					} else if (currentPage > setPage && setPage > 0) {
						loadPage(setPage - 1);
					}
				}
				
				currentPage = setPage;
				updateView(bitmap);
			}
			
			if (pageNumber == currentPage && server) {

			} else {

			}

			cacheCount += 1;
		}
		
		
		if (cacheCount > CACHE_MAX) {
			int startPage = Math.max(0, getCurrentPage() - CACHE_MAX / 2);
			int endPage = Math.min(getPageCount() - 1, startPage + CACHE_MAX
					/ 2);
			for (int i = 0; i < getPageCount(); ++i) {
				if (i < startPage || i > endPage) {
					Page t = pages.get(i);
					if (t.getBitmap() == null) {
						continue;
					}
					t.setBitmap(null);
					cacheCount -= 1;
				}
				if (cacheCount <= CACHE_MAX)
					break;
			}
		}
		
		if(mLoadListener != null) {
			mLoadListener.onPageLoaded(pageNumber);
		}
	}

	private class LoadPageCallbackImpl implements LoadPageCallback {
		@Override
		public void onLoadComplete(final Object objectKey,
				final int pageNumber, final Bitmap bitmap) {
			documentView.post(new Runnable() {
				public void run() {
					onPageLoaded(objectKey, pageNumber, bitmap);
				}
			});
		}
	}

	private Boolean loadPage(int pageNumber) {
		Page page = pages.get(pageNumber);
		// check load
		if (page.getBitmap() == null) {
			if(server) {
				if (!page.isLoading()) {
					page.setLoading(true);
					source.loadPage(page, page.getIndex(),
							new LoadPageCallbackImpl());
				}
			}
			return true;
		}
		return false;
	}

	private Paint fillPaint() {
		final Paint fillPaint = new Paint();
		fillPaint.setColor(Color.BLACK);
		fillPaint.setStyle(Paint.Style.FILL);
		return fillPaint;
	}

	private TextPaint textPaint() {
		final TextPaint paint = new TextPaint();
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setTextSize(24);
		paint.setTextAlign(Paint.Align.CENTER);
		return paint;
	}

	public static byte[] compressImage(Bitmap image) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 80, baos);
		return baos.toByteArray();
	}

	public static Bitmap decompressImage(byte[] bs, int offset, int length) {
		Bitmap bmp = null;
		try {
			bmp = BitmapFactory.decodeByteArray(bs, offset, length);
		} catch (Exception e) {
			Log.e("Document", "decompressImage error", e);
		} catch (OutOfMemoryError e) {
			Toast.makeText(Utils.getAppContext(), "out of memory",
					Toast.LENGTH_SHORT).show();
			System.gc();// try to recycle some memory
		}
		return bmp;
	}

	public void cleanup() {
		for (int i = 0; i < getPageCount(); ++i) {
			Page t = pages.get(i);
			t.setBitmap(null);
			t.setLoading(false);
		}
	}
	
	public interface PageLoadListener {
		void onPageLoaded(int page);
	}
	
	private PageLoadListener mLoadListener = null;
	
	public void setPageLoadListener(PageLoadListener listener) {
		mLoadListener = listener;
	}
}
