package org.zerock.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.zerock.domain.Criteria;
import org.zerock.domain.ReplyVO;

/**
 * @file ReplyMapper.java
 * @brief 댓글(tbl_reply) 관련 MyBatis Mapper 인터페이스
 */
public interface ReplyMapper {

	/**
	 * @brief 댓글 등록
	 * @param vo 등록할 댓글
	 * @return 등록된 행 수
	 * @todo [통합] replyer 는 세션 유저 정보로 변경 (XML 에서 임시값 "user00" 처리)
	 */
	public int insert(ReplyVO vo);

	/**
	 * @brief 댓글 상세 조회
	 * @param rno 댓글 번호
	 * @return 댓글 정보
	 */
	public ReplyVO read(Long rno);

	/**
	 * @brief 댓글 삭제
	 * @param rno 댓글 번호
	 * @return 삭제된 행 수
	 */
	public int delete(Long rno);

	/**
	 * @brief 댓글 수정
	 * @param reply 수정할 댓글 (rno, reply 사용)
	 * @return 수정된 행 수
	 */
	public int update(ReplyVO reply);

	/**
	 * @brief 특정 게시글의 댓글 전체 목록 조회
	 * @param bno 게시글 번호
	 * @return 댓글 목록 (rno 오름차순)
	 */
	public List<ReplyVO> getList(Long bno);

	/**
	 * @brief 특정 게시글의 댓글 목록 조회 (페이징)
	 * @param cri 페이지 번호, 페이지당 개수
	 * @param bno 게시글 번호
	 * @return 댓글 목록
	 */
	public List<ReplyVO> getListWithPaging(@Param("cri") Criteria cri, @Param("bno") Long bno);

	/**
	 * @brief 특정 게시글의 댓글 수 조회
	 * @param bno 게시글 번호
	 * @return 댓글 수
	 */
	public int getCountByBno(Long bno);

	/**
	 * @brief 특정 게시글의 댓글 전체 삭제 (게시글 삭제 시 외래키 제약조건 해소용)
	 * @param bno 게시글 번호
	 * @return 삭제된 행 수
	 */
	public int deleteAll(Long bno);

	/**
	 * @brief 특정 작성자가 작성한 댓글 목록 조회 (마이페이지용)
	 * @param replyer 댓글 작성자 아이디
	 * @return 댓글 목록
	 */
	public List<ReplyVO> getListByReplyer(String replyer);
}