package com.seakun.photopicker.bean;

import java.util.ArrayList;
import java.util.List;

public class ImageFolder {
	public static final String FOLDER_ALL = ".";
	/**
	 * folder path
	 */
	private String dir;

	/**
	 * folder name
	 */
	private String name;

	/**
     * all photo paths below the folder
     */
	private List<String> allPicPath = new ArrayList<>();

	public String getDir()
	{
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
		if(!FOLDER_ALL.equals(dir)){
			int lastIndexOf = this.dir.lastIndexOf("/");
			this.name = this.dir.substring(lastIndexOf);
		}
	}

	public String getName() {
		return name;
	}
	public int getCount() {
		return allPicPath.size();
	}

	public List<String> getAllPicPath() {
		return allPicPath;
	}

	public void setAllPicPath(List<String> allPicPath) {
		this.allPicPath = allPicPath;
	}

	@Override
	public boolean equals(Object o) {
		try {
			ImageFolder other = (ImageFolder) o;
			return this.dir.equalsIgnoreCase(other.dir);
		}catch (ClassCastException e){
			e.printStackTrace();
		}
		return super.equals(o);
	}
}
