package org.zerock.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.domain.AuthVO;
import org.zerock.domain.MemberVO;
import org.zerock.mapper.MemberMapper;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Log4j
@Service
public class MemberServiceImpl implements MemberService {

	@Setter(onMethod_ = @Autowired)
	private MemberMapper memberMapper;

	@Setter(onMethod_ = @Autowired(required = false))
	private PasswordEncoder pwencoder = new BCryptPasswordEncoder();

	@Override
	public boolean isIdDuplicate(String userid) {
		return memberMapper.checkId(userid) > 0;
	}

	@Transactional
	@Override
	public void register(MemberVO member) {
		log.info("회원가입 요청 처리: " + member.getUserid());

		// 1. 비밀번호 BCrypt 암호화
		String encodedPw = pwencoder.encode(member.getUserpw());
		member.setUserpw(encodedPw);

		// 2. tbl_member 테이블에 회원 저장 (userid, userpw, userName)
		memberMapper.insertMember(member);

		// 3. tbl_member_auth 테이블에 기본 권한(ROLE_USER) 부여
		AuthVO auth = new AuthVO();
		auth.setUserid(member.getUserid());
		auth.setAuth("ROLE_USER");
		memberMapper.insertAuth(auth);

		// TODO: [추후 확장] 특정 조건이나 관리자 승인 등에 따라 추가 권한을 부여하려면 아래에 작성
	}

	/*
	 * =====================================================================
	 * [추후 확장 영역] 학교 웹메일 재학생 인증 로직
	 * - 나중에 JavaMailSender 등을 연결하여 인증번호 생성 및 발송 코드를 작성할 수 있습니다.
	 * =====================================================================
	 *
	 * public String sendSchoolEmailCode(String schoolEmail) {
	 *     // 1. 학교 도메인 확인 (예: @mokpo.ac.kr 등)
	 *     // 2. 6자리 난수 인증코드 생성
	 *     // 3. 메일 발송 후 생성된 인증코드 반환 (세션에 저장하여 비교)
	 *     return "123456";
	 * }
	 */

}
