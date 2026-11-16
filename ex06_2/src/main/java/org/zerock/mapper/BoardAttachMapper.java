package org.zerock.mapper;

import java.util.List;

import org.zerock.domain.BoardAttachVO;

/**
 * @file BoardAttachMapper.java
 * @brief 첨부파일(tbl_attach) 관련 MyBatis Mapper 인터페이스
 */
public interface BoardAttachMapper {

	/**
	 * @brief 첨부파일 등록 (bno 는 BoardMapper.insertSelectKey 로 확보된 값)
	 * @param vo 등록할 첨부파일 정보
	 */
	public void insert(BoardAttachVO vo);

	/**
	 * @brief 첨부파일 개별 삭제
	 * @param uuid 첨부파일 식별값
	 */
	public void delete(String uuid);

	/**
	 * @brief 특정 게시글의 첨부파일 전체 조회
	 * @param bno 게시글 번호
	 * @return 첨부파일 목록
	 */
	public List<BoardAttachVO> findByBno(Long bno);

	/**
	 * @brief 특정 게시글의 첨부파일 전체 삭제
	 * @param bno 게시글 번호
	 */
	public void deleteAll(Long bno);

	/**
	 * @brief 어제 날짜 폴더에 등록된 파일 목록 조회 (파일 정리 스케줄러용)
	 * @return 첨부파일 목록
	 */
	public List<BoardAttachVO> getOldFiles();
}