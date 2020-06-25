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
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class HackerNewsBotServiceImpl implements HackerNewsBotService {

    @Value("${bot_token}")
    private String botToken;

    @Value("${channel_id}")
    private String channelId;

    @Value("${mattermost_uri}")
    private String mattermostUri;

//    @Override
//    @Scheduled(fixedDelay = 1000*60,initialDelay = 1000*60)
//    public  void callCloud() throws IOException {
//        CloseableHttpClient httpClient = HttpClients.createDefault();
//        HttpGet httpGet = new HttpGet("https://clistbot.el.r.appspot.com/");
//        httpClient.execute(httpGet);
//    }


    @Override
//    @Scheduled(cron="0 0 0 * * *",zone = "Asia/Kolkata")
    @Scheduled(fixedDelay = 1000*600,initialDelay = 1000*0)

    public void sendDailyNews() throws IOException {
        JSONArray topNews = getTopNewsJSON();
        List<News> newsList = getNews(topNews);
        SendPost sendPost =  getSendPost(newsList);
        sendPost(sendPost);
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

    private List<News> getNews(JSONArray topNews) throws IOException {
        List<News> newsList = new ArrayList<>();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        for(int i = 0; i<(Math.min(topNews.length(), 20)); i++){
            News news = new News();
            HttpGet httpGet = new HttpGet("https://hacker-news.firebaseio.com/v0/item/"+topNews.get(i)+".json");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String responseString = EntityUtils.toString(httpEntity);
            JSONObject responseJson = new JSONObject(responseString);
            news.setId(responseJson.getLong("id"));
            news.setTitle(responseJson.getString("title"));
            news.setUrl(responseJson.has("url")?responseJson.getString("url"):null);
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
            if (news.getUrl() != null) {
                attachment.setTitle_link(news.getUrl());
            }
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
}
