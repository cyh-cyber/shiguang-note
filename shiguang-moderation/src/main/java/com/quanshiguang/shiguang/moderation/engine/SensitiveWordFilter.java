package com.quanshiguang.shiguang.moderation.engine;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * DFA 敏感词过滤器：把词库建成 Trie 树，一遍扫描出所有命中词
 */
@Component
public class SensitiveWordFilter {

    /** Trie 节点 */
    private static class Node {
        Map<Character, Node> children = new HashMap<>();
        boolean end = false;
    }

    private final Node root = new Node();

    public SensitiveWordFilter(ResourceLoader resourceLoader,
                               @org.springframework.beans.factory.annotation.Value("${moderation.sensitive-word-file}") String file)
            throws Exception {
        Resource resource = resourceLoader.getResource(file);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (StringUtils.isNotBlank(line) && !line.startsWith("#")) {
                    add(line);
                }
            }
        }
    }

    private void add(String word) {
        Node cur = root;
        for (char c : word.toCharArray()) {
            cur = cur.children.computeIfAbsent(c, k -> new Node());
        }
        cur.end = true;
    }

    /** 扫描文本，返回所有命中的词（去重） */
    public Set<String> match(String text) {
        Set<String> hits = new LinkedHashSet<>();
        if (StringUtils.isBlank(text)) {
            return hits;
        }
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            Node cur = root;
            int j = i;
            while (j < chars.length) {
                Node next = cur.children.get(chars[j]);
                if (next == null) {
                    break;
                }
                cur = next;
                if (cur.end) {
                    hits.add(text.substring(i, j + 1));
                }
                j++;
            }
        }
        return hits;
    }
}