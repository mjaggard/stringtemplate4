/*
 [The "BSD licence"]
 Copyright (c) 2009 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.stringtemplate.test;

import org.junit.Test;
import org.stringtemplate.*;
import org.stringtemplate.misc.ErrorBuffer;
import org.stringtemplate.misc.ErrorManager;

import static org.junit.Assert.assertEquals;

public class TestRegions extends BaseTest {
    @Test public void testEmbeddedRegion() throws Exception {
        String dir = getRandomDir();
        String groupFile =
            "a() ::= <<\n" +
            "[<@r>bar<@end>]\n" +
            ">>\n";
        writeFile(dir, "group.stg", groupFile);
        STGroup group = new STGroupFile(dir+"/group.stg");
        ST st = group.getInstanceOf("a");
        String expected = "[bar]"+newline;
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testRegion() throws Exception {
        String dir = getRandomDir();
        String groupFile =
            "a() ::= <<\n" +
            "[<@r()>]\n" +
            ">>\n";
        writeFile(dir, "group.stg", groupFile);
        STGroup group = new STGroupFile(dir+"/group.stg");
        ST st = group.getInstanceOf("a");
        String expected = "[]"+newline;
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testDefineRegionInSubgroup() throws Exception {
        String dir = getRandomDir();
        String g1 = "a() ::= <<[<@r()>]>>\n";
        writeFile(dir, "g1.stg", g1);
        String g2 = "@a.r() ::= <<foo>>\n";
        writeFile(dir, "g2.stg", g2);

        STGroup group1 = new STGroupFile(dir+"/g1.stg");
        STGroup group2 = new STGroupFile(dir+"/g2.stg");
        group2.importTemplates(group1); // define r in g2
        ST st = group2.getInstanceOf("a");
        String expected = "[foo]";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testDefineRegionInSubgroupThatRefsSuper() throws Exception {
        String dir = getRandomDir();
        String g1 = "a() ::= <<[<@r>foo<@end>]>>\n";
        writeFile(dir, "g1.stg", g1);
        String g2 = "@a.r() ::= <<(<@super.r()>)>>\n";
        writeFile(dir, "g2.stg", g2);

        STGroup group1 = new STGroupFile(dir+"/g1.stg");
        STGroup group2 = new STGroupFile(dir+"/g2.stg");
        group2.importTemplates(group1); // define r in g2
        ST st = group2.getInstanceOf("a");
        String expected = "[(foo)]";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testDefineRegionInSubgroup2() throws Exception {
        String dir = getRandomDir();
        String g1 = "a() ::= <<[<@r()>]>>\n";
        writeFile(dir, "g1.stg", g1);
        String g2 = "@a.r() ::= <<foo>>>\n";
        writeFile(dir, "g2.stg", g2);

        STGroup group1 = new STGroupFile(dir+"/g1.stg");
        STGroup group2 = new STGroupFile(dir+"/g2.stg");
        group1.importTemplates(group2); // opposite of previous; g1 imports g2
        ST st = group1.getInstanceOf("a");
        String expected = "[]"; // @a.r implicitly defined in g1; can't see g2's
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testDefineRegionInSameGroup() throws Exception {
        String dir = getRandomDir();
        String g = "a() ::= <<[<@r()>]>>\n"+
                   "@a.r() ::= <<foo>>\n";
        writeFile(dir, "g.stg", g);

        STGroup group = new STGroupFile(dir+"/g.stg");
        ST st = group.getInstanceOf("a");
        String expected = "[foo]";
        String result = st.render();
        assertEquals(expected, result);
    }

    @Test public void testCantDefineEmbeddedRegionAgain() throws Exception {
        String dir = getRandomDir();
        String g = "a() ::= <<[<@r>foo<@end>]>>\n"+
                   "@a.r() ::= <<bar>>\n"; // error; dup
        writeFile(dir, "g.stg", g);

        STGroupFile group = new STGroupFile(dir+"/g.stg");
        ErrorBuffer errors = new ErrorBuffer();
        ErrorManager.setErrorListener(errors);
        group.load();
        String expected = "g.stg 2:3: region a.r is embedded and thus already implicitly defined"+newline;
        String result = errors.toString();
        assertEquals(expected, result);
    }

}
