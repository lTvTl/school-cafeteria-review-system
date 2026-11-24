package org.zerock.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zerock.domain.DailyDietVO;

@Service
public class SchoolMenuImport {
    private final JdbcTemplate jdbc;

    public SchoolMenuImport(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Transactional
    public void save(Map<LocalDate, List<DailyDietVO>> week) {
        // Serialize imports across server instances before checking for existing meals.
        jdbc.execute("LOCK TABLE tbl_daily_diet IN SHARE ROW EXCLUSIVE MODE NOWAIT");
        jdbc.execute("LOCK TABLE tbl_menu IN SHARE ROW EXCLUSIVE MODE NOWAIT");
        for (Map.Entry<LocalDate, List<DailyDietVO>> day : week.entrySet()) {
            Date date = Date.valueOf(day.getKey());
            Set<String> existing = new HashSet<>(jdbc.queryForList(
                    "SELECT DISTINCT meal_time FROM tbl_daily_diet WHERE serve_date = ?", String.class, date));
            for (DailyDietVO row : day.getValue()) {
                if (existing.contains(row.getMealTime())) continue;
                Long id = jdbc.queryForObject("SELECT MIN(menu_id) FROM tbl_menu WHERE menu_name = ? AND category = ?",
                        Long.class, row.getMenuName(), row.getCategory());
                if (id == null) {
                    id = jdbc.queryForObject("SELECT seq_menu.NEXTVAL FROM dual", Long.class);
                    jdbc.update("INSERT INTO tbl_menu (menu_id, menu_name, category) VALUES (?, ?, ?)",
                            id, row.getMenuName(), row.getCategory());
                }
                jdbc.update("INSERT INTO tbl_daily_diet (diet_id, serve_date, meal_time, planned_menu_id) "
                        + "SELECT seq_diet.NEXTVAL, ?, ?, ? FROM dual WHERE NOT EXISTS "
                        + "(SELECT 1 FROM tbl_daily_diet WHERE serve_date = ? AND meal_time = ? AND planned_menu_id = ?)",
                        date, row.getMealTime(), id, date, row.getMealTime(), id);
            }
        }
    }
}
