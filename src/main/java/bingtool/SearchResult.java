package bingtool;

import java.io.Serializable;
import java.util.ArrayList;

@SuppressWarnings("serial")
public class SearchResult implements Serializable {
	
	private int port = -1;
	private String url = null;
	private String sub = null;
	private String protocol = null;
	private ArrayList<Integer> pages = null;
	
	public SearchResult(String url) {
		this.url = url;
		
		String portInPage = a.site + ":";
		int portIndex = url.indexOf(portInPage);
		if(portIndex != -1) {
			String cutted = url.substring(portIndex + portInPage.length());
			port = Integer.parseInt(cutted.substring(0, cutted.indexOf("/")));
		}
		
		String protocolStart = "://";
		int protocolRawIndex = url.indexOf(protocolStart);
		int protocolIndex = protocolRawIndex+protocolStart.length();
		protocol = url.substring(0, protocolRawIndex);
		
		if(!url.contains(protocolStart + a.site)) {
			sub = url.substring(protocolIndex, url.indexOf("." + a.site));
		} else {
			sub = "";
		}
		
		pages = new ArrayList<>();
	}
	
	public void onPage(int pageNum) {
		pages.add(pageNum);
	}

	public ArrayList<Integer> getPages() {
		return pages;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getUrl() {
		return url;
	}

	public String getSub() {
		return sub;
	}
	
	public String getProtocol() {
		return protocol;
	}
	
	public boolean hasParameter() {
		return url.contains("?");
	}
	
}
