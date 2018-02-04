package services.graphql

import javax.inject.{Inject, Singleton}

import grpc.GrpcErrorHandler
import models.graphql.{GraphQLAnonymousContext, GraphQLContext}
import models.graphql.rotes.{PublicSchema, SchemaRegistry}
import models.{ApplicationContext, Credentials}
import play.api.libs.json.{JsObject, Json}
import sangria.ast.Document
import sangria.execution._
import sangria.marshalling.playJson._
import services.ServiceRegistry
import today.expresso.grpc.user.dto.UserDto

import scala.concurrent.ExecutionContext

/**
  * @author im.
  */
@Singleton
class GraphQLService @Inject()(registry: ServiceRegistry)(implicit ec: ExecutionContext) {

  val exceptionHandler = ExceptionHandler {
    case (m, e: io.grpc.StatusRuntimeException) =>
      val (description, details) = GrpcErrorHandler.getDescription(e.getStatus.getDescription)
      HandledException(description , Map("debug" -> m.scalarNode(details, "String", Set.empty)))
  }


  def executeQuery(query: Document, operation: Option[String], variables: Option[JsObject])
                  (app: ApplicationContext, route: UserDto.Role, creds: Credentials, debug: Boolean) = {
    Executor.execute(
      schema = SchemaRegistry.registry(route),
      queryAst = query,
      userContext = GraphQLContext(app, registry, creds),
      operationName = operation,
      variables = variables.getOrElse(Json.obj()),
      exceptionHandler = exceptionHandler
    )
      .map(play.api.mvc.Results.Ok(_))
      .recover {
        case error: QueryAnalysisError => play.api.mvc.Results.BadRequest(error.resolveError)
        case error: ErrorWithResolver => play.api.mvc.Results.BadRequest(error.resolveError)
      }
  }

  def executePublicQuery(query: Document, operation: Option[String], variables: Option[JsObject])
                        (app: ApplicationContext, debug: Boolean) = {
    Executor.execute(
      schema = PublicSchema.schema,
      queryAst = query,
      userContext = GraphQLAnonymousContext(app, registry),
      operationName = operation,
      variables = variables.getOrElse(Json.obj()),
      exceptionHandler = exceptionHandler
    )
      .map(play.api.mvc.Results.Ok(_))
      .recover {
        case error: QueryAnalysisError => play.api.mvc.Results.BadRequest(error.resolveError)
        case error: ErrorWithResolver => play.api.mvc.Results.BadRequest(error.resolveError)
      }
  }
}
