package jp.redmine.redmineclient.form.helper;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.redmine.redmineclient.activity.handler.WebviewActionInterface;
import jp.redmine.redmineclient.entity.TypeConverter;
import net.java.textilej.parser.MarkupParser;
import net.java.textilej.parser.builder.HtmlDocumentBuilder;
import net.java.textilej.parser.markup.textile.TextileDialect;

import org.apache.commons.lang3.RandomStringUtils;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.webkit.WebView;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebViewClient;

public class TextileHelper {
	protected WebView view;
	static public final String URL_PREFIX = "redmine://";
	private Pattern patternIntent = Pattern.compile(URL_PREFIX);
	private Pattern patternIssue = Pattern.compile("#(\\d+)([^;\\d]|$)");
	private Pattern patternInlineUrl = Pattern.compile(
			"\\b((" +
			//START inherits from http://www.din.or.jp/~ohzaki/perl.htm#URI
			"(?:https?|shttp)://(?:(?:[-_.!~*'()a-zA-Z0-9;:&=+$,]|%[0-9A-Fa-f" +
			"][0-9A-Fa-f])*@)?(?:(?:[a-zA-Z0-9](?:[-a-zA-Z0-9]*[a-zA-Z0-9])?\\.)" +
			"*[a-zA-Z](?:[-a-zA-Z0-9]*[a-zA-Z0-9])?\\.?|[0-9]+\\.[0-9]+\\.[0-9]+\\." +
			"[0-9]+)(?::[0-9]*)?(?:/(?:[-_.!~*'()a-zA-Z0-9:@&=+$,]|%[0-9A-Fa-f]" +
			"[0-9A-Fa-f])*(?:;(?:[-_.!~*'()a-zA-Z0-9:@&=+$,]|%[0-9A-Fa-f][0-9A-" +
			"Fa-f])*)*(?:/(?:[-_.!~*'()a-zA-Z0-9:@&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f" +
			"])*(?:;(?:[-_.!~*'()a-zA-Z0-9:@&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f])*)*)" +
			"*)?(?:\\?(?:[-_.!~*'()a-zA-Z0-9;/?:@&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f])" +
			"*)?(?:#(?:[-_.!~*'()a-zA-Z0-9;/?:@&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f])*" +
			")?" +
			")|(" +	 // ftp section
			"s?ftps?://(?:(?:[-_.!~*'()a-zA-Z0-9;&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f])*" +
			"(?::(?:[-_.!~*'()a-zA-Z0-9;&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f])*)?@)?(?" +
			":(?:[a-zA-Z0-9](?:[-a-zA-Z0-9]*[a-zA-Z0-9])?\\.)*[a-zA-Z](?:[-a-zA-" +
			"Z0-9]*[a-zA-Z0-9])?\\.?|[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)(?::[0-9]*)?" +
			"(?:/(?:[-_.!~*'()a-zA-Z0-9:@&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f])*(?:/(?" +
			":[-_.!~*'()a-zA-Z0-9:@&=+$,]|%[0-9A-Fa-f][0-9A-Fa-f])*)*(?:;type=[" +
			"AIDaid])?)?(?:\\?(?:[-_.!~*'()a-zA-Z0-9;/?:@&=+$,]|%[0-9A-Fa-f][0-9" +
			"A-Fa-f])*)?(?:#(?:[-_.!~*'()a-zA-Z0-9;/?:@&=+$,]|%[0-9A-Fa-f][0-9A" +
			"-Fa-f])*)?" +
			//END
			"))"
			);
	//private Pattern patternDocuments = Pattern.compile("document:\\d+");
	private WebviewActionInterface action;
	public TextileHelper(WebView web){
		view = web;
	}
	public void setAction(WebviewActionInterface act){
		action = act;
	}
	public void setup(){
		setupWebView();
		setupHandler();
	}

	protected void setupWebView(){
		view.getSettings().setPluginState(PluginState.OFF);
		view.getSettings().setBlockNetworkLoads(true);
	}

	protected void setupHandler(){
		view.setWebViewClient(new WebViewClient(){

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Matcher m = patternIntent.matcher(url);
				if(m.find()){
					return kickAction(m.replaceAll(""));
				} else if (action != null) {
					return action.url(url);
				} else {
					return super.shouldOverrideUrlLoading(view, url);
				}
			}
		});
	}

	protected boolean kickAction(String urlpath){

		String[] items = urlpath.split("/");
		int cnt = items.length;
		if(cnt < 3 || action == null){
			/* do nothing */
		} else if("issue".equals(items[0])){
			action.issue(TypeConverter.parseInteger(items[1]), TypeConverter.parseInteger(items[2]));
			return true;
		}
		return false;
	}

	protected String  extendHtml(int connection_id,String input){
		return extendHtml(String.valueOf(connection_id),input);
	}
	protected String  extendHtml(String connection,String input){
		String result = "";
		result = patternIssue.matcher(input).replaceAll(getAnchor("#$1","issue/",connection,"/","$1")+"$2");
		result = patternInlineUrl.matcher(result).replaceAll("<a href=\"$1\">$1</a>");
		return result;
	}

	protected String getAnchor(String name,String... params){
		StringBuffer sb = new StringBuffer();
		sb.append("<a href=\"");
		sb.append(URL_PREFIX);
		for(String item : params){
			sb.append(item);
		}
		sb.append("\">");
		sb.append(name);
		sb.append("</a>");
		return sb.toString();
	}

	public void setContent(int connectionid,String text){
		String inner = extendHtml(connectionid,convertTextileToHtml(text));
		view.loadDataWithBaseURL("", getHtml(view.getContext(),inner,""), "text/html", "UTF-8", "");

	}

	public void setContent(String text){
		String inner = convertTextileToHtml(text);
		view.loadDataWithBaseURL("", getHtml(view.getContext(),inner,""), "text/html", "UTF-8", "");
	}


	static protected String reduceExternalHtml(String input, HashMap<String,String> export){
		Pattern p = Pattern.compile("<\\s*pre\\s*>(.*)<\\s*/\\s*pre\\s*>", Pattern.DOTALL);
		String texttile = input;
		Matcher m = p.matcher(texttile);

		while(m.find()){
			String target = m.group(1);
			StringBuffer sb = new StringBuffer();
			if(!TextUtils.isEmpty(target)){
				sb.append("<div class=\"pre\">");
				sb.append(target
						.replaceAll("&", "&amp;")
						.replaceAll("<", "&lt;")
						.replaceAll(">", "&gt;")
						.replaceAll("[\\r\\n]+", "<br>\r\n")
						.replaceAll(" ", "&nbsp;")
						.replaceAll("	", "&#009;")
						);
				sb.append("</div>");
			}
			String key = RandomStringUtils.randomAlphabetic(10);
			export.put(key, sb.toString());
			texttile = m.replaceFirst(key);
			m = p.matcher(texttile);
		}
		return texttile;
	}
	static public String convertTextileToHtml(String text){
		return convertTextileToHtml(text, false);
	}
	static public String convertTextileToHtml(String text, boolean isDocument){
		HashMap<String,String> restore = new HashMap<String,String>();
		String textile = reduceExternalHtml(text,restore);
		StringWriter sw = new StringWriter();
		HtmlDocumentBuilder builder = new HtmlDocumentBuilder(sw);
		builder.setEmitAsDocument(isDocument);

		MarkupParser parser = new MarkupParser(new TextileDialect());
		parser.setBuilder(builder);
		parser.parse(textile);
		textile = sw.toString();
		for(String item : restore.keySet()){
			textile = textile.replace(item, restore.get(item));
		}
		return  textile;
	}
	static public String getHtml(Context context,String innerhtml,String headerhtml){
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" ?>");
		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		sb.append("<head>");
		sb.append(headerhtml);
		sb.append("</head>");
		sb.append("<body");
		sb.append(" bgcolor=\"#" + getRGBString(getBackgroundColor(context)) + "\"");
		sb.append(" text=\"#" + getRGBString(getFontColor(context)) + "\"");
		sb.append(" style=\"margin:0;\"");
		sb.append(">");
		sb.append(innerhtml);
		sb.append("</body></html>");
		return sb.toString();
	}
	static public int getAttribute(Context context,int target){
		TypedValue typedValue = new TypedValue();
		context.getTheme().resolveAttribute(target, typedValue, true);
		int resourceId = typedValue.resourceId;
		return context.getResources().getColor(resourceId);
	}
	static public String getRGBString(int color){
		String hex = Integer.toHexString(0xFF000000 | color);
		return hex.substring(hex.length()-6);
	}
	static public int getBackgroundColor(Context context){
		return getAttribute(context, android.R.attr.colorBackground);
	}
	static public int getFontColor(Context context){
		return getAttribute(context, android.R.attr.colorForeground);
	}
}
