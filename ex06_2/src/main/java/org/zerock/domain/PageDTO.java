package org.zerock.domain;

import lombok.Getter;
import lombok.ToString;

/**
 * @file PageDTO.java
 * @brief 게시글 목록 화면의 페이지네이션 계산 정보를 담는 DTO 클래스
 */
@Getter
@ToString
public class PageDTO {

  /** @brief 화면에 표시할 시작 페이지 번호 */
  private int startPage;

  /** @brief 화면에 표시할 끝 페이지 번호 */
  private int endPage;

  /** @brief 이전/다음 페이지 블록 존재 여부 */
  private boolean prev, next;

  /** @brief 전체 데이터 개수 */
  private int total;

  /** @brief 현재 페이지 및 검색 조건 정보 */
  private Criteria cri;

  /**
   * @brief 검색 조건과 전체 개수를 기반으로 페이지네이션 정보를 계산하는 생성자
   * @param cri 현재 페이징 및 검색 조건
   * @param total 전체 게시글 수
   */
  public PageDTO(Criteria cri, int total) {

    this.cri = cri;
    this.total = total;

    this.endPage = (int) (Math.ceil(cri.getPageNum() / 10.0)) * 10;

    this.startPage = this.endPage - 9;

    int realEnd = (int) (Math.ceil((total * 1.0) / cri.getAmount()));

    if (realEnd < this.endPage) {
      this.endPage = realEnd;
    }

    this.prev = this.startPage > 1;

    this.next = this.endPage < realEnd;
  }
  
}

