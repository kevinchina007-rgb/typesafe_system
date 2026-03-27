package org.slf4j.impl

import org.slf4j.ILoggerFactory
import org.slf4j.helpers.NOPLoggerFactory
import org.slf4j.spi.LoggerFactoryBinder

final class StaticLoggerBinder private () extends LoggerFactoryBinder:
  override def getLoggerFactory: ILoggerFactory = StaticLoggerBinder.loggerFactory

  override def getLoggerFactoryClassStr: String = classOf[NOPLoggerFactory].getName

object StaticLoggerBinder:
  private val singletonInstance = new StaticLoggerBinder()
  private val loggerFactory = new NOPLoggerFactory()

  def getSingleton: StaticLoggerBinder = singletonInstance

