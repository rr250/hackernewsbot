package com.mattermost.hackernewsbot.hackernewsbot.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Slf4j
public class SendPost {
    private String channel_id;
    private String thumb_url;
    private String title;
    private String title_url;


    private String message="For more news visit https://news.ycombinator.com/";
    private Attachments props = new Attachments();
}
