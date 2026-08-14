package com.inwoo.classtrack.dev;

import com.inwoo.classtrack.aspect.LogExecution;
import com.inwoo.classtrack.config.CorsProperties;
import com.inwoo.classtrack.dev.DevOverview.AttributeInfo;
import com.inwoo.classtrack.dev.DevOverview.EndpointInfo;
import com.inwoo.classtrack.dev.DevOverview.EntityInfo;
import com.inwoo.classtrack.dev.DevOverview.RuntimeInfo;
import com.inwoo.classtrack.dev.DevOverview.ServiceMethodInfo;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Table;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootVersion;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 실행 중인 애플리케이션 자신을 들여다봐서 구현 현황을 만든다.
 *
 * <p>표를 손으로 관리하지 않는 이유: 코드가 바뀌면 표는 곧 거짓말이 된다.
 * Spring 은 자기가 등록한 핸들러를, JPA 는 자기 엔티티 구조를 이미 알고 있으므로
 * 그대로 꺼내 쓴다.
 */
@Service
public class DevInfoService {

    /** 우리 코드만 본다. actuator 같은 프레임워크 엔드포인트는 뺀다. */
    private static final String BASE_PACKAGE = "com.inwoo.classtrack";

    /** LoggingAspect 의 포인트컷과 같은 규칙 */
    private static final String SERVICE_PACKAGE = BASE_PACKAGE + ".service";
    private static final String LOG_SERVICE = "LogService";

    /**
     * 화면에 늘어놓는 순서. 알파벳순이 아니라 <b>도메인 중요도 순</b>이다.
     * Course 가 Assignment 를 담고 있으므로 항상 먼저 나와야 읽기 편하다.
     * 목록에 없는 것은 뒤로 밀리고 그들끼리 이름순.
     */
    private static final List<String> RESOURCE_ORDER = List.of(
            "courses", "assignments", "calendar", "dashboard", "subjects", "logs",
            "developer", "dev");

    private static final List<String> DOMAIN_ORDER = List.of("Course", "Assignment");

    private static int rankOf(List<String> order, String value) {
        int index = order.indexOf(value);
        return index == -1 ? order.size() : index;
    }

    /** "/api/courses/{courseId}/assignments" -> "courses" */
    private static String groupOf(String path) {
        String[] parts = path.split("/");
        return parts.length > 2 ? parts[2] : "";
    }

    private final RequestMappingHandlerMapping handlerMapping;
    private final ApplicationContext applicationContext;
    private final EntityManagerFactory entityManagerFactory;
    private final Environment environment;
    private final CorsProperties cors;

    /**
     * actuator 가 {@code controllerEndpointHandlerMapping} 이라는 같은 타입 Bean 을 하나 더
     * 등록하므로, 우리 컨트롤러를 담고 있는 쪽을 이름으로 지정해야 한다.
     */
    public DevInfoService(
            @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
            ApplicationContext applicationContext,
            EntityManagerFactory entityManagerFactory,
            Environment environment,
            CorsProperties cors) {
        this.handlerMapping = handlerMapping;
        this.applicationContext = applicationContext;
        this.entityManagerFactory = entityManagerFactory;
        this.environment = environment;
        this.cors = cors;
    }

    public DevOverview build() {
        return new DevOverview(runtime(), endpoints(), serviceMethods(), entities());
    }

    // ------------------------------------------------------------------
    // 실행 환경 — 설정 파일이 아니라 지금 적용된 실제 값
    // ------------------------------------------------------------------

    private RuntimeInfo runtime() {
        String[] active = environment.getActiveProfiles();
        List<String> profiles = List.of(
                active.length > 0 ? active : environment.getDefaultProfiles());

        return new RuntimeInfo(
                profiles,
                environment.getProperty("local.server.port",
                        environment.getProperty("server.port", "-")),
                System.getProperty("java.version"),
                SpringBootVersion.getVersion(),
                cors.allowedOrigins(),
                new ClassPathResource("static/index.html").exists(),
                databaseHost(),
                environment.getProperty("spring.jpa.hibernate.ddl-auto", "-"));
    }

    /**
     * 크리덴셜이 섞이지 않도록 호스트와 DB 이름만 뽑는다.
     *
     * <p>운영에서 이 화면을 켜둘 수도 있으므로, local 이 아니면 호스트를 가린다.
     * 비밀은 아니지만 굳이 공개할 이유도 없다.
     */
    private String databaseHost() {
        String url = environment.getProperty("spring.datasource.url", "");
        if (url.isBlank()) {
            return "-";
        }
        String stripped = url.replaceFirst("^jdbc:postgresql://", "");
        int query = stripped.indexOf('?');
        String hostAndDb = query == -1 ? stripped : stripped.substring(0, query);

        if (environment.matchesProfiles("local")) {
            return hostAndDb;
        }
        // 호스트는 가리고 DB 이름만 남긴다
        int slash = hostAndDb.indexOf('/');
        return slash == -1 ? "(감춤)" : "(감춤)" + hostAndDb.substring(slash);
    }

    // ------------------------------------------------------------------
    // 엔드포인트 — Spring 의 핸들러 매핑에서
    // ------------------------------------------------------------------

    private List<EndpointInfo> endpoints() {
        List<EndpointInfo> result = new ArrayList<>();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry
                : handlerMapping.getHandlerMethods().entrySet()) {

            HandlerMethod handler = entry.getValue();
            Class<?> controller = handler.getBeanType();
            if (!controller.getPackageName().startsWith(BASE_PACKAGE)) {
                continue;
            }

            RequestMappingInfo mapping = entry.getKey();
            List<String> paths = pathsOf(mapping);
            List<String> httpMethods = mapping.getMethodsCondition().getMethods().stream()
                    .map(Enum::name)
                    .toList();

            // 메서드를 지정하지 않은 매핑은 모든 메서드를 받는다
            if (httpMethods.isEmpty()) {
                httpMethods = List.of("ANY");
            }

            ApiDescription description = handler.getMethodAnnotation(ApiDescription.class);

            for (String path : paths) {
                for (String httpMethod : httpMethods) {
                    result.add(new EndpointInfo(
                            groupOf(path),
                            httpMethod,
                            path,
                            controller.getSimpleName(),
                            handler.getMethod().getName(),
                            description == null ? null : description.value(),
                            annotationsOf(handler)));
                }
            }
        }

        // 리소스 묶음 → 경로 → 메서드 순. 같은 리소스의 API 가 붙어 나온다.
        result.sort(Comparator
                .comparingInt((EndpointInfo e) -> rankOf(RESOURCE_ORDER, e.group()))
                .thenComparing(EndpointInfo::group)
                .thenComparing(EndpointInfo::path)
                .thenComparing(EndpointInfo::httpMethod));
        return result;
    }

    private static List<String> pathsOf(RequestMappingInfo mapping) {
        if (mapping.getPathPatternsCondition() != null) {
            return mapping.getPathPatternsCondition().getPatternValues().stream().sorted().toList();
        }
        return List.of("(unknown)");
    }

    /** 화면에 보여줄 만한 애노테이션만 골라낸다. 매핑 애노테이션은 이미 표에 있으니 제외. */
    private static List<String> annotationsOf(HandlerMethod handler) {
        List<String> names = new ArrayList<>();

        if (handler.getMethodAnnotation(LogExecution.class) != null) {
            names.add("@LogExecution");
        }
        if (AnnotatedElementUtils.hasAnnotation(handler.getMethod(), Transactional.class)) {
            names.add("@Transactional");
        }
        return names;
    }

    // ------------------------------------------------------------------
    // 서비스 메서드 — 트랜잭션과 AOP 적용 여부
    // ------------------------------------------------------------------

    private List<ServiceMethodInfo> serviceMethods() {
        List<ServiceMethodInfo> result = new ArrayList<>();

        for (Object bean : applicationContext.getBeansWithAnnotation(Service.class).values()) {
            // 프록시가 아니라 원본 클래스를 봐야 애노테이션이 보인다
            Class<?> target = AopUtils.getTargetClass(bean);
            if (!target.getPackageName().startsWith(SERVICE_PACKAGE)) {
                continue;
            }

            boolean logged = !target.getSimpleName().equals(LOG_SERVICE);

            for (Method method : target.getDeclaredMethods()) {
                if (!Modifier.isPublic(method.getModifiers()) || method.isSynthetic()) {
                    continue;
                }

                Transactional annotation = findTransactional(method, target);

                result.add(new ServiceMethodInfo(
                        target.getSimpleName(),
                        method.getName(),
                        annotation != null,
                        annotation != null && annotation.readOnly(),
                        logged));
            }
        }

        // CourseService 를 먼저. 도메인 순서를 클래스 이름 앞부분으로 판단한다.
        result.sort(Comparator
                .comparingInt((ServiceMethodInfo s) -> domainRank(s.serviceClass()))
                .thenComparing(ServiceMethodInfo::serviceClass)
                .thenComparing(ServiceMethodInfo::method));
        return result;
    }

    /** 메서드에 없으면 클래스 레벨을 본다 — Spring 이 트랜잭션을 찾는 순서와 같다. */
    private static Transactional findTransactional(Method method, Class<?> target) {
        Transactional onMethod =
                AnnotatedElementUtils.findMergedAnnotation(method, Transactional.class);
        return onMethod != null
                ? onMethod
                : AnnotatedElementUtils.findMergedAnnotation(target, Transactional.class);
    }

    // ------------------------------------------------------------------
    // 엔티티 — JPA Metamodel 에서
    // ------------------------------------------------------------------

    private List<EntityInfo> entities() {
        List<EntityInfo> result = new ArrayList<>();

        for (EntityType<?> entityType : entityManagerFactory.getMetamodel().getEntities()) {
            Class<?> javaType = entityType.getJavaType();
            Table table = javaType.getAnnotation(Table.class);

            List<AttributeInfo> attributes = new ArrayList<>();
            for (Attribute<?, ?> attribute : entityType.getAttributes()) {
                attributes.add(toAttributeInfo(attribute));
            }
            attributes.sort(Comparator.comparing(AttributeInfo::id).reversed()
                    .thenComparing(AttributeInfo::name));

            result.add(new EntityInfo(
                    entityType.getName(),
                    table == null ? javaType.getSimpleName().toLowerCase() : table.name(),
                    attributes));
        }

        result.sort(Comparator
                .comparingInt((EntityInfo e) -> rankOf(DOMAIN_ORDER, e.name()))
                .thenComparing(EntityInfo::name));
        return result;
    }

    /** CourseService -> Course 로 보고 도메인 순서를 매긴다. */
    private static int domainRank(String className) {
        for (int i = 0; i < DOMAIN_ORDER.size(); i++) {
            if (className.startsWith(DOMAIN_ORDER.get(i))) {
                return i;
            }
        }
        return DOMAIN_ORDER.size();
    }

    private static AttributeInfo toAttributeInfo(Attribute<?, ?> attribute) {
        boolean isId = attribute instanceof SingularAttribute<?, ?> singular && singular.isId();
        boolean optional = attribute instanceof SingularAttribute<?, ?> singular2
                && singular2.isOptional();

        return new AttributeInfo(
                attribute.getName(),
                attribute.getJavaType().getSimpleName(),
                attribute.getPersistentAttributeType().name(),
                isId,
                optional,
                attribute.isAssociation() ? attribute.getJavaType().getSimpleName() : null);
    }
}
