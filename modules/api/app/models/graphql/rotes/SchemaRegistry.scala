package models.graphql.rotes

import today.expresso.grpc.user.dto.UserDto

/**
  * @author im.
  */
object SchemaRegistry {

  val registry = Map(
    UserDto.Role.USER -> UserSchema.schema,
    UserDto.Role.READER -> AdminSchema.schema,
    UserDto.Role.MEMBER -> AdminSchema.schema,
    UserDto.Role.WRITER -> AdminSchema.schema,
    UserDto.Role.EDITOR -> AdminSchema.schema,
    UserDto.Role.CHIEF_EDITOR -> AdminSchema.schema,
    UserDto.Role.ADMIN-> AdminSchema.schema,
    UserDto.Role.API -> AdminSchema.schema
  )
}
