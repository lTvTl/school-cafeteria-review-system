package org.zerock.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @file ReplyPageDTO.java
 * @brief 특정 게시글의 전체 댓글 수와 페이징된 댓글 목록을 함께 전달하는 DTO 클래스
 */
@Data
@AllArgsConstructor
@Getter
public class ReplyPageDTO {

  /** @brief 해당 게시글의 전체 댓글 수 */
  private int replyCnt;

  /** @brief 현재 페이지의 댓글 목록 */
  private List<ReplyVO> list;
}
