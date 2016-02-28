package pl.pholda.pda.feed

import java.net.URLEncoder

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import org.jsoup.nodes.Document
import pl.pholda.pda.actor.BrowserActor
import pl.pholda.pda.model.Station

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class StationFeed(browserActor: ActorRef)(implicit ec: ExecutionContext) {
  implicit val timeout = Timeout(5 seconds)

  protected def url(query: String) =
    s"http://infopasazer.intercity.pl/?p=stations&q=${URLEncoder.encode(query, "UTF-8")}"

  def search(query: String): Future[List[Station]] = {
    browserActor ? BrowserActor.Get(url(query)) map { case document: Document =>
      val tds = document extract elementList("td")
      tds map { case td =>
        Station(
        stationId = extractStationId(td extract attr("onclick")("td")),
        name = td extract text("td")
      )}
    }
  }

  def getAll: Future[List[Station]] = {
    val polishAlphabet = 'Ą' :: 'Ć' :: 'Ę' :: 'Ł' :: 'Ń' :: 'Ó' :: 'Ś' :: 'Ż' :: 'Ź' :: ('A' to 'Z').toList
    Future.sequence(
      polishAlphabet map { char => search(char.toString)}
    ).map(_.flatten)
  }
}
