package org.zerock.domain;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * @file BoardVO.java
 * @brief 게시글 정보를 담는 VO 클래스 (tbl_board)
 * @details Lombok @Data 로 getter/setter/toString/equals/hashCode 가 자동 생성된다.
 *          다중 파일 업로드를 위해 첨부파일 목록(attachList)을 포함한다.
 */
@Data
public class BoardVO {

	/** @brief 게시글 번호 (PK, seq_board) */
	private Long bno;

	/** @brief 게시글 제목 */
	private String title;

	/** @brief 게시글 본문 내용 */
	private String content;

	/**
	 * @brief 작성자 아이디
	 * @todo [통합] 로그인 모듈 연동 시 세션 유저 정보로 변경 (현재 임시값 "user00")
	 */
	private String writer; // TODO: [통합] 세션 유저 정보로 변경

	/** @brief 작성자 이름 (tbl_member.username 조인 표시용) */
	private String writerName;

	/** @brief 게시글 작성일 */
	private Date regdate;

	/** @brief 게시글 수정일 */
	private Date updateDate;

	/** @brief 게시글에 달린 댓글 수 (tbl_board.replycnt) */
	private int replyCnt;

	/** @brief 게시글에 등록된 첨부파일 목록 (tbl_attach, bno 기준) */
	private List<BoardAttachVO> attachList;
}