package exceptions

/**
  * @author im.
  */
case class FrameNotFoundException(id: Long, message: String) extends BaseException {
  override def code: _root_.exceptions.BaseException.ErrorCode.Value = BaseException.ErrorCode.FRAME_NOT_FOUND
}
