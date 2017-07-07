package bingtool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class OldMain extends Log {

	private static Header clientIdHeader = null;
	private static Header userStateHeader = null;
	private static String out;

	// String URL; SearchResult;
	private static HashMap<String, SearchResult> map;

	private static int totalEstimatedMatches = -1;

	static boolean download(int stopPage, String key, String url) {

		map = new HashMap<>();

		int currentPage = 0;

		do {
			try {
				currentPage++;

				String pageUrl = Utils.getUrlForPage(url, currentPage, map.size());

				log.debug("Searching on page {}: {}", currentPage, pageUrl);

				HttpClient client = HttpClientBuilder.create()
						.setUserAgent("Mozilla")
						.build();

				HttpGet request = new HttpGet(pageUrl);
				request.addHeader("User-Agent", "Mozilla");
				request.addHeader("Ocp-Apim-Subscription-Key", key);
				if(clientIdHeader != null) {
					request.addHeader(clientIdHeader);
					log.debug("added clientIdHeader");
				}
				if(userStateHeader != null) {
					request.addHeader(userStateHeader);
					log.debug("added userStateHeader");
				}

				HttpResponse response = null;
				try {
					response = client.execute(request);
				} catch (Exception e) {
					log.info("error in request", e);
				}

				int responseCode = response.getStatusLine().getStatusCode();

				System.out.println("Response Code: " + responseCode);

				if(responseCode != 200) {
					log.error("Da stimmt was nicht mit dem Statuscode der Antwort von Bing.");
					return false;
				}

				if(clientIdHeader == null) {
					clientIdHeader = response.getFirstHeader("X-MSEdge-ClientID");
				}
				if(userStateHeader == null) {
					userStateHeader = response.getFirstHeader("X-MSAPI-UserState");
				}

				BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

				StringBuffer result = new StringBuffer();
				String line = "";
				while((line = reader.readLine()) != null) {
					result.append(line);
				}

				out = result.toString();

				log.debug("Bing response: {}", out);

				if(totalEstimatedMatches == -1) {
					String sub1 = out.substring(out.indexOf("\"totalEstimatedMatches\": ") + "\"totalEstimatedMatches\": ".length());
					totalEstimatedMatches = Integer.parseInt(sub1.substring(0, sub1.indexOf(",")));

					log.debug("Found totalEstimatedMatches: {}", + totalEstimatedMatches);
				}
			} catch (Exception e) {
				Utils.handleException(e);
			}

			Document document = null;
			try {
				document = Jsoup.parse(out);
			} catch (Exception e) {
				Utils.handleException(e);
			}

			String body = document.children().get(0).children().get(1).html();
			//System.out.println(body);

			try {
				for(String l : getLinks(body)) {
					log.debug("Result: " + l);
					SearchResult existing = map.get(l);
					if(existing == null) {
						SearchResult r = new SearchResult(l);
						r.onPage(currentPage);
						map.put(l, r);
					} else {
						existing.onPage(currentPage);
						map.put(l, existing);
					}
				}

				// Save / backup the list
				int cr = map.size();

				log.debug("Current results: {}", cr);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} while((totalEstimatedMatches > map.size()) && (currentPage < stopPage));

		log.info("Search stopped at page: {}", currentPage);
		log.info("Results: " + map.size());

		// save results to file
		Utils.saveObjectTMP(map, "bingResultMapBackup");

		log.info("Search results saved.");

		return true;
	}

	@SuppressWarnings("unchecked")
	public static void loadMapFromFile() {
		map = (HashMap<String, SearchResult>) Utils.loadObjectTMP("bingResultMapBackup");
	}

	private static ArrayList<String> getLinks(String body) throws UnsupportedEncodingException {
		ArrayList<String> results = new ArrayList<>();
		int ir;
		while((ir = body.indexOf("r=")) != -1) {
			int cutted;
			int bodyL = body.length();
			String sub = body.substring(ir);
			int firstCut = bodyL - sub.length();
			String cut = sub.substring("r=".length(), sub.indexOf(";"));
			cutted = firstCut + cut.length();
			body = body.substring(cutted);

			String link = cut.substring(0, cut.length() - "&amp".length());
			log.debug("Found link: {}", link);

			if(!link.contains("www.bing.com")) {
				results.add(URLDecoder.decode(link, "UTF-8"));
			}
		}
		return results;
	}

	static void subdomainsAusgeben() {
		ArrayList<String> subdomains = new ArrayList<>();
		for(String s : map.keySet()) {
			SearchResult searchResult = map.get(s);
			String sub = searchResult.getSub();
			if(!subdomains.contains(sub)) {
				subdomains.add(sub);
			}
		}
		log.info("Subdomains:");
		for(String s : subdomains) {
			log.info(s);
		}
		log.info("-----");
	}

	static void duplikateMitAnderemProtokollAusgeben() {
		ArrayList<String> sites = new ArrayList<>();
		ArrayList<String> duplicates = new ArrayList<>();
		for(String s : map.keySet()) {
			SearchResult searchResult = map.get(s);
			String ptmp = searchResult.getProtocol() + "://";
			String withoutProtocol = s.substring(ptmp.length(), s.length());

			if(sites.contains(withoutProtocol)) {
				if(!duplicates.contains(withoutProtocol)) {
					duplicates.add(withoutProtocol);
				}
			} else {
				sites.add(withoutProtocol);
			}
		}

		/*log.info("Ok:");
		for(String s : sites) {
			System.out.println(s);
		}*/

		log.info("Duplicates with different protocol:");
		for(String s : duplicates) {
			System.out.println(s);
		}

		log.info("-----");
	}

	static void seitennummernZuUrlsAusgeben() {
		for(String s : map.keySet()) {
			SearchResult searchResult = map.get(s);

			//if(searchResult.getPages().size() > 1) {
			String pages = "";
			for(int page : searchResult.getPages()) {
				pages += page + ";";
			}
			log.info(s + ": " + pages);
			//}
		}

		log.info("-----");
	}

}
