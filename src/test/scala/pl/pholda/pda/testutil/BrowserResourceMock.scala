package pl.pholda.pda.testutil

import net.ruippeixotog.scalascraper.browser.Browser
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

import scala.io.Source

class BrowserResourceMock(url2resource: String => String) extends Browser {
  override def get(url: String): Document = {
    val content = Source.fromInputStream(getClass.getResourceAsStream(url2resource(url))).getLines().mkString("\n")
   Jsoup.parse(content, url)
  }
}
