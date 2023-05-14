package com.mtuci.scienceParser.service;

import com.mtuci.scienceParser.model.Article;
import com.mtuci.scienceParser.model.Author;
import com.mtuci.scienceParser.repository.ArticleRepository;
import com.mtuci.scienceParser.repository.AuthorRepository;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class ArticleService {
    private String strMonth;
    private int year;
    private LocalDate correctDate;
    private  String allNames;


    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private AuthorRepository authorRepository;
    public List<Article> parsePublication(String request, Integer numberOfPages){
        List<Article> articleList = new ArrayList<>();
        Article publication = new Article();
        List<String> urlOnPublicationForTopicSearch = findPublicationUrlInTopic(request,numberOfPages);

        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver driverTopic = new ChromeDriver(options);
        for (String str : urlOnPublicationForTopicSearch){
            driverTopic.get(str);

            WebDriverWait wait = new WebDriverWait(driverTopic, Duration.ofSeconds(10));
            By productSelector = By.cssSelector("#lite-page > main");
            WebElement productElement = wait.until(ExpectedConditions.visibilityOfElementLocated(productSelector));
            List<WebElement> publicationElements = productElement.findElements(By.xpath("//*[@id=\"lite-page\"]/main/section"));
            for (WebElement publicationElement : publicationElements){
                By publicationTitle = By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-xl.nova-legacy-e-text--family-display.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-grey-900.research-detail-header-section__title");
                WebElement t = publicationElement.findElement(publicationTitle);
                log.info("Тайтл {}", t.getText());
                publication.setTitle(t.getText());

                By publicationType = By.cssSelector(".nova-legacy-e-badge.nova-legacy-e-badge--color-green.nova-legacy-e-badge--display-inline.nova-legacy-e-badge--luminosity-high.nova-legacy-e-badge--size-l.nova-legacy-e-badge--theme-solid.nova-legacy-e-badge--radius-m.research-detail-header-section__badge");
                WebElement ty = publicationElement.findElement(publicationType);
                log.info("Тип {}", ty.getText());

                try {
                    By publicationAvailable = By.cssSelector(".nova-legacy-e-badge.nova-legacy-e-badge--color-green.nova-legacy-e-badge--display-inline.nova-legacy-e-badge--luminosity-medium.nova-legacy-e-badge--size-l.nova-legacy-e-badge--theme-ghost.nova-legacy-e-badge--radius-m.research-detail-header-section__badge");
                    WebElement a = publicationElement.findElement(publicationAvailable);
                    log.info("Доступ {}", a.getText());
                } catch (NoSuchElementException e) {
                    e.getMessage();
                    System.out.println("Тип отсутствует");
                }

                WebElement dateElement = publicationElement.findElement(By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-m.nova-legacy-e-text--family-sans-serif.nova-legacy-e-text--spacing-xxs.nova-legacy-e-text--color-grey-700"));
                WebElement dateCompletion = dateElement.findElement(By.cssSelector(".nova-legacy-e-list__item:first-child"));
                LocalDate datePublication = convertMonth(dateCompletion);
                log.info("Дата {}", datePublication);


                By annotationElement = By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-m.nova-legacy-e-text--family-sans-serif.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-grey-800.research-detail-middle-section__abstract");
                WebElement annotationText = productElement.findElement(annotationElement);
                log.info("Аннотация {}", annotationText.getText());

                try {
                    By urlElement = By.xpath("//*[@id=\"lite-page\"]/main/section/section[1]/section/div[1]/div/div[2]/div[1]/a");
                    WebElement urlOnPage = productElement.findElement(urlElement);
                    String urlForDownload = urlOnPage.getAttribute("href");
                    if (!urlForDownload.endsWith("/download")) {
                        String encodedUrlForDownload = urlForDownload.replaceAll("'", URLEncoder.encode("'", StandardCharsets.UTF_8));
                        log.info("Ссылка {}", encodedUrlForDownload);
                    }
                } catch (Exception e){
                    e.getMessage();
                    System.out.println("Ссылка для скачивания отсутствует");
                    log.info("Ссылка {}", str);
                }
                List<WebElement> authorsWebElementsList = publicationElement.findElements(By.cssSelector(".nova-legacy-l-flex__item.research-detail-author-list__item.research-detail-author-list__item--has-image"));

                List<Author> authorList = parseAuthors(authorsWebElementsList, publication.getId());
                authorRepository.saveAll(authorList);
                publication.setAuthors(authorList);
                articleList.add(publication);
           }
        }
        articleRepository.saveAll(articleList);
        driverTopic.close();
        return articleList;
    }

    public List<String> findPublicationUrlInTopic(String request, Integer numberOfPages) {
        List<String> urlOnPublicationForTopicSearch = new ArrayList<>();
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver driverTopic = new ChromeDriver(options);

        String url = "https://www.researchgate.net/topic/" + request + "/publications";
        driverTopic.get(url);

        int k = 1;
        while (numberOfPages!=0) {
            WebDriverWait wait = new WebDriverWait(driverTopic, Duration.ofSeconds(10));
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
        return urlOnPublicationForTopicSearch;
    }
    public List<String> findPublicationUrlInSearch(String request, Integer numberOfPages){
        List<String> urlOnPublicationForSearch = new ArrayList<>();
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
            WebDriverWait wait = new WebDriverWait(driverSearch, Duration.ofSeconds(10));
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
                        urlOnPublicationForSearch.add(encodedUrlForSearch);
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
        return urlOnPublicationForSearch;
    }
    public List<Author> parseAuthors(List<WebElement> authorsWebElementsList, Long id) {
        List<Author> authorList = new ArrayList<>();
        Author author = new Author();

        for (WebElement secondElement : authorsWebElementsList) {
            List<WebElement> authorsElement = secondElement.findElements(By.cssSelector(".nova-legacy-v-person-list-item__align-content"));
            for (WebElement elementAuthors : authorsElement) {
                List<WebElement> informationAboutAuthor = elementAuthors.findElements(By.cssSelector(".nova-legacy-e-link.nova-legacy-e-link--color-inherit.nova-legacy-e-link--theme-bare"));
                for (WebElement elementInformation : informationAboutAuthor) {
                    System.out.println(elementInformation.getText());
                    System.out.println(elementInformation.getAttribute("href"));
                }
            }
            authorList.add(author);
        }
        return authorList;
    }
    public Month convertToMonth(String monthStr) {
        switch (monthStr) {
            case "January":
                return Month.JANUARY;
            case "February":
                return Month.FEBRUARY;
            case "March":
                return Month.MARCH;
            case "April":
                return Month.APRIL;
            case "May":
                return Month.MAY;
            case "June":
                return Month.JUNE;
            case "July":
                return Month.JULY;
            case "August":
                return Month.AUGUST;
            case "September":
                return Month.SEPTEMBER;
            case "October":
                return Month.OCTOBER;
            case "November":
                return Month.NOVEMBER;
            case "December":
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
