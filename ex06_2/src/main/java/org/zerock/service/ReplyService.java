package org.zerock.service;

import java.util.List;

import org.zerock.domain.Criteria;
import org.zerock.domain.ReplyPageDTO;
import org.zerock.domain.ReplyVO;

/**
 * @file ReplyService.java
 * @brief 댓글 비즈니스 로직을 정의하는 Service 인터페이스
 */
public interface ReplyService {

	/**
	 * @brief 댓글 등록 및 게시글 댓글 수 증가
	 * @param vo 등록할 댓글 정보
	 * @return 등록된 행 수
	 */
	public int register(ReplyVO vo);

	/**
	 * @brief 댓글 상세 조회
	 * @param rno 댓글 번호
	 * @return 댓글 정보
	 */
	public ReplyVO get(Long rno);

	/**
	 * @brief 댓글 수정
	 * @param vo 수정할 댓글 정보
	 * @return 수정된 행 수
	 */
	public int modify(ReplyVO vo);

	/**
	 * @brief 댓글 삭제 및 게시글 댓글 수 차감
	 * @param rno 댓글 번호
	 * @return 삭제된 행 수
	 */
	public int remove(Long rno);

	/**
	 * @brief 특정 게시글의 댓글 목록 조회
	 * @param cri 페이징 조건
	 * @param bno 게시글 번호
	 * @return 댓글 목록
	 */
	public List<ReplyVO> getList(Criteria cri, Long bno);
	
	/**
	 * @brief 특정 게시글의 댓글 수와 페이징된 댓글 목록 조회
	 * @param cri 페이징 조건
	 * @param bno 게시글 번호
	 * @return 댓글 개수 및 목록 DTO
	 */
	public ReplyPageDTO getListPage(Criteria cri, Long bno);

	/**
	 * @brief 특정 작성자가 작성한 댓글 목록 조회 (마이페이지용)
	 * @param replyer 댓글 작성자 아이디
	 * @return 댓글 목록
	 */
	public List<ReplyVO> getListByReplyer(String replyer);

}
