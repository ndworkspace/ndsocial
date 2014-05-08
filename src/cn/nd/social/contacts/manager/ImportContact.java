package cn.nd.social.contacts.manager;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import cn.nd.social.card.CardUtil;
import cn.nd.social.card.CardUtil.CardData;
import cn.nd.social.util.Utils;

public class ImportContact {
	private ContentResolver getContentResolver() {
		return Utils.getAppContext().getContentResolver();
	}

	public interface ImportContactCallBack {
		/**
		 * return if the import process is cancelled
		 * true to cancel importing
		 * */
		boolean onImportOneContact(int totalCount,int index);
	}
	
	public void importAllContacts(ImportContactCallBack callback) {
		Cursor cursor = getContentResolver().query(
				ContactsContract.Contacts.CONTENT_URI,
				null,
				null,
				null,
				ContactsContract.Contacts.DISPLAY_NAME
						+ " COLLATE LOCALIZED ASC");

		CardData cData = new CardData();
		if (cursor != null) {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				if(callback != null) {
					boolean isCancel = callback.onImportOneContact(cursor.getCount(), cursor.getPosition());
					if(isCancel) {
						return;
					}
				}
				
				int idColumn = cursor
						.getColumnIndex(ContactsContract.Contacts._ID);

				int displayNameColumn = cursor
						.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);

				String contactId = cursor.getString(idColumn);
				
				cData.clean();
				cData.name = cursor.getString(displayNameColumn);
				cData.company = getCompany(contactId);
				cData.title = getTitle(contactId);
				cData.mobile = getMobile(contactId);
				cData.phone = getExtraPhone(contactId, cData.mobile);
				cData.email = getEmail(contactId);
				cData.addr = getAddress(contactId);
				CardUtil.storeCard(Utils.getAppContext(), cData,false);
			}
			cursor.close();
		}

	}
	
	private String getCompany(String contactId) {
		String company = "";
		Cursor organizations = getContentResolver().query(
				Data.CONTENT_URI,
				new String[] { Data._ID, Organization.COMPANY,Organization.TITLE },
				Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='"
						+ Organization.CONTENT_ITEM_TYPE + "'",
				new String[] { contactId }, null);
		if(organizations == null) {
			return company;
		}
		if (organizations.moveToFirst()) {
				company = organizations.getString(organizations
						.getColumnIndex(Organization.COMPANY));
		}
		organizations.close();
		return company;
	}
	
	private String getTitle(String contactId) {
		String title = "";
		Cursor organizations = getContentResolver().query(
				Data.CONTENT_URI,
				new String[] { Data._ID, Organization.COMPANY,Organization.TITLE },
				Data.CONTACT_ID + "=?" + " AND " + Data.MIMETYPE + "='"
						+ Organization.CONTENT_ITEM_TYPE + "'",
				new String[] { contactId }, null);
		if(organizations == null) {
			return title;
		}
		if (organizations.moveToFirst()) {
			title = organizations.getString(organizations.getColumnIndex(Organization.TITLE));
		}
		organizations.close();
		return title;
	}
	
	private String getAddress(String contactId) {
		String addr = "";
		Cursor address = getContentResolver()
				.query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
						null,
						Phone.CONTACT_ID + " = " + contactId, null, null);
		if(address == null) {
			return addr;
		}
		if (address.moveToFirst()) {
			addr = address.getString(address
								.getColumnIndex(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
		}
		address.close();
		return addr;		
	}
	
	private String getEmail(String contactId) {
		String emailValue = "";
		Cursor emails = getContentResolver().query(
				Email.CONTENT_URI,
				null,
				Phone.CONTACT_ID
						+ " = " + contactId, null, null);
		if(emails == null) {
			return emailValue;
		}
		if (emails.moveToFirst()) {
			emailValue = emails.getString(emails.getColumnIndex(Email.DATA));
		}
		emails.close();
		return emailValue;
	}
	
	private String getMobile(String contactId) {
		String number = "";
		Cursor phones = getContentResolver().query(
				Phone.CONTENT_URI,
				null,
				Phone.CONTACT_ID + " = " + contactId, null, null);
		if (phones == null) {
			return number;
		}
		if (phones.moveToFirst()) {
			String tmpNumber = null;
			do {
				String phoneNumber = phones
						.getString(phones.getColumnIndex(Phone.NUMBER));
				int phoneType = phones.getInt(phones.getColumnIndex(Phone.TYPE));
				if (phoneType == Phone.TYPE_MOBILE) {
					number = phoneNumber;
					break;
				}
				if(isMobileNumber(phoneNumber) && tmpNumber == null) {
					tmpNumber = phoneNumber;
				}
			} while (phones.moveToNext());
			
			if(number.equals("") && tmpNumber != null) {
				number = tmpNumber;
			}
		}
		phones.close();
		return number;
	}
	
	private String getExtraPhone(String contactId,String mobile) {
		String number = "";
		Cursor phones = getContentResolver().query(
				Phone.CONTENT_URI,
				null,
				Phone.CONTACT_ID + " = " + contactId, null, null);
		if (phones == null) {
			return number;
		}
		if(!mobile.equals("") && phones.getCount() < 2) {
			phones.close();
			return number;
		}
		if (phones.moveToFirst()) {
			do {
				String phoneNumber = phones
						.getString(phones.getColumnIndex(Phone.NUMBER));
				if(!mobile.equals(phoneNumber)) {
					number = phoneNumber;
					break;
				}
			} while (phones.moveToNext());
		}
		phones.close();
		return number;
	}
	
	private boolean isMobileNumber(String number) {
		if(number == null || number.equals("")) {
			return false;
		}
		if(number.startsWith("+861") || number.startsWith("861")
				|| (number.startsWith("1") && number.length() == 11)) {
			return true;
		}
		return false;
	}
}
