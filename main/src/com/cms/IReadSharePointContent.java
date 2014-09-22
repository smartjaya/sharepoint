package com.cms;

import java.util.Map;

/**
 * @author Jaya
 *
 */
public interface IReadSharePointContent {
	
	 void loadContent();
	 String getContentOnId(String contentId);
	 Map<String, String> getSharePointContents();
	 void setSharePointContents(Map<String, String> sharePointContents);
	 void getItem(String docURL);
}
