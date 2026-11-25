package org.zerock.service;

import java.util.List;

import org.zerock.domain.BoardAttachVO;
import org.zerock.domain.BoardVO;
import org.zerock.domain.Criteria;
import org.zerock.domain.DailyReviewVO;

/**
 * @file BoardService.java
 * @brief 커뮤니티 게시판 비즈니스 로직을 정의하는 Service 인터페이스
 */
public interface BoardService {

	/**
	 * @brief 게시글 및 첨부파일 등록
	 * @param board 등록할 게시글 정보
	 */
	public void register(BoardVO board);

	/**
	 * @brief 게시글 상세 조회
	 * @param bno 게시글 번호
	 * @return 게시글 정보
	 */
	public BoardVO get(Long bno);

	/**
	 * @brief 게시글 및 첨부파일 수정
	 * @param board 수정할 게시글 정보
	 * @return 수정 성공 여부
	 */
	public boolean modify(BoardVO board);

	/**
	 * @brief 게시글 삭제 (댓글 및 첨부파일 일괄 삭제 포함)
	 * @param bno 게시글 번호
	 * @return 삭제 성공 여부
	 */
	public boolean remove(Long bno);

	// public List<BoardVO> getList();

	/**
	 * @brief 페이징 및 검색 조건이 적용된 게시글 목록 조회
	 * @param cri 페이징 및 검색 조건
	 * @return 게시글 목록
	 */
	public List<BoardVO> getList(Criteria cri);

	/**
	 * @brief 검색 조건에 해당하는 전체 게시글 수 조회
	 * @param cri 검색 조건
	 * @return 전체 게시글 수
	 */
	public int getTotal(Criteria cri);
	
	/**
	 * @brief 특정 게시글의 첨부파일 목록 조회
	 * @param bno 게시글 번호
	 * @return 첨부파일 목록
	 */
	public List<BoardAttachVO> getAttachList(Long bno);
	
	/**
	 * @brief 특정 게시글의 첨부파일 전체 삭제
	 * @param bno 게시글 번호
	 */
	public void removeAttach(Long bno);

	/**
	 * @brief 특정 작성자가 작성한 게시글 목록 조회 (마이페이지용)
	 * @param writer 작성자 아이디
	 * @return 게시글 목록
	 */
	public List<BoardVO> getListByWriter(String writer);

	/**
	 * @brief 게시판 내 학식 리뷰 확인 목록 조회 (페이징 및 검색 포함)
	 * @param cri 페이징 및 검색 조건
	 * @return 학식 리뷰 목록
	 */
	public List<DailyReviewVO> getDailyReviewList(Criteria cri);

	/**
	 * @brief 검색 조건에 해당하는 전체 학식 리뷰 개수 조회
	 * @param cri 검색 조건
	 * @return 학식 리뷰 총 개수
	 */
	public int getDailyReviewTotal(Criteria cri);

	/**
	 * @brief 특정 학식 리뷰 단건 조회
	 * @param dailyReviewId 학식 리뷰 번호
	 * @return 학식 리뷰 정보
	 */
	public DailyReviewVO getDailyReview(Long dailyReviewId);

	/**
	 * @brief 당일 작성한 본인의 학식 리뷰 수정 (작성일이 오늘 SYSDATE 와 같을 때만 가능)
	 * @param review 수정할 학식 리뷰 정보
	 * @return 수정 성공 여부
	 */
	public boolean modifyDailyReview(DailyReviewVO review);

	/**
	 * @brief 학식 리뷰 삭제 (이전 날짜 리뷰도 본인 또는 관리자는 삭제 가능)
	 * @param dailyReviewId 삭제할 학식 리뷰 번호
	 * @return 삭제 성공 여부
	 */
	public boolean removeDailyReview(Long dailyReviewId);

}
