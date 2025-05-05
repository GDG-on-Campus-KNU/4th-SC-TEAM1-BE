package com.gdg.Todak.diary.util;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class MBTISelector {

    public String select() {
        long seed = System.currentTimeMillis();
        Random rand = new Random(seed);

        List<String> types = List.of("EI", "SN", "TF", "JP");

        String mbti = "";
        for (int i = 0; i < 4; i++) {
            mbti += types.get(i).charAt(rand.nextInt(2));
        }

        return mbti;
    }
}
