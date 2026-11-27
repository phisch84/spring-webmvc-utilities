package com.schoste.ddd.application.v2.examples;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.schoste.ddd.application.v2.interceptor.GenericAnnotationPreHandler;

/**
 * Example handler of the README: rejects requests of users who don't have the role named by {@link RequiresRole}
 */
public class RequiresRoleHandler extends GenericAnnotationPreHandler<RequiresRole>
{
    @Override
    protected boolean handle(RequiresRole what, HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        if (request.isUserInRole(what.value())) return true;

        response.sendError(HttpServletResponse.SC_FORBIDDEN);

        return false;
    }
}
