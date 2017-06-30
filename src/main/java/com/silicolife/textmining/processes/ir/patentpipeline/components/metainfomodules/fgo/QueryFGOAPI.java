package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.fgo;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

class QueryFGOAPI {
	// You can find the CSE and API_KEY here:
	// https://cse.google.com/cse/all
	//   - Go to the custom engine you created (& paid)
	//   - Edit Search Engine -> Business -> XML & JSON
	// Custom search engine identifier from above
	// API_KEY is the key allowing us to access google apis
	  private static final String CSE="XXXXXXXXXXXXXXXXXXXXX:xxxxxxxxxxx";
	  private static final String API_KEY="AAAAAAAAAAAAPPPPPPPPPPPPPPIIIIIIIIIIIII";

	// curl -s "https://www.googleapis.com/customsearch/v1?key=$API_KEY&cx=$CSE&q=$QUERY" > $QUERY.firstpage
	// "template": "https://www.googleapis.com/customsearch/v1?q={searchTerms}&num={count?}&start={startIndex?}&lr={language?}&safe={safe?}&cx={cx?}&cref={cref?}&sort={sort?}&filter={filter?}&gl={gl?}&cr={cr?}&googlehost={googleHost?}&c2coff={disableCnTwTranslation?}&hq={hq?}&hl={hl?}&siteSearch={siteSearch?}&siteSearchFilter={siteSearchFilter?}&exactTerms={exactTerms?}&excludeTerms={excludeTerms?}&linkSite={linkSite?}&orTerms={orTerms?}&relatedSite={relatedSite?}&dateRestrict={dateRestrict?}&lowRange={lowRange?}&highRange={highRange?}&searchType={searchType}&fileType={fileType?}&rights={rights?}&imgSize={imgSize?}&imgType={imgType?}&imgColorType={imgColorType?}&imgDominantColor={imgDominantColor?}&alt=json"

	  public static Set<String> query(String searchPhrase) throws IOException {
	    Set<String> idSet = new HashSet<>();
	    searchPhrase = URLEncoder.encode(searchPhrase, "UTF-8");
	    String base = "https://www.googleapis.com/customsearch/v1?key=" + API_KEY + "&cx=" + CSE + "&q=" + searchPhrase;

	    boolean hasNext = true;
	    int start = 0;
	    int page = 0;
	    while (hasNext) {
	      String url = base + "&num=10" + (start == 0 ? "" : "&start=" + start);
	      String json_str = FGOUtils.fetch(url);
	      JSONObject json = new JSONObject(json_str);
	      System.err.println("Total: " + json.getJSONObject("searchInformation").get("totalResults"));

	      JSONObject meta = json.getJSONObject("queries");
	      Set<String> patentIDs = extractAllIDs(json.getJSONArray("items"));
	      idSet.addAll(patentIDs);

	      int this_count = meta.getJSONArray("request").getJSONObject(0).getInt("count");
	      System.err.println("\t Got: " + start + " -> " + (start + this_count));

	      if (meta.has("nextPage")) {
	        // start = meta.getJSONArray("nextPage").getJSONObject(0).getInt("startIndex");
	        start += this_count;
	        page++;
	      } else {
	        hasNext = false;
	      }
	    }

	    return idSet;
	  }

	  private static Set<String> extractAllIDs(JSONArray items) {
	    Set<String> idSet = new HashSet<>();
	    String prefix = "patents/";
	    for (int i=0; i<items.length(); i++) {
	      JSONObject item = items.getJSONObject(i);
	      String url = item.getString("link");
	      // e.g., http://www.google.com/patents/CN102406082A?cl=en"
	      // e.g., https://www.google.com/patents/US7201928
	      // because it might be https or http, we look for patents/ and chop..
	      String pid = url.substring(url.indexOf(prefix) + prefix.length());
	      int suffix = pid.indexOf("?");
	      if (suffix != -1)
	        pid = pid.substring(0, suffix);
	      idSet.add(pid);
	    }
	    return idSet;
	  }
	}
