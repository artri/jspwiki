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
package org.apache.wiki.api;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.wiki.api.spi.AclsDSL;
import org.apache.wiki.api.spi.AclsSPI;
import org.apache.wiki.api.spi.ContentsDSL;
import org.apache.wiki.api.spi.ContentsSPI;
import org.apache.wiki.api.spi.ContextDSL;
import org.apache.wiki.api.spi.ContextSPI;
import org.apache.wiki.api.spi.EngineDSL;
import org.apache.wiki.api.spi.EngineSPI;
import org.apache.wiki.api.spi.SessionDSL;
import org.apache.wiki.api.spi.SessionSPI;
import org.apache.wiki.util.PropertyReader;
import org.apache.wiki.util.TextUtil;
import org.apache.wiki.util.Version;
import org.apache.wiki.util.WikiLogger;
import org.apache.wiki.util.Version.VersionFormatException;

import javax.servlet.ServletContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.ProviderNotFoundException;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 *  Contains release and version information.  You may also invoke this class directly, in which case it prints
 *  out the version string.  This is a handy way of checking which JSPWiki version you have - just type from a command line:
 *  <pre>
 *  % java -cp JSPWiki.jar org.apache.wiki.api.Wiki
 *
 */
public class Wiki {
    private static final WikiLogger log = WikiLogger.getLogger(Wiki.class);
    /**
     *  This is the default platform name.
     */
    public static final String PLATFORM_NAME = "JSPWiki";
    public static final Version PLATFORM_VERSION = Wiki.readVersion();

    private static final String PROP_PROVIDER_IMPL_ACLS = "jspwiki.provider.impl.acls";
    private static final String PROP_PROVIDER_IMPL_CONTENTS = "jspwiki.provider.impl.contents";
    private static final String PROP_PROVIDER_IMPL_CONTEXT = "jspwiki.provider.impl.context";
    private static final String PROP_PROVIDER_IMPL_ENGINE = "jspwiki.provider.impl.engine";
    private static final String PROP_PROVIDER_IMPL_SESSION = "jspwiki.provider.impl.session";
    private static final String DEFAULT_PROVIDER_IMPL_ACLS = "org.apache.wiki.spi.AclsSPIDefaultImpl";
    private static final String DEFAULT_PROVIDER_IMPL_CONTENTS = "org.apache.wiki.spi.ContentsSPIDefaultImpl";
    private static final String DEFAULT_PROVIDER_IMPL_CONTEXT = "org.apache.wiki.spi.ContextSPIDefaultImpl";
    private static final String DEFAULT_PROVIDER_IMPL_ENGINE = "org.apache.wiki.spi.EngineSPIDefaultImpl";
    private static final String DEFAULT_PROVIDER_IMPL_SESSION = "org.apache.wiki.spi.SessionSPIDefaultImpl";

    // default values
    private static Properties properties = PropertyReader.getDefaultProperties();
    private static AclsSPI aclsSPI = getSPI( AclsSPI.class, properties, PROP_PROVIDER_IMPL_ACLS, DEFAULT_PROVIDER_IMPL_ACLS );
    private static ContentsSPI contentsSPI = getSPI( ContentsSPI.class, properties, PROP_PROVIDER_IMPL_CONTENTS, DEFAULT_PROVIDER_IMPL_CONTENTS );
    private static ContextSPI contextSPI = getSPI( ContextSPI.class, properties, PROP_PROVIDER_IMPL_CONTEXT, DEFAULT_PROVIDER_IMPL_CONTEXT );
    private static EngineSPI engineSPI = getSPI( EngineSPI.class, properties, PROP_PROVIDER_IMPL_ENGINE, DEFAULT_PROVIDER_IMPL_ENGINE );
    private static SessionSPI sessionSPI = getSPI( SessionSPI.class, properties, PROP_PROVIDER_IMPL_SESSION, DEFAULT_PROVIDER_IMPL_SESSION );

    public static Properties init( final ServletContext context ) {
        properties = PropertyReader.loadWebAppProps( context );
        aclsSPI = getSPI( AclsSPI.class, properties, PROP_PROVIDER_IMPL_ACLS, DEFAULT_PROVIDER_IMPL_ACLS );
        contentsSPI = getSPI( ContentsSPI.class, properties, PROP_PROVIDER_IMPL_CONTENTS, DEFAULT_PROVIDER_IMPL_CONTENTS );
        contextSPI = getSPI( ContextSPI.class, properties, PROP_PROVIDER_IMPL_CONTEXT, DEFAULT_PROVIDER_IMPL_CONTEXT );
        engineSPI = getSPI( EngineSPI.class, properties, PROP_PROVIDER_IMPL_ENGINE, DEFAULT_PROVIDER_IMPL_ENGINE );
        sessionSPI = getSPI( SessionSPI.class, properties, PROP_PROVIDER_IMPL_SESSION, DEFAULT_PROVIDER_IMPL_SESSION );
        return properties;
    }

    /**
     * Access to {@link AclsSPI} operations.
     *
     * @return {@link AclsSPI} operations.
     */
    public static AclsDSL acls() {
        return new AclsDSL( aclsSPI );
    }

    /**
     * Access to {@link ContentsSPI} operations.
     *
     * @return {@link ContentsSPI} operations.
     */
    public static ContentsDSL contents() {
        return new ContentsDSL( contentsSPI );
    }

    /**
     * Access to {@link ContextSPI} operations.
     *
     * @return {@link ContextSPI} operations.
     */
    public static ContextDSL context() {
        return new ContextDSL( contextSPI );
    }

    /**
     * Access to {@link EngineSPI} operations.
     *
     * @return {@link EngineSPI} operations.
     */
    public static EngineDSL engine() {
        return new EngineDSL( engineSPI );
    }

    /**
     * Access to {@link SessionSPI} operations.
     *
     * @return {@link SessionSPI} operations.
     */
    public static SessionDSL session() {
        return new SessionDSL( sessionSPI );
    }

    static < SPI > SPI getSPI( final Class< SPI > spi, final Properties props, final String prop, final String defValue ) {
        final String providerImpl = TextUtil.getStringProperty( props, prop, defValue );
        final ServiceLoader< SPI > loader = ServiceLoader.load( spi );
        for( final SPI provider : loader ) {
            if( providerImpl.equals( provider.getClass().getName() ) ) {
                return provider;
            }
        }
        throw new ProviderNotFoundException( spi.getName() + " provider not found" );
    }

    /**
     *  Returns true, if this version of JSPWiki is newer or equal than what is requested.
     *
     *  @param version A version parameter string (a.b.c-something).
     *  @return A boolean value describing whether the given version is newer than the current JSPWiki.
     *  @since 2.4.57
     *  @throws IllegalArgumentException If the version string could not be parsed.
     */
    public static boolean isNewerOrEqual( final String version ) throws IllegalArgumentException {
        if(StringUtils.isBlank(version)) {
            return true;
        }
        try {
            return Wiki.PLATFORM_VERSION.isGreaterThanOrEqualTo(version);
        } catch (VersionFormatException e) {
            log.error("Found incorrect version: {}", version);
        }

        return true;
    }

    /**
     *  Returns true, if this version of JSPWiki is older or equal than what is requested.
     *
     *  @param version A version parameter string (a.b.c-something)
     *  @return A boolean value describing whether the given version is older than the current JSPWiki version
     *  @since 2.4.57
     *  @throws IllegalArgumentException If the version string could not be parsed.
     */
    public static boolean isOlderOrEqual( final String version ) throws IllegalArgumentException {
        if( StringUtils.isBlank(version) ) {
            return true;
        }

        try {
            return Wiki.PLATFORM_VERSION.isLowerThanOrEqualTo(version);
        } catch (VersionFormatException e) {
            log.error("Found incorrect version: {}", version);
        }
        return false;
    }

    protected static Version readVersion() {
        try {
            return Version.parseVersion(IOUtils.resourceToString("/version.conf", StandardCharsets.UTF_8));
        } catch (IOException ioe) {
            log.error("Failed to read version", ioe);
        }
        return Version.ZERO;
    }

    public static String getPlatformNameString() {
        return Wiki.PLATFORM_NAME;
    }

    /**
     *  This method is useful for templates, because hopefully it will not be inlined, and thus any change to version number does not
     *  need recompiling the pages.
     *
     *  @since 2.1.26.
     *  @return The version string (e.g. 2.5.23).
     */
    public static String getPlatformVersionString() {
        return Wiki.PLATFORM_VERSION.toString();
    }

    /**
     *  Executing this class directly from command line prints out the current version.  It is very useful for
     *  things like different command line tools.
     *  <P>Example:
     *  <PRE>
     *  % java org.apache.wiki.api.Wiki
     *  1.9.26-cvs
     *  </PRE>
     *
     *  @param argv The argument string.  This class takes in no arguments.
     */
    public static void main(final String[] argv ) {
        System.out.println(Wiki.PLATFORM_VERSION.toString());
    }
}
