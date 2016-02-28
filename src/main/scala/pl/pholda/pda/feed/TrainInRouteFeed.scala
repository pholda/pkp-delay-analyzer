package pl.pholda.pda.feed

import java.net.URLEncoder

import akka.actor.ActorRef
import akka.util.Timeout
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}
import org.joda.time.{Minutes, DateTime}
import pl.pholda.pda.model.{DelayAtStation, TrainInRoute, TrainId, Station}

import scala.annotation.tailrec
import scala.concurrent.{Future, ExecutionContext}
import java.net.URLEncoder

import akka.actor.ActorRef
import akka.util.Timeout
import net.ruippeixotog.scalascraper.browser.Browser
import org.jsoup.nodes.{Element, Document}
import pl.pholda.pda.actor.BrowserActor

import scala.concurrent.{ExecutionContext, Future}
import akka.pattern.ask
import scala.concurrent.duration._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._

/**
  * Created by piotr on 28.02.16.
  */
object TrainInRouteFeed {

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")
}

class TrainInRouteFeed(browserActor: ActorRef)(implicit ec: ExecutionContext) {
  import TrainInRouteFeed._

  implicit val timeout = Timeout(5 seconds)

  protected def url(trainId: TrainId) =
    s"http://infopasazer.intercity.pl/?p=train&id=${URLEncoder.encode(trainId.toString, "UTF-8")}"

  def getTrainInfo(trainId: TrainId): Future[TrainInRoute] = {
    browserActor ? BrowserActor.Get(url(trainId)) map { case document: Document =>
      val rows: List[Element] = document extract elementList("tr") tail

      @tailrec
      def parseRows(rows: List[Element], delays: List[DelayAtStation], isEstimated: Boolean): List[DelayAtStation] = {
        rows match {
          case Nil => delays.reverse
          case row :: tail =>
            val cols: List[Element] = row.children().toList
            val isCurrentEstimated = isEstimated || (row extract attr("class")("tr")).split(" ").contains("current")
            val delay = DelayAtStation(
              stationId = extractStationId(cols(3) extract attr("href")("a")),
              stationName = cols(3).text(),
              trainNumber = cols.head.text(),
              arriveDate = extractDateTime(cols(1).text, cols(4).text),
              arriveDelay = extractDelay(cols(5).text),
              departureDate = extractDateTime(cols(1).text, cols(6).text),
              departureDelay = extractDelay(cols(7).text),
              isEstimated = isCurrentEstimated
            )
            parseRows(tail, delay :: delays, isCurrentEstimated)
        }
      }

      TrainInRoute(trainId, parseRows(rows, Nil, isEstimated = false))
    }
  }

  def extractDateTime(date: String, time: String): Option[DateTime] = {
    if (time.isEmpty) {
      None
    } else {
      Some(DateTime.parse(date+" "+time, dateTimeFormatter))
    }
  }

  def extractDelay(string: String): Minutes = {
    if (string == "---") {
      Minutes.ZERO
    } else {
      delayRegex.findFirstMatchIn(string) map {m =>
        Minutes.minutes(m.group(1).toInt)
      } getOrElse Minutes.ZERO
    }
  }

}
