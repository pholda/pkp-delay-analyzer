package pl.pholda.pda.actor

import java.net.URL

import akka.actor.{Actor, Props}
import net.ruippeixotog.scalascraper.browser.Browser

/**
  * Created by piotr on 24.02.16.
  */
object BrowserActor {
  def props: Props = Props(new BrowserActor)

  case class Get(url: URL)

  object Get {
    def apply(url: String): Get = Get(new URL(url))
  }
}

class BrowserActor extends Actor {
  import BrowserActor._

  val browser = new Browser

  override def receive: Receive = {
    case Get(url) =>
      sender() ! browser.get(url.toString)
  }
}
