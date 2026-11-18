package org.zerock.domain;

import lombok.Data;

/**
 * @file BoardAttachVO.java
 * @brief 게시글 첨부파일 정보를 담는 VO 클래스 (tbl_attach)
 */
@Data
public class BoardAttachVO {

	/** @brief 첨부파일 고유 식별값 (PK, UUID) */
	private String uuid;

	/** @brief 업로드 폴더 경로 (yyyy/MM/dd) */
	private String uploadPath;

	/** @brief 원본 파일명 */
	private String fileName;

	/** @brief 이미지 여부 (true: 이미지, false: 일반 파일) */
	private boolean fileType;

	/** @brief 소속 게시글 번호 (FK, tbl_board.bno) */
	private Long bno;
}