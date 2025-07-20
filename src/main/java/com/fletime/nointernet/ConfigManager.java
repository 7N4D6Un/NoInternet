package com.fletime.nointernet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 配置管理器，负责加载和管理白名单配置
 */
public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_FILE = "nointernet.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    private static List<String> whitelist = new ArrayList<>();
    private static boolean configLoaded = false;
    private static boolean configValid = false;
    private static boolean logBlockedRequests = true;
    
    /**
     * 加载白名单配置
     */
    public static void loadConfig() {
        try {
            // 获取 config 目录路径
            Path configDir = Paths.get("config");
            Path configFile = configDir.resolve(CONFIG_FILE);
            
            // 如果配置文件不存在，创建默认配置文件
            if (!Files.exists(configFile)) {
                createDefaultConfig(configDir, configFile);
            }
            
            // 读取配置文件
            JsonObject config = GSON.fromJson(Files.newBufferedReader(configFile), JsonObject.class);
            if (config.has("whitelist")) {
                whitelist.clear();
                config.getAsJsonArray("whitelist").forEach(element -> 
                    whitelist.add(element.getAsString()));
                LOGGER.info("[NoInternet] 成功加载白名单配置，包含 " + whitelist.size() + " 个域名");
                configValid = true;
                
                // 读取是否记录拦截请求的选项
                if (config.has("logBlockedRequests")) {
                    logBlockedRequests = config.get("logBlockedRequests").getAsBoolean();
                } else {
                    logBlockedRequests = true; // 默认值
                }
            } else {
                configValid = false;
            }
            
            configLoaded = true;
            
        } catch (Exception e) {
            LOGGER.error("[NoInternet] 加载配置文件失败", e);
            configValid = false;
            configLoaded = true;
        }
    }
    
    /**
     * 创建默认配置文件
     */
    private static void createDefaultConfig(Path configDir, Path configFile) throws IOException {
        // 确保 config 目录存在
        if (!Files.exists(configDir)) {
            Files.createDirectories(configDir);
        }
        
        // 创建默认配置
        JsonObject defaultConfig = new JsonObject();
        List<String> defaultWhitelist = new ArrayList<>();
        defaultWhitelist.add("$emojang.com"); // MoJang 官方
        defaultWhitelist.add("$eminecraft.net"); // MoJang 官方
        defaultWhitelist.add("$ctoriifind.json"); // ToriiFind 数据更新
        defaultWhitelist.add("$clynn.json"); // ToriiFind 数据更新
        defaultConfig.add("whitelist", GSON.toJsonTree(defaultWhitelist));
        defaultConfig.addProperty("logBlockedRequests", true);
        
        // 写入配置文件
        try (BufferedWriter writer = Files.newBufferedWriter(configFile)) {
            GSON.toJson(defaultConfig, writer);
        }
    }
    
    /**
     * 检查 URI 是否在白名单中
     */
    public static boolean isWhitelisted(URI uri) {
        if (!configLoaded) {
            loadConfig();
        }
        
        // 如果配置文件无效，不拦截任何请求
        if (!configValid) {
            return true;
        }
        
        String host = uri.getHost();
        if (host == null) {
            return false;
        }
        
        // 构建完整的地址字符串
        String fullAddress = host;
        if (uri.getPort() > 0) {
            fullAddress += ":" + uri.getPort();
        }
        if (uri.getPath() != null) {
            fullAddress += uri.getPath();
        }
        if (uri.getQuery() != null) {
            fullAddress += "?" + uri.getQuery();
        }
        
        // 检查是否匹配白名单中的任何域名
        for (String whitelistedDomain : whitelist) {
            if (matchesPattern(host, fullAddress, whitelistedDomain)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 根据前缀匹配域名
     */
    private static boolean matchesPattern(String host, String fullAddress, String pattern) {
        if (pattern.startsWith("$r")) {
            // 正则表达式匹配
            try {
                String regex = pattern.substring(2);
                return Pattern.matches(regex, host) || Pattern.matches(regex, fullAddress);
            } catch (Exception e) {
                return false;
            }
        } else if (pattern.startsWith("$e")) {
            // 结尾匹配
            String suffix = pattern.substring(2);
            return host.endsWith(suffix) || fullAddress.endsWith(suffix);
        } else if (pattern.startsWith("$s")) {
            // 开头匹配
            String prefix = pattern.substring(2);
            return host.startsWith(prefix) || fullAddress.startsWith(prefix);
        } else if (pattern.startsWith("$c")) {
            // 包含匹配
            String substring = pattern.substring(2);
            return host.contains(substring) || fullAddress.contains(substring);
        } else {
            // 精确匹配
            return host.equals(pattern) || fullAddress.equals(pattern);
        }
    }
    
    /**
     * 获取当前白名单
     */
    public static List<String> getWhitelist() {
        if (!configLoaded) {
            loadConfig();
        }
        return new ArrayList<>(whitelist);
    }
    
    /**
     * 检查配置文件是否有效
     */
    public static boolean isConfigValid() {
        if (!configLoaded) {
            loadConfig();
        }
        return configValid;
    }
    
    /**
     * 检查是否应该记录被拦截的请求
     */
    public static boolean shouldLogBlockedRequests() {
        if (!configLoaded) {
            loadConfig();
        }
        return logBlockedRequests;
    }
} 