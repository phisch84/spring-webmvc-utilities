/**
 * Handler interceptor which executes handlers for custom annotations of controllers and their methods.
 * <p>
 * Example of an annotation {@code RequiresLogin} with a handler which rejects requests of users who aren't logged in:
 * <pre>
 * &#64;Retention(RetentionPolicy.RUNTIME)
 * &#64;Target({ ElementType.TYPE, ElementType.METHOD })
 * public &#64;interface RequiresLogin
 * {
 * }
 *
 * public class RequiresLoginHandler extends GenericAnnotationPreHandler&lt;RequiresLogin&gt; implements RequiresLogin
 * {
 *     &#64;Override
 *     protected boolean handle(RequiresLogin what, HttpServletRequest request, HttpServletResponse response) throws Exception
 *     {
 *         if (request.getSession().getAttribute("user") != null) return true;
 *
 *         response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
 *
 *         return false;
 *     }
 * }
 * </pre>
 * The handler implements the annotation's interface, so that the interceptor finds it as the bean of the annotation type.
 * The handler and the interceptor are registered in the Spring MVC configuration:
 * <pre>
 * &lt;bean id="RequiresLogin" class="RequiresLoginHandler" scope="singleton"&gt;
 *     &lt;property name="priority" value="0" /&gt;
 * &lt;/bean&gt;
 *
 * &lt;mvc:interceptors&gt;
 *     &lt;mvc:interceptor&gt;
 *         &lt;mvc:mapping path="/**" /&gt;
 *         &lt;bean class="com.schoste.ddd.application.v2.interceptor.AnnotationInterceptor" /&gt;
 *     &lt;/mvc:interceptor&gt;
 * &lt;/mvc:interceptors&gt;
 * </pre>
 * Now every controller (method) annotated with {@code @RequiresLogin} is only processed for logged in users.
 */
package com.schoste.ddd.application.v2.interceptor;
