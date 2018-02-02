package models.graphql

import sangria.execution.FieldTag
import sangria.schema.{Argument, AstSchemaBuilder, Directive, DirectiveLocation, DirectiveResolver, ListInputType, StringType}
import today.expresso.grpc.user.dto.UserDto

/**
  * @author im.
  */
object GraphQLShema {

  case object Authorised extends FieldTag

  case class Permission(role: UserDto.Role) extends FieldTag

  val RoleArg = Argument("role", ListInputType(StringType), "The variable name.")

  val ExportDirective = Directive("authorized",
    description = Some("Only users with listed roles will have access to this field"),
    arguments = RoleArg :: Nil,
    locations = Set(DirectiveLocation.Object, DirectiveLocation.Field))
}
