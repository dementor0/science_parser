package com.mtuci.scienceParser.service;

import com.mtuci.scienceParser.model.Article;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ArticleService {
    private String strMonth;
    private int year;
    private LocalDate correctDate;
    private List<String> urlOnPublicationForTopicSearch = new ArrayList<>();
    private List<String> UrlOnPublicationForSearch = new ArrayList<>();
    private  String allNames;
    private  StringBuilder result = new StringBuilder();

    public Article parseTopicArticle(String request) {
        Article publication = new Article();

        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver driverTopic = new ChromeDriver(options);

        String url = "https://www.researchgate.net/topic/" + request + "/publications";
        driverTopic.get(url);

        WebDriverWait wait = new WebDriverWait(driverTopic, Duration.ofSeconds(10));
        By productSelector = By.cssSelector("#lite-page > main > section.lite-page__content");
        WebElement productElement = wait.until(ExpectedConditions.visibilityOfElementLocated(productSelector));
        List<WebElement> publicationElements = productElement.findElements(By.xpath("//*[@id=\"lite-page\"]/main/section[3]"));
        for (WebElement publicationElement : publicationElements) {
            List<WebElement> publicationTitle = publicationElement.findElements(By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-l.nova-legacy-e-text--family-display.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-inherit.nova-legacy-v-publication-item__title"));
            for (WebElement t : publicationTitle){
                publication.setTitle(t.getText());
            }
            List<WebElement> publicationType = publicationElement.findElements(By.cssSelector(".nova-legacy-e-badge.nova-legacy-e-badge--color-green.nova-legacy-e-badge--display-block.nova-legacy-e-badge--luminosity-high.nova-legacy-e-badge--size-l.nova-legacy-e-badge--theme-solid.nova-legacy-e-badge--radius-m.nova-legacy-v-publication-item__badge"));
            for (WebElement ty : publicationType){
                publication.setTitle(ty.getText());
            }
            List<WebElement> publicationAvailable = publicationElement.findElements(By.cssSelector(".nova-legacy-e-badge.nova-legacy-e-badge--color-green.nova-legacy-e-badge--display-block.nova-legacy-e-badge--luminosity-medium.nova-legacy-e-badge--size-l.nova-legacy-e-badge--theme-ghost.nova-legacy-e-badge--radius-m.nova-legacy-v-publication-item__badge"));
            for (WebElement a : publicationAvailable){
                publication.setTitle(a.getText());
            }
            List<WebElement> dateCompletion = publicationElement.findElements(By.cssSelector(".nova-legacy-e-list__item.nova-legacy-v-publication-item__meta-data-item"));
            for (WebElement date : dateCompletion){
                LocalDate datePublication = convertMonth(date);
                publication.setDateCompletion(Date.valueOf(datePublication.withDayOfMonth(1)));
            }
            List<WebElement> divName = publicationElement.findElements(By.cssSelector(".nova-legacy-e-list.nova-legacy-e-list--size-m.nova-legacy-e-list--type-inline.nova-legacy-e-list--spacing-none.nova-legacy-v-publication-item__person-list"));
            for (WebElement str : divName){
                List<WebElement> authors = str.findElements(By.cssSelector(".nova-legacy-v-person-inline-item__fullname"));
                if (authors.size() > 1){
                    int index = 0;
                    for (WebElement str1 : authors) {
                        result.append(str1.getText());
                        if (index < authors.size() - 1) {
                            result.append(", ");
                        }
                        index++;
                    }
                    allNames = result.toString().trim();
                    publication.setAuthors(allNames);
                    result.setLength(0);
                } else {
                    publication.setAuthors(str.getText());
                }
            }
            driverTopic.close();

            for(String i : urlOnPublicationForTopicSearch){
                WebDriver driver1 = new ChromeDriver(options);
                driver1.get(i);
                WebDriverWait wait1 = new WebDriverWait(driver1, Duration.ofSeconds(10));
                By selector = By.className("lite-page__content");
                WebElement elementPage = wait1.until(ExpectedConditions.visibilityOfElementLocated(selector));
                List<WebElement> div = elementPage.findElements(By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-m.nova-legacy-e-text--family-sans-serif.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-grey-800.research-detail-middle-section__abstract"));
                for(WebElement u : div){
                    publication.setAnnotation(u.getText());
                }
                List<WebElement> urlDownload = elementPage.findElements(By.xpath("//*[@id=\"lite-page\"]/main/section/section[1]/section/div[1]/div/div[2]/div[1]/a"));
                for (WebElement r : urlDownload){
                    String download = r.getAttribute("href");
                    String encodedUrl = download.replaceAll("'", URLEncoder.encode("'", StandardCharsets.UTF_8));
                    publication.setUrlForDownload(encodedUrl);
                }
                driver1.close();
            }
        }
        return null;
    }
    public Article parseArticle(String request, Integer numberOfPages){
        Article publication = new Article();

        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver driverSearch = new ChromeDriver(options);

        String url = "https://www.researchgate.net/search/publication?q=" + request;
        driverSearch.get(url);

        WebDriverWait wait = new WebDriverWait(driverSearch, Duration.ofSeconds(10));
        By productSelector = By.className("search-indent-container");
        WebElement productElementSearch = wait.until(ExpectedConditions.visibilityOfElementLocated(productSelector));

        List<WebElement> publicationElementsSearch = productElementSearch.findElements(By.cssSelector(".nova-legacy-o-stack.nova-legacy-o-stack--gutter-xs.nova-legacy-o-stack--spacing-none.nova-legacy-o-stack--no-gutter-outside"));
        for (WebElement searchPublicationElement : publicationElementsSearch) {
            List<WebElement> sPublicationTitle = searchPublicationElement.findElements(By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-l.nova-legacy-e-text--family-display.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-inherit.nova-legacy-v-publication-item__title"));
            for (WebElement st : sPublicationTitle) {
                publication.setTitle(st.getText());
            }
            List<WebElement> sPublicationType = searchPublicationElement.findElements(By.cssSelector(".nova-legacy-e-badge.nova-legacy-e-badge--color-green.nova-legacy-e-badge--display-block.nova-legacy-e-badge--luminosity-high.nova-legacy-e-badge--size-l.nova-legacy-e-badge--theme-solid.nova-legacy-e-badge--radius-m.nova-legacy-v-publication-item__badge"));
            for (WebElement sty : sPublicationType) {
                publication.setTitle(sty.getText());
            }
            List<WebElement> sDateCompletion = searchPublicationElement.findElements(By.cssSelector(".nova-legacy-e-list__item.nova-legacy-v-publication-item__meta-data-item:first-child"));
            for (WebElement sDate : sDateCompletion) {
                LocalDate sDatePublication = convertMonth(sDate);
                publication.setDateCompletion(Date.valueOf(sDatePublication.withDayOfMonth(1)));
            }
            List<WebElement> divName = searchPublicationElement.findElements(By.cssSelector(".nova-legacy-e-list.nova-legacy-e-list--size-m.nova-legacy-e-list--type-inline.nova-legacy-e-list--spacing-none.nova-legacy-v-publication-item__person-list"));
            for (WebElement str : divName) {
                List<WebElement> authors = str.findElements(By.cssSelector(".nova-legacy-v-person-inline-item__fullname"));
                if (authors.size() > 1) {
                    int index = 0;
                    for (WebElement str1 : authors) {
                        result.append(str1.getText());
                        if (index < authors.size() - 1) {
                            result.append(", ");
                        }
                        index++;
                    }
                    allNames = result.toString().trim();
                    publication.setAuthors(allNames);
                    result.setLength(0);
                } else {
                    publication.setAuthors(str.getText());
                }
            }

        }
        driverSearch.close();

        for(String i : UrlOnPublicationForSearch) {
            WebDriver sDriver1 = new ChromeDriver(options);
            sDriver1.get(i);
            WebDriverWait wait1 = new WebDriverWait(sDriver1, Duration.ofSeconds(10));
            By selector = By.className("lite-page__content");
            WebElement sElementPage = wait1.until(ExpectedConditions.visibilityOfElementLocated(selector));
            List<WebElement> sDiv = sElementPage.findElements(By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-m.nova-legacy-e-text--family-sans-serif.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-grey-800.research-detail-middle-section__abstract"));
            for (WebElement su : sDiv) {
                publication.setAnnotation(su.getText());
            }
            try {
                List<WebElement> sUrlDownload = sElementPage.findElements(By.xpath("//*[@id=\"lite-page\"]/main/section/section[1]/section/div[1]/div/div[2]/div[1]/a"));
                for (WebElement sr : sUrlDownload) {
                    String sDownload = sr.getAttribute("href");
                    if (!sDownload.endsWith("/download")) {
                        String encodedLink = sDownload.replaceAll("'", URLEncoder.encode("'", StandardCharsets.UTF_8));
                        publication.setUrlForDownload(encodedLink);
                    }
                }
            } catch (Exception ex) {
                System.out.println("Ссылка на статью отсутствует");
                publication.setUrlForDownload(i);
            }
            sDriver1.close();
        }
        return null;
    }
    public void findTopicUrlOnPublication(String request, Integer numberOfPages) {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver driverTopic = new ChromeDriver(options);

        String url = "https://www.researchgate.net/topic/" + request + "/publications";
        driverTopic.get(url);

        int k = 1;
        while (numberOfPages!=0) {
            WebDriverWait wait = new WebDriverWait(driverTopic, Duration.ofSeconds(20));
            By productSelector = By.cssSelector("#lite-page > main > section.lite-page__content");
            WebElement productElement = wait.until(ExpectedConditions.visibilityOfElementLocated(productSelector));
            List<WebElement> publicationElements = productElement.findElements(By.xpath("//*[@id=\"lite-page\"]/main/section[3]"));
            // ссылка на след страницу
            By numberOfTopicWebpage = By.cssSelector("#lite-page > main > section.lite-page__content > div:nth-child(4) > div > div > div:nth-child(100) > div > div > div > div:nth-child(1) > div > div:nth-child(12) > a");
            for (WebElement publicationElement : publicationElements) {
                log.info("Страница: {}", k);
                List<WebElement> element = publicationElement.findElements(By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-l.nova-legacy-e-text--family-display.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-inherit.nova-legacy-v-publication-item__title"));
                for (WebElement iter : element) {
                    List<WebElement> publicationInfo = iter.findElements(By.cssSelector(".nova-legacy-e-link.nova-legacy-e-link--color-inherit.nova-legacy-e-link--theme-bare"));
                    for (WebElement s : publicationInfo) {
                        String publicationUrl = s.getAttribute("href");
                        String encodedUrlForTopic = publicationUrl.replaceAll("'", URLEncoder.encode("'", StandardCharsets.UTF_8));
                        urlOnPublicationForTopicSearch.add(encodedUrlForTopic);
                    }
                }
            }
            try {
                WebElement number = wait.until(ExpectedConditions.visibilityOfElementLocated(numberOfTopicWebpage));
                driverTopic.get(number.getAttribute("href"));
            } catch (TimeoutException e) {
                log.error("Ошибка: {}", e.getMessage());
                System.out.println("Страница не найдена");
            }
            k++;
            numberOfPages--;
        }
        driverTopic.close();
    }
    public List<String> findSearchUrlForPublication(String request, Integer numberOfPages){
        WebElement lastItem, link;
        String href;
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver driverSearch = new ChromeDriver(options);

        String url = "https://www.researchgate.net/search/publication?q=" + request;
        driverSearch.get(url);

        int k = 1;
        while (numberOfPages!=0) {
            WebDriverWait wait = new WebDriverWait(driverSearch, Duration.ofSeconds(20));
            By productSelector = By.className("search-indent-container");
            WebElement productElementSearch = wait.until(ExpectedConditions.visibilityOfElementLocated(productSelector));
            List<WebElement> publicationElementsSearch = productElementSearch.findElements(By.cssSelector(".nova-legacy-o-stack.nova-legacy-o-stack--gutter-xs.nova-legacy-o-stack--spacing-none.nova-legacy-o-stack--no-gutter-outside"));
            // ссылка на след страницу
            List<WebElement> items = productElementSearch.findElements(By.cssSelector(".nova-legacy-c-button-group__item"));
            lastItem = items.get(items.size() - 1);

            for (WebElement searchPublicationElement : publicationElementsSearch) {
                log.info("Cтраница: {}", k);
                List<WebElement> element = searchPublicationElement.findElements(By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-l.nova-legacy-e-text--family-display.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-inherit.nova-legacy-v-publication-item__title"));
                for (WebElement iter : element) {
                    List<WebElement> sPublicationInfo = iter.findElements(By.cssSelector(".nova-legacy-e-link.nova-legacy-e-link--color-inherit.nova-legacy-e-link--theme-bare"));
                    for (WebElement searchUrl : sPublicationInfo) {
                        String publicationUrl = searchUrl.getAttribute("href");
                        String encodedUrlForSearch = publicationUrl.replaceAll("'", URLEncoder.encode("'", StandardCharsets.UTF_8));
                        UrlOnPublicationForSearch.add(encodedUrlForSearch);
                    }
                }
            }
            try {
                link = lastItem.findElement(By.tagName("a"));
                href = link.getAttribute("href");
                driverSearch.get(href);
            } catch (TimeoutException e) {
                log.error("Ошибка: {}", e.getMessage());
                System.out.println("Страница не найдена");
            }
            k++;
            numberOfPages--;
        }
        driverSearch.close();
        return UrlOnPublicationForSearch;
    }
    public Month convertToMonth(String monthStr) {
        switch (monthStr) {
            case "Jan":
                return Month.JANUARY;
            case "Feb":
                return Month.FEBRUARY;
            case "Mar":
                return Month.MARCH;
            case "Apr":
                return Month.APRIL;
            case "May":
                return Month.MAY;
            case "Jun":
                return Month.JUNE;
            case "Jul":
                return Month.JULY;
            case "Aug":
                return Month.AUGUST;
            case "Sep":
                return Month.SEPTEMBER;
            case "Oct":
                return Month.OCTOBER;
            case "Nov":
                return Month.NOVEMBER;
            case "Dec":
                return Month.DECEMBER;
            default:
                throw new IllegalArgumentException("Invalid month string: " + monthStr);
        }
    }
    public LocalDate convertMonth(WebElement date){
            String dateText = date.getText();
            String[] dateSplit = dateText.split(" ");
            strMonth = dateSplit[0];
            Month month = convertToMonth(strMonth);
            year = Integer.parseInt(dateSplit[1]);
            correctDate = LocalDate.of(year, month,1);
        return correctDate;
    }

}
