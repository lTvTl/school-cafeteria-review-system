package org.zerock.service;

import java.util.List;

import org.zerock.domain.BoardAttachVO;
import org.zerock.domain.BoardVO;
import org.zerock.domain.Criteria;

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

}
