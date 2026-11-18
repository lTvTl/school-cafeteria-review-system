package org.zerock.domain;

import lombok.Data;

/**
 * @file AttachFileDTO.java
 * @brief 업로드된 첨부파일 정보를 브라우저로 전달하기 위한 DTO 클래스
 */
@Data
public class AttachFileDTO {

	/** @brief 원본 파일명 */
	private String fileName;

	/** @brief 업로드 폴더 경로 (yyyy/MM/dd) */
	private String uploadPath;

	/** @brief 첨부파일 고유 식별값 (UUID) */
	private String uuid;

	/** @brief 이미지 파일 여부 */
	private boolean image;

}
