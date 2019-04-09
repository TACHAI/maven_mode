package com.server.gataway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.discovery.PatternServiceRouteMapper;
import org.springframework.context.annotation.Bean;

import com.netflix.zuul.ZuulFilter;
import com.server.gataway.filter.AccessFilter;

@EnableZuulProxy
@SpringBootApplication
public class GatewayApplication {

	@Bean
	public ZuulFilter accessFilter(){
		return new AccessFilter();
	}
	
	/*@Bean
	public PatternServiceRouteMapper serviceRouteMapper() {
	return new PatternServiceRouteMapper(
			"(?<name>^.+)-(?<version>v.+$)",
			"${version}/${name}");
	}*/
	
	@RefreshScope
	@ConfigurationProperties("zuul")
	public ZuulProperties zuulProperties() {
		return new ZuulProperties();
	}
	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

}
