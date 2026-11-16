package org.zerock.domain;

import java.util.Date;

import lombok.Data;

/**
 * @file ReplyVO.java
 * @brief 댓글 정보를 담는 VO 클래스 (tbl_reply)
 */
@Data
public class ReplyVO {

	/** @brief 댓글 번호 (PK, seq_reply) */
	private Long rno;

	/** @brief 댓글이 달린 게시글 번호 (FK, tbl_board.bno) */
	private Long bno;

	/** @brief 댓글 내용 */
	private String reply;

	/**
	 * @brief 댓글 작성자 아이디
	 * @todo [통합] 로그인 모듈 연동 시 세션 유저 정보로 변경 (현재 임시값 "user00")
	 */
	private String replyer; // TODO: [통합] 세션 유저 정보로 변경

	/** @brief 댓글 작성일 */
	private Date replyDate;

	/** @brief 댓글 수정일 */
	private Date updateDate;
}