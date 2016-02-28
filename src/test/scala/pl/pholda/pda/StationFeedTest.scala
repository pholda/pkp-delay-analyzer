package pl.pholda.pda

import akka.actor.ActorSystem
import akka.util.Timeout
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import pl.pholda.pda.feed.StationFeed
import pl.pholda.pda.model.Station
import pl.pholda.pda.testutil.{BrowserActorMock, BrowserResourceMock}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class StationFeedTest extends WordSpec with ScalaFutures with Matchers {
  implicit val timeout = Timeout(5 seconds)

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(5 seconds, 0 seconds)
  val system = ActorSystem("test")

  "A StationFeed" should {
    "find stations in stations&q=` document" in {
      val browser = new BrowserResourceMock({
        case "http://infopasazer.intercity.pl/?p=stations&q=Rzesz%C3%B3w" => "/stations-rzeszow.html"
      })
      val browserActor = system.actorOf(BrowserActorMock.props(browser))
      val stationFeed = new StationFeed(browserActor)
      val stations = stationFeed.search("Rzeszów").futureValue

      stations should contain only (
        Station(82669,"Rzeszów"),
        Station(82677,"Rzeszów Osiedle"),
        Station(82602,"Rzeszów Staroniwa")
      )
    }

    "find all stations querying through all letters" in {
      val browser = new BrowserResourceMock({
        case "http://infopasazer.intercity.pl/?p=stations&q=E" => "/stations-e.html"
        case "http://infopasazer.intercity.pl/?p=stations&q=F" => "/stations-f.html"
        case _ => "/stations-empty.html"
      })
      val browserActor = system.actorOf(BrowserActorMock.props(browser))
      val stationFeed = new StationFeed(browserActor)
      val stations = stationFeed.getAll.futureValue

      stations should contain only (
        Station(8151,"Elbląg"),
        Station(11809,"Ełk"),
        Station(12237,"Ełk Szyba Wschód"),
        Station(24067,"Fasty"),
        Station(30940,"Fałkowo"),
        Station(20271,"Firlus"),
        Station(7914,"Fiszewo"),
        Station(179072,"Florek"),
        Station(61507,"Fosowskie"),
        Station(39784,"Fronolów"),
        Station(82859,"Frysztak")
      )
    }
  }
}
