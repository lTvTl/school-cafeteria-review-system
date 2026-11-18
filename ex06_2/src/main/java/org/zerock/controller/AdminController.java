package org.zerock.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zerock.listener.SessionListener;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {
	@GetMapping
	public String admin(HttpServletRequest request, HttpServletResponse response, Model model) {
		model.addAllAttributes(status(request, response));
		return "admin";
	}

	@GetMapping("/status")
	@ResponseBody
	public Map<String, Object> status(HttpServletRequest request, HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-store");
		request.getSession();
		Runtime runtime = Runtime.getRuntime();
		double memoryUsage = 100.0 * (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory();
		Map<String, Object> status = new LinkedHashMap<>();
		status.put("serverStatus", memoryUsage >= 90.0 ? "BUSY" : "NORMAL");
		status.put("memoryUsage", memoryUsage);
		status.put("activeUserCnt", SessionListener.getActiveUserCnt(request.getServletContext()));
		return status;
	}
}
