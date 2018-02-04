package models.graphql.rotes

import models.graphql.GraphQLAnonymousContext
import models.graphql.dto.UserDtoSchema
import sangria.schema.{Field, ObjectType, Schema, fields}
import today.expresso.grpc.user.dto.UserDto

/**
  * @author im.
  */
object PublicSchema {

  val Query = ObjectType[GraphQLAnonymousContext, Unit](
    "Query", fields[GraphQLAnonymousContext, Unit](
      Field("public", UserDtoSchema.UserDtoRoleEnum,
        resolve = _ => UserDto.Role.USER))
    )

  val schema = Schema(Query)
}
