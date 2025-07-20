package com.fletime.nointernet;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public class NoInternet implements ModInitializer {
	public static final String MOD_ID = "nointernet";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	// 标记游戏是否已完全加载
	public static boolean loaded = false;

	@Override
	public void onInitialize() {
		// 加载配置文件
		ConfigManager.loadConfig();
		
		// 检查配置文件有效性
		if (!ConfigManager.isConfigValid()) {
			LOGGER.warn("[NoInternet] 配置文件无效或为空，将不会拦截任何网络请求");
		}
		
		// 设置自定义代理选择器来拦截网络请求
		ProxySelector.setDefault(new NoInternetProxySelector(ProxySelector.getDefault()));
		
		// 注册服务器启动完成事件
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			loaded = true;
		});
	}
	
	/**
	 * 自定义代理选择器，用于拦截网络请求
	 */
	static class NoInternetProxySelector extends ProxySelector {
		private final ProxySelector defaultSelector;

		public NoInternetProxySelector(ProxySelector defaultSelector) {
			this.defaultSelector = defaultSelector;
		}

		@Override
		public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
			this.defaultSelector.connectFailed(uri, sa, ioe);
		}

		@Override
		public List<Proxy> select(URI uri) {
			// 如果游戏已完全加载，允许所有网络请求
			if (loaded) {
				return this.defaultSelector.select(uri);
			}
			
			// 允许 socket 连接（本地连接）
			if (uri.toString().startsWith("socket")) {
				return this.defaultSelector.select(uri);
			}
			
			// 检查是否在白名单中
			if (ConfigManager.isWhitelisted(uri)) {
				return this.defaultSelector.select(uri);
			}
			
			// 根据配置决定是否记录被拦截的网络请求
			if (ConfigManager.shouldLogBlockedRequests()) {
				LOGGER.info("[NoInternet] 拦截网络请求 - " + uri);
			}
			
			// 返回一个无效的代理地址，阻止网络连接
			return Collections.singletonList(new Proxy(Proxy.Type.HTTP, 
				InetSocketAddress.createUnresolved("localhost", 12345)));
		}
	}
}