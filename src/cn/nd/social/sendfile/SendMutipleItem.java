package cn.nd.social.sendfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.view.View;
import cn.nd.social.prishare.component.UserHead;
import cn.nd.social.prishare.items.CellItemBase;
import cn.nd.social.prishare.items.FilesCellItem;

public class SendMutipleItem {

	private ArrayList<String> mConnectedUser;
	private Map<CellItemBase, View> mGalaxyMap;
	private HashMap<String, View> mFileMap;
	private int flag = 0;
	
	private CellItemBase[] mItemArr;
	private int mItemIdx = 0;

	
	
	private SendMultiItemHandle mSendMulListener;
	private SendSingleItemHandle mSendSigListener;
	private CbkBeforeSend mCbkBeforeSend;
	private boolean mSendPending = false;
	
	private String mUserToSend;
	
	public SendMutipleItem(int s,ArrayList<String> usr,int f) {
		mConnectedUser = usr;
		flag = f;
	}
	
	public void setSendMulListener(SendMultiItemHandle s){
		mSendMulListener = s;
	}
	public void setSendSigListener(SendSingleItemHandle ss){
		mSendSigListener = ss;
	}
	
	public void setBeforeSendCbk(CbkBeforeSend cb) {
		mCbkBeforeSend = cb;
	}
	
	public boolean getSendPending(){
		return mSendPending;
	}
	public void setMultiMap(Map<CellItemBase, View> mul){
		mGalaxyMap = mul;
	}
	
	public void setFileList(HashMap<String, View> file){
		mFileMap = file;
	}
	
	public boolean sendMutiItems(){
		if(mConnectedUser.size() > 0) {
			if (flag == 0) {
				if(mCbkBeforeSend == null || mCbkBeforeSend.beforeSendItem()) {
					forwardSendItems();
				}
			}else{
				mSendMulListener.sendItemSkipActivity();
			}
		} else {
			//TODO: show waiting
			mSendMulListener.showSendWaiting();
			mSendPending = true;
		}
		
		return mSendPending;
	}
	
	public void forwardSendItems() {
		sendMultipleItem(mConnectedUser.get(0));
	}
	
	public void forwardSendItems(ArrayList<String> usr) {
		mConnectedUser = usr;
		sendMultipleItem(mConnectedUser.get(0));
	}
	
	
	private void sendMultipleItem(String user) {
		String pathStr = "";
		Set<CellItemBase> set = (Set<CellItemBase>) mGalaxyMap.keySet();
		Set<String> fileSet = (Set<String>) mFileMap.keySet();
		
		mItemArr = new CellItemBase[set.size()+mFileMap.size()];
		mItemIdx = 0;
		int i = 0;
		for (CellItemBase item : set) {
			mItemArr[i] = item;
			i++;
		}
		
		for (String item : fileSet) {
			mItemArr[i] = FilesCellItem.getFilesCellItem(item);
			i++;
		}

		sendRemainingFiles(user);
	}
	
	public void sendRemainingFiles(String userName) {
		if (mItemArr != null && mItemArr.length > mItemIdx) {
			mSendSigListener.sendSingleItem(userName, mItemArr[mItemIdx]);
			mItemIdx++;
		} else {
			mItemArr = null;
			mItemIdx = 0;
			//after finish sending,we should clear the items selected before
			mSendMulListener.clearSelectState(); 
		}
	}
	
	public interface SendMultiItemHandle{
		void clearSelectState(); 
		void showSendWaiting();		
		void sendItemSkipActivity();
	}
	
	public interface SendSingleItemHandle{
		void sendSingleItem(UserHead head, View v);
		void sendSingleItem(String userName, CellItemBase item);
		void sendSingleItem(final String userName, final String filePath,
				final String appName, int type);
	}

	
	public interface CbkBeforeSend {
		boolean beforeSendItem();
	}

}
