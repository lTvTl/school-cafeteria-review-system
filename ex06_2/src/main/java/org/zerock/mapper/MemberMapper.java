package org.zerock.mapper;

import org.zerock.domain.AuthVO;
import org.zerock.domain.MemberVO;

public interface MemberMapper {

	// 회원 정보 및 권한 조회 (로그인 시 사용)
	public MemberVO read(String userid);

	// 아이디 중복 체크 (0이면 사용 가능, 1 이상이면 중복)
	public int checkId(String userid);

	// 회원 기본 정보 등록 (userid, userpw, userName)
	public int insertMember(MemberVO member);

	// 회원 권한 등록 (기본 ROLE_USER 등)
	public int insertAuth(AuthVO auth);

}
