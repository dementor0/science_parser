package com.mtuci.scienceParser.service;

import com.mtuci.scienceParser.model.Author;
import com.mtuci.scienceParser.model.AuthorInfo;
import com.mtuci.scienceParser.model.Publication;
import com.mtuci.scienceParser.repository.AuthorInfoRepository;
import com.mtuci.scienceParser.repository.AuthorRepository;
import com.mtuci.scienceParser.repository.PublicationRepository;
import io.github.bonigarcia.wdm.WebDriverManager;
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
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@Slf4j
public class PublicationService {
    private String strMonth;
    private int year;
    private LocalDate correctDate;

    @Autowired
    private PublicationRepository publicationRepository;
    @Autowired
    private AuthorRepository authorRepository;
    @Autowired
    private AuthorInfoRepository authorInfoRepository;
    public List<Publication> parsePublicationInTopic(String request, Integer numberOfPages) throws InterruptedException {
        WebElement pageElement = null;
        WebElement annotationText;
        List<Publication> publicationsList = new ArrayList<>();
        List<Author> authorsList;
        List<WebElement> authorsWebElementsList;

        List<String> urlOnPublicationForTopicSearch = findPublicationUrlInTopic(request,numberOfPages);

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver driverTopic = new ChromeDriver(options);
        driverTopic.manage().window().setSize(new Dimension(1, 1));
        driverTopic.manage().window().setPosition(new Point(-2000, 0));

        for (String str : urlOnPublicationForTopicSearch){
            driverTopic.manage().timeouts().pageLoadTimeout(4, TimeUnit.SECONDS);
            try {
                driverTopic.get(str);
            }catch (TimeoutException ignore){}

            Publication publication = new Publication();

            WebDriverWait wait = new WebDriverWait(driverTopic, Duration.ofSeconds(15));
            By productSelector = By.cssSelector("#lite-page > main");
            try {
                pageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(productSelector));
            } catch (TimeoutException ignore){
                driverTopic.get(str);
            }
           
            List<WebElement> publicationElements = pageElement.findElements(By.xpath("//*[@id=\"lite-page\"]/main/section"));
            for (WebElement publicationElement : publicationElements){
                By elementTitle = By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-xl.nova-legacy-e-text--family-display.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-grey-900.research-detail-header-section__title");
                WebElement publicationTitle = publicationElement.findElement(elementTitle);
                publication.setTitle(publicationTitle.getText());

                By elementType = By.cssSelector(".nova-legacy-e-badge.nova-legacy-e-badge--color-green.nova-legacy-e-badge--display-inline.nova-legacy-e-badge--luminosity-high.nova-legacy-e-badge--size-l.nova-legacy-e-badge--theme-solid.nova-legacy-e-badge--radius-m.research-detail-header-section__badge");
                WebElement publicationType = publicationElement.findElement(elementType);
                publication.setType(publicationType.getText());

                try {
                    By elementAvailable = By.cssSelector(".nova-legacy-e-badge.nova-legacy-e-badge--color-green.nova-legacy-e-badge--display-inline.nova-legacy-e-badge--luminosity-medium.nova-legacy-e-badge--size-l.nova-legacy-e-badge--theme-ghost.nova-legacy-e-badge--radius-m.research-detail-header-section__badge");
                    WebElement publicationAvailable = publicationElement.findElement(elementAvailable);
                    publication.setTextAvailable(publicationAvailable.getText());
                } catch (NoSuchElementException e) {
                    e.getMessage();
                    publication.setTextAvailable("Отсутствует");
                }

                WebElement dateElement = publicationElement.findElement(By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-m.nova-legacy-e-text--family-sans-serif.nova-legacy-e-text--spacing-xxs.nova-legacy-e-text--color-grey-700"));
                WebElement dateCompletion = dateElement.findElement(By.cssSelector(".nova-legacy-e-list__item:first-child"));
                LocalDate datePublication = convertMonth(dateCompletion);
                publication.setDateCompletion(Date.valueOf(datePublication));

                By annotationElement = By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-m.nova-legacy-e-text--family-sans-serif.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-grey-800.research-detail-middle-section__abstract");
                try {
                    annotationText = pageElement.findElement(annotationElement);
                    publication.setAnnotation(annotationText.getText());
                }catch (NoSuchElementException e){
                    publication.setAnnotation("Отсутствует");
                }
                try {
                    By urlElement = By.xpath("//*[@id=\"lite-page\"]/main/section/section[1]/section/div[1]/div/div[2]/div[1]/a");
                    WebElement urlOnPage = pageElement.findElement(urlElement);
                    String urlForDownload = urlOnPage.getAttribute("href");
                    if (!urlForDownload.endsWith("/download")) {
                        String encodedUrlForDownload = urlForDownload.replaceAll("'", URLEncoder.encode("'", StandardCharsets.UTF_8));
                        publication.setUrlForDownload(encodedUrlForDownload);
                    }
                    else{
                        publication.setUrlForDownload("Ссылка для скачивания отсутствует");
                    }
                } catch (Exception e){
                    e.getMessage();
                    publication.setUrlForDownload("Ссылка для скачивания отсутствует");
                }
                publication.setUrlOnPublication(str);

                publicationRepository.save(publication);

                authorsWebElementsList = publicationElement.findElements(By.cssSelector(".nova-legacy-l-flex__item.research-detail-author-list__item.research-detail-author-list__item--has-image"));
                authorsList = parseAuthors(authorsWebElementsList, publication.getId());
                authorRepository.saveAll(authorsList);

               publication.setAuthors(authorsList);
                publicationRepository.save(publication);
                publication = publicationRepository.findById(publication.getId()).orElse(null);
            }
            publicationsList.add(publication);
        }
        publicationRepository.saveAll(publicationsList);
        driverTopic.close();
        return publicationsList;
    }
    public List<Publication> parsePublicationInSearch(String request, Integer numberOfPages) throws InterruptedException {
        List<Publication> publicationsList = new ArrayList<>();
        List<Author> authorsList;
        List<WebElement> authorsWebElementsList;

        List<String> urlOnPublicationForSearch = findPublicationUrlInSearch(request,numberOfPages);

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver driverSearch = new ChromeDriver(options);
        driverSearch.manage().window().setSize(new Dimension(1, 1));
        driverSearch.manage().window().setPosition(new Point(-2000, 0));

        for (String str : urlOnPublicationForSearch){
            driverSearch.manage().timeouts().pageLoadTimeout(2, TimeUnit.SECONDS);
            try {
                driverSearch.get(str);
            }catch (TimeoutException ignore){}

            Publication publication = new Publication();

            WebDriverWait wait = new WebDriverWait(driverSearch, Duration.ofSeconds(10));
            By productSelector = By.cssSelector("#lite-page > main");
            WebElement pageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(productSelector));
            List<WebElement> publicationElements = pageElement.findElements(By.xpath("//*[@id=\"lite-page\"]/main/section"));
            for (WebElement publicationElement : publicationElements){
                By elementTitle = By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-xl.nova-legacy-e-text--family-display.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-grey-900.research-detail-header-section__title");
                WebElement publicationTitle = publicationElement.findElement(elementTitle);
                publication.setTitle(publicationTitle.getText());

                By elementType = By.cssSelector(".nova-legacy-e-badge.nova-legacy-e-badge--color-green.nova-legacy-e-badge--display-inline.nova-legacy-e-badge--luminosity-high.nova-legacy-e-badge--size-l.nova-legacy-e-badge--theme-solid.nova-legacy-e-badge--radius-m.research-detail-header-section__badge");
                WebElement publicationType = publicationElement.findElement(elementType);
                publication.setType(publicationType.getText());

                try {
                    By elementAvailable = By.cssSelector(".nova-legacy-e-badge.nova-legacy-e-badge--color-green.nova-legacy-e-badge--display-inline.nova-legacy-e-badge--luminosity-medium.nova-legacy-e-badge--size-l.nova-legacy-e-badge--theme-ghost.nova-legacy-e-badge--radius-m.research-detail-header-section__badge");
                    WebElement publicationAvailable = publicationElement.findElement(elementAvailable);
                    publication.setTextAvailable(publicationAvailable.getText());
                } catch (NoSuchElementException e) {
                    e.getMessage();
                    publication.setTextAvailable("Отсутсвует");
                }

                WebElement dateElement = publicationElement.findElement(By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-m.nova-legacy-e-text--family-sans-serif.nova-legacy-e-text--spacing-xxs.nova-legacy-e-text--color-grey-700"));
                WebElement dateCompletion = dateElement.findElement(By.cssSelector(".nova-legacy-e-list__item:first-child"));
                LocalDate datePublication = convertMonth(dateCompletion);
                publication.setDateCompletion(Date.valueOf(datePublication));

                By annotationElement = By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-m.nova-legacy-e-text--family-sans-serif.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-grey-800.research-detail-middle-section__abstract");
                WebElement annotationText = pageElement.findElement(annotationElement);
                publication.setAnnotation(annotationText.getText());

                try {
                    By urlElement = By.xpath("//*[@id=\"lite-page\"]/main/section/section[1]/section/div[1]/div/div[2]/div[1]/a");
                    WebElement urlOnPage = pageElement.findElement(urlElement);
                    String urlForDownload = urlOnPage.getAttribute("href");
                    if (!urlForDownload.endsWith("/download")) {
                        String encodedUrlForDownload = urlForDownload.replaceAll("'", URLEncoder.encode("'", StandardCharsets.UTF_8));
                        publication.setUrlForDownload(encodedUrlForDownload);
                    }
                    else{
                        publication.setUrlForDownload("Ссылка для скачивания отсутствует");
                    }
                } catch (Exception e){
                    e.getMessage();
                    publication.setUrlForDownload("Ссылка для скачивания отсутствует");
                }
                publication.setUrlOnPublication(str);

                authorsWebElementsList = publicationElement.findElements(By.cssSelector(".nova-legacy-l-flex__item.research-detail-author-list__item.research-detail-author-list__item--has-image"));
                authorsList = parseAuthors(authorsWebElementsList, publication.getId());
                authorRepository.saveAll(authorsList);

                publication.setAuthors(authorsList);
                publicationRepository.save(publication);
            }
             publicationsList.add(publication);
        }
        publicationRepository.saveAll(publicationsList);
        driverSearch.close();
        return publicationsList;
    }
    public List<Author> parseAuthors(List<WebElement> authorsWebElementsList, Long id) {
        List<Author> authorList = new ArrayList<>();

        for (WebElement secondElement : authorsWebElementsList) {
            Author author = new Author();
            try {
                List<WebElement> authorsElement = secondElement.findElements(By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-m.nova-legacy-e-text--family-display.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-inherit.nova-legacy-v-person-list-item__title"));
                for (WebElement elementAuthors : authorsElement) {
                    List<WebElement> informationAboutAuthor = elementAuthors.findElements(By.cssSelector(".nova-legacy-e-link.nova-legacy-e-link--color-inherit.nova-legacy-e-link--theme-bare"));
                    for (WebElement elementInformation : informationAboutAuthor) {
                        author.setName(elementInformation.getText());
                        author.setUrl(elementInformation.getAttribute("href"));
                    }
                }
                author.setIdPublication(id);
                authorList.add(author);
                authorRepository.save(author);
            } catch (Exception e){
                e.getMessage();
                author.setName("Автор не найден");
                author.setUrl("Отсутствует");
                log.warn("Автор не найден", e);
            }
        }
        return authorList;
    }
    public AuthorInfo parseAuthorInfo(String request) {
        AuthorInfo authorInfo = new AuthorInfo();
        List<Publication> publications = new ArrayList<>();

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver driverAuthor = new ChromeDriver(options);

        String authorNameInRequest = request.replace(" ","+");
        String url = "https://www.researchgate.net/search.Search.html?query=" + authorNameInRequest + "&type=researcher";
        String encodedUrlOnAuthorResearch = request.replace(" ","-");
        String urlOnAuthorPublication = "https://www.researchgate.net/profile/" + encodedUrlOnAuthorResearch + "/research";
        String urlOnAuthorProfile = "https://www.researchgate.net/profile/" + encodedUrlOnAuthorResearch;

        try{
            driverAuthor.manage().timeouts().pageLoadTimeout(2, TimeUnit.SECONDS);
            try {
                driverAuthor.get(url);
            } catch (TimeoutException e){}
            //driverAuthor.navigate().refresh();
            WebDriverWait wait = new WebDriverWait(driverAuthor, Duration.ofSeconds(3));
            By authorSelector = By.cssSelector("body");
            WebElement pageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(authorSelector));

            WebElement buttonLogIn = pageElement.findElement(By.cssSelector(".header-login-item-link.gtm-header-login"));
            buttonLogIn.click();
            WebElement logInForm = pageElement.findElement(By.xpath("/html/body/div[5]"));
            logIn(logInForm);

        } catch (NoSuchElementException e){
            e.getMessage();
            log.warn("Автор не найден", e);
        } catch (TimeoutException e){}

        try {
            authorInfo.setUrl(urlOnAuthorProfile);
            driverAuthor.manage().timeouts().pageLoadTimeout(2, TimeUnit.SECONDS);
            driverAuthor.get(urlOnAuthorPublication);

            By authorInfoSelector = By.cssSelector("#page-container");
            WebDriverWait wait = new WebDriverWait(driverAuthor, Duration.ofSeconds(3));
            WebElement pageElementInfo = wait.until(ExpectedConditions.visibilityOfElementLocated(authorInfoSelector));

            WebElement authorName = pageElementInfo.findElement(By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-xl.nova-legacy-e-text--family-display.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-grey-900"));
            authorInfo.setName(authorName.getText());

            List<WebElement> valuesOfStatistic = pageElementInfo.findElements(By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-m.nova-legacy-e-text--family-sans-serif.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-inherit"));
            authorInfo.setResearchInterestScore(Float.valueOf(valuesOfStatistic.get(0).getText()));
            authorInfo.setCitations(valuesOfStatistic.get(1).getText());
            authorInfo.setHIndex(Long.valueOf(valuesOfStatistic.get(2).getText()));
            valuesOfStatistic.clear();

            List<WebElement> amountPublicationDiv = pageElementInfo.findElements(By.cssSelector(".nova-legacy-e-text.nova-legacy-e-text--size-m.nova-legacy-e-text--family-sans-serif.nova-legacy-e-text--spacing-none.nova-legacy-e-text--color-inherit.nova-legacy-c-nav__item-label"));
            String text = amountPublicationDiv.get(1).getText();
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(text);
            matcher.find();
            String numberString = matcher.group();
            authorInfo.setAmountPublication(Long.parseLong(numberString));

            List<WebElement> publicationDiv = pageElementInfo.findElements(By.cssSelector(".nova-legacy-e-link.nova-legacy-e-link--color-inherit.nova-legacy-e-link--theme-bare"));
            int previousSize = 0;
            int currentSize = publicationDiv.size();
            while (currentSize > previousSize) {
                JavascriptExecutor jsExecutor = (JavascriptExecutor) driverAuthor;
                jsExecutor.executeScript("window.scrollTo(0, document.body.scrollHeight)");
                Thread.sleep(1000);
                publicationDiv = pageElementInfo.findElements(By.cssSelector(".nova-legacy-e-link.nova-legacy-e-link--color-inherit.nova-legacy-e-link--theme-bare"));
                for (int i = previousSize; i < currentSize; i++) {
                    WebElement iter = publicationDiv.get(i);
                    Publication publication = new Publication();
                    String link = iter.getAttribute("href");
                    if (!iter.getText().contains("Source") && link.contains("publication")) {
                        publication.setTitle(iter.getText());
                        publication.setUrlOnPublication(link);
                    }
                    publications.add(publication);
                    authorInfo.setPublications(publications);
                }
                publicationRepository.saveAll(publications);
                authorInfoRepository.save(authorInfo);

                previousSize = currentSize;
                currentSize = publicationDiv.size();
            }
        } catch (TimeoutException e){}
        catch (NoSuchElementException e){
            Publication publication = new Publication();
            authorInfo.setName("Отсутствует");
            authorInfo.setUrl("Отсутствует");
            authorInfo.setCitations("Отсутствует");
            authorInfo.setHIndex(0L);
            authorInfo.setAmountPublication(0L);
            publication.setTitle("Отсутствует");
            publication.setUrlOnPublication("Отсутствует");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        driverAuthor.close();
        return authorInfo;
    }

    private List<String> findPublicationUrlInTopic(String request, Integer numberOfPages) {
        List<String> urlOnPublicationForTopicSearch = new ArrayList<>();
        WebElement lastItem, link;
        String href;
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver driverTopic = new ChromeDriver(options);
        driverTopic.manage().window().setSize(new Dimension(1, 1));
        driverTopic.manage().window().setPosition(new Point(-2000, 0));

        String url = "https://www.researchgate.net/topic/" + request + "/publications";
        driverTopic.manage().timeouts().pageLoadTimeout(3, TimeUnit.SECONDS);
        try {
            driverTopic.get(url);
            driverTopic.navigate().refresh();
        }catch (TimeoutException ignore){}

        int k = 1;
        while (numberOfPages!=0) {
            WebDriverWait wait = new WebDriverWait(driverTopic, Duration.ofSeconds(10));
            By productSelector = By.cssSelector("#lite-page > main > section.lite-page__content");
            WebElement pageElement = wait.until(ExpectedConditions.visibilityOfElementLocated(productSelector));
            List<WebElement> publicationElements = pageElement.findElements(By.xpath("//*[@id=\"lite-page\"]/main/section[3]"));

            // ссылка на след страницу
            WebElement divPages = pageElement.findElement(By.cssSelector(".nova-legacy-c-button-group.nova-legacy-c-button-group--wrap.nova-legacy-c-button-group--gutter-m.nova-legacy-c-button-group--orientation-horizontal.nova-legacy-c-button-group--width-auto"));
            List<WebElement> items = divPages.findElements(By.cssSelector(".nova-legacy-c-button-group__item"));
            lastItem = items.get(items.size() - 1);

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
                link = lastItem.findElement(By.tagName("a"));
                href = link.getAttribute("href");
                driverTopic.get(href);
            } catch (TimeoutException e) {
                e.getMessage();
                log.warn("Страница не найдена");
            }
            k++;
            numberOfPages--;
        }
        driverTopic.close();
        return urlOnPublicationForTopicSearch;
    }
    private List<String> findPublicationUrlInSearch(String request, Integer numberOfPages) throws InterruptedException {
        List<String> urlOnPublicationForSearch = new ArrayList<>();
        WebElement lastItem, link;
        String href;
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        WebDriver driverSearch = new ChromeDriver(options);
        driverSearch.manage().window().setSize(new Dimension(1, 1));
        driverSearch.manage().window().setPosition(new Point(-2000, 0));

        String url = "https://www.researchgate.net/search/publication?q=" + request;
        driverSearch.manage().timeouts().pageLoadTimeout(1, TimeUnit.SECONDS);
        try {
            driverSearch.get(url);
            driverSearch.navigate().refresh();
        }catch (TimeoutException ignore){}


        int k = 1;
        while (numberOfPages!=0) {
            WebDriverWait wait = new WebDriverWait(driverSearch, Duration.ofSeconds(10));
            By productSelector = By.className("search-indent-container");
            WebElement pageElementSearch = wait.until(ExpectedConditions.visibilityOfElementLocated(productSelector));
            List<WebElement> publicationElementsSearch = pageElementSearch.findElements(By.cssSelector(".nova-legacy-o-stack.nova-legacy-o-stack--gutter-xs.nova-legacy-o-stack--spacing-none.nova-legacy-o-stack--no-gutter-outside"));
            // ссылка на след страницу
            List<WebElement> items = pageElementSearch.findElements(By.cssSelector(".nova-legacy-c-button-group__item"));
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
                e.getMessage();
                log.warn("Страница не найдена");
            }
            k++;
            numberOfPages--;
        }
        driverSearch.close();
        return urlOnPublicationForSearch;
    }
    private void logIn(WebElement form){
        WebElement inputEmail = form.findElement(By.id("input-header-login"));
        inputEmail.sendKeys("d.v.chernyshov@edu.mtuci.ru");
        WebElement inputPassword = form.findElement(By.id("input-header-password"));
        inputPassword.sendKeys("PasswordForGate415");
        WebElement buttonLogIn = form.findElement(By.cssSelector(".nova-legacy-c-button.nova-legacy-c-button--align-center.nova-legacy-c-button--radius-m.nova-legacy-c-button--size-m.nova-legacy-c-button--color-blue.nova-legacy-c-button--theme-solid.nova-legacy-c-button--width-full"));
        buttonLogIn.click();
    }
    private Month convertToMonth(String monthStr) {
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
    private LocalDate convertMonth(WebElement date){
            String dateText = date.getText();
            String[] dateSplit = dateText.split(" ");
            strMonth = dateSplit[0];
            Month month = convertToMonth(strMonth);
            year = Integer.parseInt(dateSplit[1]);
            correctDate = LocalDate.of(year, month,1);
        return correctDate;
    }

}
