package org.zerock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.domain.MemberVO;
import org.zerock.service.MemberService;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

/**
 * 회원가입 및 회원 인증 관련 요청을 처리하는 컨트롤러
 */
@Controller
@Log4j
public class MemberController {

	@Setter(onMethod_ = @Autowired)
	private MemberService memberService;

	/**
	 * 회원가입 폼 제출 처리 (POST /customSignup)
	 * - 입력값: userid(아이디), userpw(비밀번호), userName(이름)
	 */
	@PostMapping("/customSignup")
	public String signupPost(MemberVO member, RedirectAttributes rttr, Model model) {
		log.info("회원가입 요청: " + member);

		String userid = member.getUserid() != null ? member.getUserid().trim() : "";
		String userpw = member.getUserpw() != null ? member.getUserpw().trim() : "";
		String userName = member.getUserName() != null ? member.getUserName().trim() : "";

		// 1. 기본 입력값 검증
		if (userid.isEmpty() || userpw.isEmpty() || userName.isEmpty()) {
			model.addAttribute("errorMsg", "아이디, 비밀번호, 이름을 모두 입력해주세요.");
			return "customSignup";
		}

		// 2. 아이디 중복 검사
		if (memberService.isIdDuplicate(userid)) {
			model.addAttribute("errorMsg", "이미 사용 중인 아이디입니다.");
			return "customSignup";
		}

		// TODO: [추후 확장] 학교 웹메일 재학생 인증 완료 여부를 세션 등에서 확인하는 코드를 여기에 추가할 수 있습니다.
		// 예: Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");

		// 3. 회원 등록 진행
		member.setUserid(userid);
		member.setUserpw(userpw);
		member.setUserName(userName);
		memberService.register(member);

		rttr.addFlashAttribute("signupMsg", "회원가입이 완료되었습니다. 로그인해주세요.");
		return "redirect:/customLogin";
	}

	/**
	 * 아이디 중복 체크 (AJAX 호출용: GET /member/checkId?userid=...)
	 * - 사용 가능: "available" 반환
	 * - 중복: "duplicate" 반환
	 */
	@GetMapping(value = "/member/checkId", produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String checkId(@RequestParam("userid") String userid) {
		if (userid == null || userid.trim().isEmpty()) {
			return "empty";
		}
		return memberService.isIdDuplicate(userid.trim()) ? "duplicate" : "available";
	}

	/*
	 * =====================================================================
	 * [추후 확장 영역] 학교 웹메일 재학생 인증용 컨트롤러 메서드 예시
	 * =====================================================================
	 *
	 * @PostMapping("/member/sendMail")
	 * @ResponseBody
	 * public String sendAuthMail(@RequestParam("schoolEmail") String schoolEmail, HttpSession session) {
	 *     // 1. 학교 웹메일 도메인 확인 (예: @mokpo.ac.kr)
	 *     // 2. 인증코드 발송 후 세션에 저장: session.setAttribute("authCode", code);
	 *     return "sent";
	 * }
	 *
	 * @PostMapping("/member/verifyMail")
	 * @ResponseBody
	 * public String verifyAuthMail(@RequestParam("code") String inputCode, HttpSession session) {
	 *     // 세션에 저장된 authCode와 inputCode가 일치하면 session.setAttribute("emailVerified", true);
	 *     return "verified";
	 * }
	 */

}
