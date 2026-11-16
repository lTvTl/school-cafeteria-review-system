package org.zerock.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.zerock.domain.Criteria;
import org.zerock.domain.ReplyPageDTO;
import org.zerock.domain.ReplyVO;
import org.zerock.service.ReplyService;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

/**
 * @file ReplyController.java
 * @brief 댓글 REST 방식 요청을 처리하는 RestController 클래스
 * @details 댓글 등록, 조회, 수정, 삭제 및 페이징 목록 조회를 JSON으로 처리한다.
 */
@RequestMapping("/replies/")
@RestController
@Log4j
@AllArgsConstructor
public class ReplyController {

	/** @brief 댓글 비즈니스 로직 Service */
	private ReplyService service;

	/**
	 * @brief 신규 댓글을 등록한다. (로그인 사용자 전용)
	 * @param vo 등록할 댓글 정보 (JSON)
	 * @return 처리 결과 문자열 ResponseEntity
	 */
	@PreAuthorize("isAuthenticated()")
	@PostMapping(value = "/new", consumes = "application/json", produces = { MediaType.TEXT_PLAIN_VALUE })
	public ResponseEntity<String> create(@RequestBody ReplyVO vo) {

		log.info("ReplyVO: " + vo);

		int insertCount = service.register(vo);

		log.info("Reply INSERT COUNT: " + insertCount);

		return insertCount == 1 ? new ResponseEntity<>("success", HttpStatus.OK)
				: new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * @brief 특정 번호의 댓글 상세 정보를 조회한다.
	 * @param rno 댓글 번호
	 * @return 댓글 VO ResponseEntity
	 */
	@GetMapping(value = "/{rno}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<ReplyVO> get(@PathVariable("rno") Long rno) {

		log.info("get: " + rno);

		return new ResponseEntity<>(service.get(rno), HttpStatus.OK);
	}

	/**
	 * @brief 댓글 내용을 수정한다. (작성자 본인만 가능)
	 * @param vo 수정할 댓글 정보 (JSON)
	 * @param rno 댓글 번호
	 * @return 처리 결과 문자열 ResponseEntity
	 * @todo [통합] 세션 사용자 아이디와 댓글 작성자 일치 검증 유지
	 */
	@PreAuthorize("principal.username == #vo.replyer")
	@RequestMapping(method = { RequestMethod.PUT,
			RequestMethod.PATCH }, value = "/{rno}", consumes = "application/json")
	public ResponseEntity<String> modify(@RequestBody ReplyVO vo, @PathVariable("rno") Long rno) {

		log.info("rno: " + rno);
		log.info("modify: " + vo);

		return service.modify(vo) == 1 ? new ResponseEntity<>("success", HttpStatus.OK)
				: new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

	}

	// @DeleteMapping(value = "/{rno}", produces = { MediaType.TEXT_PLAIN_VALUE })
	// public ResponseEntity<String> remove(@PathVariable("rno") Long rno) {
	//
	// log.info("remove: " + rno);
	//
	// return service.remove(rno) == 1 ? new ResponseEntity<>("success",
	// HttpStatus.OK)
	// : new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	//
	// }

	/**
	 * @brief 댓글을 삭제한다. (작성자 본인 또는 관리자 ROLE_ADMIN 가능)
	 * @param vo 댓글 작성자 정보가 포함된 객체
	 * @param rno 삭제할 댓글 번호
	 * @return 처리 결과 문자열 ResponseEntity
	 * @todo [통합] 회원 모듈의 관리자 권한(ROLE_ADMIN) 및 작성자 검증 연동
	 */
	@PreAuthorize("principal.username == #vo.replyer or hasRole('ROLE_ADMIN')")
	@DeleteMapping("/{rno}")
	public ResponseEntity<String> remove(@RequestBody ReplyVO vo, @PathVariable("rno") Long rno) {

		log.info("remove: " + rno);

		log.info("replyer: " + vo.getReplyer());

		return service.remove(rno) == 1 ? new ResponseEntity<>("success", HttpStatus.OK)
				: new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

	}

	// @GetMapping(value = "/pages/{bno}/{page}",
	// produces = {
	// MediaType.APPLICATION_XML_VALUE,
	// MediaType.APPLICATION_JSON_UTF8_VALUE })
	// public ResponseEntity<List<ReplyVO>> getList(
	// @PathVariable("page") int page,
	// @PathVariable("bno") Long bno) {
	//
	//
	// log.info("getList.................");
	// Criteria cri = new Criteria(page,10);
	// log.info(cri);
	//
	// return new ResponseEntity<>(service.getList(cri, bno), HttpStatus.OK);
	// }

	/**
	 * @brief 특정 게시글의 댓글 목록과 전체 댓글 수를 페이징하여 조회한다.
	 * @param page 조회할 페이지 번호
	 * @param bno 게시글 번호
	 * @return 댓글 수 및 목록 DTO ResponseEntity
	 */
	@GetMapping(value = "/pages/{bno}/{page}", produces = { MediaType.APPLICATION_XML_VALUE,
			MediaType.APPLICATION_JSON_UTF8_VALUE })
	public ResponseEntity<ReplyPageDTO> getList(@PathVariable("page") int page, @PathVariable("bno") Long bno) {

		Criteria cri = new Criteria(page, 10);

		log.info("get Reply List bno: " + bno);

		log.info("cri:" + cri);

		return new ResponseEntity<>(service.getListPage(cri, bno), HttpStatus.OK);
	}

}
