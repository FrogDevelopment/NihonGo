package fr.frogdevelopment.nihongo.kana;

import android.os.Parcel;
import android.os.Parcelable;

class Kana implements Parcelable {
	int    resource;
	String label;
	String name;

	public Kana(Hiragana hiragana) {
		resource = hiragana.resource;
		label = hiragana.label;
		name = hiragana.name();
	}

	public Kana(Katakana katakana) {
		resource = katakana.resource;
		label = katakana.label;
		name = katakana.name();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(resource);
		out.writeString(label);
		out.writeString(name);
	}

	public static final Parcelable.Creator<Kana> CREATOR = new Parcelable.Creator<Kana>() {
		public Kana createFromParcel(Parcel in) {
			return new Kana(in);
		}

		public Kana[] newArray(int size) {
			return new Kana[size];
		}
	};

	private Kana(Parcel in) {
		resource = in.readInt();
		label = in.readString();
		name = in.readString();
	}
}
