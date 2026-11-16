package org.zerock.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.domain.BoardAttachVO;
import org.zerock.domain.BoardVO;
import org.zerock.domain.Criteria;
import org.zerock.mapper.BoardAttachMapper;
import org.zerock.mapper.BoardMapper;
import org.zerock.mapper.ReplyMapper;

import lombok.Setter;
import lombok.extern.log4j.Log4j;

/**
 * @file BoardServiceImpl.java
 * @brief 커뮤니티 게시판 비즈니스 로직을 구현하는 Service 클래스
 * @details 게시글 CRUD 및 첨부파일·댓글 연동을 트랜잭션 단위로 처리한다.
 */
@Log4j
@Service
public class BoardServiceImpl implements BoardService {

	/** @brief 게시글 Mapper */
	@Setter(onMethod_ = @Autowired)
	private BoardMapper mapper;

	/** @brief 첨부파일 Mapper */
	@Setter(onMethod_ = @Autowired)
	private BoardAttachMapper attachMapper;

	/** @brief 댓글 Mapper (게시글 삭제 시 댓글 선삭제용) */
	@Setter(onMethod_ = @Autowired)
	private ReplyMapper replyMapper;

	/**
	 * @brief 게시글과 첨부파일을 함께 등록한다.
	 * @param board 등록할 게시글 및 첨부파일 목록
	 */
	@Transactional
	@Override
	public void register(BoardVO board) {

		log.info("register......" + board);

		mapper.insertSelectKey(board);

		if (board.getAttachList() == null || board.getAttachList().size() <= 0) {
			return;
		}

		board.getAttachList().forEach(attach -> {

			attach.setBno(board.getBno());
			attachMapper.insert(attach);
		});
	}

	/**
	 * @brief 특정 번호의 게시글을 상세 조회한다.
	 * @param bno 게시글 번호
	 * @return 게시글 VO
	 */
	@Override
	public BoardVO get(Long bno) {

		log.info("get......" + bno);

		return mapper.read(bno);

	}

	/**
	 * @brief 게시글 본문과 첨부파일 목록을 수정한다.
	 * @param board 수정할 게시글 및 첨부파일 정보
	 * @return 수정 성공 여부
	 */
	@Transactional
	@Override
	public boolean modify(BoardVO board) {

		log.info("modify......" + board);

		attachMapper.deleteAll(board.getBno());

		boolean modifyResult = mapper.update(board) == 1;
		
		if (modifyResult && board.getAttachList() != null) {

			board.getAttachList().forEach(attach -> {

				attach.setBno(board.getBno());
				attachMapper.insert(attach);
			});
		}

		return modifyResult;
	}

	// @Override
	// public boolean modify(BoardVO board) {
	//
	// log.info("modify......" + board);
	//
	// return mapper.update(board) == 1;
	// }

	// @Override
	// public boolean remove(Long bno) {
	//
	// log.info("remove...." + bno);
	//
	// return mapper.delete(bno) == 1;
	// }

	/**
	 * @brief 게시글 삭제 시 외래키로 연결된 댓글과 첨부파일을 먼저 삭제한 뒤 게시글을 삭제한다.
	 * @param bno 삭제할 게시글 번호
	 * @return 삭제 성공 여부
	 */
	@Transactional
	@Override
	public boolean remove(Long bno) {

		log.info("remove...." + bno);

		replyMapper.deleteAll(bno);
		attachMapper.deleteAll(bno);

		return mapper.delete(bno) == 1;
	}

	// @Override
	// public List<BoardVO> getList() {
	//
	// log.info("getList..........");
	//
	// return mapper.getList();
	// }

	/**
	 * @brief 페이징 및 검색 조건에 맞는 게시글 목록을 조회한다.
	 * @param cri 페이징 및 검색 조건
	 * @return 게시글 목록
	 */
	@Override
	public List<BoardVO> getList(Criteria cri) {

		log.info("get List with criteria: " + cri);

		return mapper.getListWithPaging(cri);
	}

	/**
	 * @brief 검색 조건에 맞는 전체 게시글 수를 조회한다.
	 * @param cri 검색 조건
	 * @return 전체 게시글 수
	 */
	@Override
	public int getTotal(Criteria cri) {

		log.info("get total count");
		return mapper.getTotalCount(cri);
	}

	/**
	 * @brief 특정 게시글에 속한 첨부파일 목록을 조회한다.
	 * @param bno 게시글 번호
	 * @return 첨부파일 목록
	 */
	@Override
	public List<BoardAttachVO> getAttachList(Long bno) {

		log.info("get Attach list by bno" + bno);

		return attachMapper.findByBno(bno);
	}

	/**
	 * @brief 특정 게시글의 첨부파일을 DB에서 모두 삭제한다.
	 * @param bno 게시글 번호
	 */
	@Override
	public void removeAttach(Long bno) {

		log.info("remove all attach files");

		attachMapper.deleteAll(bno);
	}

}
