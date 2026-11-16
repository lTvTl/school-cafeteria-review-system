package org.zerock.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.domain.BoardAttachVO;
import org.zerock.domain.BoardVO;
import org.zerock.domain.Criteria;
import org.zerock.domain.PageDTO;
import org.zerock.service.BoardService;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j;

/**
 * @file BoardController.java
 * @brief 커뮤니티 게시판 웹 요청을 처리하는 Spring MVC Controller 클래스
 * @details 게시글 목록, 등록, 상세 조회, 수정, 삭제 및 첨부파일 목록 조회를 담당한다.
 */
@Controller
@Log4j
@RequestMapping("/board/*")
@AllArgsConstructor
public class BoardController {

	/** @brief 게시판 비즈니스 로직 Service */
	private BoardService service;
	
	/**
	 * @brief 게시글 목록 화면 및 페이징 정보를 조회한다.
	 * @param cri 페이징 및 검색 조건
	 * @param model 뷰에 전달할 데이터 모델
	 */
	@GetMapping("/list")
	public void list(Criteria cri, Model model) {

		log.info("list: " + cri);
		model.addAttribute("list", service.getList(cri));
		// model.addAttribute("pageMaker", new PageDTO(cri, 123));

		int total = service.getTotal(cri);

		log.info("total: " + total);

		model.addAttribute("pageMaker", new PageDTO(cri, total));

	}
	
	/**
	 * @brief 게시글 작성 화면으로 이동한다. (로그인 사용자 전용)
	 */
	@GetMapping("/register")
	@PreAuthorize("isAuthenticated()")
	public void register() {

	}

	/**
	 * @brief 신규 게시글 및 첨부파일을 등록한다.
	 * @param board 등록할 게시글 정보
	 * @param rttr 리다이렉트 시 일회성 알림 메시지 전달 객체
	 * @return 게시글 목록 리다이렉트 경로
	 */
	@PostMapping("/register")
	@PreAuthorize("isAuthenticated()")
	public String register(BoardVO board, RedirectAttributes rttr) {

		log.info("==========================");

		log.info("register: " + board);

		if (board.getAttachList() != null) {

			board.getAttachList().forEach(attach -> log.info(attach));

		}

		log.info("==========================");

		service.register(board);

		rttr.addFlashAttribute("result", board.getBno());

		return "redirect:/board/list";
	}


	/**
	 * @brief 게시글 상세 화면 또는 수정 화면 데이터를 조회한다.
	 * @param bno 조회할 게시글 번호
	 * @param cri 기존 페이징 및 검색 조건 유지용 객체
	 * @param model 뷰에 전달할 데이터 모델
	 */
	@GetMapping({ "/get", "/modify" })
	public void get(@RequestParam("bno") Long bno, @ModelAttribute("cri") Criteria cri, Model model) {

		log.info("/get or modify");
		model.addAttribute("board", service.get(bno));
	}

	/**
	 * @brief 게시글을 수정한다. (작성자 본인만 가능)
	 * @param board 수정할 게시글 정보
	 * @param cri 페이징 및 검색 조건
	 * @param rttr 리다이렉트 알림 메시지 전달 객체
	 * @return 게시글 목록 리다이렉트 경로
	 * @todo [통합] 회원 모듈의 로그인 세션 사용자 정보와 작성자 일치 검증 유지
	 */
	@PreAuthorize("principal.username == #board.writer")
	@PostMapping("/modify")
	public String modify(BoardVO board, Criteria cri, RedirectAttributes rttr) {
		log.info("modify:" + board);

		if (service.modify(board)) {
			rttr.addFlashAttribute("result", "success");
		}

		return "redirect:/board/list" + cri.getListLink();
	}
	
	/**
	 * @brief 게시글과 첨부파일을 삭제한다. (작성자 본인 또는 관리자 ROLE_ADMIN 가능)
	 * @param bno 삭제할 게시글 번호
	 * @param cri 페이징 및 검색 조건
	 * @param rttr 리다이렉트 알림 메시지 전달 객체
	 * @param writer 게시글 작성자 아이디
	 * @return 게시글 목록 리다이렉트 경로
	 * @todo [통합] 회원 모듈의 관리자 권한(ROLE_ADMIN) 및 작성자 검증 연동
	 */
	@PreAuthorize("principal.username == #writer or hasRole('ROLE_ADMIN')")
	@PostMapping("/remove")
	public String remove(@RequestParam("bno") Long bno, Criteria cri, RedirectAttributes rttr, String writer) {

		log.info("remove..." + bno);

		List<BoardAttachVO> attachList = service.getAttachList(bno);

		if (service.remove(bno)) {

			// delete Attach Files
			deleteFiles(attachList);

			rttr.addFlashAttribute("result", "success");
		}
		return "redirect:/board/list" + cri.getListLink();
	}

	/**
	 * @brief 서버 디스크(C:/upload)에 저장된 실제 첨부파일 및 썸네일을 삭제한다.
	 * @param attachList 삭제할 첨부파일 목록
	 */
	private void deleteFiles(List<BoardAttachVO> attachList) {

		if (attachList == null || attachList.size() == 0) {
			return;
		}

		log.info("delete attach files...................");
		log.info(attachList);

		attachList.forEach(attach -> {
			try {
				Path file = Paths.get(
						"C:\\upload\\" + attach.getUploadPath() + "\\" + attach.getUuid() + "_" + attach.getFileName());

				Files.deleteIfExists(file);

				if (Files.probeContentType(file).startsWith("image")) {

					Path thumbNail = Paths.get("C:\\upload\\" + attach.getUploadPath() + "\\s_" + attach.getUuid() + "_"
							+ attach.getFileName());

					Files.delete(thumbNail);
				}

			} catch (Exception e) {
				log.error("delete file error" + e.getMessage());
			} // end catch
		});// end foreachd
	}

	/**
	 * @brief 특정 게시글의 첨부파일 목록을 JSON으로 반환한다.
	 * @param bno 게시글 번호
	 * @return 첨부파일 목록 ResponseEntity
	 */
	@GetMapping(value = "/getAttachList", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ResponseBody
	public ResponseEntity<List<BoardAttachVO>> getAttachList(Long bno) {

		log.info("getAttachList " + bno);

		return new ResponseEntity<>(service.getAttachList(bno), HttpStatus.OK);

	}

}
