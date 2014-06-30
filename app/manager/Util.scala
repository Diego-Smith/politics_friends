package manager

import org.joda.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}

/**
 * Created by diego on 27/06/14.
 */
trait Util {
  private val formatterBuilder: DateTimeFormatterBuilder = new DateTimeFormatterBuilder()
  val formatter: DateTimeFormatter =
    formatterBuilder
      .appendDayOfMonth(2)
      .appendLiteral('/')
      .appendMonthOfYear(2)
      .appendLiteral('/')
      .appendYear(4, 4)
      .toFormatter
}
