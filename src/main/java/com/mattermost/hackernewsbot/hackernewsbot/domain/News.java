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
public class News {
    private long id;
    private String title;
    private String url;
    private String description;
    private String image;
}
