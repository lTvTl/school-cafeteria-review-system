package org.zerock.controller;

import java.util.Collections;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.zerock.domain.BookmarkStatus;
import org.zerock.service.BookmarkService;

@RestController
@RequestMapping("/menu/{menuId}/bookmark")
public class BookmarkController {
    private static final Logger log = Logger.getLogger(BookmarkController.class);
    private final BookmarkService service;

    @Autowired
    public BookmarkController(BookmarkService service) { this.service = service; }

    @PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE)
    public BookmarkStatus add(@PathVariable("menuId") Long menuId) { return service.add(menuId); }

    @PostMapping(value = "/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public BookmarkStatus remove(@PathVariable("menuId") Long menuId) { return service.remove(menuId); }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> rejected(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatus()).body(Collections.singletonMap("message", e.getReason()));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> failed(DataAccessException e) {
        log.error("Bookmark persistence failed", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", "북마크를 변경하지 못했습니다. 잠시 후 다시 시도해 주세요."));
    }
}
