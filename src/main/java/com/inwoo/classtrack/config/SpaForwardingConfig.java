package com.inwoo.classtrack.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * SPA 딥링크 처리.
 *
 * <p>Vue Router 는 history 모드라 주소가 {@code /courses/1} 처럼 보인다. 브라우저에서
 * 화면을 이동할 때는 문제없지만, 그 주소로 <b>새로고침</b>하면 브라우저가 서버에
 * {@code GET /courses/1} 을 보낸다. 서버에는 그런 파일이 없으므로 404 가 난다.
 *
 * <p>정적 파일이 실제로 있으면 그걸 주고, 없으면 index.html 을 돌려준다.
 * 그러면 브라우저가 앱을 띄우고 Vue Router 가 주소를 보고 알맞은 화면을 그린다.
 *
 * <p>단, {@code /api} 와 {@code /actuator} 는 예외다. 없는 API 를 불렀을 때
 * index.html(HTML)이 오면 프론트가 JSON 파싱에 실패해서 원인을 찾기 어려워진다.
 * 그쪽은 그대로 404 로 둔다.
 */
@Configuration
public class SpaForwardingConfig implements WebMvcConfigurer {

    private static final String INDEX = "static/index.html";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {

                    @Override
                    protected Resource getResource(String resourcePath, Resource location)
                            throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }

                        if (resourcePath.startsWith("api/") || resourcePath.startsWith("actuator/")) {
                            return null;
                        }

                        // 개발 중에는 static/ 이 비어 있다 (프론트는 Vite 가 서빙)
                        Resource index = new ClassPathResource(INDEX);
                        return index.exists() ? index : null;
                    }
                });
    }
}
