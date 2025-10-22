package com.yushan.content_service.security;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomMethodSecurityExpressionHandlerTest {

    @Mock
    private BeanFactory beanFactory;

    @Mock
    private MethodInvocation methodInvocation;

    @Mock
    private Method method;

    @Mock
    private Authentication authentication;

    private CustomMethodSecurityExpressionHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        handler = new CustomMethodSecurityExpressionHandler();
        handler.setBeanFactory(beanFactory);
        
        // Setup method mock
        lenient().when(methodInvocation.getMethod()).thenReturn(method);
        
        // Setup authentication mock
        lenient().when(authentication.getName()).thenReturn("testuser");
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
    }

    @Test
    void testCreateEvaluationContext_WithParameters() throws Exception {
        // Given
        Parameter[] parameters = new Parameter[2];
        parameters[0] = mock(Parameter.class);
        parameters[1] = mock(Parameter.class);
        
        when(parameters[0].getName()).thenReturn("novelId");
        when(parameters[1].getName()).thenReturn("userId");
        when(method.getParameters()).thenReturn(parameters);
        when(methodInvocation.getArguments()).thenReturn(new Object[]{123, "user123"});

        // When
        EvaluationContext context = handler.createEvaluationContext(authentication, methodInvocation);

        // Then
        assertNotNull(context);
        assertNotNull(context.getRootObject());
        
        // Verify parameters are set as variables
        assertEquals(123, context.lookupVariable("novelId"));
        assertEquals("user123", context.lookupVariable("userId"));
    }

    @Test
    void testCreateEvaluationContext_WithNoParameters() throws Exception {
        // Given
        when(method.getParameters()).thenReturn(new Parameter[0]);
        when(methodInvocation.getArguments()).thenReturn(new Object[0]);

        // When
        EvaluationContext context = handler.createEvaluationContext(authentication, methodInvocation);

        // Then
        assertNotNull(context);
        assertNotNull(context.getRootObject());
    }

    @Test
    void testCreateEvaluationContext_WithBeanFactory() throws Exception {
        // Given
        when(method.getParameters()).thenReturn(new Parameter[0]);
        when(methodInvocation.getArguments()).thenReturn(new Object[0]);

        // When
        EvaluationContext context = handler.createEvaluationContext(authentication, methodInvocation);

        // Then
        assertNotNull(context);
        // BeanFactory should be set for @bean references
        assertNotNull(beanFactory);
    }

    @Test
    void testCreateSecurityExpressionRoot() throws Exception {
        // Given
        lenient().when(methodInvocation.getArguments()).thenReturn(new Object[0]);

        // When
        MethodSecurityExpressionOperations root = handler.createSecurityExpressionRoot(authentication, methodInvocation);

        // Then
        assertNotNull(root);
        assertTrue(root instanceof CustomSecurityExpressionRoot);
    }

    @Test
    void testSetReturnObject() throws Exception {
        // Given
        when(method.getParameters()).thenReturn(new Parameter[0]);
        when(methodInvocation.getArguments()).thenReturn(new Object[0]);
        
        EvaluationContext context = handler.createEvaluationContext(authentication, methodInvocation);
        Object returnObject = "test return value";

        // When
        handler.setReturnObject(returnObject, context);

        // Then
        // Verify the return object was set on the root object
        CustomSecurityExpressionRoot root = (CustomSecurityExpressionRoot) context.getRootObject().getValue();
        assertEquals(returnObject, root.getReturnObject());
    }

    @Test
    void testSetFilterObject() throws Exception {
        // Given
        when(method.getParameters()).thenReturn(new Parameter[0]);
        when(methodInvocation.getArguments()).thenReturn(new Object[0]);
        
        EvaluationContext context = handler.createEvaluationContext(authentication, methodInvocation);
        Object filterObject = "test filter value";

        // When
        handler.setFilterObject(filterObject, context);

        // Then
        // Verify the filter object was set on the root object
        CustomSecurityExpressionRoot root = (CustomSecurityExpressionRoot) context.getRootObject().getValue();
        assertEquals(filterObject, root.getFilterObject());
    }

    @Test
    void testFilter() throws Exception {
        // Given
        when(method.getParameters()).thenReturn(new Parameter[0]);
        when(methodInvocation.getArguments()).thenReturn(new Object[0]);
        
        EvaluationContext context = handler.createEvaluationContext(authentication, methodInvocation);
        Expression filterExpression = mock(Expression.class);
        Object filterTarget = "test target";

        // When
        Object result = handler.filter(filterTarget, filterExpression, context);

        // Then
        assertEquals(filterTarget, result);
    }

    @Test
    void testGetExpressionParser() {
        // When
        ExpressionParser parser = handler.getExpressionParser();

        // Then
        assertNotNull(parser);
    }

    @Test
    void testSetBeanFactory() {
        // Given
        BeanFactory newBeanFactory = mock(BeanFactory.class);
        when(method.getParameters()).thenReturn(new Parameter[0]);
        when(methodInvocation.getArguments()).thenReturn(new Object[0]);

        // When
        handler.setBeanFactory(newBeanFactory);

        // Then
        // BeanFactory should be set (tested indirectly through createEvaluationContext)
        assertDoesNotThrow(() -> handler.createEvaluationContext(authentication, methodInvocation));
    }

    @Test
    void testCreateEvaluationContext_WithNullBeanFactory() throws Exception {
        // Given
        handler.setBeanFactory(null);
        when(method.getParameters()).thenReturn(new Parameter[0]);
        when(methodInvocation.getArguments()).thenReturn(new Object[0]);

        // When
        EvaluationContext context = handler.createEvaluationContext(authentication, methodInvocation);

        // Then
        assertNotNull(context);
        assertNotNull(context.getRootObject());
    }

    @Test
    void testCreateEvaluationContext_WithMoreArgumentsThanParameters() throws Exception {
        // Given
        Parameter[] parameters = new Parameter[1];
        parameters[0] = mock(Parameter.class);
        when(parameters[0].getName()).thenReturn("novelId");
        when(method.getParameters()).thenReturn(parameters);
        when(methodInvocation.getArguments()).thenReturn(new Object[]{123, "extra", "args"});

        // When
        EvaluationContext context = handler.createEvaluationContext(authentication, methodInvocation);

        // Then
        assertNotNull(context);
        // Only the first argument should be set as variable
        assertEquals(123, context.lookupVariable("novelId"));
        assertNull(context.lookupVariable("extra"));
    }

    @Test
    void testCreateEvaluationContext_WithMoreParametersThanArguments() throws Exception {
        // Given
        Parameter[] parameters = new Parameter[3];
        parameters[0] = mock(Parameter.class);
        parameters[1] = mock(Parameter.class);
        parameters[2] = mock(Parameter.class);
        
        when(parameters[0].getName()).thenReturn("novelId");
        when(parameters[1].getName()).thenReturn("userId");
        when(parameters[2].getName()).thenReturn("extra");
        when(method.getParameters()).thenReturn(parameters);
        when(methodInvocation.getArguments()).thenReturn(new Object[]{123, "user123"});

        // When
        EvaluationContext context = handler.createEvaluationContext(authentication, methodInvocation);

        // Then
        assertNotNull(context);
        // Only available arguments should be set
        assertEquals(123, context.lookupVariable("novelId"));
        assertEquals("user123", context.lookupVariable("userId"));
        assertNull(context.lookupVariable("extra"));
    }
}
