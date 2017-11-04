package exceptions

import models.UserStatus

/**
  * @author im.
  */
case class InvalidUserStatusException(userId: Long, status: UserStatus.Value, message: String) extends BaseException {
  override def code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.INVALID_USER_STATUS
}
