/***
Copyright (c) 2012 CommonsWare, LLC

Licensed under the Apache License, Version 2.0 (the "License"); you may
not use this file except in compliance with the License. You may obtain
a copy of the License at
  http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package cn.nd.social.updater;

import android.app.PendingIntent;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Implementation of ConfirmationStrategy that raises
 * a Notification to alert user that we are ready to
 * move to the next step.
 */
public class InternalConfirmationStrategy implements
    ConfirmationStrategy {

	/**
   * Public constructor
    */
  public InternalConfirmationStrategy() {
  }
  
  /**
   * Private constructor for use in Parcelable implementation
   * @param in Parcel to restore instance from
   */
  private InternalConfirmationStrategy(Parcel in) {
  }

  /* (non-Javadoc)
   * @see com.commonsware.cwac.updater.ConfirmationStrategy#confirm(android.content.Context, android.app.PendingIntent)
   */
  @Override
  public boolean confirm(Context ctxt, PendingIntent contentIntent) {   
    return(false);
  }

  /* (non-Javadoc)
   * @see android.os.Parcelable#describeContents()
   */
  @Override
  public int describeContents() {
    return(0);
  }

  /* (non-Javadoc)
   * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
   */
  @Override
  public void writeToParcel(Parcel dest, int flags) {
  }

  /**
   * Required to complete Parcelable interface. Creates
   * an NotificationConfirmationStrategy instance or array
   * upon demand.
   */
  public static final Parcelable.Creator<InternalConfirmationStrategy> CREATOR=
      new Parcelable.Creator<InternalConfirmationStrategy>() {
        public InternalConfirmationStrategy createFromParcel(Parcel in) {
          return(new InternalConfirmationStrategy(in));
        }

        public InternalConfirmationStrategy[] newArray(int size) {
          return(new InternalConfirmationStrategy[size]);
        }
      };
}
