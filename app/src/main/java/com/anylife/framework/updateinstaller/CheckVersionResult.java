package com.anylife.framework.updateinstaller;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * 根据自己的业务替换
 *
 */
public class CheckVersionResult implements Parcelable {

	private String packageUrl;
	private int versionCode;
	private String versionName;
	private String created;
	private String description;
	private int updateType;  //1表示强制更新

	public CheckVersionResult(String packageUrl, int versionCode, String versionName, String created, String description, int updateType) {
		this.packageUrl = packageUrl;
		this.versionCode = versionCode;
		this.versionName = versionName;
		this.created = created;
		this.description = description;
		this.updateType = updateType;
	}

	protected CheckVersionResult(Parcel in) {
		packageUrl = in.readString();
		versionCode = in.readInt();
		versionName = in.readString();
		created = in.readString();
		description = in.readString();
		updateType = in.readInt();
	}

	public static final Creator<CheckVersionResult> CREATOR = new Creator<CheckVersionResult>() {
		@Override
		public CheckVersionResult createFromParcel(Parcel in) {
			return new CheckVersionResult(in);
		}

		@Override
		public CheckVersionResult[] newArray(int size) {
			return new CheckVersionResult[size];
		}
	};

	public String getPackageUrl() {
		return packageUrl;
	}

	public void setPackageUrl(String packageUrl) {
		this.packageUrl = packageUrl;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getUpdateType() {
		return updateType;
	}

	public void setUpdateType(int updateType) {
		this.updateType = updateType;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(packageUrl);
		dest.writeInt(versionCode);
		dest.writeString(versionName);
		dest.writeString(created);
		dest.writeString(description);
		dest.writeInt(updateType);
	}
}
