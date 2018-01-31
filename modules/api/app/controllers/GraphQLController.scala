package controllers

import javax.inject.{Inject, Singleton}

import models.ApplicationContext
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import sangria.parser.{QueryParser, SyntaxError}
import services.graphql.GraphQLService

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * @author im.
  */
@Singleton
class GraphQLController @Inject()(app: ApplicationContext,
                                  graphQLService: GraphQLService,
                                  cc: ControllerComponents) extends AbstractController(cc) {

  import models.Credentials._

  // TODO: make Credentials optional for unsecure requests
  // TODO: store correlationId in credentials. Possibly it is better to wrap into HeaderDto
  def graphql = app.auth.silhouette.SecuredAction.async(parse.json) { request =>
    val query = (request.body \ "query").as[String]
    val operation = (request.body \ "operationName").asOpt[String]
    val variables = (request.body \ "variables").toOption.map {
      case obj: JsObject => obj
      case _ => Json.obj()
    }

    QueryParser.parse(query) match {
      // query parsed successfully, time to execute it!
      case Success(queryAst) =>
        graphQLService.executeQuery(queryAst, operation, variables)(app, request, debug = true)

      // can't parse GraphQL query, return error
      case Failure(error: SyntaxError) =>
        Future.successful(BadRequest(Json.obj("error" → error.getMessage)))
    }
  }
}
