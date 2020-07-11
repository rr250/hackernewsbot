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
public class Attachment {

    private String title;
    private String title_link;
    private String text;
    private String author_name;
    private String author_link;
    private String thumb_url = "https://pbs.twimg.com/profile_images/469397708986269696/iUrYEOpJ.png";
}
