package org.zerock.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zerock.domain.DailyDietVO;

@Service
public class SchoolMenuWeek {
	private static final Logger log = LoggerFactory.getLogger(SchoolMenuWeek.class);
	private static final String URL = "https://www.mokpo.ac.kr/www/2086/subview.do";
	@org.springframework.beans.factory.annotation.Autowired
	private SchoolMenuImport importer;
	private LocalDate refreshedDate;
	private long nextFetch;

	public synchronized void refresh(LocalDate today) {
		long now = System.currentTimeMillis();
		if (now >= nextFetch || !today.equals(refreshedDate)) {
			try {
				Map<LocalDate, List<DailyDietVO>> week = parse(Jsoup.connect(URL).timeout(5000).get());
				if (!week.containsKey(today)) throw new IOException("Unexpected menu week");
				importer.save(week);
				nextFetch = now + 15 * 60_000L;
			} catch (IOException | RuntimeException ex) {
				log.warn("Could not refresh school cafeteria menu", ex);
				nextFetch = now + 60_000L;
			}
		}
		refreshedDate = today;
	}

	static Map<LocalDate, List<DailyDietVO>> parse(Document doc) throws IOException {
		Element widget = doc.selectFirst("#_JW_diet_basic");
		Element monday = widget == null ? null : widget.selectFirst("input[name=monday]");
		if (monday == null) throw new IOException("Missing menu week");
		LocalDate date = LocalDate.parse(monday.val(), DateTimeFormatter.ofPattern("uuuu.MM.dd"));
		Elements days = widget.select("._dietListWrap > li");
		if (days.size() != 7) throw new IOException("Incomplete menu week");
		Map<LocalDate, List<DailyDietVO>> week = new LinkedHashMap<>();
		for (Element day : days) {
			if (!date.format(DateTimeFormatter.ofPattern("MM.dd")).equals(day.select(".date").text()))
				throw new IOException("Unexpected menu date");
			List<DailyDietVO> rows = new ArrayList<>();
			Elements meals = day.select(".contWrap");
			if (meals.size() != 2) throw new IOException("Unexpected meal slots");
			for (int i = 0; i < meals.size(); i++) {
				Element meal = meals.get(i);
				if (meal.hasClass("noData")) continue;
				int before = rows.size();
				for (String field : new String[]{"main", "menu"}) {
					for (String name : meal.select("." + field).text().split(",")) {
						if (name.trim().isEmpty()) continue;
						DailyDietVO row = new DailyDietVO();
						row.setMenuName(name.trim());
						row.setMealTime(i == 0 ? "BREAKFAST" : "LUNCH");
						row.setCategory(field.equals("main") ? "MAIN" : "ETC");
						rows.add(row);
					}
				}
				if (before == rows.size()) throw new IOException("Missing menu text");
			}
			week.put(date, rows);
			date = date.plusDays(1);
		}
		return week;
	}
}
