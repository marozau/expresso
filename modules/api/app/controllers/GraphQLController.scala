package controllers

import javax.inject.{Inject, Singleton}

import models.{ApplicationContext, User}
import play.api.Logger
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import sangria.parser.{QueryParser, SyntaxError}
import services.graphql.GraphQLService
import utils.WithStringRole

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

  def graphql = Action.async(parse.json) { request =>
    val query = (request.body \ "query").as[String]
    val operation = (request.body \ "operationName").asOpt[String]
    val variables = (request.body \ "variables").toOption.map {
      case obj: JsObject => obj
      case _ => Json.obj()
    }

    QueryParser.parse(query) match {
      case Success(queryAst) =>
        graphQLService.executePublicQuery(queryAst, operation, variables)(app, debug = true)

      // can't parse GraphQL query, return error
      case Failure(error: SyntaxError) =>
        Future.successful(BadRequest(Json.obj("error" → error.getMessage)))
    }
  }

  def graphqlWithRole(role: String) = app.auth.silhouette.SecuredAction(WithStringRole(role)).async(parse.json) { request =>
    val query = (request.body \ "query").as[String]
    val operation = (request.body \ "operationName").asOpt[String]
    val variables = (request.body \ "variables").toOption.map {
      case obj: JsObject => obj
      case _ => Json.obj()
    }

    val rote = User.strToRoleMapper(role.toUpperCase)

    QueryParser.parse(query) match {
      case Success(queryAst) =>
        graphQLService.executeQuery(queryAst, operation, variables)(app, rote, request, debug = true)

      // can't parse GraphQL query, return error
      case Failure(error: SyntaxError) =>
        Future.successful(BadRequest(Json.obj("error" → error.getMessage)))
    }
  }
}
