package models.graphql.dto

import models.graphql.GraphQLContext
import sangria.schema._
import today.expresso.grpc.user.dto.UserDto

/**
  * @author im.
  */
object UserDtoSchema {
  val UserDtoRoleEnum = EnumType(
    "Role",
    Some(
      "User role is used for authorisation; " +
        "user can have several roles at the same time"),
    List(
      EnumValue(name = "USER",
        value = UserDto.Role.USER,
        description = Some("Basic permissions")
      ),
      EnumValue(name = "READER",
        value = UserDto.Role.READER,
        description = Some("Newsletter subscriber")
      ),
      EnumValue(name = "MEMBER",
        value = UserDto.Role.MEMBER,
        description = Some("Payed newsletter subscriber")
      ),
      EnumValue(name = "WRITER",
        value = UserDto.Role.WRITER,
        description = Some("Allowed to create and write newsletters")
      ),
      EnumValue(name = "EDITOR",
        value = UserDto.Role.EDITOR,
        description = Some("Allowed suspend newsletters, edit any posts")),
      EnumValue(name = "CHIEF_EDITOR",
        value = UserDto.Role.CHIEF_EDITOR,
        description = Some("Superuser!")
      ),
      EnumValue(name = "ADMIN",
        value = UserDto.Role.ADMIN,
        description = Some("Administrative permissions")
      ),
      EnumValue(name = "API",
        value = UserDto.Role.API,
        description = Some("Allowed to open API documentation")
      )
    )
  )

  val UserDtoStatusEnum = EnumType(
    "Status",
    Some("Is client allowed to perform any actions"),
    List(
      EnumValue(name = "NEW",
        value = UserDto.Status.NEW,
        description = Some("unverified user")
      ),
      EnumValue(name = "VERIFIED",
        value = UserDto.Status.VERIFIED,
        description = Some("verified user")
      ),
      EnumValue(name = "BLOCKED",
        value = UserDto.Status.BLOCKED,
        description = Some("blocked user, user reason field contains the reason")
      )
    )
  )


  val UserDtoType = ObjectType("User", "A main entity who performs actions in the system", fields[GraphQLContext, UserDto](
    Field("id", LongType, resolve = _.value.id),
    Field("roles", ListType(UserDtoRoleEnum), resolve = _.value.roles.toList),
    Field("status", UserDtoStatusEnum, resolve = _.value.status),
    Field("locale", StringType, resolve = _.value.locale),
    Field("timezone", IntType, resolve = _.value.timezone, deprecationReason = Some("Will not be exposed in future")),
    Field("reason", OptionType(StringType), resolve = a => if (a.value.reason.isEmpty) None else Some(a.value.reason)),
    Field("createdTimestamp", LongType, resolve = _.value.createdTimestamp)
  ))

//  implicit val userDtoPrimaryKey: HasId[UserDto, Long] = HasId[UserDto, Long](_.id)
//  //TODO: create call with seq
//  private def userGetById(c: GraphQLContext, idSeq: Seq[Long]) = {
//    c.services.userService.getById(idSeq.head)(c.creds).map(Seq(_))
//  }
//  val userByIdFetcher = Fetcher(userGetById)

  val ID = Argument("id", LongType, description = "id of the user")

  val Query = ObjectType[GraphQLContext, Unit](
    "Query", fields[GraphQLContext, Unit](
      Field("me", OptionType(UserDtoType),
        resolve = ctx => ctx.ctx.services.userService.getById(ctx.ctx.creds.user.id)(ctx.ctx.creds)),
      Field("user", OptionType(UserDtoType),
        arguments = ID :: Nil,
        resolve = ctx => ctx.ctx.services.userService.getById(ctx.arg(ID))(ctx.ctx.creds))
    ))

  val UserDtoSchema = Schema(Query)
}
