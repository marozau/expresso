package exceptions

import com.mohiva.play.silhouette.api.LoginInfo

/**
  * @author im.
  */
case class UserNotFoundException(id: Option[Long], loginInfo: Option[LoginInfo], message: String) extends BaseException {
  override def code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.USER_NOT_FOUND
}
