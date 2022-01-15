/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.    
 */
package org.apache.wiki.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides a wrapper around the <a href="http://www.slf4j.org">SFL4J</a>
 * logging facade framework. A logging framework can be chosen at runtime.
 */
public class WikiLogger {

    private final Logger logger;

    /**
     *
     */
    private WikiLogger(Logger logger) {
        this.logger = logger;
    }

    /**
     * Retrieve a named <code>WikiLogger</code> object.
     *
     * @param name The name of the log object to retrieve or create.
     * @return A logger instance for the given name.
     */
    public static WikiLogger getLogger(String name) {
        Logger logger = LoggerFactory.getLogger(name);
        return new WikiLogger(logger);
    }

    /**
     * Retrieve a named <code>WikiLogger</code> object.
     *
     * @param name The name of the log object to retrieve or create.
     * @return A logger instance for the given name.
     */
    public static WikiLogger getLogger(Class<?> clazz) {
        Logger logger = LoggerFactory.getLogger(clazz);
        return new WikiLogger(logger);
    }

    /**
     * Is the logger instance enabled for the DEBUG level?
     *
     * @return True if this Logger is enabled for the DEBUG level,
     *         false otherwise.
     */
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }

    /**
     * Log a message at the DEBUG level.
     *
     * @param msg the message string to be logged
     */
    public void debug(String msg) {
        this.logger.debug(msg);
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and argument.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     */
    public void debug(String format, Object arg) {
        this.logger.debug(format, arg);
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the DEBUG level. </p>
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public void debug(String format, Object arg1, Object arg2) {
        this.logger.debug(format, arg1, arg2);
    }

    /**
     * Log a message at the DEBUG level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the DEBUG level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for DEBUG. The variants taking
     * {@link #debug(String, Object) one} and {@link #debug(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    public void debug(String format, Object... arguments) {
        this.logger.debug(format, arguments);
    }

    /**
     * Log a message and an exception at the {@link org.slf4j.Logger#DEBUG}
     * level, provided that the current log level is {@link org.slf4j.Logger#DEBUG}
     * or greater.
     *
     * @param msg The message to be written to the log.
     * @param thrown An exception to be written to the log.
     */
    public void debug(String msg, Throwable thrown) {
        this.logger.debug(msg, thrown);
    }

    /**
     * Is the logger instance enabled for the INFO level?
     *
     * @return True if this Logger is enabled for the INFO level,
     *         false otherwise.
     */
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }

    /**
     * Log a message at the INFO level.
     *
     * @param msg the message string to be logged
     */
    public void info(String msg) {
        this.logger.info(msg);
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and argument.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the INFO level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     */
    public void info(String format, Object arg) {
        this.logger.info(format, arg);
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the INFO level. </p>
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public void info(String format, Object arg1, Object arg2) {
        this.logger.info(format, arg1, arg2);
    }

    /**
     * Log a message at the INFO level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the INFO level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for INFO. The variants taking
     * {@link #info(String, Object) one} and {@link #info(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    public void info(String format, Object... arguments) {
        this.logger.info(format, arguments);
    }

    /**
     * Log an exception (throwable) at the INFO level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param thrown   the exception (throwable) to log
     */
    public void info(String msg, Throwable thrown) {
        this.logger.info(msg, thrown);
    }

    /**
     * Is the logger instance enabled for the WARN level?
     *
     * @return True if this Logger is enabled for the WARN level,
     *         false otherwise.
     */
    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }

    /**
     * Log a message at the WARN level.
     *
     * @param msg the message string to be logged
     */
    public void warn(String msg) {
        this.logger.warn(msg);
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and argument.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the WARN level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     */
    public void warn(String format, Object arg) {
        this.logger.warn(format, arg);
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the WARN level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for WARN. The variants taking
     * {@link #warn(String, Object) one} and {@link #warn(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    public void warn(String format, Object... arguments) {
        this.logger.warn(format, arguments);
    }

    /**
     * Log a message at the WARN level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the WARN level. </p>
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public void warn(String format, Object arg1, Object arg2){
        this.logger.warn(format, arg1, arg2);
    }

    /**
     * Log an exception (throwable) at the WARN level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    public void warn(String msg, Throwable t) {
        this.logger.warn(msg, t);
    }

    /**
     * Is the logger instance enabled for the ERROR level?
     *
     * @return True if this Logger is enabled for the ERROR level,
     *         false otherwise.
     */
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }

    /**
     * Log a message at the ERROR level.
     *
     * @param msg the message string to be logged
     */
    public void error(String msg) {
        this.logger.error(msg);
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and argument.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level. </p>
     *
     * @param format the format string
     * @param arg    the argument
     */
    public void error(String format, Object arg) {
        this.logger.error(format, arg);
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous object creation when the logger
     * is disabled for the ERROR level. </p>
     *
     * @param format the format string
     * @param arg1   the first argument
     * @param arg2   the second argument
     */
    public void error(String format, Object arg1, Object arg2){
        this.logger.error(format, arg1, arg2);
    }

    /**
     * Log a message at the ERROR level according to the specified format
     * and arguments.
     * <p/>
     * <p>This form avoids superfluous string concatenation when the logger
     * is disabled for the ERROR level. However, this variant incurs the hidden
     * (and relatively small) cost of creating an <code>Object[]</code> before invoking the method,
     * even if this logger is disabled for ERROR. The variants taking
     * {@link #error(String, Object) one} and {@link #error(String, Object, Object) two}
     * arguments exist solely in order to avoid this hidden cost.</p>
     *
     * @param format    the format string
     * @param arguments a list of 3 or more arguments
     */
    public void error(String format, Object... arguments){
        this.logger.error(format, arguments);
    }

    /**
     * Log an exception (throwable) at the ERROR level with an
     * accompanying message.
     *
     * @param msg the message accompanying the exception
     * @param t   the exception (throwable) to log
     */
    public void error(String msg, Throwable thrown) {
        this.logger.error(msg, thrown);
    }

    /**
     * Return <code>true</code> if a log message of level {@link org.slf4j.Logger#TRACE}
     * can be logged.
     */
    public boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }

    /**
     * Log a message at the {@link org.slf4j.Logger#TRACE} level,
     * provided that the current log level is {@link org.slf4j.Logger#TRACE}
     * or greater.
     *
     * @param msg The message to be written to the log.
     */
    public void trace(String msg) {
        this.logger.trace(msg);
    }

    /**
     * Log a message and an exception at the {@link org.slf4j.Logger#TRACE}
     * level, provided that the current log level is {@link org.slf4j.Logger#TRACE}
     * or greater.
     *
     * @param msg The message to be written to the log.
     * @param thrown An exception to be written to the log.
     */
    public void trace(String msg, Throwable thrown) {
        this.logger.trace(msg, thrown);
    }
}
