package pl.pholda.pda

import akka.actor.ActorSystem
import akka.util.Timeout
import org.joda.time.{Minutes, DateTime}
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent.ScalaFutures
import pl.pholda.pda.feed.TrainAtStationFeed
import pl.pholda.pda.model.TrainAtStation
import pl.pholda.pda.testutil.{BrowserActorMock, BrowserResourceMock}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by piotr on 28.02.16.
  */
class TrainAtStationFeedTest extends WordSpec with ScalaFutures with Matchers {
  implicit val timeout = Timeout(5 seconds)

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(5 seconds, 0 seconds)
  val system = ActorSystem("test")

  "A TrainAtStationFeed" should {
    "fetch two trains for Zakopane" in {
      val browser = new BrowserResourceMock({
        case "http://infopasazer.intercity.pl/?p=station&id=79020" => "/trains-zakopane.html"
      })
      val browserActor = system.actorOf(BrowserActorMock.props(browser))
      val trainAtStationFeed = new TrainAtStationFeed(browserActor)

      val trains = trainAtStationFeed.getAll(79020).futureValue

      trains should contain only (
        TrainAtStation(
          44042102,
          "30511/0 GUBAŁÓWKA",
          "Przewozy Regionalne",
          "Kraków Płaszów",
          "Zakopane",
          Some(DateTime.parse("2016-02-28 20:26", TrainAtStationFeed.dateTimeFormatter)),
          Minutes.ZERO,
          None,
          Minutes.ZERO
        ),
        TrainAtStation(
          44042136,
          "33158/9 PODHALE",
          "Przewozy Regionalne",
          "Zakopane",
          "Kraków Płaszów",
          None,
          Minutes.ZERO,
          Some(DateTime.parse("2016-02-28 19:10", TrainAtStationFeed.dateTimeFormatter)),
          Minutes.ZERO
        )
      )
    }
    "correctly join informations about trains in arrival and departure tables" in {
      val browser = new BrowserResourceMock({
        case "http://infopasazer.intercity.pl/?p=station&id=33605" => "/trains-warszawa-centralna.html"
      })
      val browserActor = system.actorOf(BrowserActorMock.props(browser))
      val trainAtStationFeed = new TrainAtStationFeed(browserActor)

      val trains = trainAtStationFeed.getAll(33605).futureValue

      trains should contain (TrainAtStation(
        44045428,
        "10143/2",
        "Przewozy Regionalne",
        "Łódź Kaliska",
        "Warszawa Wschodnia",
        Some(DateTime.parse("2016-02-28 20:46", TrainAtStationFeed.dateTimeFormatter)),
        Minutes.minutes(5),
        Some(DateTime.parse("2016-02-28 20:55", TrainAtStationFeed.dateTimeFormatter)),
        Minutes.minutes(0)
      ))
    }
  }
}
