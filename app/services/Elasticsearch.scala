package services

import javax.inject.{Inject, Singleton}

import com.sksamuel.elastic4s.{ElasticsearchClientUri, Indexable}
import com.sksamuel.elastic4s.analyzers.RussianLanguageAnalyzer
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.http.index.IndexResponse
import play.api.inject.ApplicationLifecycle
import play.api.libs.json.Json
import play.api.{Configuration, Logger}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author im.
  */
@Singleton
class Elasticsearch @Inject()(appLifecycle: ApplicationLifecycle, var config: Configuration)(implicit ec: ExecutionContext) {
  import Elasticsearch._

  config = config.get[Configuration]("elasticsearch")
  private val host = config.get[String]("host")
  private val port = config.get[Int]("port")
  val client = HttpClient(ElasticsearchClientUri(host, port))


  import com.sksamuel.elastic4s.http.ElasticDsl._
  import com.sksamuel.elastic4s.playjson._


  // Next we create an index in advance ready to receive documents.
  // await is a helper method to make this operation synchronous instead of async
  // You would normally avoid doing this in a real program as it will block the calling thread
  // but is useful when testing
  client.execute {
    createIndex("expresso") shards 3 replicas 2 mappings (
      mapping("campaigns") as(
        textField("id"),
        textField("typex"),
        textField("createTime"),
        textField("archiveUrl"),
        textField("status"),
        dateField("sendTime"),
        textField("plainText") analyzer RussianLanguageAnalyzer
      )
      )
  }


  // next we index a single document. Notice we can pass in the case class directly
  // and elastic4s will marshall this for us using the circe marshaller we imported earlier.
  // the refresh policy means that we want this document to flush to the disk immmediately.
  // see the section on Eventual Consistency
  //
  //
  def index(expresso: ExpressoIndex): Future[IndexResponse] = {
    client.execute {
//      indexInto("expresso" / "campaigns") doc index refresh RefreshPolicy.IMMEDIATE
      indexInto("expresso" / "campaigns") doc expresso
    }
  }


  appLifecycle.addStopHook { () =>
    Future {
      Logger.info(s"$getClass: shutdown")
      client.close()
    }
  }
}

object Elasticsearch {

  implicit val expressoIndexWrites = Json.writes[ExpressoIndex]

  case class ExpressoIndex(
                            id: String,
                            archiveUrl: String,
                            sendTime: String,
                            plainText: String)
}
