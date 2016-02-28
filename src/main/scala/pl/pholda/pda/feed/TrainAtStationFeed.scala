package pl.pholda.pda.feed

import java.net.URLEncoder

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.joda.time.format.{DateTimeFormatter, DateTimeFormat}
import org.joda.time.{DateTime, Minutes}
import org.jsoup.nodes.{Document, Element}
import pl.pholda.pda.actor.BrowserActor
import pl.pholda.pda.model.{TrainId, StationId, TrainAtStation}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
/**
  * Created by piotr on 25.02.16.
  */
object TrainAtStationFeed {
  case class JoinedRow(arrival: Option[Element], departure: Option[Element]) {
    assert(arrival.isDefined || departure.isDefined)

    lazy val anyCols: List[Element] = (arrival orElse departure get) extract elementList("td")

    lazy val arrivalCols: Option[List[Element]] = arrival map {_ extract elementList("td")}

    lazy val departureCols: Option[List[Element]] = departure map {_ extract elementList("td")}
  }

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")
}

class TrainAtStationFeed(browserActor: ActorRef)(implicit ec: ExecutionContext) {
  import TrainAtStationFeed._

  implicit val timeout = Timeout(5 seconds)

  def getAll(stationId: StationId): Future[List[TrainAtStation]] = {
    val url = s"http://infopasazer.intercity.pl/?p=station&id=${URLEncoder.encode(stationId.toString, "UTF-8")}"
    browserActor ? BrowserActor.Get(url) map { case document: Document =>
      val tables: List[Element] = document extract elementList(".table-delay")
      val arrivals: List[Element] = tables.head extract elementList("tr") tail
      val departures: List[Element] = tables(1) extract elementList("tr") tail

      val joinedRows: Map[TrainId, JoinedRow] = joinRows(arrivals, departures)

      val arrivalTrains = joinedRows map { case (trainId, row) =>
        val relation = row.anyCols(3).text().split("-").map(_.trim)

        TrainAtStation(
          trainId = trainId,
          trainNumber = row.anyCols.head.text(),
          operator = row.anyCols(1).text(),
          from = relation(0),
          to = relation(1),
          arriveDate = extractDateTime(row.arrivalCols),
          arriveDelay = extractDelay(row.arrivalCols),
          departureDate = extractDateTime(row.departureCols),
          departureDelay = extractDelay(row.departureCols)
        )
      }

      arrivalTrains.toList
    }
  }

  protected def extractDateTime(row: Option[List[Element]]): Option[DateTime] = row map { cols =>
    DateTime.parse(cols(2).text() + " " + cols(4).text(), dateTimeFormatter)
  }

  protected def extractDelay(row: Option[List[Element]]): Minutes = row flatMap { cols =>
    delayRegex.findFirstMatchIn(cols(5).text).map(_.group(1).toInt) map {
      delay => Minutes.minutes(delay)
    }
  } getOrElse Minutes.ZERO

  protected def groupTrainTable(rows: List[Element]): Map[TrainId, Element] = {
    rows map { row =>
      val cols: List[Element] = row extract elementList("td")
      val trainId = extractTrainId(cols.head extract attr("href")("a"))
      trainId -> row
    } toMap
  }

  protected def joinRows(arrivals: List[Element], departures: List[Element]): Map[TrainId, JoinedRow] = {
    val groupedArrivals = groupTrainTable(arrivals)
    val groupedDepartures = groupTrainTable(departures)

    val trainIds = (groupedArrivals.keys ++ groupedDepartures.keys).toList.distinct

    trainIds map { trainId =>
      trainId -> JoinedRow(groupedArrivals.get(trainId), groupedDepartures.get(trainId))
    } toMap
  }
}
