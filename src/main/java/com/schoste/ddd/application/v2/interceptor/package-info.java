/**
 * Handler interceptor which executes handlers for custom annotations of controllers and their methods.
 * <p>
 * A project adds its own annotations in three steps, without changing this library:
 * <ol>
 * <li>define the annotation (with runtime retention), with any attributes it needs;</li>
 * <li>write a handler: extend {@link com.schoste.ddd.application.v2.interceptor.GenericAnnotationPreHandler} (executed before
 * the request is processed) or {@link com.schoste.ddd.application.v2.interceptor.GenericAnnotationPostHandler} (executed
 * after it was processed) with the annotation as type argument. The handler gets the annotation of the controller (method),
 * so it can read its attributes;</li>
 * <li>register the handler as singleton bean, with its priority, and the interceptor once.</li>
 * </ol>
 * An annotation may have several pre and post handlers. Example of an annotation {@code RequiresRole} with a handler which
 * rejects requests of users who don't have the role named by the annotation:
 * <pre>
 * &#64;Retention(RetentionPolicy.RUNTIME)
 * &#64;Target({ ElementType.TYPE, ElementType.METHOD })
 * public &#64;interface RequiresRole
 * {
 *     String value();
 * }
 *
 * public class RequiresRoleHandler extends GenericAnnotationPreHandler&lt;RequiresRole&gt;
 * {
 *     &#64;Override
 *     protected boolean handle(RequiresRole what, HttpServletRequest request, HttpServletResponse response) throws Exception
 *     {
 *         if (request.isUserInRole(what.value())) return true;
 *
 *         response.sendError(HttpServletResponse.SC_FORBIDDEN);
 *
 *         return false;
 *     }
 * }
 * </pre>
 * The handler and the interceptor are registered in the Spring MVC configuration (any bean id, or e.g. component scanning):
 * <pre>
 * &lt;bean id="RequiresRoleHandler" class="RequiresRoleHandler" scope="singleton"&gt;
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
 * Now every controller (method) annotated with e.g. {@code @RequiresRole("admin")} is only processed for users with this role.
 * <p>
 * Configuration errors aren't ignored: if the annotation of a handler can't be determined (its class leaves the type
 * argument open) or the interceptor wasn't created by Spring, requests fail with an {@link java.lang.IllegalStateException}.
 */
package com.schoste.ddd.application.v2.interceptor;
