package com.ashleymenhennett.linkchecker.service

import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients

import com.ashleymenhennett.linkchecker.model.*

class LinkCheckingService {

	private static final String ABSOLUTE_URL_PATTERN = /((http|https):\/\/)?[-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&\/\/=]*)/
	
	BarrierLink barrier // abc.com - dont leave this 'root' domain
	HttpClient httpClient
	Set<String> failedUrls
	Set<String> visitedUrls
	Map<Integer, List<Link>> results
	
	LinkCheckingService(BarrierLink barrier) {
		this.barrier = barrier
		this.results = [:]
		this.failedUrls = []
		this.visitedUrls = []
	}
	
	HttpClient constructHttpClient() {
		return HttpClients.createDefault()
	}
	
	HttpGet constructHttpGet() {
		return new HttpGet()
	}
	
	Map checkLinks(Link root) {
		httpClient = constructHttpClient()
		
		Stack<Link> stack = [] as Stack
		stack.push root
		
		while (! stack.isEmpty() ) {
			def u = stack.pop()
			if (visitedUrls.contains(u.url) || failedUrls.contains(u.url)) continue
			def adjacent = visit u.url
			if (! u.url?.contains(barrier.url)) continue
			adjacent.unique().each {
				if (! visitedUrls.contains(it.url) && ! failedUrls.contains(it.url))
					stack.push it
			}
		}
		
		return [
			results: results,
			visited: visitedUrls,
			failed: failedUrls	
		]
	}
	
	private List<Link> visit(String originUrl) {
		def adjacent = []
		def response
		HttpGet get = constructHttpGet()
		get.setURI(new URI(originUrl))
		try {
			response = httpClient.execute(get)
			def responseCode = response.getStatusLine().getStatusCode()
			addToResults responseCode, originUrl
			visitedUrls << originUrl
			def entity = response.entity
			InputStream is = entity.content
			adjacent = extractLinks originUrl, is.text
		} catch (Exception e) {
			failedUrls << originUrl
		}
		return adjacent
	}
	
	private void addToResults(Integer code, String url) {
		results[code] ? results[code] << url : results << [ (code) : [url] ]
	}
	
	private List<Link> extractLinks(String originUrl, String content) {		
		def links = []
		def absoluteMatches = content =~ ABSOLUTE_URL_PATTERN
		absoluteMatches.each { links << new Link(it[0]) }
		return links
	}
	
}