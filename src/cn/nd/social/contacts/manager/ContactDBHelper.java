package cn.nd.social.contacts.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

public class ContactDBHelper  {
	
	private static ContactDBHelper singleInstance;
	private List<MemberContact> mAllContacts = new ArrayList<MemberContact>(); 
	private Map<Long,MemberContact> mContactsMap = new HashMap<Long, MemberContact>();
	
	private ContactDBHelper(){
		super();
	}
	
	public static synchronized ContactDBHelper getInstance(){
		if(singleInstance == null){
			singleInstance = new ContactDBHelper();
		}
		return singleInstance;
	}
	
	public void save(MemberContact contact){
		ActiveAndroid.beginTransaction();
		try{
			contact.save();
			ActiveAndroid.setTransactionSuccessful();
		}finally {
			ActiveAndroid.endTransaction();
		}
		reBuildMap();
	}
	
	public void save(List<MemberContact> contacts){
		
		ActiveAndroid.beginTransaction();
		try {
			new Delete().from(MemberContact.class).execute();
	        for (MemberContact contact : contacts) {
//		        	From from = new Select()
//	                .from(MemberContact.class)
//	                .where("phoneNumber = ?", contact.getPhoneNumber());
//		        	MemberContact temp = (MemberContact)from.executeSingle();
//		        	if(temp != null){
//		        		temp.delete();
//		        	}
	        	contact.save();
			}
	        ActiveAndroid.setTransactionSuccessful();
		}
		finally {
			ActiveAndroid.endTransaction();
		}
		mAllContacts = getContacts();
		reBuildMap();
	}
	
	public List<MemberContact> getContacts(){
		return new Select().from(MemberContact.class).orderBy("friendFlag").execute();
	}
	
	private void reBuildMap(){
		mContactsMap.clear();
		for(MemberContact contact : mAllContacts){
			mContactsMap.put(contact.getContactid(), contact);
		}
	}
	
	public MemberContact getContactByContactid(Long contactid){
		if(mAllContacts.size() == 0){
			mAllContacts = getContacts();
			reBuildMap();
		}
		return mContactsMap.get(contactid);
	}
	
	/**
	 * 判断通讯录
	 * @param contactid
	 * @return
	 */
	public int getContactState(Long contactid){
		MemberContact contact = getContactByContactid(contactid);
		if(contact != null){
			if(contact.isFriend()){
				return 2;
			}
			return 1;
		}
		return 0;
	}
	
}
