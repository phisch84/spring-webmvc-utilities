package com.schoste.ddd.application.v1.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import com.schoste.ddd.application.v1.annotations.ExampleClassAnnotation;
import com.schoste.ddd.application.v1.annotations.ExampleMethodAnnotation;

/**
 * Example controller to test the {@link com.schoste.ddd.application.v1.interceptor.AnnotationInterceptor}
 * 
 * @author Philipp Schosteritsch <s.philipp@schoste.com>
 */
@Controller
@ExampleClassAnnotation(exampleProperty = "Hello class")
public class TestController
{
	@GetMapping({ "/" })
	@ExampleMethodAnnotation(exampleProperty = "Hello method")
	public ModelAndView index(HttpServletRequest request) throws Exception
	{
		ModelAndView homeView = new ModelAndView("index");
		
		return homeView;
	}
}
