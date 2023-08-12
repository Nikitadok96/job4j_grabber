package grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import grabber.utils.DateTimeParser;
import grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);
    private static final int PAGE_COUNT = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> list = habrCareerParse.parseToPage();
        list.forEach(System.out::println);
    }

    private List<Post> parseToPage() {
        ArrayList<Post> postList = new ArrayList<>();
        for (int i = 1; i <= PAGE_COUNT; i++) {
            postList.addAll(list(String.format(PAGE_LINK + "?page=%s", i)));
        }
        return postList;
    }

    private String retrieveDescription(String link) throws IOException {
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

    @Override
    public List<Post> list(String link) {
        List<Post> list = new ArrayList<>();
        Connection connection = Jsoup.connect(link);
        Document document;
        try {
            document = connection.get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Elements rows = document.select(".vacancy-card__inner");
        rows.forEach(row -> {
            Element dateElement = row.select(".vacancy-card__date").first();
            HabrCareerDateTimeParser timeParser = new HabrCareerDateTimeParser();
            LocalDateTime dateTime = timeParser.parse(dateElement.child(0).attr("datetime"));
            Element titleElement = row.select(".vacancy-card__title").first();
            Element linkElement = titleElement.child(0);
            String vacancyName = titleElement.text();
            String linkRef = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            try {
                String desc = retrieveDescription(linkRef);
                list.add(new Post(vacancyName, linkRef, desc, dateTime));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        return list;
    }
}
