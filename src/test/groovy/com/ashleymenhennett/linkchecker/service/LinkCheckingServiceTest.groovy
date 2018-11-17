package com.ashleymenhennett.linkchecker.service

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.StatusLine
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet

import org.junit.Test
import org.junit.Before

import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

import com.ashleymenhennett.linkchecker.model.Link
import com.ashleymenhennett.linkchecker.model.BarrierLink


class LinkCheckingServiceTest {
	
	@Mock
	HttpClient httpClient
	
	@Mock
	HttpGet httpGet
	
	@Mock
	HttpResponse httpResponse
	
	@Mock
	StatusLine statusLine
	
	@Mock
	HttpEntity httpEntity
	
	LinkCheckingService subject
	
	@Before
	void before() {
		MockitoAnnotations.initMocks(this)
		subject = Mockito.spy(new LinkCheckingService(new BarrierLink('ashley.menhennett.com')))
	}

	@Test
	void "service can traverse simple webpage"() {
		def streams = createStreams()

		Mockito.when(subject.constructHttpClient()).thenReturn(httpClient)
		Mockito.when(subject.constructHttpGet()).thenReturn(httpGet)
		Mockito.when(httpClient.execute(Mockito.anyObject())).thenReturn(httpResponse)
		Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine)
		Mockito.when(statusLine.getStatusCode()).thenReturn(200, 302, 502, 200, 200, 200, 403, 501, 500)
		Mockito.when(httpResponse.getEntity()).thenReturn(httpEntity)
		Mockito.when(httpEntity.getContent()).thenReturn(streams.articles, streams.testing, 
			streams.somethingElse, streams.anotherTest, streams.somethingElseAgain, streams.furtherAway, 
			streams.coolNewArticle, streams.anotherTest2, streams.google)
		
		def result = subject.checkLinks(new Link('http://ashley.menhennett.com/articles/'))
		
		assert result['results'][200].contains('http://ashley.menhennett.com/articles/')
		assert result['results'][200].contains('http://ashley.menhennett.com/anotherTest2')
		assert result['results'][200].contains('http://ashley.menhennett.com/cool-new-article')
		assert result['results'][200].contains('http://no-further-after-this-url.com')
		assert result['results'][302].contains('http://ashley.menhennett.com/testing')
		assert result['results'][502].contains('http://something-else.com')
		assert result['results'][403].contains('http://something-else-again.com')
		assert result['results'][501].contains('http://ashley.menhennett.com/anotherTest')
		assert result['results'][500].contains('http://google.com')
	}
	
	private Map createStreams() {
		def articlesContent = "\
			http://ashley.menhennett.com/testing \
			http://ashley.menhennett.com/anotherTest \
			http://ashley.menhennett.com/anotherTest2 \
			http://ashley.menhennett.com/anotherTest2 \
			http://ashley.menhennett.com/testing \
		"
		def articlesInputStream = new ByteArrayInputStream(articlesContent.getBytes('UTF-8'))
		
		def testingContent = "\
			http://something-else.com \
		"
		def testingInputStream = new ByteArrayInputStream(testingContent.getBytes('UTF-8'))
		
		def somethingElseContent = "\
			http://something-else.com/further-away \
		"
		def somethingElseInputStream = new ByteArrayInputStream(somethingElseContent.getBytes('UTF-8'))
			
		def anotherTestContent = "\
			http://something-else-again.com \
			http://ashley.menhennett.com/cool-new-article \
		"
		def anotherTestInputStream = new ByteArrayInputStream(anotherTestContent.getBytes('UTF-8'))
		
		def somethingElseAgainContent = "\
			http://no-further-after-this-url.com \
			gibberish \
		"
		def somethingElseAgainInputStream = new ByteArrayInputStream(somethingElseAgainContent.getBytes('UTF-8'))
		
		def furtherAwayContent = "\
			http://holiday.com \
			http://melbourne.com \
		"
		def furtherAwayInputStream = new ByteArrayInputStream(furtherAwayContent.getBytes('UTF-8'))
			
		def coolNewArticleontent = "\
			http://ashley.menhennett.com/testing \
			some random text \
		"
		def coolNewArticleInputStream = new ByteArrayInputStream(coolNewArticleontent.getBytes('UTF-8'))
		
		def anotherTest2Content = "\
			http://google.com \
		"
		def anotherTest2InputStream = new ByteArrayInputStream(anotherTest2Content.getBytes('UTF-8'))
		
		def googleContent = "\
			http://ashley.menhennett.com.com \
		"
		def googleInputStream = new ByteArrayInputStream(googleContent.getBytes('UTF-8'))
		
		return [
			'articles' : articlesInputStream,
			'testing' : testingInputStream,
			'somethingElse': somethingElseInputStream,
			'somethingElseAgain': somethingElseAgainInputStream,
			'furtherAway': furtherAwayInputStream,
			'coolNewArticle': coolNewArticleInputStream,
			'anotherTest' : anotherTestInputStream,
			'anotherTest2' : anotherTest2InputStream,
			'google' : googleInputStream
		]
	}
	
}
