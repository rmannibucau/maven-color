package org.slf4j.impl; /**
 * Copyright (c) 2004-2011 QOS.ch
 * All rights reserved.
 * <p>
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * <p>
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import org.fusesource.jansi.AnsiConsole;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

public class StaticLoggerBinder implements LoggerFactoryBinder {
    static {
        try {
            AnsiConsole.systemUninstall();
        } catch (final Throwable t) {
            System.out.println("> " + t.getMessage());
        }
    }
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    public static final StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    public static String REQUESTED_API_VERSION = "1.6.99";  // !final

    private static final String loggerFactoryClassStr = SimpleLoggerFactory.class.getName();

    private final ILoggerFactory loggerFactory;

    private StaticLoggerBinder() {
        loggerFactory = new SimpleLoggerFactory();
    }

    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    public String getLoggerFactoryClassStr() {
        return loggerFactoryClassStr;
    }
}
