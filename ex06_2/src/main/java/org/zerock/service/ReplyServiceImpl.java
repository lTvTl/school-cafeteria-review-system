package org.zerock.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.domain.Criteria;
import org.zerock.domain.ReplyPageDTO;
import org.zerock.domain.ReplyVO;
import org.zerock.mapper.BoardMapper;
import org.zerock.mapper.ReplyMapper;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

/**
 * @file ReplyServiceImpl.java
 * @brief 댓글 비즈니스 로직을 구현하는 Service 클래스
 * @details 댓글 등록·삭제 시 게시글 테이블(tbl_board)의 댓글 수(replycnt)를 트랜잭션으로 함께 갱신한다.
 */
@Service
@Log4j
public class ReplyServiceImpl implements ReplyService {

	/** @brief 댓글 Mapper */
	@Setter(onMethod_ = @Autowired)
	private ReplyMapper mapper;

	/** @brief 게시글 Mapper (댓글 수 증감용) */
	@Setter(onMethod_ = @Autowired)
	private BoardMapper boardMapper;

	// @Override
	// public int register(ReplyVO vo) {
	//
	// log.info("register......" + vo);
	//
	// return mapper.insert(vo);
	//
	// }

	/**
	 * @brief 댓글을 등록하고 해당 게시글의 댓글 수를 1 증가시킨다.
	 * @param vo 등록할 댓글 정보
	 * @return 등록된 행 수
	 */
	@Transactional
	@Override
	public int register(ReplyVO vo) {

		log.info("register......" + vo);

		boardMapper.updateReplyCnt(vo.getBno(), 1);

		return mapper.insert(vo);

	}

	/**
	 * @brief 특정 댓글을 상세 조회한다.
	 * @param rno 댓글 번호
	 * @return 댓글 VO
	 */
	@Override
	public ReplyVO get(Long rno) {

		log.info("get......" + rno);

		return mapper.read(rno);

	}

	/**
	 * @brief 댓글 내용을 수정한다.
	 * @param vo 수정할 댓글 정보
	 * @return 수정된 행 수
	 */
	@Override
	public int modify(ReplyVO vo) {

		log.info("modify......" + vo);

		return mapper.update(vo);

	}

	// @Override
	// public int remove(Long rno) {
	//
	// log.info("remove...." + rno);
	//
	// return mapper.delete(rno);
	//
	// }

	/**
	 * @brief 댓글을 삭제하고 해당 게시글의 댓글 수를 1 감소시킨다.
	 * @param rno 삭제할 댓글 번호
	 * @return 삭제된 행 수
	 */
	@Transactional
	@Override
	public int remove(Long rno) {

		log.info("remove...." + rno);

		ReplyVO vo = mapper.read(rno);

		boardMapper.updateReplyCnt(vo.getBno(), -1);
		return mapper.delete(rno);

	}

	/**
	 * @brief 특정 게시글의 페이징된 댓글 목록을 조회한다.
	 * @param cri 페이징 조건
	 * @param bno 게시글 번호
	 * @return 댓글 목록
	 */
	@Override
	public List<ReplyVO> getList(Criteria cri, Long bno) {

		log.info("get Reply List of a Board " + bno);

		return mapper.getListWithPaging(cri, bno);

	}

	/**
	 * @brief 특정 게시글의 전체 댓글 수와 페이징된 댓글 목록을 함께 조회한다.
	 * @param cri 페이징 조건
	 * @param bno 게시글 번호
	 * @return 댓글 페이지 DTO
	 */
	@Override
	public ReplyPageDTO getListPage(Criteria cri, Long bno) {

		return new ReplyPageDTO(mapper.getCountByBno(bno), mapper.getListWithPaging(cri, bno));
	}

}
