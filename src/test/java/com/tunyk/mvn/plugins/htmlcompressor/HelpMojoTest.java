/*
 * SPDX-License-Identifier: Apache-2.0
 * See LICENSE file for details.
 *
 * Copyright 2018-2026 Hazendaz
 * Copyright 2011-2018 tunyk
 */
package com.tunyk.mvn.plugins.htmlcompressor;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Tests for generated plugin help mojo utilities.
 */
class HelpMojoTest {

    /** The generated help mojo class name. */
    private static final String HELP_MOJO_CLASS = "com.github.hazendaz.maven.htmlcompressor_maven_plugin.HelpMojo";

    @Test
    void testExecuteNormalizesInvalidFormattingParameters() throws Exception {
        Mojo mojo = (Mojo) newHelpMojo();
        setField(mojo, "lineLength", -1);
        setField(mojo, "indentSize", 0);
        setField(mojo, "detail", true);

        Assertions.assertDoesNotThrow(mojo::execute);
        Assertions.assertEquals(80, getField(mojo, "lineLength"));
        Assertions.assertEquals(2, getField(mojo, "indentSize"));
    }

    @Test
    void testGetPropertyFromExpressionVariants() throws Exception {
        Assertions.assertEquals("user.prop",
                invokeStatic("getPropertyFromExpression", new Class<?>[] { String.class }, "${user.prop}"));
        Assertions.assertNull(invokeStatic("getPropertyFromExpression", new Class<?>[] { String.class }, "user.prop"));
        Assertions.assertNull(
                invokeStatic("getPropertyFromExpression", new Class<?>[] { String.class }, "${outer${inner}}"));
        Assertions
                .assertNull(invokeStatic("getPropertyFromExpression", new Class<?>[] { String.class }, (Object) null));
    }

    @Test
    void testNodeLookupUtilityBranches() throws Exception {
        Document doc = parse("<root><item>first</item><item>second</item></root>");
        Node root = doc.getDocumentElement();

        Assertions
                .assertNull(invokeStatic("findSingleChild", new Class<?>[] { Node.class, String.class }, root, "none"));

        ReflectiveOperationException duplicateSingleChild = Assertions.assertThrows(ReflectiveOperationException.class,
                () -> invokeStatic("findSingleChild", new Class<?>[] { Node.class, String.class }, root, "item"));
        Assertions.assertInstanceOf(MojoExecutionException.class, duplicateSingleChild.getCause());

        ReflectiveOperationException missingGetSingleChild = Assertions.assertThrows(ReflectiveOperationException.class,
                () -> invokeStatic("getSingleChild", new Class<?>[] { Node.class, String.class }, root, "missing"));
        Assertions.assertInstanceOf(MojoExecutionException.class, missingGetSingleChild.getCause());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testTextFormattingHelpers() throws Exception {
        List<String> lines = (List<String>) invokeStatic("toLines",
                new Class<?>[] { String.class, int.class, int.class, int.class },
                "\talpha beta gamma\u00A0delta epsilon", 1, 2, 14);

        Assertions.assertTrue(lines.size() > 1, "Long text should wrap to multiple lines");
        Assertions.assertTrue(lines.stream().noneMatch(line -> line.contains("\u00A0")),
                "Non-breaking spaces should be normalized");
        Assertions.assertEquals(2, invokeStatic("getIndentLevel", new Class<?>[] { String.class }, "\t  \tvalue"));
    }

    @Test
    void testWriteParameterIncludesMetadata() throws Exception {
        Object mojo = newHelpMojo();
        setField(mojo, "lineLength", 40);
        setField(mojo, "indentSize", 2);

        Document doc = parse(
                "<mojo>" + "<configuration><sample default-value=\"42\">${sample.property}</sample></configuration>"
                        + "<parameter><name>sample</name><description>parameter description</description>"
                        + "<required>true</required><deprecated>use replacement</deprecated></parameter>" + "</mojo>");

        StringBuilder sb = new StringBuilder();
        Node parameter = doc.getElementsByTagName("parameter").item(0);
        Node configuration = doc.getElementsByTagName("configuration").item(0);
        invokeInstance(mojo, "writeParameter", new Class<?>[] { StringBuilder.class, Node.class, Node.class }, sb,
                parameter, configuration);

        String output = sb.toString();
        Assertions.assertTrue(output.contains("sample (Default: 42)"));
        Assertions.assertTrue(output.contains("Deprecated. use replacement"));
        Assertions.assertTrue(output.contains("Required: Yes"));
        Assertions.assertTrue(output.contains("User property: sample.property"));
        Assertions.assertTrue(output.contains("parameter description"));
    }

    private static Object newHelpMojo() throws ReflectiveOperationException {
        return helpMojoClass().getDeclaredConstructor().newInstance();
    }

    private static Class<?> helpMojoClass() throws ClassNotFoundException {
        return Class.forName(HELP_MOJO_CLASS);
    }

    private static void setField(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object getField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(target);
    }

    private static Object invokeStatic(String methodName, Class<?>[] parameterTypes, Object... args)
            throws ReflectiveOperationException {
        Method method = helpMojoClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        try {
            return method.invoke(null, args);
        } catch (InvocationTargetException e) {
            throw new ReflectiveOperationException(e.getCause());
        }
    }

    private static Object invokeInstance(Object target, String methodName, Class<?>[] parameterTypes, Object... args)
            throws ReflectiveOperationException {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw new ReflectiveOperationException(e.getCause());
        }
    }

    private static Document parse(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }
}
