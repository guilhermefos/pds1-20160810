/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.jasper.compiler;

import java.util.Hashtable;
import java.util.Stack;

import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.VariableInfo;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.BodyTag;


/**
 * Custom tag support.
 *
 * @author Anil K. Vijendran
 */
public class TagEndGenerator
    extends TagGeneratorBase
    implements ServiceMethodPhase
{
    String prefix, shortTagName;
    TagLibraryInfo tli;
    TagInfo ti;
    Hashtable attrs;
    TagLibraries libraries;

    public TagEndGenerator(String prefix, String shortTagName,
                           Hashtable attrs, TagLibraryInfo tli,
                           TagInfo ti, TagLibraries libraries,
                           Stack tagHandlerStack, Hashtable tagVarNumbers)
    {
        setTagHandlerStack(tagHandlerStack);
        setTagVarNumbers(tagVarNumbers);
        this.prefix = prefix;
        this.shortTagName = shortTagName;
        this.tli = tli;
        this.ti = ti;
        this.attrs = attrs;
	this.libraries = libraries;
    }

    public void generate(ServletWriter writer, Class phase) {
        TagVariableData tvd = tagEnd();
        String thVarName = tvd.tagHandlerInstanceName;
        String evalVarName = tvd.tagEvalVarName;

        VariableInfo[] vi = ti.getVariableInfo(new TagData(attrs));

        Class tagHandlerClass =
	    libraries.getTagCache(prefix, shortTagName).getTagHandlerClass();
        boolean implementsBodyTag = BodyTag.class.isAssignableFrom(tagHandlerClass);

	writer.popIndent();

        if (implementsBodyTag)
            writer.println("} while ("+thVarName+".doAfterBody() == BodyTag.EVAL_BODY_TAG);");
        else
            writer.println("} while (false);");

        declareVariables(writer, vi, false, true, VariableInfo.AT_BEGIN);

        if (implementsBodyTag) {
            writer.popIndent(); // try

            /** FIXME: REMOVE BEGIN */
            //              writer.println("} catch (Throwable t) {");
            //              writer.pushIndent();

            //              writer.println("System.err.println(\"Caught: \");");
            //              writer.println("t.printStackTrace();");

            //              writer.popIndent();
            /** FIXME: REMOVE END */

            writer.println("} finally {");
            writer.pushIndent();
            writer.println("if ("+evalVarName+" != Tag.EVAL_BODY_INCLUDE)");
            writer.pushIndent();
            writer.println("out = pageContext.popBody();");
            writer.popIndent();

            writer.popIndent();
            writer.println("}");
        }

	writer.popIndent(); // EVAL_BODY
	writer.println("}");

	writer.println("if ("+thVarName+".doEndTag() == Tag.SKIP_PAGE)");
	writer.pushIndent(); writer.println("return;"); writer.popIndent();

	writer.popIndent(); // try

        /** FIXME: REMOVE BEGIN */
        //          writer.println("} catch (Throwable t) {");
        //          writer.pushIndent();

        //          writer.println("System.err.println(\"Caught: \");");
        //          writer.println("t.printStackTrace();");
        //          writer.popIndent();
        /** FIXME: REMOVE END */

	writer.println("} finally {");
	writer.pushIndent();
	writer.println(thVarName+".release();");
	writer.popIndent();
	writer.println("}");

        // Need to declare and update AT_END variables here.
        declareVariables(writer, vi, true, true, VariableInfo.AT_END);
    }
}
