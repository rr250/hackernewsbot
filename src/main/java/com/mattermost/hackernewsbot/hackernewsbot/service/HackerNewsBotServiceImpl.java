package com.mattermost.hackernewsbot.hackernewsbot.service;

import com.google.gson.Gson;
import com.mattermost.hackernewsbot.hackernewsbot.domain.Attachment;
import com.mattermost.hackernewsbot.hackernewsbot.domain.News;
import com.mattermost.hackernewsbot.hackernewsbot.domain.SendPost;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class HackerNewsBotServiceImpl implements HackerNewsBotService {

    @Value("${bot_token}")
    private String botToken;

    @Value("${urlmeta_token}")
    private String urlMetaToken;

    @Value("${channel_id}")
    private String channelId;

    @Value("${mattermost_uri}")
    private String mattermostUri;

    @Override
    @Scheduled(cron="0 0 0 * * *",zone = "Asia/Kolkata")
//    @Scheduled(fixedDelay = 1000*600,initialDelay = 1000*0)

    public void sendDailyNews() throws IOException {
        JSONArray topNews = getTopNewsJSON();
        List<News> newsList = getNewsList(topNews);
        SendPost sendPost =  getSendPost(newsList);
        sendPost(sendPost);
    }

    @Scheduled(fixedDelay = 1000*14400,initialDelay = 1000*0)
    public void sendTopNewsPer4hr() throws IOException {
        JSONArray topNews = getTopNewsJSON();
        News news = getNews(topNews);
        List<News> newsList = new ArrayList<>();
        newsList.add(news);
        SendPost sendPost =  getSendPost(newsList);
        sendPost(sendPost);
    }

    private News getNews(JSONArray topNews) throws IOException {
        LocalDateTime now= LocalDateTime.now(ZoneId.of("Asia/Kolkata"));
        List<News> newsList = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        JSONObject responseJson = null;
        for(int i = 0; i<(Math.min(topNews.length(), 20)); i++) {
            HttpGet httpGet = new HttpGet("https://hacker-news.firebaseio.com/v0/item/" + topNews.get(i) + ".json");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String responseString = EntityUtils.toString(httpEntity);
            responseJson = new JSONObject(responseString);
            log.info("{}", responseJson);
            long createdAtUnix = responseJson.getLong("time");
            LocalDateTime createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAtUnix), ZoneId.of("Asia/Kolkata"));
            if(now.minusHours(4).isBefore(createdAt) && responseJson.has("url"))
                break;
        }
        News news = new News();
        assert responseJson != null;
        news.setId(responseJson.getLong("id"));
        news.setTitle(responseJson.getString("title"));
        JSONObject metaJson = getMeta(responseJson.getString("url"));
        news.setUrl(responseJson.getString("url"));
        if (metaJson.has("meta")) {
            if(metaJson.getJSONObject("meta").has("description"))
                news.setDescription(metaJson.getJSONObject("meta").getString("description"));
            if(metaJson.getJSONObject("meta").has("image"))
                news.setImage(metaJson.getJSONObject("meta").getString("image"));
            else if(metaJson.getJSONObject("meta").has("site") && metaJson.getJSONObject("meta").getJSONObject("site").has("favicon"))
                news.setImage(metaJson.getJSONObject("meta").getJSONObject("site").getString("favicon"));
        }

        return news;
    }

    private JSONArray getTopNewsJSON() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://hacker-news.firebaseio.com/v0/topstories.json");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        String responseString = EntityUtils.toString(httpEntity);
        JSONArray responseJson = new JSONArray(responseString);
        log.info("{}",responseJson);
        return responseJson;
    }

    private List<News> getNewsList(JSONArray topNews) throws IOException {
        List<News> newsList = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        for(int i = 0; i<(Math.min(topNews.length(), 7)); i++){
            News news = new News();
            HttpGet httpGet = new HttpGet("https://hacker-news.firebaseio.com/v0/item/"+topNews.get(i)+".json");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String responseString = EntityUtils.toString(httpEntity);
            JSONObject responseJson = new JSONObject(responseString);
            log.info("{}",responseJson);
            news.setId(responseJson.getLong("id"));
            news.setTitle(responseJson.getString("title"));
            if(responseJson.has("url")) {
                JSONObject metaJson = getMeta(responseJson.getString("url"));
                news.setUrl(responseJson.getString("url"));
                if (metaJson.has("meta")) {
                    if(metaJson.getJSONObject("meta").has("description"))
                        news.setDescription(metaJson.getJSONObject("meta").getString("description"));
                    if(metaJson.getJSONObject("meta").has("image"))
                        news.setImage(metaJson.getJSONObject("meta").getString("image"));
                    else if(metaJson.getJSONObject("meta").has("site") && metaJson.getJSONObject("meta").getJSONObject("site").has("favicon"))
                        news.setImage(metaJson.getJSONObject("meta").getJSONObject("site").getString("favicon"));
                }
            }
            newsList.add(news);
        }
        log.info("{}",newsList);
        return newsList;
    }

    private SendPost getSendPost(List<News> newsList){
        SendPost sendPost = new SendPost();
        for (News news : newsList) {
            Attachment attachment = new Attachment();
            attachment.setTitle(news.getTitle());
            attachment.setTitle_link(news.getUrl());
            attachment.setText(news.getDescription());
            attachment.setThumb_url(news.getImage());
            sendPost.getProps().getAttachments().add(attachment);
            log.info("{}",attachment.getTitle());
        }
        sendPost.setChannel_id(channelId);
        if(newsList.size() == 0)
            sendPost.setMessage("No top news for today");
        return sendPost;
    }

    private void sendPost(SendPost sendPost) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(mattermostUri+"/api/v4/posts");
        httpPost.addHeader("Authorization",botToken);
        Gson gson = new Gson();
        StringEntity postingString = new StringEntity(gson.toJson(sendPost),"UTF8");
        log.info("{}",gson.toJson(sendPost));
        httpPost.setEntity(postingString);
        httpPost.setHeader("Content-type", "application/json");
        HttpResponse httpResponse = httpClient.execute(httpPost);
        log.info("{}",httpResponse.getStatusLine());
    }

    private JSONObject getMeta(String url) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://api.urlmeta.org/?url="+url);
        httpGet.setHeader("Authorization",urlMetaToken);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        String responseString = EntityUtils.toString(httpEntity);
        JSONObject responseJson = new JSONObject(responseString);

        log.info("{}",responseJson);
        return responseJson;
    }
}
