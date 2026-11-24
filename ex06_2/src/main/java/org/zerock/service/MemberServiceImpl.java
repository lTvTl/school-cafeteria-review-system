package org.zerock.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
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

	@Setter(onMethod_ = @Autowired(required = false))
	private JavaMailSender mailSender;

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

	@Override
	public String sendSchoolEmailCode(String schoolEmail) {
		// 1. 6자리 난수 인증코드 생성 (100000 ~ 999999)
		int randomNum = new java.util.Random().nextInt(900000) + 100000;
		String code = String.valueOf(randomNum);

		log.info("==================================================");
		log.info("[학교 웹메일 인증] 수신 이메일: " + schoolEmail);
		log.info("[학교 웹메일 인증] 발급된 인증번호: " + code);
		log.info("==================================================");

		// 2. JavaMailSender를 통해 실제 이메일 발송
		if (mailSender == null) {
			throw new IllegalStateException("root-context.xml에 mailSender가 설정되지 않았습니다.");
		}

		SimpleMailMessage message = new SimpleMailMessage();
		if (mailSender instanceof JavaMailSenderImpl) {
			String fromUser = ((JavaMailSenderImpl) mailSender).getUsername();
			if (fromUser != null && !fromUser.isEmpty()) {
				message.setFrom(fromUser);
			}
		}
		message.setTo(schoolEmail);
		message.setSubject("[학식 리뷰 시스템] 학교 웹메일 회원가입 인증번호 안내");
		message.setText("안녕하세요.\n\n요청하신 학교 웹메일 회원가입 인증번호는 [" + code + "] 입니다.\n회원가입 화면에 6자리 인증번호를 입력해주세요.");
		mailSender.send(message);

		return code;
	}

	@Override
	public boolean verifySchoolEmailCode(String inputCode, String savedCode) {
		if (inputCode == null || savedCode == null) {
			return false;
		}
		return savedCode.equals(inputCode.trim());
	}

	@Override
	public MemberVO get(String userid) {
		return memberMapper.read(userid);
	}

	@Override
	public boolean modify(MemberVO member) {
		if (member.getUserpw() != null && !member.getUserpw().trim().isEmpty()) {
			member.setUserpw(pwencoder.encode(member.getUserpw().trim()));
		} else {
			member.setUserpw(null);
		}
		return memberMapper.updateMember(member) == 1;
	}

}
