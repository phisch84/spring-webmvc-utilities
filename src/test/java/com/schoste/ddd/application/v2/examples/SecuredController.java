package com.schoste.ddd.application.v2.examples;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controller secured by {@link RequiresRole}: every method needs the role "user" (class annotation), {@link #admin()} needs
 * "admin" instead (the method's annotation is the one its handlers get). It counts its calls, so tests can check that a
 * rejected request never reaches it.
 *
 * @author Philipp Schosteritsch &lt;s.philipp@schoste.com&gt;
 */
@Controller
@RequiresRole("user")
public class SecuredController
{
	protected final AtomicInteger calls = new AtomicInteger();

	public int getCalls()
	{
		return this.calls.get();
	}

	@GetMapping("/secured/home")
	@ResponseBody
	public String home()
	{
		this.calls.incrementAndGet();

		return "home";
	}

	@GetMapping("/secured/admin")
	@ResponseBody
	@RequiresRole("admin")
	public String admin()
	{
		this.calls.incrementAndGet();

		return "admin";
	}
}
