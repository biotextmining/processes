package com.silicolife.textmining.processes.ir.fgo;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
This class is the naive crawler that traverses google search result pages
 pretending to be a human (which is why the delay(5) seconds.
We can make this much faster by using Google's custom search engines and
 querying within our quota, without delays...
 */
public class QueryFGONonAPI {

	private static final int MAX_RESULTS = 10000;
	
	public static Set<String> retrievedPatentIds(String searchPhrase) throws IOException {
		Set<String> idSet = new HashSet<>();
		searchPhrase = URLEncoder.encode(searchPhrase, "UTF-8");

		String base = "https://www.google.com/search?q=" + searchPhrase + "&num=100&biw=1440&bih=557&tbm=pts&start=INSERTSTARTINDEX&sa=N";
		// String base = "https://www.google.com/?tbm=pts&gws_rd=ssl#tbm=pts&q=Bactericide+composition+and+abietic+methanol+bacteria+coli&num=100&biw=1440&bih=557&tbm=pts&start=INSERTSTARTINDEX&sa=N";

		outer: for(int i=0; i<MAX_RESULTS; i++) {
			String pageStart = Integer.toString(i*100);
			String url = base.replaceAll("INSERTSTARTINDEX", pageStart);
			try {
				FGOAPI.delay(5);
				String text = FGOAPI.fetch(url);
				List<String> ids = extractAllIDs(text);

				//Break if it is duplicating a page
				boolean breakit = true;
				inner: for(String str : ids) {
					if(!idSet.contains(str)) {
						breakit = false;
						break inner;
					}
				}
				if(breakit) {
					break outer;
				}

				idSet.addAll(ids);
			} catch (Exception ex) {
				if (ex.getMessage().startsWith("StatusCode = 503")
						|| ex.getMessage().startsWith("StatusCode = 403")) {
					// google is blocking us now. no point in continuing, abort
					throw ex;
				} else {
					// not blocked, but some other error: dump to log, and continue
					ex.printStackTrace();
				}
			}
		}

		return idSet;
	}
	
	private static List<String> extractAllIDs(String text) {
		//First extract all urls that are within quotes
		List<String> results = new ArrayList<>();
		String patternString = "(?<=\")https://www.google.com/patents/[^\"]+(?=\")";

		Pattern patt = Pattern.compile(patternString);

		Matcher matcher = patt.matcher(text);
		boolean matches = matcher.matches();

		int count = 0;
		while(matcher.find()) {
			count++;
			results.add(text.substring(matcher.start(), matcher.end()));
		}

		// Clean up each URL to just the id
		List<String> out = new ArrayList<>();
		for(String rawurl : results) {
			String[] split = rawurl.split("[^0-9a-zA-Z]");
			String id = split[7];
			if(id.equals("related")) {
				continue;
			}
			if(!out.contains(id)) {
				out.add(id);
			}
		}
		return out;
	}
}