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

import org.apache.wiki.util.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import java.util.Objects;
import java.util.Properties;


@ExtendWith( MockitoExtension.class )
public class WikiTest {

    @Mock
    ServletContext sc;

    @Mock
    ServletConfig conf;

    @Test
    public void testWikiInit() {
        Mockito.doReturn( sc ).when( conf ).getServletContext();
        final Properties properties = Wiki.init( sc );
        Assertions.assertEquals( 5, properties.size() );

        // verify SPIs are initialized and can be invoked
        Assertions.assertNull( Wiki.acls().acl() );
        Assertions.assertNull( Wiki.acls().entry() );
        Assertions.assertNull( Wiki.contents().attachment( null, null, null ) );
        Assertions.assertNull( Wiki.contents().page( null, null ) );
        Assertions.assertNull( Wiki.context().create( null, null ) );
        Assertions.assertNull( Wiki.engine().find( conf ) );
        Assertions.assertNull( Wiki.engine().find( conf, properties ) );
        Assertions.assertNull( Wiki.session().find( null, null ) );
        Assertions.assertNull( Wiki.session().guest( null ) );
    }

    @Test
    public void testNewer1() {
        Assertions.assertTrue( Wiki.isNewerOrEqual( "1.0.100" ) );
    }

    @Test
    public void testNewer2() {
        Assertions.assertTrue( Wiki.isNewerOrEqual( "2.0.0-alpha" ) );
    }

    @Test
    public void testNewer3() {
        Assertions.assertFalse( Wiki.isNewerOrEqual( "10.0.0" ) );
    }

    @Test
    public void testNewer4() {
        Assertions.assertTrue( Wiki.isNewerOrEqual( Wiki.getPlatformVersionString() ) );
    }

    @Test
    public void testNewer9() {
        final String rel = Wiki.PLATFORM_VERSION + "";
        Assertions.assertTrue( Wiki.isNewerOrEqual( rel ) );
    }

    @Test
    public void testOlder1() {
        Assertions.assertFalse( Wiki.isOlderOrEqual( "1.0.100" ) );
    }

    @Test
    public void testOlder2() {
        Assertions.assertFalse( Wiki.isOlderOrEqual( "2.0.0-alpha" ) );
    }

    @Test
    public void testOlder3() {
        Assertions.assertTrue( Wiki.isOlderOrEqual( "10.0.0" ) );
    }

    @Test
    public void testOlder4() {
        Assertions.assertTrue( Wiki.isOlderOrEqual( Wiki.getPlatformVersionString() ) );
    }

    @Test
    public void testOlder8() {
        final String rel = Wiki.PLATFORM_VERSION + "";
        Assertions.assertTrue( Wiki.isOlderOrEqual( rel ) );
    }

    @Test
    public void testOlder9() {
        final String rel = "";
        Assertions.assertTrue( Wiki.isOlderOrEqual( rel ) );
    }

    @Test
    public void testReadVersion() {
        Version version = Wiki.readVersion();
        Assertions.assertFalse(Objects.equals(Version.ZERO, version));
    }
}
