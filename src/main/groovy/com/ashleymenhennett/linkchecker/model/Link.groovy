package com.ashleymenhennett.linkchecker.model

trait Linkable implements Comparable {
	String url
	List<Link> adjacent
	Integer responseCode
	int compareTo(Object other) {
		return this.url == other?.url
	}
	String toString() { return url }
}

class Link implements Linkable{ 
	Link(String url) { this.url = url }
}

class BarrierLink implements Linkable { 
	BarrierLink(String url) { this.url = url }
}