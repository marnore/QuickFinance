package lt.marius.converter;

import android.graphics.Bitmap;

public class Option {
	
	private String title;
	private Bitmap image;
	private int id;
	private Object tag;

	public Option(String title, Bitmap image, int id, Object tag) {
		this.title = title;
		this.image = image;
		this.id = id;
		this.tag = tag;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Object getTag() {
		return tag;
	}

	public void setTag(Object tag) {
		this.tag = tag;
	}

		
		
}
