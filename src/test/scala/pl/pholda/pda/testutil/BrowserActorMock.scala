package pl.pholda.pda.testutil

import akka.actor.Props
import net.ruippeixotog.scalascraper.browser.Browser
import pl.pholda.pda.actor.BrowserActor

/**
  * Created by piotr on 28.02.16.
  */
object BrowserActorMock {
  def props(browser: Browser): Props = Props(new BrowserActorMock(browser))
}
class BrowserActorMock(override val browser: Browser) extends BrowserActor {
}
