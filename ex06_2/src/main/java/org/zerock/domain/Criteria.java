package org.zerock.domain;

import org.springframework.web.util.UriComponentsBuilder;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @file Criteria.java
 * @brief 게시판 및 댓글의 페이징·검색 조건을 담는 클래스
 */
@ToString
@Setter
@Getter
public class Criteria {

	/** @brief 현재 페이지 번호 */
	private int pageNum;

	/** @brief 한 페이지당 출력할 데이터 개수 */
	private int amount;

	/** @brief 검색 조건 타입 (T: 제목, C: 내용, W: 작성자 조합) */
	private String type;

	/** @brief 검색 키워드 */
	private String keyword;

	/**
	 * @brief 기본 생성자 (1페이지, 10개씩 기본 설정)
	 */
	public Criteria() {
		this(1, 10);
	}

	/**
	 * @brief 페이지 번호와 출력 개수를 지정하는 생성자
	 * @param pageNum 페이지 번호
	 * @param amount 페이지당 데이터 개수
	 */
	public Criteria(int pageNum, int amount) {
		this.pageNum = pageNum;
		this.amount = amount;
	}

	/**
	 * @brief 검색 조건 문자열을 배열로 분리하여 반환
	 * @return 검색 조건 배열
	 */
	public String[] getTypeArr() {

		return type == null ? new String[] {} : type.split("");
	}

	/**
	 * @brief 페이징 및 검색 조건을 쿼리 스트링 형태로 생성하여 반환
	 * @return URL 쿼리 스트링
	 */
	public String getListLink() {

		UriComponentsBuilder builder = UriComponentsBuilder.fromPath("")
				.queryParam("pageNum", this.pageNum)
				.queryParam("amount", this.getAmount())
				.queryParam("type", this.getType())
				.queryParam("keyword", this.getKeyword());

		return builder.toUriString();

	}
}
