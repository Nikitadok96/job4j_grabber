package quartz;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;


public class HabrCareerParse {
    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        parseToPage(5);
    }

    private static void parseToPage(int page) throws IOException {
        for (int i = 1; i <= page; i++) {
            Connection connection = Jsoup.connect(String.format(PAGE_LINK + "?page=%s", i));
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element dateElement = row.select(".vacancy-card__date").first();
                HabrCareerDateTimeParser timeParser = new HabrCareerDateTimeParser();
                LocalDateTime dateTime = timeParser.parse(dateElement.child(0).attr("datetime"));
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                try {
                    String desc = retrieveDescription(link);
                    System.out.printf("%s %s %s%n%s", vacancyName, link, dateTime, desc);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private static String retrieveDescription(String link) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements rows = document.select(".vacancy-description__text");
        rows.forEach(row -> {
            Elements textRows = row.select(".style-ugc");
            textRows.forEach(textRow -> stringBuilder.append(textRow.text()).append("\n"));
        });
        return stringBuilder.toString();
    }
}
