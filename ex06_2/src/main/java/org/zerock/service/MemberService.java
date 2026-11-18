package org.zerock.service;

import org.zerock.domain.MemberVO;

/**
 * 회원 가입 및 회원 관련 비즈니스 로직 인터페이스
 * - 기본 회원가입 (아이디, 비밀번호, 이름)
 * - 추후 학교 웹메일 인증 기능 확장을 위한 기본 메서드 포함
 */
public interface MemberService {

	// 아이디 중복 여부 확인 (중복이면 true, 사용 가능하면 false)
	public boolean isIdDuplicate(String userid);

	// 회원 가입 처리 (비밀번호 암호화 후 회원 테이블 + 권한 테이블 저장)
	public void register(MemberVO member);

	// [추후 확장용] 학교 웹메일로 인증번호 발송
	// public String sendSchoolEmailCode(String schoolEmail);

	// [추후 확장용] 사용자가 입력한 인증번호 검증
	// public boolean verifySchoolEmailCode(String inputCode, String savedCode);

}
