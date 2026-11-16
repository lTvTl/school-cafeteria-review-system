package org.zerock.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zerock.domain.BoardAttachVO;
import org.zerock.domain.BoardVO;
import org.zerock.domain.Criteria;

/**
 * @file BoardMapper.java
 * @brief 게시글(tbl_board) 관련 MyBatis Mapper 인터페이스
 */
public interface BoardMapper {

	/**
	 * @brief 게시글 전체 목록 조회
	 * @return 게시글 목록
	 */
	public List<BoardVO> getList();

	/**
	 * @brief 게시글 목록 조회 (페이징, 검색 조건 포함)
	 * @param cri 페이지 번호, 페이지당 개수, 검색 조건
	 * @return 게시글 목록
	 */
	public List<BoardVO> getListWithPaging(Criteria cri);

	/**
	 * @brief 게시글 등록 (PK 를 미리 확보하지 않음)
	 * @param board 등록할 게시글
	 * @todo [통합] writer 는 세션 유저 정보로 변경 (XML 에서 임시값 "user00" 처리)
	 */
	public void insert(BoardVO board);

	/**
	 * @brief 게시글 등록 (selectKey 로 bno 를 사전 확보하여 첨부파일 등록에 사용)
	 * @param board 등록할 게시글 (등록 후 bno 가 채워짐)
	 * @return 등록된 행 수
	 * @todo [통합] writer 는 세션 유저 정보로 변경 (XML 에서 임시값 "user00" 처리)
	 */
	public Integer insertSelectKey(BoardVO board);

	/**
	 * @brief 게시글 상세 조회
	 * @param bno 게시글 번호
	 * @return 게시글 정보
	 */
	public BoardVO read(Long bno);

	/**
	 * @brief 게시글 삭제
	 * @param bno 게시글 번호
	 * @return 삭제된 행 수
	 */
	public int delete(Long bno);

	/**
	 * @brief 게시글 수정
	 * @param board 수정할 게시글
	 * @return 수정된 행 수
	 * @todo [통합] 작성자 본인 검증은 세션 유저 정보로 처리
	 */
	public int update(BoardVO board);

	/**
	 * @brief 검색 조건에 해당하는 게시글 총 개수 조회
	 * @param cri 검색 조건
	 * @return 게시글 총 개수
	 */
	public int getTotalCount(Criteria cri);

	/**
	 * @brief 댓글 수 증감
	 * @param bno 게시글 번호
	 * @param amount 증감 값 (등록 +1, 삭제 -1)
	 */
	public void updateReplyCnt(@Param("bno") Long bno, @Param("amount") int amount);

	/**
	 * @brief 게시글의 첨부파일 목록 조회
	 * @param bno 게시글 번호
	 * @return 첨부파일 목록
	 */
	public List<BoardAttachVO> findByBno(Long bno);

}