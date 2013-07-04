package cn.sinonet.common.beans;

import java.util.ArrayList;
import java.util.List;

public class Folder {

	private String id;
	private String text;
	private boolean leaf;
	private String cls;
	private String relativePath;
	private List<Folder> children = new ArrayList<Folder>();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public boolean isLeaf() {
		return leaf;
	}
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}
	public String getCls() {
		return cls;
	}
	public void setCls(String cls) {
		this.cls = cls;
	}	
	public String getRelativePath() {
		return relativePath;
	}
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
	public List<Folder> getChildren() {
		return children;
	}
	public void setChildren(List<Folder> children) {
		this.children = children;
	}    
	public void addChild(Folder file){
		this.children.add(file);
	}
	
}
