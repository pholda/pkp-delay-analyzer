package pl.pholda.pda

import akka.actor.ActorSystem
import akka.util.Timeout
import org.joda.time.{DateTime, Minutes}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import pl.pholda.pda.feed.TrainInRouteFeed
import pl.pholda.pda.model.DelayAtStation
import pl.pholda.pda.testutil.{BrowserActorMock, BrowserResourceMock}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by piotr on 28.02.16.
  */
class TrainInRouteFeedTest extends WordSpec with ScalaFutures with Matchers {
  implicit val timeout = Timeout(5 seconds)

  override implicit val patienceConfig: PatienceConfig = PatienceConfig(5 seconds, 0 seconds)
  val system = ActorSystem("test")

  "A TrainInRouteFeed" should {
    "parse single row" in {
      val browser = new BrowserResourceMock({
        case _ => "/train-polonia.html"
      })
      val browserActor = system.actorOf(BrowserActorMock.props(browser))
      val trainInRouteFeed = new TrainInRouteFeed(browserActor)

      val trainInRoute = trainInRouteFeed.getTrainInfo(44045952).futureValue

      trainInRoute.trainId shouldEqual 44045952
      trainInRoute.delays should contain (DelayAtStation(
        179221,
        "Petrovice U Karvine",
        "ECE 102 (102) POLONIA",
        None,
        Minutes.ZERO,
        Some(DateTime.parse("28.02.2016 17:31:00", TrainInRouteFeed.dateTimeFormatter)),
        Minutes.minutes(29),
        isEstimated = false
      ))
    }
    "detect current position" in {
      val browser = new BrowserResourceMock({
        case _ => "/train-polonia.html"
      })
      val browserActor = system.actorOf(BrowserActorMock.props(browser))
      val trainInRouteFeed = new TrainInRouteFeed(browserActor)

      val trainInRoute = trainInRouteFeed.getTrainInfo(44045952).futureValue

      trainInRoute.delays should contain (DelayAtStation(
        33506,
        "Warszawa Zachodnia",
        "ECE 41000 (102) POLONIA",
        Some(DateTime.parse("28.02.2016 20:58:00", TrainInRouteFeed.dateTimeFormatter)),
        Minutes.minutes(27),
        Some(DateTime.parse("28.02.2016 20:59:00", TrainInRouteFeed.dateTimeFormatter)),
        Minutes.minutes(27),
        isEstimated = true
      ))
    }
  }

}
