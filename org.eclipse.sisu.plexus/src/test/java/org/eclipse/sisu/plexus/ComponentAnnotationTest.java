/*******************************************************************************
 * Copyright (c) 2010-present Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stuart McCulloch (Sonatype, Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.sisu.plexus;

import java.util.Arrays;
import java.util.HashSet;

import org.codehaus.plexus.component.annotations.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ComponentAnnotationTest
{
    interface A
    {
    }

    @Component( role = A.class )
    static class DefaultA
        implements A
    {
    }

    @Component( role = A.class, hint = "Named" )
    static class NamedA
        implements A
    {
    }

    @Component( role = A.class, description = "Something" )
    static class DescribedA
        implements A
    {
    }

    @Component( role = A.class, instantiationStrategy = Strategies.PER_LOOKUP )
    static class PrototypeA
        implements A
    {
    }

    @Component( role = A.class, hint = "Named", instantiationStrategy = Strategies.PER_LOOKUP )
    static class NamedPrototypeA
        implements A
    {
    }

    @Component( role = Simple.class )
    static class Simple
    {
    }

    @Component( role = Simple.class, version = "2" )
    static class Simple2
        extends Simple
    {
    }

    @Component( role = Simple.class, isolatedRealm = true )
    static class Simple3
        extends Simple
    {
    }

    @Test
    public void testComponentImpl()
        throws ClassNotFoundException
    {
        checkBehaviour( "DefaultA" );
        checkBehaviour( "NamedA" );
        checkBehaviour( "PrototypeA" );
        checkBehaviour( "NamedPrototypeA" );
        checkBehaviour( "DescribedA" );

        assertNotEquals( replicate( getComponent( "DefaultA" ) ), getComponent( "NamedA" ) );
        assertNotEquals( replicate( getComponent( "DefaultA" ) ), getComponent( "PrototypeA" ) );
        assertNotEquals( replicate( getComponent( "DefaultA" ) ), getComponent( "DescribedA" ) );
        assertNotEquals( replicate( getComponent( "Simple" ) ), getComponent( "DefaultA" ) );
        assertNotEquals( replicate( getComponent( "Simple" ) ), getComponent( "Simple2" ) );
        assertNotEquals( replicate( getComponent( "Simple" ) ), getComponent( "Simple3" ) );
    }

    private static void checkBehaviour( final String name )
        throws ClassNotFoundException
    {
        final Component orig = getComponent( name );
        final Component clone = replicate( orig );

        assertEquals( orig, clone );
        assertEquals( clone, orig );
        assertEquals( clone, clone );
        assertNotEquals( "", clone );

        assertEquals( orig.hashCode(), clone.hashCode() );

        String origToString = orig.toString().replace( "\"", "" ).replace( ".class", "" );
        origToString = origToString.replace( "ComponentAnnotationTest.A", "ComponentAnnotationTest$A" );
        String cloneToString = clone.toString().replace( '[', '{' ).replace( ']', '}' );
        cloneToString = cloneToString.replace( "class ", "" ).replace( "interface ", "" );

        assertEquals( new HashSet<String>( Arrays.asList( origToString.split( "[(, )]" ) ) ),
                      new HashSet<String>( Arrays.asList( cloneToString.split( "[(, )]" ) ) ) );

        assertEquals( orig.annotationType(), clone.annotationType() );
    }

    private static Component getComponent( final String name )
        throws ClassNotFoundException
    {
        return Class.forName( ComponentAnnotationTest.class.getName() + '$' + name ).getAnnotation( Component.class );
    }

    private static Component replicate( final Component orig )
    {
        return new ComponentImpl( orig.role(), orig.hint(), orig.instantiationStrategy(), orig.description() );
    }

    @Test
    public void testNullChecks()
    {
        checkNullNotAllowed( null, "", "", "" );
        checkNullNotAllowed( Object.class, null, "", "" );
        checkNullNotAllowed( Object.class, "", null, "" );
        checkNullNotAllowed( Object.class, "", "", null );
    }

    private static void checkNullNotAllowed( final Class<?> role, final String hint, final String instantationStrategy,
                                             final String description )
    {
        try
        {
            new ComponentImpl( role, hint, instantationStrategy, description );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( final IllegalArgumentException e )
        {
        }
    }
}
