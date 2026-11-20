package org.zerock.controller;

import javax.servlet.http.HttpSession;

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

	// 대학교 공식 웹메일 도메인(.ac.kr 또는 .edu) 검증용 정규식
	private static final String UNIV_EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.(ac\\.kr|edu)$";

	@Setter(onMethod_ = @Autowired)
	private MemberService memberService;

	/**
	 * 회원가입 폼 제출 처리 (POST /customSignup)
	 * - 입력값: userid(아이디), userpw(비밀번호), userName(이름)
	 */
	@PostMapping("/customSignup")
	public String signupPost(MemberVO member, HttpSession session, RedirectAttributes rttr, Model model) {
		log.info("회원가입 요청: " + member);

		String userid = member.getUserid() != null ? member.getUserid().trim() : "";
		String userpw = member.getUserpw() != null ? member.getUserpw().trim() : "";
		String userName = member.getUserName() != null ? member.getUserName().trim() : "";

		// 1. 기본 입력값 검증
		if (userid.isEmpty() || userpw.isEmpty() || userName.isEmpty()) {
			model.addAttribute("errorMsg", "아이디, 비밀번호, 이름을 모두 입력해주세요.");
			return "customSignup";
		}

		// 2. 비밀번호 유효성 검사 (신규 가입 시에만 8자리 이상 및 특수문자 1개 이상 포함 확인)
		if (userpw.length() < 8 || !userpw.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?~`].*")) {
			model.addAttribute("errorMsg", "비밀번호는 8자리 이상이어야 하며, 특수문자를 최소 1개 포함해야 합니다.");
			return "customSignup";
		}

		// 3. 아이디 중복 검사
		if (memberService.isIdDuplicate(userid)) {
			model.addAttribute("errorMsg", "이미 사용 중인 아이디입니다.");
			return "customSignup";
		}

		// 4. 학교 웹메일 재학생 인증 완료 여부 확인
		Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
		if (emailVerified == null || !emailVerified) {
			model.addAttribute("errorMsg", "학교 웹메일 인증을 완료해주세요.");
			return "customSignup";
		}

		// 5. 회원 등록 진행
		member.setUserid(userid);
		member.setUserpw(userpw);
		member.setUserName(userName);
		memberService.register(member);

		// 가입 완료 후 세션 내 이메일 인증 정보 초기화
		session.removeAttribute("authCode");
		session.removeAttribute("emailVerified");

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

	/**
	 * 학교 웹메일 인증번호 발송 (AJAX 호출용: POST /member/sendMail)
	 * - 대학교 도메인(.ac.kr 또는 .edu)인지 확인 후 6자리 인증번호 발급 및 세션 저장
	 */
	@PostMapping(value = "/member/sendMail", produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String sendAuthMail(@RequestParam("schoolEmail") String schoolEmail, HttpSession session) {
		if (schoolEmail == null || schoolEmail.trim().isEmpty()) {
			return "empty";
		}

		String email = schoolEmail.trim();
		if (!email.matches(UNIV_EMAIL_REGEX)) {
			return "invalid_domain";
		}

		try {
			String code = memberService.sendSchoolEmailCode(email);
			session.setAttribute("authCode", code);
			session.setAttribute("emailVerified", false);
			return "sent";
		} catch (Exception e) {
			log.error("학교 웹메일 인증번호 발송 실패: " + e.getMessage(), e);
			return "mail_error";
		}
	}

	/**
	 * 학교 웹메일 인증번호 확인 (AJAX 호출용: POST /member/verifyMail)
	 * - 세션에 저장된 authCode와 사용자가 입력한 code가 일치하면 emailVerified = true 처리
	 */
	@PostMapping(value = "/member/verifyMail", produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String verifyAuthMail(@RequestParam("code") String inputCode, HttpSession session) {
		String savedCode = (String) session.getAttribute("authCode");
		if (savedCode == null) {
			return "no_code";
		}
		if (memberService.verifySchoolEmailCode(inputCode, savedCode)) {
			session.setAttribute("emailVerified", true);
			return "verified";
		}
		return "mismatch";
	}

}
